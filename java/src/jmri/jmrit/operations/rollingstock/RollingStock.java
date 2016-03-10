package jmri.jmrit.operations.rollingstock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents rolling stock, both powered (locomotives) and not powered (cars)
 * on the layout.
 *
 * @author Daniel Boudreau Copyright (C) 2009, 2010, 2013
 * @version $Revision$
 */
public class RollingStock implements java.beans.PropertyChangeListener {

    public static final String NONE = "";
    public static final int DEFAULT_BLOCKING_ORDER = 0;
    protected static final String DEFAULT_WEIGHT = "0";

    protected String _id = NONE;
    protected String _number = NONE;
    protected String _road = NONE;
    protected String _type = NONE;
    protected String _length = NONE;
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
    protected Track _trackLocation = null;
    protected Location _destination = null;
    protected Track _trackDestination = null;
    protected Train _train = null;
    protected RouteLocation _routeLocation = null;
    protected RouteLocation _routeDestination = null;
    protected int _moves = 0;
    protected String _lastLocationId = LOCATION_UNKNOWN; // the rollingstock's last location id
    protected int _blocking = DEFAULT_BLOCKING_ORDER;

    public static final String LOCATION_UNKNOWN = "0";

    protected int number = 0; // used by rolling stock manager for sort by number

    public static final String ERROR_TRACK = "ERROR wrong track for location"; // checks for coding error // NOI18N

    public static final String LOCATION_CHANGED_PROPERTY = "rolling stock location"; // NOI18N
    public static final String TRACK_CHANGED_PROPERTY = "rolling stock track location"; // NOI18N
    public static final String DESTINATION_CHANGED_PROPERTY = "rolling stock destination"; // NOI18N
    public static final String DESTINATION_TRACK_CHANGED_PROPERTY = "rolling stock track destination"; // NOI18N
    public static final String TRAIN_CHANGED_PROPERTY = "rolling stock train"; // NOI18N
    public static final String LENGTH_CHANGED_PROPERTY = "rolling stock length"; // NOI18N
    public static final String TYPE_CHANGED_PROPERTY = "rolling stock type"; // NOI18N
    public static final String ROUTE_LOCATION_CHANGED_PROPERTY = "rolling stock route location"; // NOI18N
    public static final String ROUTE_DESTINATION_CHANGED_PROPERTY = "rolling stock route destination"; // NOI18N

    // the draw bar length must only be calculated once at startup
    public static final int COUPLER = Setup.getLengthUnit().equals(Setup.FEET) ? Integer.parseInt(Bundle
            .getMessage("DrawBarLengthFeet")) : Integer.parseInt(Bundle.getMessage("DrawBarLengthMeter")); // stocks

    LocationManager locationManager = LocationManager.instance();

    public RollingStock() {
        _lastDate = (new java.util.GregorianCalendar()).getGregorianChange(); // set to change date of the Gregorian Calendar. 
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
        String id = road + number;
        return id;
    }

    public String getId() {
        return _id;
    }

    /**
     * Set the rolling stock identification or road number
     *
     * @param number
     */
    public void setNumber(String number) {
        String old = _number;
        _number = number;
        if (!old.equals(number)) {
            setDirtyAndFirePropertyChange("rolling stock number", old, number); // NOI18N
        }
    }

    public String getNumber() {
        return _number;
    }

    public void setRoadName(String road) {
        String old = _road;
        _road = road;
        if (!old.equals(road)) {
            setDirtyAndFirePropertyChange("rolling stock road", old, road); // NOI18N
        }
    }

    public String getRoadName() {
        return _road;
    }

    /**
     * For combobox and identification
     */
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

    @Deprecated
    // saved for scripts
    public String getType() {
        return getTypeName();
    }

    protected boolean _lengthChange = false; // used for loco length change

    /*
     * Sets the length of the rolling stock.
     * 
     * @param length
     */
    public void setLength(String length) {
        String old = _length;
        if (!old.equals(length)) {
            // adjust used length if rolling stock is at a location
            if (_location != null && _trackLocation != null) {
                _location.setUsedLength(_location.getUsedLength() + Integer.parseInt(length) - Integer.parseInt(old));
                _trackLocation.setUsedLength(_trackLocation.getUsedLength() + Integer.parseInt(length)
                        - Integer.parseInt(old));
                if (_destination != null && _trackDestination != null && !_lengthChange) {
                    _lengthChange = true; // prevent recursive loop, and we want the "old" loco length
                    log.debug("Rolling stock ({}) has destination ({}, {})", toString(), _destination.getName(),
                            _trackDestination.getName());
                    _trackLocation.deletePickupRS(this);
                    _trackDestination.deleteDropRS(this);
                    // now change the length and update tracks
                    _length = length;
                    _trackLocation.addPickupRS(this);
                    _trackDestination.addDropRS(this);
                    _lengthChange = false; // done
                }
            }
            _length = length;
            setDirtyAndFirePropertyChange(LENGTH_CHANGED_PROPERTY, old, length);
        }
    }

