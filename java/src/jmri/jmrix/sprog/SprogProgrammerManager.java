package jmri.jmrix.sprog;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.jmrix.sprog.SprogConstants.SprogMode;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide programmers for SPROG systems.
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author	Andrew crosland Copyright (C) 2001
 */
public class SprogProgrammerManager extends DefaultProgrammerManager {

    //private Programmer localProgrammer;
    private SprogMode mode;
    private SprogSystemConnectionMemo adapterMemo = null;

    public SprogProgrammerManager(Programmer serviceModeProgrammer, SprogSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        //localProgrammer = serviceModeProgrammer;
        this.mode = SprogMode.SERVICE;
        adapterMemo = memo;
    }

    public SprogProgrammerManager(Programmer serviceModeProgrammer, SprogMode mode, SprogSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        //localProgrammer = serviceModeProgrammer;
        this.mode = mode;
        adapterMemo = memo;
    }

    /**
     * Classic SPROG is service mode only. SPROG Command Station is Ops mode only.
     *
     * @return true for SPROG Command Station
     */
    @Override
    public boolean isAddressedModePossible() {
        if (mode == SprogMode.OPS) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isGlobalProgrammerAvailable() {
        if (mode == SprogMode.SERVICE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new SprogOpsModeProgrammer(pAddress, pLongAddress, adapterMemo);
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

}
