package jmri.jmrit.operations.trains;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * Methods to support the TrainBuilder class.
 *
 * @author Daniel Boudreau Copyright (C) 2021
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

    protected void createBuildReportFile() throws BuildFailedException {
        // backup the train's previous build report file
        InstanceManager.getDefault(TrainManagerXml.class).savePreviousBuildStatusFile(_train.getName());

        // create build report file
        File file = InstanceManager.getDefault(TrainManagerXml.class).createTrainBuildReportFile(_train.getName());
        try {
            _buildReport = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)),
                    true);
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
        addLine(_buildReport, ONE, Bundle.getMessage("BuildReportMsg", _train.getName(), _startTime));
        addLine(_buildReport, ONE,
                Bundle.getMessage("BuildReportVersion", Version.name()));
        if (!trainScheduleManager.getTrainScheduleActiveId().equals(TrainScheduleManager.NONE)) {
            if (trainScheduleManager.getTrainScheduleActiveId().equals(TrainSchedule.ANY)) {
                addLine(_buildReport, ONE, Bundle.getMessage("buildActiveSchedule", Bundle.getMessage("Any")));
            } else {
                TrainSchedule sch = trainScheduleManager.getActiveSchedule();
                if (sch != null) {
                    addLine(_buildReport, ONE, Bundle.getMessage("buildActiveSchedule", sch.getName()));
                }
            }
        }
        // show the various build detail levels
        addLine(_buildReport, THREE, Bundle.getMessage("buildReportLevelThree"));
        addLine(_buildReport, FIVE, Bundle.getMessage("buildReportLevelFive"));
        addLine(_buildReport, SEVEN, Bundle.getMessage("buildReportLevelSeven"));

        if (Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED)) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildRouterReportLevelDetailed"));
        } else if (Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED)) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildRouterReportLevelVeryDetailed"));
        }

        if (!Setup.getComment().trim().isEmpty()) {
            addLine(_buildReport, ONE, BLANK_LINE);
            addLine(_buildReport, ONE, Setup.getComment());
        }
        addLine(_buildReport, ONE, BLANK_LINE);
    }

    protected void setUpRoute() throws BuildFailedException {
        if (_train.getRoute() == null) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildErrorRoute", _train.getName()));
        }
        // get the train's route
        _routeList = _train.getRoute().getLocationsBySequenceList();
        if (_routeList.size() < 1) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildErrorNeedRoute", _train.getName()));
        }
        // train departs
        _departLocation = locationManager.getLocationByName(_train.getTrainDepartsName());
        if (_departLocation == null) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildErrorNeedDepLoc", _train.getName()));
        }
        // train terminates
        _terminateLocation = locationManager.getLocationByName(_train.getTrainTerminatesName());
        if (_terminateLocation == null) {
            throw new BuildFailedException(Bundle.getMessage("buildErrorNeedTermLoc", _train.getName()));
        }
    }

    /**
     * show train build options when in detailed mode
     */
    protected void showTrainBuildOptions() {
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
        addLine(_buildReport, FIVE, Bundle.getMessage("MenuItemBuildOptions") + ":");
        if (Setup.isBuildAggressive()) {
            addLine(_buildReport, FIVE, Bundle.getMessage("BuildModeAggressive"));
            addLine(_buildReport, FIVE, Bundle.getMessage("BuildNumberPasses", Setup.getNumberPasses()));
            if (Setup.isStagingTrackImmediatelyAvail() && _departLocation.isStaging()) {
                addLine(_buildReport, FIVE, Bundle.getMessage("BuildStagingTrackAvail"));
            }
        } else {
            addLine(_buildReport, FIVE, Bundle.getMessage("BuildModeNormal"));
        }
        // show switcher options
        if (_train.isLocalSwitcher()) {
            addLine(_buildReport, FIVE, BLANK_LINE);
            addLine(_buildReport, FIVE, rb.getString("BorderLayoutSwitcherService") + ":");
            if (Setup.isLocalInterchangeMovesEnabled()) {
                addLine(_buildReport, FIVE, rb.getString("AllowLocalInterchange"));
            } else {
                addLine(_buildReport, FIVE, rb.getString("NoAllowLocalInterchange"));
            }
            if (Setup.isLocalSpurMovesEnabled()) {
                addLine(_buildReport, FIVE, rb.getString("AllowLocalSpur"));
            } else {
                addLine(_buildReport, FIVE, rb.getString("NoAllowLocalSpur"));
            }
            if (Setup.isLocalYardMovesEnabled()) {
                addLine(_buildReport, FIVE, rb.getString("AllowLocalYard"));
            } else {
                addLine(_buildReport, FIVE, rb.getString("NoAllowLocalYard"));
            }
        }
        // show staging options
        if (_departLocation.isStaging() || _terminateLocation.isStaging()) {
            addLine(_buildReport, FIVE, BLANK_LINE);
            addLine(_buildReport, FIVE, Bundle.getMessage("buildStagingOptions"));

            if (Setup.isStagingTrainCheckEnabled() && _terminateLocation.isStaging()) {
                addLine(_buildReport, FIVE, Bundle.getMessage("buildOptionRestrictStaging"));
            }
            if (Setup.isStagingTrackImmediatelyAvail() && _terminateLocation.isStaging()) {
                addLine(_buildReport, FIVE, rb.getString("StagingAvailable"));
            }
            if (Setup.isStagingAllowReturnEnabled() &&
                    _departLocation.isStaging() &&
                    _terminateLocation.isStaging() &&
                    _departLocation == _terminateLocation) {
                addLine(_buildReport, FIVE, rb.getString("AllowCarsToReturn"));
            }
            if (Setup.isStagingPromptFromEnabled() && _departLocation.isStaging()) {
                addLine(_buildReport, FIVE, rb.getString("PromptFromStaging"));
            }
            if (Setup.isStagingPromptToEnabled() && _terminateLocation.isStaging()) {
                addLine(_buildReport, FIVE, rb.getString("PromptToStaging"));
            }
            if (Setup.isStagingTryNormalBuildEnabled() && _departLocation.isStaging()) {
                addLine(_buildReport, FIVE, rb.getString("TryNormalStaging"));
            }
        }

        // Car routing options
        addLine(_buildReport, FIVE, BLANK_LINE);
        addLine(_buildReport, FIVE, Bundle.getMessage("buildCarRoutingOptions"));

        // warn if car routing is disabled
        if (!Setup.isCarRoutingEnabled()) {
            addLine(_buildReport, FIVE, Bundle.getMessage("RoutingDisabled"));
            _warnings++;
        } else {
            if (Setup.isCarRoutingViaYardsEnabled()) {
                addLine(_buildReport, FIVE, Bundle.getMessage("RoutingViaYardsEnabled"));
            }
            if (Setup.isCarRoutingViaStagingEnabled()) {
                addLine(_buildReport, FIVE, Bundle.getMessage("RoutingViaStagingEnabled"));
            }
            if (Setup.isOnlyActiveTrainsEnabled()) {
                addLine(_buildReport, FIVE, Bundle.getMessage("OnlySelectedTrains"));
                _warnings++;
                // list the selected trains
                for (Train train : trainManager.getTrainsByNameList()) {
                    if (train.isBuildEnabled()) {
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("buildTrainNameAndDesc", train.getName(), train.getDescription()));
                    }
                }
                if (!_train.isBuildEnabled()) {
                    addLine(_buildReport, FIVE, Bundle.getMessage("buildTrainNotSelected", _train.getName()));
                }
            } else {
                addLine(_buildReport, FIVE, rb.getString("AllTrains"));
            }
            if (Setup.isCheckCarDestinationEnabled()) {
                addLine(_buildReport, FIVE, Bundle.getMessage("CheckCarDestination"));
            }
        }
        addLine(_buildReport, FIVE, BLANK_LINE);
    }

    /*
     * Show the enabled and disabled build options for this train.
     */
    protected void showSpecificTrainBuildOptions() {
        addLine(_buildReport, FIVE,
                Bundle.getMessage("buildOptionsForTrain", _train.getName()));
        showSpecificTrainBuildOptions(true);
        addLine(_buildReport, FIVE, Bundle.getMessage("buildDisabledOptionsForTrain", _train.getName()));
        showSpecificTrainBuildOptions(false);
    }

    /*
     * Enabled when true lists selected build options for this train. Enabled
     * when false list disabled build options for this train.
     */
    private void showSpecificTrainBuildOptions(boolean enabled) {

        if (_train.isBuildTrainNormalEnabled() ^ !enabled) {
            addLine(_buildReport, FIVE, Bundle.getMessage("NormalModeWhenBuilding"));
        }
        if (_train.isSendCarsToTerminalEnabled() ^ !enabled) {
            addLine(_buildReport, FIVE, Bundle.getMessage("SendToTerminal", _terminateLocation.getName()));
        }
        if ((_train.isAllowReturnToStagingEnabled() || Setup.isStagingAllowReturnEnabled()) ^ !enabled &&
                _departLocation.isStaging() &&
                _departLocation == _terminateLocation) {
            addLine(_buildReport, FIVE, Bundle.getMessage("AllowCarsToReturn"));
        }
        if (_train.isAllowLocalMovesEnabled() ^ !enabled) {
            addLine(_buildReport, FIVE, Bundle.getMessage("AllowLocalMoves"));
        }
        if (_train.isAllowThroughCarsEnabled() ^ !enabled && _departLocation != _terminateLocation) {
            addLine(_buildReport, FIVE, Bundle.getMessage("AllowThroughCars"));
        }
        if (_train.isServiceAllCarsWithFinalDestinationsEnabled() ^ !enabled) {
            addLine(_buildReport, FIVE, Bundle.getMessage("ServiceAllCars"));
        }
        if (_train.isSendCarsWithCustomLoadsToStagingEnabled() ^ !enabled) {
            addLine(_buildReport, FIVE, Bundle.getMessage("SendCustomToStaging"));
        }
        if (_train.isBuildConsistEnabled() ^ !enabled) {
            addLine(_buildReport, FIVE, Bundle.getMessage("BuildConsist"));
            if (enabled) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("BuildConsistHPT", Setup.getHorsePowerPerTon()));
            }
        }
        addLine(_buildReport, FIVE, BLANK_LINE);
    }

    /**
     * Adds to the build report what the train will service. Road and owner
     * names, built dates, and engine types.
     */
    protected void showTrainServices() {
        // show road names that this train will service
        if (!_train.getLocoRoadOption().equals(Train.ALL_ROADS)) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildTrainLocoRoads", _train.getName(),
                    _train.getLocoRoadOption(), formatStringToCommaSeparated(_train.getLocoRoadNames())));
        }
        // show owner names that this train will service
        if (!_train.getOwnerOption().equals(Train.ALL_OWNERS)) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildTrainOwners", _train.getName(), _train.getOwnerOption(),
                    formatStringToCommaSeparated(_train.getOwnerNames())));
        }
        // show built dates serviced
        if (!_train.getBuiltStartYear().equals(Train.NONE)) {
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildTrainBuiltAfter", _train.getName(), _train.getBuiltStartYear()));
        }
        if (!_train.getBuiltEndYear().equals(Train.NONE)) {
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildTrainBuiltBefore", _train.getName(), _train.getBuiltEndYear()));
        }

        // show engine types that this train will service
        if (!_train.getNumberEngines().equals("0")) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildTrainServicesEngineTypes", _train.getName()));
            addLine(_buildReport, FIVE, formatStringToCommaSeparated(_train.getLocoTypeNames()));
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

        addLine(_buildReport, THREE,
                Bundle.getMessage("buildTrainRoute", _train.getName(), _train.getRoute().getName()));

        // get the number of requested car moves for this train
        for (RouteLocation rl : _routeList) {
            // check to see if there's a location for each stop in the route
            // this checks for a deleted location
            Location location = locationManager.getLocationByName(rl.getName());
            if (location == null || rl.getLocation() == null) {
                throw new BuildFailedException(Bundle.getMessage("buildErrorLocMissing", _train.getRoute().getName()));
            }
            // train doesn't drop or pick up cars from staging locations found
            // in middle of a route
            if (location.isStaging() &&
                    rl != _train.getTrainDepartsRouteLocation() &&
                    rl != _train.getTrainTerminatesRouteLocation()) {
                addLine(_buildReport, ONE,
                        Bundle.getMessage("buildLocStaging", rl.getName()));
                rl.setCarMoves(rl.getMaxCarMoves()); // don't allow car moves
                                                     // for this location
                // if a location is skipped, no car drops or pick ups
            } else if (_train.isLocationSkipped(rl.getId())) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildLocSkippedMaxTrain", rl.getId(), rl.getName(),
                                rl.getTrainDirectionString(), _train.getName(), rl.getMaxTrainLength(),
                                Setup.getLengthUnit().toLowerCase()));
                rl.setCarMoves(rl.getMaxCarMoves()); // don't allow car moves
                                                     // for this location
            } else if (!rl.isDropAllowed() && !rl.isPickUpAllowed()) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildLocNoDropsOrPickups", rl.getId(), rl.getName(),
                                rl.getTrainDirectionString(), rl.getMaxTrainLength(),
                                Setup.getLengthUnit().toLowerCase()));
                rl.setCarMoves(rl.getMaxCarMoves()); // don't allow car moves
                                                     // for this location
            } else {
                // we're going to use this location, so initialize the route
                // location
                rl.setCarMoves(0); // clear the number of moves
                requestedCarMoves += rl.getMaxCarMoves(); // add up the total
                                                          // number of car moves
                                                          // requested
                // show the type of moves allowed at this location
                if (location.isStaging() && rl.isPickUpAllowed() && rl == _train.getTrainDepartsRouteLocation()) {
                    addLine(_buildReport, THREE,
                            Bundle.getMessage("buildStagingDeparts", rl.getId(), rl.getName(),
                                    rl.getTrainDirectionString(), rl.getMaxCarMoves(), rl.getMaxTrainLength(),
                                    Setup.getLengthUnit().toLowerCase()));
                } else if (location.isStaging() &&
                        rl.isDropAllowed() &&
                        rl == _train.getTrainTerminatesRouteLocation()) {
                    addLine(_buildReport, THREE, Bundle.getMessage("buildStagingTerminates", rl.getId(), rl.getName(),
                            rl.getTrainDirectionString(), rl.getMaxCarMoves()));
                } else if (rl == _train.getTrainTerminatesRouteLocation() &&
                        rl.isDropAllowed() &&
                        rl.isPickUpAllowed()) {
                    addLine(_buildReport, THREE, Bundle.getMessage("buildLocTerminatesMoves", rl.getId(), rl.getName(),
                            rl.getTrainDirectionString(), rl.getMaxCarMoves()));
                } else if (rl.isDropAllowed() && rl.isPickUpAllowed()) {
                    addLine(_buildReport, THREE,
                            Bundle.getMessage("buildLocRequestMoves", rl.getId(), rl.getName(),
                                    rl.getTrainDirectionString(), rl.getMaxCarMoves(), rl.getMaxTrainLength(),
                                    Setup.getLengthUnit().toLowerCase()));
                } else if (!rl.isDropAllowed()) {
                    addLine(_buildReport, THREE,
                            Bundle.getMessage("buildLocRequestPickups", rl.getId(), rl.getName(),
                                    rl.getTrainDirectionString(), rl.getMaxCarMoves(), rl.getMaxTrainLength(),
                                    Setup.getLengthUnit().toLowerCase()));
                } else if (rl == _train.getTrainTerminatesRouteLocation()) {
                    addLine(_buildReport, THREE, Bundle.getMessage("buildLocTerminates", rl.getId(), rl.getName(),
                            rl.getTrainDirectionString(), rl.getMaxCarMoves()));
                } else {
                    addLine(_buildReport, THREE,
                            Bundle.getMessage("buildLocRequestDrops", rl.getId(), rl.getName(),
                                    rl.getTrainDirectionString(), rl.getMaxCarMoves(), rl.getMaxTrainLength(),
                                    Setup.getLengthUnit().toLowerCase()));
                }
            }
            rl.setTrainWeight(0); // clear the total train weight
            rl.setTrainLength(0); // and length
        }

        // check for random moves in the train's route
        for (RouteLocation rl : _routeList) {
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
                    addLine(_buildReport, FIVE,
                            Bundle.getMessage("buildRouteRandomControl", rl.getName(), rl.getId(),
                                    rl.getRandomControl(), rl.getMaxCarMoves(), rl.getMaxCarMoves() - moves));
                } catch (NumberFormatException e) {
                    throw new BuildFailedException(Bundle.getMessage("buildErrorRandomControl",
                            _train.getRoute().getName(), rl.getName(), rl.getRandomControl()));
                }
            }
        }

        int numMoves = requestedCarMoves; // number of car moves
        if (!_train.isLocalSwitcher()) {
            requestedCarMoves = requestedCarMoves / 2; // only need half as many
                                                       // cars to meet requests
        }
        addLine(_buildReport, ONE, Bundle.getMessage("buildRouteRequest", _train.getRoute().getName(),
                Integer.toString(requestedCarMoves), Integer.toString(numMoves)));

        _train.setNumberCarsRequested(requestedCarMoves); // save number of car
                                                          // moves requested
        addLine(_buildReport, ONE, BLANK_LINE);
    }

    /**
     * reports if local switcher
     */
    protected void showIfLocalSwitcher() {
        if (_train.isLocalSwitcher()) {
            addLine(_buildReport, THREE, Bundle.getMessage("buildTrainIsSwitcher", _train.getName(),
                    TrainCommon.splitString(_train.getTrainDepartsName())));
            addLine(_buildReport, THREE, BLANK_LINE);
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
        addLine(_buildReport, ONE, Bundle.getMessage("TrainRequirements"));
        if (_train.isBuildConsistEnabled() && Setup.getHorsePowerPerTon() > 0) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildTrainReqConsist", Setup.getHorsePowerPerTon(), _train.getNumberEngines()));
        } else if (_train.getNumberEngines().equals("0")) {
                addLine(_buildReport, ONE, Bundle.getMessage("buildTrainReq0Engine"));
        } else if (_train.getNumberEngines().equals("1")) {
            addLine(_buildReport, ONE, Bundle.getMessage("buildTrainReq1Engine", _train.getTrainDepartsName(),
                    _train.getEngineModel(), _train.getEngineRoad()));
        } else {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildTrainReqEngine", _train.getTrainDepartsName(), _train.getNumberEngines(),
                            _train.getEngineModel(), _train.getEngineRoad()));
        }
        // show any required loco changes
        if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildTrainEngineChange", _train.getSecondLegStartLocationName(),
                            _train.getSecondLegNumberEngines(), _train.getSecondLegEngineModel(),
                            _train.getSecondLegEngineRoad()));
        }
        if ((_train.getSecondLegOptions() & Train.ADD_ENGINES) == Train.ADD_ENGINES) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildTrainAddEngines", _train.getSecondLegNumberEngines(),
                            _train.getSecondLegStartLocationName(), _train.getSecondLegEngineModel(),
                            _train.getSecondLegEngineRoad()));
        }
        if ((_train.getSecondLegOptions() & Train.REMOVE_ENGINES) == Train.REMOVE_ENGINES) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildTrainRemoveEngines", _train.getSecondLegNumberEngines(),
                            _train.getSecondLegStartLocationName(), _train.getSecondLegEngineModel(),
                            _train.getSecondLegEngineRoad()));
        }
        if ((_train.getSecondLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildTrainHelperEngines", _train.getSecondLegNumberEngines(),
                            _train.getSecondLegStartLocationName(), _train.getSecondLegEndLocationName(),
                            _train.getSecondLegEngineModel(), _train.getSecondLegEngineRoad()));
        }

        if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildTrainEngineChange", _train.getThirdLegStartLocationName(),
                            _train.getThirdLegNumberEngines(), _train.getThirdLegEngineModel(),
                            _train.getThirdLegEngineRoad()));
        }
        if ((_train.getThirdLegOptions() & Train.ADD_ENGINES) == Train.ADD_ENGINES) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildTrainAddEngines", _train.getThirdLegNumberEngines(),
                            _train.getThirdLegStartLocationName(), _train.getThirdLegEngineModel(),
                            _train.getThirdLegEngineRoad()));
        }
        if ((_train.getThirdLegOptions() & Train.REMOVE_ENGINES) == Train.REMOVE_ENGINES) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildTrainRemoveEngines", _train.getThirdLegNumberEngines(),
                            _train.getThirdLegStartLocationName(), _train.getThirdLegEngineModel(),
                            _train.getThirdLegEngineRoad()));
        }
        if ((_train.getThirdLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildTrainHelperEngines", _train.getThirdLegNumberEngines(),
                            _train.getThirdLegStartLocationName(), _train.getThirdLegEndLocationName(),
                            _train.getThirdLegEngineModel(), _train.getThirdLegEngineRoad()));
        }
        // show caboose or FRED requirements
        if (_train.isCabooseNeeded()) {
            addLine(_buildReport, ONE, Bundle.getMessage("buildTrainRequiresCaboose", _train.getTrainDepartsName(),
                    _train.getCabooseRoad()));
        }
        // show any caboose changes in the train's route
        if ((_train.getSecondLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (_train.getSecondLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildCabooseChange", _train.getSecondLegStartRouteLocation()));
        }
        if ((_train.getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (_train.getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            addLine(_buildReport, ONE, Bundle.getMessage("buildCabooseChange", _train.getThirdLegStartRouteLocation()));
        }
        if (_train.isFredNeeded()) {
            addLine(_buildReport, ONE,
                    Bundle.getMessage("buildTrainRequiresFRED", _train.getTrainDepartsName(), _train.getCabooseRoad()));
        }
        addLine(_buildReport, ONE, BLANK_LINE);
    }

    /**
     * Will also set the termination track if returning to staging
     *
     * @param departStageTrack departure track from staging
     */
    protected void setDepartureTrack(Track departStageTrack) {
        if ((_terminateStageTrack == null || _terminateStageTrack == _departStageTrack) &&
                _departLocation == _terminateLocation &&
                Setup.isBuildAggressive() &&
                Setup.isStagingTrackImmediatelyAvail()) {
            _terminateStageTrack = departStageTrack; // use the same track
        }
        _departStageTrack = departStageTrack;
    }

    protected void showTrainCarRoads() {
        if (!_train.getCarRoadOption().equals(Train.ALL_ROADS)) {
            addLine(_buildReport, FIVE, BLANK_LINE);
            addLine(_buildReport, FIVE, Bundle.getMessage("buildTrainRoads", _train.getName(),
                    _train.getCarRoadOption(), formatStringToCommaSeparated(_train.getCarRoadNames())));
        }
    }

    protected void showTrainCabooseRoads() {
        if (!_train.getCabooseRoadOption().equals(Train.ALL_ROADS)) {
            addLine(_buildReport, FIVE, BLANK_LINE);
            addLine(_buildReport, FIVE, Bundle.getMessage("buildTrainCabooseRoads", _train.getName(),
                    _train.getCabooseRoadOption(), formatStringToCommaSeparated(_train.getCabooseRoadNames())));
        }
    }

    protected void showTrainCarTypes() {
        addLine(_buildReport, FIVE, BLANK_LINE);
        addLine(_buildReport, FIVE, Bundle.getMessage("buildTrainServicesCarTypes", _train.getName()));
        addLine(_buildReport, FIVE, formatStringToCommaSeparated(_train.getCarTypeNames()));
    }

    protected void showTrainLoadNames() {
        if (!_train.getLoadOption().equals(Train.ALL_LOADS)) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildTrainLoads", _train.getName(), _train.getLoadOption(),
                    formatStringToCommaSeparated(_train.getLoadNames())));
        }
    }

    /**
     * Ask which staging track the train is to depart on.
     *
     * @return The departure track the user selected.
     */
    protected Track promptFromStagingDialog() {
        List<Track> tracksIn = _departLocation.getTracksByNameList(null);
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
                    Bundle.getMessage("TrainDepartingStaging", _train.getName(), _departLocation.getName()),
                    Bundle.getMessage("SelectDepartureTrack"), JmriJOptionPane.QUESTION_MESSAGE, null, tracks, null);
            if (selected != null) {
                addLine(_buildReport, FIVE, Bundle.getMessage("buildUserSelectedDeparture", selected.getName(),
                        selected.getLocation().getName()));
            }
            return selected;
        } else if (validTracks.size() == 1) {
            Track track = validTracks.get(0);
            addLine(_buildReport, FIVE,
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
        List<Track> tracksIn = _terminateLocation.getTracksByNameList(null);
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
                    Bundle.getMessage("TrainTerminatingStaging", _train.getName(), _terminateLocation.getName()),
                    Bundle.getMessage("SelectArrivalTrack"), JmriJOptionPane.QUESTION_MESSAGE, null, tracks, null);
            if (selected != null) {
                addLine(_buildReport, FIVE, Bundle.getMessage("buildUserSelectedArrival", selected.getName(),
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
        addLine(_buildReport, SEVEN, BLANK_LINE);
        addLine(_buildReport, SEVEN, Bundle.getMessage("buildRemoveCarsNotNeeded"));
        for (int i = 0; i < _carList.size(); i++) {
            Car car = _carList.get(i);
            if (car.isCaboose() || car.hasFred()) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildExcludeCarTypeAtLoc", car.toString(), car.getTypeName(),
                                car.getTypeExtensions(), car.getLocationName(), car.getTrackName()));
                // code check, should never be staging
                if (car.getTrack() == _departStageTrack) {
                    throw new BuildFailedException("ERROR: Attempt to removed car with FRED or Caboose from staging"); // NOI18N
                }
                _carList.remove(car); // remove this car from the list
                i--;
            }
        }
    }

    /**
     * Save the car's final destination and schedule,  id in case of train reset
     */
    protected void saveCarFinalDestinations() {
        for (Car car : _carList) {
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
    protected void getCarList() throws BuildFailedException {
        // get list of cars for this route
        _carList = carManager.getAvailableTrainList(_train);
        addLine(_buildReport, SEVEN, BLANK_LINE);
        addLine(_buildReport, SEVEN, Bundle.getMessage("buildRemoveCars"));
        boolean showCar = true;
        int carListSize = _carList.size();
        // now remove cars that the train can't service
        for (int i = 0; i < _carList.size(); i++) {
            Car car = _carList.get(i);
            // only show the first 100 cars removed due to wrong car type for
            // train
            if (showCar && carListSize - _carList.size() == DISPLAY_CAR_LIMIT_100) {
                showCar = false;
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildOnlyFirstXXXCars", DISPLAY_CAR_LIMIT_100, Bundle.getMessage("Type")));
            }
            // remove cars that don't have a track assignment
            if (car.getTrack() == null) {
                _warnings++;
                addLine(_buildReport, ONE,
                        Bundle.getMessage("buildWarningRsNoTrack", car.toString(), car.getLocationName()));
                _carList.remove(car);
                i--;
                continue;
            }
            // remove cars that have been reported as missing
            if (car.isLocationUnknown()) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeCarLocUnknown", car.toString(),
                        car.getLocationName(), car.getTrackName()));
                if (car.getTrack().equals(_departStageTrack)) {
                    throw new BuildFailedException(Bundle.getMessage("buildErrorLocationUnknown", car.getLocationName(),
                            car.getTrackName(), car.toString()));
                }
                _carList.remove(car);
                i--;
                continue;
            }
            // remove cars that are out of service
            if (car.isOutOfService()) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeCarOutOfService", car.toString(),
                        car.getLocationName(), car.getTrackName()));
                if (car.getTrack().equals(_departStageTrack)) {
                    throw new BuildFailedException(
                            Bundle.getMessage("buildErrorLocationOutOfService", car.getLocationName(),
                                    car.getTrackName(), car.toString()));
                }
                _carList.remove(car);
                i--;
                continue;
            }
            // does car have a destination that is part of this train's route?
            if (car.getDestination() != null) {
                RouteLocation rld = _train.getRoute().getLastLocationByName(car.getDestinationName());
                if (rld == null) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeCarDestNotPartRoute", car.toString(),
                            car.getDestinationName(), car.getDestinationTrackName(), _train.getRoute().getName()));
                    // Code check, programming ERROR if car departing staging
                    if (car.getLocation().equals(_departLocation) && _departStageTrack != null) {
                        throw new BuildFailedException(Bundle.getMessage("buildErrorCarNotPartRoute", car.toString()));
                    }
                    _carList.remove(car); // remove this car from the list
                    i--;
                    continue;
                }
            }
            // remove cars with FRED that have a destination that isn't the
            // terminal
            if (car.hasFred() && car.getDestination() != null && car.getDestination() != _terminateLocation) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildExcludeCarWrongDest", car.toString(), car.getTypeName(),
                                car.getTypeExtensions(), car.getDestinationName()));
                _carList.remove(car);
                i--;
                continue;
            }

            // remove cabooses that have a destination that isn't the terminal,
            // no caboose
            // changes in the train's route
            if (car.isCaboose() &&
                    car.getDestination() != null &&
                    car.getDestination() != _terminateLocation &&
                    (_train.getSecondLegOptions() & Train.ADD_CABOOSE + Train.REMOVE_CABOOSE) == 0 &&
                    (_train.getThirdLegOptions() & Train.ADD_CABOOSE + Train.REMOVE_CABOOSE) == 0) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildExcludeCarWrongDest", car.toString(), car.getTypeName(),
                                car.getTypeExtensions(), car.getDestinationName()));
                _carList.remove(car);
                i--;
                continue;
            }

            // is car at interchange?
            if (car.getTrack().isInterchange()) {
                // don't service a car at interchange and has been dropped off
                // by this train
                if (car.getTrack().getPickupOption().equals(Track.ANY) &&
                        car.getLastRouteId().equals(_train.getRoute().getId())) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeCarDropByTrain", car.toString(),
                            car.getTypeName(), _train.getRoute().getName(), car.getLocationName(), car.getTrackName()));
                    _carList.remove(car);
                    i--;
                    continue;
                }
            }
            // is car at interchange or spur and is this train allowed to pull?
            if (car.getTrack().isInterchange() || car.getTrack().isSpur()) {
                if (car.getTrack().getPickupOption().equals(Track.TRAINS) ||
                        car.getTrack().getPickupOption().equals(Track.EXCLUDE_TRAINS)) {
                    if (car.getTrack().isPickupTrainAccepted(_train)) {
                        log.debug("Car ({}) can be picked up by this train", car.toString());
                    } else {
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("buildExcludeCarByTrain", car.toString(), car.getTypeName(),
                                        car.getTrack().getTrackTypeName(), car.getLocationName(), car.getTrackName()));
                        _carList.remove(car);
                        i--;
                        continue;
                    }
                } else if (car.getTrack().getPickupOption().equals(Track.ROUTES) ||
                        car.getTrack().getPickupOption().equals(Track.EXCLUDE_ROUTES)) {
                    if (car.getTrack().isPickupRouteAccepted(_train.getRoute())) {
                        log.debug("Car ({}) can be picked up by this route", car.toString());
                    } else {
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("buildExcludeCarByRoute", car.toString(), car.getTypeName(),
                                        car.getTrack().getTrackTypeName(), car.getLocationName(), car.getTrackName()));
                        _carList.remove(car);
                        i--;
                        continue;
                    }
                }
            }

            // note that for trains departing staging the engine and car roads,
            // types, owners, and built date were already checked.

            // non-lead cars in a kernel are not checked
            if (car.getKernel() == null || car.isLead()) {
                if (!car.isCaboose() && !_train.isCarRoadNameAccepted(car.getRoadName()) ||
                        car.isCaboose() && !_train.isCabooseRoadNameAccepted(car.getRoadName())) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeCarWrongRoad", car.toString(),
                            car.getLocationName(), car.getTrackName(), car.getTypeName(), car.getTypeExtensions(),
                            car.getRoadName()));
                    _carList.remove(car);
                    i--;
                    continue;
                }
                if (!_train.isTypeNameAccepted(car.getTypeName())) {
                    if (showCar) {
                        addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeCarWrongType", car.toString(),
                                car.getLocationName(), car.getTrackName(), car.getTypeName()));
                    }
                    _carList.remove(car);
                    i--;
                    continue;
                }
                if (!_train.isOwnerNameAccepted(car.getOwnerName())) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildExcludeCarOwnerAtLoc", car.toString(), car.getOwnerName(),
                                    car.getLocationName(), car.getTrackName()));
                    _carList.remove(car);
                    i--;
                    continue;
                }
                if (!_train.isBuiltDateAccepted(car.getBuilt())) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildExcludeCarBuiltAtLoc", car.toString(), car.getBuilt(),
                                    car.getLocationName(), car.getTrackName()));
                    _carList.remove(car);
                    i--;
                    continue;
                }
            }

            // all cars in staging must be accepted, so don't exclude if in
            // staging
            // note that a car's load can change when departing staging
            // a car's wait value is ignored when departing staging
            // a car's pick up day is ignored when departing staging
            if (_departStageTrack == null || car.getTrack() != _departStageTrack) {
                if (!car.isCaboose() &&
                        !car.isPassenger() &&
                        !_train.isLoadNameAccepted(car.getLoadName(), car.getTypeName())) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeCarLoadAtLoc", car.toString(),
                            car.getTypeName(), car.getLoadName()));
                    _carList.remove(car);
                    i--;
                    continue;
                }
                // remove cars with FRED if not needed by train
                if (car.hasFred() && !_train.isFredNeeded()) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeCarWithFredAtLoc", car.toString(),
                            car.getTypeName(), (car.getLocationName() + ", " + car.getTrackName())));
                    _carList.remove(car); // remove this car from the list
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
                            addLine(_buildReport, SEVEN,
                                    Bundle.getMessage("buildExcludeCarSchedule", car.toString(), car.getTypeName(),
                                            car.getLocationName(), car.getTrackName(), sch.getName()));
                            _carList.remove(car);
                            i--;
                            continue;
                        }
                    }
                }
                // does car have a wait count?
                if (car.getWait() > 0) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeCarWait", car.toString(),
                            car.getTypeName(), car.getLocationName(), car.getTrackName(), car.getWait()));
                    if (_train.isServiceable(car)) {
                        addLine(_buildReport, SEVEN, Bundle.getMessage("buildTrainCanServiceWait", _train.getName(),
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
                            addLine(_buildReport, SEVEN, Bundle.getMessage("buildCarLoadChangedWait", car.toString(),
                                    car.getTypeName(), oldLoad, newLoad));
                        }
                    }
                    _carList.remove(car);
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
        if (!_train.isDepartingStaging()) {
            return; // not departing staging
        }
        int numCarsFromStaging = 0;
        _numOfBlocks = new Hashtable<>();
        addLine(_buildReport, SEVEN, BLANK_LINE);
        addLine(_buildReport, SEVEN, Bundle.getMessage("buildRemoveCarsStaging"));
        for (int i = 0; i < _carList.size(); i++) {
            Car car = _carList.get(i);
            if (car.getLocationName().equals(_departLocation.getName())) {
                if (car.getTrackName().equals(_departStageTrack.getName())) {
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
                    addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeCarAtLoc", car.toString(),
                            car.getTypeName(), car.getLocationName(), car.getTrackName()));
                    _carList.remove(car);
                    i--;
                }
            }
        }
        // show how many cars are departing from staging
        addLine(_buildReport, FIVE, BLANK_LINE);
        addLine(_buildReport, FIVE, Bundle.getMessage("buildDepartingStagingCars",
                _departStageTrack.getLocation().getName(), _departStageTrack.getName(), numCarsFromStaging));
        // and list them
        for (Car car : _carList) {
            if (car.getTrack() == _departStageTrack) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildStagingCarAtLoc", car.toString(),
                        car.getTypeName(), car.getLoadType().toLowerCase(), car.getLoadName()));
            }
        }
        // error if all of the cars from staging aren't available
        if (numCarsFromStaging != _departStageTrack.getNumberCars()) {
            throw new BuildFailedException(Bundle.getMessage("buildErrorNotAllCars", _departStageTrack.getName(),
                    Integer.toString(_departStageTrack.getNumberCars() - numCarsFromStaging)));
        }
        log.debug("Staging departure track ({}) has {} cars and {} blocks", _departStageTrack.getName(),
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
        addLine(_buildReport, FIVE, BLANK_LINE);
        addLine(_buildReport, ONE,
                Bundle.getMessage("buildFoundCars", Integer.toString(_carList.size()), _train.getName()));
        // only show cars once using the train's route
        List<String> locationNames = new ArrayList<>();
        for (RouteLocation rl : _train.getRoute().getLocationsBySequenceList()) {
            if (locationNames.contains(rl.getName())) {
                continue;
            }
            locationNames.add(rl.getName());
            int count = countRollingStockAt(rl, new ArrayList<RollingStock>(_carList));
            if (rl.getLocation().isStaging()) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildCarsInStaging", count, rl.getName()));
            } else {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildCarsAtLocation", count, rl.getName()));
            }
            // now go through the car list and remove non-lead cars in kernels,
            // destinations
            // that aren't part of this route
            int carCount = 0;
            for (int i = 0; i < _carList.size(); i++) {
                Car car = _carList.get(i);
                if (!car.getLocationName().equals(rl.getName())) {
                    continue;
                }
                // only print out the first DISPLAY_CAR_LIMIT cars for each
                // location
                if (carCount < DISPLAY_CAR_LIMIT_50 && (car.getKernel() == null || car.isLead())) {
                    if (car.getLoadPriority().equals(CarLoad.PRIORITY_LOW)) {
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("buildCarAtLocWithMoves", car.toString(), car.getTypeName(),
                                        car.getTypeExtensions(), car.getLocationName(), car.getTrackName(),
                                        car.getMoves()));
                    } else {
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("buildCarAtLocWithMovesPriority", car.toString(), car.getTypeName(),
                                        car.getTypeExtensions(), car.getLocationName(), car.getTrackName(),
                                        car.getMoves(), car.getLoadType().toLowerCase(), car.getLoadName(),
                                        car.getLoadPriority()));
                    }
                    if (car.isLead()) {
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("buildCarLeadKernel", car.toString(), car.getKernelName(),
                                        car.getKernel().getSize(), car.getKernel().getTotalLength(),
                                        Setup.getLengthUnit().toLowerCase()));
                        // list all of the cars in the kernel now
                        for (Car k : car.getKernel().getCars()) {
                            if (!k.isLead()) {
                                addLine(_buildReport, SEVEN,
                                        Bundle.getMessage("buildCarPartOfKernel", k.toString(), k.getKernelName(),
                                                k.getKernel().getSize(), k.getKernel().getTotalLength(),
                                                Setup.getLengthUnit().toLowerCase()));
                            }
                        }
                    }
                    carCount++;
                    if (carCount == DISPLAY_CAR_LIMIT_50) {
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("buildOnlyFirstXXXCars", carCount, rl.getName()));
                    }
                }
                // use only the lead car in a kernel for building trains
                if (car.getKernel() != null) {
                    checkKernel(car); // kernel needs lead car and all cars on
                                      // the same track
                    if (!car.isLead()) {
                        _carList.remove(car); // remove this car from the list
                        i--;
                        continue;
                    }
                }
                if (_train.equals(car.getTrain())) {
                    addLine(_buildReport, FIVE, Bundle.getMessage("buildCarAlreadyAssigned", car.toString()));
                }
            }
            addLine(_buildReport, SEVEN, BLANK_LINE);
        }
    }

    protected void sortCarsOnFifoLifoTracks() {
        addLine(_buildReport, SEVEN, Bundle.getMessage("buildSortCarsByLastDate"));
        for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
            Car car = _carList.get(_carIndex);
            if (car.getTrack().getServiceOrder().equals(Track.NORMAL) || car.getTrack().isStaging()) {
                continue;
            }
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildTrackModePriority", car.toString(), car.getTrack().getTrackTypeName(),
                            car.getLocationName(), car.getTrackName(), car.getTrack().getServiceOrder(), car.getLastDate()));
            Car bestCar = car;
            for (int i = _carIndex + 1; i < _carList.size(); i++) {
                Car testCar = _carList.get(i);
                if (testCar.getTrack() == car.getTrack()) {
                    log.debug("{} car ({}) last moved date: {}", car.getTrack().getTrackTypeName(), testCar.toString(),
                            testCar.getLastDate()); // NOI18N
                    if (car.getTrack().getServiceOrder().equals(Track.FIFO)) {
                        if (bestCar.getLastMoveDate().after(testCar.getLastMoveDate()) &&
                                bestCar.getLoadPriority().equals(testCar.getLoadPriority())) {
                            bestCar = testCar;
                            log.debug("New best car ({})", bestCar.toString());
                        }
                    } else if (car.getTrack().getServiceOrder().equals(Track.LIFO)) {
                        if (bestCar.getLastMoveDate().before(testCar.getLastMoveDate()) &&
                                bestCar.getLoadPriority().equals(testCar.getLoadPriority())) {
                            bestCar = testCar;
                            log.debug("New best car ({})", bestCar.toString());
                        }
                    }
                }
            }
            if (car != bestCar) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildTrackModeCarPriority", car.getTrack().getTrackTypeName(),
                                car.getTrackName(), car.getTrack().getServiceOrder(), bestCar.toString(),
                                bestCar.getLastDate(), car.toString(), car.getLastDate()));
                _carList.remove(bestCar); // change sort
                _carList.add(_carIndex, bestCar);
            }
        }
        addLine(_buildReport, SEVEN, BLANK_LINE);
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
            if (rl == _train.getTrainDepartsRouteLocation()) {
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
        if (_train.isDepartingStaging()) {
            _reqNumOfMoves = 0; // Move cars out of staging after working other
                                // locations
            // if leaving and returning to staging on the same track temporary
            // pull cars off the track
            if (_departStageTrack == _terminateStageTrack) {
                if (!_train.isAllowReturnToStagingEnabled() && !Setup.isStagingAllowReturnEnabled()) {
                    // takes care of cars in a kernel by getting all cars
                    for (Car car : carManager.getList()) {
                        // don't remove caboose or car with FRED already
                        // assigned to train
                        if (car.getTrack() == _departStageTrack && car.getRouteDestination() == null) {
                            car.setLocation(car.getLocation(), null);
                        }
                    }
                } else {
                    // since all cars can return to staging, the track space is
                    // consumed for now
                    addLine(_buildReport, THREE, BLANK_LINE);
                    addLine(_buildReport, THREE, Bundle.getMessage("buildWarnDepartStaging",
                            _departStageTrack.getLocation().getName(), _departStageTrack.getName()));
                    addLine(_buildReport, THREE, BLANK_LINE);
                }
            }
            addLine(_buildReport, THREE,
                    Bundle.getMessage("buildDepartStagingAggressive", _departStageTrack.getLocation().getName()));
        }
    }

    /**
     * Restores cars departing staging track assignment.
     */
    protected void restoreCarsIfDepartingStaging() {
        if (_train.isDepartingStaging() &&
                _departStageTrack == _terminateStageTrack &&
                !_train.isAllowReturnToStagingEnabled() &&
                !Setup.isStagingAllowReturnEnabled()) {
            // restore departure track for cars departing staging
            for (Car car : _carList) {
                if (car.getLocation() == _departStageTrack.getLocation() && car.getTrack() == null) {
                    car.setLocation(_departStageTrack.getLocation(), _departStageTrack, RollingStock.FORCE); // force
                    if (car.getKernel() != null) {
                        for (Car k : car.getKernel().getCars()) {
                            k.setLocation(_departStageTrack.getLocation(), _departStageTrack, RollingStock.FORCE); // force
                        }
                    }
                }
            }
        }
    }

    protected void showLoadGenerationOptionsStaging() {
        if (_departStageTrack != null &&
                _reqNumOfMoves > 0 &&
                (_departStageTrack.isAddCustomLoadsEnabled() ||
                        _departStageTrack.isAddCustomLoadsAnySpurEnabled() ||
                        _departStageTrack.isAddCustomLoadsAnyStagingTrackEnabled())) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildCustomLoadOptions", _departStageTrack.getName()));
            if (_departStageTrack.isAddCustomLoadsEnabled()) {
                addLine(_buildReport, FIVE, Bundle.getMessage("buildLoadCarLoads"));
            }
            if (_departStageTrack.isAddCustomLoadsAnySpurEnabled()) {
                addLine(_buildReport, FIVE, Bundle.getMessage("buildLoadAnyCarLoads"));
            }
            if (_departStageTrack.isAddCustomLoadsAnyStagingTrackEnabled()) {
                addLine(_buildReport, FIVE, Bundle.getMessage("buildLoadsStaging"));
            }
            addLine(_buildReport, FIVE, BLANK_LINE);
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
        if (!_train.isDepartingStaging()) {
            return;
        }
        int carCount = 0;
        StringBuffer buf = new StringBuffer();
        // confirm that all cars in staging are departing
        for (Car car : _carList) {
            // build failure if car departing staging without a destination or
            // train
            if (car.getTrack() == _departStageTrack &&
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
                    _departStageTrack.getLocation().getName(), _departStageTrack.getName());
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
                    Bundle.getMessage("buildOnlyFirstXXXCars", DISPLAY_CAR_LIMIT_20, _departStageTrack.getName()));
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
        if (_train.isDepartingStaging()) {
            // confirm that all cars in staging are departing
            for (Car car : _carList) {
                if (car.getTrack() == _departStageTrack &&
                        (car.getDestination() == null || car.getDestinationTrack() == null || car.getTrain() == null)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Add car to train, and adjust train length and weight
     *
     * @param car   the car being added to the train
     * @param rl    the departure route location for this car
     * @param rld   the destination route location for this car
     * @param track the destination track for this car
     */
    protected void addCarToTrain(Car car, RouteLocation rl, RouteLocation rld, Track track) {
        addLine(_buildReport, THREE,
                Bundle.getMessage("buildCarAssignedDest", car.toString(), rld.getName(), track.getName()));
        car.setDestination(track.getLocation(), track);
        int length = car.getTotalLength();
        int weightTons = car.getAdjustedWeightTons();
        // car could be part of a kernel
        if (car.getKernel() != null) {
            length = car.getKernel().getTotalLength(); // includes couplers
            weightTons = car.getKernel().getAdjustedWeightTons();
            List<Car> kCars = car.getKernel().getCars();
            addLine(_buildReport, THREE,
                    Bundle.getMessage("buildCarPartOfKernel", car.toString(), car.getKernelName(), kCars.size(),
                            car.getKernel().getTotalLength(), Setup.getLengthUnit().toLowerCase()));
            for (Car kCar : kCars) {
                if (kCar != car) {
                    addLine(_buildReport, THREE, Bundle.getMessage("buildCarKernelAssignedDest", kCar.toString(),
                            kCar.getKernelName(), rld.getName(), track.getName()));
                    kCar.setTrain(_train);
                    kCar.setRouteLocation(rl);
                    kCar.setRouteDestination(rld);
                    kCar.setDestination(track.getLocation(), track, true); // force
                                                                           // destination
                    // save final destination and track values in case of train
                    // reset
                    kCar.setPreviousFinalDestination(car.getPreviousFinalDestination());
                    kCar.setPreviousFinalDestinationTrack(car.getPreviousFinalDestinationTrack());
                }
            }
            car.updateKernel();
        }
        // warn if car's load wasn't generated out of staging
        if (!_train.isLoadNameAccepted(car.getLoadName(), car.getTypeName())) {
            _warnings++;
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildWarnCarDepartStaging", car.toString(), car.getLoadName()));
        }
        addLine(_buildReport, THREE, BLANK_LINE);
        _numberCars++; // bump number of cars moved by this train
        _completedMoves++; // bump number of car pick up moves for the location
        _reqNumOfMoves--; // decrement number of moves left for the location

        _carList.remove(car);
        _carIndex--; // removed car from list, so backup pointer

        rl.setCarMoves(rl.getCarMoves() + 1);
        if (rl != rld) {
            rld.setCarMoves(rld.getCarMoves() + 1);
        }
        // now adjust train length and weight for each location that car is in
        // the train
        finishAddRsToTrain(car, rl, rld, length, weightTons);
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
        rs.setTrain(_train);
        rs.setRouteLocation(rl);
        rs.setRouteDestination(rld);
        // now adjust train length and weight for each location that the rolling
        // stock is in the train
        boolean inTrain = false;
        for (RouteLocation routeLocation : _routeList) {
            if (rl == routeLocation) {
                inTrain = true;
            }
            if (rld == routeLocation) {
                break;
            }
            if (inTrain) {
                routeLocation.setTrainLength(routeLocation.getTrainLength() + length); // includes
                                                                                       // couplers
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
        if (_train.isLocalSwitcher()) {
            return true;
        }
        if ((rl.getTrainDirection() &
                rs.getLocation().getTrainDirections() &
                rs.getTrack().getTrainDirections()) != 0) {
            return true;
        }

        // Only track direction can cause the following message. Location
        // direction has
        // already been checked
        addLine(_buildReport, SEVEN,
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
        if (_train.isLocalSwitcher()) {
            return true;
        }
        if ((rl.getTrainDirection() & rl.getLocation().getTrainDirections()) != 0) {
            return true;
        }

        addLine(_buildReport, ONE, Bundle.getMessage("buildLocDirection", rl.getName(), rl.getTrainDirectionString()));
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
        int length = car.getTotalLength();
        if (car.getKernel() != null) {
            length = car.getKernel().getTotalLength();
        }
        boolean carInTrain = false;
        for (RouteLocation rlt : _routeList) {
            if (rl == rlt) {
                carInTrain = true;
            }
            if (rld == rlt) {
                break;
            }
            if (carInTrain && rlt.getTrainLength() + length > rlt.getMaxTrainLength()) {
                addLine(_buildReport, FIVE,
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
        if (_train.isLocalSwitcher()) {
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
                addLine(_buildReport, SEVEN,
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
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildDestinationDoesNotService", rld.getName(), rld.getTrainDirectionString()));
        } else {
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildCanNotDropRsUsingTrain", rs.toString(),
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
                if (track.isDropTrainAccepted(_train)) {
                    log.debug("Rolling stock ({}) can be droped by train to track ({})", rs.toString(),
                            track.getName());
                } else {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildCanNotDropTrain", rs.toString(), _train.getName(),
                                    track.getTrackTypeName(), track.getLocation().getName(), track.getName()));
                    return false;
                }
            }
            if (track.getDropOption().equals(Track.ROUTES) || track.getDropOption().equals(Track.EXCLUDE_ROUTES)) {
                if (track.isDropRouteAccepted(_train.getRoute())) {
                    log.debug("Rolling stock ({}) can be droped by route to track ({})", rs.toString(),
                            track.getName());
                } else {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildCanNotDropRoute", rs.toString(), _train.getRoute().getName(),
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
        addLine(_buildReport, THREE,
                Bundle.getMessage("buildStagingHas", departStageTrack.getName(),
                        Integer.toString(departStageTrack.getNumberEngines()),
                        Integer.toString(departStageTrack.getNumberCars())));
        // does this staging track service this train?
        if (!departStageTrack.isPickupTrainAccepted(_train)) {
            addLine(_buildReport, THREE, Bundle.getMessage("buildStagingNotTrain", departStageTrack.getName()));
            return false;
        }
        if (departStageTrack.getNumberRS() == 0 && _train.getTrainDepartsRouteLocation().getMaxCarMoves() > 0) {
            addLine(_buildReport, THREE, Bundle.getMessage("buildStagingEmpty", departStageTrack.getName()));
            return false;
        }
        if (departStageTrack.getUsedLength() > _train.getTrainDepartsRouteLocation().getMaxTrainLength()) {
            addLine(_buildReport, THREE,
                    Bundle.getMessage("buildStagingTrainTooLong", departStageTrack.getName(),
                            departStageTrack.getUsedLength(), Setup.getLengthUnit().toLowerCase(),
                            _train.getTrainDepartsRouteLocation().getMaxTrainLength()));
            return false;
        }
        if (departStageTrack.getNumberCars() > _train.getTrainDepartsRouteLocation().getMaxCarMoves()) {
            addLine(_buildReport, THREE, Bundle.getMessage("buildStagingTooManyCars", departStageTrack.getName(),
                    departStageTrack.getNumberCars(), _train.getTrainDepartsRouteLocation().getMaxCarMoves()));
            return false;
        }
        // does the staging track have the right number of locomotives?
        if (!_train.getNumberEngines().equals("0") &&
                getNumberEngines(_train.getNumberEngines()) != departStageTrack.getNumberEngines()) {
            addLine(_buildReport, THREE, Bundle.getMessage("buildStagingNotEngines", departStageTrack.getName(),
                    departStageTrack.getNumberEngines(), _train.getNumberEngines()));
            return false;
        }
        // is the staging track direction correct for this train?
        if ((departStageTrack.getTrainDirections() & _train.getTrainDepartsRouteLocation().getTrainDirection()) == 0) {
            addLine(_buildReport, THREE, Bundle.getMessage("buildStagingNotDirection", departStageTrack.getName()));
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
        addLine(_buildReport, FIVE,
                Bundle.getMessage("buildTrainCanDepartTrack", _train.getName(), departStageTrack.getName()));
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
            for (Engine eng : engineManager.getList()) {
                if (eng.getTrack() == departStageTrack) {
                    // has engine been assigned to another train?
                    if (eng.getRouteLocation() != null) {
                        addLine(_buildReport, THREE, Bundle.getMessage("buildStagingDepart", departStageTrack.getName(),
                                eng.getTrainName()));
                        return false;
                    }
                    if (eng.getTrain() != null && eng.getTrain() != _train) {
                        addLine(_buildReport, THREE, Bundle.getMessage("buildStagingDepartEngineTrain",
                                departStageTrack.getName(), eng.toString(), eng.getTrainName()));
                        return false;
                    }
                    // does the train accept the engine type from the staging
                    // track?
                    if (!_train.isTypeNameAccepted(eng.getTypeName())) {
                        addLine(_buildReport, THREE, Bundle.getMessage("buildStagingDepartEngineType",
                                departStageTrack.getName(), eng.toString(), eng.getTypeName(), _train.getName()));
                        return false;
                    }
                    // does the train accept the engine model from the staging
                    // track?
                    if (!_train.getEngineModel().equals(Train.NONE) &&
                            !_train.getEngineModel().equals(eng.getModel())) {
                        addLine(_buildReport, THREE,
                                Bundle.getMessage("buildStagingDepartEngineModel", departStageTrack.getName(),
                                        eng.toString(), eng.getModel(), _train.getName()));
                        return false;
                    }
                    // does the engine road match the train requirements?
                    if (!_train.getCarRoadOption().equals(Train.ALL_ROADS) &&
                            !_train.getEngineRoad().equals(Train.NONE) &&
                            !_train.getEngineRoad().equals(eng.getRoadName())) {
                        addLine(_buildReport, THREE, Bundle.getMessage("buildStagingDepartEngineRoad",
                                departStageTrack.getName(), eng.toString(), eng.getRoadName(), _train.getName()));
                        return false;
                    }
                    // does the train accept the engine road from the staging
                    // track?
                    if (_train.getEngineRoad().equals(Train.NONE) &&
                            !_train.isLocoRoadNameAccepted(eng.getRoadName())) {
                        addLine(_buildReport, THREE, Bundle.getMessage("buildStagingDepartEngineRoad",
                                departStageTrack.getName(), eng.toString(), eng.getRoadName(), _train.getName()));
                        return false;
                    }
                    // does the train accept the engine owner from the staging
                    // track?
                    if (!_train.isOwnerNameAccepted(eng.getOwnerName())) {
                        addLine(_buildReport, THREE, Bundle.getMessage("buildStagingDepartEngineOwner",
                                departStageTrack.getName(), eng.toString(), eng.getOwnerName(), _train.getName()));
                        return false;
                    }
                    // does the train accept the engine built date from the
                    // staging track?
                    if (!_train.isBuiltDateAccepted(eng.getBuilt())) {
                        addLine(_buildReport, THREE,
                                Bundle.getMessage("buildStagingDepartEngineBuilt", departStageTrack.getName(),
                                        eng.toString(), eng.getBuilt(), _train.getName()));
                        return false;
                    }
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
            for (Car car : carManager.getList()) {
                if (car.getTrack() != departStageTrack) {
                    continue;
                }
                // ignore non-lead cars in kernels
                if (car.getKernel() != null && !car.isLead()) {
                    continue; // ignore non-lead cars
                }
                // has car been assigned to another train?
                if (car.getRouteLocation() != null) {
                    log.debug("Car ({}) has route location ({})", car.toString(), car.getRouteLocation().getName());
                    addLine(_buildReport, THREE,
                            Bundle.getMessage("buildStagingDepart", departStageTrack.getName(), car.getTrainName()));
                    return false;
                }
                if (car.getTrain() != null && car.getTrain() != _train) {
                    addLine(_buildReport, THREE, Bundle.getMessage("buildStagingDepartCarTrain",
                            departStageTrack.getName(), car.toString(), car.getTrainName()));
                    return false;
                }
                // does the train accept the car type from the staging track?
                if (!_train.isTypeNameAccepted(car.getTypeName())) {
                    addLine(_buildReport, THREE,
                            Bundle.getMessage("buildStagingDepartCarType", departStageTrack.getName(), car.toString(),
                                    car.getTypeName(), _train.getName()));
                    return false;
                }
                // does the train accept the car road from the staging track?
                if (!car.isCaboose() && !_train.isCarRoadNameAccepted(car.getRoadName())) {
                    addLine(_buildReport, THREE,
                            Bundle.getMessage("buildStagingDepartCarRoad", departStageTrack.getName(), car.toString(),
                                    car.getRoadName(), _train.getName()));
                    return false;
                }
                // does the train accept the car load from the staging track?
                if (!car.isCaboose() &&
                        !car.isPassenger() &&
                        (!car.getLoadName().equals(carLoads.getDefaultEmptyName()) ||
                                !departStageTrack.isAddCustomLoadsEnabled() &&
                                        !departStageTrack.isAddCustomLoadsAnySpurEnabled() &&
                                        !departStageTrack.isAddCustomLoadsAnyStagingTrackEnabled()) &&
                        !_train.isLoadNameAccepted(car.getLoadName(), car.getTypeName())) {
                    addLine(_buildReport, THREE,
                            Bundle.getMessage("buildStagingDepartCarLoad", departStageTrack.getName(), car.toString(),
                                    car.getLoadName(), _train.getName()));
                    return false;
                }
                // does the train accept the car owner from the staging track?
                if (!_train.isOwnerNameAccepted(car.getOwnerName())) {
                    addLine(_buildReport, THREE, Bundle.getMessage("buildStagingDepartCarOwner",
                            departStageTrack.getName(), car.toString(), car.getOwnerName(), _train.getName()));
                    return false;
                }
                // does the train accept the car built date from the staging
                // track?
                if (!_train.isBuiltDateAccepted(car.getBuilt())) {
                    addLine(_buildReport, THREE, Bundle.getMessage("buildStagingDepartCarBuilt",
                            departStageTrack.getName(), car.toString(), car.getBuilt(), _train.getName()));
                    return false;
                }
                // does the car have a destination serviced by this train?
                if (car.getDestination() != null) {
                    log.debug("Car ({}) has a destination ({}, {})", car.toString(), car.getDestinationName(),
                            car.getDestinationTrackName());
                    if (!_train.isServiceable(car)) {
                        addLine(_buildReport, THREE,
                                Bundle.getMessage("buildStagingDepartCarDestination", departStageTrack.getName(),
                                        car.toString(), car.getDestinationName(), _train.getName()));
                        return false;
                    }
                }
                // is this car a caboose with the correct road for this train?
                if (car.isCaboose() &&
                        (_train.getCabooseRoad().equals(Train.NONE) ||
                                _train.getCabooseRoad().equals(car.getRoadName()))) {
                    foundCaboose = true;
                }
                // is this car have a FRED with the correct road for this train?
                if (car.hasFred() &&
                        (_train.getCabooseRoad().equals(Train.NONE) ||
                                _train.getCabooseRoad().equals(car.getRoadName()))) {
                    foundFRED = true;
                }
            }
        }
        // does the train require a caboose and did we find one from staging?
        if (_train.isCabooseNeeded() && !foundCaboose) {
            addLine(_buildReport, THREE,
                    Bundle.getMessage("buildStagingNoCaboose", departStageTrack.getName(), _train.getCabooseRoad()));
            return false;
        }
        // does the train require a car with FRED and did we find one from
        // staging?
        if (_train.isFredNeeded() && !foundFRED) {
            addLine(_buildReport, THREE,
                    Bundle.getMessage("buildStagingNoCarFRED", departStageTrack.getName(), _train.getCabooseRoad()));
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

        addLine(_buildReport, SEVEN, Bundle.getMessage("buildStagingTrackPool", departStageTrack.getName(),
                departStageTrack.getPool().getName(), departStageTrack.getPool().getSize(),
                departStageTrack.getServiceOrder()));

        List<Car> carList = carManager.getAvailableTrainList(_train);
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
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("buildStagingCarsBefore", departStageTrack.getName(),
                                        track.getName()));
                        return false;
                    }
                } else {
                    if (carOtherStageTrackDate.before(carDepartStageTrackDate)) {
                        addLine(_buildReport, SEVEN, Bundle.getMessage("buildStagingCarsBefore", track.getName(),
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
        if (!terminateStageTrack.isDropTrainAccepted(_train)) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildStagingNotTrain", terminateStageTrack.getName()));
            return false;
        }
        // In normal mode, find a completely empty track. In aggressive mode, a
        // track that scheduled to depart is okay
        if (((!Setup.isBuildAggressive() || !Setup.isStagingTrackImmediatelyAvail()) &&
                terminateStageTrack.getNumberRS() != 0) ||
                terminateStageTrack.getNumberRS() != terminateStageTrack.getPickupRS()) {
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildStagingTrackOccupied", terminateStageTrack.getName(),
                            terminateStageTrack.getNumberEngines(), terminateStageTrack.getNumberCars()));
            if (terminateStageTrack.getIgnoreUsedLengthPercentage() == Track.IGNORE_0) {
                return false;
            } else {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildTrackHasPlannedPickups", terminateStageTrack.getName(),
                                terminateStageTrack.getIgnoreUsedLengthPercentage(), terminateStageTrack.getLength(),
                                Setup.getLengthUnit().toLowerCase(), terminateStageTrack.getUsedLength(), terminateStageTrack.getReserved(),
                                terminateStageTrack.getReservedLengthDrops(),
                                terminateStageTrack.getReservedLengthDrops() - terminateStageTrack.getReserved(),
                                terminateStageTrack.getAvailableTrackSpace()));
            }
        }
        if (terminateStageTrack.getDropRS() != 0) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildStagingTrackReserved", terminateStageTrack.getName(),
                    terminateStageTrack.getDropRS()));
            return false;
        }
        if (terminateStageTrack.getPickupRS() > 0) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildStagingTrackDepart", terminateStageTrack.getName()));
        }
        // if track is setup to accept a specific train or route, then ignore
        // other track restrictions
        if (terminateStageTrack.getDropOption().equals(Track.TRAINS) ||
                terminateStageTrack.getDropOption().equals(Track.ROUTES)) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildTrainCanTerminateTrack", _train.getName(), terminateStageTrack.getName()));
            return true; // train can drop to this track, ignore other track
                         // restrictions
        }
        if (!Setup.isStagingTrainCheckEnabled()) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildTrainCanTerminateTrack", _train.getName(), terminateStageTrack.getName()));
            return true;
        } else if (!checkTerminateStagingTrackRestrictions(terminateStageTrack)) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildStagingTrackRestriction", terminateStageTrack.getName(), _train.getName()));
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildOptionRestrictStaging"));
            return false;
        }
        return true;
    }

    private boolean checkTerminateStagingTrackRestrictions(Track terminateStageTrack) {
        // check go see if location/track will accept the train's car and engine
        // types
        for (String name : _train.getTypeNames()) {
            if (!_terminateLocation.acceptsTypeName(name)) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildDestinationType", _terminateLocation.getName(), name));
                return false;
            }
            if (!terminateStageTrack.isTypeNameAccepted(name)) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildStagingTrackType", terminateStageTrack.getName(), name));
                return false;
            }
        }
        // check go see if track will accept the train's car roads
        if (_train.getCarRoadOption().equals(Train.ALL_ROADS) &&
                !terminateStageTrack.getRoadOption().equals(Track.ALL_ROADS)) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildStagingTrackAllRoads", terminateStageTrack.getName()));
            return false;
        }
        // now determine if roads accepted by train are also accepted by staging
        // track
        // TODO should we be checking caboose and loco road names?
        for (String road : InstanceManager.getDefault(CarRoads.class).getNames()) {
            if (_train.isCarRoadNameAccepted(road)) {
                if (!terminateStageTrack.isRoadNameAccepted(road)) {
                    addLine(_buildReport, FIVE,
                            Bundle.getMessage("buildStagingTrackRoad", terminateStageTrack.getName(), road));
                    return false;
                }
            }
        }

        // determine if staging will accept loads carried by train
        if (_train.getLoadOption().equals(Train.ALL_LOADS) &&
                !terminateStageTrack.getLoadOption().equals(Track.ALL_LOADS)) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildStagingTrackAllLoads", terminateStageTrack.getName()));
            return false;
        }
        // get all of the types and loads that a train can carry, and determine
        // if staging will accept
        for (String type : _train.getTypeNames()) {
            for (String load : carLoads.getNames(type)) {
                if (_train.isLoadNameAccepted(load, type)) {
                    if (!terminateStageTrack.isLoadNameAndCarTypeAccepted(load, type)) {
                        addLine(_buildReport, FIVE, Bundle.getMessage("buildStagingTrackLoad",
                                terminateStageTrack.getName(), type + CarLoad.SPLIT_CHAR + load));
                        return false;
                    }
                }
            }
        }
        addLine(_buildReport, SEVEN,
                Bundle.getMessage("buildTrainCanTerminateTrack", _train.getName(), terminateStageTrack.getName()));
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
                addLine(_buildReport, SEVEN,
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
                addLine(_buildReport, SEVEN,
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
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildSpurScheduleNotUsed", track.getLocation().getName(), track.getName(),
                                track.getScheduleName(), si.getId(), track.getScheduleModeName().toLowerCase(),
                                si.getTypeName(), si.getRoadName(), si.getReceiveLoadName()));
            }
            return null;
        }
        if (!_train.isLoadNameAccepted(si.getReceiveLoadName(), si.getTypeName())) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildTrainNotNewLoad", _train.getName(),
                    si.getReceiveLoadName(), track.getLocation().getName(), track.getName()));
            return null;
        }
        // does the departure track allow this load?
        if (!car.getTrack().isLoadNameAndCarTypeShipped(si.getReceiveLoadName(), car.getTypeName())) {
            addLine(_buildReport, SEVEN,
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
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildScheduleNotActive", track.getName(), si.getId(), tName, aName));

            return null;
        }
        if (!si.getRandom().equals(ScheduleItem.NONE)) {
            if (!si.doRandom()) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildScheduleRandom", track.getLocation().getName(), track.getName(),
                                track.getScheduleName(), si.getId(), si.getReceiveLoadName(), si.getRandom(), si.getCalculatedRandom()));
                return null;
            }
        }
        log.debug("Found track ({}) schedule item id ({}) for car ({})", track.getName(), si.getId(), car.toString());
        return si;
    }

    protected void showCarServiceOrder(Car car) {
        if (!car.getTrack().getServiceOrder().equals(Track.NORMAL) && !car.getTrack().isStaging()) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildTrackModePriority", car.toString(), car.getTrack().getTrackTypeName(),
                            car.getLocationName(), car.getTrackName(), car.getTrack().getServiceOrder(), car.getLastDate()));
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
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildTrackIsAlternate", car.toString(),
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
                addLine(_buildReport, SEVEN,
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
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildTrackHasPlannedPickups", testTrack.getName(),
                                testTrack.getIgnoreUsedLengthPercentage(), testTrack.getLength(),
                                Setup.getLengthUnit().toLowerCase(), testTrack.getUsedLength(), testTrack.getReserved(),
                                testTrack.getReservedLengthDrops(),
                                testTrack.getReservedLengthDrops() - testTrack.getReserved(),
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
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildSpurScheduleLoad", testTrack.getName(), car.getLoadName()));
            }
            // check to see if alternate track is available if track full
            if (status.startsWith(Track.LENGTH) &&
                    testTrack.getAlternateTrack() != null &&
                    car.getTrack() != testTrack.getAlternateTrack() &&
                    checkTrainCanDrop(car, testTrack.getAlternateTrack())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildTrackFullHasAlternate", testDestination.getName(),
                        testTrack.getName(), testTrack.getAlternateTrack().getName()));
                status = car.checkDestination(testDestination, testTrack.getAlternateTrack());
                if (!status.equals(Track.OKAY)) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildCanNotDropCarBecause", car.toString(),
                                    testTrack.getAlternateTrack().getTrackTypeName(),
                                    testTrack.getLocation().getName(), testTrack.getAlternateTrack().getName(),
                                    status));
                    continue;
                }
                // send car to alternate track
                tracks.add(testTrack.getAlternateTrack());
                tracks.add(testTrack); // car's final destination
                break; // done with this destination
            }
            // okay to drop car?
            if (!status.equals(Track.OKAY)) {
                addLine(_buildReport, SEVEN,
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
            RouteLocation rle = _routeList.get(m);
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

    /**
     * Checks to see if local move is allowed for this car
     *
     * @param car       the car being moved
     * @param testTrack the destination track for this car
     * @return false if local move not allowed
     */
    private boolean checkForLocalMove(Car car, Track testTrack) {
        if (_train.isLocalSwitcher()) {
            // No local moves from spur to spur
            if (!Setup.isLocalSpurMovesEnabled() && testTrack.isSpur() && car.getTrack().isSpur()) {
                addLine(_buildReport, SEVEN,
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
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoYardToYardMove", car.getTrackName(), testTrack.getName()));
                return false;
            }
            // No local moves from interchange to interchange
            if (!Setup.isLocalInterchangeMovesEnabled() &&
                    testTrack.isInterchange() &&
                    car.getTrack().isInterchange()) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoInterchangeToInterchangeMove", car.getTrackName(),
                                testTrack.getName()));
                return false;
            }
        }
        return true;
    }

    protected Track tryStaging(Car car, RouteLocation rldSave) throws BuildFailedException {
        // local switcher working staging?
        if (_train.isLocalSwitcher() &&
                !car.isPassenger() &&
                !car.isCaboose() &&
                !car.hasFred() &&
                car.getTrack() == _terminateStageTrack) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildCanNotDropCarSameTrack", car.toString(), car.getTrack().getName()));
            return null;
        }
        // no need to check train and track direction into staging, already done
        String status = car.checkDestination(_terminateStageTrack.getLocation(), _terminateStageTrack);
        if (status.equals(Track.OKAY)) {
            return _terminateStageTrack;
            // only generate a new load if there aren't any other tracks
            // available for this car
        } else if (status.startsWith(Track.LOAD) &&
                car.getTrack() == _departStageTrack &&
                car.getLoadName().equals(carLoads.getDefaultEmptyName()) &&
                rldSave == null &&
                (_departStageTrack.isAddCustomLoadsAnyStagingTrackEnabled() ||
                        _departStageTrack.isAddCustomLoadsEnabled() ||
                        _departStageTrack.isAddCustomLoadsAnySpurEnabled())) {
            // try and generate a load for this car into staging
            if (generateLoadCarDepartingAndTerminatingIntoStaging(car, _terminateStageTrack)) {
                return _terminateStageTrack;
            }
        }
        addLine(_buildReport, SEVEN,
                Bundle.getMessage("buildCanNotDropCarBecause", car.toString(), _terminateStageTrack.getTrackTypeName(),
                        _terminateStageTrack.getLocation().getName(), _terminateStageTrack.getName(), status));
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
        if (rl != rld && rld.getName().equals(car.getLocationName())) {
            // don't delay adding a caboose, passenger car, or car with FRED
            if (car.isCaboose() || car.isPassenger() || car.hasFred()) {
                return false;
            }
            // no later pick up if car is departing staging
            if (car.getLocation().isStaging()) {
                return false;
            }
            if (!checkPickUpTrainDirection(car, rld)) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoPickupLaterDirection", car.toString(), rld.getName(), rld.getId()));
                return false;
            }
            if (!rld.isPickUpAllowed()) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoPickupLater", car.toString(), rld.getName(), rld.getId()));
                return false;
            }
            if (rld.getCarMoves() >= rld.getMaxCarMoves()) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildNoPickupLaterMoves", car.toString(), rld.getName(), rld.getId()));
                return false;
            }
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildPickupLaterOkay", car.toString(), rld.getName(), rld.getId()));
            return true;
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
        if (!_train.isAllowThroughCarsEnabled() &&
                !_train.isLocalSwitcher() &&
                !car.isCaboose() &&
                !car.hasFred() &&
                !car.isPassenger() &&
                car.getSplitLocationName().equals(_departLocation.getSplitName()) &&
                splitString(destinationName).equals(_terminateLocation.getSplitName()) &&
                !_departLocation.getSplitName().equals(_terminateLocation.getSplitName())) {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildThroughTrafficNotAllow", _departLocation.getName(),
                    _terminateLocation.getName()));
            return false; // through cars not allowed
        }
        return true; // through cars allowed
    }

    private boolean checkLocalMovesAllowed(Car car, Track track) {
        if (!_train.isLocalSwitcher() && !_train.isAllowLocalMovesEnabled() &&
                car.getSplitLocationName().equals(track.getLocation().getSplitName())) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildNoLocalMoveToTrack", car.getLocationName(), car.getTrackName(),
                            track.getLocation().getName(), track.getName(), _train.getName()));
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
        // code check
        if (stageTrack == null || !stageTrack.isStaging()) {
            throw new BuildFailedException("ERROR coding issue, staging track null or not staging");
        }
        if (!stageTrack.isTypeNameAccepted(car.getTypeName())) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildStagingTrackType", stageTrack.getName(), car.getTypeName()));
            return false;
        }
        if (!stageTrack.isRoadNameAccepted(car.getRoadName())) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildStagingTrackRoad", stageTrack.getName(), car.getRoadName()));
            return false;
        }
        // Departing and returning to same location in staging?
        if (!_train.isAllowReturnToStagingEnabled() &&
                !Setup.isStagingAllowReturnEnabled() &&
                !car.isCaboose() &&
                !car.hasFred() &&
                !car.isPassenger() &&
                car.getSplitLocationName().equals(stageTrack.getLocation().getSplitName())) {
            addLine(_buildReport, SEVEN,
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
        addLine(_buildReport, SEVEN, BLANK_LINE);
        addLine(_buildReport, SEVEN,
                Bundle.getMessage("buildSearchTrackLoadStaging", car.toString(), car.getTypeName(),
                        car.getLoadType().toLowerCase(), car.getLoadName(), car.getLocationName(), car.getTrackName(),
                        stageTrack.getLocation().getName(), stageTrack.getName()));
        String oldLoad = car.getLoadName(); // save car's "E" load
        for (int i = loads.size() - 1; i >= 0; i--) {
            String load = loads.get(i);
            log.debug("Try custom load ({}) for car ({})", load, car.toString());
            if (!car.getTrack().isLoadNameAndCarTypeShipped(load, car.getTypeName()) ||
                    !stageTrack.isLoadNameAndCarTypeAccepted(load, car.getTypeName()) ||
                    !_train.isLoadNameAccepted(load, car.getTypeName())) {
                // report why the load was rejected and remove it from consideration
                if (!car.getTrack().isLoadNameAndCarTypeShipped(load, car.getTypeName())) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildTrackNotNewLoad", car.getTrackName(), load,
                                    stageTrack.getLocation().getName(), stageTrack.getName()));
                }
                if (!stageTrack.isLoadNameAndCarTypeAccepted(load, car.getTypeName())) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildDestTrackNoLoad", stageTrack.getLocation().getName(),
                                    stageTrack.getName(), car.toString(), load));
                }
                if (!_train.isLoadNameAccepted(load, car.getTypeName())) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildTrainNotNewLoad", _train.getName(), load,
                                    stageTrack.getLocation().getName(), stageTrack.getName()));
                }
                loads.remove(i);
                continue;
            }
            car.setLoadName(load);
            // does the car have a home division?
            if (car.getDivision() != null) {
                addLine(_buildReport, SEVEN,
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
                    addLine(_buildReport, SEVEN,
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
            if (!router.isCarRouteable(car, _train, stageTrack, _buildReport)) {
                loads.remove(i); // no remove this load
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildStagingTrackNotReachable",
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
            if (status.equals(Track.OKAY) || (status.startsWith(Track.LENGTH) && stageTrack != _terminateStageTrack)) {
                car.setLoadGeneratedFromStaging(true);
                car.setFinalDestination(stageTrack.getLocation());
                // don't set track assignment unless the car is going to this
                // train's staging
                if (stageTrack == _terminateStageTrack) {
                    car.setFinalDestinationTrack(stageTrack);
                } else {
                    // don't assign the track, that will be done later
                    car.setFinalDestinationTrack(null);
                }
                car.updateKernel(); // is car part of kernel?
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildAddingScheduleLoad", loads.size(), car.getLoadName(), car.toString()));
                return true;
            }
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("buildCanNotDropCarBecause", car.toString(), stageTrack.getTrackTypeName(),
                            stageTrack.getLocation().getName(), stageTrack.getName(), status));
        }
        car.setLoadName(oldLoad); // restore load and report failure
        addLine(_buildReport, SEVEN, Bundle.getMessage("buildUnableNewLoadStaging", car.toString(), car.getTrackName(),
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
        for (Train train : trainManager.getTrainsByIdList()) {
            if (!train.getLoadOption().equals(Train.ALL_LOADS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if cars that are already in the train can be redirected
     * from the alternate track to the spur that really wants the car. Fixes the
     * issue of having cars placed at the alternate when the spur's cars get
     * pulled by this train, but cars were sent to the alternate because the
     * spur was full at the time it was tested.
     *
     * @return true if one or more cars were redirected
     * @throws BuildFailedException if coding issue
     */
    protected boolean redirectCarsFromAlternateTrack() throws BuildFailedException {
        // code check, should be aggressive
        if (!Setup.isBuildAggressive()) {
            throw new BuildFailedException("ERROR coding issue, should be using aggressive mode");
        }
        boolean redirected = false;
        List<Car> cars = carManager.getByTrainList(_train);
        for (Car car : cars) {
            // does the car have a final destination and the destination is this
            // one?
            if (car.getFinalDestination() == null ||
                    car.getFinalDestinationTrack() == null ||
                    !car.getFinalDestinationName().equals(car.getDestinationName())) {
                continue;
            }
            Track alternate = car.getFinalDestinationTrack().getAlternateTrack();
            if (alternate == null || car.getDestinationTrack() != alternate) {
                continue;
            }
            // is the car in a kernel?
            if (car.getKernel() != null && !car.isLead()) {
                continue;
            }
            log.debug("Car ({}) alternaten track ({}) has final destination track ({}) location ({})", car.toString(),
                    car.getDestinationTrackName(), car.getFinalDestinationTrackName(), car.getDestinationName()); // NOI18N
            if ((alternate.isYard() || alternate.isInterchange()) &&
                    car.checkDestination(car.getFinalDestination(), car.getFinalDestinationTrack())
                            .equals(Track.OKAY) &&
                    checkDropTrainDirection(car, car.getRouteDestination(), car.getFinalDestinationTrack()) &&
                    checkTrainCanDrop(car, car.getFinalDestinationTrack())) {
                log.debug("Car ({}) alternate track ({}) can be redirected to final destination track ({})",
                        car.toString(), car.getDestinationTrackName(), car.getFinalDestinationTrackName());
                if (car.getKernel() != null) {
                    for (Car k : car.getKernel().getCars()) {
                        if (k.isLead()) {
                            continue;
                        }
                        addLine(_buildReport, FIVE,
                                Bundle.getMessage("buildRedirectFromAlternate", car.getFinalDestinationName(),
                                        car.getFinalDestinationTrackName(), k.toString(),
                                        car.getDestinationTrackName()));
                        // force car to track
                        k.setDestination(car.getFinalDestination(), car.getFinalDestinationTrack(), true);
                    }
                }
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildRedirectFromAlternate", car.getFinalDestinationName(),
                                car.getFinalDestinationTrackName(),
                                car.toString(), car.getDestinationTrackName()));
                car.setDestination(car.getFinalDestination(), car.getFinalDestinationTrack(), true);
                redirected = true;
            }
        }
        return redirected;
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
        for (int i = _carIndex; i < _carList.size(); i++) {
            if (numberCars == DISPLAY_CAR_LIMIT_100) {
                addLine(_buildReport, FIVE, Bundle.getMessage("buildOnlyFirstXXXCars", numberCars, rl.getName()));
                break;
            }
            Car car = _carList.get(i);
            // find a car at this location that hasn't been given a destination
            if (!car.getLocationName().equals(rl.getName()) || car.getRouteDestination() != null) {
                continue;
            }
            if (numberCars == 0) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildMovesCompleted", rl.getMaxCarMoves(), rl.getName()));
            }
            addLine(_buildReport, SEVEN, Bundle.getMessage("buildCarIgnored", car.toString(), car.getTypeName(),
                    car.getLoadType().toLowerCase(), car.getLoadName(), car.getLocationName(), car.getTrackName()));
            numberCars++;
        }
        addLine(_buildReport, SEVEN, BLANK_LINE);
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
        for (RouteLocation routeLocation : _routeList) {
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
            addLine(_buildReport, ONE, BLANK_LINE);
            addLine(_buildReport, ONE, Bundle.getMessage("buildCarsNotRoutable"));
            for (Car car : _notRoutable) {
                _warnings++;
                addLine(_buildReport, ONE,
                        Bundle.getMessage("buildCarNotRoutable", car.toString(), car.getLocationName(),
                                car.getTrackName(), car.getFinalDestinationName(), car.getFinalDestinationTrackName()));
            }
            addLine(_buildReport, ONE, BLANK_LINE);
        }
    }

    /**
     * build has failed due to cars in staging not having destinations this
     * routine removes those cars from the staging track by user request.
     */
    protected void removeCarsFromStaging() {
        // Code check, only called if train was departing staging
        if (_departStageTrack == null) {
            log.error("Error, called when cars in staging not assigned to train");
            return;
        }
        for (Car car : _carList) {
            // remove cars from departure staging track that haven't been
            // assigned to this train
            if (car.getTrack() == _departStageTrack && car.getTrain() == null) {
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

    /*
     * Engine methods start here
     */

    /**
     * Adds engines to the train if needed based on HPT. Note that the engine
     * additional weight isn't considered in this method so HP requirements can
     * be lower compared to the original calculation which did include the
     * weight of the engines.
     *
     * @param hpAvailable   the engine hp already assigned to the train for this
     *                      leg
     * @param extraHpNeeded the additional hp needed
     * @param rlNeedHp      where in the route the additional hp is needed
     * @param rl            the start of the leg
     * @param rld           the end of the leg
     * @throws BuildFailedException if unable to add engines to train
     */
    protected void addEnginesBasedHPT(int hpAvailable, int extraHpNeeded, RouteLocation rlNeedHp, RouteLocation rl,
            RouteLocation rld) throws BuildFailedException {
        if (rlNeedHp == null) {
            return;
        }
        int numberLocos = 0;
        // determine how many locos have already been assigned to the train
        List<Engine> engines = engineManager.getList(_train);
        for (Engine rs : engines) {
            if (rs.getRouteLocation() == rl) {
                numberLocos++;
            }
        }

        addLine(_buildReport, ONE, BLANK_LINE);
        addLine(_buildReport, ONE, Bundle.getMessage("buildTrainReqExtraHp", extraHpNeeded, rlNeedHp.getName(),
                rld.getName(), numberLocos));

        // determine engine model and road
        String model = _train.getEngineModel();
        String road = _train.getEngineRoad();
        if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                rl == _train.getSecondLegStartRouteLocation()) {
            model = _train.getSecondLegEngineModel();
            road = _train.getSecondLegEngineRoad();
        } else if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                rl == _train.getThirdLegStartRouteLocation()) {
            model = _train.getThirdLegEngineModel();
            road = _train.getThirdLegEngineRoad();
        }

        while (numberLocos < Setup.getMaxNumberEngines()) {
            // if no engines assigned, can't use B unit as first engine
            if (!getEngines("1", model, road, rl, rld, numberLocos > 0)) {
                throw new BuildFailedException(Bundle.getMessage("buildErrorEngines", Bundle.getMessage("additional"),
                        rl.getName(), rld.getName()));
            }
            numberLocos++;
            int currentHp = _train.getTrainHorsePower(rlNeedHp);
            if (currentHp > hpAvailable + extraHpNeeded) {
                break; // done
            }
            if (numberLocos < Setup.getMaxNumberEngines()) {
                addLine(_buildReport, FIVE, BLANK_LINE);
                addLine(_buildReport, THREE,
                        Bundle.getMessage("buildContinueAddLocos", (hpAvailable + extraHpNeeded - currentHp),
                                rlNeedHp.getName(), rld.getName(), numberLocos, currentHp));
            } else {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildMaxNumberLocoAssigned", Setup.getMaxNumberEngines()));
            }
        }
    }

    /**
     * Adds an engine to the train.
     *
     * @param engine the engine being added to the train
     * @param rl     where in the train's route to pick up the engine
     * @param rld    where in the train's route to set out the engine
     * @param track  the destination track for this engine
     */
    private void addEngineToTrain(Engine engine, RouteLocation rl, RouteLocation rld, Track track) {
        _lastEngine = engine; // needed in case there's a engine change in the
                              // train's route
        if (_train.getLeadEngine() == null) {
            _train.setLeadEngine(engine); // load lead engine
        }
        addLine(_buildReport, ONE, Bundle.getMessage("buildEngineAssigned", engine.toString(), rl.getName(),
                rld.getName(), track.getName()));
        engine.setDestination(track.getLocation(), track);
        int length = engine.getTotalLength();
        int weightTons = engine.getAdjustedWeightTons();
        // engine in consist?
        if (engine.getConsist() != null) {
            length = engine.getConsist().getTotalLength();
            weightTons = engine.getConsist().getAdjustedWeightTons();
            for (Engine cEngine : engine.getConsist().getEngines()) {
                if (cEngine != engine) {
                    addLine(_buildReport, ONE, Bundle.getMessage("buildEngineAssigned", cEngine.toString(),
                            rl.getName(), rld.getName(), track.getName()));
                    cEngine.setTrain(_train);
                    cEngine.setRouteLocation(rl);
                    cEngine.setRouteDestination(rld);
                    cEngine.setDestination(track.getLocation(), track, true); // force
                                                                              // destination
                }
            }
        }
        // now adjust train length and weight for each location that engines are
        // in the train
        finishAddRsToTrain(engine, rl, rld, length, weightTons);
    }

    private boolean buildConsistFromSingleLocos(int reqNumberEngines, List<Engine> singleLocos, RouteLocation rl,
            RouteLocation rld) {
        addLine(_buildReport, FIVE, Bundle.getMessage("buildOptionBuildConsist", reqNumberEngines, rl.getName()));
        addLine(_buildReport, FIVE, Bundle.getMessage("buildOptionSingleLocos", singleLocos.size(), rl.getName()));
        if (singleLocos.size() >= reqNumberEngines) {
            int locos = 0;
            // first find an "A" unit
            for (Engine engine : singleLocos) {
                if (engine.isBunit()) {
                    continue;
                }
                if (setEngineDestination(engine, rl, rld)) {
                    _engineList.remove(engine);
                    singleLocos.remove(engine);
                    locos++;
                    break; // found "A" unit
                }
            }
            // did we find an "A" unit?
            if (locos > 0) {
                // now add the rest "A" or "B" units
                for (Engine engine : singleLocos) {
                    if (setEngineDestination(engine, rl, rld)) {
                        _engineList.remove(engine);
                        locos++;
                    }
                    if (locos == reqNumberEngines) {
                        return true; // done!
                    }
                }
            } else {
                // list the "B" units found
                for (Engine engine : singleLocos) {
                    if (engine.isBunit()) {
                        addLine(_buildReport, FIVE,
                                Bundle.getMessage("BuildEngineBunit", engine.toString(), engine.getLocationName(),
                                        engine.getTrackName()));
                    }
                }
            }
        }
        return false;
    }

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
     * Sets the destination track for an engine and assigns it to the train.
     *
     * @param engine The engine to be added to train
     * @param rl     Departure route location
     * @param rld    Destination route location
     * @return true if destination track found and set
     */
    protected boolean setEngineDestination(Engine engine, RouteLocation rl, RouteLocation rld) {
        // engine to staging?
        if (rld == _train.getTrainTerminatesRouteLocation() && _terminateStageTrack != null) {
            String status = engine.checkDestination(_terminateStageTrack.getLocation(), _terminateStageTrack);
            if (status.equals(Track.OKAY)) {
                addEngineToTrain(engine, rl, rld, _terminateStageTrack);
                return true; // done
            } else {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildCanNotDropEngineToTrack", engine.toString(),
                                _terminateStageTrack.getTrackTypeName(),
                                _terminateStageTrack.getLocation().getName(), _terminateStageTrack.getName(), status));
            }
        } else {
            // find a destination track for this engine
            Location destination = rld.getLocation();
            List<Track> destTracks = destination.getTracksByMoves(null);
            if (destTracks.size() == 0) {
                addLine(_buildReport, THREE, Bundle.getMessage("buildNoTracksAtDestination", rld.getName()));
            }
            for (Track track : destTracks) {
                if (!checkDropTrainDirection(engine, rld, track)) {
                    continue;
                }
                if (!checkTrainCanDrop(engine, track)) {
                    continue;
                }
                String status = engine.checkDestination(destination, track);
                if (status.equals(Track.OKAY)) {
                    addEngineToTrain(engine, rl, rld, track);
                    return true;
                } else {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildCanNotDropEngineToTrack", engine.toString(),
                                    track.getTrackTypeName(),
                                    track.getLocation().getName(), track.getName(), status));
                }
            }
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildCanNotDropEngToDest", engine.toString(), rld.getName()));
        }
        return false; // not able to set loco's destination
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

        for (RouteLocation rl : _routeList) {
            if (rl.isPickUpAllowed() && rl != _train.getTrainTerminatesRouteLocation()) {
                moves += rl.getMaxCarMoves(); // assume all moves are pick ups
                double carDivisor = 16; // number of 40' cars per engine 1%
                                        // grade
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
        if(_train.isLocalSwitcher()) {
            nE = 1; // only one engine if switcher
        }
        addLine(_buildReport, ONE,
                Bundle.getMessage("buildAutoBuildMsg", Integer.toString(nE)));
        if (nE > Setup.getMaxNumberEngines()) {
            addLine(_buildReport, THREE, Bundle.getMessage("buildMaximumNumberEngines", Setup.getMaxNumberEngines()));
            nE = Setup.getMaxNumberEngines();
        }
        return nE;
    }

    protected boolean getConsist(String reqNumEngines, String model, String road, RouteLocation rl, RouteLocation rld)
            throws BuildFailedException {
        if (reqNumEngines.equals(Train.AUTO_HPT)) {
            for (int i = 2; i < Setup.getMaxNumberEngines(); i++) {
                if (getEngines(Integer.toString(i), model, road, rl, rld)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void showEnginesByLocation() {
        // show how many engines were found
        addLine(_buildReport, SEVEN, BLANK_LINE);
        addLine(_buildReport, ONE,
                Bundle.getMessage("buildFoundLocos", Integer.toString(_engineList.size()), _train.getName()));

        // only show engines once using the train's route
        List<String> locationNames = new ArrayList<>();
        for (RouteLocation rl : _train.getRoute().getLocationsBySequenceList()) {
            if (locationNames.contains(rl.getName())) {
                continue;
            }
            locationNames.add(rl.getName());
            int count = countRollingStockAt(rl, new ArrayList<RollingStock>(_engineList));
            if (rl.getLocation().isStaging()) {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildLocosInStaging", count, rl.getName()));
            } else {
                addLine(_buildReport, FIVE,
                        Bundle.getMessage("buildLocosAtLocation", count, rl.getName()));
            }
            for (Engine engine : _engineList) {
                if (engine.getLocationName().equals(rl.getName())) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildLocoAtLocWithMoves", engine.toString(), engine.getTypeName(),
                                    engine.getModel(), engine.getLocationName(), engine.getTrackName(),
                                    engine.getMoves()));
                }
            }
            addLine(_buildReport, SEVEN, BLANK_LINE);
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

    protected boolean getEngines(String requestedEngines, String model, String road, RouteLocation rl,
            RouteLocation rld) throws BuildFailedException {
        return getEngines(requestedEngines, model, road, rl, rld, !USE_BUNIT);
    }

    /**
     * Get the engines for this train at a route location. If departing from
     * staging engines must come from that track. Finds the required number of
     * engines in a consist, or if the option to build from single locos, builds
     * a consist for the user. When true, engines successfully added to train
     * for the leg requested.
     *
     * @param requestedEngines Requested number of Engines, can be number, AUTO
     *                         or AUTO HPT
     * @param model            Optional model name for the engines
     * @param road             Optional road name for the engines
     * @param rl               Departure route location for the engines
     * @param rld              Destination route location for the engines
     * @param useBunit         true if B unit engine is allowed
     * @return true if correct number of engines found.
     * @throws BuildFailedException if coding issue
     */
    protected boolean getEngines(String requestedEngines, String model, String road, RouteLocation rl,
            RouteLocation rld, boolean useBunit) throws BuildFailedException {
        // load departure track if staging
        Track departStageTrack = null;
        if (rl == _train.getTrainDepartsRouteLocation()) {
            departStageTrack = _departStageTrack; // get departure track from
                                                  // staging, could be null
        }

        int reqNumberEngines = getNumberEngines(requestedEngines);

        // if not departing staging track and engines aren't required done!
        if (departStageTrack == null && reqNumberEngines == 0) {
            return true;
        }
        // if departing staging and no engines required and none available,
        // we're done
        if (departStageTrack != null && reqNumberEngines == 0 && departStageTrack.getNumberEngines() == 0) {
            return true;
        }

        // code check, staging track selection checks number of engines needed
        if (departStageTrack != null &&
                reqNumberEngines != 0 &&
                departStageTrack.getNumberEngines() != reqNumberEngines) {
            throw new BuildFailedException(Bundle.getMessage("buildStagingNotEngines", departStageTrack.getName(),
                    departStageTrack.getNumberEngines(), reqNumberEngines));
        }

        // code check
        if (rl == null || rld == null) {
            throw new BuildFailedException(
                    Bundle.getMessage("buildErrorEngLocUnknown"));
        }

        addLine(_buildReport, FIVE, Bundle.getMessage("buildBegineSearchEngines", reqNumberEngines, model, road,
                rl.getName(), rld.getName()));

        int assignedLocos = 0; // the number of locos assigned to this train
        List<Engine> singleLocos = new ArrayList<>();
        for (int indexEng = 0; indexEng < _engineList.size(); indexEng++) {
            Engine engine = _engineList.get(indexEng);
            log.debug("Engine ({}) at location ({}, {})", engine.toString(), engine.getLocationName(),
                    engine.getTrackName());

            // use engines that are departing from the selected staging track
            // (departTrack
            // != null if staging)
            if (departStageTrack != null && !departStageTrack.equals(engine.getTrack())) {
                continue;
            }
            // use engines that are departing from the correct location
            if (!engine.getLocationName().equals(rl.getName())) {
                log.debug("Skipping engine ({}) at location ({})", engine.toString(), engine.getLocationName());
                continue;
            }
            // skip engines models that train does not service
            if (!model.equals(Train.NONE) && !engine.getModel().equals(model)) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeEngineModel", engine.toString(),
                        engine.getModel(), engine.getLocationName()));
                continue;
            }
            // Does the train have a very specific engine road name requirement?
            if (!road.equals(Train.NONE) && !engine.getRoadName().equals(road)) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeEngineRoad", engine.toString(),
                        engine.getLocationName(), engine.getTrackName(), engine.getRoadName()));
                continue;
            }
            // skip engines on tracks that don't service the train's departure
            // direction
            if (!checkPickUpTrainDirection(engine, rl)) {
                continue;
            }
            // skip engines that have been assigned destinations that don't
            // match the requested destination
            if (engine.getDestination() != null && !engine.getDestinationName().equals(rld.getName())) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("buildExcludeEngineDestination", engine.toString(),
                        engine.getDestinationName()));
                continue;
            }
            // don't use non lead locos in a consist
            if (engine.getConsist() != null) {
                if (engine.isLead()) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildEngineLeadConsist", engine.toString(),
                                    engine.getConsist().getName(), engine.getConsist().getEngines().size()));
                } else {
                    continue;
                }
            }
            // departing staging, then all locos must go!
            if (departStageTrack != null) {
                if (!setEngineDestination(engine, rl, rld)) {
                    return false;
                }
                _engineList.remove(indexEng--);
                if (engine.getConsist() != null) {
                    assignedLocos = assignedLocos + engine.getConsist().getSize();
                } else {
                    assignedLocos++;
                }
                continue;
            }
            // can't use B units if requesting one loco
            if (!useBunit && reqNumberEngines == 1 && engine.isBunit()) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildExcludeEngineBunit", engine.toString(), engine.getModel()));
                continue;
            }
            // is this engine part of a consist?
            if (engine.getConsist() == null) {
                // single engine, but does the train require a consist?
                if (reqNumberEngines > 1) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("buildExcludeEngineSingle", engine.toString(), reqNumberEngines));
                    singleLocos.add(engine);
                    continue;
                }
                // engine is part of a consist
            } else if (engine.getConsist().getSize() == reqNumberEngines) {
                log.debug("Consist ({}) has the required number of engines", engine.getConsist().getName()); // NOI18N
            } else if (reqNumberEngines != 0) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("buildExcludeEngConsistNumber", engine.toString(),
                                engine.getConsist().getName(), engine.getConsist().getSize()));
                continue;
            }
            // found a loco or consist!
            assignedLocos++;

            // now find terminal track for engine(s)
            addLine(_buildReport, FIVE,
                    Bundle.getMessage("buildEngineRoadModelType", engine.toString(), engine.getRoadName(),
                            engine.getModel(), engine.getTypeName(), engine.getLocationName(), engine.getTrackName(),
                            rld.getName()));
            if (setEngineDestination(engine, rl, rld)) {
                _engineList.remove(indexEng--);
                return true; // normal exit when not staging
            }
        }
        // build a consist out of non-consisted locos
        if (assignedLocos == 0 && reqNumberEngines > 1 && _train.isBuildConsistEnabled()) {
            if (buildConsistFromSingleLocos(reqNumberEngines, singleLocos, rl, rld)) {
                return true; // normal exit when building with single locos
            }
        }
        if (assignedLocos == 0) {
            String locationName = rl.getName();
            if (departStageTrack != null) {
                locationName = locationName + ", " + departStageTrack.getName();
            }
            addLine(_buildReport, FIVE, Bundle.getMessage("buildNoLocosFoundAtLocation", locationName));
        } else if (departStageTrack != null && (reqNumberEngines == 0 || reqNumberEngines == assignedLocos)) {
            return true; // normal exit assigning from staging
        }
        // not able to assign engines to train
        return false;
    }

    /**
     * Removes engine from train and attempts to replace it with engine or
     * consist that meets the HP requirements of the train.
     *
     * @param hpNeeded   How much hp is needed
     * @param leadEngine The lead engine for this leg
     * @param model      The engine's model
     * @param road       The engine's road
     * @throws BuildFailedException if new engine not found
     */
    protected void getNewEngine(int hpNeeded, Engine leadEngine, String model, String road)
            throws BuildFailedException {
        // save lead engine's rl, and rld
        RouteLocation rl = leadEngine.getRouteLocation();
        RouteLocation rld = leadEngine.getRouteDestination();
        removeEngineFromTrain(leadEngine);
        _engineList.add(0, leadEngine); // put engine back into the pool
        if (hpNeeded < 50) {
            hpNeeded = 50; // the minimum HP
        }
        int hpMax = hpNeeded;
        // largest single engine HP known today is less than 15,000.
        // high end modern diesel locos approximately 5000 HP.
        // 100 car train at 100 tons per car and 2 HPT requires 20,000 HP.
        // will assign consisted engines to train.
        boolean foundLoco = false;
        List<Engine> rejectedLocos = new ArrayList<>();
        hpLoop: while (hpMax < 20000) {
            hpMax += hpNeeded / 2; // start off looking for an engine with no
                                   // more than 50% extra HP
            log.debug("Max hp {}", hpMax);
            for (Engine engine : _engineList) {
                if (rejectedLocos.contains(engine)) {
                    continue;
                }
                // don't use non lead locos in a consist
                if (engine.getConsist() != null && !engine.isLead()) {
                    continue;
                }
                if (engine.getLocation() != rl.getLocation()) {
                    continue;
                }
                if (!model.equals(Train.NONE) && !engine.getModel().equals(model)) {
                    continue;
                }
                if (!road.equals(Train.NONE) && !engine.getRoadName().equals(road) ||
                        road.equals(Train.NONE) && !_train.isLocoRoadNameAccepted(engine.getRoadName())) {
                    continue;
                }
                int engineHp = engine.getHpInteger();
                if (engine.getConsist() != null) {
                    for (Engine e : engine.getConsist().getEngines()) {
                        if (e != engine) {
                            engineHp = engineHp + e.getHpInteger();
                        }
                    }
                }
                if (engineHp > hpNeeded && engineHp <= hpMax) {
                    addLine(_buildReport, FIVE,
                            Bundle.getMessage("buildLocoHasRequiredHp", engine.toString(), engineHp, hpNeeded));
                    if (setEngineDestination(engine, rl, rld)) {
                        foundLoco = true;
                        break hpLoop;
                    } else {
                        rejectedLocos.add(engine);
                    }
                }
            }
        }
        if (!foundLoco && !_train.isBuildConsistEnabled()) {
            throw new BuildFailedException(Bundle.getMessage("buildErrorEngHp", rl.getLocation().getName()));
        }
    }

    protected void removeEngineFromTrain(Engine engine) {
        // replace lead engine?
        if (_train.getLeadEngine() == engine) {
            _train.setLeadEngine(null);
        }
        if (engine.getConsist() != null) {
            for (Engine e : engine.getConsist().getEngines()) {
                removeRollingStockFromTrain(e);
            }
        } else {
            removeRollingStockFromTrain(engine);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainBuilderBase.class);

}
