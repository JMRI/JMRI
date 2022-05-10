package jmri.jmrix.internal;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.managers.DefaultPowerManager;
import jmri.managers.DefaultProgrammerManager;
import jmri.progdebugger.DebugProgrammerManager;
import jmri.util.NamedBeanPreferNumericComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Things this needed to do:
 * <ul>
 * <li>One of these must be automatically, transparently available - this is
 * done by inheriting from jmri.InstanceManagerAutoDefault
 * <li>It must be possible to have more than one of these, so you can have
 * multiple internal systems defined - each one keeps internal references to its
 * objects
 * <li>It must make sure that its objects are available individually through the
 * instance manager.
 * <li>But it also has to handle the ProxyManager special cases in the
 * InstanceManager
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2016
 */
public class InternalSystemConnectionMemo extends jmri.jmrix.DefaultSystemConnectionMemo implements jmri.InstanceManagerAutoDefault {

    public InternalSystemConnectionMemo(String prefix, String name, boolean defaultInstanceType) {
        super(prefix, name);
        InstanceManager.store(this, InternalSystemConnectionMemo.class); // also register as specific type
        register();
        this.defaultInstanceType = defaultInstanceType;
    }

    public InternalSystemConnectionMemo(String prefix, String name) {
        this(prefix, name, true);
    }

    public InternalSystemConnectionMemo(boolean defaultInstanceType) {
        this("I", "Internal", defaultInstanceType);
    }

    // invoked by i.e. InstanceManager via the InstanceManagerAutoDefault
    // mechanism, this creates a partial system
    public InternalSystemConnectionMemo() {
        this(true);
    }

    boolean configured = false;

    // if true, this is the default Internal instance, which
    // only provides a subset of types
    boolean defaultInstanceType = false;

    /**
     * Configure the common managers for Internal connections. This puts the
     * common manager config in one place.
     * <p>
     * Note: The Proxy system can cause some managers to be created early. We
     * don't call configureManagers in that case, as it causes an infinite loop.
     */
    public void configureManagers() {

        log.debug("Do configureManagers - doesn't pre-build anything");
        if (configured) {
            log.warn("configureManagers called for a second time", new Exception("traceback"));
        }
        configured = true;
    }

    public InternalSensorManager getSensorManager() {
        InternalSensorManager sensorManager = (InternalSensorManager) classObjectMap.get(SensorManager.class);
        if(sensorManager == null ) {
            log.debug("Create InternalSensorManager \"{}\" by request", getSystemPrefix());
            sensorManager = new InternalSensorManager(this);
            store(sensorManager,SensorManager.class);
            // special due to ProxyManager support
            InstanceManager.setSensorManager(sensorManager);
        }
        return sensorManager;
    }

    public InternalLightManager getLightManager() {
        InternalLightManager lightManager = (InternalLightManager) classObjectMap.get(LightManager.class);
        if(lightManager == null) {
            log.debug("Create InternalLightManager by request");
            lightManager = new InternalLightManager(this);
            store(lightManager,LightManager.class);
            // special due to ProxyManager support
            InstanceManager.setLightManager(lightManager);
        }
        return lightManager;
    }

    public InternalReporterManager getReporterManager() {
        InternalReporterManager reporterManager = (InternalReporterManager) classObjectMap.get(ReporterManager.class);
        if(reporterManager == null ) {
            log.debug("Create InternalReporterManager by request");
            reporterManager = new InternalReporterManager(this);
            store(reporterManager,ReporterManager.class);
            // special due to ProxyManager support
            InstanceManager.setReporterManager(reporterManager);
        }
        return reporterManager;
    }

    public InternalTurnoutManager getTurnoutManager() {
        InternalTurnoutManager turnoutManager = (InternalTurnoutManager) classObjectMap.get(TurnoutManager.class);
        if(turnoutManager == null ) {
            log.debug("Create InternalTurnoutManager \"{}\" by request", getSystemPrefix());
            turnoutManager = new InternalTurnoutManager(this);
            store(turnoutManager,TurnoutManager.class);
            // special due to ProxyManager support
            InstanceManager.setTurnoutManager(turnoutManager);
        }
        return turnoutManager;
    }

