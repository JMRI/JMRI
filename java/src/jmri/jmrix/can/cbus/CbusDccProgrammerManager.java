package jmri.jmrix.can.cbus;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide programmers for CBUS systems
 *
 * Added methods to manipulate the programmer availability.
 * 
 * @see jmri.managers.DefaultProgrammerManager
 * @author Andrew crosland Copyright (C) 2009, 2020
 */
public class CbusDccProgrammerManager extends DefaultProgrammerManager {

    public CbusDccProgrammerManager(Programmer serviceModeProgrammer, CanSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        tc = memo.getTrafficController();
    }

    jmri.jmrix.can.TrafficController tc;

    /**
     * MERG CAN_CMD may support ops mode
     *
     * @return true
     */
    @Override
    public boolean isAddressedModePossible() {
        return ((_type == ProgrammerType.BOTH) || (_type == ProgrammerType.ADDRESSED));
    }

    /**
     * MERG CAN_CMD may support service mode
     *
     * @return true
     */
    @Override
    public boolean isGlobalProgrammerAvailable() {
        return ((_type == ProgrammerType.BOTH) || (_type == ProgrammerType.GLOBAL));
    }
    
    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new CbusDccOpsModeProgrammer(pAddress, pLongAddress, tc);
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    @Override
    public void setProgrammerType(ProgrammerType type) {
        super.setProgrammerType(type);
    }
}
