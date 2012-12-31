package jmri.jmrit.operations.locations;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainScheduleManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.setup.Setup;

/**
 * Represents a location (track) on the layout Can be a spur, yard, staging, or interchange track.
 * 
 * @author Daniel Boudreau
 * @version $Revision$
 */
public class Track {

	protected String _id = "";
	protected String _name = "";
	protected String _locType = ""; // yard, spur, interchange or staging
	protected Location _location; // the location for this track
	protected String _alternativeTrackId = ""; // the alternative track id
	protected String _roadOption = ALLROADS; // controls which car roads are accepted
	protected int _trainDir = EAST + WEST + NORTH + SOUTH; // train direction served by this track
	protected int _numberRS = 0; // number of cars and engines
	protected int _numberCars = 0; // number of cars
	protected int _numberEngines = 0; // number of engines
	protected int _pickupRS = 0; // number of pick ups by trains
	protected int _dropRS = 0; // number of set outs by trains
	protected int _length = 0; // length of track
	protected int _reserved = 0; // length of track reserved by trains
	protected int _reservedDrops = 0; // length of track reserved for drops
	protected int _numberCarsInRoute = 0; // number of cars in route to this track
	protected int _usedLength = 0; // length of track filled by cars and engines
	protected int _ignoreUsedLengthPercentage = 0; // value between 0 and 100, 100 = ignore 100%
	protected int _moves = 0; // count of the drops since creation
	protected String _comment = "";

	protected String _loadOption = ALLLOADS; // track load restrictions

	// schedule options
	protected String _scheduleName = ""; // Schedule name if there's one
	protected String _scheduleId = ""; // Schedule id if there's one
	protected String _scheduleItemId = ""; // the current scheduled item id
	protected int _scheduleCount = 0; // the number of times the item has been delivered
	protected int _reservedInRoute = 0; // length of cars in route to this track
	protected int _reservationFactor = 100; // percentage of track space for cars in route
	protected int _mode = SEQUENTIAL;

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
	private static final int EMPTY_SCHEDULE_LOADS = 2;
	private static final int GENERATE_SCHEDULE_LOADS = 4;
	private static final int GENERATE_SCHEDULE_LOADS_ANY_SIDING = 8;
	private static final int EMPTY_GENERIC_LOADS = 16;
	private static final int GENERATE_CUSTOM_LOADS_ANY_STAGING_TRACK = 32;

	protected int _blockOptions = 0;
	private static final int BLOCK_CARS = 1;

	// order cars are serviced
	protected String _order = NORMAL;
	public static final String NORMAL = Bundle.getString("Normal");
	public static final String FIFO = Bundle.getString("FIFO");
	public static final String LIFO = Bundle.getString("LIFO");

	// the four types of tracks
	public static final String STAGING = "Staging";
	public static final String INTERCHANGE = "Interchange";
	public static final String YARD = "Yard";
	public static final String SIDING = "Siding";

	// train directions serviced by this track
	public static final int EAST = 1;
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;

	// how roads are serviced by this track
	public static final String ALLROADS = Bundle.getString("All"); // track accepts all roads
	public static final String INCLUDEROADS = Bundle.getString("Include"); // track accepts only certain roads
	public static final String EXCLUDEROADS = Bundle.getString("Exclude"); // track does not accept certain roads

	// load options
	public static final String ALLLOADS = Bundle.getString("All"); // track services all loads
	public static final String INCLUDELOADS = Bundle.getString("Include");
	public static final String EXCLUDELOADS = Bundle.getString("Exclude");

	// schedule modes
	public static final int SEQUENTIAL = 0;
	public static final int MATCH = 1;

	// pool
	protected Pool _pool = null;
	protected int _minimumLength = 0;

	// return status when checking rolling stock
	public static final String OKAY = Bundle.getString("okay");
	public static final String LENGTH = Bundle.getString("length");
	public static final String TYPE = Bundle.getString("type");
	public static final String ROAD = Bundle.getString("road");
	public static final String LOAD = Bundle.getString("load");

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

