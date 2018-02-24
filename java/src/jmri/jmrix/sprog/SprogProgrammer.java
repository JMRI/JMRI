package jmri.jmrix.sprog;

import java.util.*;
import jmri.*;
import jmri.jmrix.AbstractProgrammer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement the jmri.Programmer interface via commands for the Sprog
 * programmer. This provides a service mode programmer.
 *
 * @author Bob Jacobsen Copyright (C) 2001
  */
public class SprogProgrammer extends AbstractProgrammer implements SprogListener {

    private SprogSystemConnectionMemo _memo = null;

    public SprogProgrammer(SprogSystemConnectionMemo memo) {
         _memo = memo;
    }

    /**
     * Implemented Types.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(ProgrammingMode.DIRECTBITMODE);
        ret.add(ProgrammingMode.PAGEMODE);
        return ret;
    }

    @Override
    public boolean getCanRead() {
        if (getMode().equals(ProgrammingMode.PAGEMODE)) return true;
        else if (getMode().equals(ProgrammingMode.DIRECTBITMODE)) return true;
        else {
            log.error("Unknown internal mode {} returned true from getCanRead()",getMode());
            return true;
        }
    }

    // members for handling the programmer interface
    int progState = 0;
    static final int NOTPROGRAMMING = 0;    // is notProgramming
    static final int COMMANDSENT = 2;       // read/write command sent, waiting reply
    int _val;	// remember the value being read/written for confirmative reply

    // programming interface
    @Override
    synchronized public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("writeCV " + CV + " mode " + getMode() + " listens " + p);
        }
        useProgrammer(p);
        _val = val;
        startProgramming(_val, CV);
    }

    @Override
    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    @Override
    synchronized public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("readCV " + CV + " mode " + getMode() + " listens " + p);
        }
        useProgrammer(p);
        _val = -1;
        startProgramming(_val, CV);
    }

    private jmri.ProgListener _usingProgrammer = null;

    /**
     * Send the command to start programming operation.
     * 
     * @param val   Value to be written, or -1 for read
     * @param CV    CV to read/write
     */
    private void startProgramming(int val, int CV) {
        // here ready to send the read/write command
        progState = COMMANDSENT;
        // see why waiting
        try {
            startLongTimer();
            controller().sendSprogMessage(progTaskStart(getMode(), val, CV), this);
        } catch (Exception e) {
            // program op failed, go straight to end
            log.error("program operation failed, exception " + e);
            progState = NOTPROGRAMMING;
        }
    }

    /**
     * Internal method to remember who's using the programmer.
     */
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

    /**
     * Internal method to create the SprogMessage for programmer task start.
     */
    protected SprogMessage progTaskStart(ProgrammingMode mode, int val, int cvnum) {
        // val = -1 for read command; mode is direct, etc
        if (val < 0) {
            return SprogMessage.getReadCV(cvnum, mode);
        } else {
            return SprogMessage.getWriteCV(cvnum, val, mode);
        }
    }

    @Override
    public void notifyMessage(SprogMessage m) {
    }

    @Override
    synchronized public void notifyReply(SprogReply reply) {

        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            log.debug("reply in NOTPROGRAMMING state" + " [" + reply + "]");
            return;
        } else if (progState == COMMANDSENT) {
            log.debug("reply in COMMANDSENT state" + " [" + reply + "]");
            // operation done, capture result, then have to leave programming mode
            progState = NOTPROGRAMMING;
            // check for errors
            if (reply.match("No Ack") >= 0) {
                log.debug("handle No Ack reply " + reply);
                // perhaps no loco present? Fail back to end of programming
                progState = NOTPROGRAMMING;
                notifyProgListenerEnd(-1, jmri.ProgListener.NoLocoDetected);
            } else if (reply.match("!O") >= 0) {
                log.debug("handle !O reply " + reply);
                // Overload. Fail back to end of programming
                progState = NOTPROGRAMMING;
                notifyProgListenerEnd(-1, jmri.ProgListener.ProgrammingShort);
            } else {
                // see why waiting
                if (_val == -1) {
                    // read was in progress - get return value
                    _val = reply.value();
                }
                progState = NOTPROGRAMMING;
                stopTimer();
                // if this was a read, we cached the value earlier.  If its a
                // write, we're to return the original write value
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            }

            // SPROG always leaves power off after programming so we inform the
            // power manager of the new state
            controller().getAdapterMemo().getPowerManager().notePowerState(PowerManager.OFF);
        } else {
            log.debug("reply in un-decoded state");
        }
    }

    /**
     * Internal routine to handle a timeout
     */
    @Override
    synchronized protected void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            log.debug("Timeout in a programming state");
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        } else {
            log.debug("timeout in NOTPROGRAMMING state");
        }
    }

    // internal method to notify of the final result
    protected void notifyProgListenerEnd(int value, int status) {
        log.debug("notifyProgListenerEnd value " + value + " status " + status);
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        if (_usingProgrammer == null) {
            log.error("No listener to notify");
        } else {
            jmri.ProgListener temp = _usingProgrammer;
            _usingProgrammer = null;
            temp.programmingOpReply(value, status);
        }
    }

    SprogTrafficController _controller = null;

    protected SprogTrafficController controller() {
        // connect the first time
        if (_controller == null) {
            _controller = _memo.getSprogTrafficController();
        }
        return _controller;
    }

    private final static Logger log = LoggerFactory.getLogger(SprogProgrammer.class);

}
