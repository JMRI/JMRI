/* MrcProgrammerManager.java */
package jmri.jmrix.mrc;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for MRC
 * systems
 *
 * @see jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author	Ken Cameron Copyright (C) 2014
 * @author Kevin Dickerson Copyright (C) 2014
 * @version	$Revision: 23001 $
 */
public class MrcProgrammerManager extends DefaultProgrammerManager {

    MrcTrafficController tc;

    public MrcProgrammerManager(Programmer serviceModeProgrammer, MrcSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        this.tc = memo.getMrcTrafficController();
    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     *
     * @return true
     */
    public boolean isAddressedModePossible() {
        return true;
    }

    /**
     * @return true
     */
    public boolean isGlobalProgrammerAvailable() {
        return true;
    }

    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new MrcOpsModeProgrammer(tc, pAddress, pLongAddress);
    }

    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}


/* @(#)MrcProgrammerManager.java */