    public InternalMeterManager getMeterManager() {
        InternalMeterManager meterManager = (InternalMeterManager) classObjectMap.get(MeterManager.class);
        if (meterManager == null) {
            log.debug("Create InternalMeterManager by request {}", getSystemPrefix());
            meterManager = new InternalMeterManager(this);
            // special due to ProxyManager support
            InstanceManager.setMeterManager(meterManager);
        }
        return meterManager;
    }

    public InternalStringIOManager getStringIOManager() {
        InternalStringIOManager stringIOManager = (InternalStringIOManager) classObjectMap.get(StringIOManager.class);
        if (stringIOManager == null) {
            log.debug("Create InternalStringIOManager by request {}", getSystemPrefix());
            stringIOManager = new InternalStringIOManager(this);
            // special due to ProxyManager support
            InstanceManager.setStringIOManager(stringIOManager);
        }
        return stringIOManager;
    }

    public DefaultPowerManager getPowerManager() {
        return (DefaultPowerManager) classObjectMap.computeIfAbsent(PowerManager.class, (Class<?> c) -> {
            log.debug("Create DefaultPowerManager by request");
            PowerManager powerManager = new jmri.managers.DefaultPowerManager(this);
            jmri.InstanceManager.store(powerManager, PowerManager.class);
            return powerManager;
        });
    }

    @Override
    public InternalConsistManager getConsistManager() {
        if (defaultInstanceType) {
            return null;
        }
        return (InternalConsistManager) classObjectMap.computeIfAbsent((ConsistManager.class), (Class<?> c) -> {
            log.debug("Create InternalConsistManager by request");
            ConsistManager consistManager = new InternalConsistManager();
            InstanceManager.store(consistManager, jmri.ConsistManager.class);
            return consistManager;
        });
    }

    public DebugThrottleManager getThrottleManager() {
        if (defaultInstanceType) {
            return null;
        }
        return (DebugThrottleManager) classObjectMap.computeIfAbsent(ThrottleManager.class, ( Class<?> c) -> {
            log.debug("Create DebugThrottleManager by request");
            // Install a debug throttle manager
            ThrottleManager throttleManager = new jmri.jmrix.debugthrottle.DebugThrottleManager(this);
            jmri.InstanceManager.setThrottleManager(throttleManager);
            return throttleManager;
        });
    }

    public DebugProgrammerManager getProgrammerManager() {
        if (defaultInstanceType) {
            return null;
        }
        return (DebugProgrammerManager) classObjectMap.computeIfAbsent(DefaultProgrammerManager.class,
                (Class<?> c) -> {
                    // Install a debug programmer
                    log.debug("Create DebugProgrammerManager by request");
                    return new DebugProgrammerManager(this);
                });
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }

        if (!configured) {
            configureManagers();
        }

        if (type.equals(jmri.SensorManager.class)) {
            return true;
        }
        if (type.equals(jmri.LightManager.class)) {
            return true;
        }
        if (type.equals(jmri.ReporterManager.class)) {
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }

        if (!defaultInstanceType) {
            if (type.equals(jmri.PowerManager.class)) {
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
            if (type.equals(jmri.ConsistManager.class)) {
                return true;
            }
        }

        return super.provides(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> type) {
        if (getDisabled()) {
            return null;
        }

        if (!configured) {
            configureManagers();
        }

        if (type.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (type.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        if (type.equals(jmri.ReporterManager.class)) {
            return (T) getReporterManager();
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }

        if (!defaultInstanceType) {
            if (type.equals(jmri.PowerManager.class)) {
                return (T) getPowerManager();
            }

            if (type.equals(jmri.GlobalProgrammerManager.class)) {
                return (T) getProgrammerManager();
            }
            if (type.equals(jmri.AddressedProgrammerManager.class)) {
                return (T) getProgrammerManager();
            }

            if (type.equals(jmri.ThrottleManager.class)) {
                return (T) getThrottleManager();
            }
            if (type.equals(jmri.ConsistManager.class)) {
                return (T) getConsistManager();
            }
        }

        return super.get(type);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        // No actions to add at start up
        return null;
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanPreferNumericComparator<>();
    }

    @Override
    public void dispose() {
        SensorManager sensorManager = (SensorManager) classObjectMap.get(SensorManager.class);
        if (sensorManager != null) {
            sensorManager.dispose();
        }

        TurnoutManager turnoutManager = (TurnoutManager) classObjectMap.get(TurnoutManager.class);
        if (turnoutManager != null) {
            turnoutManager.dispose();
        }
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InternalSystemConnectionMemo.class);

}
