package jmri.jmrit.operations.trains;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.swing.JOptionPane;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.beans.Identifiable;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockManager;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.excel.TrainCustomManifest;
import jmri.jmrit.roster.RosterEntry;
import jmri.script.JmriScriptEngineManager;
import jmri.util.FileUtil;

/**
 * Represents a train on the layout
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2011, 2012, 2013,
 *         2014, 2015
 * @author Rodney Black Copyright (C) 2011
 */
public class Train extends PropertyChangeSupport implements Identifiable, PropertyChangeListener {

    /*
     * WARNING DO NOT LOAD CAR OR ENGINE MANAGERS WHEN Train.java IS CREATED IT
     * CAUSES A RECURSIVE LOOP AT LOAD TIME, SEE EXAMPLES BELOW CarManager
     * carManager = InstanceManager.getDefault(CarManager.class); EngineManager
     * engineManager = InstanceManager.getDefault(EngineManager.class);
     */

    // The release date for JMRI operations 10/29/2008

    public static final String NONE = "";

    protected String _id = NONE;
    protected String _name = NONE;
    protected String _description = NONE;
    protected RouteLocation _current = null;// where the train is located in its route
    protected String _buildFailedMessage = NONE; // the build failed message for this train
    protected boolean _built = false; // when true, a train manifest has been built
    protected boolean _modified = false; // when true, user has modified train after being built
    protected boolean _build = true; // when true, build this train
    protected boolean _buildFailed = false; // when true, build for this train failed
    protected boolean _printed = false; // when true, manifest has been printed
    protected boolean _sendToTerminal = false; // when true, cars picked up by train only go to terminal
    protected boolean _allowLocalMoves = true; // when true, cars with custom loads can be moved locally
    protected boolean _allowThroughCars = true; // when true, cars from the origin can be sent to the terminal
    protected boolean _buildNormal = false; // when true build this train in normal mode
    protected boolean _allowCarsReturnStaging = false; // when true allow cars to return to staging
    protected boolean _serviceAllCarsWithFinalDestinations = false; // when true, service cars with final destinations
    protected boolean _buildConsist = false; // when true, build a consist for this train using single locomotives
    protected boolean _sendCarsWithCustomLoadsToStaging = false; // when true, send cars to staging if spurs full
    protected Route _route = null;
    protected Track _departureTrack; // the departure track from staging
    protected Track _terminationTrack; // the termination track into staging
    protected String _roadOption = ALL_ROADS;// train road name restrictions
    protected int _requires = NO_CABOOSE_OR_FRED; // train requirements, caboose, FRED
    protected String _numberEngines = "0"; // number of engines this train requires
    protected String _engineRoad = NONE; // required road name for engines assigned to this train
    protected String _engineModel = NONE; // required model of engines assigned to this train
    protected String _cabooseRoad = NONE; // required road name for cabooses assigned to this train
    protected String _departureTime = "00:00"; // NOI18N departure time for this train
    protected String _leadEngineId = NONE; // lead engine for train icon info
    protected String _builtStartYear = NONE; // built start year
    protected String _builtEndYear = NONE; // built end year
    protected String _loadOption = ALL_LOADS;// train load restrictions
    protected String _ownerOption = ALL_OWNERS;// train owner name restrictions
    protected List<String> _buildScripts = new ArrayList<>(); // list of script pathnames to run before train is built
    protected List<String> _afterBuildScripts = new ArrayList<>(); // list of script pathnames to run after train is
                                                                   // built
    protected List<String> _moveScripts = new ArrayList<>(); // list of script pathnames to run when train is moved
    protected List<String> _terminationScripts = new ArrayList<>(); // list of script pathnames to run when train is
                                                                    // terminated
    protected String _railroadName = NONE; // optional railroad name for this train
    protected String _logoPathName = NONE; // optional manifest logo for this train
    protected boolean _showTimes = true; // when true, show arrival and departure times for this train
    protected Engine _leadEngine = null; // lead engine for icon
    protected String _switchListStatus = UNKNOWN; // print switch list status
    protected String _comment = NONE;
    protected String _serviceStatus = NONE; // status only if train is being built
    protected int _statusCode = CODE_UNKNOWN;
    protected int _oldStatusCode = CODE_UNKNOWN;
    protected String _statusTerminatedDate = NONE;
    protected int _statusCarsRequested = 0;
    protected String _tableRowColorName = NONE; // color of row in Trains table
    protected String _tableRowColorResetName = NONE; // color of row in Trains table when reset

    // Engine change and helper engines
    protected int _leg2Options = NO_CABOOSE_OR_FRED; // options
    protected RouteLocation _leg2Start = null; // route location when 2nd leg begins
    protected RouteLocation _end2Leg = null; // route location where 2nd leg ends
    protected String _leg2Engines = "0"; // number of engines 2nd leg
    protected String _leg2Road = NONE; // engine road name 2nd leg
    protected String _leg2Model = NONE; // engine model 2nd leg
    protected String _leg2CabooseRoad = NONE; // road name for caboose 2nd leg

    protected int _leg3Options = NO_CABOOSE_OR_FRED; // options
    protected RouteLocation _leg3Start = null; // route location when 3rd leg begins
    protected RouteLocation _leg3End = null; // route location where 3rd leg ends
    protected String _leg3Engines = "0"; // number of engines 3rd leg
    protected String _leg3Road = NONE; // engine road name 3rd leg
    protected String _leg3Model = NONE; // engine model 3rd leg
    protected String _leg3CabooseRoad = NONE; // road name for caboose 3rd leg

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
    public static final String TRAIN_ROW_COLOR_CHANGED_PROPERTY = "TrianRowColor"; // NOI18N
    public static final String TRAIN_ROW_COLOR_RESET_CHANGED_PROPERTY = "TrianRowColorReset"; // NOI18N
    public static final String TRAIN_MODIFIED_CHANGED_PROPERTY = "TrainModified"; // NOI18N

    // Train status
    public static final String TRAIN_RESET = Bundle.getMessage("TrainReset");
    public static final String RUN_SCRIPTS = Bundle.getMessage("RunScripts");
    public static final String BUILDING = Bundle.getMessage("Building");
    public static final String BUILD_FAILED = Bundle.getMessage("BuildFailed");
    public static final String BUILT = Bundle.getMessage("Built");
    public static final String PARTIAL_BUILT = Bundle.getMessage("Partial");
    public static final String TRAIN_EN_ROUTE = Bundle.getMessage("TrainEnRoute");
    public static final String TERMINATED = Bundle.getMessage("Terminated");
    public static final String MANIFEST_MODIFIED = Bundle.getMessage("Modified");

    // Train status codes
    public static final int CODE_TRAIN_RESET = 0;
    public static final int CODE_RUN_SCRIPTS = 0x100;
    public static final int CODE_BUILDING = 0x01;
    public static final int CODE_BUILD_FAILED = 0x02;
    public static final int CODE_BUILT = 0x10;
    public static final int CODE_PARTIAL_BUILT = CODE_BUILT + 0x04;
    public static final int CODE_TRAIN_EN_ROUTE = CODE_BUILT + 0x08;
    public static final int CODE_TERMINATED = 0x80;
    public static final int CODE_MANIFEST_MODIFIED = 0x200;
    public static final int CODE_UNKNOWN = 0xFFFF;

    // train requirements
    public static final int NO_CABOOSE_OR_FRED = 0; // default
    public static final int CABOOSE = 1;
    public static final int FRED = 2;

    // road options
    public static final String ALL_ROADS = Bundle.getMessage("All");
    public static final String INCLUDE_ROADS = Bundle.getMessage("Include");
    public static final String EXCLUDE_ROADS = Bundle.getMessage("Exclude");

    // owner options
    public static final String ALL_OWNERS = Bundle.getMessage("All");
    public static final String INCLUDE_OWNERS = Bundle.getMessage("Include");
    public static final String EXCLUDE_OWNERS = Bundle.getMessage("Exclude");

    // load options
    public static final String ALL_LOADS = Bundle.getMessage("All");
    public static final String INCLUDE_LOADS = Bundle.getMessage("Include");
    public static final String EXCLUDE_LOADS = Bundle.getMessage("Exclude");

    // Switch list status
    public static final String UNKNOWN = "";
    public static final String PRINTED = Bundle.getMessage("Printed");

    public static final String AUTO = Bundle.getMessage("Auto");
    public static final String AUTO_HPT = Bundle.getMessage("AutoHPT");

    public Train(String id, String name) {
        log.debug("New train ({}) id: {}", name, id);
        _name = name;
        _id = id;
        // a new train accepts all types
        setTypeNames(InstanceManager.getDefault(CarTypes.class).getNames());
        setTypeNames(InstanceManager.getDefault(EngineTypes.class).getNames());
        addPropertyChangeListerners();
    }

    @Override
    public String getId() {
        return _id;
    }

    /**
     * Sets the name of this train, normally a short name that can fit within the
     * train icon.
     *
     * @param name the train's name.
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
    @Override
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
     * @return The name of the color when highlighting the train's row
     */
    public String getTableRowColorName() {
        return _tableRowColorName;
    }

    public void setTableRowColorName(String colorName) {
        String old = _tableRowColorName;
        _tableRowColorName = colorName;
        if (!old.equals(colorName)) {
            setDirtyAndFirePropertyChange(TRAIN_ROW_COLOR_CHANGED_PROPERTY, old, colorName);
        }
    }

    /**
     * @return The name of the train row color when the train is reset
     */
    public String getRowColorNameReset() {
        return _tableRowColorResetName;
    }

    public void setRowColorNameReset(String colorName) {
        String old = _tableRowColorResetName;
        _tableRowColorResetName = colorName;
        if (!old.equals(colorName)) {
            setDirtyAndFirePropertyChange(TRAIN_ROW_COLOR_RESET_CHANGED_PROPERTY, old, colorName);
        }
    }

    /**
     * @return The color when highlighting the train's row
     */
    public Color getTableRowColor() {
        String colorName = getTableRowColorName();
        if (colorName.equals(NONE)) {
            return null;
        } else {
            return Setup.getColor(colorName);
        }
    }

    /**
     * Get's train's departure time
     *
     * @return train's departure time in the String format hh:mm
     */
    public String getDepartureTime() {
        // check to see if the route has a departure time
        RouteLocation rl = getTrainDepartsRouteLocation();
        if (rl != null) {
            rl.removePropertyChangeListener(this);
            rl.addPropertyChangeListener(this);
            if (!rl.getDepartureTime().equals(RouteLocation.NONE)) {
                return rl.getDepartureTime();
            }
        }
        return _departureTime;
    }

    /**
     * Get's train's departure time in 12hr or 24hr format
     *
     * @return train's departure time in the String format hh:mm or hh:mm(AM/PM)
     */
    public String getFormatedDepartureTime() {
        // check to see if the route has a departure time
        RouteLocation rl = getTrainDepartsRouteLocation();
        if (rl != null && !rl.getDepartureTime().equals(RouteLocation.NONE)) {
            // need to forward any changes to departure time
            rl.removePropertyChangeListener(this);
            rl.addPropertyChangeListener(this);
            return rl.getFormatedDepartureTime();
        }
        return (parseTime(getDepartTimeMinutes()));
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
        String old = _departureTime;
        int h = Integer.parseInt(hour);
        if (h < 10) {
            hour = "0" + h;
        }
        int m = Integer.parseInt(minute);
        if (m < 10) {
            minute = "0" + m;
        }
        String time = hour + ":" + minute;
        _departureTime = time;
        if (!old.equals(time)) {
            setDirtyAndFirePropertyChange(DEPARTURETIME_CHANGED_PROPERTY, old, _departureTime);
            setModified(true);
        }
    }

    public String getDepartureTimeHour() {
        String[] time = getDepartureTime().split(":");
        return time[0];
    }

    public String getDepartureTimeMinute() {
        String[] time = getDepartureTime().split(":");
        return time[1];
    }

    public static final String ALREADY_SERVICED = "-1"; // NOI18N

    /**
     * Gets the expected time when this train will arrive at the location rl.
     * Expected arrival time is based on the number of car pick up and set outs for
     * this train. TODO Doesn't provide expected arrival time if train is in route,
     * instead provides relative time. If train is at or has passed the location
     * return -1.
     *
     * @param routeLocation The RouteLocation.
     * @return expected arrival time in minutes (append AM or PM if 12 hour format)
     */
    public String getExpectedArrivalTime(RouteLocation routeLocation) {
        int minutes = getExpectedTravelTimeInMinutes(routeLocation);
        if (minutes == -1) {
            return ALREADY_SERVICED;
        }
        log.debug("Expected arrival time for train ({}) at ({}), {} minutes", getName(), routeLocation.getName(),
                minutes);
        // TODO use fast clock to get current time vs departure time
        // for now use relative
        return parseTime(minutes);
    }

    public String getExpectedDepartureTime(RouteLocation routeLocation) {
        int minutes = getExpectedTravelTimeInMinutes(routeLocation);
        if (minutes == -1) {
            return ALREADY_SERVICED;
        }
        // figure out the work at this location, note that there can be
        // consecutive locations with the same name
        if (getRoute() != null) {
            boolean foundRouteLocation = false;
            for (RouteLocation rl : getRoute().getLocationsBySequenceList()) {
                if (rl == routeLocation) {
                    foundRouteLocation = true;
                }
                if (foundRouteLocation) {
                    if (TrainCommon.splitString(rl.getName())
                            .equals(TrainCommon.splitString(routeLocation.getName()))) {
                        minutes = minutes + getWorkTimeAtLocation(rl);
                    } else {
                        break; // done
                    }
                }
            }
        }
        log.debug("Expected departure time {} for train ({}) at ({})", minutes, getName(), routeLocation.getName());
        return parseTime(minutes);
    }

    public int getWorkTimeAtLocation(RouteLocation routeLocation) {
        int minutes = 0;
        // departure?
        if (routeLocation == getTrainDepartsRouteLocation()) {
            return minutes;
        }
        // add any work at this location
        for (Car rs : InstanceManager.getDefault(CarManager.class).getList(this)) {
            if (rs.getRouteLocation() == routeLocation && !rs.getTrackName().equals(RollingStock.NONE)) {
                minutes += Setup.getSwitchTime();
            }
            if (rs.getRouteDestination() == routeLocation) {
                minutes += Setup.getSwitchTime();
            }
        }
        return minutes;
    }

