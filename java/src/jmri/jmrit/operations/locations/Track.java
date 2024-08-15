package jmri.jmrit.operations.locations;

import java.util.*;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.Reporter;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.operations.locations.divisions.Division;
import jmri.jmrit.operations.locations.schedules.*;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.*;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;

/**
 * Represents a location (track) on the layout Can be a spur, yard, staging, or
 * interchange track.
 *
 * @author Daniel Boudreau Copyright (C) 2008 - 2014
 */
public class Track extends PropertyChangeSupport {

    public static final String NONE = "";

    protected String _id = NONE;
    protected String _name = NONE;
    protected String _trackType = NONE; // yard, spur, interchange or staging
    protected Location _location; // the location for this track
    protected int _trainDir = EAST + WEST + NORTH + SOUTH; // train directions
    protected int _numberRS = 0; // number of cars and engines
    protected int _numberCars = 0; // number of cars
    protected int _numberEngines = 0; // number of engines
    protected int _pickupRS = 0; // number of pick ups by trains
    protected int _dropRS = 0; // number of set outs by trains
    protected int _length = 0; // length of track
    protected int _reserved = 0; // length of track reserved by trains
    protected int _reservedLengthDrops = 0; // reserved for car drops
    protected int _numberCarsEnRoute = 0; // number of cars en-route
    protected int _usedLength = 0; // length of track filled by cars and engines
    protected int _ignoreUsedLengthPercentage = IGNORE_0;
    // ignore values 0 - 100%
    public static final int IGNORE_0 = 0;
    public static final int IGNORE_25 = 25;
    public static final int IGNORE_50 = 50;
    public static final int IGNORE_75 = 75;
    public static final int IGNORE_100 = 100;
    protected int _moves = 0; // count of the drops since creation
    protected int _blockingOrder = 0; // the order tracks are serviced
    protected String _alternateTrackId = NONE; // the alternate track id
    protected String _comment = NONE;

    // car types serviced by this track
    protected List<String> _typeList = new ArrayList<>();

    // Manifest and switch list comments
    protected boolean _printCommentManifest = true;
    protected boolean _printCommentSwitchList = false;
    protected String _commentPickup = NONE;
    protected String _commentSetout = NONE;
    protected String _commentBoth = NONE;

    // road options
    protected String _roadOption = ALL_ROADS; // controls car roads
    protected List<String> _roadList = new ArrayList<>();

    // load options
    protected String _loadOption = ALL_LOADS; // receive track load restrictions
    protected List<String> _loadList = new ArrayList<>();
    protected String _shipLoadOption = ALL_LOADS;// ship track load restrictions
    protected List<String> _shipLoadList = new ArrayList<>();

    // destinations that this track will service
    protected String _destinationOption = ALL_DESTINATIONS;
    protected List<String> _destinationIdList = new ArrayList<>();

    // schedule options
    protected String _scheduleName = NONE; // Schedule name if there's one
    protected String _scheduleId = NONE; // Schedule id if there's one
    protected String _scheduleItemId = NONE; // the current scheduled item id
    protected int _scheduleCount = 0; // item count
    protected int _reservedEnRoute = 0; // length of cars en-route to this track
    protected int _reservationFactor = 100; // percentage of track space for
                                            // cars en-route
    protected int _mode = MATCH; // default is match mode
    protected boolean _holdCustomLoads = false; // hold cars with custom loads

    // drop options
    protected String _dropOption = ANY; // controls which route or train can set
                                        // out cars
    protected String _pickupOption = ANY; // controls which route or train can
                                          // pick up cars
    public static final String ANY = "Any"; // track accepts any train or route
    public static final String TRAINS = "trains"; // track accepts trains
    public static final String ROUTES = "routes"; // track accepts routes
    public static final String EXCLUDE_TRAINS = "excludeTrains";
    public static final String EXCLUDE_ROUTES = "excludeRoutes";
    protected List<String> _dropList = new ArrayList<>();
    protected List<String> _pickupList = new ArrayList<>();

    // load options for staging
    protected int _loadOptions = 0;
    private static final int SWAP_GENERIC_LOADS = 1;
    private static final int EMPTY_CUSTOM_LOADS = 2;
    private static final int GENERATE_CUSTOM_LOADS = 4;
    private static final int GENERATE_CUSTOM_LOADS_ANY_SPUR = 8;
    private static final int EMPTY_GENERIC_LOADS = 16;
    private static final int GENERATE_CUSTOM_LOADS_ANY_STAGING_TRACK = 32;

    // load option for spur
    private static final int DISABLE_LOAD_CHANGE = 64;

    // block options
    protected int _blockOptions = 0;
    private static final int BLOCK_CARS = 1;

    // order cars are serviced
    protected String _order = NORMAL;
    public static final String NORMAL = Bundle.getMessage("Normal");
    public static final String FIFO = Bundle.getMessage("FIFO");
    public static final String LIFO = Bundle.getMessage("LIFO");

    // the four types of tracks
    public static final String STAGING = "Staging";
    public static final String INTERCHANGE = "Interchange";
    public static final String YARD = "Yard";
    // note that code before 2020 (4.21.1) used Siding as the spur type
    public static final String SPUR = "Spur"; 
    private static final String SIDING = "Siding"; // For loading older files

    // train directions serviced by this track
    public static final int EAST = 1;
    public static final int WEST = 2;
    public static final int NORTH = 4;
    public static final int SOUTH = 8;

    // how roads are serviced by this track
    public static final String ALL_ROADS = Bundle.getMessage("All"); 
    // track accepts only certain roads
    public static final String INCLUDE_ROADS = Bundle.getMessage("Include");
    // track excludes certain roads
    public static final String EXCLUDE_ROADS = Bundle.getMessage("Exclude");

    // load options
    public static final String ALL_LOADS = Bundle.getMessage("All"); 
    public static final String INCLUDE_LOADS = Bundle.getMessage("Include");
    public static final String EXCLUDE_LOADS = Bundle.getMessage("Exclude");

    // destination options
    public static final String ALL_DESTINATIONS = Bundle.getMessage("All"); 
    public static final String INCLUDE_DESTINATIONS = Bundle.getMessage("Include");
    public static final String EXCLUDE_DESTINATIONS = Bundle.getMessage("Exclude");
    // when true only cars with final destinations are allowed to use track
    protected boolean _onlyCarsWithFD = false;

    // schedule modes
    public static final int SEQUENTIAL = 0;
    public static final int MATCH = 1;

    // pickup status
    public static final String PICKUP_OKAY = "";

    // pool
    protected Pool _pool = null;
    protected int _minimumLength = 0;

    // return status when checking rolling stock
    public static final String OKAY = Bundle.getMessage("okay");
    public static final String LENGTH = Bundle.getMessage("rollingStock") +
            " " +
            Bundle.getMessage("Length").toLowerCase(); // lower case in report
    public static final String TYPE = Bundle.getMessage("type");
    public static final String ROAD = Bundle.getMessage("road");
    public static final String LOAD = Bundle.getMessage("load");
    public static final String CAPACITY = Bundle.getMessage("track") + " " + Bundle.getMessage("capacity");
    public static final String SCHEDULE = Bundle.getMessage("schedule");
    public static final String CUSTOM = Bundle.getMessage("custom");
    public static final String DESTINATION = Bundle.getMessage("carDestination");
    public static final String NO_FINAL_DESTINATION = Bundle.getMessage("noFinalDestination");

    // For property change
    public static final String TYPES_CHANGED_PROPERTY = "trackRollingStockTypes"; // NOI18N
    public static final String ROADS_CHANGED_PROPERTY = "trackRoads"; // NOI18N
    public static final String NAME_CHANGED_PROPERTY = "trackName"; // NOI18N
    public static final String LENGTH_CHANGED_PROPERTY = "trackLength"; // NOI18N
    public static final String MIN_LENGTH_CHANGED_PROPERTY = "trackMinLength"; // NOI18N
    public static final String SCHEDULE_CHANGED_PROPERTY = "trackScheduleChange"; // NOI18N
    public static final String DISPOSE_CHANGED_PROPERTY = "trackDispose"; // NOI18N
    public static final String TRAIN_DIRECTION_CHANGED_PROPERTY = "trackTrainDirection"; // NOI18N
    public static final String DROP_CHANGED_PROPERTY = "trackDrop"; // NOI18N
    public static final String PICKUP_CHANGED_PROPERTY = "trackPickup"; // NOI18N
    public static final String TRACK_TYPE_CHANGED_PROPERTY = "trackType"; // NOI18N
    public static final String LOADS_CHANGED_PROPERTY = "trackLoads"; // NOI18N
    public static final String POOL_CHANGED_PROPERTY = "trackPool"; // NOI18N
    public static final String PLANNED_PICKUPS_CHANGED_PROPERTY = "plannedPickUps"; // NOI18N
    public static final String LOAD_OPTIONS_CHANGED_PROPERTY = "trackLoadOptions"; // NOI18N
    public static final String DESTINATIONS_CHANGED_PROPERTY = "trackDestinations"; // NOI18N
    public static final String DESTINATION_OPTIONS_CHANGED_PROPERTY = "trackDestinationOptions"; // NOI18N
    public static final String SCHEDULE_MODE_CHANGED_PROPERTY = "trackScheduleMode"; // NOI18N
    public static final String SCHEDULE_ID_CHANGED_PROPERTY = "trackScheduleId"; // NOI18N
    public static final String SERVICE_ORDER_CHANGED_PROPERTY = "trackServiceOrder"; // NOI18N
    public static final String ALTERNATE_TRACK_CHANGED_PROPERTY = "trackAlternate"; // NOI18N
    public static final String TRACK_BLOCKING_ORDER_CHANGED_PROPERTY = "trackBlockingOrder"; // NOI18N
    public static final String TRACK_REPORTER_CHANGED_PROPERTY = "trackReporterChange"; // NOI18N
    public static final String ROUTED_CHANGED_PROPERTY = "onlyCarsWithFinalDestinations"; // NOI18N
    public static final String HOLD_CARS_CHANGED_PROPERTY = "trackHoldCarsWithCustomLoads"; // NOI18N
    public static final String TRACK_COMMENT_CHANGED_PROPERTY = "trackComments"; // NOI18N

    // IdTag reader associated with this track.
    protected Reporter _reader = null;

    public Track(String id, String name, String type, Location location) {
        log.debug("New ({}) track ({}) id: {}", type, name, id);
        _location = location;
        _trackType = type;
        _name = name;
        _id = id;
        // a new track accepts all types
        setTypeNames(InstanceManager.getDefault(CarTypes.class).getNames());
        setTypeNames(InstanceManager.getDefault(EngineTypes.class).getNames());
    }

    /**
     * Creates a copy of this track.
     *
     * @param newName     The name of the new track.
     * @param newLocation The location of the new track.
     * @return Track
     */
    public Track copyTrack(String newName, Location newLocation) {
        Track newTrack = newLocation.addTrack(newName, getTrackType());
        newTrack.clearTypeNames(); // all types are accepted by a new track

        newTrack.setAddCustomLoadsAnySpurEnabled(isAddCustomLoadsAnySpurEnabled());
        newTrack.setAddCustomLoadsAnyStagingTrackEnabled(isAddCustomLoadsAnyStagingTrackEnabled());
        newTrack.setAddCustomLoadsEnabled(isAddCustomLoadsEnabled());

        newTrack.setAlternateTrack(getAlternateTrack());
        newTrack.setBlockCarsEnabled(isBlockCarsEnabled());
        newTrack.setComment(getComment());
        newTrack.setCommentBoth(getCommentBothWithColor());
        newTrack.setCommentPickup(getCommentPickupWithColor());
        newTrack.setCommentSetout(getCommentSetoutWithColor());

        newTrack.setDestinationOption(getDestinationOption());
        newTrack.setDestinationIds(getDestinationIds());

        // must set option before setting ids
        newTrack.setDropOption(getDropOption()); 
        newTrack.setDropIds(getDropIds());

        newTrack.setIgnoreUsedLengthPercentage(getIgnoreUsedLengthPercentage());
        newTrack.setLength(getLength());
        newTrack.setLoadEmptyEnabled(isLoadEmptyEnabled());
        newTrack.setLoadNames(getLoadNames());
        newTrack.setLoadOption(getLoadOption());
        newTrack.setLoadSwapEnabled(isLoadSwapEnabled());

        newTrack.setOnlyCarsWithFinalDestinationEnabled(isOnlyCarsWithFinalDestinationEnabled());

        // must set option before setting ids
        newTrack.setPickupOption(getPickupOption());
        newTrack.setPickupIds(getPickupIds());

        // track pools are only shared within a specific location
        if (getPool() != null) {
            newTrack.setPool(newLocation.addPool(getPool().getName()));
            newTrack.setMinimumLength(getMinimumLength());
        }

        newTrack.setPrintManifestCommentEnabled(isPrintManifestCommentEnabled());
        newTrack.setPrintSwitchListCommentEnabled(isPrintSwitchListCommentEnabled());

        newTrack.setRemoveCustomLoadsEnabled(isRemoveCustomLoadsEnabled());
        newTrack.setReservationFactor(getReservationFactor());
        newTrack.setRoadNames(getRoadNames());
        newTrack.setRoadOption(getRoadOption());
        newTrack.setSchedule(getSchedule());
        newTrack.setScheduleMode(getScheduleMode());
        newTrack.setServiceOrder(getServiceOrder());
        newTrack.setShipLoadNames(getShipLoadNames());
        newTrack.setShipLoadOption(getShipLoadOption());
        newTrack.setTrainDirections(getTrainDirections());
        newTrack.setTypeNames(getTypeNames());
        return newTrack;
    }

