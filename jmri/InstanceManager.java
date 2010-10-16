// InstanceManager.java

package jmri;

import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.jmrit.audio.DefaultAudioManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Provides static members for locating various interface implementations.
 * These are the base of how JMRI objects are located.
 *<P>
 * The implementations of these interfaces are specific to the layout hardware, etc.
 * During initialization, objects of the right type are created and registered
 * with the ImplementationManager class, so they can later be retrieved by
 * non-system-specific code.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2001, 2008
 * @author                      Matthew Harris copyright (c) 2009
 * @version			$Revision: 1.68 $
 */
public class InstanceManager {

    static private HashMap<Class<?>,ArrayList<Object>> managerLists;
    
    static public <T> void store(T val, Class<T> type) {
        ArrayList<Object> l = managerLists.get(type);
        if (l==null) {
            l = new ArrayList<Object>();
            managerLists.put(type, l);
        }
        l.add(val);
    }
    
    static public <T> List<Object> getList(Class<T> type) {
        return managerLists.get(type);
    }
    
    static public <T> void reset(Class<T> type) {
        managerLists.put(type, null);
    }
    
    static public <T> void deregister(T val, Class<T> type){
        ArrayList<Object> l = managerLists.get(type);
        if(l!=null)
            l.remove(val);
    }

    /**
     * Get the first object of type T that was
     * store(d). 
     *
     * Someday, we may provide another way to set the default
     * but for now it's the last one stored
     */
    @SuppressWarnings("unchecked")   // checked by construction
    static public <T> T getDefault(Class<T> type) {
        List<Object> l = getList(type);
        if (l == null) return null;
        if (l.size()<1) return null;
        return (T)l.get(l.size()-1);
    }
    
    /**
     * Set an object of type T as the default for that type 
     *
     * Now, we do that moving the item to the front;
     * see the getDefault() method
     */
    @SuppressWarnings("unchecked")   // checked by construction
    static public <T> void setDefault(Class<T> type, T val) {
        List<Object> l = getList(type);
        if (l == null || (l.size()<1) ) {
            store(val, type);
            l = getList(type);
        }
        l.remove(val);
        l.add(val);
    }
    
    /**
     * Dump generic content of InstanceManager
     * by type.
     */
    @SuppressWarnings("unchecked")   // checked by construction
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    static public String contentsToString() {
        String retval = "";
        for (Class c : managerLists.keySet()) {
            retval+= "List of "+c+" with "+getList(c).size()+" objects\n";
            for (Object o : getList(c))
                retval += "    "+o.getClass().toString()+"\n";
        }
        return retval;
    }
    
    static InstanceInitializer initializer = new jmri.managers.DefaultInstanceInitializer();
    
    static public PowerManager powerManagerInstance()  { 
        return getDefault(PowerManager.class);
    }
    static public void setPowerManager(PowerManager p) {
        store(p, PowerManager.class);
    }

    static public ProgrammerManager programmerManagerInstance()  { 
        return getDefault(ProgrammerManager.class);
    }

    static public void setProgrammerManager(ProgrammerManager p) {
        store(p, ProgrammerManager.class);

    	// Now that we have a programmer manager, install the default
        // Consist manager if Ops mode is possible, and there isn't a
        // consist manager already.
		if(programmerManagerInstance().isAddressedModePossible() 
		    && consistManagerInstance() == null) {
			setConsistManager(new DccConsistManager());
		}
    }

    static public SensorManager sensorManagerInstance()  { return instance().sensorManager; }

    static public TurnoutManager turnoutManagerInstance()  { return instance().turnoutManager; }

    static public LightManager lightManagerInstance()  { return instance().lightManager; }

    static public ConfigureManager configureManagerInstance()  { return instance().configureManager; }

    static public ThrottleManager throttleManagerInstance()  { return instance().throttleManager; }

    static public SignalHeadManager signalHeadManagerInstance()  {
        if (instance().signalHeadManager != null) return instance().signalHeadManager;
        // As a convenience, we create a default object if none was provided explicitly.
        // This must be replaced when we start registering specific implementations
        instance().signalHeadManager = (SignalHeadManager)initializer.getDefault(SignalHeadManager.class);
        return instance().signalHeadManager;
    }