    public int getExpectedTravelTimeInMinutes(RouteLocation routeLocation) {
        int minutes = 0;
        if (!isTrainEnRoute()) {
            minutes += Integer.parseInt(getDepartureTimeMinute());
            minutes += 60 * Integer.parseInt(getDepartureTimeHour());
        } else {
            minutes = -1; // -1 means train has already served the location
        }
        // boolean trainAt = false;
        boolean trainLocFound = false;
        if (getRoute() != null) {
            List<RouteLocation> routeList = getRoute().getLocationsBySequenceList();
            for (int i = 0; i < routeList.size(); i++) {
                RouteLocation rl = routeList.get(i);
                if (rl == routeLocation) {
                    break; // done
                }
                // start recording time after finding where the train is
                if (!trainLocFound && isTrainEnRoute()) {
                    if (rl == getCurrentRouteLocation()) {
                        trainLocFound = true;
                        // add travel time
                        minutes = Setup.getTravelTime();
                    }
                    continue;
                }
                // is there a departure time from this location?
                if (!rl.getDepartureTime().equals(RouteLocation.NONE)) {
                    String dt = rl.getDepartureTime();
                    log.debug("Location {} departure time {}", rl.getName(), dt);
                    String[] time = dt.split(":");
                    minutes = 60 * Integer.parseInt(time[0]) + Integer.parseInt(time[1]);
                    // log.debug("New minutes: "+minutes);
                }
                // add wait time
                minutes += rl.getWait();
                // add travel time if new location
                RouteLocation next = routeList.get(i + 1);
                if (next != null &&
                        !TrainCommon.splitString(rl.getName()).equals(TrainCommon.splitString(next.getName()))) {
                    minutes += Setup.getTravelTime();
                }
                // don't count work if there's a departure time
                if (i == 0 || !rl.getDepartureTime().equals(RouteLocation.NONE)) {
                    continue;
                }
                // now add the work at the location
                minutes = minutes + getWorkTimeAtLocation(rl);
            }
        }
        return minutes;
    }

