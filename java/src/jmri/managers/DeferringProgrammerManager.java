/* DeferringProgrammerManager.java */

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;

/**
 * Defers GlobalProgrammer operations to the default GlobalProgrammer, 
 * and AddressedProgrammer operations to the default AddressedProgrammer.
 *<p>
 * The underlying Programmer is looked up for each access to ensure that
 * it's current. 
 *
 * @see             jmri.ProgrammerManager
 * @author			Bob Jacobsen Copyright (C) 2014
 * @version			$Revision$
 */
public class DeferringProgrammerManager implements ProgrammerManager {

    public DeferringProgrammerManager() {
    }

    String userName = "<Default>";

    /**
     * Provides the human-readable 
     * representation for including ProgrammerManagers
     * directly in e.g. JComboBoxes, so it should return a
     * user-provided name for this particular one.
     */
    public String getUserName() { return userName; }
    
    /**
     * Provides the human-readable 
     * representation for including ProgrammerManagers
     * directly in e.g. JComboBoxes, so it should return a
     * user-provided name for this particular one.
     */
    public String toString() { return getUserName(); }
    
    public Programmer getGlobalProgrammer() {
        if (log.isDebugEnabled()) log.debug("return default service-mode programmer");
        return InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
    }

    public Programmer reserveGlobalProgrammer() {
        return InstanceManager.getDefault(GlobalProgrammerManager.class).reserveGlobalProgrammer();
    }
    public void releaseGlobalProgrammer(Programmer p) {
        InstanceManager.getDefault(GlobalProgrammerManager.class).releaseGlobalProgrammer(p);
    }

    /**
     * Allow for implementations that do not support Service mode programming
     * @return false if there's no chance of getting one
     */
    public boolean isGlobalProgrammerAvailable() {
        return InstanceManager.getDefault(GlobalProgrammerManager.class).isGlobalProgrammerAvailable();
    }

    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return InstanceManager.getDefault(AddressedProgrammerManager.class).getAddressedProgrammer(pLongAddress, pAddress);
    }
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return InstanceManager.getDefault(AddressedProgrammerManager.class).reserveAddressedProgrammer(pLongAddress, pAddress);
    }
    public void releaseAddressedProgrammer(AddressedProgrammer p) {
        InstanceManager.getDefault(AddressedProgrammerManager.class).releaseAddressedProgrammer(p);
    }

    /**
     * Default programmer does not provide Ops Mode
     * @return false if there's no chance of getting one
     */
    public boolean isAddressedModePossible() {
        return InstanceManager.getDefault(AddressedProgrammerManager.class).isAddressedModePossible();
    }


    static Logger log = LoggerFactory.getLogger(DeferringProgrammerManager.class.getName());
}
/* @(#)DeferringProgrammerManager.java */