    static public SignalMastManager signalMastManagerInstance()  { 
        SignalMastManager m = getDefault(SignalMastManager.class);
        if (m == null) {
            m = (SignalMastManager)initializer.getDefault(SignalMastManager.class);
            setSignalMastManager(m);
        }
        return m;
    }
    static public void setSignalMastManager(SignalMastManager p) {
        store(p, SignalMastManager.class);
    }
    
    static public SignalSystemManager signalSystemManagerInstance()  { 
        SignalSystemManager m = getDefault(SignalSystemManager.class);
        if (m == null) {
            m = (SignalSystemManager)initializer.getDefault(SignalSystemManager.class);
            setSignalSystemManager(m);
        }
        return m;
    }

    static public void setSignalSystemManager(SignalSystemManager p) {
        store(p, SignalSystemManager.class);
    }

    static public SignalGroupManager signalGroupManagerInstance()  {
        SignalGroupManager m = getDefault(SignalGroupManager.class);
        if (m == null) {
            m = (SignalGroupManager)initializer.getDefault(SignalGroupManager.class);
            setSignalGroupManager(m);
        }
        return m;
    }

    static public void setSignalGroupManager(SignalGroupManager p) {
        store(p, SignalGroupManager.class);
    }

    static public BlockManager blockManagerInstance()  {
        BlockManager o = getDefault(BlockManager.class);
        if (o != null) return o;
        o = (BlockManager)initializer.getDefault(BlockManager.class);
        store(o, BlockManager.class);
        return o;
    }

    static public OBlockManager oBlockManagerInstance()  {
        if (instance().oBlockManager != null) return instance().oBlockManager;
        instance().oBlockManager = (OBlockManager)initializer.getDefault(OBlockManager.class);
        return instance().oBlockManager;
    }

    static public WarrantManager warrantManagerInstance()  {
        if (instance().warrantManager != null) return instance().warrantManager;
        instance().warrantManager = (WarrantManager)initializer.getDefault(WarrantManager.class);
        return instance().warrantManager;
    }

    static public SectionManager sectionManagerInstance()  {
        if (instance().sectionManager != null) return instance().sectionManager;
        instance().sectionManager = (SectionManager)initializer.getDefault(SectionManager.class);
        return instance().sectionManager;
    }

    static public TransitManager transitManagerInstance()  {
        if (instance().transitManager != null) return instance().transitManager;
        instance().transitManager = (TransitManager)initializer.getDefault(TransitManager.class);
        return instance().transitManager;
    }

    static public RouteManager routeManagerInstance()  {
        RouteManager r = getDefault(RouteManager.class);
        if (r != null) return r;
        r = (RouteManager)initializer.getDefault(RouteManager.class);
        store(r, RouteManager.class);
        return r;
    }

    static public LayoutBlockManager layoutBlockManagerInstance()  {
        if (instance().layoutBlockManager != null) return instance().layoutBlockManager;
        instance().layoutBlockManager = (LayoutBlockManager)initializer.getDefault(LayoutBlockManager.class);
        return instance().layoutBlockManager;
    }

    static public ConditionalManager conditionalManagerInstance()  {
        if (instance().conditionalManager != null) return instance().conditionalManager;
        instance().conditionalManager = (ConditionalManager)initializer.getDefault(ConditionalManager.class);
        return instance().conditionalManager;
    }

    static public LogixManager logixManagerInstance()  {
        if (instance().logixManager != null) return instance().logixManager;
        instance().logixManager = (LogixManager)initializer.getDefault(LogixManager.class);
        return instance().logixManager;
    }

    static public ShutDownManager shutDownManagerInstance()  {
        return instance().shutDownManager;
    }

    static public Timebase timebaseInstance()  {
        if (instance().timebase != null) return instance().timebase;
        instance().timebase = (Timebase)initializer.getDefault(Timebase.class);
        return instance().timebase;
    }

    static public ClockControl clockControlInstance()  {
        if (instance().clockControl != null) return instance().clockControl;
        instance().clockControl = (ClockControl)initializer.getDefault(ClockControl.class);
        return instance().clockControl;
    }
	static public void addClockControl(ClockControl cc) {
		instance().clockControl = cc;
	}
    
    static public ConsistManager consistManagerInstance() { return instance().consistManager; }

    static public CommandStation commandStationInstance()  { return instance().commandStation; }

    static public ReporterManager reporterManagerInstance()  { return instance().reporterManager; }

    static public CatalogTreeManager catalogTreeManagerInstance()  {
        if (instance().catalogTreeManager == null) instance().catalogTreeManager = (CatalogTreeManager)initializer.getDefault(CatalogTreeManager.class);        
        return instance().catalogTreeManager;
    }

