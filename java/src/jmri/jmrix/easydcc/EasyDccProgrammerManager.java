/* EasyDccProgrammerManager.java */
package jmri.jmrix.easydcc;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for EasyDcc
 * systems
 *
 * @see jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision$
 */
public class EasyDccProgrammerManager extends DefaultProgrammerManager {

    //private Programmer localProgrammer;
    public EasyDccProgrammerManager(Programmer serviceModeProgrammer, EasyDccSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        //    localProgrammer = serviceModeProgrammer;

    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     *
     * @return true
     */
    public boolean isAddressedModePossible() {
        return true;
    }

    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new EasyDccOpsModeProgrammer(pAddress, pLongAddress);
    }

    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}


/* @(#)EasyDccProgrammerManager.java */