	public Track(String id, String name, String type, Location location) {
		log.debug("New track " + name + " " + id);
		_location = location;
		_locType = type;
		_name = name;
		_id = id;
		// a new track accepts all types
		setTypeNames(CarTypes.instance().getNames());
		setTypeNames(EngineTypes.instance().getNames());
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

	public String getLocType() {
		return _locType;
	}

	public void setLocType(String type) {
		String old = _locType;
		_locType = type;
		if (!old.equals(type))
			setDirtyAndFirePropertyChange(TRACK_TYPE_CHANGED_PROPERTY, old, type);
	}

	public void setLength(int length) {
		int old = _length;
		_length = length;
		if (old != length)
			setDirtyAndFirePropertyChange(LENGTH_CHANGED_PROPERTY, Integer.toString(old),
					Integer.toString(length));
		// set dirty, length can change if track is part of a pool
		// LocationManagerXml.instance().setDirty(true);
	}

	public int getLength() {
		return _length;
	}

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
			setDirtyAndFirePropertyChange("reserved", Integer.toString(old),
					Integer.toString(reserved)); // NOI18N
	}

	public int getReserved() {
		return _reserved;
	}

	public void addReservedInRoute(Car car) {
		int old = _reservedInRoute;
		_numberCarsInRoute++;
		_reservedInRoute = old + Integer.parseInt(car.getLength()) + RollingStock.COUPLER;
		if (old != _reservedInRoute)
			setDirtyAndFirePropertyChange("reservedInRoute", Integer.toString(old),
					Integer.toString(_reservedInRoute)); // NOI18N
	}

