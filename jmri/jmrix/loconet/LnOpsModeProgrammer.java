/* LnOpsModeProgrammer.java */

package jmri.jmrix.loconet;

import java.beans.*;

import jmri.*;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the LocoNet
 * SlotManager object.
 * @see             jmri.Programmer
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision: 1.3 $
 */
public class LnOpsModeProgrammer implements Programmer  {

    SlotManager mSlotMon;
    int mAddress;
    boolean mLongAddr;
    public LnOpsModeProgrammer(SlotManager pSlotMon,
                               int pAddress, boolean pLongAddr) {
        mSlotMon = pSlotMon;
        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }

    /**
     * Forward a write request to an ops-mode write operation
     */
    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        mSlotMon.writeCVOpsMode(CV, val, p, mAddress, mLongAddr);
    }

    public void readCV(int CV, ProgListener p) throws ProgrammerException {
        mSlotMon.readCVOpsMode(CV, p, mAddress, mLongAddr);
    }

    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        mSlotMon.confirmCVOpsMode(CV, val, p, mAddress, mLongAddr);
    }

    public void setMode(int mode) {
        mSlotMon.setMode(mode);
    }

    public int  getMode() {
        return mSlotMon.getMode();
    }

    public boolean hasMode(int mode) {
        return mSlotMon.hasMode(mode);
    }

    /**
     * Can this ops-mode programmer read back values?  For now, no,
     * but maybe later.
     * @return always false for now
     */
    public boolean getCanRead() {
        return false;
    }

    public void addPropertyChangeListener(PropertyChangeListener p) {
        mSlotMon.addPropertyChangeListener(p);
    }
    public void removePropertyChangeListener(PropertyChangeListener p) {
        mSlotMon.removePropertyChangeListener(p);
    }

    public String decodeErrorCode(int i) {
        return mSlotMon.decodeErrorCode(i);
    }

}


/* @(#)LnOpsModeProgrammer.java */
