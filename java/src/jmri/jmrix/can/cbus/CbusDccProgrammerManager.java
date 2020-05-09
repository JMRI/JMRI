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

    private boolean _isAddressedModePossible = true;
    private boolean _isGlobalProgrammerAvailable = true;
    
    public CbusDccProgrammerManager(Programmer serviceModeProgrammer, CanSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        tc = memo.getTrafficController();
    }

    jmri.jmrix.can.TrafficController tc;

    /**
     * MERG CAN_CMD supports ops mode
     *
     * @return true
     */
    @Override
    public boolean isAddressedModePossible() {
        return _isAddressedModePossible;
    }

    /**
     * Set availability of addressed (ops mode) programmer.
     * 
     * @param state true if available
     */
    public void setAddressedModePossible(boolean state) {
        boolean old = _isAddressedModePossible;
        _isAddressedModePossible = state;
        firePropertyChange("addressedModePossible", old, state);
    }

    /**
     * MERG CAN_CMD supports service mode
     *
     * @return true
     */
    @Override
    public boolean isGlobalProgrammerAvailable() {
        return _isGlobalProgrammerAvailable;
    }
    
    /**
     * Set availability of global (service mode) programmer.
     * 
     * @param state true if available
     */
    public void setGlobalProgrammerAvailable(boolean state) {
        boolean old = _isGlobalProgrammerAvailable;
        _isGlobalProgrammerAvailable = state;
        firePropertyChange("globalProgrammerAvailable", old, state);
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new CbusDccOpsModeProgrammer(pAddress, pLongAddress, tc);
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}
