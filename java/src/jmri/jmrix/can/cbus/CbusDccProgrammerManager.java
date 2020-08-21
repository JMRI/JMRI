package jmri.jmrix.can.cbus;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.DefaultProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend DefaultProgrammerManager to provide programmers for CBUS systems
 *
 * Added methods to manipulate the programmer availability to support hardware
 * that can redirect ops mode or service mode packets to a particular interface.
 * 
 * @see jmri.managers.DefaultProgrammerManager
 * @author Andrew crosland Copyright (C) 2009, 2020
 */
public class CbusDccProgrammerManager extends DefaultProgrammerManager {

    private boolean _isAddressedModePossible = true;
    private boolean _isGlobalProgrammerAvailable = true;
    
    private CbusPreferences prefs;

    public CbusDccProgrammerManager(Programmer serviceModeProgrammer, CanSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        tc = memo.getTrafficController();
        prefs = jmri.InstanceManager.getDefault(jmri.jmrix.can.cbus.CbusPreferences.class);
        mySetAddressedModePossible(prefs.isAddressedModePossible());
        mySetGlobalProgrammerAvailable(prefs.isGlobalProgrammerAvailable());
        log.info("Preferences for programmers: global {} addressed {}", prefs.isGlobalProgrammerAvailable(), prefs.isAddressedModePossible());
    }

    jmri.jmrix.can.TrafficController tc;

    /**
     * CBUS DCC Programmer has hardware support for ops mode
     *
     * @return true
     */
    public boolean isAddressedModeHardwareAvailable() {
        return true;
    }

    /**
     * CBUS DCC Programmer has hardware support for service mode
     *
     * @return true if available
     */
    public boolean isGlobalProgrammerHardwareAvailable() {
        return true;
    }
    
    /**
     * Does Programmer currently support ops mode
     *
     * @return true if possible
     */
    @Override
    public boolean isAddressedModePossible() {
        return _isAddressedModePossible;
    }

    /**
     * Set availability of addressed (ops mode) programmer.
     * To avoid calling overridable method from constructor
     * 
     * @param state true if possible
     */
    public final void mySetAddressedModePossible(boolean state) {
        boolean old = _isAddressedModePossible;
        _isAddressedModePossible = state;
        firePropertyChange("addressedModePossible", old, state);
    }

    /**
     * Set availability of addressed (ops mode) programmer.
     * 
     * @param state true if available
     */
    public void setAddressedModePossible(boolean state) {
        mySetAddressedModePossible(state);
    }

    /**
     * Programmer currently support service mode
     *
     * @return true if available
     */
    @Override
    public boolean isGlobalProgrammerAvailable() {
        return _isGlobalProgrammerAvailable;
    }
    
    /**
     * Set availability of global (service mode) programmer.
     * To avoid calling overridable method from constructor
     * 
     * @param state true if available
     */
    public final void mySetGlobalProgrammerAvailable(boolean state) {
        boolean old = _isGlobalProgrammerAvailable;
        _isGlobalProgrammerAvailable = state;
        firePropertyChange("globalProgrammerAvailable", old, state);
    }

    /**
     * Set availability of global (service mode) programmer.
     * 
     * @param state true if available
     */
    public void setGlobalProgrammerAvailable(boolean state) {
        mySetGlobalProgrammerAvailable(state);
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new CbusDccOpsModeProgrammer(pAddress, pLongAddress, tc);
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusDccProgrammerManager.class);
    
}
