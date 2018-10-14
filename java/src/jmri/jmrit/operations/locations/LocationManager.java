package jmri.jmrit.operations.locations;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InstanceManagerAutoInitialize;
import jmri.Reporter;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainCommon;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages locations.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2013, 2014
 */
public class LocationManager implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize, PropertyChangeListener {

    public static final String LISTLENGTH_CHANGED_PROPERTY = "locationsListLength"; // NOI18N

    public LocationManager() {
    }

    private int _id = 0;

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized LocationManager instance() {
        return InstanceManager.getDefault(LocationManager.class);
    }

    public void dispose() {
        _locationHashTable.clear();
        _id = 0;
    }

    protected Hashtable<String, Location> _locationHashTable = new Hashtable<String, Location>();

    /**
     * @return Number of locations
     */
    public int getNumberOfLocations() {
        return _locationHashTable.size();
    }

    /**
     * @param name The string name of the Location to get.
     * @return requested Location object or null if none exists
     */
    public Location getLocationByName(String name) {
        Location location;
        Enumeration<Location> en = _locationHashTable.elements();
        while (en.hasMoreElements()) {
            location = en.nextElement();
            if (location.getName().equals(name)) {
                return location;
            }
        }
        return null;
    }

    public Location getLocationById(String id) {
        return _locationHashTable.get(id);
    }

    /**
     * Request a location associated with a given reporter.
     *
     * @param r Reporter object associated with desired location.
     * @return requested Location object or null if none exists
     */
    public Location getLocationByReporter(Reporter r) {
        for (Location location : _locationHashTable.values()) {
            try {
                if (location.getReporter().equals(r)) {
                    return location;
                }
            } catch (java.lang.NullPointerException npe) {
                // it's valid for a reporter to be null (no reporter
                // at a given location.
            }
        }
        return null;
    }

    /**
     * Request a track associated with a given reporter.
     *
     * @param r Reporter object associated with desired location.
     * @return requested Location object or null if none exists
     */
    public Track getTrackByReporter(Reporter r) {
        for (Track track : getTracks(null)) {
            try {
                if (track.getReporter().equals(r)) {
                    return track;
                }
            } catch (java.lang.NullPointerException npe) {
                // it's valid for a reporter to be null (no reporter
                // at a given location.
            }
        }
        return null;
    }