    // for combo boxes
    @Override
    public String toString() {
        return _name;
    }

    public String getId() {
        return _id;
    }

    public Location getLocation() {
        return _location;
    }

    public void setName(String name) {
        String old = _name;
        _name = name;
        if (!old.equals(name)) {
            // recalculate max track name length
            InstanceManager.getDefault(LocationManager.class).resetNameLengths();
            setDirtyAndFirePropertyChange(NAME_CHANGED_PROPERTY, old, name);
        }
    }

    public String getName() {
        return _name;
    }
    
    public String getSplitName() {
        return TrainCommon.splitString(getName());
    }

    public Division getDivision() {
        return getLocation().getDivision();
    }

    public String getDivisionName() {
        return getLocation().getDivisionName();
    }

    public boolean isSpur() {
        return getTrackType().equals(Track.SPUR);
    }

    public boolean isYard() {
        return getTrackType().equals(Track.YARD);
    }

    public boolean isInterchange() {
        return getTrackType().equals(Track.INTERCHANGE);
    }

    public boolean isStaging() {
        return getTrackType().equals(Track.STAGING);
    }

    public boolean hasMessages() {
        if (!getCommentBoth().isBlank() ||
                !getCommentPickup().isBlank() ||
                !getCommentSetout().isBlank()) {
            return true;
        }
        return false;
    }

    /**
     * Gets the track type
     *
     * @return Track.SPUR Track.YARD Track.INTERCHANGE or Track.STAGING
     */
    public String getTrackType() {
        return _trackType;
    }

    /**
     * Sets the track type, spur, interchange, yard, staging
     *
     * @param type Track.SPUR Track.YARD Track.INTERCHANGE Track.STAGING
     */
    public void setTrackType(String type) {
        String old = _trackType;
        _trackType = type;
        if (!old.equals(type)) {
            setDirtyAndFirePropertyChange(TRACK_TYPE_CHANGED_PROPERTY, old, type);
        }
    }

    public String getTrackTypeName() {
        return (getTrackTypeName(getTrackType()));
    }

    public static String getTrackTypeName(String trackType) {
        if (trackType.equals(Track.SPUR)) {
            return Bundle.getMessage("Spur").toLowerCase();
        }
        if (trackType.equals(Track.YARD)) {
            return Bundle.getMessage("Yard").toLowerCase();
        }
        if (trackType.equals(Track.INTERCHANGE)) {
            return Bundle.getMessage("Class/Interchange"); // abbreviation
        }
        if (trackType.equals(Track.STAGING)) {
            return Bundle.getMessage("Staging").toLowerCase();
        }
        return ("unknown"); // NOI18N
    }

    public void setLength(int length) {
        int old = _length;
        _length = length;
        if (old != length) {
            setDirtyAndFirePropertyChange(LENGTH_CHANGED_PROPERTY, Integer.toString(old), Integer.toString(length));
        }
    }

    public int getLength() {
        return _length;
    }

    /**
     * Sets the minimum length of this track when the track is in a pool.
     *
     * @param length minimum
     */
    public void setMinimumLength(int length) {
        int old = _minimumLength;
        _minimumLength = length;
        if (old != length) {
            setDirtyAndFirePropertyChange(MIN_LENGTH_CHANGED_PROPERTY, Integer.toString(old), Integer.toString(length));
        }
    }

    public int getMinimumLength() {
        return _minimumLength;
    }

    public void setReserved(int reserved) {
        int old = _reserved;
        _reserved = reserved;
        if (old != reserved) {
            setDirtyAndFirePropertyChange("trackReserved", Integer.toString(old), // NOI18N
                    Integer.toString(reserved)); // NOI18N
        }
    }

    public int getReserved() {
        return _reserved;
    }

    public void addReservedInRoute(Car car) {
        int old = _reservedEnRoute;
        _numberCarsEnRoute++;
        _reservedEnRoute = old + car.getTotalLength();
        if (old != _reservedEnRoute) {
            setDirtyAndFirePropertyChange("trackAddReservedInRoute", Integer.toString(old), // NOI18N
                    Integer.toString(_reservedEnRoute)); // NOI18N
        }
    }

    public void deleteReservedInRoute(Car car) {
        int old = _reservedEnRoute;
        _numberCarsEnRoute--;
        _reservedEnRoute = old - car.getTotalLength();
        if (old != _reservedEnRoute) {
            setDirtyAndFirePropertyChange("trackDeleteReservedInRoute", Integer.toString(old), // NOI18N
                    Integer.toString(_reservedEnRoute)); // NOI18N
        }
    }

    /**
     * Used to determine how much track space is going to be consumed by cars in
     * route to this track. See isSpaceAvailable().
     *
     * @return The length of all cars en route to this track including couplers.
     */
    public int getReservedInRoute() {
        return _reservedEnRoute;
    }

    public int getNumberOfCarsInRoute() {
        return _numberCarsEnRoute;
    }

    /**
     * Set the reservation factor. Default 100 (100%). Used by the program when
     * generating car loads from staging. A factor of 100% allows the program to
     * fill a track with car loads. Numbers over 100% can overload a track.
     *
     * @param factor A number from 0 to 10000.
     */
    public void setReservationFactor(int factor) {
        int old = _reservationFactor;
        _reservationFactor = factor;
        if (old != factor) {
            setDirtyAndFirePropertyChange("trackReservationFactor", old, factor); // NOI18N
        }
    }

    public int getReservationFactor() {
        return _reservationFactor;
    }

    /**
     * Sets the mode of operation for the schedule assigned to this track.
     *
     * @param mode Track.SEQUENTIAL or Track.MATCH
     */
    public void setScheduleMode(int mode) {
        int old = _mode;
        _mode = mode;
        if (old != mode) {
            setDirtyAndFirePropertyChange(SCHEDULE_MODE_CHANGED_PROPERTY, old, mode); // NOI18N
        }
    }

    /**
     * Gets the mode of operation for the schedule assigned to this track.
     *
     * @return Mode of operation: Track.SEQUENTIAL or Track.MATCH
     */
    public int getScheduleMode() {
        return _mode;
    }

    public String getScheduleModeName() {
        if (getScheduleMode() == Track.MATCH) {
            return Bundle.getMessage("Match");
        }
        return Bundle.getMessage("Sequential");
    }

    public void setAlternateTrack(Track track) {
        Track oldTrack = _location.getTrackById(_alternateTrackId);
        String old = _alternateTrackId;
        if (track != null) {
            _alternateTrackId = track.getId();
        } else {
            _alternateTrackId = NONE;
        }
        if (!old.equals(_alternateTrackId)) {
            setDirtyAndFirePropertyChange(ALTERNATE_TRACK_CHANGED_PROPERTY, oldTrack, track);
        }
    }

    /**
     * Returns the alternate track for a spur
     * 
     * @return alternate track
     */
    public Track getAlternateTrack() {
        if (!isSpur()) {
            return null;
        }
        return _location.getTrackById(_alternateTrackId);
    }

    public void setHoldCarsWithCustomLoadsEnabled(boolean enable) {
        boolean old = _holdCustomLoads;
        _holdCustomLoads = enable;
        setDirtyAndFirePropertyChange(HOLD_CARS_CHANGED_PROPERTY, old, enable);
    }

    /**
     * If enabled (true), hold cars with custom loads rather than allowing them
     * to go to staging if the spur and the alternate track were full. If
     * disabled, cars with custom loads can be forwarded to staging when this
     * spur and all others with this option are also false.
     * 
     * @return True if enabled
     */
    public boolean isHoldCarsWithCustomLoadsEnabled() {
        return _holdCustomLoads;
    }

    /**
     * Used to determine if there's space available at this track for the car.
     * Considers cars en-route to this track. Used to prevent overloading the
     * track.
     *
     * @param car The car to be set out.
     * @return true if space available.
     */
    public boolean isSpaceAvailable(Car car) {
        int carLength = car.getTotalLength();
        if (car.getKernel() != null) {
            carLength = car.getKernel().getTotalLength();
        }
        int trackLength = getLength();

        // is the car or kernel too long for the track?
        if (trackLength < carLength && getPool() == null) {
            return false;
        }
        // is track part of a pool?
        if (getPool() != null && getPool().getMaxLengthTrack(this) < carLength) {
            return false;
        }
        // ignore reservation factor unless car is departing staging
        if (car.getTrack() != null && car.getTrack().isStaging()) {
            return (getLength() * getReservationFactor() / 100 - (getReservedInRoute() + carLength) >= 0);
        }
        // if there's alternate, include that length in the calculation
        if (getAlternateTrack() != null) {
            trackLength = trackLength + getAlternateTrack().getLength();
        }
        return (trackLength - (getReservedInRoute() + carLength) >= 0);
    }

    public void setUsedLength(int length) {
        int old = _usedLength;
        _usedLength = length;
        if (old != length) {
            setDirtyAndFirePropertyChange("trackUsedLength", Integer.toString(old), // NOI18N
                    Integer.toString(length));
        }
    }

    public int getUsedLength() {
        return _usedLength;
    }

    /**
     * The amount of consumed track space to be ignored when sending new rolling
     * stock to the track. See Planned Pickups in help.
     *
     * @param percentage a number between 0 and 100
     */
    public void setIgnoreUsedLengthPercentage(int percentage) {
        int old = _ignoreUsedLengthPercentage;
        _ignoreUsedLengthPercentage = percentage;
        if (old != percentage) {
            setDirtyAndFirePropertyChange(PLANNED_PICKUPS_CHANGED_PROPERTY, Integer.toString(old),
                    Integer.toString(percentage));
        }
    }

    public int getIgnoreUsedLengthPercentage() {
        return _ignoreUsedLengthPercentage;
    }

    /**
     * Sets the number of rolling stock (cars and or engines) on this track
     */
    private void setNumberRS(int number) {
        int old = _numberRS;
        _numberRS = number;
        if (old != number) {
            setDirtyAndFirePropertyChange("trackNumberRS", Integer.toString(old), // NOI18N
                    Integer.toString(number)); // NOI18N
        }
    }

    /**
     * Sets the number of cars on this track
     */
    private void setNumberCars(int number) {
        int old = _numberCars;
        _numberCars = number;
        if (old != number) {
            setDirtyAndFirePropertyChange("trackNumberCars", Integer.toString(old), // NOI18N
                    Integer.toString(number));
        }
    }

    /**
     * Sets the number of engines on this track
     */
    private void setNumberEngines(int number) {
        int old = _numberEngines;
        _numberEngines = number;
        if (old != number) {
            setDirtyAndFirePropertyChange("trackNumberEngines", Integer.toString(old), // NOI18N
                    Integer.toString(number));
        }
    }

