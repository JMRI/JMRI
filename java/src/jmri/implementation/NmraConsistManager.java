package jmri.implementation;

import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;
import jmri.CommandStation;
import jmri.InstanceManager;

/**
 * Default Consist Manager which uses the NmraConsist class for
 * the consists it builds
 *
 * @author Paul Bender Copyright (C) 2003
 * @author Randall Wood Copyright (C) 2013
 */
public class NmraConsistManager extends DccConsistManager {

    private CommandStation commandStation = null;

    public NmraConsistManager() {
        this(InstanceManager.getDefault(CommandStation.class));
    }

    public NmraConsistManager(CommandStation cs) {
        super();
        commandStation = cs;
    }

    @Override
    public Consist addConsist(DccLocoAddress address) {
        if (consistTable.containsKey(address)) {
            return (consistTable.get(address));
        }
        NmraConsist consist;
        consist = new NmraConsist(address,commandStation);
        consistTable.put(address, consist);
        return (consist);
    }
}
