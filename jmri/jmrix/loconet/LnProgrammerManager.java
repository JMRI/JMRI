/* LnProgrammerManager.java */

package jmri.jmrix.loconet;

import jmri.DefaultProgrammerManager;
import jmri.Programmer;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers on LocoNet
 *
 * @see         jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision: 1.3 $
 */
public class LnProgrammerManager  extends DefaultProgrammerManager {

    private Programmer mProgrammer;

    public LnProgrammerManager(SlotManager pSlotManager) {
        super(pSlotManager);
        mSlotManager = pSlotManager;
    }

    SlotManager mSlotManager;

    /**
     * LocoNet command station does provide Ops Mode
     * @return true
     */
    public boolean isOpsModePossible() {return true;}

    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return new LnOpsModeProgrammer(mSlotManager, pAddress, pLongAddress);
    }

    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    };
    public void releaseOopsModeProgrammer(Programmer p) {}
}


/* @(#)DefaultProgrammerManager.java */