    public String getLength() {
        return _length;
    }

    public int getLengthInteger() {
        try {
            return Integer.parseInt(getLength());
        } catch (Exception e) {
            log.error("Rolling stock ({}) length ({}) is not valid ", toString(), getLength());
        }
        return 0;
    }

    /**
     * Returns the length of the rolling stock including the couplers
     *
     * @return total length of the rolling stock
     */
    public int getTotalLength() {
        return getLengthInteger() + RollingStock.COUPLER;
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

        double weight = 0;
        try {
            weight = Double.parseDouble(getWeight());
        } catch (Exception e) {
            // log.debug("Weight not set for rolling stock ("+toString()+")");
        }
        return Integer.toString((int) (weight * Setup.getScaleTonRatio()));
    }

    public int getAdjustedWeightTons() {
        int weightTons = 0;
        try {
            // get loaded weight
            weightTons = Integer.parseInt(getWeightTons());
        } catch (Exception e) {
            log.debug("Rolling stock ({}) weight not set", toString());
        }
        return weightTons;
    }

    /**
     * Set the date that the rolling stock was built. Use 4 digits for the year,
     * or the format MM-YY where MM is the two digit month, and YY is the last
     * two years if the rolling stock was built in the 1900s. Use MM-YYYY for
     * units build after 1999.
     *
     * @param built
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
     * @return location unknown symbol, out of service symbol, or none if car
     *         okay
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
        if (_location != null) {
            return _location.getName();
        }
        return NONE;
    }

    /**
     * Get rolling stock's location id
     *
     * @return empty string if rolling stock isn't on the layout
     */
    public String getLocationId() {
        if (_location != null) {
            return _location.getId();
        }
        return NONE;
    }

    public Track getTrack() {
        return _trackLocation;
    }

    /**
     * Set the rolling stock's location and track. Doesn't do any checking and
     * does not fire a property change. Used exclusively by the Router code. Use
     * setLocation(Location, Track) instead.
     *
     * @param track to place the rolling stock on.
     */
    public void setTrack(Track track) {
        if (track != null) {
            _location = track.getLocation();
        }
        _trackLocation = track;
    }

    /**
     * Get rolling stock's track name
     *
     * @return empty string if rolling stock isn't on a track
     */
    public String getTrackName() {
        if (_trackLocation != null) {
            return _trackLocation.getName();
        }
        return NONE;
    }

    /**
     * Get rolling stock's track id
     *
     * @return empty string if rolling stock isn't on a track
     */
    public String getTrackId() {
        if (_trackLocation != null) {
            return _trackLocation.getId();
        }
        return NONE;
    }

    /**
     * Sets rolling stock location on the layout
     *
     * @param location
     * @param track (yard, spur, staging, or interchange track)
     *
     * @return "okay" if successful, "type" if the rolling stock's type isn't
     *         acceptable, or "length" if the rolling stock length didn't fit.
     */
    public String setLocation(Location location, Track track) {
        return setLocation(location, track, false);
    }

