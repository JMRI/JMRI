package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainSchedule;
import jmri.jmrit.operations.trains.TrainScheduleManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Represents a location (track) on the layout Can be a spur, yard, staging, or interchange track.
 * 
 * @author Daniel Boudreau Copyright (C) 2008 - 2014
 * @version $Revision$
 */
public class Track {

	protected String _id = "";
	protected String _name = "";
	protected String _trackType = ""; // yard, spur, interchange or staging
	protected Location _location; // the location for this track
	protected String _alternateTrackId = ""; // the alternate track id
	protected String _roadOption = ALL_ROADS; // controls which car roads are accepted
	protected int _trainDir = EAST + WEST + NORTH + SOUTH; // train direction served by this track
	protected int _numberRS = 0; // number of cars and engines
	protected int _numberCars = 0; // number of cars
	protected int _numberEngines = 0; // number of engines
	protected int _pickupRS = 0; // number of pick ups by trains
	protected int _dropRS = 0; // number of set outs by trains
	protected int _length = 0; // length of track
	protected int _reserved = 0; // length of track reserved by trains
	protected int _reservedLengthDrops = 0; // length of track reserved for drops
	protected int _numberCarsInRoute = 0; // number of cars in route to this track
	protected int _usedLength = 0; // length of track filled by cars and engines
	protected int _ignoreUsedLengthPercentage = 0; // value between 0 and 100, 100 = ignore 100%
	protected int _moves = 0; // count of the drops since creation
	protected String _comment = "";
	
	protected String _commentPickup = "";
	protected String _commentSetout = "";
	protected String _commentBoth = "";

	protected String _loadOption = ALL_LOADS; // receive track load restrictions
	protected String _shipLoadOption = ALL_LOADS; // ship track load restrictions
	
	protected String _destinationOption = ALL_DESTINATIONS; // track destination restriction

	// schedule options
	protected String _scheduleName = ""; // Schedule name if there's one
	protected String _scheduleId = ""; // Schedule id if there's one
	protected String _scheduleItemId = ""; // the current scheduled item id
	protected int _scheduleCount = 0; // the number of times the item has been delivered
	protected int _reservedInRoute = 0; // length of cars in route to this track
	protected int _reservationFactor = 100; // percentage of track space for cars in route
	protected int _mode = MATCH;	// default is match mode

	// drop options
	protected String _dropOption = ANY; // controls which route or train can set out cars
	protected String _pickupOption = ANY; // controls which route or train can pick up cars
	public static final String ANY = "Any"; // track accepts any train or route
	public static final String TRAINS = "trains"; // NOI18N track only accepts certain trains
	public static final String ROUTES = "routes"; // NOI18N track only accepts certain routes
	public static final String EXCLUDE_TRAINS = "excludeTrains"; // NOI18N track excludes certain trains
	public static final String EXCLUDE_ROUTES = "excludeRoutes"; // NOI18N track excludes certain routes

	// load options
	protected int _loadOptions = 0;
	private static final int SWAP_GENERIC_LOADS = 1;
	private static final int EMPTY_CUSTOM_LOADS = 2;
	private static final int GENERATE_CUSTOM_LOADS = 4;
	private static final int GENERATE_CUSTOM_LOADS_ANY_SPUR = 8;
	private static final int EMPTY_GENERIC_LOADS = 16;
	private static final int GENERATE_CUSTOM_LOADS_ANY_STAGING_TRACK = 32;

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
	public static final String SPUR = "Siding";	// NOI18N note that early code used Siding as the spur type

	// train directions serviced by this track
	public static final int EAST = 1;
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;

	// how roads are serviced by this track
	public static final String ALL_ROADS = Bundle.getMessage("All"); // track accepts all roads
	public static final String INCLUDE_ROADS = Bundle.getMessage("Include"); // track accepts only certain roads
	public static final String EXCLUDE_ROADS = Bundle.getMessage("Exclude"); // track does not accept certain roads

	// load options
	public static final String ALL_LOADS = Bundle.getMessage("All"); // track services all loads
	public static final String INCLUDE_LOADS = Bundle.getMessage("Include");
	public static final String EXCLUDE_LOADS = Bundle.getMessage("Exclude");
	
	// destination options
	public static final String ALL_DESTINATIONS = Bundle.getMessage("All"); // track services all loads
	public static final String INCLUDE_DESTINATIONS = Bundle.getMessage("Include");
	public static final String EXCLUDE_DESTINATIONS = Bundle.getMessage("Exclude");

	// schedule modes
	public static final int SEQUENTIAL = 0;
	public static final int MATCH = 1;

	// pool
	protected Pool _pool = null;
	protected int _minimumLength = 0;

	// return status when checking rolling stock
	public static final String OKAY = Bundle.getMessage("okay");
	public static final String LENGTH = Bundle.getMessage("length");
	public static final String TYPE = Bundle.getMessage("type");
	public static final String ROAD = Bundle.getMessage("road");
	public static final String LOAD = Bundle.getMessage("load");
//	public static final String CAPACITY = Bundle.getMessage("capacity");
	public static final String SCHEDULE = Bundle.getMessage("schedule");
	public static final String CUSTOM = Bundle.getMessage("custom");
	public static final String DESTINATION = Bundle.getMessage("carDestination");

	// For property change
	public static final String TYPES_CHANGED_PROPERTY = "trackRollingStockTypes"; // NOI18N
	public static final String ROADS_CHANGED_PROPERTY = "trackRoads"; // NOI18N
	public static final String NAME_CHANGED_PROPERTY = "trackName"; // NOI18N
	public static final String LENGTH_CHANGED_PROPERTY = "trackLength"; // NOI18N
	public static final String MIN_LENGTH_CHANGED_PROPERTY = "trackMinLength"; // NOI18N
	public static final String SCHEDULE_CHANGED_PROPERTY = "trackScheduleChange"; // NOI18N
	public static final String DISPOSE_CHANGED_PROPERTY = "trackDispose"; // NOI18N
	public static final String TRAINDIRECTION_CHANGED_PROPERTY = "trackTrainDirection"; // NOI18N
	public static final String DROP_CHANGED_PROPERTY = "trackDrop"; // NOI18N
	public static final String PICKUP_CHANGED_PROPERTY = "trackPickup"; // NOI18N
	public static final String TRACK_TYPE_CHANGED_PROPERTY = "trackType"; // NOI18N
	public static final String LOADS_CHANGED_PROPERTY = "trackLoads"; // NOI18N
	public static final String POOL_CHANGED_PROPERTY = "trackPool"; // NOI18N
	public static final String PLANNEDPICKUPS_CHANGED_PROPERTY = "plannedPickUps"; // NOI18N
	public static final String LOAD_OPTIONS_CHANGED_PROPERTY = "trackLoadOptions"; // NOI18N
	public static final String DESTINATIONS_CHANGED_PROPERTY = "trackDestinations"; // NOI18N
	public static final String DESTINATION_OPTIONS_CHANGED_PROPERTY = "trackDestinationOptions"; // NOI18N
	public static final String SCHEDULE_MODE_CHANGED_PROPERTY = "trackScheduleMode"; // NOI18N
	public static final String SCHEDULE_ID_CHANGED_PROPERTY = "trackScheduleId"; // NOI18N
	public static final String SERVICE_ORDER_CHANGED_PROPERTY = "trackServiceOrder"; // NOI18N

	public Track(String id, String name, String type, Location location) {
		log.debug("New ({}) track ({}) id: {}", type, name, id);
		_location = location;
		_trackType = type;
		_name = name;
		_id = id;
		// a new track accepts all types
		setTypeNames(CarTypes.instance().getNames());
		setTypeNames(EngineTypes.instance().getNames());
	}
	
	/**
	 * Creates a copy of this track.
	 * @param newName The name of the new track.
	 * @param newLocation The location of the new track.
	 * @return Track
	 */
	public Track copyTrack(String newName, Location newLocation) {
		Track newTrack = newLocation.addTrack(newName, getTrackType());
		newTrack.clearTypeNames();		// all types are accepted by a new track
		
		newTrack.setAddCustomLoadsAnySpurEnabled(isAddCustomLoadsAnySpurEnabled());
		newTrack.setAddCustomLoadsAnyStagingTrackEnabled(isAddCustomLoadsAnyStagingTrackEnabled());
		newTrack.setAddCustomLoadsEnabled(isAddCustomLoadsEnabled());

		newTrack.setAlternateTrack(getAlternateTrack());
		newTrack.setBlockCarsEnabled(isBlockCarsEnabled());
		newTrack.setComment(getComment());
		newTrack.setCommentBoth(getCommentBoth());
		newTrack.setCommentPickup(getCommentPickup());
		newTrack.setCommentSetout(getCommentSetout());
		
		newTrack.setDestinationOption(getDestinationOption());
		newTrack.setDestinationIds(getDestinationIds());
		
		newTrack.setDropOption(getDropOption());	// must set option before setting ids
		newTrack.setDropIds(getDropIds());
		
		newTrack.setIgnoreUsedLengthPercentage(getIgnoreUsedLengthPercentage());
		newTrack.setLength(getLength());
		newTrack.setLoadEmptyEnabled(isLoadEmptyEnabled());
		newTrack.setLoadNames(getLoadNames());
		newTrack.setLoadOption(getLoadOption());
		newTrack.setLoadSwapEnabled(isLoadSwapEnabled());

		newTrack.setPickupOption(getPickupOption());	// must set option before setting ids
		newTrack.setPickupIds(getPickupIds());
		
		// track pools are only shared within a specific location
		if (getPool() != null) {
			newTrack.setPool(newLocation.addPool(getPool().getName()));
			newTrack.setMinimumLength(getMinimumLength());
		}
		newTrack.setRemoveCustomLoadsEnabled(isRemoveCustomLoadsEnabled());
		newTrack.setReservationFactor(getReservationFactor());
		newTrack.setRoadNames(getRoadNames());
		newTrack.setRoadOption(getRoadOption());
		newTrack.setScheduleId(getScheduleId());
		newTrack.setScheduleMode(getScheduleMode());
		newTrack.setServiceOrder(getServiceOrder());
		newTrack.setShipLoadNames(getShipLoadNames());
		newTrack.setShipLoadOption(getShipLoadOption());
		newTrack.setTrainDirections(getTrainDirections());
		newTrack.setTypeNames(getTypeNames());
		return newTrack;
	}