    static public MemoryManager memoryManagerInstance()  { 
    	if (instance().memoryManager == null) instance().memoryManager = (MemoryManager)initializer.getDefault(MemoryManager.class);
    	return instance().memoryManager; 
    }

    static public AudioManager audioManagerInstance() {
        if (instance().audioManager == null) instance().audioManager = DefaultAudioManager.instance();
        return instance().audioManager;
    }
    
    static public RosterIconFactory rosterIconFactoryInstance()  { 
    	if (instance().rosterIconFactory == null) instance().rosterIconFactory = RosterIconFactory.instance();
    	return instance().rosterIconFactory; 
    }    

    static private InstanceManager instance() {
        if (root==null) root = new InstanceManager();
        return root;
    }

    public InstanceManager() {
        init();
    }

    // This is a separate, protected member so it
    // can be overridden in unit tests
    protected void init() {
        managerLists = new  HashMap<Class<?>,ArrayList<Object>>();
        turnoutManager = new jmri.managers.ProxyTurnoutManager();
        sensorManager = new jmri.managers.ProxySensorManager();
        lightManager = new jmri.managers.ProxyLightManager();
        reporterManager = new jmri.managers.ProxyReporterManager();
    }

    /**
     * The "root" object is the instance manager that's answering
     * requests for other instances. Protected access to allow
     * changes during JUnit testing.
     */
    static protected InstanceManager root;

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

    private OBlockManager oBlockManager = null;
    private WarrantManager warrantManager = null;
	
    private SectionManager sectionManager = null;
	
    private TransitManager transitManager = null;

    /**
     * @deprecated 2.9.5
     */
    @Deprecated
    static public void setRouteManager(RouteManager p) {
        store(p, RouteManager.class);
    }

    private LayoutBlockManager layoutBlockManager = null;
    static public void setLayoutBlockManager(LayoutBlockManager p) {
        instance().addLayoutBlockManager(p);
    }
    protected void addLayoutBlockManager(LayoutBlockManager p) {
        if (p!=layoutBlockManager && layoutBlockManager!=null && log.isDebugEnabled()) log.debug("LayoutBlockManager instance is being replaced: "+p);
        if (p!=layoutBlockManager && layoutBlockManager==null && log.isDebugEnabled()) log.debug("LayoutBlockManager instance is being installed: "+p);
        layoutBlockManager = p;
    }

    private ConditionalManager conditionalManager = null;
    static public void setConditionalManager(ConditionalManager p) {
        instance().addConditionalManager(p);
    }
    protected void addConditionalManager(ConditionalManager p) {
        if (p!=conditionalManager && conditionalManager!=null && log.isDebugEnabled()) log.debug("ConditionalManager instance is being replaced: "+p);
        if (p!=conditionalManager && conditionalManager==null && log.isDebugEnabled()) log.debug("ConditionalManager instance is being installed: "+p);
        conditionalManager = p;
    }

    private LogixManager logixManager = null;
    static public void setLogixManager(LogixManager p) {
        instance().addLogixManager(p);
    }
    protected void addLogixManager(LogixManager p) {
        if (p!=logixManager && logixManager!=null && log.isDebugEnabled()) log.debug("LogixManager instance is being replaced: "+p);
        if (p!=logixManager && logixManager==null && log.isDebugEnabled()) log.debug("LogixManager instance is being installed: "+p);
        logixManager = p;
    }

    private ShutDownManager shutDownManager = null;
    static public void setShutDownManager(ShutDownManager p) {
        instance().addShutDownManager(p);
    }
    protected void addShutDownManager(ShutDownManager p) {
        if (p!=shutDownManager && shutDownManager!=null && log.isDebugEnabled()) log.debug("ShutDownManager instance is being replaced: "+p);
        if (p!=shutDownManager && shutDownManager==null && log.isDebugEnabled()) log.debug("ShutDownManager instance is being installed: "+p);
        shutDownManager = p;
    }

    private Timebase timebase = null;
	
    private ClockControl clockControl = null;

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

    private CatalogTreeManager catalogTreeManager = null;

    private AudioManager audioManager = null;

	private MemoryManager memoryManager = null;
	
	private RosterIconFactory rosterIconFactory = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InstanceManager.class.getName());
}

/* @(#)InstanceManager.java */
