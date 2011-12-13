/* LnProgrammerManager.java */

package jmri.jmrix.loconet;

import jmri.managers.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers on LocoNet
 *
 * @see         jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision$
 */
public class LnProgrammerManager  extends DefaultProgrammerManager {

    //private Programmer mProgrammer;

    public LnProgrammerManager(SlotManager pSlotManager, LocoNetSystemConnectionMemo memo) {
        super(pSlotManager, memo);
        mSlotManager = pSlotManager;
    }

    SlotManager mSlotManager;

    /**
     * LocoNet command station does provide Ops Mode
     * @return true
     */
    public boolean isAddressedModePossible() {return true;}

    public Programmer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new LnOpsModeProgrammer(mSlotManager, pAddress, pLongAddress);
    }

    public Programmer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}


/* @(#)DefaultProgrammerManager.java */
