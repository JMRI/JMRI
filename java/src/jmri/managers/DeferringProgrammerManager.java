package jmri.managers;

import java.util.ArrayList;
import java.util.Set;
import jmri.AddressedProgrammer;
import jmri.AddressedProgrammerManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.implementation.AbstractInstanceInitializer;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defers global programmer operations to the default global Programmer, and
 * addressed programmer operations to the default AddressedProgrammer.
 * <p>
 * The underlying Programmer is looked up for each access to ensure that it is
 * current.
 *
 * @see jmri.GlobalProgrammerManager
 * @see jmri.AddressedProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2014
 */
public class DeferringProgrammerManager implements AddressedProgrammerManager, GlobalProgrammerManager {

    public DeferringProgrammerManager() {
    }

    String userName = "<Default>";

    /**
     * Provides the human-readable representation for including
     * ProgrammerManagers directly in user interface components, so it should
     * return a user-provided name for this particular one.
     */
    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * Provides the human-readable representation for including
     * ProgrammerManagers directly in user interface components, so it should
     * return a user-provided name for this particular one.
     */
    @Override
    public String toString() {
        return getUserName();
    }

    @Override
    public Programmer getGlobalProgrammer() {
        GlobalProgrammerManager gp = InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
        if (gp == null || this.equals(gp)) {
            log.debug("no defaultGlobal ProgrammerManager, getGlobalProgrammer returns null");
            return null;
        }
        Programmer p = gp.getGlobalProgrammer();
        log.debug("getGlobalProgrammer returns default service-mode programmer of type {} from {}",
                (p != null ? p.getClass() : "(null)"), gp.getClass());
        return p;
    }

    @Override
    public Programmer reserveGlobalProgrammer() {
        GlobalProgrammerManager gp = InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
        if (gp == null || this.equals(gp)) {
            return null;
        }
        return gp.reserveGlobalProgrammer();
    }

    @Override
    public void releaseGlobalProgrammer(Programmer p) {
        GlobalProgrammerManager gp = InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
        if (gp == null || this.equals(gp)) {
            return;
        }
        gp.releaseGlobalProgrammer(p);
    }

    /**
     * Allow for implementations that do not support Service mode programming
     *
     * @return false if there's no chance of getting one
     */
    @Override
    public boolean isGlobalProgrammerAvailable() {
        GlobalProgrammerManager gp = InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
        if (gp == null || this.equals(gp)) {
            return false;
        }
        return InstanceManager.getDefault(GlobalProgrammerManager.class).isGlobalProgrammerAvailable();
    }

    @Override
    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        AddressedProgrammerManager ap = InstanceManager.getNullableDefault(AddressedProgrammerManager.class);
        if (ap == null || this.equals(ap)) {
            return null;
        }
        return ap.getAddressedProgrammer(pLongAddress, pAddress);
    }

    @Override
    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        AddressedProgrammerManager ap = InstanceManager.getNullableDefault(AddressedProgrammerManager.class);
        if (ap == null || this.equals(ap)) {
            return null;
        }
        return ap.reserveAddressedProgrammer(pLongAddress, pAddress);
    }

    @Override
    public void releaseAddressedProgrammer(AddressedProgrammer p) {
        AddressedProgrammerManager ap = InstanceManager.getNullableDefault(AddressedProgrammerManager.class);
        if (ap == null || this.equals(ap)) {
            return;
        }
        ap.releaseAddressedProgrammer(p);
    }

    /**
     * Default programmer does not provide Ops Mode
     *
     * @return false if there's no chance of getting one
     */
    @Override
    public boolean isAddressedModePossible() {
        AddressedProgrammerManager ap = InstanceManager.getNullableDefault(AddressedProgrammerManager.class);
        if (ap == null || this.equals(ap)) {
            return false;
        }
        return ap.isAddressedModePossible();
    }

    /**
     * Default programmer doesn't depend on address
     *
     * @return false if there's no chance of getting one
     */
    @Override
    public boolean isAddressedModePossible(jmri.LocoAddress l) {
        return isAddressedModePossible();
    }

    @Override
    public java.util.List<ProgrammingMode> getDefaultModes() {
        AddressedProgrammerManager ap = InstanceManager.getNullableDefault(AddressedProgrammerManager.class);
        if (ap == null || this.equals(ap)) {
            return new ArrayList<>();
        }
        return InstanceManager.getDefault(AddressedProgrammerManager.class).getDefaultModes();
    }

    @ServiceProvider(service=InstanceInitializer.class)
    public static final class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type == AddressedProgrammerManager.class) {
                return new DeferringProgrammerManager();
            }
            if (type == GlobalProgrammerManager.class) {
                return new DeferringProgrammerManager();
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(AddressedProgrammerManager.class);
            set.add(GlobalProgrammerManager.class);
            return set;
        }

    }
    private final static Logger log = LoggerFactory.getLogger(DeferringProgrammerManager.class);
}