    /**
     * @return The number of rolling stock (cars and engines) on this track
     */
    public int getNumberRS() {
        return _numberRS;
    }

    /**
     * @return The number of cars on this track
     */
    public int getNumberCars() {
        return _numberCars;
    }

    /**
     * @return The number of engines on this track
     */
    public int getNumberEngines() {
        return _numberEngines;
    }

    /**
     * Adds rolling stock to a specific track.
     * 
     * @param rs The rolling stock to place on the track.
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
     * train from this track.
     * 
     * @param rs The rolling stock.
     */
    public void addPickupRS(RollingStock rs) {
        int old = _pickupRS;
        _pickupRS++;
        if (Setup.isBuildAggressive()) {
            setReserved(getReserved() - rs.getTotalLength());
        }
        setDirtyAndFirePropertyChange("trackPickupRS", Integer.toString(old), // NOI18N
                Integer.toString(_pickupRS));
    }

    public void deletePickupRS(RollingStock rs) {
        int old = _pickupRS;
        if (Setup.isBuildAggressive()) {
            setReserved(getReserved() + rs.getTotalLength());
        }
        _pickupRS--;
        setDirtyAndFirePropertyChange("trackDeletePickupRS", Integer.toString(old), // NOI18N
                Integer.toString(_pickupRS));
    }

    /**
     * @return the number of rolling stock (cars and or locos) that are
     *         scheduled for pick up from this track.
     */
    public int getPickupRS() {
        return _pickupRS;
    }

    public int getDropRS() {
        return _dropRS;
    }

    public void addDropRS(RollingStock rs) {
        int old = _dropRS;
        _dropRS++;
        bumpMoves();
        setReserved(getReserved() + rs.getTotalLength());
        _reservedLengthDrops = _reservedLengthDrops + rs.getTotalLength();
        setDirtyAndFirePropertyChange("trackAddDropRS", Integer.toString(old), Integer.toString(_dropRS)); // NOI18N
    }

    public void deleteDropRS(RollingStock rs) {
        int old = _dropRS;
        _dropRS--;
        setReserved(getReserved() - rs.getTotalLength());
        _reservedLengthDrops = _reservedLengthDrops - rs.getTotalLength();
        setDirtyAndFirePropertyChange("trackDeleteDropRS", Integer.toString(old), // NOI18N
                Integer.toString(_dropRS));
    }

