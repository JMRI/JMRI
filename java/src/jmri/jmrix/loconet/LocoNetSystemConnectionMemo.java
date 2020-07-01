package jmri.jmrix.loconet;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.jmrix.loconet.swing.LnComponentFactory;
import jmri.jmrix.swing.ComponentFactory;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.NamedBeanComparator;

import org.python.antlr.op.Pow;
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
public class LocoNetSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {


    /**
     * Must manually register() after construction is complete.
     * @param lt Traffic controller to be used
     * @param sm Slot Manager to be used
     */
    public LocoNetSystemConnectionMemo(LnTrafficController lt, SlotManager sm) {
        super("L", "LocoNet"); // NOI18N
        this.lt = lt;

        this.sm = sm; // doesn't full register, but fine for this purpose.

        // self-registration is deferred until the command station type is set below
                
        // create and register the ComponentFactory for the GUI
        InstanceManager.store(cf = new LnComponentFactory(this),
                ComponentFactory.class);
    }

    /**
     * Must manually register() after construction is complete.
     */
    public LocoNetSystemConnectionMemo() {
        this("L", "LocoNet"); // NOI18N
    }

    public LocoNetSystemConnectionMemo(@Nonnull String prefix, @Nonnull String name) {
        super(prefix, name); // NOI18N

        // create and register the ComponentFactory for the GUI
        InstanceManager.store(cf = new LnComponentFactory(this),
                ComponentFactory.class);
    }

    /**
     * Do both the default parent
     * {@link jmri.SystemConnectionMemo} registration,
     * and register this specific type.
     */
    @Override
    public void register() {
        super.register(); // registers general type
        InstanceManager.store(this, LocoNetSystemConnectionMemo.class); // also register as specific type
    }

    ComponentFactory cf = null;
    private LnTrafficController lt;
    protected LocoNetThrottledTransmitter tm;
    private SlotManager sm;
    private LnMessageManager lnm = null;

    /**
     * Provide access to the SlotManager for this particular connection.
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
     * Provide access to the TrafficController for this particular connection.
     *
     * @return the LocoNet-specific TrafficController
     */
    public LnTrafficController getLnTrafficController() {
        if (lt == null) {
            setLnTrafficController(new LnPacketizer(this)); // default to Packetizer TrafficController
            log.debug("Auto create of LnTrafficController for initial configuration");
        }
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
            programmerManager = new LnProgrammerManager(this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(DefaultProgrammerManager p) {
        programmerManager = p;
    }

    protected boolean mTurnoutNoRetry = false;
    protected boolean mTurnoutExtraSpace = false;

    /**
     * Configure the programming manager and "command station" objects.
     *
     * @param type               Command station type, used to configure various
     *                           operations
     * @param mTurnoutNoRetry    Is the user configuration set for no turnout
     *                           operation retries?
     * @param mTurnoutExtraSpace Is the user configuration set for extra time
     *                           between turnout operations?
     * @param mTranspondingAvailable    Is the layout configured to provide
     *                                  transopnding reports
     */
    public void configureCommandStation(LnCommandStationType type, boolean mTurnoutNoRetry,
                                            boolean mTurnoutExtraSpace, boolean mTranspondingAvailable) {

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
            sm.setTranspondingAvailable(mTranspondingAvailable);

            // store as CommandStation object
            InstanceManager.store(sm, jmri.CommandStation.class);
        }

        // register this SystemConnectionMemo to connect to rest of system
        register();
    }

    /**
     * Configure the common managers for LocoNet connections. This puts the
     * common manager config in one place.
     */
    public void configureManagers() {

        tm = new LocoNetThrottledTransmitter(getLnTrafficController(), mTurnoutExtraSpace);
        log.debug("ThrottleTransmitted configured with: {}", mTurnoutExtraSpace);
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
            InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }

        InstanceManager.setReporterManager(getReporterManager());
        
        InstanceManager.setDefault(CabSignalManager.class,getCabSignalManager());

        setConsistManager(new LocoNetConsistManager(this));

        ClockControl cc = getClockControl();

        InstanceManager.setDefault(ClockControl.class, cc);

        jmri.InstanceManager.store(getMultiMeter(), jmri.MultiMeter.class);

        getIdTagManager();

    }

    public LnPowerManager getPowerManager() {
        if (getDisabled()) {
            return null;
        }
        return (LnPowerManager) classObjectMap.computeIfAbsent(PowerManager.class,(Class c) -> new LnPowerManager(this));
    }

