// Train.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.jdom.Element;

import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;

import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.Editor;

/**
 * Represents a train on the layout
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2011, 2012
 * @author Rodney Black Copyright (C) 2011
 * @version $Revision$
 */
public class Train implements java.beans.PropertyChangeListener {
	/*
	 * WARNING DO NOT LOAD CAR OR ENGINE MANAGERS WHEN Train.java IS CREATED IT CAUSES A RECURSIVE LOOP AT LOAD TIME,
	 * SEE EXAMPLES BELOW CarManager carManager = CarManager.instance(); EngineManager engineManager =
	 * EngineManager.instance();
	 */

	protected String _id = "";
	protected String _name = "";
	protected String _description = "";
	protected RouteLocation _current = null;// where the train is located in its route
	protected String _status = "";
	protected String _buildFailedMessage = ""; // the build failed message for this train
	protected boolean _built = false; // when true, a train manifest has been built
	protected boolean _modified = false; // when true, user has modified train after being built
	protected boolean _build = true; // when true, build this train
	protected boolean _buildFailed = false; // when true, build for this train failed
	protected boolean _printed = false; // when true, manifest has been printed
	protected boolean _sendToTerminal = false; // when true, cars picked up by train only go to terminal
	protected boolean _allowLocalMoves = true; // when true, cars with custom loads can be moved locally
	protected boolean _allowThroughCars = true; // when true, cars from the origin can be sent to the terminal
	protected boolean _buildNormal = false; // when true build this train in normal mode
	protected boolean _allowCarsReturnStaging = false; // when true allow cars to return to staging if necessary
	protected Route _route = null;
	protected Track _departureTrack; // the departure track from staging
	protected Track _terminationTrack; // the termination track into staging
	protected String _roadOption = ALLROADS;// train road name restrictions
	protected int _requires = 0; // train requirements, caboose, FRED
	protected String _numberEngines = "0"; // number of engines this train requires
	protected String _engineRoad = ""; // required road name for engines assigned to this train
	protected String _engineModel = ""; // required model of engines assigned to this train
	protected String _cabooseRoad = ""; // required road name for cabooses assigned to this train
	protected Calendar _departureTime = Calendar.getInstance(); // departure time for this train
	protected String _leadEngineId = ""; // lead engine for train icon info
	protected String _builtStartYear = ""; // built start year
	protected String _builtEndYear = ""; // built end year
	protected String _loadOption = ALLLOADS;// train load restrictions
	protected String _ownerOption = ALLOWNERS;// train owner name restrictions
	protected List<String> _buildScripts = new ArrayList<String>(); // list of script pathnames to run before train is
																	// built
	protected List<String> _afterBuildScripts = new ArrayList<String>(); // list of script pathnames to run after train
																			// is built
	protected List<String> _moveScripts = new ArrayList<String>(); // list of script pathnames to run when train is
																	// moved
	protected List<String> _terminationScripts = new ArrayList<String>(); // list of script pathnames to run when train
																			// is terminated
	protected String _railroadName = ""; // optional railroad name for this train
	protected String _logoURL = ""; // optional manifest logo for this train
	protected boolean _showTimes = true; // when true, show arrival and departure times for this train
	protected Engine _leadEngine = null; // lead engine for icon
	protected String _switchListStatus = UNKNOWN; // print switch list status
	protected String _comment = "";

	// Engine change and helper engines
	protected int _leg2Options = 0; // options
	protected RouteLocation _leg2Start = null; // route location when 2nd leg begins
	protected RouteLocation _end2Leg = null; // route location where 2nd leg ends
	protected String _leg2Engines = "0"; // number of engines 2nd leg
	protected String _leg2Road = ""; // engine road name 2nd leg
	protected String _leg2Model = ""; // engine model 2nd leg
	protected String _leg2CabooseRoad = ""; // road name for caboose 2nd leg

	protected int _leg3Options = 0; // options
	protected RouteLocation _leg3Start = null; // route location when 3rd leg begins
	protected RouteLocation _leg3End = null; // route location where 3rd leg ends
	protected String _leg3Engines = "0"; // number of engines 3rd leg
	protected String _leg3Road = ""; // engine road name 3rd leg
	protected String _leg3Model = ""; // engine model 3rd leg
	protected String _leg3CabooseRoad = ""; // road name for caboose 3rd leg

	// engine change and helper options
	public static final int CHANGE_ENGINES = 1; // change engines
	public static final int HELPER_ENGINES = 2; // add helper engines
	public static final int ADD_CABOOSE = 4; // add caboose
	public static final int REMOVE_CABOOSE = 8; // remove caboose

	// property change names
	public static final String DISPOSE_CHANGED_PROPERTY = "TrainDispose"; // NOI18N
	public static final String STOPS_CHANGED_PROPERTY = "TrainStops"; // NOI18N
	public static final String TYPES_CHANGED_PROPERTY = "TrainTypes"; // NOI18N
	public static final String BUILT_CHANGED_PROPERTY = "TrainBuilt"; // NOI18N
	public static final String BUILT_YEAR_CHANGED_PROPERTY = "TrainBuiltYear"; // NOI18N
	public static final String BUILD_CHANGED_PROPERTY = "TrainBuild"; // NOI18N
	public static final String ROADS_CHANGED_PROPERTY = "TrainRoads"; // NOI18N
	public static final String LOADS_CHANGED_PROPERTY = "TrainLoads"; // NOI18N
	public static final String OWNERS_CHANGED_PROPERTY = "TrainOwners"; // NOI18N
	public static final String NAME_CHANGED_PROPERTY = "TrainName"; // NOI18N
	public static final String DESCRIPTION_CHANGED_PROPERTY = "TrainDescription"; // NOI18N
	public static final String STATUS_CHANGED_PROPERTY = "TrainStatus"; // NOI18N
	public static final String DEPARTURETIME_CHANGED_PROPERTY = "TrainDepartureTime"; // NOI18N
	public static final String TRAIN_LOCATION_CHANGED_PROPERTY = "TrainLocation"; // NOI18N
	public static final String TRAIN_ROUTE_CHANGED_PROPERTY = "TrainRoute"; // NOI18N
	public static final String TRAIN_REQUIREMENTS_CHANGED_PROPERTY = "TrainRequirements"; // NOI18N
	public static final String TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY = "TrainMoveComplete"; // NOI18N

	// Train status
	public static final String BUILDFAILED = Bundle.getMessage("BuildFailed");
	public static final String BUILDING = Bundle.getMessage("Building");
	public static final String BUILT = Bundle.getMessage("Built");
	public static final String PARTIALBUILT = Bundle.getMessage("Partial");
	public static final String TERMINATED = Bundle.getMessage("Terminated");
	public static final String TRAINRESET = Bundle.getMessage("TrainReset");
	public static final String TRAININROUTE = Bundle.getMessage("TrainInRoute");

	// train requirements
	public static final int NONE = 0; // default
	public static final int CABOOSE = 1;
	public static final int FRED = 2;

	// road options
	public static final String ALLROADS = Bundle.getMessage("All"); // train services all road names
	public static final String INCLUDEROADS = Bundle.getMessage("Include");
	public static final String EXCLUDEROADS = Bundle.getMessage("Exclude");

	// owner options
	public static final String ALLOWNERS = Bundle.getMessage("All"); // train services all owner names
	public static final String INCLUDEOWNERS = Bundle.getMessage("Include");
	public static final String EXCLUDEOWNERS = Bundle.getMessage("Exclude");

	// load options
	public static final String ALLLOADS = Bundle.getMessage("All"); // train services all loads
	public static final String INCLUDELOADS = Bundle.getMessage("Include");
	public static final String EXCLUDELOADS = Bundle.getMessage("Exclude");

	// Switch list status
	public static final String UNKNOWN = "";
	public static final String PRINTED = Bundle.getMessage("Printed");

	public static final String AUTO = Bundle.getMessage("Auto"); // how engines are assigned to this train

	public Train(String id, String name) {
		log.debug("New train " + name + " " + id);
		_name = name;
		_id = id;
		// a new train accepts all types
		setTypeNames(CarTypes.instance().getNames());
		setTypeNames(EngineTypes.instance().getNames());
		addPropertyChangeListerners();
	}

	public String getId() {
		return _id;
	}

	/**
	 * Sets the name of this train, normally a short name that can fit within the train icon.
	 * 
	 * @param name
	 *            the train's name.
	 */
	public void setName(String name) {
		String old = _name;
		_name = name;
		if (!old.equals(name)) {
			setDirtyAndFirePropertyChange(NAME_CHANGED_PROPERTY, old, name);
		}
	}

	// for combo boxes
	/**
	 * Get's a train's name
	 * 
	 * @return train's name
	 */
	public String toString() {
		return _name;
	}

	/**
	 * Get's a train's name
	 * 
	 * @return train's name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Get's train's departure time
	 * 
	 * @return train's departure time in the String format hh:mm
	 */
	public String getDepartureTime() {
		// check to see if the route has a departure time
		RouteLocation rl = getTrainDepartsRouteLocation();
		if (rl != null && !rl.getDepartureTime().equals("")) {
			// need to forward any changes to departure time
			rl.removePropertyChangeListener(this);
			rl.addPropertyChangeListener(this);
			return rl.getDepartureTime();
		}
		int hour = _departureTime.get(Calendar.HOUR_OF_DAY);
		int minute = _departureTime.get(Calendar.MINUTE);
		String h = Integer.toString(hour);
		if (hour < 10)
			h = "0" + h;
		if (minute < 10)
			return h + ":0" + minute; // NOI18N
		return h + ":" + minute;
	}

	/**
	 * Get's train's departure time in 12hr or 24hr format
	 * 
	 * @return train's departure time in the String format hh:mm or hh:mm(AM/PM)
	 */
	public String getFormatedDepartureTime() {
		// check to see if the route has a departure time
		RouteLocation rl = getTrainDepartsRouteLocation();
		if (rl != null && !rl.getDepartureTime().equals("")) {
			// need to forward any changes to departure time
			rl.removePropertyChangeListener(this);
			rl.addPropertyChangeListener(this);
			return rl.getFormatedDepartureTime();
		}
		int hour = _departureTime.get(Calendar.HOUR_OF_DAY);
		// AM_PM field
		String am_pm = "";
		if (Setup.is12hrFormatEnabled()) {
			hour = _departureTime.get(Calendar.HOUR);
			if (hour == 0)
				hour = 12;
			am_pm = (_departureTime.get(Calendar.AM_PM) == Calendar.AM) ? " " + Bundle.getMessage("AM") : " "
					+ Bundle.getMessage("PM");
		}
		int minute = _departureTime.get(Calendar.MINUTE);
		String h = Integer.toString(hour);
		if (hour < 10)
			h = "0" + h;
		if (minute < 10)
			return h + ":0" + minute + am_pm; // NOI18N
		return h + ":" + minute + am_pm;
	}

	/**
	 * Get train's departure time in minutes from midnight for sorting
	 * 
	 * @return int hh*60+mm
	 */
	public int getDepartTimeMinutes() {
		int hour = Integer.parseInt(getDepartureTimeHour());
		int minute = Integer.parseInt(getDepartureTimeMinute());
		return (hour * 60) + minute;
	}

	public void setDepartureTime(String hour, String minute) {
		String oldHour = getDepartureTimeHour();
		String oldMinute = getDepartureTimeMinute();
		// new departure time?
		if (Integer.parseInt(hour) == Integer.parseInt(oldHour)
				&& Integer.parseInt(minute) == Integer.parseInt(oldMinute))
			return;
		_departureTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
		_departureTime.set(Calendar.MINUTE, Integer.parseInt(minute));
		setDirtyAndFirePropertyChange(DEPARTURETIME_CHANGED_PROPERTY, oldHour + ":" + oldMinute, hour + ":"
				+ minute);
	}

	public String getDepartureTimeHour() {
		String[] time = getDepartureTime().split(":");
		return time[0];
	}

	public String getDepartureTimeMinute() {
		String[] time = getDepartureTime().split(":");
		return time[1];
	}

	/**
	 * Gets the expected time when this train will arrive at the location rl. Expected arrival time is based on the
	 * number of car pick up and set outs for this train. TODO Doesn't provide expected arrival time if train is in
	 * route, instead provides relative time. If train has passed the location return -1.
	 * 
	 * @return expected arrival time
	 */
	public String getExpectedArrivalTime(RouteLocation routeLocation) {
		int minutes = getExpectedTravelTimeInMinutes(routeLocation);
		if (minutes == -1)
			return "-1"; // NOI18N
		log.debug("Calculate arrival time for train (" + getName() + ") at (" + routeLocation.getName()
				+ "), minutes from departure: " + minutes); // NOI18N
		// TODO use fast clock to get current time vs departure time
		// for now use relative
		return parseTime(minutes);
	}

	public String getExpectedDepartureTime(RouteLocation routeLocation) {
		int minutes = getExpectedTravelTimeInMinutes(routeLocation);
		if (minutes == -1)
			return "-1"; // NOI18N
		log.debug("Calculate departure time for train (" + getName() + ") at (" + routeLocation.getName()
				+ ")");
		
		// figure out the work at this location, note that there can be consecutive locations with the same name
		if (getRoute() != null) {
			List<String> routeList = getRoute().getLocationsBySequenceList();
			boolean foundRouteLocation = false;
			for (int i = 0; i < routeList.size(); i++) {
				RouteLocation rl = getRoute().getLocationById(routeList.get(i));
				if (rl == routeLocation) {
					foundRouteLocation = true;
				}
				if (foundRouteLocation) {
					if (TrainCommon.splitString(rl.getName()).equals(TrainCommon.splitString(routeLocation.getName())))
						minutes = minutes + calculateWorkAtLocation(rl);
					else
						break; // done
				}
			}
		}
		return parseTime(minutes);
	}
	
	private int calculateWorkAtLocation(RouteLocation routeLocation) {
		CarManager carManager = CarManager.instance();
		List<String> carList = carManager.getByTrainList(this);
		int minutes = 0;
		// add any work at this location
		for (int j = 0; j < carList.size(); j++) {
			Car car = carManager.getById(carList.get(j));
			if (car.getRouteLocation() == routeLocation && !car.getTrackName().equals("")) {
				minutes += Setup.getSwitchTime();
			}
			if (car.getRouteDestination() == routeLocation) {
				minutes += Setup.getSwitchTime();
			}
		}
		return minutes;
	}

