/* LnOpsModeProgrammer.java */

package jmri.jmrix.loconet;

import java.beans.*;

import jmri.*;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the LocoNet
 * SlotManager object.
 * @see             jmri.Programmer
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision: 1.1 $
 */
public class LnOpsModeProgrammer implements Programmer  {

    SlotManager mSlotMon;
    public LnOpsModeProgrammer(SlotManager pSlotMon) {
        mSlotMon = pSlotMon;
    }

    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        mSlotMon.writeCV(CV, val, p);
    }

    public void readCV(int CV, ProgListener p) throws ProgrammerException {
        mSlotMon.readCV(CV, p);
    }

    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        mSlotMon.confirmCV(CV, val, p);
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

    public boolean getCanRead() {
        return mSlotMon.getCanRead();
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
