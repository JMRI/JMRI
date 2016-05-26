 package jmri.jmrit.logix;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Path;
import jmri.Sensor;
import jmri.Turnout;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OBlock extends jmri.Block to be used in Logix Conditionals and Warrants. It
 * is the smallest piece of track that can have occupancy detection. A better
 * name would be Detection Circuit. However, an OBlock can be defined without an
 * occupancy sensor and used to calculate routes.
 *
 * Additional states are defined to indicate status of the track and trains to
 * control panels. A jmri.Block has a PropertyChangeListener on the occupancy
 * sensor and the OBlock will pass state changes of the sensor on to its
 * warrant.
 *
 * <P>
 * Entrances (exits when train moves in opposite direction) to OBlocks have
 * Portals. A Portal object is a pair of OBlocks. Each OBlock has a list of its
 * Portals.
 *
 * <P>
 * When an OBlock (Detection Circuit) has a Portal whose entrance to the OBlock
 * has a signal, then the OBlock and its chains of adjacent OBlocks up to the
 * next OBlock having an entrance Portal with a signal, can be considered a
 * "Block" in the sense of a prototypical railroad. Preferrably all entrances to
 * the "Block" should have entrance Portals with a signal.
 *
 *
 * <P>
 * A Portal has a list of paths (OPath objects) for each OBlock it separates.
 * The paths are determined by the turnout settings of the turnouts contained in
 * the block. Paths are contained within the Block boundaries. Names of OPath
 * objects only need be unique within an OBlock.
 *
 * <BR>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * </P><P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * </P>
 *
 * @author	Pete Cressman (C) 2009
 */
public class OBlock extends jmri.Block implements java.beans.PropertyChangeListener {

    /*
     * Block states.
     * NamedBean.UNKNOWN                 = 0x01;
     * Block.OCCUPIED =  Sensor.ACTIVE =   0x02;
     * Block.UNOCCUPIED = Sensor.INACTIVE= 0x04;
     * NamedBean.INCONSISTENT            = 0x08;
     * Add the following to the 4 sensor states.
     * States are OR'ed to show combination.  e.g. ALLOCATED | OCCUPIED = allocated block is occupied by rouge
     */
    static final public int ALLOCATED = 0x10;   // reserve the block for subsequent use by a train
    static final public int RUNNING = 0x20;     // Block that running train has reached 
    static final public int OUT_OF_SERVICE = 0x40;     // Block that running train has reached 
    static final public int DARK = 0x01;        // Block has no Sensor, same as UNKNOWN
    static final public int TRACK_ERROR = 0x80; // Block has Error

    static final HashMap<String, Integer> _oldstatusMap = new HashMap<String, Integer>();
    static final HashMap<String, Integer> _statusMap = new HashMap<String, Integer>();
    static final HashMap<String, String> _statusNameMap = new HashMap<String, String>();

    static final Color DEFAULT_FILL_COLOR = new Color(200, 0, 200);

    static void loadStatusMap() {
        _statusMap.put("unoccupied", Integer.valueOf(UNOCCUPIED));
        _statusMap.put("occupied", Integer.valueOf(OCCUPIED));
        _statusMap.put("allocated", Integer.valueOf(ALLOCATED));
        _statusMap.put("running", Integer.valueOf(RUNNING));
        _statusMap.put("outOfService", Integer.valueOf(OUT_OF_SERVICE));
        _statusMap.put("dark", Integer.valueOf(DARK));
        _statusMap.put("powerError", Integer.valueOf(TRACK_ERROR));
        _oldstatusMap.put(Bundle.getMessage("unoccupied"), Integer.valueOf(UNOCCUPIED));
        _oldstatusMap.put(Bundle.getMessage("occupied"), Integer.valueOf(OCCUPIED));
        _oldstatusMap.put(Bundle.getMessage("allocated"), Integer.valueOf(ALLOCATED));
        _oldstatusMap.put(Bundle.getMessage("running"), Integer.valueOf(RUNNING));
        _oldstatusMap.put(Bundle.getMessage("outOfService"), Integer.valueOf(OUT_OF_SERVICE));
        _oldstatusMap.put(Bundle.getMessage("dark"), Integer.valueOf(DARK));
        _oldstatusMap.put(Bundle.getMessage("powerError"), Integer.valueOf(TRACK_ERROR));
    }

