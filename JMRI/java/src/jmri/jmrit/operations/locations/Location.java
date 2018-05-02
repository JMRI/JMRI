package jmri.jmrit.operations.locations;

import java.awt.Point;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.PhysicalLocation;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a location on the layout
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2012, 2013
 */
public class Location implements java.beans.PropertyChangeListener {

    public static final String NONE = "";
    public static final int RANGE_DEFAULT = 25;

    protected String _id = NONE;
    protected String _name = NONE;
    protected int _IdNumber = 0;
    protected int _numberRS = 0; // number of cars and engines (total rolling stock)
    protected int _numberCars = 0; // number of cars
    protected int _numberEngines = 0; // number of engines
    protected int _pickupRS = 0;
    protected int _dropRS = 0;
    protected int _locationOps = NORMAL; // type of operations at this location
    protected int _trainDir = EAST + WEST + NORTH + SOUTH; // train direction served by this location
    protected int _length = 0; // length of all tracks at this location
    protected int _usedLength = 0; // length of track filled by cars and engines
    protected String _comment = NONE;
    protected String _switchListComment = NONE; // optional switch list comment
    protected boolean _switchList = true; // when true print switchlist for this location
    protected String _defaultPrinter = NONE; // the default printer name when printing a switchlist
    protected String _status = UNKNOWN; // print switch list status
    protected int _switchListState = SW_CREATE; // switch list state, saved between sessions
    protected Point _trainIconEast = new Point(); // coordinates of east bound train icons
    protected Point _trainIconWest = new Point();
    protected Point _trainIconNorth = new Point();
    protected Point _trainIconSouth = new Point();
    protected int _trainIconRangeX = RANGE_DEFAULT; // the x & y detection range for the train icon
    protected int _trainIconRangeY = RANGE_DEFAULT;
    protected Hashtable<String, Track> _trackHashTable = new Hashtable<>();
    protected PhysicalLocation _physicalLocation = new PhysicalLocation();
    protected List<String> _listTypes = new ArrayList<>();

    // IdTag reader associated with this location.
    protected Reporter _reader = null;

    // Pool
    protected int _idPoolNumber = 0;
    protected Hashtable<String, Pool> _poolHashTable = new Hashtable<>();

    public static final int NORMAL = 1; // types of track allowed at this location
    public static final int STAGING = 2; // staging only

    public static final int EAST = 1; // train direction serviced by this location
    public static final int WEST = 2;
    public static final int NORTH = 4;
    public static final int SOUTH = 8;

    // Switch list status
    public static final String UNKNOWN = "";
    public static final String PRINTED = Bundle.getMessage("Printed");
    public static final String CSV_GENERATED = Bundle.getMessage("CsvGenerated");
    public static final String MODIFIED = Bundle.getMessage("Modified");
    public static final String UPDATED = Bundle.getMessage("Updated");

    // Switch list states
    public static final int SW_CREATE = 0; // create new switch list
    public static final int SW_APPEND = 1; // append train into to switch list
    public static final int SW_PRINTED = 2; // switch list printed

    // For property change
    public static final String TRACK_LISTLENGTH_CHANGED_PROPERTY = "trackListLength"; // NOI18N
    public static final String TYPES_CHANGED_PROPERTY = "locationTypes"; // NOI18N
    public static final String TRAINDIRECTION_CHANGED_PROPERTY = "locationTrainDirection"; // NOI18N
    public static final String LENGTH_CHANGED_PROPERTY = "locationTrackLengths"; // NOI18N
    public static final String USEDLENGTH_CHANGED_PROPERTY = "locationUsedLength"; // NOI18N
    public static final String NAME_CHANGED_PROPERTY = "locationName"; // NOI18N
    public static final String SWITCHLIST_CHANGED_PROPERTY = "switchList"; // NOI18N
    public static final String DISPOSE_CHANGED_PROPERTY = "locationDispose"; // NOI18N
    public static final String STATUS_CHANGED_PROPERTY = "locationStatus"; // NOI18N
    public static final String POOL_LENGTH_CHANGED_PROPERTY = "poolLengthChanged"; // NOI18N
    public static final String SWITCHLIST_COMMENT_CHANGED_PROPERTY = "switchListComment";// NOI18N
    public static final String TRACK_BLOCKING_ORDER_CHANGED_PROPERTY = "locationTrackBlockingOrder";// NOI18N

    public Location(String id, String name) {
        log.debug("New location ({}) id: {}", name, id);
        _name = name;
        _id = id;
        // a new location accepts all types
        setTypeNames(InstanceManager.getDefault(CarTypes.class).getNames());
        setTypeNames(InstanceManager.getDefault(EngineTypes.class).getNames());
        addPropertyChangeListeners();
    }

    public String getId() {
        return _id;
    }

    /**
     * Sets the location's name.
     * @param name The string name for this location.
     *
     */
    public void setName(String name) {
        String old = _name;
        _name = name;
        if (!old.equals(name)) {
            InstanceManager.getDefault(LocationManager.class).resetNameLengths(); // recalculate max location name length for manifests
            setDirtyAndFirePropertyChange(NAME_CHANGED_PROPERTY, old, name);
        }
    }

    // for combo boxes
    @Override
    public String toString() {
        return _name;
    }

    public String getName() {
        return _name;
    }

    /**
     * Makes a copy of this location.
     *
     * @param newLocation the location to copy to
     */
    public void copyLocation(Location newLocation) {
        newLocation.setComment(getComment());
        newLocation.setDefaultPrinterName(getDefaultPrinterName());
        newLocation.setLocationOps(getLocationOps());
        newLocation.setSwitchListComment(getSwitchListComment());
        newLocation.setSwitchListEnabled(isSwitchListEnabled());
        newLocation.setTrainDirections(getTrainDirections());
        // TODO should we set the train icon coordinates?
        // rolling stock serviced by this location
        for (String type : newLocation.getTypeNames()) {
            if (acceptsTypeName(type)) {
                continue;
            } else {
                newLocation.deleteTypeName(type);
            }
        }
        copyTracksLocation(newLocation);

    }

