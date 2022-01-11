package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanUsageReport;
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
 * <p>
 * Additional states are defined to indicate status of the track and trains to
 * control panels. A jmri.Block has a PropertyChangeListener on the occupancy
 * sensor and the OBlock will pass state changes of the occ.sensor on to its
 * Warrant.
 * <p>
 * Entrances (exits when train moves in opposite direction) to OBlocks have
 * Portals. A Portal object is a pair of OBlocks. Each OBlock has a list of its
 * Portals.
 * <p>
 * When an OBlock (Detection Circuit) has a Portal whose entrance to the OBlock
 * has a signal, then the OBlock and its chains of adjacent OBlocks up to the
 * next OBlock having an entrance Portal with a signal, can be considered a
 * "Block" in the sense of a prototypical railroad. Preferably all entrances to
 * the "Block" should have entrance Portals with a signal.
 * <p>
 * A Portal has a list of paths (OPath objects) for each OBlock it separates.
 * The paths are determined by the turnout settings of the turnouts contained in
 * the block. Paths are contained within the Block boundaries. Names of OPath
 * objects only need be unique within an OBlock.
 *
 * @author Pete Cressman (C) 2009
 * @author Egbert Broerse (C) 2020
 */
public class OBlock extends jmri.Block implements java.beans.PropertyChangeListener {

    public enum OBlockStatus {
        Unoccupied(UNOCCUPIED, "unoccupied", Bundle.getMessage("unoccupied")),
        Occupied(OCCUPIED, "occupied", Bundle.getMessage("occupied")),
        Allocated(ALLOCATED, "allocated", Bundle.getMessage("allocated")),
        Running(RUNNING, "running", Bundle.getMessage("running")),
        OutOfService(OUT_OF_SERVICE, "outOfService", Bundle.getMessage("outOfService")),
        Dark(UNDETECTED, "dark", Bundle.getMessage("dark")),
        TrackError(TRACK_ERROR, "powerError", Bundle.getMessage("powerError"));
        
        private final int status;
        private final String name;
        private final String descr;
        
        private static final Map<String, OBlockStatus> map = new HashMap<>();
        private static final Map<String, OBlockStatus> reverseMap = new HashMap<>();
        
        private OBlockStatus(int status, String name, String descr) {
            this.status = status;
            this.name = name;
            this.descr = descr;
        }
        
        public int getStatus() { return status; }
        
        public String getName() { return name; }
        
        public String getDescr() { return descr; }
        
        public static OBlockStatus getByName(String name) { return map.get(name); }
        public static OBlockStatus getByDescr(String descr) { return reverseMap.get(descr); }
        
        static {
            for (OBlockStatus oblockStatus : OBlockStatus.values()) {
                map.put(oblockStatus.name, oblockStatus);
                reverseMap.put(oblockStatus.descr, oblockStatus);
            }
        }
    }
    
    /*
     * OBlock states:
     * NamedBean.UNKNOWN                 = 0x01
     * Block.OCCUPIED =  Sensor.ACTIVE   = 0x02
     * Block.UNOCCUPIED = Sensor.INACTIVE= 0x04
     * NamedBean.INCONSISTENT            = 0x08
     * Add the following to the 4 sensor states.
     * States are OR'ed to show combination.  e.g. ALLOCATED | OCCUPIED = allocated block is occupied
     */
    public static final int ALLOCATED = 0x10;      // reserve the block for subsequent use by a train
    public static final int RUNNING = 0x20;        // OBlock that running train has reached
    public static final int OUT_OF_SERVICE = 0x40; // OBlock that should not be used
    public static final int TRACK_ERROR = 0x80;    // OBlock has Error
    // UNDETECTED state bit is used for DARK blocks
    // static final public int DARK = 0x01;        // meaning: OBlock has no Sensor, same as UNKNOWN

    private static final Color DEFAULT_FILL_COLOR = new Color(200, 0, 200);

    public static String getLocalStatusName(String str) {
        return OBlockStatus.getByName(str).descr;
    }

    public static String getSystemStatusName(String str) {
        return OBlockStatus.getByDescr(str).name;
    }
    private List<Portal> _portals = new ArrayList<>();     // portals to this block

