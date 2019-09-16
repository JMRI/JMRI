package jmri.jmrix.can.cbus;

import jmri.AddressedProgrammer;
import jmri.jmrix.can.TrafficController;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide node variable programmer for CBUS
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Bob Jacobsen Copyright (C) 2008
 * @deprecated since 4.17.1; use {@link jmri.jmrix.can.cbus.node.CbusNode} instead
 */
@Deprecated
public class CbusProgrammerManager extends DefaultProgrammerManager {

    public CbusProgrammerManager(TrafficController tc) {
        super();  // no service mode programmer available
        this.tc = tc;
    }

    TrafficController tc;

    @Override
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

    @Override
    public boolean isAddressedModePossible() {
        return true;
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new CbusProgrammer(pAddress, tc);
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}
