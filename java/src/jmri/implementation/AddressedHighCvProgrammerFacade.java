// AddressedHighCvProgrammerFacade.java

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
 * Programmer facade, at this point just an example.
 * <p>
 * This is for decoders that have an
 * alternate high-CV access method for
 * command stations that can't address all 2048.
 * It falls back to that mode if the CS can't
 * directly address an requested CV address.
 * In the fall back, 
 * CVs from 0 to "top" are addressed
 * directly. (Top being a power of two)
 * Above the top CV, the upper bits are written to 
 * a specific CV, followed by an operation
 * with just the lower bits. This works
 * for CV addresses up to some known "max" value. 
 *
 * @author      Bob Jacobsen  Copyright (C) 2013
 * @version	$Revision: 24246 $
 */
public class AddressedHighCvProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * @param top CVs above this use the indirect method
     * @param addrCVhigh  CV to which the high part of address is to be written
     * @param addrCVlow  CV to which the low part of address is to be written
     * @param valueCV Value read/written here once address has been written
     */
    public AddressedHighCvProgrammerFacade(Programmer prog, String top, String addrCVhigh, String addrCVlow, String valueCV) {
        super(prog);
        this.prog = prog;
        this.top = Integer.parseInt(top);
        this.addrCVhigh = Integer.parseInt(addrCVhigh);
        this.addrCVlow = Integer.parseInt(addrCVlow);
        this.valueCV = Integer.parseInt(valueCV);
    }

    Programmer prog;
    
    int top;
    int addrCVhigh;
    int addrCVlow;
    int valueCV;


    // members for handling the programmer interface

    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    synchronized public void writeCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        _cv = Integer.parseInt(CV);
        _val = val;
        useProgrammer(p);
        if (prog.getCanRead(CV) || _cv <= top) {
            state = ProgState.PROGRAMMING;
            prog.writeCV(_cv, val, this);
        } else {
            // write index first
            state = ProgState.WRITELOWWRITE;
            prog.writeCV(addrCVhigh, _cv/top, this);
        }
    }

    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    synchronized public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        _cv = Integer.parseInt(CV);
        useProgrammer(p);
        if (prog.getCanWrite(CV) || _cv <= top) {
            state = ProgState.PROGRAMMING;
            prog.readCV(_cv, this);
        } else {
            // write index first
            state = ProgState.WRITELOWREAD;
            prog.writeCV(addrCVhigh, _cv/top, this);
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

    enum ProgState { PROGRAMMING, WRITELOWREAD, WRITELOWWRITE, FINISHREAD, FINISHWRITE, NOTPROGRAMMING }
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
            case WRITELOWREAD:
                try {
                    state = ProgState.FINISHREAD;
                    prog.writeCV(addrCVlow, _cv%top, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final read", e);
                }
                break;
            case WRITELOWWRITE:
                try {
                    state = ProgState.FINISHWRITE;
                    prog.writeCV(addrCVlow, _cv%top, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final write", e);
                }
                break;
            case FINISHREAD:
                try {
                    state = ProgState.PROGRAMMING;
                    prog.readCV(valueCV, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final read", e);
                }
                break;
            case FINISHWRITE:
                try {
                    state = ProgState.PROGRAMMING;
                    prog.writeCV(valueCV, _val, this);
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

    // Access to full address space provided by this.
    public boolean getCanRead() { return true; }
    public boolean getCanRead(String addr) { return Integer.parseInt(addr)<=2048; }
    public boolean getCanRead(int mode, String addr) { return getCanRead(addr); }

    public boolean getCanWrite()  { return true; }
    public boolean getCanWrite(String addr) { return Integer.parseInt(addr)<=2048; }
    public boolean getCanWrite(int mode, String addr)  { return getCanWrite(addr); }

    static Logger log = LoggerFactory.getLogger(AddressedHighCvProgrammerFacade.class.getName());

}

/* @(#)SprogProgrammer.java */
