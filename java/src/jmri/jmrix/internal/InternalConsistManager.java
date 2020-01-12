package jmri.jmrix.internal;

import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.implementation.AbstractConsistManager;

/**
 * Default Consist Manager which uses the NmraConsist class for
 * the consists it builds.
 *
 * @author Paul Bender Copyright (C) 2003
 * @author Randall Wood Copyright (C) 2013
 */
public class InternalConsistManager extends AbstractConsistManager {

    public InternalConsistManager() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCommandStationConsistPossible(){
       return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean csConsistNeedsSeperateAddress(){
      return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Consist addConsist(LocoAddress address) {
        if (! (address instanceof DccLocoAddress)) {
            throw new IllegalArgumentException("address is not a DccLocoAddress object");
        }
        if (consistTable.containsKey(address)) {
            return (consistTable.get(address));
        }
        Consist consist = null;
        if (InstanceManager.getNullableDefault(jmri.CommandStation.class) != null ) {
           consist = new jmri.implementation.NmraConsist((DccLocoAddress) address);
        }
        else if (InstanceManager.getNullableDefault(jmri.AddressedProgrammerManager.class) != null){
           consist = new jmri.implementation.DccConsist((DccLocoAddress) address);
        }
        if (consist != null) {
           consistTable.put(address, consist);
        }
        return (consist); 
    }

}
