package jmri.jmrix;

import java.beans.PropertyChangeListener;
import java.util.List;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

/**
 * Common implementations of the Programmer interface for making Programmer
 * facade classes.
 *
 * @author	Bob Jacobsen Copyright (C) 2013
 */
public abstract class AbstractProgrammerFacade implements Programmer {

    protected Programmer prog;

    public AbstractProgrammerFacade(Programmer prog) {
        this.prog = prog;
    }

    @Override
    public String decodeErrorCode(int code) {
        return prog.decodeErrorCode(code);
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        prog.addPropertyChangeListener(l);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        prog.removePropertyChangeListener(l);
    }

    @Override
    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        prog.writeCV(CV, val, p);
    }

    @Override
    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
        prog.writeCV(CV, val, p);
    }

    @Override
    public void readCV(int CV, ProgListener p) throws ProgrammerException {
        prog.readCV(CV, p);
    }

    @Override
    public void readCV(String CV, ProgListener p) throws ProgrammerException {
        prog.readCV(CV, p);
    }

    @Override
    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        prog.confirmCV(CV, val, p);
    }

    @Override
    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        prog.confirmCV(CV, val, p);
    }

    @Override
    public ProgrammingMode getMode() {
        return prog.getMode();
    }

    @Override
    public List<ProgrammingMode> getSupportedModes() {
        return prog.getSupportedModes();
    }

    @Override
    public void setMode(ProgrammingMode p) {
        prog.setMode(p);
    }

    @Override
    public boolean getCanRead() {
        return prog.getCanRead();
    }

    @Override
    public boolean getCanRead(String addr) {
        return prog.getCanRead(addr);
    }

    @Override
    public boolean getCanWrite() {
        return prog.getCanWrite();
    }

    @Override
    public boolean getCanWrite(String addr) {
        return prog.getCanWrite(addr);
    }

}
