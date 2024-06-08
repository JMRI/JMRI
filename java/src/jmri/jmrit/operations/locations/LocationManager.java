package jmri.jmrit.operations.locations;

import java.beans.PropertyChangeListener;
import java.util.*;

import javax.swing.JComboBox;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.*;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.operations.OperationsPanel;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.setup.OperationsSetupXml;

/**
 * Manages locations.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2013, 2014
 */
public class LocationManager extends PropertyChangeSupport implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize, PropertyChangeListener {

    public static final String LISTLENGTH_CHANGED_PROPERTY = "locationsListLength"; // NOI18N
    
    protected boolean _showId = false; // when true show location ids 

    public LocationManager() {
    }

    private int _id = 0;

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
     * Used to determine if a division name has been assigned to a location
     * @return true if a location has a division name
     */
    public boolean hasDivisions() {
        for (Location location : getList()) {
            if (location.getDivision() != null) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasWork() {
        for (Location location : getList()) {
            if (location.hasWork()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Used to determine if a reporter has been assigned to a location
     * @return true if a location has a RFID reporter
     */
    public boolean hasReporters() {
        for (Location location : getList()) {
            if (location.getReporter() != null) {
                return true;
            }
        }
        return false;
    }
    
    public void setShowIdEnabled(boolean showId) {
        _showId = showId;
    }
    
    public boolean isShowIdEnabled() {
        return _showId;
    }

    /**
     * Request a location associated with a given reporter.
     *
     * @param r Reporter object associated with desired location.
     * @return requested Location object or null if none exists
     */
    public Location getLocationByReporter(Reporter r) {
        for (Location location : _locationHashTable.values()) {
            if (location.getReporter() != null) {
                if (location.getReporter().equals(r)) {
                    return location;
                }
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
            if (track.getReporter() != null) {
                if (track.getReporter().equals(r)) {
                    return track;
                }
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
     * Get unique locations list by location name.
     *
     * @return list of locations ordered by name. Locations with "similar" names
     *         to the primary location are not returned. Also checks and updates
     *         the primary location for any changes to the other "similar"
     *         locations.
     */
    public List<Location> getUniqueLocationsByNameList() {
        List<Location> locations = getLocationsByNameList();
        List<Location> out = new ArrayList<Location>();
        Location mainLocation = null;
        
        // also update the primary location for locations with similar names
        for (Location location : locations) {
            String name = location.getSplitName();
            if (mainLocation != null && mainLocation.getSplitName().equals(name)) {
                location.setSwitchListEnabled(mainLocation.isSwitchListEnabled());
                if (mainLocation.isSwitchListEnabled() && location.getStatus().equals(Location.MODIFIED)) {
                    mainLocation.setStatus(Location.MODIFIED); // we need to update the primary location
                    location.setStatus(Location.UPDATED); // and clear the secondaries
                }
                continue;
            }
            mainLocation = location;
            out.add(location);
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
            List<Track> tracks = location.getTracksByNameList(type);
            for (Track track : tracks) {
                trackList.add(track);
            }
        }
        return trackList;
    }

    /**
     * Returns all tracks of type sorted by use. Alternate tracks
     * are not included.
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
            if (track.isAlternate()) {
                continue;
            }
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

    /**
     * Sets move count to 0 for all tracks
     */
    public void resetMoves() {
        List<Location> locations = getList();
        for (Location loc : locations) {
            loc.resetMoves();
        }
    }

    /**
     * Returns a JComboBox with locations sorted alphabetically.
     * @return locations for this railroad
     */
    public JComboBox<Location> getComboBox() {
        JComboBox<Location> box = new JComboBox<>();
        updateComboBox(box);
        OperationsPanel.padComboBox(box, getMaxLocationNameLength());
        return box;
    }

    /**
     * Updates JComboBox alphabetically with a list of locations.
     * @param box The JComboBox to update.
     */
    public void updateComboBox(JComboBox<Location> box) {
        box.removeAllItems();
        box.addItem(null);
        for (Location loc : getLocationsByNameList()) {
            box.addItem(loc);
        }
    }

    /**
     * Replace all track car load names for a given type of car
     * 
     * @param type type of car
     * @param oldLoadName load name to replace
     * @param newLoadName new load name
     */
    public void replaceLoad(String type, String oldLoadName, String newLoadName) {
        List<Location> locs = getList();
        for (Location loc : locs) {
            // now adjust tracks
            List<Track> tracks = loc.getTracksList();
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

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SLF4J_FORMAT_SHOULD_BE_CONST",
            justification = "I18N of Info Message")
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
            if (track.getSplitName().length() > _maxTrackNameLength) {
                maxTrackName = track.getName();
                maxLocNameForTrack = track.getLocation().getName();
                _maxTrackNameLength = track.getSplitName().length();
            }
            if (track.getLocation().getSplitName().length() > _maxLocationNameLength) {
                maxLocationName = track.getLocation().getName();
                _maxLocationNameLength = track.getLocation().getSplitName().length();
            }
            if (track.getLocation().getSplitName().length()
                    + track.getSplitName().length() > _maxLocationAndTrackNameLength) {
                maxLocationAndTrackName = track.getLocation().getName() + ", " + track.getName();
                _maxLocationAndTrackNameLength = track.getLocation().getSplitName().length()
                        + track.getSplitName().length();
            }
        }
        log.info(Bundle.getMessage("InfoMaxTrackName", maxTrackName, _maxTrackNameLength, maxLocNameForTrack));
        log.info(Bundle.getMessage("InfoMaxLocationName", maxLocationName, _maxLocationNameLength));
        log.info(Bundle.getMessage("InfoMaxLocAndTrackName", maxLocationAndTrackName, _maxLocationAndTrackNameLength));
    }

    /**
     * Load the locations from a xml file.
     * @param root xml file
     */
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

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // set dirty
        InstanceManager.getDefault(LocationManagerXml.class).setDirty(true);
        firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(LocationManager.class);

    @Override
    public void initialize() {
        InstanceManager.getDefault(OperationsSetupXml.class); // load setup
        InstanceManager.getDefault(LocationManagerXml.class); // load locations
    }
}
