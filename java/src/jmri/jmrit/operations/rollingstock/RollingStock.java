package jmri.jmrit.operations.rollingstock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.*;
import jmri.beans.Identifiable;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.divisions.Division;
import jmri.jmrit.operations.locations.divisions.DivisionManager;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.trainbuilder.TrainCommon;

/**
 * Represents rolling stock, both powered (locomotives) and not powered (cars)
 * on the layout.
 *
 * @author Daniel Boudreau Copyright (C) 2009, 2010, 2013, 2023
 */
public abstract class RollingStock extends PropertyChangeSupport implements Identifiable, PropertyChangeListener {

    public static final String NONE = "";
    public static final int DEFAULT_BLOCKING_ORDER = 0;
    public static final int MAX_BLOCKING_ORDER = 100;
    public static final boolean FORCE = true; // ignore length, type, etc. when setting car's track
    protected static final String DEFAULT_WEIGHT = "0";

    protected String _id = NONE;
    protected String _number = NONE;
    protected String _road = NONE;
    protected String _type = NONE;
    protected String _length = "0";
    protected String _color = NONE;
    protected String _weight = DEFAULT_WEIGHT;
    protected String _weightTons = DEFAULT_WEIGHT;
    protected String _built = NONE;
    protected String _owner = NONE;
    protected String _comment = NONE;
    protected String _routeId = NONE; // saved route for interchange tracks
    protected String _rfid = NONE;
    protected String _value = NONE;
    protected Date _lastDate = null;
    protected boolean _locationUnknown = false;
    protected boolean _outOfService = false;
    protected boolean _selected = false;

    protected Location _location = null;
    protected Track _track = null;
    protected Location _destination = null;
    protected Track _trackDestination = null;
    protected Train _train = null;
    protected RouteLocation _routeLocation = null;
    protected RouteLocation _routeDestination = null;
    protected Division _division = null;
    protected boolean _clone = false;
    protected int _cloneOrder = 9999999;
    protected int _moves = 0;
    protected String _lastLocationId = LOCATION_UNKNOWN; // the rollingstock's last location id
    protected String _lastTrackId = LOCATION_UNKNOWN; // the rollingstock's last track id
    protected Train _lastTrain = null; // the last train moving this rs
    protected int _blocking = DEFAULT_BLOCKING_ORDER;
    protected String _pickupTime = NONE;
    protected String _setoutTime = NONE;

    protected IdTag _tag = null;
    protected PropertyChangeListener _tagListener = null;
    protected Location _whereLastSeen = null; // location reported by tag reader
    protected Date _whenLastSeen = null; // date reported by tag reader

    public static final String LOCATION_UNKNOWN = "0";

    protected int number = 0; // used by rolling stock manager for sort by number

    public static final String ERROR_TRACK = "ERROR wrong track for location"; // checks for coding error // NOI18N

    // property changes
    public static final String TRACK_CHANGED_PROPERTY = "rolling stock track location"; // NOI18N
    public static final String DESTINATION_TRACK_CHANGED_PROPERTY = "rolling stock track destination"; // NOI18N
    public static final String TRAIN_CHANGED_PROPERTY = "rolling stock train"; // NOI18N
    public static final String LENGTH_CHANGED_PROPERTY = "rolling stock length"; // NOI18N
    public static final String TYPE_CHANGED_PROPERTY = "rolling stock type"; // NOI18N
    public static final String ROUTE_LOCATION_CHANGED_PROPERTY = "rolling stock route location"; // NOI18N
    public static final String ROUTE_DESTINATION_CHANGED_PROPERTY = "rolling stock route destination"; // NOI18N
    public static final String COMMENT_CHANGED_PROPERTY = "rolling stock comment"; // NOI18N

    // the draw bar length must only be calculated once at startup
    public static final int COUPLERS = Setup.getLengthUnit().equals(Setup.FEET)
            ? Integer.parseInt(Bundle.getMessage("DrawBarLengthFeet"))
            : Integer.parseInt(Bundle.getMessage("DrawBarLengthMeter")); // stocks TODO catch empty/non-integer value

    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    public RollingStock() {
        _lastDate = (new java.util.GregorianCalendar()).getGregorianChange(); // set to change date of the Gregorian
                                                                              // Calendar.
    }

    public RollingStock(String road, String number) {
        this();
        log.debug("New rolling stock ({} {})", road, number);
        _road = road;
        _number = number;
        _id = createId(road, number);
        addPropertyChangeListeners();
    }

    public static String createId(String road, String number) {
        return road + number;
    }

    @Override
    public String getId() {
        return _id;
    }

    /**
     * Set the rolling stock identification or road number
     *
     * @param number The rolling stock road number.
     *
     */
    public void setNumber(String number) {
        String oldNumber = _number;
        _number = number;
        if (!oldNumber.equals(number)) {
            firePropertyChange("rolling stock number", oldNumber, number); // NOI18N
            String oldId = _id;
            _id = createId(_road, number);
            setDirtyAndFirePropertyChange(Xml.ID, oldId, _id);
        }
    }

    public String getNumber() {
        return _number;
    }

    public void setRoadName(String road) {
        String old = _road;
        _road = road;
        if (!old.equals(road)) {
            firePropertyChange("rolling stock road", old, road); // NOI18N
            String oldId = _id;
            _id = createId(road, _number);
            setDirtyAndFirePropertyChange(Xml.ID, oldId, _id);
        }
    }

    public String getRoadName() {
        return _road;
    }

    /**
     * For combobox and identification
     */
    @Override
    public String toString() {
        return getRoadName() + " " + getNumber();
    }

    /**
     * Sets the type of rolling stock. "Boxcar" for example is a type of car.
     *
     * @param type The type of rolling stock.
     */
    public void setTypeName(String type) {
        String old = _type;
        _type = type;
        if (!old.equals(type)) {
            setDirtyAndFirePropertyChange("rolling stock type", old, type); // NOI18N
        }
    }

    public String getTypeName() {
        return _type;
    }

    protected boolean _lengthChange = false; // used for loco length change

