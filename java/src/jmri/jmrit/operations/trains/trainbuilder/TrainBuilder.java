package jmri.jmrit.operations.trains.trainbuilder;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.*;
import jmri.jmrit.operations.trains.csv.TrainCsvManifest;
import jmri.util.swing.JmriJOptionPane;

/**
 * Builds a train and then creates the train's manifest.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2011, 2012, 2013,
 *         2014, 2015, 2021, 2026
 */
public class TrainBuilder extends TrainBuilderCars {

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
     * <li>Service locations based on train direction, location car types, roads
     * and loads.
     * <li>Ignore track direction when train is a local (serves one location)
     * </ol>
     * <p>
     * History:
     * <p>
     * First version of train builder found cars along a train's route and
     * assigned destinations (tracks) willing to accept the car. This is called
     * the random method as cars just bounce around the layout without purpose.
     * Afterwards custom loads and routing was added to the program. Cars with
     * custom loads or final destinations move with purpose as those cars are
     * routed. The last major feature added was car divisions. Cars assigned a
     * division are always routed.
     * <p>
     * The program was written around the concept of a build report. The report
     * provides a description of the train build process and the steps taken to
     * place rolling stock in a train. The goal was to help users understand why
     * rolling stock was either assigned to the train or not, and which choices
     * the program had available when determining an engine's or car's
     * destination.
     *
     * @param train the train that is to be built
     * @return True if successful.
     */
    public boolean build(Train train) {
        setTrain(train);
        try {
            build();
            return true;
        } catch (BuildFailedException e) {
            buildFailed(e);
            return false;
        }
    }

    private void build() throws BuildFailedException {
        setStartTime(new Date());
        log.debug("Building train ({})", getTrain().getName());

        getTrain().setStatusCode(Train.CODE_BUILDING);
        getTrain().setBuilt(false);
        getTrain().setLeadEngine(null);

        createBuildReportFile(); // backup build report and create new
        showBuildReportInfo(); // add the build report header information
        setUpRoute(); // load route, departure and terminate locations
        showTrainBuildOptions(); // show the build options
        showSpecificTrainBuildOptions(); // show the train build options
        showAndInitializeTrainRoute(); // show the train's route and initialize
        showIfLocalSwitcher(); // show if this train a switcher
        showTrainRequirements(); // show how many engines, caboose, FRED changes
        showTrainServices(); // engine roads, owners, built dates, and types
        getAndRemoveEnginesFromList(); // get a list of available engines
        showEnginesByLocation(); // list available engines by location
        determineIfTrainTerminatesIntoStaging(); // find staging terminus track
        determineIfTrainDepartsStagingAndAddEngines(); // add engines if staging
        addEnginesToTrain(); // 1st, 2nd and 3rd engine swaps in a train's route
        showTrainCarRoads(); // show car roads that this train will service
        showTrainCabooseRoads(); // show caboose roads that this train will service
        showTrainCarTypes(); // show car types that this train will service
        showTrainLoadNames(); // show load names that this train will service
        createCarList(); // remove unwanted cars
        adjustCarsInStaging(); // adjust for cars on one staging track
        showCarsByLocation(); // list available cars by location
        sortCarsOnFifoLifoTracks(); // sort cars on FIFO or LIFO tracks
        saveCarFinalDestinations(); // save car's final dest and schedule id
        addCabooseOrFredToTrain(); // caboose and FRED changes
        removeCaboosesAndCarsWithFred(); // done with cabooses and FRED
        blockCarsFromStaging(); // block cars from staging
        showTracksNotQuickService(); // list tracks that aren't using quick service

        addCarsToTrain(); // finds and adds cars to the train (main routine)

        checkStuckCarsInStaging(); // determine if cars are stuck in staging
        setTrainBuildStatus(); // show how well the build went
        checkEngineHP(); // determine if train has appropriate engine HP 
        checkNumnberOfEnginesNeededHPT(); // check train engine requirements
        showCarsNotRoutable(); // list cars that couldn't be routed
        finshBuildReport(); // number of warnings, build time

        getBuildReport().flush();
        getBuildReport().close();

        createManifests(); // now make Manifests

        // notify locations have been modified by this train's build
        for (Location location : _modifiedLocations) {
            location.setStatus(Location.MODIFIED);
        }

        // operations automations use wait for train built to create custom
        // manifests and switch lists
        getTrain().setPrinted(false);
        getTrain().setSwitchListStatus(Train.UNKNOWN);
        getTrain().setCurrentLocation(getTrain().getTrainDepartsRouteLocation());
        getTrain().setBuilt(true);
        // create and place train icon
        getTrain().moveTrainIcon(getTrain().getTrainDepartsRouteLocation());

        log.debug("Done building train ({})", getTrain().getName());
        showWarningMessage();
    }

