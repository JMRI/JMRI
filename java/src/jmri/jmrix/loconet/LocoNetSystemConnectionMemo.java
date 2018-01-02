package jmri.jmrix.loconet;

import java.util.ResourceBundle;
import jmri.AddressedProgrammerManager;
import jmri.ClockControl;
import jmri.CommandStation;
import jmri.ConsistManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.PowerManager;
import jmri.ReporterManager;
import jmri.SensorManager;
import jmri.ThrottleManager;
import jmri.TurnoutManager;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.jmrix.loconet.swing.LnComponentFactory;
import jmri.jmrix.swing.ComponentFactory;
import jmri.managers.DefaultProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class LocoNetSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public LocoNetSystemConnectionMemo(LnTrafficController lt, SlotManager sm) {
        super("L", "LocoNet"); // NOI18N
        this.lt = lt;

        this.sm = sm; // doesn't full register, but fine for this purpose.

        register(); // registers general type
        InstanceManager.store(this, LocoNetSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory for the GUI
        InstanceManager.store(cf = new LnComponentFactory(this),
                ComponentFactory.class);
    }

    public LocoNetSystemConnectionMemo() {
        super("L", "LocoNet"); // NOI18N
        register(); // registers general type
        InstanceManager.store(this, LocoNetSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory for the GUI
        InstanceManager.store(cf = new LnComponentFactory(this),
                ComponentFactory.class);
    }

    ComponentFactory cf = null;
    private LnTrafficController lt;
    private SlotManager sm;
    private LnMessageManager lnm = null;

    /**
     * Provides access to the SlotManager for this particular connection.
     *
     * @return the slot manager or null if no valid slot manager is available
     */
    public SlotManager getSlotManager() {
        if (sm == null) {
            log.debug("slot manager is null, but there should always be a valid SlotManager", new Exception("Traceback"));
        }
        return sm;
    }

    /**
     * Provides access to the TrafficController for this particular connection.
     *
     * @return the LocoNet-specific TrafficController
     */
    public LnTrafficController getLnTrafficController() {
        return lt;
    }

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

    protected DefaultProgrammerManager programmerManager;

    public DefaultProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new LnProgrammerManager(getSlotManager(), this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(DefaultProgrammerManager p) {
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

            sm.setCommandStationType(type);
            sm.setSystemConnectionMemo(this);

            // store as CommandStation object
            InstanceManager.setCommandStation(sm);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(GlobalProgrammerManager.class)) {
            return getProgrammerManager().isGlobalProgrammerAvailable();
        }
        if (type.equals(AddressedProgrammerManager.class)) {
            return getProgrammerManager().isAddressedModePossible();
        }

        if (type.equals(ThrottleManager.class)) {
            return true;
        }
        if (type.equals(PowerManager.class)) {
            return true;
        }
        if (type.equals(SensorManager.class)) {
            return true;
        }
        if (type.equals(TurnoutManager.class)) {
            return true;
        }
        if (type.equals(LightManager.class)) {
            return true;
        }
        if (type.equals(ReporterManager.class)) {
            return true;
        }
        if (type.equals(ConsistManager.class)) {
            return true;
        }
        if (type.equals(ClockControl.class)) {
            return true;
        }
        if (type.equals(CommandStation.class)) {
            return true;
        }
        return super.provides(type);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }

        if (T.equals(ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(PowerManager.class)) {
            return (T) getPowerManager();
        }
        if (T.equals(SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(LightManager.class)) {
            return (T) getLightManager();
        }
        if (T.equals(ClockControl.class)) {
            return (T) getClockControl();
        }
        if (T.equals(ReporterManager.class)) {
            return (T) getReporterManager();
        }
        if (T.equals(ConsistManager.class)) {
            return (T) getConsistManager();
        }
        if (T.equals(CommandStation.class)) {
            return (T) getSlotManager();
        }
        return super.get(T);
    }

    protected LocoNetThrottledTransmitter tm;

    /**
     * Configure the common managers for LocoNet connections. This puts the
     * common manager config in one place.
     */
    public void configureManagers() {

        tm = new LocoNetThrottledTransmitter(getLnTrafficController(), mTurnoutExtraSpace);
        log.debug("ThrottleTransmitted configured with :{}", mTurnoutExtraSpace);
        if (sm != null) {
            sm.setThrottledTransmitter(tm, mTurnoutNoRetry);
            log.debug("set turnout retry: {}", mTurnoutNoRetry);
        }

        InstanceManager.store(getPowerManager(), PowerManager.class);

        InstanceManager.setSensorManager(
                getSensorManager());

        InstanceManager.setTurnoutManager(
                getTurnoutManager());

        InstanceManager.setLightManager(
                getLightManager());

        InstanceManager.setThrottleManager(
                getThrottleManager());

        if (getProgrammerManager().isAddressedModePossible()) {
            InstanceManager.setAddressedProgrammerManager(getProgrammerManager());
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }

        InstanceManager.setReporterManager(
                getReporterManager());

        setConsistManager(new LocoNetConsistManager(this));

        InstanceManager.addClockControl(
                getClockControl());

    }

    protected LnPowerManager powerManager;

    public LnPowerManager getPowerManager() {
        if (getDisabled()) {
            return null;
        }
        if (powerManager == null) {
            powerManager = new LnPowerManager(this);
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
            turnoutManager = new LnTurnoutManager(getLnTrafficController(), tm, getSystemPrefix(), mTurnoutNoRetry);
        }
        return turnoutManager;
    }

    protected LnClockControl clockControl;

    public LnClockControl getClockControl() {
        if (getDisabled()) {
            return null;
        }
        if (clockControl == null) {
            clockControl = new LnClockControl(getSlotManager(), getLnTrafficController());
        }
        return clockControl;
    }

    protected LnReporterManager reporterManager;

    public LnReporterManager getReporterManager() {
        if (getDisabled()) {
            return null;
        }
        if (reporterManager == null) {
            reporterManager = new LnReporterManager(getLnTrafficController(), getSystemPrefix());
        }
        return reporterManager;
    }

    protected LnSensorManager sensorManager;

    public LnSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        if (sensorManager == null) {
            sensorManager = new LnSensorManager(getLnTrafficController(), getSystemPrefix());
        }
        return sensorManager;
    }

    protected LnLightManager lightManager;

    public LnLightManager getLightManager() {
        if (getDisabled()) {
            return null;
        }
        if (lightManager == null) {
            lightManager = new LnLightManager(getLnTrafficController(), getSystemPrefix());
        }
        return lightManager;
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.loconet.LocoNetActionListBundle");
    }

    @Override
    public void dispose() {
        lt = null;
        sm = null;
        InstanceManager.deregister(this, LocoNetSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, ComponentFactory.class);
        }
        if (powerManager != null) {
            InstanceManager.deregister(powerManager, LnPowerManager.class);
        }
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, LnTurnoutManager.class);
        }
        if (lightManager != null) {
            InstanceManager.deregister(lightManager, LnLightManager.class);
        }
        if (sensorManager != null) {
            InstanceManager.deregister(sensorManager, LnSensorManager.class);
        }
        if (reporterManager != null) {
            InstanceManager.deregister(reporterManager, LnReporterManager.class);
        }
        if (throttleManager != null) {
            if (throttleManager instanceof LnThrottleManager) {
                InstanceManager.deregister(((LnThrottleManager) throttleManager), LnThrottleManager.class);
            } else if (throttleManager instanceof DebugThrottleManager) {
                InstanceManager.deregister(((DebugThrottleManager) throttleManager), DebugThrottleManager.class);
            }
        }
        if (clockControl != null) {
            InstanceManager.deregister(clockControl, LnClockControl.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(LocoNetSystemConnectionMemo.class);

}
