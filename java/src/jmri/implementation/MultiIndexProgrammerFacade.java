package jmri.implementation;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer facade for single index multi-CV access.
 * <p>
 * Used through the String write/read/confirm interface. Accepts address
 * formats:
 * <ul>
 * <li>If cvFirst is true:<ul>
 * <li> 123 Do write/read/confirm to 123
 * <li> 123.11 Writes 11 to the first index CV, then does write/read/confirm to
 * 123
 * <li> 123.11.12 Writes 11 to the first index CV, then 12 to the second index
 * CV, then does write/read/confirm to 123
 * </ul>
 * <li>If cvFirst is false:<ul>
 * <li> 123 Do write/read/confirm to 123
 * <li> 11.123 Writes 11 to the first index CV, then does write/read/confirm to
 * 123
 * <li> 11.12.123 Writes 11 to the first index CV, then 12 to the second index
 * CV, then does write/read/confirm to 123
 * </ul>
 * </ul>
 * <p>
 * Is skipDupIndexWrite is true, sequential operations with the same PI and SI
 * values (and only immediately sequential operations with both PI and SI
 * unchanged) will skip writing of the PI and SI CVs. This might not work for
 * some decoders, hence is configurable.
 *
 * @see jmri.implementation.ProgrammerFacadeSelector
 *
 * @author Bob Jacobsen Copyright (C) 2013
 */
