// MultiIndexProgrammerFacade.java

package jmri.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Programmer;
import jmri.ProgListener;
import jmri.jmrix.AbstractProgrammerFacade;

/**
 * Programmer facade for single index multi-CV access.
 * <p>
 * Used through the String write/read/confirm interface.  Accepts address formats:
 *<ul>
 *<li> 123 Do write/read/confirm to 123
 *<li> 123.11 Writes 11 to the first index CV, then does write/read/confirm to 123
 *<li> 123.11.12 Writes 11 to the first index CV, then 12 to the second index CV, then does write/read/confirm to 123
 *</ul>
 *
 * @author      Bob Jacobsen  Copyright (C) 2013
 * @version	$Revision: 24246 $
 */
public class MultiIndexProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * @param indexPI  CV to which the first value is to be written for NN.NN and NN.NN.NN forms
     * @param indexSI  CV to which the second value is to be written for NN.NN.NN forms
     */
    public MultiIndexProgrammerFacade(Programmer prog, String indexPI, String indexSI) {
        super(prog);
        this.indexPI = indexPI;
        this.indexSI = indexSI;
    }
    
    String indexPI;
    String indexSI;

    // members for handling the programmer interface

    int _val;	// remember the value being read/written for confirmative reply
    String _cv;	// remember the cv number being read/written
    int valuePI;  //  value to write to PI or -1
    int valueSI;  //  value to write to SI or -1

    void parseCV(String cv) {
        valuePI = -1;
        valueSI = -1;
        if (cv.contains(".")) {
            String[] splits = cv.split("\\.");
            if (splits.length == 2) {
                valuePI = Integer.parseInt(splits[1]);
                _cv = splits[0];
            } else if (splits.length == 3) {
                valuePI = Integer.parseInt(splits[1]);
                valueSI = Integer.parseInt(splits[2]);
                _cv = splits[0];
            } else {
                log.error("Too many parts in CV name "+cv);
                valuePI = Integer.parseInt(splits[1]);
                valueSI = Integer.parseInt(splits[2]);
                _cv = splits[0];
            }
        } else {
            _cv = cv;
        }
    }
    
    // programming interface
    synchronized public void writeCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        _val = val;
        useProgrammer(p);
        parseCV(CV);
        if (valuePI==-1) {
            state = ProgState.PROGRAMMING;
            prog.writeCV(_cv, val, this);
        } else {
            // write index first
            state = ProgState.FINISHWRITE;
            prog.writeCV(indexPI, valuePI, this);
        }
    }

    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    synchronized public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        useProgrammer(p);
        parseCV(CV);
        if (valuePI==-1) {
            state = ProgState.PROGRAMMING;
            prog.readCV(_cv, this);
        } else {
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
            if (log.isInfoEnabled()) log.info("programmer already in use by "+_usingProgrammer);
            throw new jmri.ProgrammerException("programmer in use");
        }
        else {
            _usingProgrammer = p;
            return;
        }
    }

    enum ProgState { PROGRAMMING, FINISHREAD, FINISHWRITE, NOTPROGRAMMING }
    ProgState state = ProgState.NOTPROGRAMMING;
    
    // get notified of the final result
    // Note this assumes that there's only one phase to the operation
    public void programmingOpReply(int value, int status) {
        if (log.isDebugEnabled()) log.debug("notifyProgListenerEnd value "+value+" status "+status);
        
        if (_usingProgrammer == null) log.error("No listener to notify");

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
                if (valueSI == -1 ) {
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
                if (valueSI == -1 ) {
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
                log.error("Unexpected state on reply: "+state);
                // clean up as much as possible
                _usingProgrammer = null;
                state = ProgState.NOTPROGRAMMING;
                
        }
    }

    static Logger log = LoggerFactory.getLogger(MultiIndexProgrammerFacade.class.getName());

}

