package jmri.jmrit.operations.router;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.*;

/**
 * Router for car movement. This code attempts to find a way (a route) to move a
 * car to its final destination through the use of two or more trains. First the
 * code tries to move car using a single train. If that fails, attempts are made
 * using two trains via a classification/interchange (C/I) tracks, then yard
 * tracks if enabled. Next attempts are made using three or more trains using
 * any combination of C/I and yard tracks. If that fails and routing via staging
 * is enabled, the code tries two trains using staging tracks, then multiple
 * trains using a combination of C/I, yards, and staging tracks. Currently the
 * router is limited to seven trains.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011, 2012, 2013, 2015, 2021,
 *         2022, 2024
 */
public class Router extends TrainCommon implements InstanceManagerAutoDefault {

    TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);

    protected final List<Track> _nextLocationTracks = new ArrayList<>();
    protected final List<Track> _lastLocationTracks = new ArrayList<>();
    private final List<Track> _otherLocationTracks = new ArrayList<>();

    protected final List<Track> _next2ndLocationTracks = new ArrayList<>();
    protected final List<Track> _next3rdLocationTracks = new ArrayList<>();
    protected final List<Track> _next4thLocationTracks = new ArrayList<>();

    protected final List<Train> _nextLocationTrains = new ArrayList<>();
    protected final List<Train> _lastLocationTrains = new ArrayList<>();

    protected Hashtable<String, Train> _listTrains = new Hashtable<>();

    protected static final String STATUS_NOT_THIS_TRAIN = Bundle.getMessage("RouterTrain");
    public static final String STATUS_NOT_THIS_TRAIN_PREFIX =
            STATUS_NOT_THIS_TRAIN.substring(0, STATUS_NOT_THIS_TRAIN.indexOf('('));
    protected static final String STATUS_NOT_ABLE = Bundle.getMessage("RouterNotAble");
    protected static final String STATUS_ROUTER_DISABLED = Bundle.getMessage("RouterDisabled");

    private String _status = "";
    private Train _train = null;
    PrintWriter _buildReport = null; // build report
    Date _startTime; // when routing started

    private static final String SEVEN = Setup.BUILD_REPORT_VERY_DETAILED;
    private boolean _addtoReport = false;
    private boolean _addtoReportVeryDetailed = false;

    /**
     * Returns the status of the router when using the setDestination() for a
     * car.
     *
     * @return Track.OKAY, STATUS_NOT_THIS_TRAIN, STATUS_NOT_ABLE,
     *         STATUS_ROUTER_DISABLED, or the destination track status is
     *         there's an issue.
     */
    public String getStatus() {
        return _status;
    }

    /**
     * Determines if car can be routed to the destination track
     * 
     * @param car         the car being tested
     * @param train       the first train servicing the car, can be null
     * @param track       the destination track, can not be null
     * @param buildReport the report, can be null
     * @return true if the car can be routed to the track
     */
    public boolean isCarRouteable(Car car, Train train, Track track, PrintWriter buildReport) {
        addLine(buildReport, SEVEN, Bundle.getMessage("RouterIsCarRoutable",
                car.toString(), car.getLocationName(), car.getTrackName(), car.getLoadName(),
                track.getLocation().getName(), track.getName()));
        return isCarRouteable(car, train, track.getLocation(), track, buildReport);
    }

    public boolean isCarRouteable(Car car, Train train, Location destination, Track track, PrintWriter buildReport) {
        Car c = car.copy();
        c.setTrack(car.getTrack());
        c.setFinalDestination(destination);
        c.setFinalDestinationTrack(track);
        boolean results = setDestination(c, train, buildReport);
        c.setDestination(null, null); // clear router car destinations
        c.setFinalDestinationTrack(null);
        // transfer route path info
        car.setRoutePath(c.getRoutePath());
        return results;
    }

    /**
     * Attempts to set the car's destination if a final destination exists. Only
     * sets the car's destination if the train is part of the car's route.
     *
     * @param car         the car to route
     * @param train       the first train to carry this car, can be null
     * @param buildReport PrintWriter for build report, and can be null
     * @return true if car can be routed.
     */
    public boolean setDestination(Car car, Train train, PrintWriter buildReport) {
        if (car.getTrack() == null || car.getFinalDestination() == null) {
            return false;
        }
        _startTime = new Date();
        _status = Track.OKAY;
        _train = train;
        _buildReport = buildReport;
        _addtoReport = Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED) ||
                Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED);
        _addtoReportVeryDetailed = Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED);
        log.debug("Car ({}) at location ({}, {}) final destination ({}, {}) car routing begins", car,
                car.getLocationName(), car.getTrackName(), car.getFinalDestinationName(),
                car.getFinalDestinationTrackName());
        if (_train != null) {
            log.debug("Routing using train ({})", train.getName());
        }
        // is car part of kernel?
        if (car.getKernel() != null && !car.isLead()) {
            return false;
        }
        // note clone car has the car's "final destination" as its destination
        Car clone = clone(car);
        // Note the following test doesn't check for car length which is what we
        // want.
        // Also ignores spur schedule since the car's destination is already
        // set.
        _status = clone.checkDestination(clone.getDestination(), clone.getDestinationTrack());
        if (!_status.equals(Track.OKAY)) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterCanNotDeliverCar",
                    car.toString(), car.getFinalDestinationName(), car.getFinalDestinationTrackName(),
                    _status, (car.getFinalDestinationTrack() == null ? Bundle.getMessage("RouterDestination")
                            : car.getFinalDestinationTrack().getTrackTypeName())));
            return false;
        }
        // check to see if car has a destination track or one is available
        if (!checkForDestinationTrack(clone)) {
            return false; // no destination track found
        }
        // check to see if car will move to destination using a single train
        if (checkForSingleTrain(car, clone)) {
            return true; // a single train can service this car
        }
        if (!Setup.isCarRoutingEnabled()) {
            log.debug("Car ({}) final destination ({}) is not served directly by any train", car,
                    car.getFinalDestinationName()); // NOI18N
            _status = STATUS_ROUTER_DISABLED;
            car.setFinalDestination(null);
            car.setFinalDestinationTrack(null);
            return false;
        }
        log.debug("Car ({}) final destination ({}) is not served by a single train", car,
                car.getFinalDestinationName());
        // was the request for a local move? Try multiple trains to move car
        if (car.getLocationName().equals(car.getFinalDestinationName())) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterCouldNotFindTrain",
                    car.getLocationName(), car.getTrackName(), car.getFinalDestinationName(),
                    car.getFinalDestinationTrackName()));
        }
        if (_addtoReport) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterBeginTwoTrain",
                    car.toString(), car.getLocationName(), car.getFinalDestinationName()));
        }

        _nextLocationTracks.clear();
        _next2ndLocationTracks.clear();
        _next3rdLocationTracks.clear();
        _next4thLocationTracks.clear();
        _lastLocationTracks.clear();
        _otherLocationTracks.clear();
        _nextLocationTrains.clear();
        _lastLocationTrains.clear();
        _listTrains.clear();

        // first try using 2 trains and an interchange track to route the car
        if (setCarDestinationTwoTrainsInterchange(car)) {
            if (car.getDestination() == null) {
                log.debug(
                        "Was able to find a route via classification/interchange track, but not using specified train" +
                                " or car destination not set, try again using yard tracks"); // NOI18N
                if (setCarDestinationTwoTrainsYard(car)) {
                    log.debug("Was able to find route via yard ({}, {}) for car ({})", car.getDestinationName(),
                            car.getDestinationTrackName(), car);
                }
            } else {
                log.debug("Was able to find route via interchange ({}, {}) for car ({})", car.getDestinationName(),
                        car.getDestinationTrackName(), car);
            }
            // now try 2 trains using a yard track
        } else if (setCarDestinationTwoTrainsYard(car)) {
            log.debug("Was able to find route via yard ({}, {}) for car ({}) using two trains",
                    car.getDestinationName(), car.getDestinationTrackName(), car);
            // now try 3 or more trains to route car, but not through staging
        } else if (setCarDestinationMultipleTrains(car, false)) {
            log.debug("Was able to find multiple train route for car ({})", car);
            // now try 2 trains using a staging track to connect
        } else if (setCarDestinationTwoTrainsStaging(car)) {
            log.debug("Was able to find route via staging ({}, {}) for car ({}) using two trains",
                    car.getDestinationName(), car.getDestinationTrackName(), car);
            // now try 3 or more trains to route car, include staging if enabled
        } else if (setCarDestinationMultipleTrains(car, true)) {
            log.debug("Was able to find multiple train route for car ({}) through staging", car);
        } else {
            log.debug("Wasn't able to set route for car ({}) took {} mSec", car,
                    new Date().getTime() - _startTime.getTime());
            _status = STATUS_NOT_ABLE;
            return false; // maybe next time
        }
        return true; // car's destination has been set
    }

    /*
     * Checks to see if the car has a destination track, no destination track,
     * searches for one. returns true if the car has a destination track or if
     * there's one available.
     */
    private boolean checkForDestinationTrack(Car clone) {
        if (clone.getDestination() != null && clone.getDestinationTrack() == null) {
            // determine if there's a track that can service the car
            String status = "";
            for (Track track : clone.getDestination().getTracksList()) {
                status = track.isRollingStockAccepted(clone);
                if (status.equals(Track.OKAY) || status.startsWith(Track.LENGTH)) {
                    log.debug("Track ({}) will accept car ({})", track.getName(), clone.toString());
                    break;
                }
            }
            if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
                addLine(_buildReport, SEVEN, _status = Bundle.getMessage("RouterNoTracks",
                        clone.getDestinationName(), clone.toString()));
                return false;
            }
        }
        return true;
    }

    /**
     * Checks to see if a single train can transport car to its final
     * destination. Special case if car is departing staging.
     *
     * @return true if single train can transport car to its final destination.
     */
    private boolean checkForSingleTrain(Car car, Car clone) {
        boolean trainServicesCar = false; // true the specified train can service the car
        Train testTrain = null;
        if (_train != null) {
            trainServicesCar = _train.isServiceable(_buildReport, clone);
        }
        if (trainServicesCar) {
            testTrain = _train; // use the specified train
            log.debug("Train ({}) can service car ({})", _train.getName(), car.toString());
        } else if (_train != null && !_train.getServiceStatus().equals(Train.NONE)) {
            // _train isn't able to service car
            // determine if car was attempting to go to the train's termination staging
            String trackName = car.getFinalDestinationTrackName();
            if (car.getFinalDestinationTrack() == null &&
                    car.getFinalDestinationName().equals(_train.getTrainTerminatesName()) &&
                    _train.getTerminationTrack() != null) {
                trackName = _train.getTerminationTrack().getName(); // use staging track
            }
            // report that train can't service car
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterTrainCanNotDueTo", _train.getName(), car.toString(),
                    car.getFinalDestinationName(), trackName, _train.getServiceStatus()));
            if (!car.getTrack().isStaging() &&
                    !_train.isServiceAllCarsWithFinalDestinationsEnabled()) {
                _status = MessageFormat.format(STATUS_NOT_THIS_TRAIN, new Object[]{_train.getName()});
                return true; // temporary issue with train moves, length, or destination track length
            }
        }
        // Determines if specified train can service car out of staging.
        // Note that the router code will try to route the car using
        // two or more trains just to get the car out of staging.
        if (car.getTrack().isStaging() && _train != null && !trainServicesCar) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterTrainCanNotStaging",
                    _train.getName(), car.toString(), car.getLocationName(),
                    clone.getDestinationName(), clone.getDestinationTrackName()));
            if (!_train.getServiceStatus().equals(Train.NONE)) {
                addLine(_buildReport, SEVEN, _train.getServiceStatus());
            }
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("RouterStagingTryRouting", car.toString(), clone.getLocationName(),
                            clone.getDestinationName(), clone.getDestinationTrackName()));
            // note that testTrain = null, return false
        } else if (!trainServicesCar) {
            List<Train> excludeTrains = new ArrayList<>(Arrays.asList(_train));
            testTrain = tmanager.getTrainForCar(clone, excludeTrains, _buildReport);
        }
        // report that another train could transport the car
        if (testTrain != null &&
                _train != null &&
                !trainServicesCar &&
                _train.isServiceAllCarsWithFinalDestinationsEnabled()) {
            // log.debug("Option to service all cars with a final destination is enabled");
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterOptionToCarry",
                    _train.getName(), testTrain.getName(), car.toString(),
                    clone.getDestinationName(), clone.getDestinationTrackName()));
            testTrain = null; // return false
        }
        if (testTrain != null) {
            return finishRouteUsingOneTrain(testTrain, car, clone);
        }
        return false;
    }

    /**
     * A single train can service the car. Provide various messages to build
     * report detailing which train can service the car. Also checks to see if
     * the needs to go the alternate track or yard track if the car's final
     * destination track is full. Returns false if car is stuck in staging. Sets
     * the car's destination if specified _train is available
     *
     * @return true for all cases except if car is departing staging and is
     *         stuck there.
     */
    private boolean finishRouteUsingOneTrain(Train testTrain, Car car, Car clone) {
        addLine(_buildReport, SEVEN, Bundle.getMessage("RouterTrainCanTransport", testTrain.getName(), car.toString(),
                car.getTrack().getTrackTypeName(), car.getLocationName(), car.getTrackName(),
                clone.getDestinationName(), clone.getDestinationTrackName()));
        showRoute(car, new ArrayList<>(Arrays.asList(testTrain)),
                new ArrayList<>(Arrays.asList(car.getFinalDestinationTrack())));
        // don't modify car if a train wasn't specified
        if (_train == null) {
            return true; // done, car can be routed
        }
        // now check to see if specified train can service car directly
        else if (_train != testTrain) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("TrainDoesNotServiceCar", _train.getName(), car.toString(),
                    clone.getDestinationName(), clone.getDestinationTrackName()));
            _status = MessageFormat.format(STATUS_NOT_THIS_TRAIN, new Object[]{testTrain.getName()});
            return true; // car can be routed, but not by this train!
        }
        _status = car.setDestination(clone.getDestination(), clone.getDestinationTrack());
        if (_status.equals(Track.OKAY)) {
            return true; // done, car has new destination
        }
        addLine(_buildReport, SEVEN,
                Bundle.getMessage("RouterCanNotDeliverCar", car.toString(), clone.getDestinationName(),
                        clone.getDestinationTrackName(), _status,
                        (clone.getDestinationTrack() == null ? Bundle.getMessage("RouterDestination")
                                : clone.getDestinationTrack().getTrackTypeName())));
        // check to see if an alternative track was specified
        if ((_status.startsWith(Track.LENGTH) || _status.startsWith(Track.SCHEDULE)) &&
                clone.getDestinationTrack() != null &&
                clone.getDestinationTrack().getAlternateTrack() != null &&
                clone.getDestinationTrack().getAlternateTrack() != car.getTrack()) {
            String status = car.setDestination(clone.getDestination(), clone.getDestinationTrack().getAlternateTrack());
            if (status.equals(Track.OKAY)) {
                if (_train.isServiceable(car)) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("RouterSendCarToAlternative",
                            car.toString(), clone.getDestinationTrack().getAlternateTrack().getName(),
                            clone.getDestination().getName()));
                    return true; // car is going to alternate track
                }
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("RouterNotSendCarToAlternative", _train.getName(), car.toString(),
                                clone.getDestinationTrack().getAlternateTrack().getName(),
                                clone.getDestination().getName()));
            } else {
                addLine(_buildReport, SEVEN, Bundle.getMessage("RouterAlternateFailed",
                        clone.getDestinationTrack().getAlternateTrack().getName(), status));
            }
        } else if (clone.getDestinationTrack() != null &&
                clone.getDestinationTrack().getAlternateTrack() != null &&
                clone.getDestinationTrack().getAlternateTrack() == car.getTrack()) {
            // state that car is spotted at the alternative track
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterAtAlternate",
                    car.toString(), clone.getDestinationTrack().getAlternateTrack().getName(),
                    clone.getLocationName(), clone.getDestinationTrackName()));
        } else if (car.getLocation() == clone.getDestination()) {
            // state that alternative and yard track options are not available
            // if car is at final destination
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("RouterIgnoreAlternate", car.toString(), car.getLocationName()));
        }
        // check to see if spur was full, if so, forward to yard if possible
        if (Setup.isForwardToYardEnabled() &&
                _status.startsWith(Track.LENGTH) &&
                car.getLocation() != clone.getDestination()) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterSpurFull",
                    clone.getDestinationTrackName(), clone.getDestinationName()));
            Location dest = clone.getDestination();
            List<Track> yards = dest.getTracksByMoves(Track.YARD);
            log.debug("Found {} yard(s) at destination ({})", yards.size(), clone.getDestinationName());
            for (Track track : yards) {
                String status = car.setDestination(dest, track);
                if (status.equals(Track.OKAY)) {
                    if (!_train.isServiceable(car)) {
                        log.debug("Train ({}) can not deliver car ({}) to yard ({})", _train.getName(), car,
                                track.getName());
                        continue;
                    }
                    addLine(_buildReport, SEVEN, Bundle.getMessage("RouterSendCarToYard",
                            car.toString(), track.getName(), dest.getName()));
                    return true; // car is going to a yard
                } else {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("RouterCanNotUseYard",
                            track.getName(), status));
                }
            }
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterNoYardTracks",
                    dest.getName(), car.toString()));
        }
        car.setDestination(null, null);
        if (car.getTrack().isStaging()) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("RouterStagingTryRouting", car.toString(), clone.getLocationName(),
                            clone.getDestinationName(), clone.getDestinationTrackName()));
            return false; // try 2 or more trains
        }
        return true; // able to route, but unable to set the car's destination
    }

    /**
     * Sets a car's destination to an interchange track if two trains can route
     * the car.
     *
     * @param car the car to be routed
     * @return true if car's destination has been modified to an interchange.
     *         False if an interchange track wasn't found that could service the
     *         car's final destination.
     */
    private boolean setCarDestinationTwoTrainsInterchange(Car car) {
        return setCarDestinationTwoTrains(car, Track.INTERCHANGE);
    }

    /**
     * Sets a car's destination to a yard track if two trains can route the car.
     *
     * @param car the car to be routed
     * @return true if car's destination has been modified to a yard. False if a
     *         yard track wasn't found that could service the car's final
     *         destination.
     */
    private boolean setCarDestinationTwoTrainsYard(Car car) {
        if (Setup.isCarRoutingViaYardsEnabled()) {
            return setCarDestinationTwoTrains(car, Track.YARD);
        }
        return false;
    }

    /**
     * Sets a car's destination to a staging track if two trains can route the
     * car.
     *
     * @param car the car to be routed
     * @return true if car's destination has been modified to a staging track.
     *         False if a staging track wasn't found that could service the
     *         car's final destination.
     */
    private boolean setCarDestinationTwoTrainsStaging(Car car) {
        if (Setup.isCarRoutingViaStagingEnabled()) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterAttemptStaging", car.toString(),
                    car.getFinalDestinationName(), car.getFinalDestinationTrackName()));
            return setCarDestinationTwoTrains(car, Track.STAGING);
        }
        return false;
    }

    /*
     * Note that this routine loads the last set of tracks and trains that can
     * service the car to its final location. This routine attempts to find a
     * "two" train route by cycling through various interchange, yard, and
     * staging tracks searching for a second train that can pull the car from
     * the track and deliver the car to the its destination. Then the program
     * determines if the train being built or another train (first) can deliver
     * the car to the track from its current location. If successful, a two
     * train route was found, and returns true.
     */
    private boolean setCarDestinationTwoTrains(Car car, String trackType) {
        Car testCar = clone(car); // reload
        log.debug("Two train routing, find {} track for car ({}) final destination ({}, {})", trackType, car,
                testCar.getDestinationName(), testCar.getDestinationTrackName());
        if (_addtoReportVeryDetailed) {
            addLine(_buildReport, SEVEN, BLANK_LINE);
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("RouterFindTrack", Track.getTrackTypeName(trackType), car.toString(),
                            testCar.getDestinationName(), testCar.getDestinationTrackName()));
        }
        boolean foundRoute = false;
        // now search for a yard or interchange that a train can pick up and
        // deliver the car to its destination
        List<Track> tracks = getTracks(car, testCar, trackType);
        for (Track track : tracks) {
            if (_addtoReportVeryDetailed) {
                addLine(_buildReport, SEVEN, BLANK_LINE);
                addLine(_buildReport, SEVEN, Bundle.getMessage("RouterFoundTrack",
                        Track.getTrackTypeName(trackType), track.getLocation().getName(),
                        track.getName(), car.toString()));
            }
            // test to see if there's a train that can deliver the car to its
            // final location
            testCar.setTrack(track);
            testCar.setDestination(car.getFinalDestination());
            // note that destination track can be null
            testCar.setDestinationTrack(car.getFinalDestinationTrack());
            Train secondTrain = tmanager.getTrainForCar(testCar, _buildReport);
            if (secondTrain == null) {
                // maybe the train being built can service the car?
                String specified = canSpecifiedTrainService(testCar);
                if (specified.equals(NOT_NOW)) {
                    secondTrain = _train;
                } else {
                    if (_addtoReportVeryDetailed) {
                        addLine(_buildReport, SEVEN, Bundle.getMessage("RouterNotFindTrain",
                                Track.getTrackTypeName(trackType), track.getLocation().getName(),
                                track.getName(), testCar.getDestinationName(),
                                testCar.getDestinationTrackName()));
                    }
                    continue;
                }
            }
            if (_addtoReportVeryDetailed) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("RouterTrainCanTransport",
                        secondTrain.getName(), car.toString(), Track.getTrackTypeName(trackType),
                        testCar.getLocationName(), testCar.getTrackName(), testCar.getDestinationName(),
                        testCar.getDestinationTrackName()));
            }
            // Save the "last" tracks for later use if needed
            _lastLocationTracks.add(track);
            _lastLocationTrains.add(secondTrain);
            // now try to forward car to this track
            testCar.setTrack(car.getTrack()); // restore car origin
            testCar.setDestination(track.getLocation());
            testCar.setDestinationTrack(track);
            // determine if car can be transported from current location to this
            // interchange, yard, or staging track
            // Now find a train that will transport the car to this track
            Train firstTrain = null;
            String specified = canSpecifiedTrainService(testCar);
            if (specified.equals(YES)) {
                firstTrain = _train;
            } else if (specified.equals(NOT_NOW)) {
                // found a two train route for this car, show the car's route
                List<Train> trains = new ArrayList<>(Arrays.asList(_train, secondTrain));
                tracks = new ArrayList<>(Arrays.asList(track, car.getFinalDestinationTrack()));
                showRoute(car, trains, tracks);

                addLine(_buildReport, SEVEN, Bundle.getMessage("RouterTrainCanNotDueTo",
                        _train.getName(), car.toString(), track.getLocation().getName(), track.getName(),
                        _train.getServiceStatus()));
                foundRoute = true; // issue is route moves or train length
            } else {
                firstTrain = tmanager.getTrainForCar(testCar, _buildReport);
            }
            // check to see if a train or trains with the same route is delivering and pulling the car to an interchange track
            if (firstTrain != null &&
                    firstTrain.getRoute() == secondTrain.getRoute() &&
                    track.isInterchange() &&
                    track.getPickupOption().equals(Track.ANY)) {
                if (_addtoReportVeryDetailed) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("RouterSameInterchange", firstTrain.getName(),
                            track.getLocation().getName(), track.getName()));
                }
                List<Train> excludeTrains = new ArrayList<>();
                excludeTrains.add(firstTrain);
                firstTrain = tmanager.getTrainForCar(testCar, excludeTrains, _buildReport);
            }
            if (firstTrain == null && _addtoReportVeryDetailed) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("RouterNotFindTrain",
                        testCar.getTrack().getTrackTypeName(), testCar.getTrack().getLocation().getName(),
                        testCar.getTrack().getName(),
                        testCar.getDestinationName(), testCar.getDestinationTrackName()));
            }
            // Can the specified train carry this car out of staging?
            if (_train != null && car.getTrack().isStaging() && !specified.equals(YES)) {
                if (_addtoReport) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("RouterTrainCanNot",
                            _train.getName(), car.toString(), car.getLocationName(),
                            car.getTrackName(), track.getLocation().getName(), track.getName()));
                }
                continue; // can't use this train
            }
            // Is the option for the specified train carry this car?
            if (firstTrain != null &&
                    _train != null &&
                    _train.isServiceAllCarsWithFinalDestinationsEnabled() &&
                    !specified.equals(YES)) {
                if (_addtoReport) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("RouterOptionToCarry",
                            _train.getName(), firstTrain.getName(), car.toString(),
                            track.getLocation().getName(), track.getName()));
                }
                continue; // can't use this train
            }
            if (firstTrain != null) {
                foundRoute = true; // found a route
                if (_addtoReportVeryDetailed) {
                    addLine(_buildReport, SEVEN,
                            Bundle.getMessage("RouterTrainCanTransport", firstTrain.getName(), car.toString(),
                                    Track.getTrackTypeName(trackType),
                                    testCar.getLocationName(), testCar.getTrackName(), testCar.getDestinationName(),
                                    testCar.getDestinationTrackName()));
                }
                // found a two train route for this car, show the car's route
                List<Train> trains = new ArrayList<>(Arrays.asList(firstTrain, secondTrain));
                tracks = new ArrayList<>(Arrays.asList(track, car.getFinalDestinationTrack()));
                showRoute(car, trains, tracks);

                _status = car.checkDestination(track.getLocation(), track);
                if (_status.startsWith(Track.LENGTH)) {
                    // if the issue is length at the interim track, add message
                    // to build report
                    addLine(_buildReport, SEVEN, Bundle.getMessage("RouterCanNotDeliverCar",
                            car.toString(), track.getLocation().getName(), track.getName(),
                            _status, track.getTrackTypeName()));
                    continue;
                }
                if (_status.equals(Track.OKAY)) {
                    // only set car's destination if specified train can service
                    // car
                    if (_train != null && _train != firstTrain) {
                        addLine(_buildReport, SEVEN, Bundle.getMessage("TrainDoesNotServiceCar",
                                _train.getName(), car.toString(), testCar.getDestinationName(),
                                testCar.getDestinationTrackName()));
                        _status = MessageFormat.format(STATUS_NOT_THIS_TRAIN, new Object[]{firstTrain.getName()});
                        continue;// found a route but it doesn't start with the
                                 // specified train
                    }
                    // is this the staging track assigned to the specified
                    // train?
                    if (track.isStaging() &&
                            firstTrain.getTerminationTrack() != null &&
                            firstTrain.getTerminationTrack() != track) {
                        addLine(_buildReport, SEVEN, Bundle.getMessage("RouterTrainIntoStaging", firstTrain.getName(),
                                firstTrain.getTerminationTrack().getLocation().getName(),
                                firstTrain.getTerminationTrack().getName()));
                        continue;
                    }
                    _status = car.setDestination(track.getLocation(), track);
                    if (_addtoReport) {
                        addLine(_buildReport, SEVEN, Bundle.getMessage("RouterTrainCanService",
                                firstTrain.getName(), car.toString(), car.getLocationName(), car.getTrackName(),
                                Track.getTrackTypeName(trackType), track.getLocation().getName(), track.getName()));
                    }
                    return true; // the specified train and another train can
                                 // carry the car to its destination
                }
            }
        }
        if (foundRoute) {
            if (_train != null) {
                _status = MessageFormat.format(STATUS_NOT_THIS_TRAIN, new Object[]{_train.getName()});
            } else {
                _status = STATUS_NOT_ABLE;
            }
        }
        return foundRoute;
    }

    /**
     * This routine builds a set of tracks that could be used for routing. It
     * also lists all of the tracks that can't be used.
     * 
     * @param car       The car being routed
     * @param testCar   the test car
     * @param trackType the type of track used for routing
     * @return list of usable tracks
     */
    private List<Track> getTracks(Car car, Car testCar, String trackType) {
        List<Track> inTracks = InstanceManager.getDefault(LocationManager.class).getTracksByMoves(trackType);
        List<Track> tracks = new ArrayList<Track>();
        for (Track track : inTracks) {
            if (car.getTrack() == track || car.getFinalDestinationTrack() == track) {
                continue; // don't use car's current track
            }
            // can't use staging if car's load can be modified
            if (trackType.equals(Track.STAGING) && track.isModifyLoadsEnabled()) {
                if (_addtoReportVeryDetailed) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("RouterStagingExcluded",
                            track.getLocation().getName(), track.getName()));
                }
                continue;
            }
            String status = track.isRollingStockAccepted(testCar);
            if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
                if (_addtoReportVeryDetailed) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("RouterCanNotDeliverCar",
                            car.toString(), track.getLocation().getName(), track.getName(),
                            status, track.getTrackTypeName()));
                }
                continue;
            }
            tracks.add(track);
        }
        return tracks;
    }

    /*
     * Note that "last" set of location/tracks (_lastLocationTracks) was loaded
     * by setCarDestinationTwoTrains. The following code builds two additional
     * sets of location/tracks called "next" (_nextLocationTracks) and "other"
     * (_otherLocationTracks). "next" is the next set of location/tracks that
     * the car can reach by a single train. "last" is the last set of
     * location/tracks that services the cars final destination. And "other" is
     * the remaining sets of location/tracks that are not "next" or "last". The
     * code then tries to connect the "next" and "last" location/track sets with
     * a train that can service the car. If successful, that would be a three
     * train route for the car. If not successful, the code than tries
     * combinations of "next", "other" and "last" location/tracks to create a
     * route for the car.
     */
    private boolean setCarDestinationMultipleTrains(Car car, boolean useStaging) {
        if (useStaging && !Setup.isCarRoutingViaStagingEnabled())
            return false; // routing via staging is disabled

        if (_addtoReportVeryDetailed) {
            addLine(_buildReport, SEVEN, BLANK_LINE);
        }
        if (_lastLocationTracks.isEmpty()) {
            if (useStaging) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("RouterCouldNotFindStaging",
                        car.getFinalDestinationName()));
            } else {
                addLine(_buildReport, SEVEN, Bundle.getMessage("RouterCouldNotFindLast",
                        car.getFinalDestinationName()));
            }
            return false;
        }

        Car testCar = clone(car); // reload
        // build the "next" and "other" location/tracks
        List<Track> tracks;
        if (!useStaging) {
            // start with interchanges
            tracks = InstanceManager.getDefault(LocationManager.class).getTracksByMoves(Track.INTERCHANGE);
            loadTracksAndTrains(car, testCar, tracks);
            // next load yards if enabled
            if (Setup.isCarRoutingViaYardsEnabled()) {
                tracks = InstanceManager.getDefault(LocationManager.class).getTracksByMoves(Track.YARD);
                loadTracksAndTrains(car, testCar, tracks);
            }
        } else {
            // add staging if requested
            List<Track> stagingTracks =
                    InstanceManager.getDefault(LocationManager.class).getTracksByMoves(Track.STAGING);
            tracks = new ArrayList<Track>();
            for (Track staging : stagingTracks) {
                if (!staging.isModifyLoadsEnabled()) {
                    tracks.add(staging);
                }
            }
            loadTracksAndTrains(car, testCar, tracks);
        }

        if (_nextLocationTracks.isEmpty()) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterCouldNotFindLoc",
                    car.getLocationName()));
            return false;
        }

        addLine(_buildReport, SEVEN, Bundle.getMessage("RouterTwoTrainsFailed", car));

        if (_addtoReport) {
            // tracks that could be the very next destination for the car
            for (Track t : _nextLocationTracks) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("RouterNextTrack", t.getTrackTypeName(), t.getLocation().getName(),
                                t.getName(), car, car.getLocationName(), car.getTrackName(),
                                _nextLocationTrains.get(_nextLocationTracks.indexOf(t))));
            }
            // tracks that could be the next to last destination for the car
            for (Track t : _lastLocationTracks) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("RouterLastTrack",
                        t.getTrackTypeName(), t.getLocation().getName(), t.getName(), car,
                        car.getFinalDestinationName(), car.getFinalDestinationTrackName(),
                        _lastLocationTrains.get(_lastLocationTracks.indexOf(t))));
            }
        }
        if (_addtoReportVeryDetailed) {
            // tracks that are not the next or the last list
            for (Track t : _otherLocationTracks) {
                addLine(_buildReport, SEVEN,
                        Bundle.getMessage("RouterOtherTrack", t.getTrackTypeName(), t.getLocation().getName(),
                                t.getName(), car));
            }
            addLine(_buildReport, SEVEN, BLANK_LINE);
        }
        boolean foundRoute = routeUsing3Trains(car);
        if (!foundRoute) {
            log.debug("Using 3 trains to route car to ({}) was unsuccessful", car.getFinalDestinationName());
            foundRoute = routeUsing4Trains(car);
        }
        if (!foundRoute) {
            log.debug("Using 4 trains to route car to ({}) was unsuccessful", car.getFinalDestinationName());
            foundRoute = routeUsing5Trains(car);
        }
        if (!foundRoute) {
            log.debug("Using 5 trains to route car to ({}) was unsuccessful", car.getFinalDestinationName());
            foundRoute = routeUsing6Trains(car);
        }
        if (!foundRoute) {
            log.debug("Using 6 trains to route car to ({}) was unsuccessful", car.getFinalDestinationName());
            foundRoute = routeUsing7Trains(car);
        }
        if (!foundRoute) {
            addLine(_buildReport, SEVEN,
                    Bundle.getMessage("RouterNotAbleToRoute", car.toString(), car.getLocationName(),
                            car.getTrackName(), car.getFinalDestinationName(), car.getFinalDestinationTrackName()));
        }
        return foundRoute;
    }

    private boolean routeUsing3Trains(Car car) {
        addLine(_buildReport, SEVEN, Bundle.getMessage("RouterNTrains", "3", car.getFinalDestinationName(),
                car.getFinalDestinationTrackName()));
        Car testCar = clone(car); // reload
        boolean foundRoute = false;
        for (Track nlt : _nextLocationTracks) {
            for (Track llt : _lastLocationTracks) {
                // does a train service these two locations?
                Train middleTrain =
                        getTrainForCar(testCar, nlt, llt, _nextLocationTrains.get(_nextLocationTracks.indexOf(nlt)),
                                _lastLocationTrains.get(_lastLocationTracks.indexOf(llt)));
                if (middleTrain != null) {
                    log.debug("Found 3 train route, setting car destination ({}, {})", nlt.getLocation().getName(),
                            nlt.getName());
                    foundRoute = true;
                    // show the car's route by building an ordered list of
                    // trains and tracks
                    List<Train> trains = new ArrayList<>(
                            Arrays.asList(_nextLocationTrains.get(_nextLocationTracks.indexOf(nlt)), middleTrain,
                                    _lastLocationTrains.get(_lastLocationTracks.indexOf(llt))));
                    List<Track> tracks = new ArrayList<>(Arrays.asList(nlt, llt, car.getFinalDestinationTrack()));
                    showRoute(car, trains, tracks);
                    if (finshSettingRouteFor(car, nlt)) {
                        return true; // done 3 train routing
                    }
                    break; // there was an issue with the first stop in the
                           // route
                }
            }
        }
        return foundRoute;
    }

    private boolean routeUsing4Trains(Car car) {
        addLine(_buildReport, SEVEN, Bundle.getMessage("RouterNTrains", "4", car.getFinalDestinationName(),
                car.getFinalDestinationTrackName()));
        Car testCar = clone(car); // reload
        boolean foundRoute = false;
        for (Track nlt : _nextLocationTracks) {
            otherloop: for (Track mlt : _otherLocationTracks) {
                Train middleTrain2 = getTrainForCar(testCar, nlt, mlt,
                        _nextLocationTrains.get(_nextLocationTracks.indexOf(nlt)), null);
                if (middleTrain2 == null) {
                    continue;
                }
                // build a list of tracks that are reachable from the 1st
                // interchange
                if (!_next2ndLocationTracks.contains(mlt)) {
                    _next2ndLocationTracks.add(mlt);
                    if (_addtoReport) {
                        addLine(_buildReport, SEVEN,
                                Bundle.getMessage("RouterNextHop", mlt.getTrackTypeName(), mlt.getLocation().getName(),
                                        mlt.getName(), car, nlt.getLocation().getName(), nlt.getName(),
                                        middleTrain2.getName()));
                    }
                }
                for (Track llt : _lastLocationTracks) {
                    Train middleTrain3 = getTrainForCar(testCar, mlt, llt, middleTrain2,
                            _lastLocationTrains.get(_lastLocationTracks.indexOf(llt)));
                    if (middleTrain3 == null) {
                        continue;
                    }
                    log.debug("Found 4 train route, setting car destination ({}, {})", nlt.getLocation().getName(),
                            nlt.getName());
                    foundRoute = true;
                    // show the car's route by building an ordered list of
                    // trains and tracks
                    List<Train> trains = new ArrayList<>(
                            Arrays.asList(_nextLocationTrains.get(_nextLocationTracks.indexOf(nlt)), middleTrain2,
                                    middleTrain3, _lastLocationTrains.get(_lastLocationTracks.indexOf(llt))));
                    List<Track> tracks = new ArrayList<>(Arrays.asList(nlt, mlt, llt, car.getFinalDestinationTrack()));
                    showRoute(car, trains, tracks);
                    if (finshSettingRouteFor(car, nlt)) {
                        return true; // done 4 train routing
                    }
                    break otherloop; // there was an issue with the first
                                     // stop in the route
                }
            }
        }
        return foundRoute;
    }

    private boolean routeUsing5Trains(Car car) {
        addLine(_buildReport, SEVEN, Bundle.getMessage("RouterNTrains", "5", car.getFinalDestinationName(),
                car.getFinalDestinationTrackName()));
        Car testCar = clone(car); // reload
        boolean foundRoute = false;
        for (Track nlt : _nextLocationTracks) {
            otherloop: for (Track mlt1 : _next2ndLocationTracks) {
                Train middleTrain2 = getTrainForCar(testCar, nlt, mlt1,
                        _nextLocationTrains.get(_nextLocationTracks.indexOf(nlt)), null);
                if (middleTrain2 == null) {
                    continue;
                }
                for (Track mlt2 : _otherLocationTracks) {
                    if (_next2ndLocationTracks.contains(mlt2)) {
                        continue;
                    }
                    Train middleTrain3 = getTrainForCar(testCar, mlt1, mlt2, middleTrain2, null);
                    if (middleTrain3 == null) {
                        continue;
                    }
                    // build a list of tracks that are reachable from the 2nd
                    // interchange
                    if (!_next3rdLocationTracks.contains(mlt2)) {
                        _next3rdLocationTracks.add(mlt2);
                        if (_addtoReport) {
                            addLine(_buildReport, SEVEN,
                                    Bundle.getMessage("RouterNextHop", mlt2.getTrackTypeName(),
                                            mlt2.getLocation().getName(),
                                            mlt2.getName(), car, mlt1.getLocation().getName(), mlt1.getName(),
                                            middleTrain3.getName()));
                        }
                    }
                    for (Track llt : _lastLocationTracks) {
                        Train middleTrain4 = getTrainForCar(testCar, mlt2, llt, middleTrain3,
                                _lastLocationTrains.get(_lastLocationTracks.indexOf(llt)));
                        if (middleTrain4 == null) {
                            continue;
                        }
                        log.debug("Found 5 train route, setting car destination ({}, {})",
                                nlt.getLocation().getName(),
                                nlt.getName());
                        foundRoute = true;
                        // show the car's route by building an ordered list
                        // of trains and tracks
                        List<Train> trains = new ArrayList<>(Arrays.asList(
                                _nextLocationTrains.get(_nextLocationTracks.indexOf(nlt)), middleTrain2, middleTrain3,
                                middleTrain4, _lastLocationTrains.get(_lastLocationTracks.indexOf(llt))));
                        List<Track> tracks =
                                new ArrayList<>(Arrays.asList(nlt, mlt1, mlt2, llt, car.getFinalDestinationTrack()));
                        showRoute(car, trains, tracks);
                        if (finshSettingRouteFor(car, nlt)) {
                            return true; // done 5 train routing
                        }
                        break otherloop; // there was an issue with the
                                         // first stop in the route
                    }
                }
            }
        }
        return foundRoute;
    }

    private boolean routeUsing6Trains(Car car) {
        addLine(_buildReport, SEVEN, Bundle.getMessage("RouterNTrains", "6", car.getFinalDestinationName(),
                car.getFinalDestinationTrackName()));
        Car testCar = clone(car); // reload
        boolean foundRoute = false;
        for (Track nlt : _nextLocationTracks) {
            otherloop: for (Track mlt1 : _next2ndLocationTracks) {
                Train middleTrain2 = getTrainForCar(testCar, nlt, mlt1,
                        _nextLocationTrains.get(_nextLocationTracks.indexOf(nlt)), null);
                if (middleTrain2 == null) {
                    continue;
                }
                for (Track mlt2 : _next3rdLocationTracks) {
                    Train middleTrain3 = getTrainForCar(testCar, mlt1, mlt2, middleTrain2, null);
                    if (middleTrain3 == null) {
                        continue;
                    }
                    for (Track mlt3 : _otherLocationTracks) {
                        if (_next2ndLocationTracks.contains(mlt3) || _next3rdLocationTracks.contains(mlt3)) {
                            continue;
                        }
                        Train middleTrain4 = getTrainForCar(testCar, mlt2, mlt3, middleTrain3, null);
                        if (middleTrain4 == null) {
                            continue;
                        }
                        if (!_next4thLocationTracks.contains(mlt3)) {
                            _next4thLocationTracks.add(mlt3);
                            if (_addtoReport) {
                                addLine(_buildReport, SEVEN, Bundle.getMessage("RouterNextHop", mlt3.getTrackTypeName(),
                                        mlt3.getLocation().getName(), mlt3.getName(), car, mlt2.getLocation().getName(),
                                        mlt2.getName(), middleTrain4.getName()));
                            }
                        }
                        for (Track llt : _lastLocationTracks) {
                            Train middleTrain5 = getTrainForCar(testCar, mlt3, llt, middleTrain4,
                                    _lastLocationTrains.get(_lastLocationTracks.indexOf(llt)));
                            if (middleTrain5 == null) {
                                continue;
                            }
                            log.debug("Found 6 train route, setting car destination ({}, {})",
                                    nlt.getLocation().getName(), nlt.getName());
                            foundRoute = true;
                            // show the car's route by building an ordered
                            // list of trains and tracks
                            List<Train> trains = new ArrayList<>(
                                    Arrays.asList(_nextLocationTrains.get(_nextLocationTracks.indexOf(nlt)),
                                            middleTrain2, middleTrain3, middleTrain4, middleTrain5,
                                            _lastLocationTrains.get(_lastLocationTracks.indexOf(llt))));
                            List<Track> tracks = new ArrayList<>(
                                    Arrays.asList(nlt, mlt1, mlt2, mlt3, llt, car.getFinalDestinationTrack()));
                            showRoute(car, trains, tracks);
                            // only set car's destination if specified train
                            // can service car
                            if (finshSettingRouteFor(car, nlt)) {
                                return true; // done 6 train routing
                            }
                            break otherloop; // there was an issue with the
                                             // first stop in the route
                        }
                    }
                }
            }
        }
        return foundRoute;
    }

    private boolean routeUsing7Trains(Car car) {
        addLine(_buildReport, SEVEN, Bundle.getMessage("RouterNTrains", "7", car.getFinalDestinationName(),
                car.getFinalDestinationTrackName()));
        Car testCar = clone(car); // reload
        boolean foundRoute = false;
        for (Track nlt : _nextLocationTracks) {
            otherloop: for (Track mlt1 : _next2ndLocationTracks) {
                Train middleTrain2 = getTrainForCar(testCar, nlt, mlt1,
                        _nextLocationTrains.get(_nextLocationTracks.indexOf(nlt)), null);
                if (middleTrain2 == null) {
                    continue;
                }
                for (Track mlt2 : _next3rdLocationTracks) {
                    Train middleTrain3 = getTrainForCar(testCar, mlt1, mlt2, middleTrain2, null);
                    if (middleTrain3 == null) {
                        continue;
                    }
                    for (Track mlt3 : _next4thLocationTracks) {
                        Train middleTrain4 = getTrainForCar(testCar, mlt2, mlt3, middleTrain3, null);
                        if (middleTrain4 == null) {
                            continue;
                        }
                        for (Track mlt4 : _otherLocationTracks) {
                            if (_next2ndLocationTracks.contains(mlt4) ||
                                    _next3rdLocationTracks.contains(mlt4) ||
                                    _next4thLocationTracks.contains(mlt4)) {
                                continue;
                            }
                            Train middleTrain5 = getTrainForCar(testCar, mlt3, mlt4, middleTrain4, null);
                            if (middleTrain5 == null) {
                                continue;
                            }
                            for (Track llt : _lastLocationTracks) {
                                Train middleTrain6 = getTrainForCar(testCar, mlt4, llt, middleTrain5,
                                        _lastLocationTrains.get(_lastLocationTracks.indexOf(llt)));
                                if (middleTrain6 == null) {
                                    continue;
                                }
                                log.debug("Found 7 train route, setting car destination ({}, {})",
                                        nlt.getLocation().getName(), nlt.getName());
                                foundRoute = true;
                                // show the car's route by building an ordered
                                // list of trains and tracks
                                List<Train> trains = new ArrayList<>(
                                        Arrays.asList(_nextLocationTrains.get(_nextLocationTracks.indexOf(nlt)),
                                                middleTrain2, middleTrain3, middleTrain4, middleTrain5, middleTrain6,
                                                _lastLocationTrains.get(_lastLocationTracks.indexOf(llt))));
                                List<Track> tracks = new ArrayList<>(Arrays.asList(nlt, mlt1, mlt2, mlt3, mlt4, llt,
                                        car.getFinalDestinationTrack()));
                                showRoute(car, trains, tracks);
                                // only set car's destination if specified train
                                // can service car
                                if (finshSettingRouteFor(car, nlt)) {
                                    return true; // done 7 train routing
                                }
                                break otherloop; // there was an issue with the
                                                 // first stop in the route
                            }
                        }
                    }
                }
            }
        }
        return foundRoute;
    }

    /**
     * This method returns a train that is able to move the test car between the
     * fromTrack and the toTrack. The default for an interchange track is to not
     * allow the same train to spot and pull a car.
     * 
     * @param testCar   test car
     * @param fromTrack departure track
     * @param toTrack   arrival track
     * @param fromTrain train servicing fromTrack (previous drop to fromTrack)
     * @param toTrain   train servicing toTrack (pulls from the toTrack)
     * @return null if no train found, else a train able to move test car
     *         between fromTrack and toTrack.
     */
    private Train getTrainForCar(Car testCar, Track fromTrack, Track toTrack, Train fromTrain, Train toTrain) {
        testCar.setTrack(fromTrack); // car to this location and track
        testCar.setDestinationTrack(toTrack); // car to this destination & track
        List<Train> excludeTrains = new ArrayList<>();
        if (fromTrack.isInterchange() && fromTrack.getPickupOption().equals(Track.ANY)) {
            excludeTrains.add(fromTrain);
        }
        if (toTrack.isInterchange() && toTrack.getPickupOption().equals(Track.ANY)) {
            excludeTrains.add(toTrain);
        }
        // does a train service these two locations? 
        String key = fromTrack.getId() + toTrack.getId();
        Train train = _listTrains.get(key);
        if (train == null) {
            train = tmanager.getTrainForCar(testCar, excludeTrains, null);
            if (train != null) {
                _listTrains.put(key, train);
            } else {
                _listTrains.put(key, new Train("null", "null"));
            }
        } else if (train.getId().equals("null")) {
            return null;
        }
        return train;

    }

    private void showRoute(Car car, List<Train> trains, List<Track> tracks) {
        StringBuffer buf = new StringBuffer(
                Bundle.getMessage("RouterRouteForCar", car.toString(), car.getLocationName(), car.getTrackName()));
        StringBuffer bufRp = new StringBuffer(
                Bundle.getMessage("RouterRoutePath", car.getLocationName(), car.getTrackName()));
        for (Track track : tracks) {
            if (_addtoReport) {
                buf.append(Bundle.getMessage("RouterRouteTrain", trains.get(tracks.indexOf(track)).getName()));
            }
            bufRp.append(Bundle.getMessage("RouterRoutePathTrain", trains.get(tracks.indexOf(track)).getName()));
            if (track != null) {
                buf.append(Bundle.getMessage("RouterRouteTrack", track.getLocation().getName(), track.getName()));
                bufRp.append(
                        Bundle.getMessage("RouterRoutePathTrack", track.getLocation().getName(), track.getName()));
            } else {
                buf.append(Bundle.getMessage("RouterRouteTrack", car.getFinalDestinationName(),
                        car.getFinalDestinationTrackName()));
                bufRp.append(Bundle.getMessage("RouterRoutePathTrack", car.getFinalDestinationName(),
                        car.getFinalDestinationTrackName()));
            }
        }
        car.setRoutePath(bufRp.toString());
        addLine(_buildReport, SEVEN, buf.toString());
    }

    /**
     * @param car   The car to which the destination (track) is going to be
     *              applied. Will set car's destination if specified train can
     *              service car
     * @param track The destination track for car
     * @return false if there's an issue with the destination track length or
     *         wrong track into staging, otherwise true.
     */
    private boolean finshSettingRouteFor(Car car, Track track) {
        // only set car's destination if specified train can service car
        Car ts2 = clone(car);
        ts2.setDestinationTrack(track);
        String specified = canSpecifiedTrainService(ts2);
        if (specified.equals(NO)) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("TrainDoesNotServiceCar",
                    _train.getName(), car.toString(), track.getLocation().getName(), track.getName()));
            _status = MessageFormat.format(STATUS_NOT_THIS_TRAIN, new Object[]{_train.getName()});
            return false;
        } else if (specified.equals(NOT_NOW)) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterTrainCanNotDueTo", _train.getName(), car.toString(),
                    track.getLocation().getName(), track.getName(), _train.getServiceStatus()));
            return false; // the issue is route moves or train length
        }
        // check to see if track is staging
        if (track.isStaging() &&
                _train != null &&
                _train.getTerminationTrack() != null &&
                _train.getTerminationTrack() != track) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterTrainIntoStaging",
                    _train.getName(), _train.getTerminationTrack().getLocation().getName(),
                    _train.getTerminationTrack().getName()));
            return false; // wrong track into staging
        }
        _status = car.setDestination(track.getLocation(), track);
        if (!_status.equals(Track.OKAY)) {
            addLine(_buildReport, SEVEN, Bundle.getMessage("RouterCanNotDeliverCar", car.toString(),
                    track.getLocation().getName(), track.getName(), _status, track.getTrackTypeName()));
            if (_status.startsWith(Track.LENGTH) && !redirectToAlternate(car, track)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Used when the 1st hop interchanges and yards are full. Will attempt to
     * use a spur's alternate track when pulling a car from the spur. This will
     * create a local move. Code checks to see if local move by the train being
     * used is allowed. Will only use the alternate track if all possible 1st
     * hop tracks were tested.
     * 
     * @param car the car being redirected
     * @return true if car's destination was set to alternate track
     */
    private boolean redirectToAlternate(Car car, Track track) {
        if (car.getTrack().isSpur() &&
                car.getTrack().getAlternateTrack() != null &&
                _nextLocationTracks.indexOf(track) == _nextLocationTracks.size() - 1) {
            // try redirecting car to the alternate track
            Car ts = clone(car);
            ts.setDestinationTrack(car.getTrack().getAlternateTrack());
            String specified = canSpecifiedTrainService(ts);
            if (specified.equals(YES)) {
                _status = car.setDestination(car.getTrack().getAlternateTrack().getLocation(),
                        car.getTrack().getAlternateTrack());
                if (_status.equals(Track.OKAY)) {
                    addLine(_buildReport, SEVEN, Bundle.getMessage("RouterSendCarToAlternative",
                            car.toString(), car.getTrack().getAlternateTrack().getName(),
                            car.getTrack().getAlternateTrack().getLocation().getName()));
                    return true;
                }
            }
        }
        return false;
    }

    // sets clone car destination to final destination and track
    private Car clone(Car car) {
        Car clone = car.copy();
        // modify clone car length if car is part of kernel
        if (car.getKernel() != null) {
            clone.setLength(Integer.toString(car.getKernel().getTotalLength() - RollingStock.COUPLERS));
        }
        clone.setTrack(car.getTrack());
        clone.setFinalDestination(car.getFinalDestination());
        // don't set the clone's final destination track, that will record the
        // car as being inbound
        // next two items is where the clone is different
        clone.setDestination(car.getFinalDestination());
        // note that final destination track can be null
        clone.setDestinationTrack(car.getFinalDestinationTrack());
        return clone;
    }

    /*
     * Creates two sets of tracks when routing. 1st set (_nextLocationTracks) is
     * one hop away from car's current location. 2nd set is all other tracks
     * (_otherLocationTracks) that aren't one hop away from car's current
     * location or destination. Also creates the list of trains used to service
     * _nextLocationTracks.
     */
    private void loadTracksAndTrains(Car car, Car testCar, List<Track> tracks) {
        for (Track track : tracks) {
            if (track == car.getTrack()) {
                continue; // don't use car's current track
            }
            // note that last could equal next if this routine was used for two
            // train routing
            if (_lastLocationTracks.contains(track)) {
                continue;
            }
            String status = track.isRollingStockAccepted(testCar);
            if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
                continue; // track doesn't accept this car
            }
            // test to see if there's a train that can deliver the car to this
            // destination
            testCar.setDestinationTrack(track);
            Train train = null;
            String specified = canSpecifiedTrainService(testCar);
            if (specified.equals(YES) || specified.equals(NOT_NOW)) {
                train = _train;
            } else {
                train = tmanager.getTrainForCar(testCar, null);
            }
            // Can specified train carry this car out of staging?
            if (car.getTrack().isStaging() && !specified.equals(YES)) {
                train = null;
            }
            // is the option carry all cars with a final destination enabled?
            if (train != null &&
                    _train != null &&
                    _train != train &&
                    _train.isServiceAllCarsWithFinalDestinationsEnabled() &&
                    !specified.equals(YES)) {
                addLine(_buildReport, SEVEN, Bundle.getMessage("RouterOptionToCarry", _train.getName(),
                        train.getName(), car.toString(), track.getLocation().getName(), track.getName()));
                train = null;
            }
            if (train != null) {
                _nextLocationTracks.add(track);
                _nextLocationTrains.add(train);
            } else {
                _otherLocationTracks.add(track);
            }
        }
    }

    private static final String NO = "no"; // NOI18N
    private static final String YES = "yes"; // NOI18N
    private static final String NOT_NOW = "not now"; // NOI18N
    private static final String NO_SPECIFIED_TRAIN = "no specified train"; // NOI18N

    private String canSpecifiedTrainService(Car car) {
        if (_train == null) {
            return NO_SPECIFIED_TRAIN;
        }
        if (_train.isServiceable(car)) {
            return YES;
        } // is the reason this train can't service route moves or train length?
        else if (!_train.getServiceStatus().equals(Train.NONE)) {
            return NOT_NOW; // the issue is route moves or train length
        }
        return NO;
    }

    private final static Logger log = LoggerFactory.getLogger(Router.class);

}