    /**
     * Copies all of the tracks at this location. If there's a track already at
     * the copy to location with the same name, the track is skipped.
     *
     * @param location the location to copy the tracks to.
     */
    public void copyTracksLocation(Location location) {
        for (Track track : getTrackList()) {
            if (location.getTrackByName(track.getName(), null) != null) {
                continue;
            }
            track.copyTrack(track.getName(), location);
        }
    }

    public PhysicalLocation getPhysicalLocation() {
        return (_physicalLocation);
    }

    public void setPhysicalLocation(PhysicalLocation l) {
        _physicalLocation = l;
        InstanceManager.getDefault(LocationManagerXml.class).setDirty(true);
    }

    /**
     * Set total length of all tracks for this location
     * @param length The integer sum of all tracks at this location.
     *
     */
    public void setLength(int length) {
        int old = _length;
        _length = length;
        if (old != length) {
            setDirtyAndFirePropertyChange(LENGTH_CHANGED_PROPERTY, Integer.toString(old), Integer
                    .toString(length));
        }
    }

    /**
     *
     * @return total length of all tracks for this location
     */
    public int getLength() {
        return _length;
    }

    public void setUsedLength(int length) {
        int old = _usedLength;
        _usedLength = length;
        if (old != length) {
            setDirtyAndFirePropertyChange(USEDLENGTH_CHANGED_PROPERTY, Integer.toString(old), Integer
                    .toString(length));
        }
    }

    /**
     *
     * @return The length of the track that is occupied by cars and engines
     */
    public int getUsedLength() {
        return _usedLength;
    }

    /**
     * Set the operations mode for this location
     *
     * @param ops NORMAL STAGING
     */
    public void setLocationOps(int ops) {
        int old = _locationOps;
        _locationOps = ops;
        if (old != ops) {
            setDirtyAndFirePropertyChange("locationOps", Integer.toString(old), Integer.toString(ops)); // NOI18N
        }
    }

    /**
     * Gets the operations mode for this location
     *
     * @return NORMAL STAGING
     */
    public int getLocationOps() {
        return _locationOps;
    }

    /**
     * Used to determine if location is setup for staging
     *
     * @return true if location is setup as staging
     */
    public boolean isStaging() {
        return getLocationOps() == STAGING;
    }

    /**
     *
     * @return True if location has spurs
     */
    public boolean hasSpurs() {
        return hasTrackType(Track.SPUR);
    }

    /**
     *
     * @return True if location has classification/interchange tracks
     */
    public boolean hasInterchanges() {
        return hasTrackType(Track.INTERCHANGE);
    }

    /**
     *
     * @return True if location has yard tracks
     */
    public boolean hasYards() {
        return hasTrackType(Track.YARD);
    }

    /**
     *
     * @param trackType The track type to check.
     * @return True if location has the track type specified Track.INTERCHANGE
     *         Track.YARD Track.SPUR Track.Staging
     */
    public boolean hasTrackType(String trackType) {
        Track track;
        Enumeration<Track> en = _trackHashTable.elements();
        while (en.hasMoreElements()) {
            track = en.nextElement();
            if (track.getTrackType().equals(trackType)) {
                return true;
            }
        }
        return false;
    }

    public int getNumberOfTracks() {
        return _trackHashTable.size();
    }

    /**
     * Sets the train directions that this location can service. EAST means that
     * an Eastbound train can service the location.
     *
     * @param direction Any combination of EAST WEST NORTH SOUTH
     */
    public void setTrainDirections(int direction) {
        int old = _trainDir;
        _trainDir = direction;
        if (old != direction) {
            setDirtyAndFirePropertyChange(TRAINDIRECTION_CHANGED_PROPERTY, Integer.toString(old), Integer
                    .toString(direction));
        }
    }

    /**
     * Gets the train directions that this location can service. EAST means that
     * an Eastbound train can service the location.
     *
     * @return Any combination of EAST WEST NORTH SOUTH
     */
    public int getTrainDirections() {
        return _trainDir;
    }

    /**
     * Sets the quantity of rolling stock for this location
     * @param number An integer representing the quantity of rolling stock at this location.
     *
     */
    public void setNumberRS(int number) {
        int old = _numberRS;
        _numberRS = number;
        if (old != number) {
            setDirtyAndFirePropertyChange("locationNumberRS", Integer.toString(old), Integer.toString(number)); // NOI18N
        }
    }

    /**
     * Gets the number of cars and engines at this location
     *
     * @return number of cars at this location
     */
    public int getNumberRS() {
        return _numberRS;
    }

    /**
     * Sets the number of cars at this location
     *
     */
    private void setNumberCars(int number) {
        int old = _numberCars;
        _numberCars = number;
        if (old != number) {
            setDirtyAndFirePropertyChange("locationNumberCars", Integer.toString(old), // NOI18N
                    Integer.toString(number)); // NOI18N
        }
    }

    /**
     *
     * @return The number of cars at this location
     */
    public int getNumberCars() {
        return _numberCars;
    }

    /**
     * Sets the number of engines at this location
     *
     */
    private void setNumberEngines(int number) {
        int old = _numberEngines;
        _numberEngines = number;
        if (old != number) {
            setDirtyAndFirePropertyChange("locationNumberEngines", Integer.toString(old), // NOI18N
                    Integer.toString(number)); // NOI18N
        }
    }

    /**
     *
     * @return The number of engines at this location
     */
    public int getNumberEngines() {
        return _numberEngines;
    }

    /**
     * When true, a switchlist is desired for this location. Used for preview
     * and printing a manifest for a single location
     * @param switchList When true, switch lists are enabled for this location.
     *
     */
    public void setSwitchListEnabled(boolean switchList) {
        boolean old = _switchList;
        _switchList = switchList;
        if (old != switchList) {
            setDirtyAndFirePropertyChange(SWITCHLIST_CHANGED_PROPERTY, old ? "true" : "false", // NOI18N
                    switchList ? "true" : "false"); // NOI18N
        }
    }

    /**
     * Used to determine if switch list is needed for this location
     *
     * @return true if switch list needed
     */
    public boolean isSwitchListEnabled() {
        return _switchList;
    }

    public void setDefaultPrinterName(String name) {
        String old = _defaultPrinter;
        _defaultPrinter = name;
        if (!old.equals(name)) {
            setDirtyAndFirePropertyChange("locationDefaultPrinter", old, name); // NOI18N
        }
    }

