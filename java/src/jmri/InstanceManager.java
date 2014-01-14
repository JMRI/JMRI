// InstanceManager.java

package jmri;

import apps.gui3.TabbedPreferences;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import jmri.implementation.DccConsistManager;
import jmri.implementation.NmraConsistManager;
import jmri.jmrit.audio.DefaultAudioManager;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for locating various interface implementations.
 * These form the base for locating JMRI objects, including the key managers.
 *<p>
 * The structural goal is to have the jmri package not depend on the
 * lower jmri.jmrit and jmri.jmrix packages, with the implementations
 * still available at run-time through the InstanceManager.
 *<p>
 * To retrieve the default object of a specific type, do 
 * {@link    InstanceManager#getDefault}
 * where the argument is e.g. "SensorManager.class".
 * In other words, you ask for the default object of a particular type.
 *<p>
 * Multiple items can be held, and are retrieved as a list with
 * {@link    InstanceManager#getList}.
 *<p>
 * If a specific item is needed, e.g. one that has been constructed via
 * a complex process during startup, it should be installed with
 * {@link     InstanceManager#store}.
 * If it's OK for the InstanceManager to create an object on first
 * request, have that object's class implement the 
 * {@link     InstanceManagerAutoDefault}
 * flag interface. The InstanceManager will then construct a default
 * object via the no-argument constructor when one is first needed.
 *<p>
 * For initialization of more complex objects, see the 
 * {@link InstanceInitializer} mechanism and it's default implementation
 * in {@link jmri.managers.DefaultInstanceInitializer}.
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
 * @author			Bob Jacobsen Copyright (C) 2001, 2008, 2013
 * @author          Matthew Harris copyright (c) 2009
 * @version			$Revision$
 */
public class InstanceManager {

    static private HashMap<Class<?>,ArrayList<Object>> managerLists;
    /* properties */
    public static String CONSIST_MANAGER = "consistmanager"; // NOI18N
    public static String COMMAND_STATION = "commandstation"; // NOI18N
    public static String PROGRAMMER_MANAGER = "programmermanager"; // NOI18N
    public static String THROTTLE_MANAGER = "throttlemanager"; // NOI18N
    
    /**
     * Store an object of a particular type for later
     * retrieval via {@link #getDefault} or {@link #getList}.
     * @param item The object of type T to be stored
     * @param type The class Object for the item's type.  This will be used
     *               as the key to retrieve the object later.
     */
    static public <T> void store(T item, Class<T> type) {
        ArrayList<Object> l = managerLists.get(type);
        if (l==null) {
            l = new ArrayList<Object>();
            managerLists.put(type, l);
        }
        l.add(item);
    }
    
    /**
     * Retrieve a list of all objects of type T that were
     * registered with {@link #store}.
     * @param type The class Object for the items' type.
     */
    static public <T> List<Object> getList(Class<T> type) {
        if (managerLists!=null)
            return managerLists.get(type);
        return null;
    }
    
    /**
     * Deregister all objects of a particular type.
     * @param type The class Object for the items to be removed.
     */
    static public <T> void reset(Class<T> type) {
        if (managerLists == null) return;
        managerLists.put(type, null);
    }
    
    /**
     * Remove an object of a particular type 
     * that had earlier been registered with {@link #store}.
     * @param item The object of type T to be deregistered
     * @param type The class Object for the item's type.  
     */
    static public <T> void deregister(T item, Class<T> type){
        if (managerLists == null) return;
        ArrayList<Object> l = managerLists.get(type);
        if(l!=null)
            l.remove(item);
    }

    /**
     * Retrieve the last object of type T that was
     * registered with {@link #store}.
     * <p>
     * Someday, we may provide another way to set the default
     * but for now it's the last one stored, see the
     * {@link #setDefault} method.
     */
    @SuppressWarnings("unchecked")   // checked by construction
    static public <T> T getDefault(Class<T> type) {
        if (managerLists == null) return null;
        ArrayList<Object> l = managerLists.get(type);
        if (l == null || l.size()<1) {
            // see if need to autocreate
            if (InstanceManagerAutoDefault.class.isAssignableFrom(type)) {
                // yes, make sure list is present before creating object
                if (l==null) {
                    l = new ArrayList<Object>();
                    managerLists.put(type, l);
                }
                try {
                    l.add(type.getConstructor((Class[])null).newInstance((Object[])null));
                } catch (Exception e) {
                    log.error("Exception creating default object", e); // unexpected
                    return null;
                }
                return (T)l.get(l.size()-1);
            } else {
                return null;
            }
        }
        return (T)l.get(l.size()-1);
    }
    
    /**
     * Set an object of type T as the default for that type.
     *<p>
     * Also registers (stores) the object if not already present. 
     *<p>
     * Now, we do that moving the item to the back of the list;
     * see the {@link #getDefault} method
     */
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
    static public String contentsToString() {

        StringBuffer retval = new StringBuffer();
        for (Class<?> c : managerLists.keySet()) {
            retval.append("List of");
            retval.append(c);
            retval.append(" with ");
            retval.append(Integer.toString(getList(c).size()));
            retval.append(" objects\n");
            for (Object o : getList(c)){
                retval.append("    ");
                retval.append(o.getClass().toString());
                retval.append("\n");
            }
        }
        return retval.toString();
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
        instance().notifyPropertyChangeListener(PROGRAMMER_MANAGER, null, null);
    }

    static public SensorManager sensorManagerInstance()  { return instance().sensorManager; }

    static public TurnoutManager turnoutManagerInstance()  { return instance().turnoutManager; }

    static public LightManager lightManagerInstance()  { return instance().lightManager; }

    static public ConfigureManager configureManagerInstance()  { return instance().configureManager; }

    static public ThrottleManager throttleManagerInstance()  {
        return getDefault(ThrottleManager.class);
    }

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

    /**
     * @deprecated Since 3.3.1, use @{link #getDefault} directly.
     */
    @Deprecated
    static public jmri.jmrit.logix.OBlockManager oBlockManagerInstance()  {
        return getDefault(jmri.jmrit.logix.OBlockManager.class);
    }

    /**
     * @deprecated Since 3.3.1, use @{link #getDefault} directly.
     */
    @Deprecated
    static public jmri.jmrit.logix.WarrantManager warrantManagerInstance()  {
        return getDefault(jmri.jmrit.logix.WarrantManager.class);
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

    static public SignalMastLogicManager signalMastLogicManagerInstance()  {
        SignalMastLogicManager r = getDefault(SignalMastLogicManager.class);
        if (r != null) return r;
        r = (SignalMastLogicManager)initializer.getDefault(SignalMastLogicManager.class);
        store(r, SignalMastLogicManager.class);
        return r;
    }

    static public RouteManager routeManagerInstance()  {
        RouteManager r = getDefault(RouteManager.class);
        if (r != null) return r;
        r = (RouteManager)initializer.getDefault(RouteManager.class);
        store(r, RouteManager.class);
        return r;
    }

    /**
     * @deprecated Since 3.3.1, use @{link #getDefault} directly.
     */
    @Deprecated
    static public jmri.jmrit.display.layoutEditor.LayoutBlockManager layoutBlockManagerInstance()  {
        return getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
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
    
    static public TabbedPreferences tabbedPreferencesInstance()  {
        return instance().tabbedPreferencesManager;
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
    
    static public ConsistManager consistManagerInstance() { return getDefault(ConsistManager.class); 
    }

    static public CommandStation commandStationInstance()  {
        return getDefault(CommandStation.class);
    }

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

    static public VSDecoderManager vsdecoderManagerInstance() {
	if (instance().vsdecoderManager == null) instance().vsdecoderManager = VSDecoderManager.instance();
	return instance().vsdecoderManager;
    }

    static private InstanceManager instance() {
        if (root==null){
            setRootInstance();
        }
        return root;
    }
    
    private static synchronized void setRootInstance(){
        if(root!=null)
            return;
        root = new InstanceManager();
    }

    public InstanceManager() {
        init();
    }

    // This is a separate, protected member so it
    // can be overridden in unit tests
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                    justification="Only used during system initialization")
    protected void init() {
        managerLists = new  HashMap<Class<?>,ArrayList<Object>>();
        sensorManager = new jmri.managers.ProxySensorManager();
        turnoutManager = new jmri.managers.ProxyTurnoutManager();
        lightManager = new jmri.managers.ProxyLightManager();
        reporterManager = new jmri.managers.ProxyReporterManager();
    }

    /**
     * The "root" object is the instance manager that's answering
     * requests for other instances. Protected access to allow
     * changes during JUnit testing.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value="MS_PKGPROTECT",
        justification="Protected access to allow changes during JUnit testing.")
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

    static public void setThrottleManager(ThrottleManager p) {
        store(p, ThrottleManager.class);
        instance().notifyPropertyChangeListener(THROTTLE_MANAGER, null, null);
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

    private SectionManager sectionManager = null;
	
    private TransitManager transitManager = null;

    /**
     * @deprecated 2.9.5
     */
    @Deprecated
    static public void setRouteManager(RouteManager p) {
        store(p, RouteManager.class);
    }

    /**
     * @deprecated Since 3.3.1, use @{link #store} directly.
     */
    @Deprecated
    static public void setLayoutBlockManager(jmri.jmrit.display.layoutEditor.LayoutBlockManager p) {
        store(p, jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
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

    private TabbedPreferences tabbedPreferencesManager = null;
    static public void setTabbedPreferences(TabbedPreferences p) {
        instance().addTabbedPreferences(p);
    }
    protected void addTabbedPreferences(TabbedPreferences p) {
        tabbedPreferencesManager = p;
    }
    
    private Timebase timebase = null;
	
    private ClockControl clockControl = null;

    static public void setConsistManager(ConsistManager p) {
        store(p, ConsistManager.class);
        instance().notifyPropertyChangeListener(CONSIST_MANAGER, null, null);
    }


    static public void setCommandStation(CommandStation p) {
         store(p, CommandStation.class);
	 if(consistManagerInstance() == null || 
            (consistManagerInstance()).getClass()==DccConsistManager.class){
                // if there is a command station available, use
                // the NMRA consist manager instead of the generic consist
                // manager.
		setConsistManager(new NmraConsistManager());
	 }
         instance().notifyPropertyChangeListener(COMMAND_STATION, null, null);
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

    private VSDecoderManager vsdecoderManager = null;

    public static synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (listeners.contains(l)) {
            listeners.removeElement(l);
        }
    }

    public static synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        // add only if not already registered
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
    }
    
    /**
     * Trigger the notification of all PropertyChangeListeners
     */
    @SuppressWarnings("unchecked")
	protected void notifyPropertyChangeListener(String property, Object oldValue, Object newValue) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<PropertyChangeListener> v;
        synchronized(this)
            {
                v = (Vector<PropertyChangeListener>) listeners.clone();
            }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            PropertyChangeListener client = v.elementAt(i);
            client.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }
    
    // data members to hold contact with the property listeners
    final private static Vector<PropertyChangeListener> listeners = new Vector<PropertyChangeListener>();

    static Logger log = LoggerFactory.getLogger(InstanceManager.class.getName());
}

/* @(#)InstanceManager.java */
