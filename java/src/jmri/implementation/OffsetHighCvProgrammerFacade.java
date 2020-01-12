package jmri.implementation;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer facade, at this point just an example.
 * <p>
 * This is for decoders that have an alternate high-CV access method for command
 * stations that can't address all 1024. It falls back to that mode if the CS
 * can't directly address an requested CV address. In the fall back, CVs from 0
 * to "top" are addressed directly. Above the top CV, the upper part of the
 * address is written to a specific CV, followed by an operation to just the
 * lower part of the address. The upper and lower parts are calculated using a
 * supplied modulus, e.g. 100.
 * <p>
 * For example, to write the value N to CV xyy, this will do (modulo = 100):
 * <ul>
 * <li>Write x*10 to CV7 where 10 is cvFactor and 7 is addrCV
 * <li>Write N to CVyy
 * </ul>
 * <p>
 * This method is used by some Zimo decoders
 *
 * @see jmri.implementation.ProgrammerFacadeSelector
 *
 * @author Bob Jacobsen Copyright (C) 2013
 */
public class OffsetHighCvProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * @param prog     the programmer to attach this facade to
     * @param top      CVs above this use the indirect method
     * @param addrCV   CV to which the high part of address is to be written
     * @param cvFactor CV to which the low part of address is to be written
     * @param modulo   Modulus for determining high/low address parts
     */
    public OffsetHighCvProgrammerFacade(Programmer prog, String top, String addrCV, String cvFactor, String modulo) {
        super(prog);
        this.top = Integer.parseInt(top);
        this.addrCV = addrCV;
        this.cvFactor = Integer.parseInt(cvFactor);
        this.modulo = Integer.parseInt(modulo);
    }

    int top;
    String addrCV;
    int cvFactor;
    int modulo;

    // members for handling the programmer interface
    int _val; // remember the value being read/written for confirmative reply
    int _cv; // remember the cv being read/written

    @Override
    public void writeCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("start writeCV");
        _cv = Integer.parseInt(CV);
        _val = val;
        useProgrammer(p);
        if (prog.getCanWrite(CV) || _cv <= top) {
            state = ProgState.PROGRAMMING;
            prog.writeCV(CV, val, this);
        } else {
            // write index first
            state = ProgState.FINISHWRITE;
            prog.writeCV(addrCV, (_cv / modulo) * cvFactor, this);
        }
    }

    @Override
    public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("start readCV");
        _cv = Integer.parseInt(CV);
        useProgrammer(p);
        if (prog.getCanRead(CV) || _cv <= top) {
            state = ProgState.PROGRAMMING;
            prog.readCV(CV, this);
        } else {
            // write index first
            state = ProgState.FINISHREAD;
            prog.writeCV(addrCV, (_cv / modulo) * cvFactor, this);
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
        }
    }

    enum ProgState {

        PROGRAMMING, FINISHREAD, FINISHWRITE, NOTPROGRAMMING
    }
    ProgState state = ProgState.NOTPROGRAMMING;

    // get notified of the final result
    // Note this assumes that there's only one phase to the operation
    @Override
    public void programmingOpReply(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("notifyProgListenerEnd value " + value + " status " + status);
        }

        if (status != OK ) {
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
            case PROGRAMMING:
                // the programmingOpReply handler might send an immediate reply, so
                // clear the current listener _first_
                jmri.ProgListener temp = _usingProgrammer;
                _usingProgrammer = null; // done
                state = ProgState.NOTPROGRAMMING;
                temp.programmingOpReply(value, status);
                break;
            case FINISHREAD:
                try {
                    state = ProgState.PROGRAMMING;
                    prog.readCV(String.valueOf(_cv % modulo), this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final read", e);
                }
                break;
            case FINISHWRITE:
                try {
                    state = ProgState.PROGRAMMING;
                    prog.writeCV(""+(_cv % modulo), _val, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final write", e);
                }
                break;
            default:
                log.error("Unexpected state on reply: " + state);
                // clean up as much as possible
                _usingProgrammer = null;
                state = ProgState.NOTPROGRAMMING;

        }

    }

    // Access to full address space provided by this.
    @Override
    public boolean getCanRead() {
        return true;
    }

    @Override
    public boolean getCanRead(String addr) {
        return Integer.parseInt(addr) <= 1024;
    }

    @Override
    public boolean getCanWrite() {
        return true;
    }

    @Override
    public boolean getCanWrite(String addr) {
        return Integer.parseInt(addr) <= 1024;
    }

    private final static Logger log = LoggerFactory.getLogger(OffsetHighCvProgrammerFacade.class);

}