	public void deleteReservedInRoute(Car car) {
		int old = _reservedInRoute;
		_numberCarsInRoute--;
		_reservedInRoute = old - (Integer.parseInt(car.getLength()) + RollingStock.COUPLER);
		if (old != _reservedInRoute)
			setDirtyAndFirePropertyChange("reservedInRoute", Integer.toString(old),
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
			setDirtyAndFirePropertyChange("scheduleMode", old, mode); // NOI18N
	}

	/**
	 * Gets the mode of operation for the schedule assigned to this track.
	 * 
	 * @return Mode of operation: Track.SEQUENTIAL or Track.MATCH
	 */
	public int getScheduleMode() {
		return _mode;
	}

	public void setAlternativeTrack(Track track) {
		Track oldTrack = _location.getTrackById(_alternativeTrackId);
		String old = _alternativeTrackId;
		if (track != null)
			_alternativeTrackId = track.getId();
		else
			_alternativeTrackId = "";
		if (!old.equals(_alternativeTrackId))
			setDirtyAndFirePropertyChange("alternativeTrack", oldTrack, track); // NOI18N
	}

	public Track getAlternativeTrack() {
		return _location.getTrackById(_alternativeTrackId);
	}

	/**
	 * Used to determine if there's space available at this track for the car. Considers cars currently placed on the
	 * track and cars in route to this track. Ignores car pick ups. Used to prevent overloading the track with cars from
	 * staging.
	 * 
	 * @param car
	 *            The car to be set out.
	 * @return true if space available.
	 */
	public boolean isSpaceAvailable(Car car) {
		int length = Integer.parseInt(car.getLength()) + RollingStock.COUPLER;
		if (car.getKernel() != null)
			length = car.getKernel().getLength();
		int reservationFactor = getReservationFactor();
		// ignore reservation factor unless car is departing staging
		if (car.getTrack() != null && !car.getTrack().getLocType().equals(Track.STAGING))
			reservationFactor = 100; // ignore, track isn't staging
		if (getLength() * reservationFactor / 100 - (getReservedInRoute() + length) >= 0)
			return true;
		else
			return false;
	}

	public void setUsedLength(int length) {
		int old = _usedLength;
		_usedLength = length;
		if (old != length)
			setDirtyAndFirePropertyChange("usedLength", Integer.toString(old),
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
			setDirtyAndFirePropertyChange("numberRS", Integer.toString(old),
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
			setDirtyAndFirePropertyChange("numberCars", Integer.toString(old),
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
			setDirtyAndFirePropertyChange("numberEngines", Integer.toString(old),
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
		setUsedLength(getUsedLength() + Integer.parseInt(rs.getLength()) + RollingStock.COUPLER);
	}

	public void deleteRS(RollingStock rs) {
		setNumberRS(getNumberRS() - 1);
		if (rs.getClass() == Car.class)
			setNumberCars(getNumberCars() - 1);
		else if (rs.getClass() == Engine.class)
			setNumberEngines(getNumberEngines() - 1);
		setUsedLength(getUsedLength() - (Integer.parseInt(rs.getLength()) + RollingStock.COUPLER));
	}

	/**
	 * Increments the number of cars and or engines that will be picked up by a train from this track.
	 */
	public void addPickupRS(RollingStock rs) {
		int old = _pickupRS;
		_pickupRS++;
		if (Setup.isBuildAggressive())
			setReserved(getReserved() - (Integer.parseInt(rs.getLength()) + RollingStock.COUPLER));
		setDirtyAndFirePropertyChange("pickupRS", Integer.toString(old),
				Integer.toString(_pickupRS)); // NOI18N
	}

	public void deletePickupRS(RollingStock rs) {
		int old = _pickupRS;
		if (Setup.isBuildAggressive())
			setReserved(getReserved() + (Integer.parseInt(rs.getLength()) + RollingStock.COUPLER));
		_pickupRS--;
		setDirtyAndFirePropertyChange("pickupRS", Integer.toString(old),
				Integer.toString(_pickupRS)); // NOI18N
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
		setReserved(getReserved() + Integer.parseInt(rs.getLength()) + RollingStock.COUPLER);
		_reservedDrops = _reservedDrops + Integer.parseInt(rs.getLength()) + RollingStock.COUPLER;
		setDirtyAndFirePropertyChange("addDropRS", Integer.toString(old), Integer.toString(_dropRS)); // NOI18N
	}

	public void deleteDropRS(RollingStock rs) {
		int old = _dropRS;
		_dropRS--;
		setReserved(getReserved() - (Integer.parseInt(rs.getLength()) + RollingStock.COUPLER));
		_reservedDrops = _reservedDrops - (Integer.parseInt(rs.getLength()) + RollingStock.COUPLER);
		setDirtyAndFirePropertyChange("deleteDropRS", Integer.toString(old),
				Integer.toString(_dropRS)); // NOI18N
	}

	public void setComment(String comment) {
		String old = _comment;
		_comment = comment;
		setDirtyAndFirePropertyChange("trackComment", old, comment); // NOI18N
	}

	public String getComment() {
		return _comment;
	}

	List<String> _typeList = new ArrayList<String>();

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
			_typeList.add(types[i]);
	}

	public void addTypeName(String type) {
		// insert at start of list, sort later
		if (_typeList.contains(type))
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
		if (!CarTypes.instance().containsName(type) && !EngineTypes.instance().containsName(type))
			return false;
		if (!_location.acceptsTypeName(type))
			return false;
		return _typeList.contains(type);
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
		log.debug("track " + getName() + " add car road " + road);
		setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, _roadList.size() - 1,
				_roadList.size());
	}

	public void deleteRoadName(String road) {
		_roadList.remove(road);
		log.debug("track " + getName() + " delete car road " + road);
		setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, _roadList.size() + 1,
				_roadList.size());
	}

	public boolean acceptsRoadName(String road) {
		if (_roadOption.equals(ALLROADS)) {
			return true;
		}
		if (_roadOption.equals(INCLUDEROADS)) {
			return _roadList.contains(road);
		}
		// exclude!
		return !_roadList.contains(road);
	}

	public boolean containsRoadName(String road) {
		return _roadList.contains(road);
	}

	/**
	 * Gets the car load option for this track.
	 * 
	 * @return ALLLOADS INCLUDELOADS EXCLUDELOADS
	 */
	public String getLoadOption() {
		return _loadOption;
	}

	/**
	 * Set how this track deals with car loads
	 * 
	 * @param option
	 *            ALLLOADS INCLUDELOADS EXCLUDELOADS
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
	 * Provides a list of loads that the track will either service or exclude. See setLoadOption
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
	 * Add a load that the track will either service or exclude. See setLoadOption
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
	 * Delete a load name that the track will either service or exclude. See setLoadOption
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
	 * Determine if track will service a specific load name.
	 * 
	 * @param load
	 *            the load name to check.
	 * @return true if track will service this load.
	 */
	public boolean acceptsLoadName(String load) {
		if (_loadOption.equals(ALLLOADS)) {
			return true;
		}
		if (_loadOption.equals(INCLUDELOADS)) {
			return _loadList.contains(load);
		}
		// exclude!
		return !_loadList.contains(load);
	}

	/**
	 * Determine if track will service a specific load and car type.
	 * 
	 * @param load
	 *            the load name to check.
	 * @param type
	 *            the type of car used to carry the load.
	 * @return true if track will service this load.
	 */
	public boolean acceptsLoad(String load, String type) {
		if (_loadOption.equals(ALLLOADS)) {
			return true;
		}
		if (_loadOption.equals(INCLUDELOADS)) {
			return _loadList.contains(load) || _loadList.contains(type + CarLoad.SPLIT_CHAR + load);
		}
		// exclude!
		return !_loadList.contains(load) && !_loadList.contains(type + CarLoad.SPLIT_CHAR + load);
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
		if (ids.length == 0)
			return;
		for (int i = 0; i < ids.length; i++)
			_dropList.add(ids[i]);
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
		if (getLocType().equals(YARD))
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
		if (getLocType().equals(YARD))
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
		if (ids.length == 0)
			return;
		for (int i = 0; i < ids.length; i++)
			_pickupList.add(ids[i]);
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
		if (getLocType().equals(YARD))
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
		if (getLocType().equals(YARD))
			return true;
		if (_pickupOption.equals(EXCLUDE_ROUTES))
			return !containsPickupId(route.getId());
		return containsPickupId(route.getId());
	}

	public boolean containsPickupId(String id) {
		return _pickupList.contains(id);
	}

	// @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="BC_UNCONFIRMED_CAST")
	public String accepts(RollingStock rs) {
		// first determine if rolling stock can be move to the new location
		if (!acceptsTypeName(rs.getType())) {
			log.debug("Rolling stock (" + rs.toString() + ") type (" + rs.getType()
					+ ") not accepted at location (" + getLocation().getName() + ", " + getName()
					+ ") wrong type");
			return TYPE + " (" + rs.getType() + ")";
		}
		if (!acceptsRoadName(rs.getRoad())) {
			log.debug("Rolling stock (" + rs.toString() + ") road (" + rs.getRoad()
					+ ") not accepted at location (" + getLocation().getName() + ", " + getName()
					+ ") wrong road");
			return ROAD + " (" + rs.getRoad() + ")";
		}
		// now determine if there's enough space for the rolling stock
		int length = 0;
		try {
			length = Integer.parseInt(rs.getLength()) + RollingStock.COUPLER;
		} catch (Exception e) {
			return LENGTH + " (" + rs.getLength() + ")";
		}
		// check for car in kernel
		if (Car.class.isInstance(rs)) {
			Car car = (Car) rs;
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
			if (!acceptsLoad(car.getLoad(), car.getType())) {
				log.debug("Car  (" + rs.toString() + ") load (" + car.getLoad()
						+ ") not accepted at location (" + getLocation().getName() + ", "
						+ getName() + ") wrong load");
				return LOAD + " (" + car.getLoad() + ")";
			}
		}
		// check for loco in consist
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
			if (getIgnoreUsedLengthPercentage() > 0) {
				int consumed = getUsedLength() * (100 - getIgnoreUsedLengthPercentage());
				if (consumed > 0)
					consumed = consumed / 100; // as a percentage
				// log.debug("Ignore used length, reservedDrops = "+_reservedDrops +
				// " rs length= "+length+" track length= "+getLength());
				// two checks, can not drop more than one track length, and second, can not exceed 100% of track length
				if (consumed + _reservedDrops + length <= getLength()
						&& getUsedLength() + _reservedDrops + length < (getLength() + getLength()
								* getIgnoreUsedLengthPercentage() / 100))
					return OKAY;
			}
			log.debug("Rolling stock (" + rs.toString() + ") not accepted at location ("
					+ getLocation().getName() + ", " + getName() + ") no room!");
			return LENGTH + " (" + length + ")";
		}
		return OKAY;
	}

	public int getMoves() {
		return _moves;
	}

	public void setMoves(int moves) {
		int old = _moves;
		_moves = moves;
		setDirtyAndFirePropertyChange("trackMoves", old, moves); // NOI18N
		// set dirty
		// LocationManagerXml.instance().setDirty(true);
	}

	/**
	 * Get the service order for this track. Only yards and interchange have this feature.
	 * 
	 * @return Service order: Track.NORMAL, Track.FIFO, Track.LIFO
	 */
	public String getServiceOrder() {
		if (getLocType().equals(SIDING) || getLocType().equals(STAGING))
			return NORMAL;
		return _order;
	}

	public void setServiceOrder(String order) {
		String old = _order;
		_order = order;
		setDirtyAndFirePropertyChange("trackServiceOrder", old, order); // NOI18N
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
		if (!getLocType().equals(Track.SIDING))
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
				return;
			}
			// set the id to the first item in the list
			if (schedule.getItemsBySequenceList().size() > 0)
				setScheduleItemId(schedule.getItemsBySequenceList().get(0));
			setScheduleCount(0);
			setDirtyAndFirePropertyChange(SCHEDULE_CHANGED_PROPERTY, old, id);
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
		log.debug("set schedule item id: " + id + " for track (" + getName() + ")");
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
			setScheduleItemId((sch.getItemById(sch.getItemsBySequenceList().get(0)).getId()));
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
		if (getScheduleMode() == Track.MATCH)
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
		List<String> l = sch.getItemsBySequenceList();
		ScheduleItem nextSi = null;
		for (int i = 0; i < l.size(); i++) {
			nextSi = sch.getItemById(l.get(i));
			if (getCurrentScheduleItem() == nextSi) {
				if (++i < l.size()) {
					nextSi = sch.getItemById(l.get(i));
				} else {
					nextSi = sch.getItemById(l.get(0));
				}
				setScheduleItemId(nextSi.getId());
				break;
			}
		}
		return nextSi;
	}

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
			return MessageFormat.format(Bundle.getString("CanNotFindSchedule"),
					new Object[] { getScheduleId() });
		List<String> scheduleItems = schedule.getItemsBySequenceList();
		if (scheduleItems.size() == 0) {
			return Bundle.getString("empty");
		}
		for (int i = 0; i < scheduleItems.size(); i++) {
			ScheduleItem si = schedule.getItemById(scheduleItems.get(i));
			// check train schedule
			if (!si.getTrainScheduleId().equals("")
					&& TrainScheduleManager.instance().getScheduleById(si.getTrainScheduleId()) == null) {
				status = MessageFormat.format(Bundle.getString("NotValid"),
						new Object[] { si.getTrainScheduleId() });
				break;
			}
			if (!_location.acceptsTypeName(si.getType())) {
				status = MessageFormat.format(Bundle.getString("NotValid"),
						new Object[] { si.getType() });
				break;
			}
			if (!acceptsTypeName(si.getType())) {
				status = MessageFormat.format(Bundle.getString("NotValid"),
						new Object[] { si.getType() });
				break;
			}
			if (!si.getRoad().equals("") && !acceptsRoadName(si.getRoad())) {
				status = MessageFormat.format(Bundle.getString("NotValid"),
						new Object[] { si.getRoad() });
				break;
			}
			if (!si.getRoad().equals("") && !CarRoads.instance().containsName(si.getRoad())) {
				status = MessageFormat.format(Bundle.getString("NotValid"),
						new Object[] { si.getRoad() });
				break;
			}
			// check loads
			if (!si.getLoad().equals("") && !acceptsLoad(si.getLoad(), si.getType())) {
				status = MessageFormat.format(Bundle.getString("NotValid"),
						new Object[] { si.getLoad() });
				break;
			}
			List<String> loads = CarLoads.instance().getNames(si.getType());
			if (!si.getLoad().equals("") && !loads.contains(si.getLoad())) {
				status = MessageFormat.format(Bundle.getString("NotValid"),
						new Object[] { si.getLoad() });
				break;
			}
			if (!si.getShip().equals("") && !loads.contains(si.getShip())) {
				status = MessageFormat.format(Bundle.getString("NotValid"),
						new Object[] { si.getShip() });
				break;
			}
			// check destination
			if (si.getDestination() != null && !si.getDestination().acceptsTypeName(si.getType())) {
				status = MessageFormat.format(Bundle.getString("NotValid"),
						new Object[] { si.getDestination() });
				break;
			}
			// check destination track
			if (si.getDestination() != null && si.getDestinationTrack() != null) {
				if (!si.getDestinationTrack().acceptsTypeName(si.getType())) {
					status = MessageFormat.format(
							Bundle.getString("NotValid"),
							new Object[] { si.getDestinationTrack() + " ("
									+ Bundle.getString("Type") + ")" });
					break;
				}
				if (!si.getRoad().equals("")
						&& !si.getDestinationTrack().acceptsRoadName(si.getRoad())) {
					status = MessageFormat.format(
							Bundle.getString("NotValid"),
							new Object[] { si.getDestinationTrack() + " ("
									+ Bundle.getString("Road") + ")" });
					break;
				}
				if (!si.getShip().equals("")
						&& !si.getDestinationTrack().acceptsLoad(si.getShip(), si.getType())) {
					status = MessageFormat.format(
							Bundle.getString("NotValid"),
							new Object[] { si.getDestinationTrack() + " ("
									+ Bundle.getString("Load") + ")" });
					break;
				}
			}
		}
		return status;
	}

	/**
	 * Enable changing the car generic load state when car arrives at this track.
	 * 
	 * @param enable
	 *            when true, swap generic car load state
	 */
	public void setLoadSwapsEnabled(boolean enable) {
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

	public boolean isSetLoadEmptyEnabled() {
		return (0 < (_loadOptions & EMPTY_GENERIC_LOADS));
	}

	/**
	 * When enabled, remove Scheduled car loads.
	 * 
	 * @param enable
	 *            when true, remove Scheduled loads from cars
	 */
	public void setRemoveLoadsEnabled(boolean enable) {
		if (enable)
			_loadOptions = _loadOptions | EMPTY_SCHEDULE_LOADS;
		else
			_loadOptions = _loadOptions & 0xFFFF - EMPTY_SCHEDULE_LOADS;
	}

	public boolean isRemoveLoadsEnabled() {
		return (0 < (_loadOptions & EMPTY_SCHEDULE_LOADS));
	}

	/**
	 * When enabled, add Scheduled car loads if there's a demand.
	 * 
	 * @param enable
	 *            when true, add Scheduled loads from cars
	 */
	public void setAddLoadsEnabled(boolean enable) {
		if (enable)
			_loadOptions = _loadOptions | GENERATE_SCHEDULE_LOADS;
		else
			_loadOptions = _loadOptions & 0xFFFF - GENERATE_SCHEDULE_LOADS;
	}

	public boolean isAddLoadsEnabled() {
		return (0 < (_loadOptions & GENERATE_SCHEDULE_LOADS));
	}

	/**
	 * When enabled, add Scheduled car loads if there's a demand.
	 * 
	 * @param enable
	 *            when true, add Scheduled loads from cars
	 */
	public void setAddLoadsAnySidingEnabled(boolean enable) {
		if (enable)
			_loadOptions = _loadOptions | GENERATE_SCHEDULE_LOADS_ANY_SIDING;
		else
			_loadOptions = _loadOptions & 0xFFFF - GENERATE_SCHEDULE_LOADS_ANY_SIDING;
	}

	public boolean isAddLoadsAnySidingEnabled() {
		return (0 < (_loadOptions & GENERATE_SCHEDULE_LOADS_ANY_SIDING));
	}

	/**
	 * When enabled, add custom car loads to cars in staging for new destinations that are staging.
	 * 
	 * @param enable
	 *            when true, add custom load to car
	 */
	public void setAddCustomLoadsAnyStagingTrackEnabled(boolean enable) {
		if (enable)
			_loadOptions = _loadOptions | GENERATE_CUSTOM_LOADS_ANY_STAGING_TRACK;
		else
			_loadOptions = _loadOptions & 0xFFFF - GENERATE_CUSTOM_LOADS_ANY_STAGING_TRACK;
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

	public void dispose() {
		setDirtyAndFirePropertyChange(DISPOSE_CHANGED_PROPERTY, null, DISPOSE_CHANGED_PROPERTY);
	}

	static final String TRACK = "track"; // NOI18N
	static final String ID = "id"; // NOI18N
	static final String NAME = "name"; // NOI18N
	static final String LOC_TYPE = "locType"; // NOI18N
	static final String XML_LENGTH = "length"; // NOI18N
	static final String MOVES = "moves"; // NOI18N
	static final String DIR = "dir"; // NOI18N
	static final String COMMENT = "comment"; // NOI18N
	static final String CAR_TYPES = "carTypes"; // NOI18N
	static final String CAR_ROAD_OPERATION = "carRoadOperation"; // NOI18N misspelled should have been carRoadOption
	static final String CAR_ROADS = "carRoads"; // NOI18N
	static final String CAR_LOAD_OPTION = "carLoadOption"; // NOI18N
	static final String CAR_LOADS = "carLoads"; // NOI18N
	static final String DROP_IDS = "dropIds"; // NOI18N
	static final String DROP_OPTION = "dropOption"; // NOI18N
	static final String PICKUP_IDS = "pickupIds"; // NOI18N
	static final String PICKUP_OPTION = "pickupOption"; // NOI18N
	static final String SCHEDULE = "schedule"; // NOI18N
	static final String SCHEDULE_ID = "scheduleId"; // NOI18N
	static final String ITEM_ID = "itemId"; // NOI18N
	static final String ITEM_COUNT = "itemCount"; // NOI18N
	static final String FACTOR = "factor"; // NOI18N
	static final String SCHEDULE_MODE = "scheduleMode"; // NOI18N
	static final String ALTERNATIVE = "alternative"; // NOI18N
	static final String LOAD_OPTIONS = "loadOptions"; // NOI18N
	static final String BLOCK_OPTIONS = "blockOptions"; // NOI18N
	static final String ORDER = "order"; // NOI18N
	static final String POOL = "pool"; // NOI18N
	static final String MIN_LENGTH = "minLength"; // NOI18N
	static final String IGNORE_USED_PERCENTAGE = "ignoreUsedPercentage"; // NOI18N

	/**
	 * Construct this Entry from XML. This member has to remain synchronized with the detailed DTD in
	 * operations-config.xml
	 * 
	 * @param e
	 *            Consist XML element
	 */
	private boolean debugFlag = false;

	public Track(org.jdom.Element e, Location location) {
		// if (log.isDebugEnabled()) log.debug("ctor from element "+e);
		_location = location;
		org.jdom.Attribute a;
		if ((a = e.getAttribute(ID)) != null)
			_id = a.getValue();
		else
			log.warn("no id attribute in track element when reading operations");
		if ((a = e.getAttribute(NAME)) != null)
			_name = a.getValue();
		if ((a = e.getAttribute(LOC_TYPE)) != null)
			_locType = a.getValue();
		if ((a = e.getAttribute(XML_LENGTH)) != null)
			_length = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(MOVES)) != null)
			_moves = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(DIR)) != null)
			_trainDir = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(COMMENT)) != null)
			_comment = a.getValue();
		if ((a = e.getAttribute(CAR_TYPES)) != null) {
			String names = a.getValue();
			String[] types = names.split("%%"); // NOI18N
			if (log.isDebugEnabled() && debugFlag)
				log.debug("track (" + getName() + ") accepts car types: " + names);
			setTypeNames(types);
		}
		if ((a = e.getAttribute(CAR_LOAD_OPTION)) != null)
			_loadOption = a.getValue();
		if ((a = e.getAttribute(CAR_LOADS)) != null) {
			String names = a.getValue();
			String[] loads = names.split("%%"); // NOI18N
			if (log.isDebugEnabled())
				log.debug("Track (" + getName() + ") " + getLoadOption() + " car loads: " + names);
			setLoadNames(loads);
		}
		if ((a = e.getAttribute(DROP_IDS)) != null) {
			String names = a.getValue();
			String[] ids = names.split("%%"); // NOI18N
			if (log.isDebugEnabled() && debugFlag)
				log.debug("track (" + getName() + ") has drop ids : " + names);
			setDropIds(ids);
		}
		if ((a = e.getAttribute(DROP_OPTION)) != null)
			_dropOption = a.getValue();
		if ((a = e.getAttribute(PICKUP_IDS)) != null) {
			String names = a.getValue();
			String[] ids = names.split("%%"); // NOI18N
			if (log.isDebugEnabled() && debugFlag)
				log.debug("track (" + getName() + ") has pickup ids : " + names);
			setPickupIds(ids);
		}
		if ((a = e.getAttribute(PICKUP_OPTION)) != null)
			_pickupOption = a.getValue();
		if ((a = e.getAttribute(CAR_ROADS)) != null) {
			String names = a.getValue();
			String[] roads = names.split("%%"); // NOI18N
			if (log.isDebugEnabled() && debugFlag)
				log.debug("track (" + getName() + ") " + getRoadOption() + " car roads: " + names);
			setRoadNames(roads);
		}
		if ((a = e.getAttribute(CAR_ROAD_OPERATION)) != null)
			_roadOption = a.getValue();
		if ((a = e.getAttribute(SCHEDULE)) != null)
			_scheduleName = a.getValue();
		if ((a = e.getAttribute(SCHEDULE_ID)) != null)
			_scheduleId = a.getValue();
		if ((a = e.getAttribute(ITEM_ID)) != null)
			_scheduleItemId = a.getValue();
		if ((a = e.getAttribute(ITEM_COUNT)) != null)
			_scheduleCount = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(FACTOR)) != null)
			_reservationFactor = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(SCHEDULE_MODE)) != null)
			_mode = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(ALTERNATIVE)) != null)
			_alternativeTrackId = a.getValue();

		if ((a = e.getAttribute(LOAD_OPTIONS)) != null)
			_loadOptions = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(BLOCK_OPTIONS)) != null)
			_blockOptions = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(ORDER)) != null)
			_order = a.getValue();
		if ((a = e.getAttribute(POOL)) != null) {
			setPool(_location.addPool(a.getValue()));
			if ((a = e.getAttribute(MIN_LENGTH)) != null)
				_minimumLength = Integer.parseInt(a.getValue());
		}
		if ((a = e.getAttribute(IGNORE_USED_PERCENTAGE)) != null)
			_ignoreUsedLengthPercentage = Integer.parseInt(a.getValue());
	}

	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the detailed DTD in
	 * operations-location.dtd.
	 * 
	 * @return Contents in a JDOM Element
	 */
	public org.jdom.Element store() {
		org.jdom.Element e = new org.jdom.Element(TRACK);
		e.setAttribute(ID, getId());
		e.setAttribute(NAME, getName());
		e.setAttribute(LOC_TYPE, getLocType());
		e.setAttribute(DIR, Integer.toString(getTrainDirections()));
		e.setAttribute(XML_LENGTH, Integer.toString(getLength()));
		e.setAttribute(MOVES, Integer.toString(getMoves() - getDropRS()));
		// build list of car types for this track
		String[] types = getTypeNames();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < types.length; i++) {
			// remove types that have been deleted by user
			if (CarTypes.instance().containsName(types[i])
					|| EngineTypes.instance().containsName(types[i]))
				buf.append(types[i] + "%%"); // NOI18N
		}
		e.setAttribute(CAR_TYPES, buf.toString());
		e.setAttribute(CAR_ROAD_OPERATION, getRoadOption());
		// build list of car roads for this track
		String[] roads = getRoadNames();
		buf = new StringBuffer();
		for (int i = 0; i < roads.length; i++) {
			buf.append(roads[i] + "%%"); // NOI18N
		}
		e.setAttribute(CAR_ROADS, buf.toString());
		e.setAttribute(CAR_LOAD_OPTION, getLoadOption());
		// save list of car loads for this track
		if (!getLoadOption().equals(ALLLOADS)) {
			String[] loads = getLoadNames();
			buf = new StringBuffer();
			for (int i = 0; i < loads.length; i++) {
				buf.append(loads[i] + "%%"); // NOI18N
			}
			e.setAttribute(CAR_LOADS, buf.toString());
		}
		e.setAttribute(DROP_OPTION, getDropOption());
		// build list of drop ids for this track
		String[] dropIds = getDropIds();
		buf = new StringBuffer();
		for (int i = 0; i < dropIds.length; i++) {
			buf.append(dropIds[i] + "%%"); // NOI18N
		}
		e.setAttribute(DROP_IDS, buf.toString());
		e.setAttribute(PICKUP_OPTION, getPickupOption());
		// build list of pickup ids for this track
		String[] pickupIds = getPickupIds();
		buf = new StringBuffer();
		for (int i = 0; i < pickupIds.length; i++) {
			buf.append(pickupIds[i] + "%%"); // NOI18N
		}
		e.setAttribute(PICKUP_IDS, buf.toString());
		if (getSchedule() != null) {
			e.setAttribute(SCHEDULE, getScheduleName());
			e.setAttribute(SCHEDULE_ID, getScheduleId());
			e.setAttribute(ITEM_ID, getScheduleItemId());
			e.setAttribute(ITEM_COUNT, Integer.toString(getScheduleCount()));
			e.setAttribute(FACTOR, Integer.toString(getReservationFactor()));
			e.setAttribute(SCHEDULE_MODE, Integer.toString(getScheduleMode()));
		}
		if (getAlternativeTrack() != null)
			e.setAttribute(ALTERNATIVE, getAlternativeTrack().getId());
		if (_loadOptions != 0)
			e.setAttribute(LOAD_OPTIONS, Integer.toString(_loadOptions));
		if (_blockOptions != 0)
			e.setAttribute(BLOCK_OPTIONS, Integer.toString(_blockOptions));
		if (!getServiceOrder().equals(NORMAL))
			e.setAttribute(ORDER, getServiceOrder());
		e.setAttribute(COMMENT, getComment());
		if (getPool() != null) {
			e.setAttribute(POOL, getPool().getName());
			e.setAttribute(MIN_LENGTH, Integer.toString(getMinimumLength()));
		}
		if (getIgnoreUsedLengthPercentage() > 0)
			e.setAttribute(IGNORE_USED_PERCENTAGE,
					Integer.toString(getIgnoreUsedLengthPercentage()));

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

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Track.class.getName());

}
