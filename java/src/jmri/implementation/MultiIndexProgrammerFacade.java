package jmri.implementation;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer facade for accessing CVs that require one or more "index CVs" 
 * to have specific values before doing the final read or write operation.
 * <p>
 * Currently supports direct access to CVs (the usual style), operations where
 * one index CV (called PI, for primary index) must have a specific value first,
 * and operations where two index CVs (called PI and SI, for secondary index)
 * must have a specific value first. 
 * <p>
 * Accepts two different address formats so that the CV addresses can be 
 * written in the same style as the decoder manufacturer's documentation:
 * <ul>
 * <li>If cvFirst is true:
 * <ul>
 *   <li> 123 Do read or write directly to CV 123; this allows unindexed CVs to go through
 *   <li> 123.11 Writes 11 to PI, the index CV, then does the final read or write to CV 123
 *   <li> 123.11.12 Writes 11 to the first index CV, then 12 to the second index CV, 
 *                    then does the final read or write to CV 123
 * </ul>
 * <li>If cvFirst is false:
 * <ul>
 *   <li> 123 Do read or write directly to CV 123; this allows unindexed CVs to go through
 *   <li> 11.123 Writes 11 to the first index CV, then does the final read or write to CV 123
 *   <li> 11.12.123 Writes 11 to the first index CV, then 12 to the second index CV, 
 *              then does the final read or write to CV 123
 * </ul>
 * </ul>
 * QSI decoders generally use the 1st format, and ESU LokSound decoders the second.
 * <p>
 * The specific CV numbers for PI and SI are provided when constructing the object.
 * They can be read from a decoder definition file by e.g. {@link jmri.implementation.ProgrammerFacadeSelector}.
 * <p>
 * Alternately the PI and/or SI CV numbers can be set by using a "nn=nn" syntax when specifying
 * PI and/or SI.  For example, using a cvFirst false syntax, "101=12.80" sets CV101 to 12 before
 * accessing CV 80, regardless of the PI value configured into the facade.
 * <p>
 * If skipDupIndexWrite is true, sequential operations with the same PI and SI values
 * (and only immediately sequential operations with both PI and SI unchanged) will
 * skip writing of the PI and SI CVs.  This might not work for some decoders, hence is
 * configurable. See the logic in {@link jmri.implementation.ProgrammerFacadeSelector}
 * for how the decoder file contents and default (preferences) interact.
 * <p>
 * State Diagram for read and write operations (click to magnify):
 * <a href="doc-files/MultiIndexProgrammerFacade-State-Diagram.png"><img src="doc-files/MultiIndexProgrammerFacade-State-Diagram.png" alt="UML State diagram" height="50%" width="50%"></a>
 *
 * @see jmri.implementation.ProgrammerFacadeSelector
 *
 * @author Bob Jacobsen Copyright (C) 2013
 */
 
