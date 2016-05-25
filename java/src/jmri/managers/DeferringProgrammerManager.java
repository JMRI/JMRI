package jmri.managers;

import jmri.AddressedProgrammer;
import jmri.AddressedProgrammerManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ProgrammerManager;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defers GlobalProgrammer operations to the default GlobalProgrammer, and
 * AddressedProgrammer operations to the default AddressedProgrammer.
 * <p>
 * The underlying Programmer is looked up for each access to ensure that it's
 * current.
 *
 * @see jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2014
 */
public class DeferringProgrammerManager implements ProgrammerManager {

    public DeferringProgrammerManager() {
    }

    String userName = "<Default>";

    /**
     * Provides the human-readable representation for including
     * ProgrammerManagers directly in e.g. JComboBoxes, so it should return a
     * user-provided name for this particular one.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Provides the human-readable representation for including
     * ProgrammerManagers directly in e.g. JComboBoxes, so it should return a
     * user-provided name for this particular one.
     */
    public String toString() {
        return getUserName();
    }

    public Programmer getGlobalProgrammer() {
        GlobalProgrammerManager gp = InstanceManager.getDefault(GlobalProgrammerManager.class);
        if (gp == null) {
            log.debug("no defaultGlobal ProgrammerManager, getGlobalProgrammer returns null" );
            return null;
        }
        Programmer p = gp.getGlobalProgrammer();
        log.debug("getGlobalProgrammer returns default service-mode programmer of type {} from {}", 
            (p != null ? p.getClass() : "(null)"), gp.getClass() );
        return p;
    }

    public Programmer reserveGlobalProgrammer() {
        GlobalProgrammerManager gp = InstanceManager.getDefault(GlobalProgrammerManager.class);
        if (gp == null) {
            return null;
        }
        return gp.reserveGlobalProgrammer();
    }

    public void releaseGlobalProgrammer(Programmer p) {
        GlobalProgrammerManager gp = InstanceManager.getDefault(GlobalProgrammerManager.class);
        if (gp == null) {
            return;
        }
        gp.releaseGlobalProgrammer(p);
    }

    /**
     * Allow for implementations that do not support Service mode programming
     *
     * @return false if there's no chance of getting one
     */
    public boolean isGlobalProgrammerAvailable() {
        GlobalProgrammerManager gp = InstanceManager.getDefault(GlobalProgrammerManager.class);
        if (gp == null) {
            return false;
        }
        return InstanceManager.getDefault(GlobalProgrammerManager.class).isGlobalProgrammerAvailable();
    }

    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        AddressedProgrammerManager ap = InstanceManager.getDefault(AddressedProgrammerManager.class);
        if (ap == null) {
            return null;
        }
        return ap.getAddressedProgrammer(pLongAddress, pAddress);
    }

    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        AddressedProgrammerManager ap = InstanceManager.getDefault(AddressedProgrammerManager.class);
        if (ap == null) {
            return null;
        }
        return ap.reserveAddressedProgrammer(pLongAddress, pAddress);
    }

    public void releaseAddressedProgrammer(AddressedProgrammer p) {
        AddressedProgrammerManager ap = InstanceManager.getDefault(AddressedProgrammerManager.class);
        if (ap == null) {
            return;
        }
        ap.releaseAddressedProgrammer(p);
    }

    /**
     * Default programmer does not provide Ops Mode
     *
     * @return false if there's no chance of getting one
     */
    public boolean isAddressedModePossible() {
        AddressedProgrammerManager ap = InstanceManager.getDefault(AddressedProgrammerManager.class);
        if (ap == null) {
            return false;
        }
        return ap.isAddressedModePossible();
    }

    /**
     * Default programmer doesn't depend on address
     *
     * @return false if there's no chance of getting one
     */
    public boolean isAddressedModePossible(jmri.LocoAddress l) {
        return isAddressedModePossible();
    }

    public java.util.List<ProgrammingMode> getDefaultModes() {
        return InstanceManager.getDefault(AddressedProgrammerManager.class).getDefaultModes();
    }

    private final static Logger log = LoggerFactory.getLogger(DeferringProgrammerManager.class.getName());
}

