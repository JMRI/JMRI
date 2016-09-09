package jmri.server.json.consist;

import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.implementation.AbstractConsistManager;

/**
 * ConsistManager for the JSON services. This consist manager passes requests for CS consisting to the 
 *
 * @author Randall Wood Copyright (C) 2016
 */
public class JsonConsistManager extends AbstractConsistManager {

    final ConsistManager systemConsistManager;

    public JsonConsistManager() {
        systemConsistManager = InstanceManager.getNullableDefault(ConsistManager.class);
    }

    @Override
    protected Consist addConsist(DccLocoAddress address) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isCommandStationConsistPossible() {
        if (systemConsistManager != null) {
            return systemConsistManager.isCommandStationConsistPossible();
        }
        return false;
    }

    @Override
    public boolean csConsistNeedsSeperateAddress() {
        if (systemConsistManager != null) {
            return systemConsistManager.csConsistNeedsSeperateAddress();
        }
        return false;
    }

}
