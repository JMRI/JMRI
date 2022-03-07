package jmri.jmrix.loconet;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.jmrix.loconet.swing.LnComponentFactory;
import jmri.jmrix.swing.ComponentFactory;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.NamedBeanComparator;

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
    private LncvDevicesManager lncvdm = null;
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

    public DefaultProgrammerManager getProgrammerManager() {
        return (DefaultProgrammerManager) classObjectMap.computeIfAbsent(DefaultProgrammerManager.class,(Class<?> c) -> new LnProgrammerManager(this));
    }

    public void setProgrammerManager(DefaultProgrammerManager p) {
        store(p,DefaultProgrammerManager.class);
    }

    public void setLncvDevicesManager(LncvDevicesManager lncvdm) {
        this.lncvdm = lncvdm;
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
            store(sm, jmri.CommandStation.class);
        }

    }

    /**
     * Configure the common managers for LocoNet connections. This puts the
     * common manager config in one place.
     */
    @Override
    public void configureManagers() {

        tm = new LocoNetThrottledTransmitter(getLnTrafficController(), mTurnoutExtraSpace);
        log.debug("ThrottleTransmitted configured with: {}", mTurnoutExtraSpace);
        if (sm != null) {
            sm.setThrottledTransmitter(tm, mTurnoutNoRetry);
            log.debug("set turnout retry: {}", mTurnoutNoRetry);
        }

        PowerManager pm = getPowerManager();
        if ( pm != null ) {
            InstanceManager.store(pm, PowerManager.class);
        }

        SensorManager lsm = getSensorManager();
        if ( lsm != null ) {
            InstanceManager.setSensorManager(lsm);
        }

        TurnoutManager ltm = getTurnoutManager();
        if ( ltm != null ) {
            InstanceManager.setTurnoutManager(ltm);
        }

        InstanceManager.setLightManager(
                getLightManager());

        InstanceManager.setDefault(StringIOManager.class, getStringIOManager());

        InstanceManager.setThrottleManager(
                getThrottleManager());

        DefaultProgrammerManager programmerManager = getProgrammerManager();

        if (programmerManager.isAddressedModePossible()) {
            store(programmerManager, AddressedProgrammerManager.class);
            InstanceManager.store(programmerManager, AddressedProgrammerManager.class);
        }
        if (programmerManager.isGlobalProgrammerAvailable()) {
            store(getProgrammerManager(), GlobalProgrammerManager.class);
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }

        InstanceManager.setReporterManager(getReporterManager());

        InstanceManager.setDefault(CabSignalManager.class,getCabSignalManager());

        setConsistManager(new LocoNetConsistManager(this));

        setLncvDevicesManager(new jmri.jmrix.loconet.LncvDevicesManager(this));

        ClockControl cc = getClockControl();

        InstanceManager.setDefault(ClockControl.class, cc);

        getIdTagManager();

        // register this SystemConnectionMemo to connect to rest of system
        register();

        // This must be done after the memo is registered
        getPredefinedMeters();

        // This must be done after the memo is registered
        getThrottleStringIO();
    }

    public LnPowerManager getPowerManager() {
        if (getDisabled()) {
            return null;
        }
        return (LnPowerManager) classObjectMap.computeIfAbsent(PowerManager.class,(Class<?> c) -> new LnPowerManager(this));
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
        return (LnTurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class,(Class<?> c) -> new LnTurnoutManager(this, tm, mTurnoutNoRetry));
    }

    public LnClockControl getClockControl() {
        if (getDisabled()) {
            return null;
        }
        return (LnClockControl) classObjectMap.computeIfAbsent(ClockControl.class,(Class<?> c) -> new LnClockControl(this));
    }

    public LnReporterManager getReporterManager() {
        if (getDisabled()) {
            return null;
        }
        return (LnReporterManager) classObjectMap.computeIfAbsent(ReporterManager.class, (Class<?> c) -> new LnReporterManager(this));
    }

    public LnSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        return (LnSensorManager) classObjectMap.computeIfAbsent(SensorManager.class, (Class<?> c) -> new LnSensorManager(this));
    }

    public LnLightManager getLightManager() {
        if (getDisabled()) {
            return null;
        }
        return (LnLightManager) classObjectMap.computeIfAbsent(LightManager.class, (Class<?> c) -> new LnLightManager(this));
    }

    public LncvDevicesManager getLncvDevicesManager() {
        if (getDisabled()) {
            return null;
        }
        if (lncvdm == null) {
            setLncvDevicesManager(new LncvDevicesManager(this));
            log.debug("Auto create of LncvDevicesManager for initial configuration");
        }
        return lncvdm;
    }

    public LnStringIOManager getStringIOManager() {
        if (getDisabled()) {
            return null;
        }
        return (LnStringIOManager) classObjectMap.computeIfAbsent(StringIOManager.class, (Class<?> c) -> new LnStringIOManager(this));
    }

    protected LnPredefinedMeters predefinedMeters;

    public LnPredefinedMeters getPredefinedMeters() {
        if (getDisabled()) {
            log.warn("Aborting getPredefinedMeters account is disabled!");
            return null;
        }
//        switch (getSlotManager().commandStationType) {
//            case COMMAND_STATION_USB_DCS240_ALONE:
//            case COMMAND_STATION_DCS240:
//            case COMMAND_STATION_DCS210:
//            case COMMAND_STATION_USB_DCS52_ALONE:
//            case COMMAND_STATION_DCS052:
//                break;
//            default:
//                // The command station does not support these meters
//                return null;
//        }
        if (predefinedMeters == null) {
            predefinedMeters = new LnPredefinedMeters(this);
        }
        return predefinedMeters;
    }

    LnThrottleStringIO throttleStringIO;

    public void getThrottleStringIO() {
        if (getDisabled()) {
            log.warn("Aborting getThrottleStringIO account is disabled!");
            return;
        }
        if (throttleStringIO == null) {
            throttleStringIO = new LnThrottleStringIO(this);
            InstanceManager.getDefault(jmri.StringIOManager.class)
                    .register(throttleStringIO);
        }
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
    static TranspondingTagManager tagManager;

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
        return (LnCabSignalManager) classObjectMap.computeIfAbsent(CabSignalManager.class,(Class<?> c) -> new LnCabSignalManager(this));
    }

    @Override
    public void dispose() {
        if (throttleStringIO != null) {
            throttleStringIO = null;
        }
        if (predefinedMeters != null) {
            predefinedMeters.dispose();
        }
        InstanceManager.deregister(this, LocoNetSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, ComponentFactory.class);
            cf = null;
        }
        ThrottleManager throttleManager = get(ThrottleManager.class);
        if (throttleManager != null) {
            if (throttleManager instanceof LnThrottleManager) {
                InstanceManager.deregister(((LnThrottleManager) throttleManager), LnThrottleManager.class);
            } else if (throttleManager instanceof DebugThrottleManager) {
                InstanceManager.deregister(((DebugThrottleManager) throttleManager), DebugThrottleManager.class);
            }
            deregister(throttleManager,ThrottleManager.class);
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