	protected int getExpectedTravelTimeInMinutes(RouteLocation routeLocation) {
		int minutes = 0;
		if (!isTrainInRoute()) {
			minutes += _departureTime.get(Calendar.MINUTE);
			minutes += 60 * _departureTime.get(Calendar.HOUR_OF_DAY);
		} else {
			minutes = -1;
		}
		// boolean trainAt = false;
		CarManager carManager = CarManager.instance();
		List<String> carList = carManager.getByTrainList(this);
		boolean trainLocFound = false;
		if (getRoute() != null) {
			List<String> routeList = getRoute().getLocationsBySequenceList();
			for (int i = 0; i < routeList.size(); i++) {
				RouteLocation rl = getRoute().getLocationById(routeList.get(i));
				if (rl == routeLocation)
					break; // done
				// start recording time after finding where the train is
				if (!trainLocFound && isTrainInRoute()) {
					if (rl == getCurrentLocation()) {
						trainLocFound = true;
						// add travel time
						minutes = Setup.getTravelTime();
					}
					continue;
				}
				// is there a departure time from this location?
				if (!rl.getDepartureTime().equals("")) {
					String dt = rl.getDepartureTime();
					log.debug("Location " + rl.getName() + " departure time " + dt);
					String[] time = dt.split(":");
					minutes = 60 * Integer.parseInt(time[0]) + Integer.parseInt(time[1]);
					// log.debug("New minutes: "+minutes);
				}
				// add wait time
				minutes += rl.getWait();
				// add travel time
				minutes += Setup.getTravelTime();
				// don't count work if there's a departure time
				if (i == 0 || !rl.getDepartureTime().equals(""))
					continue;
				for (int j = 0; j < carList.size(); j++) {
					Car car = carManager.getById(carList.get(j));
					if (car.getRouteLocation() == rl && !car.getTrackName().equals("")) {
						minutes += Setup.getSwitchTime();
					}
					if (car.getRouteDestination() == rl) {
						minutes += Setup.getSwitchTime();
					}
				}
			}
		}
		return minutes;
	}

	private String parseTime(int minutes) {
		int hours = 0;
		int days = 0;

		if (minutes >= 60) {
			int h = minutes / 60;
			minutes = minutes - h * 60;
			hours += h;
		}

		String d = "";
		if (hours >= 24) {
			int nd = hours / 24;
			hours = hours - nd * 24;
			days += nd;
			d = Integer.toString(days) + ":";
		}

		// AM_PM field
		String am_pm = "";
		if (Setup.is12hrFormatEnabled()) {
			am_pm = " " + Bundle.getMessage("AM");
			if (hours >= 12) {
				hours = hours - 12;
				am_pm = " " + Bundle.getMessage("PM");
			}
			if (hours == 0)
				hours = 12;
		}

		String h = Integer.toString(hours);
		if (hours < 10)
			h = "0" + h;
		if (minutes < 10)
			return d + h + ":0" + minutes + am_pm; // NOI18N
		return d + h + ":" + minutes + am_pm;
	}

	/**
	 * Set train requirements. If NONE, then train doesn't require a caboose or car with FRED.
	 * 
	 * @param requires
	 *            NONE CABOOSE FRED
	 */
	public void setRequirements(int requires) {
		int old = _requires;
		_requires = requires;
		if (old != requires)
			setDirtyAndFirePropertyChange(TRAIN_REQUIREMENTS_CHANGED_PROPERTY, Integer.toString(old), Integer
					.toString(requires));
	}

	/**
	 * Get a train's requirements with regards to the last car in the train.
	 * 
	 * @return NONE CABOOSE FRED
	 */
	public int getRequirements() {
		return _requires;
	}

	public void setRoute(Route route) {
		Route old = _route;
		String oldRoute = "";
		String newRoute = "";
		if (old != null) {
			old.removePropertyChangeListener(this);
			oldRoute = old.toString();
		}
		if (route != null) {
			route.addPropertyChangeListener(this);
			newRoute = route.toString();
		}
		_route = route;
		_skipLocationsList.clear();
		if (old == null || !old.equals(route)) {
			setDirtyAndFirePropertyChange(TRAIN_ROUTE_CHANGED_PROPERTY, oldRoute, newRoute);
		}
	}

	/**
	 * Gets the train's route
	 * 
	 * @return train's route
	 */
	public Route getRoute() {
		return _route;
	}

	/**
	 * Get's the train's route name.
	 * 
	 * @return Train's route name.
	 */
	public String getTrainRouteName() {
		if (_route == null)
			return "";
		return _route.getName();
	}

	/**
	 * Get the train's departure location's name
	 * 
	 * @return train's departure location's name
	 */
	public String getTrainDepartsName() {
		if (getTrainDepartsRouteLocation() != null) {
			return getTrainDepartsRouteLocation().getName();
		}
		return "";
	}

	protected RouteLocation getTrainDepartsRouteLocation() {
		if (_route == null)
			return null;
		return _route.getDepartsRouteLocation();
	}

	/**
	 * Get train's final location's name
	 * 
	 * @return train's final location's name
	 */
	public String getTrainTerminatesName() {
		if (getTrainTerminatesRouteLocation() != null) {
			return getTrainTerminatesRouteLocation().getName();
		}
		return "";
	}

	protected RouteLocation getTrainTerminatesRouteLocation() {
		if (_route == null) {
			return null;
		}
		List<String> list = _route.getLocationsBySequenceList();
		if (list.size() > 0) {
			RouteLocation rl = _route.getLocationById(list.get(list.size() - 1));
			return rl;
		}
		return null;
	}

	/**
	 * Set train's current route location
	 */
	protected void setCurrentLocation(RouteLocation location) {
		RouteLocation old = _current;
		_current = location;
		if ((old != null && !old.equals(location)) || (old == null && location != null)) {
			setDirtyAndFirePropertyChange("current", old, location); // NOI18N
		}
	}

	/**
	 * Get train's current location name
	 * 
	 * @return Train's current route location name
	 */
	public String getCurrentLocationName() {
		if (getCurrentLocation() == null)
			return "";
		return getCurrentLocation().getName();
	}

	/**
	 * Get train's current location
	 * 
	 * @return Train's current route location
	 */
	public RouteLocation getCurrentLocation() {
		if (getRoute() == null)
			return null;
		if (_current == null)
			return null;
		// this will verify that the current location still exists
		return getRoute().getLocationById(_current.getId());
	}

	/**
	 * Get the train's next location name
	 * 
	 * @return Train's next route location name
	 */
	public String getNextLocationName() {
		return getNextLocationName(1);
	}

	/**
	 * Get a location name in a train's route from the current train's location. A number of "1" means get the next
	 * location name in a train's route.
	 * 
	 * @param number
	 *            The stop number, must be greater than 0
	 * @return Name of the location that is the number of stops away from the train's current location.
	 */
	public String getNextLocationName(int number) {
		RouteLocation rl = getCurrentLocation();
		while (number-- > 0) {
			rl = getNextLocation(rl);
			if (rl == null)
				return "";
		}
		return rl.getName();
	}

	public RouteLocation getNextLocation(RouteLocation rlc) {
		if (getRoute() == null)
			return null;
		List<String> routeList = getRoute().getLocationsBySequenceList();
		for (int i = 0; i < routeList.size(); i++) {
			RouteLocation rl = getRoute().getLocationById(routeList.get(i));
			if (rl == rlc) {
				i++;
				if (i < routeList.size()) {
					rl = getRoute().getLocationById(routeList.get(i));
					return rl;
				}
				break;
			}
		}
		return null; // At end of route
	}

	public void setDepartureTrack(Track track) {
		Track old = _departureTrack;
		_departureTrack = track;
		if (old != track) {
			setDirtyAndFirePropertyChange("DepartureTrackChanged", old, track); // NOI18N
		}
	}

	public Track getDepartureTrack() {
		return _departureTrack;
	}

	public void setTerminationTrack(Track track) {
		Track old = _terminationTrack;
		_terminationTrack = track;
		if (old != track) {
			setDirtyAndFirePropertyChange("TerminationTrackChanged", old, track); // NOI18N
		}
	}

	public Track getTerminationTrack() {
		return _terminationTrack;
	}

	/**
	 * Set's the train's status
	 * 
	 * @param status
	 *            BUILDING PARTIALBUILT BUILT TERMINATED TRAINRESET TRAININROUTE
	 */
	public void setStatus(String status) {
		String old = _status;
		_status = status;
		if (!old.equals(status)) {
			setDirtyAndFirePropertyChange(STATUS_CHANGED_PROPERTY, old, status);
		}
	}

	/**
	 * Get train's status
	 * 
	 * @return Train's status BUILDING PARTIALBUILT BUILT TERMINATED TRAINRESET TRAININROUTE
	 */
	public String getStatus() {
		return _status;
	}

	/**
	 * Used to determine if train has departed its
	 * 
	 * @return true if train is in route
	 */
	public boolean isTrainInRoute() {
		return !getCurrentLocationName().equals("") && getTrainDepartsRouteLocation() != getCurrentLocation();
	}

	/**
	 * Used to determine if train is a local switcher serving one location. Note the the train can have more than
	 * location in its route, but all location names must be same.
	 * 
	 * @return true if local switcher
	 */
	public boolean isLocalSwitcher() {
		String departureName = TrainCommon.splitString(getTrainDepartsName());
		Route route = getRoute();
		if (route != null) {
			List<String> routeList = route.getLocationsBySequenceList();
			for (int i = 0; i < routeList.size(); i++) {
				String name = TrainCommon.splitString(route.getLocationById(routeList.get(i)).getName());
				if (!departureName.equals(name))
					return false; // not a local switcher
			}
		}
		return true;
	}

	List<String> _skipLocationsList = new ArrayList<String>();

	protected String[] getTrainSkipsLocations() {
		String[] locationIds = new String[_skipLocationsList.size()];
		for (int i = 0; i < _skipLocationsList.size(); i++)
			locationIds[i] = _skipLocationsList.get(i);
		return locationIds;
	}

	protected void setTrainSkipsLocations(String[] locationIds) {
		if (locationIds.length == 0)
			return;
		jmri.util.StringUtil.sort(locationIds);
		for (int i = 0; i < locationIds.length; i++)
			_skipLocationsList.add(locationIds[i]);
	}

	/**
	 * Train will skip the RouteLocation
	 * 
	 * @param locationId
	 *            RouteLocation Id
	 */
	public void addTrainSkipsLocation(String locationId) {
		// insert at start of _skipLocationsList, sort later
		if (_skipLocationsList.contains(locationId))
			return;
		_skipLocationsList.add(0, locationId);
		log.debug("train does not stop at " + locationId);
		setDirtyAndFirePropertyChange(STOPS_CHANGED_PROPERTY, _skipLocationsList.size() - 1,
				_skipLocationsList.size());
	}

	public void deleteTrainSkipsLocation(String locationId) {
		_skipLocationsList.remove(locationId);
		log.debug("train will stop at " + locationId);
		setDirtyAndFirePropertyChange(STOPS_CHANGED_PROPERTY, _skipLocationsList.size() + 1,
				_skipLocationsList.size());
	}

	/**
	 * Determines if this train skips a location (doesn't service the location).
	 * 
	 * @param locationId
	 *            The route location id.
	 * @return true if the train will not service the location.
	 */
	public boolean skipsLocation(String locationId) {
		return _skipLocationsList.contains(locationId);
	}

	List<String> _typeList = new ArrayList<String>();

	/**
	 * Get's the type names of rolling stock this train will service
	 * 
	 * @return The type names for cars and or engines
	 */
	protected String[] getTypeNames() {
		String[] types = new String[_typeList.size()];
		for (int i = 0; i < _typeList.size(); i++)
			types[i] = _typeList.get(i);
		return types;
	}
	
