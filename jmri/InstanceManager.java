// InstanceManager.java

package jmri;

/**
 * Provides static members for locating various interface implementations.
 * These are the base of how JMRI objects are located.
 *<P>
 * The implementations of these interfaces are specific to the layout hardware, etc.
 * During initialization, objects of the right type are created and registered
 * with the ImplementationManager class, so they can later be retrieved by
 * non-system-specific code.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.17 $
 */
public class InstanceManager {

    static public PowerManager powerManagerInstance()  { return instance().powerManager; }

    static public ProgrammerManager programmerManagerInstance()  { return instance().programmerManager; }

    static public SensorManager sensorManagerInstance()  { return instance().sensorManager; }

    static public TurnoutManager turnoutManagerInstance()  { return instance().turnoutManager; }

    static public ConfigureManager configureManagerInstance()  { return instance().configureManager; }

    static public ThrottleManager throttleManagerInstance()  { return instance().throttleManager; }

    static public SignalHeadManager signalHeadManagerInstance()  {
        if (instance().signalHeadManager != null) return instance().signalHeadManager;
        // As a convenience, we create a default object if none was provided explicitly.
        // This must be replaced when we start registering specific implementations
        instance().signalHeadManager = new AbstractSignalHeadManager();
        return instance().signalHeadManager;
    }

    static public CommandStation commandStationInstance()  { return instance().commandStation; }

    static private InstanceManager instance() {
        if (root==null) root = new InstanceManager();
        return root;
    }

    private InstanceManager() {
        turnoutManager = new jmri.managers.ProxyTurnoutManager();
    }

    /**
     * The "root" object is the instance manager that's answering
     * requests for other instances.
     */
    static private InstanceManager root;

    private PowerManager powerManager = null;
    static public void setPowerManager(PowerManager p) {
        instance().addPowerManager(p);
    }
    public void addPowerManager(PowerManager p) {
        if (p!=powerManager && powerManager!=null && log.isDebugEnabled()) log.debug("PowerManager instance is being replaced: "+p);
        if (p!=powerManager && powerManager==null && log.isDebugEnabled()) log.debug("PowerManager instance is being installed: "+p);
        powerManager = p;
    }

    private ProgrammerManager programmerManager = null;
    static public void setProgrammerManager(ProgrammerManager p) {
        instance().addProgrammerManager(p);
    }
    public void addProgrammerManager(ProgrammerManager p) {
        if (p!=programmerManager && programmerManager!=null && log.isDebugEnabled()) log.debug("ProgrammerManager instance is being replaced: "+p);
        if (p!=programmerManager && programmerManager==null && log.isDebugEnabled()) log.debug("ProgrammerManager instance is being installed: "+p);
        programmerManager = p;
    }

    private SensorManager sensorManager = null;
    static public void setSensorManager(SensorManager p) {
        instance().addSensorManager(p);
    }
    public void addSensorManager(SensorManager p) {
        if (p!=sensorManager && sensorManager!=null && log.isDebugEnabled()) log.debug("SensorManager instance is being replaced: "+p);
        if (p!=sensorManager && sensorManager==null && log.isDebugEnabled()) log.debug("SensorManager instance is being installed: "+p);
        sensorManager = p;
    }

    private TurnoutManager turnoutManager = null;
    static public void setTurnoutManager(TurnoutManager p) {
        instance().addTurnoutManager(p);
    }
    public void addTurnoutManager(TurnoutManager p) {
        ((jmri.managers.ProxyTurnoutManager)instance().turnoutManager).addManager(p);
        // if (p!=turnoutManager && turnoutManager!=null && log.isDebugEnabled()) log.debug("TurnoutManager instance is being replaced: "+p);
        // if (p!=turnoutManager && turnoutManager==null && log.isDebugEnabled()) log.debug("TurnoutManager instance is being installed: "+p);
        // turnoutManager = p;
    }

    private ConfigureManager configureManager = null;
    static public void setConfigureManager(ConfigureManager p) {
        instance().addConfigureManager(p);
    }
    public void addConfigureManager(ConfigureManager p) {
        if (p!=configureManager && configureManager!=null && log.isDebugEnabled()) log.debug("ConfigureManager instance is being replaced: "+p);
        if (p!=configureManager && configureManager==null && log.isDebugEnabled()) log.debug("ConfigureManager instance is being installed: "+p);
        configureManager = p;
    }

    private ThrottleManager throttleManager = null;
    static public void setThrottleManager(ThrottleManager p) {
        instance().addThrottleManager(p);
    }
    public void addThrottleManager(ThrottleManager p) {
        if (p!=throttleManager && throttleManager!=null && log.isDebugEnabled()) log.debug("ThrottleManager instance is being replaced: "+p);
        if (p!=throttleManager && throttleManager==null && log.isDebugEnabled()) log.debug("ThrottleManager instance is being installed: "+p);
        throttleManager = p;
    }

    private SignalHeadManager signalHeadManager = null;
    static public void setSignalHeadManager(SignalHeadManager p) {
        instance().addSignalHeadManager(p);
    }
    public void addSignalHeadManager(SignalHeadManager p) {
        if (p!=signalHeadManager && signalHeadManager!=null && log.isDebugEnabled()) log.debug("SignalHeadManager instance is being replaced: "+p);
        if (p!=signalHeadManager && signalHeadManager==null && log.isDebugEnabled()) log.debug("SignalHeadManager instance is being installed: "+p);
        signalHeadManager = p;
    }

    private CommandStation commandStation = null;
    static public void setCommandStation(CommandStation p) {
        instance().addCommandStation(p);
    }
    public void addCommandStation(CommandStation p) {
        if (p!=commandStation && commandStation!=null && log.isDebugEnabled()) log.debug("CommandStation instance is being replaced: "+p);
        if (p!=commandStation && commandStation==null && log.isDebugEnabled()) log.debug("CommandStation instance is being installed: "+p);
        commandStation = p;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(InstanceManager.class.getName());
}

/* @(#)InstanceManager.java */