    /**
     * Sets rolling stock location on the layout
     *
     * @param location
     * @param track (yard, spur, staging, or interchange track)
     * @param force when true place rolling stock ignore track length, type, &
     *            road
     * @return "okay" if successful, "type" if the rolling stock's type isn't
     *         acceptable, "road" if rolling stock road isn't acceptable, or
     *         "length" if the rolling stock length didn't fit.
     */
    public String setLocation(Location location, Track track, boolean force) {
        // first determine if rolling stock can be move to the new location
        if (!force) {
            String status = testLocation(location, track);
            if (!status.equals(Track.OKAY)) {
                return status;
            }
        }
        // now update
        Location oldLocation = _location;
        _location = location;
        Track oldTrack = _trackLocation;
        _trackLocation = track;

        if (oldLocation != location || oldTrack != track) {
            // update rolling stock location on layout, maybe this should be a property change?
            // first remove rolling stock from existing location
            if (oldLocation != null) {
                oldLocation.deleteRS(this);
                oldLocation.removePropertyChangeListener(this);
                // if track is null, then rolling stock is in a train
                if (oldTrack != null) {
                    oldTrack.deleteRS(this);
                    oldTrack.removePropertyChangeListener(this);
                    // if there's a destination then pickup complete
                    if (_destination != null) {
                        oldLocation.deletePickupRS();
                        oldTrack.deletePickupRS(this);
                        // don't update rs's previous location if just re-staging
                        if (getTrain() != null && getTrain().getRoute() != null && getTrain().getRoute().size() > 2) {
                            setLastLocationId(oldLocation.getId());
                        }
                    }
                }
            }
            if (_location != null) {
                _location.addRS(this);
                // Need to know if location name changes so we can forward to listeners
                _location.addPropertyChangeListener(this);
            }
            if (_trackLocation != null) {
                _trackLocation.addRS(this);
                // Need to know if location name changes so we can forward to listeners
                _trackLocation.addPropertyChangeListener(this);
                // if there's a destination then there's a pick up
                if (_destination != null) {
                    _location.addPickupRS();
                    _trackLocation.addPickupRS(this);
                }
            }
            setDirtyAndFirePropertyChange(LOCATION_CHANGED_PROPERTY, oldLocation, location);
            setDirtyAndFirePropertyChange(TRACK_CHANGED_PROPERTY, oldTrack, track);
        }
        return Track.OKAY;
    }

    public String testLocation(Location location, Track track) {
        if (track == null) {
            return Track.OKAY;
        }
        if (location != null && !location.isTrackAtLocation(track)) {
            return ERROR_TRACK;
        }
        return track.accepts(this);
    }

    /**
     * Sets rolling stock destination on the layout
     *
     * @param destination
     * @param track (yard, spur, staging, or interchange track)
     * @return "okay" if successful, "type" if the rolling stock's type isn't
     *         acceptable, or "length" if the rolling stock length didn't fit.
     */
    public String setDestination(Location destination, Track track) {
        return setDestination(destination, track, false);
    }

    /**
     * Sets rolling stock destination on the layout
     *
     * @param destination
     * @param track (yard, spur, staging, or interchange track)
     * @param force when true ignore track length, type, & road when setting
     *            destination
     * @return "okay" if successful, "type" if the rolling stock's type isn't
     *         acceptable, or "length" if the rolling stock length didn't fit.
     */
    public String setDestination(Location destination, Track track, boolean force) {
        // first determine if rolling stock can be move to the new destination
        if (!force) {
            String status = rsTestDestination(destination, track);
            if (!status.equals(Track.OKAY)) {
                return status;
            }
        }
        // now set the rolling stock destination!
        Location oldDestination = _destination;
        _destination = destination;
        Track oldTrack = _trackDestination;
        _trackDestination = track;

        if (oldDestination != destination || oldTrack != track) {
            if (oldDestination != null) {
                oldDestination.deleteDropRS();
                oldDestination.removePropertyChangeListener(this);
                // delete pick up in case destination is null
                if (_location != null && _trackLocation != null) {
                    _location.deletePickupRS();
                    _trackLocation.deletePickupRS(this);
                }
            }
            if (oldTrack != null) {
                oldTrack.deleteDropRS(this);
                oldTrack.removePropertyChangeListener(this);
            }
            if (_destination != null) {
                _destination.addDropRS();
                if (_location != null && _trackLocation != null) {
                    _location.addPickupRS();
                    _trackLocation.addPickupRS(this);
                }

                // Need to know if destination name changes so we can forward to listeners
                _destination.addPropertyChangeListener(this);
            }
            if (_trackDestination != null) {
                _trackDestination.addDropRS(this);
                // Need to know if destination name changes so we can forward to listeners
                _trackDestination.addPropertyChangeListener(this);
            } else {
                // rolling stock has been terminated or reset, bump rolling stock moves
                if (getTrain() != null && getTrain().getRoute() != null) {
                    setSavedRouteId(getTrain().getRoute().getId());
                }
                if (getRouteDestination() != null) {
                    setMoves(getMoves() + 1);
                    setLastDate(java.util.Calendar.getInstance().getTime());
                }
                setRouteLocation(null);
                setRouteDestination(null);
            }

            setDirtyAndFirePropertyChange(DESTINATION_CHANGED_PROPERTY, oldDestination, destination);
            setDirtyAndFirePropertyChange(DESTINATION_TRACK_CHANGED_PROPERTY, oldTrack, track);
        }
        return Track.OKAY;
    }