    /**
     * Returns time in hour:minute format
     *
     * @param minutes number of minutes from midnight
     * @return hour:minute (optionally AM:PM format)
     */
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
            if (hours == 0) {
                hours = 12;
            }
        }

        String h = Integer.toString(hours);
        if (hours < 10) {
            h = "0" + h;
        }
        if (minutes < 10) {
            return d + h + ":0" + minutes + am_pm; // NOI18N
        }
        return d + h + ":" + minutes + am_pm;
    }

    /**
     * Set train requirements. If NO_CABOOSE_OR_FRED, then train doesn't require a
     * caboose or car with FRED.
     *
     * @param requires NO_CABOOSE_OR_FRED, CABOOSE, FRED
     */
    public void setRequirements(int requires) {
        int old = _requires;
        _requires = requires;
        if (old != requires) {
            setDirtyAndFirePropertyChange(TRAIN_REQUIREMENTS_CHANGED_PROPERTY, Integer.toString(old),
                    Integer.toString(requires));
        }
    }

    /**
     * Get a train's requirements with regards to the last car in the train.
     *
     * @return NONE CABOOSE FRED
     */
    public int getRequirements() {
        return _requires;
    }

    public boolean isCabooseNeeded() {
        return (getRequirements() & CABOOSE) == CABOOSE;
    }

    public boolean isFredNeeded() {
        return (getRequirements() & FRED) == FRED;
    }

    public void setRoute(Route route) {
        Route old = _route;
        String oldRoute = NONE;
        String newRoute = NONE;
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
        if (getRoute() == null) {
            return NONE;
        }
        return getRoute().getName();
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
        return NONE;
    }

    public RouteLocation getTrainDepartsRouteLocation() {
        if (getRoute() == null) {
            return null;
        }
        return getRoute().getDepartsRouteLocation();
    }

    public String getTrainDepartsDirection() {
        String direction = NONE;
        if (getTrainDepartsRouteLocation() != null) {
            direction = getTrainDepartsRouteLocation().getTrainDirectionString();
        }
        return direction;
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
        return NONE;
    }

    public RouteLocation getTrainTerminatesRouteLocation() {
        if (getRoute() == null) {
            return null;
        }
        return getRoute().getTerminatesRouteLocation();
    }
    
    /**
     * Returns the order the train should be blocked.
     * @return routeLocations for this train.
     */
    public List<RouteLocation> getTrainBlockingOrder() {
        if (getRoute() == null) {
            return null;
        }
        return getRoute().getLocationsBySequenceList();
    }

    /**
     * Set train's current route location
     *
     * @param location The current RouteLocation.
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
        if (getCurrentRouteLocation() == null) {
            return NONE;
        }
        return getCurrentRouteLocation().getName();
    }

    /**
     * Get train's current route location
     *
     * @return Train's current route location
     */
    public RouteLocation getCurrentRouteLocation() {
        if (getRoute() == null) {
            return null;
        }
        if (_current == null) {
            return null;
        }
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
     * Get a location name in a train's route from the current train's location. A
     * number of "1" means get the next location name in a train's route.
     *
     * @param number The stop number, must be greater than 0
     * @return Name of the location that is the number of stops away from the
     *         train's current location.
     */
    public String getNextLocationName(int number) {
        RouteLocation rl = getCurrentRouteLocation();
        while (number-- > 0) {
            rl = getNextRouteLocation(rl);
            if (rl == null) {
                return NONE;
            }
        }
        return rl.getName();
    }

    public RouteLocation getNextRouteLocation(RouteLocation currentRouteLocation) {
        if (getRoute() == null) {
            return null;
        }
        List<RouteLocation> routeList = getRoute().getLocationsBySequenceList();
        for (int i = 0; i < routeList.size(); i++) {
            RouteLocation rl = routeList.get(i);
            if (rl == currentRouteLocation) {
                i++;
                if (i < routeList.size()) {
                    return routeList.get(i);
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
     * Set the train's machine readable status. Calls update train table row color.
     *
     * @param code machine readable
     */
    public void setStatusCode(int code) {
        String oldStatus = getStatus();
        int oldCode = getStatusCode();
        _statusCode = code;
        // always fire property change for train en route
        if (oldCode != getStatusCode() || code == CODE_TRAIN_EN_ROUTE) {
            setDirtyAndFirePropertyChange(STATUS_CHANGED_PROPERTY, oldStatus, getStatus());
        }
        updateTrainTableRowColor();
    }

    private void updateTrainTableRowColor() {
        if (!InstanceManager.getDefault(TrainManager.class).isRowColorManual()) {
            switch (getStatusCode()) {
                case CODE_TRAIN_RESET:
                    setTableRowColorName(getRowColorNameReset());
                    break;
                case CODE_BUILT:
                case CODE_PARTIAL_BUILT:
                    setTableRowColorName(InstanceManager.getDefault(TrainManager.class).getRowColorNameForBuilt());
                    break;
                case CODE_BUILD_FAILED:
                    setTableRowColorName(
                            InstanceManager.getDefault(TrainManager.class).getRowColorNameForBuildFailed());
                    break;
                case CODE_TRAIN_EN_ROUTE:
                    setTableRowColorName(
                            InstanceManager.getDefault(TrainManager.class).getRowColorNameForTrainEnRoute());
                    break;
                case CODE_TERMINATED:
                    setTableRowColorName(InstanceManager.getDefault(TrainManager.class).getRowColorNameForTerminated());
                    break;
                default: // all other cases do nothing
                    break;
            }
        }
    }

    /**
     * Get train's status in the default locale.
     *
     * @return Human-readable status
     */
    public String getStatus() {
        return this.getStatus(Locale.getDefault());
    }

    /**
     * Get train's status in the specified locale.
     *
     * @param locale The Locale.
     * @return Human-readable status
     */
    public String getStatus(Locale locale) {
        return this.getStatus(locale, this.getStatusCode());
    }

    /**
     * Get the human-readable status for the requested status code.
     *
     * @param locale The Locale.
     * @param code   requested status
     * @return Human-readable status
     */
    public String getStatus(Locale locale, int code) {
        switch (code) {
            case CODE_RUN_SCRIPTS:
                return RUN_SCRIPTS;
            case CODE_BUILDING:
                return BUILDING;
            case CODE_BUILD_FAILED:
                return BUILD_FAILED;
            case CODE_BUILT:
                // getNumberCarsWorked() is assumed to be constant if status is
                // "built" or "partially built"
                return Bundle.getMessage(locale, "StatusBuilt", this.getNumberCarsWorked()); // NOI18N
            case CODE_PARTIAL_BUILT:
                // 0 should be number of cars requested to be worked
                return Bundle.getMessage(locale, "StatusPartialBuilt", this.getNumberCarsWorked(),
                        this.getNumberCarsRequested()); // NOI18N
            case CODE_TERMINATED:
                return Bundle.getMessage(locale, "StatusTerminated", this.getTerminationDate()); // NOI18N
            case CODE_TRAIN_EN_ROUTE:
                return Bundle.getMessage(locale, "StatusEnRoute", this.getNumberCarsInTrain(), this.getTrainLength(),
                        Setup.getLengthUnit().toLowerCase(), this.getTrainWeight()); // NOI18N
            case CODE_TRAIN_RESET:
                return TRAIN_RESET;
            case CODE_MANIFEST_MODIFIED:
                return MANIFEST_MODIFIED;
            case CODE_UNKNOWN:
            default:
                return UNKNOWN;
        }
    }

    public String getMRStatus() {
        switch (getStatusCode()) {
            case CODE_PARTIAL_BUILT:
                return getStatusCode() + "||" + this.getNumberCarsRequested(); // NOI18N
            case CODE_TERMINATED:
                return getStatusCode() + "||" + this.getTerminationDate(); // NOI18N
            default:
                return Integer.toString(getStatusCode());
        }
    }

    public int getStatusCode() {
        return _statusCode;
    }

    protected void setOldStatusCode(int code) {
        _oldStatusCode = code;
    }

    protected int getOldStatusCode() {
        return _oldStatusCode;
    }

    /**
     * Used to determine if train has departed the first location in the train's
     * route
     *
     * @return true if train has departed
     */
    public boolean isTrainEnRoute() {
        return !getCurrentLocationName().equals(NONE) && getTrainDepartsRouteLocation() != getCurrentRouteLocation();
    }

    /**
     * Used to determine if train is a local switcher serving one location. Note the
     * train can have more than location in its route, but all location names must
     * be "same". See TrainCommon.splitString(String name) for the definition of the
     * "same" name.
     *
     * @return true if local switcher
     */
    public boolean isLocalSwitcher() {
        String departureName = TrainCommon.splitString(getTrainDepartsName());
        Route route = getRoute();
        if (route != null) {
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                String name = TrainCommon.splitString(rl.getName());
                if (!departureName.equals(name)) {
                    return false; // not a local switcher
                }
            }
        }
        return true;
    }

    /**
     * Used to determine if train is carrying only passenger cars.
     *
     * @return true if only passenger cars have been assigned to this train.
     */
    public boolean isOnlyPassengerCars() {
        for (Car car : InstanceManager.getDefault(CarManager.class).getByTrainDestinationList(this)) {
            if (!car.isPassenger()) {
                return false;
            }
        }
        return true;
    }

    List<String> _skipLocationsList = new ArrayList<>();

    protected String[] getTrainSkipsLocations() {
        String[] locationIds = new String[_skipLocationsList.size()];
        for (int i = 0; i < _skipLocationsList.size(); i++) {
            locationIds[i] = _skipLocationsList.get(i);
        }
        return locationIds;
    }

    protected void setTrainSkipsLocations(String[] locationIds) {
        if (locationIds.length == 0) {
            return;
        }
        java.util.Arrays.sort(locationIds);
        for (String id : locationIds) {
            _skipLocationsList.add(id);
        }
    }

    /**
     * Train will skip the RouteLocation
     *
     * @param routelocationId RouteLocation Id
     */
    public void addTrainSkipsLocation(String routelocationId) {
        // insert at start of _skipLocationsList, sort later
        if (_skipLocationsList.contains(routelocationId)) {
            return;
        }
        _skipLocationsList.add(0, routelocationId);
        log.debug("train does not stop at {}", routelocationId);
        setDirtyAndFirePropertyChange(STOPS_CHANGED_PROPERTY, _skipLocationsList.size() - 1, _skipLocationsList.size());
    }

    public void deleteTrainSkipsLocation(String locationId) {
        _skipLocationsList.remove(locationId);
        log.debug("train will stop at {}", locationId);
        setDirtyAndFirePropertyChange(STOPS_CHANGED_PROPERTY, _skipLocationsList.size() + 1, _skipLocationsList.size());
    }

    /**
     * Determines if this train skips a location (doesn't service the location).
     *
     * @param locationId The route location id.
     * @return true if the train will not service the location.
     */
    public boolean isLocationSkipped(String locationId) {
        return _skipLocationsList.contains(locationId);
    }

    List<String> _typeList = new ArrayList<>();

    /**
     * Get's the type names of rolling stock this train will service
     *
     * @return The type names for cars and or engines
     */
    protected String[] getTypeNames() {
        return _typeList.toArray(new String[0]);
    }

    public String[] getCarTypeNames() {
        List<String> list = new ArrayList<>();
        for (String type : _typeList) {
            if (InstanceManager.getDefault(CarTypes.class).containsName(type)) {
                list.add(type);
            }
        }
        return list.toArray(new String[0]);
    }

    public String[] getLocoTypeNames() {
        List<String> list = new ArrayList<>();
        for (String type : _typeList) {
            if (InstanceManager.getDefault(EngineTypes.class).containsName(type)) {
                list.add(type);
            }
        }
        return list.toArray(new String[0]);
    }

    /**
     * Set the type of cars or engines this train will service, see types in Cars
     * and Engines.
     *
     * @param types The type names for cars and or engines
     */
    protected void setTypeNames(String[] types) {
        if (types.length > 0) {
            java.util.Arrays.sort(types);
            for (String type : types) {
                _typeList.add(type);
            }
        }
    }

    /**
     * Add a car or engine type name that this train will service.
     *
     * @param type The new type name to service.
     */
    public void addTypeName(String type) {
        // insert at start of list, sort later
        if (type == null || _typeList.contains(type)) {
            return;
        }
        _typeList.add(0, type);
        log.debug("Train ({}) add car type ({})", getName(), type);
        setDirtyAndFirePropertyChange(TYPES_CHANGED_PROPERTY, _typeList.size() - 1, _typeList.size());
    }

    public void deleteTypeName(String type) {
        if (!_typeList.contains(type)) {
            return;
        }
        _typeList.remove(type);
        log.debug("Train ({}) delete car type ({})", getName(), type);
        setDirtyAndFirePropertyChange(TYPES_CHANGED_PROPERTY, _typeList.size() + 1, _typeList.size());
    }

    /**
     * Returns true if this train will service the type of car or engine.
     *
     * @param type The car or engine type name.
     * @return true if this train will service the particular type.
     */
    public boolean isTypeNameAccepted(String type) {
        return _typeList.contains(type);
    }

    protected void replaceType(String oldType, String newType) {
        if (isTypeNameAccepted(oldType)) {
            deleteTypeName(oldType);
            addTypeName(newType);
            // adjust custom loads
            for (String load : getLoadNames()) {
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
     * @return ALL_ROADS INCLUDE_ROADS EXCLUDE_ROADS
     */
    public String getRoadOption() {
        return _roadOption;
    }

    /**
     * Set how this train deals with car road names.
     *
     * @param option ALL_ROADS INCLUDE_ROADS EXCLUDE_ROADS
     */
    public void setRoadOption(String option) {
        String old = _roadOption;
        _roadOption = option;
        setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, old, option);
    }

    List<String> _roadList = new ArrayList<>();

    protected void setRoadNames(String[] roads) {
        if (roads.length > 0) {
            java.util.Arrays.sort(roads);
            for (String road : roads) {
                if (!road.isEmpty()) {
                    _roadList.add(road);
                }
            }
        }
    }

    /**
     * Provides a list of road names that the train will either service or exclude.
     * See setRoadOption
     *
     * @return Array of sorted road names as Strings
     */
    public String[] getRoadNames() {
        String[] roads = _roadList.toArray(new String[0]);
        if (_roadList.size() > 0) {
            java.util.Arrays.sort(roads);
        }
        return roads;
    }

    /**
     * Add a road name that the train will either service or exclude. See
     * setRoadOption
     *
     * @param road The string road name.
     * @return true if road name was added, false if road name wasn't in the list.
     */
    public boolean addRoadName(String road) {
        if (_roadList.contains(road)) {
            return false;
        }
        _roadList.add(road);
        log.debug("train ({}) add car road {}", getName(), road);
        setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, _roadList.size() - 1, _roadList.size());
        return true;
    }

    /**
     * Delete a road name that the train will either service or exclude. See
     * setRoadOption
     *
     * @param road The string road name to delete.
     * @return true if road name was removed, false if road name wasn't in the list.
     */
    public boolean deleteRoadName(String road) {
        if (!_roadList.contains(road)) {
            return false;
        }
        _roadList.remove(road);
        log.debug("train ({}) delete car road {}", getName(), road);
        setDirtyAndFirePropertyChange(ROADS_CHANGED_PROPERTY, _roadList.size() + 1, _roadList.size());
        return true;
    }

    /**
     * Determine if train will service a specific road name.
     *
     * @param road the road name to check.
     * @return true if train will service this road name.
     */
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

    protected void replaceRoad(String oldRoad, String newRoad) {
        if (newRoad != null) {
            if (deleteRoadName(oldRoad)) {
                addRoadName(newRoad);
            }
            if (getEngineRoad().equals(oldRoad)) {
                setEngineRoad(newRoad);
            }
            if (getCabooseRoad().equals(oldRoad)) {
                setCabooseRoad(newRoad);
            }
            if (getSecondLegEngineRoad().equals(oldRoad)) {
                setSecondLegEngineRoad(newRoad);
            }
            if (getSecondLegCabooseRoad().equals(oldRoad)) {
                setSecondLegCabooseRoad(newRoad);
            }
            if (getThirdLegEngineRoad().equals(oldRoad)) {
                setThirdLegEngineRoad(newRoad);
            }
            if (getThirdLegCabooseRoad().equals(oldRoad)) {
                setThirdLegCabooseRoad(newRoad);
            }
        }
    }

    /**
     * Gets the car load option for this train.
     *
     * @return ALL_LOADS INCLUDE_LOADS EXCLUDE_LOADS
     */
    public String getLoadOption() {
        return _loadOption;
    }

    /**
     * Set how this train deals with car loads
     *
     * @param option ALL_LOADS INCLUDE_LOADS EXCLUDE_LOADS
     */
    public void setLoadOption(String option) {
        String old = _loadOption;
        _loadOption = option;
        setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, old, option);
    }

    List<String> _loadList = new ArrayList<>();

    protected void setLoadNames(String[] loads) {
        if (loads.length > 0) {
            java.util.Arrays.sort(loads);
            for (String load : loads) {
                if (!load.isEmpty()) {
                    _loadList.add(load);
                }
            }
        }
    }

    /**
     * Provides a list of loads that the train will either service or exclude. See
     * setLoadOption
     *
     * @return Array of load names as Strings
     */
    public String[] getLoadNames() {
        String[] loads = _loadList.toArray(new String[0]);
        if (_loadList.size() > 0) {
            java.util.Arrays.sort(loads);
        }
        return loads;
    }

    /**
     * Add a load that the train will either service or exclude. See setLoadOption
     *
     * @param load The string load name.
     * @return true if load name was added, false if load name wasn't in the list.
     */
    public boolean addLoadName(String load) {
        if (_loadList.contains(load)) {
            return false;
        }
        _loadList.add(load);
        log.debug("train ({}) add car load {}", getName(), load);
        setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, _loadList.size() - 1, _loadList.size());
        return true;
    }

    /**
     * Delete a load name that the train will either service or exclude. See
     * setLoadOption
     *
     * @param load The string load name.
     * @return true if load name was removed, false if load name wasn't in the list.
     */
    public boolean deleteLoadName(String load) {
        if (!_loadList.contains(load)) {
            return false;
        }
        _loadList.remove(load);
        log.debug("train ({}) delete car load {}", getName(), load);
        setDirtyAndFirePropertyChange(LOADS_CHANGED_PROPERTY, _loadList.size() + 1, _loadList.size());
        return true;
    }

    /**
     * Determine if train will service a specific load name.
     *
     * @param load the load name to check.
     * @return true if train will service this load.
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
     * Determine if train will service a specific load and car type.
     *
     * @param load the load name to check.
     * @param type the type of car used to carry the load.
     * @return true if train will service this load.
     */
    public boolean isLoadNameAccepted(String load, String type) {
        if (_loadOption.equals(ALL_LOADS)) {
            return true;
        }
        if (_loadOption.equals(INCLUDE_LOADS)) {
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
     * @param option ALL_OWNERS INCLUDE_OWNERS EXCLUDE_OWNERS
     */
    public void setOwnerOption(String option) {
        String old = _ownerOption;
        _ownerOption = option;
        setDirtyAndFirePropertyChange(OWNERS_CHANGED_PROPERTY, old, option);
    }

    List<String> _ownerList = new ArrayList<>();

    protected void setOwnerNames(String[] owners) {
        if (owners.length == 0) {
            return;
        }
        java.util.Arrays.sort(owners);
        for (String owner : owners) {
            if (!owner.isEmpty()) {
                _ownerList.add(owner);
            }
        }
    }

    /**
     * Provides a list of owner names that the train will either service or exclude.
     * See setOwnerOption
     *
     * @return Array of owner names as Strings
     */
    public String[] getOwnerNames() {
        String[] owners = _ownerList.toArray(new String[0]);
        if (_ownerList.size() > 0) {
            java.util.Arrays.sort(owners);
        }
        return owners;
    }

    /**
     * Add a owner name that the train will either service or exclude. See
     * setOwnerOption
     *
     * @param owner The string representing the owner's name.
     * @return true if owner name was added, false if owner name wasn't in the list.
     */
    public boolean addOwnerName(String owner) {
        if (_ownerList.contains(owner)) {
            return false;
        }
        _ownerList.add(owner);
        log.debug("train ({}) add car owner {}", getName(), owner);
        setDirtyAndFirePropertyChange(OWNERS_CHANGED_PROPERTY, _ownerList.size() - 1, _ownerList.size());
        return true;
    }

    /**
     * Delete a owner name that the train will either service or exclude. See
     * setOwnerOption
     *
     * @param owner The string representing the owner's name.
     * @return true if owner name was removed, false if owner name wasn't in the
     *         list.
     */
    public boolean deleteOwnerName(String owner) {
        if (!_ownerList.contains(owner)) {
            return false;
        }
        _ownerList.remove(owner);
        log.debug("train ({}) delete car owner {}", getName(), owner);
        setDirtyAndFirePropertyChange(OWNERS_CHANGED_PROPERTY, _ownerList.size() + 1, _ownerList.size());
        return true;
    }

    /**
     * Determine if train will service a specific owner name.
     *
     * @param owner the owner name to check.
     * @return true if train will service this owner name.
     */
    public boolean isOwnerNameAccepted(String owner) {
        if (_ownerOption.equals(ALL_OWNERS)) {
            return true;
        }
        if (_ownerOption.equals(INCLUDE_OWNERS)) {
            return _ownerList.contains(owner);
        }
        // exclude!
        return !_ownerList.contains(owner);
    }

    protected void replaceOwner(String oldName, String newName) {
        if (deleteOwnerName(oldName)) {
            addOwnerName(newName);
        }
    }

    /**
     * Only rolling stock built in or after this year will be used.
     *
     * @param year A string representing a year.
     */
    public void setBuiltStartYear(String year) {
        String old = _builtStartYear;
        _builtStartYear = year;
        if (!old.equals(year)) {
            setDirtyAndFirePropertyChange(BUILT_YEAR_CHANGED_PROPERTY, old, year);
        }
    }

    public String getBuiltStartYear() {
        return _builtStartYear;
    }

    /**
     * Only rolling stock built in or before this year will be used.
     *
     * @param year A string representing a year.
     */
    public void setBuiltEndYear(String year) {
        String old = _builtEndYear;
        _builtEndYear = year;
        if (!old.equals(year)) {
            setDirtyAndFirePropertyChange(BUILT_YEAR_CHANGED_PROPERTY, old, year);
        }
    }

    public String getBuiltEndYear() {
        return _builtEndYear;
    }

    /**
     * Determine if train will service rolling stock by built date.
     *
     * @param date A string representing the built date for a car or engine.
     * @return true is built date is in the acceptable range.
     */
    public boolean isBuiltDateAccepted(String date) {
        if (getBuiltStartYear().equals(NONE) && getBuiltEndYear().equals(NONE)) {
            return true; // range dates not defined
        }
        int startYear = 0; // default start year;
        int endYear = 99999; // default end year;
        int builtYear = -1900;
        if (!getBuiltStartYear().equals(NONE)) {
            try {
                startYear = Integer.parseInt(getBuiltStartYear());
            } catch (NumberFormatException e) {
                log.debug("Train ({}) built start date not initialized, start: {}", getName(), getBuiltStartYear());
            }
        }
        if (!getBuiltEndYear().equals(NONE)) {
            try {
                endYear = Integer.parseInt(getBuiltEndYear());
            } catch (NumberFormatException e) {
                log.debug("Train ({}) built end date not initialized, end: {}", getName(), getBuiltEndYear());
            }
        }
        try {
            builtYear = Integer.parseInt(RollingStockManager.convertBuildDate(date));
        } catch (NumberFormatException e) {
            log.debug("Unable to parse car built date {}", date);
        }
        if (startYear < builtYear && builtYear < endYear) {
            return true;
        }
        return false;
    }

    private final boolean debugFlag = false;

    /**
     * Determines if this train will service this car. Note this code doesn't check
     * the location or tracks that needs to be done separately. See Router.java.
     *
     * @param car The car to be tested.
     * @return true if this train can service the car.
     */
    public boolean isServiceable(Car car) {
        return isServiceable(null, car);
    }

    /**
     * Note that this code was written after TrainBuilder. It does pretty much the
     * same as TrainBuilder but with much fewer build report messages.
     * 
     * @param buildReport PrintWriter
     * @param car         the car to be tested
     * @return true if this train can service the car.
     */
    public boolean isServiceable(PrintWriter buildReport, Car car) {
        setServiceStatus(NONE);
        // check to see if train can carry car
        if (!isTypeNameAccepted(car.getTypeName())) {
            addLine(buildReport, MessageFormat.format(Bundle.getMessage("trainCanNotServiceCarType"),
                    new Object[] { getName(), car.toString(), car.getTypeName() }));
            return false;
        }
        if (!isLoadNameAccepted(car.getLoadName(), car.getTypeName())) {
            addLine(buildReport, MessageFormat.format(Bundle.getMessage("trainCanNotServiceCarLoad"),
                    new Object[] { getName(), car.toString(), car.getTypeName(), car.getLoadName() }));
            return false;
        }
        if (!isBuiltDateAccepted(car.getBuilt()) ||
                !isOwnerNameAccepted(car.getOwner()) ||
                !isRoadNameAccepted(car.getRoadName())) {
            addLine(buildReport, MessageFormat.format(Bundle.getMessage("trainCanNotServiceCar"),
                    new Object[] { getName(), car.toString() }));
            return false;
        }

        Route route = getRoute();
        if (route == null) {
            return false;
        }

        if (car.getLocation() == null || car.getTrack() == null) {
            return false;
        }

        // determine if the car's location and destination is serviced by this
        // train
        if (route.getLastLocationByName(car.getLocationName()) == null) {
            addLine(buildReport, MessageFormat.format(Bundle.getMessage("trainNotThisLocation"),
                    new Object[] { getName(), car.getLocationName() }));
            return false;
        }
        if (car.getDestination() != null && route.getLastLocationByName(car.getDestinationName()) == null) {
            addLine(buildReport, MessageFormat.format(Bundle.getMessage("trainNotThisLocation"),
                    new Object[] { getName(), car.getDestinationName() }));
            return false;
        }
        // now find the car in the train's route
        List<RouteLocation> rLocations = route.getLocationsBySequenceList();
        for (RouteLocation rLoc : rLocations) {
            if (rLoc.getName().equals(car.getLocationName()) &&
                    rLoc.isPickUpAllowed() &&
                    rLoc.getMaxCarMoves() > 0 &&
                    !isLocationSkipped(rLoc.getId()) &&
                    ((car.getLocation().getTrainDirections() & rLoc.getTrainDirection()) != 0 || isLocalSwitcher())) {

                if (((car.getTrack().getTrainDirections() & rLoc.getTrainDirection()) == 0 && !isLocalSwitcher()) ||
                        !car.getTrack().isPickupTrainAccepted(this)) {
                    addLine(buildReport,
                            MessageFormat.format(Bundle.getMessage("trainCanNotServiceCarFrom"),
                                    new Object[] { getName(), car.toString(), car.getLocationName(), car.getTrackName(),
                                            rLoc.getId() }));
                    continue;
                }
                if (debugFlag) {
                    log.debug("Car ({}) can be picked up by train ({}) location ({}, {}) destination ({}, {})",
                            car.toString(), getName(), car.getLocationName(), car.getTrackName(),
                            car.getDestinationName(), car.getDestinationTrackName());
                }
                addLine(buildReport, MessageFormat.format(Bundle.getMessage("trainCanPickUpCar"), new Object[] {
                        getName(), car.toString(), car.getLocationName(), car.getTrackName(), rLoc.getId() }));
                if (car.getDestination() == null) {
                    if (debugFlag) {
                        log.debug("Car ({}) does not have a destination", car.toString());
                    }
                    return true;
                }
                // now check car's destination
                return isServiceableDestination(buildReport, car, rLoc, rLocations);
            } else if (rLoc.getName().equals(car.getLocationName())) {
                addLine(buildReport, MessageFormat.format(Bundle.getMessage("trainCanNotServiceCarFrom"), new Object[] {
                        getName(), car.toString(), car.getLocationName(), car.getTrackName(), rLoc.getId() }));
            }
        }
        if (debugFlag) {
            log.debug("Train ({}) can't service car ({}) from ({}, {})", getName(), car.toString(),
                    car.getLocationName(), car.getTrackName());
        }
        return false;
    }

    /**
     * Second step in determining if train can service car, check to see if car's
     * destination is serviced by this train's route.
     * 
     * @param buildReport add messages if needed to build report
     * @param car         The test car
     * @param rLoc        Where in the train's route the car was found
     * @param rLocations  The ordered routeLocations in this train's route
     * @return true if car's destination can be serviced
     */
    private boolean isServiceableDestination(PrintWriter buildReport, Car car, RouteLocation rLoc,
            List<RouteLocation> rLocations) {
        // need the car's length when building train
        int length = car.getTotalLength();
        // car can be a kernel so get total length
        if (car.getKernel() != null) {
            length = car.getKernel().getTotalLength();
        }
        // now see if the train's route services the car's destination
        for (int k = rLocations.indexOf(rLoc); k < rLocations.size(); k++) {
            RouteLocation rldest = rLocations.get(k);
            if (rldest.getName().equals(car.getDestinationName()) &&
                    rldest.isDropAllowed() &&
                    rldest.getMaxCarMoves() > 0 &&
                    !isLocationSkipped(rldest.getId()) &&
                    ((car.getDestination().getTrainDirections() & rldest.getTrainDirection()) != 0 ||
                            isLocalSwitcher()) &&
                    (!Setup.isCheckCarDestinationEnabled() ||
                            car.getTrack().isDestinationAccepted(car.getDestination()))) {
                // found a destination, now check destination track
                if (car.getDestinationTrack() != null) {
                    if (!isServicableTrack(buildReport, car, rldest, car.getDestinationTrack())) {
                        continue;
                    }
                } else if (rldest.getLocation().isStaging() &&
                        getStatusCode() == CODE_BUILDING &&
                        getTerminationTrack() != null &&
                        getTerminationTrack().getLocation() == rldest.getLocation()) {
                    if (debugFlag) {
                        log.debug("Car ({}) destination is staging, check train ({}) termination track ({})",
                                car.toString(), getName(), getTerminationTrack().getName());
                    }
                    String status = car.testDestination(getTerminationTrack().getLocation(), getTerminationTrack());
                    if (!status.equals(Track.OKAY)) {
                        addLine(buildReport,
                                MessageFormat.format(Bundle.getMessage("trainCanNotDeliverToStaging"),
                                        new Object[] { getName(), car.toString(),
                                                getTerminationTrack().getLocation().getName(),
                                                getTerminationTrack().getName(), status }));
                        continue;
                    }
                } else {
                    if (debugFlag) {
                        log.debug("Find track for car ({}) at destination ({})", car.toString(),
                                car.getDestinationName());
                    }
                    // determine if there's a destination track that is willing to accept this car
                    String status = "";
                    List<Track> tracks = rldest.getLocation().getTracksList();
                    for (Track track : tracks) {
                        if (!isServicableTrack(buildReport, car, rldest, track)) {
                            continue;
                        }
                        // will the track accept this car?
                        status = track.isRollingStockAccepted(car);
                        if (status.equals(Track.OKAY) || status.startsWith(Track.LENGTH)) {
                            if (debugFlag) {
                                log.debug("Found track ({}) for car ({})", track.getName(), car.toString());
                            }
                            break; // found track
                        }
                    }
                    if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
                        if (debugFlag) {
                            log.debug("Destination ({}) can not service car ({}) using train ({}) no track available",
                                    car.getDestinationName(), car.toString(), getName()); // NOI18N
                        }
                        addLine(buildReport, MessageFormat.format(Bundle.getMessage("trainCanNotDeliverNoTracks"),
                                new Object[] { getName(), car.toString(), car.getDestinationName(), rldest.getId() }));
                        continue;
                    }
                }
                // restriction to only carry cars to terminal?
                if (isSendCarsToTerminalEnabled() &&
                        !TrainCommon.splitString(car.getLocationName())
                                .equals(TrainCommon.splitString(getTrainDepartsName())) &&
                        !TrainCommon.splitString(car.getDestinationName())
                                .equals(TrainCommon.splitString(getTrainTerminatesName()))) {
                    if (debugFlag) {
                        log.debug("option send cars to terminal is enabled");
                    }
                    // check to see if local move allowed
                    if (!isAllowLocalMovesEnabled() ||
                            isAllowLocalMovesEnabled() &&
                                    !TrainCommon.splitString(car.getLocationName())
                                            .equals(TrainCommon.splitString(car.getDestinationName())))
                        addLine(buildReport,
                                MessageFormat.format(Bundle.getMessage("trainCanNotCarryCarOption"),
                                        new Object[] { getName(), car.toString(), car.getLocationName(),
                                                car.getTrackName(), car.getDestinationName(),
                                                car.getDestinationTrackName() }));
                    continue;
                }
                // allow car to return to staging?
                if (isAllowReturnToStagingEnabled() &&
                        car.getTrack().isStaging() &&
                        rldest.getLocation() == car.getLocation()) {
                    addLine(buildReport,
                            MessageFormat.format(Bundle.getMessage("trainCanReturnCarToStaging"),
                                    new Object[] { getName(), car.toString(), car.getDestinationName(),
                                            car.getDestinationTrackName() }));
                    return true;
                }
                // is this a local move?
                if (!isAllowLocalMovesEnabled() &&
                        !car.isCaboose() &&
                        !car.hasFred() &&
                        !car.isPassenger() &&
                        TrainCommon.splitString(car.getLocationName())
                                .equals(TrainCommon.splitString(car.getDestinationName()))) {
                    if (debugFlag) {
                        log.debug("Local move not allowed");
                    }
                    addLine(buildReport, MessageFormat.format(Bundle.getMessage("trainCanNotPerformLocalMove"),
                            new Object[] { getName(), car.toString(), car.getLocationName() }));
                    continue;
                }
                // Can cars travel from origin to terminal?
                if (!isAllowThroughCarsEnabled() &&
                        TrainCommon.splitString(getTrainDepartsName())
                                .equals(TrainCommon.splitString(rLoc.getName())) &&
                        TrainCommon.splitString(getTrainTerminatesName())
                                .equals(TrainCommon.splitString(rldest.getName())) &&
                        !TrainCommon.splitString(getTrainDepartsName())
                                .equals(TrainCommon.splitString(getTrainTerminatesName())) &&
                        !isLocalSwitcher() &&
                        !car.isCaboose() &&
                        !car.hasFred() &&
                        !car.isPassenger()) {
                    if (debugFlag) {
                        log.debug("Through car ({}) not allowed", car.toString());
                    }
                    addLine(buildReport, MessageFormat.format(Bundle.getMessage("trainDoesNotCarryOriginTerminal"),
                            new Object[] { getName(), car.getLocationName(), car.getDestinationName() }));
                    continue;
                }
                // check to see if moves are available
                if (getStatusCode() == CODE_BUILDING && rldest.getMaxCarMoves() - rldest.getCarMoves() <= 0) {
                    setServiceStatus(MessageFormat.format(Bundle.getMessage("trainNoMoves"),
                            new Object[] { getName(), getRoute().getName(), rldest.getId(), rldest.getName() }));
                    if (debugFlag) {
                        log.debug("No available moves for destination {}", rldest.getName());
                    }
                    addLine(buildReport, getServiceStatus());
                    continue;
                }
                if (debugFlag) {
                    log.debug("Car ({}) can be dropped by train ({}) to ({}, {})", car.toString(), getName(),
                            car.getDestinationName(), car.getDestinationTrackName());
                }
                return true;
            }
            // check to see if train length is okay
            if (getStatusCode() == CODE_BUILDING && rldest.getTrainLength() + length > rldest.getMaxTrainLength()) {
                setServiceStatus(MessageFormat.format(Bundle.getMessage("trainExceedsMaximumLength"),
                        new Object[] { getName(), getRoute().getName(), rldest.getId(), rldest.getMaxTrainLength(),
                                Setup.getLengthUnit().toLowerCase(), rldest.getName(), car.toString(),
                                rldest.getTrainLength() + length - rldest.getMaxTrainLength() }));
                if (debugFlag) {
                    log.debug("Car ({}) exceeds maximum train length {} when departing ({})", car.toString(),
                            rldest.getMaxTrainLength(), rldest.getName());
                }
                addLine(buildReport, getServiceStatus());
                return false;
            }
        }
        addLine(buildReport, MessageFormat.format(Bundle.getMessage("trainCanNotDeliverToDestination"),
                new Object[] { getName(), car.toString(), car.getDestinationName(), car.getDestinationTrackName() }));
        return false;
    }

    private boolean isServicableTrack(PrintWriter buildReport, Car car, RouteLocation rldest, Track track) {
        if ((track.getTrainDirections() & rldest.getTrainDirection()) == 0 && !isLocalSwitcher()) {
            addLine(buildReport, MessageFormat.format(Bundle.getMessage("buildCanNotDropRsUsingTrain"),
                    new Object[] { car.toString(), rldest.getTrainDirectionString() }));
            addLine(buildReport, MessageFormat.format(Bundle.getMessage("buildCanNotDropRsUsingTrain2"),
                    new Object[] { track.getName() }));
            return false;
        }
        if (!track.isDropTrainAccepted(this)) {
            addLine(buildReport, MessageFormat.format(Bundle.getMessage("buildCanNotDropCarTrain"),
                    new Object[] { car.toString(), getName(), track.getTrackTypeName(), track.getName() }));
            return false;
        }
        return true;
    }

    protected static final String SEVEN = Setup.BUILD_REPORT_VERY_DETAILED;

    private void addLine(PrintWriter buildReport, String string) {
        if (Setup.getRouterBuildReportLevel().equals(SEVEN)) {
            TrainCommon.addLine(buildReport, SEVEN, string);
        }
    }

    protected void setServiceStatus(String status) {
        _serviceStatus = status;
    }

    /**
     * Returns the statusCode of the "isServiceable(Car)" routine. There are two
     * statusCodes that need special consideration when the train is being built,
     * the moves in a train's route and the maximum train length. NOTE: The code
     * using getServiceStatus() currently assumes that if there's a service status
     * that the issue is either route moves or maximum train length.
     *
     * @return The statusCode.
     */
    public String getServiceStatus() {
        return _serviceStatus;
    }

    /**
     * @return The number of cars worked by this train
     */
    public int getNumberCarsWorked() {
        int count = 0;
        for (Car rs : InstanceManager.getDefault(CarManager.class).getList(this)) {
            if (rs.getRouteLocation() != null) {
                count++;
            }
        }
        return count;
    }

    public void setNumberCarsRequested(int number) {
        _statusCarsRequested = number;
    }

    public int getNumberCarsRequested() {
        return _statusCarsRequested;
    }

    public void setTerminationDate(String date) {
        _statusTerminatedDate = date;
    }

    public String getTerminationDate() {
        return _statusTerminatedDate;
    }

    /**
     * Gets the number of cars in the train at the current location in the train's
     * route.
     *
     * @return The number of cars currently in the train
     */
    public int getNumberCarsInTrain() {
        return getNumberCarsInTrain(getCurrentRouteLocation());
    }

    /**
     * Gets the number of cars in the train when train departs the route location.
     *
     * @param routeLocation The RouteLocation.
     * @return The number of cars in the train departing the route location.
     */
    public int getNumberCarsInTrain(RouteLocation routeLocation) {
        int number = 0;
        Route route = getRoute();
        if (route != null) {
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                for (Car rs : InstanceManager.getDefault(CarManager.class).getList(this)) {
                    if (rs.getRouteLocation() == rl) {
                        number++;
                    }
                    if (rs.getRouteDestination() == rl) {
                        number--;
                    }
                }
                if (rl == routeLocation) {
                    break;
                }
            }
        }
        return number;
    }

    /**
     * Gets the number of empty cars in the train when train departs the route
     * location.
     *
     * @param routeLocation The RouteLocation.
     * @return The number of empty cars in the train departing the route location.
     */
    public int getNumberEmptyCarsInTrain(RouteLocation routeLocation) {
        int number = 0;
        Route route = getRoute();
        if (route != null) {
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                for (Car car : InstanceManager.getDefault(CarManager.class).getList(this)) {
                    if (!car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
                        continue;
                    }
                    if (car.getRouteLocation() == rl) {
                        number++;
                    }
                    if (car.getRouteDestination() == rl) {
                        number--;
                    }
                }
                if (rl == routeLocation) {
                    break;
                }
            }
        }

        return number;
    }

    public int getNumberLoadedCarsInTrain(RouteLocation routeLocation) {
        return getNumberCarsInTrain(routeLocation) - getNumberEmptyCarsInTrain(routeLocation);
    }

    /**
     * Gets the number of cars pulled from a location
     * 
     * @param routeLocation the location
     * @return number of pick ups
     */
    public int getNumberCarsPickedUp(RouteLocation routeLocation) {
        int number = 0;
        for (Car rs : InstanceManager.getDefault(CarManager.class).getList(this)) {
            if (rs.getRouteLocation() == routeLocation) {
                number++;
            }
        }
        return number;
    }

    /**
     * Gets the number of cars delivered to a location
     * 
     * @param routeLocation the location
     * @return number of set outs
     */
    public int getNumberCarsSetout(RouteLocation routeLocation) {
        int number = 0;
        for (Car rs : InstanceManager.getDefault(CarManager.class).getList(this)) {
            if (rs.getRouteDestination() == routeLocation) {
                number++;
            }
        }
        return number;
    }

    /**
     * Gets the train's length at the current location in the train's route.
     *
     * @return The train length at the train's current location
     */
    public int getTrainLength() {
        return getTrainLength(getCurrentRouteLocation());
    }

    /**
     * Gets the train's length at the route location specified
     *
     * @param routeLocation The route location
     * @return The train length at the route location
     */
    public int getTrainLength(RouteLocation routeLocation) {
        int length = 0;
        Route route = getRoute();
        if (route != null) {
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                for (RollingStock rs : InstanceManager.getDefault(EngineManager.class).getList(this)) {
                    if (rs.getRouteLocation() == rl) {
                        length += rs.getTotalLength();
                    }
                    if (rs.getRouteDestination() == rl) {
                        length += -rs.getTotalLength();
                    }
                }
                for (RollingStock rs : InstanceManager.getDefault(CarManager.class).getList(this)) {
                    if (rs.getRouteLocation() == rl) {
                        length += rs.getTotalLength();
                    }
                    if (rs.getRouteDestination() == rl) {
                        length += -rs.getTotalLength();
                    }
                }
                if (rl == routeLocation) {
                    break;
                }
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
        return getTrainWeight(getCurrentRouteLocation());
    }

    public int getTrainWeight(RouteLocation routeLocation) {
        int weight = 0;
        Route route = getRoute();
        if (route != null) {
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                for (RollingStock rs : InstanceManager.getDefault(EngineManager.class).getList(this)) {
                    if (rs.getRouteLocation() == rl) {
                        weight += rs.getAdjustedWeightTons();
                    }
                    if (rs.getRouteDestination() == rl) {
                        weight += -rs.getAdjustedWeightTons();
                    }
                }
                for (Car car : InstanceManager.getDefault(CarManager.class).getList(this)) {
                    if (car.getRouteLocation() == rl) {
                        weight += car.getAdjustedWeightTons(); // weight depends
                                                               // on car load
                    }
                    if (car.getRouteDestination() == rl) {
                        weight += -car.getAdjustedWeightTons();
                    }
                }
                if (rl == routeLocation) {
                    break;
                }
            }
        }
        return weight;
    }

    /**
     * Gets the train's locomotive horsepower at the route location specified
     *
     * @param routeLocation The route location
     * @return The train's locomotive horsepower at the route location
     */
    public int getTrainHorsePower(RouteLocation routeLocation) {
        int hp = 0;
        Route route = getRoute();
        if (route != null) {
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                for (Engine eng : InstanceManager.getDefault(EngineManager.class).getList(this)) {
                    if (eng.getRouteLocation() == rl) {
                        hp += eng.getHpInteger();
                    }
                    if (eng.getRouteDestination() == rl) {
                        hp += -eng.getHpInteger();
                    }
                }
                if (rl == routeLocation) {
                    break;
                }
            }
        }
        return hp;
    }

    /**
     * Gets the current caboose road and number if there's one assigned to the
     * train.
     *
     * @return Road and number of caboose.
     */
    @Nonnull
    public String getCabooseRoadAndNumber() {
        String cabooseRoadNumber = NONE;
        RouteLocation rl = getCurrentRouteLocation();
        List<Car> cars = InstanceManager.getDefault(CarManager.class).getByTrainList(this);
        for (Car car : cars) {
            if (car.getRouteLocation() == rl && car.getRouteDestination() != rl && car.isCaboose()) {
                cabooseRoadNumber = car.toString();
            }
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

    public String getRawDescription() {
        return _description;
    }

    /**
     * Returns a formated string providing the train's description. {0} = lead
     * engine number, {1} = train's departure direction {2} = lead engine road {3} =
     * DCC address of lead engine.
     *
     * @return The train's description.
     */
    public String getDescription() {
        String description = MessageFormat.format(_description, new Object[] { getLeadEngineNumber(),
                getTrainDepartsDirection(), getLeadEngineRoadName(), getLeadEngineDccAddress() });
        return description;
    }

    public void setNumberEngines(String number) {
        String old = _numberEngines;
        _numberEngines = number;
        if (!old.equals(number)) {
            setDirtyAndFirePropertyChange("trainNmberEngines", old, number); // NOI18N
        }
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
        if (!old.equals(number)) {
            setDirtyAndFirePropertyChange("trainNmberEngines", old, number); // NOI18N
        }
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
        if (!old.equals(number)) {
            setDirtyAndFirePropertyChange("trainNmberEngines", old, number); // NOI18N
        }
    }

    /**
     * Set the road name of engines servicing this train.
     *
     * @param road The road name of engines servicing this train.
     */
    public void setEngineRoad(String road) {
        String old = _engineRoad;
        _engineRoad = road;
        if (!old.equals(road)) {
            setDirtyAndFirePropertyChange("trainEngineRoad", old, road); // NOI18N
        }
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
     * @param road The road name of engines servicing this train.
     */
    public void setSecondLegEngineRoad(String road) {
        String old = _leg2Road;
        _leg2Road = road;
        if (!old.equals(road)) {
            setDirtyAndFirePropertyChange("trainEngineRoad", old, road); // NOI18N
        }
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
     * @param road The road name of engines servicing this train.
     */
    public void setThirdLegEngineRoad(String road) {
        String old = _leg3Road;
        _leg3Road = road;
        if (!old.equals(road)) {
            setDirtyAndFirePropertyChange("trainEngineRoad", old, road); // NOI18N
        }
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
     * @param model The model name of engines servicing this train.
     */
    public void setEngineModel(String model) {
        String old = _engineModel;
        _engineModel = model;
        if (!old.equals(model)) {
            setDirtyAndFirePropertyChange("trainEngineModel", old, model); // NOI18N
        }
    }

    public String getEngineModel() {
        return _engineModel;
    }

    /**
     * Set the model name of engines servicing this train's 2nd leg.
     *
     * @param model The model name of engines servicing this train.
     */
    public void setSecondLegEngineModel(String model) {
        String old = _leg2Model;
        _leg2Model = model;
        if (!old.equals(model)) {
            setDirtyAndFirePropertyChange("trainEngineModel", old, model); // NOI18N
        }
    }

    public String getSecondLegEngineModel() {
        return _leg2Model;
    }

    /**
     * Set the model name of engines servicing this train's 3rd leg.
     *
     * @param model The model name of engines servicing this train.
     */
    public void setThirdLegEngineModel(String model) {
        String old = _leg3Model;
        _leg3Model = model;
        if (!old.equals(model)) {
            setDirtyAndFirePropertyChange("trainEngineModel", old, model); // NOI18N
        }
    }

    public String getThirdLegEngineModel() {
        return _leg3Model;
    }

    protected void replaceModel(String oldModel, String newModel) {
        if (getEngineModel().equals(oldModel)) {
            setEngineModel(newModel);
        }
        if (getSecondLegEngineModel().equals(oldModel)) {
            setSecondLegEngineModel(newModel);
        }
        if (getThirdLegEngineModel().equals(oldModel)) {
            setThirdLegEngineModel(newModel);
        }
    }

    /**
     * Set the road name of the caboose servicing this train.
     *
     * @param road The road name of the caboose servicing this train.
     */
    public void setCabooseRoad(String road) {
        String old = _cabooseRoad;
        _cabooseRoad = road;
        if (!old.equals(road)) {
            setDirtyAndFirePropertyChange("trainCabooseRoad", old, road); // NOI18N
        }
    }

    public String getCabooseRoad() {
        return _cabooseRoad;
    }

    /**
     * Set the road name of the second leg caboose servicing this train.
     *
     * @param road The road name of the caboose servicing this train's 2nd leg.
     */
    public void setSecondLegCabooseRoad(String road) {
        String old = _leg2CabooseRoad;
        _leg2CabooseRoad = road;
        if (!old.equals(road)) {
            setDirtyAndFirePropertyChange("trainCabooseRoad", old, road); // NOI18N
        }
    }

    public String getSecondLegCabooseRoad() {
        return _leg2CabooseRoad;
    }

    /**
     * Set the road name of the third leg caboose servicing this train.
     *
     * @param road The road name of the caboose servicing this train's 3rd leg.
     */
    public void setThirdLegCabooseRoad(String road) {
        String old = _leg3CabooseRoad;
        _leg3CabooseRoad = road;
        if (!old.equals(road)) {
            setDirtyAndFirePropertyChange("trainCabooseRoad", old, road); // NOI18N
        }
    }

    public String getThirdLegCabooseRoad() {
        return _leg3CabooseRoad;
    }

    public void setSecondLegStartRouteLocation(RouteLocation rl) {
        _leg2Start = rl;
    }

    public RouteLocation getSecondLegStartRouteLocation() {
        return _leg2Start;
    }

    public String getSecondLegStartLocationName() {
        if (getSecondLegStartRouteLocation() == null) {
            return NONE;
        }
        return getSecondLegStartRouteLocation().getName();
    }

    public void setThirdLegStartRouteLocation(RouteLocation rl) {
        _leg3Start = rl;
    }

    public RouteLocation getThirdLegStartRouteLocation() {
        return _leg3Start;
    }

    public String getThirdLegStartLocationName() {
        if (getThirdLegStartRouteLocation() == null) {
            return NONE;
        }
        return getThirdLegStartRouteLocation().getName();
    }

    public void setSecondLegEndRouteLocation(RouteLocation rl) {
        _end2Leg = rl;
    }

    public String getSecondLegEndLocationName() {
        if (getSecondLegEndRouteLocation() == null) {
            return NONE;
        }
        return getSecondLegEndRouteLocation().getName();
    }

    public RouteLocation getSecondLegEndRouteLocation() {
        return _end2Leg;
    }

    public void setThirdLegEndRouteLocation(RouteLocation rl) {
        _leg3End = rl;
    }

    public RouteLocation getThirdLegEndRouteLocation() {
        return _leg3End;
    }

    public String getThirdLegEndLocationName() {
        if (getThirdLegEndRouteLocation() == null) {
            return NONE;
        }
        return getThirdLegEndRouteLocation().getName();
    }

    /**
     * Optional changes to train while en route.
     *
     * @param options NO_CABOOSE_OR_FRED, CHANGE_ENGINES, ADD_CABOOSE,
     *                HELPER_ENGINES, REMOVE_CABOOSE
     */
    public void setSecondLegOptions(int options) {
        int old = _leg2Options;
        _leg2Options = options;
        if (old != options) {
            setDirtyAndFirePropertyChange("trainLegOptions", old, options); // NOI18N
        }
    }

    public int getSecondLegOptions() {
        return _leg2Options;
    }

    /**
     * Optional changes to train while en route.
     *
     * @param options NO_CABOOSE_OR_FRED, CHANGE_ENGINES, ADD_CABOOSE,
     *                HELPER_ENGINES, REMOVE_CABOOSE
     */
    public void setThirdLegOptions(int options) {
        int old = _leg3Options;
        _leg3Options = options;
        if (old != options) {
            setDirtyAndFirePropertyChange("trainLegOptions", old, options); // NOI18N
        }
    }

    public int getThirdLegOptions() {
        return _leg3Options;
    }

    public void setComment(String comment) {
        String old = _comment;
        _comment = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange("trainComment", old, comment); // NOI18N
        }
    }

    public String getComment() {
        return _comment;
    }

    /**
     * Add a script to run before a train is built
     *
     * @param pathname The script's pathname
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
     * @param pathname The script's pathname
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
     * @param pathname The script's pathname
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
     * @param pathname The script's pathname
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
     * @param name The railroad name for this train.
     */
    public void setRailroadName(String name) {
        String old = _railroadName;
        _railroadName = name;
        if (!old.equals(name)) {
            setDirtyAndFirePropertyChange("trainRailroadName", old, name); // NOI18N
        }
    }

    public String getManifestLogoPathName() {
        return _logoPathName;
    }

    /**
     * Overrides the default logo for this train.
     *
     * @param pathName file location for the logo.
     */
    public void setManifestLogoPathName(String pathName) {
        _logoPathName = pathName;
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

    /**
     * Allow local moves if car has a custom load or Final Destination
     *
     * @return true if local move is allowed
     */
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
     * When true allow a turn to return cars to staging. A turn is a train that
     * departs and terminates at the same location.
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

    public boolean isServiceAllCarsWithFinalDestinationsEnabled() {
        return _serviceAllCarsWithFinalDestinations;
    }

    public void setServiceAllCarsWithFinalDestinationsEnabled(boolean enable) {
        boolean old = _serviceAllCarsWithFinalDestinations;
        _serviceAllCarsWithFinalDestinations = enable;
        if (old != enable) {
            setDirtyAndFirePropertyChange("TrainServiceAllCarsWithFinalDestinations", old ? "true" : "false", // NOI18N
                    enable ? "true" : "false"); // NOI18N
        }
    }

    public boolean isBuildConsistEnabled() {
        return _buildConsist;
    }

    public void setBuildConsistEnabled(boolean enable) {
        boolean old = _buildConsist;
        _buildConsist = enable;
        if (old != enable) {
            setDirtyAndFirePropertyChange("TrainBuildConsist", old ? "true" : "false", // NOI18N
                    enable ? "true" : "false"); // NOI18N
        }
    }

    public boolean isSendCarsWithCustomLoadsToStagingEnabled() {
        return _sendCarsWithCustomLoadsToStaging;
    }

    public void setSendCarsWithCustomLoadsToStagingEnabled(boolean enable) {
        boolean old = _sendCarsWithCustomLoadsToStaging;
        _sendCarsWithCustomLoadsToStaging = enable;
        if (old != enable) {
            setDirtyAndFirePropertyChange("SendCarsWithCustomLoadsToStaging", old ? "true" : "false", // NOI18N
                    enable ? "true" : "false"); // NOI18N
        }
    }

    protected void setBuilt(boolean built) {
        boolean old = _built;
        _built = built;
        if (old != built) {
            setDirtyAndFirePropertyChange(BUILT_CHANGED_PROPERTY, old, built); // NOI18N
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

    /**
     * Set true whenever the train's manifest has been modified. For example adding
     * or removing a car from a train, or changing the manifest format. Once the
     * manifest has been regenerated (modified == false), the old status for the
     * train is restored.
     *
     * @param modified True if train's manifest has been modified.
     */
    public void setModified(boolean modified) {
        log.debug("Set modified {}", modified);
        if (!isBuilt()) {
            _modified = false;
            return; // there isn't a manifest to modify
        }
        boolean old = _modified;
        _modified = modified;
        if (modified) {
            setPrinted(false);
        }
        if (old != modified) {
            if (modified) {
                // scripts can call setModified() for a train
                if (getStatusCode() != CODE_RUN_SCRIPTS) {
                    setOldStatusCode(getStatusCode());
                }
                setStatusCode(CODE_MANIFEST_MODIFIED);
            } else {
                setStatusCode(getOldStatusCode()); // restore previous train
                                                   // status
            }
        }
        setDirtyAndFirePropertyChange(TRAIN_MODIFIED_CHANGED_PROPERTY, null, modified); // NOI18N
    }

    public boolean isModified() {
        return _modified;
    }

    /**
     * Control flag used to decide if this train is to be built.
     *
     * @param build When true, build this train.
     */
    public void setBuildEnabled(boolean build) {
        boolean old = _build;
        _build = build;
        if (old != build) {
            setDirtyAndFirePropertyChange(BUILD_CHANGED_PROPERTY, old, build); // NOI18N
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
     * Build this train if the build control flag is true.
     *
     * @return True only if train is successfully built.
     */
    public boolean buildIfSelected() {
        if (isBuildEnabled() && !isBuilt()) {
            return build();
        }
        log.debug("Train ({}) not selected or already built, skipping build", getName());
        return false;
    }

    /**
     * Build this train. Creates a train manifest.
     *
     * @return True if build successful.
     */
    public synchronized boolean build() {
        reset();
        // check to see if any other trains are building
        while (InstanceManager.getDefault(TrainManager.class).isAnyTrainBuilding()) {
            try {
                wait(100); // 100 msec
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                log.error("Thread unexpectedly interrupted", e);
            }
        }
        // run before build scripts
        runScripts(getBuildScripts());
        TrainBuilder tb = new TrainBuilder();
        boolean results = tb.build(this);
        setPrinted(false);
        setSwitchListStatus(UNKNOWN);
        // run after build scripts
        runScripts(getAfterBuildScripts());
        return results;
    }

    /**
     * Run train scripts, waits for completion before returning.
     */
    private synchronized void runScripts(List<String> scripts) {
        if (scripts.size() > 0) {
            // save the current status
            setOldStatusCode(getStatusCode());
            setStatusCode(CODE_RUN_SCRIPTS);
            JmriScriptEngineManager.getDefault().initializeAllEngines(); // create
                                                                         // the
                                                                         // python
                                                                         // interpreter
                                                                         // thread
            // find the number of active threads
            ThreadGroup root = Thread.currentThread().getThreadGroup();
            int numberOfThreads = root.activeCount();
            // log.debug("Number of active threads: {}", numberOfThreads);
            for (String scriptPathname : scripts) {
                try {
                    JmriScriptEngineManager.getDefault()
                            .runScript(new File(jmri.util.FileUtil.getExternalFilename(scriptPathname)));
                } catch (Exception e) {
                    log.error("Problem with script: {}", scriptPathname);
                }
            }
            // need to wait for scripts to complete or 4 seconds maximum
            int count = 0;
            while (root.activeCount() > numberOfThreads) {
                log.debug("Number of active threads: {}, at start: {}", root.activeCount(), numberOfThreads);
                try {
                    wait(40);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // retain if needed
                                                        // later
                }
                if (count++ > 100) {
                    break; // 4 seconds maximum 40*100 = 4000
                }
            }
            setStatusCode(getOldStatusCode());
        }
    }

    public void printBuildReport() {
        boolean isPreview = (InstanceManager.getDefault(TrainManager.class).isPrintPreviewEnabled() ||
                Setup.isBuildReportAlwaysPreviewEnabled());
        printBuildReport(isPreview);
    }

    public boolean printBuildReport(boolean isPreview) {
        File buildFile = InstanceManager.getDefault(TrainManagerXml.class).getTrainBuildReportFile(getName());
        if (!buildFile.exists()) {
            log.warn("Build file missing for train {}", getName());
            return false;
        }

        if (isPreview && Setup.isBuildReportEditorEnabled()) {
            TrainPrintUtilities.editReport(buildFile, getName());
        } else {
            TrainPrintUtilities.printReport(buildFile,
                    MessageFormat.format(Bundle.getMessage("buildReport"), new Object[] { getDescription() }),
                    isPreview, NONE, true, NONE, NONE, Setup.PORTRAIT, Setup.getBuildReportFontSize());
        }
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
     * Returns true if the train build failed. Note that returning false doesn't
     * mean the build was successful.
     *
     * @return true if train build failed.
     */
    public boolean isBuildFailed() {
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
        if (isBuilt()) {
            boolean isPreview = InstanceManager.getDefault(TrainManager.class).isPrintPreviewEnabled();
            return (printManifest(isPreview));
        } else {
            log.debug("Need to build train ({}) before printing manifest", getName());
            return false;
        }
    }

    /**
     * Print manifest for train.
     *
     * @param isPreview True if preview.
     * @return true if print successful, false if train print file not found.
     */
    public boolean printManifest(boolean isPreview) {
        if (isModified()) {
            new TrainManifest(this);
            try {
                new JsonManifest(this).build();
            } catch (IOException ex) {
                log.error("Unable to create JSON manifest {}", ex.getLocalizedMessage());
            }
            new TrainCsvManifest(this);
        }
        File file = InstanceManager.getDefault(TrainManagerXml.class).getTrainManifestFile(getName());
        if (!file.exists()) {
            log.warn("Manifest file missing for train ({})", getName());
            return false;
        }
        if (isPreview && Setup.isManifestEditorEnabled()) {
            TrainUtilities.openDesktop(file);
            return true;
        }
        String logoURL = Setup.NONE;
        if (!getManifestLogoPathName().equals(NONE)) {
            logoURL = FileUtil.getExternalFilename(getManifestLogoPathName());
        } else if (!Setup.getManifestLogoURL().equals(Setup.NONE)) {
            logoURL = FileUtil.getExternalFilename(Setup.getManifestLogoURL());
        }
        Location departs = InstanceManager.getDefault(LocationManager.class).getLocationByName(getTrainDepartsName());
        String printerName = Location.NONE;
        if (departs != null) {
            printerName = departs.getDefaultPrinterName();
        }
        // the train description shouldn't exceed half of the page width or the
        // page number will be overwritten
        String name = getDescription();
        if (name.length() > TrainCommon.getManifestHeaderLineLength() / 2) {
            name = name.substring(0, TrainCommon.getManifestHeaderLineLength() / 2);
        }
        TrainPrintUtilities.printReport(file, name, isPreview, Setup.getFontName(), false, logoURL, printerName,
                Setup.getManifestOrientation(), Setup.getManifestFontSize());
        if (!isPreview) {
            setPrinted(true);
        }
        return true;
    }

    public boolean openFile() {
        File file = createCsvManifestFile();
        if (file == null || !file.exists()) {
            log.warn("CSV manifest file missing for train {}", getName());
            return false;
        }
        TrainUtilities.openDesktop(file);
        return true;
    }

    public boolean runFile() {
        File file = createCsvManifestFile();
        if (file == null || !file.exists()) {
            log.warn("CSV manifest file missing for train {}", getName());
            return false;
        }
        // Set up to process the CSV file by the external Manifest program
        InstanceManager.getDefault(TrainCustomManifest.class).addCsvFile(file);
        if (!InstanceManager.getDefault(TrainCustomManifest.class).process()) {
            if (!InstanceManager.getDefault(TrainCustomManifest.class).excelFileExists()) {
                JOptionPane.showMessageDialog(null,
                        MessageFormat.format(Bundle.getMessage("LoadDirectoryNameFileName"),
                                new Object[] { InstanceManager.getDefault(TrainCustomManifest.class).getDirectoryName(),
                                        InstanceManager.getDefault(TrainCustomManifest.class).getFileName() }),
                        Bundle.getMessage("ManifestCreatorNotFound"), JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
        return true;
    }

    public File createCsvManifestFile() {
        if (isModified()) {
            new TrainManifest(this);
            try {
                new JsonManifest(this).build();
            } catch (IOException ex) {
                log.error("Unable to create JSON manifest {}", ex.getLocalizedMessage());
            }
            new TrainCsvManifest(this);
        }
        File file = InstanceManager.getDefault(TrainManagerXml.class).getTrainCsvManifestFile(getName());
        if (!file.exists()) {
            log.warn("CSV manifest file was not created for train ({})", getName());
            return null;
        }
        return file;
    }

    public void setPrinted(boolean printed) {
        boolean old = _printed;
        _printed = printed;
        if (old != printed) {
            setDirtyAndFirePropertyChange("trainPrinted", old ? "true" : "false", printed ? "true" : "false"); // NOI18N
        }
    }

    /**
     * Used to determine if train manifest was printed.
     *
     * @return true if the train manifest was printed.
     */
    public boolean isPrinted() {
        return _printed;
    }

    /**
     * Sets the panel position for the train icon for the current route location.
     *
     * @return true if train coordinates can be set
     */
    public boolean setTrainIconCoordinates() {
        if (Setup.isTrainIconCordEnabled() && getCurrentRouteLocation() != null && _trainIcon != null) {
            getCurrentRouteLocation().setTrainIconX(_trainIcon.getX());
            getCurrentRouteLocation().setTrainIconY(_trainIcon.getY());
            return true;
        }
        return false;
    }

    /**
     * Terminate train.
     */
    public void terminate() {
        while (isBuilt()) {
            move();
        }
    }

    /**
     * Move train to next location in the route. Will move engines, cars, and train
     * icon. Will also terminate a train after it arrives at its final destination.
     */
    public void move() {
        log.debug("Move train ({})", getName());
        if (getRoute() == null || getCurrentRouteLocation() == null) {
            setBuilt(false); // break terminate loop
            return;
        }
        if (!isBuilt()) {
            log.error("ERROR attempt to move train ({}) that hasn't been built", getName());
            return;
        }
        RouteLocation rl = getCurrentRouteLocation();
        RouteLocation rlNext = getNextRouteLocation(rl);

        setCurrentLocation(rlNext);

        // cars and engines will move via property change
        setDirtyAndFirePropertyChange(TRAIN_LOCATION_CHANGED_PROPERTY, rl, rlNext);
        moveTrainIcon(rlNext);
        updateStatus(rl, rlNext);
        // tell GUI that train has complete its move
        setDirtyAndFirePropertyChange(TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY, rl, rlNext);
    }

    /**
     * Move train to a location in the train's route. Code checks to see if the
     * location requested is part of the train's route and if the train hasn't
     * already visited the location. This command can only move the train forward in
     * its route. Note that you can not terminate the train using this command. See
     * move() or terminate().
     *
     * @param locationName The name of the location to move this train.
     * @return true if train was able to move to the named location.
     */
    public boolean move(String locationName) {
        log.info("Move train ({}) to location ({})", getName(), locationName);
        if (getRoute() == null || getCurrentRouteLocation() == null) {
            return false;
        }
        List<RouteLocation> routeList = getRoute().getLocationsBySequenceList();
        for (int i = 0; i < routeList.size(); i++) {
            RouteLocation rl = routeList.get(i);
            if (getCurrentRouteLocation() == rl) {
                for (int j = i + 1; j < routeList.size(); j++) {
                    rl = routeList.get(j);
                    if (rl.getName().equals(locationName)) {
                        log.debug("Found location ({}) moving train to this location", locationName);
                        for (j = i + 1; j < routeList.size(); j++) {
                            rl = routeList.get(j);
                            move();
                            if (rl.getName().equals(locationName)) {
                                return true;
                            }
                        }
                    }
                }
                break; // done
            }
        }
        return false;
    }

    /**
     * Moves the train to the specified route location
     *
     * @param rl route location
     * @return true if successful
     */
    public boolean move(RouteLocation rl) {
        if (rl == null) {
            return false;
        }
        log.debug("Move train ({}) to location ({})", getName(), rl.getName());
        if (getRoute() == null || getCurrentRouteLocation() == null) {
            return false;
        }
        boolean foundCurrent = false;
        for (RouteLocation xrl : getRoute().getLocationsBySequenceList()) {
            if (getCurrentRouteLocation() == xrl) {
                foundCurrent = true;
            }
            if (xrl == rl) {
                if (foundCurrent) {
                    return true; // done
                } else {
                    break; // train passed this location
                }
            }
            if (foundCurrent) {
                move();
            }
        }
        return false;
    }

    /**
     * Move train to the next location in the train's route. The location name
     * provided must be equal to the next location name in the train's route.
     *
     * @param locationName The next location name in the train's route.
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
        if (getCurrentRouteLocation() != null) {
            moveTrainIcon(getCurrentRouteLocation());
        }
    }

    private final boolean animation = true; // when true use animation for icon
                                            // moves
    TrainIconAnimation _ta;

    /*
     * The train icon is moved to route location (rl) for this train
     */
    @SuppressWarnings("null")
    protected void moveTrainIcon(RouteLocation rl) {
        // create train icon if at departure or if program has been restarted
        if (rl == getTrainDepartsRouteLocation() || _trainIcon == null) {
            createTrainIcon(rl);
        }
        // is the lead engine still in train
        if (getLeadEngine() != null && getLeadEngine().getRouteDestination() == rl && rl != null) {
            log.debug("Engine ({}) arriving at destination {}", getLeadEngine().toString(), rl.getName());
        }
        if (_trainIcon != null && _trainIcon.isActive()) {
            setTrainIconColor();
            _trainIcon.setShowToolTip(true);
            String txt = null;
            if (getCurrentLocationName().equals(NONE)) {
                txt = getDescription() + " " + Bundle.getMessage("Terminated") + " (" + getTrainTerminatesName() + ")";
            } else {
                txt = MessageFormat.format(Bundle.getMessage("TrainAtNext"),
                        new Object[] { getDescription(), getCurrentLocationName(), getNextLocationName() });
            }
            _trainIcon.getToolTip().setText(txt);
            _trainIcon.getToolTip().setBackgroundColor(Color.white);
            // rl can be null when train is terminated.
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
        if (isBuilt() && getLeadEngine() != null && Setup.isTrainIconAppendEnabled()) {
            name += " " + getLeadEngine().getNumber();
        }
        return name;
    }

    public String getLeadEngineNumber() {
        if (getLeadEngine() == null) {
            return NONE;
        }
        return getLeadEngine().getNumber();
    }

    public String getLeadEngineRoadName() {
        if (getLeadEngine() == null) {
            return NONE;
        }
        return getLeadEngine().getRoadName();
    }

    public String getLeadEngineRoadAndNumber() {
        if (getLeadEngine() == null) {
            return NONE;
        }
        return getLeadEngine().toString();
    }

    public String getLeadEngineDccAddress() {
        if (getLeadEngine() == null) {
            return NONE;
        }
        return getLeadEngine().getDccAddress();
    }

    /**
     * Gets the lead engine, will create it if the program has been restarted
     *
     * @return lead engine for this train
     */
    public Engine getLeadEngine() {
        if (_leadEngine == null && !_leadEngineId.equals(NONE)) {
            _leadEngine = InstanceManager.getDefault(EngineManager.class).getById(_leadEngineId);
        }
        return _leadEngine;
    }

    public void setLeadEngine(Engine engine) {
        if (engine == null) {
            _leadEngineId = NONE;
        }
        _leadEngine = engine;
    }

    /**
     * Returns the lead engine in a train's route. There can be up to two changes in
     * the lead engine for a train.
     * 
     * @param routeLocation where in the train's route to find the lead engine.
     * @return lead engine
     */
    public Engine getLeadEngine(RouteLocation routeLocation) {
        Engine lead = null;
        for (RouteLocation rl : getRoute().getLocationsBySequenceList()) {
            for (Engine engine : InstanceManager.getDefault(EngineManager.class).getByTrainList(this)) {
                if (engine.getRouteLocation() == rl && (engine.getConsist() == null || engine.isLead())) {
                    lead = engine;
                    break;
                }
            }
            if (rl == routeLocation) {
                break;
            }
        }
        return lead;
    }

    protected TrainIcon _trainIcon = null;

    public TrainIcon getTrainIcon() {
        return _trainIcon;
    }

    public void createTrainIcon(RouteLocation rl) {
        if (_trainIcon != null && _trainIcon.isActive()) {
            _trainIcon.remove();
        }
        // if there's a panel specified, get it and place icon
        if (!Setup.getPanelName().isEmpty()) {
            Editor editor = InstanceManager.getDefault(EditorManager.class).getTargetFrame(Setup.getPanelName());
            if (editor != null) {
                try {
                    _trainIcon = editor.addTrainIcon(getIconName());
                } catch (Exception e) {
                    log.error("Error placing train ({}) icon on panel ({}) {}", getName(), Setup.getPanelName(), e);
                    return;
                }
                _trainIcon.setTrain(this);
                if (getIconName().length() > 9) {
                    _trainIcon.setFont(_trainIcon.getFont().deriveFont(8.f));
                }
                if (rl != null) {
                    _trainIcon.setLocation(rl.getTrainIconX(), rl.getTrainIconY());
                }
                // add throttle if there's a throttle manager
                if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) != null) {
                    // add throttle if JMRI loco roster entry exist
                    RosterEntry entry = null;
                    if (getLeadEngine() != null) {
                        // first try and find a match based on loco road number
                        entry = getLeadEngine().getRosterEntry();
                    }
                    if (entry != null) {
                        _trainIcon.setRosterEntry(entry);
                        if (getLeadEngine().getConsist() != null) {
                            _trainIcon.setConsistNumber(getLeadEngine().getConsist().getConsistNumber());
                        }
                    } else {
                        log.debug("Loco roster entry not found for train ({})", getName());
                    }
                }
            }
        }
    }

    private void setTrainIconColor() {
        // Terminated train?
        if (getCurrentLocationName().equals(NONE)) {
            _trainIcon.setLocoColor(Setup.getTrainIconColorTerminate());
            return;
        }
        // local train serving only one location?
        if (isLocalSwitcher()) {
            _trainIcon.setLocoColor(Setup.getTrainIconColorLocal());
            return;
        }
        // set color based on train direction at current location
        if (getCurrentRouteLocation().getTrainDirection() == RouteLocation.NORTH) {
            _trainIcon.setLocoColor(Setup.getTrainIconColorNorth());
        }
        if (getCurrentRouteLocation().getTrainDirection() == RouteLocation.SOUTH) {
            _trainIcon.setLocoColor(Setup.getTrainIconColorSouth());
        }
        if (getCurrentRouteLocation().getTrainDirection() == RouteLocation.EAST) {
            _trainIcon.setLocoColor(Setup.getTrainIconColorEast());
        }
        if (getCurrentRouteLocation().getTrainDirection() == RouteLocation.WEST) {
            _trainIcon.setLocoColor(Setup.getTrainIconColorWest());
        }
    }

    private void updateStatus(RouteLocation old, RouteLocation next) {
        if (next != null) {
            setStatusCode(CODE_TRAIN_EN_ROUTE);
            // run move scripts
            runScripts(getMoveScripts());
        } else {
            log.debug("Train ({}) terminated", getName());
            setTerminationDate(TrainCommon.getDate(false));
            setStatusCode(CODE_TERMINATED);
            setBuilt(false);
            // run termination scripts
            runScripts(getTerminationScripts());
        }
    }

    /**
     * Sets the print status for switch lists
     *
     * @param status UNKNOWN PRINTED
     */
    public void setSwitchListStatus(String status) {
        String old = _switchListStatus;
        _switchListStatus = status;
        if (!old.equals(status)) {
            setDirtyAndFirePropertyChange("switch list train status", old, status); // NOI18N
        }
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
        if (isTrainEnRoute()) {
            log.info("Train ({}) has started its route, can not be reset", getName());
            return false;
        }
        setCurrentLocation(null);
        setDepartureTrack(null);
        setTerminationTrack(null);
        setBuilt(false);
        setBuildFailed(false);
        setBuildFailedMessage(NONE);
        setPrinted(false);
        setModified(false);
        // remove cars and engines from this train via property change
        setStatusCode(CODE_TRAIN_RESET);
        // remove train icon
        if (_trainIcon != null && _trainIcon.isActive()) {
            _trainIcon.remove();
        }
        return true;
    }

    public void dispose() {
        if (getRoute() != null) {
            getRoute().removePropertyChangeListener(this);
        }
        InstanceManager.getDefault(CarRoads.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarOwners.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineModels.class).removePropertyChangeListener(this);

        setDirtyAndFirePropertyChange(DISPOSE_CHANGED_PROPERTY, null, "Dispose"); // NOI18N
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized with
     * the detailed DTD in operations-trains.dtd
     *
     * @param e Consist XML element
     */
    @SuppressWarnings("deprecation") // until there's a replacement for
                                     // convertFromXmlComment()
    public Train(Element e) {
        org.jdom2.Attribute a;
        if ((a = e.getAttribute(Xml.ID)) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in train element when reading operations");
        }
        if ((a = e.getAttribute(Xml.NAME)) != null) {
            _name = a.getValue();
        }
        if ((a = e.getAttribute(Xml.DESCRIPTION)) != null) {
            _description = a.getValue();
        }
        if ((a = e.getAttribute(Xml.DEPART_HOUR)) != null) {
            String hour = a.getValue();
            if ((a = e.getAttribute(Xml.DEPART_MINUTE)) != null) {
                String minute = a.getValue();
                _departureTime = hour + ":" + minute;
            }
        }

        // Trains table row color
        Element eRowColor = e.getChild(Xml.ROW_COLOR);
        if (eRowColor != null && (a = eRowColor.getAttribute(Xml.NAME)) != null) {
            _tableRowColorName = a.getValue();
        }
        if (eRowColor != null && (a = eRowColor.getAttribute(Xml.RESET_ROW_COLOR)) != null) {
            _tableRowColorResetName = a.getValue();
        }

        Element eRoute = e.getChild(Xml.ROUTE);
        if (eRoute != null) {
            if ((a = eRoute.getAttribute(Xml.ID)) != null) {
                setRoute(InstanceManager.getDefault(RouteManager.class).getRouteById(a.getValue()));
            }
            if (eRoute.getChild(Xml.SKIPS) != null) {
                List<Element> skips = eRoute.getChild(Xml.SKIPS).getChildren(Xml.LOCATION);
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
                setRoute(InstanceManager.getDefault(RouteManager.class).getRouteById(a.getValue()));
            } else if ((a = e.getAttribute(Xml.ROUTE)) != null) {
                setRoute(InstanceManager.getDefault(RouteManager.class).getRouteByName(a.getValue()));
            }
            if ((a = e.getAttribute(Xml.SKIP)) != null) {
                String locationIds = a.getValue();
                String[] locs = locationIds.split("%%"); // NOI18N
                // log.debug("Train skips: {}", locationIds);
                setTrainSkipsLocations(locs);
            }
        }
        // new way of reading car types using elements
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
        } // old way of reading car types up to version 2.99.6
        else if ((a = e.getAttribute(Xml.CAR_TYPES)) != null) {
            String names = a.getValue();
            String[] types = names.split("%%"); // NOI18N
            // log.debug("Car types: {}", names);
            setTypeNames(types);
        }
        // old misspelled format
        if ((a = e.getAttribute(Xml.CAR_ROAD_OPERATION)) != null) {
            _roadOption = a.getValue();
        }
        if ((a = e.getAttribute(Xml.CAR_ROAD_OPTION)) != null) {
            _roadOption = a.getValue();
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
        } // old way of reading car roads up to version 2.99.6
        else if ((a = e.getAttribute(Xml.CAR_ROADS)) != null) {
            String names = a.getValue();
            String[] roads = names.split("%%"); // NOI18N
            log.debug("Train ({}) {} car roads: {}", getName(), getRoadOption(), names);
            setRoadNames(roads);
        }

        if ((a = e.getAttribute(Xml.CAR_LOAD_OPTION)) != null) {
            _loadOption = a.getValue();
        }
        if ((a = e.getAttribute(Xml.CAR_OWNER_OPTION)) != null) {
            _ownerOption = a.getValue();
        }
        if ((a = e.getAttribute(Xml.BUILT_START_YEAR)) != null) {
            _builtStartYear = a.getValue();
        }
        if ((a = e.getAttribute(Xml.BUILT_END_YEAR)) != null) {
            _builtEndYear = a.getValue();
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
        } // old way of reading car loads up to version 2.99.6
        else if ((a = e.getAttribute(Xml.CAR_LOADS)) != null) {
            String names = a.getValue();
            String[] loads = names.split("%%"); // NOI18N
            log.debug("Train ({}) {} car loads: {}", getName(), getLoadOption(), names);
            setLoadNames(loads);
        }
        // new way of reading car owners using elements
        if (e.getChild(Xml.CAR_OWNERS) != null) {
            List<Element> carOwners = e.getChild(Xml.CAR_OWNERS).getChildren(Xml.CAR_OWNER);
            String[] owners = new String[carOwners.size()];
            for (int i = 0; i < carOwners.size(); i++) {
                Element owner = carOwners.get(i);
                if ((a = owner.getAttribute(Xml.NAME)) != null) {
                    owners[i] = a.getValue();
                }
            }
            setOwnerNames(owners);
        } // old way of reading car owners up to version 2.99.6
        else if ((a = e.getAttribute(Xml.CAR_OWNERS)) != null) {
            String names = a.getValue();
            String[] owners = names.split("%%"); // NOI18N
            log.debug("Train ({}) {} car owners: {}", getName(), getOwnerOption(), names);
            setOwnerNames(owners);
        }

        if ((a = e.getAttribute(Xml.NUMBER_ENGINES)) != null) {
            _numberEngines = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LEG2_ENGINES)) != null) {
            _leg2Engines = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LEG3_ENGINES)) != null) {
            _leg3Engines = a.getValue();
        }
        if ((a = e.getAttribute(Xml.ENGINE_ROAD)) != null) {
            _engineRoad = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LEG2_ROAD)) != null) {
            _leg2Road = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LEG3_ROAD)) != null) {
            _leg3Road = a.getValue();
        }
        if ((a = e.getAttribute(Xml.ENGINE_MODEL)) != null) {
            _engineModel = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LEG2_MODEL)) != null) {
            _leg2Model = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LEG3_MODEL)) != null) {
            _leg3Model = a.getValue();
        }
        if ((a = e.getAttribute(Xml.REQUIRES)) != null) {
            try {
                _requires = Integer.parseInt(a.getValue());
            } catch (NumberFormatException ee) {
                log.error("Requires ({}) isn't a valid number for train ({})", a.getValue(), getName());
            }
        }
        if ((a = e.getAttribute(Xml.CABOOSE_ROAD)) != null) {
            _cabooseRoad = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LEG2_CABOOSE_ROAD)) != null) {
            _leg2CabooseRoad = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LEG3_CABOOSE_ROAD)) != null) {
            _leg3CabooseRoad = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LEG2_OPTIONS)) != null) {
            try {
                _leg2Options = Integer.parseInt(a.getValue());
            } catch (NumberFormatException ee) {
                log.error("Leg 2 options ({}) isn't a valid number for train ({})", a.getValue(), getName());
            }
        }
        if ((a = e.getAttribute(Xml.LEG3_OPTIONS)) != null) {
            try {
                _leg3Options = Integer.parseInt(a.getValue());
            } catch (NumberFormatException ee) {
                log.error("Leg 3 options ({}) isn't a valid number for train ({})", a.getValue(), getName());
            }
        }
        if ((a = e.getAttribute(Xml.BUILD_NORMAL)) != null) {
            _buildNormal = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.TO_TERMINAL)) != null) {
            _sendToTerminal = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.ALLOW_LOCAL_MOVES)) != null) {
            _allowLocalMoves = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.ALLOW_THROUGH_CARS)) != null) {
            _allowThroughCars = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.ALLOW_RETURN)) != null) {
            _allowCarsReturnStaging = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.SERVICE_ALL)) != null) {
            _serviceAllCarsWithFinalDestinations = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.BUILD_CONSIST)) != null) {
            _buildConsist = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.SEND_CUSTOM_STAGING)) != null) {
            _sendCarsWithCustomLoadsToStaging = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.BUILT)) != null) {
            _built = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.BUILD)) != null) {
            _build = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.BUILD_FAILED)) != null) {
            _buildFailed = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.BUILD_FAILED_MESSAGE)) != null) {
            _buildFailedMessage = a.getValue();
        }
        if ((a = e.getAttribute(Xml.PRINTED)) != null) {
            _printed = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.MODIFIED)) != null) {
            _modified = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.SWITCH_LIST_STATUS)) != null) {
            _switchListStatus = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LEAD_ENGINE)) != null) {
            _leadEngineId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.TERMINATION_DATE)) != null) {
            _statusTerminatedDate = a.getValue();
        }
        if ((a = e.getAttribute(Xml.REQUESTED_CARS)) != null) {
            try {
                _statusCarsRequested = Integer.parseInt(a.getValue());
            } catch (NumberFormatException ee) {
                log.error("Status cars requested ({}) isn't a valid number for train ({})", a.getValue(), getName());
            }
        }
        if ((a = e.getAttribute(Xml.STATUS)) != null && e.getAttribute(Xml.STATUS_CODE) == null) {
            String status = a.getValue();
            if (status.startsWith(BUILD_FAILED)) {
                _statusCode = CODE_BUILD_FAILED;
            } else if (status.startsWith(BUILT)) {
                _statusCode = CODE_BUILT;
            } else if (status.startsWith(PARTIAL_BUILT)) {
                _statusCode = CODE_PARTIAL_BUILT;
            } else if (status.startsWith(TERMINATED)) {
                String[] splitStatus = status.split(" ");
                if (splitStatus.length > 1) {
                    _statusTerminatedDate = splitStatus[1];
                }
                _statusCode = CODE_TERMINATED;
            } else if (status.startsWith(TRAIN_EN_ROUTE)) {
                _statusCode = CODE_TRAIN_EN_ROUTE;
            } else if (status.startsWith(TRAIN_RESET)) {
                _statusCode = CODE_TRAIN_RESET;
            } else {
                _statusCode = CODE_UNKNOWN;
            }
        }
        if ((a = e.getAttribute(Xml.STATUS_CODE)) != null) {
            try {
                _statusCode = Integer.parseInt(a.getValue());
            } catch (NumberFormatException ee) {
                log.error("Status code ({}) isn't a valid number for train ({})", a.getValue(), getName());
            }
        }
        if ((a = e.getAttribute(Xml.OLD_STATUS_CODE)) != null) {
            try {
                _oldStatusCode = Integer.parseInt(a.getValue());
            } catch (NumberFormatException ee) {
                log.error("Old status code ({}) isn't a valid number for train ({})", a.getValue(), getName());
            }
        } else {
            _oldStatusCode = getStatusCode(); // use current status code if one
                                              // wasn't saved
        }
        if ((a = e.getAttribute(Xml.COMMENT)) != null) {
            _comment = OperationsXml.convertFromXmlComment(a.getValue());
        }
        if (getRoute() != null) {
            if ((a = e.getAttribute(Xml.CURRENT)) != null) {
                _current = getRoute().getLocationById(a.getValue());
            }
            if ((a = e.getAttribute(Xml.LEG2_START)) != null) {
                _leg2Start = getRoute().getLocationById(a.getValue());
            }
            if ((a = e.getAttribute(Xml.LEG3_START)) != null) {
                _leg3Start = getRoute().getLocationById(a.getValue());
            }
            if ((a = e.getAttribute(Xml.LEG2_END)) != null) {
                _end2Leg = getRoute().getLocationById(a.getValue());
            }
            if ((a = e.getAttribute(Xml.LEG3_END)) != null) {
                _leg3End = getRoute().getLocationById(a.getValue());
            }
            if ((a = e.getAttribute(Xml.DEPARTURE_TRACK)) != null) {
                Location location = InstanceManager.getDefault(LocationManager.class)
                        .getLocationByName(getTrainDepartsName());
                if (location != null) {
                    _departureTrack = location.getTrackById(a.getValue());
                } else {
                    log.error("Departure location not found for track {}", a.getValue());
                }
            }
            if ((a = e.getAttribute(Xml.TERMINATION_TRACK)) != null) {
                Location location = InstanceManager.getDefault(LocationManager.class)
                        .getLocationByName(getTrainTerminatesName());
                if (location != null) {
                    _terminationTrack = location.getTrackById(a.getValue());
                } else {
                    log.error("Termiation location not found for track {}", a.getValue());
                }
            }
        }

        // check for scripts
        if (e.getChild(Xml.SCRIPTS) != null) {
            List<Element> lb = e.getChild(Xml.SCRIPTS).getChildren(Xml.BUILD);
            for (Element es : lb) {
                if ((a = es.getAttribute(Xml.NAME)) != null) {
                    addBuildScript(a.getValue());
                }
            }
            List<Element> lab = e.getChild(Xml.SCRIPTS).getChildren(Xml.AFTER_BUILD);
            for (Element es : lab) {
                if ((a = es.getAttribute(Xml.NAME)) != null) {
                    addAfterBuildScript(a.getValue());
                }
            }
            List<Element> lm = e.getChild(Xml.SCRIPTS).getChildren(Xml.MOVE);
            for (Element es : lm) {
                if ((a = es.getAttribute(Xml.NAME)) != null) {
                    addMoveScript(a.getValue());
                }
            }
            List<Element> lt = e.getChild(Xml.SCRIPTS).getChildren(Xml.TERMINATE);
            for (Element es : lt) {
                if ((a = es.getAttribute(Xml.NAME)) != null) {
                    addTerminationScript(a.getValue());
                }
            }
        }
        // check for optional railroad name and logo
        if ((e.getChild(Xml.RAIL_ROAD) != null) && (a = e.getChild(Xml.RAIL_ROAD).getAttribute(Xml.NAME)) != null) {
            String name = a.getValue();
            setRailroadName(name);
        }
        if ((e.getChild(Xml.MANIFEST_LOGO) != null)) {
            if ((a = e.getChild(Xml.MANIFEST_LOGO).getAttribute(Xml.NAME)) != null) {
                setManifestLogoPathName(a.getValue());
            }
        }
        if ((a = e.getAttribute(Xml.SHOW_TIMES)) != null) {
            _showTimes = a.getValue().equals(Xml.TRUE);
        }

        addPropertyChangeListerners();
    }

    private void addPropertyChangeListerners() {
        InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(EngineTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarOwners.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(EngineModels.class).addPropertyChangeListener(this);
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-trains.dtd.
     *
     * @return Contents in a JDOM Element
     */
    public Element store() {
        Element e = new Element(Xml.TRAIN);
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.NAME, getName());
        e.setAttribute(Xml.DESCRIPTION, getRawDescription());
        e.setAttribute(Xml.DEPART_HOUR, getDepartureTimeHour());
        e.setAttribute(Xml.DEPART_MINUTE, getDepartureTimeMinute());

        Element eRowColor = new Element(Xml.ROW_COLOR);
        eRowColor.setAttribute(Xml.NAME, getTableRowColorName());
        eRowColor.setAttribute(Xml.RESET_ROW_COLOR, getRowColorNameReset());
        e.addContent(eRowColor);

        Element eRoute = new Element(Xml.ROUTE);
        if (getRoute() != null) {
            eRoute.setAttribute(Xml.NAME, getRoute().getName());
            eRoute.setAttribute(Xml.ID, getRoute().getId());
            e.addContent(eRoute);
            // build list of locations that this train skips
            String[] locationIds = getTrainSkipsLocations();
            if (locationIds.length > 0) {
                Element eSkips = new Element(Xml.SKIPS);
                for (String id : locationIds) {
                    Element eLoc = new Element(Xml.LOCATION);
                    RouteLocation rl = getRoute().getLocationById(id);
                    if (rl != null) {
                        eLoc.setAttribute(Xml.NAME, rl.getName());
                        eLoc.setAttribute(Xml.ID, id);
                        eSkips.addContent(eLoc);
                    }
                }
                eRoute.addContent(eSkips);
            }
        }
        // build list of locations that this train skips
        if (getCurrentRouteLocation() != null) {
            e.setAttribute(Xml.CURRENT, getCurrentRouteLocation().getId());
        }
        if (getDepartureTrack() != null) {
            e.setAttribute(Xml.DEPARTURE_TRACK, getDepartureTrack().getId());
        }
        if (getTerminationTrack() != null) {
            e.setAttribute(Xml.TERMINATION_TRACK, getTerminationTrack().getId());
        }
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
        e.setAttribute(Xml.SERVICE_ALL, isServiceAllCarsWithFinalDestinationsEnabled() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.SEND_CUSTOM_STAGING, isSendCarsWithCustomLoadsToStagingEnabled() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.BUILD_CONSIST, isBuildConsistEnabled() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.BUILT, isBuilt() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.BUILD, isBuildEnabled() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.BUILD_FAILED, isBuildFailed() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.BUILD_FAILED_MESSAGE, getBuildFailedMessage());
        e.setAttribute(Xml.PRINTED, isPrinted() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.MODIFIED, isModified() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.SWITCH_LIST_STATUS, getSwitchListStatus());
        if (getLeadEngine() != null) {
            e.setAttribute(Xml.LEAD_ENGINE, getLeadEngine().getId());
        }
        e.setAttribute(Xml.STATUS, getStatus());
        e.setAttribute(Xml.TERMINATION_DATE, getTerminationDate());
        e.setAttribute(Xml.REQUESTED_CARS, Integer.toString(getNumberCarsRequested()));
        e.setAttribute(Xml.STATUS_CODE, Integer.toString(getStatusCode()));
        e.setAttribute(Xml.OLD_STATUS_CODE, Integer.toString(getOldStatusCode()));
        e.setAttribute(Xml.COMMENT, getComment());
        e.setAttribute(Xml.SHOW_TIMES, isShowArrivalAndDepartureTimesEnabled() ? Xml.TRUE : Xml.FALSE);
        // build list of car types for this train
        String[] types = getTypeNames();
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
        // save list of car roads for this train
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
        // save list of car loads for this train
        if (!getLoadOption().equals(ALL_LOADS)) {
            e.setAttribute(Xml.CAR_LOAD_OPTION, getLoadOption());
            String[] loads = getLoadNames();
            // new way of saving car loads
            Element eLoads = new Element(Xml.CAR_LOADS);
            for (String load : loads) {
                Element eLoad = new Element(Xml.CAR_LOAD);
                eLoad.setAttribute(Xml.NAME, load);
                eLoads.addContent(eLoad);
            }
            e.addContent(eLoads);
        }
        // save list of car owners for this train
        if (!getOwnerOption().equals(ALL_OWNERS)) {
            e.setAttribute(Xml.CAR_OWNER_OPTION, getOwnerOption());
            String[] owners = getOwnerNames();
            // new way of saving car owners
            Element eOwners = new Element(Xml.CAR_OWNERS);
            for (String owner : owners) {
                Element eOwner = new Element(Xml.CAR_OWNER);
                eOwner.setAttribute(Xml.NAME, owner);
                eOwners.addContent(eOwner);
            }
            e.addContent(eOwners);
        }
        // save list of scripts for this train
        if (getBuildScripts().size() > 0 ||
                getAfterBuildScripts().size() > 0 ||
                getMoveScripts().size() > 0 ||
                getTerminationScripts().size() > 0) {
            Element es = new Element(Xml.SCRIPTS);
            if (getBuildScripts().size() > 0) {
                for (String scriptPathname : getBuildScripts()) {
                    Element em = new Element(Xml.BUILD);
                    em.setAttribute(Xml.NAME, scriptPathname);
                    es.addContent(em);
                }
            }
            if (getAfterBuildScripts().size() > 0) {
                for (String scriptPathname : getAfterBuildScripts()) {
                    Element em = new Element(Xml.AFTER_BUILD);
                    em.setAttribute(Xml.NAME, scriptPathname);
                    es.addContent(em);
                }
            }
            if (getMoveScripts().size() > 0) {
                for (String scriptPathname : getMoveScripts()) {
                    Element em = new Element(Xml.MOVE);
                    em.setAttribute(Xml.NAME, scriptPathname);
                    es.addContent(em);
                }
            }
            // save list of termination scripts for this train
            if (getTerminationScripts().size() > 0) {
                for (String scriptPathname : getTerminationScripts()) {
                    Element et = new Element(Xml.TERMINATE);
                    et.setAttribute(Xml.NAME, scriptPathname);
                    es.addContent(et);
                }
            }
            e.addContent(es);
        }
        if (!getRailroadName().equals(NONE)) {
            Element r = new Element(Xml.RAIL_ROAD);
            r.setAttribute(Xml.NAME, getRailroadName());
            e.addContent(r);
        }
        if (!getManifestLogoPathName().equals(NONE)) {
            Element l = new Element(Xml.MANIFEST_LOGO);
            l.setAttribute(Xml.NAME, getManifestLogoPathName());
            e.addContent(l);
        }

        if (getSecondLegOptions() != NO_CABOOSE_OR_FRED) {
            e.setAttribute(Xml.LEG2_OPTIONS, Integer.toString(getSecondLegOptions()));
            e.setAttribute(Xml.LEG2_ENGINES, getSecondLegNumberEngines());
            e.setAttribute(Xml.LEG2_ROAD, getSecondLegEngineRoad());
            e.setAttribute(Xml.LEG2_MODEL, getSecondLegEngineModel());
            e.setAttribute(Xml.LEG2_CABOOSE_ROAD, getSecondLegCabooseRoad());
            if (getSecondLegStartRouteLocation() != null) {
                e.setAttribute(Xml.LEG2_START, getSecondLegStartRouteLocation().getId());
            }
            if (getSecondLegEndRouteLocation() != null) {
                e.setAttribute(Xml.LEG2_END, getSecondLegEndRouteLocation().getId());
            }
        }
        if (getThirdLegOptions() != NO_CABOOSE_OR_FRED) {
            e.setAttribute(Xml.LEG3_OPTIONS, Integer.toString(getThirdLegOptions()));
            e.setAttribute(Xml.LEG3_ENGINES, getThirdLegNumberEngines());
            e.setAttribute(Xml.LEG3_ROAD, getThirdLegEngineRoad());
            e.setAttribute(Xml.LEG3_MODEL, getThirdLegEngineModel());
            e.setAttribute(Xml.LEG3_CABOOSE_ROAD, getThirdLegCabooseRoad());
            if (getThirdLegStartRouteLocation() != null) {
                e.setAttribute(Xml.LEG3_START, getThirdLegStartRouteLocation().getId());
            }
            if (getThirdLegEndRouteLocation() != null) {
                e.setAttribute(Xml.LEG3_END, getThirdLegEndRouteLocation().getId());
            }
        }
        return e;
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Train ({}) sees property change: ({}) old: ({}) new: ({})", getName(), e.getPropertyName(),
                    e.getOldValue(), e.getNewValue());
        }
        if (e.getPropertyName().equals(Route.DISPOSE)) {
            setRoute(null);
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY)) {
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
        if (e.getSource().getClass().equals(Route.class)) {
            setDirtyAndFirePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
        }
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        InstanceManager.getDefault(TrainManagerXml.class).setDirty(true);
        firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(Train.class);

}
