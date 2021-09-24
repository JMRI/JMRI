package jmri.jmrit.operations.trains;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.ScheduleItem;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a train and then creates the train's manifest.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2011, 2012, 2013,
 *         2014, 2015, 2021
 */
public class TrainBuilder extends TrainBuilderBase {

    /**
     * Build rules:
     * <ol>
     * <li>Need at least one location in route to build train
     * <li>Select only locos and cars that the train can service
     * <li>If required, add caboose or car with FRED to train
     * <li>When departing staging find a track matching train requirements
     * <li>All cars and locos on one track must leave staging
     * <li>Optionally block cars from staging
     * <li>Route cars with home divisions
     * <li>Route cars with custom loads or final destinations.
     * <li>Service locations based on train direction, location car types, roads and
     * loads.
     * <li>Ignore track direction when train is a local (serves one location)
     * </ol>
     *
     * @param train the train that is to be built
     * @return True if successful.
     *
     */
    public boolean build(Train train) {
        this._train = train;
        try {
            build();
            return true;
        } catch (BuildFailedException e) {
            buildFailed(e);
            return false;
        }
    }

    private void build() throws BuildFailedException {
        _startTime = new Date();

        log.debug("Building train ({})", _train.getName());

        _train.setStatusCode(Train.CODE_BUILDING);
        _train.setBuilt(false);
        _train.setLeadEngine(null);

        createBuildReportFile(); // backup previous build report and create new build report file
        addBuildReportInfo(); // add the build report header information
        setUpRoute(); // load route, departure and terminate locations
        showTrainBuildOptions(); // show the build options
        showSpecificTrainBuildOptions(); // show the build options for this train
        showAndInitializeTrainRoute(); // show the train's route and initialize it
        showIfLocalSwitcher(); // show if this train a switcher, a train that works only one location
        showTrainRequirements(); // show how many engines, caboose, car with FRED and changes in the route
        showTrainServices(); // show which roads, owners, built dates, and engine types
        getAndRemoveEnginesFromList(); // get a list of available engines
        determineIfTrainTerminatesIntoStaging(); // find a terminus track in staging for this train
        determineIfTrainDepartsStagingAndAddEngines(); // assign engines to train if departing staging
        addEngines(); // 1st, 2nd and 3rd engine swaps in a train's route
        showTrainCarTypes(); // show car types that this train will service
        showTrainLoadNames(); // show load names that this train will service
        loadCarList(); // remove unwanted cars
        adjustCarsInStaging(); // adjust for cars on one staging track
        listCarsByLocation(); // list available cars by location
        sortCarsOnFifoLifoTracks(); // sort cars on FIFO or LIFO tracks
        addCabooseOrFredToTrain(); // do all caboose and FRED changes in the train's route
        removeCaboosesAndCarsWithFred(); // done assigning cabooses and cars with FRED, remove the rest
        saveCarFinalDestinations(); // save car's final destination and schedule id in case of train reset
        blockCarsFromStaging(); // optionally block cars from staging by setting destinations
        addCarsToTrain(); // finds and adds cars to the train, throws BuildFailedException
        checkStuckCarsInStaging(); // determine if cars are stuck in staging, throws BuildFailedException
        showTrainBuildStatus(); // show how well the build went with regards to cars requested and actual
        checkEngineHP(); // check that engine assigned to the train has the appropriate HP
        checkNumnberOfEnginesNeededHPT(); // check to see if additional engines are needed for this train
        showCarsNotRoutable(); // list cars that couldn't be routed

        // done building
        if (_warnings > 0) {
            addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildWarningMsg"),
                    new Object[] { _train.getName(), _warnings }));
        }
        addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTime"),
                new Object[] { _train.getName(), new Date().getTime() - _startTime.getTime() }));

        _buildReport.flush();
        _buildReport.close();

        createManifests(); // now make Manifests

        // notify locations have been modified by this train's build
        for (Location location : _modifiedLocations) {
            location.setStatus(Location.MODIFIED);
        }

        // operations automations use wait for train built to create custom manifests
        // and switch lists
        _train.setCurrentLocation(_train.getTrainDepartsRouteLocation());
        _train.setBuilt(true);
        _train.moveTrainIcon(_train.getTrainDepartsRouteLocation()); // create and place train icon

        log.debug("Done building train ({})", _train.getName());
        showWarningMessage();
    }

    /**
     * Figures out if the train terminates into staging, and if true, sets the
     * termination track. Note if the train is returning back to the same track in
     * staging _terminateStageTrack is null, and is loaded later when the departure
     * track is determined.
     * 
     * @throws BuildFailedException if staging track can't be found
     */
    private void determineIfTrainTerminatesIntoStaging() throws BuildFailedException {
        // does train terminate into staging?
        _terminateStageTrack = null;
        List<Track> stagingTracksTerminate = _terminateLocation.getTracksByMovesList(Track.STAGING);
        if (stagingTracksTerminate.size() > 0) {
            addLine(_buildReport, THREE, BLANK_LINE);
            addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTerminateStaging"),
                    new Object[] { _terminateLocation.getName(), Integer.toString(stagingTracksTerminate.size()) }));
            if (stagingTracksTerminate.size() > 1 && Setup.isStagingPromptToEnabled()) {
                _terminateStageTrack = promptToStagingDialog();
                _startTime = new Date(); // reset build time since user can take awhile to pick
            } else {
                // is this train returning to the same staging in aggressive mode?
                if (_departLocation == _terminateLocation &&
                        Setup.isBuildAggressive() &&
                        Setup.isStagingTrackImmediatelyAvail()) {
                    addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildStagingReturn"),
                            new Object[] { _terminateLocation.getName() }));
                } else {
                    for (Track track : stagingTracksTerminate) {
                        _terminateStageTrack = track;
                        if (checkTerminateStagingTrack(_terminateStageTrack)) {
                            addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildStagingAvail"),
                                    new Object[] { _terminateStageTrack.getName(), _terminateLocation.getName() }));
                            break;
                        }
                        _terminateStageTrack = null;
                    }
                }
            }
            if (_terminateStageTrack == null) {
                // is this train returning to the same staging in aggressive mode?
                if (_departLocation == _terminateLocation &&
                        Setup.isBuildAggressive() &&
                        Setup.isStagingTrackImmediatelyAvail()) {
                    log.debug("Train is returning to same track in staging");
                } else {
                    addLine(_buildReport, ONE, Bundle.getMessage("buildErrorStagingFullNote"));
                    throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorStagingFull"),
                            new Object[] { _terminateLocation.getName() }));
                }
            }
        }
    }

    /**
     * Figures out if the train is departing staging, and if true, sets the
     * departure track. Also sets the arrival track if the train is returning to the
     * same departure track in staging.
     * 
     * @throws BuildFailedException if staging departure track not found
     */
    private void determineIfTrainDepartsStagingAndAddEngines() throws BuildFailedException {
        // allow up to two engine and caboose swaps in the train's route
        RouteLocation engineTerminatesFirstLeg = _train.getTrainTerminatesRouteLocation();

        // Adjust where the locos will terminate
        if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                _train.getSecondLegStartRouteLocation() != null) {
            engineTerminatesFirstLeg = _train.getSecondLegStartRouteLocation();
        }
        if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                _train.getThirdLegStartRouteLocation() != null) {
            if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) != Train.CHANGE_ENGINES) {
                engineTerminatesFirstLeg = _train.getThirdLegStartRouteLocation();
            }
        }

        // determine if train is departing staging
        List<Track> stagingTracks = _departLocation.getTracksByMovesList(Track.STAGING);
        if (stagingTracks.size() > 0) {
            addLine(_buildReport, THREE, BLANK_LINE);
            addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildDepartStaging"),
                    new Object[] { _departLocation.getName(), Integer.toString(stagingTracks.size()) }));
            if (stagingTracks.size() > 1 && Setup.isStagingPromptFromEnabled()) {
                setDepartureTrack(promptFromStagingDialog());
                _startTime = new Date(); // restart build timer
                if (_departStageTrack == null) {
                    showTrainRequirements();
                    throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorStagingEmpty"),
                            new Object[] { _departLocation.getName() }));
                }
            } else {
                for (Track track : stagingTracks) {
                    addLine(_buildReport, THREE,
                            MessageFormat.format(Bundle.getMessage("buildStagingHas"),
                                    new Object[] { track.getName(), Integer.toString(track.getNumberEngines()),
                                            Integer.toString(track.getNumberCars()) }));
                    // is the departure track available?
                    if (!checkDepartureStagingTrack(track)) {
                        addLine(_buildReport, SEVEN,
                                MessageFormat.format(Bundle.getMessage("buildStagingTrackRestriction"),
                                        new Object[] { track.getName(), _train.getName() }));
                        continue;
                    }
                    setDepartureTrack(track);
                    // try each departure track for the required engines
                    if (getEngines(_train.getNumberEngines(), _train.getEngineModel(), _train.getEngineRoad(),
                            _train.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)) {
                        addLine(_buildReport, SEVEN, Bundle.getMessage("buildDoneAssignEnginesStaging"));
                        break; // done!
                    }
                    setDepartureTrack(null);
                }
            }
            if (_departStageTrack == null) {
                showTrainRequirements();
                throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorStagingEmpty"),
                        new Object[] { _departLocation.getName() }));
            }
        }
        _train.setTerminationTrack(_terminateStageTrack);
        _train.setDepartureTrack(_departStageTrack);
    }

    /**
     * Adds engines to the train starting at the first location in the train's
     * route. Note that engines from staging are already part of the train. There
     * can be up to two engine swaps in a train's route.
     * 
     * @throws BuildFailedException if required engines can't be added to train.
     */
    private void addEngines() throws BuildFailedException {
        // allow up to two engine and caboose swaps in the train's route
        RouteLocation engineTerminatesFirstLeg = _train.getTrainTerminatesRouteLocation();
        RouteLocation engineTerminatesSecondLeg = _train.getTrainTerminatesRouteLocation();

        // Adjust where the locos will terminate
        if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                _train.getSecondLegStartRouteLocation() != null) {
            engineTerminatesFirstLeg = _train.getSecondLegStartRouteLocation();
        }
        if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                _train.getThirdLegStartRouteLocation() != null) {
            engineTerminatesSecondLeg = _train.getThirdLegStartRouteLocation();
            // No engine or caboose change at first leg?
            if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) != Train.CHANGE_ENGINES) {
                engineTerminatesFirstLeg = _train.getThirdLegStartRouteLocation();
            }
        }

        // load engines at the start of the route for this train
        if (_train.getLeadEngine() == null) {
            addLine(_buildReport, THREE, BLANK_LINE);
            if (getEngines(_train.getNumberEngines(), _train.getEngineModel(), _train.getEngineRoad(),
                    _train.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)) {
                _secondLeadEngine = _lastEngine; // when adding a caboose later in the route, no engine change
                _thirdLeadEngine = _lastEngine;
            } else if (getConsist(_train.getNumberEngines(), _train.getEngineModel(), _train.getEngineRoad(),
                    _train.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)) {
                _secondLeadEngine = _lastEngine; // when adding a caboose later in the route, no engine change
                _thirdLeadEngine = _lastEngine;
            } else {
                throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorEngines"),
                        new Object[] { _train.getNumberEngines(), _train.getTrainDepartsName(),
                                engineTerminatesFirstLeg.getName() }));
            }
        }

        // First engine change in route?
        if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
            addLine(_buildReport, THREE, BLANK_LINE);
            addLine(_buildReport, THREE,
                    MessageFormat.format(Bundle.getMessage("buildTrainEngineChange"),
                            new Object[] { _train.getSecondLegStartLocationName(), _train.getSecondLegNumberEngines(),
                                    _train.getSecondLegEngineModel(), _train.getSecondLegEngineRoad() }));
            if (getEngines(_train.getSecondLegNumberEngines(), _train.getSecondLegEngineModel(),
                    _train.getSecondLegEngineRoad(), _train.getSecondLegStartRouteLocation(),
                    engineTerminatesSecondLeg)) {
                _secondLeadEngine = _lastEngine;
                _thirdLeadEngine = _lastEngine;
            } else if (getConsist(_train.getSecondLegNumberEngines(), _train.getSecondLegEngineModel(),
                    _train.getSecondLegEngineRoad(), _train.getSecondLegStartRouteLocation(),
                    engineTerminatesSecondLeg)) {
                _secondLeadEngine = _lastEngine;
                _thirdLeadEngine = _lastEngine;
            } else {
                throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorEngines"),
                        new Object[] { _train.getSecondLegNumberEngines(), _train.getSecondLegStartRouteLocation(),
                                engineTerminatesSecondLeg }));
            }
        }
        // Second engine change in route?
        if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
            addLine(_buildReport, THREE, BLANK_LINE);
            addLine(_buildReport, THREE,
                    MessageFormat.format(Bundle.getMessage("buildTrainEngineChange"),
                            new Object[] { _train.getThirdLegStartLocationName(), _train.getThirdLegNumberEngines(),
                                    _train.getThirdLegEngineModel(), _train.getThirdLegEngineRoad() }));
            if (getEngines(_train.getThirdLegNumberEngines(), _train.getThirdLegEngineModel(),
                    _train.getThirdLegEngineRoad(), _train.getThirdLegStartRouteLocation(),
                    _train.getTrainTerminatesRouteLocation())) {
                _thirdLeadEngine = _lastEngine;
            } else if (getConsist(_train.getThirdLegNumberEngines(), _train.getThirdLegEngineModel(),
                    _train.getThirdLegEngineRoad(), _train.getThirdLegStartRouteLocation(),
                    _train.getTrainTerminatesRouteLocation())) {
                _thirdLeadEngine = _lastEngine;
            } else {
                throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorEngines"),
                        new Object[] { Integer.parseInt(_train.getThirdLegNumberEngines()),
                                _train.getThirdLegStartRouteLocation(), _train.getTrainTerminatesRouteLocation() }));
            }
        }
        if (!_train.getNumberEngines().equals("0") &&
                (!_train.isBuildConsistEnabled() || Setup.getHorsePowerPerTon() == 0)) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDoneAssingEnginesTrain"),
                    new Object[] { _train.getName() }));
        }
    }

    /**
     * Adds and removes cabooses or car with FRED in the train's route. Up to 2
     * caboose changes.
     * 
     * @throws BuildFailedException
     */
    private void addCabooseOrFredToTrain() throws BuildFailedException {
        // allow up to two caboose swaps in the train's route
        RouteLocation cabooseOrFredTerminatesFirstLeg = _train.getTrainTerminatesRouteLocation();
        RouteLocation cabooseOrFredTerminatesSecondLeg = _train.getTrainTerminatesRouteLocation();

        // determine if there are any caboose changes
        if ((_train.getSecondLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (_train.getSecondLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            cabooseOrFredTerminatesFirstLeg = _train.getSecondLegStartRouteLocation();
        } else if ((_train.getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (_train.getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            cabooseOrFredTerminatesFirstLeg = _train.getThirdLegStartRouteLocation();
        }
        if ((_train.getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (_train.getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            cabooseOrFredTerminatesSecondLeg = _train.getThirdLegStartRouteLocation();
        }

        // Do caboose changes in reverse order in case there isn't enough track space
        // second caboose change?
        if ((_train.getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE &&
                _train.getThirdLegStartRouteLocation() != null &&
                _train.getTrainTerminatesRouteLocation() != null) {
            getCaboose(_train.getThirdLegCabooseRoad(), _thirdLeadEngine, _train.getThirdLegStartRouteLocation(),
                    _train.getTrainTerminatesRouteLocation(), true);
        }

        // first caboose change?
        if ((_train.getSecondLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE &&
                _train.getSecondLegStartRouteLocation() != null &&
                cabooseOrFredTerminatesSecondLeg != null) {
            getCaboose(_train.getSecondLegCabooseRoad(), _secondLeadEngine, _train.getSecondLegStartRouteLocation(),
                    cabooseOrFredTerminatesSecondLeg, true);
        }

        // departure caboose or car with FRED
        getCaboose(_train.getCabooseRoad(), _train.getLeadEngine(), _train.getTrainDepartsRouteLocation(),
                cabooseOrFredTerminatesFirstLeg, _train.isCabooseNeeded());
        getCarWithFred(_train.getCabooseRoad(), _train.getTrainDepartsRouteLocation(), cabooseOrFredTerminatesFirstLeg);
    }

    /**
     * Find a caboose if needed at the correct location and add it to the train. If
     * departing staging, all cabooses are added to the train. If there isn't a road
     * name required for the caboose, tries to find a caboose with the same road
     * name as the lead engine.
     *
     * @param roadCaboose     Optional road name for this car.
     * @param leadEngine      The lead engine for this train. Used to find a caboose
     *                        with the same road name as the engine.
     * @param rl              Where in the route to pick up this car.
     * @param rld             Where in the route to set out this car.
     * @param requiresCaboose When true, the train requires a caboose.
     * @throws BuildFailedException If car not found.
     */
    private void getCaboose(String roadCaboose, Engine leadEngine, RouteLocation rl, RouteLocation rld,
            boolean requiresCaboose) throws BuildFailedException {
        // code check
        if (rl == null) {
            throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorCabooseNoLocation"),
                    new Object[] { _train.getName() }));
        }
        // code check
        if (rld == null) {
            throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorCabooseNoDestination"),
                    new Object[] { _train.getName(), rl.getName() }));
        }
        // load departure track if staging
        Track departTrack = null;
        if (rl == _train.getTrainDepartsRouteLocation()) {
            departTrack = _departStageTrack; // can be null
        }
        if (!requiresCaboose) {
            addLine(_buildReport, FIVE,
                    MessageFormat.format(Bundle.getMessage("buildTrainNoCaboose"), new Object[] { rl.getName() }));
            if (departTrack == null) {
                return;
            }
        } else {
            addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainReqCaboose"),
                    new Object[] { _train.getName(), roadCaboose, rl.getName(), rld.getName() }));
        }

        // Now go through the car list looking for cabooses
        boolean cabooseTip = true; // add a user tip to the build report about cabooses if none found
        boolean cabooseAtDeparture = false; // set to true if caboose at departure location is found
        boolean foundCaboose = false;
        for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
            Car car = _carList.get(_carIndex);
            if (!car.isCaboose()) {
                continue;
            }
            showCarServiceOrder(car);

            cabooseTip = false; // found at least one caboose, so they exist!
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarIsCaboose"),
                    new Object[] { car.toString(), car.getRoadName(), car.getLocationName(), car.getTrackName() }));
            // car departing staging must leave with train
            if (car.getTrack() == departTrack) {
                foundCaboose = false;
                if (!generateCarLoadFromStaging(car, rld)) {
                    // departing and terminating into staging?
                    if (car.getTrack().isAddCustomLoadsAnyStagingTrackEnabled() &&
                            rld.getLocation() == _terminateLocation &&
                            _terminateStageTrack != null) {
                        // try and generate a custom load for this caboose
                        generateLoadCarDepartingAndTerminatingIntoStaging(car, _terminateStageTrack);
                    }
                }
                if (checkAndAddCarForDestinationAndTrack(car, rl, rld)) {
                    if (car.getTrain() == _train) {
                        foundCaboose = true;
                    }
                } else if (findDestinationAndTrack(car, rl, rld)) {
                    foundCaboose = true;
                }
                if (!foundCaboose) {
                    throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorCarStageDest"),
                            new Object[] { car.toString() }));
                }
                // is there a specific road requirement for the caboose?
            } else if (!roadCaboose.equals(Train.NONE) && !roadCaboose.equals(car.getRoadName())) {
                continue;
            } else if (!foundCaboose && car.getLocationName().equals(rl.getName())) {
                // remove cars that can't be picked up due to train and track directions
                if (!checkPickUpTrainDirection(car, rl)) {
                    addLine(_buildReport, SEVEN,
                            MessageFormat.format(Bundle.getMessage("buildExcludeCarTypeAtLoc"),
                                    new Object[] { car.toString(), car.getTypeName(),
                                            (car.getLocationName() + " " + car.getTrackName()) }));
                    _carList.remove(car); // remove this car from the list
                    _carIndex--;
                    continue;
                }
                // first pass, find a caboose that matches the engine road
                if (leadEngine != null && car.getRoadName().equals(leadEngine.getRoadName())) {
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCabooseRoadMatches"),
                            new Object[] { car.toString(), car.getRoadName(), leadEngine.toString() }));
                    if (checkAndAddCarForDestinationAndTrack(car, rl, rld)) {
                        if (car.getTrain() == _train) {
                            foundCaboose = true;
                        }
                    } else if (findDestinationAndTrack(car, rl, rld)) {
                        foundCaboose = true;
                    }
                    if (!foundCaboose) {
                        _carList.remove(car); // remove this car from the list
                        _carIndex--;
                        continue;
                    }
                }
                // done if we found a caboose and not departing staging
                if (foundCaboose && departTrack == null) {
                    break;
                }
            }
        }
        // second pass, take a caboose with a road name that is "similar" (hyphen
        // feature) to the engine road name
        if (requiresCaboose && !foundCaboose && roadCaboose.equals(Train.NONE)) {
            log.debug("Second pass looking for caboose");
            for (Car car : _carList) {
                if (car.isCaboose() && car.getLocationName().equals(rl.getName())) {
                    if (leadEngine != null &&
                            TrainCommon.splitString(car.getRoadName())
                                    .equals(TrainCommon.splitString(leadEngine.getRoadName()))) {
                        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCabooseRoadMatches"),
                                new Object[] { car.toString(), car.getRoadName(), leadEngine.toString() }));
                        if (checkAndAddCarForDestinationAndTrack(car, rl, rld)) {
                            if (car.getTrain() == _train) {
                                foundCaboose = true;
                                break;
                            }
                        } else if (findDestinationAndTrack(car, rl, rld)) {
                            foundCaboose = true;
                            break;
                        }
                    }
                }
            }
        }
        // third pass, take any caboose unless a caboose road name is specified
        if (requiresCaboose && !foundCaboose) {
            log.debug("Third pass looking for caboose");
            for (Car car : _carList) {
                if (!car.isCaboose()) {
                    continue;
                }
                if (car.getLocationName().equals(rl.getName())) {
                    // is there a specific road requirement for the caboose?
                    if (!roadCaboose.equals(Train.NONE) && !roadCaboose.equals(car.getRoadName())) {
                        continue; // yes
                    }
                    // okay, we found a caboose at the departure location
                    cabooseAtDeparture = true;
                    if (checkAndAddCarForDestinationAndTrack(car, rl, rld)) {
                        if (car.getTrain() == _train) {
                            foundCaboose = true;
                            break;
                        }
                    } else if (findDestinationAndTrack(car, rl, rld)) {
                        foundCaboose = true;
                        break;
                    }
                }
            }
        }
        if (requiresCaboose && !foundCaboose) {
            if (cabooseTip) {
                addLine(_buildReport, ONE, Bundle.getMessage("buildNoteCaboose"));
                addLine(_buildReport, ONE, Bundle.getMessage("buildNoteCaboose2"));
            }
            if (!cabooseAtDeparture) {
                throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorReqDepature"),
                        new Object[] { _train.getName(), Bundle.getMessage("Caboose").toLowerCase(), rl.getName() }));
            }
            // we did find a caboose at departure that meet requirements, but couldn't place
            // it at destination.
            throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorReqDest"),
                    new Object[] { _train.getName(), Bundle.getMessage("Caboose"), rld.getName() }));
        }
    }

    /**
     * Find a car with FRED if needed at the correct location and adds the car to
     * the train. If departing staging, will make sure all cars with FRED are added
     * to the train.
     *
     * @param road Optional road name for this car.
     * @param rl   Where in the route to pick up this car.
     * @param rld  Where in the route to set out this car.
     * @throws BuildFailedException If car not found.
     */
    private void getCarWithFred(String road, RouteLocation rl, RouteLocation rld) throws BuildFailedException {
        // load departure track if staging
        Track departTrack = null;
        if (rl == _train.getTrainDepartsRouteLocation()) {
            departTrack = _departStageTrack;
        }
        boolean foundCarWithFred = false;
        if (_train.isFredNeeded()) {
            addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildTrainReqFred"),
                    new Object[] { _train.getName(), road, rl.getName(), rld.getName() }));
        } else {
            addLine(_buildReport, FIVE, Bundle.getMessage("buildTrainNoFred"));
            // if not departing staging we're done
            if (departTrack == null) {
                return;
            }
        }
        for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
            Car car = _carList.get(_carIndex);
            if (!car.hasFred()) {
                continue;
            }
            showCarServiceOrder(car);
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarHasFRED"),
                    new Object[] { car.toString(), car.getRoadName(), car.getLocationName() }));
            // all cars with FRED departing staging must leave with train
            if (car.getTrack() == departTrack) {
                foundCarWithFred = false;
                if (!generateCarLoadFromStaging(car, rld)) {
                    // departing and terminating into staging?
                    if (car.getTrack().isAddCustomLoadsAnyStagingTrackEnabled() &&
                            rld.getLocation() == _terminateLocation &&
                            _terminateStageTrack != null) {
                        // try and generate a custom load for this car with FRED
                        generateLoadCarDepartingAndTerminatingIntoStaging(car, _terminateStageTrack);
                    }
                }
                if (checkAndAddCarForDestinationAndTrack(car, rl, rld)) {
                    if (car.getTrain() == _train) {
                        foundCarWithFred = true;
                    }
                } else if (findDestinationAndTrack(car, rl, rld)) {
                    foundCarWithFred = true;
                }
                if (!foundCarWithFred) {
                    throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorCarStageDest"),
                            new Object[] { car.toString() }));
                }
            } // is there a specific road requirement for the car with FRED?
            else if (!road.equals(Train.NONE) && !road.equals(car.getRoadName())) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildExcludeCarWrongRoad"),
                        new Object[] { car.toString(), car.getTypeName(), car.getRoadName() }));
                _carList.remove(car); // remove this car from the list
                _carIndex--;
                continue;
            } else if (!foundCarWithFred && car.getLocationName().equals(rl.getName())) {
                // remove cars that can't be picked up due to train and track directions
                if (!checkPickUpTrainDirection(car, rl)) {
                    addLine(_buildReport, SEVEN,
                            MessageFormat.format(Bundle.getMessage("buildExcludeCarTypeAtLoc"),
                                    new Object[] { car.toString(), car.getTypeName(),
                                            (car.getLocationName() + " " + car.getTrackName()) }));
                    _carList.remove(car); // remove this car from the list
                    _carIndex--;
                    continue;
                }
                if (checkAndAddCarForDestinationAndTrack(car, rl, rld)) {
                    if (car.getTrain() == _train) {
                        foundCarWithFred = true;
                    }
                } else if (findDestinationAndTrack(car, rl, rld)) {
                    foundCarWithFred = true;
                }
                if (foundCarWithFred && departTrack == null) {
                    break;
                }
            }
        }
        if (_train.isFredNeeded() && !foundCarWithFred) {
            throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorRequirements"),
                    new Object[] { _train.getName(), Bundle.getMessage("FRED"), rl.getName(), rld.getName() }));
        }
    }

    /**
     * Determine if caboose or car with FRED was given a destination and track. Need
     * to check if there's been a train assignment.
     * 
     * @param car the car in question
     * @param rl  car's route location
     * @param rld car's route location destination
     * @return true if car has a destination. Need to check if there's been a train
     *         assignment.
     * @throws BuildFailedException if destination was staging and can't place car
     *                              there
     */
    private boolean checkAndAddCarForDestinationAndTrack(Car car, RouteLocation rl, RouteLocation rld)
            throws BuildFailedException {
        return checkCarForDestination(car, rl, _routeList.indexOf(rld));
    }

    /**
     * Optionally block cars departing staging. No guarantee that cars departing
     * staging can be blocked by destination. By using the pick up location id, this
     * routine tries to find destinations that are willing to accepts all of the
     * cars that were "blocked" together when they were picked up. Rules: The route
     * must allow set outs at the destination. The route must allow the correct
     * number of set outs. The destination must accept all cars in the pick up
     * block.
     *
     * @throws BuildFailedException if blocking fails
     */
    private void blockCarsFromStaging() throws BuildFailedException {
        if (_departStageTrack == null || !_departStageTrack.isBlockCarsEnabled()) {
            return;
        }

        addLine(_buildReport, THREE, BLANK_LINE);
        addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("blockDepartureHasBlocks"),
                new Object[] { _departStageTrack.getName(), _numOfBlocks.size() }));

        Enumeration<String> en = _numOfBlocks.keys();
        while (en.hasMoreElements()) {
            String locId = en.nextElement();
            int numCars = _numOfBlocks.get(locId);
            String locName = "";
            Location l = locationManager.getLocationById(locId);
            if (l != null) {
                locName = l.getName();
            }
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockFromHasCars"),
                    new Object[] { locId, locName, numCars }));
            if (_numOfBlocks.size() < 2) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("blockUnable"));
                return;
            }
        }
        blockByLocationMoves();
        addLine(_buildReport, SEVEN,
                MessageFormat.format(Bundle.getMessage("blockDone"), new Object[] { _departStageTrack.getName() }));
    }

    /**
     * Blocks cars out of staging by assigning the largest blocks of cars to
     * locations requesting the most moves.
     * 
     * @throws BuildFailedException
     */
    private void blockByLocationMoves() throws BuildFailedException {
        List<RouteLocation> blockRouteList = _train.getRoute().getLocationsBySequenceList();
        for (RouteLocation rl : blockRouteList) {
            // start at the second location in the route to begin blocking
            if (rl == _train.getTrainDepartsRouteLocation()) {
                continue;
            }
            int possibleMoves = rl.getMaxCarMoves() - rl.getCarMoves();
            if (rl.isDropAllowed() && possibleMoves > 0) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockLocationHasMoves"),
                        new Object[] { rl.getName(), possibleMoves }));
            }
        }
        // now block out cars, send the largest block of cars to the locations
        // requesting the greatest number of moves
        while (true) {
            String blockId = getLargestBlock(); // get the id of the largest block of cars
            if (blockId.isEmpty() || _numOfBlocks.get(blockId) == 1) {
                break; // done
            }
            // get the remaining location with the greatest number of moves
            RouteLocation rld = getLocationWithMaximumMoves(blockRouteList, blockId);
            if (rld == null) {
                break; // done
            }
            // check to see if there are enough moves for all of the cars departing staging
            if (rld.getMaxCarMoves() > _numOfBlocks.get(blockId)) {
                // remove the largest block and maximum moves RouteLocation from the lists
                _numOfBlocks.remove(blockId);
                // block 0 cars have never left staging.
                if (blockId.equals(Car.LOCATION_UNKNOWN)) {
                    continue;
                }
                blockRouteList.remove(rld);
                Location loc = locationManager.getLocationById(blockId);
                Location setOutLoc = rld.getLocation();
                if (loc != null && setOutLoc != null && checkDropTrainDirection(rld)) {
                    for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
                        Car car = _carList.get(_carIndex);
                        if (car.getTrack() == _departStageTrack && car.getLastLocationId().equals(blockId)) {
                            if (car.getDestination() != null) {
                                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockNotAbleDest"),
                                        new Object[] { car.toString(), car.getDestinationName() }));
                                continue; // can't block this car
                            }
                            if (car.getFinalDestination() != null) {
                                addLine(_buildReport, SEVEN,
                                        MessageFormat.format(Bundle.getMessage("blockNotAbleFinalDest"),
                                                new Object[] { car.toString(), car.getFinalDestination().getName() }));
                                continue; // can't block this car
                            }
                            if (!car.getLoadName().equals(carLoads.getDefaultEmptyName()) &&
                                    !car.getLoadName().equals(carLoads.getDefaultLoadName())) {
                                addLine(_buildReport, SEVEN,
                                        MessageFormat.format(Bundle.getMessage("blockNotAbleCustomLoad"),
                                                new Object[] { car.toString(), car.getLoadName() }));
                                continue; // can't block this car
                            }
                            if (car.getLoadName().equals(carLoads.getDefaultEmptyName()) &&
                                    (_departStageTrack.isAddCustomLoadsEnabled() ||
                                            _departStageTrack.isAddCustomLoadsAnySpurEnabled() ||
                                            _departStageTrack.isAddCustomLoadsAnyStagingTrackEnabled())) {
                                addLine(_buildReport, SEVEN,
                                        MessageFormat.format(Bundle.getMessage("blockNotAbleCarTypeGenerate"),
                                                new Object[] { car.toString(), car.getLoadName() }));
                                continue; // can't block this car
                            }
                            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockingCar"),
                                    new Object[] { car.toString(), loc.getName(), rld.getName() }));
                            if (!findDestinationAndTrack(car, _train.getTrainDepartsRouteLocation(), rld)) {
                                addLine(_buildReport, SEVEN,
                                        MessageFormat.format(Bundle.getMessage("blockNotAbleCarType"),
                                                new Object[] { car.toString(), rld.getName(), car.getTypeName() }));
                            }
                        }
                    }
                }
            } else {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("blockDestNotEnoughMoves"),
                        new Object[] { rld.getName(), blockId }));
                _numOfBlocks.remove(blockId); // block is too large for any stop along this train's route
            }
        }
    }

    /**
     * Routine to find and add available cars to the train. In normal mode performs
     * a single pass. In aggressive mode, will perform multiple passes. If train is
     * departing staging and in aggressive mode, will try again using normal mode if
     * there's a train build issue.
     * 
     * @throws BuildFailedException
     */
    private void addCarsToTrain() throws BuildFailedException {
        addLine(_buildReport, THREE, BLANK_LINE);
        addLine(_buildReport, THREE, MessageFormat.format(Bundle.getMessage("buildTrain"),
                new Object[] { _train.getNumberCarsRequested(), _train.getName(), _carList.size() }));

        if (Setup.isBuildAggressive() && !_train.isBuildTrainNormalEnabled()) {
            // perform a multiple pass build for this train, default is two passes
            int passes = 0;
            boolean firstPass = true;
            while (passes++ < Setup.getNumberPasses()) {
                placeCars(PERCENT_100 * passes / Setup.getNumberPasses(), firstPass);
                firstPass = false;
            }
            // are cars stuck in staging?
            secondAttemptNormalBuild();
        } else {
            placeCars(PERCENT_100, false); // normal build one pass
        }
    }

    /**
     * If cars stuck in staging, try building again in normal mode.
     * 
     * @throws BuildFailedException
     */
    private void secondAttemptNormalBuild() throws BuildFailedException {
        if (Setup.isStagingTryNormalBuildEnabled() && isCarStuckStaging()) {
            addLine(_buildReport, ONE, Bundle.getMessage("buildFailedTryNormalMode"));
            addLine(_buildReport, ONE, BLANK_LINE);
            _train.reset();
            _train.setStatusCode(Train.CODE_BUILDING);
            _train.setLeadEngine(null);
            showAndInitializeTrainRoute();
            getAndRemoveEnginesFromList();
            addEngines();
            loadCarList();
            adjustCarsInStaging();
            listCarsByLocation();
            addCabooseOrFredToTrain();
            removeCaboosesAndCarsWithFred();
            saveCarFinalDestinations(); // save final destination and schedule id
            blockCarsFromStaging(); // block cars from staging
            placeCars(PERCENT_100, false); // try normal build one pass
        }
    }

    boolean _multipass = false;

    /**
     * Main routine to place cars into the train. Can be called multiple times,
     * percent controls how many cars are placed in any given pass. When departing
     * staging, ignore staged cars on the first pass unless the option to build
     * normal was selected by user.
     *
     * @param percent   How much of the available moves should be used in this pass.
     * @param firstPass True if first pass, ignore cars in staging.
     * @throws BuildFailedException
     */
    private void placeCars(int percent, boolean firstPass) throws BuildFailedException {
        addLine(_buildReport, THREE, BLANK_LINE);
        if (percent < PERCENT_100) {
            addLine(_buildReport, THREE,
                    MessageFormat.format(Bundle.getMessage("buildMultiplePass"), new Object[] { percent }));
            _multipass = true;
        }
        if (percent == PERCENT_100 && _multipass) {
            addLine(_buildReport, THREE, Bundle.getMessage("buildFinalPass"));
        }
        // now go through each location starting at departure and place cars as
        // requested
        for (RouteLocation rl : _routeList) {
            if (_train.isLocationSkipped(rl.getId())) {
                addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildLocSkipped"),
                        new Object[] { rl.getName(), rl.getId(), _train.getName() }));
                continue;
            }
            if (!rl.isPickUpAllowed()) {
                addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildLocNoPickups"),
                        new Object[] { _train.getRoute().getName(), rl.getId(), rl.getName() }));
                continue;
            }
            // no pick ups from staging unless at the start of the train's route
            if (rl != _train.getTrainDepartsRouteLocation() && rl.getLocation().isStaging()) {
                addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildNoPickupsFromStaging"),
                        new Object[] { rl.getName() }));
                continue;
            }
            // the next check provides a build report message if there's an issue with the
            // train direction
            if (!checkPickUpTrainDirection(rl)) {
                continue;
            }
            _completedMoves = 0; // the number of moves completed for this location
            _success = true; // true when done with this location
            _reqNumOfMoves = (rl.getMaxCarMoves() - rl.getCarMoves()) * percent / 99; // the number of moves requested
            // round up requested moves if less than half way through build. Improves
            // pickups when the move count is small.
            int remainder = (rl.getMaxCarMoves() - rl.getCarMoves()) % (PERCENT_100 / percent);
            if (percent < 51 && remainder > 0) {
                _reqNumOfMoves++;
            }

            // if departing staging make adjustments
            if (rl == _train.getTrainDepartsRouteLocation()) {
                if (firstPass) {
                    makeAdjustmentsIfDepartingStaging();
                } else {
                    restoreCarsIfDepartingStaging();
                }
            }

            int saveReqMoves = _reqNumOfMoves; // save a copy for status message
            addLine(_buildReport, ONE,
                    MessageFormat.format(Bundle.getMessage("buildLocReqMoves"), new Object[] { rl.getName(), rl.getId(),
                            _reqNumOfMoves, rl.getMaxCarMoves() - rl.getCarMoves(), rl.getMaxCarMoves() }));
            addLine(_buildReport, FIVE, BLANK_LINE);

            // show the car load generation options for staging
            if (rl == _train.getTrainDepartsRouteLocation()) {
                showLoadGenerationOptionsStaging();
            }

            _carIndex = 0; // see reportCarsNotMoved(rl) below

            findDestinationsForCarsFromLocation(rl, false); // first pass
            // perform a another pass if aggressive and there are requested moves
            // this will perform local moves at this location, services off spot tracks
            // only in aggressive mode, and at least one car has a new destination
            if (Setup.isBuildAggressive() && saveReqMoves != _reqNumOfMoves) {
                log.debug("Perform extra pass at location ({})", rl.getName());
                // use up to half of the available moves left for this location
                if (_reqNumOfMoves < (rl.getMaxCarMoves() - rl.getCarMoves()) / 2) {
                    _reqNumOfMoves = (rl.getMaxCarMoves() - rl.getCarMoves()) / 2;
                }
                findDestinationsForCarsFromLocation(rl, true); // second pass

                // we might have freed up space at a spur that has an alternate track
                if (redirectCarsFromAlternateTrack()) {
                    addLine(_buildReport, SEVEN, BLANK_LINE);
                }
            }
            if (rl == _train.getTrainDepartsRouteLocation() && percent == PERCENT_100 && isCarStuckStaging()) {
                return; // report ASAP that there are stuck cars
            }
            addLine(_buildReport, ONE,
                    MessageFormat.format(Bundle.getMessage("buildStatusMsg"),
                            new Object[] {
                                    (saveReqMoves <= _completedMoves ? Bundle.getMessage("Success")
                                            : Bundle.getMessage("Partial")),
                                    Integer.toString(_completedMoves), Integer.toString(saveReqMoves), rl.getName(),
                                    _train.getName() }));

            if (_success && percent == PERCENT_100) {
                reportCarsNotMoved(rl);
            }
        }
    }

    /**
     * Attempts to find a destinations for cars departing a specific route location.
     *
     * @param rl           The route location where cars need destinations.
     * @param isSecondPass When true this is the second time we've looked at these
     *                     cars. Used to perform local moves.
     * @throws BuildFailedException
     */
    private void findDestinationsForCarsFromLocation(RouteLocation rl, boolean isSecondPass)
            throws BuildFailedException {
        if (_reqNumOfMoves <= 0) {
            return;
        }
        boolean messageFlag = true;
        boolean foundCar = false;
        _success = false;
        for (_carIndex = 0; _carIndex < _carList.size(); _carIndex++) {
            Car car = _carList.get(_carIndex);
            // second pass deals with cars that have a final destination equal to this
            // location.
            // therefore a local move can be made. This causes "off spots" to be serviced.
            if (isSecondPass && !car.getFinalDestinationName().equals(rl.getName())) {
                continue;
            }
            // find a car at this location
            if (!car.getLocationName().equals(rl.getName())) {
                continue;
            }
            foundCar = true;
            // add message that we're on the second pass for this location
            if (isSecondPass && messageFlag) {
                messageFlag = false;
                addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildExtraPassForLocation"),
                        new Object[] { rl.getName() }));
                addLine(_buildReport, SEVEN, BLANK_LINE);
            }
            // can this car be picked up?
            if (!checkPickUpTrainDirection(car, rl)) {
                addLine(_buildReport, FIVE, BLANK_LINE);
                continue; // no
            }

            showCarServiceOrder(car); // car on FIFO or LIFO track?

            // is car departing staging and generate custom load?
            if (!generateCarLoadFromStaging(car)) {
                if (!generateCarLoadStagingToStaging(car) &&
                        car.getTrack() == _departStageTrack &&
                        !_departStageTrack.isLoadNameAndCarTypeShipped(car.getLoadName(), car.getTypeName())) {
                    // report build failure car departing staging with a restricted load
                    addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildErrorCarStageLoad"),
                            new Object[] { car.toString(), car.getLoadName(), _departStageTrack.getName() }));
                    addLine(_buildReport, FIVE, BLANK_LINE);
                    continue; // keep going and see if there are other cars with issues outs of staging
                }
            }
            // If car been given a home division follow division rules for car movement.
            if (!findDestinationsForCarsWithHomeDivision(car)) {
                continue; // hold car at current location
            }
            // does car have a custom load without a destination?
            // if departing staging, a destination for this car is needed, so keep going
            if (findFinalDestinationForCarLoad(car) &&
                    car.getDestination() == null &&
                    car.getTrack() != _departStageTrack) {
                // done with this car, it has a custom load, and there are spurs/schedules, but
                // no destination found
                addLine(_buildReport, FIVE,
                        MessageFormat.format(Bundle.getMessage("buildNoDestForCar"), new Object[] { car.toString() }));
                addLine(_buildReport, FIVE, BLANK_LINE);
                continue;
            }
            // Check car for final destination, then an assigned destination, if neither,
            // find a destination for the car
            if (checkCarForFinalDestination(car)) {
                log.debug("Car ({}) has a final desination that can't be serviced by train", car.toString());
            } else if (checkCarForDestination(car, rl, _routeList.indexOf(rl))) {
                // car had a destination, could have been added to the train.
                log.debug("Car ({}) has desination ({}) using train ({})", car.toString(), car.getDestinationName(),
                        car.getTrainName());
            } else {
                findDestinationAndTrack(car, rl, _routeList.indexOf(rl), _routeList.size());
            }
            if (_success) {
                break; // done
            }
            // build failure if car departing staging without a destination and a train
            // we'll just put out a warning message here so we can find out how many cars
            // have issues
            if (car.getTrack() == _departStageTrack &&
                    (car.getDestination() == null || car.getDestinationTrack() == null || car.getTrain() == null)) {
                addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildWarningCarStageDest"),
                        new Object[] { car.toString() }));
                // does the car have a final destination to staging? If so we need to reset this
                // car
                if (car.getFinalDestinationTrack() != null && car.getFinalDestinationTrack() == _terminateStageTrack) {
                    addLine(_buildReport, THREE,
                            MessageFormat.format(Bundle.getMessage("buildStagingCarHasFinal"),
                                    new Object[] { car.toString(), car.getFinalDestinationName(),
                                            car.getFinalDestinationTrackName() }));
                    car.reset();
                }
                addLine(_buildReport, SEVEN, BLANK_LINE);
            }
        }
        if (!foundCar && !isSecondPass) {
            addLine(_buildReport, FIVE,
                    MessageFormat.format(Bundle.getMessage("buildNoCarsAtLocation"), new Object[] { rl.getName() }));
            addLine(_buildReport, FIVE, BLANK_LINE);
        }
    }

    private boolean generateCarLoadFromStaging(Car car) throws BuildFailedException {
        return generateCarLoadFromStaging(car, null);
    }

    /**
     * Used to generate a car's load from staging. Search for a spur with a schedule
     * and load car if possible.
     *
     * @param car the car
     * @param rld The route location destination for this car. Can be null.
     * @return true if car given a custom load
     * @throws BuildFailedException If code check fails
     */
    private boolean generateCarLoadFromStaging(Car car, RouteLocation rld) throws BuildFailedException {
        // Code Check, car should have a track assignment
        if (car.getTrack() == null) {
            throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildWarningRsNoTrack"),
                    new Object[] { car.toString(), car.getLocationName() }));
        }
        if (!car.getTrack().isStaging() ||
                (!car.getTrack().isAddCustomLoadsAnySpurEnabled() && !car.getTrack().isAddCustomLoadsEnabled()) ||
                !car.getLoadName().equals(carLoads.getDefaultEmptyName()) ||
                car.getDestination() != null ||
                car.getFinalDestination() != null) {
            log.debug(
                    "No load generation for car ({}) isAddLoadsAnySpurEnabled: {}, car load ({}) destination ({}) final destination ({})",
                    car.toString(), car.getTrack().isAddCustomLoadsAnySpurEnabled() ? "true" : "false",
                    car.getLoadName(), car.getDestinationName(), car.getFinalDestinationName()); // NOI18N
            // if car has a destination or final destination add "no load generated" message
            // to report
            if (car.getTrack().isStaging() &&
                    car.getTrack().isAddCustomLoadsAnySpurEnabled() &&
                    car.getLoadName().equals(carLoads.getDefaultEmptyName())) {
                addLine(_buildReport, FIVE,
                        MessageFormat.format(Bundle.getMessage("buildCarNoLoadGenerated"),
                                new Object[] { car.toString(), car.getLoadName(), car.getDestinationName(),
                                        car.getFinalDestinationName() }));
            }
            return false; // no load generated for this car
        }
        addLine(_buildReport, FIVE,
                MessageFormat.format(Bundle.getMessage("buildSearchTrackNewLoad"),
                        new Object[] { car.toString(), car.getTypeName(), car.getLoadName(), car.getLocationName(),
                                car.getTrackName(), rld != null ? rld.getLocation().getName() : "" }));
        // check to see if car type has custom loads
        if (carLoads.getNames(car.getTypeName()).size() == 2) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarNoCustomLoad"),
                    new Object[] { car.toString(), car.getTypeName() }));
            return false;
        }
        if (car.getKernel() != null) {
            addLine(_buildReport, SEVEN,
                    MessageFormat.format(Bundle.getMessage("buildCarLeadKernel"),
                            new Object[] { car.toString(), car.getKernelName(), car.getKernel().getSize(),
                                    car.getKernel().getTotalLength(), Setup.getLengthUnit().toLowerCase() }));
        }
        List<Track> tracks = locationManager.getTracksByMoves(Track.SPUR);
        log.debug("Found {} spurs", tracks.size());
        // show locations not serviced by departure track once
        List<Location> locationsNotServiced = new ArrayList<>();
        for (Track track : tracks) {
            if (locationsNotServiced.contains(track.getLocation())) {
                continue;
            }
            if (rld != null && track.getLocation() != rld.getLocation()) {
                locationsNotServiced.add(track.getLocation());
                continue;
            }
            if (!car.getTrack().isDestinationAccepted(track.getLocation())) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDestinationNotServiced"),
                        new Object[] { track.getLocation().getName(), car.getTrackName() }));
                locationsNotServiced.add(track.getLocation());
                continue;
            }
            // only use tracks serviced by this train?
            if (car.getTrack().isAddCustomLoadsEnabled() &&
                    _train.getRoute().getLastLocationByName(track.getLocation().getName()) == null) {
                continue;
            }
            // only the first match in a schedule is used for a spur
            ScheduleItem si = getScheduleItem(car, track);
            if (si == null) {
                continue; // no match
            }
            // need to set car load so testDestination will work properly
            String oldCarLoad = car.getLoadName(); // should be the default empty
            car.setLoadName(si.getReceiveLoadName());
            String status = car.testDestination(track.getLocation(), track);
            if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
                addLine(_buildReport, SEVEN,
                        MessageFormat.format(Bundle.getMessage("buildNoDestTrackNewLoad"),
                                new Object[] { track.getLocation().getName(), track.getName(), car.toString(),
                                        si.getReceiveLoadName(), status }));
                // restore car's load
                car.setLoadName(oldCarLoad);
                continue;
            }
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrySpurLoad"),
                    new Object[] { track.getLocation().getName(), track.getName(), car.getLoadName() }));
            // does the car have a home division?
            if (car.getDivision() != null) {
                addLine(_buildReport, SEVEN,
                        MessageFormat.format(Bundle.getMessage("buildCarHasDivisionStaging"),
                                new Object[] { car.toString(), car.getTypeName(), car.getLoadType().toLowerCase(),
                                        car.getLoadName(), car.getDivisionName(), car.getLocationName(),
                                        car.getTrackName(), car.getTrack().getDivisionName() }));
                // load type empty must return to car's home division
                // or load type load from foreign division must return to car's home division
                if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY) && car.getDivision() != track.getDivision() ||
                        car.getLoadType().equals(CarLoad.LOAD_TYPE_LOAD) &&
                                car.getTrack().getDivision() != car.getDivision() &&
                                car.getDivision() != track.getDivision()) {
                    addLine(_buildReport, SEVEN,
                            MessageFormat.format(Bundle.getMessage("buildNoDivisionTrack"),
                                    new Object[] { track.getTrackTypeName(), track.getLocation().getName(),
                                            track.getName(), track.getDivisionName(), car.toString(),
                                            car.getLoadType().toLowerCase(), car.getLoadName() }));
                    // restore car's load
                    car.setLoadName(oldCarLoad);
                    continue;
                }
            }
            if (!track.isSpaceAvailable(car)) {
                addLine(_buildReport, SEVEN,
                        MessageFormat.format(Bundle.getMessage("buildNoDestTrackSpace"),
                                new Object[] { car.toString(), track.getLocation().getName(), track.getName(),
                                        track.getNumberOfCarsInRoute(), track.getReservedInRoute(),
                                        Setup.getLengthUnit().toLowerCase(), track.getReservationFactor() }));
                // restore car's load
                car.setLoadName(oldCarLoad);
                continue;
            }
            // try routing car
            car.setFinalDestination(track.getLocation());
            car.setFinalDestinationTrack(track);
            if (router.setDestination(car, _train, _buildReport) && car.getDestination() != null) {
                // return car with this custom load and destination
                addLine(_buildReport, FIVE,
                        MessageFormat.format(Bundle.getMessage("buildCreateNewLoadForCar"),
                                new Object[] { car.toString(), si.getReceiveLoadName(), track.getLocation().getName(),
                                        track.getName() }));
                car.setLoadGeneratedFromStaging(true);
                // is car part of kernel?
                car.updateKernel();
                track.bumpSchedule();
                return true; // done, car now has a custom load
            }
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotRouteCar"), new Object[] {
                    car.toString(), si.getReceiveLoadName(), track.getLocation().getName(), track.getName() }));
            car.setDestination(null, null);
            // restore load and final destination and track
            car.setLoadName(oldCarLoad);
            car.setFinalDestination(null);
            car.setFinalDestinationTrack(null);
        }
        addLine(_buildReport, SEVEN,
                MessageFormat.format(Bundle.getMessage("buildUnableNewLoad"), new Object[] { car.toString() }));
        return false; // done, no load generated for this car
    }

    /**
     * Tries to place a custom load in the car that is departing staging and
     * attempts to find a destination for the car that is also staging.
     *
     * @param car the car
     * @return True if custom load added to car
     * @throws BuildFailedException If code check fails
     */
    private boolean generateCarLoadStagingToStaging(Car car) throws BuildFailedException {
        // Code Check, car should have a track assignment
        if (car.getTrack() == null) {
            throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildWarningRsNoTrack"),
                    new Object[] { car.toString(), car.getLocationName() }));
        }
        if (!car.getTrack().isStaging() ||
                !car.getTrack().isAddCustomLoadsAnyStagingTrackEnabled() ||
                !car.getLoadName().equals(carLoads.getDefaultEmptyName()) ||
                car.getDestination() != null ||
                car.getFinalDestination() != null) {
            log.debug(
                    "No load generation for car ({}) isAddCustomLoadsAnyStagingTrackEnabled: {}, car load ({}) destination ({}) final destination ({})",
                    car.toString(), car.getTrack().isAddCustomLoadsAnyStagingTrackEnabled() ? "true" : "false",
                    car.getLoadName(), car.getDestinationName(), car.getFinalDestinationName());
            return false;
        }
        List<Track> tracks = locationManager.getTracks(Track.STAGING);
        // log.debug("Found {} staging tracks for load generation", tracks.size());
        addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTryStagingToStaging"),
                new Object[] { car.toString(), tracks.size() }));
        if (Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED)) {
            for (Track track : tracks) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildStagingLocationTrack"),
                        new Object[] { track.getLocation().getName(), track.getName() }));
            }
        }
        // list of locations that can't be reached by the router
        List<Location> locationsNotServiced = new ArrayList<>();
        if (_terminateStageTrack != null) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildIgnoreStagingFirstPass"),
                    new Object[] { _terminateStageTrack.getLocation().getName() }));
            locationsNotServiced.add(_terminateStageTrack.getLocation());
        }
        while (tracks.size() > 0) {
            // pick a track randomly
            int rnd = (int) (Math.random() * tracks.size());
            Track track = tracks.get(rnd);
            tracks.remove(track);
            log.debug("Try staging track ({}, {})", track.getLocation().getName(), track.getName());
            // find a staging track that isn't at the departure
            if (track.getLocation() == _departLocation) {
                log.debug("Can't use departure location ({})", track.getLocation().getName());
                continue;
            }
            if (!_train.isAllowThroughCarsEnabled() && track.getLocation() == _terminateLocation) {
                log.debug("Through cars to location ({}) not allowed", track.getLocation().getName());
                continue;
            }
            if (locationsNotServiced.contains(track.getLocation())) {
                log.debug("Location ({}) not reachable", track.getLocation().getName());
                continue;
            }
            if (!car.getTrack().isDestinationAccepted(track.getLocation())) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDestinationNotServiced"),
                        new Object[] { track.getLocation().getName(), car.getTrackName() }));
                locationsNotServiced.add(track.getLocation());
                continue;
            }
            // the following method sets the Car load generated from staging boolean
            if (generateLoadCarDepartingAndTerminatingIntoStaging(car, track)) {
                // test to see if destination is reachable by this train
                if (router.setDestination(car, _train, _buildReport) && car.getDestination() != null) {
                    return true; // done, car has a custom load and a final destination
                }
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildStagingTrackNotReachable"),
                        new Object[] { track.getLocation().getName(), track.getName(), car.getLoadName() }));
                // return car to original state
                car.setLoadName(carLoads.getDefaultEmptyName());
                car.setLoadGeneratedFromStaging(false);
                car.setFinalDestination(null);
                car.updateKernel();
                locationsNotServiced.add(track.getLocation()); // couldn't route to this staging location
            }
        }
        // No staging tracks reachable, try the track the train is terminating to
        if (_train.isAllowThroughCarsEnabled() &&
                _terminateStageTrack != null &&
                car.getTrack().isDestinationAccepted(_terminateStageTrack.getLocation()) &&
                generateLoadCarDepartingAndTerminatingIntoStaging(car, _terminateStageTrack)) {
            return true;
        }

        addLine(_buildReport, SEVEN,
                MessageFormat.format(Bundle.getMessage("buildNoStagingForCarCustom"), new Object[] { car.toString() }));
        return false;
    }

    /**
     * Check to see if car has been assigned a home division. If car has a home
     * division the following rules are applied when assigning the car a
     * destination:
     * <p>
     * If car load is type empty not at car's home division yard: Car is sent to a
     * home division yard. If home division yard not available, then car is sent to
     * home division staging, then spur (industry).
     * <p>
     * If car load is type empty at a yard at the car's home division: Car is sent
     * to a home division spur, then home division staging.
     * <p>
     * If car load is type load not at car's home division: Car is sent to home
     * division spur, and if spur not available then home division staging.
     * <p>
     * If car load is type load at car's home division: Car is sent to any division
     * spur or staging.
     * 
     * @param car the car being checked for a home division
     * @return false if destination track not found for this car
     * @throws BuildFailedException
     */
    private boolean findDestinationsForCarsWithHomeDivision(Car car) throws BuildFailedException {
        if (car.getDivision() == null || car.getDestination() != null || car.getFinalDestination() != null) {
            return true;
        }
        if (car.getDivision() == car.getTrack().getDivision()) {
            addLine(_buildReport, FIVE,
                    MessageFormat.format(Bundle.getMessage("buildCarDepartHomeDivision"),
                            new Object[] { car.toString(), car.getTypeName(), car.getLoadType().toLowerCase(),
                                    car.getLoadName(), car.getDivisionName(), car.getLocationName(), car.getTrackName(),
                                    car.getTrack().getDivisionName() }));
        } else {
            addLine(_buildReport, FIVE,
                    MessageFormat.format(Bundle.getMessage("buildCarDepartForeignDivision"),
                            new Object[] { car.toString(), car.getTypeName(), car.getLoadType().toLowerCase(),
                                    car.getLoadName(), car.getDivisionName(), car.getLocationName(), car.getTrackName(),
                                    car.getTrack().getDivisionName() }));
        }
        if (car.getKernel() != null) {
            addLine(_buildReport, SEVEN,
                    MessageFormat.format(Bundle.getMessage("buildCarLeadKernel"),
                            new Object[] { car.toString(), car.getKernelName(), car.getKernel().getSize(),
                                    car.getKernel().getTotalLength(), Setup.getLengthUnit().toLowerCase() }));
        }
        if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
            log.debug("Car ({}) has home division ({}) and load type empty", car.toString(), car.getDivisionName());
            if (car.getTrack().isYard() && car.getTrack().getDivision() == car.getDivision()) {
                log.debug("Car ({}) at it's home division yard", car.toString());
                if (!sendCarToHomeDivisionTrack(car, Track.SPUR, HOME_DIVISION)) {
                    return sendCarToHomeDivisionTrack(car, Track.STAGING, HOME_DIVISION);
                }
            }
            // try to send to home division yard, then home division staging, then home
            // division spur
            else if (!sendCarToHomeDivisionTrack(car, Track.YARD, HOME_DIVISION)) {
                if (!sendCarToHomeDivisionTrack(car, Track.STAGING, HOME_DIVISION)) {
                    return sendCarToHomeDivisionTrack(car, Track.SPUR, HOME_DIVISION);
                }
            }
        } else {
            log.debug("Car ({}) has home division ({}) and load type load", car.toString(), car.getDivisionName());
            // 1st send car to spur dependent of shipping track division, then try staging
            if (!sendCarToHomeDivisionTrack(car, Track.SPUR, car.getTrack().getDivision() != car.getDivision())) {
                return sendCarToHomeDivisionTrack(car, Track.STAGING,
                        car.getTrack().getDivision() != car.getDivision());
            }
        }
        return true;
    }

    private static final boolean HOME_DIVISION = true;

    /**
     * Tries to set a final destination for the car with a home division.
     * 
     * @param car           the car
     * @param trackType     One of three track types: Track.SPUR Track.YARD or
     *                      Track.STAGING
     * @param home_division If true track's division must match the car's
     * @return true if car was given a final destination
     */
    private boolean sendCarToHomeDivisionTrack(Car car, String trackType, boolean home_division) {
        List<Location> locationsNotServiced = new ArrayList<>(); // locations not reachable
        List<Track> tracks = locationManager.getTracksByMoves(trackType);
        log.debug("Found {} {} tracks", tracks.size(), trackType);
        for (Track track : tracks) {
            if (home_division && car.getDivision() != track.getDivision()) {
                addLine(_buildReport, SEVEN,
                        MessageFormat.format(Bundle.getMessage("buildNoDivisionTrack"),
                                new Object[] { track.getTrackTypeName(), track.getLocation().getName(), track.getName(),
                                        track.getDivisionName(), car.toString(), car.getLoadType().toLowerCase(),
                                        car.getLoadName() }));
                continue;
            }
            if (locationsNotServiced.contains(track.getLocation())) {
                continue;
            }
            if (!car.getTrack().isDestinationAccepted(track.getLocation())) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDestinationNotServiced"),
                        new Object[] { track.getLocation().getName(), car.getTrackName() }));
                locationsNotServiced.add(track.getLocation()); // location not reachable
                continue;
            }
            // only use the termination staging track for this train
            if (trackType.equals(Track.STAGING) &&
                    _terminateStageTrack != null &&
                    track.getLocation() == _terminateLocation &&
                    track != _terminateStageTrack) {
                continue;
            }
            if (trackType.equals(Track.SPUR)) {
                if (sendCarToDestinationSpur(car, track)) {
                    return true;
                }
            } else {
                if (sendCarToDestinationTrack(car, track)) {
                    return true;
                }
            }
        }
        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCouldNotFindTrack"),
                new Object[] { trackType, car.toString(), car.getLoadName() }));
        addLine(_buildReport, FIVE, BLANK_LINE);
        return false;
    }

    /**
     * Set the final destination and track for a car with a custom load. Car must
     * not have a destination or final destination. There's a check to see if
     * there's a spur/schedule for this car. Returns true if a schedule was found.
     * Will hold car at current location if any of the spurs checked has the the
     * option to "Hold cars with custom loads" enabled and the spur has an alternate
     * track assigned.
     * 
     * Tries to sent the car to staging if there aren't any spurs with schedules
     * available.
     *
     * @param car the car with the load
     * @return true if there's a schedule that can be routed to for this car and
     *         load
     * @throws BuildFailedException
     */
    private boolean findFinalDestinationForCarLoad(Car car) throws BuildFailedException {
        if (car.getLoadName().equals(carLoads.getDefaultEmptyName()) ||
                car.getLoadName().equals(carLoads.getDefaultLoadName()) ||
                car.getDestination() != null ||
                car.getFinalDestination() != null) {
            return false; // car doesn't have a custom load, or already has a destination set
        }
        addLine(_buildReport, FIVE,
                MessageFormat.format(Bundle.getMessage("buildSearchForSpur"),
                        new Object[] { car.toString(), car.getTypeName(), car.getLoadType().toLowerCase(),
                                car.getLoadName(), car.getLocationName(), car.getTrackName() }));
        if (car.getKernel() != null) {
            addLine(_buildReport, SEVEN,
                    MessageFormat.format(Bundle.getMessage("buildCarLeadKernel"),
                            new Object[] { car.toString(), car.getKernelName(), car.getKernel().getSize(),
                                    car.getKernel().getTotalLength(), Setup.getLengthUnit().toLowerCase() }));
        }
        _routeToTrackFound = false;
        List<Track> tracks = locationManager.getTracksByMoves(Track.SPUR);
        log.debug("Found {} spurs", tracks.size());
        List<Location> locationsNotServiced = new ArrayList<>(); // locations not reachable
        for (Track track : tracks) {
            if (car.getTrack() == track || track.getSchedule() == null) {
                continue;
            }
            if (locationsNotServiced.contains(track.getLocation())) {
                continue;
            }
            if (!car.getTrack().isDestinationAccepted(track.getLocation())) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDestinationNotServiced"),
                        new Object[] { track.getLocation().getName(), car.getTrackName() }));
                locationsNotServiced.add(track.getLocation()); // location not reachable
                continue;
            }
            if (sendCarToDestinationSpur(car, track)) {
                return true;
            }
        }
        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCouldNotFindTrack"),
                new Object[] { Track.getTrackTypeName(Track.SPUR), car.toString(), car.getLoadName() }));
        if (_routeToTrackFound &&
                !_train.isSendCarsWithCustomLoadsToStagingEnabled() &&
                !car.getLocation().isStaging()) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildHoldCarValidRoute"),
                    new Object[] { car.toString(), car.getLocationName(), car.getTrackName() }));
        } else {
            // try and send car to staging
            addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrySendCarToStaging"),
                    new Object[] { car.toString(), car.getLoadName() }));
            tracks = locationManager.getTracks(Track.STAGING);
            log.debug("Found {} staging tracks", tracks.size());
            while (tracks.size() > 0) {
                // pick a track randomly
                int rnd = (int) (Math.random() * tracks.size());
                Track track = tracks.get(rnd);
                tracks.remove(track);
                log.debug("Staging track ({}, {})", track.getLocation().getName(), track.getName());
                if (track.getLocation() == car.getLocation()) {
                    continue;
                }
                if (locationsNotServiced.contains(track.getLocation())) {
                    continue;
                }
                if (_terminateStageTrack != null &&
                        track.getLocation() == _terminateLocation &&
                        track != _terminateStageTrack) {
                    continue; // ignore other staging tracks at terminus
                }
                if (!car.getTrack().isDestinationAccepted(track.getLocation())) {
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDestinationNotServiced"),
                            new Object[] { track.getLocation().getName(), car.getTrackName() }));
                    locationsNotServiced.add(track.getLocation());
                    continue;
                }
                String status = track.isRollingStockAccepted(car);
                if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
                    log.debug("Staging track ({}) can't accept car ({})", track.getName(), car.toString());
                    continue;
                }
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildStagingCanAcceptLoad"),
                        new Object[] { track.getLocation(), track.getName(), car.getLoadName() }));
                // try to send car to staging
                car.setFinalDestination(track.getLocation());
                // test to see if destination is reachable by this train
                if (router.setDestination(car, _train, _buildReport)) {
                    _routeToTrackFound = true; // found a route to staging
                }
                if (car.getDestination() != null) {
                    car.updateKernel(); // car part of kernel?
                    return true;
                }
                locationsNotServiced.add(track.getLocation()); // couldn't route to this staging location
                car.setFinalDestination(null);
            }
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNoStagingForCarLoad"),
                    new Object[] { car.toString(), car.getLoadName() }));
        }
        log.debug("routeToSpurFound is {}", _routeToTrackFound);
        return _routeToTrackFound; // done
    }

    boolean _routeToTrackFound;

    /**
     * Used to determine if spur can accept car. Also will set routeToTrackFound to
     * true if there's a valid route available to the spur being tested. Sets car's
     * final destination to track if okay.
     * 
     * @param car   the car
     * @param track the spur
     * @return false if there's an issue with using the spur
     */
    private boolean sendCarToDestinationSpur(Car car, Track track) {
        if (!checkBasicMoves(car, track)) {
            return false;
        }
        String status = car.testDestination(track.getLocation(), track);
        if (!status.equals(Track.OKAY)) {
            if (track.getScheduleMode() == Track.SEQUENTIAL && status.startsWith(Track.SCHEDULE)) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildTrackSequentialMode"),
                        new Object[] { track.getName(), track.getLocation().getName(), status }));
            }
            // if the track has an alternate track don't abort if the issue was space
            if (!status.startsWith(Track.LENGTH) || !track.checkSchedule(car).equals(Track.OKAY)) {
                addLine(_buildReport, SEVEN,
                        MessageFormat.format(Bundle.getMessage("buildNoDestTrackNewLoad"),
                                new Object[] { track.getLocation().getName(), track.getName(), car.toString(),
                                        car.getLoadName(), status }));
                return false;
            }
            if (track.getAlternateTrack() == null) {
                // report that the spur is full and no alternate
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildSpurFullNoAlternate"),
                        new Object[] { track.getLocation().getName(), track.getName() }));
                return false; // ignore hold, no alternate
            } else {
                addLine(_buildReport, SEVEN,
                        MessageFormat.format(Bundle.getMessage("buildTrackFullHasAlternate"), new Object[] {
                                track.getLocation().getName(), track.getName(), track.getAlternateTrack().getName() }));
                // check to see if alternate and track are configured properly
                if (!_train.isLocalSwitcher() &&
                        (track.getTrainDirections() & track.getAlternateTrack().getTrainDirections()) == 0) {
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropRsUsingTrain4"),
                            new Object[] { track.getName(),
                                    formatStringToCommaSeparated(Setup.getDirectionStrings(track.getTrainDirections())),
                                    track.getAlternateTrack().getName(), formatStringToCommaSeparated(Setup
                                            .getDirectionStrings(track.getAlternateTrack().getTrainDirections())), }));
                    return false;
                }
            }
        }
        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildSetFinalDestination"),
                new Object[] { car.toString(), car.getLoadName(), track.getLocation().getName(), track.getName() }));

        // show if track is requesting cars with custom loads to only go to spurs
        if (track.isHoldCarsWithCustomLoadsEnabled()) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildHoldCarsCustom"),
                    new Object[] { track.getLocation().getName(), track.getName() }));
        }
        // check the number of in bound cars to this track
        if (!track.isSpaceAvailable(car)) {
            // Now determine if we should move the car or just leave it where it is
            String id = track.getScheduleItemId(); // save the tracks schedule item id
            // determine if this car can be routed to the spur
            car.setFinalDestination(track.getLocation());
            car.setFinalDestinationTrack(track);
            // hold car if able to route to track
            if (router.setDestination(car, _train, _buildReport) && track.isHoldCarsWithCustomLoadsEnabled()) {
                _routeToTrackFound = true; // if we don't find another spur, don't move car
            }
            car.setDestination(null, null);
            car.setFinalDestination(null);
            car.setFinalDestinationTrack(null);
            track.setScheduleItemId(id); // restore id
            addLine(_buildReport, SEVEN,
                    MessageFormat.format(Bundle.getMessage("buildNoDestTrackSpace"),
                            new Object[] { car.toString(), track.getLocation().getName(), track.getName(),
                                    track.getNumberOfCarsInRoute(), track.getReservedInRoute(),
                                    Setup.getLengthUnit().toLowerCase(), track.getReservationFactor() }));
            return false;
        }
        // try to send car to this spur
        car.setFinalDestination(track.getLocation());
        car.setFinalDestinationTrack(track);
        // test to see if destination is reachable by this train
        if (router.setDestination(car, _train, _buildReport) && track.isHoldCarsWithCustomLoadsEnabled()) {
            _routeToTrackFound = true; // if we don't find another spur, don't move car
        }
        if (car.getDestination() == null) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNotAbleToSetDestination"),
                    new Object[] { car.toString(), router.getStatus() }));
            car.setFinalDestination(null);
            car.setFinalDestinationTrack(null);
            return false;
        }
        car.updateKernel(); // car part of kernel?
        if (car.getDestinationTrack() != track) {
            // car is being routed to this track
            if (track.getSchedule() != null) {
                car.setScheduleItemId(track.getCurrentScheduleItem().getId());
                track.bumpSchedule();
            } else {
                // bump the track move count
                track.setMoves(track.getMoves() + 1);
            }
        }
        return true; // done, car has a new destination
    }

    /**
     * Destination track can be yard or staging, NOT a spur.
     * 
     * @param car   the car
     * @param track the car's destination track
     * @return true if car given a new final destination
     */
    private boolean sendCarToDestinationTrack(Car car, Track track) {
        if (!checkBasicMoves(car, track)) {
            return false;
        }
        String status = car.testDestination(track.getLocation(), track);
        if (!status.equals(Track.OKAY)) {
            addLine(_buildReport, SEVEN,
                    MessageFormat.format(Bundle.getMessage("buildNoDestTrackNewLoad"),
                            new Object[] { track.getLocation().getName(), track.getName(), car.toString(),
                                    car.getLoadName(), status }));
            return false;
        }
        if (!track.isSpaceAvailable(car)) {
            addLine(_buildReport, SEVEN,
                    MessageFormat.format(Bundle.getMessage("buildNoDestSpace"),
                            new Object[] { car.toString(), track.getTrackTypeName(), track.getLocation().getName(),
                                    track.getName(), track.getNumberOfCarsInRoute(), track.getReservedInRoute(),
                                    Setup.getLengthUnit().toLowerCase() }));
            return false;
        }
        // try to send car to this track
        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildSetFinalDestination"),
                new Object[] { car.toString(), car.getLoadName(), track.getLocation().getName(), track.getName() }));
        car.setFinalDestination(track.getLocation());
        car.setFinalDestinationTrack(track);
        // test to see if destination is reachable by this train
        if (router.setDestination(car, _train, _buildReport)) {
            log.debug("Can route car to destination ({}, {})", track.getLocation().getName(), track.getName());
        }
        if (car.getDestination() == null) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNotAbleToSetDestination"),
                    new Object[] { car.toString(), router.getStatus() }));
            car.setFinalDestination(null);
            car.setFinalDestinationTrack(null);
            return false;
        }
        car.updateKernel(); // car part of kernel?
        return true; // done, car has a new final destination
    }

    /**
     * Checks for a car's final destination, and then after checking, tries to route
     * the car to that destination. Normal return from this routine is false, with
     * the car returning with a set destination. Returns true if car has a final
     * destination, but can't be used for this train.
     *
     * @param car
     * @return false if car needs destination processing (normal).
     */
    private boolean checkCarForFinalDestination(Car car) {
        if (car.getFinalDestination() == null || car.getDestination() != null) {
            return false;
        }

        addLine(_buildReport, FIVE,
                MessageFormat.format(Bundle.getMessage("buildCarRoutingBegins"),
                        new Object[] { car.toString(), car.getTypeName(), car.getLoadName(), car.getLocationName(),
                                car.getTrackName(), car.getFinalDestinationName(),
                                car.getFinalDestinationTrackName() }));

        // no local moves for this train?
        if (!_train.isAllowLocalMovesEnabled() &&
                splitString(car.getLocationName()).equals(splitString(car.getFinalDestinationName())) &&
                car.getTrack() != _departStageTrack) {
            addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarHasFinalDestNoMove"),
                    new Object[] { car.toString(), car.getFinalDestinationName() }));
            addLine(_buildReport, FIVE, BLANK_LINE);
            log.debug("Removing car ({}) from list", car.toString());
            _carList.remove(car);
            _carIndex--;
            return true; // car has a final destination, but no local moves by this train
        }
        // is the car's destination the terminal and is that allowed?
        if (!checkThroughCarsAllowed(car, car.getFinalDestinationName())) {
            // don't remove car from list if departing staging
            if (car.getTrack() == _departStageTrack) {
                addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildErrorCarStageDest"),
                        new Object[] { car.toString() }));
            } else {
                log.debug("Removing car ({}) from list", car.toString());
                _carList.remove(car);
                _carIndex--;
            }
            return true; // car has a final destination, but through traffic not allowed by this train
        }
        // does the car have a final destination track that is willing to service the
        // car?
        // note the default mode for all track types is MATCH
        if (car.getFinalDestinationTrack() != null && car.getFinalDestinationTrack().getScheduleMode() == Track.MATCH) {
            String status = car.testDestination(car.getFinalDestination(), car.getFinalDestinationTrack());
            // keep going if the only issue was track length and the track accepts the car's
            // load
            if (!status.equals(Track.OKAY) &&
                    !status.startsWith(Track.LENGTH) &&
                    !(status.contains(Track.CUSTOM) && status.contains(Track.LOAD))) {
                addLine(_buildReport, SEVEN,
                        MessageFormat.format(Bundle.getMessage("buildNoDestTrackNewLoad"),
                                new Object[] { car.getFinalDestination().getName(),
                                        car.getFinalDestinationTrack().getName(), car.toString(), car.getLoadName(),
                                        status }));
                // is this car or kernel being sent to a track that is too short?
                if (status.startsWith(Track.CAPACITY)) {
                    // track is too short for this car or kernel, can never go there
                    addLine(_buildReport, SEVEN,
                            MessageFormat.format(Bundle.getMessage("buildTrackTooShort"),
                                    new Object[] { car.getFinalDestination().getName(),
                                            car.getFinalDestinationTrack().getName(), car.toString() }));
                }
                addLine(_buildReport, SEVEN,
                        MessageFormat.format(Bundle.getMessage("buildRemovingFinalDestinaton"),
                                new Object[] { car.getFinalDestination().getName(),
                                        car.getFinalDestinationTrack().getName(), car.toString() }));
                car.setFinalDestination(null);
                car.setFinalDestinationTrack(null);
                return false; // car no longer has a final destination
            }
        }

        // now try and route the car
        if (!router.setDestination(car, _train, _buildReport)) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildNotAbleToSetDestination"),
                    new Object[] { car.toString(), router.getStatus() }));
            // don't move car if routing issue was track space but not departing staging
            if ((!router.getStatus().startsWith(Track.LENGTH) &&
                    !_train.isServiceAllCarsWithFinalDestinationsEnabled()) || (car.getTrack() == _departStageTrack)) {
                // add car to not able to route list
                if (!_notRoutable.contains(car)) {
                    _notRoutable.add(car);
                }
                addLine(_buildReport, FIVE, BLANK_LINE);
                addLine(_buildReport, FIVE,
                        MessageFormat.format(Bundle.getMessage("buildWarningCarNotRoutable"),
                                new Object[] { car.toString(), car.getLocationName(), car.getTrackName(),
                                        car.getFinalDestinationName(), car.getFinalDestinationTrackName() }));
                addLine(_buildReport, FIVE, BLANK_LINE);
                return false; // move this car, routing failed!
            }
        } else {
            if (car.getDestination() != null) {
                return false; // routing successful process this car, normal exit from this routine
            }
            if (car.getTrack() == _departStageTrack) {
                log.debug("Car ({}) departing staging with final destination ({}) and no destination", // NOI18N
                        car.toString(), car.getFinalDestinationName());
                return false; // try and move this car out of staging
            }
        }
        addLine(_buildReport, FIVE,
                MessageFormat.format(Bundle.getMessage("buildNoDestForCar"), new Object[] { car.toString() }));
        addLine(_buildReport, FIVE, BLANK_LINE);
        return true;
    }

    /**
     * Checks to see if car has a destination and tries to add car to train. Will
     * find a track for the car if needed. Returns false if car doesn't have a
     * destination.
     *
     * @param rl         the car's route location
     * @param routeIndex where in the route to start search
     * @return true if car has a destination. Need to check if car given a train
     *         assignment.
     * @throws BuildFailedException if destination was staging and can't place car
     *                              there
     */
    private boolean checkCarForDestination(Car car, RouteLocation rl, int routeIndex) throws BuildFailedException {
        if (car.getDestination() == null) {
            return false; // the only false return
        }
        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarHasAssignedDest"),
                new Object[] { car.toString(), (car.getDestinationName() + ", " + car.getDestinationTrackName()) }));
        RouteLocation rld = _train.getRoute().getLastLocationByName(car.getDestinationName());
        if (rld == null) {
            // code check, router doesn't set a car's destination if not carried by train
            // being built. Car has a destination that isn't serviced by this train. Find
            // buildExcludeCarDestNotPartRoute in loadRemoveAndListCars()
            throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildExcludeCarDestNotPartRoute"),
                    new Object[] { car.toString(), car.getDestinationName(), _train.getRoute().getName() }));
        }
        // now go through the route and try and find a location with
        // the correct destination name
        for (int k = routeIndex; k < _routeList.size(); k++) {
            rld = _routeList.get(k);
            // if car can be picked up later at same location, skip
            if (checkForLaterPickUp(car, rl, rld)) {
                return true; // done
            }
            if (!rld.getName().equals(car.getDestinationName())) {
                continue;
            }
            // is the car's destination the terminal and is that allowed?
            if (!checkThroughCarsAllowed(car, car.getDestinationName())) {
                return true; // done
            }
            log.debug("Car ({}) found a destination in train's route", car.toString());
            // are drops allows at this location?
            if (!rld.isDropAllowed()) {
                addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildRouteNoDropLocation"),
                        new Object[] { _train.getRoute().getName(), rld.getName(), rld.getId() }));
                continue;
            }
            if (_train.isLocationSkipped(rld.getId())) {
                addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildLocSkipped"),
                        new Object[] { rld.getName(), rld.getId(), _train.getName() }));
                continue;
            }
            // any moves left at this location?
            if (rld.getCarMoves() >= rld.getMaxCarMoves()) {
                addLine(_buildReport, FIVE,
                        MessageFormat.format(Bundle.getMessage("buildNoAvailableMovesDest"),
                                new Object[] { rld.getCarMoves(), rld.getMaxCarMoves(), _train.getRoute().getName(),
                                        rld.getId(), rld.getName() }));
                continue;
            }
            // is the train length okay?
            if (!checkTrainLength(car, rl, rld)) {
                continue;
            }
            // check for valid destination track
            if (car.getDestinationTrack() == null) {
                addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarDoesNotHaveDest"),
                        new Object[] { car.toString() }));
                // is car going into staging?
                if (rld == _train.getTrainTerminatesRouteLocation() && _terminateStageTrack != null) {
                    String status = car.testDestination(car.getDestination(), _terminateStageTrack);
                    if (status.equals(Track.OKAY)) {
                        addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarAssignedToStaging"),
                                new Object[] { car.toString(), _terminateStageTrack.getName() }));
                        addCarToTrain(car, rl, rld, _terminateStageTrack);
                        return true;
                    } else {
                        addLine(_buildReport, SEVEN,
                                MessageFormat.format(Bundle.getMessage("buildCanNotDropCarBecause"),
                                        new Object[] { car.toString(), _terminateStageTrack.getName(), status,
                                                _terminateStageTrack.getTrackTypeName() }));
                        continue;
                    }
                } else {
                    // no staging at this location, now find a destination track for this car
                    List<Track> tracks = findTrackAtDestination(car, rld);
                    if (tracks.size() > 0) {
                        if (tracks.get(1) != null) {
                            car.setFinalDestination(car.getDestination());
                            car.setFinalDestinationTrack(tracks.get(1));
                            tracks.get(1).setMoves(tracks.get(1).getMoves() + 1); // bump the number of moves
                        }
                        addCarToTrain(car, rl, rld, tracks.get(0));
                        return true;
                    }
                }
            } else {
                log.debug("Car ({}) has a destination track ({})", car.toString(), car.getDestinationTrack().getName());
                // going into the correct staging track?
                if (rld.equals(_train.getTrainTerminatesRouteLocation()) &&
                        _terminateStageTrack != null &&
                        _terminateStageTrack != car.getDestinationTrack()) {
                    // car going to wrong track in staging, change track
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarDestinationStaging"),
                            new Object[] { car.toString(), car.getDestinationName(), car.getDestinationTrackName() }));
                    car.setDestination(_terminateStageTrack.getLocation(), _terminateStageTrack);
                }
                if (!rld.equals(_train.getTrainTerminatesRouteLocation()) ||
                        _terminateStageTrack == null ||
                        _terminateStageTrack == car.getDestinationTrack()) {
                    // is train direction correct? and drop to interchange or spur?
                    if (checkDropTrainDirection(car, rld, car.getDestinationTrack()) &&
                            checkTrainCanDrop(car, car.getDestinationTrack())) {
                        String status = car.testDestination(car.getDestination(), car.getDestinationTrack());
                        if (status.equals(Track.OKAY)) {
                            addCarToTrain(car, rl, rld, car.getDestinationTrack());
                            return true;
                        } else {
                            addLine(_buildReport, SEVEN,
                                    MessageFormat.format(Bundle.getMessage("buildCanNotDropCarBecause"),
                                            new Object[] { car.toString(), car.getDestinationTrackName(), status,
                                                    car.getDestinationTrack().getTrackTypeName() }));
                        }
                    }
                } else {
                    // code check
                    throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildCarDestinationStaging"),
                            new Object[] { car.toString(), car.getDestinationName(), car.getDestinationTrackName() }));
                }
            }
            addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCanNotDropCar"),
                    new Object[] { car.toString(), car.getDestinationName(), rld.getId() }));
            if (car.getDestinationTrack() == null) {
                log.debug("Could not find a destination track for location ({})", car.getDestinationName());
            }
        }
        log.debug("car ({}) not added to train", car.toString());
        addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildDestinationNotReachable"),
                new Object[] { car.getDestinationName(), rl.getName(), rl.getId() }));
        // remove destination and revert to final destination
        if (car.getDestinationTrack() != null) {
            // going to remove this destination from car
            car.getDestinationTrack().setMoves(car.getDestinationTrack().getMoves() - 1);
            Track destTrack = car.getDestinationTrack();
            // TODO should we leave the car's destination? The spur expects this car!
            if (destTrack.getSchedule() != null && destTrack.getScheduleMode() == Track.SEQUENTIAL) {
                // log.debug("Scheduled delivery to ("+destTrack.getName()+") cancelled");
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildPickupCancelled"),
                        new Object[] { destTrack.getLocation().getName(), destTrack.getName() }));
            }
        }
        car.setFinalDestination(car.getPreviousFinalDestination());
        car.setFinalDestinationTrack(car.getPreviousFinalDestinationTrack());
        car.setDestination(null, null);
        car.updateKernel();

        addLine(_buildReport, FIVE,
                MessageFormat.format(Bundle.getMessage("buildNoDestForCar"), new Object[] { car.toString() }));
        addLine(_buildReport, FIVE, BLANK_LINE);
        return true; // car no longer has a destination, but it had one.
    }

    /**
     * Find a destination and track for a car at a route location.
     *
     * @param car the car!
     * @param rl  The car's route location
     * @param rld The car's route destination
     * @return true if successful.
     * @throws BuildFailedException if code check fails
     */
    private boolean findDestinationAndTrack(Car car, RouteLocation rl, RouteLocation rld) throws BuildFailedException {
        int index = _routeList.indexOf(rld);
        if (_train.isLocalSwitcher()) {
            return findDestinationAndTrack(car, rl, index, index + 1);
        }
        return findDestinationAndTrack(car, rl, index - 1, index + 1);
    }

    /**
     * Find a destination and track for a car, and add the car to the train.
     *
     * @param car        The car that is looking for a destination and destination
     *                   track.
     * @param rl         The route location for this car.
     * @param routeIndex Where in the train's route to begin a search for a
     *                   destination for this car.
     * @param routeEnd   Where to stop looking for a destination.
     * @return true if successful, car has destination, track and a train.
     * @throws BuildFailedException if code check fails
     */
    private boolean findDestinationAndTrack(Car car, RouteLocation rl, int routeIndex, int routeEnd)
            throws BuildFailedException {
        if (routeIndex + 1 == routeEnd) {
            log.debug("Car ({}) is at the last location in the train's route", car.toString());
        }
        addLine(_buildReport, FIVE,
                MessageFormat.format(Bundle.getMessage("buildFindDestinationForCar"),
                        new Object[] { car.toString(), car.getTypeName(), car.getLoadType().toLowerCase(),
                                car.getLoadName(), (car.getLocationName() + ", " + car.getTrackName()) }));
        if (car.getKernel() != null) {
            addLine(_buildReport, SEVEN,
                    MessageFormat.format(Bundle.getMessage("buildCarLeadKernel"),
                            new Object[] { car.toString(), car.getKernelName(), car.getKernel().getSize(),
                                    car.getKernel().getTotalLength(), Setup.getLengthUnit().toLowerCase() }));
        }

        int start = routeIndex; // normally start looking after car's route location
        RouteLocation rld = null; // the route location destination being checked for the car
        RouteLocation rldSave = null; // holds the best route location destination for the car
        Track trackSave = null; // holds the best track at destination for the car
        Track finalDestinationTrackSave = null; // used when a spur has an alternate track and no schedule
        boolean multiplePickup = false; // true when car can be picked up from two or more locations in the route

        // more than one location in this route?
        if (!_train.isLocalSwitcher()) {
            start++; // begin looking for tracks at the next location
        }
        // all pick ups to terminal?
        if (_train.isSendCarsToTerminalEnabled() &&
                !splitString(rl.getName()).equals(splitString(_departLocation.getName())) &&
                routeEnd == _routeList.size()) {
            addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildSendToTerminal"),
                    new Object[] { _terminateLocation.getName() }));
            // user could have specified several terminal locations with the "same" name
            start = routeEnd - 1;
            while (start > routeIndex) {
                if (!splitString(_routeList.get(start - 1).getName())
                        .equals(splitString(_terminateLocation.getName()))) {
                    break;
                }
                start--;
            }
        }
        // now search for a destination for this car
        for (int k = start; k < routeEnd; k++) {
            rld = _routeList.get(k);
            // if car can be picked up later at same location, set flag
            if (checkForLaterPickUp(car, rl, rld)) {
                multiplePickup = true;
            }
            if (rld.isDropAllowed() || car.hasFred() || car.isCaboose()) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildSearchingLocation"),
                        new Object[] { rld.getName(), rld.getId() }));
            } else {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildRouteNoDropLocation"),
                        new Object[] { _train.getRoute().getName(), rld.getId(), rld.getName() }));
                continue;
            }
            if (_train.isLocationSkipped(rld.getId())) {
                addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildLocSkipped"),
                        new Object[] { rld.getName(), rld.getId(), _train.getName() }));
                continue;
            }
            // any moves left at this location?
            if (rld.getCarMoves() >= rld.getMaxCarMoves()) {
                addLine(_buildReport, FIVE,
                        MessageFormat.format(Bundle.getMessage("buildNoAvailableMovesDest"),
                                new Object[] { rld.getCarMoves(), rld.getMaxCarMoves(), _train.getRoute().getName(),
                                        rld.getId(), rld.getName() }));
                continue;
            }
            // get the destination
            Location testDestination = rld.getLocation();
            // code check, all locations in the route have been already checked
            if (testDestination == null) {
                throw new BuildFailedException(MessageFormat.format(Bundle.getMessage("buildErrorRouteLoc"),
                        new Object[] { _train.getRoute().getName(), rld.getName() }));
            }
            // don't move car to same location unless the train is a switcher (local moves)
            // or is passenger, caboose or car with FRED
            if (splitString(rl.getName()).equals(splitString(rld.getName())) &&
                    !_train.isLocalSwitcher() &&
                    !car.isPassenger() &&
                    !car.isCaboose() &&
                    !car.hasFred()) {
                // allow cars to return to the same staging location if no other options
                // (tracks) are available
                if ((_train.isAllowReturnToStagingEnabled() || Setup.isStagingAllowReturnEnabled()) &&
                        testDestination.isStaging() &&
                        trackSave == null) {
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildReturnCarToStaging"),
                            new Object[] { car.toString(), rld.getName() }));
                } else {
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCarLocEqualDestination"),
                            new Object[] { car.toString(), rld.getName() }));
                    continue;
                }
            }

            // check to see if departure track has any restrictions
            if (!car.getTrack().isDestinationAccepted(testDestination)) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDestinationNotServiced"),
                        new Object[] { testDestination.getName(), car.getTrackName() }));
                continue;
            }

            if (!testDestination.acceptsTypeName(car.getTypeName())) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildCanNotDropLocation"),
                        new Object[] { car.toString(), car.getTypeName(), testDestination.getName() }));
                continue;
            }
            // can this location service this train's direction
            if (!checkDropTrainDirection(rld)) {
                continue;
            }
            // is the train length okay?
            if (!checkTrainLength(car, rl, rld)) {
                break; // no, done with this car
            }
            // is the car's destination the terminal and is that allowed?
            if (!checkThroughCarsAllowed(car, rld.getName())) {
                continue; // not allowed
            }

            Track trackTemp = null;
            Track finalDestinationTrackTemp = null; // used when alternate track selected

            // is there a track assigned for staging cars?
            if (rld == _train.getTrainTerminatesRouteLocation() && _terminateStageTrack != null) {
                trackTemp = tryStaging(car, rldSave);
                if (trackTemp == null) {
                    continue; // no
                }
            } else {
                // no staging, start track search
                List<Track> tracks = findTrackAtDestination(car, rld);
                if (tracks.size() > 0) {
                    trackTemp = tracks.get(0);
                    finalDestinationTrackTemp = tracks.get(1);
                }
            }
            // did we find a new destination?
            if (trackTemp == null) {
                addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCouldNotFindDestForCar"),
                        new Object[] { car.toString(), rld.getName() }));
            } else {
                addLine(_buildReport, FIVE,
                        MessageFormat.format(Bundle.getMessage("buildCarCanDropMoves"),
                                new Object[] { car.toString(), trackTemp.getTrackTypeName(),
                                        trackTemp.getLocation().getName(), trackTemp.getName(), +rld.getCarMoves(),
                                        rld.getMaxCarMoves() }));
                if (rldSave == null && multiplePickup) {
                    addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarHasSecond"),
                            new Object[] { car.toString(), car.getLocationName() }));
                    trackSave = null;
                    break; // done
                }
                // if there's more than one available destination use the lowest ratio
                if (rldSave != null) {
                    // check for an earlier drop in the route
                    rld = checkForEarlierDrop(car, trackTemp, rld, start, routeEnd);
                    double saveCarMoves = rldSave.getCarMoves();
                    double saveRatio = saveCarMoves / rldSave.getMaxCarMoves();
                    double nextCarMoves = rld.getCarMoves();
                    double nextRatio = nextCarMoves / rld.getMaxCarMoves();

                    // bias cars to the terminal
                    if (rld == _train.getTrainTerminatesRouteLocation()) {
                        nextRatio = nextRatio * nextRatio;
                        log.debug("Location ({}) is terminate location, adjusted nextRatio {}", rld.getName(),
                                Double.toString(nextRatio));

                        // bias cars with default loads to a track with a schedule
                    } else if (!trackTemp.getScheduleId().equals(Track.NONE)) {
                        nextRatio = nextRatio * nextRatio;
                        log.debug("Track ({}) has schedule ({}), adjusted nextRatio {}", trackTemp.getName(),
                                trackTemp.getScheduleName(), Double.toString(nextRatio));
                    }
                    // bias cars with default loads to saved track with a schedule
                    if (trackSave != null && !trackSave.getScheduleId().equals(Track.NONE)) {
                        saveRatio = saveRatio * saveRatio;
                        log.debug("Saved track ({}) has schedule ({}), adjusted nextRatio {}", trackSave.getName(),
                                trackSave.getScheduleName(), Double.toString(saveRatio));
                    }
                    log.debug("Saved {} = {}, {} = {}", rldSave.getName(), Double.toString(saveRatio), rld.getName(),
                            Double.toString(nextRatio));
                    if (saveRatio < nextRatio) {
                        rld = rldSave; // the saved is better than the last found
                        trackTemp = trackSave;
                        finalDestinationTrackTemp = finalDestinationTrackSave;
                    } else if (multiplePickup) {
                        addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildCarHasSecond"),
                                new Object[] { car.toString(), car.getLocationName() }));
                        trackSave = null;
                        break; // done
                    }
                }
                // every time through, save the best route destination, and track
                rldSave = rld;
                trackSave = trackTemp;
                finalDestinationTrackSave = finalDestinationTrackTemp;
            }
        }
        // did we find a destination?
        if (trackSave != null) {
            if (finalDestinationTrackSave != null) {
                car.setFinalDestination(finalDestinationTrackSave.getLocation());
                car.setFinalDestinationTrack(finalDestinationTrackSave);
                if (trackSave.isAlternate()) {
                    finalDestinationTrackSave.setMoves(finalDestinationTrackSave.getMoves() + 1); // bump move count
                }
            }
            addCarToTrain(car, rl, rldSave, trackSave);
            return true;
        }
        addLine(_buildReport, FIVE,
                MessageFormat.format(Bundle.getMessage("buildNoDestForCar"), new Object[] { car.toString() }));
        addLine(_buildReport, FIVE, BLANK_LINE);
        return false; // no build errors, but car not given destination
    }

    private void showTrainBuildStatus() {
        if (_numberCars < _train.getNumberCarsRequested()) {
            _train.setStatusCode(Train.CODE_PARTIAL_BUILT);
            addLine(_buildReport, ONE,
                    Train.PARTIAL_BUILT +
                            " " +
                            _train.getNumberCarsWorked() +
                            "/" +
                            _train.getNumberCarsRequested() +
                            " " +
                            Bundle.getMessage("cars"));
        } else {
            _train.setStatusCode(Train.CODE_BUILT);
            addLine(_buildReport, ONE,
                    Train.BUILT + " " + _train.getNumberCarsWorked() + " " + Bundle.getMessage("cars"));
        }
    }

    /**
     * Checks to see if the engine or consist assigned to the train has the
     * appropriate HP. If the train's HP requirements are significantly higher or
     * lower than the engine that was assigned, the program will search for a more
     * appropriate engine or consist, and assign that engine or consist to the
     * train.
     *
     * The HP calculation is based on a minimum train speed of 36 MPH. The formula
     * HPT x 12 / % Grade = Speed, is used to determine the horsepower required.
     * Speed is fixed at 36 MPH. For example a 1% grade requires a minimum of 3 HPT.
     * 
     * Disabled for trains departing staging.
     * 
     * @throws BuildFailedException
     */
    private void checkEngineHP() throws BuildFailedException {
        if (Setup.getHorsePowerPerTon() != 0) {
            if (_train.getNumberEngines().equals(Train.AUTO_HPT)) {
                checkEngineHP(_train.getLeadEngine(), _train.getEngineModel(), _train.getEngineRoad()); // 1st leg
            }
            if ((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                    _train.getSecondLegNumberEngines().equals(Train.AUTO_HPT)) {
                checkEngineHP(_secondLeadEngine, _train.getSecondLegEngineModel(), _train.getSecondLegEngineRoad());
            }
            if ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                    _train.getThirdLegNumberEngines().equals(Train.AUTO_HPT)) {
                checkEngineHP(_thirdLeadEngine, _train.getThirdLegEngineModel(), _train.getThirdLegEngineRoad());
            }
        }
    }

    private void checkEngineHP(Engine leadEngine, String model, String road) throws BuildFailedException {
        // code check
        if (leadEngine == null) {
            throw new BuildFailedException("ERROR coding issue, engine missing from checkEngineHP()");
        }
        // departing staging?
        if (leadEngine.getRouteLocation() == _train.getTrainDepartsRouteLocation() && _departStageTrack != null) {
            return;
        }
        addLine(_buildReport, ONE, BLANK_LINE);
        addLine(_buildReport, ONE,
                MessageFormat.format(Bundle.getMessage("buildDetermineHpNeeded"), new Object[] { leadEngine.toString(),
                        leadEngine.getLocationName(), leadEngine.getDestinationName(),
                        _train.getTrainHorsePower(leadEngine.getRouteLocation()), Setup.getHorsePowerPerTon() }));
        // now determine the HP needed for this train
        int hpNeeded = 0;
        int hpAvailable = 0;
        Route route = _train.getRoute();
        if (route != null) {
            boolean helper = false;
            boolean foundStart = false;
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                if (!foundStart && rl != leadEngine.getRouteLocation()) {
                    continue;
                }
                foundStart = true;
                if ((_train.getSecondLegOptions() == Train.HELPER_ENGINES &&
                        rl == _train.getSecondLegStartRouteLocation()) ||
                        (_train.getThirdLegOptions() == Train.HELPER_ENGINES &&
                                rl == _train.getThirdLegStartRouteLocation())) {
                    addLine(_buildReport, FIVE,
                            MessageFormat.format(Bundle.getMessage("AddHelpersAt"), new Object[] { rl.getName() }));
                    helper = true;
                }
                if ((_train.getSecondLegOptions() == Train.HELPER_ENGINES &&
                        rl == _train.getSecondLegEndRouteLocation()) ||
                        (_train.getThirdLegOptions() == Train.HELPER_ENGINES &&
                                rl == _train.getThirdLegEndRouteLocation())) {
                    addLine(_buildReport, FIVE,
                            MessageFormat.format(Bundle.getMessage("RemoveHelpersAt"), new Object[] { rl.getName() }));
                    helper = false;
                }
                if (helper) {
                    continue; // ignore HP needed when helpers are assigned to the train
                }
                // check for a change of engines in the train's route
                if (rl == leadEngine.getRouteDestination()) {
                    log.debug("Remove loco ({}) at ({})", leadEngine.toString(), rl.getName());
                    break; // done
                }
                if (_train.getTrainHorsePower(rl) > hpAvailable)
                    hpAvailable = _train.getTrainHorsePower(rl);
                int weight = rl.getTrainWeight();
                int hpRequired = (int) ((36 * rl.getGrade() / 12) * weight);
                if (hpRequired < Setup.getHorsePowerPerTon() * weight)
                    hpRequired = Setup.getHorsePowerPerTon() * weight; // minimum HPT
                if (hpRequired > hpNeeded) {
                    addLine(_buildReport, SEVEN,
                            MessageFormat.format(Bundle.getMessage("buildReportTrainHpNeeds"),
                                    new Object[] { weight, _train.getNumberCarsInTrain(rl), rl.getGrade(), rl.getName(),
                                            rl.getId(), hpRequired }));
                    hpNeeded = hpRequired;
                }
            }
        }
        if (hpNeeded > hpAvailable) {
            addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildAssignedHpNotEnough"),
                    new Object[] { leadEngine.toString(), hpAvailable, hpNeeded }));
            findNewEngine(hpNeeded, leadEngine, model, road);
        } else if (hpAvailable > 2 * hpNeeded) {
            addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildAssignedHpTooMuch"),
                    new Object[] { leadEngine.toString(), hpAvailable, hpNeeded }));
            findNewEngine(hpNeeded, leadEngine, model, road);
        } else {
            log.debug("Keeping engine ({}) it meets the train's HP requirement", leadEngine.toString());
        }
    }

    /**
     * Checks to see if additional engines are needed for the train based on the
     * train's calculated tonnage. Minimum speed for the train is fixed at 36 MPH.
     * The formula HPT x 12 / % Grade = Speed, is used to determine the horsepower
     * needed. For example a 1% grade requires a minimum of 3 HPT.
     *
     * @throws BuildFailedException
     */
    private void checkNumnberOfEnginesNeededHPT() throws BuildFailedException {
        if (_train.getNumberEngines().equals("0") ||
                !_train.isBuildConsistEnabled() ||
                Setup.getHorsePowerPerTon() == 0) {
            return;
        }
        addLine(_buildReport, ONE, BLANK_LINE);
        addLine(_buildReport, ONE, MessageFormat.format(Bundle.getMessage("buildDetermineNeeds"),
                new Object[] { Setup.getHorsePowerPerTon() }));
        Route route = _train.getRoute();
        int hpAvailable = 0;
        int extraHpNeeded = 0;
        RouteLocation rlNeedHp = null;
        RouteLocation rlStart = _train.getTrainDepartsRouteLocation();
        RouteLocation rlEnd = _train.getTrainTerminatesRouteLocation();
        if (route != null) {
            boolean helper = false;
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                if ((_train.getSecondLegOptions() == Train.HELPER_ENGINES &&
                        rl == _train.getSecondLegStartRouteLocation()) ||
                        (_train.getThirdLegOptions() == Train.HELPER_ENGINES &&
                                rl == _train.getThirdLegStartRouteLocation())) {
                    addLine(_buildReport, FIVE,
                            MessageFormat.format(Bundle.getMessage("AddHelpersAt"), new Object[] { rl.getName() }));
                    helper = true;
                }
                if ((_train.getSecondLegOptions() == Train.HELPER_ENGINES &&
                        rl == _train.getSecondLegEndRouteLocation()) ||
                        (_train.getThirdLegOptions() == Train.HELPER_ENGINES &&
                                rl == _train.getThirdLegEndRouteLocation())) {
                    addLine(_buildReport, FIVE,
                            MessageFormat.format(Bundle.getMessage("RemoveHelpersAt"), new Object[] { rl.getName() }));
                    helper = false;
                }
                if (helper) {
                    continue;
                }
                // check for a change of engines in the train's route
                if (((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                        rl == _train.getSecondLegStartRouteLocation()) ||
                        ((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                                rl == _train.getThirdLegStartRouteLocation())) {
                    log.debug("Loco change at ({})", rl.getName());
                    addLocosBasedHPT(hpAvailable, extraHpNeeded, rlNeedHp, rlStart, rl);
                    addLine(_buildReport, THREE, BLANK_LINE);
                    // reset for next leg of train's route
                    rlStart = rl;
                    rlNeedHp = null;
                    extraHpNeeded = 0;
                }
                int weight = rl.getTrainWeight();
                if (weight > 0) {
                    double hptMinimum = Setup.getHorsePowerPerTon();
                    double hptGrade = (36 * rl.getGrade() / 12);
                    int hp = _train.getTrainHorsePower(rl);
                    int hpt = hp / weight;
                    if (hptGrade > hptMinimum) {
                        hptMinimum = hptGrade;
                    }
                    if (hptMinimum > hpt) {
                        int addHp = (int) (hptMinimum * weight - hp);
                        if (addHp > extraHpNeeded) {
                            hpAvailable = hp;
                            extraHpNeeded = addHp;
                            rlNeedHp = rl;
                        }
                        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildAddLocosStatus"),
                                new Object[] { weight, hp, rl.getGrade(), hpt, hptMinimum, rl.getName(), rl.getId() }));
                        addLine(_buildReport, FIVE, MessageFormat.format(Bundle.getMessage("buildTrainRequiresAddHp"),
                                new Object[] { addHp, rl.getName(), hptMinimum }));
                    }
                }
            }
        }
        addLocosBasedHPT(hpAvailable, extraHpNeeded, rlNeedHp, rlStart, rlEnd);
        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("buildDoneAssingEnginesTrain"),
                new Object[] { _train.getName() }));
        addLine(_buildReport, THREE, BLANK_LINE);
    }

    private void createManifests() throws BuildFailedException {
        new TrainManifest(_train);
        try {
            new JsonManifest(_train).build();
        } catch (IOException ex) {
            log.error("Unable to create JSON manifest: {}", ex.getLocalizedMessage());
            throw new BuildFailedException(ex);
        }
        new TrainCsvManifest(_train);
    }

    private void showWarningMessage() {
        if (trainManager.isBuildMessagesEnabled() && _warnings > 0) {
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("buildCheckReport"),
                            new Object[] { _train.getName(), _train.getDescription() }),
                    MessageFormat.format(Bundle.getMessage("buildWarningMsg"),
                            new Object[] { _train.getName(), _warnings }),
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void buildFailed(BuildFailedException e) {
        String msg = e.getMessage();
        _train.setBuildFailedMessage(msg);
        _train.setStatusCode(Train.CODE_BUILD_FAILED);
        _train.setBuildFailed(true);
        log.debug(msg);

        if (trainManager.isBuildMessagesEnabled()) {
            // don't pass the object _train to the GUI, can cause thread lock
            String trainName = _train.getName();
            String trainDescription = _train.getDescription();
            if (e.getExceptionType().equals(BuildFailedException.NORMAL)) {
                JOptionPane.showMessageDialog(null, msg, MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                        new Object[] { trainName, trainDescription }), JOptionPane.ERROR_MESSAGE);
            } else {
                // build error, could not find destinations for cars departing staging
                Object[] options = { Bundle.getMessage("buttonRemoveCars"), Bundle.getMessage("ButtonOK") };
                int results = JOptionPane.showOptionDialog(null, msg,
                        MessageFormat.format(Bundle.getMessage("buildErrorMsg"),
                                new Object[] { trainName, trainDescription }),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);
                if (results == 0) {
                    log.debug("User requested that cars be removed from staging track");
                    removeCarsFromStaging();
                }
            }
            int size = carManager.getList(_train).size();
            if (size > 0) {
                if (JOptionPane.showConfirmDialog(null,
                        MessageFormat.format(Bundle.getMessage("buildCarsResetTrain"),
                                new Object[] { size, trainName }),
                        Bundle.getMessage("buildResetTrain"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    _train.reset();
                }
            } else if ((size = engineManager.getList(_train).size()) > 0) {
                if (JOptionPane.showConfirmDialog(null,
                        MessageFormat.format(Bundle.getMessage("buildEnginesResetTrain"),
                                new Object[] { size, trainName }),
                        Bundle.getMessage("buildResetTrain"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    _train.reset();
                }
            }
        }
        if (_buildReport != null) {
            addLine(_buildReport, ONE, msg);
            // Write to disk and close buildReport
            addLine(_buildReport, ONE,
                    MessageFormat.format(Bundle.getMessage("buildFailedMsg"), new Object[] { _train.getName() }));
            _buildReport.flush();
            _buildReport.close();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainBuilder.class);

}
