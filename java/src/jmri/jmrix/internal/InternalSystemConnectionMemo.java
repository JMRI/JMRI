package jmri.jmrix.internal;

import java.util.ResourceBundle;
import jmri.InstanceManager;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class InternalSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo implements jmri.InstanceManagerAutoDefault {

    public InternalSystemConnectionMemo() {
        super("I", "Internal");
        InstanceManager.store(this, InternalSystemConnectionMemo.class); // also register as specific type
        register();
    }

    boolean configured = false;
    
    /**
     * Configure the common managers for Internal connections. This puts the
     * common manager config in one place. This method is static so that it can
     * be referenced from classes that don't inherit, including
     * hexfile.HexFileFrame and locormi.LnMessageClient
     */
    public void configureManagers() {

        log.debug("Do configureManagers");
        if (configured) log.warn("configureManagers called for a second time", new Exception("traceback"));
        
        turnoutManager = new InternalTurnoutManager(getSystemPrefix());
        InstanceManager.setTurnoutManager(turnoutManager);

        sensorManager = new InternalSensorManager(getSystemPrefix());
        InstanceManager.setSensorManager(sensorManager);

        powerManager = new jmri.managers.DefaultPowerManager();
        jmri.InstanceManager.setPowerManager(powerManager);

        // Install a debug programmer
        programManager = new jmri.progdebugger.DebugProgrammerManager();
        jmri.InstanceManager.setProgrammerManager(programManager);

        // Install a debug throttle manager
        throttleManager = new jmri.jmrix.debugthrottle.DebugThrottleManager(this);
        jmri.InstanceManager.setThrottleManager(throttleManager
        );

        configured = true;
    }

    private InternalSensorManager sensorManager;
    private InternalTurnoutManager turnoutManager;
    private jmri.jmrix.debugthrottle.DebugThrottleManager throttleManager;
    private jmri.managers.DefaultPowerManager powerManager;
    private jmri.progdebugger.DebugProgrammerManager programManager;

    public InternalTurnoutManager getTurnoutManager() {
        if (!configured) configureManagers();
        return turnoutManager;
    }

    public InternalSensorManager getSensorManager() {
        if (!configured) configureManagers();
        return sensorManager;
    }

    public jmri.jmrix.debugthrottle.DebugThrottleManager getThrottleManager() {
        if (!configured) configureManagers();
        return throttleManager;
    }

    public jmri.managers.DefaultPowerManager getPowerManager() {
        if (!configured) configureManagers();
        return powerManager;
    }

    public jmri.progdebugger.DebugProgrammerManager getProgrammerManager() {
        if (!configured) configureManagers();
        return programManager;
    }

    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }

        if (!configured) configureManagers();

        if (type.equals(jmri.ProgrammerManager.class)) {
            return true;
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
        if (type.equals(jmri.SensorManager.class)) {
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }
        return false; // nothing, by default
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }

        if (!configured) configureManagers();

        if (T.equals(jmri.ProgrammerManager.class)) {
            return (T) getProgrammerManager();
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
        return null; // nothing, by default
    }

    protected ResourceBundle getActionModelResourceBundle() {
        //No actions to add at start up
        return null;
    }

    public void dispose() {
        if (sensorManager != null) {
            sensorManager.dispose();
            sensorManager = null;
        }
        if (turnoutManager != null) {
            turnoutManager.dispose();
            turnoutManager = null;
        }
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InternalSystemConnectionMemo.class.getName());
}

