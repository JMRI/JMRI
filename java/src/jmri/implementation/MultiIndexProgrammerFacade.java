// MultiIndexProgrammerFacade.java

package jmri.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.PowerManager;
import jmri.Programmer;
import jmri.ProgListener;
import jmri.jmrix.AbstractProgrammerFacade;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

/**
 * Programmer facade for single index multi-CV access.
 * <p>
 * Used through the String write/read/confirm interface.  Accepts address formats:
 *<ul>
 *<li> 123 Do write/read/confirm to 123
 *<li> 13.123 Writes 13 to the index CV, then does write/read/confirm to 123
 *</ul>
 *
 * @author      Bob Jacobsen  Copyright (C) 2013
 * @version	$Revision: 24246 $
 */
public class MultiIndexProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * @param indexCV  CV to which the first value is to be written for NN.NN forms
     */
    public MultiIndexProgrammerFacade(Programmer prog, int indexCV) {
        super(prog);
        this.indexCV = indexCV;
    }
    
    int indexCV;

    // members for handling the programmer interface

    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written
    int _indexVal;  //  value to write to index or -1

    void parseCV(String cv) {
        if (cv.contains(".")) {
            String[] splits = cv.split("\\.");
            _indexVal = Integer.parseInt(splits[0]);
            _cv = Integer.parseInt(splits[1]);
        } else {
            _indexVal = -1;
            _cv = Integer.parseInt(cv);
        }
    }
    
    // programming interface
    synchronized public void writeCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        _val = val;
        useProgrammer(p);
        parseCV(CV);
        if (_indexVal == -1 ) {
            state = ProgState.PROGRAMMING;
            prog.writeCV(_cv, val, this);
        } else {
            // write index first
            state = ProgState.FINISHWRITE;
            prog.writeCV(indexCV, _indexVal, this);
        }
    }

    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    synchronized public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        useProgrammer(p);
        parseCV(CV);
        if (_indexVal == -1 ) {
            state = ProgState.PROGRAMMING;
            prog.readCV(_cv, this);
        } else {
            // write index first
            state = ProgState.FINISHREAD;
            prog.writeCV(indexCV, _indexVal, this);
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
                try {
                    state = ProgState.PROGRAMMING;
                    prog.readCV(_cv, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final read", e);
                }
                break;
            case FINISHWRITE:
                try {
                    state = ProgState.PROGRAMMING;
                    prog.writeCV(_cv, _val, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final write", e);
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