/*
 * @startuml jmri/implementation/doc-files/MultiIndexProgrammerFacade-State-Diagram.png
 * [*] --> NOTPROGRAMMING 
 * NOTPROGRAMMING --> PROGRAMMING: readCV() & & PI==-1\n(read CV)
 * NOTPROGRAMMING --> FINISHREAD: readCV() & PI!=-1\n(write PI)
 * NOTPROGRAMMING --> PROGRAMMING: writeCV() & single CV\n(write CV)
 * NOTPROGRAMMING --> FINISHWRITE: writeCV() & PI write needed\n(write PI)
 * FINISHREAD --> FINISHREAD: OK reply & SI!=-1\n(write SI)
 * FINISHREAD --> PROGRAMMING: OK reply & SI==-1\n(read CV)
 * FINISHWRITE --> FINISHWRITE: OK reply & SI!=-1\n(write SI)
 * FINISHWRITE --> PROGRAMMING: OK reply & SI==-1\n(write CV)
 * PROGRAMMING --> NOTPROGRAMMING: OK reply received\n(return status and value)
 * FINISHREAD --> NOTPROGRAMMING : Error reply received
 * FINISHWRITE --> NOTPROGRAMMING : Error reply received
 * PROGRAMMING --> NOTPROGRAMMING : Error reply received
 * @enduml
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
        this.defaultIndexPI = indexPI;
        this.defaultIndexSI = indexSI;
        this.cvFirst = cvFirst;
        this.skipDupIndexWrite = skipDupIndexWrite;
    }

    String defaultIndexPI;
    String defaultIndexSI;

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

    // take the CV string and configure the actions to take
    void parseCV(String cv) {
        valuePI = -1;
        valueSI = -1;
        if (cv.contains(".")) {
            if (cvFirst) {
                String[] splits = cv.split("\\.");
                switch (splits.length) {
                    case 2:
                        if (hasAlternateAddress(splits[1])) {
                            valuePI = getAlternateValue(splits[1]);
                            indexPI = getAlternateAddress(splits[1]);
                        } else {
                            valuePI = Integer.parseInt(splits[1]);
                            indexPI = defaultIndexPI;
                        }
                        _cv = splits[0];
                        break;
                    case 3:
                        if (hasAlternateAddress(splits[1])) {
                            valuePI = getAlternateValue(splits[1]);
                            indexPI = getAlternateAddress(splits[1]);
                        } else {
                            valuePI = Integer.parseInt(splits[1]);
                            indexPI = defaultIndexPI;
                        }
                        if (hasAlternateAddress(splits[2])) {
                            valueSI = getAlternateValue(splits[2]);
                            indexSI = getAlternateAddress(splits[2]);
                        } else {
                            valueSI = Integer.parseInt(splits[2]);
                            indexSI = defaultIndexSI;
                        }
                        _cv = splits[0];
                        break;
                    default:
                        log.error("Too many parts in CV name; taking 1st two " + cv);
                        valuePI = Integer.parseInt(splits[1]);
                        valueSI = Integer.parseInt(splits[2]);
                        _cv = splits[0];
                        break;
                }
            } else {
                String[] splits = cv.split("\\.");
                switch (splits.length) {
                    case 2:
                        if (hasAlternateAddress(splits[0])) {
                            valuePI = getAlternateValue(splits[0]);
                            indexPI = getAlternateAddress(splits[0]);
                        } else {
                            valuePI = Integer.parseInt(splits[0]);
                            indexPI = defaultIndexPI;
                        }
                        _cv = splits[1];
                        break;
                    case 3:
                        if (hasAlternateAddress(splits[0])) {
                            valuePI = getAlternateValue(splits[0]);
                            indexPI = getAlternateAddress(splits[0]);
                        } else {
                            valuePI = Integer.parseInt(splits[0]);
                            indexPI = defaultIndexPI;
                        }
                        if (hasAlternateAddress(splits[1])) {
                            valueSI = getAlternateValue(splits[1]);
                            indexSI = getAlternateAddress(splits[1]);
                        } else {
                            valueSI = Integer.parseInt(splits[1]);
                            indexSI = defaultIndexSI;
                        }
                        _cv = splits[2];
                        break;
                    default:
                        log.error("Too many parts in CV name; taking 1st two " + cv);
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

    boolean hasAlternateAddress(String cv) {
        return cv.contains("=");
    }
    
    String getAlternateAddress(String cv) {
        return cv.split("=")[0];
    }
    
    int getAlternateValue(String cv) {
        return Integer.parseInt(cv.split("=")[1]);
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

    @Override
    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        _val = val;
        useProgrammer(p);
        parseCV(CV);
        if (valuePI == -1) {
            lastValuePI = -1;  // next indexed operation needs to write PI, SI
            lastValueSI = -1;

            // non-indexed operation
            state = ProgState.PROGRAMMING;
            prog.confirmCV(_cv, val, this);
        } else if (useCachePiSi()) {
            // indexed operation with set values is same as non-indexed operation
            state = ProgState.PROGRAMMING;
            prog.confirmCV(_cv, val, this);
        } else {
            lastValuePI = valuePI;  // after check in 'if' statement
            lastValueSI = valueSI;

            // write index first
            state = ProgState.FINISHCONFIRM;
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

    /**
     * State machine for MultiIndexProgrammerFacade  (click to magnify):
     * <a href="doc-files/MultiIndexProgrammerFacade-State-Diagram.png"><img src="doc-files/MultiIndexProgrammerFacade-State-Diagram.png" alt="UML State diagram" height="50%" width="50%"></a>
     */
    enum ProgState {
        /** Waiting for response to (final) read or write operation, final reply next */
        PROGRAMMING,
        /** Waiting for response to first or second index write before a final read operation */
        FINISHREAD,
        /** Waiting for response to first or second index write before a final write operation */
        FINISHWRITE,
        /** Waiting for response to first or second index write before a final confirm operation */
        FINISHCONFIRM,
        /** No current operation */
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
            case FINISHCONFIRM:
                if (valueSI == -1) {
                    try {
                        state = ProgState.PROGRAMMING;
                        prog.confirmCV(_cv, _val, this);
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing final confirm", e);
                    }
                } else {
                    try {
                        int tempSI = valueSI;
                        valueSI = -1;
                        state = ProgState.FINISHCONFIRM;
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

    private final static Logger log = LoggerFactory.getLogger(MultiIndexProgrammerFacade.class);

}