    static void loadStatusNameMap() {
        _statusNameMap.put(Bundle.getMessage("unoccupied"), "unoccupied");
        _statusNameMap.put(Bundle.getMessage("occupied"), "occupied");
        _statusNameMap.put(Bundle.getMessage("allocated"), "allocated");
        _statusNameMap.put(Bundle.getMessage("running"), "running");
        _statusNameMap.put(Bundle.getMessage("outOfService"), "outOfService");
        _statusNameMap.put(Bundle.getMessage("dark"), "dark");
        _statusNameMap.put(Bundle.getMessage("powerError"), "powerError");
    }

    public static Iterator<String> getLocalStatusNames() {
        if (_statusNameMap.size() == 0) {
            loadStatusNameMap();
        }
        return _statusNameMap.keySet().iterator();
    }

    public static String getLocalStatusName(String str) {
        try {
            return Bundle.getMessage(str);
        } catch (java.util.MissingResourceException mre) {
            return str;
        }
    }

    public static String getSystemStatusName(String str) {
        if (_statusNameMap.size() == 0) {
            loadStatusNameMap();
        }
        return _statusNameMap.get(str);
    }
    /* maybe show fast clock time later
     protected static Timebase 	_clock = InstanceManager.timebaseInstance();
     private java.util.Date	_entryTime;		// time when block became occupied
     */
    ArrayList<Portal> _portals = new ArrayList<Portal>();     // portals to this block

    private Warrant _warrant;       // when not null, block is allocated to this warrant
    private String _pathName;      // when not null, this is the allocated path
    protected long _entryTime;		// time when block became occupied
    private boolean _metric = false; // desired display mode
    private NamedBeanHandle<Sensor> _errNamedSensor;
    // path keys a list of Blocks whose paths conflict with the path.  These Blocks key 
    // a list of their conflicting paths.
    private HashMap<String, List<HashMap<OBlock, List<OPath>>>> _sharedTO
            = new HashMap<String, List<HashMap<OBlock, List<OPath>>>>();
    private boolean _ownsTOs = false;
    private Color _markerForeground = Color.WHITE;
    private Color _markerBackground = DEFAULT_FILL_COLOR;
    private Font _markerFont;

    public OBlock(String systemName) {
        super(systemName);
        setState(DARK);
    }

    public OBlock(String systemName, String userName) {
        super(systemName, userName);
        setState(DARK);
    }


