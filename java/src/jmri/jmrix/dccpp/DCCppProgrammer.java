package jmri.jmrix.dccpp;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;

/**
 * Programmer support for DCC++.
 * <p>
 * The read operation state sequence is:
 * <ul>
 * <li>Send Register Mode / Paged mode /Direct Mode read request
 * <li>Wait for results reply, interpret
 * </ul>
 *
 * @author Bob Jacobsen Copyright (c) 2002, 2007
 * @author Paul Bender Copyright (c) 2003-2010
 * @author Giorgio Terdina Copyright (c) 2007
 * @author Mark Underwood Copyright (c) 2015
 */
public class DCCppProgrammer extends AbstractProgrammer implements DCCppListener {

    // NOTE: We will embed the command opcode in the CALLBACKSUB field
    // so that we can tell what type of message the response keys to.

    static protected final int DCCppProgrammerTimeout = 90000;

    // keep track of whether or not the command station is in service 
    // mode.  Used for determining if "OK" message is an aproriate 
    // response to a request to a programming request. 
    protected boolean _service_mode = false;  // TODO: Is this even meaningful for DCC++?

    static protected final int LISTENER_MASK = DCCppInterface.CS_INFO | DCCppInterface.COMMINFO | DCCppInterface.INTERFACE;

    public DCCppProgrammer(@Nonnull DCCppTrafficController tc) {
        // error if more than one constructed?
        _controller = tc;
        init();
    }

    private void init() {
        // connect to listen
        controller().addDCCppListener(LISTENER_MASK, this);
        setMode(ProgrammingMode.DIRECTBYTEMODE);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<>();
        //ret.add(ProgrammingMode.PAGEMODE);
//        ret.add(ProgrammingMode.DIRECTBITMODE);
        ret.add(ProgrammingMode.DIRECTBYTEMODE);
        //ret.add(ProgrammingMode.REGISTERMODE);
        return ret;
    }

    /** 
     * {@inheritDoc}
     *
     * Can we read from a specific CV in the specified mode? Answer may not be
     * correct if the command station type and version sent by the command
     * station mimics one of the known command stations.
     */
    @Override
    public boolean getCanRead(String addr) {
        if (log.isDebugEnabled()) {
            log.debug("check mode {} CV {}", getMode(), addr);
        }
        if (!getCanRead()) {
            return false; // check basic implementation first
        }
        if (getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
            return Integer.parseInt(addr) <= DCCppConstants.MAX_DIRECT_CV;
        } else {
            return Integer.parseInt(addr) <= 256;
        }
    }

    /** 
     * {@inheritDoc}
     *
     * Can we write to a specific CV in the specified mode? Answer may not be
     * correct if the command station type and version sent by the command
     * station mimics one of the known command stations.
     */
    @Override
    public boolean getCanWrite(String addr) {
        log.debug("check CV {}", addr);
        log.debug("cs Type: {} CS Build: {}", controller().getCommandStation().getStationType(), controller().getCommandStation().getBuild());
        if (!getCanWrite()) {
            return false; // check basic implementation first
        }
        if (getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
            return Integer.parseInt(addr) <= DCCppConstants.MAX_DIRECT_CV;
        } else {
            return Integer.parseInt(addr) <= 256;
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Programmer.WriteConfirmMode getWriteConfirmMode(String addr) { return WriteConfirmMode.DecoderReply; }

    // members for handling the programmer interface
    protected int progState = 0;
    static protected final int NOTPROGRAMMING = 0; // is notProgramming
    static protected final int REQUESTSENT = 1; // waiting reply to command to go into programming mode
    static protected final int INQUIRESENT = 2; // read/write command sent, waiting reply
    protected boolean _progRead = false;
    protected int _val; // remember the value being read/written for confirmative reply
    protected int _cv; // remember the cv being read/written

    // programming interface

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void writeCV(String CVname, int val, ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        if (log.isDebugEnabled()) {
            log.debug("writeCV {} listens {}", CV, p);
        }
        useProgrammer(p);
        _progRead = false;
        // set new state & save values
        progState = REQUESTSENT;
        _val = val;
        _cv = 0xffff & CV;

        // start the error timer
        restartTimer(DCCppProgrammerTimeout);

        // format and send message to go to program mode
        if (getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
            DCCppMessage msg = DCCppMessage.makeWriteDirectCVMsg(CV, val);
            controller().sendDCCppMessage(msg, this);
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void confirmCV(String CV, int val, ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p, val);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void readCV(String CVname, ProgListener p) throws jmri.ProgrammerException {
        readCV(CVname, p, 0); //default starting value to zero
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void readCV(String CVname, ProgListener p, int startVal) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("readCV {}, startVal {}", CV, startVal);
        // If can't read (e.g. multiMaus CS), this shouldnt be invoked, but
        // still we need to do something rational by returning a NotImplemented error
        if (!getCanRead()) {
            notifyProgListenerEnd(p,CV,ProgListener.NotImplemented);
            return;
        }
        useProgrammer(p);
        _cv = 0xffff & CV;
        _progRead = true;
        // set new state
        progState = REQUESTSENT;
        // start the error timer
        restartTimer(DCCppProgrammerTimeout);

        if (getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
            if (controller().getCommandStation().isReadStartValSupported()) { //use the 'V' command with a startVal
                DCCppMessage msg = DCCppMessage.makeVerifyCVMsg(CV, startVal);
                controller().sendDCCppMessage(msg, this);                
            } else { //use the older 'R' command
                DCCppMessage msg = DCCppMessage.makeReadDirectCVMsg(CV);
                controller().sendDCCppMessage(msg, this);
            }
        }
    }

    private ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isInfoEnabled()) {
                log.info("programmer already in use by {}", _usingProgrammer);
            }
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void message(DCCppReply m) {
        if (progState == NOTPROGRAMMING) {
            return;
        }
        if (m.getElement(0) == DCCppConstants.PROGRAM_REPLY || 
                m.getElement(0) == DCCppConstants.VERIFY_REPLY) {
            if (log.isDebugEnabled()) {
                log.debug("reply in REQUESTSENT state");
                log.debug("DCC++ Program or Verify Reply value = {}", m.getCVString());
            }
            _val = m.getReadValueInt();
            progState = NOTPROGRAMMING;
            if (_val == -1) {
                log.debug("Reporting NoAck");
                notifyProgListenerEnd(_val, ProgListener.NoAck);
            } else {
                log.debug("Reporting OK");
                notifyProgListenerEnd(_val, ProgListener.OK);
            }
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void message(DCCppMessage l) {
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        log.debug("Notified of timeout on message '{}'", msg);
    }


    /*
     * Indicate when the Programmer is in the middle of an operation.
     */
    public synchronized boolean programmerBusy() {
        return (progState != NOTPROGRAMMING);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected synchronized void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            if (log.isDebugEnabled()) {
                log.debug("timeout!");
            }
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            if (getCanRead()) {
                notifyProgListenerEnd(_val, ProgListener.FailedTimeout);
            } else {
                notifyProgListenerEnd(_val, ProgListener.OK);
            }
        }
    }

    // internal method to notify of the final result
    protected void notifyProgListenerEnd(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("notifyProgListenerEnd value {} status {}", value, status);
        }
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        jmri.ProgListener temp = _usingProgrammer;
        _usingProgrammer = null;
        notifyProgListenerEnd(temp,value,status);
    }

    private final DCCppTrafficController _controller;

    protected DCCppTrafficController controller() {
        return _controller;
    }

    public void dispose() {
        controller().removeDCCppListener(LISTENER_MASK, this);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppProgrammer.class);

}