    /**
     * Sets the body length of the rolling stock. For example, a 40' boxcar would be
     * entered as 40 feet. Coupler length is added by the program when determining
     * if a car could fit on a track.
     * 
     * @see #getTotalLength()
     * @param length the body length in feet or meters
     */
    public void setLength(String length) {
        String old = _length;
        if (!old.equals(length)) {
            // adjust used length if rolling stock is at a location
            if (getLocation() != null && getTrack() != null) {
                getLocation().setUsedLength(getLocation().getUsedLength() + Integer.parseInt(length) - Integer.parseInt(old));
                getTrack().setUsedLength(getTrack().getUsedLength() + Integer.parseInt(length) - Integer.parseInt(old));
                if (getDestination() != null && getDestinationTrack() != null && !_lengthChange) {
                    _lengthChange = true; // prevent recursive loop, and we want the "old" engine length
                    log.debug("Rolling stock ({}) has destination ({}, {})", this, getDestination().getName(),
                            getDestinationTrack().getName());
                    getTrack().deletePickupRS(this);
                    getDestinationTrack().deleteDropRS(this);
                    // now change the length and update tracks
                    _length = length;
                    getTrack().addPickupRS(this);
                    getDestinationTrack().addDropRS(this);
                    _lengthChange = false; // done
                }
            }
            _length = length;
            setDirtyAndFirePropertyChange(LENGTH_CHANGED_PROPERTY, old, length);
        }
    }

    /**
     * gets the body length of the rolling stock
     * 
     * @return length in feet or meters
     * 
     * @see #getTotalLength()
     */
    public String getLength() {
        return _length;
    }

    public int getLengthInteger() {
        try {
            return Integer.parseInt(getLength());
        } catch (NumberFormatException e) {
            log.error("Rolling stock ({}) length ({}) is not valid ", this, getLength());
        }
        return 0;
    }

    /**
     * Returns the length of the rolling stock including the couplers
     *
     * 
     * @return total length of the rolling stock in feet or meters
     */
    public int getTotalLength() {
        return getLengthInteger() + RollingStock.COUPLERS;
    }

    public void setColor(String color) {
        String old = _color;
        _color = color;
        if (!old.equals(color)) {
            setDirtyAndFirePropertyChange("rolling stock color", old, color); // NOI18N
        }
    }

    public String getColor() {
        return _color;
    }

    /**
     *
     * @param weight rolling stock weight in ounces.
     */
    public void setWeight(String weight) {
        String old = _weight;
        _weight = weight;
        if (!old.equals(weight)) {
            setDirtyAndFirePropertyChange("rolling stock weight", old, weight); // NOI18N
        }
    }

    public String getWeight() {
        return _weight;
    }

    /**
     * Sets the full scale weight in tons.
     *
     * @param weight full scale rolling stock weight in tons.
     */
    public void setWeightTons(String weight) {
        String old = _weightTons;
        _weightTons = weight;
        if (!old.equals(weight)) {
            setDirtyAndFirePropertyChange("rolling stock weight tons", old, weight); // NOI18N
        }
    }

    public String getWeightTons() {
        if (!_weightTons.equals(DEFAULT_WEIGHT)) {
            return _weightTons;
        }
        // calculate the ton weight based on actual weight
        double weight = 0;
        try {
            weight = Double.parseDouble(getWeight());
        } catch (NumberFormatException e) {
            log.trace("Weight not set for rolling stock ({})", this);
        }
        return Integer.toString((int) (weight * Setup.getScaleTonRatio()));
    }

    public int getAdjustedWeightTons() {
        int weightTons = 0;
        try {
            // get loaded weight
            weightTons = Integer.parseInt(getWeightTons());
        } catch (NumberFormatException e) {
            log.debug("Rolling stock ({}) weight not set", this);
        }
        return weightTons;
    }

    /**
     * Set the date that the rolling stock was built. Use 4 digits for the year, or
     * the format MM-YY where MM is the two digit month, and YY is the last two
     * years if the rolling stock was built in the 1900s. Use MM-YYYY for units
     * build after 1999.
     *
     * @param built The string built date.
     *
     */
    public void setBuilt(String built) {
        String old = _built;
        _built = built;
        if (!old.equals(built)) {
            setDirtyAndFirePropertyChange("rolling stock built", old, built); // NOI18N
        }
    }

    public String getBuilt() {
        return _built;
    }

    /**
     *
     * @return location unknown symbol, out of service symbol, or none if car okay
     */
    public String getStatus() {
        return (isLocationUnknown() ? "<?> " : (isOutOfService() ? "<O> " : NONE)); // NOI18N
    }

    public Location getLocation() {
        return _location;
    }

    /**
     * Get rolling stock's location name
     *
     * @return empty string if rolling stock isn't on layout
     */
    public String getLocationName() {
        if (getLocation() != null) {
            return getLocation().getName();
        }
        return NONE;
    }
    
    public String getSplitLocationName() {
        return TrainCommon.splitString(getLocationName());
    }

    /**
     * Get rolling stock's location id
     *
     * @return empty string if rolling stock isn't on the layout
     */
    public String getLocationId() {
        if (getLocation() != null) {
            return getLocation().getId();
        }
        return NONE;
    }

    public Track getTrack() {
        return _track;
    }

    /**
     * Set the rolling stock's location and track. Doesn't do any checking and does
     * not fire a property change. Used exclusively by the Router code. Use
     * setLocation(Location, Track) instead.
     *
     * @param track to place the rolling stock on.
     */
    public void setTrack(Track track) {
        if (track != null) {
            _location = track.getLocation();
        }
        _track = track;
    }

    /**
     * Get rolling stock's track name
     *
     * @return empty string if rolling stock isn't on a track
     */
    public String getTrackName() {
        if (getTrack() != null) {
            return getTrack().getName();
        }
        return NONE;
    }
    
    public String getSplitTrackName() {
        return TrainCommon.splitString(getTrackName());
    }
    
    public String getTrackType() {
        if (getTrack() != null) {
            return getTrack().getTrackTypeName();
        }
        return NONE;
    }

    /**
     * Get rolling stock's track id
     *
     * @return empty string if rolling stock isn't on a track
     */
    public String getTrackId() {
        if (getTrack() != null) {
            return getTrack().getId();
        }
        return NONE;
    }

    /**
     * Sets rolling stock location on the layout
     *
     * @param location The Location.
     * @param track    (yard, spur, staging, or interchange track)
     * @return "okay" if successful, "type" if the rolling stock's type isn't
     *         acceptable, "road" if rolling stock road isn't acceptable, or
     *         "length" if the rolling stock length didn't fit.
     */
    public String setLocation(Location location, Track track) {
        return setLocation(location, track, !FORCE); // don't force
    }