    public void setComment(String comment) {
        String old = _comment;
        _comment = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange("trackComment", old, comment); // NOI18N
        }
    }

    public String getComment() {
        return _comment;
    }

    public void setCommentPickup(String comment) {
        String old = _commentPickup;
        _commentPickup = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange(TRACK_COMMENT_CHANGED_PROPERTY, old, comment); // NOI18N
        }
    }

    public String getCommentPickup() {
        return TrainCommon.getTextColorString(getCommentPickupWithColor());
    }

    public String getCommentPickupWithColor() {
        return _commentPickup;
    }

    public void setCommentSetout(String comment) {
        String old = _commentSetout;
        _commentSetout = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange(TRACK_COMMENT_CHANGED_PROPERTY, old, comment); // NOI18N
        }
    }

    public String getCommentSetout() {
        return TrainCommon.getTextColorString(getCommentSetoutWithColor());
    }

    public String getCommentSetoutWithColor() {
        return _commentSetout;
    }

    public void setCommentBoth(String comment) {
        String old = _commentBoth;
        _commentBoth = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange(TRACK_COMMENT_CHANGED_PROPERTY, old, comment); // NOI18N
        }
    }

    public String getCommentBoth() {
        return TrainCommon.getTextColorString(getCommentBothWithColor());
    }

    public String getCommentBothWithColor() {
        return _commentBoth;
    }

    public boolean isPrintManifestCommentEnabled() {
        return _printCommentManifest;
    }

    public void setPrintManifestCommentEnabled(boolean enable) {
        boolean old = isPrintManifestCommentEnabled();
        _printCommentManifest = enable;
        setDirtyAndFirePropertyChange("trackPrintManifestComment", old, enable);
    }

    public boolean isPrintSwitchListCommentEnabled() {
        return _printCommentSwitchList;
    }

    public void setPrintSwitchListCommentEnabled(boolean enable) {
        boolean old = isPrintSwitchListCommentEnabled();
        _printCommentSwitchList = enable;
        setDirtyAndFirePropertyChange("trackPrintSwitchListComment", old, enable);
    }

    /**
     * Returns all of the rolling stock type names serviced by this track.
     *
     * @return rolling stock type names
     */
    public String[] getTypeNames() {
        List<String> list = new ArrayList<>();
        for (String typeName : _typeList) {
            if (_location.acceptsTypeName(typeName)) {
                list.add(typeName);
            }
        }
        return list.toArray(new String[0]);
    }

    private void setTypeNames(String[] types) {
        if (types.length > 0) {
            Arrays.sort(types);
            for (String type : types) {
                if (!_typeList.contains(type)) {
                    _typeList.add(type);
                }
            }
        }
    }

    private void clearTypeNames() {
        _typeList.clear();
    }

    public void addTypeName(String type) {
        // insert at start of list, sort later
        if (type == null || _typeList.contains(type)) {
            return;
        }
        _typeList.add(0, type);
        log.debug("Track ({}) add rolling stock type ({})", getName(), type);
        setDirtyAndFirePropertyChange(TYPES_CHANGED_PROPERTY, _typeList.size() - 1, _typeList.size());
    }

    public void deleteTypeName(String type) {
        if (_typeList.remove(type)) {
            log.debug("Track ({}) delete rolling stock type ({})", getName(), type);
            setDirtyAndFirePropertyChange(TYPES_CHANGED_PROPERTY, _typeList.size() + 1, _typeList.size());
        }
    }

    public boolean isTypeNameAccepted(String type) {
        if (!_location.acceptsTypeName(type)) {
            return false;
        }
        return _typeList.contains(type);
    }

    /**
     * Sets the train directions that can service this track
     *
     * @param direction EAST, WEST, NORTH, SOUTH
     */
    public void setTrainDirections(int direction) {
        int old = _trainDir;
        _trainDir = direction;
        if (old != direction) {
            setDirtyAndFirePropertyChange(TRAIN_DIRECTION_CHANGED_PROPERTY, Integer.toString(old),
                    Integer.toString(direction));
        }
    }

    public int getTrainDirections() {
        return _trainDir;
    }

    public String getRoadOption() {
        return _roadOption;
    }

    public String getRoadOptionString() {
        String s;
        if (getRoadOption().equals(Track.INCLUDE_ROADS)) {
            s = Bundle.getMessage("AcceptOnly") + " " + getRoadNames().length + " " + Bundle.getMessage("Roads");
        } else if (getRoadOption().equals(Track.EXCLUDE_ROADS)) {
            s = Bundle.getMessage("Exclude") + " " + getRoadNames().length + " " + Bundle.getMessage("Roads");
        } else {
            s = Bundle.getMessage("AcceptsAllRoads");
        }
        return s;
    }

    /**
     * Set the road option for this track.
     *
     * @param option ALLROADS, INCLUDEROADS, or EXCLUDEROADS
     */
    public void setRoadOption(String option) {
        String old = _roadOption;
        _roadOption = option;
        setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, old, option);
    }

    public String[] getRoadNames() {
        String[] roads = _roadList.toArray(new String[0]);
        if (_roadList.size() > 0) {
            Arrays.sort(roads);
        }
        return roads;
    }

    private void setRoadNames(String[] roads) {
        if (roads.length > 0) {
            Arrays.sort(roads);
            for (String roadName : roads) {
                if (!roadName.equals(NONE)) {
                    _roadList.add(roadName);
                }
            }
        }
    }

    public void addRoadName(String road) {
        if (!_roadList.contains(road)) {
            _roadList.add(road);
            log.debug("Track ({}) add car road ({})", getName(), road);
            setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, _roadList.size() - 1, _roadList.size());
        }
    }

    public void deleteRoadName(String road) {
        if (_roadList.remove(road)) {
            log.debug("Track ({}) delete car road ({})", getName(), road);
            setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, _roadList.size() + 1, _roadList.size());
        }
    }

    public boolean isRoadNameAccepted(String road) {
        if (_roadOption.equals(ALL_ROADS)) {
            return true;
        }
        if (_roadOption.equals(INCLUDE_ROADS)) {
            return _roadList.contains(road);
        }
        // exclude!
        return !_roadList.contains(road);
    }

    public boolean containsRoadName(String road) {
        return _roadList.contains(road);
    }

    /**
     * Gets the car receive load option for this track.
     *
     * @return ALL_LOADS INCLUDE_LOADS EXCLUDE_LOADS
     */
    public String getLoadOption() {
        return _loadOption;
    }

    public String getLoadOptionString() {
        String s;
        if (getLoadOption().equals(Track.INCLUDE_LOADS)) {
            s = Bundle.getMessage("AcceptOnly") + " " + getLoadNames().length + " " + Bundle.getMessage("Loads");
        } else if (getLoadOption().equals(Track.EXCLUDE_LOADS)) {
            s = Bundle.getMessage("Exclude") + " " + getLoadNames().length + " " + Bundle.getMessage("Loads");
        } else {
            s = Bundle.getMessage("AcceptsAllLoads");
        }
        return s;
    }

    /**
     * Set how this track deals with receiving car loads
     *
     * @param option ALL_LOADS INCLUDE_LOADS EXCLUDE_LOADS
     */
    public void setLoadOption(String option) {
        String old = _loadOption;
        _loadOption = option;
        setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, old, option);
    }

    private void setLoadNames(String[] loads) {
        if (loads.length > 0) {
            Arrays.sort(loads);
            for (String loadName : loads) {
                if (!loadName.equals(NONE)) {
                    _loadList.add(loadName);
                }
            }
        }
    }

    /**
     * Provides a list of receive loads that the track will either service or
     * exclude. See setLoadOption
     *
     * @return Array of load names as Strings
     */
    public String[] getLoadNames() {
        String[] loads = _loadList.toArray(new String[0]);
        if (_loadList.size() > 0) {
            Arrays.sort(loads);
        }
        return loads;
    }

    /**
     * Add a receive load that the track will either service or exclude. See
     * setLoadOption
     * 
     * @param load The string load name.
     */
    public void addLoadName(String load) {
        if (!_loadList.contains(load)) {
            _loadList.add(load);
            log.debug("track ({}) add car load ({})", getName(), load);
            setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, _loadList.size() - 1, _loadList.size());
        }
    }

    /**
     * Delete a receive load name that the track will either service or exclude.
     * See setLoadOption
     * 
     * @param load The string load name.
     */
    public void deleteLoadName(String load) {
        if (_loadList.remove(load)) {
            log.debug("track ({}) delete car load ({})", getName(), load);
            setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, _loadList.size() + 1, _loadList.size());
        }
    }

    /**
     * Determine if track will service a specific receive load name.
     *
     * @param load the load name to check.
     * @return true if track will service this load.
     */
    public boolean isLoadNameAccepted(String load) {
        if (_loadOption.equals(ALL_LOADS)) {
            return true;
        }
        if (_loadOption.equals(INCLUDE_LOADS)) {
            return _loadList.contains(load);
        }
        // exclude!
        return !_loadList.contains(load);
    }

    /**
     * Determine if track will service a specific receive load and car type.
     *
     * @param load the load name to check.
     * @param type the type of car used to carry the load.
     * @return true if track will service this load.
     */
    public boolean isLoadNameAndCarTypeAccepted(String load, String type) {
        if (_loadOption.equals(ALL_LOADS)) {
            return true;
        }
        if (_loadOption.equals(INCLUDE_LOADS)) {
            return _loadList.contains(load) || _loadList.contains(type + CarLoad.SPLIT_CHAR + load);
        }
        // exclude!
        return !_loadList.contains(load) && !_loadList.contains(type + CarLoad.SPLIT_CHAR + load);
    }

    /**
     * Gets the car ship load option for this track.
     *
     * @return ALL_LOADS INCLUDE_LOADS EXCLUDE_LOADS
     */
    public String getShipLoadOption() {
        if (!isStaging()) {
            return ALL_LOADS;
        }
        return _shipLoadOption;
    }

    public String getShipLoadOptionString() {
        String s;
        if (getShipLoadOption().equals(Track.INCLUDE_LOADS)) {
            s = Bundle.getMessage("ShipOnly") + " " + getShipLoadNames().length + " " + Bundle.getMessage("Loads");
        } else if (getShipLoadOption().equals(Track.EXCLUDE_LOADS)) {
            s = Bundle.getMessage("Exclude") + " " + getShipLoadNames().length + " " + Bundle.getMessage("Loads");
        } else {
            s = Bundle.getMessage("ShipsAllLoads");
        }
        return s;
    }

    /**
     * Set how this track deals with shipping car loads
     *
     * @param option ALL_LOADS INCLUDE_LOADS EXCLUDE_LOADS
     */
    public void setShipLoadOption(String option) {
        String old = _shipLoadOption;
        _shipLoadOption = option;
        setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, old, option);
    }

    private void setShipLoadNames(String[] loads) {
        if (loads.length > 0) {
            Arrays.sort(loads);
            for (String shipLoadName : loads) {
                if (!shipLoadName.equals(NONE)) {
                    _shipLoadList.add(shipLoadName);
                }
            }
        }
    }

    /**
     * Provides a list of ship loads that the track will either service or
     * exclude. See setShipLoadOption
     *
     * @return Array of load names as Strings
     */
    public String[] getShipLoadNames() {
        String[] loads = _shipLoadList.toArray(new String[0]);
        if (_shipLoadList.size() > 0) {
            Arrays.sort(loads);
        }
        return loads;
    }

    /**
     * Add a ship load that the track will either service or exclude. See
     * setShipLoadOption
     * 
     * @param load The string load name.
     */
    public void addShipLoadName(String load) {
        if (!_shipLoadList.contains(load)) {
            _shipLoadList.add(load);
            log.debug("track ({}) add car load ({})", getName(), load);
            setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, _shipLoadList.size() - 1, _shipLoadList.size());
        }
    }

    /**
     * Delete a ship load name that the track will either service or exclude.
     * See setLoadOption
     * 
     * @param load The string load name.
     */
    public void deleteShipLoadName(String load) {
        if (_shipLoadList.remove(load)) {
            log.debug("track ({}) delete car load ({})", getName(), load);
            setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, _shipLoadList.size() + 1, _shipLoadList.size());
        }
    }

    /**
     * Determine if track will service a specific ship load name.
     *
     * @param load the load name to check.
     * @return true if track will service this load.
     */
    public boolean isLoadNameShipped(String load) {
        if (_shipLoadOption.equals(ALL_LOADS)) {
            return true;
        }
        if (_shipLoadOption.equals(INCLUDE_LOADS)) {
            return _shipLoadList.contains(load);
        }
        // exclude!
        return !_shipLoadList.contains(load);
    }

    /**
     * Determine if track will service a specific ship load and car type.
     *
     * @param load the load name to check.
     * @param type the type of car used to carry the load.
     * @return true if track will service this load.
     */
    public boolean isLoadNameAndCarTypeShipped(String load, String type) {
        if (_shipLoadOption.equals(ALL_LOADS)) {
            return true;
        }
        if (_shipLoadOption.equals(INCLUDE_LOADS)) {
            return _shipLoadList.contains(load) || _shipLoadList.contains(type + CarLoad.SPLIT_CHAR + load);
        }
        // exclude!
        return !_shipLoadList.contains(load) && !_shipLoadList.contains(type + CarLoad.SPLIT_CHAR + load);
    }

    /**
     * Gets the drop option for this track. ANY means that all trains and routes
     * can drop cars to this track. The other four options are used to restrict
     * the track to certain trains or routes.
     * 
     * @return ANY, TRAINS, ROUTES, EXCLUDE_TRAINS, or EXCLUDE_ROUTES
     */
    public String getDropOption() {
        if (isYard()) {
            return ANY;
        }
        return _dropOption;
    }

    /**
     * Set the car drop option for this track.
     *
     * @param option ANY, TRAINS, ROUTES, EXCLUDE_TRAINS, or EXCLUDE_ROUTES
     */
    public void setDropOption(String option) {
        String old = _dropOption;
        _dropOption = option;
        if (!old.equals(option)) {
            _dropList.clear();
        }
        setDirtyAndFirePropertyChange(DROP_CHANGED_PROPERTY, old, option);
    }

    /**
     * Gets the pickup option for this track. ANY means that all trains and
     * routes can pull cars from this track. The other four options are used to
     * restrict the track to certain trains or routes.
     * 
     * @return ANY, TRAINS, ROUTES, EXCLUDE_TRAINS, or EXCLUDE_ROUTES
     */
    public String getPickupOption() {
        if (isYard()) {
            return ANY;
        }
        return _pickupOption;
    }

    /**
     * Set the car pick up option for this track.
     *
     * @param option ANY, TRAINS, ROUTES, EXCLUDE_TRAINS, or EXCLUDE_ROUTES
     */
    public void setPickupOption(String option) {
        String old = _pickupOption;
        _pickupOption = option;
        if (!old.equals(option)) {
            _pickupList.clear();
        }
        setDirtyAndFirePropertyChange(PICKUP_CHANGED_PROPERTY, old, option);
    }

    public String[] getDropIds() {
        return _dropList.toArray(new String[0]);
    }

    private void setDropIds(String[] ids) {
        for (String id : ids) {
            if (id != null) {
                _dropList.add(id);
            }
        }
    }

    public void addDropId(String id) {
        if (!_dropList.contains(id)) {
            _dropList.add(id);
            log.debug("Track ({}) add drop id: {}", getName(), id);
            setDirtyAndFirePropertyChange(DROP_CHANGED_PROPERTY, null, id);
        }
    }

    public void deleteDropId(String id) {
        if (_dropList.remove(id)) {
            log.debug("Track ({}) delete drop id: {}", getName(), id);
            setDirtyAndFirePropertyChange(DROP_CHANGED_PROPERTY, id, null);
        }
    }

    /**
     * Determine if train can set out cars to this track. Based on the train's
     * id or train's route id. See setDropOption(option).
     * 
     * @param train The Train to test.
     * @return true if the train can set out cars to this track.
     */
    public boolean isDropTrainAccepted(Train train) {
        if (getDropOption().equals(ANY)) {
            return true;
        }
        if (getDropOption().equals(TRAINS)) {
            return containsDropId(train.getId());
        }
        if (getDropOption().equals(EXCLUDE_TRAINS)) {
            return !containsDropId(train.getId());
        } else if (train.getRoute() == null) {
            return false;
        }
        return isDropRouteAccepted(train.getRoute());
    }

    public boolean isDropRouteAccepted(Route route) {
        if (getDropOption().equals(ANY) || getDropOption().equals(TRAINS) || getDropOption().equals(EXCLUDE_TRAINS)) {
            return true;
        }
        if (getDropOption().equals(EXCLUDE_ROUTES)) {
            return !containsDropId(route.getId());
        }
        return containsDropId(route.getId());
    }

    public boolean containsDropId(String id) {
        return _dropList.contains(id);
    }

    public String[] getPickupIds() {
        return _pickupList.toArray(new String[0]);
    }

    private void setPickupIds(String[] ids) {
        for (String id : ids) {
            if (id != null) {
                _pickupList.add(id);
            }
        }
    }

    /**
     * Add train or route id to this track.
     * 
     * @param id The string id for the train or route.
     */
    public void addPickupId(String id) {
        if (!_pickupList.contains(id)) {
            _pickupList.add(id);
            log.debug("track ({}) add pick up id {}", getName(), id);
            setDirtyAndFirePropertyChange(PICKUP_CHANGED_PROPERTY, null, id);
        }
    }

    public void deletePickupId(String id) {
        if (_pickupList.remove(id)) {
            log.debug("track ({}) delete pick up id {}", getName(), id);
            setDirtyAndFirePropertyChange(PICKUP_CHANGED_PROPERTY, id, null);
        }
    }

    /**
     * Determine if train can pick up cars from this track. Based on the train's
     * id or train's route id. See setPickupOption(option).
     * 
     * @param train The Train to test.
     * @return true if the train can pick up cars from this track.
     */
    public boolean isPickupTrainAccepted(Train train) {
        if (getPickupOption().equals(ANY)) {
            return true;
        }
        if (getPickupOption().equals(TRAINS)) {
            return containsPickupId(train.getId());
        }
        if (getPickupOption().equals(EXCLUDE_TRAINS)) {
            return !containsPickupId(train.getId());
        } else if (train.getRoute() == null) {
            return false;
        }
        return isPickupRouteAccepted(train.getRoute());
    }

    public boolean isPickupRouteAccepted(Route route) {
        if (getPickupOption().equals(ANY) ||
                getPickupOption().equals(TRAINS) ||
                getPickupOption().equals(EXCLUDE_TRAINS)) {
            return true;
        }
        if (getPickupOption().equals(EXCLUDE_ROUTES)) {
            return !containsPickupId(route.getId());
        }
        return containsPickupId(route.getId());
    }

    public boolean containsPickupId(String id) {
        return _pickupList.contains(id);
    }

    /**
     * Checks to see if all car types can be pulled from this track
     * 
     * @return PICKUP_OKAY if any train can pull all car types from this track
     */
    public String checkPickups() {
        String status = PICKUP_OKAY;
        S1: for (String carType : InstanceManager.getDefault(CarTypes.class).getNames()) {
            if (!isTypeNameAccepted(carType)) {
                continue;
            }
            for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByNameList()) {
                if (!train.isTypeNameAccepted(carType) || !isPickupTrainAccepted(train)) {
                    continue;
                }
                // does the train services this location and track?
                Route route = train.getRoute();
                if (route != null) {
                    for (RouteLocation rLoc : route.getLocationsBySequenceList()) {
                        if (rLoc.getName().equals(getLocation().getName()) &&
                                rLoc.isPickUpAllowed() &&
                                rLoc.getMaxCarMoves() > 0 &&
                                !train.isLocationSkipped(rLoc.getId()) &&
                                ((getTrainDirections() & rLoc.getTrainDirection()) != 0 || train.isLocalSwitcher()) &&
                                ((getLocation().getTrainDirections() & rLoc.getTrainDirection()) != 0 ||
                                        train.isLocalSwitcher())) {

                            continue S1; // car type serviced by this train, try
                                         // next car type
                        }
                    }
                }
            }
            // None of the trains servicing this track can pick up car type
            // ({0})
            status = Bundle.getMessage("ErrorNoTrain", getName(), carType);
            break;
        }
        return status;
    }

    /**
     * Used to determine if track can service the rolling stock.
     *
     * @param rs the car or loco to be tested
     * @return Error string starting with TYPE, ROAD, CAPACITY, LENGTH,
     *         DESTINATION or LOAD if there's an issue. OKAY if track can
     *         service Rolling Stock.
     */
    public String isRollingStockAccepted(RollingStock rs) {
        // first determine if rolling stock can be move to the new location
        // note that there's code that checks for certain issues by checking the
        // first word of the status string returned
        if (!isTypeNameAccepted(rs.getTypeName())) {
            log.debug("Rolling stock ({}) type ({}) not accepted at location ({}, {}) wrong type", rs.toString(),
                    rs.getTypeName(), getLocation().getName(), getName()); // NOI18N
            return TYPE + " (" + rs.getTypeName() + ")";
        }
        if (!isRoadNameAccepted(rs.getRoadName())) {
            log.debug("Rolling stock ({}) road ({}) not accepted at location ({}, {}) wrong road", rs.toString(),
                    rs.getRoadName(), getLocation().getName(), getName()); // NOI18N
            return ROAD + " (" + rs.getRoadName() + ")";
        }
        // now determine if there's enough space for the rolling stock
        int length = rs.getTotalLength();
        // error check
        try {
            Integer.parseInt(rs.getLength());
        } catch (Exception e) {
            return LENGTH + " (" + rs.getLength() + ")";
        }

        if (Car.class.isInstance(rs)) {
            Car car = (Car) rs;
            // does this track service the car's final destination?
            if (!isDestinationAccepted(car.getFinalDestination())) {
                // && getLocation() != car.getFinalDestination()) { // 4/14/2014
                // I can't remember why this was needed
                return DESTINATION +
                        " (" +
                        car.getFinalDestinationName() +
                        ") " +
                        Bundle.getMessage("carIsNotAllowed", getName()); // no
            }
            // does this track accept cars without a final destination?
            if (isOnlyCarsWithFinalDestinationEnabled() && car.getFinalDestination() == null) {
                return NO_FINAL_DESTINATION;
            }
            // check for car in kernel
            if (car.isLead()) {
                length = car.getKernel().getTotalLength();
            }
            if (!isLoadNameAndCarTypeAccepted(car.getLoadName(), car.getTypeName())) {
                log.debug("Car ({}) load ({}) not accepted at location ({}, {})", rs.toString(), car.getLoadName(),
                        getLocation(), getName()); // NOI18N
                return LOAD + " (" + car.getLoadName() + ")";
            }
        }
        // check for loco in consist
        if (Engine.class.isInstance(rs)) {
            Engine eng = (Engine) rs;
            if (eng.isLead()) {
                length = eng.getConsist().getTotalLength();
            }
        }
        if (rs.getTrack() != this &&
                rs.getDestinationTrack() != this &&
                (getUsedLength() + getReserved() + length) > getLength()) {
            // not enough track length check to see if track is in a pool
            if (getPool() != null && getPool().requestTrackLength(this, length)) {
                return OKAY;
            }
            // ignore used length option?
            if (checkPlannedPickUps(length)) {
                return OKAY;
            }
            // The code assumes everything is fine with the track if the Length issue is returned.
            // Is rolling stock too long for this track?
            if ((getLength() < length && getPool() == null) ||
                    (getPool() != null && getPool().getTotalLengthTracks() < length)) {
                return Bundle.getMessage("capacityIssue",
                        CAPACITY, length, Setup.getLengthUnit().toLowerCase(), getLength());
            }
            log.debug("Rolling stock ({}) not accepted at location ({}, {}) no room!", rs.toString(),
                    getLocation().getName(), getName()); // NOI18N

            return Bundle.getMessage("lengthIssue",
                    LENGTH, length, Setup.getLengthUnit().toLowerCase(), getAvailableTrackSpace(), getLength());
        }
        return OKAY;
    }

    /**
     * Performs two checks, number of new set outs shouldn't exceed the track
     * length. The second check protects against overloading, the total number
     * of cars shouldn't exceed the track length plus the number of cars to
     * ignore.
     * 
     * @param length rolling stock length
     * @return true if the program should ignore some percentage of the car's
     *         length currently consuming track space.
     */
    private boolean checkPlannedPickUps(int length) {
        if (getIgnoreUsedLengthPercentage() > IGNORE_0 && getAvailableTrackSpace() >= length) {
            return true;
        }
        return false;
    }

    /**
     * Available track space. Adjusted when a track is using the planned pickups
     * feature
     * 
     * @return available track space
     */
    public int getAvailableTrackSpace() {
        // calculate the available space
        int available = getLength() -
                (getUsedLength() * (IGNORE_100 - getIgnoreUsedLengthPercentage()) / IGNORE_100 + getReserved());
        // could be less if track is overloaded
        int available3 = getLength() +
                (getLength() * getIgnoreUsedLengthPercentage() / IGNORE_100) -
                getUsedLength() -
                getReserved();
        if (available3 < available) {
            available = available3;
        }
        // could be less based on track length
        int available2 = getLength() - getReservedLengthDrops();
        if (available2 < available) {
            available = available2;
        }
        return available;
    }

    public int getReservedLengthDrops() {
        return _reservedLengthDrops;
    }

    public int getMoves() {
        return _moves;
    }

    public void setMoves(int moves) {
        int old = _moves;
        _moves = moves;
        setDirtyAndFirePropertyChange("trackMoves", old, moves); // NOI18N
    }

    public void bumpMoves() {
        setMoves(getMoves() + 1);
    }

    /**
     * Gets the blocking order for this track. Default is zero, in that case,
     * tracks are sorted by name.
     * 
     * @return the blocking order
     */
    public int getBlockingOrder() {
        return _blockingOrder;
    }

    public void setBlockingOrder(int order) {
        int old = _blockingOrder;
        _blockingOrder = order;
        setDirtyAndFirePropertyChange(TRACK_BLOCKING_ORDER_CHANGED_PROPERTY, old, order); // NOI18N
    }

    /**
     * Get the service order for this track. Yards and interchange have this
     * feature for cars. Staging has this feature for trains.
     *
     * @return Service order: Track.NORMAL, Track.FIFO, Track.LIFO
     */
    public String getServiceOrder() {
        if (isSpur() || (isStaging() && getPool() == null)) {
            return NORMAL;
        }
        return _order;
    }

    /**
     * Set the service order for this track. Only yards and interchange have
     * this feature.
     * 
     * @param order Track.NORMAL, Track.FIFO, Track.LIFO
     */
    public void setServiceOrder(String order) {
        String old = _order;
        _order = order;
        setDirtyAndFirePropertyChange(SERVICE_ORDER_CHANGED_PROPERTY, old, order); // NOI18N
    }

    /**
     * Returns the name of the schedule. Note that this returns the schedule
     * name based on the schedule's id. A schedule's name can be modified by the
     * user.
     *
     * @return Schedule name
     */
    public String getScheduleName() {
        if (getScheduleId().equals(NONE)) {
            return NONE;
        }
        Schedule schedule = getSchedule();
        if (schedule == null) {
            log.error("No name schedule for id: {}", getScheduleId());
            return NONE;
        }
        return schedule.getName();
    }

    public Schedule getSchedule() {
        if (getScheduleId().equals(NONE)) {
            return null;
        }
        Schedule schedule = InstanceManager.getDefault(ScheduleManager.class).getScheduleById(getScheduleId());
        if (schedule == null) {
            log.error("No schedule for id: {}", getScheduleId());
        }
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        String scheduleId = NONE;
        if (schedule != null) {
            scheduleId = schedule.getId();
        }
        setScheduleId(scheduleId);
    }

    public String getScheduleId() {
        // Only spurs can have a schedule
        if (!isSpur()) {
            return NONE;
        }
        // old code only stored schedule name, so create id if needed.
        if (_scheduleId.equals(NONE) && !_scheduleName.equals(NONE)) {
            Schedule schedule = InstanceManager.getDefault(ScheduleManager.class).getScheduleByName(_scheduleName);
            if (schedule == null) {
                log.error("No schedule for name: {}", _scheduleName);
            } else {
                _scheduleId = schedule.getId();
            }
        }
        return _scheduleId;
    }

    public void setScheduleId(String id) {
        String old = _scheduleId;
        _scheduleId = id;
        if (!old.equals(id)) {
            Schedule schedule = InstanceManager.getDefault(ScheduleManager.class).getScheduleById(id);
            if (schedule == null) {
                _scheduleName = NONE;
            } else {
                // set the sequence to the first item in the list
                if (schedule.getItemsBySequenceList().size() > 0) {
                    setScheduleItemId(schedule.getItemsBySequenceList().get(0).getId());
                }
                setScheduleCount(0);
            }
            setDirtyAndFirePropertyChange(SCHEDULE_ID_CHANGED_PROPERTY, old, id);
        }
    }

    /**
     * Recommend getCurrentScheduleItem() to get the current schedule item for
     * this track. Protects against user deleting a schedule item from the
     * schedule.
     *
     * @return schedule item id
     */
    public String getScheduleItemId() {
        return _scheduleItemId;
    }

    public void setScheduleItemId(String id) {
        log.debug("Set schedule item id ({}) for track ({})", id, getName());
        String old = _scheduleItemId;
        _scheduleItemId = id;
        setDirtyAndFirePropertyChange(SCHEDULE_CHANGED_PROPERTY, old, id);
    }

    /**
     * Get's the current schedule item for this track Protects against user
     * deleting an item in a shared schedule. Recommend using this versus
     * getScheduleItemId() as the id can be obsolete.
     * 
     * @return The current ScheduleItem.
     */
    public ScheduleItem getCurrentScheduleItem() {
        Schedule sch = getSchedule();
        if (sch == null) {
            log.debug("Can not find schedule id: ({}) assigned to track ({})", getScheduleId(), getName());
            return null;
        }
        ScheduleItem currentSi = sch.getItemById(getScheduleItemId());
        if (currentSi == null && sch.getSize() > 0) {
            log.debug("Can not find schedule item id: ({}) for schedule ({})", getScheduleItemId(), getScheduleName());
            // reset schedule
            setScheduleItemId((sch.getItemsBySequenceList().get(0)).getId());
            currentSi = sch.getItemById(getScheduleItemId());
        }
        return currentSi;
    }

    /**
     * Increments the schedule count if there's a schedule and the schedule is
     * running in sequential mode. Resets the schedule count if the maximum is
     * reached and then goes to the next item in the schedule's list.
     */
    public void bumpSchedule() {
        if (getSchedule() != null && getScheduleMode() == SEQUENTIAL) {
            // bump the schedule count
            setScheduleCount(getScheduleCount() + 1);
            if (getScheduleCount() >= getCurrentScheduleItem().getCount()) {
                setScheduleCount(0);
                // go to the next item in the schedule
                getNextScheduleItem();
            }
        }
    }

    public ScheduleItem getNextScheduleItem() {
        Schedule sch = getSchedule();
        if (sch == null) {
            log.warn("Can not find schedule ({}) assigned to track ({})", getScheduleId(), getName());
            return null;
        }
        List<ScheduleItem> items = sch.getItemsBySequenceList();
        ScheduleItem nextSi = null;
        for (int i = 0; i < items.size(); i++) {
            nextSi = items.get(i);
            if (getCurrentScheduleItem() == nextSi) {
                if (++i < items.size()) {
                    nextSi = items.get(i);
                } else {
                    nextSi = items.get(0);
                }
                setScheduleItemId(nextSi.getId());
                break;
            }
        }
        return nextSi;
    }

    /**
     * Returns how many times the current schedule item has been accessed.
     *
     * @return count
     */
    public int getScheduleCount() {
        return _scheduleCount;
    }

    public void setScheduleCount(int count) {
        int old = _scheduleCount;
        _scheduleCount = count;
        setDirtyAndFirePropertyChange(SCHEDULE_CHANGED_PROPERTY, old, count);
    }

    /**
     * Check to see if schedule is valid for the track at this location.
     *
     * @return SCHEDULE_OKAY if schedule okay, otherwise an error message.
     */
    public String checkScheduleValid() {
        if (getScheduleId().equals(NONE)) {
            return Schedule.SCHEDULE_OKAY;
        }
        Schedule schedule = getSchedule();
        if (schedule == null) {
            return Bundle.getMessage("CanNotFindSchedule", getScheduleId());
        }
        return schedule.checkScheduleValid(this);
    }

    /**
     * Checks to see if car can be placed on this spur using this schedule.
     * Returns OKAY if the schedule can service the car.
     * 
     * @param car The Car to be tested.
     * @return Track.OKAY track.CUSTOM track.SCHEDULE
     */
    public String checkSchedule(Car car) {
        // does car already have this destination?
        if (car.getDestinationTrack() == this) {
            return OKAY;
        }
        // only spurs can have a schedule
        if (!isSpur()) {
            return OKAY;
        }
        if (getScheduleId().equals(NONE)) {
            // does car have a custom load?
            if (car.getLoadName().equals(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName()) ||
                    car.getLoadName().equals(InstanceManager.getDefault(CarLoads.class).getDefaultLoadName())) {
                return OKAY; // no
            }
            return Bundle.getMessage("carHasA", CUSTOM, LOAD, car.getLoadName());
        }
        log.debug("Track ({}) has schedule ({}) mode {} ({})", getName(), getScheduleName(), getScheduleMode(),
                getScheduleModeName()); // NOI18N

        ScheduleItem si = getCurrentScheduleItem();
        // code check, should never be null
        if (si == null) {
            log.error("Could not find schedule item id: ({}) for schedule ({})", getScheduleItemId(),
                    getScheduleName()); // NOI18N
            return SCHEDULE + " ERROR"; // NOI18N
        }
        if (getScheduleMode() == SEQUENTIAL) {
            return getSchedule().checkScheduleItem(si, car, this);
        }
        // schedule in is match mode search entire schedule for a match
        return getSchedule().searchSchedule(car, this);
    }

    /**
     * Check to see if track has schedule and if it does will schedule the next
     * item in the list. Load the car with the next schedule load if one exists,
     * and set the car's final destination if there's one in the schedule.
     * 
     * @param car The Car to be modified.
     * @return Track.OKAY or Track.SCHEDULE
     */
    public String scheduleNext(Car car) {
        // clean up the car's final destination if sent to that destination and
        // there isn't a schedule
        if (getScheduleId().equals(NONE) &&
                car.getDestination() != null &&
                car.getDestination().equals(car.getFinalDestination()) &&
                car.getDestinationTrack() != null &&
                (car.getDestinationTrack().equals(car.getFinalDestinationTrack()) ||
                        car.getFinalDestinationTrack() == null)) {
            car.setFinalDestination(null);
            car.setFinalDestinationTrack(null);
        }
        // check for schedule, only spurs can have a schedule
        if (getScheduleId().equals(NONE) || getSchedule() == null) {
            return OKAY;
        }
        // is car part of a kernel?
        if (car.getKernel() != null && !car.isLead()) {
            log.debug("Car ({}) is part of kernel ({}) not lead", car.toString(), car.getKernelName());
            return OKAY;
        }
        if (!car.getScheduleItemId().equals(Car.NONE)) {
            log.debug("Car ({}) has schedule item id ({})", car.toString(), car.getScheduleItemId());
            ScheduleItem si = car.getScheduleItem(this);
            if (si != null) {
                car.loadNext(si);
                return OKAY;
            }
            log.debug("Schedule id ({}) not valid for track ({})", car.getScheduleItemId(), getName());
            car.setScheduleItemId(Car.NONE);
        }
        // search schedule if match mode
        if (getScheduleMode() == MATCH && !getSchedule().searchSchedule(car, this).equals(OKAY)) {
            return Bundle.getMessage("matchMessage", SCHEDULE, getScheduleName(),
                            getSchedule().hasRandomItem() ? Bundle.getMessage("Random") : "");
        }
        ScheduleItem currentSi = getCurrentScheduleItem();
        log.debug("Destination track ({}) has schedule ({}) item id ({}) mode: {} ({})", getName(), getScheduleName(),
                getScheduleItemId(), getScheduleMode(), getScheduleModeName()); // NOI18N
        if (currentSi != null &&
                (currentSi.getSetoutTrainScheduleId().equals(ScheduleItem.NONE) ||
                        InstanceManager.getDefault(TrainScheduleManager.class).getTrainScheduleActiveId()
                                .equals(currentSi.getSetoutTrainScheduleId())) &&
                car.getTypeName().equals(currentSi.getTypeName()) &&
                (currentSi.getRoadName().equals(ScheduleItem.NONE) ||
                        car.getRoadName().equals(currentSi.getRoadName())) &&
                (currentSi.getReceiveLoadName().equals(ScheduleItem.NONE) ||
                        car.getLoadName().equals(currentSi.getReceiveLoadName()))) {
            car.setScheduleItemId(currentSi.getId());
            car.loadNext(currentSi);
            // bump schedule
            bumpSchedule();
        } else if (currentSi != null) {
            // build return failure message
            String scheduleName = "";
            String currentTrainScheduleName = "";
            TrainSchedule sch = InstanceManager.getDefault(TrainScheduleManager.class)
                    .getScheduleById(InstanceManager.getDefault(TrainScheduleManager.class).getTrainScheduleActiveId());
            if (sch != null) {
                scheduleName = sch.getName();
            }
            sch = InstanceManager.getDefault(TrainScheduleManager.class)
                    .getScheduleById(currentSi.getSetoutTrainScheduleId());
            if (sch != null) {
                currentTrainScheduleName = sch.getName();
            }
            return Bundle.getMessage("sequentialMessage", SCHEDULE, getScheduleName(), getScheduleModeName(),
                    car.toString(), car.getTypeName(), scheduleName, car.getRoadName(), car.getLoadName(),
                    currentSi.getTypeName(), currentTrainScheduleName, currentSi.getRoadName(),
                    currentSi.getReceiveLoadName());
        } else {
            log.error("ERROR Track {} current schedule item is null!", getName());
            return SCHEDULE + " ERROR Track " + getName() + " current schedule item is null!"; // NOI18N
        }
        return OKAY;
    }

    public static final String TRAIN_SCHEDULE = "trainSchedule"; // NOI18N
    public static final String ALL = "all"; // NOI18N

    public boolean checkScheduleAttribute(String attribute, String carType, Car car) {
        Schedule schedule = getSchedule();
        if (schedule == null) {
            return true;
        }
        // if car is already placed at track, don't check car type and load
        if (car != null && car.getTrack() == this) {
            return true;
        }
        return schedule.checkScheduleAttribute(attribute, carType, car);
    }

    /**
     * Enable changing the car generic load state when car arrives at this
     * track.
     *
     * @param enable when true, swap generic car load state
     */
    public void setLoadSwapEnabled(boolean enable) {
        boolean old = isLoadSwapEnabled();
        if (enable) {
            _loadOptions = _loadOptions | SWAP_GENERIC_LOADS;
        } else {
            _loadOptions = _loadOptions & 0xFFFF - SWAP_GENERIC_LOADS;
        }
        setDirtyAndFirePropertyChange(LOAD_OPTIONS_CHANGED_PROPERTY, old, enable);
    }

    public boolean isLoadSwapEnabled() {
        return (0 != (_loadOptions & SWAP_GENERIC_LOADS));
    }

    /**
     * Enable setting the car generic load state to empty when car arrives at
     * this track.
     *
     * @param enable when true, set generic car load to empty
     */
    public void setLoadEmptyEnabled(boolean enable) {
        boolean old = isLoadEmptyEnabled();
        if (enable) {
            _loadOptions = _loadOptions | EMPTY_GENERIC_LOADS;
        } else {
            _loadOptions = _loadOptions & 0xFFFF - EMPTY_GENERIC_LOADS;
        }
        setDirtyAndFirePropertyChange(LOAD_OPTIONS_CHANGED_PROPERTY, old, enable);
    }

    public boolean isLoadEmptyEnabled() {
        return (0 != (_loadOptions & EMPTY_GENERIC_LOADS));
    }

    /**
     * When enabled, remove Scheduled car loads.
     *
     * @param enable when true, remove Scheduled loads from cars
     */
    public void setRemoveCustomLoadsEnabled(boolean enable) {
        boolean old = isRemoveCustomLoadsEnabled();
        if (enable) {
            _loadOptions = _loadOptions | EMPTY_CUSTOM_LOADS;
        } else {
            _loadOptions = _loadOptions & 0xFFFF - EMPTY_CUSTOM_LOADS;
        }
        setDirtyAndFirePropertyChange(LOAD_OPTIONS_CHANGED_PROPERTY, old, enable);
    }

    public boolean isRemoveCustomLoadsEnabled() {
        return (0 != (_loadOptions & EMPTY_CUSTOM_LOADS));
    }

    /**
     * When enabled, add custom car loads if there's a demand.
     *
     * @param enable when true, add custom loads to cars
     */
    public void setAddCustomLoadsEnabled(boolean enable) {
        boolean old = isAddCustomLoadsEnabled();
        if (enable) {
            _loadOptions = _loadOptions | GENERATE_CUSTOM_LOADS;
        } else {
            _loadOptions = _loadOptions & 0xFFFF - GENERATE_CUSTOM_LOADS;
        }
        setDirtyAndFirePropertyChange(LOAD_OPTIONS_CHANGED_PROPERTY, old, enable);
    }

    public boolean isAddCustomLoadsEnabled() {
        return (0 != (_loadOptions & GENERATE_CUSTOM_LOADS));
    }

    /**
     * When enabled, add custom car loads if there's a demand by any
     * spur/industry.
     *
     * @param enable when true, add custom loads to cars
     */
    public void setAddCustomLoadsAnySpurEnabled(boolean enable) {
        boolean old = isAddCustomLoadsAnySpurEnabled();
        if (enable) {
            _loadOptions = _loadOptions | GENERATE_CUSTOM_LOADS_ANY_SPUR;
        } else {
            _loadOptions = _loadOptions & 0xFFFF - GENERATE_CUSTOM_LOADS_ANY_SPUR;
        }
        setDirtyAndFirePropertyChange(LOAD_OPTIONS_CHANGED_PROPERTY, old, enable);
    }

    public boolean isAddCustomLoadsAnySpurEnabled() {
        return (0 != (_loadOptions & GENERATE_CUSTOM_LOADS_ANY_SPUR));
    }

    /**
     * When enabled, add custom car loads to cars in staging for new
     * destinations that are staging.
     *
     * @param enable when true, add custom load to car
     */
    public void setAddCustomLoadsAnyStagingTrackEnabled(boolean enable) {
        boolean old = isAddCustomLoadsAnyStagingTrackEnabled();
        if (enable) {
            _loadOptions = _loadOptions | GENERATE_CUSTOM_LOADS_ANY_STAGING_TRACK;
        } else {
            _loadOptions = _loadOptions & 0xFFFF - GENERATE_CUSTOM_LOADS_ANY_STAGING_TRACK;
        }
        setDirtyAndFirePropertyChange(LOAD_OPTIONS_CHANGED_PROPERTY, old, enable);
    }

    public boolean isAddCustomLoadsAnyStagingTrackEnabled() {
        return (0 != (_loadOptions & GENERATE_CUSTOM_LOADS_ANY_STAGING_TRACK));
    }

    public boolean isModifyLoadsEnabled() {
        return isLoadEmptyEnabled() ||
                isLoadSwapEnabled() ||
                isRemoveCustomLoadsEnabled() ||
                isAddCustomLoadsAnySpurEnabled() ||
                isAddCustomLoadsAnyStagingTrackEnabled() ||
                isAddCustomLoadsEnabled();
    }

    public void setDisableLoadChangeEnabled(boolean enable) {
        boolean old = isDisableLoadChangeEnabled();
        if (enable) {
            _loadOptions = _loadOptions | DISABLE_LOAD_CHANGE;
        } else {
            _loadOptions = _loadOptions & 0xFFFF - DISABLE_LOAD_CHANGE;
        }
        setDirtyAndFirePropertyChange(LOAD_OPTIONS_CHANGED_PROPERTY, old, enable);
    }

    public boolean isDisableLoadChangeEnabled() {
        return (0 != (_loadOptions & DISABLE_LOAD_CHANGE));
    }

    public void setBlockCarsEnabled(boolean enable) {
        boolean old = isBlockCarsEnabled();
        if (enable) {
            _blockOptions = _blockOptions | BLOCK_CARS;
        } else {
            _blockOptions = _blockOptions & 0xFFFF - BLOCK_CARS;
        }
        setDirtyAndFirePropertyChange(LOAD_OPTIONS_CHANGED_PROPERTY, old, enable);
    }

    /**
     * When enabled block cars from staging.
     *
     * @return true if blocking is enabled.
     */
    public boolean isBlockCarsEnabled() {
        if (isStaging()) {
            return (0 != (_blockOptions & BLOCK_CARS));
        }
        return false;
    }

    public void setPool(Pool pool) {
        Pool old = _pool;
        _pool = pool;
        if (old != pool) {
            if (old != null) {
                old.remove(this);
            }
            if (_pool != null) {
                _pool.add(this);
            }
            setDirtyAndFirePropertyChange(POOL_CHANGED_PROPERTY, old, pool);
        }
    }

    public Pool getPool() {
        return _pool;
    }

    public String getPoolName() {
        if (getPool() != null) {
            return getPool().getName();
        }
        return NONE;
    }

    public int getDestinationListSize() {
        return _destinationIdList.size();
    }

    /**
     * adds a location to the list of acceptable destinations for this track.
     * 
     * @param destination location that is acceptable
     */
    public void addDestination(Location destination) {
        if (!_destinationIdList.contains(destination.getId())) {
            _destinationIdList.add(destination.getId());
            setDirtyAndFirePropertyChange(DESTINATIONS_CHANGED_PROPERTY, null, destination.getName()); // NOI18N
        }
    }

    public void deleteDestination(Location destination) {
        if (_destinationIdList.remove(destination.getId())) {
            setDirtyAndFirePropertyChange(DESTINATIONS_CHANGED_PROPERTY, destination.getName(), null); // NOI18N
        }
    }

    /**
     * Returns true if destination is valid from this track.
     * 
     * @param destination The Location to be checked.
     * @return true if track services the destination
     */
    public boolean isDestinationAccepted(Location destination) {
        if (getDestinationOption().equals(ALL_DESTINATIONS) || destination == null) {
            return true;
        }
        return _destinationIdList.contains(destination.getId());
    }

    public void setDestinationIds(String[] ids) {
        for (String id : ids) {
            _destinationIdList.add(id);
        }
    }

    public String[] getDestinationIds() {
        String[] ids = _destinationIdList.toArray(new String[0]);
        return ids;
    }

    /**
     * Sets the destination option for this track. The three options are:
     * <p>
     * ALL_DESTINATIONS which means this track services all destinations, the
     * default.
     * <p>
     * INCLUDE_DESTINATIONS which means this track services only certain
     * destinations.
     * <p>
     * EXCLUDE_DESTINATIONS which means this track does not service certain
     * destinations.
     *
     * @param option Track.ALL_DESTINATIONS, Track.INCLUDE_DESTINATIONS, or
     *               Track.EXCLUDE_DESTINATIONS
     */
    public void setDestinationOption(String option) {
        String old = _destinationOption;
        _destinationOption = option;
        if (!option.equals(old)) {
            setDirtyAndFirePropertyChange(DESTINATION_OPTIONS_CHANGED_PROPERTY, old, option); // NOI18N
        }
    }

    /**
     * Get destination option for interchange or staging track
     * 
     * @return option
     */
    public String getDestinationOption() {
        if (isInterchange() || isStaging()) {
            return _destinationOption;
        }
        return ALL_DESTINATIONS;
    }

    public void setOnlyCarsWithFinalDestinationEnabled(boolean enable) {
        boolean old = _onlyCarsWithFD;
        _onlyCarsWithFD = enable;
        setDirtyAndFirePropertyChange(ROUTED_CHANGED_PROPERTY, old, enable);
    }

    /**
     * When true the track will only accept cars that have a final destination
     * that can be serviced by the track. See acceptsDestination(Location).
     * 
     * @return false if any car spotted, true if only cars with a FD.
     */
    public boolean isOnlyCarsWithFinalDestinationEnabled() {
        if (isInterchange() || isStaging()) {
            return _onlyCarsWithFD;
        }
        return false;
    }

    /**
     * Used to determine if track has been assigned as an alternate
     *
     * @return true if track is an alternate
     */
    public boolean isAlternate() {
        for (Track track : getLocation().getTracksList()) {
            if (track.getAlternateTrack() == this) {
                return true;
            }
        }
        return false;
    }

    public void dispose() {
        // change the name in case object is still in use, for example
        // ScheduleItem.java
        setName(Bundle.getMessage("NotValid", getName()));
        setDirtyAndFirePropertyChange(DISPOSE_CHANGED_PROPERTY, null, DISPOSE_CHANGED_PROPERTY);
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in operations-location.dtd.
     *
     * @param e        Consist XML element
     * @param location The Location loading this track.
     */
    public Track(Element e, Location location) {
        _location = location;
        Attribute a;
        if ((a = e.getAttribute(Xml.ID)) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in track element when reading operations");
        }
        if ((a = e.getAttribute(Xml.NAME)) != null) {
            _name = a.getValue();
        }
        if ((a = e.getAttribute(Xml.TRACK_TYPE)) != null) {
            _trackType = a.getValue();

            // old way of storing track type before 4.21.1
        } else if ((a = e.getAttribute(Xml.LOC_TYPE)) != null) {
            if (a.getValue().equals(SIDING)) {
                _trackType = SPUR;
            } else {
                _trackType = a.getValue();
            }
        }

        if ((a = e.getAttribute(Xml.LENGTH)) != null) {
            try {
                _length = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Track length isn't a vaild number for track {}", getName());
            }
        }
        if ((a = e.getAttribute(Xml.MOVES)) != null) {
            try {
                _moves = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Track moves isn't a vaild number for track {}", getName());
            }

        }
        if ((a = e.getAttribute(Xml.BLOCKING_ORDER)) != null) {
            try {
                _blockingOrder = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Track blocking order isn't a vaild number for track {}", getName());
            }
        }
        if ((a = e.getAttribute(Xml.DIR)) != null) {
            try {
                _trainDir = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Track service direction isn't a vaild number for track {}", getName());
            }
        }
        // old way of reading track comment, see comments below for new format
        if ((a = e.getAttribute(Xml.COMMENT)) != null) {
            _comment = a.getValue();
        }
        // new way of reading car types using elements added in 3.3.1
        if (e.getChild(Xml.TYPES) != null) {
            List<Element> carTypes = e.getChild(Xml.TYPES).getChildren(Xml.CAR_TYPE);
            String[] types = new String[carTypes.size()];
            for (int i = 0; i < carTypes.size(); i++) {
                Element type = carTypes.get(i);
                if ((a = type.getAttribute(Xml.NAME)) != null) {
                    types[i] = a.getValue();
                }
            }
            setTypeNames(types);
            List<Element> locoTypes = e.getChild(Xml.TYPES).getChildren(Xml.LOCO_TYPE);
            types = new String[locoTypes.size()];
            for (int i = 0; i < locoTypes.size(); i++) {
                Element type = locoTypes.get(i);
                if ((a = type.getAttribute(Xml.NAME)) != null) {
                    types[i] = a.getValue();
                }
            }
            setTypeNames(types);
        } // old way of reading car types up to version 3.2
        else if ((a = e.getAttribute(Xml.CAR_TYPES)) != null) {
            String names = a.getValue();
            String[] types = names.split("%%"); // NOI18N
            setTypeNames(types);
        }
        if ((a = e.getAttribute(Xml.CAR_LOAD_OPTION)) != null) {
            _loadOption = a.getValue();
        }
        // new way of reading car loads using elements
        if (e.getChild(Xml.CAR_LOADS) != null) {
            List<Element> carLoads = e.getChild(Xml.CAR_LOADS).getChildren(Xml.CAR_LOAD);
            String[] loads = new String[carLoads.size()];
            for (int i = 0; i < carLoads.size(); i++) {
                Element load = carLoads.get(i);
                if ((a = load.getAttribute(Xml.NAME)) != null) {
                    loads[i] = a.getValue();
                }
            }
            setLoadNames(loads);
        } // old way of reading car loads up to version 3.2
        else if ((a = e.getAttribute(Xml.CAR_LOADS)) != null) {
            String names = a.getValue();
            String[] loads = names.split("%%"); // NOI18N
            log.debug("Track ({}) {} car loads: {}", getName(), getLoadOption(), names);
            setLoadNames(loads);
        }
        if ((a = e.getAttribute(Xml.CAR_SHIP_LOAD_OPTION)) != null) {
            _shipLoadOption = a.getValue();
        }
        // new way of reading car loads using elements
        if (e.getChild(Xml.CAR_SHIP_LOADS) != null) {
            List<Element> carLoads = e.getChild(Xml.CAR_SHIP_LOADS).getChildren(Xml.CAR_LOAD);
            String[] loads = new String[carLoads.size()];
            for (int i = 0; i < carLoads.size(); i++) {
                Element load = carLoads.get(i);
                if ((a = load.getAttribute(Xml.NAME)) != null) {
                    loads[i] = a.getValue();
                }
            }
            setShipLoadNames(loads);
        }
        // new way of reading drop ids using elements
        if (e.getChild(Xml.DROP_IDS) != null) {
            List<Element> dropIds = e.getChild(Xml.DROP_IDS).getChildren(Xml.DROP_ID);
            String[] ids = new String[dropIds.size()];
            for (int i = 0; i < dropIds.size(); i++) {
                Element dropId = dropIds.get(i);
                if ((a = dropId.getAttribute(Xml.ID)) != null) {
                    ids[i] = a.getValue();
                }
            }
            setDropIds(ids);
        } // old way of reading drop ids up to version 3.2
        else if ((a = e.getAttribute(Xml.DROP_IDS)) != null) {
            String names = a.getValue();
            String[] ids = names.split("%%"); // NOI18N
            setDropIds(ids);
        }
        if ((a = e.getAttribute(Xml.DROP_OPTION)) != null) {
            _dropOption = a.getValue();
        }

        // new way of reading pick up ids using elements
        if (e.getChild(Xml.PICKUP_IDS) != null) {
            List<Element> pickupIds = e.getChild(Xml.PICKUP_IDS).getChildren(Xml.PICKUP_ID);
            String[] ids = new String[pickupIds.size()];
            for (int i = 0; i < pickupIds.size(); i++) {
                Element pickupId = pickupIds.get(i);
                if ((a = pickupId.getAttribute(Xml.ID)) != null) {
                    ids[i] = a.getValue();
                }
            }
            setPickupIds(ids);
        } // old way of reading pick up ids up to version 3.2
        else if ((a = e.getAttribute(Xml.PICKUP_IDS)) != null) {
            String names = a.getValue();
            String[] ids = names.split("%%"); // NOI18N
            setPickupIds(ids);
        }
        if ((a = e.getAttribute(Xml.PICKUP_OPTION)) != null) {
            _pickupOption = a.getValue();
        }

        // new way of reading car roads using elements
        if (e.getChild(Xml.CAR_ROADS) != null) {
            List<Element> carRoads = e.getChild(Xml.CAR_ROADS).getChildren(Xml.CAR_ROAD);
            String[] roads = new String[carRoads.size()];
            for (int i = 0; i < carRoads.size(); i++) {
                Element road = carRoads.get(i);
                if ((a = road.getAttribute(Xml.NAME)) != null) {
                    roads[i] = a.getValue();
                }
            }
            setRoadNames(roads);
        } // old way of reading car roads up to version 3.2
        else if ((a = e.getAttribute(Xml.CAR_ROADS)) != null) {
            String names = a.getValue();
            String[] roads = names.split("%%"); // NOI18N
            setRoadNames(roads);
        }
        if ((a = e.getAttribute(Xml.CAR_ROAD_OPTION)) != null) {
            _roadOption = a.getValue();
        } else if ((a = e.getAttribute(Xml.CAR_ROAD_OPERATION)) != null) {
            _roadOption = a.getValue();
        }

        if ((a = e.getAttribute(Xml.SCHEDULE)) != null) {
            _scheduleName = a.getValue();
        }
        if ((a = e.getAttribute(Xml.SCHEDULE_ID)) != null) {
            _scheduleId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.ITEM_ID)) != null) {
            _scheduleItemId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.ITEM_COUNT)) != null) {
            try {
                _scheduleCount = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Schedule count isn't a vaild number for track {}", getName());
            }
        }
        if ((a = e.getAttribute(Xml.FACTOR)) != null) {
            try {
                _reservationFactor = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Reservation factor isn't a vaild number for track {}", getName());
            }
        }
        if ((a = e.getAttribute(Xml.SCHEDULE_MODE)) != null) {
            try {
                _mode = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Schedule mode isn't a vaild number for track {}", getName());
            }
        }
        if ((a = e.getAttribute(Xml.HOLD_CARS_CUSTOM)) != null) {
            setHoldCarsWithCustomLoadsEnabled(a.getValue().equals(Xml.TRUE));
        }
        if ((a = e.getAttribute(Xml.ONLY_CARS_WITH_FD)) != null) {
            setOnlyCarsWithFinalDestinationEnabled(a.getValue().equals(Xml.TRUE));
        }

        if ((a = e.getAttribute(Xml.ALTERNATIVE)) != null) {
            _alternateTrackId = a.getValue();
        }

        if ((a = e.getAttribute(Xml.LOAD_OPTIONS)) != null) {
            try {
                _loadOptions = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Load options isn't a vaild number for track {}", getName());
            }
        }
        if ((a = e.getAttribute(Xml.BLOCK_OPTIONS)) != null) {
            try {
                _blockOptions = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Block options isn't a vaild number for track {}", getName());
            }
        }
        if ((a = e.getAttribute(Xml.ORDER)) != null) {
            _order = a.getValue();
        }
        if ((a = e.getAttribute(Xml.POOL)) != null) {
            setPool(getLocation().addPool(a.getValue()));
            if ((a = e.getAttribute(Xml.MIN_LENGTH)) != null) {
                try {
                    _minimumLength = Integer.parseInt(a.getValue());
                } catch (NumberFormatException nfe) {
                    log.error("Minimum pool length isn't a vaild number for track {}", getName());
                }
            }
        }
        if ((a = e.getAttribute(Xml.IGNORE_USED_PERCENTAGE)) != null) {
            try {
                _ignoreUsedLengthPercentage = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Ignore used percentage isn't a vaild number for track {}", getName());
            }
        }
        if ((a = e.getAttribute(Xml.TRACK_DESTINATION_OPTION)) != null) {
            _destinationOption = a.getValue();
        }
        if (e.getChild(Xml.DESTINATIONS) != null) {
            List<Element> eDestinations = e.getChild(Xml.DESTINATIONS).getChildren(Xml.DESTINATION);
            for (Element eDestination : eDestinations) {
                if ((a = eDestination.getAttribute(Xml.ID)) != null) {
                    _destinationIdList.add(a.getValue());
                }
            }
        }

        if (e.getChild(Xml.COMMENTS) != null) {
            if (e.getChild(Xml.COMMENTS).getChild(Xml.TRACK) != null &&
                    (a = e.getChild(Xml.COMMENTS).getChild(Xml.TRACK).getAttribute(Xml.COMMENT)) != null) {
                _comment = a.getValue();
            }
            if (e.getChild(Xml.COMMENTS).getChild(Xml.BOTH) != null &&
                    (a = e.getChild(Xml.COMMENTS).getChild(Xml.BOTH).getAttribute(Xml.COMMENT)) != null) {
                _commentBoth = a.getValue();
            }
            if (e.getChild(Xml.COMMENTS).getChild(Xml.PICKUP) != null &&
                    (a = e.getChild(Xml.COMMENTS).getChild(Xml.PICKUP).getAttribute(Xml.COMMENT)) != null) {
                _commentPickup = a.getValue();
            }
            if (e.getChild(Xml.COMMENTS).getChild(Xml.SETOUT) != null &&
                    (a = e.getChild(Xml.COMMENTS).getChild(Xml.SETOUT).getAttribute(Xml.COMMENT)) != null) {
                _commentSetout = a.getValue();
            }
            if (e.getChild(Xml.COMMENTS).getChild(Xml.PRINT_MANIFEST) != null &&
                    (a = e.getChild(Xml.COMMENTS).getChild(Xml.PRINT_MANIFEST).getAttribute(Xml.COMMENT)) != null) {
                _printCommentManifest = a.getValue().equals(Xml.TRUE);
            }
            if (e.getChild(Xml.COMMENTS).getChild(Xml.PRINT_SWITCH_LISTS) != null &&
                    (a = e.getChild(Xml.COMMENTS).getChild(Xml.PRINT_SWITCH_LISTS).getAttribute(Xml.COMMENT)) != null) {
                _printCommentSwitchList = a.getValue().equals(Xml.TRUE);
            }
        }

        if ((a = e.getAttribute(Xml.READER)) != null) {
            try {
                Reporter r = jmri.InstanceManager.getDefault(jmri.ReporterManager.class).provideReporter(a.getValue());
                _reader = r;
            } catch (IllegalArgumentException ex) {
                log.warn("Not able to find reader: {} for location ({})", a.getValue(), getName());
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-location.dtd.
     *
     * @return Contents in a JDOM Element
     */
    public Element store() {
        Element e = new Element(Xml.TRACK);
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.NAME, getName());
        e.setAttribute(Xml.TRACK_TYPE, getTrackType());
        e.setAttribute(Xml.DIR, Integer.toString(getTrainDirections()));
        e.setAttribute(Xml.LENGTH, Integer.toString(getLength()));
        e.setAttribute(Xml.MOVES, Integer.toString(getMoves() - getDropRS()));
        if (getBlockingOrder() != 0) {
            e.setAttribute(Xml.BLOCKING_ORDER, Integer.toString(getBlockingOrder()));
        }
        // build list of car types for this track
        String[] types = getTypeNames();
        // new way of saving car types using elements
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

        // build list of car roads for this track
        if (!getRoadOption().equals(ALL_ROADS)) {
            e.setAttribute(Xml.CAR_ROAD_OPTION, getRoadOption());
            String[] roads = getRoadNames();
            // new way of saving road names
            Element eRoads = new Element(Xml.CAR_ROADS);
            for (String road : roads) {
                Element eRoad = new Element(Xml.CAR_ROAD);
                eRoad.setAttribute(Xml.NAME, road);
                eRoads.addContent(eRoad);
            }
            e.addContent(eRoads);
        }

        // save list of car loads for this track
        if (!getLoadOption().equals(ALL_LOADS)) {
            e.setAttribute(Xml.CAR_LOAD_OPTION, getLoadOption());
            String[] loads = getLoadNames();
            // new way of saving car loads using elements
            Element eLoads = new Element(Xml.CAR_LOADS);
            for (String load : loads) {
                Element eLoad = new Element(Xml.CAR_LOAD);
                eLoad.setAttribute(Xml.NAME, load);
                eLoads.addContent(eLoad);
            }
            e.addContent(eLoads);
        }

        // save list of car loads for this track
        if (!getShipLoadOption().equals(ALL_LOADS)) {
            e.setAttribute(Xml.CAR_SHIP_LOAD_OPTION, getShipLoadOption());
            String[] loads = getShipLoadNames();
            // new way of saving car loads using elements
            Element eLoads = new Element(Xml.CAR_SHIP_LOADS);
            for (String load : loads) {
                Element eLoad = new Element(Xml.CAR_LOAD);
                eLoad.setAttribute(Xml.NAME, load);
                eLoads.addContent(eLoad);
            }
            e.addContent(eLoads);
        }

        if (!getDropOption().equals(ANY)) {
            e.setAttribute(Xml.DROP_OPTION, getDropOption());
            // build list of drop ids for this track
            String[] dropIds = getDropIds();
            // new way of saving drop ids using elements
            Element eDropIds = new Element(Xml.DROP_IDS);
            for (String id : dropIds) {
                Element eDropId = new Element(Xml.DROP_ID);
                eDropId.setAttribute(Xml.ID, id);
                eDropIds.addContent(eDropId);
            }
            e.addContent(eDropIds);
        }

        if (!getPickupOption().equals(ANY)) {
            e.setAttribute(Xml.PICKUP_OPTION, getPickupOption());
            // build list of pickup ids for this track
            String[] pickupIds = getPickupIds();
            // new way of saving pick up ids using elements
            Element ePickupIds = new Element(Xml.PICKUP_IDS);
            for (String id : pickupIds) {
                Element ePickupId = new Element(Xml.PICKUP_ID);
                ePickupId.setAttribute(Xml.ID, id);
                ePickupIds.addContent(ePickupId);
            }
            e.addContent(ePickupIds);
        }

        if (getSchedule() != null) {
            e.setAttribute(Xml.SCHEDULE, getScheduleName());
            e.setAttribute(Xml.SCHEDULE_ID, getScheduleId());
            e.setAttribute(Xml.ITEM_ID, getScheduleItemId());
            e.setAttribute(Xml.ITEM_COUNT, Integer.toString(getScheduleCount()));
            e.setAttribute(Xml.FACTOR, Integer.toString(getReservationFactor()));
            e.setAttribute(Xml.SCHEDULE_MODE, Integer.toString(getScheduleMode()));
            e.setAttribute(Xml.HOLD_CARS_CUSTOM, isHoldCarsWithCustomLoadsEnabled() ? Xml.TRUE : Xml.FALSE);
        }
        if (isInterchange() || isStaging()) {
            e.setAttribute(Xml.ONLY_CARS_WITH_FD, isOnlyCarsWithFinalDestinationEnabled() ? Xml.TRUE : Xml.FALSE);
        }
        if (getAlternateTrack() != null) {
            e.setAttribute(Xml.ALTERNATIVE, getAlternateTrack().getId());
        }
        if (_loadOptions != 0) {
            e.setAttribute(Xml.LOAD_OPTIONS, Integer.toString(_loadOptions));
        }
        if (isBlockCarsEnabled()) {
            e.setAttribute(Xml.BLOCK_OPTIONS, Integer.toString(_blockOptions));
        }
        if (!getServiceOrder().equals(NORMAL)) {
            e.setAttribute(Xml.ORDER, getServiceOrder());
        }
        if (getPool() != null) {
            e.setAttribute(Xml.POOL, getPool().getName());
            e.setAttribute(Xml.MIN_LENGTH, Integer.toString(getMinimumLength()));
        }
        if (getIgnoreUsedLengthPercentage() > IGNORE_0) {
            e.setAttribute(Xml.IGNORE_USED_PERCENTAGE, Integer.toString(getIgnoreUsedLengthPercentage()));
        }

        if ((isStaging() || isInterchange()) && !getDestinationOption().equals(ALL_DESTINATIONS)) {
            e.setAttribute(Xml.TRACK_DESTINATION_OPTION, getDestinationOption());
            // save destinations if they exist
            String[] destIds = getDestinationIds();
            if (destIds.length > 0) {
                Element destinations = new Element(Xml.DESTINATIONS);
                for (String id : destIds) {
                    Location loc = InstanceManager.getDefault(LocationManager.class).getLocationById(id);
                    if (loc != null) {
                        Element destination = new Element(Xml.DESTINATION);
                        destination.setAttribute(Xml.ID, id);
                        destination.setAttribute(Xml.NAME, loc.getName());
                        destinations.addContent(destination);
                    }
                }
                e.addContent(destinations);
            }
        }
        // save manifest track comments if they exist
        if (!getComment().equals(NONE) ||
                !getCommentBothWithColor().equals(NONE) ||
                !getCommentPickupWithColor().equals(NONE) ||
                !getCommentSetoutWithColor().equals(NONE)) {
            Element comments = new Element(Xml.COMMENTS);
            Element track = new Element(Xml.TRACK);
            Element both = new Element(Xml.BOTH);
            Element pickup = new Element(Xml.PICKUP);
            Element setout = new Element(Xml.SETOUT);
            Element printManifest = new Element(Xml.PRINT_MANIFEST);
            Element printSwitchList = new Element(Xml.PRINT_SWITCH_LISTS);

            comments.addContent(track);
            comments.addContent(both);
            comments.addContent(pickup);
            comments.addContent(setout);
            comments.addContent(printManifest);
            comments.addContent(printSwitchList);

            track.setAttribute(Xml.COMMENT, getComment());
            both.setAttribute(Xml.COMMENT, getCommentBothWithColor());
            pickup.setAttribute(Xml.COMMENT, getCommentPickupWithColor());
            setout.setAttribute(Xml.COMMENT, getCommentSetoutWithColor());
            printManifest.setAttribute(Xml.COMMENT, isPrintManifestCommentEnabled() ? Xml.TRUE : Xml.FALSE);
            printSwitchList.setAttribute(Xml.COMMENT, isPrintSwitchListCommentEnabled() ? Xml.TRUE : Xml.FALSE);

            e.addContent(comments);
        }
        if (getReporter() != null) {
            e.setAttribute(Xml.READER, getReporter().getDisplayName());
        }
        return e;
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        InstanceManager.getDefault(LocationManagerXml.class).setDirty(true);
        firePropertyChange(p, old, n);
    }

    /*
     * set the jmri.Reporter object associated with this location.
     *
     * @param reader jmri.Reporter object.
     */
    public void setReporter(Reporter r) {
        Reporter old = _reader;
        _reader = r;
        if (old != r) {
            setDirtyAndFirePropertyChange(TRACK_REPORTER_CHANGED_PROPERTY, old, r);
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

    public String getReporterName() {
        if (getReporter() != null) {
            return getReporter().getDisplayName();
        }
        return "";
    }

    private final static Logger log = LoggerFactory.getLogger(Track.class);

}