    public String getDefaultPrinterName() {
        return _defaultPrinter;
    }

    /**
     * Sets the print status for this location's switch list
     *
     * @param status UNKNOWN PRINTED MODIFIED UPDATED CSV_GENERATED
     */
    public void setStatus(String status) {
        String old = _status;
        _status = status;
        if (!old.equals(status)) {
            setDirtyAndFirePropertyChange(STATUS_CHANGED_PROPERTY, old, status);
        }
    }

    /**
     * The print status for this location's switch list
     * 
     * @return UNKNOWN PRINTED MODIFIED UPDATED CSV_GENERATED
     */
    public String getStatus() {
        return _status;
    }

    /**
     *
     * @param state Location.SW_CREATE Location.SW_PRINTED Location.SW_APPEND
     */
    public void setSwitchListState(int state) {
        int old = _switchListState;
        _switchListState = state;
        if (old != state) {
            setDirtyAndFirePropertyChange("locationSwitchListState", old, state); // NOI18N
        }
    }

    /**
     * Returns the state of the switch list for this location.
     *
     * @return Location.SW_CREATE, Location.SW_PRINTED or Location.SW_APPEND
     */
    public int getSwitchListState() {
        return _switchListState;
    }

    /**
     * Sets the train icon coordinates for an eastbound train arriving at this
     * location.
     *
     * @param point The XY coordinates on the panel.
     */
    public void setTrainIconEast(Point point) {
        Point old = _trainIconEast;
        _trainIconEast = point;
        setDirtyAndFirePropertyChange("locationTrainIconEast", old.toString(), point.toString()); // NOI18N
    }

    public Point getTrainIconEast() {
        return _trainIconEast;
    }

    public void setTrainIconWest(Point point) {
        Point old = _trainIconWest;
        _trainIconWest = point;
        setDirtyAndFirePropertyChange("locationTrainIconWest", old.toString(), point.toString()); // NOI18N
    }

    public Point getTrainIconWest() {
        return _trainIconWest;
    }

    public void setTrainIconNorth(Point point) {
        Point old = _trainIconNorth;
        _trainIconNorth = point;
        setDirtyAndFirePropertyChange("locationTrainIconNorth", old.toString(), point.toString()); // NOI18N
    }

    public Point getTrainIconNorth() {
        return _trainIconNorth;
    }

    public void setTrainIconSouth(Point point) {
        Point old = _trainIconSouth;
        _trainIconSouth = point;
        setDirtyAndFirePropertyChange("locationTrainIconSouth", old.toString(), point.toString()); // NOI18N
    }

    public Point getTrainIconSouth() {
        return _trainIconSouth;
    }
    
    /**
     * Sets the X range for detecting the manual movement of a train icon.
     * @param x the +/- range for detection
     */
    public void setTrainIconRangeX(int x) {
        int old = _trainIconRangeX;
        _trainIconRangeX = x;
        if (old != x) {
            setDirtyAndFirePropertyChange("trainIconRangeX", Integer.toString(old), Integer.toString(x)); // NOI18N
        }
    }

    public int getTrainIconRangeX() {
        return _trainIconRangeX;
    }

    /**
     * Sets the Y range for detecting the manual movement of a train icon.
     * @param y the +/- range for detection
     */
    public void setTrainIconRangeY(int y) {
        int old = _trainIconRangeY;
        _trainIconRangeY = y;
        if (old != y) {
            setDirtyAndFirePropertyChange("trainIconRangeY", Integer.toString(old), Integer.toString(y)); // NOI18N
        }
    }

    public int getTrainIconRangeY() {
        return _trainIconRangeY;
    }

    /**
     * Adds rolling stock to a specific location.
     * @param rs The RollingStock to add.
     *
     */
    public void addRS(RollingStock rs) {
        setNumberRS(getNumberRS() + 1);
        if (rs.getClass() == Car.class) {
            setNumberCars(getNumberCars() + 1);
        } else if (rs.getClass() == Engine.class) {
            setNumberEngines(getNumberEngines() + 1);
        }
        setUsedLength(getUsedLength() + rs.getTotalLength());
    }

    public void deleteRS(RollingStock rs) {
        setNumberRS(getNumberRS() - 1);
        if (rs.getClass() == Car.class) {
            setNumberCars(getNumberCars() - 1);
        } else if (rs.getClass() == Engine.class) {
            setNumberEngines(getNumberEngines() - 1);
        }
        setUsedLength(getUsedLength() - rs.getTotalLength());
    }

    /**
     * Increments the number of cars and or engines that will be picked up by a
     * train at this location.
     */
    public void addPickupRS() {
        int old = _pickupRS;
        _pickupRS++;
        setDirtyAndFirePropertyChange("locationAddPickupRS", Integer.toString(old), Integer.toString(_pickupRS)); // NOI18N
    }

    /**
     * Decrements the number of cars and or engines that will be picked up by a
     * train at this location.
     */
    public void deletePickupRS() {
        int old = _pickupRS;
        _pickupRS--;
        setDirtyAndFirePropertyChange("locationDeletePickupRS", Integer.toString(old), Integer.toString(_pickupRS)); // NOI18N
    }

    /**
     * Increments the number of cars and or engines that will be dropped off by
     * trains at this location.
     */
    public void addDropRS() {
        int old = _dropRS;
        _dropRS++;
        setDirtyAndFirePropertyChange("locationAddDropRS", Integer.toString(old), Integer.toString(_dropRS)); // NOI18N
    }

    /**
     * Decrements the number of cars and or engines that will be dropped off by
     * trains at this location.
     */
    public void deleteDropRS() {
        int old = _dropRS;
        _dropRS--;
        setDirtyAndFirePropertyChange("locationDeleteDropRS", Integer.toString(old), Integer.toString(_dropRS)); // NOI18N
    }

    /**
     *
     * @return the number of cars and engines that are scheduled for pick up at
     *         this location.
     */
    public int getPickupRS() {
        return _pickupRS;
    }

    /**
     *
     * @return the number of cars and engines that are scheduled for drop at
     *         this location.
     */
    public int getDropRS() {
        return _dropRS;
    }