    /**
     * Used to check destination track to see if it will accept rolling stock
     *
     * @param destination
     * @param track
     * @return status OKAY, TYPE, ROAD, LENGTH, ERROR_TRACK
     */
    public String testDestination(Location destination, Track track) {
        return rsTestDestination(destination, track);
    }

    private String rsTestDestination(Location destination, Track track) {
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
        return track.accepts(this);
    }

    public Location getDestination() {
        return _destination;
    }

    /**
     * Sets rolling stock destination without reserving destination track space
     * or drop count. Does not fire a property change. Used by car router to
     * test destinations. Use setDestination(Location, Track) instead.
     *
     * @param destination for the rolling stock
     */
    public void setDestination(Location destination) {
        _destination = destination;
    }

    public String getDestinationName() {
        if (_destination != null) {
            return _destination.getName();
        }
        return NONE;
    }

    public String getDestinationId() {
        if (_destination != null) {
            return _destination.getId();
        }
        return NONE;
    }

    /**
     * Sets rolling stock destination track without reserving destination track
     * space or drop count. Used by car router to test destinations. Does not
     * fire a property change. Use setDestination(Location, Track) instead.
     *
     * @param track
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
        if (_trackDestination != null) {
            return _trackDestination.getName();
        }
        return NONE;
    }

    public String getDestinationTrackId() {
        if (_trackDestination != null) {
            return _trackDestination.getId();
        }
        return NONE;
    }

    /**
     * Used to block cars from staging
     *
     * @param id The location id from where the car came from before going into
     *            staging.
     */
    public void setLastLocationId(String id) {
        _lastLocationId = id;
    }