	// for combo boxes
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
			setDirtyAndFirePropertyChange(NAME_CHANGED_PROPERTY, old, name);
		}
	}

	public String getName() {
		return _name;
	}

	/**
	 * Gets the track type
	 * @return Track.SPUR Track.YARD Track.INTERCHANGE or Track.STAGING
	 */
	public String getTrackType() {
		return _trackType;
	}
	
	/**
	 * Sets the track type, spur, interchange, yard, staging
	 * @param type Track.SPUR Track.YARD Track.INTERCHANGE Track.STAGING
	 */
	public void setTrackType(String type) {
		String old = _trackType;
		_trackType = type;
		if (!old.equals(type))
			setDirtyAndFirePropertyChange(TRACK_TYPE_CHANGED_PROPERTY, old, type);
	}
	
	public String getTrackTypeName() {
		if (getTrackType().equals(Track.SPUR))
			return Bundle.getMessage("Spur").toLowerCase();
		if (getTrackType().equals(Track.YARD))
			return Bundle.getMessage("Yard").toLowerCase();
		if (getTrackType().equals(Track.INTERCHANGE))
			return Bundle.getMessage("Class/Interchange");	// this is an abbreviation
		if (getTrackType().equals(Track.STAGING))
			return Bundle.getMessage("Staging").toLowerCase();
		return ("unknown");
	}

	@Deprecated // as of 10/27/2013 version 3.5.5
	public void setLocType(String type) {
		setTrackType(type);
	}

	public void setLength(int length) {
		int old = _length;
		_length = length;
		if (old != length)
			setDirtyAndFirePropertyChange(LENGTH_CHANGED_PROPERTY, Integer.toString(old),
					Integer.toString(length));
	}

	public int getLength() {
		return _length;
	}

	/**
	 * Sets the minimum length of this track when the track is in a pool.
	 * @param length minimum 
	 */
	public void setMinimumLength(int length) {
		int old = _minimumLength;
		_minimumLength = length;
		if (old != length)
			setDirtyAndFirePropertyChange(MIN_LENGTH_CHANGED_PROPERTY, Integer.toString(old),
					Integer.toString(length));
	}

	public int getMinimumLength() {
		return _minimumLength;
	}

	public void setReserved(int reserved) {
		int old = _reserved;
		_reserved = reserved;
		if (old != reserved)
			setDirtyAndFirePropertyChange("reserved", Integer.toString(old),	// NOI18N
					Integer.toString(reserved)); // NOI18N
	}

	public int getReserved() {
		return _reserved;
	}

	public void addReservedInRoute(Car car) {
		int old = _reservedInRoute;
		_numberCarsInRoute++;
		_reservedInRoute = old + car.getTotalLength();
		if (old != _reservedInRoute)
			setDirtyAndFirePropertyChange("reservedInRoute", Integer.toString(old),	// NOI18N
					Integer.toString(_reservedInRoute)); // NOI18N
	}

	public void deleteReservedInRoute(Car car) {
		int old = _reservedInRoute;
		_numberCarsInRoute--;
		_reservedInRoute = old - car.getTotalLength();
		if (old != _reservedInRoute)
			setDirtyAndFirePropertyChange("reservedInRoute", Integer.toString(old),	// NOI18N
					Integer.toString(_reservedInRoute)); // NOI18N
	}

	/**
	 * Used to determine how much track space is going to be consumed by cars in route to this track. See
	 * isSpaceAvailable().
	 * 
	 * @return The length of all cars in route to this track including couplers.
	 */
	public int getReservedInRoute() {
		return _reservedInRoute;
	}

	public int getNumberOfCarsInRoute() {
		return _numberCarsInRoute;
	}

	/**
	 * Set the reservation factor. Default 100 (100%). Used by the program when generating car loads from staging. A
	 * factor of 100% allows the program to fill a track with car loads. Numbers over 100% can overload a track.
	 * 
	 * @param factor
	 *            A number from 0 to 10000.
	 */
	public void setReservationFactor(int factor) {
		int old = _reservationFactor;
		_reservationFactor = factor;
		if (old != factor)
			setDirtyAndFirePropertyChange("reservationFactor", old, factor); // NOI18N
	}

	public int getReservationFactor() {
		return _reservationFactor;
	}

	/**
	 * Sets the mode of operation for the schedule assigned to this track.
	 * 
	 * @param mode
	 *            Track.SEQUENTIAL or Track.MATCH
	 */
	public void setScheduleMode(int mode) {
		int old = _mode;
		_mode = mode;
		if (old != mode)
			setDirtyAndFirePropertyChange(SCHEDULE_MODE_CHANGED_PROPERTY, old, mode); // NOI18N
	}

	/**
	 * Gets the mode of operation for the schedule assigned to this track.
	 * 
	 * @return Mode of operation: Track.SEQUENTIAL or Track.MATCH
	 */
	public int getScheduleMode() {
		return _mode;
	}

	public void setAlternateTrack(Track track) {
		Track oldTrack = _location.getTrackById(_alternateTrackId);
		String old = _alternateTrackId;
		if (track != null)
			_alternateTrackId = track.getId();
		else
			_alternateTrackId = "";
		if (!old.equals(_alternateTrackId))
			setDirtyAndFirePropertyChange("alternateTrack", oldTrack, track); // NOI18N
	}

	public Track getAlternateTrack() {
		return _location.getTrackById(_alternateTrackId);
	}

	/**
	 * Used to determine if there's space available at this track for the car. Considers cars in route to this track.
	 * Used to prevent overloading the track with cars from staging or cars with custom loads.
	 * 
	 * @param car
	 *            The car to be set out.
	 * @return true if space available.
	 */
	public boolean isSpaceAvailable(Car car) {
		int carLength = car.getTotalLength();
		if (car.getKernel() != null)
			carLength = car.getKernel().getTotalLength();
		// ignore reservation factor unless car is departing staging
		if (car.getTrack() != null && car.getTrack().getTrackType().equals(STAGING))
			return (getLength() * getReservationFactor() / 100 - (getReservedInRoute() + carLength) >= 0);
		// if there's alternate, include that length in the calculation
		int trackLength = getLength();
		if (getAlternateTrack() != null)
			trackLength = trackLength + getAlternateTrack().getLength();
		return (trackLength - (getReservedInRoute() + carLength) >= 0);
	}

	public void setUsedLength(int length) {
		int old = _usedLength;
		_usedLength = length;
		if (old != length)
			setDirtyAndFirePropertyChange("usedLength", Integer.toString(old),	// NOI18N
					Integer.toString(length)); // NOI18N
	}

	public int getUsedLength() {
		return _usedLength;
	}

	/**
	 * The amount of consumed track space to be ignored when sending new rolling stock to the track.
	 * 
	 * @param percentage
	 *            a number between 0 and 100
	 */
	public void setIgnoreUsedLengthPercentage(int percentage) {
		int old = _ignoreUsedLengthPercentage;
		_ignoreUsedLengthPercentage = percentage;
		if (old != percentage)
			setDirtyAndFirePropertyChange(PLANNEDPICKUPS_CHANGED_PROPERTY, Integer.toString(old),
					Integer.toString(percentage));
	}

	public int getIgnoreUsedLengthPercentage() {
		return _ignoreUsedLengthPercentage;
	}

	/**
	 * Sets the number of rolling stock (cars and or engines) on this track
	 * 
	 * @param number
	 */
	private void setNumberRS(int number) {
		int old = _numberRS;
		_numberRS = number;
		if (old != number)
			setDirtyAndFirePropertyChange("numberRS", Integer.toString(old),	// NOI18N
					Integer.toString(number)); // NOI18N
	}

	/**
	 * Sets the number of cars on this track
	 * 
	 * @param number
	 */
	private void setNumberCars(int number) {
		int old = _numberCars;
		_numberCars = number;
		if (old != number)
			setDirtyAndFirePropertyChange("numberCars", Integer.toString(old),	// NOI18N
					Integer.toString(number)); // NOI18N
	}

	/**
	 * Sets the number of engines on this track
	 * 
	 * @param number
	 */
	private void setNumberEngines(int number) {
		int old = _numberEngines;
		_numberEngines = number;
		if (old != number)
			setDirtyAndFirePropertyChange("numberEngines", Integer.toString(old),	// NOI18N
					Integer.toString(number)); // NOI18N
	}

	/**
	 * 
	 * @return The number of rolling stock (cars and engines) on this track
	 */
	public int getNumberRS() {
		return _numberRS;
	}

	/**
	 * 
	 * @return The number of cars on this track
	 */
	public int getNumberCars() {
		return _numberCars;
	}

	/**
	 * 
	 * @return The number of engines on this track
	 */
	public int getNumberEngines() {
		return _numberEngines;
	}

	/**
	 * Adds rolling stock to a specific track.
	 * 
	 * @param rs
	 */
	public void addRS(RollingStock rs) {
		setNumberRS(getNumberRS() + 1);
		if (rs.getClass() == Car.class)
			setNumberCars(getNumberCars() + 1);
		else if (rs.getClass() == Engine.class)
			setNumberEngines(getNumberEngines() + 1);
		setUsedLength(getUsedLength() + rs.getTotalLength());
	}

	public void deleteRS(RollingStock rs) {
		setNumberRS(getNumberRS() - 1);
		if (rs.getClass() == Car.class)
			setNumberCars(getNumberCars() - 1);
		else if (rs.getClass() == Engine.class)
			setNumberEngines(getNumberEngines() - 1);
		setUsedLength(getUsedLength() - rs.getTotalLength());
	}

	/**
	 * Increments the number of cars and or engines that will be picked up by a train from this track.
	 */
	public void addPickupRS(RollingStock rs) {
		int old = _pickupRS;
		_pickupRS++;
		if (Setup.isBuildAggressive())
			setReserved(getReserved() - rs.getTotalLength());
		setDirtyAndFirePropertyChange("pickupRS", Integer.toString(old), // NOI18N
				Integer.toString(_pickupRS));
	}

	public void deletePickupRS(RollingStock rs) {
		int old = _pickupRS;
		if (Setup.isBuildAggressive())
			setReserved(getReserved() + rs.getTotalLength());
		_pickupRS--;
		setDirtyAndFirePropertyChange("pickupRS", Integer.toString(old), // NOI18N
				Integer.toString(_pickupRS));
	}

	/**
	 * 
	 * @return the number of rolling stock (cars and or locos) that are scheduled for pick up from this track.
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
		setMoves(getMoves() + 1);
		setReserved(getReserved() + rs.getTotalLength());
		_reservedLengthDrops = _reservedLengthDrops + rs.getTotalLength();
		setDirtyAndFirePropertyChange("addDropRS", Integer.toString(old), Integer.toString(_dropRS)); // NOI18N
	}

	public void deleteDropRS(RollingStock rs) {
		int old = _dropRS;
		_dropRS--;
		setReserved(getReserved() - rs.getTotalLength());
		_reservedLengthDrops = _reservedLengthDrops - rs.getTotalLength();
		setDirtyAndFirePropertyChange("deleteDropRS", Integer.toString(old), // NOI18N
				Integer.toString(_dropRS));
	}

	public void setComment(String comment) {
		String old = _comment;
		_comment = comment;
		if (!old.equals(comment))
			setDirtyAndFirePropertyChange("trackComment", old, comment); // NOI18N
	}

	public String getComment() {
		return _comment;
	}
	
	public void setCommentPickup(String comment) {
		String old = _commentPickup;
		_commentPickup = comment;
		if (!old.equals(comment))
			setDirtyAndFirePropertyChange("trackCommentPickup", old, comment); // NOI18N
	}

	public String getCommentPickup() {
		return _commentPickup;
	}
	
	public void setCommentSetout(String comment) {
		String old = _commentSetout;
		_commentSetout = comment;
		if (!old.equals(comment))
			setDirtyAndFirePropertyChange("trackCommentSetout", old, comment); // NOI18N
	}

	public String getCommentSetout() {
		return _commentSetout;
	}
	
	public void setCommentBoth(String comment) {
		String old = _commentBoth;
		_commentBoth = comment;
		if (!old.equals(comment))
			setDirtyAndFirePropertyChange("trackCommentBoth", old, comment); // NOI18N
	}

	public String getCommentBoth() {
		return _commentBoth;
	}

	List<String> _typeList = new ArrayList<String>();

	/**
	 * Returns all of the rolling stock type names serviced by this track.
	 * 
	 * @return rolling stock type names
	 */
	public String[] getTypeNames() {
		String[] types = new String[_typeList.size()];
		for (int i = 0; i < _typeList.size(); i++)
			types[i] = _typeList.get(i);
		return types;
	}

	private void setTypeNames(String[] types) {
		if (types.length == 0)
			return;
		jmri.util.StringUtil.sort(types);
		for (int i = 0; i < types.length; i++)
			if (!_typeList.contains(types[i]))
				_typeList.add(types[i]);
	}
	
	private void clearTypeNames() {
		_typeList.clear();
	}

	public void addTypeName(String type) {
		// insert at start of list, sort later
		if (type == null || _typeList.contains(type))
			return;
		_typeList.add(0, type);
		log.debug("track (" + getName() + ") add rolling stock type " + type);
		setDirtyAndFirePropertyChange(TYPES_CHANGED_PROPERTY, _typeList.size() - 1,
				_typeList.size());
	}

	public void deleteTypeName(String type) {
		if (!_typeList.contains(type))
			return;
		_typeList.remove(type);
		log.debug("track (" + getName() + ") delete rolling stock type " + type);
		setDirtyAndFirePropertyChange(TYPES_CHANGED_PROPERTY, _typeList.size() + 1,
				_typeList.size());
	}

	public boolean acceptsTypeName(String type) {
		if (!_location.acceptsTypeName(type))
			return false;
		return _typeList.contains(type);
	}

	/**
	 * Sets the train directions that can service this track
	 * 
	 * @param direction
	 *            EAST, WEST, NORTH, SOUTH
	 */
	public void setTrainDirections(int direction) {
		int old = _trainDir;
		_trainDir = direction;
		if (old != direction)
			setDirtyAndFirePropertyChange(TRAINDIRECTION_CHANGED_PROPERTY, Integer.toString(old),
					Integer.toString(direction));
	}

	public int getTrainDirections() {
		return _trainDir;
	}

	public String getRoadOption() {
		return _roadOption;
	}

	/**
	 * Set the road option for this track.
	 * 
	 * @param option
	 *            ALLROADS, INCLUDEROADS, or EXCLUDEROADS
	 */
	public void setRoadOption(String option) {
		String old = _roadOption;
		_roadOption = option;
		setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, old, option);
	}
	List<String> _roadList = new ArrayList<String>();

	public String[] getRoadNames() {
		String[] roads = new String[_roadList.size()];
		for (int i = 0; i < _roadList.size(); i++)
			roads[i] = _roadList.get(i);
		if (_roadList.size() == 0)
			return roads;
		jmri.util.StringUtil.sort(roads);
		return roads;
	}

	private void setRoadNames(String[] roads) {
		if (roads.length == 0)
			return;
		jmri.util.StringUtil.sort(roads);
		for (int i = 0; i < roads.length; i++) {
			if (!roads[i].equals(""))
				_roadList.add(roads[i]);
		}
	}

	public void addRoadName(String road) {
		if (_roadList.contains(road))
			return;
		_roadList.add(road);
		log.debug("track (" + getName() + ") add car road " + road);
		setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, _roadList.size() - 1,
				_roadList.size());
	}

	public void deleteRoadName(String road) {
		_roadList.remove(road);
		log.debug("track (" + getName() + ") delete car road " + road);
		setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, _roadList.size() + 1,
				_roadList.size());
	}

	public boolean acceptsRoadName(String road) {
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

	/**
	 * Set how this track deals with receiving car loads
	 * 
	 * @param option
	 *            ALL_LOADS INCLUDE_LOADS EXCLUDE_LOADS
	 */
	public void setLoadOption(String option) {
		String old = _loadOption;
		_loadOption = option;
		setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, old, option);
	}

	List<String> _loadList = new ArrayList<String>();

	private void setLoadNames(String[] loads) {
		if (loads.length == 0)
			return;
		jmri.util.StringUtil.sort(loads);
		for (int i = 0; i < loads.length; i++) {
			if (!loads[i].equals(""))
				_loadList.add(loads[i]);
		}
	}

	/**
	 * Provides a list of receive loads that the track will either service or exclude. See setLoadOption
	 * 
	 * @return Array of load names as Strings
	 */
	public String[] getLoadNames() {
		String[] loads = new String[_loadList.size()];
		for (int i = 0; i < _loadList.size(); i++)
			loads[i] = _loadList.get(i);
		if (_loadList.size() == 0)
			return loads;
		jmri.util.StringUtil.sort(loads);
		return loads;
	}

	/**
	 * Add a receive load that the track will either service or exclude. See setLoadOption
	 * 
	 * @return true if load name was added, false if load name wasn't in the list.
	 */
	public boolean addLoadName(String load) {
		if (_loadList.contains(load))
			return false;
		_loadList.add(load);
		log.debug("track (" + getName() + ") add car load " + load);
		setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, _loadList.size() - 1,
				_loadList.size());
		return true;
	}

	/**
	 * Delete a receive load name that the track will either service or exclude. See setLoadOption
	 * 
	 * @return true if load name was removed, false if load name wasn't in the list.
	 */
	public boolean deleteLoadName(String load) {
		if (!_loadList.contains(load))
			return false;
		_loadList.remove(load);
		log.debug("track (" + getName() + ") delete car load " + load);
		setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, _loadList.size() + 1,
				_loadList.size());
		return true;
	}

	/**
	 * Determine if track will service a specific receive load name.
	 * 
	 * @param load
	 *            the load name to check.
	 * @return true if track will service this load.
	 */
	public boolean acceptsLoadName(String load) {
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
	 * @param load
	 *            the load name to check.
	 * @param type
	 *            the type of car used to carry the load.
	 * @return true if track will service this load.
	 */
	public boolean acceptsLoad(String load, String type) {
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
		return _shipLoadOption;
	}

	/**
	 * Set how this track deals with shipping car loads
	 * 
	 * @param option
	 *            ALL_LOADS INCLUDE_LOADS EXCLUDE_LOADS
	 */
	public void setShipLoadOption(String option) {
		String old = _shipLoadOption;
		_shipLoadOption = option;
		setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, old, option);
	}

	List<String> _shipLoadList = new ArrayList<String>();

	private void setShipLoadNames(String[] loads) {
		if (loads.length == 0)
			return;
		jmri.util.StringUtil.sort(loads);
		for (int i = 0; i < loads.length; i++) {
			if (!loads[i].equals(""))
				_shipLoadList.add(loads[i]);
		}
	}

	/**
	 * Provides a list of ship loads that the track will either service or exclude. See setShipLoadOption
	 * 
	 * @return Array of load names as Strings
	 */
	public String[] getShipLoadNames() {
		String[] loads = new String[_shipLoadList.size()];
		for (int i = 0; i < _shipLoadList.size(); i++)
			loads[i] = _shipLoadList.get(i);
		if (_shipLoadList.size() == 0)
			return loads;
		jmri.util.StringUtil.sort(loads);
		return loads;
	}

	/**
	 * Add a ship load that the track will either service or exclude. See setShipLoadOption
	 * 
	 * @return true if load name was added, false if load name wasn't in the list.
	 */
	public boolean addShipLoadName(String load) {
		if (_shipLoadList.contains(load))
			return false;
		_shipLoadList.add(load);
		log.debug("track (" + getName() + ") add car load " + load);
		setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, _shipLoadList.size() - 1,
				_shipLoadList.size());
		return true;
	}

	/**
	 * Delete a ship load name that the track will either service or exclude. See setLoadOption
	 * 
	 * @return true if load name was removed, false if load name wasn't in the list.
	 */
	public boolean deleteShipLoadName(String load) {
		if (!_shipLoadList.contains(load))
			return false;
		_shipLoadList.remove(load);
		log.debug("track (" + getName() + ") delete car load " + load);
		setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, _shipLoadList.size() + 1,
				_shipLoadList.size());
		return true;
	}

	/**
	 * Determine if track will service a specific ship load name.
	 * 
	 * @param load
	 *            the load name to check.
	 * @return true if track will service this load.
	 */
	public boolean shipsLoadName(String load) {
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
	 * @param load
	 *            the load name to check.
	 * @param type
	 *            the type of car used to carry the load.
	 * @return true if track will service this load.
	 */
	public boolean shipsLoad(String load, String type) {
		if (_shipLoadOption.equals(ALL_LOADS)) {
			return true;
		}
		if (_shipLoadOption.equals(INCLUDE_LOADS)) {
			return _shipLoadList.contains(load) || _shipLoadList.contains(type + CarLoad.SPLIT_CHAR + load);
		}
		// exclude!
		return !_shipLoadList.contains(load) && !_shipLoadList.contains(type + CarLoad.SPLIT_CHAR + load);
	}


	public String getDropOption() {
		return _dropOption;
	}

	/**
	 * Set the car drop option for this track.
	 * 
	 * @param option
	 *            ANY, TRAINS, or ROUTES
	 */
	public void setDropOption(String option) {
		String old = _dropOption;
		_dropOption = option;
		if (!old.equals(option))
			_dropList.clear();
		setDirtyAndFirePropertyChange(DROP_CHANGED_PROPERTY, old, option);
	}

	public String getPickupOption() {
		return _pickupOption;
	}

	/**
	 * Set the car pick up option for this track.
	 * 
	 * @param option
	 *            ANY, TRAINS, or ROUTES
	 */
	public void setPickupOption(String option) {
		String old = _pickupOption;
		_pickupOption = option;
		if (!old.equals(option))
			_pickupList.clear();
		setDirtyAndFirePropertyChange(PICKUP_CHANGED_PROPERTY, old, option);
	}

	List<String> _dropList = new ArrayList<String>();

	public String[] getDropIds() {
		String[] ids = new String[_dropList.size()];
		for (int i = 0; i < _dropList.size(); i++)
			ids[i] = _dropList.get(i);
		return ids;
	}

	private void setDropIds(String[] ids) {
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] != null)
				_dropList.add(ids[i]);
		}
	}

	public void addDropId(String id) {
		if (_dropList.contains(id))
			return;
		_dropList.add(id);
		log.debug("track " + getName() + " add drop id " + id);
		setDirtyAndFirePropertyChange(DROP_CHANGED_PROPERTY, null, id);
	}

	public void deleteDropId(String id) {
		_dropList.remove(id);
		log.debug("track " + getName() + " delete drop id " + id);
		setDirtyAndFirePropertyChange(DROP_CHANGED_PROPERTY, id, null);
	}

	/**
	 * Determine if train can set out cars to this track. Based on the train's id or train's route id. See
	 * setDropOption(option).
	 * 
	 * @param train
	 * @return true if the train can set out cars to this track.
	 */
	public boolean acceptsDropTrain(Train train) {
		if (_dropOption.equals(ANY))
			return true;
		// yard tracks accept all trains
		if (getTrackType().equals(YARD))
			return true;
		if (_dropOption.equals(TRAINS))
			return containsDropId(train.getId());
		if (_dropOption.equals(EXCLUDE_TRAINS))
			return !containsDropId(train.getId());
		else if (train.getRoute() == null)
			return false;
		return acceptsDropRoute(train.getRoute());
	}

	public boolean acceptsDropRoute(Route route) {
		if (_dropOption.equals(ANY) || _dropOption.equals(TRAINS)
				|| _dropOption.equals(EXCLUDE_TRAINS))
			return true;
		// yard tracks accept all routes
		if (getTrackType().equals(YARD))
			return true;
		if (_dropOption.equals(EXCLUDE_ROUTES))
			return !containsDropId(route.getId());
		return containsDropId(route.getId());
	}

	public boolean containsDropId(String id) {
		return _dropList.contains(id);
	}

	List<String> _pickupList = new ArrayList<String>();

	public String[] getPickupIds() {
		String[] ids = new String[_pickupList.size()];
		for (int i = 0; i < _pickupList.size(); i++)
			ids[i] = _pickupList.get(i);
		return ids;
	}

	private void setPickupIds(String[] ids) {
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] != null)
				_pickupList.add(ids[i]);
		}
	}

	/**
	 * Add train or route id to this track.
	 * 
	 * @param id
	 */
	public void addPickupId(String id) {
		if (_pickupList.contains(id))
			return;
		_pickupList.add(id);
		log.debug("track " + getName() + " add pick up id " + id);
		setDirtyAndFirePropertyChange(PICKUP_CHANGED_PROPERTY, null, id);
	}

	public void deletePickupId(String id) {
		_pickupList.remove(id);
		log.debug("track " + getName() + " delete pick up id " + id);
		setDirtyAndFirePropertyChange(PICKUP_CHANGED_PROPERTY, id, null);
	}

	/**
	 * Determine if train can pick up cars from this track. Based on the train's id or train's route id. See
	 * setPickupOption(option).
	 * 
	 * @param train
	 * @return true if the train can pick up cars from this track.
	 */
	public boolean acceptsPickupTrain(Train train) {
		if (_pickupOption.equals(ANY))
			return true;
		// yard tracks accept all trains
		if (getTrackType().equals(YARD))
			return true;
		if (_pickupOption.equals(TRAINS))
			return containsPickupId(train.getId());
		if (_pickupOption.equals(EXCLUDE_TRAINS))
			return !containsPickupId(train.getId());
		else if (train.getRoute() == null)
			return false;
		return acceptsPickupRoute(train.getRoute());
	}

	public boolean acceptsPickupRoute(Route route) {
		if (_pickupOption.equals(ANY) || _pickupOption.equals(TRAINS)
				|| _pickupOption.equals(EXCLUDE_TRAINS))
			return true;
		// yard tracks accept all routes
		if (getTrackType().equals(YARD))
			return true;
		if (_pickupOption.equals(EXCLUDE_ROUTES))
			return !containsPickupId(route.getId());
		return containsPickupId(route.getId());
	}

	public boolean containsPickupId(String id) {
		return _pickupList.contains(id);
	}

	
	/**
	 * Used to determine if track can service the rolling stock.
	 * @param rs the car or loco to be tested
	 * @return TYPE, ROAD, LENGTH, OKAY
	 */
	public String accepts(RollingStock rs) {
		// first determine if rolling stock can be move to the new location
		if (!acceptsTypeName(rs.getTypeName())) {
			log.debug("Rolling stock (" + rs.toString() + ") type (" + rs.getTypeName()
					+ ") not accepted at location (" + getLocation().getName() + ", " + getName() // NOI18N
					+ ") wrong type"); // NOI18N
			return TYPE + " (" + rs.getTypeName() + ")";
		}
		if (!acceptsRoadName(rs.getRoadName())) {
			log.debug("Rolling stock (" + rs.toString() + ") road (" + rs.getRoadName()
					+ ") not accepted at location (" + getLocation().getName() + ", " + getName() // NOI18N
					+ ") wrong road"); // NOI18N
			return ROAD + " (" + rs.getRoadName() + ")";
		}
		// now determine if there's enough space for the rolling stock
		int length = 0;
		try {
			length = Integer.parseInt(rs.getLength()) + RollingStock.COUPLER;
		} catch (Exception e) {
			return LENGTH + " (" + rs.getLength() + ")";
		}
		// @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="BC_UNCONFIRMED_CAST")
		if (Car.class.isInstance(rs)) {
			Car car = (Car) rs;
			// does this track service the car's final destination?
			if (!acceptsDestination(car.getFinalDestination())) {
//					&& getLocation() != car.getFinalDestination()) { // 4/14/2014 I can't remember why this was needed
				return DESTINATION + " (" + car.getFinalDestinationName() + ") "
						+ MessageFormat.format(Bundle.getMessage("carIsNotAllowed"), new Object[] { getName() }); // no
			}
		// check for car in kernel
			if (car.getKernel() != null && car.getKernel().isLead(car)) {
				length = 0;
				List<Car> cars = car.getKernel().getCars();
				for (int i = 0; i < cars.size(); i++) {
					Car c = cars.get(i);
					// don't add length for cars already on this track or already going to this track
					if (c.getTrack() != null && c.getTrack().equals(this)
							|| c.getDestinationTrack() != null
							&& c.getDestinationTrack().equals(this))
						continue;
					length = length + Integer.parseInt(c.getLength()) + RollingStock.COUPLER;
				}
			}
			if (!acceptsLoad(car.getLoadName(), car.getTypeName())) {
				log.debug("Car  (" + rs.toString() + ") load (" + car.getLoadName()
						+ ") not accepted at location (" + getLocation().getName() + ", " // NOI18N
						+ getName() + ") wrong load"); // NOI18N
				return LOAD + " (" + car.getLoadName() + ")";
			}
		}
		// check for loco in consist
		// @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="BC_UNCONFIRMED_CAST")
		if (Engine.class.isInstance(rs)) {
			Engine eng = (Engine) rs;
			if (eng.getConsist() != null && eng.getConsist().isLead(eng)) {
				length = 0;
				List<Engine> engines = eng.getConsist().getEngines();
				for (int i = 0; i < engines.size(); i++) {
					Engine e = engines.get(i);
					// don't add length for locos already on this track or already going to this track
					if (e.getTrack() != null && e.getTrack().equals(this)
							|| e.getDestinationTrack() != null
							&& e.getDestinationTrack().equals(this))
						continue;
					length = length + Integer.parseInt(e.getLength()) + RollingStock.COUPLER;
				}
			}
		}
		if (rs.getTrack() != this && rs.getDestinationTrack() != this
				&& (getUsedLength() + getReserved() + length) > getLength()) {
			// not enough track length check to see if track is in a pool
			if (getPool() != null && getPool().requestTrackLength(this, length))
				return OKAY;
			// ignore used length option?
			if (checkPlannedPickUps(length))
					return OKAY;
			// Note that a lot of the code checks for track length being an issue, therefore it has to be the last
			// check.
			log.debug("Rolling stock (" + rs.toString() + ") not accepted at location (" + getLocation().getName()
					+ ", " + getName() + ") no room!"); // NOI18N
			return LENGTH + " (" + length + ") " + Setup.getLengthUnit().toLowerCase();// NOI18N
		}
		// a spur with a schedule can overload in aggressive mode, check track capacity
//		if (Setup.isBuildAggressive() && !getScheduleId().equals("") && getUsedLength() + getReserved() > getLength()) {
//			// ignore used length option?
//			if (checkPlannedPickUps(length))
//				return OKAY;
//			log.debug("Can't set (" + rs.toString() + ") due to exceeding maximum capacity for track (" + getName()
//					+ ")"); // NOI18N
//			return CAPACITY;
//		}
		return OKAY;
	}
	
	private boolean checkPlannedPickUps(int length) {
		if (getIgnoreUsedLengthPercentage() > 0) {
			int consumed = getUsedLength() * (100 - getIgnoreUsedLengthPercentage());
			if (consumed > 0)
				consumed = consumed / 100;
			// two checks, number of inbound cars can't exceed the track length, and the total number of cars can't
			// exceed track length plus the number of cars to ignore.
			if (consumed + _reservedLengthDrops + length <= getLength()
					&& getUsedLength() + getReserved() + length <= getLength()
							+ (getLength() * getIgnoreUsedLengthPercentage() / 100))
				return true;
		}
		return false;
	}

	public int getMoves() {
		return _moves;
	}

	public void setMoves(int moves) {
		int old = _moves;
		_moves = moves;
		setDirtyAndFirePropertyChange("trackMoves", old, moves); // NOI18N
	}

	/**
	 * Get the service order for this track. Only yards and interchange have this feature.
	 * 
	 * @return Service order: Track.NORMAL, Track.FIFO, Track.LIFO
	 */
	public String getServiceOrder() {
		if (getTrackType().equals(SPUR) || getTrackType().equals(STAGING))
			return NORMAL;
		return _order;
	}

	public void setServiceOrder(String order) {
		String old = _order;
		_order = order;
		setDirtyAndFirePropertyChange(SERVICE_ORDER_CHANGED_PROPERTY, old, order); // NOI18N
	}

	/**
	 * Returns the name of the schedule. Note that this returns the schedule name based on the schedule's id. A
	 * schedule's name can be modified by the user.
	 * 
	 * @return Schedule name
	 */
	public String getScheduleName() {
		if (getScheduleId().equals(""))
			return "";
		Schedule schedule = getSchedule();
		if (schedule == null) {
			log.error("No name schedule for id: " + getScheduleId());
			return "";
		}
		return schedule.getName();
	}

	public Schedule getSchedule() {
		if (getScheduleId().equals(""))
			return null;
		Schedule schedule = ScheduleManager.instance().getScheduleById(getScheduleId());
		if (schedule == null) {
			log.error("No schedule for id: " + getScheduleId());
		}
		return schedule;
	}

	public String getScheduleId() {
		// Only spurs can have a schedule
		if (!getTrackType().equals(SPUR))
			return "";
		// old code only stored schedule name, so create id if needed.
		if (_scheduleId.equals("") && !_scheduleName.equals("")) {
			Schedule schedule = ScheduleManager.instance().getScheduleByName(_scheduleName);
			if (schedule == null) {
				log.error("No schedule for name: " + _scheduleName);
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
			Schedule schedule = ScheduleManager.instance().getScheduleById(id);
			if (schedule == null) {
				_scheduleName = "";
			} else {
				// set the id to the first item in the list
				if (schedule.getItemsBySequenceList().size() > 0)
					setScheduleItemId(schedule.getItemsBySequenceList().get(0).getId());
				setScheduleCount(0);
			}
			setDirtyAndFirePropertyChange(SCHEDULE_ID_CHANGED_PROPERTY, old, id);
		}
	}

	/**
	 * Recommend getCurrentScheduleItem() to get the current schedule item for this track. Protects against user
	 * deleting a schedule item from the schedule.
	 * 
	 * @return schedule item id
	 */
	public String getScheduleItemId() {
		return _scheduleItemId;
	}

	public void setScheduleItemId(String id) {
		log.debug("set schedule item id: {} for track ({})",  id, getName());
		String old = _scheduleItemId;
		_scheduleItemId = id;
		setDirtyAndFirePropertyChange(SCHEDULE_CHANGED_PROPERTY, old, id);
	}

	/**
	 * Get's the current schedule item for this track Protects against user deleting an item in a shared schedule.
	 * Recommend using this versus getScheduleItemId() as the id can be obsolete.
	 */
	public ScheduleItem getCurrentScheduleItem() {
		Schedule sch = getSchedule();
		if (sch == null) {
			log.debug("Can not find schedule (" + getScheduleId() + ") assigned to track ("
					+ getName() + ")");
			return null;
		}
		ScheduleItem currentSi = sch.getItemById(getScheduleItemId());
		if (currentSi == null && sch.getSize() > 0) {
			log.debug("Can not find schedule item (" + getScheduleItemId() + ") for schedule ("
					+ getScheduleName() + ")");
			// reset schedule
			setScheduleItemId((sch.getItemsBySequenceList().get(0)).getId());
			currentSi = sch.getItemById(getScheduleItemId());
		}
		return currentSi;
	}

	public void bumpSchedule() {
		// bump the track move count
		setMoves(getMoves() + 1);
		// bump the schedule count
		setScheduleCount(getScheduleCount() + 1);
		if (getScheduleCount() < getCurrentScheduleItem().getCount())
			return;
		setScheduleCount(0);
		// is the schedule in match mode?
		if (getScheduleMode() == MATCH)
			return;
		// go to the next item on the schedule
		getNextScheduleItem();
	}

	public ScheduleItem getNextScheduleItem() {
		Schedule sch = getSchedule();
		if (sch == null) {
			log.warn("Can not find schedule (" + getScheduleId() + ") assigned to track ("
					+ getName() + ")");
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
	 * @return "" if schedule okay, otherwise an error message.
	 */
	public String checkScheduleValid() {
		String status = "";
		if (getScheduleId().equals(""))
			return status;
		Schedule schedule = getSchedule();
		if (schedule == null)
			return MessageFormat.format(Bundle.getMessage("CanNotFindSchedule"),
					new Object[] { getScheduleId() });
		List<ScheduleItem> scheduleItems = schedule.getItemsBySequenceList();
		if (scheduleItems.size() == 0) {
			return Bundle.getMessage("empty");
		}
		for (int i = 0; i < scheduleItems.size(); i++) {
			ScheduleItem si = scheduleItems.get(i);
			// check train schedule
			if (!si.getTrainScheduleId().equals("")
					&& TrainScheduleManager.instance().getScheduleById(si.getTrainScheduleId()) == null) {
				status = MessageFormat.format(Bundle.getMessage("NotValid"),
						new Object[] { si.getTrainScheduleId() });
				break;
			}
			if (!_location.acceptsTypeName(si.getTypeName())) {
				status = MessageFormat.format(Bundle.getMessage("NotValid"),
						new Object[] { si.getTypeName() });
				break;
			}
			if (!acceptsTypeName(si.getTypeName())) {
				status = MessageFormat.format(Bundle.getMessage("NotValid"),
						new Object[] { si.getTypeName() });
				break;
			}
			// check roads, accepted by track, valid road, and there's at least one car with that road
			if (!si.getRoadName().equals("")
					&& (!acceptsRoadName(si.getRoadName()) || !CarRoads.instance().containsName(si.getRoadName()) || CarManager
							.instance().getByTypeAndRoad(si.getTypeName(), si.getRoadName()) == null)) {
				status = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si.getRoadName() });
				break;
			}
			// check loads
			List<String> loads = CarLoads.instance().getNames(si.getTypeName());
			if (!si.getReceiveLoadName().equals("")
					&& (!acceptsLoad(si.getReceiveLoadName(), si.getTypeName()) || !loads.contains(si
							.getReceiveLoadName()))) {
				status = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si.getReceiveLoadName() });
				break;
			}
			if (!si.getShipLoadName().equals("") && !loads.contains(si.getShipLoadName())) {
				status = MessageFormat.format(Bundle.getMessage("NotValid"),
						new Object[] { si.getShipLoadName() });
				break;
			}
			// check destination
			if (si.getDestination() != null && !si.getDestination().acceptsTypeName(si.getTypeName())) {
				status = MessageFormat.format(Bundle.getMessage("NotValid"),
						new Object[] { si.getDestination() });
				break;
			}
			// check destination track
			if (si.getDestination() != null && si.getDestinationTrack() != null) {
				if (!si.getDestinationTrack().acceptsTypeName(si.getTypeName())) {
					status = MessageFormat.format(
							Bundle.getMessage("NotValid"),
							new Object[] { si.getDestinationTrack() + " ("
									+ Bundle.getMessage("Type") + ")" });
					break;
				}
				if (!si.getRoadName().equals("")
						&& !si.getDestinationTrack().acceptsRoadName(si.getRoadName())) {
					status = MessageFormat.format(
							Bundle.getMessage("NotValid"),
							new Object[] { si.getDestinationTrack() + " ("
									+ Bundle.getMessage("Road") + ")" });
					break;
				}
				if (!si.getShipLoadName().equals("")
						&& !si.getDestinationTrack().acceptsLoad(si.getShipLoadName(), si.getTypeName())) {
					status = MessageFormat.format(
							Bundle.getMessage("NotValid"),
							new Object[] { si.getDestinationTrack() + " ("
									+ Bundle.getMessage("Load") + ")" });
					break;
				}
			}
		}
		return status;
	}
	/**
	 * Checks to see if car can be placed on this spur using this schedule.  Returns
	 * OKAY if the schedule can service the car.
	 * @param car
	 * @return Track.OKAY track.CUSTOM track.SCHEDULE
	 */
	public String checkSchedule(Car car) {
		// does car already have this destination?
		if (car.getDestinationTrack() == this)
			return OKAY;
		// only spurs can have a schedule
		if (!getTrackType().equals(SPUR))
			return OKAY;
		if (getScheduleId().equals("")) {
			// does car have a scheduled load?
			if (car.getLoadName().equals(CarLoads.instance().getDefaultEmptyName())
					|| car.getLoadName().equals(CarLoads.instance().getDefaultLoadName()))
				return OKAY; // no
			return MessageFormat.format(Bundle.getMessage("carHasA"), new Object[] { CUSTOM, LOAD, car.getLoadName() });
		}
		log.debug("Track (" + getName() + ") has schedule (" + getScheduleName() + ") mode "
				+ getScheduleMode() + (getScheduleMode() == SEQUENTIAL? " Sequential" : " Match")); // NOI18N

		ScheduleItem si = getCurrentScheduleItem();
		if (si == null) {
			log.error("Could not find schedule item id (" + getScheduleItemId() + ") for schedule ("
					+ getScheduleName() + ")"); // NOI18N
			return SCHEDULE + " ERROR"; // NOI18N
		}
		if (getScheduleMode() == SEQUENTIAL)
			return checkScheduleItem(si, car);
		// schedule in is match mode search entire schedule for a match
		return searchSchedule(car);
	}

	private static boolean debugFlag = false;

	private String searchSchedule(Car car) {
		if (debugFlag)
			log.debug("Search match for car " + toString() + " type (" + car.getTypeName() + ") load (" + car.getLoadName()
					+ ")");
		if (!car.getScheduleId().equals("")) {
			ScheduleItem si = getSchedule().getItemById(car.getScheduleId());
			if (si != null && checkScheduleItem(si, car).equals(OKAY))
				return OKAY;
		}
		for (int i = 0; i < getSchedule().getSize(); i++) {
			ScheduleItem si = getNextScheduleItem();
			if (debugFlag)
				log.debug("Item id (" + si.getId() + ") requesting type (" + si.getTypeName() + ") " + "load ("
						+ si.getReceiveLoadName() + ") final dest (" + si.getDestinationName() + ") track (" // NOI18N
						+ si.getDestinationTrackName() + ")"); // NOI18N
			String status = checkScheduleItem(si, car);
			if (status.equals(OKAY)) {
				log.debug("Found item match (" + si.getId() + ") car (" + car.toString() + ") load ("
						+ si.getReceiveLoadName() + ") ship (" + si.getShipLoadName() + ") " + "destination (" // NOI18N
						+ si.getDestinationName() + ", " + si.getDestinationTrackName() + ")"); // NOI18N
				car.setScheduleId(si.getId());	// remember which item was a match
				return OKAY;
			} else {
				if (debugFlag)
					log.debug("Item id (" + si.getId() + ") status (" + status + ")");
			}
		}
		if (debugFlag)
			log.debug("No Match");
		car.setScheduleId("");	// clear the car's schedule id
		return SCHEDULE + " " +Bundle.getMessage("noMatch");
	}

	private String checkScheduleItem(ScheduleItem si, Car car) {
		if (!si.getTrainScheduleId().equals("")
				&& !TrainManager.instance().getTrainScheduleActiveId().equals(si.getTrainScheduleId())) {
			TrainSchedule sch = TrainScheduleManager.instance().getScheduleById(si.getTrainScheduleId());
			if (sch != null)
				return SCHEDULE + " (" + getScheduleName() + ") " + Bundle.getMessage("requestCarOnly")
						+ " (" + sch.getName() + ")";
		}
		// Check for correct car type, road, load
		if (!car.getTypeName().equals(si.getTypeName())) {
			return SCHEDULE + " (" + getScheduleName() + ") " + Bundle.getMessage("requestCar") + " "
					+ TYPE + " (" + si.getTypeName() + ")";
		}
		if (!si.getRoadName().equals("") && !car.getRoadName().equals(si.getRoadName())) {
			return SCHEDULE + " (" + getScheduleName() + ") " + Bundle.getMessage("requestCar") + " "
					+ TYPE + " (" + si.getTypeName() + ") " + ROAD + " (" + si.getRoadName() + ")";
		}
		if (!si.getReceiveLoadName().equals("") && !car.getLoadName().equals(si.getReceiveLoadName())) {
			return SCHEDULE + " (" + getScheduleName() + ") " + Bundle.getMessage("requestCar") + " "
					+ TYPE + " (" + si.getTypeName() + ") " + LOAD + " (" + si.getReceiveLoadName() + ")";
		}
		return OKAY;
	}
	
	/**
	 * Check to see if track has schedule and if it does will schedule the next item in the list. Load the car with the
	 * next schedule load if one exists, and set the car's final destination if there's one in the schedule.
	 * 
	 * @param car
	 * @return Track.OKAY or Track.SCHEDULE
	 */
	public String scheduleNext(Car car) {
		// clean up the car's final destination if sent to that destination and there isn't a schedule
		if (getScheduleId().equals("") && car.getDestination() != null
				&& car.getDestination().equals(car.getFinalDestination()) && car.getDestinationTrack() != null
				&& (car.getDestinationTrack().equals(car.getFinalDestinationTrack()) || car.getFinalDestinationTrack() == null)) {
			car.setFinalDestination(null);
			car.setFinalDestinationTrack(null);
		}
		// check for schedule, only spurs can have a schedule
		if (getScheduleId().equals("") || getSchedule() == null)
			return OKAY;
		// is car part of a kernel?
		if (car.getKernel() != null && !car.getKernel().isLead(car)) {
			log.debug("Car (" + car.toString() + ") is part of kernel (" + car.getKernelName() + ")");
			return OKAY;
		}
		// a car has a schedule id if the schedule was in match mode
		if (!car.getScheduleId().equals("")) {
			String id = car.getScheduleId();
			log.debug("Car ({}) has schedule id ({})", car.toString(), car.getScheduleId());
			Schedule sch = getSchedule();
			if (sch != null) {
				ScheduleItem si = sch.getItemById(id);
				car.setScheduleId("");
				if (si != null) {
					loadNext(si, car);
					return OKAY;
				}
				log.debug("Schedule id " + id + " not valid for track (" + getName() + ")");
				// user could have deleted the schedule item after build train, so not really an error
				// return SCHEDULE + " ERROR id " + id + " not valid for track ("+ getName() + ")"; // NOI18N
			}
		}
		if (getScheduleMode() == MATCH && !searchSchedule(car).equals(OKAY)) {
			return SCHEDULE +MessageFormat.format(Bundle.getMessage("matchMessage"), new Object[] {getScheduleName()});
		}
		ScheduleItem currentSi = getCurrentScheduleItem();
		log.debug("Destination track (" + getName() + ") has schedule (" + getScheduleName()
				+ ") item id: " + getScheduleItemId() + " mode: " + getScheduleMode()); // NOI18N
		if (currentSi != null
				&& (currentSi.getTrainScheduleId().equals("") || TrainManager.instance()
						.getTrainScheduleActiveId().equals(currentSi.getTrainScheduleId()))
				&& car.getTypeName().equals(currentSi.getTypeName())
				&& (currentSi.getRoadName().equals("") || car.getRoadName().equals(currentSi.getRoadName()))
				&& (currentSi.getReceiveLoadName().equals("") || car.getLoadName().equals(currentSi.getReceiveLoadName()))) {
			loadNext(currentSi, car);
			car.setScheduleId("");
			// bump schedule
			bumpSchedule();
		} else if (currentSi != null) {
//			log.debug("Car (" + toString() + ") type (" + getType() + ") road (" + getRoad() + ") load ("
//					+ getLoad() + ") arrived out of sequence, needed type (" + currentSi.getType() // NOI18N
//					+ ") road (" + currentSi.getRoad() + ") load (" + currentSi.getLoad() + ")"); // NOI18N		
			// build return message
			String timetableName = "";
			String currentTimetableName = "";
			TrainSchedule sch = TrainScheduleManager.instance().getScheduleById(
					TrainManager.instance().getTrainScheduleActiveId());
			if (sch != null)
				timetableName = sch.getName();
			sch = TrainScheduleManager.instance().getScheduleById(currentSi.getTrainScheduleId());
			if (sch != null)
				currentTimetableName = sch.getName();
			String mode = Bundle.getMessage("sequential");
			if (getScheduleMode() == 1)
				mode = Bundle.getMessage("match");
			return SCHEDULE
					+ MessageFormat.format(Bundle.getMessage("sequentialMessage"), new Object[] {
							getScheduleName(), mode,  car.toString(), car.getTypeName(), timetableName,
							car.getRoadName(), car.getLoadName(), currentSi.getTypeName(), currentTimetableName,  currentSi.getRoadName(),
							currentSi.getReceiveLoadName() });
		} else {
			log.error("ERROR Track " + getName() + " current schedule item is null!");
			return SCHEDULE + " ERROR Track " + getName() + " current schedule item is null!"; // NOI18N
		}
		return OKAY;
	}

	/**
	 * Loads the car's with a final destination which is the ship address for the schedule item. Also sets the next load
	 * and wait count that will kick in when the car arrives at the spur with this schedule.
	 * 
	 * @param scheduleItem
	 * @param car
	 */
	private void loadNext(ScheduleItem scheduleItem, Car car) {
		if (scheduleItem == null) {
			log.debug("schedule item is null!, id " + getScheduleId());
			return;
		}
		// set the car's next load
		car.setNextLoadName(scheduleItem.getShipLoadName());
		// set the car's final destination and track
		car.setFinalDestination(scheduleItem.getDestination());
		car.setFinalDestinationTrack(scheduleItem.getDestinationTrack());
		// set the wait count
		car.setNextWait(scheduleItem.getWait());
		// bump hit count for this schedule item
		scheduleItem.setHits(scheduleItem.getHits() + 1);

		log.debug("Car (" + car.toString() + ") type (" + car.getTypeName() + ") next load (" + car.getNextLoadName()
				+ ") final destination (" + car.getFinalDestinationName() + ", " + car.getFinalDestinationTrackName() // NOI18N
				+ ") next wait: " + car.getNextWait()); // NOI18N
		// set all cars in kernel to the next load
		car.updateKernel();
	}


	/**
	 * Enable changing the car generic load state when car arrives at this track.
	 * 
	 * @param enable
	 *            when true, swap generic car load state
	 */
	public void setLoadSwapEnabled(boolean enable) {
		if (enable)
			_loadOptions = _loadOptions | SWAP_GENERIC_LOADS;
		else
			_loadOptions = _loadOptions & 0xFFFF - SWAP_GENERIC_LOADS;
	}

	public boolean isLoadSwapEnabled() {
		return (0 < (_loadOptions & SWAP_GENERIC_LOADS));
	}

	/**
	 * Enable setting the car generic load state to empty when car arrives at this track.
	 * 
	 * @param enable
	 *            when true, set generic car load to empty
	 */
	public void setLoadEmptyEnabled(boolean enable) {
		if (enable)
			_loadOptions = _loadOptions | EMPTY_GENERIC_LOADS;
		else
			_loadOptions = _loadOptions & 0xFFFF - EMPTY_GENERIC_LOADS;
	}

	public boolean isLoadEmptyEnabled() {
		return (0 < (_loadOptions & EMPTY_GENERIC_LOADS));
	}

	/**
	 * When enabled, remove Scheduled car loads.
	 * 
	 * @param enable
	 *            when true, remove Scheduled loads from cars
	 */
	public void setRemoveCustomLoadsEnabled(boolean enable) {
		if (enable)
			_loadOptions = _loadOptions | EMPTY_CUSTOM_LOADS;
		else
			_loadOptions = _loadOptions & 0xFFFF - EMPTY_CUSTOM_LOADS;
	}

	public boolean isRemoveCustomLoadsEnabled() {
		return (0 < (_loadOptions & EMPTY_CUSTOM_LOADS));
	}

	/**
	 * When enabled, add custom car loads if there's a demand.
	 * 
	 * @param enable
	 *            when true, add custom loads to cars
	 */
	public void setAddCustomLoadsEnabled(boolean enable) {
		boolean old = isAddCustomLoadsEnabled();
		if (enable)
			_loadOptions = _loadOptions | GENERATE_CUSTOM_LOADS;
		else
			_loadOptions = _loadOptions & 0xFFFF - GENERATE_CUSTOM_LOADS;
		setDirtyAndFirePropertyChange(LOAD_OPTIONS_CHANGED_PROPERTY, old, enable);
	}

	public boolean isAddCustomLoadsEnabled() {
		return (0 < (_loadOptions & GENERATE_CUSTOM_LOADS));
	}

	/**
	 * When enabled, add custom car loads if there's a demand by any spur/industry.
	 * 
	 * @param enable
	 *            when true, add custom loads to cars
	 */
	public void setAddCustomLoadsAnySpurEnabled(boolean enable) {
		boolean old = isAddCustomLoadsAnySpurEnabled();
		if (enable)
			_loadOptions = _loadOptions | GENERATE_CUSTOM_LOADS_ANY_SPUR;
		else
			_loadOptions = _loadOptions & 0xFFFF - GENERATE_CUSTOM_LOADS_ANY_SPUR;
		setDirtyAndFirePropertyChange(LOAD_OPTIONS_CHANGED_PROPERTY, old, enable);
	}

	public boolean isAddCustomLoadsAnySpurEnabled() {
		return (0 < (_loadOptions & GENERATE_CUSTOM_LOADS_ANY_SPUR));
	}

	/**
	 * When enabled, add custom car loads to cars in staging for new destinations that are staging.
	 * 
	 * @param enable
	 *            when true, add custom load to car
	 */
	public void setAddCustomLoadsAnyStagingTrackEnabled(boolean enable) {
		boolean old = isAddCustomLoadsAnyStagingTrackEnabled();
		if (enable)
			_loadOptions = _loadOptions | GENERATE_CUSTOM_LOADS_ANY_STAGING_TRACK;
		else
			_loadOptions = _loadOptions & 0xFFFF - GENERATE_CUSTOM_LOADS_ANY_STAGING_TRACK;
		setDirtyAndFirePropertyChange(LOAD_OPTIONS_CHANGED_PROPERTY, old, enable);
	}

	public boolean isAddCustomLoadsAnyStagingTrackEnabled() {
		return (0 < (_loadOptions & GENERATE_CUSTOM_LOADS_ANY_STAGING_TRACK));
	}

	public void setBlockCarsEnabled(boolean enable) {
		if (enable)
			_blockOptions = _blockOptions | BLOCK_CARS;
		else
			_blockOptions = _blockOptions & 0xFFFF - BLOCK_CARS;
	}

	/**
	 * When enabled block cars from staging.
	 * 
	 * @return true if blocking is enabled.
	 */
	public boolean isBlockCarsEnabled() {
		return (0 < (_blockOptions & BLOCK_CARS));
	}

	public void setPool(Pool pool) {
		Pool old = _pool;
		_pool = pool;
		if (old != pool) {
			if (old != null)
				old.remove(this);
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
		if (getPool() != null)
			return getPool().getName();
		return "";
	}
	
	List<String> _destinationIdList = new ArrayList<String>();
	public int getDestinationListSize() {
		return _destinationIdList.size();
	}
	
	public boolean addDestination(Location destination) {
		if (_destinationIdList.contains(destination.getId()))
			return false;		
		_destinationIdList.add(destination.getId());
		setDirtyAndFirePropertyChange(DESTINATIONS_CHANGED_PROPERTY, null, destination.getName()); // NOI18N
		return true;
	}
	
	public void deleteDestination(Location destination) {
		if (!_destinationIdList.contains(destination.getId()))
			return;
		_destinationIdList.remove(destination.getId());
		setDirtyAndFirePropertyChange(DESTINATIONS_CHANGED_PROPERTY, destination.getName(), null); // NOI18N
		return;
	}
	
	/**
	 * Returns true if destination is valid from this track.
	 * @param destination
	 * @return true if track services the destination
	 */
	public boolean acceptsDestination(Location destination) {
		if (getDestinationOption().equals(ALL_DESTINATIONS) || destination == null)
			return true;
		return _destinationIdList.contains(destination.getId());
	}
	
	public void setDestinationIds(String[] ids) {
		for (int i = 0; i < ids.length; i++)
			_destinationIdList.add(ids[i]);
	}
	
	public String[] getDestinationIds() {
		String[] ids = _destinationIdList.toArray(new String[0]);
		return ids;
	}
	
	/**
	 * Sets the destination option for this track. The three options are:
	 * <P>
	 * ALL_DESTINATIONS which means this track services all destinations, the default.
	 * <P>
	 * INCLUDE_DESTINATIONS which means this track services only certain destinations.
	 * <P>
	 * EXCLUDE_DESTINATIONS which means this track does not service certain destinations.
	 * 
	 * @param option Track.ALL_DESTINATIONS, Track.INCLUDE_DESTINATIONS, or Track.EXCLUDE_DESTINATIONS
	 */
	public void setDestinationOption(String option) {
		String old = _destinationOption;
		_destinationOption = option;
		if (!option.equals(old))
			setDirtyAndFirePropertyChange(DESTINATION_OPTIONS_CHANGED_PROPERTY, old, option); // NOI18N
	}
	
	public String getDestinationOption() {
		if (getTrackType().equals(INTERCHANGE))
			return _destinationOption;
		return ALL_DESTINATIONS;
	}

	public void dispose() {
		setDirtyAndFirePropertyChange(DISPOSE_CHANGED_PROPERTY, null, DISPOSE_CHANGED_PROPERTY);
	}


	/**
	 * Construct this Entry from XML. This member has to remain synchronized with the detailed DTD in
	 * operations-config.xml
	 * 
	 * @param e
	 *            Consist XML element
	 */

	public Track(Element e, Location location) {
		_location = location;
		Attribute a;
		if ((a = e.getAttribute(Xml.ID)) != null)
			_id = a.getValue();
		else
			log.warn("no id attribute in track element when reading operations");
		if ((a = e.getAttribute(Xml.NAME)) != null)
			_name = a.getValue();
		if ((a = e.getAttribute(Xml.LOC_TYPE)) != null)
			_trackType = a.getValue();
		if ((a = e.getAttribute(Xml.LENGTH)) != null)
			_length = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(Xml.MOVES)) != null)
			_moves = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(Xml.DIR)) != null)
			_trainDir = Integer.parseInt(a.getValue());
		// old way of reading track comment, see comments below for new format
		if ((a = e.getAttribute(Xml.COMMENT)) != null)
			_comment = OperationsXml.convertFromXmlComment(a.getValue());
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
		}
		// old way of reading car types up to version 3.2
		else if ((a = e.getAttribute(Xml.CAR_TYPES)) != null) {
			String names = a.getValue();
			String[] types = names.split("%%"); // NOI18N
			if (debugFlag)
				log.debug("track (" + getName() + ") accepts car types: " + names);
			setTypeNames(types);
		}
		if ((a = e.getAttribute(Xml.CAR_LOAD_OPTION)) != null)
			_loadOption = a.getValue();
		// new way of reading car loads using elements
		if (e.getChild(Xml.CAR_LOADS) != null) {
			@SuppressWarnings("unchecked")
			List<Element> carLoads = e.getChild(Xml.CAR_LOADS).getChildren(Xml.CAR_LOAD);
			String[] loads = new String[carLoads.size()];
			for (int i = 0; i < carLoads.size(); i++) {
				Element load = carLoads.get(i);
				if ((a = load.getAttribute(Xml.NAME)) != null) {
					loads[i] = a.getValue();
				}
			}
			setLoadNames(loads);
		}
		// old way of reading car loads up to version 3.2
		else if ((a = e.getAttribute(Xml.CAR_LOADS)) != null) {
			String names = a.getValue();
			String[] loads = names.split("%%"); // NOI18N
			if (log.isDebugEnabled())
				log.debug("Track (" + getName() + ") " + getLoadOption() + " car loads: " + names);
			setLoadNames(loads);
		}
		if ((a = e.getAttribute(Xml.CAR_SHIP_LOAD_OPTION)) != null)
			_shipLoadOption = a.getValue();
		// new way of reading car loads using elements
		if (e.getChild(Xml.CAR_SHIP_LOADS) != null) {
			@SuppressWarnings("unchecked")
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
			@SuppressWarnings("unchecked")
			List<Element> dropIds = e.getChild(Xml.DROP_IDS).getChildren(Xml.DROP_ID);
			String[] ids = new String[dropIds.size()];
			for (int i = 0; i < dropIds.size(); i++) {
				Element dropId = dropIds.get(i);
				if ((a = dropId.getAttribute(Xml.ID)) != null) {
					ids[i] = a.getValue();
				}
			}
			setDropIds(ids);
		}
		// old way of reading drop ids up to version 3.2
		else if ((a = e.getAttribute(Xml.DROP_IDS)) != null) {
			String names = a.getValue();
			String[] ids = names.split("%%"); // NOI18N
			if (debugFlag)
				log.debug("track (" + getName() + ") has drop ids : " + names);
			setDropIds(ids);
		}
		if ((a = e.getAttribute(Xml.DROP_OPTION)) != null)
			_dropOption = a.getValue();
		
		// new way of reading pick up ids using elements
		if (e.getChild(Xml.PICKUP_IDS) != null) {
			@SuppressWarnings("unchecked")
			List<Element> pickupIds = e.getChild(Xml.PICKUP_IDS).getChildren(Xml.PICKUP_ID);
			String[] ids = new String[pickupIds.size()];
			for (int i = 0; i < pickupIds.size(); i++) {
				Element pickupId = pickupIds.get(i);
				if ((a = pickupId.getAttribute(Xml.ID)) != null) {
					ids[i] = a.getValue();
				}
			}
			setPickupIds(ids);
		}
		// old way of reading pick up ids up to version 3.2
		else if ((a = e.getAttribute(Xml.PICKUP_IDS)) != null) {
			String names = a.getValue();
			String[] ids = names.split("%%"); // NOI18N
			if (debugFlag)
				log.debug("track (" + getName() + ") has pickup ids : " + names);
			setPickupIds(ids);
		}
		if ((a = e.getAttribute(Xml.PICKUP_OPTION)) != null)
			_pickupOption = a.getValue();
		
		// new way of reading car roads using elements
		if (e.getChild(Xml.CAR_ROADS) != null) {
			@SuppressWarnings("unchecked")
			List<Element> carRoads = e.getChild(Xml.CAR_ROADS).getChildren(Xml.CAR_ROAD);
			String[] roads = new String[carRoads.size()];
			for (int i = 0; i < carRoads.size(); i++) {
				Element road = carRoads.get(i);
				if ((a = road.getAttribute(Xml.NAME)) != null) {
					roads[i] = a.getValue();
				}
			}
			setRoadNames(roads);
		}
		// old way of reading car roads up to version 3.2
		else if ((a = e.getAttribute(Xml.CAR_ROADS)) != null) {
			String names = a.getValue();
			String[] roads = names.split("%%"); // NOI18N
			if (debugFlag)
				log.debug("track (" + getName() + ") " + getRoadOption() + " car roads: " + names);
			setRoadNames(roads);
		}
		if ((a = e.getAttribute(Xml.CAR_ROAD_OPTION)) != null)
			_roadOption = a.getValue();
		else if ((a = e.getAttribute(Xml.CAR_ROAD_OPERATION)) != null)
			_roadOption = a.getValue();
		
		if ((a = e.getAttribute(Xml.SCHEDULE)) != null)
			_scheduleName = a.getValue();
		if ((a = e.getAttribute(Xml.SCHEDULE_ID)) != null)
			_scheduleId = a.getValue();
		if ((a = e.getAttribute(Xml.ITEM_ID)) != null)
			_scheduleItemId = a.getValue();
		if ((a = e.getAttribute(Xml.ITEM_COUNT)) != null)
			_scheduleCount = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(Xml.FACTOR)) != null)
			_reservationFactor = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(Xml.SCHEDULE_MODE)) != null)
			_mode = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(Xml.ALTERNATIVE)) != null)
			_alternateTrackId = a.getValue();

		if ((a = e.getAttribute(Xml.LOAD_OPTIONS)) != null)
			_loadOptions = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(Xml.BLOCK_OPTIONS)) != null)
			_blockOptions = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(Xml.ORDER)) != null)
			_order = a.getValue();
		if ((a = e.getAttribute(Xml.POOL)) != null) {
			setPool(getLocation().addPool(a.getValue()));
			if ((a = e.getAttribute(Xml.MIN_LENGTH)) != null)
				_minimumLength = Integer.parseInt(a.getValue());
		}
		if ((a = e.getAttribute(Xml.IGNORE_USED_PERCENTAGE)) != null)
			_ignoreUsedLengthPercentage = Integer.parseInt(a.getValue());
		
		if ((a = e.getAttribute(Xml.TRACK_DESTINATION_OPTION)) != null) {
			_destinationOption = a.getValue();
		}
		if (e.getChild(Xml.DESTINATIONS) != null) {
			@SuppressWarnings("unchecked")
			List<Element> destinations = e.getChild(Xml.DESTINATIONS).getChildren(Xml.DESTINATION);
			for (int i = 0; i < destinations.size(); i++) {
				Element destination = destinations.get(i);
				if ((a = destination.getAttribute(Xml.ID)) != null) {
					_destinationIdList.add(a.getValue());
				}
			}
		}
		
		if (e.getChild(Xml.COMMENTS) != null) {
			if (e.getChild(Xml.COMMENTS).getChild(Xml.TRACK) != null &&
					( a = e.getChild(Xml.COMMENTS).getChild(Xml.TRACK).getAttribute(Xml.COMMENT)) != null) {
				_comment = a.getValue();
			}
			if (e.getChild(Xml.COMMENTS).getChild(Xml.BOTH) != null &&
					( a = e.getChild(Xml.COMMENTS).getChild(Xml.BOTH).getAttribute(Xml.COMMENT)) != null) {
				_commentBoth = a.getValue();
			}
			if (e.getChild(Xml.COMMENTS).getChild(Xml.PICKUP) != null &&
					( a = e.getChild(Xml.COMMENTS).getChild(Xml.PICKUP).getAttribute(Xml.COMMENT)) != null) {
				_commentPickup = a.getValue();
			}
			if (e.getChild(Xml.COMMENTS).getChild(Xml.SETOUT) != null &&
					( a = e.getChild(Xml.COMMENTS).getChild(Xml.SETOUT).getAttribute(Xml.COMMENT)) != null) {
				_commentSetout = a.getValue();
			}
		}
	}

	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the detailed DTD in
	 * operations-location.dtd.
	 * 
	 * @return Contents in a JDOM Element
	 */
	public Element store() {
		Element e = new Element(Xml.TRACK);
		e.setAttribute(Xml.ID, getId());
		e.setAttribute(Xml.NAME, getName());
		e.setAttribute(Xml.LOC_TYPE, getTrackType());
		e.setAttribute(Xml.DIR, Integer.toString(getTrainDirections()));
		e.setAttribute(Xml.LENGTH, Integer.toString(getLength()));
		e.setAttribute(Xml.MOVES, Integer.toString(getMoves() - getDropRS()));
		// build list of car types for this track
		String[] types = getTypeNames();
	       // Old way of saving car types
        if (Control.backwardCompatible) {
        	StringBuffer buf = new StringBuffer();
        	for (int i = 0; i < types.length; i++) {
        		// remove types that have been deleted by user
        		if (CarTypes.instance().containsName(types[i])
        				|| EngineTypes.instance().containsName(types[i]))
        			buf.append(types[i] + "%%"); // NOI18N
        	}
        	e.setAttribute(Xml.CAR_TYPES, buf.toString());
        }
		// new way of saving car types using elements
		Element eTypes = new Element(Xml.TYPES);
		for (int i = 0; i < types.length; i++) {
			// don't save types that have been deleted by user
			if (EngineTypes.instance().containsName(types[i])) {
				Element eType = new Element(Xml.LOCO_TYPE);
				eType.setAttribute(Xml.NAME, types[i]);
				eTypes.addContent(eType);
			} else if (CarTypes.instance().containsName(types[i])) {
				Element eType = new Element(Xml.CAR_TYPE);
				eType.setAttribute(Xml.NAME, types[i]);
				eTypes.addContent(eType);
			}
		}
		e.addContent(eTypes);
 
		if (Control.backwardCompatible)
			e.setAttribute(Xml.CAR_ROAD_OPERATION, getRoadOption());	// early versions had a misspelling

		// build list of car roads for this track
		if (!getRoadOption().equals(ALL_ROADS)) {
			e.setAttribute(Xml.CAR_ROAD_OPTION, getRoadOption());
			String[] roads = getRoadNames();
			if (Control.backwardCompatible) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < roads.length; i++) {
					buf.append(roads[i] + "%%"); // NOI18N
				}
				e.setAttribute(Xml.CAR_ROADS, buf.toString());
			}
			// new way of saving road names
			Element eRoads = new Element(Xml.CAR_ROADS);
			for (int i = 0; i < roads.length; i++) {
				Element eRoad = new Element(Xml.CAR_ROAD);
				eRoad.setAttribute(Xml.NAME, roads[i]);
				eRoads.addContent(eRoad);
			}
			e.addContent(eRoads);
		}
		
		// save list of car loads for this track
		if (!getLoadOption().equals(ALL_LOADS)) {
			e.setAttribute(Xml.CAR_LOAD_OPTION, getLoadOption());
			String[] loads = getLoadNames();
			if (Control.backwardCompatible) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < loads.length; i++) {
					buf.append(loads[i] + "%%"); // NOI18N
				}
				e.setAttribute(Xml.CAR_LOADS, buf.toString());
			}
			// new way of saving car loads using elements
			Element eLoads = new Element(Xml.CAR_LOADS);
			for (int i = 0; i < loads.length; i++) {
				Element eLoad = new Element(Xml.CAR_LOAD);
				eLoad.setAttribute(Xml.NAME, loads[i]);
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
			for (int i = 0; i < loads.length; i++) {
				Element eLoad = new Element(Xml.CAR_LOAD);
				eLoad.setAttribute(Xml.NAME, loads[i]);
				eLoads.addContent(eLoad);
			}
			e.addContent(eLoads);
		}
		
		if (!getDropOption().equals(ANY)) {
			e.setAttribute(Xml.DROP_OPTION, getDropOption());
			// build list of drop ids for this track
			String[] dropIds = getDropIds();		
			if (Control.backwardCompatible) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < dropIds.length; i++) {
					buf.append(dropIds[i] + "%%"); // NOI18N
				}
				e.setAttribute(Xml.DROP_IDS, buf.toString());
			}
			// new way of saving drop ids using elements
			Element eDropIds = new Element(Xml.DROP_IDS);
			for (int i = 0; i < dropIds.length; i++) {
				Element eDropId = new Element(Xml.DROP_ID);
				eDropId.setAttribute(Xml.ID, dropIds[i]);
				eDropIds.addContent(eDropId);
			}
			e.addContent(eDropIds);
		}
		
		if (!getPickupOption().equals(ANY)) {
			e.setAttribute(Xml.PICKUP_OPTION, getPickupOption());
			// build list of pickup ids for this track
			String[] pickupIds = getPickupIds();
			if (Control.backwardCompatible) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < pickupIds.length; i++) {
					buf.append(pickupIds[i] + "%%"); // NOI18N
				}
				e.setAttribute(Xml.PICKUP_IDS, buf.toString());
			}
			// new way of saving pick up ids using elements
			Element ePickupIds = new Element(Xml.PICKUP_IDS);
			for (int i = 0; i < pickupIds.length; i++) {
				Element ePickupId = new Element(Xml.PICKUP_ID);
				ePickupId.setAttribute(Xml.ID, pickupIds[i]);
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
		}
		if (getAlternateTrack() != null)
			e.setAttribute(Xml.ALTERNATIVE, getAlternateTrack().getId());
		if (_loadOptions != 0)
			e.setAttribute(Xml.LOAD_OPTIONS, Integer.toString(_loadOptions));
		if (_blockOptions != 0)
			e.setAttribute(Xml.BLOCK_OPTIONS, Integer.toString(_blockOptions));
		if (!getServiceOrder().equals(NORMAL))
			e.setAttribute(Xml.ORDER, getServiceOrder());
		if (getPool() != null) {
			e.setAttribute(Xml.POOL, getPool().getName());
			e.setAttribute(Xml.MIN_LENGTH, Integer.toString(getMinimumLength()));
		}
		if (getIgnoreUsedLengthPercentage() > 0)
			e.setAttribute(Xml.IGNORE_USED_PERCENTAGE,
					Integer.toString(getIgnoreUsedLengthPercentage()));

		if (!getDestinationOption().equals(ALL_DESTINATIONS)) {
			e.setAttribute(Xml.TRACK_DESTINATION_OPTION, getDestinationOption());
			// save destinations if they exist
			String[] destIds = getDestinationIds();
			if (destIds.length > 0) {
				Element destinations = new Element(Xml.DESTINATIONS);
				for (int i = 0; i < destIds.length; i++) {
					String locId = destIds[i];
					Location loc = LocationManager.instance().getLocationById(locId);
					if (loc != null) {
						Element destination = new Element(Xml.DESTINATION);
						destination.setAttribute(Xml.ID, locId);
						destination.setAttribute(Xml.NAME, loc.getName());
						destinations.addContent(destination);
					}
				}
				e.addContent(destinations);
			}
		}
		if (Control.backwardCompatible)
			e.setAttribute(Xml.COMMENT, getComment());
		// save manifest track comments if they exist
		if (!getComment().equals("") || !getCommentBoth().equals("") || !getCommentPickup().equals("") || !getCommentSetout().equals("")) {
			Element comments = new Element(Xml.COMMENTS);
			Element track = new Element(Xml.TRACK);
			Element both = new Element(Xml.BOTH);
			Element pickup = new Element(Xml.PICKUP);
			Element setout = new Element(Xml.SETOUT);
			comments.addContent(track);
			comments.addContent(both);
			comments.addContent(pickup);
			comments.addContent(setout);
			track.setAttribute(Xml.COMMENT, getComment());
			both.setAttribute(Xml.COMMENT, getCommentBoth());
			pickup.setAttribute(Xml.COMMENT, getCommentPickup());
			setout.setAttribute(Xml.COMMENT, getCommentSetout());
			e.addContent(comments);
		}

		return e;
	}

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
		LocationManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static Logger log = LoggerFactory.getLogger(Track.class.getName());

}