    /** 
     * Note: equality consists of the underlying (superclass) Block implementation
     * being the same.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (!(getClass() == obj.getClass())) {
            return false;
        }

        return super.equals(obj);
    }

    /**
     * override to only set an existing sensor and to amend state with not DARK
     * return true if an existing Sensor is set or sensor is to be removed from
     * block
     */
    @Override
    public boolean setSensor(String pName) {
        boolean ret = false;
        String oldName = null;
        Sensor sensor = getSensor();
        if (sensor != null) {
            oldName = sensor.getDisplayName();
        }
        if (pName == null || pName.trim().length() == 0) {
            setState(DARK);
            setNamedSensor(null);
            ret = true;
        } else {
            sensor = InstanceManager.sensorManagerInstance().getByUserName(pName);
            if (sensor == null) {
                sensor = InstanceManager.sensorManagerInstance().getBySystemName(pName);
            }
            if (sensor == null) {
                setState(DARK);
                if (log.isDebugEnabled()) {
                    log.debug("no sensor named \"" + pName + "\" exists.");
                }
                ret = false;
            } else {
                setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));
                setState(getSensor().getState() & ~DARK);
                ret = true;
            }
        }
        firePropertyChange("OccupancySensorChange", oldName, pName);
        return ret;
    }

    // override to determine if not DARK
    @Override
    public void setNamedSensor(NamedBeanHandle<Sensor> namedSensor) {
        super.setNamedSensor(namedSensor);
        if (namedSensor != null) {
            setState(getSensor().getState() & ~DARK);
        }
    }

    /*
     * retuen true if successful
     */
    public boolean setErrorSensor(String pName) {
        if (getErrorSensor() != null) {
            getErrorSensor().removePropertyChangeListener(this);
        }
        if (pName == null || pName.trim().length() == 0) {
            _errNamedSensor = null;
            return true;
        }
        Sensor sensor = InstanceManager.sensorManagerInstance().getByUserName(pName);
        if (sensor == null) {
            sensor = InstanceManager.sensorManagerInstance().getBySystemName(pName);
        }
        if (sensor == null) {
            if (log.isDebugEnabled()) {
                log.debug("no sensor named \"" + pName + "\" exists.");
            }
        }

        sensor = jmri.InstanceManager.sensorManagerInstance().getSensor(pName);
        if (sensor != null) {
            _errNamedSensor = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor);
            getErrorSensor().addPropertyChangeListener(this, _errNamedSensor.getName(), "OBlock Error Sensor " + getDisplayName());
            return true;
        }
        return false;
    }

    public Sensor getErrorSensor() {
        if (_errNamedSensor == null) {
            return null;
        }
        return _errNamedSensor.getBean();
    }

    public NamedBeanHandle<Sensor> getNamedErrorSensor() {
        return _errNamedSensor;
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) {
            log.debug("property change: " + getDisplayName() + " property " + evt.getPropertyName() + " is now "
                    + evt.getNewValue() + " from " + evt.getSource().getClass().getName());
        }
        if ((getErrorSensor() != null) && (evt.getSource().equals(getErrorSensor()))) {
            if (evt.getPropertyName().equals("KnownState")) {
                int errState = ((Integer) evt.getNewValue()).intValue();
                int oldState = getState();
                if (errState == Sensor.ACTIVE) {
                    setState(oldState | TRACK_ERROR);
                } else {
                    setState(oldState & ~TRACK_ERROR);
                }
                firePropertyChange("pathState", Integer.valueOf(oldState), Integer.valueOf(getState()));
            }
        }
    }

    /**
     * This block shares a turnout (e.g. a crossover) with another block.
     * Typically one JMRI turnout driving two switches where each switch is in a
     * different block.
     *
     * @param key   a path in this block
     * @param block another block
     * @param path  a path in that block sharing a turnout with key
     */
    public boolean addSharedTurnout(OPath key, OBlock block, OPath path) {
        if (log.isDebugEnabled()) {
            log.debug("Path " + key.getName() + " in block \"" + getDisplayName()
                    + "\" has turnouts shared with path " + path.getName() + " in block " + block.getDisplayName());
        }
        List<HashMap<OBlock, List<OPath>>> blockList = _sharedTO.get(key.getName());
        if (blockList != null) {
            Iterator<HashMap<OBlock, List<OPath>>> iter = blockList.iterator();
            while (iter.hasNext()) {
                HashMap<OBlock, List<OPath>> map = iter.next();
                Iterator<Entry<OBlock, List<OPath>>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<OBlock, List<OPath>> entry = it.next();
                    OBlock b = entry.getKey();
                    if (b.equals(block)) {
                        List<OPath> pathList = entry.getValue();
                        if (pathList.contains(path)) {
                            return false;
                        } else {
                            pathList.add(path);
                            return true;
                        }
                    } else {
                        List<OPath> pathList = new ArrayList<OPath>();
                        pathList.add(path);
                        map.put(block, pathList);
                        return true;
                    }
                }
            }
            HashMap<OBlock, List<OPath>> map = new HashMap<OBlock, List<OPath>>();
            List<OPath> pathList = new ArrayList<OPath>();
            pathList.add(path);
            map.put(block, pathList);
            blockList.add(map);
            return true;
        } else {
            List<OPath> pathList = new ArrayList<OPath>();
            pathList.add(path);
            HashMap<OBlock, List<OPath>> map = new HashMap<OBlock, List<OPath>>();
            map.put(block, pathList);
            blockList = new ArrayList<HashMap<OBlock, List<OPath>>>();
            blockList.add(map);
            _sharedTO.put(key.getName(), blockList);
            return true;
        }
    }

    /**
     * Called from setPath. looking for other warrants that may have allocated
     * blocks that share TO's with this block.
     *
     * @return
     */
    private String checkSharedTO() {
        List<HashMap<OBlock, List<OPath>>> blockList = _sharedTO.get(_pathName);
        if (blockList != null) {
            Iterator<HashMap<OBlock, List<OPath>>> iter = blockList.iterator();
            if (log.isDebugEnabled()) {
                log.debug("Path " + _pathName + " in block \"" + getDisplayName()
                        + "\" has turnouts thrown from " + blockList.size() + " other blocks");
            }
            while (iter.hasNext()) {
                HashMap<OBlock, List<OPath>> map = iter.next();
                Iterator<Entry<OBlock, List<OPath>>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<OBlock, List<OPath>> entry = it.next();
                    OBlock block = entry.getKey();
                    Iterator<OPath> i = entry.getValue().iterator();
                    while (i.hasNext()) {
                        OPath path = i.next();
                        if (log.isDebugEnabled()) {
                            log.debug("Path " + _pathName + " in block \"" + getDisplayName()
                                    + "\" has turnouts shared with path " + path.getName() + " in block " + block.getDisplayName());
                        }
                        // call sharing block to check for conflict
                        String name = block.isPathSet(path.getName());
                        if (name != null) {
                            _warrant.setShareTOBlock(block);
                            return Bundle.getMessage("pathIsSet", _pathName, getDisplayName(),
                                    _warrant.getDisplayName(), path.getName(),
                                    block.getDisplayName(), name);
                        } else {
                            _ownsTOs = true;
                        }
                    }
                }
            }

        }
        return null;
    }

    protected boolean ownsTOs() {
        return _ownsTOs;
    }

    /**
     * Another block sharing a turnout with this block queries whether turnout
     * is in use.
     *
     * @param path that uses a common shared turnout
     * @return If warrant exists and path==pathname, return warrant display
     *         name, else null.
     */
    protected String isPathSet(String path) {
        String msg = null;
        if (_warrant != null) {
            if (path.equals(_pathName)) {
                msg = _warrant.getDisplayName();
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Path \"" + path + "\" set in block \"" + getDisplayName()
                    + "\"= " + (msg != null) + ". _pathName= " + _pathName);
        }
        return msg;
    }

    protected Warrant getWarrant() {
        return _warrant;
    }
    
    public boolean isAllocatedTo(Warrant warrant) {
        return (warrant == _warrant);
    }

    public String getAllocatedPathName() {
        return _pathName;
    }

    public void setMetricUnits(boolean type) {
        _metric = type;
    }

    public boolean isMetric() {
        return _metric;
    }

    public void setMarkerForeground(Color c) {
        _markerForeground = c;
    }

    public Color getMarkerForeground() {
        return _markerForeground;
    }

    public void setMarkerBackground(Color c) {
        _markerBackground = c;
    }

    public Color getMarkerBackground() {
        return _markerBackground;
    }

    public void setMarkerFont(Font f) {
        _markerFont = f;
    }

    public Font getMarkerFont() {
        return _markerFont;
    }

    /**
     * override
     *
     */
    @Override
    public void setValue(Object o) {
        super.setValue(o);
        if (o == null) {
            _markerForeground = Color.WHITE;
            _markerBackground = DEFAULT_FILL_COLOR;
            _markerFont = null;
        }
    }

    /*_
     *  From the universal name for block status, check if it is the current status
     */
    public boolean statusIs(String statusName) {
        if (_statusMap.size() == 0) {
            loadStatusMap();
        }
        Integer i = _statusMap.get(statusName);
        if (i == null) {
            i = _oldstatusMap.get(statusName);
        }
        if (i != null) {
            return ((getState() & i.intValue()) != 0);
        }
        log.error("\"" + statusName
                + "\" resource not found.  Please update Conditional State Variable testing OBlock \""
                + getDisplayName() + "\" status.");
        return false;
    }

    /**
     * Test that block is not occupied and and not allocated
     */
    public boolean isFree() {
        int state = getState();
        return ((state & ALLOCATED) == 0 && (state & OCCUPIED) == 0);
    }

    /**
     * Allocate (reserves) the block for the Warrant that is the 'value' object
     * Note the block may be OCCUPIED by a non-warranted train, but the
     * allocation is permitted.
     *
     * @param warrant
     * @return name of block if block is already allocated to another warrant or
     *         block is OUT_OF_SERVICE
     */
    protected String allocate(Warrant warrant) {
        if (log.isDebugEnabled()) {
            log.debug("Allocate block \"" + getDisplayName()
                    + "\" to warrant \"" + warrant.getDisplayName() + "\"");
        }
        if (warrant == null) {
            return "ERROR! allocate called with null warrant in block \"" + getDisplayName() + "\"!";
        }
        if (_warrant != null && !warrant.equals(_warrant)) {
            // allocated to another warrant
            return Bundle.getMessage("AllocatedToWarrant", _warrant.getDisplayName(), getDisplayName());
        }
        int state = getState();
        if ((state & OUT_OF_SERVICE) != 0) {
            return Bundle.getMessage("BlockOutOfService", getDisplayName());
        }
        if (_pathName == null) {
            _pathName = warrant.getRoutePathInBlock(this);
        }
        _warrant = warrant;
        // firePropertyChange signaled in super.setState()
        setState(getState() | ALLOCATED);
        return null;
    }

    /**
     * Note path name may be set if block is not allocated to a warrant. For use
     * by Circuitbuilder Only.
     *
     * @return error message
     */
    public String allocate(String pathName) {
        if (log.isDebugEnabled()) {
            log.debug("Allocate OBlock path \"" + pathName + "\" in block \""
                    + getSystemName() + "\", state= " + getState());
        }
        if (pathName == null) {
            log.error("allocate called with null pathName in block \"" + getDisplayName() + "\"!");
            return null;
        } else if (_warrant != null) {
            // allocated to another warrant
            return Bundle.getMessage("AllocatedToWarrant", _warrant.getDisplayName(), getDisplayName());
        }
        if (_pathName != null && !_pathName.equals(pathName)) {
            return Bundle.getMessage("AllocatedToPath", pathName, getDisplayName(), _pathName);
        }
        _pathName = pathName;
//        setState(getState() | ALLOCATED);  DO NOT ALLOCATE
        return null;
    }

    /**
     * Remove allocation state Remove listener regardless of ownership
     */
    public String deAllocate(Warrant warrant) {
        //if (log.isDebugEnabled()) log.debug("deAllocate block \""+getDisplayName()+
        //				"\" from warrant \""+warrant.getDisplayName()+"\"");
        if (_warrant != null) {
            if (!_warrant.equals(warrant)) {
                // check if _warrant is registered
                if (jmri.InstanceManager.getDefault(WarrantManager.class).getBySystemName(_warrant.getSystemName()) != null) {
                    String msg = "cannot deAllocate. warrant \"" + _warrant.getDisplayName()
                            + "\" owns block \"" + getDisplayName() + "\"!";
                    log.error(msg);
                    return msg;
                }
                // warrant not found, fall through and clear
            }
            try {
                removePropertyChangeListener(_warrant);
            } catch (Exception ex) {
                // disposed warrant may throw null pointer - continue deallocation
                log.warn("Perhaps normal? Code not clear.", ex);
            }
        }
        if (_pathName != null) {
            OPath path = getPathByName(_pathName);
            if (path != null) {
                int lockState = Turnout.CABLOCKOUT & Turnout.PUSHBUTTONLOCKOUT;
                path.setTurnouts(0, false, lockState, false);
                Portal portal = path.getFromPortal();
                try {
                    if (portal != null) {
                        portal.setState(Portal.UNKNOWN);
                    }
                    portal = path.getToPortal();
                    if (portal != null) {
                        portal.setState(Portal.UNKNOWN);
                    }
                } catch (jmri.JmriException ex) {
                }
            }
        }
        _warrant = null;
        _pathName = null;
        _ownsTOs = false;
        setState(getState() & ~(ALLOCATED | RUNNING));  // unset allocated and running bits
        return null;
    }

    public void setOutOfService(boolean set) {
        if (set) {
            setState(getState() | OUT_OF_SERVICE);  // set OoS bit
        } else {
            setState(getState() & ~OUT_OF_SERVICE);  // unset OoS bit
        }
    }

    public void setError(boolean set) {
        if (set) {
            setState(getState() | TRACK_ERROR);  // set err bit
        } else {
            setState(getState() & ~TRACK_ERROR);  // unset err bit
        }
    }

    /**
     * Enforce unique portal names. Portals are not managed beans and multiple
     * configuration loads will create multiple Portal Objects that are
     * "duplicates".
     */
    public void addPortal(Portal portal) {
        String name = getDisplayName();
        if (!name.equals(portal.getFromBlockName()) && !name.equals(portal.getToBlockName())) {
            log.warn(portal.toString() + " not in block " + getDisplayName());
            return;
        }
        String pName = portal.getName();
        if (pName != null) {  // pName may be null if called from Portal ctor
            for (int i = 0; i < _portals.size(); i++) {
                if (pName.equals(_portals.get(i).getName())) {
                    return;
                }
            }
        }
        int oldSize = _portals.size();
        _portals.add(portal);
        if (log.isDebugEnabled()) {
            log.debug("addPortal: portal= \"" + portal.getName()
                    + "\", to Block= \"" + getDisplayName() + "\".");
        }
        firePropertyChange("portalCount", Integer.valueOf(oldSize), Integer.valueOf(_portals.size()));
    }

    /**
     * Remove portal from block and stub all paths using this portal to be dead
     * end spurs.
     */
    public void removePortal(Portal portal) {
        int oldSize = _portals.size();
        int oldPathSize = getPaths().size();
        if (portal != null) {
            //String name = portal.getName();
            Iterator<Path> iter = getPaths().iterator();
            while (iter.hasNext()) {
                OPath path = (OPath) iter.next();
                if (portal.equals(path.getFromPortal())) {
                    path.setFromPortal(null);
                    if (log.isDebugEnabled()) {
                        log.debug("removed Portal " + portal.getName() + " from Path "
                                + path.getName() + " in block " + getSystemName());
                    }
                }
                if (portal.equals(path.getToPortal())) {
                    path.setToPortal(null);
                    if (log.isDebugEnabled()) {
                        log.debug("removed Portal " + portal.getName() + " from Path "
                                + path.getName() + " in block " + getSystemName());
                    }
                }
                if (path.getFromPortal() == null && path.getToPortal() == null) {
                    removePath(path);
                    if (log.isDebugEnabled()) {
                        log.debug("removed Path "
                                + path.getName() + " in block " + getSystemName());
                    }
                }
            }
            //_portals.remove(portal);
            for (int i = 0; i < _portals.size(); i++) {
                if (portal.equals(_portals.get(i))) {
                    _portals.remove(i);
                    log.debug("removed portal " + portal.getName() + " from block " + getSystemName());
                    i--;
                }
            }
        }
        log.debug("removePortal: block " + getSystemName() + " portals decreased from "
                + oldSize + " to " + _portals.size() + " - paths decreased from "
                + oldPathSize + " to " + getPaths().size());
        firePropertyChange("portalCount", Integer.valueOf(oldSize), Integer.valueOf(_portals.size()));
    }

    public Portal getPortalByName(String name) {
        //if (log.isDebugEnabled()) log.debug("getPortalByName: name= \""+name+"\"." ); 
        for (int i = 0; i < _portals.size(); i++) {
            if (_portals.get(i).getName().equals(name)) {
                return _portals.get(i);
            }
        }
        return null;
    }

    public List<Portal> getPortals() {
        return _portals;
    }

    public OPath getPathByName(String name) {
        Iterator<Path> iter = getPaths().iterator();
        while (iter.hasNext()) {
            OPath path = (OPath) iter.next();
            if (path.getName().equals(name)) {
                return path;
            }
        }
        return null;
    }

    /**
     * Enforce unique path names within block, but allow a duplicate path to be
     * checked if it is also in one of the bloc's portals
     */
    public boolean addPath(OPath path) {
        String pName = path.getName();
        if (log.isDebugEnabled()) {
            log.debug("addPath \"" + pName + "\" to OBlock " + getSystemName());
        }
        List<Path> list = getPaths();
        for (int i = 0; i < list.size(); i++) {
            if (((OPath) list.get(i)).equals(path)) {
                log.warn("Path \"" + pName + "\" duplicated in OBlock " + getSystemName());
                return false;
            }
            if (pName.equals(((OPath) list.get(i)).getName())) {
                log.warn("Path named \"" + pName + "\" already exists in OBlock " + getSystemName());
                return false;
            }
        }
        path.setBlock(this);
        Portal portal = path.getFromPortal();
        if (portal != null) {
            if (!portal.addPath(path)) {
                log.warn("Path \"" + pName + "\" rejected by portal  " + portal.getName());
                return false;
            }
        }
        portal = path.getToPortal();
        if (portal != null) {
            if (!portal.addPath(path)) {
                log.warn("Path \"" + pName + "\" rejected by portal  " + portal.getName());
                return false;
            }
        }
        int oldSize = list.size();
        super.addPath(path);
        firePropertyChange("pathCount", Integer.valueOf(oldSize), Integer.valueOf(getPaths().size()));
        return true;
    }

    @Override
    public void removePath(Path path) {
        if (!getSystemName().equals(path.getBlock().getSystemName())) {
            return;
        }
//        if (log.isDebugEnabled()) log.debug("Path "+((OPath)path).getName()+" removed from "+getSystemName());
        path.clearSettings();
        int oldSize = getPaths().size();
        super.removePath(path);
        if (path instanceof OPath) {
            ((OPath) path).dispose();
        }
        firePropertyChange("pathCount", Integer.valueOf(oldSize), Integer.valueOf(getPaths().size()));
    }

    /**
     * Set Turnouts for the path Called by warrants to set turnouts for a train
     * it is able to run. The warrant parameter is verifies that the block is
     * indeed allocated to the warrant, If the block is unwarranted then the
     * block is allocated to the calling warrant. A logix conditional may also
     * call this method with a null warrant parameter for manual logix control.
     * However, if the block is under a warrant the call will be rejected.
     *
     * @param pathName name of the path
     * @param warrant  warrant the block is allocated to
     * @return error message if the call fails. null if the call succeeds
     */
    protected String setPath(String pathName, Warrant warrant) {
        if (log.isDebugEnabled()) {
            log.debug("setPath: OBlock \"" + getDisplayName() + "\", path  \""
                    + pathName + "\" for warrant " + warrant.getDisplayName());
        }
        String msg = null;
        if (_warrant != null && !_warrant.equals(warrant)) {
            msg = Bundle.getMessage("AllocatedToWarrant", _warrant.getDisplayName(), getDisplayName());
            return msg;
        }
        pathName = pathName.trim();
        OPath path = getPathByName(pathName);
        if (path == null) {
            msg = Bundle.getMessage("PathNotFound", pathName, getDisplayName());
        } else {
            // Sanity check
            String p = warrant.getRoutePathInBlock(this);
            if (!pathName.equals(p)) {
                log.error("path \"" + pathName + "\" for block \"" + getDisplayName()
                        + "\" does not agree with path \"" + p + "\" in route of warrant \""
                        + warrant.getDisplayName() + "\".");
            }
            _pathName = pathName;
            _warrant = warrant;
            setState(getState() | ALLOCATED);
            if (!_ownsTOs) {
                msg = checkSharedTO();		// does a callback to the warrant to set up a wait            	
            }
            if (msg == null) {
                int lockState = Turnout.CABLOCKOUT & Turnout.PUSHBUTTONLOCKOUT;
                path.setTurnouts(0, true, lockState, true);
                firePropertyChange("pathState", Integer.valueOf(0), Integer.valueOf(getState()));
            }
        }
        return msg;
    }

    /**
     * Call for Circuit Builder to make icon color changes for its GUI
     */
    public void pseudoPropertyChange(String propName, Object old, Object n) {
        if (log.isDebugEnabled()) {
            log.debug("pseudoPropertyChange: Block \"" + getSystemName() + " property \""
                    + propName + "\" new value= " + n.toString());
        }
        firePropertyChange(propName, old, n);
    }

    /**
     * (Override) Handles Block sensor going INACTIVE: this block is empty.
     * Called by handleSensorChange
     */
    @Override
    public void goingInactive() {
        if (log.isDebugEnabled()) {
            log.debug("Allocated OBlock \"" + getSystemName()
                    + "\" goes UNOCCUPIED. from state= " + getState());
        }
        setState((getState() & ~(OCCUPIED | RUNNING)) | UNOCCUPIED);
        setValue(null);
        if (_warrant != null) {
            ThreadingUtil.runOnLayout(()->{
                _warrant.goingInactive(this);
            });
        }
    }

    /**
     * (Override) Handles Block sensor going ACTIVE: this block is now occupied,
     * figure out from who and copy their value. Called by handleSensorChange
     */
    @Override
    public void goingActive() {
        setState((getState() & ~UNOCCUPIED) | OCCUPIED);
//        if (log.isDebugEnabled()) log.debug("Allocated OBlock \""+getSystemName()+
//                                            "\" goes OCCUPIED. state= "+getState());
        if (_warrant != null) {
            ThreadingUtil.runOnLayout(()->{
                _warrant.goingActive(this);
            });
        }
        if (log.isDebugEnabled()) {
            log.debug("Block \"" + getSystemName() + " went active, path= "
                    + _pathName + ", state= " + getState());
        }
    }

    @Override
    public void dispose() {
        List<Portal> list = getPortals();
        for (int i = 0; i < list.size(); i++) {
            Portal portal = list.get(i);
            OBlock opBlock = portal.getOpposingBlock(this);
            // remove portal and stub paths through portal in opposing block
            opBlock.removePortal(portal);
        }
        List<Path> pathList = getPaths();
        for (int i = 0; i < pathList.size(); i++) {
            removePath(pathList.get(i));
        }
        jmri.InstanceManager.getDefault(OBlockManager.class).deregister(this);
        super.dispose();
    }

    public String getDescription() {
        return java.text.MessageFormat.format(
                Bundle.getMessage("BlockDescription"), getDisplayName());
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameOBlock");
    }

    private final static Logger log = LoggerFactory.getLogger(OBlock.class.getName());
}
