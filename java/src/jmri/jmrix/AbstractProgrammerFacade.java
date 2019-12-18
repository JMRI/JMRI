package jmri.jmrix;

import java.beans.PropertyChangeListener;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

/**
 * Common implementations of the Programmer interface for making Programmer
 * facade classes.
 *
 * @author Bob Jacobsen Copyright (C) 2013
 */
public abstract class AbstractProgrammerFacade implements Programmer {

    protected Programmer prog;

    public AbstractProgrammerFacade(Programmer prog) {
        this.prog = prog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String decodeErrorCode(int code) {
        return prog.decodeErrorCode(code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        prog.addPropertyChangeListener(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        prog.removePropertyChangeListener(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
        prog.writeCV(CV, val, p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readCV(String CV, ProgListener p) throws ProgrammerException {
        prog.readCV(CV, p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        prog.confirmCV(CV, val, p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProgrammingMode getMode() {
        return prog.getMode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        return prog.getSupportedModes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMode(ProgrammingMode p) {
        prog.setMode(p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCanRead() {
        return prog.getCanRead();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCanRead(String addr) {
        return prog.getCanRead(addr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCanWrite() {
        return prog.getCanWrite();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCanWrite(String addr) {
        return prog.getCanWrite(addr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public WriteConfirmMode getWriteConfirmMode(String addr) { return prog.getWriteConfirmMode(addr); }

}
