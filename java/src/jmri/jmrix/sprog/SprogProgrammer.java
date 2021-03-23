package jmri.jmrix.sprog;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.AbstractProgrammer;
import jmri.jmrix.sprog.update.*;

/**
 * Implement the jmri.Programmer interface via commands for the Sprog
 * programmer. This provides a service mode programmer.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Andrew Crosland Copyright (C) 2021
 */
public class SprogProgrammer extends AbstractProgrammer implements SprogListener, SprogVersionListener {

    private SprogSystemConnectionMemo _memo = null;
    private SprogVersion _sv = null;

    public SprogProgrammer(SprogSystemConnectionMemo memo) {
        _memo = memo;
    }

    /** 
     * {@inheritDoc}
     *
     * Implemented Types.
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(ProgrammingMode.DIRECTBITMODE);
        ret.add(ProgrammingMode.PAGEMODE);
        return ret;
    }

    /** 
     * {@inheritDoc}
     */
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
    int _val; // remember the value being read/written for confirmative reply

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void writeCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        if (log.isDebugEnabled()) {
            log.debug("writeCV {} mode {} listens {}", CV, getMode(), p);
        }
        useProgrammer(p);
        _val = val;
        startProgramming(_val, CV, 0);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCVWithDefault(CVname, p, 0);
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void readCV(String CVname, jmri.ProgListener p, int startVal) throws jmri.ProgrammerException {
        if (_sv != null) {
            if (_sv.supportsCVHints()) {
                // Connected hardware supports CV hint
                log.debug("Hardware supports hints");
                readCVWithDefault(CVname, p, startVal);
            } else {
                // Fallback to not using a hint
                log.debug("Hardware does not support hints");
                readCVWithDefault(CVname, p, 0);
            }
        } else {
            // The SPROG version is not known yet so request the version for later
            // and fall back to normal CV read this time
            log.debug("SPROG version is unknown");
            readCVWithDefault(CVname, p, 0);
            _memo.getSprogVersionQuery().requestVersion(this);
        }
    }

    /**
     * Internal method to read a CV with a possible default value
     * 
     * @param CVname    Index of CV to read
     * @param p         Programming listener
     * @param startVal  CV default value, Use 0 if no default available
     * @throws jmri.ProgrammerException if programming operation fails
     */
    synchronized public void readCVWithDefault(String CVname, jmri.ProgListener p, int startVal) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        if (log.isDebugEnabled()) {
            log.debug("readCV {} mode {} hint {} listens {}", CV, getMode(), startVal, p);
        }
        useProgrammer(p);
        _val = -1;
        startProgramming(_val, CV, startVal);
    }

    private jmri.ProgListener _usingProgrammer = null;

    /**
     * Send the command to start programming operation.
     * 
     * @param val       Value to be written, or -1 for read
     * @param CV        CV to read/write
     * @param startVal  Hint of what current CV value may be
     */
    private void startProgramming(int val, int CV, int startVal) {
        // here ready to send the read/write command
        progState = COMMANDSENT;
        // see why waiting
        try {
            startLongTimer();
            controller().sendSprogMessage(progTaskStart(getMode(), val, CV, startVal), this);
        } catch (Exception e) {
            // program op failed, go straight to end
            log.error("program operation failed, exception {}",e);
            progState = NOTPROGRAMMING;
        }
    }

    /**
     * Internal method to remember who's using the programmer.
     * @param p Who gets reply
     * @throws ProgrammerException when programmer in invalid state
     */
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isInfoEnabled()) {
                log.info("programmer already in use by {}", _usingProgrammer);
            }
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
            return;
        }
    }

    /**
     * Internal method to create the SprogMessage for programmer task start.
     * @param mode Mode to be used
     * @param val value to be written
     * @param cvnum CV address to write to 
     * @param startVal Hint of what the CV may contain, or 0
     * @return formatted message to do programming operation
     */
    protected SprogMessage progTaskStart(ProgrammingMode mode, int val, int cvnum, int startVal) {
        // val = -1 for read command; mode is direct, etc
        if (val < 0) {
            if (startVal == 0) {
                // No hint value, or normal starting value
                return SprogMessage.getReadCV(cvnum, mode);
            } else {
                return SprogMessage.getReadCV(cvnum, mode, startVal);
            }
        } else {
            return SprogMessage.getWriteCV(cvnum, val, mode);
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void notifyMessage(SprogMessage m) {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void notifyReply(SprogReply reply) {

        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            log.debug("reply in NOTPROGRAMMING state [{}]", reply);
            return;
        } else if (progState == COMMANDSENT) {
            log.debug("reply in COMMANDSENT state [{}]", reply);
            // operation done, capture result, then have to leave programming mode
            progState = NOTPROGRAMMING;
            // check for errors
            if (reply.match("No Ack") >= 0) {
                log.debug("handle No Ack reply {}", reply);
                // perhaps no loco present? Fail back to end of programming
                progState = NOTPROGRAMMING;
                notifyProgListenerEnd(-1, jmri.ProgListener.NoLocoDetected);
            } else if (reply.match("!O") >= 0) {
                log.debug("handle !O reply {}", reply);
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
     * Handle a SprogVersion notification.
     * <p>
     * Decode the SPROG version and populate the console gui appropriately with
     * the features applicable to the version.
     *
     * @param v The SprogVersion being handled
     */
    @Override
    synchronized public void notifyVersion(SprogVersion v) {
        // Save it for subsequent operations
        _sv = v;
        // Save it for others
        _memo.setSprogVersion(v);
        if (log.isDebugEnabled()) {
            log.debug("Found: {}", v.toString());
        }
    }

    /** 
     * {@inheritDoc}
     *
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
        log.debug("notifyProgListenerEnd value {} status {}", value, status);
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        jmri.ProgListener temp = _usingProgrammer;
        _usingProgrammer = null;
        notifyProgListenerEnd(temp, value, status);
    }

    SprogTrafficController _controller = null;

    protected SprogTrafficController controller() {
        // connect the first time
        if (_controller == null) {
            _controller = _memo.getSprogTrafficController();
        }
        return _controller;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SprogProgrammer.class);

}
