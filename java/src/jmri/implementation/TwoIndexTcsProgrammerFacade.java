// TwoIndexTcsProgrammerFacade.java

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
 *<li> T2CV.11.12 Writes 11 to the first index CV (201), 12 to the 2nd index CV (202), 
 *      then does write/read/confirm operations on CV 203 and 204
  *</ul>
 * All others pass through to the next facade or programmer. E.g. 123 will do a write/read/confirm to 123,
 * or some other facade can provide "normal" indexed addressing.
 *
 * @author      Bob Jacobsen  Copyright (C) 2013
 * @version	$Revision: 24246 $
 */
public class TwoIndexTcsProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     */
    public TwoIndexTcsProgrammerFacade(Programmer prog) {
        super(prog);
    }
    
    // these could be constructor arguments, but until there's another decoder
    // this weird, for simplicity we leave them as constants
    static final String indexPI = "201";
    static final String indexSI = "202";
    static final String valMSB  = "203";
    static final String valLSB  = "204";
    static final String readStrobe = "204";  // CV that has to be written before read
    static final String formatFlag = "T2CV"; // flag to indicate this type of CV
    static final int readOffset = 100;

    // members for handling the programmer interface

    int _val;	// remember the value being read/written for confirmative reply
    String _cv;	// remember the cv number being read/written
    int valuePI;  //  value to write to PI or -1
    int valueSI;  //  value to write to SI or -1

    void parseCV(String cv) throws IllegalArgumentException {
        valuePI = -1;
        valueSI = -1;
        if (cv.contains(".")) {
            String[] splits = cv.split("\\.");
            if (splits.length == 3 && splits[0].equals(formatFlag)) {
                valuePI = Integer.parseInt(splits[1]);
                valueSI = Integer.parseInt(splits[2]);
            } else {
                log.error("Invalid CV name: \""+cv+"\"");
                _cv = cv;  // this is a pass through operation
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
        upperByte = 0;
        if (valuePI==-1) { // this is pass through
            state = ProgState.PROGRAMMING;
            prog.writeCV(_cv, val, this);
        } else {
            // write index first
            state = ProgState.DOSIFORWRITE;
            prog.writeCV(indexPI, valuePI, this);
        }
    }

    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    synchronized public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        useProgrammer(p);
        parseCV(CV);
        upperByte = 0;
        if (valuePI==-1) {
            state = ProgState.PROGRAMMING;
            prog.readCV(_cv, this);
        } else {
            // write index first
            state = ProgState.DOSIFORREAD;
            prog.writeCV(indexPI, valuePI+readOffset, this);
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

    enum ProgState { 
        PROGRAMMING,    // doing last read/write, next reply is end
        DOSIFORREAD,    // reading, write to SI next
        DOSTROBEFORREAD, // reading, write to strobe CV next
        DOREADFIRST,    // reading, get MSB next
        FINISHREAD,     // reading, read CV (LSB) next
        DOSIFORWRITE,   // writing, write to SI next
        DOWRITEFIRST,   // writing, write MSB next
        FINISHWRITE,    // writing, write CV (LSB) next
        NOTPROGRAMMING  // idle, doing nothing, no reply expected
    }
    
    ProgState state = ProgState.NOTPROGRAMMING;
    
    int upperByte;
    
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
                temp.programmingOpReply(upperByte*256+value, status);
                break;
            case DOSIFORREAD:
                try {
                    state = ProgState.DOSTROBEFORREAD;
                    prog.writeCV(indexSI, valueSI, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing write SI for read", e);
                }
                break;
            case DOSTROBEFORREAD:
                try {
                    state = ProgState.DOREADFIRST;
                    prog.writeCV(readStrobe, readOffset, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing write strobe for read", e);
                }
                break;
            case DOREADFIRST:
                try {
                    state = ProgState.FINISHREAD;
                    prog.readCV(valMSB, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing write strobe for read", e);
                }
                break;
            case FINISHREAD:
                upperByte = value;
                try {
                    state = ProgState.PROGRAMMING;
                    prog.readCV(valLSB, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final read", e);
                }
                break;
                
            case DOSIFORWRITE:
                try {
                    state = ProgState.DOWRITEFIRST;
                    prog.writeCV(indexSI, valueSI, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing write SI for write", e);
                }
                break;
            case DOWRITEFIRST:
                try {
                    state = ProgState.FINISHWRITE;
                    prog.writeCV(valMSB, _val/256, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing write MSB for write", e);
                }
                break;
            case FINISHWRITE:
                try {
                    state = ProgState.PROGRAMMING;
                    prog.writeCV(valLSB, _val&255, this);
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

    static Logger log = LoggerFactory.getLogger(TwoIndexTcsProgrammerFacade.class.getName());

}