    /**
     * Sets rolling stock location on the layout
     *
     * @param location The Location.
     * @param track    (yard, spur, staging, or interchange track)
     * @param force    when true place rolling stock ignore track length, type, and
     *                 road
     * @return "okay" if successful, "type" if the rolling stock's type isn't
     *         acceptable, "road" if rolling stock road isn't acceptable, or
     *         "length" if the rolling stock length didn't fit.
     */
    public String setLocation(Location location, Track track, boolean force) {
        Location oldLocation = getLocation();
        Track oldTrack = getTrack();
        // first determine if rolling stock can be move to the new location
        if (!force && (oldLocation != location || oldTrack != track)) {
            String status = testLocation(location, track);
            if (!status.equals(Track.OKAY)) {
                return status;
            }
        }
        // now update
        _location = location;
        _track = track;

        if (oldLocation != location || oldTrack != track) {
            // update rolling stock location on layout, maybe this should be a property
            // change?
            // first remove rolling stock from existing location
            if (oldLocation != null) {
                oldLocation.deleteRS(this);
                oldLocation.removePropertyChangeListener(this);
                // if track is null, then rolling stock is in a train
                if (oldTrack != null) {
                    oldTrack.deleteRS(this);
                    oldTrack.removePropertyChangeListener(this);
                    // if there's a destination then pickup complete
                    if (getDestination() != null) {
                        oldLocation.deletePickupRS();
                        oldTrack.deletePickupRS(this);
                        // don't update rs's previous location if just re-staging
                        if (!oldLocation.isStaging() ||
                                location == null ||
                                !location.isStaging() ||
                                getTrain() != null &&
                                        getTrain().getRoute() != null &&
                                        getTrain().getRoute().size() > 2) {
                            setLastLocationId(oldLocation.getId());
                            setLastTrackId(oldTrack.getId());
                        }
                    }
                }
            }
            if (getLocation() != null) {
                getLocation().addRS(this);
                // Need to know if location name changes so we can forward to listeners
                getLocation().addPropertyChangeListener(this);
            }
            if (getTrack() != null) {
                getTrack().addRS(this);
                // Need to know if location name changes so we can forward to listeners
                getTrack().addPropertyChangeListener(this);
                // if there's a destination then there's a pick up
                if (getDestination() != null) {
                    getLocation().addPickupRS();
                    getTrack().addPickupRS(this);
                }
            }
            setDirtyAndFirePropertyChange(TRACK_CHANGED_PROPERTY, oldTrack, track);
        }
        return Track.OKAY;
    }

    /**
     * Used to confirm that a track is associated with a location, and that rolling
     * stock can be placed on track.
     * 
     * @param location The location
     * @param track    The track, can be null
     * @return OKAY if track exists at location and rolling stock can be placed on
     *         track, ERROR_TRACK if track isn't at location, other if rolling stock
     *         can't be placed on track.
     */
    public String testLocation(Location location, Track track) {
        if (track == null) {
            return Track.OKAY;
        }
        if (location != null && !location.isTrackAtLocation(track)) {
            return ERROR_TRACK;
        }
        return track.isRollingStockAccepted(this);
    }

    /**
     * Sets rolling stock destination on the layout
     *
     * @param destination The Location.
     *
     * @param track       (yard, spur, staging, or interchange track)
     * @return "okay" if successful, "type" if the rolling stock's type isn't
     *         acceptable, or "length" if the rolling stock length didn't fit.
     */
    public String setDestination(Location destination, Track track) {
        return setDestination(destination, track, !RollingStock.FORCE);
    }

    /**
     * Sets rolling stock destination on the layout
     *
     * @param destination The Location.
     *
     * @param track       (yard, spur, staging, or interchange track)
     * @param force       when true ignore track length, type, and road when setting
     *                    destination
     * @return "okay" if successful, "type" if the rolling stock's type isn't
     *         acceptable, or "length" if the rolling stock length didn't fit.
     */
    public String setDestination(Location destination, Track track, boolean force) {
        // first determine if rolling stock can be move to the new destination
        if (!force) {
            String status = rsCheckDestination(destination, track);
            if (!status.equals(Track.OKAY)) {
                return status;
            }
        }
        // now set the rolling stock destination!
        Location oldDestination = getDestination();
        _destination = destination;
        Track oldTrack = getDestinationTrack();
        _trackDestination = track;

        if (oldDestination != destination || oldTrack != track) {
            if (oldDestination != null) {
                oldDestination.deleteDropRS();
                oldDestination.removePropertyChangeListener(this);
                // delete pick up in case destination is null
                if (getLocation() != null && getTrack() != null) {
                    getLocation().deletePickupRS();
                    getTrack().deletePickupRS(this);
                }
            }
            if (oldTrack != null) {
                oldTrack.deleteDropRS(this);
                oldTrack.removePropertyChangeListener(this);
            }
            if (getDestination() != null) {
                getDestination().addDropRS();
                if (getLocation() != null && getTrack() != null) {
                    getLocation().addPickupRS();
                    getTrack().addPickupRS(this);
                }
                // Need to know if destination name changes so we can forward to listeners
                getDestination().addPropertyChangeListener(this);
            }
            if (getDestinationTrack() != null) {
                getDestinationTrack().addDropRS(this);
                // Need to know if destination name changes so we can forward to listeners
                getDestinationTrack().addPropertyChangeListener(this);
            } else {
                // rolling stock has been terminated or reset
                if (getTrain() != null && getTrain().getRoute() != null) {
                    setLastRouteId(getTrain().getRoute().getId());
                }
                setRouteLocation(null);
                setRouteDestination(null);
            }
            setDirtyAndFirePropertyChange(DESTINATION_TRACK_CHANGED_PROPERTY, oldTrack, track);
        }
        return Track.OKAY;
    }

    /**
     * Used to check destination track to see if it will accept rolling stock
     *
     * @param destination The Location.
     * @param track       The Track at destination.
     *
     * @return status OKAY, TYPE, ROAD, LENGTH, ERROR_TRACK
     */
    public String checkDestination(Location destination, Track track) {
        return rsCheckDestination(destination, track);
    }

    private String rsCheckDestination(Location destination, Track track) {
        // first perform a code check
        if (destination != null && !destination.isTrackAtLocation(track)) {
            return ERROR_TRACK;
        }
        if (destination != null && !destination.acceptsTypeName(getTypeName())) {
            return Track.TYPE + " (" + getTypeName() + ")";
        }
        if (destination == null || track == null) {
            return Track.OKAY;
        }
        return track.isRollingStockAccepted(this);
    }

    public Location getDestination() {
        return _destination;
    }

    /**
     * Sets rolling stock destination without reserving destination track space or
     * drop count. Does not fire a property change. Used by car router to test
     * destinations. Use setDestination(Location, Track) instead.
     *
     * @param destination for the rolling stock
     */
    public void setDestination(Location destination) {
        _destination = destination;
    }

    public String getDestinationName() {
        if (getDestination() != null) {
            return getDestination().getName();
        }
        return NONE;
    }
    
