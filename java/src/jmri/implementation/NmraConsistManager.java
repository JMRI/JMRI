package jmri.implementation;

import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.CommandStation;

/**
 * Default Consist Manager which uses the NmraConsist class for
 * the consists it builds
 *
 * @author Paul Bender Copyright (C) 2003
 * @author Randall Wood Copyright (C) 2013
 */
public class NmraConsistManager extends AbstractConsistManager {

    private CommandStation commandStation = null;

    public NmraConsistManager(CommandStation cs) {
        super();
        commandStation = cs;
    }

    @Override
    public Consist addConsist(LocoAddress address) {
        if (! (address instanceof DccLocoAddress)) {
            throw new IllegalArgumentException("address is not a DccLocoAddress object");
        }
        if (consistTable.containsKey(address)) {
            return (consistTable.get(address));
        }
        NmraConsist consist;
        consist = new NmraConsist((DccLocoAddress) address, commandStation);
        consistTable.put(address, consist);
        return (consist);
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