public class MultiIndexProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * @param prog              the programmer to which this facade is attached
     * @param indexPI           CV to which the first value is to be written for
     *                          NN.NN and NN.NN.NN forms
     * @param indexSI           CV to which the second value is to be written
     *                          for NN.NN.NN forms
     * @param cvFirst           true if first value in parsed CV is to be
     *                          written; false if second value is to be written
     * @param skipDupIndexWrite true if heuristics can be used to skip PI and SI
     *                          writes; false requires them to be written each
     *                          time.
     */
    public MultiIndexProgrammerFacade(Programmer prog, String indexPI, String indexSI, boolean cvFirst, boolean skipDupIndexWrite) {
        super(prog);
        this.indexPI = indexPI;
        this.indexSI = indexSI;
        this.cvFirst = cvFirst;
        this.skipDupIndexWrite = skipDupIndexWrite;
    }

    String indexPI;
    String indexSI;
    boolean cvFirst;
    boolean skipDupIndexWrite;

    long maxDelay = 1000;  // max mSec since last successful end-of-operation for skipDupIndexWrite; longer delay writes anyway

    // members for handling the programmer interface
    int _val; // remember the value being read/written for confirmative reply
    String _cv; // remember the cv number being read/written
    int valuePI;  //  value to write to PI in current operation or -1
    int valueSI;  //  value to write to SI in current operation or -1

    // remember last operation for skipDupIndexWrite
    int lastValuePI = -1;  // value written in last operation
    int lastValueSI = -1;  // value written in last operation
    long lastOpTime = -1;  // time of last complete

    void parseCV(String cv) {
        valuePI = -1;
        valueSI = -1;
        if (cv.contains(".")) {
            if (cvFirst) {
                String[] splits = cv.split("\\.");
                switch (splits.length) {
                    case 2:
                        valuePI = Integer.parseInt(splits[1]);
                        _cv = splits[0];
                        break;
                    case 3:
                        valuePI = Integer.parseInt(splits[1]);
                        valueSI = Integer.parseInt(splits[2]);
                        _cv = splits[0];
                        break;
                    default:
                        log.error("Too many parts in CV name " + cv);
                        valuePI = Integer.parseInt(splits[1]);
                        valueSI = Integer.parseInt(splits[2]);
                        _cv = splits[0];
                        break;
                }
            } else {
                String[] splits = cv.split("\\.");
                switch (splits.length) {
                    case 2:
                        valuePI = Integer.parseInt(splits[0]);
                        _cv = splits[1];
                        break;
                    case 3:
                        valuePI = Integer.parseInt(splits[0]);
                        valueSI = Integer.parseInt(splits[1]);
                        _cv = splits[2];
                        break;
                    default:
                        log.error("Too many parts in CV name " + cv);
                        valuePI = Integer.parseInt(splits[0]);
                        valueSI = Integer.parseInt(splits[1]);
                        _cv = splits[2];
                        break;
                }
            }
        } else {
            _cv = cv;
        }
    }

    /**
     * Check if the last-written PI and SI values can still be counted on.
     *
     * @return true if last-written values are reliable; false otherwise
     */
    boolean useCachePiSi() {
        return skipDupIndexWrite
                && (lastValuePI == valuePI)
                && (lastValueSI == valueSI)
                && ((System.currentTimeMillis() - lastOpTime) < maxDelay);
    }

    // programming interface
    @Override
    synchronized public void writeCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        _val = val;
        useProgrammer(p);
        parseCV(CV);
        if (valuePI == -1) {
            lastValuePI = -1;  // next indexed operation needs to write PI, SI
            lastValueSI = -1;

            // non-indexed operation
            state = ProgState.PROGRAMMING;
            prog.writeCV(_cv, val, this);
        } else if (useCachePiSi()) {
            // indexed operation with set values is same as non-indexed operation
            state = ProgState.PROGRAMMING;
            prog.writeCV(_cv, val, this);
        } else {
            lastValuePI = valuePI;  // after check in 'if' statement
            lastValueSI = valueSI;

            // write index first
            state = ProgState.FINISHWRITE;
            prog.writeCV(indexPI, valuePI, this);
        }
    }

    @Override
    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    @Override
    synchronized public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        useProgrammer(p);
        parseCV(CV);
        if (valuePI == -1) {
            lastValuePI = -1;  // next indexed operation needs to write PI, SI
            lastValueSI = -1;

            state = ProgState.PROGRAMMING;
            prog.readCV(_cv, this);
        } else if (useCachePiSi()) {
            // indexed operation with set values is same as non-indexed operation
            state = ProgState.PROGRAMMING;
            prog.readCV(_cv, this);
        } else {
            lastValuePI = valuePI;  // after check in 'if' statement
            lastValueSI = valueSI;

            // write index first
            state = ProgState.FINISHREAD;
            prog.writeCV(indexPI, valuePI, this);
        }
    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            log.info("programmer already in use by {}", _usingProgrammer);
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
        }
    }

    enum ProgState {
        PROGRAMMING,
        FINISHREAD,
        FINISHWRITE,
        NOTPROGRAMMING
    }
    ProgState state = ProgState.NOTPROGRAMMING;

    // get notified of the final result
    // Note this assumes that there's only one phase to the operation
    @Override
    public void programmingOpReply(int value, int status) {
        log.debug("notifyProgListenerEnd value {} status {} ", value, status);

        if (status != OK) {
            // clear memory of last PI, SI written
            lastValuePI = -1;
            lastValueSI = -1;
            lastOpTime = -1;

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
                lastOpTime = System.currentTimeMillis();
                temp.programmingOpReply(value, status);
                break;
            case FINISHREAD:
                if (valueSI == -1) {
                    try {
                        state = ProgState.PROGRAMMING;
                        prog.readCV(_cv, this);
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing final read", e);
                    }
                } else {
                    try {
                        int tempSI = valueSI;
                        valueSI = -1;
                        state = ProgState.FINISHREAD;
                        prog.writeCV(indexSI, tempSI, this);
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing write SI for read", e);
                    }
                }
                break;
            case FINISHWRITE:
                if (valueSI == -1) {
                    try {
                        state = ProgState.PROGRAMMING;
                        prog.writeCV(_cv, _val, this);
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing final write", e);
                    }
                } else {
                    try {
                        int tempSI = valueSI;
                        valueSI = -1;
                        state = ProgState.FINISHWRITE;
                        prog.writeCV(indexSI, tempSI, this);
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing write SI for write", e);
                    }
                }
                break;
            default:
                log.error("Unexpected state on reply: " + state);
                // clean up as much as possible
                _usingProgrammer = null;
                state = ProgState.NOTPROGRAMMING;
                lastValuePI = -1;
                lastValueSI = -1;
                lastOpTime = -1;

        }
    }

    private final static Logger log = LoggerFactory.getLogger(MultiIndexProgrammerFacade.class.getName());

}
