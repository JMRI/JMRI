package jmri.jmrit.operations.trains.trainbuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import jmri.InstanceManager;
import jmri.Version;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.ScheduleItem;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.router.Router;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.*;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * Methods to support the TrainBuilder class.
 *
 * @author Daniel Boudreau Copyright (C) 2021, 2026
 */
public class TrainBuilderBase extends TrainCommon {

    // report levels
    protected static final String ONE = Setup.BUILD_REPORT_MINIMAL;
    protected static final String THREE = Setup.BUILD_REPORT_NORMAL;
    protected static final String FIVE = Setup.BUILD_REPORT_DETAILED;
    protected static final String SEVEN = Setup.BUILD_REPORT_VERY_DETAILED;

    protected static final int DISPLAY_CAR_LIMIT_20 = 20; // build exception out
                                                          // of staging
    protected static final int DISPLAY_CAR_LIMIT_50 = 50;
    protected static final int DISPLAY_CAR_LIMIT_100 = 100;

    protected static final boolean USE_BUNIT = true;
    protected static final String TIMING = "timing of trains";

    // build variables shared between local routines
    Date _startTime; // when the build report started
    Train _train; // the train being built
    int _numberCars = 0; // number of cars moved by this train
    List<Engine> _engineList; // engines for this train, modified during build
    Engine _lastEngine; // last engine found from getEngine
    Engine _secondLeadEngine; // lead engine 2nd part of train's route
    Engine _thirdLeadEngine; // lead engine 3rd part of the train's route
    int _carIndex; // index for carList
    List<Car> _carList; // cars for this train, modified during the build
    List<RouteLocation> _routeList; // ordered list of locations
    Hashtable<String, Integer> _numOfBlocks; // Number of blocks of cars
                                             // departing staging.
    int _completedMoves; // the number of pick up car moves for a location
    int _reqNumOfMoves; // the requested number of car moves for a location
    Location _departLocation; // train departs this location
    Track _departStageTrack; // departure staging track (null if not staging)
    Location _terminateLocation; // train terminates at this location
    Track _terminateStageTrack; // terminate staging track (null if not staging)
    PrintWriter _buildReport; // build report for this train
    List<Car> _notRoutable = new ArrayList<>(); // cars that couldn't be routed
    List<Location> _modifiedLocations = new ArrayList<>(); // modified locations
    int _warnings = 0; // the number of warnings in the build report

    // managers
    TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
    TrainScheduleManager trainScheduleManager = InstanceManager.getDefault(TrainScheduleManager.class);
    CarLoads carLoads = InstanceManager.getDefault(CarLoads.class);
    Router router = InstanceManager.getDefault(Router.class);

    protected Date getStartTime() {
        return _startTime;
    }

    protected void setStartTime(Date date) {
        _startTime = date;
    }

    protected Train getTrain() {
        return _train;
    }

    protected void setTrain(Train train) {
        _train = train;
    }

    protected List<Engine> getEngineList() {
        return _engineList;
    }

    protected void setEngineList(List<Engine> list) {
        _engineList = list;
    }

    protected List<Car> getCarList() {
        return _carList;
    }

    protected void setCarList(List<Car> list) {
        _carList = list;
    }

    protected List<RouteLocation> getRouteList() {
        return _routeList;
    }

    protected void setRouteList(List<RouteLocation> list) {
        _routeList = list;
    }

    protected PrintWriter getBuildReport() {
        return _buildReport;
    }

    protected void setBuildReport(PrintWriter printWriter) {
        _buildReport = printWriter;
    }

    /**
     * Will also set the termination track if returning to staging
     *
     * @param track departure track from staging
     */
    protected void setDepartureStagingTrack(Track track) {
        if ((getTerminateStagingTrack() == null || getTerminateStagingTrack() == _departStageTrack) &&
                getDepartureLocation() == getTerminateLocation() &&
                Setup.isBuildAggressive() &&
                Setup.isStagingTrackImmediatelyAvail()) {
            setTerminateStagingTrack(track); // use the same track
        }
        _departStageTrack = track;
    }

    protected Location getDepartureLocation() {
        return _departLocation;
    }

    protected void setDepartureLocation(Location location) {
        _departLocation = location;
    }

    protected Track getDepartureStagingTrack() {
        return _departStageTrack;
    }

    protected void setTerminateStagingTrack(Track track) {
        _terminateStageTrack = track;
    }

    protected Location getTerminateLocation() {
        return _terminateLocation;
    }

    protected void setTerminateLocation(Location location) {
        _terminateLocation = location;
    }

    protected Track getTerminateStagingTrack() {
        return _terminateStageTrack;
    }

