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
 * supplied modulus, e.g. 100, and an indicator value that's added in. Finally,
 * the specific CV is reset to zero to end the offset operation.
 * <p>
 * For example, to write the value N to CV xyy, this will do (modulo = 100,
 * indicator = 200):
 * <ul>
 * <li>Write 200+x*10 to CV7 where 10 is cvFactor, 200 is indicator and 7 is
 * addrCV
 * <li>Write N to CVyy
 * <li>Write 0 to CV7
 * </ul>
 * <p>
 * This method is used by some Zimo decoders
 *
 * @see jmri.implementation.ProgrammerFacadeSelector
 *
 * @author Bob Jacobsen Copyright (C) 2013
 */
public class ResettingOffsetHighCvProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * @param prog      the programmer this facade is attached to
     * @param top       CVs above this use the indirect method
     * @param addrCV    CV to which the high part of address is to be written
     * @param cvFactor  CV to which the low part of address is to be written
     * @param modulo    modulus for determining high/low address parts
     * @param indicator value added to calculation to split high and low parts
     */
    public ResettingOffsetHighCvProgrammerFacade(Programmer prog, String top, String addrCV, String cvFactor, String modulo, String indicator) {
        super(prog);
        this.top = Integer.parseInt(top);
        this.addrCV = addrCV;
        this.cvFactor = Integer.parseInt(cvFactor);
        this.modulo = Integer.parseInt(modulo);
        this.indicator = Integer.parseInt(indicator);
    }

    int top;
    String addrCV;
    int cvFactor;
    int modulo;
    int indicator;

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
            prog.writeCV(addrCV, (_cv / modulo) * cvFactor + indicator, this);
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
            prog.writeCV(addrCV, (_cv / modulo) * cvFactor + indicator, this);
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

        PROGRAMMING, FINISHREAD, FINISHWRITE, RESET, NOTPROGRAMMING
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
            case FINISHREAD:
                try {
                    state = ProgState.RESET;
                    prog.readCV(""+(_cv % modulo), this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final read", e);
                }
                break;
            case FINISHWRITE:
                try {
                    state = ProgState.RESET;
                    prog.writeCV(""+(_cv % modulo), _val, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final write", e);
                }
                break;
            case RESET:
                try {
                    state = ProgState.PROGRAMMING;
                    prog.writeCV(addrCV, 0, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing reset write", e);
                }
                break;
            case PROGRAMMING:
                // the programmingOpReply handler might send an immediate reply, so
                // clear the current listener _first_
                jmri.ProgListener temp = _usingProgrammer;
                _usingProgrammer = null; // done
                state = ProgState.NOTPROGRAMMING;
                temp.programmingOpReply(value, status);
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

    private final static Logger log = LoggerFactory.getLogger(ResettingOffsetHighCvProgrammerFacade.class);

}
