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
 * @version			$Revision: 1.25 $
 */
public class InstanceManager {

    static public PowerManager powerManagerInstance()  { return instance().powerManager; }

    static public ProgrammerManager programmerManagerInstance()  { return instance().programmerManager; }

    static public SensorManager sensorManagerInstance()  { return instance().sensorManager; }

    static public TurnoutManager turnoutManagerInstance()  { return instance().turnoutManager; }

    static public LightManager lightManagerInstance()  { return instance().lightManager; }

    static public ConfigureManager configureManagerInstance()  { return instance().configureManager; }

    static public ThrottleManager throttleManagerInstance()  { return instance().throttleManager; }

    static public SignalHeadManager signalHeadManagerInstance()  {
        if (instance().signalHeadManager != null) return instance().signalHeadManager;
        // As a convenience, we create a default object if none was provided explicitly.
        // This must be replaced when we start registering specific implementations
        instance().signalHeadManager = new AbstractSignalHeadManager();
        return instance().signalHeadManager;
    }

    static public RouteManager routeManagerInstance()  {
        if (instance().routeManager != null) return instance().routeManager;
        // As a convenience, we create a default object if none was provided explicitly.
        // This must be replaced when we start registering specific implementations
        instance().routeManager = new DefaultRouteManager();
        return instance().routeManager;
    }

    static public Timebase timebaseInstance()  {
        if (instance().timebase != null) return instance().timebase;
        // As a convenience, we create a default object if none was provided explicitly.
        // This must be replaced when we start registering specific implementations
        instance().timebase = new jmri.jmrit.simpleclock.SimpleTimebase();
        if (InstanceManager.configureManagerInstance() != null)
            InstanceManager.configureManagerInstance().registerConfig(instance().timebase);        
        return instance().timebase;
    }

    static public ConsistManager consistManagerInstance() { return instance().consistManager; }

    static public CommandStation commandStationInstance()  { return instance().commandStation; }

    static public ReporterManager reporterManagerInstance()  { return instance().reporterManager; }

    static public MemoryManager memoryManagerInstance()  { 
    	if (instance().memoryManager == null) instance().memoryManager = DefaultMemoryManager.instance();
    	return instance().memoryManager; 
    }

    static private InstanceManager instance() {
        if (root==null) root = new InstanceManager();
        return root;
    }