    protected void createBuildReportFile() throws BuildFailedException {
        // backup the train's previous build report file
        InstanceManager.getDefault(TrainManagerXml.class).savePreviousBuildStatusFile(getTrain().getName());

        // create build report file
        File file = InstanceManager.getDefault(TrainManagerXml.class).createTrainBuildReportFile(getTrain().getName());
        try {
            setBuildReport(new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)),
                    true));
        } catch (IOException e) {
            log.error("Can not open build report file: {}", e.getLocalizedMessage());
            throw new BuildFailedException(e);
        }
    }

    /**
     * Creates the build report header information lines. Build report date,
     * JMRI version, train schedule, build report display levels, setup comment.
     */
    protected void showBuildReportInfo() {
        addLine(ONE, Bundle.getMessage("BuildReportMsg", getTrain().getName(), getDate(getStartTime())));
        addLine(ONE,
                Bundle.getMessage("BuildReportVersion", Version.name()));
        if (!trainScheduleManager.getTrainScheduleActiveId().equals(TrainScheduleManager.NONE)) {
            if (trainScheduleManager.getTrainScheduleActiveId().equals(TrainSchedule.ANY)) {
                addLine(ONE, Bundle.getMessage("buildActiveSchedule", Bundle.getMessage("Any")));
            } else {
                TrainSchedule sch = trainScheduleManager.getActiveSchedule();
                if (sch != null) {
                    addLine(ONE, Bundle.getMessage("buildActiveSchedule", sch.getName()));
                }
            }
        }
        // show the various build detail levels
        addLine(THREE, Bundle.getMessage("buildReportLevelThree"));
        addLine(FIVE, Bundle.getMessage("buildReportLevelFive"));
        addLine(SEVEN, Bundle.getMessage("buildReportLevelSeven"));

        if (Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED)) {
            addLine(SEVEN, Bundle.getMessage("buildRouterReportLevelDetailed"));
        } else if (Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED)) {
            addLine(SEVEN, Bundle.getMessage("buildRouterReportLevelVeryDetailed"));
        }

        if (!Setup.getComment().trim().isEmpty()) {
            addLine(ONE, BLANK_LINE);
            addLine(ONE, Setup.getComment());
        }
        addLine(ONE, BLANK_LINE);
    }

    protected void setUpRoute() throws BuildFailedException {
        if (getTrain().getRoute() == null) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildErrorRoute", getTrain().getName()));
        }
        // get the train's route
        setRouteList(getTrain().getRoute().getLocationsBySequenceList());
        if (getRouteList().size() < 1) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildErrorNeedRoute", getTrain().getName()));
        }
        // train departs
        setDepartureLocation(locationManager.getLocationByName(getTrain().getTrainDepartsName()));
        if (getDepartureLocation() == null) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildErrorNeedDepLoc", getTrain().getName()));
        }
        // train terminates
        setTerminateLocation(locationManager.getLocationByName(getTrain().getTrainTerminatesName()));
        if (getTerminateLocation() == null) {
            throw new BuildFailedException(Bundle.getMessage("buildErrorNeedTermLoc", getTrain().getName()));
        }
    }

    /**
     * show train build options when in detailed mode
     */
    protected void showTrainBuildOptions() {
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
        addLine(FIVE, Bundle.getMessage("MenuItemBuildOptions") + ":");
        if (Setup.isBuildAggressive()) {
            if (Setup.isBuildOnTime()) {
                addLine(FIVE, Bundle.getMessage("BuildModeOnTime"));
            } else {
                addLine(FIVE, Bundle.getMessage("BuildModeAggressive"));
            }
            addLine(FIVE, Bundle.getMessage("BuildNumberPasses", Setup.getNumberPasses()));
            if (Setup.isStagingTrackImmediatelyAvail() && getDepartureLocation().isStaging()) {
                addLine(FIVE, Bundle.getMessage("BuildStagingTrackAvail"));
            }
        } else {
            addLine(FIVE, Bundle.getMessage("BuildModeNormal"));
        }
        // show switcher options
        if (getTrain().isLocalSwitcher()) {
            addLine(FIVE, BLANK_LINE);
            addLine(FIVE, rb.getString("BorderLayoutSwitcherService") + ":");
            if (Setup.isLocalInterchangeMovesEnabled()) {
                addLine(FIVE, rb.getString("AllowLocalInterchange"));
            } else {
                addLine(FIVE, rb.getString("NoAllowLocalInterchange"));
            }
            if (Setup.isLocalSpurMovesEnabled()) {
                addLine(FIVE, rb.getString("AllowLocalSpur"));
            } else {
                addLine(FIVE, rb.getString("NoAllowLocalSpur"));
            }
            if (Setup.isLocalYardMovesEnabled()) {
                addLine(FIVE, rb.getString("AllowLocalYard"));
            } else {
                addLine(FIVE, rb.getString("NoAllowLocalYard"));
            }
        }
        // show staging options
        if (getDepartureLocation().isStaging() || getTerminateLocation().isStaging()) {
            addLine(FIVE, BLANK_LINE);
            addLine(FIVE, Bundle.getMessage("buildStagingOptions"));

            if (Setup.isStagingTrainCheckEnabled() && getTerminateLocation().isStaging()) {
                addLine(FIVE, Bundle.getMessage("buildOptionRestrictStaging"));
            }
            if (Setup.isStagingTrackImmediatelyAvail() && getTerminateLocation().isStaging()) {
                addLine(FIVE, rb.getString("StagingAvailable"));
            }
            if (Setup.isStagingAllowReturnEnabled() &&
                    getDepartureLocation().isStaging() &&
                    getTerminateLocation().isStaging() &&
                    getDepartureLocation() == getTerminateLocation()) {
                addLine(FIVE, rb.getString("AllowCarsToReturn"));
            }
            if (Setup.isStagingPromptFromEnabled() && getDepartureLocation().isStaging()) {
                addLine(FIVE, rb.getString("PromptFromStaging"));
            }
            if (Setup.isStagingPromptToEnabled() && getTerminateLocation().isStaging()) {
                addLine(FIVE, rb.getString("PromptToStaging"));
            }
            if (Setup.isStagingTryNormalBuildEnabled() && getDepartureLocation().isStaging()) {
                addLine(FIVE, rb.getString("TryNormalStaging"));
            }
        }

        // Car routing options
        addLine(FIVE, BLANK_LINE);
        addLine(FIVE, Bundle.getMessage("buildCarRoutingOptions"));

        // warn if car routing is disabled
        if (!Setup.isCarRoutingEnabled()) {
            addLine(FIVE, Bundle.getMessage("RoutingDisabled"));
            _warnings++;
        } else {
            if (Setup.isCarRoutingViaYardsEnabled()) {
                addLine(FIVE, Bundle.getMessage("RoutingViaYardsEnabled"));
            }
            if (Setup.isCarRoutingViaStagingEnabled()) {
                addLine(FIVE, Bundle.getMessage("RoutingViaStagingEnabled"));
            }
            if (Setup.isOnlyActiveTrainsEnabled()) {
                addLine(FIVE, Bundle.getMessage("OnlySelectedTrains"));
                _warnings++;
                // list the selected trains
                for (Train train : trainManager.getTrainsByNameList()) {
                    if (train.isBuildEnabled()) {
                        addLine(SEVEN,
                                Bundle.getMessage("buildTrainNameAndDesc", train.getName(), train.getDescription()));
                    }
                }
                if (!getTrain().isBuildEnabled()) {
                    addLine(FIVE, Bundle.getMessage("buildTrainNotSelected", getTrain().getName()));
                }
            } else {
                addLine(FIVE, rb.getString("AllTrains"));
            }
            if (Setup.isCheckCarDestinationEnabled()) {
                addLine(FIVE, Bundle.getMessage("CheckCarDestination"));
            }
        }
        addLine(FIVE, BLANK_LINE);
    }

    /*
     * Show the enabled and disabled build options for this train.
     */
    protected void showSpecificTrainBuildOptions() {
        addLine(FIVE,
                Bundle.getMessage("buildOptionsForTrain", getTrain().getName()));
        showSpecificTrainBuildOptions(true);
        addLine(FIVE, Bundle.getMessage("buildDisabledOptionsForTrain", getTrain().getName()));
        showSpecificTrainBuildOptions(false);
    }

    /*
     * Enabled when true lists selected build options for this train. Enabled
     * when false list disabled build options for this train.
     */
    private void showSpecificTrainBuildOptions(boolean enabled) {

        if (getTrain().isBuildTrainNormalEnabled() ^ !enabled) {
            addLine(FIVE, Bundle.getMessage("NormalModeWhenBuilding"));
        }
        if (getTrain().isSendCarsToTerminalEnabled() ^ !enabled) {
            addLine(FIVE, Bundle.getMessage("SendToTerminal", getTerminateLocation().getName()));
        }
        if ((getTrain().isAllowReturnToStagingEnabled() || Setup.isStagingAllowReturnEnabled()) ^ !enabled &&
                getDepartureLocation().isStaging() &&
                getDepartureLocation() == getTerminateLocation()) {
            addLine(FIVE, Bundle.getMessage("AllowCarsToReturn"));
        }
        if (getTrain().isAllowLocalMovesEnabled() ^ !enabled) {
            addLine(FIVE, Bundle.getMessage("AllowLocalMoves"));
        }
        if (getTrain().isAllowThroughCarsEnabled() ^ !enabled && getDepartureLocation() != getTerminateLocation()) {
            addLine(FIVE, Bundle.getMessage("AllowThroughCars"));
        }
        if (getTrain().isServiceAllCarsWithFinalDestinationsEnabled() ^ !enabled) {
            addLine(FIVE, Bundle.getMessage("ServiceAllCars"));
        }
        if (getTrain().isSendCarsWithCustomLoadsToStagingEnabled() ^ !enabled) {
            addLine(FIVE, Bundle.getMessage("SendCustomToStaging"));
        }
        if (getTrain().isBuildConsistEnabled() ^ !enabled) {
            addLine(FIVE, Bundle.getMessage("BuildConsist"));
            if (enabled) {
                addLine(SEVEN, Bundle.getMessage("BuildConsistHPT", Setup.getHorsePowerPerTon()));
            }
        }
        addLine(FIVE, BLANK_LINE);
    }

    /**
     * Adds to the build report what the train will service. Road and owner
     * names, built dates, and engine types.
     */
    protected void showTrainServices() {
        // show road names that this train will service
        if (!getTrain().getLocoRoadOption().equals(Train.ALL_ROADS)) {
            addLine(FIVE, Bundle.getMessage("buildTrainLocoRoads", getTrain().getName(),
                    getTrain().getLocoRoadOption(), formatStringToCommaSeparated(getTrain().getLocoRoadNames())));
        }
        // show owner names that this train will service
        if (!getTrain().getOwnerOption().equals(Train.ALL_OWNERS)) {
            addLine(FIVE, Bundle.getMessage("buildTrainOwners", getTrain().getName(), getTrain().getOwnerOption(),
                    formatStringToCommaSeparated(getTrain().getOwnerNames())));
        }
        // show built dates serviced
        if (!getTrain().getBuiltStartYear().equals(Train.NONE)) {
            addLine(FIVE,
                    Bundle.getMessage("buildTrainBuiltAfter", getTrain().getName(), getTrain().getBuiltStartYear()));
        }
        if (!getTrain().getBuiltEndYear().equals(Train.NONE)) {
            addLine(FIVE,
                    Bundle.getMessage("buildTrainBuiltBefore", getTrain().getName(), getTrain().getBuiltEndYear()));
        }

        // show engine types that this train will service
        if (!getTrain().getNumberEngines().equals("0")) {
            addLine(FIVE, Bundle.getMessage("buildTrainServicesEngineTypes", getTrain().getName()));
            addLine(FIVE, formatStringToCommaSeparated(getTrain().getLocoTypeNames()));
        }
    }

    /**
     * Show and initialize the train's route. Determines the number of car moves
     * requested for this train. Also adjust the number of car moves if the
     * random car moves option was selected.
     *
     * @throws BuildFailedException if random variable isn't an integer
     */
    protected void showAndInitializeTrainRoute() throws BuildFailedException {
        int requestedCarMoves = 0; // how many cars were asked to be moved
        // TODO: DAB control minimal build by each train

        addLine(THREE,
                Bundle.getMessage("buildTrainRoute", getTrain().getName(), getTrain().getRoute().getName()));

        // get the number of requested car moves for this train
        for (RouteLocation rl : getRouteList()) {
            // check to see if there's a location for each stop in the route
            // this checks for a deleted location
            Location location = locationManager.getLocationByName(rl.getName());
            if (location == null || rl.getLocation() == null) {
                throw new BuildFailedException(
                        Bundle.getMessage("buildErrorLocMissing", getTrain().getRoute().getName()));
            }
            // train doesn't drop or pick up cars from staging locations found
            // in middle of a route
            if (location.isStaging() &&
                    rl != getTrain().getTrainDepartsRouteLocation() &&
                    rl != getTrain().getTrainTerminatesRouteLocation()) {
                addLine(ONE,
                        Bundle.getMessage("buildLocStaging", rl.getName()));
                // don't allow car moves for this location
                rl.setCarMoves(rl.getMaxCarMoves());
            } else if (getTrain().isLocationSkipped(rl)) {
                // if a location is skipped, no car drops or pick ups
                addLine(THREE,
                        Bundle.getMessage("buildLocSkippedMaxTrain", rl.getId(), rl.getName(),
                                rl.getTrainDirectionString(), getTrain().getName(), rl.getMaxTrainLength(),
                                Setup.getLengthUnit().toLowerCase()));
                // don't allow car moves for this location
                rl.setCarMoves(rl.getMaxCarMoves());
            } else {
                // we're going to use this location, so initialize
                rl.setCarMoves(0); // clear the number of moves
                // add up the total number of car moves requested
                requestedCarMoves += rl.getMaxCarMoves();
                // show the type of moves allowed at this location
                if (!rl.isDropAllowed() && !rl.isPickUpAllowed() && !rl.isLocalMovesAllowed()) {
                    addLine(THREE,
                            Bundle.getMessage("buildLocNoDropsOrPickups", rl.getId(),
                                    location.isStaging() ? Bundle.getMessage("Staging") : Bundle.getMessage("Location"),
                                    rl.getName(),
                                    rl.getTrainDirectionString(), rl.getMaxTrainLength(),
                                    Setup.getLengthUnit().toLowerCase()));
                } else if (rl == getTrain().getTrainTerminatesRouteLocation()) {
                    addLine(THREE, Bundle.getMessage("buildLocTerminates", rl.getId(),
                            location.isStaging() ? Bundle.getMessage("Staging") : Bundle.getMessage("Location"),
                            rl.getName(), rl.getTrainDirectionString(), rl.getMaxCarMoves(),
                            rl.isPickUpAllowed() ? Bundle.getMessage("Pickups").toLowerCase() + ", " : "",
                            rl.isDropAllowed() ? Bundle.getMessage("Drop").toLowerCase() + ", " : "",
                            rl.isLocalMovesAllowed() ? Bundle.getMessage("LocalMoves").toLowerCase() + ", " : ""));
                } else {
                    addLine(THREE, Bundle.getMessage("buildLocRequestMoves", rl.getId(),
                            location.isStaging() ? Bundle.getMessage("Staging") : Bundle.getMessage("Location"),
                            rl.getName(), rl.getTrainDirectionString(), rl.getMaxCarMoves(),
                            rl.isPickUpAllowed() ? Bundle.getMessage("Pickups").toLowerCase() + ", " : "",
                            rl.isDropAllowed() ? Bundle.getMessage("Drop").toLowerCase() + ", " : "",
                            rl.isLocalMovesAllowed() ? Bundle.getMessage("LocalMoves").toLowerCase() + ", " : "",
                            rl.getMaxTrainLength(), Setup.getLengthUnit().toLowerCase()));
                }
            }
            rl.setTrainWeight(0); // clear the total train weight
            rl.setTrainLength(0); // and length
        }

        // check for random moves in the train's route
        for (RouteLocation rl : getRouteList()) {
            if (rl.getRandomControl().equals(RouteLocation.DISABLED)) {
                continue;
            }
            if (rl.getCarMoves() == 0 && rl.getMaxCarMoves() > 0) {
                log.debug("Location ({}) has random control value {} and maximum moves {}", rl.getName(),
                        rl.getRandomControl(), rl.getMaxCarMoves());
                try {
                    int value = Integer.parseInt(rl.getRandomControl());
                    // now adjust the number of available moves for this
                    // location
                    double random = Math.random();
                    log.debug("random {}", random);
                    int moves = (int) (random * ((rl.getMaxCarMoves() * value / 100) + 1));
                    log.debug("Reducing number of moves for location ({}) by {}", rl.getName(), moves);
                    rl.setCarMoves(moves);
                    requestedCarMoves = requestedCarMoves - moves;
                    addLine(FIVE,
                            Bundle.getMessage("buildRouteRandomControl", rl.getName(), rl.getId(),
                                    rl.getRandomControl(), rl.getMaxCarMoves(), rl.getMaxCarMoves() - moves));
                } catch (NumberFormatException e) {
                    throw new BuildFailedException(Bundle.getMessage("buildErrorRandomControl",
                            getTrain().getRoute().getName(), rl.getName(), rl.getRandomControl()));
                }
            }
        }

        int numMoves = requestedCarMoves; // number of car moves
        if (!getTrain().isLocalSwitcher()) {
            requestedCarMoves = requestedCarMoves / 2; // only need half as many
                                                       // cars to meet requests
        }
        addLine(ONE, Bundle.getMessage("buildRouteRequest", getTrain().getRoute().getName(),
                Integer.toString(requestedCarMoves), Integer.toString(numMoves)));

        getTrain().setNumberCarsRequested(requestedCarMoves); // save number of car
        // moves requested
        addLine(ONE, BLANK_LINE);
    }

    /**
     * reports if local switcher
     */
    protected void showIfLocalSwitcher() {
        if (getTrain().isLocalSwitcher()) {
            addLine(THREE, Bundle.getMessage("buildTrainIsSwitcher", getTrain().getName(),
                    TrainCommon.splitString(getTrain().getTrainDepartsName())));
            addLine(THREE, BLANK_LINE);
        }
    }

    /**
     * Show how many engines are required for this train, and if a certain road
     * name for the engine is requested. Show if there are any engine changes in
     * the route, or if helper engines are needed. There can be up to 2 engine
     * changes or helper requests. Show if caboose or FRED is needed for train,
     * and if there's a road name requested. There can be up to 2 caboose
     * changes in the route.
     */
    protected void showTrainRequirements() {
        addLine(ONE, Bundle.getMessage("TrainRequirements"));
        if (getTrain().isBuildConsistEnabled() && Setup.getHorsePowerPerTon() > 0) {
            addLine(ONE,
                    Bundle.getMessage("buildTrainReqConsist", Setup.getHorsePowerPerTon(),
                            getTrain().getNumberEngines()));
        } else if (getTrain().getNumberEngines().equals("0")) {
            addLine(ONE, Bundle.getMessage("buildTrainReq0Engine"));
        } else if (getTrain().getNumberEngines().equals("1")) {
            addLine(ONE, Bundle.getMessage("buildTrainReq1Engine", getTrain().getTrainDepartsName(),
                    getTrain().getEngineModel(), getTrain().getEngineRoad()));
        } else {
            addLine(ONE,
                    Bundle.getMessage("buildTrainReqEngine", getTrain().getTrainDepartsName(),
                            getTrain().getNumberEngines(),
                            getTrain().getEngineModel(), getTrain().getEngineRoad()));
        }
        // show any required loco changes
        if ((getTrain().getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
            addLine(ONE,
                    Bundle.getMessage("buildTrainEngineChange", getTrain().getSecondLegStartLocationName(),
                            getTrain().getSecondLegNumberEngines(), getTrain().getSecondLegEngineModel(),
                            getTrain().getSecondLegEngineRoad()));
        }
        if ((getTrain().getSecondLegOptions() & Train.ADD_ENGINES) == Train.ADD_ENGINES) {
            addLine(ONE,
                    Bundle.getMessage("buildTrainAddEngines", getTrain().getSecondLegNumberEngines(),
                            getTrain().getSecondLegStartLocationName(), getTrain().getSecondLegEngineModel(),
                            getTrain().getSecondLegEngineRoad()));
        }
        if ((getTrain().getSecondLegOptions() & Train.REMOVE_ENGINES) == Train.REMOVE_ENGINES) {
            addLine(ONE,
                    Bundle.getMessage("buildTrainRemoveEngines", getTrain().getSecondLegNumberEngines(),
                            getTrain().getSecondLegStartLocationName(), getTrain().getSecondLegEngineModel(),
                            getTrain().getSecondLegEngineRoad()));
        }
        if ((getTrain().getSecondLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
            addLine(ONE,
                    Bundle.getMessage("buildTrainHelperEngines", getTrain().getSecondLegNumberEngines(),
                            getTrain().getSecondLegStartLocationName(), getTrain().getSecondLegEndLocationName(),
                            getTrain().getSecondLegEngineModel(), getTrain().getSecondLegEngineRoad()));
        }

        if ((getTrain().getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
            addLine(ONE,
                    Bundle.getMessage("buildTrainEngineChange", getTrain().getThirdLegStartLocationName(),
                            getTrain().getThirdLegNumberEngines(), getTrain().getThirdLegEngineModel(),
                            getTrain().getThirdLegEngineRoad()));
        }
        if ((getTrain().getThirdLegOptions() & Train.ADD_ENGINES) == Train.ADD_ENGINES) {
            addLine(ONE,
                    Bundle.getMessage("buildTrainAddEngines", getTrain().getThirdLegNumberEngines(),
                            getTrain().getThirdLegStartLocationName(), getTrain().getThirdLegEngineModel(),
                            getTrain().getThirdLegEngineRoad()));
        }
        if ((getTrain().getThirdLegOptions() & Train.REMOVE_ENGINES) == Train.REMOVE_ENGINES) {
            addLine(ONE,
                    Bundle.getMessage("buildTrainRemoveEngines", getTrain().getThirdLegNumberEngines(),
                            getTrain().getThirdLegStartLocationName(), getTrain().getThirdLegEngineModel(),
                            getTrain().getThirdLegEngineRoad()));
        }
        if ((getTrain().getThirdLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
            addLine(ONE,
                    Bundle.getMessage("buildTrainHelperEngines", getTrain().getThirdLegNumberEngines(),
                            getTrain().getThirdLegStartLocationName(), getTrain().getThirdLegEndLocationName(),
                            getTrain().getThirdLegEngineModel(), getTrain().getThirdLegEngineRoad()));
        }
        // show caboose or FRED requirements
        if (getTrain().isCabooseNeeded()) {
            addLine(ONE, Bundle.getMessage("buildTrainRequiresCaboose", getTrain().getTrainDepartsName(),
                    getTrain().getCabooseRoad()));
        }
        // show any caboose changes in the train's route
        if ((getTrain().getSecondLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (getTrain().getSecondLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            addLine(ONE,
                    Bundle.getMessage("buildCabooseChange", getTrain().getSecondLegStartRouteLocation()));
        }
        if ((getTrain().getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (getTrain().getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            addLine(ONE, Bundle.getMessage("buildCabooseChange", getTrain().getThirdLegStartRouteLocation()));
        }
        if (getTrain().isFredNeeded()) {
            addLine(ONE,
                    Bundle.getMessage("buildTrainRequiresFRED", getTrain().getTrainDepartsName(),
                            getTrain().getCabooseRoad()));
        }
        addLine(ONE, BLANK_LINE);
    }

    protected void showTrainCarRoads() {
        if (!getTrain().getCarRoadOption().equals(Train.ALL_ROADS)) {
            addLine(FIVE, BLANK_LINE);
            addLine(FIVE, Bundle.getMessage("buildTrainRoads", getTrain().getName(),
                    getTrain().getCarRoadOption(), formatStringToCommaSeparated(getTrain().getCarRoadNames())));
        }
    }

    protected void showTrainCabooseRoads() {
        if (!getTrain().getCabooseRoadOption().equals(Train.ALL_ROADS)) {
            addLine(FIVE, BLANK_LINE);
            addLine(FIVE, Bundle.getMessage("buildTrainCabooseRoads", getTrain().getName(),
                    getTrain().getCabooseRoadOption(), formatStringToCommaSeparated(getTrain().getCabooseRoadNames())));
        }
    }

    protected void showTrainCarTypes() {
        addLine(FIVE, BLANK_LINE);
        addLine(FIVE, Bundle.getMessage("buildTrainServicesCarTypes", getTrain().getName()));
        addLine(FIVE, formatStringToCommaSeparated(getTrain().getCarTypeNames()));
    }

    protected void showTrainLoadNames() {
        if (!getTrain().getLoadOption().equals(Train.ALL_LOADS)) {
            addLine(FIVE, Bundle.getMessage("buildTrainLoads", getTrain().getName(), getTrain().getLoadOption(),
                    formatStringToCommaSeparated(getTrain().getLoadNames())));
        }
    }

    /**
     * Ask which staging track the train is to depart on.
     *
     * @return The departure track the user selected.
     */
    protected Track promptFromStagingDialog() {
        List<Track> tracksIn = getDepartureLocation().getTracksByNameList(null);
        List<Track> validTracks = new ArrayList<>();
        // only show valid tracks
        for (Track track : tracksIn) {
            if (checkDepartureStagingTrack(track)) {
                validTracks.add(track);
            }
        }
        if (validTracks.size() > 1) {
            // need an object array for dialog window
            Object[] tracks = new Object[validTracks.size()];
            for (int i = 0; i < validTracks.size(); i++) {
                tracks[i] = validTracks.get(i);
            }

            Track selected = (Track) JmriJOptionPane.showInputDialog(null,
                    Bundle.getMessage("TrainDepartingStaging", getTrain().getName(), getDepartureLocation().getName()),
                    Bundle.getMessage("SelectDepartureTrack"), JmriJOptionPane.QUESTION_MESSAGE, null, tracks, null);
            if (selected != null) {
                addLine(FIVE, Bundle.getMessage("buildUserSelectedDeparture", selected.getName(),
                        selected.getLocation().getName()));
            }
            return selected;
        } else if (validTracks.size() == 1) {
            Track track = validTracks.get(0);
            addLine(FIVE,
                    Bundle.getMessage("buildOnlyOneDepartureTrack", track.getName(), track.getLocation().getName()));
            return track;
        }
        return null; // no tracks available
    }

    /**
     * Ask which staging track the train is to terminate on.
     *
     * @return The termination track selected by the user.
     */
    protected Track promptToStagingDialog() {
        List<Track> tracksIn = getTerminateLocation().getTracksByNameList(null);
        List<Track> validTracks = new ArrayList<>();
        // only show valid tracks
        for (Track track : tracksIn) {
            if (checkTerminateStagingTrack(track)) {
                validTracks.add(track);
            }
        }
        if (validTracks.size() > 1) {
            // need an object array for dialog window
            Object[] tracks = new Object[validTracks.size()];
            for (int i = 0; i < validTracks.size(); i++) {
                tracks[i] = validTracks.get(i);
            }

            Track selected = (Track) JmriJOptionPane.showInputDialog(null,
                    Bundle.getMessage("TrainTerminatingStaging", getTrain().getName(),
                            getTerminateLocation().getName()),
                    Bundle.getMessage("SelectArrivalTrack"), JmriJOptionPane.QUESTION_MESSAGE, null, tracks, null);
            if (selected != null) {
                addLine(FIVE, Bundle.getMessage("buildUserSelectedArrival", selected.getName(),
                        selected.getLocation().getName()));
            }
            return selected;
        } else if (validTracks.size() == 1) {
            return validTracks.get(0);
        }
        return null; // no tracks available
    }

    /**
     * Removes the remaining cabooses and cars with FRED from consideration.
     *
     * @throws BuildFailedException code check if car being removed is in
     *                              staging
     */
    protected void removeCaboosesAndCarsWithFred() throws BuildFailedException {
        addLine(SEVEN, BLANK_LINE);
        addLine(SEVEN, Bundle.getMessage("buildRemoveCarsNotNeeded"));
        for (int i = 0; i < getCarList().size(); i++) {
            Car car = getCarList().get(i);
            if (car.isCaboose() || car.hasFred()) {
                addLine(SEVEN,
                        Bundle.getMessage("buildExcludeCarTypeAtLoc", car.toString(), car.getTypeName(),
                                car.getTypeExtensions(), car.getLocationName(), car.getTrackName()));
                // code check, should never be staging
                if (car.getTrack() == getDepartureStagingTrack()) {
                    throw new BuildFailedException("ERROR: Attempt to removed car with FRED or Caboose from staging"); // NOI18N
                }
                getCarList().remove(car); // remove this car from the list
                i--;
            }
        }
        addLine(SEVEN, BLANK_LINE);
    }

    /**
     * Save the car's final destination and schedule id in case of train reset
     */
    protected void saveCarFinalDestinations() {
        for (Car car : getCarList()) {
            car.setPreviousFinalDestination(car.getFinalDestination());
            car.setPreviousFinalDestinationTrack(car.getFinalDestinationTrack());
            car.setPreviousScheduleId(car.getScheduleItemId());
        }
    }

    /**
     * Creates the carList. Only cars that can be serviced by this train are in
     * the list.
     *
     * @throws BuildFailedException if car is marked as missing and is in
     *                              staging
     */
    protected void createCarList() throws BuildFailedException {
        // get list of cars for this route
        setCarList(carManager.getAvailableTrainList(getTrain()));
        addLine(SEVEN, BLANK_LINE);
        addLine(SEVEN, Bundle.getMessage("buildRemoveCars"));
        boolean showCar = true;
        int carListSize = getCarList().size();
        // now remove cars that the train can't service
        for (int i = 0; i < getCarList().size(); i++) {
            Car car = getCarList().get(i);
            // only show the first 100 cars removed due to wrong car type for
            // train
            if (showCar && carListSize - getCarList().size() == DISPLAY_CAR_LIMIT_100) {
                showCar = false;
                addLine(FIVE,
                        Bundle.getMessage("buildOnlyFirstXXXCars", DISPLAY_CAR_LIMIT_100, Bundle.getMessage("Type")));
            }
            // remove cars that don't have a track assignment
            if (car.getTrack() == null) {
                _warnings++;
                addLine(ONE,
                        Bundle.getMessage("buildWarningRsNoTrack", car.toString(), car.getLocationName()));
                getCarList().remove(car);
                i--;
                continue;
            }
            // remove cars that have been reported as missing
            if (car.isLocationUnknown()) {
                addLine(SEVEN, Bundle.getMessage("buildExcludeCarLocUnknown", car.toString(),
                        car.getLocationName(), car.getTrackName()));
                if (car.getTrack() == getDepartureStagingTrack()) {
                    throw new BuildFailedException(Bundle.getMessage("buildErrorLocationUnknown", car.getLocationName(),
                            car.getTrackName(), car.toString()));
                }
                getCarList().remove(car);
                i--;
                continue;
            }
            // remove cars that are out of service
            if (car.isOutOfService()) {
                addLine(SEVEN, Bundle.getMessage("buildExcludeCarOutOfService", car.toString(),
                        car.getLocationName(), car.getTrackName()));
                if (car.getTrack() == getDepartureStagingTrack()) {
                    throw new BuildFailedException(
                            Bundle.getMessage("buildErrorLocationOutOfService", car.getLocationName(),
                                    car.getTrackName(), car.toString()));
                }
                getCarList().remove(car);
                i--;
                continue;
            }
            // does car have a destination that is part of this train's route?
            if (car.getDestination() != null) {
                RouteLocation rld = getTrain().getRoute().getLastLocationByName(car.getDestinationName());
                if (rld == null) {
                    addLine(SEVEN, Bundle.getMessage("buildExcludeCarDestNotPartRoute", car.toString(),
                            car.getDestinationName(), car.getDestinationTrackName(), getTrain().getRoute().getName()));
                    // Code check, programming ERROR if car departing staging
                    if (car.getLocation() == getDepartureLocation() && getDepartureStagingTrack() != null) {
                        throw new BuildFailedException(Bundle.getMessage("buildErrorCarNotPartRoute", car.toString()));
                    }
                    getCarList().remove(car); // remove this car from the list
                    i--;
                    continue;
                }
            }
            // remove cars with FRED that have a destination that isn't the
            // terminal
            if (car.hasFred() && car.getDestination() != null && car.getDestination() != getTerminateLocation()) {
                addLine(FIVE,
                        Bundle.getMessage("buildExcludeCarWrongDest", car.toString(), car.getTypeName(),
                                car.getTypeExtensions(), car.getDestinationName()));
                getCarList().remove(car);
                i--;
                continue;
            }

            // remove cabooses that have a destination that isn't the terminal,
            // and no caboose changes in the train's route
            if (car.isCaboose() &&
                    car.getDestination() != null &&
                    car.getDestination() != getTerminateLocation() &&
                    (getTrain().getSecondLegOptions() & Train.ADD_CABOOSE + Train.REMOVE_CABOOSE) == 0 &&
                    (getTrain().getThirdLegOptions() & Train.ADD_CABOOSE + Train.REMOVE_CABOOSE) == 0) {
                addLine(FIVE,
                        Bundle.getMessage("buildExcludeCarWrongDest", car.toString(), car.getTypeName(),
                                car.getTypeExtensions(), car.getDestinationName()));
                getCarList().remove(car);
                i--;
                continue;
            }

            // is car at interchange or spur and is this train allowed to pull?
            if (!checkPickupInterchangeOrSpur(car)) {
                getCarList().remove(car);
                i--;
                continue;
            }

            // is car at interchange with destination restrictions?
            if (!checkPickupInterchangeDestinationRestrictions(car)) {
                getCarList().remove(car);
                i--;
                continue;
            }
            // note that for trains departing staging the engine and car roads,
            // types, owners, and built date were already checked.

            if (!car.isCaboose() && !getTrain().isCarRoadNameAccepted(car.getRoadName()) ||
                    car.isCaboose() && !getTrain().isCabooseRoadNameAccepted(car.getRoadName())) {
                addLine(SEVEN, Bundle.getMessage("buildExcludeCarWrongRoad", car.toString(),
                        car.getLocationName(), car.getTrackName(), car.getTypeName(), car.getTypeExtensions(),
                        car.getRoadName()));
                getCarList().remove(car);
                i--;
                continue;
            }
            if (!getTrain().isTypeNameAccepted(car.getTypeName())) {
                // only show lead cars when excluding car type
                if (showCar && (car.getKernel() == null || car.isLead())) {
                    addLine(SEVEN, Bundle.getMessage("buildExcludeCarWrongType", car.toString(),
                            car.getLocationName(), car.getTrackName(), car.getTypeName()));
                }
                getCarList().remove(car);
                i--;
                continue;
            }
            if (!getTrain().isOwnerNameAccepted(car.getOwnerName())) {
                addLine(SEVEN,
                        Bundle.getMessage("buildExcludeCarOwnerAtLoc", car.toString(), car.getOwnerName(),
                                car.getLocationName(), car.getTrackName()));
                getCarList().remove(car);
                i--;
                continue;
            }
            if (!getTrain().isBuiltDateAccepted(car.getBuilt())) {
                addLine(SEVEN,
                        Bundle.getMessage("buildExcludeCarBuiltAtLoc", car.toString(), car.getBuilt(),
                                car.getLocationName(), car.getTrackName()));
                getCarList().remove(car);
                i--;
                continue;
            }

            // all cars in staging must be accepted, so don't exclude if in
            // staging
            // note that a car's load can change when departing staging
            // a car's wait value is ignored when departing staging
            // a car's pick up day is ignored when departing staging
            if (getDepartureStagingTrack() == null || car.getTrack() != getDepartureStagingTrack()) {
                if (!car.isCaboose() &&
                        !car.isPassenger() &&
                        !getTrain().isLoadNameAccepted(car.getLoadName(), car.getTypeName())) {
                    addLine(SEVEN, Bundle.getMessage("buildExcludeCarLoadAtLoc", car.toString(),
                            car.getTypeName(), car.getLoadName()));
                    getCarList().remove(car);
                    i--;
                    continue;
                }
                // remove cars with FRED if not needed by train
                if (car.hasFred() && !getTrain().isFredNeeded()) {
                    addLine(SEVEN, Bundle.getMessage("buildExcludeCarWithFredAtLoc", car.toString(),
                            car.getTypeName(), (car.getLocationName() + ", " + car.getTrackName())));
                    getCarList().remove(car); // remove this car from the list
                    i--;
                    continue;
                }
                // does the car have a pick up day?
                if (!car.getPickupScheduleId().equals(Car.NONE)) {
                    if (trainScheduleManager.getTrainScheduleActiveId().equals(TrainSchedule.ANY) ||
                            car.getPickupScheduleId().equals(trainScheduleManager.getTrainScheduleActiveId())) {
                        car.setPickupScheduleId(Car.NONE);
                    } else {
                        TrainSchedule sch = trainScheduleManager.getScheduleById(car.getPickupScheduleId());
                        if (sch != null) {
                            addLine(SEVEN,
                                    Bundle.getMessage("buildExcludeCarSchedule", car.toString(), car.getTypeName(),
                                            car.getLocationName(), car.getTrackName(), sch.getName()));
                            getCarList().remove(car);
                            i--;
                            continue;
                        }
                    }
                }
                // does car have a wait count?
                if (car.getWait() > 0) {
                    addLine(SEVEN, Bundle.getMessage("buildExcludeCarWait", car.toString(),
                            car.getTypeName(), car.getLocationName(), car.getTrackName(), car.getWait()));
                    if (getTrain().isServiceable(car)) {
                        addLine(SEVEN, Bundle.getMessage("buildTrainCanServiceWait", getTrain().getName(),
                                car.toString(), car.getWait() - 1));
                        car.setWait(car.getWait() - 1); // decrement wait count
                        // a car's load changes when the wait count reaches 0
                        String oldLoad = car.getLoadName();
                        if (car.getTrack().isSpur()) {
                            car.updateLoad(car.getTrack()); // has the wait
                                                            // count reached 0?
                        }
                        String newLoad = car.getLoadName();
                        if (!oldLoad.equals(newLoad)) {
                            addLine(SEVEN, Bundle.getMessage("buildCarLoadChangedWait", car.toString(),
                                    car.getTypeName(), oldLoad, newLoad));
                        }
                    }
                    getCarList().remove(car);
                    i--;
                    continue;
                }
            }
        }
    }

    /**
     * Adjust car list to only have cars from one staging track
     *
     * @throws BuildFailedException if all cars departing staging can't be used
     */
    protected void adjustCarsInStaging() throws BuildFailedException {
        if (!getTrain().isDepartingStaging()) {
            return; // not departing staging
        }
        int numCarsFromStaging = 0;
        _numOfBlocks = new Hashtable<>();
        addLine(SEVEN, BLANK_LINE);
        addLine(SEVEN, Bundle.getMessage("buildRemoveCarsStaging"));
        for (int i = 0; i < getCarList().size(); i++) {
            Car car = getCarList().get(i);
            if (car.getLocation() == getDepartureLocation()) {
                if (car.getTrack() == getDepartureStagingTrack()) {
                    numCarsFromStaging++;
                    // populate car blocking hashtable
                    // don't block cabooses, cars with FRED, or passenger. Only
                    // block lead cars in
                    // kernel
                    if (!car.isCaboose() &&
                            !car.hasFred() &&
                            !car.isPassenger() &&
                            (car.getKernel() == null || car.isLead())) {
                        log.debug("Car {} last location id: {}", car.toString(), car.getLastLocationId());
                        Integer number = 1;
                        if (_numOfBlocks.containsKey(car.getLastLocationId())) {
                            number = _numOfBlocks.get(car.getLastLocationId()) + 1;
                            _numOfBlocks.remove(car.getLastLocationId());
                        }
                        _numOfBlocks.put(car.getLastLocationId(), number);
                    }
                } else {
                    addLine(SEVEN, Bundle.getMessage("buildExcludeCarAtLoc", car.toString(),
                            car.getTypeName(), car.getLocationName(), car.getTrackName()));
                    getCarList().remove(car);
                    i--;
                }
            }
        }
        // show how many cars are departing from staging
        addLine(FIVE, BLANK_LINE);
        addLine(FIVE, Bundle.getMessage("buildDepartingStagingCars",
                getDepartureStagingTrack().getLocation().getName(), getDepartureStagingTrack().getName(),
                numCarsFromStaging));
        // and list them
        for (Car car : getCarList()) {
            if (car.getTrack() == getDepartureStagingTrack()) {
                addLine(SEVEN, Bundle.getMessage("buildStagingCarAtLoc", car.toString(),
                        car.getTypeName(), car.getLoadType().toLowerCase(), car.getLoadName()));
            }
        }
        // error if all of the cars from staging aren't available
        if (!Setup.isBuildOnTime() && numCarsFromStaging != getDepartureStagingTrack().getNumberCars()) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildErrorNotAllCars", getDepartureStagingTrack().getName(),
                            Integer.toString(getDepartureStagingTrack().getNumberCars() - numCarsFromStaging)));
        }
        log.debug("Staging departure track ({}) has {} cars and {} blocks", getDepartureStagingTrack().getName(),
                numCarsFromStaging, _numOfBlocks.size()); // NOI18N
    }

    /**
     * List available cars by location. Removes non-lead kernel cars from the
     * car list.
     *
     * @throws BuildFailedException if kernel doesn't have lead or cars aren't
     *                              on the same track.
     */
    protected void showCarsByLocation() throws BuildFailedException {
        // show how many cars were found
        addLine(FIVE, BLANK_LINE);
        addLine(ONE,
                Bundle.getMessage("buildFoundCars", Integer.toString(getCarList().size()), getTrain().getName()));
        // only show cars once using the train's route
        List<String> locationNames = new ArrayList<>();
        for (RouteLocation rl : getTrain().getRoute().getLocationsBySequenceList()) {
            if (locationNames.contains(rl.getName())) {
                continue;
            }
            locationNames.add(rl.getName());
            int count = countRollingStockAt(rl, new ArrayList<RollingStock>(getCarList()));
            if (rl.getLocation().isStaging()) {
                addLine(FIVE,
                        Bundle.getMessage("buildCarsInStaging", count, rl.getName()));
            } else {
                addLine(FIVE,
                        Bundle.getMessage("buildCarsAtLocation", count, rl.getName()));
            }
            // now go through the car list and remove non-lead cars in kernels,
            // destinations
            // that aren't part of this route
            int carCount = 0;
            for (int i = 0; i < getCarList().size(); i++) {
                Car car = getCarList().get(i);
                if (!car.getLocationName().equals(rl.getName())) {
                    continue;
                }
                // only print out the first DISPLAY_CAR_LIMIT cars for each
                // location
                if (carCount < DISPLAY_CAR_LIMIT_50 && (car.getKernel() == null || car.isLead())) {
                    if (car.getLoadPriority().equals(CarLoad.PRIORITY_LOW) &&
                            car.getTrack().getTrackPriority().equals(Track.PRIORITY_NORMAL)) {
                        addLine(SEVEN,
                                Bundle.getMessage("buildCarAtLocWithMoves", car.toString(), car.getTypeName(),
                                        car.getTypeExtensions(), car.getLocationName(), car.getTrackName(),
                                        car.getMoves()));
                    } else {
                        addLine(SEVEN,
                                Bundle.getMessage("buildCarAtLocWithMovesPriority", car.toString(), car.getTypeName(),
                                        car.getTypeExtensions(), car.getLocationName(), car.getTrackName(),
                                        car.getTrack().getTrackPriority(), car.getMoves(),
                                        car.getLoadType().toLowerCase(), car.getLoadName(),
                                        car.getLoadPriority()));
                    }
                    if (car.isLead()) {
                        addLine(SEVEN,
                                Bundle.getMessage("buildCarLeadKernel", car.toString(), car.getKernelName(),
                                        car.getKernel().getSize(), car.getKernel().getTotalLength(),
                                        Setup.getLengthUnit().toLowerCase()));
                        // list all of the cars in the kernel now
                        for (Car k : car.getKernel().getCars()) {
                            if (!k.isLead()) {
                                addLine(SEVEN,
                                        Bundle.getMessage("buildCarPartOfKernel", k.toString(), k.getKernelName(),
                                                k.getKernel().getSize(), k.getKernel().getTotalLength(),
                                                Setup.getLengthUnit().toLowerCase()));
                            }
                        }
                    }
                    carCount++;
                    if (carCount == DISPLAY_CAR_LIMIT_50) {
                        addLine(SEVEN,
                                Bundle.getMessage("buildOnlyFirstXXXCars", carCount, rl.getName()));
                    }
                }
                // report car in kernel but lead has been removed
                if (car.getKernel() != null && !getCarList().contains(car.getKernel().getLead())) {
                    addLine(SEVEN,
                            Bundle.getMessage("buildCarPartOfKernel", car.toString(), car.getKernelName(),
                                    car.getKernel().getSize(), car.getKernel().getTotalLength(),
                                    Setup.getLengthUnit().toLowerCase()));
                }
                // use only the lead car in a kernel for building trains
                if (car.getKernel() != null) {
                    checkKernel(car); // kernel needs lead car and all cars on
                                      // the same track
                    if (!car.isLead()) {
                        getCarList().remove(car); // remove this car from the list
                        i--;
                        continue;
                    }
                }
                if (getTrain().equals(car.getTrain())) {
                    addLine(FIVE, Bundle.getMessage("buildCarAlreadyAssigned", car.toString()));
                }
            }
            addLine(SEVEN, BLANK_LINE);
        }
    }

    protected void sortCarsOnFifoLifoTracks() {
        addLine(SEVEN, Bundle.getMessage("buildSortCarsByLastDate"));
        for (_carIndex = 0; _carIndex < getCarList().size(); _carIndex++) {
            Car car = getCarList().get(_carIndex);
            if (car.getTrack().getServiceOrder().equals(Track.NORMAL) || car.getTrack().isStaging()) {
                continue;
            }
            addLine(SEVEN,
                    Bundle.getMessage("buildTrackModePriority", car.toString(), car.getTrack().getTrackTypeName(),
                            car.getLocationName(), car.getTrackName(), car.getTrack().getServiceOrder(),
                            car.getLastDate()));
            Car bestCar = car;
            for (int i = _carIndex + 1; i < getCarList().size(); i++) {
                Car testCar = getCarList().get(i);
                if (testCar.getTrack() == car.getTrack() &&
                        bestCar.getLoadPriority().equals(testCar.getLoadPriority())) {
                    log.debug("{} car ({}) last moved date: {}", car.getTrack().getTrackTypeName(), testCar.toString(),
                            testCar.getLastDate()); // NOI18N
                    if (car.getTrack().getServiceOrder().equals(Track.FIFO)) {
                        if (bestCar.getLastMoveDate().after(testCar.getLastMoveDate())) {
                            bestCar = testCar;
                            log.debug("New best car ({})", bestCar.toString());
                        }
                    } else if (car.getTrack().getServiceOrder().equals(Track.LIFO)) {
                        if (bestCar.getLastMoveDate().before(testCar.getLastMoveDate())) {
                            bestCar = testCar;
                            log.debug("New best car ({})", bestCar.toString());
                        }
                    }
                }
            }
            if (car != bestCar) {
                addLine(SEVEN,
                        Bundle.getMessage("buildTrackModeCarPriority", car.getTrack().getTrackTypeName(),
                                car.getTrackName(), car.getTrack().getServiceOrder(), bestCar.toString(),
                                bestCar.getLastDate(), car.toString(), car.getLastDate()));
                getCarList().remove(bestCar); // change sort
                getCarList().add(_carIndex, bestCar);
            }
        }
        addLine(SEVEN, BLANK_LINE);
    }

    /**
     * Verifies that all cars in the kernel have the same departure track. Also
     * checks to see if the kernel has a lead car and the lead car is in
     * service.
     *
     * @throws BuildFailedException
     */
    private void checkKernel(Car car) throws BuildFailedException {
        boolean foundLeadCar = false;
        for (Car c : car.getKernel().getCars()) {
            // check that lead car exists
            if (c.isLead() && !c.isOutOfService()) {
                foundLeadCar = true;
            }
            // check to see that all cars have the same location and track
            if (car.getLocation() != c.getLocation() ||
                    c.getTrack() == null ||
                    !car.getTrack().getSplitName().equals(c.getTrack().getSplitName())) {
                throw new BuildFailedException(Bundle.getMessage("buildErrorCarKernelLocation", c.toString(),
                        car.getKernelName(), c.getLocationName(), c.getTrackName(), car.toString(),
                        car.getLocationName(), car.getTrackName()));
            }
        }
        // code check, all kernels should have a lead car
        if (foundLeadCar == false) {
            throw new BuildFailedException(Bundle.getMessage("buildErrorCarKernelNoLead", car.getKernelName()));
        }
    }

    /*
     * For blocking cars out of staging
     */
    protected String getLargestBlock() {
        Enumeration<String> en = _numOfBlocks.keys();
        String largestBlock = "";
        int maxCars = 0;
        while (en.hasMoreElements()) {
            String locId = en.nextElement();
            if (_numOfBlocks.get(locId) > maxCars) {
                largestBlock = locId;
                maxCars = _numOfBlocks.get(locId);
            }
        }
        return largestBlock;
    }

    /**
     * Returns the routeLocation with the most available moves. Used for
     * blocking a train out of staging.
     *
     * @param blockRouteList The route for this train, modified by deleting
     *                       RouteLocations serviced
     * @param blockId        Where these cars were originally picked up from.
     * @return The location in the route with the most available moves.
     */
    protected RouteLocation getLocationWithMaximumMoves(List<RouteLocation> blockRouteList, String blockId) {
        RouteLocation rlMax = null;
        int maxMoves = 0;
        for (RouteLocation rl : blockRouteList) {
            if (rl == getTrain().getTrainDepartsRouteLocation()) {
                continue;
            }
            if (rl.getMaxCarMoves() - rl.getCarMoves() > maxMoves) {
                maxMoves = rl.getMaxCarMoves() - rl.getCarMoves();
                rlMax = rl;
            }
            // if two locations have the same number of moves, return the one
            // that doesn't match the block id
            if (rl.getMaxCarMoves() - rl.getCarMoves() == maxMoves && !rl.getLocation().getId().equals(blockId)) {
                rlMax = rl;
            }
        }
        return rlMax;
    }

    /**
     * Temporally remove cars from staging track if train returning to the same
     * staging track to free up track space.
     */
    protected void makeAdjustmentsIfDepartingStaging() {
        if (getTrain().isDepartingStaging()) {
            _reqNumOfMoves = 0;
            // Move cars out of staging after working other locations
            // if leaving and returning to staging on the same track, temporary pull cars off the track
            if (getDepartureStagingTrack() == getTerminateStagingTrack()) {
                if (!getTrain().isAllowReturnToStagingEnabled() && !Setup.isStagingAllowReturnEnabled()) {
                    // takes care of cars in a kernel by getting all cars
                    for (Car car : carManager.getList()) {
                        // don't remove caboose or car with FRED already
                        // assigned to train
                        if (car.getTrack() == getDepartureStagingTrack() && car.getRouteDestination() == null) {
                            car.setLocation(car.getLocation(), null);
                        }
                    }
                } else {
                    // since all cars can return to staging, the track space is
                    // consumed for now
                    addLine(THREE, BLANK_LINE);
                    addLine(THREE, Bundle.getMessage("buildWarnDepartStaging",
                            getDepartureStagingTrack().getLocation().getName(), getDepartureStagingTrack().getName()));
                    addLine(THREE, BLANK_LINE);
                }
            }
            addLine(THREE,
                    Bundle.getMessage("buildDepartStagingAggressive",
                            getDepartureStagingTrack().getLocation().getName()));
        }
    }

    /**
     * Restores cars departing staging track assignment.
     */
    protected void restoreCarsIfDepartingStaging() {
        if (getTrain().isDepartingStaging() &&
                getDepartureStagingTrack() == getTerminateStagingTrack() &&
                !getTrain().isAllowReturnToStagingEnabled() &&
                !Setup.isStagingAllowReturnEnabled()) {
            // restore departure track for cars departing staging
            for (Car car : getCarList()) {
                if (car.getLocation() == getDepartureStagingTrack().getLocation() && car.getTrack() == null) {
                    car.setLocation(getDepartureStagingTrack().getLocation(), getDepartureStagingTrack(),
                            RollingStock.FORCE); // force
                    if (car.getKernel() != null) {
                        for (Car k : car.getKernel().getCars()) {
                            k.setLocation(getDepartureStagingTrack().getLocation(), getDepartureStagingTrack(),
                                    RollingStock.FORCE); // force
                        }
                    }
                }
            }
        }
    }

    protected void showLoadGenerationOptionsStaging() {
        if (getDepartureStagingTrack() != null &&
                _reqNumOfMoves > 0 &&
                (getDepartureStagingTrack().isAddCustomLoadsEnabled() ||
                        getDepartureStagingTrack().isAddCustomLoadsAnySpurEnabled() ||
                        getDepartureStagingTrack().isAddCustomLoadsAnyStagingTrackEnabled())) {
            addLine(FIVE, Bundle.getMessage("buildCustomLoadOptions", getDepartureStagingTrack().getName()));
            if (getDepartureStagingTrack().isAddCustomLoadsEnabled()) {
                addLine(FIVE, Bundle.getMessage("buildLoadCarLoads"));
            }
            if (getDepartureStagingTrack().isAddCustomLoadsAnySpurEnabled()) {
                addLine(FIVE, Bundle.getMessage("buildLoadAnyCarLoads"));
            }
            if (getDepartureStagingTrack().isAddCustomLoadsAnyStagingTrackEnabled()) {
                addLine(FIVE, Bundle.getMessage("buildLoadsStaging"));
            }
            addLine(FIVE, BLANK_LINE);
        }
    }

    /**
     * Checks to see if all cars on a staging track have been given a
     * destination. Throws exception if there's a car without a destination.
     *
     * @throws BuildFailedException if car on staging track not assigned to
     *                              train
     */
    protected void checkStuckCarsInStaging() throws BuildFailedException {
        if (!getTrain().isDepartingStaging()) {
            return;
        }
        int carCount = 0;
        StringBuffer buf = new StringBuffer();
        // confirm that all cars in staging are departing
        for (Car car : getCarList()) {
            // build failure if car departing staging without a destination or
            // train
            if (car.getTrack() == getDepartureStagingTrack() &&
                    (car.getDestination() == null || car.getDestinationTrack() == null || car.getTrain() == null)) {
                if (car.getKernel() != null) {
                    for (Car c : car.getKernel().getCars()) {
                        carCount++;
                        addCarToStuckStagingList(c, buf, carCount);
                    }
                } else {
                    carCount++;
                    addCarToStuckStagingList(car, buf, carCount);
                }
            }
        }
        if (carCount > 0) {
            log.debug("{} cars stuck in staging", carCount);
            String msg = Bundle.getMessage("buildStagingCouldNotFindDest", carCount,
                    getDepartureStagingTrack().getLocation().getName(), getDepartureStagingTrack().getName());
            throw new BuildFailedException(msg + buf.toString(), BuildFailedException.STAGING);
        }
    }

    /**
     * Creates a list of up to 20 cars stuck in staging.
     *
     * @param car      The car to add to the list
     * @param buf      StringBuffer
     * @param carCount how many cars in the list
     */
    private void addCarToStuckStagingList(Car car, StringBuffer buf, int carCount) {
        if (carCount <= DISPLAY_CAR_LIMIT_20) {
            buf.append(NEW_LINE + " " + car.toString());
        } else if (carCount == DISPLAY_CAR_LIMIT_20 + 1) {
            buf.append(NEW_LINE +
                    Bundle.getMessage("buildOnlyFirstXXXCars", DISPLAY_CAR_LIMIT_20,
                            getDepartureStagingTrack().getName()));
        }
    }

    /**
     * Used to determine if a car on a staging track doesn't have a destination
     * or train
     *
     * @return true if at least one car doesn't have a destination or train.
     *         false if all cars have a destination.
     */
    protected boolean isCarStuckStaging() {
        if (getTrain().isDepartingStaging()) {
            // confirm that all cars in staging are departing
            for (Car car : getCarList()) {
                if (car.getTrack() == getDepartureStagingTrack() &&
                        (car.getDestination() == null || car.getDestinationTrack() == null || car.getTrain() == null)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void finishAddRsToTrain(RollingStock rs, RouteLocation rl, RouteLocation rld, int length,
            int weightTons) {
        // notify that locations have been modified when build done
        // allows automation actions to run properly
        if (!_modifiedLocations.contains(rl.getLocation())) {
            _modifiedLocations.add(rl.getLocation());
        }
        if (!_modifiedLocations.contains(rld.getLocation())) {
            _modifiedLocations.add(rld.getLocation());
        }
        rs.setTrain(getTrain());
        rs.setRouteLocation(rl);
        rs.setRouteDestination(rld);
        // now adjust train length and weight for each location that the rolling
        // stock is in the train
        boolean inTrain = false;
        for (RouteLocation routeLocation : getRouteList()) {
            if (rl == routeLocation) {
                inTrain = true;
            }
            if (rld == routeLocation) {
                break; // done
            }
            if (inTrain) {
                routeLocation.setTrainLength(routeLocation.getTrainLength() + length);
                routeLocation.setTrainWeight(routeLocation.getTrainWeight() + weightTons);
            }
        }
    }

    /**
     * Determine if rolling stock can be picked up based on train direction at
     * the route location.
     *
     * @param rs The rolling stock
     * @param rl The rolling stock's route location
     * @throws BuildFailedException if coding issue
     * @return true if there isn't a problem
     */
    protected boolean checkPickUpTrainDirection(RollingStock rs, RouteLocation rl) throws BuildFailedException {
        // Code Check, car or engine should have a track assignment
        if (rs.getTrack() == null) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildWarningRsNoTrack", rs.toString(), rs.getLocationName()));
        }
        // ignore local switcher direction
        if (getTrain().isLocalSwitcher()) {
            return true;
        }
        if ((rl.getTrainDirection() &
                rs.getLocation().getTrainDirections() &
                rs.getTrack().getTrainDirections()) != 0) {
            return true;
        }

        // Only track direction can cause the following message. Location
        // direction has already been checked
        addLine(SEVEN,
                Bundle.getMessage("buildRsCanNotPickupUsingTrain", rs.toString(), rl.getTrainDirectionString(),
                        rs.getTrackName(), rs.getLocationName(), rl.getId()));
        return false;
    }

    /**
     * Used to report a problem picking up the rolling stock due to train
     * direction.
     *
     * @param rl The route location
     * @return true if there isn't a problem
     */
    protected boolean checkPickUpTrainDirection(RouteLocation rl) {
        // ignore local switcher direction
        if (getTrain().isLocalSwitcher()) {
            return true;
        }
        if ((rl.getTrainDirection() & rl.getLocation().getTrainDirections()) != 0) {
            return true;
        }

        addLine(ONE, Bundle.getMessage("buildLocDirection", rl.getName(), rl.getTrainDirectionString()));
        return false;
    }

    /**
     * Determines if car can be pulled from an interchange or spur. Needed for
     * quick service tracks.
     * 
     * @param car the car being pulled
     * @return true if car can be pulled, otherwise false.
     */
    protected boolean checkPickupInterchangeOrSpur(Car car) {
        if (car.getTrack().isInterchange()) {
            // don't service a car at interchange and has been dropped off
            // by this train
            if (car.getTrack().getPickupOption().equals(Track.ANY) &&
                    car.getLastRouteId().equals(getTrain().getRoute().getId())) {
                addLine(SEVEN, Bundle.getMessage("buildExcludeCarDropByTrain", car.toString(),
                        car.getTypeName(), getTrain().getRoute().getName(), car.getLocationName(), car.getTrackName()));
                return false;
            }
        }
        // is car at interchange or spur and is this train allowed to pull?
        if (car.getTrack().isInterchange() || car.getTrack().isSpur()) {
            if (car.getTrack().getPickupOption().equals(Track.TRAINS) ||
                    car.getTrack().getPickupOption().equals(Track.EXCLUDE_TRAINS)) {
                if (car.getTrack().isPickupTrainAccepted(getTrain())) {
                    log.debug("Car ({}) can be picked up by this train", car.toString());
                } else {
                    addLine(SEVEN,
                            Bundle.getMessage("buildExcludeCarByTrain", car.toString(), car.getTypeName(),
                                    car.getTrack().getTrackTypeName(), car.getLocationName(), car.getTrackName()));
                    return false;
                }
            } else if (car.getTrack().getPickupOption().equals(Track.ROUTES) ||
                    car.getTrack().getPickupOption().equals(Track.EXCLUDE_ROUTES)) {
                if (car.getTrack().isPickupRouteAccepted(getTrain().getRoute())) {
                    log.debug("Car ({}) can be picked up by this route", car.toString());
                } else {
                    addLine(SEVEN,
                            Bundle.getMessage("buildExcludeCarByRoute", car.toString(), car.getTypeName(),
                                    car.getTrack().getTrackTypeName(), car.getLocationName(), car.getTrackName()));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks to see if an interchange track has destination restrictions.
     * Returns true if there's at least one destination in the train's route
     * that can service the car departing the interchange.
     * 
     * @param car the car being evaluated
     * @return true if car can be pulled
     */
    protected boolean checkPickupInterchangeDestinationRestrictions(Car car) {
        if (!car.getTrack().isInterchange() ||
                car.getTrack().getDestinationOption().equals(Track.ALL_DESTINATIONS) ||
                car.getFinalDestination() != null) {
            return true;
        }
        for (RouteLocation rl : getTrain().getRoute().getLocationsBySequenceList()) {
            if (car.getTrack().isDestinationAccepted(rl.getLocation())) {
                return true;
            }
        }
        addLine(SEVEN, Bundle.getMessage("buildExcludeCarByInterchange", car.toString(),
                car.getTypeName(), car.getTrackType(), car.getLocationName(), car.getTrackName()));
        return false;
    }

    /**
     * Checks to see if train length would be exceeded if this car was added to
     * the train.
     *
     * @param car the car in question
     * @param rl  the departure route location for this car
     * @param rld the destination route location for this car
     * @return true if car can be added to train
     */
    protected boolean checkTrainLength(Car car, RouteLocation rl, RouteLocation rld) {
        // car can be a kernel so get total length
        int length = car.getTotalKernelLength();
        boolean carInTrain = false;
        for (RouteLocation rlt : getRouteList()) {
            if (rl == rlt) {
                carInTrain = true;
            }
            if (rld == rlt) {
                break;
            }
            if (carInTrain && rlt.getTrainLength() + length > rlt.getMaxTrainLength()) {
                addLine(FIVE,
                        Bundle.getMessage("buildCanNotPickupCarLength", car.toString(), length,
                                Setup.getLengthUnit().toLowerCase(), rlt.getMaxTrainLength(),
                                Setup.getLengthUnit().toLowerCase(),
                                rlt.getTrainLength() + length - rlt.getMaxTrainLength(), rlt.getName(), rlt.getId()));
                return false;
            }
        }
        return true;
    }

    protected boolean checkDropTrainDirection(RollingStock rs, RouteLocation rld, Track track) {
        // local?
        if (getTrain().isLocalSwitcher()) {
            return true;
        }
        // this location only services trains with these directions
        int serviceTrainDir = rld.getLocation().getTrainDirections();
        if (track != null) {
            serviceTrainDir = serviceTrainDir & track.getTrainDirections();
        }

        // is this a car going to alternate track? Check to see if direct move
        // from alternate to FD track is possible
        if ((rld.getTrainDirection() & serviceTrainDir) != 0 &&
                rs != null &&
                track != null &&
                Car.class.isInstance(rs)) {
            Car car = (Car) rs;
            if (car.getFinalDestinationTrack() != null &&
                    track == car.getFinalDestinationTrack().getAlternateTrack() &&
                    (track.getTrainDirections() & car.getFinalDestinationTrack().getTrainDirections()) == 0) {
                addLine(SEVEN,
                        Bundle.getMessage("buildCanNotDropRsUsingTrain4", car.getFinalDestinationTrack().getName(),
                                formatStringToCommaSeparated(
                                        Setup.getDirectionStrings(car.getFinalDestinationTrack().getTrainDirections())),
                                car.getFinalDestinationTrack().getAlternateTrack().getName(),
                                formatStringToCommaSeparated(Setup.getDirectionStrings(
                                        car.getFinalDestinationTrack().getAlternateTrack().getTrainDirections()))));
                return false;
            }
        }

        if ((rld.getTrainDirection() & serviceTrainDir) != 0) {
            return true;
        }
        if (rs == null || track == null) {
            addLine(SEVEN,
                    Bundle.getMessage("buildDestinationDoesNotService", rld.getName(), rld.getTrainDirectionString()));
        } else {
            addLine(SEVEN, Bundle.getMessage("buildCanNotDropRsUsingTrain", rs.toString(),
                    rld.getTrainDirectionString(), track.getName()));
        }
        return false;
    }

    protected boolean checkDropTrainDirection(RouteLocation rld) {
        return (checkDropTrainDirection(null, rld, null));
    }

    /**
     * Determinate if rolling stock can be dropped by this train to the track
     * specified.
     *
     * @param rs    the rolling stock to be set out.
     * @param track the destination track.
     * @return true if able to drop.
     */
    protected boolean checkTrainCanDrop(RollingStock rs, Track track) {
        if (track.isInterchange() || track.isSpur()) {
            if (track.getDropOption().equals(Track.TRAINS) || track.getDropOption().equals(Track.EXCLUDE_TRAINS)) {
                if (track.isDropTrainAccepted(getTrain())) {
                    log.debug("Rolling stock ({}) can be droped by train to track ({})", rs.toString(),
                            track.getName());
                } else {
                    addLine(SEVEN,
                            Bundle.getMessage("buildCanNotDropTrain", rs.toString(), getTrain().getName(),
                                    track.getTrackTypeName(), track.getLocation().getName(), track.getName()));
                    return false;
                }
            }
            if (track.getDropOption().equals(Track.ROUTES) || track.getDropOption().equals(Track.EXCLUDE_ROUTES)) {
                if (track.isDropRouteAccepted(getTrain().getRoute())) {
                    log.debug("Rolling stock ({}) can be droped by route to track ({})", rs.toString(),
                            track.getName());
                } else {
                    addLine(SEVEN,
                            Bundle.getMessage("buildCanNotDropRoute", rs.toString(), getTrain().getRoute().getName(),
                                    track.getTrackTypeName(), track.getLocation().getName(), track.getName()));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check departure staging track to see if engines and cars are available to
     * a new train. Also confirms that the engine and car type, load, road, etc.
     * are accepted by the train.
     *
     * @param departStageTrack The staging track
     * @return true is there are engines and cars available.
     */
    protected boolean checkDepartureStagingTrack(Track departStageTrack) {
        addLine(THREE,
                Bundle.getMessage("buildStagingHas", departStageTrack.getName(),
                        Integer.toString(departStageTrack.getNumberEngines()),
                        Integer.toString(departStageTrack.getNumberCars())));
        // does this staging track service this train?
        if (!departStageTrack.isPickupTrainAccepted(getTrain())) {
            addLine(THREE, Bundle.getMessage("buildStagingNotTrain", departStageTrack.getName()));
            return false;
        }
        if (departStageTrack.getNumberRS() == 0 && getTrain().getTrainDepartsRouteLocation().getMaxCarMoves() > 0) {
            addLine(THREE, Bundle.getMessage("buildStagingEmpty", departStageTrack.getName()));
            return false;
        }
        if (departStageTrack.getUsedLength() > getTrain().getTrainDepartsRouteLocation().getMaxTrainLength()) {
            addLine(THREE,
                    Bundle.getMessage("buildStagingTrainTooLong", departStageTrack.getName(),
                            departStageTrack.getUsedLength(), Setup.getLengthUnit().toLowerCase(),
                            getTrain().getTrainDepartsRouteLocation().getMaxTrainLength()));
            return false;
        }
        if (departStageTrack.getNumberCars() > getTrain().getTrainDepartsRouteLocation().getMaxCarMoves()) {
            addLine(THREE, Bundle.getMessage("buildStagingTooManyCars", departStageTrack.getName(),
                    departStageTrack.getNumberCars(), getTrain().getTrainDepartsRouteLocation().getMaxCarMoves()));
            return false;
        }
        // does the staging track have the right number of locomotives?
        if (!getTrain().getNumberEngines().equals("0") &&
                getNumberEngines(getTrain().getNumberEngines()) != departStageTrack.getNumberEngines()) {
            addLine(THREE, Bundle.getMessage("buildStagingNotEngines", departStageTrack.getName(),
                    departStageTrack.getNumberEngines(), getTrain().getNumberEngines()));
            return false;
        }
        // is the staging track direction correct for this train?
        if ((departStageTrack.getTrainDirections() &
                getTrain().getTrainDepartsRouteLocation().getTrainDirection()) == 0) {
            addLine(THREE, Bundle.getMessage("buildStagingNotDirection", departStageTrack.getName()));
            return false;
        }

        // check engines on staging track
        if (!checkStagingEngines(departStageTrack)) {
            return false;
        }

        // check for car road, load, owner, built, Caboose or FRED needed
        if (!checkStagingCarTypeRoadLoadOwnerBuiltCabooseOrFRED(departStageTrack)) {
            return false;
        }

        // determine if staging track is in a pool (multiple trains on one
        // staging track)
        if (!checkStagingPool(departStageTrack)) {
            return false;
        }
        addLine(FIVE,
                Bundle.getMessage("buildTrainCanDepartTrack", getTrain().getName(), departStageTrack.getName()));
        return true;
    }

    /**
     * Used to determine if engines on staging track are acceptable to the train
     * being built.
     *
     * @param departStageTrack Depart staging track
     * @return true if engines on staging track meet train requirement
     */
    private boolean checkStagingEngines(Track departStageTrack) {
        if (departStageTrack.getNumberEngines() > 0) {
            for (Engine eng : engineManager.getList(departStageTrack)) {
                // clones are are already assigned to a train
                if (eng.isClone()) {
                    continue;
                }
                // has engine been assigned to another train?
                if (eng.getRouteLocation() != null) {
                    addLine(THREE, Bundle.getMessage("buildStagingDepart", departStageTrack.getName(),
                            eng.getTrainName()));
                    return false;
                }
                if (eng.getTrain() != null && eng.getTrain() != getTrain()) {
                    addLine(THREE, Bundle.getMessage("buildStagingDepartEngineTrain",
                            departStageTrack.getName(), eng.toString(), eng.getTrainName()));
                    return false;
                }
                // does the train accept the engine type from the staging
                // track?
                if (!getTrain().isTypeNameAccepted(eng.getTypeName())) {
                    addLine(THREE, Bundle.getMessage("buildStagingDepartEngineType",
                            departStageTrack.getName(), eng.toString(), eng.getTypeName(), getTrain().getName()));
                    return false;
                }
                // does the train accept the engine model from the staging
                // track?
                if (!getTrain().getEngineModel().equals(Train.NONE) &&
                        !getTrain().getEngineModel().equals(eng.getModel())) {
                    addLine(THREE,
                            Bundle.getMessage("buildStagingDepartEngineModel", departStageTrack.getName(),
                                    eng.toString(), eng.getModel(), getTrain().getName()));
                    return false;
                }
                // does the engine road match the train requirements?
                if (!getTrain().getCarRoadOption().equals(Train.ALL_ROADS) &&
                        !getTrain().getEngineRoad().equals(Train.NONE) &&
                        !getTrain().getEngineRoad().equals(eng.getRoadName())) {
                    addLine(THREE, Bundle.getMessage("buildStagingDepartEngineRoad",
                            departStageTrack.getName(), eng.toString(), eng.getRoadName(), getTrain().getName()));
                    return false;
                }
                // does the train accept the engine road from the staging
                // track?
                if (getTrain().getEngineRoad().equals(Train.NONE) &&
                        !getTrain().isLocoRoadNameAccepted(eng.getRoadName())) {
                    addLine(THREE, Bundle.getMessage("buildStagingDepartEngineRoad",
                            departStageTrack.getName(), eng.toString(), eng.getRoadName(), getTrain().getName()));
                    return false;
                }
                // does the train accept the engine owner from the staging
                // track?
                if (!getTrain().isOwnerNameAccepted(eng.getOwnerName())) {
                    addLine(THREE, Bundle.getMessage("buildStagingDepartEngineOwner",
                            departStageTrack.getName(), eng.toString(), eng.getOwnerName(), getTrain().getName()));
                    return false;
                }
                // does the train accept the engine built date from the
                // staging track?
                if (!getTrain().isBuiltDateAccepted(eng.getBuilt())) {
                    addLine(THREE,
                            Bundle.getMessage("buildStagingDepartEngineBuilt", departStageTrack.getName(),
                                    eng.toString(), eng.getBuilt(), getTrain().getName()));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks to see if all cars in staging can be serviced by the train being
     * built. Also searches for caboose or car with FRED.
     *
     * @param departStageTrack Departure staging track
     * @return True if okay
     */
    private boolean checkStagingCarTypeRoadLoadOwnerBuiltCabooseOrFRED(Track departStageTrack) {
        boolean foundCaboose = false;
        boolean foundFRED = false;
        if (departStageTrack.getNumberCars() > 0) {
            for (Car car : carManager.getList(departStageTrack)) {
                // clones are are already assigned to a train
                if (car.isClone()) {
                    continue;
                }
                // ignore non-lead cars in kernels
                if (car.getKernel() != null && !car.isLead()) {
                    continue; // ignore non-lead cars
                }
                // has car been assigned to another train?
                if (car.getRouteLocation() != null) {
                    log.debug("Car ({}) has route location ({})", car.toString(), car.getRouteLocation().getName());
                    addLine(THREE,
                            Bundle.getMessage("buildStagingDepart", departStageTrack.getName(), car.getTrainName()));
                    return false;
                }
                if (car.getTrain() != null && car.getTrain() != getTrain()) {
                    addLine(THREE, Bundle.getMessage("buildStagingDepartCarTrain",
                            departStageTrack.getName(), car.toString(), car.getTrainName()));
                    return false;
                }
                // does the train accept the car type from the staging track?
                if (!getTrain().isTypeNameAccepted(car.getTypeName())) {
                    addLine(THREE,
                            Bundle.getMessage("buildStagingDepartCarType", departStageTrack.getName(), car.toString(),
                                    car.getTypeName(), getTrain().getName()));
                    return false;
                }
                // does the train accept the car road from the staging track?
                if (!car.isCaboose() && !getTrain().isCarRoadNameAccepted(car.getRoadName())) {
                    addLine(THREE,
                            Bundle.getMessage("buildStagingDepartCarRoad", departStageTrack.getName(), car.toString(),
                                    car.getRoadName(), getTrain().getName()));
                    return false;
                }
                // does the train accept the car load from the staging track?
                if (!car.isCaboose() &&
                        !car.isPassenger() &&
                        (!car.getLoadName().equals(carLoads.getDefaultEmptyName()) ||
                                !departStageTrack.isAddCustomLoadsEnabled() &&
                                        !departStageTrack.isAddCustomLoadsAnySpurEnabled() &&
                                        !departStageTrack.isAddCustomLoadsAnyStagingTrackEnabled()) &&
                        !getTrain().isLoadNameAccepted(car.getLoadName(), car.getTypeName())) {
                    addLine(THREE,
                            Bundle.getMessage("buildStagingDepartCarLoad", departStageTrack.getName(), car.toString(),
                                    car.getLoadName(), getTrain().getName()));
                    return false;
                }
                // does the train accept the car owner from the staging track?
                if (!getTrain().isOwnerNameAccepted(car.getOwnerName())) {
                    addLine(THREE, Bundle.getMessage("buildStagingDepartCarOwner",
                            departStageTrack.getName(), car.toString(), car.getOwnerName(), getTrain().getName()));
                    return false;
                }
                // does the train accept the car built date from the staging
                // track?
                if (!getTrain().isBuiltDateAccepted(car.getBuilt())) {
                    addLine(THREE, Bundle.getMessage("buildStagingDepartCarBuilt",
                            departStageTrack.getName(), car.toString(), car.getBuilt(), getTrain().getName()));
                    return false;
                }
                // does the car have a destination serviced by this train?
                if (car.getDestination() != null) {
                    log.debug("Car ({}) has a destination ({}, {})", car.toString(), car.getDestinationName(),
                            car.getDestinationTrackName());
                    if (!getTrain().isServiceable(car)) {
                        addLine(THREE,
                                Bundle.getMessage("buildStagingDepartCarDestination", departStageTrack.getName(),
                                        car.toString(), car.getDestinationName(), getTrain().getName()));
                        return false;
                    }
                }
                // is this car a caboose with the correct road for this train?
                if (car.isCaboose() &&
                        (getTrain().getCabooseRoad().equals(Train.NONE) ||
                                getTrain().getCabooseRoad().equals(car.getRoadName()))) {
                    foundCaboose = true;
                }
                // is this car have a FRED with the correct road for this train?
                if (car.hasFred() &&
                        (getTrain().getCabooseRoad().equals(Train.NONE) ||
                                getTrain().getCabooseRoad().equals(car.getRoadName()))) {
                    foundFRED = true;
                }
            }
        }
        // does the train require a caboose and did we find one from staging?
        if (getTrain().isCabooseNeeded() && !foundCaboose) {
            addLine(THREE,
                    Bundle.getMessage("buildStagingNoCaboose", departStageTrack.getName(),
                            getTrain().getCabooseRoad()));
            return false;
        }
        // does the train require a car with FRED and did we find one from
        // staging?
        if (getTrain().isFredNeeded() && !foundFRED) {
            addLine(THREE,
                    Bundle.getMessage("buildStagingNoCarFRED", departStageTrack.getName(),
                            getTrain().getCabooseRoad()));
            return false;
        }
        return true;
    }

    /**
     * Used to determine if staging track in a pool is the appropriated one for
     * departure. Staging tracks in a pool can operate in one of two ways FIFO
     * or LIFO. In FIFO mode (First in First out), the program selects a staging
     * track from the pool that has cars with the earliest arrival date. In LIFO
     * mode (Last in First out), the program selects a staging track from the
     * pool that has cars with the latest arrival date.
     *
     * @param departStageTrack the track being tested
     * @return true if departure on this staging track is possible
     */
    private boolean checkStagingPool(Track departStageTrack) {
        if (departStageTrack.getPool() == null ||
                departStageTrack.getServiceOrder().equals(Track.NORMAL) ||
                departStageTrack.getNumberCars() == 0) {
            return true;
        }

        addLine(SEVEN, Bundle.getMessage("buildStagingTrackPool", departStageTrack.getName(),
                departStageTrack.getPool().getName(), departStageTrack.getPool().getSize(),
                departStageTrack.getServiceOrder()));

        List<Car> carList = carManager.getAvailableTrainList(getTrain());
        Date carDepartStageTrackDate = null;
        for (Car car : carList) {
            if (car.getTrack() == departStageTrack) {
                carDepartStageTrackDate = car.getLastMoveDate();
                break; // use 1st car found
            }
        }
        // next check isn't really necessary, null is never returned
        if (carDepartStageTrackDate == null) {
            return true; // no cars with found date
        }

        for (Track track : departStageTrack.getPool().getTracks()) {
            if (track == departStageTrack || track.getNumberCars() == 0) {
                continue;
            }
            // determine dates cars arrived into staging
            Date carOtherStageTrackDate = null;

            for (Car car : carList) {
                if (car.getTrack() == track) {
                    carOtherStageTrackDate = car.getLastMoveDate();
                    break; // use 1st car found
                }
            }
            if (carOtherStageTrackDate != null) {
                if (departStageTrack.getServiceOrder().equals(Track.LIFO)) {
                    if (carDepartStageTrackDate.before(carOtherStageTrackDate)) {
                        addLine(SEVEN,
                                Bundle.getMessage("buildStagingCarsBefore", departStageTrack.getName(),
                                        track.getName()));
                        return false;
                    }
                } else {
                    if (carOtherStageTrackDate.before(carDepartStageTrackDate)) {
                        addLine(SEVEN, Bundle.getMessage("buildStagingCarsBefore", track.getName(),
                                departStageTrack.getName()));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks to see if staging track can accept train.
     *
     * @param terminateStageTrack the staging track
     * @return true if staging track is empty, not reserved, and accepts car and
     *         engine types, roads, and loads.
     */
    protected boolean checkTerminateStagingTrack(Track terminateStageTrack) {
        if (!terminateStageTrack.isDropTrainAccepted(getTrain())) {
            addLine(FIVE, Bundle.getMessage("buildStagingNotTrain", terminateStageTrack.getName()));
            return false;
        }
        // In normal mode, find a completely empty track. In aggressive mode, a
        // track that scheduled to depart is okay
        if (((!Setup.isBuildAggressive() ||
                !Setup.isStagingTrackImmediatelyAvail() ||
                terminateStageTrack.isQuickServiceEnabled()) &&
                terminateStageTrack.getNumberRS() != 0) ||
                (terminateStageTrack.getNumberRS() != terminateStageTrack.getPickupRS()) &&
                        terminateStageTrack.getNumberRS() != 0) {
            addLine(FIVE,
                    Bundle.getMessage("buildStagingTrackOccupied", terminateStageTrack.getName(),
                            terminateStageTrack.getNumberEngines(), terminateStageTrack.getNumberCars()));
            if (terminateStageTrack.getIgnoreUsedLengthPercentage() == Track.IGNORE_0) {
                return false;
            } else {
                addLine(FIVE,
                        Bundle.getMessage("buildTrackHasPlannedPickups", terminateStageTrack.getName(),
                                terminateStageTrack.getIgnoreUsedLengthPercentage(), terminateStageTrack.getLength(),
                                Setup.getLengthUnit().toLowerCase(), terminateStageTrack.getUsedLength(),
                                terminateStageTrack.getReserved(),
                                terminateStageTrack.getReservedLengthSetouts(),
                                terminateStageTrack.getReservedLengthSetouts() - terminateStageTrack.getReserved(),
                                terminateStageTrack.getAvailableTrackSpace()));
            }
        }
        if ((!Setup.isBuildOnTime() || !terminateStageTrack.isQuickServiceEnabled()) &&
                terminateStageTrack.getDropRS() != 0) {
            addLine(FIVE, Bundle.getMessage("buildStagingTrackReserved", terminateStageTrack.getName(),
                    terminateStageTrack.getDropRS()));
            return false;
        }
        if (terminateStageTrack.getPickupRS() > 0) {
            addLine(FIVE, Bundle.getMessage("buildStagingTrackDepart", terminateStageTrack.getName()));
        }
        // if track is setup to accept a specific train or route, then ignore
        // other track restrictions
        if (terminateStageTrack.getDropOption().equals(Track.TRAINS) ||
                terminateStageTrack.getDropOption().equals(Track.ROUTES)) {
            addLine(SEVEN,
                    Bundle.getMessage("buildTrainCanTerminateTrack", getTrain().getName(),
                            terminateStageTrack.getName()));
            return true; // train can drop to this track, ignore other track
                         // restrictions
        }
        if (!Setup.isStagingTrainCheckEnabled()) {
            addLine(SEVEN,
                    Bundle.getMessage("buildTrainCanTerminateTrack", getTrain().getName(),
                            terminateStageTrack.getName()));
            return true;
        } else if (!checkTerminateStagingTrackRestrictions(terminateStageTrack)) {
            addLine(SEVEN,
                    Bundle.getMessage("buildStagingTrackRestriction", terminateStageTrack.getName(),
                            getTrain().getName()));
            addLine(SEVEN, Bundle.getMessage("buildOptionRestrictStaging"));
            return false;
        }
        return true;
    }

    private boolean checkTerminateStagingTrackRestrictions(Track terminateStageTrack) {
        // check go see if location/track will accept the train's car and engine
        // types
        for (String name : getTrain().getTypeNames()) {
            if (!getTerminateLocation().acceptsTypeName(name)) {
                addLine(FIVE,
                        Bundle.getMessage("buildDestinationType", getTerminateLocation().getName(), name));
                return false;
            }
            if (!terminateStageTrack.isTypeNameAccepted(name)) {
                addLine(FIVE,
                        Bundle.getMessage("buildStagingTrackType", terminateStageTrack.getLocation().getName(),
                                terminateStageTrack.getName(), name));
                return false;
            }
        }
        // check go see if track will accept the train's car roads
        if (getTrain().getCarRoadOption().equals(Train.ALL_ROADS) &&
                !terminateStageTrack.getRoadOption().equals(Track.ALL_ROADS)) {
            addLine(FIVE, Bundle.getMessage("buildStagingTrackAllRoads", terminateStageTrack.getName()));
            return false;
        }
        // now determine if roads accepted by train are also accepted by staging
        // track
        // TODO should we be checking caboose and loco road names?
        for (String road : InstanceManager.getDefault(CarRoads.class).getNames()) {
            if (getTrain().isCarRoadNameAccepted(road)) {
                if (!terminateStageTrack.isRoadNameAccepted(road)) {
                    addLine(FIVE,
                            Bundle.getMessage("buildStagingTrackRoad", terminateStageTrack.getLocation().getName(),
                                    terminateStageTrack.getName(), road));
                    return false;
                }
            }
        }

        // determine if staging will accept loads carried by train
        if (getTrain().getLoadOption().equals(Train.ALL_LOADS) &&
                !terminateStageTrack.getLoadOption().equals(Track.ALL_LOADS)) {
            addLine(FIVE, Bundle.getMessage("buildStagingTrackAllLoads", terminateStageTrack.getName()));
            return false;
        }
        // get all of the types and loads that a train can carry, and determine
        // if staging will accept
        for (String type : getTrain().getTypeNames()) {
            for (String load : carLoads.getNames(type)) {
                if (getTrain().isLoadNameAccepted(load, type)) {
                    if (!terminateStageTrack.isLoadNameAndCarTypeAccepted(load, type)) {
                        addLine(FIVE,
                                Bundle.getMessage("buildStagingTrackLoad", terminateStageTrack.getLocation().getName(),
                                        terminateStageTrack.getName(), type + CarLoad.SPLIT_CHAR + load));
                        return false;
                    }
                }
            }
        }
        addLine(SEVEN,
                Bundle.getMessage("buildTrainCanTerminateTrack", getTrain().getName(), terminateStageTrack.getName()));
        return true;
    }

    boolean routeToTrackFound;

    protected boolean checkBasicMoves(Car car, Track track) {
        if (car.getTrack() == track) {
            return false;
        }
        // don't allow local move to track with a "similar" name
        if (car.getSplitLocationName().equals(track.getLocation().getSplitName()) &&
                car.getSplitTrackName().equals(track.getSplitName())) {
            return false;
        }
        if (track.isStaging() && car.getLocation() == track.getLocation()) {
            return false; // don't use same staging location
        }
        // is the car's destination the terminal and is that allowed?
        if (!checkThroughCarsAllowed(car, track.getLocation().getName())) {
            return false;
        }
        if (!checkLocalMovesAllowed(car, track)) {
            return false;
        }
        return true;
    }

    /**
     * Used when generating a car load from staging.
     *
     * @param car   the car.
     * @param track the car's destination track that has the schedule.
     * @return ScheduleItem si if match found, null otherwise.
     * @throws BuildFailedException if schedule doesn't have any line items
     */
    protected ScheduleItem getScheduleItem(Car car, Track track) throws BuildFailedException {
        if (track.getSchedule() == null) {
            return null;
        }
        if (!track.isTypeNameAccepted(car.getTypeName())) {
            log.debug("Track ({}) doesn't service car type ({})", track.getName(), car.getTypeName());
            if (!Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL)) {
                addLine(SEVEN,
                        Bundle.getMessage("buildSpurNotThisType", track.getLocation().getName(), track.getName(),
                                track.getScheduleName(), car.getTypeName()));
            }
            return null;
        }
        ScheduleItem si = null;
        if (track.getScheduleMode() == Track.SEQUENTIAL) {
            si = track.getCurrentScheduleItem();
            // code check
            if (si == null) {
                throw new BuildFailedException(Bundle.getMessage("buildErrorNoScheduleItem", track.getScheduleItemId(),
                        track.getScheduleName(), track.getName(), track.getLocation().getName()));
            }
            return checkScheduleItem(si, car, track);
        }
        log.debug("Track ({}) in match mode", track.getName());
        // go through entire schedule looking for a match
        for (int i = 0; i < track.getSchedule().getSize(); i++) {
            si = track.getNextScheduleItem();
            // code check
            if (si == null) {
                throw new BuildFailedException(Bundle.getMessage("buildErrorNoScheduleItem", track.getScheduleItemId(),
                        track.getScheduleName(), track.getName(), track.getLocation().getName()));
            }
            si = checkScheduleItem(si, car, track);
            if (si != null) {
                break;
            }
        }
        return si;
    }

    /**
     * Used when generating a car load from staging. Checks a schedule item to
     * see if the car type matches, and the train and track can service the
     * schedule item's load. This code doesn't check to see if the car's load
     * can be serviced by the schedule. Instead a schedule item is returned that
     * allows the program to assign a custom load to the car that matches a
     * schedule item. Therefore, schedule items that don't request a custom load
     * are ignored.
     *
     * @param si    the schedule item
     * @param car   the car to check
     * @param track the destination track
     * @return Schedule item si if okay, null otherwise.
     */
    private ScheduleItem checkScheduleItem(ScheduleItem si, Car car, Track track) {
        if (!car.getTypeName().equals(si.getTypeName()) ||
                si.getReceiveLoadName().equals(ScheduleItem.NONE) ||
                si.getReceiveLoadName().equals(carLoads.getDefaultEmptyName()) ||
                si.getReceiveLoadName().equals(carLoads.getDefaultLoadName())) {
            log.debug("Not using track ({}) schedule request type ({}) road ({}) load ({})", track.getName(),
                    si.getTypeName(), si.getRoadName(), si.getReceiveLoadName()); // NOI18N
            if (!Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL)) {
                addLine(SEVEN,
                        Bundle.getMessage("buildSpurScheduleNotUsed", track.getLocation().getName(), track.getName(),
                                track.getScheduleName(), si.getId(), track.getScheduleModeName().toLowerCase(),
                                si.getTypeName(), si.getRoadName(), si.getReceiveLoadName()));
            }
            return null;
        }
        if (!si.getRoadName().equals(ScheduleItem.NONE) && !car.getRoadName().equals(si.getRoadName())) {
            log.debug("Not using track ({}) schedule request type ({}) road ({}) load ({})", track.getName(),
                    si.getTypeName(), si.getRoadName(), si.getReceiveLoadName()); // NOI18N
            if (!Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL)) {
                addLine(SEVEN,
                        Bundle.getMessage("buildSpurScheduleNotUsed", track.getLocation().getName(), track.getName(),
                                track.getScheduleName(), si.getId(), track.getScheduleModeName().toLowerCase(),
                                si.getTypeName(), si.getRoadName(), si.getReceiveLoadName()));
            }
            return null;
        }
        if (!getTrain().isLoadNameAccepted(si.getReceiveLoadName(), si.getTypeName())) {
            addLine(SEVEN, Bundle.getMessage("buildTrainNotNewLoad", getTrain().getName(),
                    si.getReceiveLoadName(), track.getLocation().getName(), track.getName()));
            return null;
        }
        // does the departure track allow this load?
        if (!car.getTrack().isLoadNameAndCarTypeShipped(si.getReceiveLoadName(), car.getTypeName())) {
            addLine(SEVEN,
                    Bundle.getMessage("buildTrackNotLoadSchedule", car.getTrackName(), si.getReceiveLoadName(),
                            track.getLocation().getName(), track.getName(), si.getId()));
            return null;
        }
        if (!si.getSetoutTrainScheduleId().equals(ScheduleItem.NONE) &&
                !trainScheduleManager.getTrainScheduleActiveId().equals(si.getSetoutTrainScheduleId())) {
            log.debug("Schedule item isn't active");
            // build the status message
            TrainSchedule aSch = trainScheduleManager.getScheduleById(trainScheduleManager.getTrainScheduleActiveId());
            TrainSchedule tSch = trainScheduleManager.getScheduleById(si.getSetoutTrainScheduleId());
            String aName = "";
            String tName = "";
            if (aSch != null) {
                aName = aSch.getName();
            }
            if (tSch != null) {
                tName = tSch.getName();
            }
            addLine(SEVEN,
                    Bundle.getMessage("buildScheduleNotActive", track.getName(), si.getId(), tName, aName));

            return null;
        }
        if (!si.getRandom().equals(ScheduleItem.NONE)) {
            if (!si.doRandom()) {
                addLine(SEVEN,
                        Bundle.getMessage("buildScheduleRandom", track.getLocation().getName(), track.getName(),
                                track.getScheduleName(), si.getId(), si.getReceiveLoadName(), si.getRandom(),
                                si.getCalculatedRandom()));
                return null;
            }
        }
        log.debug("Found track ({}) schedule item id ({}) for car ({})", track.getName(), si.getId(), car.toString());
        return si;
    }

    protected void showCarServiceOrder(Car car) {
        if (!car.getTrack().getServiceOrder().equals(Track.NORMAL) && !car.getTrack().isStaging()) {
            addLine(SEVEN,
                    Bundle.getMessage("buildTrackModePriority", car.toString(), car.getTrack().getTrackTypeName(),
                            car.getLocationName(), car.getTrackName(), car.getTrack().getServiceOrder(),
                            car.getLastDate()));
        }
    }

    /**
     * Returns a list containing two tracks. The 1st track found for the car,
     * the 2nd track is the car's final destination if an alternate track was
     * used for the car. 2nd track can be null.
     *
     * @param car The car needing a destination track
     * @param rld the RouteLocation destination
     * @return List containing up to two tracks. No tracks if none found.
     */
    protected List<Track> getTracksAtDestination(Car car, RouteLocation rld) {
        List<Track> tracks = new ArrayList<>();
        Location testDestination = rld.getLocation();
        // first report if there are any alternate tracks
        for (Track track : testDestination.getTracksByNameList(null)) {
            if (track.isAlternate()) {
                addLine(SEVEN, Bundle.getMessage("buildTrackIsAlternate", car.toString(),
                        track.getTrackTypeName(), track.getLocation().getName(), track.getName()));
            }
        }
        // now find a track for this car
        for (Track testTrack : testDestination.getTracksByMoves(null)) {
            // normally don't move car to a track with the same name at the same
            // location
            if (car.getSplitLocationName().equals(testTrack.getLocation().getSplitName()) &&
                    car.getSplitTrackName().equals(testTrack.getSplitName()) &&
                    !car.isPassenger() &&
                    !car.isCaboose() &&
                    !car.hasFred()) {
                addLine(SEVEN,
                        Bundle.getMessage("buildCanNotDropCarSameTrack", car.toString(), testTrack.getName()));
                continue;
            }
            // Can the train service this track?
            if (!checkDropTrainDirection(car, rld, testTrack)) {
                continue;
            }
            // drop to interchange or spur?
            if (!checkTrainCanDrop(car, testTrack)) {
                continue;
            }
            // report if track has planned pickups
            if (testTrack.getIgnoreUsedLengthPercentage() > Track.IGNORE_0) {
                addLine(SEVEN,
                        Bundle.getMessage("buildTrackHasPlannedPickups", testTrack.getName(),
                                testTrack.getIgnoreUsedLengthPercentage(), testTrack.getLength(),
                                Setup.getLengthUnit().toLowerCase(), testTrack.getUsedLength(), testTrack.getReserved(),
                                testTrack.getReservedLengthSetouts(),
                                testTrack.getReservedLengthPickups(),
                                testTrack.getAvailableTrackSpace()));
            }
            String status = car.checkDestination(testDestination, testTrack);
            // Can be a caboose or car with FRED with a custom load
            // is the destination a spur with a schedule demanding this car's
            // custom load?
            if (status.equals(Track.OKAY) &&
                    !testTrack.getScheduleId().equals(Track.NONE) &&
                    !car.getLoadName().equals(carLoads.getDefaultEmptyName()) &&
                    !car.getLoadName().equals(carLoads.getDefaultLoadName())) {
                addLine(FIVE,
                        Bundle.getMessage("buildSpurScheduleLoad", testTrack.getName(), car.getLoadName()));
            }
            // check to see if alternate track is available if track full
            if (status.startsWith(Track.LENGTH)) {
                addLine(SEVEN,
                        Bundle.getMessage("buildCanNotDropCarBecause", car.toString(), testTrack.getTrackTypeName(),
                                testTrack.getLocation().getName(), testTrack.getName(), status));
                if (checkForAlternate(car, testTrack)) {
                    // send car to alternate track
                    tracks.add(testTrack.getAlternateTrack());
                    tracks.add(testTrack); // car's final destination
                    break; // done with this destination
                }
                continue;
            }
            // check for train timing
            if (status.equals(Track.OKAY)) {
                status = checkReserved(getTrain(), rld, car, testTrack, true);
                if (status.equals(TIMING) && checkForAlternate(car, testTrack)) {
                    // send car to alternate track
                    tracks.add(testTrack.getAlternateTrack());
                    tracks.add(testTrack); // car's final destination
                    break; // done with this destination
                }
            }
            // okay to drop car?
            if (!status.equals(Track.OKAY)) {
                addLine(SEVEN,
                        Bundle.getMessage("buildCanNotDropCarBecause", car.toString(), testTrack.getTrackTypeName(),
                                testTrack.getLocation().getName(), testTrack.getName(), status));
                continue;
            }
            if (!checkForLocalMove(car, testTrack)) {
                continue;
            }
            tracks.add(testTrack);
            tracks.add(null); // no final destination for this car
            break; // done with this destination
        }
        return tracks;
    }

    /**
     * Checks to see if track has an alternate and can be used
     * 
     * @param car       the car being dropped
     * @param testTrack the destination track
     * @return true if track has an alternate and can be used
     */
    protected boolean checkForAlternate(Car car, Track testTrack) {
        if (testTrack.getAlternateTrack() != null &&
                car.getTrack() != testTrack.getAlternateTrack() &&
                checkTrainCanDrop(car, testTrack.getAlternateTrack())) {
            addLine(SEVEN,
                    Bundle.getMessage("buildTrackFullHasAlternate", testTrack.getLocation().getName(),
                            testTrack.getName(), testTrack.getAlternateTrack().getName()));
            String status = car.checkDestination(testTrack.getLocation(), testTrack.getAlternateTrack());
            if (status.equals(Track.OKAY)) {
                return true;
            }
            addLine(SEVEN,
                    Bundle.getMessage("buildCanNotDropCarBecause", car.toString(),
                            testTrack.getAlternateTrack().getTrackTypeName(),
                            testTrack.getLocation().getName(), testTrack.getAlternateTrack().getName(),
                            status));
        }
        return false;
    }

    /**
     * Used to determine if car could be set out at earlier location in the
     * train's route.
     *
     * @param car       The car
     * @param trackTemp The destination track for this car
     * @param rld       Where in the route the destination track was found
     * @param start     Where to begin the check
     * @param routeEnd  Where to stop the check
     * @return The best RouteLocation to drop off the car
     */
    protected RouteLocation checkForEarlierDrop(Car car, Track trackTemp, RouteLocation rld, int start, int routeEnd) {
        for (int m = start; m < routeEnd; m++) {
            RouteLocation rle = getRouteList().get(m);
            if (rle == rld) {
                break;
            }
            if (rle.getName().equals(rld.getName()) &&
                    (rle.getCarMoves() < rle.getMaxCarMoves()) &&
                    rle.isDropAllowed() &&
                    checkDropTrainDirection(car, rle, trackTemp)) {
                log.debug("Found an earlier drop for car ({}) destination ({})", car.toString(), rle.getName()); // NOI18N
                return rle; // earlier drop in train's route
            }
        }
        return rld;
    }

    /*
     * Determines if rolling stock can be delivered to track when considering
     * timing of car pulls by other trains.
     */
    protected String checkReserved(Train train, RouteLocation rld, Car car, Track destTrack, boolean printMsg) {
        // car returning to same track?
        if (car.getTrack() != destTrack) {
            // car can be a kernel so get total length
            int length = car.getTotalKernelLength();
            log.debug("Car length: {}, available track space: {}, reserved: {}", length,
                    destTrack.getAvailableTrackSpace(), destTrack.getReserved());
            if (length > destTrack.getAvailableTrackSpace() +
                    destTrack.getReserved()) {
                boolean returned = false;
                String trainExpectedArrival = train.getExpectedArrivalTime(rld, true);
                int trainArrivalTimeMinutes = convertStringTime(trainExpectedArrival);
                int reservedReturned = 0;
                // does this car already have this destination?
                if (car.getDestinationTrack() == destTrack) {
                    reservedReturned = -car.getTotalKernelLength();
                }
                // get a list of cars on this track
                List<Car> cars = carManager.getList(destTrack);
                for (Car kar : cars) {
                    if (kar.getTrain() != null && kar.getTrain() != train) {
                        int carPullTime = convertStringTime(kar.getPickupTime());
                        if (trainArrivalTimeMinutes < carPullTime) {
                            // don't print if checking redirect to alternate
                            if (printMsg) {
                                addLine(SEVEN,
                                        Bundle.getMessage("buildCarTrainTiming", kar.toString(),
                                                kar.getTrack().getTrackTypeName(), kar.getLocationName(),
                                                kar.getTrackName(), kar.getTrainName(), kar.getPickupTime(),
                                                getTrain().getName(), trainExpectedArrival));
                            }
                            reservedReturned += kar.getTotalLength();
                            returned = true;
                        }
                    }
                }
                if (returned && length > destTrack.getAvailableTrackSpace() - reservedReturned) {
                    if (printMsg) {
                        addLine(SEVEN,
                                Bundle.getMessage("buildWarnTrainTiming", car.toString(), destTrack.getTrackTypeName(),
                                        destTrack.getLocation().getName(), destTrack.getName(), getTrain().getName(),
                                        destTrack.getAvailableTrackSpace() - reservedReturned,
                                        Setup.getLengthUnit().toLowerCase()));
                    }
                    return TIMING;
                }
            }
        }
        return Track.OKAY;
    }

    /**
     * Checks to see if local move is allowed for this car
     *
     * @param car       the car being moved
     * @param testTrack the destination track for this car
     * @return false if local move not allowed
     */
    private boolean checkForLocalMove(Car car, Track testTrack) {
        if (getTrain().isLocalSwitcher()) {
            // No local moves from spur to spur
            if (!Setup.isLocalSpurMovesEnabled() && testTrack.isSpur() && car.getTrack().isSpur()) {
                addLine(SEVEN,
                        Bundle.getMessage("buildNoSpurToSpurMove", car.getTrackName(), testTrack.getName()));
                return false;
            }
            // No local moves from yard to yard, except for cabooses and cars
            // with FRED
            if (!Setup.isLocalYardMovesEnabled() &&
                    testTrack.isYard() &&
                    car.getTrack().isYard() &&
                    !car.isCaboose() &&
                    !car.hasFred()) {
                addLine(SEVEN,
                        Bundle.getMessage("buildNoYardToYardMove", car.getTrackName(), testTrack.getName()));
                return false;
            }
            // No local moves from interchange to interchange
            if (!Setup.isLocalInterchangeMovesEnabled() &&
                    testTrack.isInterchange() &&
                    car.getTrack().isInterchange()) {
                addLine(SEVEN,
                        Bundle.getMessage("buildNoInterchangeToInterchangeMove", car.getTrackName(),
                                testTrack.getName()));
                return false;
            }
        }
        return true;
    }

    protected Track tryStaging(Car car, RouteLocation rldSave) throws BuildFailedException {
        // local switcher working staging?
        if (getTrain().isLocalSwitcher() &&
                !car.isPassenger() &&
                !car.isCaboose() &&
                !car.hasFred() &&
                car.getTrack() == getTerminateStagingTrack()) {
            addLine(SEVEN,
                    Bundle.getMessage("buildCanNotDropCarSameTrack", car.toString(), car.getTrack().getName()));
            return null;
        }
        // no need to check train and track direction into staging, already done
        String status = car.checkDestination(getTerminateStagingTrack().getLocation(), getTerminateStagingTrack());
        if (status.equals(Track.OKAY)) {
            return getTerminateStagingTrack();
            // only generate a new load if there aren't any other tracks
            // available for this car
        } else if (status.startsWith(Track.LOAD) &&
                car.getTrack() == getDepartureStagingTrack() &&
                car.getLoadName().equals(carLoads.getDefaultEmptyName()) &&
                rldSave == null &&
                (getDepartureStagingTrack().isAddCustomLoadsAnyStagingTrackEnabled() ||
                        getDepartureStagingTrack().isAddCustomLoadsEnabled() ||
                        getDepartureStagingTrack().isAddCustomLoadsAnySpurEnabled())) {
            // try and generate a load for this car into staging
            if (generateLoadCarDepartingAndTerminatingIntoStaging(car, getTerminateStagingTrack())) {
                return getTerminateStagingTrack();
            }
        }
        addLine(SEVEN,
                Bundle.getMessage("buildCanNotDropCarBecause", car.toString(),
                        getTerminateStagingTrack().getTrackTypeName(),
                        getTerminateStagingTrack().getLocation().getName(), getTerminateStagingTrack().getName(),
                        status));
        return null;
    }

    /**
     * Returns true if car can be picked up later in a train's route
     *
     * @param car the car
     * @param rl  car's route location
     * @param rld car's route location destination
     * @return true if car can be picked up later in a train's route
     * @throws BuildFailedException if coding issue
     */
    protected boolean checkForLaterPickUp(Car car, RouteLocation rl, RouteLocation rld) throws BuildFailedException {
        // is there another pick up location in the route?
        if (rl == rld || !rld.getName().equals(car.getLocationName())) {
            return false;
        }
        // last route location in the route?
        if (rld == getTrain().getTrainTerminatesRouteLocation() && !car.isLocalMove()) {
            return false;
        }
        // don't delay adding a caboose, passenger car, or car with FRED
        if (car.isCaboose() || car.isPassenger() || car.hasFred()) {
            return false;
        }
        // no later pick up if car is departing staging
        if (car.getLocation().isStaging()) {
            return false;
        }
        if (!checkPickUpTrainDirection(car, rld)) {
            addLine(SEVEN,
                    Bundle.getMessage("buildNoPickupLaterDirection", car.toString(), rld.getName(), rld.getId()));
            return false;
        }
        if (!rld.isPickUpAllowed() && !rld.isLocalMovesAllowed() ||
                !rld.isPickUpAllowed() && rld.isLocalMovesAllowed() && !car.isLocalMove()) {
            addLine(SEVEN,
                    Bundle.getMessage("buildNoPickupLater", car.toString(), rld.getName(), rld.getId()));
            return false;
        }
        if (rld.getCarMoves() >= rld.getMaxCarMoves()) {
            addLine(SEVEN,
                    Bundle.getMessage("buildNoPickupLaterMoves", car.toString(), rld.getName(), rld.getId()));
            return false;
        }
        // is the track full? If so, pull immediately, prevents overloading
        if (checkForPickUps(car, rl, false)) {
            addLine(SEVEN, Bundle.getMessage("buildNoPickupLaterTrack", car.toString(), rld.getName(),
                    car.getTrackName(), rld.getId(), car.getTrack().getLength() - car.getTrack().getUsedLength(),
                    Setup.getLengthUnit().toLowerCase()));
            return false;
        }
        // are there any other cars being pull from the same track, route location, and train?
        if (checkForPickUps(car, rl, true)) {
            addLine(SEVEN, Bundle.getMessage("buildAlreadyPickups", car.toString(), rld.getName(),
                    car.getTrackName(), rld.getId(), car.getTrack().getTrackTypeName(), rl.getName(),
                    car.getTrack().getName(), getTrain().getName()));
            return false;
        }
        addLine(SEVEN,
                Bundle.getMessage("buildPickupLaterOkay", car.toString(), rld.getName(), rld.getId()));
        return true;
    }

    /*
     * checks to see if the train being built already has car pick ups at the
     * same track, route location rl, and train, and there's a track space
     * issue.
     * 
     * return true if there are already pick ups from the car's track
     */
    private boolean checkForPickUps(Car car, RouteLocation rl, boolean isCheckForCars) {
        if (!car.isLocalMove() && rl.isDropAllowed()) {
            int length = 0;
            if (isCheckForCars) {
                for (Car c : carManager.getByTrainList(getTrain())) {
                    if (car.getTrack() == c.getTrack() && rl == c.getRouteLocation()) {
                        length += c.getTotalKernelLength();
                    }
                }
            }
            if (car.getTrack().getLength() - car.getTrack().getUsedLength() < car.getTotalKernelLength() + length) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true is cars are allowed to travel from origin to terminal
     *
     * @param car             The car
     * @param destinationName Destination name for this car
     * @return true if through cars are allowed. false if not.
     */
    protected boolean checkThroughCarsAllowed(Car car, String destinationName) {
        if (!getTrain().isAllowThroughCarsEnabled() &&
                !getTrain().isLocalSwitcher() &&
                !car.isCaboose() &&
                !car.hasFred() &&
                !car.isPassenger() &&
                car.getSplitLocationName().equals(getDepartureLocation().getSplitName()) &&
                splitString(destinationName).equals(getTerminateLocation().getSplitName()) &&
                !getDepartureLocation().getSplitName().equals(getTerminateLocation().getSplitName())) {
            addLine(FIVE, Bundle.getMessage("buildThroughTrafficNotAllow", getDepartureLocation().getName(),
                    getTerminateLocation().getName()));
            return false; // through cars not allowed
        }
        return true; // through cars allowed
    }

    private boolean checkLocalMovesAllowed(Car car, Track track) {
        if (!getTrain().isLocalSwitcher() &&
                !getTrain().isAllowLocalMovesEnabled() &&
                car.getSplitLocationName().equals(track.getLocation().getSplitName())) {
            addLine(SEVEN,
                    Bundle.getMessage("buildNoLocalMoveToTrack", car.getLocationName(), car.getTrackName(),
                            track.getLocation().getName(), track.getName(), getTrain().getName()));
            return false;
        }
        return true;
    }

    /**
     * Creates a car load for a car departing staging and eventually terminating
     * into staging.
     *
     * @param car        the car!
     * @param stageTrack the staging track the car will terminate to
     * @return true if a load was generated this this car.
     * @throws BuildFailedException if coding check fails
     */
    protected boolean generateLoadCarDepartingAndTerminatingIntoStaging(Car car, Track stageTrack)
            throws BuildFailedException {
        addLine(SEVEN, BLANK_LINE);
        // code check
        if (stageTrack == null || !stageTrack.isStaging()) {
            throw new BuildFailedException("ERROR coding issue, staging track null or not staging");
        }
        if (!stageTrack.isTypeNameAccepted(car.getTypeName())) {
            addLine(SEVEN,
                    Bundle.getMessage("buildStagingTrackType", stageTrack.getLocation().getName(), stageTrack.getName(),
                            car.getTypeName()));
            return false;
        }
        if (!stageTrack.isRoadNameAccepted(car.getRoadName())) {
            addLine(SEVEN,
                    Bundle.getMessage("buildStagingTrackRoad", stageTrack.getLocation().getName(), stageTrack.getName(),
                            car.getRoadName()));
            return false;
        }
        // Departing and returning to same location in staging?
        if (!getTrain().isAllowReturnToStagingEnabled() &&
                !Setup.isStagingAllowReturnEnabled() &&
                !car.isCaboose() &&
                !car.hasFred() &&
                !car.isPassenger() &&
                car.getSplitLocationName().equals(stageTrack.getLocation().getSplitName())) {
            addLine(SEVEN,
                    Bundle.getMessage("buildNoReturnStaging", car.toString(), stageTrack.getLocation().getName()));
            return false;
        }
        // figure out which loads the car can use
        List<String> loads = carLoads.getNames(car.getTypeName());
        // remove the default names
        loads.remove(carLoads.getDefaultEmptyName());
        loads.remove(carLoads.getDefaultLoadName());
        if (loads.size() == 0) {
            log.debug("No custom loads for car type ({}) ignoring staging track ({})", car.getTypeName(),
                    stageTrack.getName());
            return false;
        }
        addLine(SEVEN,
                Bundle.getMessage("buildSearchTrackLoadStaging", car.toString(), car.getTypeName(),
                        car.getLoadType().toLowerCase(), car.getLoadName(), car.getLocationName(), car.getTrackName(),
                        stageTrack.getLocation().getName(), stageTrack.getName()));
        String oldLoad = car.getLoadName(); // save car's "E" load
        for (int i = loads.size() - 1; i >= 0; i--) {
            String load = loads.get(i);
            log.debug("Try custom load ({}) for car ({})", load, car.toString());
            if (!car.getTrack().isLoadNameAndCarTypeShipped(load, car.getTypeName()) ||
                    !stageTrack.isLoadNameAndCarTypeAccepted(load, car.getTypeName()) ||
                    !getTrain().isLoadNameAccepted(load, car.getTypeName())) {
                // report why the load was rejected and remove it from consideration
                if (!car.getTrack().isLoadNameAndCarTypeShipped(load, car.getTypeName())) {
                    addLine(SEVEN,
                            Bundle.getMessage("buildTrackNotNewLoad", car.getTrackName(), load,
                                    stageTrack.getLocation().getName(), stageTrack.getName()));
                }
                if (!stageTrack.isLoadNameAndCarTypeAccepted(load, car.getTypeName())) {
                    addLine(SEVEN,
                            Bundle.getMessage("buildDestTrackNoLoad", stageTrack.getLocation().getName(),
                                    stageTrack.getName(), car.toString(), load));
                }
                if (!getTrain().isLoadNameAccepted(load, car.getTypeName())) {
                    addLine(SEVEN,
                            Bundle.getMessage("buildTrainNotNewLoad", getTrain().getName(), load,
                                    stageTrack.getLocation().getName(), stageTrack.getName()));
                }
                loads.remove(i);
                continue;
            }
            car.setLoadName(load);
            // does the car have a home division?
            if (car.getDivision() != null) {
                addLine(SEVEN,
                        Bundle.getMessage("buildCarHasDivisionStaging", car.toString(), car.getTypeName(),
                                car.getLoadType().toLowerCase(), car.getLoadName(), car.getDivisionName(),
                                car.getLocationName(),
                                car.getTrackName(), car.getTrack().getDivisionName()));
                // load type empty must return to car's home division
                // or load type load from foreign division must return to car's
                // home division
                if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY) &&
                        car.getDivision() != stageTrack.getDivision() ||
                        car.getLoadType().equals(CarLoad.LOAD_TYPE_LOAD) &&
                                car.getTrack().getDivision() != car.getDivision() &&
                                car.getDivision() != stageTrack.getDivision()) {
                    addLine(SEVEN,
                            Bundle.getMessage("buildNoDivisionTrack", stageTrack.getTrackTypeName(),
                                    stageTrack.getLocation().getName(), stageTrack.getName(),
                                    stageTrack.getDivisionName(), car.toString(),
                                    car.getLoadType().toLowerCase(), car.getLoadName()));
                    loads.remove(i);
                    continue;
                }
            }
        }
        // do we need to test all car loads?
        boolean loadRestrictions = isLoadRestrictions();
        // now determine if the loads can be routed to the staging track
        for (int i = loads.size() - 1; i >= 0; i--) {
            String load = loads.get(i);
            car.setLoadName(load);
            if (!router.isCarRouteable(car, getTrain(), stageTrack, getBuildReport())) {
                loads.remove(i); // no remove this load
                addLine(SEVEN, Bundle.getMessage("buildStagingTrackNotReachable",
                        stageTrack.getLocation().getName(), stageTrack.getName(), load));
                if (!loadRestrictions) {
                    loads.clear(); // no loads can be routed
                    break;
                }
            } else if (!loadRestrictions) {
                break; // done all loads can be routed
            }
        }
        // Use random loads rather that the first one that works to create
        // interesting loads
        if (loads.size() > 0) {
            int rnd = (int) (Math.random() * loads.size());
            car.setLoadName(loads.get(rnd));
            // check to see if car is now accepted by staging
            String status = car.checkDestination(stageTrack.getLocation(), stageTrack);
            if (status.equals(Track.OKAY) ||
                    (status.startsWith(Track.LENGTH) && stageTrack != getTerminateStagingTrack())) {
                car.setLoadGeneratedFromStaging(true);
                car.setFinalDestination(stageTrack.getLocation());
                // don't set track assignment unless the car is going to this
                // train's staging
                if (stageTrack == getTerminateStagingTrack()) {
                    car.setFinalDestinationTrack(stageTrack);
                } else {
                    // don't assign the track, that will be done later
                    car.setFinalDestinationTrack(null);
                }
                car.updateKernel(); // is car part of kernel?
                addLine(SEVEN,
                        Bundle.getMessage("buildAddingScheduleLoad", loads.size(), car.getLoadName(), car.toString()));
                return true;
            }
            addLine(SEVEN,
                    Bundle.getMessage("buildCanNotDropCarBecause", car.toString(), stageTrack.getTrackTypeName(),
                            stageTrack.getLocation().getName(), stageTrack.getName(), status));
        }
        car.setLoadName(oldLoad); // restore load and report failure
        addLine(SEVEN, Bundle.getMessage("buildUnableNewLoadStaging", car.toString(), car.getTrackName(),
                stageTrack.getLocation().getName(), stageTrack.getName()));
        return false;
    }

    /**
     * Checks to see if there are any load restrictions for trains,
     * interchanges, and yards if routing through yards is enabled.
     *
     * @return true if there are load restrictions.
     */
    private boolean isLoadRestrictions() {
        boolean restrictions = isLoadRestrictionsTrain() || isLoadRestrictions(Track.INTERCHANGE);
        if (Setup.isCarRoutingViaYardsEnabled()) {
            restrictions = restrictions || isLoadRestrictions(Track.YARD);
        }
        return restrictions;
    }

    private boolean isLoadRestrictions(String type) {
        for (Track track : locationManager.getTracks(type)) {
            if (!track.getLoadOption().equals(Track.ALL_LOADS)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLoadRestrictionsTrain() {
        for (Train train : trainManager.getList()) {
            if (!train.getLoadOption().equals(Train.ALL_LOADS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * report any cars left at route location
     *
     * @param rl route location
     */
    protected void showCarsNotMoved(RouteLocation rl) {
        if (_carIndex < 0) {
            _carIndex = 0;
        }
        // cars up this point have build report messages, only show the cars
        // that aren't
        // in the build report
        int numberCars = 0;
        for (int i = _carIndex; i < getCarList().size(); i++) {
            if (numberCars == DISPLAY_CAR_LIMIT_100) {
                addLine(FIVE, Bundle.getMessage("buildOnlyFirstXXXCars", numberCars, rl.getName()));
                break;
            }
            Car car = getCarList().get(i);
            // find a car at this location that hasn't been given a destination
            if (!car.getLocationName().equals(rl.getName()) || car.getRouteDestination() != null) {
                continue;
            }
            if (numberCars == 0) {
                addLine(SEVEN,
                        Bundle.getMessage("buildMovesCompleted", rl.getMaxCarMoves(), rl.getName()));
            }
            addLine(SEVEN, Bundle.getMessage("buildCarIgnored", car.toString(), car.getTypeName(),
                    car.getLoadType().toLowerCase(), car.getLoadName(), car.getLocationName(), car.getTrackName()));
            numberCars++;
        }
        addLine(SEVEN, BLANK_LINE);
    }

    /**
     * Remove rolling stock from train
     *
     * @param rs the rolling stock to be removed
     */
    protected void removeRollingStockFromTrain(RollingStock rs) {
        // adjust train length and weight for each location that the rolling
        // stock is in the train
        boolean inTrain = false;
        for (RouteLocation routeLocation : getRouteList()) {
            if (rs.getRouteLocation() == routeLocation) {
                inTrain = true;
            }
            if (rs.getRouteDestination() == routeLocation) {
                break;
            }
            if (inTrain) {
                routeLocation.setTrainLength(routeLocation.getTrainLength() - rs.getTotalLength()); // includes
                                                                                                    // couplers
                routeLocation.setTrainWeight(routeLocation.getTrainWeight() - rs.getAdjustedWeightTons());
            }
        }
        rs.reset(); // remove this rolling stock from the train
    }

    /**
     * Lists cars that couldn't be routed.
     */
    protected void showCarsNotRoutable() {
        // any cars unable to route?
        if (_notRoutable.size() > 0) {
            addLine(ONE, BLANK_LINE);
            addLine(ONE, Bundle.getMessage("buildCarsNotRoutable"));
            for (Car car : _notRoutable) {
                _warnings++;
                addLine(ONE,
                        Bundle.getMessage("buildCarNotRoutable", car.toString(), car.getLocationName(),
                                car.getTrackName(), car.getPreviousFinalDestinationName(),
                                car.getPreviousFinalDestinationTrackName()));
            }
            addLine(ONE, BLANK_LINE);
        }
    }

    /**
     * build has failed due to cars in staging not having destinations this
     * routine removes those cars from the staging track by user request.
     */
    protected void removeCarsFromStaging() {
        // Code check, only called if train was departing staging
        if (getDepartureStagingTrack() == null) {
            log.error("Error, called when cars in staging not assigned to train");
            return;
        }
        for (Car car : getCarList()) {
            // remove cars from departure staging track that haven't been
            // assigned to this train
            if (car.getTrack() == getDepartureStagingTrack() && car.getTrain() == null) {
                // remove track from kernel
                if (car.getKernel() != null) {
                    for (Car c : car.getKernel().getCars())
                        c.setLocation(car.getLocation(), null);
                } else {
                    car.setLocation(car.getLocation(), null);
                }
            }
        }
    }

    protected int countRollingStockAt(RouteLocation rl, List<RollingStock> list) {
        int count = 0;
        for (RollingStock rs : list) {
            if (rs.getLocationName().equals(rl.getName())) {
                count++;
            }
        }
        return count;
    }

    /*
     * lists the tracks that aren't in quick service mode
     */
    protected void showTracksNotQuickService() {
        if (Setup.isBuildOnTime()) {
            addLine(FIVE, Bundle.getMessage("buildTracksNotQuickService"));
            for (Track track : locationManager.getTracks(null)) {
                if (!track.isQuickServiceEnabled()) {
                    addLine(SEVEN, Bundle.getMessage("buildTrackNotQuick",
                            StringUtils.capitalize(track.getTrackTypeName()), track.getLocation().getName(),
                            track.getName()));
                }
            }
            addLine(FIVE, BLANK_LINE);
        }
    }

    /**
     * 
     * Checks to see if rolling stock is departing a quick service track and is
     * allowed to be pulled by this train. To pull, the route location must be
     * different than the one used to deliver the rolling stock. To service the
     * rolling stock, the train must arrive after the rolling stock's clone is
     * set out by this train or by another train.
     * @param rs the rolling stock
     * @param rl the route location pulling the rolling stock
     * @return true if rolling stock can be pulled
     */
    protected boolean checkQuickServiceDeparting(RollingStock rs, RouteLocation rl) {
        if (rs.getTrack().isQuickServiceEnabled()) {
            RollingStock clone = null;
            if (Car.class.isInstance(rs)) {
                clone = carManager.getClone(rs);
            }
            if (Engine.class.isInstance(rs)) {
                clone = engineManager.getClone(rs);
            }
            if (clone != null) {
                // was the rolling stock delivered using this route location?
                if (rs.getRouteDestination() == rl) {
                    addLine(FIVE,
                            Bundle.getMessage("buildRouteLocation", rs.toString(),
                                    rs.getTrack().getTrackTypeName(),
                                    rs.getLocationName(), rs.getTrackName(), getTrain().getName(), rl.getName(),
                                    rl.getId()));
                    addLine(FIVE, BLANK_LINE);
                    return false;
                }

                // determine when the train arrives
                String trainExpectedArrival = getTrain().getExpectedArrivalTime(rl, true);
                int trainArrivalTimeMinutes = convertStringTime(trainExpectedArrival);
                // determine when the clone is going to be delivered
                int cloneSetoutTimeMinutes = convertStringTime(clone.getSetoutTime());
                // in aggressive mode the dwell time is 0
                int dwellTime = 0;
                if (Setup.isBuildOnTime()) {
                    dwellTime = Setup.getDwellTime();
                }
                if (cloneSetoutTimeMinutes + dwellTime > trainArrivalTimeMinutes) {
                    String earliest = convertMinutesTime(cloneSetoutTimeMinutes + dwellTime);
                    addLine(FIVE, Bundle.getMessage("buildDeliveryTiming", rs.toString(),
                            clone.getSetoutTime(), rs.getTrack().getTrackTypeName(), rs.getLocationName(),
                            rs.getTrackName(), clone.getTrainName(), getTrain().getName(), trainExpectedArrival,
                            dwellTime, earliest));
                    addLine(FIVE, BLANK_LINE);
                    return false;
                } else {
                    addLine(SEVEN, Bundle.getMessage("buildCloneDeliveryTiming", clone.toString(),
                            clone.getSetoutTime(), rs.getTrack().getTrackTypeName(), rs.getLocationName(),
                            rs.getTrackName(), clone.getTrainName(), getTrain().getName(), trainExpectedArrival,
                            dwellTime, rs.toString()));
                }
            }
        }
        return true;
    }

    /*
     * Engine methods start here
     */

    /**
     * Used to determine the number of engines requested by the user.
     *
     * @param requestEngines Can be a number, AUTO or AUTO HPT.
     * @return the number of engines requested by user.
     */
    protected int getNumberEngines(String requestEngines) {
        int numberEngines = 0;
        if (requestEngines.equals(Train.AUTO)) {
            numberEngines = getAutoEngines();
        } else if (requestEngines.equals(Train.AUTO_HPT)) {
            numberEngines = 1; // get one loco for now, check HP requirements
                               // after train is built
        } else {
            numberEngines = Integer.parseInt(requestEngines);
        }
        return numberEngines;
    }

    /**
     * Returns the number of engines needed for this train, minimum 1, maximum
     * user specified in setup. Based on maximum allowable train length and
     * grade between locations, and the maximum cars that the train can have at
     * the maximum train length. One engine per sixteen 40' cars for 1% grade.
     *
     * @return The number of engines needed
     */
    private int getAutoEngines() {
        double numberEngines = 1;
        int moves = 0;
        int carLength = 40 + Car.COUPLERS; // typical 40' car

        // adjust if length in meters
        if (!Setup.getLengthUnit().equals(Setup.FEET)) {
            carLength = 12 + Car.COUPLERS; // typical car in meters
        }

        for (RouteLocation rl : getRouteList()) {
            if (rl.isPickUpAllowed() && rl != getTrain().getTrainTerminatesRouteLocation()) {
                moves += rl.getMaxCarMoves(); // assume all moves are pick ups
                double carDivisor = 16; // number of 40' cars per engine 1% grade
                // change engine requirements based on grade
                if (rl.getGrade() > 1) {
                    carDivisor = carDivisor / rl.getGrade();
                }
                log.debug("Maximum train length {} for location ({})", rl.getMaxTrainLength(), rl.getName());
                if (rl.getMaxTrainLength() / (carDivisor * carLength) > numberEngines) {
                    numberEngines = rl.getMaxTrainLength() / (carDivisor * carLength);
                    // round up to next whole integer
                    numberEngines = Math.ceil(numberEngines);
                    // determine if there's enough car pick ups at this point to
                    // reach the max train length
                    if (numberEngines > moves / carDivisor) {
                        // no reduce based on moves
                        numberEngines = Math.ceil(moves / carDivisor);
                    }
                }
            }
        }
        int nE = (int) numberEngines;
        if (getTrain().isLocalSwitcher()) {
            nE = 1; // only one engine if switcher
        }
        addLine(ONE,
                Bundle.getMessage("buildAutoBuildMsg", Integer.toString(nE)));
        if (nE > Setup.getMaxNumberEngines()) {
            addLine(THREE, Bundle.getMessage("buildMaximumNumberEngines", Setup.getMaxNumberEngines()));
            nE = Setup.getMaxNumberEngines();
        }
        return nE;
    }

    protected void addLine(String level, String string) {
        addLine(getBuildReport(), level, string);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainBuilderBase.class);

}
