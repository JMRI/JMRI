package jmri.jmrix.bidib;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.Programmer;

import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;
import org.bidib.jbidibc.core.DefaultMessageListener;
import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.messages.enums.CommandStationProgState;
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.messages.enums.BoosterControl;
import org.bidib.jbidibc.messages.enums.BoosterState;
import org.bidib.jbidibc.messages.enums.CommandStationPt;
import org.bidib.jbidibc.messages.message.BidibCommandMessage;
import org.bidib.jbidibc.messages.message.CommandStationProgMessage;
import org.bidib.jbidibc.messages.utils.NodeUtils;

/**
 * Convert the jmri.Programmer interface into BiDiB.
 * <P>
 * This has two states: NOTPROGRAMMING, and COMMANDSENT. The transitions to and
 * from programming mode are now handled in the TrafficController code.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2016
 * @author Eckart Meyer Copyright (C) 2019-2023
 */
public class BiDiBProgrammer extends AbstractProgrammer {

    protected BiDiBTrafficController tc;
    protected Node progNode; //the BiDiB progNode to sent the MSG_CS_PROG message to
    private boolean isBoosterOn = false;

//    @SuppressWarnings("OverridableMethodCallInConstructor")
    public BiDiBProgrammer(BiDiBTrafficController tc) {
        this.tc = tc;
        super.SHORT_TIMEOUT = 4000;
        progNode = tc.getCurrentGlobalProgrammerNode();
        log.debug("global programmer node: {}", progNode);

        if (getSupportedModes().size() > 0) {
            setMode(getSupportedModes().get(0));
        }
        
        createProgrammerListener();
    }

    /** 
     * {@inheritDoc}
     *
     * BiDiB programming modes available depend on settings
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<>();
        if (tc == null) {
            log.warn("getSupportedModes called with null tc", new Exception("traceback"));
        }
        java.util.Objects.requireNonNull(tc, "TrafficController reference needed");

        ret.add(ProgrammingMode.DIRECTBYTEMODE);
        //ret.add(ProgrammingMode.DIRECTBITMODE); //TODO! BiDiB should be able to do this!
        return ret;
    }
    
    // getCanRead/getCanWrite: BiDiB protocol allows CVs from 1...1024 - this is the default implementation

    /** 
     * {@inheritDoc}
     * 
     * The default implementation does not check for cv &gt; 1024 - not neccessary? We do it here anywhere
     */
    @Override
    public boolean getCanWrite(String cv) {
        if (!getCanWrite()) {
            return false; // check basic implementation first
        }
        return Integer.parseInt(cv) <= 1024;
    }

