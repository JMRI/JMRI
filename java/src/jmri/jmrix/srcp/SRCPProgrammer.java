package jmri.jmrix.srcp;

import java.util.ArrayList;
import java.util.List;
import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the jmri.Programmer interface via commands for the SRCP
 * powerstation
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 */
public class SRCPProgrammer extends AbstractProgrammer implements SRCPListener {

    protected SRCPBusConnectionMemo _memo = null;
    private int _bus;

    public SRCPProgrammer(SRCPBusConnectionMemo memo) {
        _bus = memo.getBus();
        _memo = memo;
        // need a longer LONG_TIMEOUT
        LONG_TIMEOUT = 180000;
    }

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<>();
        ret.add(ProgrammingMode.DIRECTBYTEMODE);
        ret.add(ProgrammingMode.REGISTERMODE);
        return ret;
    }

    // members for handling the programmer interface
    int progState = 0;
    static final int NOTPROGRAMMING = 0;// is notProgramming
    static final int COMMANDSENT = 2; 	// read/write command sent, waiting reply
    boolean _progRead = false;
    boolean _progConfirm = false;
    int _confirmVal;  // remember the value to be confirmed for reply
    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    @Override
    synchronized public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("writeCV " + CV + " listens " + p);
        }
        useProgrammer(p);
        _progRead = false;
        _progConfirm = false;
        // set commandPending state
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;

        try {
            SRCPMessage m;
            // start the error timer
            startLongTimer();

            // write
            if (getMode() == ProgrammingMode.DIRECTBYTEMODE) {
                m = SRCPMessage.getWriteDirectCV(_bus, _cv, _val);
            } else {
                m = SRCPMessage.getWriteRegister(_bus, registerFromCV(_cv), _val);
            }
            // format and send the write message
            controller().sendSRCPMessage(m, this);
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }
    }

    @Override
    synchronized public void confirmCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        int CV = Integer.parseInt(CVname);
        if (log.isDebugEnabled()) {
            log.debug("confirmCV " + CV + " val " + val + " listens " + p);
        }
        useProgrammer(p);
        _progRead = false;
        _progConfirm = true;

        progState = COMMANDSENT;
        _cv = CV;
        _confirmVal = val;

        try {
            SRCPMessage m;
            // start the error timer
            startLongTimer();

            if (getMode() == ProgrammingMode.DIRECTBYTEMODE) {
                m = SRCPMessage.getConfirmDirectCV(_bus, _cv, _confirmVal);
            } else {
                m = SRCPMessage.getConfirmRegister(_bus, registerFromCV(_cv), _confirmVal);
            }

            // format and send the confirm message
            controller().sendSRCPMessage(m, this);
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }

        //readCV(CV, p);
    }

    @Override
    synchronized public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("readCV " + CV + " listens " + p);
        }
        useProgrammer(p);
        _progRead = true;
        _progConfirm = false;

        progState = COMMANDSENT;
        _cv = CV;

        try {
            SRCPMessage m;
            // start the error timer
            startLongTimer();

            // format and send the write message
            if (getMode() == ProgrammingMode.DIRECTBYTEMODE) {
                m = SRCPMessage.getReadDirectCV(_bus, _cv);
            } else {
                m = SRCPMessage.getReadRegister(_bus, registerFromCV(_cv));
            }

            controller().sendSRCPMessage(m, this);
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }

    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isDebugEnabled()) {
                log.debug("programmer already in use by " + _usingProgrammer);
            }
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
        }
    }

    @Override
    public void message(SRCPMessage m) {
        log.error("message received unexpectedly: " + m.toString());
    }

    @Override
    synchronized public void reply(SRCPReply m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            if (log.isDebugEnabled()) {
                log.debug("reply in NOTPROGRAMMING state");
            }
            if (!m.isResponseOK()) {
                log.warn("Reply \"" + m.toString() + "\"");
            }
        } else if (progState == COMMANDSENT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in COMMANDSENT state");
            }
            // operation done, capture result, then have to leave programming mode
            progState = NOTPROGRAMMING;
            // check for errors
            if (!m.isResponseOK()) {
                if (log.isDebugEnabled()) {
                    log.debug("handle error reply " + m);
                }
                log.warn("Reply \"" + m.toString() + "\"");
                if (_progConfirm && m.getResponseCode().equals("412")) {
                    // handle the Verify return message "412 ERROR wrong value"
                    notifyProgListenerEnd(_val, jmri.ProgListener.ConfirmFailed);
                    return;
                }
                // perhaps no loco present? Fail back to end of programming
                notifyProgListenerEnd(-1, jmri.ProgListener.NoLocoDetected);
            } else {
                // see why waiting
                if (_progRead) {
                    // read was in progress - get return value
                    _val = m.value();
                }
                if (_progConfirm) {
                    _val = _confirmVal;
                }
                // If this was a read or verify, we retreived the value above. 
                // If its a write, we're to return the original write value.
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            }
        }
    }

    @Override
    synchronized public void reply(jmri.jmrix.srcp.parser.SimpleNode n) {
        if (log.isDebugEnabled()) {
            log.debug("reply called with simpleNode " + n.jjtGetValue());
        }
        if (n.jjtGetChild(3) instanceof jmri.jmrix.srcp.parser.ASTsm) {
            reply(new SRCPReply(n));
        }
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
            cleanup();
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    /**
     * Internal method to send a cleanup message (if needed) on timeout.
     * <P>
     * Here, it sends a request to exit from programming mode. But subclasses
     * may redefine that.
     */
    void cleanup() {
        controller().sendSRCPMessage(SRCPMessage.getExitProgMode(_bus), this);
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

    SRCPTrafficController _controller = null;

    protected SRCPTrafficController controller() {
        // connect the first time
        if (_controller == null) {
            _controller = _memo.getTrafficController();
        }
        return _controller;
    }

    private final static Logger log = LoggerFactory.getLogger(SRCPProgrammer.class);

}
