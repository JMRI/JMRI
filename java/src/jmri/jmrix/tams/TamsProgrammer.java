package jmri.jmrix.tams;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert the jmri.Programmer interface into commands for the NCE power house.
 * <p>
 * This has two states: NOTPROGRAMMING, and COMMANDSENT. The transitions to and
 * from programming mode are now handled in the TrafficController code. Based on
 * work by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public class TamsProgrammer extends AbstractProgrammer implements TamsListener {

    protected TamsTrafficController tc;

    public TamsProgrammer(TamsTrafficController tc) {
        this.tc = tc;
        super.SHORT_TIMEOUT = 6000;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    @Nonnull
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
    static final int COMMANDSENT = 2; 	// read/write command sent, waiting reply
    static final int COMMANDSENT_2 = 4;	// ops programming mode, send msg twice
    boolean _progRead = false;
    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void writeCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        if (log.isDebugEnabled()) {
            log.debug("writeCV " + CV + " listens " + p);
        }
        useProgrammer(p);
        _progRead = false;
        // set state
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;

        try {
            // start the error timer
            startLongTimer();

            // format and send the write message
            tc.sendTamsMessage(progTaskStart(_val, _cv), this);
        } catch (jmri.ProgrammerException e) {
            useProgrammer(null);
            progState = NOTPROGRAMMING;
            throw e;
        }
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
        if (log.isDebugEnabled()) {
            log.debug("readCV " + CV + " listens " + p);
        }
        useProgrammer(p);
        _progRead = true;

        // set commandPending state
        progState = COMMANDSENT;
        _cv = CV;

        try {
            // start the error timer
            startLongTimer();

            // format and send the write message
            tc.sendTamsMessage(progTaskStart(-1, _cv), this);
        } catch (jmri.ProgrammerException e) {
            useProgrammer(null);
            progState = NOTPROGRAMMING;
            throw e;
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

    // internal method to create the TamsMessage for programmer task start
    protected TamsMessage progTaskStart(int val, int cvnum) throws jmri.ProgrammerException {
        // val = -1 for read command; mode is direct, etc
        if (val < 0) {
            // read
            if (getMode() == ProgrammingMode.PAGEMODE) {
                return TamsMessage.getReadPagedCV(cvnum);
            } else if (getMode() == ProgrammingMode.DIRECTBYTEMODE) {
                return TamsMessage.getReadDirectByteCV(cvnum);
            } else {
                return TamsMessage.getReadRegister(registerFromCV(cvnum));
            }
        } else {
            // write
            if (getMode() == ProgrammingMode.PAGEMODE) {
                return TamsMessage.getWritePagedCV(cvnum, val);
            } else if (getMode() == ProgrammingMode.DIRECTBYTEMODE) {
                return TamsMessage.getWriteDirectByteCV(cvnum, val);
            } else {
                return TamsMessage.getWriteRegister(registerFromCV(cvnum), val);
            }
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void message(TamsMessage m) {
        log.error("message received unexpectedly: " + m.toString());
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void reply(TamsReply m) {
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
            // operation done, capture result, then post response
            progState = NOTPROGRAMMING;
            // check for errors
            if (m.match("Ok") >= 0) {
                // see why waiting
                if (_progRead) {
                    // read was in progress - get return value
                    _val = m.value();
                }
                // if this was a read, we retrieved the value above.  If its a
                // write, we're to return the original write value
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);

            } else if ((m.match("No ack") >= 0)) {
                if (log.isDebugEnabled()) {
                    log.debug("handle NO Ack");
                }
                // perhaps no loco present? Fail back to end of programming
                notifyProgListenerEnd(_val, jmri.ProgListener.NoAck);
            } else if (m.match("Busy") >= 0) {
                if (log.isDebugEnabled()) {
                    log.debug("handle Busy");
                }
                // perhaps no loco present? Fail back to end of programming
                notifyProgListenerEnd(_val, jmri.ProgListener.ProgrammerBusy);
            } else if (m.match("Timeout") >= 0) {
                if (log.isDebugEnabled()) {
                    log.debug("handle Timeout");
                }
                // perhaps no loco present? Fail back to end of programming
                notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
            } else if (m.match("Error") >= 0) {
                if (log.isDebugEnabled()) {
                    log.debug("handle Other Error");
                }
                // perhaps no loco present? Fail back to end of programming
                notifyProgListenerEnd(_val, jmri.ProgListener.UnknownError);
            } else {
                // see why waiting
                if (_progRead) {
                    // read was in progress - get return value
                    _val = m.value();
                }
                // if this was a read, we retrieved the value above.  If its a
                // write, we're to return the original write value
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);

            }

        } else if (progState == COMMANDSENT_2) {
            if (log.isDebugEnabled()) {
                log.debug("first reply in COMMANDSENT_2 state");
            }
            // first message sent, now wait for second reply to arrive
            progState = COMMANDSENT;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("reply in un-decoded state");
            }
        }
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
            cleanup();
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    // Internal method to cleanup in case of a timeout. Separate routine
    // so it can be changed in subclasses.
    void cleanup() {
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
        notifyProgListenerEnd(temp, value, status);
    }

    private final static Logger log = LoggerFactory.getLogger(TamsProgrammer.class);

}