	protected String[] getCarTypeNames() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < _typeList.size(); i++) {
			if (CarTypes.instance().containsName(_typeList.get(i)))
				list.add(_typeList.get(i));
		}
		String[] types = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
			types[i] = list.get(i);
		return types;
	}
	
	protected String[] getLocoTypeNames() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < _typeList.size(); i++) {
			if (EngineTypes.instance().containsName(_typeList.get(i)))
				list.add(_typeList.get(i));
		}
		String[] types = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
			types[i] = list.get(i);
		return types;
	}

	/**
	 * Set the type of cars or engines this train will service, see types in Cars and Engines.
	 * 
	 * @param types
	 *            The type names for cars and or engines
	 */
	protected void setTypeNames(String[] types) {
		if (types.length == 0)
			return;
		jmri.util.StringUtil.sort(types);
		for (int i = 0; i < types.length; i++)
			_typeList.add(types[i]);
	}

	/**
	 * Add a car or engine type name that this train will service.
	 * 
	 * @param type
	 *            The new type name to service.
	 */
	public void addTypeName(String type) {
		// insert at start of list, sort later
		if (_typeList.contains(type))
			return;
		_typeList.add(0, type);
		log.debug("train " + getName() + " add car type " + type);
		setDirtyAndFirePropertyChange(TYPES_CHANGED_PROPERTY, _typeList.size() - 1, _typeList.size());
	}

	public void deleteTypeName(String type) {
		if (!_typeList.contains(type))
			return;
		_typeList.remove(type);
		log.debug("train " + getName() + " delete car type " + type);
		setDirtyAndFirePropertyChange(TYPES_CHANGED_PROPERTY, _typeList.size() + 1, _typeList.size());
	}

	/**
	 * Returns true if this train will service the type of car or engine.
	 * 
	 * @param type
	 *            The car or engine type name.
	 * @return true if this train will service the particular type.
	 */
	public boolean acceptsTypeName(String type) {
		return _typeList.contains(type);
	}

	public void replaceType(String oldType, String newType) {
		if (acceptsTypeName(oldType)) {
			deleteTypeName(oldType);
			addTypeName(newType);
			// adjust custom loads
			String[] loadNames = getLoadNames();
			for (int k = 0; k < loadNames.length; k++) {
				String load = loadNames[k];
				String[] splitLoad = load.split(CarLoad.SPLIT_CHAR);
				if (splitLoad.length > 1) {
					if (splitLoad[0].equals(oldType)) {
						deleteLoadName(load);
						if (newType != null) {
							load = newType + CarLoad.SPLIT_CHAR + splitLoad[1];
							addLoadName(load);
						}
					}
				}
			}
		}
	}

	/**
	 * Get how this train deals with road names.
	 * 
	 * @return ALLROADS INCLUDEROADS EXCLUDEROADS
	 */
	public String getRoadOption() {
		return _roadOption;
	}

	/**
	 * Set how this train deals with car road names.
	 * 
	 * @param option
	 *            ALLROADS INCLUDEROADS EXCLUDEROADS
	 */
	public void setRoadOption(String option) {
		String old = _roadOption;
		_roadOption = option;
		setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, old, option);
	}

	List<String> _roadList = new ArrayList<String>();

	protected void setRoadNames(String[] roads) {
		if (roads.length == 0)
			return;
		jmri.util.StringUtil.sort(roads);
		for (int i = 0; i < roads.length; i++) {
			if (!roads[i].equals(""))
				_roadList.add(roads[i]);
		}
	}

	/**
	 * Provides a list of road names that the train will either service or exclude. See setRoadOption
	 * 
	 * @return Array of road names as Strings
	 */
	public String[] getRoadNames() {
		String[] roads = new String[_roadList.size()];
		for (int i = 0; i < _roadList.size(); i++)
			roads[i] = _roadList.get(i);
		if (_roadList.size() == 0)
			return roads;
		jmri.util.StringUtil.sort(roads);
		return roads;
	}

	/**
	 * Add a road name that the train will either service or exclude. See setRoadOption
	 * 
	 * @return true if road name was added, false if road name wasn't in the list.
	 */
	public boolean addRoadName(String road) {
		if (_roadList.contains(road))
			return false;
		_roadList.add(road);
		log.debug("train (" + getName() + ") add car road " + road);
		setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, _roadList.size() - 1, _roadList.size());
		return true;
	}

	/**
	 * Delete a road name that the train will either service or exclude. See setRoadOption
	 * 
	 * @return true if road name was removed, false if road name wasn't in the list.
	 */
	public boolean deleteRoadName(String road) {
		if (!_roadList.contains(road))
			return false;
		_roadList.remove(road);
		log.debug("train (" + getName() + ") delete car road " + road);
		setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, _roadList.size() + 1, _roadList.size());
		return true;
	}

	/**
	 * Determine if train will service a specific road name.
	 * 
	 * @param road
	 *            the road name to check.
	 * @return true if train will service this road name.
	 */
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

	private void replaceRoad(String oldRoad, String newRoad) {
		if (newRoad != null) {
			if (deleteRoadName(oldRoad))
				addRoadName(newRoad);
			if (getEngineRoad().equals(oldRoad))
				setEngineRoad(newRoad);
			if (getCabooseRoad().equals(oldRoad))
				setCabooseRoad(newRoad);
			if (getSecondLegEngineRoad().equals(oldRoad))
				setSecondLegEngineRoad(newRoad);
			if (getSecondLegCabooseRoad().equals(oldRoad))
				setSecondLegCabooseRoad(newRoad);
			if (getThirdLegEngineRoad().equals(oldRoad))
				setThirdLegEngineRoad(newRoad);
			if (getThirdLegCabooseRoad().equals(oldRoad))
				setThirdLegCabooseRoad(newRoad);
		}
	}

	/**
	 * Gets the car load option for this train.
	 * 
	 * @return ALLLOADS INCLUDELOADS EXCLUDELOADS
	 */
	public String getLoadOption() {
		return _loadOption;
	}

	/**
	 * Set how this train deals with car loads
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

	protected void setLoadNames(String[] loads) {
		if (loads.length == 0)
			return;
		jmri.util.StringUtil.sort(loads);
		for (int i = 0; i < loads.length; i++) {
			if (!loads[i].equals(""))
				_loadList.add(loads[i]);
		}
	}

	/**
	 * Provides a list of loads that the train will either service or exclude. See setLoadOption
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
	 * Add a load that the train will either service or exclude. See setLoadOption
	 * 
	 * @return true if load name was added, false if load name wasn't in the list.
	 */
	public boolean addLoadName(String load) {
		if (_loadList.contains(load))
			return false;
		_loadList.add(load);
		log.debug("train (" + getName() + ") add car load " + load);
		setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, _loadList.size() - 1, _loadList.size());
		return true;
	}

	/**
	 * Delete a load name that the train will either service or exclude. See setLoadOption
	 * 
	 * @return true if load name was removed, false if load name wasn't in the list.
	 */
	public boolean deleteLoadName(String load) {
		if (!_loadList.contains(load))
			return false;
		_loadList.remove(load);
		log.debug("train (" + getName() + ") delete car load " + load);
		setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, _loadList.size() + 1, _loadList.size());
		return true;
	}

	/**
	 * Determine if train will service a specific load name.
	 * 
	 * @param load
	 *            the load name to check.
	 * @return true if train will service this load.
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
	 * Determine if train will service a specific load and car type.
	 * 
	 * @param load
	 *            the load name to check.
	 * @param type
	 *            the type of car used to carry the load.
	 * @return true if train will service this load.
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

	public String getOwnerOption() {
		return _ownerOption;
	}

	/**
	 * Set how this train deals with car owner names
	 * 
	 * @param option
	 *            ALLOWNERS INCLUDEOWNERS EXCLUDEOWNERS
	 */
	public void setOwnerOption(String option) {
		String old = _ownerOption;
		_ownerOption = option;
		setDirtyAndFirePropertyChange(OWNERS_CHANGED_PROPERTY, old, option);
	}

	List<String> _ownerList = new ArrayList<String>();

	protected void setOwnerNames(String[] owners) {
		if (owners.length == 0)
			return;
		jmri.util.StringUtil.sort(owners);
		for (int i = 0; i < owners.length; i++) {
			if (!owners[i].equals(""))
				_ownerList.add(owners[i]);
		}
	}

	/**
	 * Provides a list of owner names that the train will either service or exclude. See setOwnerOption
	 * 
	 * @return Array of owner names as Strings
	 */
	public String[] getOwnerNames() {
		String[] owners = new String[_ownerList.size()];
		for (int i = 0; i < _ownerList.size(); i++)
			owners[i] = _ownerList.get(i);
		if (_ownerList.size() == 0)
			return owners;
		jmri.util.StringUtil.sort(owners);
		return owners;
	}

	/**
	 * Add a owner name that the train will either service or exclude. See setOwnerOption
	 * 
	 * @return true if owner name was added, false if owner name wasn't in the list.
	 */
	public boolean addOwnerName(String owner) {
		if (_ownerList.contains(owner))
			return false;
		_ownerList.add(owner);
		log.debug("train (" + getName() + ") add car owner " + owner);
		setDirtyAndFirePropertyChange(OWNERS_CHANGED_PROPERTY, _ownerList.size() - 1, _ownerList.size());
		return true;
	}

	/**
	 * Delete a owner name that the train will either service or exclude. See setOwnerOption
	 * 
	 * @return true if owner name was removed, false if owner name wasn't in the list.
	 */
	public boolean deleteOwnerName(String owner) {
		if (!_ownerList.contains(owner))
			return false;
		_ownerList.remove(owner);
		log.debug("train (" + getName() + ") delete car owner " + owner);
		setDirtyAndFirePropertyChange(OWNERS_CHANGED_PROPERTY, _ownerList.size() + 1, _ownerList.size());
		return true;
	}

	/**
	 * Determine if train will service a specific owner name.
	 * 
	 * @param owner
	 *            the owner name to check.
	 * @return true if train will service this owner name.
	 */
	public boolean acceptsOwnerName(String owner) {
		if (_ownerOption.equals(ALLOWNERS)) {
			return true;
		}
		if (_ownerOption.equals(INCLUDEOWNERS)) {
			return _ownerList.contains(owner);
		}
		// exclude!
		return !_ownerList.contains(owner);
	}

	public void replaceOwner(String oldName, String newName) {
		if (deleteOwnerName(oldName))
			addOwnerName(newName);
	}

	/**
	 * Only rolling stock built in or after this year will be used.
	 * 
	 * @param year
	 */
	public void setBuiltStartYear(String year) {
		String old = _builtStartYear;
		_builtStartYear = year;
		if (!old.equals(year))
			setDirtyAndFirePropertyChange(BUILT_YEAR_CHANGED_PROPERTY, old, year);
	}

	public String getBuiltStartYear() {
		return _builtStartYear;
	}

	/**
	 * Only rolling stock built in or before this year will be used.
	 * 
	 * @param year
	 */
	public void setBuiltEndYear(String year) {
		String old = _builtEndYear;
		_builtEndYear = year;
		if (!old.equals(year))
			setDirtyAndFirePropertyChange(BUILT_YEAR_CHANGED_PROPERTY, old, year);
	}

	public String getBuiltEndYear() {
		return _builtEndYear;
	}

	/**
	 * Determine if train will service rolling stock by built date.
	 * 
	 * @param date
	 * @return true is built date is in the acceptable range.
	 */
	public boolean acceptsBuiltDate(String date) {
		if (getBuiltStartYear().equals("") && getBuiltEndYear().equals(""))
			return true; // range dates not defined
		int s = 0; // default start year;
		int e = 99999; // default end year;
		try {
			s = Integer.parseInt(getBuiltStartYear());
		} catch (NumberFormatException e1) {
			log.debug("Train (" + getName() + ") built start date not initialized, start: "
					+ getBuiltStartYear());
		}
		try {
			e = Integer.parseInt(getBuiltEndYear());
		} catch (NumberFormatException e1) {
			log.debug("Train (" + getName() + ") built end date not initialized, end: " + getBuiltEndYear());
		}
		try {
			int d = Integer.parseInt(date);
			if (s < d && d < e)
				return true;
			else
				return false;
		} catch (NumberFormatException e1) {
			// log.debug("Built date: "+date+" isn't an integer");
			// maybe the built date is in the format month-year
			String[] built = date.split("-");
			if (built.length > 1)
				try {
					int d = Integer.parseInt(built[1]);
					if (d < 100)
						d = d + 1900;
					if (s < d && d < e)
						return true;
					else
						return false;
				} catch (NumberFormatException e2) {
					log.debug("Unable to parse car built date " + date);
				}
			return false;
		}
	}

	private boolean debugFlag = false;

	/**
	 * Determines if this train will service this car. Note this code doesn't check the location or tracks that needs to
	 * be done separately. See Router.java.
	 * 
	 * @param rs
	 *            The car to be tested.
	 * @return true if this train can service the car.
	 */
	public boolean servicesCar(RollingStock rs) {
		if (!acceptsTypeName(rs.getType()) || !acceptsBuiltDate(rs.getBuilt())
				|| !acceptsOwnerName(rs.getOwner()) || !acceptsRoadName(rs.getRoad())) {
			if (debugFlag)
				log.debug("Car (" + rs.toString() + ") not serviced by train "+getName());
			return false;
		}
		int length = Integer.parseInt(rs.getLength()) + Car.COUPLER;
		// if car, check to see if train accepts car load
		if (Car.class.isInstance(rs)) {
			Car car = (Car) rs;
			if (!acceptsLoad(car.getLoad(), car.getType()))
				return false;
			if (!isAllowThroughCarsEnabled() && !isLocalSwitcher() && !car.isCaboose()
					&& !car.hasFred() && !car.isPassenger()
					&& TrainCommon.splitString(car.getLocationName()).equals(TrainCommon.splitString(getTrainDepartsName()))
					&& TrainCommon.splitString(car.getDestinationName()).equals(TrainCommon.splitString(getTrainTerminatesName()))) {
				if (debugFlag)
					log.debug("Through car (" + rs.toString() + ") not allowed");
				return false;
			}
			// car can be a kernel so get total length
			if (car.getKernel() != null)
				length = car.getKernel().getLength();
		}
		Route route = getRoute();
		if (route != null) {
			List<String> rLocations = route.getLocationsBySequenceList();
			// single location in this train's route?
			if (rLocations.size() == 1) {
				RouteLocation rLoc = route.getLocationById(rLocations.get(0));
				if (rLoc.getName().equals(rs.getLocationName()) && rLoc.canPickup()
						&& rLoc.getMaxCarMoves() > 0 && !skipsLocation(rLoc.getId())
						&& rLoc.getName().equals(rs.getDestinationName()) && rLoc.canDrop()) {
					if (rs.getTrack() != null && !rs.getTrack().acceptsPickupTrain(this)) {
						return false;
					}
					if (rs.getDestinationTrack() != null && !rs.getDestinationTrack().acceptsDropTrain(this)) {
						return false;
					}
					if (debugFlag)
						log.debug("Local switcher " + getName() + " for location " + rLoc.getName());
					return true;
				}
				return false;
			}
			// Multiple locations in the train's route
			for (int j = 0; j < rLocations.size() - 1; j++) {
				RouteLocation rLoc = route.getLocationById(rLocations.get(j));
				if (rLoc.getName().equals(rs.getLocationName()) && rLoc.canPickup()
						&& rLoc.getMaxCarMoves() > 0 && !skipsLocation(rLoc.getId())
						&& (rs.getLocation().getTrainDirections() & rLoc.getTrainDirection()) > 0) {
					if (rs.getTrack() != null) {
						if ((rs.getTrack().getTrainDirections() & rLoc.getTrainDirection()) == 0
								|| !rs.getTrack().acceptsPickupTrain(this))
							continue;
					}
					if (debugFlag)
						log.debug("Car (" + rs.toString() + ") can be picked up by train (" + getName()
								+ ") from (" + rs.getLocationName() + ", " + rs.getTrackName() + ")"); // NOI18N
					if (rs.getDestination() == null) {
						if (debugFlag)
							log.debug("Car (" + rs.toString() + ") does not have a destination");
						return true;
					}
					// now check car's destination
					for (int k = j; k < rLocations.size(); k++) {
						rLoc = route.getLocationById(rLocations.get(k));
						if (rLoc.getName().equals(rs.getDestinationName()) && rLoc.canDrop()
								&& rLoc.getMaxCarMoves() > 0 && !skipsLocation(rLoc.getId())
								&& (rs.getDestination().getTrainDirections() & rLoc.getTrainDirection()) > 0) {
							// found a destination, now check destination track
							if (rs.getDestinationTrack() != null) {
								if ((rs.getDestinationTrack().getTrainDirections() & rLoc.getTrainDirection()) == 0
										|| !rs.getDestinationTrack().acceptsDropTrain(this))
									continue;
							} else if (rLoc.getLocation().getLocationOps() == Location.STAGING
									&& getStatus().equals(BUILDING) && getTerminationTrack() != null
									&& getTerminationTrack().getLocation() == rLoc.getLocation()) {
								if (debugFlag)
									log.debug("Car (" + rs.toString()
											+ ") destination is staging, check train (" + getName() // NOI18N
											+ ") termination track (" + getTerminationTrack().getName() // NOI18N
											+ ")");
								String status = rs.testDestination(getTerminationTrack().getLocation(),
										getTerminationTrack());
								if (!status.equals(Track.OKAY))
									continue;
							} else {
								// determine if there's a track that is willing to accept this car
								String status = "";
								List<String> trackIds = rLoc.getLocation().getTrackIdsByIdList();
								for (int i = 0; i < trackIds.size(); i++) {
									Track track = rLoc.getLocation().getTrackById(trackIds.get(i));
									if ((track.getTrainDirections() & rLoc.getTrainDirection()) == 0
											|| !track.acceptsDropTrain(this))
										continue;
									// will the track accept this car?
									status = track.accepts(rs);
									if (status.equals(Track.OKAY) || status.startsWith(Track.LENGTH))
										break; // yes, done
								}
								if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
									if (debugFlag)
										log.debug("Destination ("+rs.getDestinationName()+") can not service car ("+rs.toString()+") using train ("+getName()+")");
									continue;
								}
							}
							// is this a local move?
							if (rs.getLocationName().equals(rs.getDestinationName())
									&& !isAllowLocalMovesEnabled()) {
								if (debugFlag)
									log.debug("Local moves is disabled");
								continue;
							}
							// check to see if moves are available
							if (getStatus().equals(BUILDING)
									&& rLoc.getMaxCarMoves() - rLoc.getCarMoves() <= 0) {
								if (debugFlag)
									log.debug("No available moves for destination " + rLoc.getName());
								continue;
							}
							if (debugFlag)
								log.debug("Car (" + rs.toString() + ") can be dropped by train (" + getName()
										+ ") to (" + rs.getDestinationName() + ", " // NOI18N
										+ rs.getDestinationTrackName() + ")");
							return true;
						}
						// check to see if train length is okay
						if (getStatus().equals(BUILDING)
								&& rLoc.getTrainLength() + length > rLoc.getMaxTrainLength()) {
							if (debugFlag)
								log.debug("Car (" + rs.toString()
										+ ") exceeds maximum train length when departing (" // NOI18N
										+ rLoc.getName() + ")");
							return false;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @return The number of cars worked by this train
	 */
	public int getNumberCarsWorked() {
		List<String> cars = CarManager.instance().getByTrainList(this);
		// remove cars that haven't been assigned by the train builder
		for (int i = 0; i < cars.size(); i++) {
			Car c = CarManager.instance().getById(cars.get(i));
			if (c.getRouteLocation() == null) {
				cars.remove(i--);
			}
		}
		return cars.size();
	}

	/**
	 * Gets the number of cars in the train at the current location in the train's route.
	 * 
	 * @return The number of cars currently in the train
	 */
	public int getNumberCarsInTrain() {
		RouteLocation rl = getCurrentLocation();
		int number = 0;
		List<String> cars = CarManager.instance().getByTrainList(this);
		// remove cars that aren't in the train
		for (int i = 0; i < cars.size(); i++) {
			Car car = CarManager.instance().getById(cars.get(i));
			if (car.getRouteLocation() == rl && car.getRouteDestination() != rl) {
				number++;
			}
		}
		return number;
	}

	
	/**
	 * Gets the number of cars in the train when train departs the route location.
	 * 
	 * @return The number of cars in the train departing the route location.
	 */
	public int getNumberCarsInTrain(RouteLocation routeLocation) {
		int number = 0;
		Route route = getRoute();
		if (route != null) {
			List<String> cars = CarManager.instance().getByTrainList(this);
			List<String> ids = route.getLocationsBySequenceList();
			for (int i = 0; i < ids.size(); i++) {
				RouteLocation rl = route.getLocationById(ids.get(i));
				for (int j = 0; j < cars.size(); j++) {
					Car car = CarManager.instance().getById(cars.get(j));
					if (car.getRouteLocation() == rl) {
						number++;
					}
					if (car.getRouteDestination() == rl) {
						number--;
					}
				}
				if (rl == routeLocation)
					break;
			}
		}
	
		return number;
	}
	
	/**
	 * Gets the number of empty cars in the train when train departs the route location.
	 * 
	 * @return The number of empty cars in the train departing the route location.
	 */
	public int getNumberEmptyCarsInTrain(RouteLocation routeLocation) {
		int number = 0;
		Route route = getRoute();
		if (route != null) {
			List<String> cars = CarManager.instance().getByTrainList(this);
			List<String> ids = route.getLocationsBySequenceList();
			for (int i = 0; i < ids.size(); i++) {
				RouteLocation rl = route.getLocationById(ids.get(i));
				for (int j = 0; j < cars.size(); j++) {
					Car car = CarManager.instance().getById(cars.get(j));
					if (!CarLoads.instance().getLoadType(car.getType(), car.getLoad()).equals(
							CarLoad.LOAD_TYPE_EMPTY))
						continue;
					if (car.getRouteLocation() == rl) {
						number++;
					}
					if (car.getRouteDestination() == rl) {
						number--;
					}
				}
				if (rl == routeLocation)
					break;
			}
		}
	
		return number;
	}


	/**
	 * Gets the train's length at the current location in the train's route.
	 * 
	 * @return The train length at this location
	 */
	public int getTrainLength() {
		RouteLocation rl = getCurrentLocation();
		int length = 0;
		List<String> engines = EngineManager.instance().getByTrainList(this);
		for (int i = 0; i < engines.size(); i++) {
			Engine eng = EngineManager.instance().getById(engines.get(i));
			if (eng.getRouteLocation() == rl && eng.getRouteDestination() != rl) {
				length = length + Integer.parseInt(eng.getLength()) + Engine.COUPLER;
			}
		}
		List<String> cars = CarManager.instance().getByTrainList(this);
		for (int i = 0; i < cars.size(); i++) {
			Car car = CarManager.instance().getById(cars.get(i));
			if (car.getRouteLocation() == rl && car.getRouteDestination() != rl) {
				length = length + Integer.parseInt(car.getLength()) + Car.COUPLER;
			}
		}
		return length;
	}

	/**
	 * Gets the train's length at the route location specified
	 * 
	 * @param routeLocation
	 *            The route location
	 * @return The train length at the route location
	 */
	public int getTrainLength(RouteLocation routeLocation) {
		int length = 0;
		Route route = getRoute();
		if (route != null) {
			List<String> engines = EngineManager.instance().getByTrainList(this);
			List<String> cars = CarManager.instance().getByTrainList(this);
			List<String> ids = route.getLocationsBySequenceList();
			for (int i = 0; i < ids.size(); i++) {
				RouteLocation rl = route.getLocationById(ids.get(i));
				for (int j = 0; j < engines.size(); j++) {
					Engine eng = EngineManager.instance().getById(engines.get(j));
					if (eng.getRouteLocation() == rl) {
						length += Integer.parseInt(eng.getLength()) + Engine.COUPLER;
					}
					if (eng.getRouteDestination() == rl) {
						length += -(Integer.parseInt(eng.getLength()) + Engine.COUPLER);
					}
				}
				for (int j = 0; j < cars.size(); j++) {
					Car car = CarManager.instance().getById(cars.get(j));
					if (car.getRouteLocation() == rl) {
						length += Integer.parseInt(car.getLength()) + Car.COUPLER;
					}
					if (car.getRouteDestination() == rl) {
						length += -(Integer.parseInt(car.getLength()) + Car.COUPLER);
					}
				}
				if (rl == routeLocation)
					break;
			}
		}
		return length;
	}

	/**
	 * Get the train's weight at the current location.
	 * 
	 * @return Train's weight in tons.
	 */
	public int getTrainWeight() {
		RouteLocation rl = getCurrentLocation();
		int weight = 0;
		List<String> engines = EngineManager.instance().getByTrainList(this);
		for (int i = 0; i < engines.size(); i++) {
			Engine eng = EngineManager.instance().getById(engines.get(i));
			if (eng.getRouteLocation() == rl && eng.getRouteDestination() != rl) {
				weight = weight + eng.getAdjustedWeightTons();
			}
		}
		List<String> cars = CarManager.instance().getByTrainList(this);
		for (int i = 0; i < cars.size(); i++) {
			Car car = CarManager.instance().getById(cars.get(i));
			if (car.getRouteLocation() == rl && car.getRouteDestination() != rl) {
				weight = weight + car.getAdjustedWeightTons();
			}
		}
		return weight;
	}

	public int getTrainWeight(RouteLocation routeLocation) {
		int weight = 0;
		Route route = getRoute();
		if (route != null) {
			List<String> engines = EngineManager.instance().getByTrainList(this);
			List<String> cars = CarManager.instance().getByTrainList(this);
			List<String> ids = route.getLocationsBySequenceList();
			for (int i = 0; i < ids.size(); i++) {
				RouteLocation rl = route.getLocationById(ids.get(i));
				for (int j = 0; j < engines.size(); j++) {
					Engine eng = EngineManager.instance().getById(engines.get(j));
					if (eng.getRouteLocation() == rl) {
						weight += eng.getAdjustedWeightTons();
					}
					if (eng.getRouteDestination() == rl) {
						weight += -eng.getAdjustedWeightTons();
					}
				}
				for (int j = 0; j < cars.size(); j++) {
					Car car = CarManager.instance().getById(cars.get(j));
					if (car.getRouteLocation() == rl) {
						weight += car.getAdjustedWeightTons();
					}
					if (car.getRouteDestination() == rl) {
						weight += -car.getAdjustedWeightTons();
					}
				}
				if (rl == routeLocation)
					break;
			}
		}
		return weight;
	}

	/**
	 * Gets the current caboose road and number if there's one assigned to the train.
	 * 
	 * @return Road and number of caboose.
	 */
	public String getCabooseRoadAndNumber() {
		String cabooseRoadNumber = "";
		RouteLocation rl = getCurrentLocation();
		List<String> cars = CarManager.instance().getByTrainList(this);
		for (int i = 0; i < cars.size(); i++) {
			Car car = CarManager.instance().getById(cars.get(i));
			if (car.getRouteLocation() == rl && car.getRouteDestination() != rl && car.isCaboose())
				cabooseRoadNumber = car.toString();
		}
		return cabooseRoadNumber;
	}

	public void setDescription(String description) {
		String old = _description;
		_description = description;
		if (!old.equals(description)) {
			setDirtyAndFirePropertyChange(DESCRIPTION_CHANGED_PROPERTY, old, description);
		}
	}

	public String getDescription() {
		return _description;
	}

	public void setNumberEngines(String number) {
		String old = _numberEngines;
		_numberEngines = number;
		if (!old.equals(number))
			setDirtyAndFirePropertyChange("trainNmberEngines", old, number); // NOI18N
	}

	/**
	 * Get the number of engines that this train requires.
	 * 
	 * @return The number of engines that this train requires.
	 */
	public String getNumberEngines() {
		return _numberEngines;
	}

	/**
	 * Get the number of engines needed for the second set.
	 * 
	 * @return The number of engines needed in route
	 */
	public String getSecondLegNumberEngines() {
		return _leg2Engines;
	}

	public void setSecondLegNumberEngines(String number) {
		String old = _leg2Engines;
		_leg2Engines = number;
		if (!old.equals(number))
			setDirtyAndFirePropertyChange("trainNmberEngines", old, number); // NOI18N
	}

	/**
	 * Get the number of engines needed for the third set.
	 * 
	 * @return The number of engines needed in route
	 */
	public String getThirdLegNumberEngines() {
		return _leg3Engines;
	}

	public void setThirdLegNumberEngines(String number) {
		String old = _leg3Engines;
		_leg3Engines = number;
		if (!old.equals(number))
			setDirtyAndFirePropertyChange("trainNmberEngines", old, number); // NOI18N
	}

	/**
	 * Set the road name of engines servicing this train.
	 * 
	 * @param road
	 *            The road name of engines servicing this train.
	 */
	public void setEngineRoad(String road) {
		String old = _engineRoad;
		_engineRoad = road;
		if (!old.equals(road))
			setDirtyAndFirePropertyChange("trainEngineRoad", old, road); // NOI18N
	}

	/**
	 * Get the road name of engines servicing this train.
	 * 
	 * @return The road name of engines servicing this train.
	 */
	public String getEngineRoad() {
		return _engineRoad;
	}

	/**
	 * Set the road name of engines servicing this train 2nd leg.
	 * 
	 * @param road
	 *            The road name of engines servicing this train.
	 */
	public void setSecondLegEngineRoad(String road) {
		String old = _leg2Road;
		_leg2Road = road;
		if (!old.equals(road))
			setDirtyAndFirePropertyChange("trainEngineRoad", old, road); // NOI18N
	}

	/**
	 * Get the road name of engines servicing this train 2nd leg.
	 * 
	 * @return The road name of engines servicing this train.
	 */
	public String getSecondLegEngineRoad() {
		return _leg2Road;
	}

	/**
	 * Set the road name of engines servicing this train 3rd leg.
	 * 
	 * @param road
	 *            The road name of engines servicing this train.
	 */
	public void setThirdLegEngineRoad(String road) {
		String old = _leg3Road;
		_leg3Road = road;
		if (!old.equals(road))
			setDirtyAndFirePropertyChange("trainEngineRoad", old, road); // NOI18N
	}

	/**
	 * Get the road name of engines servicing this train 3rd leg.
	 * 
	 * @return The road name of engines servicing this train.
	 */
	public String getThirdLegEngineRoad() {
		return _leg3Road;
	}

	/**
	 * Set the model name of engines servicing this train.
	 * 
	 * @param model
	 *            The model name of engines servicing this train.
	 */
	public void setEngineModel(String model) {
		String old = _engineModel;
		_engineModel = model;
		if (!old.equals(model))
			setDirtyAndFirePropertyChange("trainEngineModel", old, model); // NOI18N
	}

	public String getEngineModel() {
		return _engineModel;
	}

	/**
	 * Set the model name of engines servicing this train's 2nd leg.
	 * 
	 * @param model
	 *            The model name of engines servicing this train.
	 */
	public void setSecondLegEngineModel(String model) {
		String old = _leg2Model;
		_leg2Model = model;
		if (!old.equals(model))
			setDirtyAndFirePropertyChange("trainEngineModel", old, model); // NOI18N
	}

	public String getSecondLegEngineModel() {
		return _leg2Model;
	}

	/**
	 * Set the model name of engines servicing this train's 3rd leg.
	 * 
	 * @param model
	 *            The model name of engines servicing this train.
	 */
	public void setThirdLegEngineModel(String model) {
		String old = _leg3Model;
		_leg3Model = model;
		if (!old.equals(model))
			setDirtyAndFirePropertyChange("trainEngineModel", old, model); // NOI18N
	}

	public String getThirdLegEngineModel() {
		return _leg3Model;
	}

	private void replaceModel(String oldModel, String newModel) {
		if (getEngineModel().equals(oldModel))
			setEngineModel(newModel);
		if (getSecondLegEngineModel().equals(oldModel))
			setSecondLegEngineModel(newModel);
		if (getThirdLegEngineModel().equals(oldModel))
			setThirdLegEngineModel(newModel);
	}

	/**
	 * Set the road name of the caboose servicing this train.
	 * 
	 * @param road
	 *            The road name of the caboose servicing this train.
	 */
	public void setCabooseRoad(String road) {
		String old = _cabooseRoad;
		_cabooseRoad = road;
		if (!old.equals(road))
			setDirtyAndFirePropertyChange("trainCabooseRoad", old, road); // NOI18N
	}

	public String getCabooseRoad() {
		return _cabooseRoad;
	}

	/**
	 * Set the road name of the second leg caboose servicing this train.
	 * 
	 * @param road
	 *            The road name of the caboose servicing this train's 2nd leg.
	 */
	public void setSecondLegCabooseRoad(String road) {
		String old = _leg2CabooseRoad;
		_leg2CabooseRoad = road;
		if (!old.equals(road))
			setDirtyAndFirePropertyChange("trainCabooseRoad", old, road); // NOI18N
	}

	public String getSecondLegCabooseRoad() {
		return _leg2CabooseRoad;
	}

	/**
	 * Set the road name of the third leg caboose servicing this train.
	 * 
	 * @param road
	 *            The road name of the caboose servicing this train's 3rd leg.
	 */
	public void setThirdLegCabooseRoad(String road) {
		String old = _leg3CabooseRoad;
		_leg3CabooseRoad = road;
		if (!old.equals(road))
			setDirtyAndFirePropertyChange("trainCabooseRoad", old, road); // NOI18N
	}

	public String getThirdLegCabooseRoad() {
		return _leg3CabooseRoad;
	}

	public void setSecondLegStartLocation(RouteLocation rl) {
		_leg2Start = rl;
	}

	public RouteLocation getSecondLegStartLocation() {
		return _leg2Start;
	}

	public String getSecondLegStartLocationName() {
		if (getSecondLegStartLocation() == null)
			return "";
		return getSecondLegStartLocation().getName();
	}

	public void setThirdLegStartLocation(RouteLocation rl) {
		_leg3Start = rl;
	}

	public RouteLocation getThirdLegStartLocation() {
		return _leg3Start;
	}

	public String getThirdLegStartLocationName() {
		if (getThirdLegStartLocation() == null)
			return "";
		return getThirdLegStartLocation().getName();
	}

	public void setSecondLegEndLocation(RouteLocation rl) {
		_end2Leg = rl;
	}

	public String getSecondLegEndLocationName() {
		if (getSecondLegEndLocation() == null)
			return "";
		return getSecondLegEndLocation().getName();
	}

	public RouteLocation getSecondLegEndLocation() {
		return _end2Leg;
	}

	public void setThirdLegEndLocation(RouteLocation rl) {
		_leg3End = rl;
	}

	public RouteLocation getThirdLegEndLocation() {
		return _leg3End;
	}

	public String getThirdLegEndLocationName() {
		if (getThirdLegEndLocation() == null)
			return "";
		return getThirdLegEndLocation().getName();
	}

	/**
	 * Optional changes to train while in route.
	 * 
	 * @param options
	 *            NONE, CHANGE_ENGINES, ADD_CABOOSE, HELPER_ENGINES, REMOVE_CABOOSE
	 */
	public void setSecondLegOptions(int options) {
		int old = _leg2Options;
		_leg2Options = options;
		if (old != options)
			setDirtyAndFirePropertyChange("trainLegOptions", old, options); // NOI18N
	}

	public int getSecondLegOptions() {
		return _leg2Options;
	}

	/**
	 * Optional changes to train while in route.
	 * 
	 * @param options
	 *            NONE, CHANGE_ENGINES, ADD_CABOOSE, HELPER_ENGINES, REMOVE_CABOOSE
	 */
	public void setThirdLegOptions(int options) {
		int old = _leg3Options;
		_leg3Options = options;
		if (old != options)
			setDirtyAndFirePropertyChange("trainLegOptions", old, options); // NOI18N
	}

	public int getThirdLegOptions() {
		return _leg3Options;
	}

	public void setComment(String comment) {
		String old = _comment;
		_comment = comment;
		if (!old.equals(comment))
			setDirtyAndFirePropertyChange("trainComment", old, comment); // NOI18N
	}

	public String getComment() {
		return _comment;
	}

	/**
	 * Add a script to run before a train is built
	 * 
	 * @param pathname
	 *            The script's pathname
	 */
	public void addBuildScript(String pathname) {
		_buildScripts.add(pathname);
		setDirtyAndFirePropertyChange("addBuildScript", pathname, null); // NOI18N
	}

	public void deleteBuildScript(String pathname) {
		_buildScripts.remove(pathname);
		setDirtyAndFirePropertyChange("deleteBuildScript", null, pathname); // NOI18N
	}

	/**
	 * Gets a list of pathnames (scripts) to run before this train is built
	 * 
	 * @return A list of pathnames to run before this train is built
	 */
	public List<String> getBuildScripts() {
		return _buildScripts;
	}

	/**
	 * Add a script to run after a train is built
	 * 
	 * @param pathname
	 *            The script's pathname
	 */
	public void addAfterBuildScript(String pathname) {
		_afterBuildScripts.add(pathname);
		setDirtyAndFirePropertyChange("addAfterBuildScript", pathname, null); // NOI18N
	}

	public void deleteAfterBuildScript(String pathname) {
		_afterBuildScripts.remove(pathname);
		setDirtyAndFirePropertyChange("deleteAfterBuildScript", null, pathname); // NOI18N
	}

	/**
	 * Gets a list of pathnames (scripts) to run after this train is built
	 * 
	 * @return A list of pathnames to run after this train is built
	 */
	public List<String> getAfterBuildScripts() {
		return _afterBuildScripts;
	}

	/**
	 * Add a script to run when train is moved
	 * 
	 * @param pathname
	 *            The script's pathname
	 */
	public void addMoveScript(String pathname) {
		_moveScripts.add(pathname);
		setDirtyAndFirePropertyChange("addMoveScript", pathname, null); // NOI18N
	}

	public void deleteMoveScript(String pathname) {
		_moveScripts.remove(pathname);
		setDirtyAndFirePropertyChange("deleteMoveScript", null, pathname); // NOI18N
	}

	/**
	 * Gets a list of pathnames (scripts) to run when this train moved
	 * 
	 * @return A list of pathnames to run when this train moved
	 */
	public List<String> getMoveScripts() {
		return _moveScripts;
	}

	/**
	 * Add a script to run when train is terminated
	 * 
	 * @param pathname
	 *            The script's pathname
	 */
	public void addTerminationScript(String pathname) {
		_terminationScripts.add(pathname);
		setDirtyAndFirePropertyChange("addTerminationScript", pathname, null); // NOI18N
	}

	public void deleteTerminationScript(String pathname) {
		_terminationScripts.remove(pathname);
		setDirtyAndFirePropertyChange("deleteTerminationScript", null, pathname); // NOI18N
	}

	/**
	 * Gets a list of pathnames (scripts) to run when this train terminates
	 * 
	 * @return A list of pathnames to run when this train terminates
	 */
	public List<String> getTerminationScripts() {
		return _terminationScripts;
	}

	/**
	 * Gets the optional railroad name for this train.
	 * 
	 * @return Train's railroad name.
	 */
	public String getRailroadName() {
		return _railroadName;
	}

	/**
	 * Overrides the default railroad name for this train.
	 * 
	 * @param name
	 *            The railroad name for this train.
	 */
	public void setRailroadName(String name) {
		String old = _railroadName;
		_railroadName = name;
		if (!old.equals(name))
			setDirtyAndFirePropertyChange("trainRailroadName", old, name); // NOI18N
	}

	public String getManifestLogoURL() {
		return _logoURL;
	}

	/**
	 * Overrides the default logo for this train.
	 * 
	 * @param pathName
	 *            file location for the logo.
	 */
	public void setManifestLogoURL(String pathName) {
		_logoURL = pathName;
	}

	public boolean isShowArrivalAndDepartureTimesEnabled() {
		return _showTimes;
	}

	public void setShowArrivalAndDepartureTimes(boolean enable) {
		boolean old = _showTimes;
		_showTimes = enable;
		if (old != enable) {
			setDirtyAndFirePropertyChange("showArrivalAndDepartureTimes", old ? "true" : "false", // NOI18N
					enable ? "true" : "false"); // NOI18N
		}
	}

	public boolean isSendCarsToTerminalEnabled() {
		return _sendToTerminal;
	}

	public void setSendCarsToTerminalEnabled(boolean enable) {
		boolean old = _sendToTerminal;
		_sendToTerminal = enable;
		if (old != enable) {
			setDirtyAndFirePropertyChange("send cars to terminal", old ? "true" : "false", enable ? "true" // NOI18N
					: "false"); // NOI18N
		}
	}

	public boolean isAllowLocalMovesEnabled() {
		return _allowLocalMoves;
	}

	public void setAllowLocalMovesEnabled(boolean enable) {
		boolean old = _allowLocalMoves;
		_allowLocalMoves = enable;
		if (old != enable) {
			setDirtyAndFirePropertyChange("allow local moves", old ? "true" : "false", enable ? "true" // NOI18N
					: "false"); // NOI18N
		}
	}

	public boolean isAllowThroughCarsEnabled() {
		return _allowThroughCars;
	}

	public void setAllowThroughCarsEnabled(boolean enable) {
		boolean old = _allowThroughCars;
		_allowThroughCars = enable;
		if (old != enable) {
			setDirtyAndFirePropertyChange("allow through cars", old ? "true" : "false", enable ? "true" // NOI18N
					: "false"); // NOI18N
		}
	}

	public boolean isBuildTrainNormalEnabled() {
		return _buildNormal;
	}

	public void setBuildTrainNormalEnabled(boolean enable) {
		boolean old = _buildNormal;
		_buildNormal = enable;
		if (old != enable) {
			setDirtyAndFirePropertyChange("build train normal", old ? "true" : "false", enable ? "true" // NOI18N
					: "false"); // NOI18N
		}
	}

	/**
	 * When true allow a turn to return cars to staging. A turn is a train that departs and terminates at the same
	 * location.
	 * 
	 * @return true if cars can return to staging
	 */
	public boolean isAllowReturnToStagingEnabled() {
		return _allowCarsReturnStaging;
	}

	public void setAllowReturnToStagingEnabled(boolean enable) {
		boolean old = _allowCarsReturnStaging;
		_allowCarsReturnStaging = enable;
		if (old != enable) {
			setDirtyAndFirePropertyChange("allow cars to return to staging", old ? "true" : "false", // NOI18N
					enable ? "true" : "false"); // NOI18N
		}
	}

	protected void setBuilt(boolean built) {
		boolean old = _built;
		_built = built;
		if (old != built) {
			setDirtyAndFirePropertyChange(BUILT_CHANGED_PROPERTY, old ? "true" : "false", built ? "true" // NOI18N
					: "false"); // NOI18N
		}
	}

	/**
	 * Used to determine if this train has been built.
	 * 
	 * @return true if the train was successfully built.
	 */
	public boolean isBuilt() {
		return _built;
	}

	public void setModified(boolean modified) {
		boolean old = _modified;
		_modified = modified;
		if (modified)
			setPrinted(false);
		if (old != modified) {
			setDirtyAndFirePropertyChange("TrainModified", old ? "true" : "false", modified ? "true" // NOI18N
					: "false"); // NOI18N
		}
	}

	public boolean isModified() {
		return _modified;
	}

	/**
	 * Deprecated, kept for user scripts. Use isBuilt()
	 * 
	 * @return true if the train was successfully built.
	 * 
	 */
	@Deprecated
	public boolean getBuilt() {
		return isBuilt();
	}

	/**
	 * Control flag used to decide if this train is to be built.
	 * 
	 * @param build
	 *            When true, build this train.
	 */
	public void setBuildEnabled(boolean build) {
		boolean old = _build;
		_build = build;
		if (old != build) {
			setDirtyAndFirePropertyChange(BUILD_CHANGED_PROPERTY, old ? "true" : "false", build ? "true" // NOI18N
					: "false"); // NOI18N
		}
	}

	/**
	 * Used to determine if train is to be built.
	 * 
	 * @return true if train is to be built.
	 */
	public boolean isBuildEnabled() {
		return _build;
	}

	/**
	 * Deprecated, use setBuildEnabled(build).
	 * 
	 * @param build
	 *            When true, build this train.
	 */
	@Deprecated
	public void setBuild(boolean build) {
		setBuildEnabled(build);
	}

	/**
	 * Deprecated, use isBuildEnabled()
	 * 
	 * @return true if train is to be built.
	 */
	@Deprecated
	public boolean getBuild() {
		return isBuildEnabled();
	}

	/**
	 * Build this train if the build control flag is true.
	 * 
	 * @return True only if train is successfully built.
	 */
	public boolean buildIfSelected() {
		if (_build && !_built) {
			build();
			return true;
		}
		log.debug("Train (" + getName() + ") not selected or already built, skipping build");
		return false;
	}

	/**
	 * Build this train. Creates a train manifest.
	 */
	public void build() {
		reset();
		// run before build scripts
		runScripts(getBuildScripts());
		TrainBuilder tb = new TrainBuilder();
		tb.build(this);
		setPrinted(false);
		setSwitchListStatus(UNKNOWN);
		// run after build scripts
		runScripts(getAfterBuildScripts());
	}

	/**
	 * Run train scripts, waits for completion before returning.
	 */
	private synchronized void runScripts(List<String> scripts) {
		if (scripts.size() > 0) {
			// find the number of active threads
			ThreadGroup root = Thread.currentThread().getThreadGroup();
			int numberOfThreads = root.activeCount();
			for (int i = 0; i < scripts.size(); i++) {
				jmri.util.PythonInterp.runScript(jmri.util.FileUtil.getExternalFilename(scripts.get(i)));
			}
			// need to wait for scripts to complete or 2 seconds maximum
			int count = 0;
			while (root.activeCount() > numberOfThreads) {
				synchronized (this) {
					log.debug("Number of active threads: " + root.activeCount());
					try {
						wait(20);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt(); // retain if needed later
					}
					if (count++ > 100)
						break; // 2 seconds maximum 20*100 = 2000
				}
			}
		}
	}

	public void printBuildReport() {
		boolean isPreview = TrainManager.instance().isPrintPreviewEnabled();
		printBuildReport(isPreview);
	}

	public boolean printBuildReport(boolean isPreview) {
		File buildFile = TrainManagerXml.instance().getTrainBuildReportFile(getName());
		if (!buildFile.exists()) {
			log.warn("Build file missing for train " + getName());
			return false;
		}

		if (isPreview && Setup.isBuildReportEditorEnabled())
			TrainPrintUtilities.editReport(buildFile, getName());
		else
			TrainPrintUtilities.printReport(buildFile, MessageFormat.format(Bundle.getMessage("buildReport"),
					new Object[] { getDescription() }), isPreview, "", true, "", "", Setup.PORTRAIT, Setup.getBuildReportFontSize());
		return true;
	}

	protected void setBuildFailed(boolean status) {
		boolean old = _buildFailed;
		_buildFailed = status;
		if (old != status) {
			setDirtyAndFirePropertyChange("buildFailed", old ? "true" : "false", status ? "true" : "false"); // NOI18N
		}
	}

	/**
	 * Returns true if the train build failed. Note that returning false doesn't mean the build was successful.
	 * 
	 * @return true if train build failed.
	 */
	public boolean getBuildFailed() {
		return _buildFailed;
	}

	protected void setBuildFailedMessage(String message) {
		String old = _buildFailedMessage;
		_buildFailedMessage = message;
		if (!old.equals(message)) {
			setDirtyAndFirePropertyChange("buildFailedMessage", old, message); // NOI18N
		}
	}

	protected String getBuildFailedMessage() {
		return _buildFailedMessage;
	}

	/**
	 * Print manifest for train if already built.
	 * 
	 * @return true if print successful.
	 */
	public boolean printManifestIfBuilt() {
		if (_built) {
			boolean isPreview = TrainManager.instance().isPrintPreviewEnabled();
			printManifest(isPreview);
		} else {
			log.debug("Need to build train (" + getName() + ") before printing manifest");
			return false;
		}
		return true;
	}

	/**
	 * Print manifest for train.
	 * 
	 * @return true if print successful.
	 */
	public boolean printManifest(boolean isPreview) {
		if (isModified()) {
			new TrainManifest(this);
			if (Setup.isGenerateCsvManifestEnabled())
				new TrainCsvManifest(this);
		}
		File file = TrainManagerXml.instance().getTrainManifestFile(getName());
		if (!file.exists()) {
			log.warn("Manifest file missing for train " + getName());
			return false;
		}
		if (isPreview && Setup.isManifestEditorEnabled()) {
			TrainPrintUtilities.openDesktopEditor(file);
			return true;
		}
		String logoURL = "";
		if (!getManifestLogoURL().equals(""))
			logoURL = getManifestLogoURL();
		else
			logoURL = Setup.getManifestLogoURL();
		Location departs = LocationManager.instance().getLocationByName(getTrainDepartsName());
		String printerName = departs.getDefaultPrinterName();
		TrainPrintUtilities.printReport(file, Bundle.getMessage("TrainManifest") + " " + getDescription(),
				isPreview, Setup.getFontName(), false, logoURL, printerName, Setup.getManifestOrientation(), Setup.getManifestFontSize());
		if (!isPreview)
			setPrinted(true);
		return true;
	}

	public boolean openFile() {
		if (isModified()) {
			new TrainManifest(this);
			if (Setup.isGenerateCsvManifestEnabled())
				new TrainCsvManifest(this);
		}
		File file = TrainManagerXml.instance().getTrainCsvManifestFile(getName());
		if (!file.exists()) {
			log.warn("CSV manifest file missing for train " + getName());
			return false;
		}
	
		TrainUtilities.openDesktop(file);

		return true;
	}
	
	public boolean runFile() {
		if (isModified()) {
			new TrainManifest(this);
			if (Setup.isGenerateCsvManifestEnabled())
				new TrainCsvManifest(this);
		}
		File file = TrainManagerXml.instance().getTrainCsvManifestFile(getName());
		if (!file.exists()) {
			log.warn("CSV manifest file missing for train " + getName());
			return false;
		}
	
		// Set up to process the CSV file by the external Manifest program
		CustomManifest.addCVSFile(file);
		if (!CustomManifest.process()) {
			if (!CustomManifest.manifestCreatorFileExists()) {
				log.warn("Manifest creator file not found!, directory name: "
						+ CustomManifest.getDirectoryName() + ", file name: " + CustomManifest.getFileName()); // NOI18N
			}
			TrainUtilities.openDesktop(file);
			return false;
		}

		return true;
	}

	public File createCSVManifestFile() {
		if (isModified()) {
			new TrainManifest(this);
			if (Setup.isGenerateCsvManifestEnabled())
				new TrainCsvManifest(this);
		}
		File file = TrainManagerXml.instance().getTrainCsvManifestFile(getName());
		if (!file.exists()) {
			log.warn("CSV manifest file was not created for train " + getName());
			return null;
		}

		return file;
	}

	private void setPrinted(boolean printed) {
		boolean old = _printed;
		_printed = printed;
		if (old != printed)
			setDirtyAndFirePropertyChange("trainPrinted", old ? "true" : "false", printed ? "true" : "false"); // NOI18N
	}

	/**
	 * Used to determine if train manifest was printed.
	 * 
	 * @return true if the train manifest was printed.
	 * 
	 */
	public boolean isPrinted() {
		return _printed;
	}

	/**
	 * Deprecated, kept for user scripts. Use isPrinted()
	 * 
	 * @return true if the train manifest was printed.
	 * 
	 */
	@Deprecated
	public boolean getPrinted() {
		return _printed;
	}

	protected RouteLocation _trainIconRl = null; // saves the icon current route location

	/**
	 * Sets the panel position for the train icon for the current route location.
	 * 
	 * @return true if train coordinates can be set
	 */
	public boolean setTrainIconCoordinates() {
		if (Setup.isTrainIconCordEnabled()) {
			_trainIconRl.setTrainIconX(_trainIcon.getX());
			_trainIconRl.setTrainIconY(_trainIcon.getY());
			RouteManagerXml.instance().setDirty(true);
			return true;
		}
		return false;
	}

	/**
	 * Terminate train.
	 */
	public void terminate() {
		while (_built)
			move();
	}

	/**
	 * Move train to next location in route. Will move engines, cars, and train icon. Will also terminate a train after
	 * it arrives at its final destination.
	 */
	public void move() {
		log.debug("Move train (" + getName() + ")");
		if (getRoute() == null || getCurrentLocation() == null)
			return;
		List<String> routeList = getRoute().getLocationsBySequenceList();
		for (int i = 0; i < routeList.size(); i++) {
			RouteLocation rl = getRoute().getLocationById(routeList.get(i));
			if (getCurrentLocation() == rl) {
				i++;
				RouteLocation rlNew = null; // use null if end of route
				if (i < routeList.size()) {
					rlNew = getRoute().getLocationById(routeList.get(i));
				}
				setCurrentLocation(rlNew);
				moveTrainIcon(rlNew);
				// cars and engines will move via property change
				setDirtyAndFirePropertyChange(TRAIN_LOCATION_CHANGED_PROPERTY, rl, rlNew);
				updateStatus(rl, rlNew);
				// tell GUI that train has complete its move
				setDirtyAndFirePropertyChange(TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY, rl, rlNew);
				break;
			}
		}
	}

	/**
	 * Move train to a location in the train's route. Code checks to see if the location requested is part of the
	 * train's route and if the train hasn't already visited the location. This command can only move the train forward
	 * in its route. Note that you can not terminate the train using this command. See move() or terminate().
	 * 
	 * @param locationName
	 *            The name of the location to move this train.
	 * @return true if train was able to move to the named location.
	 */
	public boolean move(String locationName) {
		log.info("Move train (" + getName() + ") to location (" + locationName + ")");
		if (getRoute() == null || getCurrentLocation() == null)
			return false;
		List<String> routeList = getRoute().getLocationsBySequenceList();
		for (int i = 0; i < routeList.size(); i++) {
			RouteLocation rl = getRoute().getLocationById(routeList.get(i));
			if (getCurrentLocation() == rl) {
				for (int j = i + 1; j < routeList.size(); j++) {
					rl = getRoute().getLocationById(routeList.get(j));
					if (rl.getName().equals(locationName)) {
						log.debug("Found location (" + locationName + ") moving train to this location");
						for (j = i + 1; j < routeList.size(); j++) {
							rl = getRoute().getLocationById(routeList.get(j));
							move();
							if (rl.getName().equals(locationName))
								return true;
						}
					}
				}
				break; // done
			}
		}
		return false;
	}

	/**
	 * Move train to the next location in the train's route. The location name provided must be equal to the next
	 * location name in the train's route.
	 * 
	 * @param locationName
	 *            The next location name in the train's route.
	 * @return true if successful.
	 */
	public boolean moveToNextLocation(String locationName) {
		if (getNextLocationName().equals(locationName)) {
			move();
			return true;
		}
		return false;
	}

	public void loadTrainIcon() {
		if (getCurrentLocation() != null)
			moveTrainIcon(getCurrentLocation());
	}

	private boolean animation = true; // when true use animation for icon moves
	TrainIconAnimation _ta;

	/*
	 * rl = to the next route location for this train
	 */
	protected void moveTrainIcon(RouteLocation rl) {
		_trainIconRl = rl;
		// create train icon if at departure or if program has been restarted
		if (rl == getTrainDepartsRouteLocation() || _trainIcon == null) {
			createTrainIcon();
		}
		// is the lead engine still in train
		if (getLeadEngine() != null && getLeadEngine().getRouteDestination() == rl && rl != null) {
			log.debug("Engine (" + getLeadEngine().toString() + ") arriving at destination " + rl.getName());
		}
		if (_trainIcon != null && _trainIcon.isActive()) {
			setTrainIconColor();
			_trainIcon.setShowTooltip(true);
			String txt = null;
			if (getCurrentLocationName().equals(""))
				txt = getDescription() + " " + TERMINATED + " (" + getTrainTerminatesName() + ")";
			else
				txt = MessageFormat.format(Bundle.getMessage("TrainAtNext"), new Object[] { getDescription(),
						getCurrentLocationName(), getNextLocationName() });
			_trainIcon.getTooltip().setText(txt);
			_trainIcon.getTooltip().setBackgroundColor(Color.white);
			if (rl != null) {
				if (rl.getTrainIconX() != 0 || rl.getTrainIconY() != 0) {
					if (animation) {
						TrainIconAnimation ta = new TrainIconAnimation(_trainIcon, rl, _ta);
						ta.start(); // start the animation
						_ta = ta;
					} else {
						_trainIcon.setLocation(rl.getTrainIconX(), rl.getTrainIconY());
					}
				}
			}
		}
	}

	public String getIconName() {
		String name = getName();
		if (isBuilt() && getLeadEngine() != null && Setup.isTrainIconAppendEnabled())
			name += " " + getLeadEngine().getNumber();
		return name;
	}

	/**
	 * Gets the lead engine, will create it if the program has been restarted
	 * 
	 * @return lead engine for this train
	 */
	public Engine getLeadEngine() {
		if (_leadEngine == null && !_leadEngineId.equals("")) {
			_leadEngine = EngineManager.instance().getById(_leadEngineId);
		}
		return _leadEngine;
	}

	public void setLeadEngine(Engine engine) {
		if (engine == null)
			_leadEngineId = "";
		_leadEngine = engine;
	}

	protected TrainIcon _trainIcon = null;

	public TrainIcon getTrainIcon() {
		return _trainIcon;
	}

	public void createTrainIcon() {
		if (_trainIcon != null && _trainIcon.isActive()) {
			_trainIcon.remove();
			_trainIcon.dispose();
		}
		// if there's a panel specified, get it and place icon
		if (!Setup.getPanelName().equals("")) {
			Editor editor = PanelMenu.instance().getEditorByName(Setup.getPanelName());
			if (editor != null) {
				_trainIcon = editor.addTrainIcon(getIconName());
				_trainIcon.setTrain(this);
				if (getIconName().length() > 9) {
					_trainIcon.setFont(jmri.util.FontUtil.deriveFont(_trainIcon.getFont(), 8.f));
				}
				if (getCurrentLocation() != null)
					_trainIcon.setLocation(getCurrentLocation().getTrainIconX(), getCurrentLocation()
							.getTrainIconY());
				// add throttle if there's a throttle manager
				if (jmri.InstanceManager.throttleManagerInstance() != null) {
					// add throttle if JMRI loco roster entry exist
					RosterEntry entry = null;
					if (getLeadEngine() != null) {
						// first try and find a match based on loco road number
						List<RosterEntry> entries = Roster.instance().matchingList(null,
								getLeadEngine().getNumber(), null, null, null, null, null);
						if (entries.size() > 0) {
							entry = entries.get(0);
						}
						if (entry == null) {
							// now try finding a match based on DCC address
							entries = Roster.instance().matchingList(null, null, getLeadEngine().getNumber(),
									null, null, null, null);
							if (entries.size() > 0) {
								entry = entries.get(0);
							}
						}
					}
					if (entry != null) {
						_trainIcon.setRosterEntry(entry);
						if (getLeadEngine().getConsist() != null)
							_trainIcon.setConsistNumber(getLeadEngine().getConsist().getConsistNumber());
					} else {
						log.debug("Loco roster entry not found for train (" + getName() + ")");
					}
				}
			}
		}
	}

	private void setTrainIconColor() {
		// Terminated train?
		if (getCurrentLocationName().equals("")) {
			_trainIcon.setLocoColor(Setup.getTrainIconColorTerminate());
			return;
		}
		// local train?
		if (getRoute().getLocationsBySequenceList().size() == 1) {
			_trainIcon.setLocoColor(Setup.getTrainIconColorLocal());
			return;
		}
		// set color based on train direction at current location
		if (_trainIconRl.getTrainDirection() == RouteLocation.NORTH)
			_trainIcon.setLocoColor(Setup.getTrainIconColorNorth());
		if (_trainIconRl.getTrainDirection() == RouteLocation.SOUTH)
			_trainIcon.setLocoColor(Setup.getTrainIconColorSouth());
		if (_trainIconRl.getTrainDirection() == RouteLocation.EAST)
			_trainIcon.setLocoColor(Setup.getTrainIconColorEast());
		if (_trainIconRl.getTrainDirection() == RouteLocation.WEST)
			_trainIcon.setLocoColor(Setup.getTrainIconColorWest());
	}

	// LocationManager locationManager = LocationManager.instance();

	private void updateStatus(RouteLocation old, RouteLocation next) {
		if (next != null) {
			setStatus(TRAININROUTE + " " + getNumberCarsInTrain() + " " + Bundle.getMessage("cars") + " "
					+ getTrainLength() + " " + Bundle.getMessage("feet") + ", " + getTrainWeight() + " "
					+ Bundle.getMessage("tons"));
			// run move scripts
			runScripts(getMoveScripts());
		} else {
			log.debug("Train (" + getName() + ") terminated");
			setStatus(TERMINATED + " " + TrainCommon.getDate(false));
			setBuilt(false);
			// run termination scripts
			runScripts(getTerminationScripts());
		}
	}

	/**
	 * Sets the print status for switch lists
	 * 
	 * @param status
	 *            UNKNOWN PRINTED
	 */
	public void setSwitchListStatus(String status) {
		String old = _switchListStatus;
		_switchListStatus = status;
		if (!old.equals(status))
			setDirtyAndFirePropertyChange("switch list train status", old, status); // NOI18N
	}

	public String getSwitchListStatus() {
		return _switchListStatus;
	}

	/**
	 * Resets the train, removes engines and cars from this train.
	 * 
	 * @return true if reset successful
	 */
	public boolean reset() {
		// is this train in route?
		if (isTrainInRoute()) {
			log.info("Train (" + getName() + ") has started its route, can not be reset");
			return false;
		}
		setCurrentLocation(null);
		setDepartureTrack(null);
		setTerminationTrack(null);
		setBuilt(false);
		setBuildFailed(false);
		setBuildFailedMessage("");
		setPrinted(false);
		// remove cars and engines from this train via property change
		setStatus(TRAINRESET);
		// remove train icon
		if (_trainIcon != null && _trainIcon.isActive()) {
			_trainIcon.remove();
			_trainIcon.dispose();
		}
		return true;
	}

	public void dispose() {
		if (getRoute() != null)
			getRoute().removePropertyChangeListener(this);
		CarRoads.instance().removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		EngineTypes.instance().removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);
		EngineModels.instance().removePropertyChangeListener(this);

		setDirtyAndFirePropertyChange(DISPOSE_CHANGED_PROPERTY, null, "Dispose"); // NOI18N
	}

	/**
	 * Construct this Entry from XML. This member has to remain synchronized with the detailed DTD in
	 * operations-trains.dtd
	 * 
	 * @param e
	 *            Consist XML element
	 */
	public Train(org.jdom.Element e) {
		org.jdom.Attribute a;
		if ((a = e.getAttribute(Xml.ID)) != null)
			_id = a.getValue();
		else
			log.warn("no id attribute in train element when reading operations");
		if ((a = e.getAttribute(Xml.NAME)) != null)
			_name = a.getValue();
		if ((a = e.getAttribute(Xml.DESCRIPTION)) != null)
			_description = a.getValue();
		// create the train calendar
		_departureTime = Calendar.getInstance();
		_departureTime.set(2008, 10, 29, 12, 00); // The first release date for JMRI operations
		if ((a = e.getAttribute(Xml.DEPART_HOUR)) != null) {
			String hour = a.getValue();
			if ((a = e.getAttribute(Xml.DEPART_MINUTE)) != null) {
				String minute = a.getValue();
				setDepartureTime(hour, minute);
			}
		}
		// new format for train's route added in 2.99.7
		Element route = e.getChild(Xml.ROUTE);
		if (route != null) {
			if ((a = route.getAttribute(Xml.ID)) != null) {
				setRoute(RouteManager.instance().getRouteById(a.getValue()));
			}
			if (route.getChild(Xml.SKIPS) != null) {
				@SuppressWarnings("unchecked")
				List<Element> skips = route.getChild(Xml.SKIPS).getChildren(Xml.LOCATION);
				String[] locs = new String[skips.size()];
				for (int i = 0; i < skips.size(); i++) {
					Element loc = skips.get(i);
					if ((a = loc.getAttribute(Xml.ID)) != null) {
						locs[i] = a.getValue();
					}
				}
				setTrainSkipsLocations(locs);
			}
		} else {
			// old format
			// try and first get the route by id then by name
			if ((a = e.getAttribute(Xml.ROUTE_ID)) != null) {
				setRoute(RouteManager.instance().getRouteById(a.getValue()));
			} else if ((a = e.getAttribute(Xml.ROUTE)) != null) {
				setRoute(RouteManager.instance().getRouteByName(a.getValue()));
			}
			if ((a = e.getAttribute(Xml.SKIP)) != null) {
				String locationIds = a.getValue();
				String[] locs = locationIds.split("%%"); // NOI18N
				// if (log.isDebugEnabled()) log.debug("Train skips : "+locationIds);
				setTrainSkipsLocations(locs);
			}
		}
		// new way of reading car types using elements
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
		// old way of reading car types up to version 2.99.6
		else if ((a = e.getAttribute(Xml.CAR_TYPES)) != null) {
			String names = a.getValue();
			String[] types = names.split("%%"); // NOI18N
			// if (log.isDebugEnabled()) log.debug("Car types: "+names);
			setTypeNames(types);
		}
		// old misspelled format
		if ((a = e.getAttribute(Xml.CAR_ROAD_OPERATION)) != null)
			_roadOption = a.getValue();
		if ((a = e.getAttribute(Xml.CAR_ROAD_OPTION)) != null)
			_roadOption = a.getValue();
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
		// old way of reading car roads up to version 2.99.6
		else if ((a = e.getAttribute(Xml.CAR_ROADS)) != null) {
			String names = a.getValue();
			String[] roads = names.split("%%"); // NOI18N
			if (log.isDebugEnabled())
				log.debug("Train (" + getName() + ") " + getRoadOption() + " car roads: " + names);
			setRoadNames(roads);
		}

		if ((a = e.getAttribute(Xml.CAR_LOAD_OPTION)) != null)
			_loadOption = a.getValue();
		if ((a = e.getAttribute(Xml.CAR_OWNER_OPTION)) != null)
			_ownerOption = a.getValue();
		if ((a = e.getAttribute(Xml.BUILT_START_YEAR)) != null)
			_builtStartYear = a.getValue();
		if ((a = e.getAttribute(Xml.BUILT_END_YEAR)) != null)
			_builtEndYear = a.getValue();
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
		// old way of reading car loads up to version 2.99.6
		else if ((a = e.getAttribute(Xml.CAR_LOADS)) != null) {
			String names = a.getValue();
			String[] loads = names.split("%%"); // NOI18N
			if (log.isDebugEnabled())
				log.debug("Train (" + getName() + ") " + getLoadOption() + " car loads: " + names);
			setLoadNames(loads);
		}
		// new way of reading car owners using elements
		if (e.getChild(Xml.CAR_OWNERS) != null) {
			@SuppressWarnings("unchecked")
			List<Element> carOwners = e.getChild(Xml.CAR_OWNERS).getChildren(Xml.CAR_OWNER);
			String[] owners = new String[carOwners.size()];
			for (int i = 0; i < carOwners.size(); i++) {
				Element owner = carOwners.get(i);
				if ((a = owner.getAttribute(Xml.NAME)) != null) {
					owners[i] = a.getValue();
				}
			}
			setOwnerNames(owners);
		}
		// old way of reading car owners up to version 2.99.6
		else if ((a = e.getAttribute(Xml.CAR_OWNERS)) != null) {
			String names = a.getValue();
			String[] owners = names.split("%%"); // NOI18N
			if (log.isDebugEnabled())
				log.debug("Train (" + getName() + ") " + getOwnerOption() + " car owners: " + names);
			setOwnerNames(owners);
		}

		if ((a = e.getAttribute(Xml.NUMBER_ENGINES)) != null)
			_numberEngines = a.getValue();
		if ((a = e.getAttribute(Xml.LEG2_ENGINES)) != null)
			_leg2Engines = a.getValue();
		if ((a = e.getAttribute(Xml.LEG3_ENGINES)) != null)
			_leg3Engines = a.getValue();
		if ((a = e.getAttribute(Xml.ENGINE_ROAD)) != null)
			_engineRoad = a.getValue();
		if ((a = e.getAttribute(Xml.LEG2_ROAD)) != null)
			_leg2Road = a.getValue();
		if ((a = e.getAttribute(Xml.LEG3_ROAD)) != null)
			_leg3Road = a.getValue();
		if ((a = e.getAttribute(Xml.ENGINE_MODEL)) != null)
			_engineModel = a.getValue();
		if ((a = e.getAttribute(Xml.LEG2_MODEL)) != null)
			_leg2Model = a.getValue();
		if ((a = e.getAttribute(Xml.LEG3_MODEL)) != null)
			_leg3Model = a.getValue();
		if ((a = e.getAttribute(Xml.REQUIRES)) != null)
			_requires = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(Xml.CABOOSE_ROAD)) != null)
			_cabooseRoad = a.getValue();
		if ((a = e.getAttribute(Xml.LEG2_CABOOSE_ROAD)) != null)
			_leg2CabooseRoad = a.getValue();
		if ((a = e.getAttribute(Xml.LEG3_CABOOSE_ROAD)) != null)
			_leg3CabooseRoad = a.getValue();
		if ((a = e.getAttribute(Xml.LEG2_OPTIONS)) != null)
			_leg2Options = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(Xml.LEG3_OPTIONS)) != null)
			_leg3Options = Integer.parseInt(a.getValue());
		if ((a = e.getAttribute(Xml.BUILD_NORMAL)) != null)
			_buildNormal = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.TO_TERMINAL)) != null)
			_sendToTerminal = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.ALLOW_LOCAL_MOVES)) != null)
			_allowLocalMoves = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.ALLOW_THROUGH_CARS)) != null)
			_allowThroughCars = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.ALLOW_RETURN)) != null)
			_allowCarsReturnStaging = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.BUILT)) != null)
			_built = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.BUILD)) != null)
			_build = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.BUILD_FAILED)) != null)
			_buildFailed = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.BUILD_FAILED_MESSAGE)) != null)
			_buildFailedMessage = a.getValue();
		if ((a = e.getAttribute(Xml.PRINTED)) != null)
			_printed = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.MODIFIED)) != null)
			_modified = a.getValue().equals(Xml.TRUE);
		if ((a = e.getAttribute(Xml.SWITCH_LIST_STATUS)) != null)
			_switchListStatus = a.getValue();
		if ((a = e.getAttribute(Xml.LEAD_ENGINE)) != null)
			_leadEngineId = a.getValue();
		if ((a = e.getAttribute(Xml.STATUS)) != null)
			_status = a.getValue();
		if ((a = e.getAttribute(Xml.COMMENT)) != null)			
			_comment = OperationsXml.convertFromXmlComment(a.getValue());
		if (_route != null) {
			if ((a = e.getAttribute(Xml.CURRENT)) != null)
				_current = _route.getLocationById(a.getValue());
			if ((a = e.getAttribute(Xml.LEG2_START)) != null)
				_leg2Start = _route.getLocationById(a.getValue());
			if ((a = e.getAttribute(Xml.LEG3_START)) != null)
				_leg3Start = _route.getLocationById(a.getValue());
			if ((a = e.getAttribute(Xml.LEG2_END)) != null)
				_end2Leg = _route.getLocationById(a.getValue());
			if ((a = e.getAttribute(Xml.LEG3_END)) != null)
				_leg3End = _route.getLocationById(a.getValue());
			if ((a = e.getAttribute(Xml.DEPARTURE_TRACK)) != null) {
				Location location = LocationManager.instance().getLocationByName(getTrainDepartsName());
				if (location != null)
					_departureTrack = location.getTrackById(a.getValue());
				else
					log.error("Departure location not found for track " + a.getValue());
			}
			if ((a = e.getAttribute(Xml.TERMINATION_TRACK)) != null) {
				Location location = LocationManager.instance().getLocationByName(getTrainTerminatesName());
				if (location != null)
					_terminationTrack = location.getTrackById(a.getValue());
				else
					log.error("Termiation location not found for track " + a.getValue());
			}
		}

		// check for scripts
		if (e.getChild(Xml.SCRIPTS) != null) {
			@SuppressWarnings("unchecked")
			List<Element> lb = e.getChild(Xml.SCRIPTS).getChildren(Xml.BUILD);
			for (int i = 0; i < lb.size(); i++) {
				Element es = lb.get(i);
				if ((a = es.getAttribute(Xml.NAME)) != null) {
					addBuildScript(a.getValue());
				}
			}
			@SuppressWarnings("unchecked")
			List<Element> lab = e.getChild(Xml.SCRIPTS).getChildren(Xml.AFTER_BUILD);
			for (int i = 0; i < lab.size(); i++) {
				Element es = lab.get(i);
				if ((a = es.getAttribute(Xml.NAME)) != null) {
					addAfterBuildScript(a.getValue());
				}
			}
			@SuppressWarnings("unchecked")
			List<Element> lm = e.getChild(Xml.SCRIPTS).getChildren(Xml.MOVE);
			for (int i = 0; i < lm.size(); i++) {
				Element es = lm.get(i);
				if ((a = es.getAttribute(Xml.NAME)) != null) {
					addMoveScript(a.getValue());
				}
			}
			@SuppressWarnings("unchecked")
			List<Element> lt = e.getChild(Xml.SCRIPTS).getChildren(Xml.TERMINATE);
			for (int i = 0; i < lt.size(); i++) {
				Element es = lt.get(i);
				if ((a = es.getAttribute(Xml.NAME)) != null) {
					addTerminationScript(a.getValue());
				}
			}
		}
		// check for optional railroad name and logo
		if ((e.getChild(Xml.RAIL_ROAD) != null)
				&& (a = e.getChild(Xml.RAIL_ROAD).getAttribute(Xml.NAME)) != null) {
			String name = a.getValue();
			setRailroadName(name);
		}
		if ((e.getChild(Xml.MANIFEST_LOGO) != null)) {
			if ((a = e.getChild(Xml.MANIFEST_LOGO).getAttribute(Xml.NAME)) != null) {
				setManifestLogoURL(a.getValue());
			}
		}
		if ((a = e.getAttribute(Xml.SHOW_TIMES)) != null)
			_showTimes = a.getValue().equals(Xml.TRUE);

		addPropertyChangeListerners();
	}

	private void addPropertyChangeListerners() {
		CarRoads.instance().addPropertyChangeListener(this);
		CarTypes.instance().addPropertyChangeListener(this);
		EngineTypes.instance().addPropertyChangeListener(this);
		CarOwners.instance().addPropertyChangeListener(this);
		EngineModels.instance().addPropertyChangeListener(this);
	}

	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the detailed DTD in
	 * operations-trains.dtd.
	 * 
	 * @return Contents in a JDOM Element
	 */
	public Element store() {
		Element e = new Element(Xml.TRAIN);
		e.setAttribute(Xml.ID, getId());
		e.setAttribute(Xml.NAME, getName());
		e.setAttribute(Xml.DESCRIPTION, getDescription());
		e.setAttribute(Xml.DEPART_HOUR, getDepartureTimeHour());
		e.setAttribute(Xml.DEPART_MINUTE, getDepartureTimeMinute());
		Element eRoute = new Element(Xml.ROUTE);
		if (getRoute() != null) {
			// old format
			e.setAttribute(Xml.ROUTE, getRoute().getName());
			e.setAttribute(Xml.ROUTE_ID, getRoute().getId());
			// new format
			eRoute.setAttribute(Xml.NAME, getRoute().getName());
			eRoute.setAttribute(Xml.ID, getRoute().getId());
			e.addContent(eRoute);
			// build list of locations that this train skips
			// new format
			String[] locationIds = getTrainSkipsLocations();
			if (locationIds.length > 0) {
				Element eSkips = new Element(Xml.SKIPS);
				for (int i = 0; i < locationIds.length; i++) {
					Element eLoc = new Element(Xml.LOCATION);
					RouteLocation rl = getRoute().getLocationById(locationIds[i]);
					if (rl != null) {
						eLoc.setAttribute(Xml.NAME, getRoute().getLocationById(locationIds[i]).getName());
						eLoc.setAttribute(Xml.ID, locationIds[i]);
						eSkips.addContent(eLoc);
					}
				}
				eRoute.addContent(eSkips);
			}
		}
		// build list of locations that this train skips
		// old format
		if (Control.backwardCompatible) {
			String[] locationIds = getTrainSkipsLocations();
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < locationIds.length; i++) {
				buf.append(locationIds[i] + "%%"); // NOI18N
			}
			e.setAttribute(Xml.SKIP, buf.toString());
		}
		if (getCurrentLocation() != null)
			e.setAttribute(Xml.CURRENT, getCurrentLocation().getId());
		if (getDepartureTrack() != null)
			e.setAttribute(Xml.DEPARTURE_TRACK, getDepartureTrack().getId());
		if (getTerminationTrack() != null)
			e.setAttribute(Xml.TERMINATION_TRACK, getTerminationTrack().getId());
		if (Control.backwardCompatible)
			e.setAttribute(Xml.CAR_ROAD_OPERATION, getRoadOption()); // misspelled should have been option not operation
		e.setAttribute(Xml.CAR_ROAD_OPTION, getRoadOption());
		e.setAttribute(Xml.CAR_LOAD_OPTION, getLoadOption());
		e.setAttribute(Xml.CAR_OWNER_OPTION, getOwnerOption());
		e.setAttribute(Xml.BUILT_START_YEAR, getBuiltStartYear());
		e.setAttribute(Xml.BUILT_END_YEAR, getBuiltEndYear());
		e.setAttribute(Xml.NUMBER_ENGINES, getNumberEngines());
		e.setAttribute(Xml.ENGINE_ROAD, getEngineRoad());
		e.setAttribute(Xml.ENGINE_MODEL, getEngineModel());
		e.setAttribute(Xml.REQUIRES, Integer.toString(getRequirements()));
		e.setAttribute(Xml.CABOOSE_ROAD, getCabooseRoad());
		e.setAttribute(Xml.BUILD_NORMAL, isBuildTrainNormalEnabled() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.TO_TERMINAL, isSendCarsToTerminalEnabled() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.ALLOW_LOCAL_MOVES, isAllowLocalMovesEnabled() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.ALLOW_RETURN, isAllowReturnToStagingEnabled() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.ALLOW_THROUGH_CARS, isAllowThroughCarsEnabled() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.BUILT, isBuilt() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.BUILD, isBuildEnabled() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.BUILD_FAILED, getBuildFailed() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.BUILD_FAILED_MESSAGE, getBuildFailedMessage());
		e.setAttribute(Xml.PRINTED, isPrinted() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.MODIFIED, isModified() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.SWITCH_LIST_STATUS, getSwitchListStatus());
		if (getLeadEngine() != null)
			e.setAttribute(Xml.LEAD_ENGINE, getLeadEngine().getId());
		e.setAttribute(Xml.STATUS, getStatus());
		e.setAttribute(Xml.COMMENT, getComment());
		e.setAttribute(Xml.SHOW_TIMES, isShowArrivalAndDepartureTimesEnabled() ? Xml.TRUE : Xml.FALSE);
		// build list of car types for this train
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
		// new way of saving car types
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
		// save list of car roads for this train
		if (!getRoadOption().equals(ALLROADS)) {
			String[] roads = getRoadNames();
			// old way of saving road names
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
		// save list of car loads for this train
		if (!getLoadOption().equals(ALLLOADS)) {
			String[] loads = getLoadNames();
			// old way of saving car loads
			if (Control.backwardCompatible) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < loads.length; i++) {
					buf.append(loads[i] + "%%"); // NOI18N
				}
				e.setAttribute(Xml.CAR_LOADS, buf.toString());
			}
			// new way of saving car loads
			Element eLoads = new Element(Xml.CAR_LOADS);
			for (int i = 0; i < loads.length; i++) {
				Element eLoad = new Element(Xml.CAR_LOAD);
				eLoad.setAttribute(Xml.NAME, loads[i]);
				eLoads.addContent(eLoad);
			}
			e.addContent(eLoads);
		}
		// save list of car owners for this train
		if (!getOwnerOption().equals(ALLOWNERS)) {
			String[] owners = getOwnerNames();
			// old way of saving car owners
			if (Control.backwardCompatible) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < owners.length; i++) {
					buf.append(owners[i] + "%%"); // NOI18N
				}
				e.setAttribute(Xml.CAR_OWNERS, buf.toString());
			}
			// new way of saving car owners
			Element eOwners = new Element(Xml.CAR_OWNERS);
			for (int i = 0; i < owners.length; i++) {
				Element eOwner = new Element(Xml.CAR_OWNER);
				eOwner.setAttribute(Xml.NAME, owners[i]);
				eOwners.addContent(eOwner);
			}
			e.addContent(eOwners);
		}
		// save list of scripts for this train
		if (getBuildScripts().size() > 0 || getAfterBuildScripts().size() > 0 || getMoveScripts().size() > 0
				|| getTerminationScripts().size() > 0) {
			Element es = new Element(Xml.SCRIPTS);
			if (getBuildScripts().size() > 0) {
				for (int i = 0; i < getBuildScripts().size(); i++) {
					Element em = new Element(Xml.BUILD);
					em.setAttribute(Xml.NAME, getBuildScripts().get(i));
					es.addContent(em);
				}
			}
			if (getAfterBuildScripts().size() > 0) {
				for (int i = 0; i < getAfterBuildScripts().size(); i++) {
					Element em = new Element(Xml.AFTER_BUILD);
					em.setAttribute(Xml.NAME, getAfterBuildScripts().get(i));
					es.addContent(em);
				}
			}
			if (getMoveScripts().size() > 0) {
				for (int i = 0; i < getMoveScripts().size(); i++) {
					Element em = new Element(Xml.MOVE);
					em.setAttribute(Xml.NAME, getMoveScripts().get(i));
					es.addContent(em);
				}
			}
			// save list of termination scripts for this train
			if (getTerminationScripts().size() > 0) {
				for (int i = 0; i < getTerminationScripts().size(); i++) {
					Element et = new Element(Xml.TERMINATE);
					et.setAttribute(Xml.NAME, getTerminationScripts().get(i));
					es.addContent(et);
				}
			}
			e.addContent(es);
		}
		if (!getRailroadName().equals("")) {
			Element r = new Element(Xml.RAIL_ROAD);
			r.setAttribute(Xml.NAME, getRailroadName());
			e.addContent(r);
		}
		if (!getManifestLogoURL().equals("")) {
			Element l = new Element(Xml.MANIFEST_LOGO);
			l.setAttribute(Xml.NAME, getManifestLogoURL());
			e.addContent(l);
		}

		if (getSecondLegOptions() != Train.NONE) {
			e.setAttribute(Xml.LEG2_OPTIONS, Integer.toString(getSecondLegOptions()));
			e.setAttribute(Xml.LEG2_ENGINES, getSecondLegNumberEngines());
			e.setAttribute(Xml.LEG2_ROAD, getSecondLegEngineRoad());
			e.setAttribute(Xml.LEG2_MODEL, getSecondLegEngineModel());
			e.setAttribute(Xml.LEG2_CABOOSE_ROAD, getSecondLegCabooseRoad());
			if (getSecondLegStartLocation() != null)
				e.setAttribute(Xml.LEG2_START, getSecondLegStartLocation().getId());
			if (getSecondLegEndLocation() != null)
				e.setAttribute(Xml.LEG2_END, getSecondLegEndLocation().getId());
		}
		if (getThirdLegOptions() != Train.NONE) {
			e.setAttribute(Xml.LEG3_OPTIONS, Integer.toString(getThirdLegOptions()));
			e.setAttribute(Xml.LEG3_ENGINES, getThirdLegNumberEngines());
			e.setAttribute(Xml.LEG3_ROAD, getThirdLegEngineRoad());
			e.setAttribute(Xml.LEG3_MODEL, getThirdLegEngineModel());
			e.setAttribute(Xml.LEG3_CABOOSE_ROAD, getThirdLegCabooseRoad());
			if (getThirdLegStartLocation() != null)
				e.setAttribute(Xml.LEG3_START, getThirdLegStartLocation().getId());
			if (getThirdLegEndLocation() != null)
				e.setAttribute(Xml.LEG3_END, getThirdLegEndLocation().getId());
		}
		return e;
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("train (" + getName() + ") sees property change: " + e.getPropertyName() + " old: "
					+ e.getOldValue() + " new: " // NOI18N
					+ e.getNewValue());
		if (e.getPropertyName().equals(Route.DISPOSE)) {
			setRoute(null);
		}
		if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY)) {
			replaceType((String) e.getOldValue(), (String) e.getNewValue());
		}
		if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)) {
			replaceRoad((String) e.getOldValue(), (String) e.getNewValue());
		}
		if (e.getPropertyName().equals(CarOwners.CAROWNERS_NAME_CHANGED_PROPERTY)) {
			replaceOwner((String) e.getOldValue(), (String) e.getNewValue());
		}
		if (e.getPropertyName().equals(EngineModels.ENGINEMODELS_NAME_CHANGED_PROPERTY)) {
			replaceModel((String) e.getOldValue(), (String) e.getNewValue());
		}
		// forward route departure time property changes
		if (e.getPropertyName().equals(RouteLocation.DEPARTURE_TIME_CHANGED_PROPERTY)) {
			setDirtyAndFirePropertyChange(DEPARTURETIME_CHANGED_PROPERTY, e.getOldValue(), e.getNewValue());
		}
		// forward any property changes in this train's route
		if (e.getSource().getClass().equals(Route.class))
			setDirtyAndFirePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
	}

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
		TrainManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static Logger log = LoggerFactory.getLogger(Train.class.getName());

}