    public String getSplitDestinationName() {
        return TrainCommon.splitString(getDestinationName());
    }

    public String getDestinationId() {
        if (getDestination() != null) {
            return getDestination().getId();
        }
        return NONE;
    }

    /**
     * Sets rolling stock destination track without reserving destination track
     * space or drop count. Used by car router to test destinations. Does not fire a
     * property change. Use setDestination(Location, Track) instead.
     *
     * @param track The Track for set out at destination.
     *
     */
    public void setDestinationTrack(Track track) {
        if (track != null) {
            _destination = track.getLocation();
        }
        _trackDestination = track;
    }

    public Track getDestinationTrack() {
        return _trackDestination;
    }

    public String getDestinationTrackName() {
        if (getDestinationTrack() != null) {
            return getDestinationTrack().getName();
        }
        return NONE;
    }
    
    public String getSplitDestinationTrackName() {
        return TrainCommon.splitString(getDestinationTrackName());
    }

    public String getDestinationTrackId() {
        if (getDestinationTrack() != null) {
            return getDestinationTrack().getId();
        }
        return NONE;
    }

    public void setClone(boolean clone) {
        boolean old = _clone;
        _clone = clone;
        if (!old == clone) {
            setDirtyAndFirePropertyChange("clone", old ? "true" : "false", clone ? "true" : "false"); // NOI18N
        }
    }

    public boolean isClone() {
        return _clone;
    }

    public void setCloneOrder(int number) {
        _cloneOrder = number;
    }

    public int getCloneOrder() {
        return _cloneOrder;
    }

    public void setDivision(Division division) {
        Division old = _division;
        _division = division;
        if (old != _division) {
            setDirtyAndFirePropertyChange("homeDivisionChange", old, division);
        }
    }

    public Division getDivision() {
        return _division;
    }

    public String getDivisionName() {
        if (getDivision() != null) {
            return getDivision().getName();
        }
        return NONE;
    }

    public String getDivisionId() {
        if (getDivision() != null) {
            return getDivision().getId();
        }
        return NONE;
    }

    /**
     * Used to block cars from staging
     *
     * @param id The location id from where the car came from before going into
     *           staging.
     */
    public void setLastLocationId(String id) {
        _lastLocationId = id;
    }

    public String getLastLocationId() {
        return _lastLocationId;
    }
    
    public String getLastLocationName() {
        Location location = locationManager.getLocationById(getLastLocationId());
        if (location != null) {
           return location.getName(); 
        }
        return NONE;
    }
    
    public void setLastTrackId(String id) {
        _lastTrackId = id;
    }
    
    public String getLastTrackId() {
        return _lastTrackId;
    }
    
    public String getLastTrackName() {
        Location location = locationManager.getLocationById(getLastLocationId());
        if (location != null) {
            Track track = location.getTrackById(getLastTrackId());
            if (track != null) {
                return track.getName();
            }
        }
        return NONE;
    }

    public void setMoves(int moves) {
        int old = _moves;
        _moves = moves;
        if (old != moves) {
            setDirtyAndFirePropertyChange("rolling stock moves", Integer.toString(old), // NOI18N
                    Integer.toString(moves));
        }
    }

    public int getMoves() {
        return _moves;
    }

    /**
     * Sets the train that will service this rolling stock.
     *
     * @param train The Train.
     *
     */
    public void setTrain(Train train) {
        Train old = _train;
        _train = train;
        if (old != train) {
            if (old != null) {
                old.removePropertyChangeListener(this);
            }
            if (train != null) {
                train.addPropertyChangeListener(this);
            }
            setDirtyAndFirePropertyChange(TRAIN_CHANGED_PROPERTY, old, train);
        }
    }

    public Train getTrain() {
        return _train;
    }

    public String getTrainName() {
        if (getTrain() != null) {
            return getTrain().getName();
        }
        return NONE;
    }

    /**
     * Sets the last train that serviced this rolling stock.
     *
     * @param train The last Train.
     */
    public void setLastTrain(Train train) {
        Train old = _lastTrain;
        _lastTrain = train;
        if (old != train) {
            if (old != null) {
                old.removePropertyChangeListener(this);
            }
            if (train != null) {
                train.addPropertyChangeListener(this);
            }
            setDirtyAndFirePropertyChange(TRAIN_CHANGED_PROPERTY, old, train);
        }
    }

    public Train getLastTrain() {
        return _lastTrain;
    }

    public String getLastTrainName() {
        if (getLastTrain() != null) {
            return getLastTrain().getName();
        }
        return NONE;
    }

    /**
     * Sets the location where the rolling stock will be picked up by the train.
     *
     * @param routeLocation the pick up location for this rolling stock.
     */
    public void setRouteLocation(RouteLocation routeLocation) {
        // a couple of error checks before setting the route location
        if (getLocation() == null && routeLocation != null) {
            log.debug("WARNING rolling stock ({}) does not have an assigned location", this); // NOI18N
        } else if (routeLocation != null && getLocation() != null && !routeLocation.getName().equals(getLocation().getName())) {
            log.error("ERROR route location name({}) not equal to location name ({}) for rolling stock ({})",
                    routeLocation.getName(), getLocation().getName(), this); // NOI18N
        }
        RouteLocation old = _routeLocation;
        _routeLocation = routeLocation;
        if (old != routeLocation) {
            setDirtyAndFirePropertyChange(ROUTE_LOCATION_CHANGED_PROPERTY, old, routeLocation);
        }
    }

    /**
     * Where in a train's route this car resides
     * 
     * @return the location in a train's route
     */
    public RouteLocation getRouteLocation() {
        return _routeLocation;
    }

    public String getRouteLocationId() {
        if (getRouteLocation() != null) {
            return getRouteLocation().getId();
        }
        return NONE;
    }

    /**
     * Used to determine which train delivered a car to an interchange track.
     * 
     * @return the route id of the last train delivering this car.
     */
    public String getLastRouteId() {
        return _routeId;
    }

    /**
     * Sets the id of the route that was used to set out the rolling stock. Used to
     * determine if the rolling stock can be pick ups from an interchange track.
     *
     * @param id The route id.
     */
    public void setLastRouteId(String id) {
        _routeId = id;
    }

    public String getValue() {
        return _value;
    }

    /**
     * Sets the value (cost, price) for this rolling stock. Currently has nothing to
     * do with operations. But nice to have.
     *
     * @param value a string representing what this item is worth.
     */
    public void setValue(String value) {
        String old = _value;
        _value = value;
        if (!old.equals(value)) {
            setDirtyAndFirePropertyChange("rolling stock value", old, value); // NOI18N
        }
    }

