package jmri.jmrix.easydcc;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for EasyDCC
 * systems.
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Bob Jacobsen Copyright (C) 2002
 */
public class EasyDccProgrammerManager extends DefaultProgrammerManager {

    private EasyDccSystemConnectionMemo _memo = null;
    //private Programmer localProgrammer;

    public EasyDccProgrammerManager(Programmer serviceModeProgrammer, EasyDccSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        _memo = memo;
    }

    /**
     * Works with command station to provide Ops Mode, so say it works.
     *
     * @return always true
     */
    @Override
    public boolean isAddressedModePossible() {
        return true;
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new EasyDccOpsModeProgrammer(pAddress, pLongAddress, _memo);
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

}
