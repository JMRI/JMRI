/**
 * NmraConsistManager.java
 *
 * Description: The Default Consist Manager which uses the NmraConsist class for
 * the consists it builds
 *
 * @author Paul Bender Copyright (C) 2003
 * @author Randall Wood Copyright (C) 2013
 * @version $Revision$
 */
package jmri.implementation;

import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;

public class NmraConsistManager extends DccConsistManager implements ConsistManager {

    public NmraConsistManager() {
        super();
    }

    @Override
    public Consist addConsist(DccLocoAddress address) {
        if (consistTable.containsKey(address)) {
            return (consistTable.get(address));
        }
        NmraConsist consist;
        consist = new NmraConsist(address);
        consistTable.put(address, consist);
        return (consist);
    }
}