    public ThrottleManager getThrottleManager() {
        if (getSlotManager() != null) {
            log.debug("GetThrottleManager for {}", getSlotManager().getCommandStationType());
        }
        if (getDisabled()) {
            return null;
        }
        ThrottleManager throttleManager = get(ThrottleManager.class);
        if (throttleManager == null && getSlotManager() != null) {
            // ask command station type for specific throttle manager
            LnCommandStationType cmdstation = getSlotManager().getCommandStationType();
            log.debug("getThrottleManager constructs for {}", cmdstation.getName());
            throttleManager = cmdstation.getThrottleManager(this);
            log.debug("result was type {}", throttleManager.getClass());
            store(throttleManager,ThrottleManager.class);
        }
        return throttleManager;
    }

    public void setThrottleManager(ThrottleManager t) {
        store(t,ThrottleManager.class);
    }

    public LnTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        return (LnTurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class,(Class c) -> new LnTurnoutManager(this, tm, mTurnoutNoRetry));
    }

    public LnClockControl getClockControl() {
        if (getDisabled()) {
            return null;
        }
        return (LnClockControl) classObjectMap.computeIfAbsent(ClockControl.class,(Class c) -> new LnClockControl(this));
    }

    public LnReporterManager getReporterManager() {
        if (getDisabled()) {
            return null;
        }
        return (LnReporterManager) classObjectMap.computeIfAbsent(ReporterManager.class, (Class c) -> new LnReporterManager(this));
    }

    public LnSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        return (LnSensorManager) classObjectMap.computeIfAbsent(LnSensorManager.class, (Class c) -> new LnSensorManager(this));
    }

    public LnLightManager getLightManager() {
        if (getDisabled()) {
            return null;
        }
        return (LnLightManager) classObjectMap.computeIfAbsent(LightManager.class, (Class c) -> new LnLightManager(this));
    }

    public LnMultiMeter getMultiMeter() {
        if (getDisabled()) {
            return null;
        }
        return (LnMultiMeter) classObjectMap.computeIfAbsent(MultiMeter.class,(Class c) -> new LnMultiMeter(this));
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.loconet.LocoNetActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    // yes, tagManager is static.  Tags can move between system connections.
    // when readers are not all on the same LocoNet
    // this manager is loaded on demand.
    protected static TranspondingTagManager tagManager;

    static public TranspondingTagManager getIdTagManager() {
        synchronized (LocoNetSystemConnectionMemo.class) { // since tagManager can be null, can't synch on that
            if (tagManager == null) {
                tagManager = new TranspondingTagManager();
                InstanceManager.setIdTagManager(tagManager);
            }
            return tagManager;
        }
    }

    public LnCabSignalManager getCabSignalManager() {
        return (LnCabSignalManager) classObjectMap.computeIfAbsent(CabSignalManager.class,(Class c) -> new LnCabSignalManager(this));
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, LocoNetSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, ComponentFactory.class);
            cf = null;
        }
        PowerManager powerManager = get(PowerManager.class);
        if (powerManager != null) {
            try {
                powerManager.dispose();
            } catch (JmriException je){
                log.warn("Exception disposing of power manager ",je);
            }
            InstanceManager.deregister(powerManager, PowerManager.class);
        }
        TurnoutManager turnoutManager = get(TurnoutManager.class);
        if (turnoutManager != null) {
            turnoutManager.dispose();
            InstanceManager.deregister(turnoutManager, TurnoutManager.class);
        }
        LightManager lightManager = get(LightManager.class);
        if (lightManager != null) {
            lightManager.dispose();
            InstanceManager.deregister(lightManager, LightManager.class);
        }
        SensorManager sensorManager = get(SensorManager.class);
        if (sensorManager != null) {
            sensorManager.dispose();
            InstanceManager.deregister(sensorManager, SensorManager.class);
        }
        ReporterManager reporterManager = get(ReporterManager.class);
        if (reporterManager != null) {
            reporterManager.dispose();
            InstanceManager.deregister(reporterManager, ReporterManager.class);
        }
        ThrottleManager throttleManager = get(ThrottleManager.class);
        if (throttleManager != null) {
            if (throttleManager instanceof LnThrottleManager) {
                InstanceManager.deregister(((LnThrottleManager) throttleManager), LnThrottleManager.class);
            } else if (throttleManager instanceof DebugThrottleManager) {
                InstanceManager.deregister(((DebugThrottleManager) throttleManager), DebugThrottleManager.class);
            }
        }

        ClockControl clockControl = get(ClockControl.class);
        if (clockControl != null) {
            InstanceManager.deregister(clockControl, ClockControl.class);
        }
        if (tm != null){
            tm.dispose();
            tm = null;
        }
        if (sm != null){
            sm.dispose();
            sm = null;
        }
        if (lt != null){
            lt.dispose();
            lt = null;
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(LocoNetSystemConnectionMemo.class);

}
