package jmri.implementation;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.jmrix.AbstractProgrammerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer facade for access to Accessory Decoder Ops Mode programming
 * <p>
 * (Eventually implements four modes, passing all others to underlying
 * programmer:
 * <ul>
 * <li>OPSACCBYTEMODE
 * <li>OPSACCBITMODE
 * <li>OPSACCEXTBYTEMODE
 * <li>OPSACCEXTBITMODE
 * </ul>
 * <p>
 * Used through the String write/read/confirm interface. Accepts integers as
 * addresses, but then emits NMRA DCC packets through the default CommandStation
 * interface (which must be present)
 *
 * @see jmri.implementation.ProgrammerFacadeSelector
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
// @ToDo("transform to annotations requires e.g. http://alchemy.grimoire.ca/m2/sites/ca.grimoire/todo-annotations/")
// @ToDo("read handling needs to be aligned with other ops mode programmers")
// @ToDo("make sure jmri/jmrit/progsupport/ProgServiceModePane shows the modes, and that DP/DP3 displays them as it configures a decoder")
public class OpsModeDelayedProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * Programmer facade for access to Accessory Decoder Ops Mode programming.
     *
     * @param prog       The Ops Mode Programmer we are piggybacking on.
     * @param writeDelay A string representing the desired delay after a write
     *                   operation, in milliseconds.
     */
    public OpsModeDelayedProgrammerFacade(Programmer prog, int writeDelay) {
        super(prog);
        log.debug("Constructing OpsModeDelayedProgrammerFacade");
        this._usingProgrammer = null;
        this.prog = prog;
        this._readDelay = 0;
        this._writeDelay = writeDelay;
    }

    // members for handling the programmer interface
    int _val;           // remember the value being read/written for confirmative reply
    String _cv;         // remember the cv number being read/written
    String _addrType;   // remember the address type: ("decoder" or null) or ("accessory" or "output")
    int _readDelay;     // remember the programming delay, in milliseconds
    int _writeDelay;    // remember the programming delay, in milliseconds
    int _delay;         // remember the programming delay, in milliseconds

    // programming interface
    @Override
    public synchronized void writeCV(String cv, int val, ProgListener p) throws ProgrammerException {
        log.debug("writeCV entry: ProgListener p is {}", p);
        useProgrammer(p);
        state = ProgState.WRITECOMMANDSENT;
        prog.writeCV(cv, val, this);
    }

    @Override
    public synchronized void readCV(String cv, jmri.ProgListener p) throws jmri.ProgrammerException {
        useProgrammer(p);
        state = ProgState.READCOMMANDSENT;
        prog.readCV(cv, this);
    }

    @Override
    public synchronized void confirmCV(String cv, int val, ProgListener p) throws ProgrammerException {
        useProgrammer(p);
        state = ProgState.READCOMMANDSENT;
        prog.confirmCV(cv, val, this);
    }

    private transient volatile jmri.ProgListener _usingProgrammer;

    /**
     * Internal method to remember who's using the programmer.
     *
     *
     * @param p the programmer
     * @throws ProgrammerException if p is already in use
     */
    protected synchronized void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        log.debug("useProgrammer entry: _usingProgrammer is {}", _usingProgrammer);
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isInfoEnabled()) {
                log.info("programmer already in use by " + _usingProgrammer);
            }
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
        }
        log.debug("useProgrammer exit: _usingProgrammer is {}", _usingProgrammer);
    }

    enum ProgState {

        READCOMMANDSENT, WRITECOMMANDSENT, NOTPROGRAMMING
    }
    ProgState state = ProgState.NOTPROGRAMMING;

    // get notified of the final result
    // Note this assumes that there's only one phase to the operation
    @Override
    public synchronized void programmingOpReply(int value, int status) {
        log.debug("notifyProgListenerEnd value={}, status={}", value, status);

        if (status != OK) {
            // pass abort up
            log.debug("Reset and pass abort up");
            jmri.ProgListener temp = _usingProgrammer;
            _usingProgrammer = null; // done
            state = ProgState.NOTPROGRAMMING;
            temp.programmingOpReply(value, status);
            return;
        }

        if (_usingProgrammer == null) {
            log.error("No listener to notify, reset and ignore");
            state = ProgState.NOTPROGRAMMING;
            return;
        }

        switch (state) {
            case READCOMMANDSENT:
                _delay = _readDelay;
                break;
            case WRITECOMMANDSENT:
                _delay = _writeDelay;
                break;
            default:
                log.error("Unexpected state on reply: " + state);
                // clean up as much as possible
                _usingProgrammer = null;
                state = ProgState.NOTPROGRAMMING;
        }

        log.debug("delaying {} milliseconds", _delay);
        jmri.util.ThreadingUtil.runOnLayoutDelayed(() -> {
            // the programmingOpReply handler might send an immediate reply, so
            // clear the current listener _first_
            log.debug("going NOTPROGRAMMING after value {}, status={}", value, status);
            jmri.ProgListener temp = _usingProgrammer;
            _usingProgrammer = null; // done
            state = ProgState.NOTPROGRAMMING;
            log.debug("notifying value " + value + " status " + status);
            temp.programmingOpReply(value, status);
        }, _delay);

    }

    private final static Logger log = LoggerFactory.getLogger(OpsModeDelayedProgrammerFacade.class);

}
