/* LnProgrammerManager.java */

package jmri.jmrix.loconet;

import jmri.*;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers on LocoNet
 *
 * @see         jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision: 1.1 $
 */
public class LnProgrammerManager  extends DefaultProgrammerManager {

    private Programmer mProgrammer;

    public LnProgrammerManager(SlotManager slotManager) {
        super(slotManager);
    }


    public Programmer getOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    public Programmer reserveOpsModeProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    };
    public void releaseOopsModeProgrammer(Programmer p) {}
}


/* @(#)DefaultProgrammerManager.java */
