/* TamsProgrammerManager.java */
package jmri.jmrix.tams;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for NCE
 * systems
 *
 * @see jmri.managers.DefaultProgrammerManager Based on work by Bob Jacobsen
 * @author	Kevin Dickerson Copyright (C) 2012
 *
 */
public class TamsProgrammerManager extends DefaultProgrammerManager {

    TamsTrafficController tc;

    public TamsProgrammerManager(Programmer serviceModeProgrammer, TamsSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        this.tc = memo.getTrafficController();
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

    /**
     * Works with PH command station to provide Service Mode and USB connect to
     * PowerCab.
     *
     * @return true if not USB connect to SB3
     */
    @Override
    public boolean isGlobalProgrammerAvailable() {
        return true;
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new TamsOpsModeProgrammer(tc, pAddress, pLongAddress);
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}
