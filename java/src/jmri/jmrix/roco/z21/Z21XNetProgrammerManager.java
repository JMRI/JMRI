package jmri.jmrix.roco.z21;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;

/**
 * Extend XNetProgrammerManager for Z21.
 * @see jmri.jmrix.lenz.XNetProgrammerManager
 * @author Paul Bender Copyright (C) 2018
 * @navassoc 1 - 1 jmri.jmrix.roco.z21.Z21XNetProgrammer
 * @navassoc 1 - * jmri.jmrix.roco.z21.Z21XNetOpsModeProgrammer
 */
public class Z21XNetProgrammerManager extends jmri.jmrix.lenz.XNetProgrammerManager {

    public Z21XNetProgrammerManager(Programmer pProgrammer, XNetSystemConnectionMemo memo) {
        super(pProgrammer, memo);
    }

    /**
     * Z21 Command stations do provide Ops Mode.
     * @return true
     */
    @Override
    public boolean isAddressedModePossible() {
        return true; 
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new Z21XNetOpsModeProgrammer(pAddress, tc);
    }

}
