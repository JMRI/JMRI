package jmri.jmrit;

import jmri.Programmer;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for common code of {@link jmri.jmrit.roster.IdentifyLoco} and
 * {@link jmri.jmrit.decoderdefn.IdentifyDecoder}, the two classes that use a
 * programmer to match Roster entries to what's on the programming track.
 * <p>
 * This is a class (instead of a {@link jmri.jmrit.roster.Roster} member
 * function) to simplify use of {@link jmri.Programmer} callbacks.
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2015
 * @see jmri.jmrit.symbolicprog.CombinedLocoSelPane
 * @see jmri.jmrit.symbolicprog.NewLocoSelPane
 */
public abstract class AbstractIdentify implements jmri.ProgListener {

    static final int RETRY_COUNT = 2;

    public abstract boolean test1();  // no argument to start

    public abstract boolean test2(int value);

    public abstract boolean test3(int value);

    public abstract boolean test4(int value);

    public abstract boolean test5(int value);

    public abstract boolean test6(int value);

    public abstract boolean test7(int value);

    public abstract boolean test8(int value);

    public abstract boolean test9(int value);

    protected AbstractIdentify(Programmer p) {
        this.programmer = p;
    }
    Programmer programmer;
    ProgrammingMode savedMode;

    /**
     * Update the status field (if any). Invoked with "Done" when the results
     * are in.
     *
     * @param status the new status
     */
    protected abstract void statusUpdate(String status);

    /**
     * Start the identification state machine.
     */
    public void start() {
        if (log.isDebugEnabled()) {
            log.debug("identify begins");
        }
        // must be idle, or something quite bad has happened
        if (state != 0) {
            log.error("start with state " + state + ", should have been zero");
        }

        if (programmer != null) {
            savedMode = programmer.getMode(); // In case we need to change modes
        }

        // The first test is invoked here; the rest are handled in the programmingOpReply callback
        state = 1;
        retry = 0;
        lastValue = -1;
        setOptionalCv(false);
        test1();
    }

    /**
     * Stop the identification state machine. This also stops the identification
     * process. Its invoked when a testN returns true; that routine should _not_
     * have invoked a read or write that will result in a callback.
     */
    protected void identifyDone() {
        if (log.isDebugEnabled()) {
            log.debug("identifyDone ends in state " + state);
        }
        statusUpdate("Done");
        state = 0;
    }

