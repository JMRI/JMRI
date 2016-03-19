package jmri.jmrix.can.cbus;

import jmri.AddressedProgrammer;
import jmri.jmrix.can.TrafficController;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide node variable programmer for CBUS
 *
 * @see jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class CbusProgrammerManager extends DefaultProgrammerManager {

    public CbusProgrammerManager(TrafficController tc) {
        super(null);  // no service mode programmer available
        this.tc = tc;
    }

    TrafficController tc;

    public String getUserName() {
        return "MERG";
    }

    /**
     * Global not relevant for CBUS
     *
     * @return false
     */
    public boolean isGlobalModePossible() {
        return false;
    }

    public boolean isAddressedModePossible() {
        return true;
    }

    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new CbusProgrammer(pAddress, tc);
    }

    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}
