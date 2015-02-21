/* LnOpsModeProgrammer.java */
package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.*;
import java.util.*;

import jmri.*;
import jmri.managers.DefaultProgrammerManager;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the LocoNet
 * SlotManager object.
 *
 * @see jmri.Programmer
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision$
 */
public class LnOpsModeProgrammer implements AddressedProgrammer {

    SlotManager mSlotMgr;
    int mAddress;
    boolean mLongAddr;

    public LnOpsModeProgrammer(SlotManager pSlotMgr,
            int pAddress, boolean pLongAddr) {
        mSlotMgr = pSlotMgr;
        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }

    /**
     * Forward a write request to an ops-mode write operation
     */
    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        mSlotMgr.writeCVOpsMode(CV, val, p, mAddress, mLongAddr);
    }

    public void readCV(int CV, ProgListener p) throws ProgrammerException {
        mSlotMgr.readCVOpsMode(CV, p, mAddress, mLongAddr);
    }

    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        mSlotMgr.confirmCVOpsMode(CV, val, p, mAddress, mLongAddr);
    }

    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
        writeCV(Integer.parseInt(CV), val, p);
    }

    public void readCV(String CV, ProgListener p) throws ProgrammerException {
        readCV(Integer.parseInt(CV), p);
    }

    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        confirmCV(Integer.parseInt(CV), val, p);
    }

    // handle mode
    protected ProgrammingMode mode = DefaultProgrammerManager.OPSBYTEMODE;

    @Override
    public final void setMode(ProgrammingMode m) {
        if (getSupportedModes().contains(m)) {
            mode = m;
            notifyPropertyChange("Mode", mode, m);
        } else {
            throw new IllegalArgumentException("Invalid requested mode: " + m);
        }
    }

    public final ProgrammingMode getMode() {
        return mode;
    }

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(DefaultProgrammerManager.OPSBYTEMODE);
        return ret;
    }

    /**
     * Provide a {@link java.beans.PropertyChangeSupport} helper.
     */
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param listener The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void notifyPropertyChange(String key, Object oldValue, Object value) {
        propertyChangeSupport.firePropertyChange(key, oldValue, value);
    }

    /**
     * Can this ops-mode programmer read back values? Yes, if transponding
     * hardware is present, but we don't check that here.
     *
     * @return always true
     */
    @Override
    public boolean getCanRead() {
        return true;
    }

    @Override
    public boolean getCanRead(String addr) {
        return getCanRead();
    }

    @Override
    public boolean getCanWrite() {
        return true;
    }

    @Override
    public boolean getCanWrite(String addr) {
        return getCanWrite() && Integer.parseInt(addr) <= 1024;
    }

    public String decodeErrorCode(int i) {
        return mSlotMgr.decodeErrorCode(i);
    }

    public boolean getLongAddress() {
        return mLongAddr;
    }

    public int getAddressNumber() {
        return mAddress;
    }

    public String getAddress() {
        return "" + getAddressNumber() + " " + getLongAddress();
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(LnOpsModeProgrammer.class.getName());

}

/* @(#)LnOpsModeProgrammer.java */