    public void setComment(String comment) {
        String old = _comment;
        _comment = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange("locationComment", old, comment); // NOI18N
        }
    }

    public String getComment() {
        return _comment;
    }

    public void setSwitchListComment(String comment) {
        String old = _switchListComment;
        _switchListComment = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange(SWITCHLIST_COMMENT_CHANGED_PROPERTY, old, comment);
        }
    }

    public String getSwitchListComment() {
        return _switchListComment;
    }

    private String[] getTypeNames() {
        String[] types = new String[_listTypes.size()];
        for (int i = 0; i < _listTypes.size(); i++) {
            types[i] = _listTypes.get(i);
        }
        return types;
    }

    private void setTypeNames(String[] types) {
        if (types.length == 0) {
            return;
        }
        java.util.Arrays.sort(types);
        for (String type : types) {
            _listTypes.add(type);
        }
    }

    /**
     * Adds the specific type of rolling stock to the will service list
     *
     * @param type of rolling stock that location will service
     */
    public void addTypeName(String type) {
        // insert at start of list, sort later
        if (type == null || _listTypes.contains(type)) {
            return;
        }
        _listTypes.add(0, type);
        log.debug("Location ({}) add rolling stock type ({})", getName(), type);
        setDirtyAndFirePropertyChange(TYPES_CHANGED_PROPERTY, _listTypes.size() - 1, _listTypes.size());
    }

    public void deleteTypeName(String type) {
        if (!_listTypes.contains(type)) {
            return;
        }
        _listTypes.remove(type);
        log.debug("Location ({}) delete rolling stock type ({})", getName(), type);
        setDirtyAndFirePropertyChange(TYPES_CHANGED_PROPERTY, _listTypes.size() + 1, _listTypes.size());
    }

    public boolean acceptsTypeName(String type) {
        return _listTypes.contains(type);
    }

    /**
     * Adds a track to this location. Valid track types are spurs, yards,
     * staging and interchange tracks.
     *
     * @param name of track
     * @param type of track, Track.INTERCHANGE, Track.SPUR, Track.STAGING,
     *            Track.YARD
     * @return Track
     */
    public Track addTrack(String name, String type) {
        Track track = getTrackByName(name, type);
        if (track == null) {
            _IdNumber++;
            String id = _id + "s" + Integer.toString(_IdNumber);
            log.debug("Adding new ({}) to ({}) track name ({}) id: {}", type, getName(), name, id);
            track = new Track(id, name, type, this);
            InstanceManager.getDefault(LocationManager.class).resetNameLengths(); // recalculate max track name length for manifests
            register(track);
        }
        resetMoves(); // give all of the tracks equal weighting
        return track;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     * @param track The Track to be loaded at this location.
     */
    public void register(Track track) {
        Integer old = Integer.valueOf(_trackHashTable.size());
        _trackHashTable.put(track.getId(), track);
        // add to the locations's available track length
        setLength(getLength() + track.getLength());
        // find last id created
        String[] getId = track.getId().split("s");
        int id = Integer.parseInt(getId[1]);
        if (id > _IdNumber) {
            _IdNumber = id;
        }
        setDirtyAndFirePropertyChange(TRACK_LISTLENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_trackHashTable
                .size()));
        // listen for name and state changes to forward
        track.addPropertyChangeListener(this);
    }

    public void deleteTrack(Track track) {
        if (track != null) {
            track.removePropertyChangeListener(this);
            // subtract from the locations's available track length
            setLength(getLength() - track.getLength());
            track.dispose();
            Integer old = Integer.valueOf(_trackHashTable.size());
            _trackHashTable.remove(track.getId());
            setDirtyAndFirePropertyChange(TRACK_LISTLENGTH_CHANGED_PROPERTY, old, Integer
                    .valueOf(_trackHashTable.size()));
        }
    }

    /**
     * Get track at this location by name and type. Track type can be null.
     *
     * @param name track's name
     * @param type track type
     * @return track at location
     */
    public Track getTrackByName(String name, String type) {
        Track track;
        Enumeration<Track> en = _trackHashTable.elements();
        while (en.hasMoreElements()) {
            track = en.nextElement();
            if (type == null) {
                if (track.getName().equals(name)) {
                    return track;
                }
            } else if (track.getName().equals(name) && track.getTrackType().equals(type)) {
                return track;
            }
        }
        return null;
    }

    public Track getTrackById(String id) {
        return _trackHashTable.get(id);
    }

    /**
     * Gets a list of track ids ordered by id for this location.
     *
     * @return list of track ids for this location
     */
    public List<String> getTrackIdsByIdList() {
        String[] arr = new String[_trackHashTable.size()];
        List<String> out = new ArrayList<>();
        Enumeration<String> en = _trackHashTable.keys();
        int i = 0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        for (i = 0; i < arr.length; i++) {
            out.add(arr[i]);
        }
        return out;
    }

    /**
     * Gets a sorted by id list of tracks for this location.
     *
     * @return Sorted list of tracks by id for this location.
     */
    public List<Track> getTrackByIdList() {
        List<Track> out = new ArrayList<>();
        List<String> trackIds = getTrackIdsByIdList();
        for (String id : trackIds) {
            out.add(getTrackById(id));
        }
        return out;
    }

    /**
     * Gets a unsorted list of the tracks at this location.
     *
     * @return tracks at this location.
     */
    public List<Track> getTrackList() {
        List<Track> out = new ArrayList<>();
        Enumeration<Track> en = _trackHashTable.elements();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    /**
     * Sorted list by track name. Returns a list of tracks of a given track
     * type. If type is null returns all tracks for the location.
     *
     * @param type track type: Track.YARD, Track.SPUR, Track.INTERCHANGE,
     *            Track.STAGING
     * @return list of tracks ordered by name for this location
     */
    public List<Track> getTrackByNameList(String type) {

        List<Track> out = new ArrayList<>();

        for (Track track : getTrackByIdList()) {
            boolean locAdded = false;
            for (int j = 0; j < out.size(); j++) {
                if (track.getName().compareToIgnoreCase(out.get(j).getName()) < 0 &&
                        (type != null && track.getTrackType().equals(type) || type == null)) {
                    out.add(j, track);
                    locAdded = true;
                    break;
                }
            }
            if (!locAdded && (type != null && track.getTrackType().equals(type) || type == null)) {
                out.add(track);
            }
        }
        return out;
    }

    /**
     * Sorted list by track moves. Returns a list of a given track type. If type
     * is null, all tracks for the location are returned. Tracks with schedules
     * are placed at the start of the list. Tracks that are alternates are
     * removed.
     *
     * @param type track type: Track.YARD, Track.SPUR, Track.INTERCHANGE,
     *            Track.STAGING
     * @return list of tracks at this location ordered by moves
     */
    public List<Track> getTrackByMovesList(String type) {

        List<Track> moveList = new ArrayList<>();

        for (Track track : getTrackByIdList()) {
            boolean locAdded = false;
            for (int j = 0; j < moveList.size(); j++) {
                if (track.getMoves() < moveList.get(j).getMoves() &&
                        (type != null && track.getTrackType().equals(type) || type == null)) {
                    moveList.add(j, track);
                    locAdded = true;
                    break;
                }
            }
            if (!locAdded && (type != null && track.getTrackType().equals(type) || type == null)) {
                moveList.add(track);
            }
        }
        // bias tracks with schedules to the start of the list
        // remove any alternate tracks from the list
        List<Track> out = new ArrayList<>();
        for (int i = 0; i < moveList.size(); i++) {
            Track track = moveList.get(i);
            if (!track.getScheduleId().equals(NONE)) {
                out.add(track);
                moveList.remove(i--);
            } else if (track.isAlternate()) {
                moveList.remove(i--);
            }
        }
        for (Track track : moveList) {
            out.add(track);
        }
        return out;
    }

    /**
     * Sorted list by track blocking order. Returns a list of a given track
     * type. If type is null, all tracks for the location are returned.
     *
     * @param type track type: Track.YARD, Track.SPUR, Track.INTERCHANGE,
     *            Track.STAGING
     * @return list of tracks at this location ordered by blocking order
     */
    public List<Track> getTracksByBlockingOrderList(String type) {
        List<Track> orderList = new ArrayList<>();
        for (Track track : getTrackByNameList(type)) {
            boolean trackAdded = false;
            for (int j = 0; j < orderList.size(); j++) {
                if (track.getBlockingOrder() < orderList.get(j).getBlockingOrder()) {
                    orderList.add(j, track);
                    trackAdded = true;
                    break;
                }
            }
            if (!trackAdded) {
                orderList.add(track);
            }
        }
        return orderList;
    }

    public void resetTracksByBlockingOrder() {
        for (Track track : getTrackList()) {
            track.setBlockingOrder(0);
        }
        setDirtyAndFirePropertyChange(TRACK_BLOCKING_ORDER_CHANGED_PROPERTY, true, false);
    }

    public void resequnceTracksByBlockingOrder() {
        int order = 1;
        for (Track track : getTracksByBlockingOrderList(null)) {
            track.setBlockingOrder(order++);
        }
        setDirtyAndFirePropertyChange(TRACK_BLOCKING_ORDER_CHANGED_PROPERTY, true, false);
    }

    public void changeTrackBlockingOrderEarlier(Track track) {
        // if track blocking order is 0, then the blocking table has never been initialized
        if (track.getBlockingOrder() != 0) {
            //first adjust the track being replaced
            Track repalceTrack = getTrackByBlockingOrder(track.getBlockingOrder() - 1);
            if (repalceTrack != null) {
                repalceTrack.setBlockingOrder(track.getBlockingOrder());
            }
            track.setBlockingOrder(track.getBlockingOrder() - 1);
            // move the end of order
            if (track.getBlockingOrder() <= 0)
                track.setBlockingOrder(_trackHashTable.size() + 1);
        }
        resequnceTracksByBlockingOrder();
    }

    public void changeTrackBlockingOrderLater(Track track) {
        // if track blocking order is 0, then the blocking table has never been initialized
        if (track.getBlockingOrder() != 0) {
            //first adjust the track being replaced
            Track repalceTrack = getTrackByBlockingOrder(track.getBlockingOrder() + 1);
            if (repalceTrack != null) {
                repalceTrack.setBlockingOrder(track.getBlockingOrder());
            }
            track.setBlockingOrder(track.getBlockingOrder() + 1);
            // move the start of order
            if (track.getBlockingOrder() > _trackHashTable.size())
                track.setBlockingOrder(0);
        }
        resequnceTracksByBlockingOrder();
    }

    private Track getTrackByBlockingOrder(int order) {
        for (Track track : getTrackList()) {
            if (track.getBlockingOrder() == order)
                return track;
        }
        return null; // not found!
    }

    public boolean isTrackAtLocation(Track track) {
        if (track == null) {
            return true;
        }
        return _trackHashTable.contains(track);
    }

    /**
     * Reset the move count for all tracks at this location
     */
    public void resetMoves() {
        List<Track> tracks = getTrackList();
        for (Track track : tracks) {
            track.setMoves(0);
        }
    }

    /**
     * Updates a JComboBox with all of the track locations for this location.
     *
     * @param box JComboBox to be updated.
     */
    public void updateComboBox(JComboBox<Track> box) {
        box.removeAllItems();
        box.addItem(null);
        List<Track> tracks = getTrackByNameList(null);
        for (Track track : tracks) {
            box.addItem(track);
        }
    }

    /**
     * Updates a JComboBox with tracks that can service the rolling stock.
     *
     * @param box JComboBox to be updated.
     * @param rs Rolling Stock to be serviced
     * @param filter When true, remove tracks not able to service rs.
     * @param destination When true, the tracks are destinations for the rs.
     */
    public void updateComboBox(JComboBox<Track> box, RollingStock rs, boolean filter, boolean destination) {
        updateComboBox(box);
        if (!filter || rs == null) {
            return;
        }
        List<Track> tracks = getTrackByNameList(null);
        for (Track track : tracks) {
            String status = "";
            if (destination) {
                status = rs.testDestination(this, track);
            } else {
                status = rs.testLocation(this, track);
            }
            if (status.equals(Track.OKAY) && (!destination || !track.getTrackType().equals(Track.STAGING))) {
                box.setSelectedItem(track);
                log.debug("Available track: {} for location: {}", track.getName(), getName());
            } else {
                box.removeItem(track);
            }
        }
    }

    /**
     * Adds a track pool for this location. A track pool is a set of tracks
     * where the length of the tracks is shared between all of them.
     *
     * @param name the name of the Pool to create
     * @return Pool
     */
    public Pool addPool(String name) {
        Pool pool = getPoolByName(name);
        if (pool == null) {
            _idPoolNumber++;
            String id = _id + "p" + Integer.toString(_idPoolNumber);
            log.debug("creating new pool ({}) id: {}", name, id);
            pool = new Pool(id, name);
            register(pool);
        }
        return pool;
    }

    public void removePool(Pool pool) {
        if (pool != null) {
            _poolHashTable.remove(pool.getId());
            setDirtyAndFirePropertyChange(POOL_LENGTH_CHANGED_PROPERTY, Integer
                    .valueOf(_poolHashTable.size() + 1), Integer.valueOf(_poolHashTable.size()));
        }
    }

    public Pool getPoolByName(String name) {
        Pool pool;
        Enumeration<Pool> en = _poolHashTable.elements();
        while (en.hasMoreElements()) {
            pool = en.nextElement();
            if (pool.getName().equals(name)) {
                return pool;
            }
        }
        return null;
    }

    public void register(Pool pool) {
        Integer old = Integer.valueOf(_poolHashTable.size());
        _poolHashTable.put(pool.getId(), pool);
        // find last id created
        String[] getId = pool.getId().split("p");
        int id = Integer.parseInt(getId[1]);
        if (id > _idPoolNumber) {
            _idPoolNumber = id;
        }
        setDirtyAndFirePropertyChange(POOL_LENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_poolHashTable
                .size()));
    }

    public void updatePoolComboBox(JComboBox<Pool> box) {
        box.removeAllItems();
        box.addItem(null);
        for (Pool pool : getPoolsByNameList()) {
            box.addItem(pool);
        }
    }

    /**
     * Gets a list of Pools for this location.
     *
     * @return A list of Pools
     */
    public List<Pool> getPoolsByNameList() {
        List<Pool> pools = new ArrayList<>();
        Enumeration<Pool> en = _poolHashTable.elements();
        while (en.hasMoreElements()) {
            pools.add(en.nextElement());
        }
        return pools;
    }

    /**
     * True if this location has a track with pick up or set out restrictions.
     * @return True if there are restrictions at this location.
     */
    public boolean hasServiceRestrictions() {
        Track track;
        Enumeration<Track> en = _trackHashTable.elements();
        while (en.hasMoreElements()) {
            track = en.nextElement();
            if (!track.getDropOption().equals(Track.ANY) || !track.getPickupOption().equals(Track.ANY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used to determine if there are Pools at this location.
     *
     * @return True if there are Pools at this location
     */
    public boolean hasPools() {
        return _poolHashTable.size() > 0;
    }

    /**
     * Used to determine if there are any planned pickups at this location.
     *
     * @return True if there are planned pickups
     */
    public boolean hasPlannedPickups() {
        List<Track> tracks = getTrackList();
        for (Track track : tracks) {
            if (track.getIgnoreUsedLengthPercentage() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used to determine if there are any load restrictions at this location.
     *
     * @return True if there are load restrictions
     */
    public boolean hasLoadRestrictions() {
        List<Track> tracks = getTrackList();
        for (Track track : tracks) {
            if (!track.getLoadOption().equals(Track.ALL_LOADS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used to determine if there are any load ship restrictions at this
     * location.
     *
     * @return True if there are load ship restrictions
     */
    public boolean hasShipLoadRestrictions() {
        List<Track> tracks = getTrackList();
        for (Track track : tracks) {
            if (!track.getShipLoadOption().equals(Track.ALL_LOADS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used to determine if there are any road restrictions at this location.
     *
     * @return True if there are road restrictions
     */
    public boolean hasRoadRestrictions() {
        List<Track> tracks = getTrackList();
        for (Track track : tracks) {
            if (!track.getRoadOption().equals(Track.ALL_ROADS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used to determine if there are any track destination restrictions at this
     * location.
     *
     * @return True if there are road restrictions
     */
    public boolean hasDestinationRestrictions() {
        List<Track> tracks = getTrackList();
        for (Track track : tracks) {
            if (!track.getDestinationOption().equals(Track.ALL_DESTINATIONS)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAlternateTracks() {
        for (Track track : getTrackList()) {
            if (track.getAlternateTrack() != null) {
                return true;
            }
        }
        return false;
    }

    /*
     * set the jmri.Reporter object associated with this location.
     * 
     * @param reader jmri.Reporter object.
     */
    protected void setReporter(Reporter r) {
        Reporter old = _reader;
        _reader = r;
        if (old != r) {
            setDirtyAndFirePropertyChange("reporterChange", old, r);
        }
    }

    /*
     * get the jmri.Reporter object associated with this location.
     * 
     * @return jmri.Reporter object.
     */
    public Reporter getReporter() {
        return _reader;
    }

    public void dispose() {
        List<Track> tracks = getTrackList();
        for (Track track : tracks) {
            deleteTrack(track);
        }
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarRoads.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineTypes.class).removePropertyChangeListener(this);
        // Change name in case object is still in use, for example Schedules
        setName(MessageFormat.format(Bundle.getMessage("NotValid"), new Object[]{getName()}));
        setDirtyAndFirePropertyChange(DISPOSE_CHANGED_PROPERTY, null, DISPOSE_CHANGED_PROPERTY);
    }

    private void addPropertyChangeListeners() {
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(EngineTypes.class).addPropertyChangeListener(this);
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in operations-locations.dtd
     *
     * @param e Consist XML element
     */
    public Location(Element e) {
        Attribute a;
        if ((a = e.getAttribute(Xml.ID)) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in location element when reading operations");
        }
        if ((a = e.getAttribute(Xml.NAME)) != null) {
            _name = a.getValue();
        }
        if ((a = e.getAttribute(Xml.OPS)) != null) {
            try {
                _locationOps = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Location ops isn't a vaild number for location {}", getName());
            }
        }
        if ((a = e.getAttribute(Xml.DIR)) != null) {
            try {
                _trainDir = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Train directions isn't a vaild number for location {}", getName());
            }
        }
        if ((a = e.getAttribute(Xml.SWITCH_LIST)) != null) {
            _switchList = (a.getValue().equals(Xml.TRUE));
        }
        if ((a = e.getAttribute(Xml.SWITCH_LIST_STATE)) != null) {
            try {
                _switchListState = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Switch list state isn't a vaild number for location {}", getName());
            }
            if (getSwitchListState() == SW_PRINTED) {
                setStatus(PRINTED);
            }
        }
        if ((a = e.getAttribute(Xml.PRINTER_NAME)) != null) {
            _defaultPrinter = a.getValue();
        }
        // load train icon coordinates
        Attribute x;
        Attribute y;
        try {
            if ((x = e.getAttribute(Xml.EAST_TRAIN_ICON_X)) != null &&
                    (y = e.getAttribute(Xml.EAST_TRAIN_ICON_Y)) != null) {
                setTrainIconEast(new Point(Integer.parseInt(x.getValue()), Integer.parseInt(y.getValue())));
            }
            if ((x = e.getAttribute(Xml.WEST_TRAIN_ICON_X)) != null &&
                    (y = e.getAttribute(Xml.WEST_TRAIN_ICON_Y)) != null) {
                setTrainIconWest(new Point(Integer.parseInt(x.getValue()), Integer.parseInt(y.getValue())));
            }
            if ((x = e.getAttribute(Xml.NORTH_TRAIN_ICON_X)) != null &&
                    (y = e.getAttribute(Xml.NORTH_TRAIN_ICON_Y)) != null) {
                setTrainIconNorth(new Point(Integer.parseInt(x.getValue()), Integer.parseInt(y.getValue())));
            }
            if ((x = e.getAttribute(Xml.SOUTH_TRAIN_ICON_X)) != null &&
                    (y = e.getAttribute(Xml.SOUTH_TRAIN_ICON_Y)) != null) {
                setTrainIconSouth(new Point(Integer.parseInt(x.getValue()), Integer.parseInt(y.getValue())));
            }
            
            if ((x = e.getAttribute(Xml.TRAIN_ICON_RANGE_X)) != null){
                setTrainIconRangeX(Integer.parseInt(x.getValue()));
            }
            if ((y = e.getAttribute(Xml.TRAIN_ICON_RANGE_Y)) != null){
                setTrainIconRangeY(Integer.parseInt(y.getValue()));
            }
            
        } catch (NumberFormatException nfe) {
            log.error("Train icon coordinates aren't vaild for location {}", getName());
        }
        if ((a = e.getAttribute(Xml.COMMENT)) != null) {
            _comment = OperationsXml.convertFromXmlComment(a.getValue());
        }
        if ((a = e.getAttribute(Xml.SWITCH_LIST_COMMENT)) != null) {
            _switchListComment = a.getValue();
        }
        if ((a = e.getAttribute(Xml.PHYSICAL_LOCATION)) != null) {
            _physicalLocation = PhysicalLocation.parse(a.getValue());
        }
        // new way of reading car types using elements added in 3.3.1
        if (e.getChild(Xml.TYPES) != null) {
            @SuppressWarnings("unchecked")
            List<Element> carTypes = e.getChild(Xml.TYPES).getChildren(Xml.CAR_TYPE);
            String[] types = new String[carTypes.size()];
            for (int i = 0; i < carTypes.size(); i++) {
                Element type = carTypes.get(i);
                if ((a = type.getAttribute(Xml.NAME)) != null) {
                    types[i] = a.getValue();
                }
            }
            setTypeNames(types);
            @SuppressWarnings("unchecked")
            List<Element> locoTypes = e.getChild(Xml.TYPES).getChildren(Xml.LOCO_TYPE);
            types = new String[locoTypes.size()];
            for (int i = 0; i < locoTypes.size(); i++) {
                Element type = locoTypes.get(i);
                if ((a = type.getAttribute(Xml.NAME)) != null) {
                    types[i] = a.getValue();
                }
            }
            setTypeNames(types);
        } // old way of reading car types up to version 2.99.6
        else if ((a = e.getAttribute(Xml.CAR_TYPES)) != null) {
            String names = a.getValue();
            String[] Types = names.split("%%"); // NOI18N
            setTypeNames(Types);
        }
        // early version of operations called tracks "secondary"
        if (e.getChildren(Xml.SECONDARY) != null) {
            @SuppressWarnings("unchecked")
            List<Element> eTracks = e.getChildren(Xml.SECONDARY);
            for (Element eTrack : eTracks) {
                register(new Track(eTrack, this));
            }
        }
        if (e.getChildren(Xml.TRACK) != null) {
            @SuppressWarnings("unchecked")
            List<Element> eTracks = e.getChildren(Xml.TRACK);
            log.debug("location ({}) has {} tracks", getName(), eTracks.size());
            for (Element eTrack : eTracks) {
                register(new Track(eTrack, this));
            }
        }
        if (e.getAttribute(Xml.READER) != null) {
            //            @SuppressWarnings("unchecked")
            try {
                Reporter r = jmri.InstanceManager
                        .getDefault(jmri.ReporterManager.class)
                        .provideReporter(
                                e.getAttribute(Xml.READER).getValue());
                _reader = r;
            } catch (IllegalArgumentException ex) {
                log.warn("Not able to find reader: {} for location ({})", e.getAttribute(Xml.READER).getValue(),
                        getName());
            }
        }
        addPropertyChangeListeners();
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-locations.dtd.
     *
     * @return Contents in a JDOM Element
     */
    public Element store() {
        Element e = new Element(Xml.LOCATION);
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.NAME, getName());
        e.setAttribute(Xml.OPS, Integer.toString(getLocationOps()));
        e.setAttribute(Xml.DIR, Integer.toString(getTrainDirections()));
        e.setAttribute(Xml.SWITCH_LIST, isSwitchListEnabled() ? Xml.TRUE : Xml.FALSE);
        if (!Setup.isSwitchListRealTime()) {
            e.setAttribute(Xml.SWITCH_LIST_STATE, Integer.toString(getSwitchListState()));
        }
        if (!getDefaultPrinterName().equals(NONE)) {
            e.setAttribute(Xml.PRINTER_NAME, getDefaultPrinterName());
        }
        if (!getTrainIconEast().equals(new Point())) {
            e.setAttribute(Xml.EAST_TRAIN_ICON_X, Integer.toString(getTrainIconEast().x));
            e.setAttribute(Xml.EAST_TRAIN_ICON_Y, Integer.toString(getTrainIconEast().y));
        }
        if (!getTrainIconWest().equals(new Point())) {
            e.setAttribute(Xml.WEST_TRAIN_ICON_X, Integer.toString(getTrainIconWest().x));
            e.setAttribute(Xml.WEST_TRAIN_ICON_Y, Integer.toString(getTrainIconWest().y));
        }
        if (!getTrainIconNorth().equals(new Point())) {
            e.setAttribute(Xml.NORTH_TRAIN_ICON_X, Integer.toString(getTrainIconNorth().x));
            e.setAttribute(Xml.NORTH_TRAIN_ICON_Y, Integer.toString(getTrainIconNorth().y));
        }
        if (!getTrainIconSouth().equals(new Point())) {
            e.setAttribute(Xml.SOUTH_TRAIN_ICON_X, Integer.toString(getTrainIconSouth().x));
            e.setAttribute(Xml.SOUTH_TRAIN_ICON_Y, Integer.toString(getTrainIconSouth().y));
        }
        if (getTrainIconRangeX() != RANGE_DEFAULT) {
            e.setAttribute(Xml.TRAIN_ICON_RANGE_X, Integer.toString(getTrainIconRangeX()));
        }
        if (getTrainIconRangeY() != RANGE_DEFAULT) {
            e.setAttribute(Xml.TRAIN_ICON_RANGE_Y, Integer.toString(getTrainIconRangeY()));
        }
        if (_reader != null) {
            e.setAttribute(Xml.READER, _reader.getDisplayName());
        }
        // build list of rolling stock types for this location
        String[] types = getTypeNames();
        // Old way of saving car types
        if (Control.backwardCompatible) {
            StringBuffer buf = new StringBuffer();
            for (String type : types) {
                // remove types that have been deleted by user
                if (InstanceManager.getDefault(CarTypes.class).containsName(type) || InstanceManager.getDefault(EngineTypes.class).containsName(type)) {
                    buf.append(type + "%%"); // NOI18N
                }
            }
            e.setAttribute(Xml.CAR_TYPES, buf.toString());
        }
        // new way of saving car types
        Element eTypes = new Element(Xml.TYPES);
        for (String type : types) {
            // don't save types that have been deleted by user
            if (InstanceManager.getDefault(EngineTypes.class).containsName(type)) {
                Element eType = new Element(Xml.LOCO_TYPE);
                eType.setAttribute(Xml.NAME, type);
                eTypes.addContent(eType);
            } else if (InstanceManager.getDefault(CarTypes.class).containsName(type)) {
                Element eType = new Element(Xml.CAR_TYPE);
                eType.setAttribute(Xml.NAME, type);
                eTypes.addContent(eType);
            }
        }
        e.addContent(eTypes);

        // save physical location if not default
        if (getPhysicalLocation() != null && !getPhysicalLocation().equals(PhysicalLocation.Origin)) {
            e.setAttribute(Xml.PHYSICAL_LOCATION, getPhysicalLocation().toString());
        }

        e.setAttribute(Xml.COMMENT, getComment());
        e.setAttribute(Xml.SWITCH_LIST_COMMENT, getSwitchListComment());

        List<Track> tracks = getTrackByIdList();
        for (Track track : tracks) {
            e.addContent(track.store());
        }

        return e;
    }

    private void replaceType(String oldType, String newType) {
        if (acceptsTypeName(oldType)) {
            if (newType != null) {
                addTypeName(newType);
            }
            // now adjust tracks
            List<Track> tracks = getTrackList();
            for (Track track : tracks) {
                if (track.acceptsTypeName(oldType)) {
                    track.deleteTypeName(oldType);
                    if (newType != null) {
                        track.addTypeName(newType);
                    }
                }
                // adjust custom loads
                String[] loadNames = track.getLoadNames();
                for (String load : loadNames) {
                    String[] splitLoad = load.split(CarLoad.SPLIT_CHAR);
                    if (splitLoad.length > 1) {
                        if (splitLoad[0].equals(oldType)) {
                            track.deleteLoadName(load);
                            if (newType != null) {
                                load = newType + CarLoad.SPLIT_CHAR + splitLoad[1];
                                track.addLoadName(load);
                            }
                        }
                    }
                }
                loadNames = track.getShipLoadNames();
                for (String load : loadNames) {
                    String[] splitLoad = load.split(CarLoad.SPLIT_CHAR);
                    if (splitLoad.length > 1) {
                        if (splitLoad[0].equals(oldType)) {
                            track.deleteShipLoadName(load);
                            if (newType != null) {
                                load = newType + CarLoad.SPLIT_CHAR + splitLoad[1];
                                track.addShipLoadName(load);
                            }
                        }
                    }
                }
            }
            deleteTypeName(oldType);
        }
    }

    private void replaceRoad(String oldRoad, String newRoad) {
        // now adjust any track locations
        List<Track> tracks = getTrackList();
        for (Track track : tracks) {
            if (track.containsRoadName(oldRoad)) {
                track.deleteRoadName(oldRoad);
                if (newRoad != null) {
                    track.addRoadName(newRoad);
                }
            }
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        // update length of tracks at this location if track length changes
        if (e.getPropertyName().equals(Track.LENGTH_CHANGED_PROPERTY)) {
            setLength(getLength() -
                    Integer.parseInt((String) e.getOldValue()) +
                    Integer.parseInt((String) e.getNewValue()));
        }
        // if a track type change, must update all tables
        if (e.getPropertyName().equals(Track.TRACK_TYPE_CHANGED_PROPERTY)) {
            setDirtyAndFirePropertyChange(TRACK_LISTLENGTH_CHANGED_PROPERTY, null, null);
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY)) {
            replaceType((String) e.getOldValue(), (String) e.getNewValue());
        }
        if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)) {
            replaceRoad((String) e.getOldValue(), (String) e.getNewValue());
        }
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        InstanceManager.getDefault(LocationManagerXml.class).setDirty(true);
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(Location.class);

}
