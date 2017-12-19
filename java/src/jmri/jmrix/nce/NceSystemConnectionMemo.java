package jmri.jmrix.nce;

import java.util.ResourceBundle;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author ken cameron Copyright (C) 2013
 */
public class NceSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public NceSystemConnectionMemo() {
        super("N", "NCE");
        register(); // registers general type
        InstanceManager.store(this, NceSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.nce.swing.NceComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    public void setNceUsbSystem(int result) {
        getNceTrafficController().setUsbSystem(result);
    }

    public int getNceUsbSystem() {
        if (getNceTrafficController() != null) {
            return getNceTrafficController().getUsbSystem();
        }
        return NceTrafficController.USB_SYSTEM_NONE;
    } // error no connection!

    public void setNceCmdGroups(long result) {
        getNceTrafficController().setCmdGroups(result);
    }

    public long getNceCmdGroups() {
        if (getNceTrafficController() != null) {
            return getNceTrafficController().getCmdGroups();
        }
        return NceTrafficController.CMDS_NONE;
    } // error no connection!

    /**
     * Provides access to the TrafficController for this particular connection.
     *
     * @return tc for this connection
     */
    public NceTrafficController getNceTrafficController() {
        return nceTrafficController;
    }
    private NceTrafficController nceTrafficController;

    public void setNceTrafficController(NceTrafficController tc) {
        nceTrafficController = tc;
        if (tc != null) {
            tc.setSystemConnectionMemo(this);
        }
    }

    private NceProgrammerManager programmerManager;

    public NceProgrammerManager getProgrammerManager() {
        //Do not want to return a programmer if the system is disabled
        if (getDisabled()) {
            return null;
        }
        if (programmerManager == null) {
            programmerManager = new NceProgrammerManager(this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(NceProgrammerManager p) {
        programmerManager = p;
    }

    /**
     * Sets the NCE message option.
     *
     * @param val command option value
     */
    public void configureCommandStation(int val) {
        getNceTrafficController().setCommandOptions(val);
        jmri.InstanceManager.setCommandStation(nceTrafficController);
    }

    /**
     * Tells which managers this class provides.
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return true;
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            return true;
        }

        if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        }
        if (type.equals(jmri.PowerManager.class)) {
            return true;
        }
        if (type.equals(jmri.SensorManager.class)) {
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }
        if (type.equals(jmri.LightManager.class)) {
            return true;
        }
        if (type.equals(jmri.ClockControl.class)) {
            return true;
        }
        if (type.equals(jmri.CommandStation.class)) {
            return true;
        }
        if (type.equals(jmri.ConsistManager.class)) {
            return true;
        }
        return super.provides(type); // nothing, by default
    }

    /**
     * Provide manager by class
     */
    @SuppressWarnings({"unchecked", "deprecation"})
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
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        if (T.equals(jmri.ClockControl.class)) {
            return (T) getClockControl();
        }
        if (T.equals(jmri.CommandStation.class)) {
            return (T) getNceTrafficController();
        }
        if (T.equals(jmri.ConsistManager.class)) {
            return (T) getConsistManager();
        }
        return super.get(T);
    }

    private NcePowerManager powerManager;
    private NceTurnoutManager turnoutManager;
    private NceLightManager lightManager;
    private NceSensorManager sensorManager;
    private NceThrottleManager throttleManager;
    private NceClockControl clockManager;

    /**
     * Configure the common managers for NCE connections. This puts the common
     * manager config in one place.
     */
    @SuppressWarnings("deprecation")
    public void configureManagers() {
        powerManager = new jmri.jmrix.nce.NcePowerManager(this);
        InstanceManager.store(powerManager, jmri.PowerManager.class);

        turnoutManager = new jmri.jmrix.nce.NceTurnoutManager(getNceTrafficController(), getSystemPrefix());
        InstanceManager.setTurnoutManager(turnoutManager);

        lightManager = new jmri.jmrix.nce.NceLightManager(getNceTrafficController(), getSystemPrefix());
        InstanceManager.setLightManager(lightManager);

        sensorManager = new jmri.jmrix.nce.NceSensorManager(getNceTrafficController(), getSystemPrefix());
        InstanceManager.setSensorManager(sensorManager);

        throttleManager = new jmri.jmrix.nce.NceThrottleManager(this);
        InstanceManager.setThrottleManager(throttleManager);

        if (getNceUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            if (getNceUsbSystem() != NceTrafficController.USB_SYSTEM_POWERHOUSE) {
                // don't set a service mode programmer
            }
        } else {
            if (getProgrammerManager().isAddressedModePossible()) {
                InstanceManager.setAddressedProgrammerManager(getProgrammerManager());
            }
            if (getProgrammerManager().isGlobalProgrammerAvailable()) {
                InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
            }
        }

        clockManager = new jmri.jmrix.nce.NceClockControl(getNceTrafficController(), getSystemPrefix());
        InstanceManager.addClockControl(clockManager);

        setConsistManager(new jmri.jmrix.nce.NceConsistManager(this));

    }

    public NcePowerManager getPowerManager() {
        return powerManager;
    }

    public NceTurnoutManager getTurnoutManager() {
        return turnoutManager;
    }

    public NceLightManager getLightManager() {
        return lightManager;
    }

    public NceSensorManager getSensorManager() {
        return sensorManager;
    }

    public NceThrottleManager getThrottleManager() {
        return throttleManager;
    }

    public NceClockControl getClockControl() {
        return clockManager;
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.nce.NceActionListBundle");
    }

    @Override
    public void dispose() {
        nceTrafficController = null;
        InstanceManager.deregister(this, NceSystemConnectionMemo.class);
        if (componentFactory != null) {
            InstanceManager.deregister(componentFactory, jmri.jmrix.swing.ComponentFactory.class);
        }
        if (powerManager != null) {
            InstanceManager.deregister(powerManager, jmri.jmrix.nce.NcePowerManager.class);
        }
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, jmri.jmrix.nce.NceTurnoutManager.class);
        }
        if (lightManager != null) {
            InstanceManager.deregister(lightManager, jmri.jmrix.nce.NceLightManager.class);
        }
        if (sensorManager != null) {
            InstanceManager.deregister(sensorManager, jmri.jmrix.nce.NceSensorManager.class);
        }
        if (throttleManager != null) {
            InstanceManager.deregister(throttleManager, jmri.jmrix.nce.NceThrottleManager.class);
        }
        if (clockManager != null) {
            InstanceManager.deregister(clockManager, jmri.jmrix.nce.NceClockControl.class);
        }

        super.dispose();
    }

}