    /** 
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Programmer.WriteConfirmMode getWriteConfirmMode(String addr) {
        return WriteConfirmMode.DecoderReply;
    }

    // members for handling the programmer interface
    int progState = 0;
    static final int NOTPROGRAMMING = 0;// is notProgramming
    static final int COMMANDSENT = 2;  // read/write command sent, waiting reply
    static final int COMMANDSENT_2 = 4; // ops programming mode, send msg twice
    boolean _progRead = false;
    int _val; // remember the value being read/written for confirmative reply
    int _cv; // remember the cv being read/written

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void writeCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.info("write mode: {}, CV={}, val={}", getMode().getStandardName(), CV, val);
        if (log.isDebugEnabled()) {
            log.debug("writeCV {} listens {}", CV, p);
        }
        useProgrammer(p);
        if (!getCanWrite(CVname)) {
            throw new jmri.ProgrammerException("CV number not supported");
        }
        if (progNode == null) {
            throw new jmri.ProgrammerException("No Global Programmer node found!");
        }
        _progRead = false;
        // set state
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;

//TODO bit mode ??
        sendBiDiBMessage(new CommandStationProgMessage(CommandStationPt.BIDIB_CS_PROG_WR_BYTE, _cv, _val));
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void readCV(String CVname, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.info("read mode: {}, CV={}", getMode().getStandardName(), CV);
        if (log.isDebugEnabled()) {
            log.debug("readCV {} listens {}", CV, p);
        }
        useProgrammer(p);
        if (!getCanRead(CVname)) {
            throw new jmri.ProgrammerException("CV number not supported");
        }
        _progRead = true;

        // set commandPending state
        progState = COMMANDSENT;
        _cv = CV;

//TODO bit mode ??
        sendBiDiBMessage(new CommandStationProgMessage(CommandStationPt.BIDIB_CS_PROG_RD_BYTE, _cv, 0));
    }
    
    private void sendBiDiBMessage(BidibCommandMessage message) {
        progNode = tc.getCurrentGlobalProgrammerNode(); //the global programmer progNode may have changed TODO: make the progNode user selectable!
        if (progNode != null) {
            if (isBoosterOn) {
                startLongTimer();
                tc.sendBiDiBMessage(message, progNode);
            }
            else {
                // if the booster of OFF, return immediately without waiting for the timeout.
                progState = NOTPROGRAMMING;
                notifyProgListenerEnd(_val, jmri.ProgListener.NoAck);
            }
        }
        else {
            progState = NOTPROGRAMMING;
            notifyProgListenerEnd(_val, jmri.ProgListener.NotImplemented);
        }
    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
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
    
    
    private void createProgrammerListener() {
        // create BiDiB message listener
        MessageListener messageListener = new DefaultMessageListener() {
            //TODO implement retries somewhow...
            @Override
            public void csProgState(
                byte[] address, int messageNum, CommandStationProgState commandStationProgState, int remainingTime, int cvNumber, int cvData) {
                if (progState == NOTPROGRAMMING) {
                    // we get the complete set of replies now, so ignore these
                    if (log.isDebugEnabled()) {
                        log.debug("reply in NOTPROGRAMMING state");
                    }
                } else if (progState == COMMANDSENT) {
                    log.debug("node addr: {}, msg node addr: {}", progNode.getAddr(), address);
                    if (NodeUtils.isAddressEqual(progNode.getAddr(), address)  &&  _cv == cvNumber) {
                        log.info("GLOBAL PROGRAMMER CS_PROG_STATE was signalled, node addr: {}, state: {}, CV: {}, value: {}, remaining time: {}",
                                address, commandStationProgState.getType(), cvNumber, cvData, remainingTime);
                        if ( (commandStationProgState.getType() & 0x80) != 0) { //bit 7 = 1 means operation has finished
                            stopTimer();
                            progState = NOTPROGRAMMING;
                            if ( (commandStationProgState.getType() & 0x40) == 0) {//bit 6 = 0 means OK
                                log.debug(" prog ok");
                                if (_progRead) {
                                    // read was in progress - get return value
                                    _val = cvData;
                                }
                                // if this was a read, we retrieved the value above.  If its a
                                // write, we're to return the original write value
                                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                            }
                            else { //not ok - return error
                                if (commandStationProgState == CommandStationProgState.PROG_NO_LOCO ) {
                                    log.debug(" error: no loco detected");
                                    notifyProgListenerEnd(_val, jmri.ProgListener.NoLocoDetected);
                                }
                                else if (commandStationProgState == CommandStationProgState.PROG_STOPPED) {
                                    log.debug(" error: user aborted");
                                    notifyProgListenerEnd(_val, jmri.ProgListener.UserAborted);
                                }
                                else if (commandStationProgState == CommandStationProgState.PROG_NO_ANSWER) {
                                    log.debug(" error: no answer");
                                    // hack for BiDiB simulator - it does not report CV8 (manufacturer) and CV7 (decoder version)
                                    // JMRI identify needs them, so we use return CV8=238 (NMRA Reserved) and CV7=42 (you know...)
                                    if ( _progRead  &&  (cvNumber == 8 || cvNumber == 7)) {
                                        //if (cvNumber == 8) _val = 238;
                                        //if (cvNumber == 7) _val = 42;
                                        if (cvNumber == 8) _val = 145;
                                        if (cvNumber == 7) _val = 26;
                                        notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                                    }
                                    else {
                                        _val = 0;
                                        log.warn(" error: no answer, CV probably not implemented");
                                        notifyProgListenerEnd(_val, jmri.ProgListener.NoAck);
                                        //notifyProgListenerEnd(_val, jmri.ProgListener.NotImplemented);
                                        //notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                                    }
                                }
                                else if (commandStationProgState == CommandStationProgState.PROG_SHORT) {
                                    log.warn(" error: programming short");
                                    notifyProgListenerEnd(_val, jmri.ProgListener.ProgrammingShort);
                                }
                                else if (commandStationProgState == CommandStationProgState.PROG_VERIFY_FAILED) {
                                    log.warn(" error: verify failed");
                                    notifyProgListenerEnd(_val, jmri.ProgListener.ConfirmFailed);
                                }
                                else {
                                    log.warn(" error: unknown error");
                                    notifyProgListenerEnd(_val, jmri.ProgListener.UnknownError);
                                }
                            }
                        }
                        else {
                            log.debug(" not finished...");
                            // not finished - ignore so far...
                        }
                    }
                }
            }
            @Override
            public void boosterState(byte[] address, int messageNum, BoosterState state, BoosterControl control) {
                Node node = tc.getNodeByAddr(address);
                log.info("BOOSTER STATE was signalled: {}, control: {}", state.getType(), control.getType());
                if (node != null  &&  node == progNode) {
                    isBoosterOn = ((state.getType() & 0x80) == 0x80);
                }
            }
        };
        tc.addMessageListener(messageListener);        
    }

    /** 
     * {@inheritDoc}
     *
     * Internal routine to handle a timeout
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
            cleanup();
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
            
            tc.checkProgMode(false, progNode); //be sure PROG mode is switched off
            tc.setCurrentGlobalProgrammerNode(null); //invalidate, so the progNode must be evaluated again the next time
        }
    }

    // Internal method to cleanup in case of a timeout. Separate routine
    // so it can be changed in subclasses.
    void cleanup() {
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
        notifyProgListenerEnd(temp, value, status);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BiDiBProgrammer.class);

}
