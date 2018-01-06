package jmri.jmrix.can.cbus;

import java.util.ArrayList;
import java.util.List;
import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the jmri.Programmer interface via commands for the CBUS
 * programmer.
 *
 * @author Andrew Crosland Copyright (C) 2009
 */
public class CbusDccProgrammer extends AbstractProgrammer implements CanListener {

    public CbusDccProgrammer(jmri.jmrix.can.TrafficController tc) {
        this.tc = tc;
        tc.addCanListener(this);
    }

    jmri.jmrix.can.TrafficController tc;

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(ProgrammingMode.PAGEMODE);
        ret.add(ProgrammingMode.DIRECTBITMODE);
        ret.add(ProgrammingMode.DIRECTBYTEMODE);
        ret.add(ProgrammingMode.REGISTERMODE);
        return ret;
    }

    // members for handling the programmer interface
    int progState = 0;
    static final int NOTPROGRAMMING = 0;// is notProgramming
    static final int MODESENT = 1;   // waiting reply to command to go into programming mode
    static final int COMMANDSENT = 2;  // read/write command sent, waiting reply
    static final int RETURNSENT = 4;  // waiting reply to go back to ops mode
    boolean _progRead = false;
    int _val; // remember the value being read/written for confirmative reply
    int _cv; // remember the cv being read/written

    // programming interface
    @Override
    synchronized public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("writeCV " + CV + " listens " + p);
        }
        useProgrammer(p);
        _progRead = false;
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;
        // see why waiting
        try {
            startLongTimer();
            // write was in progress - send write command
            tc.sendCanMessage(CbusMessage.getWriteCV(_cv, _val, getMode(), tc.getCanid()), this);
        } catch (Exception e) {
            // program op failed, go straight to end
            log.error("Write operation failed, exception " + e);
            progState = RETURNSENT;
            //controller().sendCanMessage(CbusMessage.getExitProgMode(), this);
        }
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
        useProgrammer(p);
        _progRead = true;
        progState = COMMANDSENT;
        _cv = CV;
        // see why waiting
        try {
            startLongTimer();
            // read was in progress - send read command
            tc.sendCanMessage(CbusMessage.getReadCV(_cv, getMode(), tc.getCanid()), this);
        } catch (Exception e) {
            // program op failed, go straight to end
            log.error("Read operation failed, exception " + e);
            progState = RETURNSENT;
            //controller().sendCanMessage(CbusMessage.getExitProgMode(), this);
        }
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
    public void message(CanMessage m) {
        //log.error("message received unexpectedly: "+m.toString());
    }

    @Override
    synchronized public void reply(CanReply m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            if (log.isDebugEnabled()) {
                log.debug("reply in NOTPROGRAMMING state");
            }
            return;
        } else if (progState == COMMANDSENT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in COMMANDSENT state");
            }
            // operation done, capture result, then have to leave programming mode
            // check for errors
            if ((m.getElement(0) == CbusConstants.CBUS_SSTAT)
                    && (m.getElement(2) == CbusConstants.SSTAT_NO_ACK)) {
                if (log.isDebugEnabled()) {
                    log.debug("handle error reply " + m);
                }
                // perhaps no loco present? Fail back to end of programming
                //controller().sendCanMessage(CbusMessage.getExitProgMode(), this);
                stopTimer();
                notifyProgListenerEnd(-1, jmri.ProgListener.NoLocoDetected);
            } else {
                // see why waiting
                if (_progRead && (m.getElement(0) == CbusConstants.CBUS_PCVS)) {
                    // read was in progress - received report CV message
                    _val = m.getElement(4);
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    // if this was a read, we cached the value earlier.  If its a
                    // write, we're to return the original write value
                    notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                } else if ((!_progRead) && (m.getElement(0) == CbusConstants.CBUS_SSTAT)
                        && (m.getElement(2) == CbusConstants.SSTAT_WR_ACK)) {
                    // write was in progress - acknowledge received
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    // if this was a read, we cached the value earlier.  If its a
                    // write, we're to return the original write value
                    notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                } else {
                    // Carry on waiting
                    if (log.isDebugEnabled()) {
                        log.debug("Reply ignored: " + m);
                    }
                }
            }

        } else {
            if (log.isDebugEnabled()) {
                log.debug("reply in un-decoded state");
            }
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
            //controller().sendCbusMessage(CbusMessage.getExitProgMode(), this);
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    // internal method to notify of the final result
    protected void notifyProgListenerEnd(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("notifyProgListenerEnd value " + value + " status " + status);
        }
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

    private final static Logger log = LoggerFactory.getLogger(CbusDccProgrammer.class);

}