    private Warrant _warrant;        // when not null, oblock is allocated to this warrant
    private String _pathName;        // when not null, this is the allocated path
    protected long _entryTime;       // time when block became occupied
    private boolean _metric = false; // desired display mode
    private NamedBeanHandle<Sensor> _errNamedSensor;
    private Color _markerForeground = Color.WHITE;
    private Color _markerBackground = DEFAULT_FILL_COLOR;
    private Font _markerFont;

    public OBlock(@Nonnull String systemName) {
        super(systemName);
        setState(UNDETECTED);
    }

    public OBlock(@Nonnull String systemName, String userName) {
        super(systemName, userName);
        setState(UNDETECTED);
    }

    /* What super does currently is fine.
     * FindBug wants us to duplicate and override anyway
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!((OBlock) obj).getSystemName().equals(this.getSystemName())) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.getSystemName().hashCode();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Override to only set an existing sensor and to amend state with not
     * UNDETECTED return true if an existing Sensor is set or sensor is to be
     * removed from block.
     */
    @Override
    public boolean setSensor(String pName) {
        Sensor oldSensor = getSensor();
        Sensor newSensor = null;
        if (pName != null && pName.trim().length() > 0) {
            newSensor = InstanceManager.sensorManagerInstance().getByUserName(pName);
            if (newSensor == null) {
                newSensor = InstanceManager.sensorManagerInstance().getBySystemName(pName);
            }
            if (newSensor == null) {
                log.error("No sensor named '{}' exists.", pName);
                return false;
            }
        }
        if (oldSensor != null) {
            if (oldSensor.equals(newSensor)) {
                return true;
            }
        }

        // save the non-sensor states
        int saveState = getState() & ~(UNKNOWN | OCCUPIED | UNOCCUPIED | INCONSISTENT | UNDETECTED);
        if (newSensor == null || pName == null) {
            setNamedSensor(null);                    
        } else {
            setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, newSensor));
        }
        setState(getState() | saveState);   // add them back into new sensor
        firePropertyChange("OccupancySensorChange", oldSensor, newSensor);
        return true;
    }

    // override to determine if not UNDETECTED
    @Override
    public void setNamedSensor(NamedBeanHandle<Sensor> namedSensor) {
        super.setNamedSensor(namedSensor);
        if (namedSensor != null) {
            setState(getSensor().getState() & ~UNDETECTED);
        }
    }

    /**
     * @param pName name of error sensor
     * @return true if successful
     */
    public boolean setErrorSensor(String pName) {
        NamedBeanHandle<Sensor> newErrSensorHdl = null;
        Sensor newErrSensor = null;
        if (pName != null && pName.trim().length() > 0) {
            newErrSensor = InstanceManager.sensorManagerInstance().getByUserName(pName);
            if (newErrSensor == null) {
                newErrSensor = InstanceManager.sensorManagerInstance().getBySystemName(pName);
            }
           if (newErrSensor != null) {
                newErrSensorHdl = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, newErrSensor);
           }
           if (newErrSensor == null) {
               log.error("No sensor named '{}' exists.", pName);
               return false;
           }
        }
        if (_errNamedSensor != null) {
            if (_errNamedSensor.equals(newErrSensorHdl)) {
                return true;
            } else {
                getErrorSensor().removePropertyChangeListener(this);
            }
        }

        _errNamedSensor = newErrSensorHdl;
        setState(getState() & ~TRACK_ERROR);
        if (newErrSensor  != null) {
            newErrSensor.addPropertyChangeListener(this, _errNamedSensor.getName(), "OBlock Error Sensor " + getDisplayName());
            if (newErrSensor.getState() == Sensor.ACTIVE) {
                setState(getState() | TRACK_ERROR);
            } else {
                setState(getState() & ~TRACK_ERROR);
            }
        }
        return true;
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

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) {
            log.debug("property change: of \"{}\" property {} is now {} from {}",
                    getDisplayName(), evt.getPropertyName(), evt.getNewValue(), evt.getSource().getClass().getName());
        }
        if ((getErrorSensor() != null) && (evt.getSource().equals(getErrorSensor()))) {
            if (evt.getPropertyName().equals("KnownState")) {
                int errState = ((Integer) evt.getNewValue());
                int oldState = getState();
                if (errState == Sensor.ACTIVE) {
                    setState(oldState | TRACK_ERROR);
                } else {
                    setState(oldState & ~TRACK_ERROR);
                }
                firePropertyChange("pathState", oldState, getState());
            }
        }
    }

    /**
     * Another block sharing a turnout with this block queries whether turnout
     * is in use.
     *
     * @param path that uses a common shared turnout
     * @return If warrant exists and path==pathname, return warrant display
     *         name, else null.
     */
    public String isPathSet(String path) {
        String msg = null;
        if (_warrant != null) {
            if (path.equals(_pathName)) {
                msg = _warrant.getDisplayName();
            }
        }
        log.trace("Path \"{}\" in oblock \"{}\" {}", path, getDisplayName(), (msg == null ? "not set" : " set in warrant " + msg));
        return msg;
    }

    public Warrant getWarrant() {
        return _warrant;
    }

    /*
     * Does a deAllocation, but keeps the listener
     */
    protected boolean releaseWarrant(Warrant w) {
        if (w != null && w.equals(_warrant)) {
            if (_pathName != null) {
                OPath path = getPathByName(_pathName);
                if (path != null) {
                    int lockState = Turnout.CABLOCKOUT & Turnout.PUSHBUTTONLOCKOUT;
                    path.setTurnouts(0, false, lockState, false);
                    Portal portal = path.getFromPortal();
                    if (portal != null) {
                        portal.setState(Portal.UNKNOWN);
                    }
                    portal = path.getToPortal();
                    if (portal != null) {
                        portal.setState(Portal.UNKNOWN);
                    }
                }
                _pathName = null;
            }
            _warrant = null;
            setState(getState() & ~(ALLOCATED | RUNNING));  // unset allocated and running bits
            return true;
        }
        return false;
    }

    public boolean isAllocatedTo(Warrant warrant) {
        if (warrant == null) {
            return false;
        }
        return warrant.equals(_warrant);
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
     * Update the Oblock status.
     * Override Block because change must come from an OBlock for Web Server to receive it
     *
     * @param v the new state, from OBlock.ALLOCATED etc, named 'status' in JSON Servlet and Web Server
     */
    @Override
    public void setState(int v) {
        int old = getState();
        super.setState(v);
        // override Block to get proper source to be recognized by listener in Web Server
        //log.debug("OBLOCK.JAVA {} setState({})", getSystemName(), getState()); // used by CPE indicator track icons
        firePropertyChange("state", old, getState());
    }

    /**
     * {@inheritDoc}
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
        OBlockStatus oblockStatus = OBlockStatus.getByName(statusName);
        if (oblockStatus != null) {
            return ((getState() & oblockStatus.status) != 0);
        }
        log.error("\"{}\" type not found.  Update Conditional State Variable testing OBlock \"{}\" status",
                getDisplayName(), statusName);
        return false;
    }

    public boolean isDark() {
        return (getState() & OBlock.UNDETECTED) != 0;
    }

    public boolean isOccupied() {
        return (getState() & OBlock.OCCUPIED) != 0;
    }

    public String occupiedBy() {
        Warrant w = _warrant;
        if (isOccupied()) {
            if (w != null) {
                return w.getTrainName();
            } else {
                return Bundle.getMessage("unknownTrain");
            }
        } else {
            return null;
        }
    }

    /**
     * Test that block is not occupied and not allocated
     *
     * @return true if not occupied and not allocated
     */
    public boolean isFree() {
        int state = getState();
        return ((state & ALLOCATED) == 0 && (state & OCCUPIED) == 0);
    }

    /**
     * Allocate (reserves) the block for the Warrant Note the block may be
     * OCCUPIED by a non-warranted train, but the allocation is permitted.
     *
     * @param warrant the Warrant
     * @return message with if block is already allocated to another warrant or
     *         block is OUT_OF_SERVICE
     */
    public String allocate(Warrant warrant) {
        if (warrant == null) {
            return "ERROR! allocate called with null warrant in block \"" + getDisplayName() + "\"!";
        }
        String msg = null;
        if (_warrant != null) {
            if (!warrant.equals(_warrant)) {
                msg = Bundle.getMessage("AllocatedToWarrant",
                        _warrant.getDisplayName(), getDisplayName(), _warrant.getTrainName());
            } else {
                return null;
            }
        }
        if (msg == null) {
            int state = getState();
            if ((state & OUT_OF_SERVICE) != 0) {
                msg = Bundle.getMessage("BlockOutOfService", getDisplayName());
            }
        }
        if (msg == null) {
            if (_pathName == null) {
                _pathName = warrant.getRoutePathInBlock(this);
            }
            _warrant = warrant;
            // firePropertyChange signaled in super.setState()
            setState(getState() | ALLOCATED);
            log.debug("Allocate oblock \"{}\" to warrant \"{}\".", getDisplayName(), warrant.getDisplayName());
        } else {
            log.debug("Allocate oblock \"{}\" failed for warrant {}. err= {}",
                    getDisplayName(), warrant.getDisplayName(), msg);
        }
        return msg;
    }

    /**
     * Note path name may be set if block is not allocated to a warrant. For use
     * by CircuitBuilder Only.
     *
     * @param pathName name of a path
     * @return error message, otherwise null
     */
    public String allocatePath(String pathName) {
        log.debug("Allocate OBlock path \"{}\" in block \"{}\", state= {}",
                pathName, getSystemName(), getState());
        if (pathName == null) {
            log.error("allocate called with null pathName in block \"{}\"!", getDisplayName());
            return null;
        } else if (_warrant != null) {
            // allocated to another warrant
            return Bundle.getMessage("AllocatedToWarrant",
                    _warrant.getDisplayName(), getDisplayName(), _warrant.getTrainName());
        }
        if ((_pathName != null) && !_pathName.equals(pathName)) {
            return Bundle.getMessage("AllocatedToPath", pathName, getDisplayName(), _pathName);
        }
        _pathName = pathName;
        // setState(getState() | ALLOCATED);  DO NOT ALLOCATE
        return null;
    }

    public String getAllocatingWarrantName() {
        if (_warrant == null) {
            return ("no warrant");
        } else {
            return _warrant.getDisplayName();
        }
    }

    /**
     * Remove allocation state // maybe restore this? Remove listener regardless of ownership
     *
     * @param warrant warrant that has reserved this block. null is allowed for
     *                Conditionals and CircuitBuilder to reset the block.
     *                Otherwise, null should not be used.
     * @return true if warrant deallocated.
     */
    public boolean deAllocate(Warrant warrant) {
        if (_warrant != null) {
            if (!_warrant.equals(warrant)) {
                // check if _warrant is registered
                if (jmri.InstanceManager.getDefault(WarrantManager.class).getBySystemName(_warrant.getSystemName()) != null) {
                    StringBuilder sb = new StringBuilder("Warrant \"");
                    sb.append(warrant.getDisplayName());
                    sb.append("\" Cannot deallocate Block \"");
                    sb.append(getDisplayName());
                    sb.append("\"! Owned by warrant \"");
                    sb.append(_warrant.getDisplayName());
                    sb.append("\".");
                    log.warn(sb.toString());
                    return false;
                }
            }
            try {
                log.debug("deAllocate block \"{}\" from warrant \"{}\"",
                        getDisplayName(), warrant.getDisplayName());
                removePropertyChangeListener(_warrant);
            } catch (Exception ex) {
                // disposed warrant may throw null pointer - continue deallocation
                log.debug("Warrant {} unregistered.", _warrant.getDisplayName(), ex);
                }
        }
        if (warrant == null) {
            return false;
        }
        return releaseWarrant(warrant);
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
     * Enforce unique portal names. Portals are now managed beans since 2014.
     * This enforces unique names.
     *
     * @param portal the Portal to add
     */
    public void addPortal(Portal portal) {
        String name = getDisplayName();
        if (!name.equals(portal.getFromBlockName()) && !name.equals(portal.getToBlockName())) {
            log.warn("{} not in block {}", portal.getDescription(), getDisplayName());
            return;
        }
        String pName = portal.getName();
        if (pName != null) {  // pName may be null if called from Portal ctor
            for (Portal value : _portals) {
                if (pName.equals(value.getName())) {
                    return;
                }
            }
        }
        int oldSize = _portals.size();
        _portals.add(portal);
        log.debug("add portal \"{}\" to Block \"{}\"", portal.getName(), getDisplayName());
        firePropertyChange("portalCount", oldSize, _portals.size());
    }

    /*
     * Remove portal from oblock and stub all paths using this portal to be dead
     * end spurs.
     *
     * @param portal the Portal to remove
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "OPath extends Path")
    protected void removePortal(Portal portal) {
        if (portal != null) {
            Iterator<Path> iter = getPaths().iterator();
            while (iter.hasNext()) {
                OPath path = (OPath) iter.next();
                if (portal.equals(path.getFromPortal())) {
                    path.setFromPortal(null);
                    log.debug("removed Portal {} from Path \"{}\" in oblock {}",
                            portal.getName(), path.getName(), getDisplayName());
                }
                if (portal.equals(path.getToPortal())) {
                    path.setToPortal(null);
                    log.debug("removed Portal {} from Path \"{}\" in oblock {}",
                            portal.getName(), path.getName(), getDisplayName());
                }
            }
            iter = getPaths().iterator();
            while (iter.hasNext()) {
                OPath path = (OPath) iter.next();
                if (path.getFromPortal() == null && path.getToPortal() == null) {
                    removeOPath(path);
                    log.debug("removed Path \"{}\" from oblock {}", path.getName(), getDisplayName());
                }
            }
            int oldSize = _portals.size();
            _portals = _portals.stream().filter(p -> !Objects.equals(p,portal)).collect(Collectors.toList());
            firePropertyChange("portalCount", oldSize, _portals.size());
        }
    }

    public Portal getPortalByName(String name) {
//        log.debug("getPortalByName: name= \"{}\".", name);
        for (Portal po : _portals) {
            if (po.getName().equals(name)) {
                return po;
            }
        }
        return null;
    }

    @Nonnull
    public List<Portal> getPortals() {
        return new ArrayList<>(_portals);
    }

    public void setPortals(ArrayList<Portal> portals) {
        _portals = portals;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "OPath extends Path")
    public OPath getPathByName(String name) {
        for (Path opa : getPaths()) {
            OPath path = (OPath) opa;
            if (path.getName().equals(name)) {
                return path;
            }
        }
        return null;
    }

    @Override
    public void setLength(float len) {
        float oldLen = getLengthMm();
        if (oldLen > 0.0f) {   // if new oblock, paths also have length 0
            float ratio = getLengthMm() / oldLen;
            getPaths().forEach(path -> path.setLength(path.getLength() * ratio));
        }
        super.setLength(len);
    }

    /**
     * Enforce unique path names within OBlock, but allow a duplicate name of an
     * OPath from another OBlock to be checked if it is in one of the OBlock's
     * Portals.
     *
     * @param path the OPath to add
     * @return true if path was added to OBlock
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "OPath extends Path")
    public boolean addPath(OPath path) {
        String pName = path.getName();
        log.debug("addPath \"{}\" to OBlock {}", pName, getSystemName());
        List<Path> list = getPaths();
        for (Path p : list) {
            if (((OPath) p).equals(path)) {
                log.debug("Path \"{}\" duplicated in OBlock {}", pName, getSystemName());
                return false;
            }
            if (pName.equals(((OPath) p).getName())) {
                log.debug("Path named \"{}\" already exists in OBlock {}", pName, getSystemName());
                return false;
            }
        }
        OBlock pathBlock = (OBlock) path.getBlock();
        if (pathBlock != null && !this.equals(pathBlock)) {
            log.warn("Path \"{}\" already in block {}, cannot be added to block {}",
                    pName, pathBlock.getDisplayName(), getDisplayName());
            return false;
        }
        path.setBlock(this);
        Portal portal = path.getFromPortal();
        if (portal != null) {
            if (!portal.addPath(path)) {
                log.debug("Path \"{}\" rejected by portal  {}", pName, portal.getName());
                return false;
            }
        }
        portal = path.getToPortal();
        if (portal != null) {
            if (!portal.addPath(path)) {
                log.debug("Path \"{}\" rejected by portal  {}", pName, portal.getName());
                return false;
            }
        }
        super.addPath(path);
        firePropertyChange("pathCount", null, getPaths().size());
        return true;
    }

    public boolean removeOPath(OPath path) {
        jmri.Block block = path.getBlock();
        if (block != null && !getSystemName().equals(block.getSystemName())) {
            return false;
        }
        if (!InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class).okToRemoveBlockPath(this, path)) {
            return false;
        }
        path.clearSettings();
        super.removePath(path);
        // remove path from its portals
        Portal portal = path.getToPortal();
        if (portal != null) {
            portal.removePath(path);
        }
        portal = path.getFromPortal();
        if (portal != null) {
            portal.removePath(path);
        }
        path.dispose();
        firePropertyChange("pathCount", path, getPaths().size());
        return true;
    }

    /**
     * Set Turnouts for the path.
     * <p>
     * Called by warrants to set turnouts for a train it is able to run.
     * The warrant parameter verifies that the block is
     * indeed allocated to the warrant. If the block is unwarranted then the
     * block is allocated to the calling warrant. A logix conditional may also
     * call this method with a null warrant parameter for manual logix control.
     * If the block is under a different warrant the call will be rejected.
     *
     * @param pathName name of the path
     * @param warrant  warrant the block is allocated to
     * @return error message if the call fails. null if the call succeeds
     */
    protected String setPath(String pathName, Warrant warrant) {
        if (_warrant != null && !_warrant.equals(warrant)) {
            return Bundle.getMessage("AllocatedToWarrant",
                    _warrant.getDisplayName(), getDisplayName(), _warrant.getTrainName());
        }
        if (pathName == null) {
            return Bundle.getMessage("PathNotFound", pathName, getDisplayName());
        }
        String msg = allocate(warrant);
        if (msg != null) {
            return msg;
        }
        pathName = pathName.trim();
        OPath path = getPathByName(pathName);
        if (path == null) {
            return Bundle.getMessage("PathNotFound", pathName, getDisplayName());
        }
        _pathName = pathName;
        int lockState = Turnout.CABLOCKOUT & Turnout.PUSHBUTTONLOCKOUT;
        path.setTurnouts(0, true, lockState, true);
        firePropertyChange("pathState", 0, getState());
        log.debug("setPath: Path \"{}\" in OBlock \"{}\" {} set for warrant {}",
                    pathName, getDisplayName(), warrant.getDisplayName());
        return null;
    }

    /*
     * Call for Circuit Builder to make icon color changes for its GUI
     */
    public void pseudoPropertyChange(String propName, Object old, Object n) {
        log.debug("pseudoPropertyChange: Block \"{}\" property \"{}\" new value= {}",
                getSystemName(), propName, n);
        firePropertyChange(propName, old, n);
    }

    /**
     * (Override) Handles Block sensor going INACTIVE: this block is empty.
     * Called by handleSensorChange
     */
    @Override
    public void goingInactive() {
        log.debug("OBlock \"{} going UNOCCUPIED from state= {}", getDisplayName(), getState());
        // preserve the non-sensor states
        // non-UNOCCUPIED sensor states are removed (also cannot be RUNNING there if being UNOCCUPIED)
        setState((getState() & ~(UNKNOWN | OCCUPIED | INCONSISTENT | RUNNING)) | UNOCCUPIED);
        setValue(null);
        if (_warrant != null) {
            ThreadingUtil.runOnLayout(() -> _warrant.goingInactive(this));
        }
    }

    /**
     * (Override) Handles Block sensor going ACTIVE: this block is now occupied,
     * figure out from who and copy their value. Called by handleSensorChange
     */
    @Override
    public void goingActive() {
        log.debug("OBlock \"{}\" going OCCUPIED with path \"{}\" from state= {}",
                getDisplayName(), _pathName, getState());
        // preserve the non-sensor states when being OCCUPIED and remove non-OCCUPIED sensor states
        setState((getState() & ~(UNKNOWN | UNOCCUPIED | INCONSISTENT)) | OCCUPIED);
        if (_warrant != null) {
            ThreadingUtil.runOnLayout(() -> _warrant.goingActive(this));
        }
    }

    @Override
    public void goingUnknown() {
        log.debug("OBlock \"{} going UNKNOWN from state= {}", getDisplayName(), getState());
        setState((getState() & ~(UNOCCUPIED | OCCUPIED | INCONSISTENT)) | UNKNOWN);
    }

    @Override
    public void goingInconsistent() {
        log.debug("OBlock \"{} going INCONSISTENT from state= {}", getDisplayName(), getState());
        setState((getState() & ~(UNKNOWN | UNOCCUPIED | OCCUPIED)) | INCONSISTENT);
    }

    @Override
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "OPath extends Path")
    public void dispose() {
        if (!InstanceManager.getDefault(WarrantManager.class).okToRemoveBlock(this)) {
            return;
        }
        firePropertyChange("deleted", null, null);
        // remove paths first
        for (Path pa : getPaths()) {
            removeOPath((OPath)pa);
        }
        for (Portal portal : getPortals()) {
            if (log.isDebugEnabled()) {
                log.debug("this = {}, toBlock = {}, fromblock= {}", getDisplayName(),
                        portal.getToBlock().getDisplayName(), portal.getFromBlock().getDisplayName());
            }
            if (this.equals(portal.getToBlock())) {
                portal.setToBlock(null, false);
            }
            if (this.equals(portal.getFromBlock())) {
                portal.setFromBlock(null, false);
            }
        }
        _portals.clear();
        for (PropertyChangeListener listener : getPropertyChangeListeners()) {
            removePropertyChangeListener(listener);
        }
        jmri.InstanceManager.getDefault(OBlockManager.class).deregister(this);
        super.dispose();
    }

    public String getDescription() {
        return java.text.MessageFormat.format(
                Bundle.getMessage("BlockDescription"), getDisplayName());
    }

    @Override
    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        List<NamedBeanUsageReport> report = new ArrayList<>();
        List<NamedBean> duplicateCheck = new ArrayList<>();
        if (bean != null) {
            if (log.isDebugEnabled()) {
                Sensor s = getSensor();
                log.debug("oblock: {}, sensor = {}", getDisplayName(), (s==null?"Dark OBlock":s.getDisplayName()));  // NOI18N
            }
            if (bean.equals(getSensor())) {
                report.add(new NamedBeanUsageReport("OBlockSensor"));  // NOI18N
            }
            if (bean.equals(getErrorSensor())) {
                report.add(new NamedBeanUsageReport("OBlockSensorError"));  // NOI18N
            }
            if (bean.equals(getWarrant())) {
                report.add(new NamedBeanUsageReport("OBlockWarant"));  // NOI18N
            }

            getPortals().forEach((portal) -> {
                log.debug("    portal: {}, fb = {}, tb = {}, fs = {}, ts = {}",  // NOI18N
                        portal.getName(), portal.getFromBlockName(), portal.getToBlockName(),
                        portal.getFromSignalName(), portal.getToSignalName());
                if (bean.equals(portal.getFromBlock()) || bean.equals(portal.getToBlock())) {
                    report.add(new NamedBeanUsageReport("OBlockPortalNeighborOBlock", portal.getName()));  // NOI18N
                }
                if (bean.equals(portal.getFromSignal()) || bean.equals(portal.getToSignal())) {
                    report.add(new NamedBeanUsageReport("OBlockPortalSignal", portal.getName()));  // NOI18N
                }

                portal.getFromPaths().forEach((path) -> {
                    log.debug("        from path = {}", path.getName());  // NOI18N
                    path.getSettings().forEach((setting) -> {
                        log.debug("            turnout = {}", setting.getBean().getDisplayName());  // NOI18N
                        if (bean.equals(setting.getBean())) {
                            if (!duplicateCheck.contains(bean)) {
                                report.add(new NamedBeanUsageReport("OBlockPortalPathTurnout", portal.getName()));  // NOI18N
                                duplicateCheck.add(bean);
                            }
                        }
                    });
                });
                portal.getToPaths().forEach((path) -> {
                    log.debug("        to path   = {}", path.getName());  // NOI18N
                    path.getSettings().forEach((setting) -> {
                        log.debug("            turnout = {}", setting.getBean().getDisplayName());  // NOI18N
                        if (bean.equals(setting.getBean())) {
                            if (!duplicateCheck.contains(bean)) {
                                report.add(new NamedBeanUsageReport("OBlockPortalPathTurnout", portal.getName()));  // NOI18N
                                duplicateCheck.add(bean);
                            }
                        }
                    });
                });
            });
        }
        return report;
    }

    @Override
    @Nonnull
    public String getBeanType() {
        return Bundle.getMessage("BeanNameOBlock");
    }

    private static final Logger log = LoggerFactory.getLogger(OBlock.class);

}
