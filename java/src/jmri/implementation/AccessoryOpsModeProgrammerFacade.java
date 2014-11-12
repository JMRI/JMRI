// MultiIndexProgrammerFacade.java

package jmri.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import jmri.jmrix.AbstractProgrammerFacade;

/**
 * Programmer facade for access to Accessory Decoder Ops Mode programming
 * <p>
 * (Eventually implements four modes, passing all others to underlying programmer:
 * <ul>
 * <li>
 * <li>
 * <li>
 * <li>
 * </ul>
 * <P>
 * Used through the String write/read/confirm interface.  Accepts integers
 * as addresses, but then emits NMRA DCC packets through the 
 * default CommandStation interface (which must be present)
 *
 * @see jmri.implementation.ProgrammerFacadeSelector
 *
 * @author      Bob Jacobsen  Copyright (C) 2014
 */
// @ToDo("transform to annotations requires e.g. http://alchemy.grimoire.ca/m2/sites/ca.grimoire/todo-annotations/")
// @ToDo("get address from underlyng programmer (which might require adding a new subclass structure to Programmer)")
// @ToDo("finish mode handling; what gets passed through?")
// @ToDo("write almost certainly needs a delay")
// @ToDo("read handling needs to be aligned with other ops mode programmers")
// @ToDo("make sure jmri/jmrit/progsupport/ProgServiceModePane shows the modes, and that DP/DP3 displays them as it configures a decoder")

public class AccessoryOpsModeProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    public AccessoryOpsModeProgrammerFacade(Programmer prog) {
        super(prog);
        this.mode = prog.getMode();
    }
    
    // ops accessory mode can't read locally
    int mode;
    boolean checkMode(int mode) {
        switch (mode) {
            case Programmer.OPSACCBYTEMODE:
            case Programmer.OPSACCBITMODE:
            case Programmer.OPSACCEXTBYTEMODE:
            case Programmer.OPSACCEXTBITMODE:
                return true;
            default: 
                return false;
        }
    }
    
    public boolean hasMode(int mode) {
        if (checkMode(mode)) return true;
        return prog.hasMode(mode);
    }
    
    public int getMode() { return prog.getMode(); }
    public void setMode(int mode) { 
        if (checkMode(mode)) {
            this.mode = mode;
        } else {
            prog.setMode(mode); 
            this.mode = prog.getMode();
        }
    }

    public boolean getCanRead() { return prog.getCanRead(); }
    public boolean getCanRead(String addr) { return prog.getCanRead(addr); }
    public boolean getCanRead(int mode, String addr) { return prog.getCanRead(mode, addr); }
    
    public boolean getCanWrite()  { return prog.getCanWrite(); }
    public boolean getCanWrite(String addr) { return prog.getCanWrite(addr); }
    public boolean getCanWrite(int mode, String addr)  { return prog.getCanWrite(mode, addr); }


    // members for handling the programmer interface
    int _val;	// remember the value being read/written for confirmative reply
    String _cv;	// remember the cv number being read/written
    

    // programming interface
    synchronized public void writeCV(String cv, int val, ProgListener p) throws ProgrammerException {
        _val = val;
        useProgrammer(p);
        state = ProgState.PROGRAMMING;
        
        // send DCC command to implement prog.writeCV(cv, val, this);
        byte[] b = NmraPacket.accDecoderPktOpsMode(333, Integer.parseInt(cv), val);
        InstanceManager.getDefault(CommandStation.class).sendPacket(b,1);
        
        // and reply done
        p.programmingOpReply(val, ProgListener.OK);
    }

    synchronized public void confirmCV(String cv, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(cv, p);
    }

    synchronized public void readCV(String cv, jmri.ProgListener p) throws jmri.ProgrammerException {
        useProgrammer(p);
        state = ProgState.PROGRAMMING;
        prog.readCV(cv, this);
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

    enum ProgState { PROGRAMMING, NOTPROGRAMMING }
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
            default:
                log.error("Unexpected state on reply: "+state);
                // clean up as much as possible
                _usingProgrammer = null;
                state = ProgState.NOTPROGRAMMING;
                
        }
    }

    static Logger log = LoggerFactory.getLogger(AccessoryOpsModeProgrammerFacade.class.getName());

}

