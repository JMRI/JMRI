package jmri.implementation;

import jmri.Consist;
import jmri.ConsistManager;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

/**
 * Default Consist Manager which uses the NmraConsist class for
 * the consists it builds
 *
 * @author Paul Bender Copyright (C) 2003
 * @author Randall Wood Copyright (C) 2013
 */
public class NmraConsistManager extends DccConsistManager implements ConsistManager {

    public NmraConsistManager() {
        super();
    }

    @Override
    public Consist addConsist(LocoAddress address) {
        if (consistTable.containsKey(address)) {
            return (consistTable.get(address));
        }
        NmraConsist consist;
        consist = new NmraConsist((DccLocoAddress) address);
        consistTable.put(address, consist);
        return (consist);
    }
}
