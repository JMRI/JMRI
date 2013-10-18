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
 * command stations that can't address all 1024.
 * It falls back to that mode if the CS can't
 * directly address an requested CV address.
 * In the fall back, 
 * CVs from 0 to "top" are addressed
 * directly. (Top being a supplied parameter)
 * Above the top CV, the upper part of the CV address written to 
 * a specific CV, followed by an write
 * with just the lower part to a second CV, then access
 * to a 3rd CV for the value read/write.
 * The upper and lower parts are calculated using a supplied modulus, e.g. 100.
 * <p>
 * This method is used by some ESU decoders.
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
     * @param modulo Modulus for determining high/low address parts
     */
    public AddressedHighCvProgrammerFacade(Programmer prog, String top, String addrCVhigh, String addrCVlow, String valueCV, String modulo) {
        super(prog);
        this.prog = prog;
        this.top = Integer.parseInt(top);
        this.addrCVhigh = Integer.parseInt(addrCVhigh);
        this.addrCVlow = Integer.parseInt(addrCVlow);
        this.valueCV = Integer.parseInt(valueCV);
        this.modulo = Integer.parseInt(modulo);
        log.debug("Created with "+prog+", "+this.top+", "+this.addrCVhigh+", "+this.addrCVlow+", "+this.valueCV+", "+this.modulo);
    }

    Programmer prog;
    
    int top;
    int addrCVhigh;
    int addrCVlow;
    int valueCV;
    int modulo;


    // members for handling the programmer interface

    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    @Override
    public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        writeCV(""+CV, val, p);
    }
    @Override
    public void writeCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("start writeCV");
        _cv = Integer.parseInt(CV);
        _val = val;
        useProgrammer(p);
        if (prog.getCanRead(CV) || _cv <= top) {
            state = ProgState.PROGRAMMING;
            prog.writeCV(_cv, val, this);
        } else {
            // write index first
            state = ProgState.WRITELOWWRITE;
            prog.writeCV(addrCVhigh, _cv/modulo, this);
        }
    }

    @Override
    public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        confirmCV(""+CV, val, p);
    }
    @Override
    public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    @Override
    public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(""+CV, p);
    }
    @Override
    public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("start readCV");
        _cv = Integer.parseInt(CV);
        useProgrammer(p);
        if (prog.getCanWrite(CV) || _cv <= top) {
            state = ProgState.PROGRAMMING;
            prog.readCV(_cv, this);
        } else {
            // write index first
            state = ProgState.WRITELOWREAD;
            prog.writeCV(addrCVhigh, _cv/modulo, this);
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
                    prog.writeCV(addrCVlow, _cv%modulo, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final read", e);
                }
                break;
            case WRITELOWWRITE:
                try {
                    state = ProgState.FINISHWRITE;
                    prog.writeCV(addrCVlow, _cv%modulo, this);
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
    public boolean getCanRead(String addr) { return Integer.parseInt(addr)<=1024; }
    public boolean getCanRead(int mode, String addr) { return getCanRead(addr); }

    public boolean getCanWrite()  { return true; }
    public boolean getCanWrite(String addr) { return Integer.parseInt(addr)<=1024; }
    public boolean getCanWrite(int mode, String addr)  { return getCanWrite(addr); }

    static Logger log = LoggerFactory.getLogger(AddressedHighCvProgrammerFacade.class.getName());

}

/* @(#)SprogProgrammer.java */