    /**
     * Finds an existing location or creates a new location if needed requires
     * location's name creates a unique id for this location
     *
     * @param name The string name for a new Location.
     *
     *
     * @return new location or existing location
     */
    public Location newLocation(String name) {
        Location location = getLocationByName(name);
        if (location == null) {
            _id++;
            location = new Location(Integer.toString(_id), name);
            Integer oldSize = Integer.valueOf(_locationHashTable.size());
            _locationHashTable.put(location.getId(), location);
            resetNameLengths();
            setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
                    Integer.valueOf(_locationHashTable.size()));
        }
        return location;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     *
     * @param location The Location to add.
     */
    public void register(Location location) {
        Integer oldSize = Integer.valueOf(_locationHashTable.size());
        _locationHashTable.put(location.getId(), location);
        // find last id created
        int id = Integer.parseInt(location.getId());
        if (id > _id) {
            _id = id;
        }
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_locationHashTable.size()));
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     *
     * @param location The Location to delete.
     */
    public void deregister(Location location) {
        if (location == null) {
            return;
        }
        location.dispose();
        Integer oldSize = Integer.valueOf(_locationHashTable.size());
        _locationHashTable.remove(location.getId());
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_locationHashTable.size()));
    }

    /**
     * Sort by location name
     *
     * @return list of locations ordered by name
     */
    public List<Location> getLocationsByNameList() {
        // first get id list
        List<Location> sortList = getList();
        // now re-sort
        List<Location> out = new ArrayList<Location>();
        for (Location location : sortList) {
            for (int j = 0; j < out.size(); j++) {
                if (location.getName().compareToIgnoreCase(out.get(j).getName()) < 0) {
                    out.add(j, location);
                    break;
                }
            }
            if (!out.contains(location)) {
                out.add(location);
            }
        }
        return out;

    }

    /**
     * Sort by location number, number can alpha numeric
     *
     * @return list of locations ordered by id numbers
     */
    public List<Location> getLocationsByIdList() {
        List<Location> sortList = getList();
        // now re-sort
        List<Location> out = new ArrayList<Location>();
        for (Location location : sortList) {
            for (int j = 0; j < out.size(); j++) {
                try {
                    if (Integer.parseInt(location.getId()) < Integer.parseInt(out.get(j).getId())) {
                        out.add(j, location);
                        break;
                    }
                } catch (NumberFormatException e) {
                    log.debug("list id number isn't a number");
                }
            }
            if (!out.contains(location)) {
                out.add(location);
            }
        }
        return out;
    }

    /**
     * Gets an unsorted list of all locations.
     *
     * @return All locations.
     */
    public List<Location> getList() {
        List<Location> out = new ArrayList<Location>();
        Enumeration<Location> en = _locationHashTable.elements();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    /**
     * Returns all tracks of type
     *
     * @param type Spur (Track.SPUR), Yard (Track.YARD), Interchange
     *             (Track.INTERCHANGE), Staging (Track.STAGING), or null
     *             (returns all track types)
     * @return List of tracks
     */
    public List<Track> getTracks(String type) {
        List<Location> sortList = getList();
        List<Track> trackList = new ArrayList<Track>();
        for (Location location : sortList) {
            List<Track> tracks = location.getTrackByNameList(type);
            for (Track track : tracks) {
                trackList.add(track);
            }
        }
        return trackList;
    }

    /**
     * Returns all tracks of type sorted by use
     *
     * @param type Spur (Track.SPUR), Yard (Track.YARD), Interchange
     *             (Track.INTERCHANGE), Staging (Track.STAGING), or null
     *             (returns all track types)
     * @return List of tracks ordered by use
     */
    public List<Track> getTracksByMoves(String type) {
        List<Track> trackList = getTracks(type);
        // now re-sort
        List<Track> moveList = new ArrayList<Track>();
        for (Track track : trackList) {
            boolean locAdded = false;
            for (int j = 0; j < moveList.size(); j++) {
                if (track.getMoves() < moveList.get(j).getMoves()) {
                    moveList.add(j, track);
                    locAdded = true;
                    break;
                }
            }
            if (!locAdded) {
                moveList.add(track);
            }
        }
        return moveList;
    }

    public void resetMoves() {
        List<Location> locations = getList();
        for (Location loc : locations) {
            loc.resetMoves();
        }
    }

    /**
     *
     * @return locations for this railroad
     */
    public JComboBox<Location> getComboBox() {
        JComboBox<Location> box = new JComboBox<>();
        updateComboBox(box);
        return box;
    }

    public void updateComboBox(JComboBox<Location> box) {
        box.removeAllItems();
        box.addItem(null);
        for (Location loc : getLocationsByNameList()) {
            box.addItem(loc);
        }
    }

    public void replaceLoad(String type, String oldLoadName, String newLoadName) {
        List<Location> locs = getList();
        for (Location loc : locs) {
            // now adjust tracks
            List<Track> tracks = loc.getTrackList();
            for (Track track : tracks) {
                for (String loadName : track.getLoadNames()) {
                    if (loadName.equals(oldLoadName)) {
                        track.deleteLoadName(oldLoadName);
                        if (newLoadName != null) {
                            track.addLoadName(newLoadName);
                        }
                    }
                    // adjust combination car type and load name
                    String[] splitLoad = loadName.split(CarLoad.SPLIT_CHAR);
                    if (splitLoad.length > 1) {
                        if (splitLoad[0].equals(type) && splitLoad[1].equals(oldLoadName)) {
                            track.deleteLoadName(loadName);
                            if (newLoadName != null) {
                                track.addLoadName(type + CarLoad.SPLIT_CHAR + newLoadName);
                            }
                        }
                    }
                }
                // now adjust ship load names
                for (String loadName : track.getShipLoadNames()) {
                    if (loadName.equals(oldLoadName)) {
                        track.deleteShipLoadName(oldLoadName);
                        if (newLoadName != null) {
                            track.addShipLoadName(newLoadName);
                        }
                    }
                    // adjust combination car type and load name
                    String[] splitLoad = loadName.split(CarLoad.SPLIT_CHAR);
                    if (splitLoad.length > 1) {
                        if (splitLoad[0].equals(type) && splitLoad[1].equals(oldLoadName)) {
                            track.deleteShipLoadName(loadName);
                            if (newLoadName != null) {
                                track.addShipLoadName(type + CarLoad.SPLIT_CHAR + newLoadName);
                            }
                        }
                    }
                }
            }
        }
    }

    protected int _maxLocationNameLength = 0;
    protected int _maxTrackNameLength = 0;
    protected int _maxLocationAndTrackNameLength = 0;

    public void resetNameLengths() {
        _maxLocationNameLength = 0;
        _maxTrackNameLength = 0;
        _maxLocationAndTrackNameLength = 0;
    }

    public int getMaxLocationNameLength() {
        calculateMaxNameLengths();
        return _maxLocationNameLength;
    }

    public int getMaxTrackNameLength() {
        calculateMaxNameLengths();
        return _maxTrackNameLength;
    }

    public int getMaxLocationAndTrackNameLength() {
        calculateMaxNameLengths();
        return _maxLocationAndTrackNameLength;
    }

    private void calculateMaxNameLengths() {
        if (_maxLocationNameLength != 0) // only do this once
        {
            return;
        }
        String maxTrackName = "";
        String maxLocNameForTrack = "";
        String maxLocationName = "";
        String maxLocationAndTrackName = "";
        for (Track track : getTracks(null)) {
            if (TrainCommon.splitString(track.getName()).length() > _maxTrackNameLength) {
                maxTrackName = track.getName();
                maxLocNameForTrack = track.getLocation().getName();
                _maxTrackNameLength = TrainCommon.splitString(track.getName()).length();
            }
            if (TrainCommon.splitString(track.getLocation().getName()).length() > _maxLocationNameLength) {
                maxLocationName = track.getLocation().getName();
                _maxLocationNameLength = TrainCommon.splitString(track.getLocation().getName()).length();
            }
            if (TrainCommon.splitString(track.getLocation().getName()).length()
                    + TrainCommon.splitString(track.getName()).length() > _maxLocationAndTrackNameLength) {
                maxLocationAndTrackName = track.getLocation().getName() + ", " + track.getName();
                _maxLocationAndTrackNameLength = TrainCommon.splitString(track.getLocation().getName()).length()
                        + TrainCommon.splitString(track.getName()).length();
            }
        }
        log.info("Max track name ({}) at ({}) length {}", maxTrackName, maxLocNameForTrack, _maxTrackNameLength);
        log.info("Max location name ({}) length {}", maxLocationName, _maxLocationNameLength);
        log.info("Max location and track name ({}) length {}", maxLocationAndTrackName, _maxLocationAndTrackNameLength);
    }

    public void load(Element root) {
        if (root.getChild(Xml.LOCATIONS) != null) {
            List<Element> locs = root.getChild(Xml.LOCATIONS).getChildren(Xml.LOCATION);
            log.debug("readFile sees {} locations", locs.size());
            for (Element loc : locs) {
                register(new Location(loc));
            }
        }
    }

    public void store(Element root) {
        Element values;
        root.addContent(values = new Element(Xml.LOCATIONS));
        // add entries
        List<Location> locationList = getLocationsByIdList();
        for (Location loc : locationList) {
            values.addContent(loc.store());
        }
    }

    /**
     * There aren't any current property changes being monitored.
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("LocationManager sees property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e
                .getOldValue(), e.getNewValue()); // NOI18N
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // set dirty
        InstanceManager.getDefault(LocationManagerXml.class).setDirty(true);
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(LocationManager.class);

    @Override
    public void initialize() {
        InstanceManager.getDefault(OperationsSetupXml.class); // load setup
        InstanceManager.getDefault(LocationManagerXml.class); // load locations
    }
}