    public String getLastLocationId() {
        return _lastLocationId;
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
     * @param train
     */
    public void setTrain(Train train) {
        Train old = _train;
        _train = train;
        if ((old != null && !old.equals(train)) || old != train) {
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
     * Sets the location where the rolling stock will be picked up by the train.
     *
     * @param routeLocation the pick up location for this rolling stock.
     */
    public void setRouteLocation(RouteLocation routeLocation) {
        // a couple of error checks before setting the route location
        if (_location == null && routeLocation != null) {
            log.debug("WARNING rolling stock ({}) does not have an assigned location", toString()); // NOI18N
        } else if (routeLocation != null && _location != null && !routeLocation.getName().equals(_location.getName())) {
            log.error("ERROR route location name({}) not equal to location name ({}) for rolling stock ({})",
                    routeLocation.getName(), _location.getName(), toString()); // NOI18N
        }
        RouteLocation old = _routeLocation;
        _routeLocation = routeLocation;
        if (old != routeLocation) {
            setDirtyAndFirePropertyChange(ROUTE_LOCATION_CHANGED_PROPERTY, old, routeLocation);
        }
    }

    public RouteLocation getRouteLocation() {
        return _routeLocation;
    }

    public String getRouteLocationId() {
        if (_routeLocation != null) {
            return _routeLocation.getId();
        }
        return NONE;
    }

    public String getSavedRouteId() {
        return _routeId;
    }

    /**
     * Sets the id of the route that was used to set out the rolling stock. Used
     * to determine if the rolling stock can be pick ups from an interchange
     * track.
     *
     * @param id The route id.
     */
    public void setSavedRouteId(String id) {
        _routeId = id;
    }

    public String getValue() {
        return _value;
    }

    /**
     * Sets the value (cost, price) for this rolling stock. Currently has
     * nothing to do with operations. But nice to have.
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

    private IdTag _tag = null;
    private PropertyChangeListener _tagListener = null;

    public String getRfid() {
        return _rfid;
    }

    public IdTag getIdTag() {
        return _tag;
    }

    public void setIdTag(IdTag tag) {
        if (_tag != null)
            _tag.removePropertyChangeListener(_tagListener);
        _tag = tag;
        if (_tagListener == null) {
            // store the tag listener so we can reuse it and 
            // dispose of it as necessary.
            _tagListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("whereLastSeen")) {
                        log.debug("Tag Reader Position update received for {}", toString());
                        // update the position of this piece of rolling
                        // stock when it's IdTag is seen, but only if 
                        // the actual location changes.
                        if (e.getNewValue() != null) {
                            Location newLocation = locationManager.getLocationByReporter((jmri.Reporter) e.getNewValue());
                            if (newLocation != getLocation())
                                setLocation(newLocation, null);
                        }
                    }
                    if (e.getPropertyName().equals("whenLastSeen")) {
                        log.debug("Tag Reader Time at Location update received for {}", toString());
                        // update the time when this car was last moved
                        // stock when it's IdTag is seen, but only if 
                        // the actual location changes.
                        if (e.getNewValue() != null) {
                            Date newDate = ((Date) e.getNewValue());
                            setLastDate(newDate);
                        }
                    }
                }
            };
        }
        if (_tag != null)
            _tag.addPropertyChangeListener(_tagListener);
    }

    /**
     * Sets the RFID for this rolling stock.
     * 
     * @param id 12 character RFID string.
     */
    public void setRfid(String id) {
        String old = _rfid;
        _rfid = id;
        log.debug("Changing IdTag for {} to {}", toString(), id);
        if (!old.equals(id))
            setDirtyAndFirePropertyChange("rolling stock rfid", old, id); // NOI18N
        try {
            IdTag tag = InstanceManager.getDefault(IdTagManager.class).getIdTag(id.toUpperCase());
            log.debug("Tag {} Found", tag.toString());
            setIdTag(tag);
        } catch (NullPointerException e) {
            log.error("Tag {} Not Found", id);
        }
    }

    /**
     * Provides the last date when this rolling stock was moved, or was reset
     * from a built train, as a string.
     *
     * @return date
     */
    public String getLastDate() {
        if (_lastDate.equals((new java.util.GregorianCalendar()).getGregorianChange()))
            return NONE; // return an empty string for the default date.
        SimpleDateFormat format =
                new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return format.format(_lastDate);
    }

    /**
     * Provides the last date when this rolling stock was moved, or was reset
     * from a built train.
     *
     * @return date
     */
    public Date getLastMoveDate() {
        return _lastDate;
    }

    /**
     * Sets the last date when this rolling stock was moved, or was reset from a
     * built train. This method is used only for loading data from a file. Use
     * setLastDate(Date) instead.
     *
     * @param date MM/dd/yyyy HH:mm:ss
     */
    private void setLastDate(String date) {
        if (date == NONE)
            return; // there was no date specified.
        Date oldDate = _lastDate;
        // create a date object from the value.
        try {
            // try the new format (with seconds).
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            _lastDate = formatter.parse(date);
        } catch (java.text.ParseException pe0) {
            // try the old 12 hour format (no seconds).
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mmaa");
                _lastDate = formatter.parse(date);
            } catch (java.text.ParseException pe1) {
                try {
                    // try 24hour clock.
                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                    _lastDate = formatter.parse(date);
                } catch (java.text.ParseException pe2) {
                    log.warn("Not able to parse date: {} for rolling stock ({})", date, toString());
                    _lastDate = oldDate; // set the date back to what it was before
                }
            }
        }
    }

    /**
     * Sets the last date when this rolling stock was moved, or was reset from a
     * built train.
     *
     * @param date
     */
    public void setLastDate(Date date) {
        Date old = _lastDate;
        _lastDate = date;
        if (!old.equals(_lastDate)) {
            setDirtyAndFirePropertyChange("rolling stock date", old, date); // NOI18N
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
     * @param routeDestination the location where the rolling stock is to leave
     *            the train.
     */
    public void setRouteDestination(RouteLocation routeDestination) {
        if (routeDestination != null && _destination != null
                && !routeDestination.getName().equals(_destination.getName())) {
            log.debug("WARNING route destination name ({}) not equal to destination name ({}) for rolling stock ({})",
                    routeDestination.getName(), _destination.getName(), toString()); // NOI18N
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
        if (_routeDestination != null) {
            return _routeDestination.getId();
        }
        return NONE;
    }

    public void setOwner(String owner) {
        String old = _owner;
        _owner = owner;
        if (!old.equals(owner)) {
            setDirtyAndFirePropertyChange("rolling stock owner", old, owner); // NOI18N
        }
    }

    public String getOwner() {
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
            setDirtyAndFirePropertyChange("rolling stock comment", old, comment); // NOI18N
        }
    }

    public String getComment() {
        return _comment;
    }

    protected void moveRollingStock(RouteLocation current, RouteLocation next) {
        if (current == getRouteLocation()) {
            // Arriving at destination?
            if (getRouteLocation() == getRouteDestination() || next == null) {
                if (getRouteLocation() == getRouteDestination()) {
                    log.debug("Rolling stock ({}) has arrived at destination ({})", toString(), getDestination());
                } else {
                    log.error("Rolling stock ({}) has a null route location for next", toString()); // NOI18N
                }
                setLocation(getDestination(), getDestinationTrack(), true); // force RS to destination
                setDestination(null, null); // this also clears the route locations
                setTrain(null); // this must come after setDestination (route id is set)
            } else {
                log.debug("Rolling stock ({}) is in train ({}) leaves location ({}) destination ({})", toString(),
                        getTrainName(), current.getName(), next.getName());
                setLocation(next.getLocation(), null, true); // force RS to location
                setRouteLocation(next);
            }
        }
    }

    public void reset() {
        // the order of the next two instructions is important, otherwise rs will have train's route id
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
        CarRoads.instance().removePropertyChangeListener(this);
        CarOwners.instance().removePropertyChangeListener(this);
        CarColors.instance().removePropertyChangeListener(this);
        if (_tag != null) {
            _tag.removePropertyChangeListener(_tagListener);
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

        Location location = null;
        Track track = null;
        if ((a = e.getAttribute(Xml.LOCATION_ID)) != null) {
            location = locationManager.getLocationById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.SEC_LOCATION_ID)) != null && location != null) {
            track = location.getTrackById(a.getValue());
        }
        setLocation(location, track, true); // force location

        Location destination = null;
        track = null;
        if ((a = e.getAttribute(Xml.DESTINATION_ID)) != null) {
            destination = locationManager.getLocationById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.SEC_DESTINATION_ID)) != null && destination != null) {
            track = destination.getTrackById(a.getValue());
        }
        setDestination(destination, track, true); // force destination

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
        if ((a = e.getAttribute(Xml.TRAIN_ID)) != null) {
            setTrain(TrainManager.instance().getTrainById(a.getValue()));
        } else if ((a = e.getAttribute(Xml.TRAIN)) != null) {
            setTrain(TrainManager.instance().getTrainByName(a.getValue()));
        }
       if (getTrain() != null && getTrain().getRoute() != null &&
                (a = e.getAttribute(Xml.ROUTE_LOCATION_ID)) != null) {
            _routeLocation = getTrain().getRoute().getLocationById(a.getValue());
            if ((a = e.getAttribute(Xml.ROUTE_DESTINATION_ID)) != null) {
                _routeDestination = getTrain().getRoute().getLocationById(a.getValue());
            }
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
        if ((a = e.getAttribute(Xml.BLOCKING)) != null) {
            _blocking = Integer.parseInt(a.getValue());
        }
        // check for rolling stock without a track assignment
        if (getLocation() != null && getTrack() == null && getTrain() == null) {
            log.warn("Rollingstock ({}) at ({}) doesn't have a track assignment", toString(), getLocationName());
        }
        addPropertyChangeListeners();
    }

    boolean verboseStore = false;

    /**
     * Add XML elements to represent this Entry.
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
        if (!getSavedRouteId().equals(NONE)) {
            e.setAttribute(Xml.LAST_ROUTE_ID, getSavedRouteId());
        }
        if (verboseStore) {
            e.setAttribute(Xml.LOCATION, getLocationName());
            e.setAttribute(Xml.TRACK, getTrackName());
            e.setAttribute(Xml.DESTINATION, getDestinationName());
            e.setAttribute(Xml.DES_TRACK, getDestinationTrackName());
        }
        e.setAttribute(Xml.MOVES, Integer.toString(getMoves()));
        e.setAttribute(Xml.DATE, getLastDate());
        e.setAttribute(Xml.SELECTED, isSelected() ? Xml.TRUE : Xml.FALSE);
        if (!getLastLocationId().equals(LOCATION_UNKNOWN)) {
            e.setAttribute(Xml.LAST_LOCATION_ID, getLastLocationId());
        }
        if (!getTrainName().equals(NONE)) {
            e.setAttribute(Xml.TRAIN, getTrainName());
            e.setAttribute(Xml.TRAIN_ID, getTrain().getId());
        }
        if (!getOwner().equals(NONE)) {
            e.setAttribute(Xml.OWNER, getOwner());
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
        if (getBlocking() != 0) {
            e.setAttribute(Xml.BLOCKING, Integer.toString(getBlocking()));
        }
        if (!getComment().equals(NONE)) {
            e.setAttribute(Xml.COMMENT, getComment());
        }
        return e;
    }

    private void addPropertyChangeListeners() {
        CarRoads.instance().addPropertyChangeListener(this);
        CarOwners.instance().addPropertyChangeListener(this);
        CarColors.instance().addPropertyChangeListener(this);
    }

    // rolling stock listens for changes in a location name or if a location is deleted
    public void propertyChange(PropertyChangeEvent e) {
        // if (log.isDebugEnabled()) log.debug("Property change for rolling stock: " + toString()+ " property name: "
        // +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
        // notify if track or location name changes
        if (e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY)) {
            if (log.isDebugEnabled()) {
                log.debug("Property change for rolling stock: ({}) property name: ({}) old: ({}) new: ({})",
                        toString(), e.getPropertyName(), e.getOldValue(), e.getNewValue());
            }
            setDirtyAndFirePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
        }
        if (e.getPropertyName().equals(Location.DISPOSE_CHANGED_PROPERTY)) {
            if (e.getSource() == _location) {
                if (log.isDebugEnabled()) {
                    log.debug("delete location for rolling stock: ({})", toString());
                }
                setLocation(null, null);
            }
            if (e.getSource() == _destination) {
                if (log.isDebugEnabled()) {
                    log.debug("delete destination for rolling stock: ({})", toString());
                }
                setDestination(null, null);
            }
        }
        if (e.getPropertyName().equals(Track.DISPOSE_CHANGED_PROPERTY)) {
            if (e.getSource() == _trackLocation) {
                if (log.isDebugEnabled()) {
                    log.debug("delete location for rolling stock: ({})", toString());
                }
                setLocation(_location, null);
            }
            if (e.getSource() == _trackDestination) {
                if (log.isDebugEnabled()) {
                    log.debug("delete destination for rolling stock: ({})", toString());
                }
                setDestination(_destination, null);
            }
        }
        if (e.getPropertyName().equals(Train.DISPOSE_CHANGED_PROPERTY) && e.getSource() == getTrain()) {
            if (log.isDebugEnabled()) {
                log.debug("delete train for rolling stock: ({})", toString());
            }
            setTrain(null);
        }
        if (e.getPropertyName().equals(Train.TRAIN_LOCATION_CHANGED_PROPERTY) && e.getSource() == getTrain()) {
            if (log.isDebugEnabled()) {
                log.debug("Rolling stock ({}) is serviced by train ({})", toString(), getTrainName());
            }
            moveRollingStock((RouteLocation) e.getOldValue(), (RouteLocation) e.getNewValue());
        }
        if (e.getPropertyName().equals(Train.STATUS_CHANGED_PROPERTY) && e.getNewValue().equals(Train.TRAIN_RESET)
                && e.getSource() == getTrain()) {
            if (log.isDebugEnabled()) {
                log.debug("Rolling stock ({}) is removed from train ({}) by reset", toString(), getTrainName()); // NOI18N
            }
            reset();
        }
        if (e.getPropertyName().equals(Train.NAME_CHANGED_PROPERTY)) {
            setDirtyAndFirePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
        }
        if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getRoadName())) {
                if (log.isDebugEnabled()) {
                    log.debug("Rolling stock ({}) sees road name change from ({}) to ({})", toString(),
                            e.getOldValue(), e.getNewValue()); // NOI18N
                }
                if (e.getNewValue() != null) {
                    setRoadName((String) e.getNewValue());
                }
            }
        }
        if (e.getPropertyName().equals(CarOwners.CAROWNERS_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getOwner())) {
                if (log.isDebugEnabled()) {
                    log.debug("Rolling stock ({}) sees owner name change from ({}) to ({})", toString(), e
                            .getOldValue(), e.getNewValue()); // NOI18N
                }
                setOwner((String) e.getNewValue());
            }
        }
        if (e.getPropertyName().equals(CarColors.CARCOLORS_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getColor())) {
                if (log.isDebugEnabled()) {
                    log.debug("Rolling stock ({}) sees color name change from ({}) to ({})", toString(), e
                            .getOldValue(), e.getNewValue()); // NOI18N
                }
                setColor((String) e.getNewValue());
            }
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
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(RollingStock.class.getName());

}