    /**
     * Internal method to handle the programmer callbacks, e.g. when a CV read
     * request terminates. Each will reduce (if possible) the list of consistent
     * decoders, and starts the next step.
     *
     * @param value  the value returned
     * @param status the status reported
     */
    @Override
    public void programmingOpReply(int value, int status) {
        // we abort if there's no programmer
        //  (doing this now to simplify later)
        if (programmer == null) {
            log.warn("No programmer connected");
            statusUpdate("No programmer connected");

            state = 0;
            retry = 0;
            error();
            return;
        }
        log.debug("Entering programmingOpReply, state {}, isOptionalCv {},retry {}, value {}, status {}", state, isOptionalCv(), retry, value, programmer.decodeErrorCode(status));

        // we check if the status isn't normal
        if (status != jmri.ProgListener.OK) {
            if (retry < RETRY_COUNT) {
                statusUpdate("Programmer error: "
                        + programmer.decodeErrorCode(status));
                state--;
                retry++;
                value = lastValue;  // Restore the last good value. Needed for retries.
            } else if (state == 1 && programmer.getMode() != ProgrammingMode.PAGEMODE
                    && programmer.getSupportedModes().contains(ProgrammingMode.PAGEMODE)) {
                programmer.setMode(ProgrammingMode.PAGEMODE); // Try paged mode only if test1 (CV8)
                retry = 0;
                state--;
                value = lastValue;  // Restore the last good value. Needed for retries.
                log.warn("{} readng CV {}, trying {} mode", programmer.decodeErrorCode(status),
                        cvToRead, programmer.getMode().toString());
            } else {
                retry = 0;
                if (programmer.getMode() != savedMode) {  // restore original mode
                    log.warn("Restoring " + savedMode.toString() + " mode");
                    programmer.setMode(savedMode);
                }
                if (isOptionalCv()) {
                    log.warn("CV {} is optional. Will assume not present...", cvToRead);
                    statusUpdate("CV " + cvToRead + " is optional. Will assume not present...");
                } else {
                    log.warn("Stopping due to error: "
                            + programmer.decodeErrorCode(status));
                    statusUpdate("Stopping due to error: "
                            + programmer.decodeErrorCode(status));
                    state = 0;
                    error();
                    return;
                }
            }
        } else {
            retry = 0;
            lastValue = value;  // Save the last good value. Needed for retries.
            setOptionalCv(false); // successful read clears flag
        }
        // continuing for normal operation
        // this should eventually be something smarter, maybe using reflection,
        // but for now...
        log.debug("Was state {}, switching to state {}, test{}, isOptionalCv {},retry {}, value {}, status {}",
                state, state + 1, state + 1, isOptionalCv(), retry, value, programmer.decodeErrorCode(status));
        switch (state) {
            case 0:
                state = 1;
                if (test1()) {
                    identifyDone();
                }
                return;
            case 1:
                state = 2;
                if (test2(value)) {
                    identifyDone();
                }
                return;
            case 2:
                state = 3;
                if (test3(value)) {
                    identifyDone();
                }
                return;
            case 3:
                state = 4;
                if (test4(value)) {
                    identifyDone();
                }
                return;
            case 4:
                state = 5;
                if (test5(value)) {
                    identifyDone();
                }
                return;
            case 5:
                state = 6;
                if (test6(value)) {
                    identifyDone();
                }
                return;
            case 6:
                state = 7;
                if (test7(value)) {
                    identifyDone();
                }
                return;
            case 7:
                state = 8;
                if (test8(value)) {
                    identifyDone();
                }
                return;
            case 8:
                state = 9;
                if (test9(value)) {
                    identifyDone();
                } else {
                    log.error("test9 with value = " + value + " returned false, but there is no next step");
                }
                return;
            default:
                // this is an error
                log.error("unexpected state in normal operation: " + state + " value: " + value + ", ending identification");
                identifyDone();
        }
    }

    /**
     * Abstract routine to notify of errors.
     */
    protected abstract void error();

    /**
     * To check if running now.
     *
     * @return true if running; false otherwise
     */
    public boolean isRunning() {
        return state != 0;
    }

    /**
     * State of the internal sequence.
     */
    int state = 0;
    int retry = 0;
    int lastValue = 0;
    boolean optionalCv = false;
    String cvToRead;
    String cvToWrite;

    /**
     * Read a single CV for the next step.
     *
     * @param cv the CV to read
     */
    protected void readCV(String cv) {
        if (programmer == null) {
            statusUpdate("No programmer connected");
        } else {
            cvToRead = cv;
            log.debug("Invoking readCV {}", cvToRead);
            try {
                programmer.readCV(cv, this);
            } catch (jmri.ProgrammerException ex) {
                statusUpdate("" + ex);
            }
        }
    }

    /**
     * Write a single CV for the next step.
     *
     * @param cv    the CV to write
     * @param value to write to the CV
     *
     */
    protected void writeCV(String cv, int value) {
        if (programmer == null) {
            statusUpdate("No programmer connected");
        } else {
            cvToWrite = cv;
            log.debug("Invoking writeCV {}", cvToWrite);
            try {
                programmer.writeCV(cv, value, this);
            } catch (jmri.ProgrammerException ex) {
                statusUpdate("" + ex);
            }
        }
    }

    /**
     * Check the current status of the {@code optionalCv} flag.
     * <ul>
     * <li>If {@code true}, prevents the next CV read from aborting the
     * identification process.</li>
     * <li>Always {@code false} after a successful read.</li>
     * </ul>
     *
     * @return the current status of the {@code optionalCv} flag
     */
    public boolean isOptionalCv() {
        return optionalCv;
    }

    /**
     *
     * Specify whether the next CV read may legitimately fail in some cases.
     *
     * @param flag Set {@code true} to indicate that the next read may fail. A
     *             successful read will automatically set to {@code false}.
     */
    public void setOptionalCv(boolean flag) {
        this.optionalCv = flag;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(AbstractIdentify.class);

}
