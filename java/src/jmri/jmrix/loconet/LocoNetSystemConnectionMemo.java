package jmri.jmrix.loconet;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.ProgrammerManager;
import jmri.ThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class LocoNetSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public LocoNetSystemConnectionMemo(LnTrafficController lt,
            SlotManager sm) {
        super("L", "LocoNet");
        this.lt = lt;

        this.sm = sm; // doesn't full register, but fine for this purpose.

        register(); // registers general type
        InstanceManager.store(this, LocoNetSystemConnectionMemo.class); // also register as specific type

        // create and register the LnComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.loconet.swing.LnComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    public LocoNetSystemConnectionMemo() {
        super("L", "LocoNet");
        register(); // registers general type
        InstanceManager.store(this, LocoNetSystemConnectionMemo.class); // also register as specific type

        // create and register the LnComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.loconet.swing.LnComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the SlotManager for this particular connection.
     */
    public SlotManager getSlotManager() {
        if (sm == null) {
            log.debug("slot manager is null, but there should always be a valid SlotManager", new Exception("Traceback"));
        }
        return sm;
    }
    private SlotManager sm;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public LnTrafficController getLnTrafficController() {
        return lt;
    }
    private LnTrafficController lt;

    public void setLnTrafficController(LnTrafficController lt) {
        this.lt = lt;
    }

    public LnMessageManager getLnMessageManager() {
        // create when needed
        if (lnm == null) {
            lnm = new LnMessageManager(getLnTrafficController());
        }
        return lnm;
    }
    private LnMessageManager lnm = null;

    protected ProgrammerManager programmerManager;

    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new LnProgrammerManager(getSlotManager(), this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }

    protected boolean mTurnoutNoRetry = false;
    protected boolean mTurnoutExtraSpace = false;

    /**
     * Configure the programming manager and "command station" objects
     *
     * @param type               Command station type, used to configure various
     *                           operations
     * @param mTurnoutNoRetry    Is the user configuration set for no turnout
     *                           operation retries?
     * @param mTurnoutExtraSpace Is the user configuration set for extra time
     *                           between turnout operations?
     */
    public void configureCommandStation(LnCommandStationType type, boolean mTurnoutNoRetry, boolean mTurnoutExtraSpace) {

        // store arguments
        this.mTurnoutNoRetry = mTurnoutNoRetry;
        this.mTurnoutExtraSpace = mTurnoutExtraSpace;

        // create and install SlotManager
        if (sm != null) {
            log.error("Installing SlotManager twice", new Exception("TraceBack"));
        }
        sm = type.getSlotManager(lt);
        if (sm != null) {
            sm.setThrottledTransmitter(tm, mTurnoutNoRetry);
        }

        sm.setCommandStationType(type);
        sm.setSystemConnectionMemo(this);

        // store as CommandStation object
        jmri.InstanceManager.setCommandStation(sm);

    }

    /**
     * Tells which managers this provides by class
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
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
        if (type.equals(jmri.LightManager.class)) {
            return true;
        }
        if (type.equals(jmri.ReporterManager.class)) {
            return true;
        }
        if (type.equals(jmri.ConsistManager.class)) {
            return true;
        }
        if (type.equals(jmri.ClockControl.class)) {
            return true;
        }
        if (type.equals(jmri.CommandStation.class)) {
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
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        if (T.equals(jmri.ClockControl.class)) {
            return (T) getClockControl();
        }
        if (T.equals(jmri.ReporterManager.class)) {
            return (T) getReporterManager();
        }
        if (T.equals(jmri.ConsistManager.class)) {
            return (T) getConsistManager();
        }
        if (T.equals(jmri.CommandStation.class)) {
            return (T) getSlotManager();
        }
        return null; // nothing, by default
    }

    protected LocoNetThrottledTransmitter tm;

    /**
     * Configure the common managers for LocoNet connections. This puts the
     * common manager config in one place.
     */
    public void configureManagers() {

        tm = new LocoNetThrottledTransmitter(getLnTrafficController(), mTurnoutExtraSpace);
        log.debug("ThrottleTransmitted configured with :" + mTurnoutExtraSpace);
        if (sm != null) {
            sm.setThrottledTransmitter(tm, mTurnoutNoRetry);
            log.debug("set turnout retry: " + mTurnoutNoRetry);
        }

        InstanceManager.setPowerManager(
                getPowerManager());

        InstanceManager.setSensorManager(
                getSensorManager());

        InstanceManager.setTurnoutManager(
                getTurnoutManager());

        InstanceManager.setLightManager(
                getLightManager());

        InstanceManager.setThrottleManager(
                getThrottleManager());

        jmri.InstanceManager.setProgrammerManager(
                getProgrammerManager());

        InstanceManager.setReporterManager(
                getReporterManager());

        InstanceManager.setConsistManager(
                getConsistManager());

        InstanceManager.addClockControl(
                getClockControl());

    }

    protected LnPowerManager powerManager;

    public LnPowerManager getPowerManager() {
        if (getDisabled()) {
            return null;
        }
        if (powerManager == null) {
            powerManager = new jmri.jmrix.loconet.LnPowerManager(this);
        }
        return powerManager;
    }

    protected ThrottleManager throttleManager;

    public ThrottleManager getThrottleManager() {
        if (getSlotManager() != null) {
            log.debug("GetThrottleManager for {}", getSlotManager().getCommandStationType());
        }
        if (getDisabled()) {
            return null;
        }
        if (throttleManager == null && getSlotManager() != null) {
            // ask command station type for specific throttle manager
            LnCommandStationType cmdstation = getSlotManager().getCommandStationType();
            log.debug("getThrottleManager constructs for {}", cmdstation.getName());
            throttleManager = cmdstation.getThrottleManager(this);
            log.debug("result was type {}", throttleManager.getClass());
        }
        return throttleManager;
    }

    public void setThrottleManager(ThrottleManager t) {
        throttleManager = t;
    }

    protected LnTurnoutManager turnoutManager;

    public LnTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        if (turnoutManager == null) {
            turnoutManager = new jmri.jmrix.loconet.LnTurnoutManager(getLnTrafficController(), tm, getSystemPrefix(), mTurnoutNoRetry);
        }
        return turnoutManager;
    }

    protected LnClockControl clockControl;

    public LnClockControl getClockControl() {
        if (getDisabled()) {
            return null;
        }
        if (clockControl == null) {
            clockControl = new jmri.jmrix.loconet.LnClockControl(getSlotManager(), getLnTrafficController());
        }
        return clockControl;
    }

    protected LnReporterManager reporterManager;

    public LnReporterManager getReporterManager() {
        if (getDisabled()) {
            return null;
        }
        if (reporterManager == null) {
            reporterManager = new jmri.jmrix.loconet.LnReporterManager(getLnTrafficController(), getSystemPrefix());
        }
        return reporterManager;
    }

    protected LnSensorManager sensorManager;

    public LnSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        if (sensorManager == null) {
            sensorManager = new jmri.jmrix.loconet.LnSensorManager(getLnTrafficController(), getSystemPrefix());
        }
        return sensorManager;
    }

    protected LnLightManager lightManager;

    public LnLightManager getLightManager() {
        if (getDisabled()) {
            return null;
        }
        if (lightManager == null) {
            lightManager = new jmri.jmrix.loconet.LnLightManager(getLnTrafficController(), getSystemPrefix());
        }
        return lightManager;
    }

    private LocoNetConsistManager consistManager;

    public LocoNetConsistManager getConsistManager() {
        if (getDisabled()) {
            return null;
        }
        if (consistManager == null) {
            consistManager = new jmri.jmrix.loconet.LocoNetConsistManager(this);
        }
        return consistManager;
    }

    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.loconet.LocoNetActionListBundle");
    }

    public void dispose() {
        lt = null;
        sm = null;
        InstanceManager.deregister(this, LocoNetSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        if (powerManager != null) {
            InstanceManager.deregister(powerManager, jmri.jmrix.loconet.LnPowerManager.class);
        }
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, jmri.jmrix.loconet.LnTurnoutManager.class);
        }
        if (lightManager != null) {
            InstanceManager.deregister(lightManager, jmri.jmrix.loconet.LnLightManager.class);
        }
        if (sensorManager != null) {
            InstanceManager.deregister(sensorManager, jmri.jmrix.loconet.LnSensorManager.class);
        }
        if (reporterManager != null) {
            InstanceManager.deregister(reporterManager, jmri.jmrix.loconet.LnReporterManager.class);
        }
        if (throttleManager != null) {
            if (throttleManager instanceof LnThrottleManager) {
                InstanceManager.deregister(((LnThrottleManager) throttleManager), jmri.jmrix.loconet.LnThrottleManager.class);
            } else if (throttleManager instanceof jmri.jmrix.debugthrottle.DebugThrottleManager) {
                InstanceManager.deregister(((jmri.jmrix.debugthrottle.DebugThrottleManager) throttleManager), jmri.jmrix.debugthrottle.DebugThrottleManager.class);
            }
        }
        if (consistManager != null) {
            InstanceManager.deregister(consistManager, jmri.jmrix.loconet.LocoNetConsistManager.class);
        }
        if (clockControl != null) {
            InstanceManager.deregister(clockControl, jmri.jmrix.loconet.LnClockControl.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(LocoNetSystemConnectionMemo.class.getName());
}
