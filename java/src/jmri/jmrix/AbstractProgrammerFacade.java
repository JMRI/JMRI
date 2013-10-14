// AbstractProgrammerFacade.java

package jmri.jmrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import java.beans.PropertyChangeListener;
import java.util.Vector;

/**
 * Common implementations of the Programmer interface
 * for making Programmer facade classes.
 *
 * @author	Bob Jacobsen  Copyright (C) 2013
 * @version     $Revision$
 */
public abstract class AbstractProgrammerFacade implements Programmer {

    protected Programmer prog;
    
    public AbstractProgrammerFacade(Programmer prog) {
        this.prog = prog;
    }
    
    public String decodeErrorCode(int code) {
        return prog.decodeErrorCode(code);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        prog.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        prog.removePropertyChangeListener(l);
    }

    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        prog.writeCV(CV, val, p);
    }
    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
        prog.writeCV(CV, val, p);
    }
    public void readCV(int CV, ProgListener p) throws ProgrammerException {
        prog.readCV(CV, p);
    }
    public void readCV(String CV, ProgListener p) throws ProgrammerException {
        prog.readCV(CV, p);
    }
    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        prog.confirmCV(CV, val, p);
    }
    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        prog.confirmCV(CV, val, p);
    }

    public boolean hasMode(int mode) {
        return prog.hasMode(mode);
    }
    public int getMode() { return prog.getMode(); }
    public void setMode(int mode) { prog.setMode(mode); }

    public boolean getCanRead() { return prog.getCanRead(); }
    public boolean getCanRead(String addr) { return prog.getCanRead(addr); }
    public boolean getCanRead(int mode, String addr) { return prog.getCanRead(mode, addr); }
    
    public boolean getCanWrite()  { return prog.getCanWrite(); }
    public boolean getCanWrite(String addr) { return prog.getCanWrite(addr); }
    public boolean getCanWrite(int mode, String addr)  { return prog.getCanWrite(mode, addr); }

}

