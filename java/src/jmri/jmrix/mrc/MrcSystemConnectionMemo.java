package jmri.jmrix.mrc;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Ken Cameron Copyright (C) 2014
 * @author Kevin Dickerson Copyright (C) 2014
 *
 */
public class MrcSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public MrcSystemConnectionMemo() {
        super("M", "MRC"); //IN18N
        register(); // registers general type
        InstanceManager.store(this, MrcSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.mrc.swing.MrcComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     *
     * @return current traffic controller for this connection
     */
    public MrcTrafficController getMrcTrafficController() {
        if (mrcTrafficController == null) log.error("found tc null in request", new Exception("traceback"));
        return mrcTrafficController;
    }
    private MrcTrafficController mrcTrafficController;

    public void setMrcTrafficController(MrcTrafficController tc) {
        mrcTrafficController = tc;
        if (tc != null) {
            tc.setAdapterMemo(this);
        }
    }

    private MrcProgrammerManager programmerManager;

    public MrcProgrammerManager getProgrammerManager() {
        //Do not want to return a programmer if the system is disabled
        if (getDisabled()) {
            return null;
        }
        if (programmerManager == null) {
            programmerManager = new MrcProgrammerManager(new MrcProgrammer(this), this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(MrcProgrammerManager p) {
        programmerManager = p;
    }

    /**
     * Sets the MRC message option.
     */
    /*    public void configureCommandStation(int val) {
     getMrcTrafficController().setCommandOptions(val);
     jmri.InstanceManager.store(mrcTrafficController, jmri.CommandStation.class);
     }*/

    /**
     * Tells which managers this class provides.
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return getProgrammerManager().isGlobalProgrammerAvailable();
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            return getProgrammerManager().isAddressedModePossible();
        }

        if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        }
        if (type.equals(jmri.PowerManager.class)) {
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }
        if (type.equals(jmri.ClockControl.class)) {
            return true;
        }
        return super.provides(type); // nothing, by default
    }

    /**
     * Provide manager by class
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }

        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(jmri.ClockControl.class)) {
            return (T) getClockControl();
        }
        return super.get(T);
    }

    private MrcPowerManager powerManager;
    private MrcTurnoutManager turnoutManager;
    private MrcThrottleManager throttleManager;
    private MrcClockControl clockManager;

    /**
     * Configure the common managers for MRC connections. This puts the common
     * manager config in one place.
     */
    public void configureManagers() {
        powerManager = new jmri.jmrix.mrc.MrcPowerManager(this);
        InstanceManager.store(powerManager, jmri.PowerManager.class);

        turnoutManager = new jmri.jmrix.mrc.MrcTurnoutManager(this);
        InstanceManager.setTurnoutManager(turnoutManager);

        throttleManager = new jmri.jmrix.mrc.MrcThrottleManager(this);
        InstanceManager.setThrottleManager(throttleManager);

        if (getProgrammerManager().isAddressedModePossible()) {
            InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }

        clockManager = new jmri.jmrix.mrc.MrcClockControl(getMrcTrafficController(), getSystemPrefix());
        // make sure InstanceManager knows about that
        InstanceManager.store(clockManager, jmri.ClockControl.class);
        InstanceManager.setDefault(jmri.ClockControl.class, clockManager);

    }

    public MrcPowerManager getPowerManager() {
        return powerManager;
    }

    public MrcTurnoutManager getTurnoutManager() {
        return turnoutManager;
    }

    public MrcThrottleManager getThrottleManager() {
        return throttleManager;
    }

    public MrcClockControl getClockControl() {
        return clockManager;
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.mrc.MrcActionListBundle"); //NO18N
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        mrcTrafficController = null;
        InstanceManager.deregister(this, MrcSystemConnectionMemo.class);
        if (componentFactory != null) {
            InstanceManager.deregister(componentFactory, jmri.jmrix.swing.ComponentFactory.class);
        }
        if (powerManager != null) {
            InstanceManager.deregister(powerManager, jmri.jmrix.mrc.MrcPowerManager.class);
        }
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, jmri.jmrix.mrc.MrcTurnoutManager.class);
        }
        if (throttleManager != null) {
            InstanceManager.deregister(throttleManager, jmri.jmrix.mrc.MrcThrottleManager.class);
        }
        if (clockManager != null) {
            InstanceManager.deregister(clockManager, jmri.jmrix.mrc.MrcClockControl.class);
        }

        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MrcSystemConnectionMemo.class.getName());

}