    private InstanceManager() {
        turnoutManager = new jmri.managers.ProxyTurnoutManager();
        sensorManager = new jmri.managers.ProxySensorManager();
        lightManager = new jmri.managers.ProxyLightManager();
        reporterManager = new jmri.managers.ProxyReporterManager();
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
    protected void addPowerManager(PowerManager p) {
        if (p!=powerManager && powerManager!=null && log.isDebugEnabled()) log.debug("PowerManager instance is being replaced: "+p);
        if (p!=powerManager && powerManager==null && log.isDebugEnabled()) log.debug("PowerManager instance is being installed: "+p);
        powerManager = p;
    }

    private ProgrammerManager programmerManager = null;
    static public void setProgrammerManager(ProgrammerManager p) {
        instance().addProgrammerManager(p);
    }
    protected void addProgrammerManager(ProgrammerManager p) {
        if (p!=programmerManager && programmerManager!=null && log.isDebugEnabled()) log.debug("ProgrammerManager instance is being replaced: "+p);
        if (p!=programmerManager && programmerManager==null && log.isDebugEnabled()) log.debug("ProgrammerManager instance is being installed: "+p);
        programmerManager = p;
	// Now that we have a programmer manager, install the default
        // Consist manager if Ops mode is possible, and there isn't a
        // consist manager already.
	if(programmerManager.isOpsModePossible() && consistManager == null) {
   		 setConsistManager(new DccConsistManager());
	}
    }

    private SensorManager sensorManager = null;
    static public void setSensorManager(SensorManager p) {
        instance().addSensorManager(p);
    }
    protected void addSensorManager(SensorManager p) {
        ((jmri.managers.AbstractProxyManager)instance().sensorManager).addManager(p);
    }

    private TurnoutManager turnoutManager = null;
    static public void setTurnoutManager(TurnoutManager p) {
        instance().addTurnoutManager(p);
    }
    protected void addTurnoutManager(TurnoutManager p) {
        ((jmri.managers.AbstractProxyManager)instance().turnoutManager).addManager(p);
    }

    private LightManager lightManager = null;
    static public void setLightManager(LightManager p) {
        instance().addLightManager(p);
    }
    protected void addLightManager(LightManager p) {
        ((jmri.managers.AbstractProxyManager)instance().lightManager).addManager(p);
    }

    private ConfigureManager configureManager = null;
    static public void setConfigureManager(ConfigureManager p) {
        instance().addConfigureManager(p);
    }
    protected void addConfigureManager(ConfigureManager p) {
        if (p!=configureManager && configureManager!=null && log.isDebugEnabled()) log.debug("ConfigureManager instance is being replaced: "+p);
        if (p!=configureManager && configureManager==null && log.isDebugEnabled()) log.debug("ConfigureManager instance is being installed: "+p);
        configureManager = p;
    }

    private ThrottleManager throttleManager = null;
    static public void setThrottleManager(ThrottleManager p) {
        instance().addThrottleManager(p);
    }
    protected void addThrottleManager(ThrottleManager p) {
        if (p!=throttleManager && throttleManager!=null && log.isDebugEnabled()) log.debug("ThrottleManager instance is being replaced: "+p);
        if (p!=throttleManager && throttleManager==null && log.isDebugEnabled()) log.debug("ThrottleManager instance is being installed: "+p);
        throttleManager = p;
    }

    private SignalHeadManager signalHeadManager = null;
    static public void setSignalHeadManager(SignalHeadManager p) {
        instance().addSignalHeadManager(p);
    }
    protected void addSignalHeadManager(SignalHeadManager p) {
        if (p!=signalHeadManager && signalHeadManager!=null && log.isDebugEnabled()) log.debug("SignalHeadManager instance is being replaced: "+p);
        if (p!=signalHeadManager && signalHeadManager==null && log.isDebugEnabled()) log.debug("SignalHeadManager instance is being installed: "+p);
        signalHeadManager = p;
    }

    private RouteManager routeManager = null;
    static public void setRouteManager(RouteManager p) {
        instance().addRouteManager(p);
    }
    protected void addRouteManager(RouteManager p) {
        if (p!=routeManager && routeManager!=null && log.isDebugEnabled()) log.debug("RouteManager instance is being replaced: "+p);
        if (p!=routeManager && routeManager==null && log.isDebugEnabled()) log.debug("RouteManager instance is being installed: "+p);
        routeManager = p;
    }

    private Timebase timebase = null;

    private ConsistManager consistManager = null;

    static public void setConsistManager(ConsistManager p) {
        instance().addConsistManager(p);
    }

    protected void addConsistManager(ConsistManager p) {
        if (p!=consistManager && consistManager!=null && log.isDebugEnabled()) log.debug("ConsistManager instance is being replaced: "+p);
        if (p!=consistManager && consistManager==null && log.isDebugEnabled()) log.debug("consistManager instance is being installed: "+p);
        consistManager = p;
    }

    private CommandStation commandStation = null;
    static public void setCommandStation(CommandStation p) {
        instance().addCommandStation(p);
    }
    protected void addCommandStation(CommandStation p) {
        if (p!=commandStation && commandStation!=null && log.isDebugEnabled()) log.debug("CommandStation instance is being replaced: "+p);
        if (p!=commandStation && commandStation==null && log.isDebugEnabled()) log.debug("CommandStation instance is being installed: "+p);
        commandStation = p;
    }

    private ReporterManager reporterManager = null;
    static public void setReporterManager(ReporterManager p) {
        instance().addReporterManager(p);
    }
    protected void addReporterManager(ReporterManager p) {
        ((jmri.managers.AbstractProxyManager)instance().reporterManager).addManager(p);
    }


	private MemoryManager memoryManager = null;
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(InstanceManager.class.getName());
}

/* @(#)InstanceManager.java */
