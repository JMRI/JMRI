package jmri.implementation;

import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;

/**
 * The Default Consist Manager which uses the DccConsist class for
 * the consists it builds
 *
 * @author Paul Bender Copyright (C) 2003
 * @author Randall Wood Copyright (C) 2013
 */
public class DccConsistManager extends AbstractConsistManager implements ConsistManager {

    public DccConsistManager() {
        super();
    }

    @Override
    public Consist addConsist(DccLocoAddress address) {
        if (consistTable.containsKey(address)) {
            return consistTable.get(address);
        }
        DccConsist consist;
        consist = new DccConsist(address);
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