    /**
     * Figures out if the train terminates into staging, and if true, sets the
     * termination track. Note if the train is returning back to the same track
     * in staging getTerminateStagingTrack() is null, and is loaded later when the
     * departure track is determined.
     * 
     * @throws BuildFailedException if staging track can't be found
     */
    private void determineIfTrainTerminatesIntoStaging() throws BuildFailedException {
        // does train terminate into staging?
        setTerminateStagingTrack(null);
        List<Track> stagingTracksTerminate = getTerminateLocation().getTracksByMoves(Track.STAGING);
        if (stagingTracksTerminate.size() > 0) {
            addLine(THREE, BLANK_LINE);
            addLine(ONE, Bundle.getMessage("buildTerminateStaging", getTerminateLocation().getName(),
                    Integer.toString(stagingTracksTerminate.size())));
            if (stagingTracksTerminate.size() > 1 && Setup.isStagingPromptToEnabled()) {
                setTerminateStagingTrack(promptToStagingDialog());
                setStartTime(new Date()); // reset build time since user can take
                                         // awhile to pick
            } else {
                // is this train returning to the same staging in aggressive
                // mode?
                if (getDepartureLocation() == getTerminateLocation() &&
                        Setup.isBuildAggressive() &&
                        Setup.isStagingTrackImmediatelyAvail()) {
                    addLine(ONE, Bundle.getMessage("buildStagingReturn", getTerminateLocation().getName()));
                } else {
                    for (Track track : stagingTracksTerminate) {
                        if (checkTerminateStagingTrack(track)) {
                            setTerminateStagingTrack(track);
                            addLine(ONE, Bundle.getMessage("buildStagingAvail",
                                    getTerminateStagingTrack().getName(), getTerminateLocation().getName()));
                            break;
                        }
                    }
                }
            }
            if (getTerminateStagingTrack() == null) {
                // is this train returning to the same staging in aggressive
                // mode?
                if (getDepartureLocation() == getTerminateLocation() &&
                        Setup.isBuildAggressive() &&
                        Setup.isStagingTrackImmediatelyAvail()) {
                    log.debug("Train is returning to same track in staging");
                } else {
                    addLine(ONE, Bundle.getMessage("buildErrorStagingFullNote"));
                    throw new BuildFailedException(
                            Bundle.getMessage("buildErrorStagingFull", getTerminateLocation().getName()));
                }
            }
        }
    }

