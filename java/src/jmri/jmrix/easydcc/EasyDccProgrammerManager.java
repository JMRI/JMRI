/* EasyDccProgrammerManager.java */
package jmri.jmrix.easydcc;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for EasyDcc
 * systems
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Bob Jacobsen Copyright (C) 2002
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
    @Override
    public boolean isAddressedModePossible() {
        return true;
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new EasyDccOpsModeProgrammer(pAddress, pLongAddress);
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}



