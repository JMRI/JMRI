package jmri.jmrix.nce;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;

/**
 * Convert the jmri.Programmer interface into commands for the NCE power house.
 * <p>
 * This has two states: NOTPROGRAMMING, and COMMANDSENT. The transitions to and
 * from programming mode are now handled in the TrafficController code.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2016
 * @author kcameron Copyright (C) 2014
 */
public class NceProgrammer extends AbstractProgrammer implements NceListener {

    protected NceTrafficController tc;

    public NceProgrammer(NceTrafficController tc) {
        this.tc = tc;
        super.SHORT_TIMEOUT = 4000;

        if (getSupportedModes().size() > 0) {
            setMode(getSupportedModes().get(0));
        }
    }

    /** 
     * {@inheritDoc}
     *
     * NCE programming modes available depend on settings
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        if (tc == null) {
            log.warn("getSupportedModes called with null tc", new Exception("traceback"));
        }
        java.util.Objects.requireNonNull(tc, "TrafficController reference needed");

        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            // USB connection
            switch (tc.getUsbSystem()) {
                case NceTrafficController.USB_SYSTEM_POWERCAB:
                case NceTrafficController.USB_SYSTEM_TWIN:
                    ret.add(ProgrammingMode.DIRECTMODE);
                    ret.add(ProgrammingMode.PAGEMODE);
                    ret.add(ProgrammingMode.REGISTERMODE);
                    return ret;

                case NceTrafficController.USB_SYSTEM_SB3:
                case NceTrafficController.USB_SYSTEM_SB5:
                case NceTrafficController.USB_SYSTEM_POWERPRO:
                    log.trace("no programming modes available for USB {}", tc.getUsbSystem());
                    return ret;

                default:
                    log.warn("should not have hit default");
                    return ret;
            }
        }

        // here not USB
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            ret.add(ProgrammingMode.DIRECTMODE);
        }

        ret.add(ProgrammingMode.PAGEMODE);
        ret.add(ProgrammingMode.REGISTERMODE);

        return ret;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean getCanRead() {
        return !(tc != null && tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_POWERCAB
                && tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_TWIN
                && tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean getCanWrite(String cv) {
        return getCanWrite(Integer.parseInt(cv));
    }

    boolean getCanWrite(int cv) {
        // prevent writing Prog Track mode CV > 256 on PowerPro 2007C and earlier
        return !((cv > 256)
                && ((getMode() == ProgrammingMode.PAGEMODE)
                || (getMode() == ProgrammingMode.DIRECTMODE)
                || (getMode() == ProgrammingMode.REGISTERMODE))
                && ((tc != null)
                && ((tc.getCommandOptions() == NceTrafficController.OPTION_1999)
                || (tc.getCommandOptions() == NceTrafficController.OPTION_2004)
                || (tc.getCommandOptions() == NceTrafficController.OPTION_2006))));
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
        if (log.isDebugEnabled()) {
            log.debug("writeCV " + CV + " listens " + p);
        }
        useProgrammer(p);
        // prevent writing Prog Track mode CV > 256 on PowerPro 2007C and earlier
        if (!getCanWrite(CV)) {
            throw new jmri.ProgrammerException("CV number not supported");
        }
        _progRead = false;
        // set state
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;

        try {
            // start the error timer
            startLongTimer();

            // format and send the write message
            tc.sendNceMessage(progTaskStart(getMode(), _val, _cv), this);
        } catch (jmri.ProgrammerException e) {
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
            tc.sendNceMessage(progTaskStart(getMode(), -1, _cv), this);
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
            if (log.isInfoEnabled()) {
                log.info("programmer already in use by " + _usingProgrammer);
            }
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
            return;
        }
    }

    // internal method to create the NceMessage for programmer task start
    protected NceMessage progTaskStart(ProgrammingMode mode, int val, int cvnum) throws jmri.ProgrammerException {
        // val = -1 for read command; mode is direct, etc
        if (val < 0) {
            // read
            if (mode == ProgrammingMode.PAGEMODE) {
                return NceMessage.getReadPagedCV(tc, cvnum);
            } else if (mode == ProgrammingMode.DIRECTMODE) {
                return NceMessage.getReadDirectCV(tc, cvnum);
            } else {
                return NceMessage.getReadRegister(tc, registerFromCV(cvnum));
            }
        } else {
            // write
            if (mode == ProgrammingMode.PAGEMODE) {
                return NceMessage.getWritePagedCV(tc, cvnum, val);
            } else if (mode == ProgrammingMode.DIRECTMODE) {
                return NceMessage.getWriteDirectCV(tc, cvnum, val);
            } else {
                return NceMessage.getWriteRegister(tc, registerFromCV(cvnum), val);
            }
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void message(NceMessage m) {
        log.error("message received unexpectedly: " + m.toString());
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void reply(NceReply m) {
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
            if ((m.match("NO FEEDBACK DETECTED") >= 0)
                    || (m.isBinary() && !_progRead && (m.getElement(0) != '!'))
                    || (m.isBinary() && _progRead && (m.getElement(1) != '!'))) {
                if (log.isDebugEnabled()) {
                    log.debug("handle NO FEEDBACK DETECTED");
                }
                // perhaps no loco present? Fail back to end of programming
                notifyProgListenerEnd(_val, jmri.ProgListener.NoLocoDetected);
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NceProgrammer.class);

}