    public String getRfid() {
        return _rfid;
    }

    /**
     * Sets the RFID for this rolling stock.
     *
     * @param id 12 character RFID string.
     */
    public void setRfid(String id) {
        String old = _rfid;
        if (id != null && !id.equals(old)) {
            log.debug("Setting IdTag for {} to {}", this, id);
            _rfid = id;
            if (!id.equals(NONE)) {
                try {
                    IdTag tag = InstanceManager.getDefault(IdTagManager.class).provideIdTag(id);
                    setIdTag(tag);
                } catch (IllegalArgumentException e) {
                    log.error("Exception recording tag {} - exception value {}", id, e.getMessage());
                }
            }
            setDirtyAndFirePropertyChange("rolling stock rfid", old, id); // NOI18N
        }
    }

    public IdTag getIdTag() {
        return _tag;
    }

    /**
     * Sets the id tag for this rolling stock. The id tag isn't saved, between
     * session but the tag label is saved as _rfid.
     *
     * @param tag the id tag
     */
    public void setIdTag(IdTag tag) {
        if (_tag != null) {
            _tag.removePropertyChangeListener(_tagListener);
        }
        _tag = tag;
        if (_tagListener == null) {
            // store the tag listener so we can reuse it and
            // dispose of it as necessary.
            _tagListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("whereLastSeen")) {
                        log.debug("Tag Reader Position update received for {}", this);
                        // update the position of this piece of rolling
                        // stock when its IdTag is seen, but only if
                        // the actual location changes.
                        if (e.getNewValue() != null) {
                            // first, check to see if this reader is
                            // associated with a track.
                            Track newTrack = locationManager.getTrackByReporter((jmri.Reporter) e.getNewValue());
                            if (newTrack != null) {
                                if (newTrack != getTrack()) {
                                    // set the car's location based on the track.
                                    setLocation(newTrack.getLocation(), newTrack);
                                    // also notify listeners that the last seen
                                    // location has changed.
                                    setDirtyAndFirePropertyChange("rolling stock whereLastSeen", _whereLastSeen,
                                            _whereLastSeen = newTrack.getLocation());

                                }
                            } else {
                                // the reader isn't associated with a track,
                                Location newLocation = locationManager
                                        .getLocationByReporter((jmri.Reporter) e.getNewValue());
                                if (newLocation != getLocation()) {
                                    // we really should be able to set the
                                    // location where we last saw the tag:
                                    // setLocation(newLocation,null);
                                    // for now, notify listeners that the
                                    // location changed.
                                    setDirtyAndFirePropertyChange("rolling stock whereLastSeen", _whereLastSeen,
                                            _whereLastSeen = newLocation);

                                }
                            }
                        }
                    }
                    if (e.getPropertyName().equals("whenLastSeen")) {
                        log.debug("Tag Reader Time at Location update received for {}", this);
                        // update the time when this car was last moved
                        // stock when its IdTag is seen.
                        if (e.getNewValue() != null) {
                            Date newDate = ((Date) e.getNewValue());
                            setLastDate(newDate);
                            // and notify listeners when last seen was updated.
                            setDirtyAndFirePropertyChange("rolling stock whenLastSeen", _whenLastSeen,
                                    _whenLastSeen = newDate);
                        }
                    }
                }
            };
        }
        if (_tag != null) {
            _tag.addPropertyChangeListener(_tagListener);
            setRfid(_tag.getSystemName());
        } else {
            setRfid(NONE);
        }
        // initilize _whenLastSeen and _whereLastSeen for property
        // change notification.
        _whereLastSeen = getWhereLastSeen();
        _whenLastSeen = getWhenLastSeen();
    }

    public String getWhereLastSeenName() {
        if (getWhereLastSeen() != null) {
            return getWhereLastSeen().getName();
        }
        return NONE;
    }

    public Location getWhereLastSeen() {
        if (_tag == null) {
            return null;
        }
        jmri.Reporter r = _tag.getWhereLastSeen();
        Track t = locationManager.getTrackByReporter(r);
        if (t != null) {
            return t.getLocation();
        }
        // the reader isn't associated with a track, return
        // the location it is associated with, which might be null.
        return locationManager.getLocationByReporter(r);
    }

    public Track getTrackLastSeen() {
        if (_tag == null) {
            // there isn't a tag associated with this piece of rolling stock.
            return null;
        }
        jmri.Reporter r = _tag.getWhereLastSeen();
        if (r == null) {
            // there is a tag associated with this piece
            // of rolling stock, but no reporter has seen it (or it was reset).
            return null;
        }
        // this return value will be null, if there isn't an associated track
        // for the last reporter.
        return locationManager.getTrackByReporter(r);
    }

    public String getTrackLastSeenName() {
        // let getTrackLastSeen() find the track, if it exists.
        Track t = getTrackLastSeen();
        if (t != null) {
            // if there is a track, return the name.
            return t.getName();
        }
        // otherwise, there is no track to return the name of.
        return NONE;
    }

    public Date getWhenLastSeen() {
        if (_tag == null) {
            return null; // never seen, so no date.
        }
        return _tag.getWhenLastSeen();
    }

    /**
     * Provides the last date when this rolling stock was moved, or was reset from a
     * built train, as a string.
     *
     * @return date
     */
    public String getWhenLastSeenDate() {
        if (getWhenLastSeen() == null) {
            return NONE; // return an empty string for the default date.
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // NOI18N
        return format.format(getWhenLastSeen());
    }

    /**
     * Provides the last date when this rolling stock was moved
     *
     * @return String yyyy/MM/dd HH:mm:ss
     */
    public String getSortDate() {
        if (_lastDate.equals((new java.util.GregorianCalendar()).getGregorianChange())) {
            return NONE; // return an empty string for the default date.
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // NOI18N
        return format.format(_lastDate);
    }

    /**
     * Provides the last date when this rolling stock was moved
     *
     * @return String MM/dd/yyyy HH:mm:ss
     */
    public String getLastDate() {
        if (_lastDate.equals((new java.util.GregorianCalendar()).getGregorianChange())) {
            return NONE; // return an empty string for the default date.
        }
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); // NOI18N
        return format.format(_lastDate);
    }

    /**
     * Sets the last date when this rolling stock was moved
     *
     * @param date The Date when this rolling stock was last moved.
     *
     */
    public void setLastDate(Date date) {
        Date old = _lastDate;
        _lastDate = date;
        if (!old.equals(_lastDate)) {
            setDirtyAndFirePropertyChange("rolling stock date", old, date); // NOI18N
        }
    }

    /**
     * Provides the last date when this rolling stock was moved
     *
     * @return date
     */
    public Date getLastMoveDate() {
        return _lastDate;
    }

    /**
     * Sets the last date when this rolling stock was moved. This method is used
     * only for loading data from a file. Use setLastDate(Date) instead.
     *
     * @param date yyyy/MM/dd HH:mm:ss, MM/dd/yyyy HH:mm:ss, MM/dd/yyyy hh:mmaa,
     *             or MM/dd/yyyy HH:mm
     */
    public void setLastDate(String date) {
        Date d = TrainCommon.convertStringToDate(date);
        if (d != null) {
            _lastDate = d;
        }
    }

    public void setBlocking(int number) {
        int old = _blocking;
        _blocking = number;
        if (old != number) {
            setDirtyAndFirePropertyChange("rolling stock blocking changed", old, number); // NOI18N
        }
    }

    public int getBlocking() {
        return _blocking;
    }

    /**
     * Set where in a train's route this rolling stock will be set out.
     *
     * @param routeDestination the location where the rolling stock is to leave the
     *                         train.
     */
    public void setRouteDestination(RouteLocation routeDestination) {
        if (routeDestination != null &&
                getDestination() != null &&
                !routeDestination.getName().equals(getDestination().getName())) {
            log.debug("WARNING route destination name ({}) not equal to destination name ({}) for rolling stock ({})",
                    routeDestination.getName(), getDestination().getName(), this); // NOI18N
        }
        RouteLocation old = _routeDestination;
        _routeDestination = routeDestination;
        if (old != routeDestination) {
            setDirtyAndFirePropertyChange(ROUTE_DESTINATION_CHANGED_PROPERTY, old, routeDestination);
        }
    }

    public RouteLocation getRouteDestination() {
        return _routeDestination;
    }

    public String getRouteDestinationId() {
        if (getRouteDestination() != null) {
            return getRouteDestination().getId();
        }
        return NONE;
    }

    public void setOwnerName(String owner) {
        String old = _owner;
        _owner = owner;
        if (!old.equals(owner)) {
            setDirtyAndFirePropertyChange("rolling stock owner", old, owner); // NOI18N
        }
    }

    public String getOwnerName() {
        return _owner;
    }

    /**
     * Set the rolling stock location as unknown.
     *
     * @param unknown when true, the rolling stock location is unknown.
     */
    public void setLocationUnknown(boolean unknown) {
        boolean old = _locationUnknown;
        _locationUnknown = unknown;
        if (!old == unknown) {
            setDirtyAndFirePropertyChange("car location known", old ? "true" : "false", unknown ? "true" // NOI18N
                    : "false"); // NOI18N
        }
    }

    /**
     *
     * @return true when car's location is unknown
     */
    public boolean isLocationUnknown() {
        return _locationUnknown;
    }

    /**
     * Sets the rolling stock service state. When true, rolling stock is out of
     * service. Normal state is false, the rolling stock is in service and
     * available.
     *
     * @param outOfService when true, out of service
     */
    public void setOutOfService(boolean outOfService) {
        boolean old = _outOfService;
        _outOfService = outOfService;
        if (!old == outOfService) {
            setDirtyAndFirePropertyChange("car out of service", old ? "true" : "false", outOfService ? "true" // NOI18N
                    : "false"); // NOI18N
        }
    }

    /**
     *
     * @return true when rolling stock is out of service
     */
    public boolean isOutOfService() {
        return _outOfService;
    }

    public void setSelected(boolean selected) {
        boolean old = _selected;
        _selected = selected;
        if (!old == selected) {
            setDirtyAndFirePropertyChange("selected", old ? "true" : "false", selected ? "true" // NOI18N
                    : "false"); // NOI18N
        }
    }

    /**
     *
     * @return true when rolling stock is selected
     */
    public boolean isSelected() {
        return _selected;
    }

    public void setComment(String comment) {
        String old = _comment;
        _comment = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange(COMMENT_CHANGED_PROPERTY, old, comment); // NOI18N
        }
    }

    public String getComment() {
        return _comment;
    }

    public void setPickupTime(String time) {
        String old = _pickupTime;
        _pickupTime = time;
        setDirtyAndFirePropertyChange("Pickup Time Changed", old, time); // NOI18N
    }

    public String getPickupTime() {
        if (getTrain() != null) {
            return _pickupTime;
        }
        return NONE;
    }

    public void setSetoutTime(String time) {
        String old = _setoutTime;
        _setoutTime = time;
        setDirtyAndFirePropertyChange("Setout Time Changed", old, time); // NOI18N
    }

    public String getSetoutTime() {
        if (getTrain() != null) {
            return _setoutTime;
        }
        return NONE;
    }

    protected void moveRollingStock(RouteLocation current, RouteLocation next) {
        if (current == getRouteLocation()) {
            setLastDate(java.util.Calendar.getInstance().getTime());
            // Arriving at destination?
            if (getRouteLocation() == getRouteDestination() || next == null) {
                if (getRouteLocation() == getRouteDestination()) {
                    log.debug("Rolling stock ({}) has arrived at destination ({})", this, getDestination());
                } else {
                    log.error("Rolling stock ({}) has a null route location for next", this); // NOI18N
                }
                setLocation(getDestination(), getDestinationTrack(), RollingStock.FORCE); // force RS to destination
                setDestination(null, null); // this also clears the route locations
                setLastTrain(getTrain()); // save the last train moving this rs
                setTrain(null); // this must come after setDestination (route id is set)
                setMoves(getMoves() + 1); // bump count
            } else {
                log.debug("Rolling stock ({}) is in train ({}) leaves location ({}) destination ({})", this,
                        getTrainName(), current.getName(), next.getName());
                setLocation(next.getLocation(), null, RollingStock.FORCE); // force RS to location
                setRouteLocation(next);
            }
        }
    }

    public void reset() {
        // the order of the next two instructions is important, otherwise rs will have
        // train's route id
        setTrain(null);
        setDestination(null, null);
    }

    /**
     * Remove rolling stock. Releases all listeners.
     */
    public void dispose() {
        setTrain(null);
        setDestination(null, null);
        setLocation(null, null);
        InstanceManager.getDefault(CarRoads.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarOwners.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarColors.class).removePropertyChangeListener(this);
        if (getIdTag() != null) {
            getIdTag().removePropertyChangeListener(_tagListener);
        }
    }

    /**
     * Construct this Entry from XML.
     *
     * @param e RollingStock XML element
     */
    public RollingStock(org.jdom2.Element e) {
        this();
        org.jdom2.Attribute a;
        if ((a = e.getAttribute(Xml.ID)) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in rolling stock element when reading operations");
        }
        if ((a = e.getAttribute(Xml.ROAD_NUMBER)) != null) {
            _number = a.getValue();
        }
        if ((a = e.getAttribute(Xml.ROAD_NAME)) != null) {
            _road = a.getValue();
        }
        if (_id == null || !_id.equals(createId(_road, _number))) {
            _id = createId(_road, _number);
        }
        if ((a = e.getAttribute(Xml.TYPE)) != null) {
            _type = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LENGTH)) != null) {
            _length = a.getValue();
        }
        if ((a = e.getAttribute(Xml.COLOR)) != null) {
            _color = a.getValue();
        }
        if ((a = e.getAttribute(Xml.WEIGHT)) != null) {
            _weight = a.getValue();
        }
        if ((a = e.getAttribute(Xml.WEIGHT_TONS)) != null) {
            _weightTons = a.getValue();
        }
        if ((a = e.getAttribute(Xml.BUILT)) != null) {
            _built = a.getValue();
        }
        if ((a = e.getAttribute(Xml.CLONE)) != null) {
            _clone = a.getValue().equals(Xml.TRUE);
        }
        Location location = null;
        Track track = null;
        if ((a = e.getAttribute(Xml.LOCATION_ID)) != null) {
            location = locationManager.getLocationById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.SEC_LOCATION_ID)) != null && location != null) {
            track = location.getTrackById(a.getValue());
        }
        setLocation(location, track, RollingStock.FORCE); // force location

        Location destination = null;
        track = null;
        if ((a = e.getAttribute(Xml.DESTINATION_ID)) != null) {
            destination = locationManager.getLocationById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.SEC_DESTINATION_ID)) != null && destination != null) {
            track = destination.getTrackById(a.getValue());
        }
        setDestination(destination, track, RollingStock.FORCE); // force destination

        if ((a = e.getAttribute(Xml.DIVISION_ID)) != null) {
            _division = InstanceManager.getDefault(DivisionManager.class).getDivisionById(a.getValue());
        }
        // TODO remove the following 3 lines in 2022
        if ((a = e.getAttribute(Xml.DIVISION_ID_ERROR)) != null) {
            _division = InstanceManager.getDefault(DivisionManager.class).getDivisionById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.MOVES)) != null) {
            try {
                _moves = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Move count ({}) for rollingstock ({}) isn't a valid number!", a.getValue(), toString());
            }
        }
        if ((a = e.getAttribute(Xml.LAST_LOCATION_ID)) != null) {
            _lastLocationId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LAST_TRACK_ID)) != null) {
            _lastTrackId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.TRAIN_ID)) != null) {
            setTrain(InstanceManager.getDefault(TrainManager.class).getTrainById(a.getValue()));
        } else if ((a = e.getAttribute(Xml.TRAIN)) != null) {
            setTrain(InstanceManager.getDefault(TrainManager.class).getTrainByName(a.getValue()));
        }
        if (getTrain() != null &&
                getTrain().getRoute() != null &&
                (a = e.getAttribute(Xml.ROUTE_LOCATION_ID)) != null) {
            _routeLocation = getTrain().getRoute().getRouteLocationById(a.getValue());
            if ((a = e.getAttribute(Xml.ROUTE_DESTINATION_ID)) != null) {
                _routeDestination = getTrain().getRoute().getRouteLocationById(a.getValue());
            }
        }
        if ((a = e.getAttribute(Xml.LAST_TRAIN_ID)) != null) {
            setLastTrain(InstanceManager.getDefault(TrainManager.class).getTrainById(a.getValue()));
        }
        if ((a = e.getAttribute(Xml.LAST_ROUTE_ID)) != null) {
            _routeId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.OWNER)) != null) {
            _owner = a.getValue();
        }
        if ((a = e.getAttribute(Xml.COMMENT)) != null) {
            _comment = a.getValue();
        }
        if ((a = e.getAttribute(Xml.VALUE)) != null) {
            _value = a.getValue();
        }
        if ((a = e.getAttribute(Xml.RFID)) != null) {
            setRfid(a.getValue());
        }
        if ((a = e.getAttribute(Xml.LOC_UNKNOWN)) != null) {
            _locationUnknown = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.OUT_OF_SERVICE)) != null) {
            _outOfService = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.SELECTED)) != null) {
            _selected = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.DATE)) != null) {
            setLastDate(a.getValue()); // uses the setLastDate(String) method.
        }
        if ((a = e.getAttribute(Xml.PICKUP_TIME)) != null) {
            _pickupTime = a.getValue();
        }
        if ((a = e.getAttribute(Xml.SETOUT_TIME)) != null) {
            _setoutTime = a.getValue();
        }
        if ((a = e.getAttribute(Xml.BLOCKING)) != null) {
            try {
                _blocking = Integer.parseInt(a.getValue());
            } catch (NumberFormatException nfe) {
                log.error("Blocking ({}) for rollingstock ({}) isn't a valid number!", a.getValue(), toString());
            }
        }
        // check for rolling stock without a track assignment
        if (getLocation() != null && getTrack() == null && getTrain() == null) {
            log.warn("Rollingstock ({}) at ({}) doesn't have a track assignment", this, getLocationName());
        }
        addPropertyChangeListeners();
    }

    /**
     * Add XML elements to represent this Entry.
     *
     * @param e Element for car or engine store.
     *
     * @return Contents in a JDOM Element
     */
    protected org.jdom2.Element store(org.jdom2.Element e) {
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.ROAD_NAME, getRoadName());
        e.setAttribute(Xml.ROAD_NUMBER, getNumber());
        e.setAttribute(Xml.TYPE, getTypeName());
        e.setAttribute(Xml.LENGTH, getLength());
        if (!getColor().equals(NONE)) {
            e.setAttribute(Xml.COLOR, getColor());
        }
        if (!getWeight().equals(DEFAULT_WEIGHT)) {
            e.setAttribute(Xml.WEIGHT, getWeight());
        }
        if (!getWeightTons().equals(NONE)) {
            e.setAttribute(Xml.WEIGHT_TONS, getWeightTons());
        }
        if (!getBuilt().equals(NONE)) {
            e.setAttribute(Xml.BUILT, getBuilt());
        }
        if (!getLocationId().equals(NONE)) {
            e.setAttribute(Xml.LOCATION_ID, getLocationId());
        }
        if (!getRouteLocationId().equals(NONE)) {
            e.setAttribute(Xml.ROUTE_LOCATION_ID, getRouteLocationId());
        }
        if (!getTrackId().equals(NONE)) {
            e.setAttribute(Xml.SEC_LOCATION_ID, getTrackId());
        }
        if (!getDestinationId().equals(NONE)) {
            e.setAttribute(Xml.DESTINATION_ID, getDestinationId());
        }
        if (!getRouteDestinationId().equals(NONE)) {
            e.setAttribute(Xml.ROUTE_DESTINATION_ID, getRouteDestinationId());
        }
        if (!getDestinationTrackId().equals(NONE)) {
            e.setAttribute(Xml.SEC_DESTINATION_ID, getDestinationTrackId());
        }
        if (!getDivisionId().equals(NONE)) {
            e.setAttribute(Xml.DIVISION_ID, getDivisionId());
        }
        if (!getLastRouteId().equals(NONE)) {
            e.setAttribute(Xml.LAST_ROUTE_ID, getLastRouteId());
        }
        if (isClone()) {
            e.setAttribute(Xml.CLONE, isClone() ? Xml.TRUE : Xml.FALSE);
        }
        e.setAttribute(Xml.MOVES, Integer.toString(getMoves()));
        e.setAttribute(Xml.DATE, getLastDate());
        e.setAttribute(Xml.SELECTED, isSelected() ? Xml.TRUE : Xml.FALSE);
        if (!getLastLocationId().equals(LOCATION_UNKNOWN)) {
            e.setAttribute(Xml.LAST_LOCATION_ID, getLastLocationId());
        }
        if (!getLastTrackId().equals(LOCATION_UNKNOWN)) {
            e.setAttribute(Xml.LAST_TRACK_ID, getLastTrackId());
        }
        if (!getTrainName().equals(NONE)) {
            e.setAttribute(Xml.TRAIN, getTrainName());
            e.setAttribute(Xml.TRAIN_ID, getTrain().getId());
        }
        if (!getLastTrainName().equals(NONE)) {
            e.setAttribute(Xml.LAST_TRAIN, getLastTrainName());
            e.setAttribute(Xml.LAST_TRAIN_ID, getLastTrain().getId());
        }
        if (!getOwnerName().equals(NONE)) {
            e.setAttribute(Xml.OWNER, getOwnerName());
        }
        if (!getValue().equals(NONE)) {
            e.setAttribute(Xml.VALUE, getValue());
        }
        if (!getRfid().equals(NONE)) {
            e.setAttribute(Xml.RFID, getRfid());
        }
        if (isLocationUnknown()) {
            e.setAttribute(Xml.LOC_UNKNOWN, isLocationUnknown() ? Xml.TRUE : Xml.FALSE);
        }
        if (isOutOfService()) {
            e.setAttribute(Xml.OUT_OF_SERVICE, isOutOfService() ? Xml.TRUE : Xml.FALSE);
        }
        if (!getPickupTime().equals(NONE)) {
            e.setAttribute(Xml.PICKUP_TIME, getPickupTime());
        }
        if (!getSetoutTime().equals(NONE)) {
            e.setAttribute(Xml.SETOUT_TIME, getSetoutTime());
        }
        if (getBlocking() != 0) {
            e.setAttribute(Xml.BLOCKING, Integer.toString(getBlocking()));
        }
        if (!getComment().equals(NONE)) {
            e.setAttribute(Xml.COMMENT, getComment());
        }
        return e;
    }

    private void addPropertyChangeListeners() {
        InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarOwners.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarColors.class).addPropertyChangeListener(this);
    }

    // rolling stock listens for changes in a location name or if a location is
    // deleted
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        // log.debug("Property change for rolling stock: " + toString()+ " property
        // name: "
        // +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
        // notify if track or location name changes
        if (e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY)) {
            log.debug("Property change for rolling stock: ({}) property name: ({}) old: ({}) new: ({})", this,
                    e.getPropertyName(), e.getOldValue(), e.getNewValue());
            setDirtyAndFirePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
        }
        if (e.getPropertyName().equals(Location.DISPOSE_CHANGED_PROPERTY)) {
            if (e.getSource() == getLocation()) {
                log.debug("delete location for rolling stock: ({})", this);
                setLocation(null, null);
            }
            if (e.getSource() == getDestination()) {
                log.debug("delete destination for rolling stock: ({})", this);
                setDestination(null, null);
            }
        }
        if (e.getPropertyName().equals(Track.DISPOSE_CHANGED_PROPERTY)) {
            if (e.getSource() == getTrack()) {
                log.debug("delete location for rolling stock: ({})", this);
                setLocation(getLocation(), null);
            }
            if (e.getSource() == getDestinationTrack()) {
                log.debug("delete destination for rolling stock: ({})", this);
                setDestination(getDestination(), null);
            }
        }
        if (e.getPropertyName().equals(Train.DISPOSE_CHANGED_PROPERTY) && e.getSource() == getTrain()) {
            log.debug("delete train for rolling stock: ({})", this);
            setTrain(null);
        }
        if (e.getPropertyName().equals(Train.TRAIN_LOCATION_CHANGED_PROPERTY) && e.getSource() == getTrain()) {
            log.debug("Rolling stock ({}) is serviced by train ({})", this, getTrainName());
            moveRollingStock((RouteLocation) e.getOldValue(), (RouteLocation) e.getNewValue());
        }
        if (e.getPropertyName().equals(Train.STATUS_CHANGED_PROPERTY) &&
                e.getNewValue().equals(Train.TRAIN_RESET) &&
                e.getSource() == getTrain()) {
            log.debug("Rolling stock ({}) is removed from train ({}) by reset", this, getTrainName()); // NOI18N
            reset();
        }
        if (e.getPropertyName().equals(Train.NAME_CHANGED_PROPERTY)) {
            setDirtyAndFirePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
        }
        if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getRoadName())) {
                log.debug("Rolling stock ({}) sees road name change from ({}) to ({})", this, e.getOldValue(),
                        e.getNewValue()); // NOI18N
                if (e.getNewValue() != null) {
                    setRoadName((String) e.getNewValue());
                }
            }
        }
        if (e.getPropertyName().equals(CarOwners.CAROWNERS_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getOwnerName())) {
                log.debug("Rolling stock ({}) sees owner name change from ({}) to ({})", this, e.getOldValue(),
                        e.getNewValue()); // NOI18N
                setOwnerName((String) e.getNewValue());
            }
        }
        if (e.getPropertyName().equals(CarColors.CARCOLORS_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getColor())) {
                log.debug("Rolling stock ({}) sees color name change from ({}) to ({})", this, e.getOldValue(),
                        e.getNewValue()); // NOI18N
                setColor((String) e.getNewValue());
            }
        }
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(RollingStock.class);

}