    /**
     * Figures out if the train is departing staging, and if true, sets the
     * departure track. Also sets the arrival track if the train is returning to
     * the same departure track in staging.
     * 
     * @throws BuildFailedException if staging departure track not found
     */
    private void determineIfTrainDepartsStagingAndAddEngines() throws BuildFailedException {
        // allow up to two engine and caboose swaps in the train's route
        RouteLocation engineTerminatesFirstLeg = getTrain().getTrainTerminatesRouteLocation();

        // Adjust where the locos will terminate
        if ((getTrain().getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                getTrain().getSecondLegStartRouteLocation() != null) {
            engineTerminatesFirstLeg = getTrain().getSecondLegStartRouteLocation();
        } else if ((getTrain().getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                getTrain().getThirdLegStartRouteLocation() != null) {
            engineTerminatesFirstLeg = getTrain().getThirdLegStartRouteLocation();
        }

        // determine if train is departing staging
        List<Track> stagingTracks = getDepartureLocation().getTracksByMoves(Track.STAGING);
        if (stagingTracks.size() > 0) {
            addLine(THREE, BLANK_LINE);
            addLine(ONE, Bundle.getMessage("buildDepartStaging", getDepartureLocation().getName(),
                    Integer.toString(stagingTracks.size())));
            if (stagingTracks.size() > 1 && Setup.isStagingPromptFromEnabled()) {
                setDepartureStagingTrack(promptFromStagingDialog());
                setStartTime(new Date()); // restart build timer
                if (getDepartureStagingTrack() == null) {
                    showTrainRequirements();
                    throw new BuildFailedException(
                            Bundle.getMessage("buildErrorStagingEmpty", getDepartureLocation().getName()));
                }
            } else {
                for (Track track : stagingTracks) {
                    // is the departure track available?
                    if (!checkDepartureStagingTrack(track)) {
                        addLine(SEVEN,
                                Bundle.getMessage("buildStagingTrackRestriction", track.getName(), getTrain().getName()));
                        continue;
                    }
                    setDepartureStagingTrack(track);
                    // try each departure track for the required engines
                    if (getEngines(getTrain().getNumberEngines(), getTrain().getEngineModel(), getTrain().getEngineRoad(),
                            getTrain().getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)) {
                        addLine(SEVEN, Bundle.getMessage("buildDoneAssignEnginesStaging"));
                        break; // done!
                    }
                    setDepartureStagingTrack(null);
                }
            }
            if (getDepartureStagingTrack() == null) {
                showTrainRequirements();
                throw new BuildFailedException(Bundle.getMessage("buildErrorStagingEmpty", getDepartureLocation().getName()));
            }
        }
        getTrain().setTerminationTrack(getTerminateStagingTrack());
        getTrain().setDepartureTrack(getDepartureStagingTrack());
    }

    /**
     * Adds and removes cabooses or car with FRED in the train's route. Up to 2
     * caboose changes.
     * 
     * @throws BuildFailedException
     */
    private void addCabooseOrFredToTrain() throws BuildFailedException {
        // allow up to two caboose swaps in the train's route
        RouteLocation cabooseOrFredTerminatesFirstLeg = getTrain().getTrainTerminatesRouteLocation();
        RouteLocation cabooseOrFredTerminatesSecondLeg = getTrain().getTrainTerminatesRouteLocation();

        // determine if there are any caboose changes
        if ((getTrain().getSecondLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (getTrain().getSecondLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            cabooseOrFredTerminatesFirstLeg = getTrain().getSecondLegStartRouteLocation();
        } else if ((getTrain().getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (getTrain().getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            cabooseOrFredTerminatesFirstLeg = getTrain().getThirdLegStartRouteLocation();
        }
        if ((getTrain().getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (getTrain().getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            cabooseOrFredTerminatesSecondLeg = getTrain().getThirdLegStartRouteLocation();
        }

        // Do caboose changes in reverse order in case there isn't enough track
        // space second caboose change?
        if ((getTrain().getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE &&
                getTrain().getThirdLegStartRouteLocation() != null &&
                getTrain().getTrainTerminatesRouteLocation() != null) {
            getCaboose(getTrain().getThirdLegCabooseRoad(), _thirdLeadEngine, getTrain().getThirdLegStartRouteLocation(),
                    getTrain().getTrainTerminatesRouteLocation(), true);
        }

        // first caboose change?
        if ((getTrain().getSecondLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE &&
                getTrain().getSecondLegStartRouteLocation() != null &&
                cabooseOrFredTerminatesSecondLeg != null) {
            getCaboose(getTrain().getSecondLegCabooseRoad(), _secondLeadEngine, getTrain().getSecondLegStartRouteLocation(),
                    cabooseOrFredTerminatesSecondLeg, true);
        }

        // departure caboose or car with FRED
        getCaboose(getTrain().getCabooseRoad(), getTrain().getLeadEngine(), getTrain().getTrainDepartsRouteLocation(),
                cabooseOrFredTerminatesFirstLeg, getTrain().isCabooseNeeded());
        getCarWithFred(getTrain().getCabooseRoad(), getTrain().getTrainDepartsRouteLocation(), cabooseOrFredTerminatesFirstLeg);
    }

    /**
     * Routine to find and add available cars to the train. In normal mode
     * performs a single pass. In aggressive mode, will perform multiple passes.
     * If train is departing staging and in aggressive mode, will try again
     * using normal mode if there's a train build issue.
     * 
     * @throws BuildFailedException
     */
    private void addCarsToTrain() throws BuildFailedException {
        addLine(THREE,
                Bundle.getMessage("buildTrain", getTrain().getNumberCarsRequested(), getTrain().getName(), getCarList().size()));

        if (Setup.isBuildAggressive() && !getTrain().isBuildTrainNormalEnabled()) {
            // perform a multiple pass build for this train, default is two
            // passes
            int pass = 0;
            while (pass++ < Setup.getNumberPasses()) {
                addCarsToTrain(pass, false);
            }
            // are cars stuck in staging?
            secondAttemptNormalBuild();
        } else {
            addCarsToTrain(Setup.getNumberPasses(), true); // normal build one
                                                           // pass
        }
    }

    /**
     * If cars stuck in staging, try building again in normal mode.
     * 
     * @throws BuildFailedException
     */
    private void secondAttemptNormalBuild() throws BuildFailedException {
        if (Setup.isStagingTryNormalBuildEnabled() && isCarStuckStaging()) {
            addLine(ONE, Bundle.getMessage("buildFailedTryNormalMode"));
            addLine(ONE, BLANK_LINE);
            getTrain().reset();
            getTrain().setStatusCode(Train.CODE_BUILDING);
            getTrain().setLeadEngine(null);
            // using the same departure and termination tracks
            getTrain().setDepartureTrack(getDepartureStagingTrack());
            getTrain().setTerminationTrack(getTerminateStagingTrack());
            showAndInitializeTrainRoute();
            getAndRemoveEnginesFromList();
            addEnginesToTrain();
            createCarList();
            adjustCarsInStaging();
            showCarsByLocation();
            addCabooseOrFredToTrain();
            removeCaboosesAndCarsWithFred();
            saveCarFinalDestinations(); // save final destination and schedule
                                        // id
            blockCarsFromStaging(); // block cars from staging
            addCarsToTrain(Setup.getNumberPasses(), true); // try normal build
                                                           // one pass
        }
    }

    /**
     * Main routine to place cars into the train. Can be called multiple times.
     * When departing staging, ignore staged cars on the first pass unless the
     * option to build normal was selected by user.
     *
     * @param pass   Which pass when there are multiple passes requested by
     *               user.
     * @param normal True if single pass or normal mode is requested by user.
     * @throws BuildFailedException
     */
    private void addCarsToTrain(int pass, boolean normal) throws BuildFailedException {
        addLine(THREE, BLANK_LINE);
        if (normal) {
            addLine(THREE, Bundle.getMessage("NormalModeWhenBuilding"));
        } else {
            addLine(THREE, Bundle.getMessage("buildMultiplePass", pass, Setup.getNumberPasses()));
        }
        // now go through each location starting at departure and place cars as
        // requested
        for (RouteLocation rl : getRouteList()) {
            if (getTrain().isLocationSkipped(rl)) {
                addLine(ONE,
                        Bundle.getMessage("buildLocSkipped", rl.getName(), rl.getId(), getTrain().getName()));
                continue;
            }
            if (!rl.isPickUpAllowed() && !rl.isLocalMovesAllowed()) {
                addLine(ONE,
                        Bundle.getMessage("buildLocNoPickups", getTrain().getRoute().getName(), rl.getId(), rl.getName()));
                continue;
            }
            // no pick ups from staging unless at the start of the train's route
            if (rl != getTrain().getTrainDepartsRouteLocation() && rl.getLocation().isStaging()) {
                addLine(ONE, Bundle.getMessage("buildNoPickupsFromStaging", rl.getName()));
                continue;
            }
            // the next check provides a build report message if there's an
            // issue with the train direction
            if (!checkPickUpTrainDirection(rl)) {
                continue;
            }
            _completedMoves = 0; // moves completed for this location
            _reqNumOfMoves = rl.getMaxCarMoves() - rl.getCarMoves();

            if (!normal) {
                if (rl == getTrain().getTrainDepartsRouteLocation()) {
                    _reqNumOfMoves = (rl.getMaxCarMoves() - rl.getCarMoves()) * pass / Setup.getNumberPasses();
                } else if (pass == 1) {
                    _reqNumOfMoves = (rl.getMaxCarMoves() - rl.getCarMoves()) / 2;
                    // round up requested moves
                    int remainder = (rl.getMaxCarMoves() - rl.getCarMoves()) % 2;
                    if (remainder > 0) {
                        _reqNumOfMoves++;
                    }
                }
            }

            // if departing staging make adjustments
            if (rl == getTrain().getTrainDepartsRouteLocation()) {
                if (pass == 1) {
                    makeAdjustmentsIfDepartingStaging();
                } else {
                    restoreCarsIfDepartingStaging();
                }
            }

            int saveReqMoves = _reqNumOfMoves; // save a copy for status message
            addLine(ONE,
                    Bundle.getMessage("buildLocReqMoves", rl.getName(), rl.getId(), _reqNumOfMoves,
                            rl.getMaxCarMoves() - rl.getCarMoves(), rl.getMaxCarMoves()));
            addLine(FIVE, BLANK_LINE);

            // show the car load generation options for staging
            if (rl == getTrain().getTrainDepartsRouteLocation()) {
                showLoadGenerationOptionsStaging();
            }

            _carIndex = 0; // see reportCarsNotMoved(rl) below

            findDestinationsForCarsFromLocation(rl, false); // first pass

            // perform 2nd pass if aggressive mode and there are requested
            // moves. This will perform local moves at this location, services
            // off spot tracks, only in aggressive mode and at least one car
            // has a new destination
            if (Setup.isBuildAggressive() && saveReqMoves != _reqNumOfMoves) {
                log.debug("Perform extra pass at location ({})", rl.getName());
                // use up to half of the available moves left for this location
                if (_reqNumOfMoves < (rl.getMaxCarMoves() - rl.getCarMoves()) / 2) {
                    _reqNumOfMoves = (rl.getMaxCarMoves() - rl.getCarMoves()) / 2;
                }
                findDestinationsForCarsFromLocation(rl, true); // second pass

                // we might have freed up space at a spur that has an alternate
                // track
                if (redirectCarsFromAlternateTrack()) {
                    addLine(SEVEN, BLANK_LINE);
                }
            }
            if (rl == getTrain().getTrainDepartsRouteLocation() && pass == Setup.getNumberPasses() && isCarStuckStaging()) {
                return; // report ASAP that there are stuck cars
            }
            addLine(ONE,
                    Bundle.getMessage("buildStatusMsg",
                            (saveReqMoves <= _completedMoves ? Bundle.getMessage("Success")
                                    : Bundle.getMessage("Partial")),
                            Integer.toString(_completedMoves), Integer.toString(saveReqMoves), rl.getName(),
                            getTrain().getName()));

            if (_reqNumOfMoves <= 0 && pass == Setup.getNumberPasses()) {
                showCarsNotMoved(rl);
            }
        }
    }

    private void setTrainBuildStatus() {
        if (_numberCars < getTrain().getNumberCarsRequested()) {
            getTrain().setStatusCode(Train.CODE_PARTIAL_BUILT);
            addLine(ONE,
                    Train.PARTIAL_BUILT +
                            " " +
                            getTrain().getNumberCarsWorked() +
                            "/" +
                            getTrain().getNumberCarsRequested() +
                            " " +
                            Bundle.getMessage("cars"));
        } else {
            getTrain().setStatusCode(Train.CODE_BUILT);
            addLine(ONE,
                    Train.BUILT + " " + getTrain().getNumberCarsWorked() + " " + Bundle.getMessage("cars"));
        }
    }

    private void createManifests() throws BuildFailedException {
        new TrainManifest(getTrain());
        try {
            new JsonManifest(getTrain()).build();
        } catch (IOException | RuntimeException ex) {
            log.error("Unable to create JSON manifest: {}", ex.getLocalizedMessage());
            log.error("JSON manifest stack trace:", ex);
            throw new BuildFailedException(ex);
        }
        new TrainCsvManifest(getTrain());
    }

    private void showWarningMessage() {
        if (trainManager.isBuildMessagesEnabled() && _warnings > 0) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("buildCheckReport", getTrain().getName(), getTrain().getDescription()),
                    Bundle.getMessage("buildWarningMsg", getTrain().getName(), _warnings),
                    JmriJOptionPane.WARNING_MESSAGE);
        }
    }

    private void buildFailed(BuildFailedException e) {
        String msg = e.getMessage();
        getTrain().setBuildFailedMessage(msg);
        getTrain().setBuildFailed(true);
        log.debug(msg);

        if (trainManager.isBuildMessagesEnabled()) {
            // don't pass the object getTrain() to the GUI, can cause thread lock
            String trainName = getTrain().getName();
            String trainDescription = getTrain().getDescription();
            if (e.getExceptionType().equals(BuildFailedException.NORMAL)) {
                JmriJOptionPane.showMessageDialog(null, msg,
                        Bundle.getMessage("buildErrorMsg", trainName, trainDescription), JmriJOptionPane.ERROR_MESSAGE);
            } else {
                // build error, could not find destinations for cars departing
                // staging
                Object[] options = {Bundle.getMessage("buttonRemoveCars"), Bundle.getMessage("ButtonOK")};
                int results = JmriJOptionPane.showOptionDialog(null, msg,
                        Bundle.getMessage("buildErrorMsg", trainName, trainDescription),
                        JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.ERROR_MESSAGE, null, options, options[1]);
                if (results == 0) {
                    log.debug("User requested that cars be removed from staging track");
                    removeCarsFromStaging();
                }
            }
            int size = carManager.getList(getTrain()).size();
            if (size > 0) {
                if (JmriJOptionPane.showConfirmDialog(null,
                        Bundle.getMessage("buildCarsResetTrain", size, trainName),
                        Bundle.getMessage("buildResetTrain"),
                        JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
                    getTrain().setStatusCode(Train.CODE_TRAIN_RESET);
                }
            } else if ((size = engineManager.getList(getTrain()).size()) > 0) {
                if (JmriJOptionPane.showConfirmDialog(null,
                        Bundle.getMessage("buildEnginesResetTrain", size, trainName),
                        Bundle.getMessage("buildResetTrain"),
                        JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
                    getTrain().setStatusCode(Train.CODE_TRAIN_RESET);
                }
            }
        } else {
            // build messages disabled
            // remove cars and engines from this train via property change
            getTrain().setStatusCode(Train.CODE_TRAIN_RESET);
        }

        getTrain().setStatusCode(Train.CODE_BUILD_FAILED);

        if (getBuildReport() != null) {
            addLine(ONE, msg);
            // Write to disk and close buildReport
            addLine(ONE,
                    Bundle.getMessage("buildFailedMsg", getTrain().getName()));
            getBuildReport().flush();
            getBuildReport().close();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainBuilder.class);

}
