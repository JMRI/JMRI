package jmri.implementation;

import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.AddressedProgrammerManager;

/**
 * The Default Consist Manager which uses the DccConsist class for
 * the consists it builds. This implementation just tracks the
 * consist via a table of {@link jmri.implementation.DccConsist} objects
 * that handle the actual operations.
 *
 * @author Paul Bender Copyright (C) 2003
 * @author Randall Wood Copyright (C) 2013
 */
public class DccConsistManager extends AbstractConsistManager {

    private AddressedProgrammerManager opsProgManager = null;

    public DccConsistManager(AddressedProgrammerManager apm) {
        super();
        opsProgManager = apm;
    }

    @Override
    public Consist addConsist(LocoAddress address) {
        if (! (address instanceof DccLocoAddress)) {
            throw new IllegalArgumentException("address is not a DccLocoAddress object");
        }
        if (consistTable.containsKey(address)) {
            return consistTable.get(address);
        }
        DccConsist consist;
        consist = new DccConsist((DccLocoAddress) address,opsProgManager);
        consistTable.put(address, consist);
        return consist;
    }

    /**
     * This implementation does NOT support Command Station consists, so return
     * false.
     */
    @Override
    public boolean isCommandStationConsistPossible() {
        return false;
    }

    /**
     * Does a CS consist require a separate consist address? This implementation
     * does not support Command Station consists, so return false
     */
    @Override
    public boolean csConsistNeedsSeperateAddress() {
        return false;
    }
}
