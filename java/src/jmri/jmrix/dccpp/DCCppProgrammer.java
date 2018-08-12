package jmri.jmrix.dccpp;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Programmer support for DCC++.
 * <P>
 * The read operation state sequence is:
 * <UL>
 * <LI>Send Register Mode / Paged mode /Direct Mode read request
 * <LI>Wait for results reply, interpret
 * </UL>
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

    public DCCppProgrammer(DCCppTrafficController tc) {
        // error if more than one constructed?

        _controller = tc;

        // connect to listen
        controller().addDCCppListener(DCCppInterface.CS_INFO
                | DCCppInterface.COMMINFO
                | DCCppInterface.INTERFACE,
                this);

        setMode(ProgrammingMode.DIRECTBYTEMODE);
    }

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        //ret.add(ProgrammingMode.PAGEMODE);
        ret.add(ProgrammingMode.DIRECTBITMODE);
        ret.add(ProgrammingMode.DIRECTBYTEMODE);
        //ret.add(ProgrammingMode.REGISTERMODE);
        return ret;
    }

    /**
     * Can we read from a specific CV in the specified mode? Answer may not be
     * correct if the command station type and version sent by the command
     * station mimics one of the known command stations.
     */
    @Override
    public boolean getCanRead(String addr) {
        if (log.isDebugEnabled()) {
            log.debug("check mode " + getMode() + " CV " + addr);
        }
        if (!getCanRead()) {
            return false; // check basic implementation first
        }
        if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
     return Integer.parseInt(addr) <= DCCppConstants.MAX_DIRECT_CV;
        } else {
            return Integer.parseInt(addr) <= 256;
        }
    }

    /**
     * Can we write to a specific CV in the specified mode? Answer may not be
     * correct if the command station type and version sent by the command
     * station mimics one of the known command stations.
     */
    @Override
    public boolean getCanWrite(String addr) {
        if (log.isDebugEnabled()) {
            log.debug("check CV " + addr);
        }
        log.debug("cs Type: " + controller().getCommandStation().getBaseStationType() + " CS Version: " + controller().getCommandStation().getCodeBuildDate());
        if (!getCanWrite()) {
            return false; // check basic implementation first
        }
        if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
     return Integer.parseInt(addr) <= DCCppConstants.MAX_DIRECT_CV;
        } else {
            return Integer.parseInt(addr) <= 256;
        }
    }

    /**
     * 
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

    @Override
    synchronized public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("writeCV " + CV + " listens " + p);
        }
        useProgrammer(p);
        _progRead = false;
        // set new state & save values
        progState = REQUESTSENT;
        _val = val;
        _cv = 0xffff & CV;

        //try {
            // start the error timer
            restartTimer(DCCppProgrammerTimeout);

            // format and send message to go to program mode
            if (getMode().equals(ProgrammingMode.PAGEMODE)) {
                //DCCppMessage msg = DCCppMessage.getWritePagedCVMsg(CV, val);
                //controller().sendDCCppMessage(msg, this);
            } else if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
                DCCppMessage msg = DCCppMessage.makeWriteDirectCVMsg(CV, val);
                controller().sendDCCppMessage(msg, this);
            } else { // register mode by elimination 
                //DCCppMessage msg = DCCppMessage.getWriteRegisterMsg(registerFromCV(CV), val);
                //controller().sendDCCppessage(msg, this);
            }
     //} catch (jmri.ProgrammerException e) {
            //progState = NOTPROGRAMMING;
            //throw e;
     //}
    }

    @Override
    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    @Override
    synchronized public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("readCV " + CV + " listens " + p);
        }
        // If can't read (e.g. multiMaus CS), this shouldnt be invoked, but
        // still we need to do something rational by returning a NotImplemented error
        if (!getCanRead()) {
            p.programmingOpReply(CV, jmri.ProgListener.NotImplemented);
            return;
        }
        useProgrammer(p);
        _cv = 0xffff & CV;
        _progRead = true;
        // set new state
        progState = REQUESTSENT;
        //try {
            // start the error timer
            restartTimer(DCCppProgrammerTimeout);

            // format and send message to go to program mode
            if (getMode().equals(ProgrammingMode.PAGEMODE)) {
                //DCCppMessage msg = DCCppMessage.getReadPagedCVMsg(CV);
                //controller().sendDCCppMessage(msg, this);
            } else if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
                DCCppMessage msg = DCCppMessage.makeReadDirectCVMsg(CV);
                controller().sendDCCppMessage(msg, this);
            } else { // register mode by elimination    
                //DCCppMessage msg = DCCppMessage.getReadRegisterMsg(registerFromCV(CV));
                //controller().sendDCCppMessage(msg, this);
            }
     //} catch (jmri.ProgrammerException e) {
            //progState = NOTPROGRAMMING;
            //throw e;
     //}

    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isInfoEnabled()) {
                log.info("programmer already in use by " + _usingProgrammer);
            }
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
            return;
        }
    }

    @Override
    synchronized public void message(DCCppReply m) {
 if (progState == NOTPROGRAMMING) {
     return;
 }
 if (m.getElement(0) == DCCppConstants.PROGRAM_REPLY) {
            if (log.isDebugEnabled()) {
                log.debug("reply in REQUESTSENT state");
            }
     log.debug("DCC++ Programming Reply value = {}", m.getCVString());
     // CALLBACKNUM = mt.group(1)
     // CALLBACKSUB = mt.group(2)
     _val = m.getReadValueInt();
     progState = NOTPROGRAMMING;
            if (_val == -1) {
                log.debug("Reporting NoAck");
                notifyProgListenerEnd(_val, jmri.ProgListener.NoAck);
            } else {
                log.debug("Reporting OK");
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            }
 }
    }

    // listen for the messages to the LI100/LI101
    @Override
    synchronized public void message(DCCppMessage l) {
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }


    /*
     * Indicate when the Programmer is in the middle of an operation.
     */
    public boolean programmerBusy() {
        return (progState != NOTPROGRAMMING);
    }

    /**
     * Internal routine to handle a timeout
     */
    @Override
    synchronized protected void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            if (log.isDebugEnabled()) {
                log.debug("timeout!");
            }
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            if (getCanRead()) {
                notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
            } else {
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            }
        }
    }

    // internal method to notify of the final result
    protected void notifyProgListenerEnd(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("notifyProgListenerEnd value " + value + " status " + status);
        }
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        jmri.ProgListener temp = _usingProgrammer;
        _usingProgrammer = null;
        temp.programmingOpReply(value, status);
    }

    DCCppTrafficController _controller = null;

    protected DCCppTrafficController controller() {
        return _controller;
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppProgrammer.class);

}
