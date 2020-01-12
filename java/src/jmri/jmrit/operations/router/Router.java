package jmri.jmrit.operations.router;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Router for car movement. This code attempts to find a way (a route) to move a
 * car to its final destination through the use of two or more trains. First the
 * code tries to move car using a single train. If that fails, attempts are made
 * using two trains via a classification/interchange (C/I) tracks, then yard
 * tracks if enabled. Next attempts are made using three or more trains using
 * any combination of C/I and yard tracks. If that fails and routing via staging
 * is enabled, the code tries two trains using staging tracks, then multiple
 * trains using a combination of C/I, yards, and staging tracks. Currently the
 * router is limited to five trains.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011, 2012, 2013, 2015
 */
public class Router extends TrainCommon implements InstanceManagerAutoDefault {

    private final List<Track> _nextLocationTracks = new ArrayList<>();
    private final List<Track> _lastLocationTracks = new ArrayList<>();
    private final List<Track> _otherLocationTracks = new ArrayList<>();

    private final List<Train> _nextLocationTrains = new ArrayList<>();
    private final List<Train> _lastLocationTrains = new ArrayList<>();

    protected static final String STATUS_NOT_THIS_TRAIN = Bundle.getMessage("RouterTrain");
    protected static final String STATUS_NOT_ABLE = Bundle.getMessage("RouterNotAble");
    protected static final String STATUS_ROUTER_DISABLED = Bundle.getMessage("RouterDisabled");

    private String _status = "";
    private Train _train = null;
    PrintWriter _buildReport = null; // build report

    private static final boolean debugFlag = false; // developer debug flag

    private static final String SEVEN = Setup.BUILD_REPORT_VERY_DETAILED;
    private boolean _addtoReport = false;
    private boolean _addtoReportVeryDetailed = false;

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     *             {@link jmri.InstanceManager#getDefault(java.lang.Class)}
     *             instead
     */
    @Deprecated
    public static synchronized Router instance() {
        return InstanceManager.getDefault(Router.class);
    }

    /**
     * Returns the status of the router when using the setDestination() for a
     * car.
     *
     * @return Track.OKAY, STATUS_NOT_THIS_TRAIN, STATUS_NOT_ABLE,
     *         STATUS_CAR_AT_DESINATION, or STATUS_ROUTER_DISABLED
     */
    public String getStatus() {
        return _status;
    }

    /**
     * Determines if car can be routed to the destination track
     * @param car the car being tested 
     * @param train the train servicing the car, can be null
     * @param track the destination track
     * @param buildReport the report, can be null
     * @return true if the car can be routed to the track
     */
    public boolean isCarRouteable(Car car, Train train, Track track, PrintWriter buildReport) {

        addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterIsCarRoutable"),
                new Object[]{car.toString(), car.getLocationName(), car.getTrackName(),
                        car.getLoadName(), track.getLocation().getName(), track.getName()}));
        
        Car c = car.copy();
        c.setTrack(car.getTrack());
        c.setFinalDestination(track.getLocation());
        c.setFinalDestinationTrack(track);
        boolean results = setDestination(c, train, buildReport);
        c.setDestination(null, null); // clear router car destinations
        return results;
    }

    /**
     * Attempts to set the car's destination if a final destination exists. Only
     * sets the car's destination if the train is part of the car's route.
     *
     * @param car the car to route
     * @param train the first train to carry this car, can be null
     * @param buildReport PrintWriter for build report, and can be null
     * @return true if car can be routed.
     */
    public boolean setDestination(Car car, Train train, PrintWriter buildReport) {
        if (car.getLocation() == null || car.getTrack() == null || car.getFinalDestination() == null) {
            return false;
        }
        _status = Track.OKAY;
        _train = train;
        _buildReport = buildReport;
        _addtoReport = Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED) ||
                Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED);
        _addtoReportVeryDetailed = Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED);
        log.debug("Car ({}) at location ({}, {}) final destination ({}, {}) car routing begins", car, car
                .getLocationName(), car.getTrackName(), car.getFinalDestinationName(),
                car
                        .getFinalDestinationTrackName());
        if (_train != null) {
            log.debug("Routing using train ({})", train.getName());
        }
        // is car part of kernel?
        if (car.getKernel() != null && !car.isLead()) {
            return false;
        }
        // note clone car has the car's "final destination" as its destination
        Car clone = clone(car);
        // Note the following test doesn't check for car length which is what we want. Also ignores if track has a
        // schedule.
        _status = clone.testDestination(clone.getDestination(), clone.getDestinationTrack());
        if (!_status.equals(Track.OKAY)) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCanNotDeliverCar"),
                    new Object[]{
                            car.toString(),
                            car.getFinalDestinationName(),
                            car.getFinalDestinationTrackName(),
                            _status,
                            (car.getFinalDestinationTrack() == null ? Bundle.getMessage("RouterDestination") : car
                                    .getFinalDestinationTrack().getTrackTypeName())}));
            return false;
        }
        if (clone.getDestination() != null && clone.getDestinationTrack() == null) {
            // determine if there's a track that can service the car
            String status = "";
            for (Track track : clone.getDestination().getTrackList()) {
                status = track.accepts(clone);
                if (status.equals(Track.OKAY) || status.startsWith(Track.LENGTH)) {
                    log.debug("Track ({}) will accept car ({})", track.getName(), car);
                    break;
                }
            }
            if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
                addLine(_buildReport, SEVEN,
                        _status = MessageFormat.format(Bundle.getMessage("RouterNoTracks"), new Object[]{
                                clone.getDestinationName(), car.toString()}));
                return false;
            }
        }
        // check to see if car will move to destination using a single train
        if (checkForSingleTrain(car, clone)) {
            return true; // a single train can service this car
        } else if (Setup.isCarRoutingEnabled()) {
            log.debug("Car ({}) final destination ({}) is not served by a single train", car, car
                    .getFinalDestinationName());
            // was the request for a local move?
            if (car.getLocationName().equals(car.getFinalDestinationName())) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCouldNotFindTrain"),
                        new Object[]{car.getLocationName(), car.getTrackName(), car.getFinalDestinationName(),
                                car.getFinalDestinationTrackName()}));
                // _status = STATUS_NO_TRAINS;
                // return false; // maybe next time
            }
            if (_addtoReport) {
                addLine(_buildReport, SEVEN, BLANK_LINE);
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterBeginTwoTrain"),
                        new Object[]{car.toString(), car.getLocationName(), car.getFinalDestinationName()}));
            }

            _nextLocationTracks.clear();
            _lastLocationTracks.clear();
            _otherLocationTracks.clear();
            _nextLocationTrains.clear();
            _lastLocationTrains.clear();

            // first try using 2 trains and an interchange track to route the car
            if (setCarDestinationTwoTrainsInterchange(car)) {
                if (car.getDestination() == null) {
                    log.debug(
                            "Was able to find a route via classification/interchange track, but not using train ({})" +
                                    " or car destination not set, try again using yard tracks",
                            _train.getName()); // NOI18N
                    if (setCarDestinationTwoTrainsYard(car)) {
                        log.debug("Was able to find route via yard ({}, {}) for car ({})", car.getDestinationName(),
                                car.getDestinationTrackName(), car);
                    }
                } else {
                    log.debug("Was able to find route via interchange ({}, {}) for car ({})", car.getDestinationName(),
                            car.getDestinationTrackName(), car);
                }
                // now try 2 trains and a yard track
            } else if (setCarDestinationTwoTrainsYard(car)) {
                log.debug("Was able to find route via yard ({}, {}) for car ({}) using two trains", car
                        .getDestinationName(), car.getDestinationTrackName(), car);
                // now try 3 or more trains to route car, but not through staging
            } else if (setCarDestinationMultipleTrains(car, false)) {
                log.debug("Was able to find multiple train route for car ({})", car);
                // now try 2 trains and a staging track
            } else if (setCarDestinationTwoTrainsStaging(car)) {
                log.debug("Was able to find route via staging ({}, {}) for car ({}) using two trains", car
                        .getDestinationName(), car.getDestinationTrackName(), car);
                // now try 3 or more trains to route car, include staging if enabled
            } else if (setCarDestinationMultipleTrains(car, true)) {
                log.debug("Was able to find multiple train route for car ({}) through staging", car);
            } else {
                log.debug("Wasn't able to set route for car ({})", car);
                _status = STATUS_NOT_ABLE;
                return false; // maybe next time
            }
        } else {
            log.debug("Car ({}) final destination ({}) is not served directly by any train", car, car
                    .getFinalDestinationName()); // NOI18N
            _status = STATUS_ROUTER_DISABLED;
            car.setFinalDestination(null);
            car.setFinalDestinationTrack(null);
            return false;
        }
        return true; // car's destination has been set
    }

    /**
     * Checks to see if a single train can transport car to its final
     * destination.
     *
     * @return true if single train can transport car to its final destination.
     */
    private boolean checkForSingleTrain(Car car, Car clone) {
        boolean trainServicesCar = false; // specific train
        Train testTrain = null;
        if (_train != null) {
            trainServicesCar = _train.services(_buildReport, clone);
        }
        if (trainServicesCar) {
            testTrain = _train; // use the specific train
        }
        // can specific train can service car out of staging. Note that the router code will try to route the car using
        // two or more trains just to get the car out of staging.
        if (car.getTrack().isStaging() && _train != null && !trainServicesCar) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterTrainCanNotStaging"),
                    new Object[]{_train.getName(), car.toString(), car.getLocationName(), clone.getDestinationName(),
                            clone.getDestinationTrackName()}));
            if (!_train.getServiceStatus().equals(Train.NONE)) {
                addLine(_buildReport, SEVEN, _train.getServiceStatus());
            }
        } else if (!trainServicesCar) {
            testTrain = InstanceManager.getDefault(TrainManager.class).getTrainForCar(clone, _train, _buildReport);
        }
        if (testTrain != null &&
                _train != null &&
                !trainServicesCar &&
                _train.isServiceAllCarsWithFinalDestinationsEnabled()) {
            // log.debug("Option to service all cars with a final destination is enabled");
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterOptionToCarry"), new Object[]{
                    _train.getName(), testTrain.getName(), car.toString(), clone.getDestinationName(),
                    clone.getDestinationTrackName()}));
            testTrain = null;
        }
        if (testTrain != null) {
            return routeUsingOneTrain(testTrain, car, clone);
        }
        return false;
    }

    /**
     * A single train can service the car. Provide various messages to build
     * report detailing which train can service the car. Also checks to see if
     * the needs to go the alternate track or yard track if the car's final
     * destination track is full. Returns false if car is stuck in staging.
     *
     * @return true for all cases except if car is departing staging and is
     *         stuck there.
     */
    private boolean routeUsingOneTrain(Train testTrain, Car car, Car clone) {
        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterTrainCanTransport"), new Object[]{
                testTrain.getName(), car.toString(), car.getTrack().getTrackTypeName(), car.getLocationName(),
                car.getTrackName(),
                clone.getDestinationName(), clone.getDestinationTrackName()}));
        if (_addtoReport) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterRoute1TrainsForCar"),
                    new Object[]{car.toString(), car.getLocationName(), car.getTrackName(), testTrain.getName(),
                            clone.getDestinationName(), clone.getDestinationTrackName()}));
        }
        // now check to see if specific train can service car directly
        if (_train != null && _train != testTrain) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("TrainDoesNotServiceCar"),
                    new Object[]{_train.getName(), car.toString(), clone.getDestinationName(),
                            clone.getDestinationTrackName()}));
            if (!_train.getServiceStatus().equals(Train.NONE)) {
                addLine(_buildReport, SEVEN, _train.getServiceStatus());
            }
            _status = STATUS_NOT_THIS_TRAIN;
            return true; // car can be routed, but not by this train!
        }
        _status = car.setDestination(clone.getDestination(), clone.getDestinationTrack());
        if (_status.equals(Track.OKAY)) {
            return true; // done, car has new destination
        }
        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCanNotDeliverCar"), new Object[]{
                car.toString(),
                clone.getDestinationName(),
                clone.getDestinationTrackName(),
                _status,
                (clone.getDestinationTrack() == null ? Bundle.getMessage("RouterDestination") : clone
                        .getDestinationTrack().getTrackTypeName())}));
        // check to see if an alternative track was specified
        if ((_status.startsWith(Track.LENGTH) || _status.startsWith(Track.SCHEDULE)) &&
                clone.getDestinationTrack() != null &&
                clone.getDestinationTrack().getAlternateTrack() != null &&
                clone.getDestinationTrack().getAlternateTrack() != car.getTrack()) {
            String status = car.setDestination(clone.getDestination(), clone.getDestinationTrack().getAlternateTrack());
            if (status.equals(Track.OKAY)) {
                if (_train == null || _train.services(car)) {
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterSendCarToAlternative"),
                            new Object[]{car.toString(), clone.getDestinationTrack().getAlternateTrack().getName(),
                                    clone.getDestination().getName()}));
                    return true; // car is going to alternate track
                }
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterNotSendCarToAlternative"),
                        new Object[]{_train.getName(), car.toString(),
                                clone.getDestinationTrack().getAlternateTrack().getName(),
                                clone.getDestination().getName()}));
            } else {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterAlternateFailed"),
                        new Object[]{clone.getDestinationTrack().getAlternateTrack().getName(), status}));
            }
        } else if (clone.getDestinationTrack() != null &&
                clone.getDestinationTrack().getAlternateTrack() != null &&
                clone.getDestinationTrack().getAlternateTrack() == car.getTrack()) {
            // state that car is spotted at the alternative track
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterAtAlternate"), new Object[]{
                    car.toString(), clone.getDestinationTrack().getAlternateTrack().getName(), clone.getLocationName(),
                    clone.getDestinationTrackName()}));
        } else if (car.getLocation() == clone.getDestination()) {
            // state that alternative and yard track options are not available if car is at final destination
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterIgnoreAlternate"), new Object[]{
                    car.toString(), car.getLocationName()}));
        }
        // check to see if spur was full, if so, forward to yard if possible
        if (Setup.isForwardToYardEnabled() &&
                _status.startsWith(Track.LENGTH) &&
                car.getLocation() != clone.getDestination()) {
            // log.debug("Spur full, searching for a yard at destination ("+clone.getDestinationName()+")");
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterSpurFull"), new Object[]{
                    clone.getDestinationTrackName(), clone.getDestinationName()}));
            Location dest = clone.getDestination();
            List<Track> yards = dest.getTrackByMovesList(Track.YARD);
            log.debug("Found {} yard(s) at destination ({})", yards.size(), clone.getDestinationName());
            for (Track track : yards) {
                String status = car.setDestination(dest, track);
                if (status.equals(Track.OKAY)) {
                    if (_train != null && !_train.services(car)) {
                        log.debug("Train ({}) can not deliver car ({}) to yard ({})", _train.getName(), car,
                                track.getName());
                        continue;
                    }
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterSendCarToYard"),
                            new Object[]{car.toString(), track.getName(), dest.getName()}));
                    return true; // car is going to a yard
                } else {
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCanNotUseYard"),
                            new Object[]{track.getName(), status}));
                }
            }
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterNoYardTracks"), new Object[]{
                    dest.getName(), car.toString()}));
        }
        car.setDestination(null, null);
        if (car.getTrack().isStaging()) {
            log.debug("Car ({}) departing staging, single train can't deliver car to ({}, {})", car, clone
                    .getDestinationName(), clone.getDestinationTrackName());
            return false; // try 2 or more trains
        }
        return true; // able to route, but not able to set the car's destination
    }

    /**
     * Sets a car's final destination to an interchange track if two trains can
     * route the car.
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
     * Sets a car's final destination to a yard track if two train can route the
     * car.
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
     * Sets a car's final destination to a staging track if two train can route
     * the car.
     *
     * @param car the car to be routed
     * @return true if car's destination has been modified to a staging track.
     *         False if a staging track wasn't found that could service the
     *         car's final destination.
     */
    private boolean setCarDestinationTwoTrainsStaging(Car car) {
        if (Setup.isCarRoutingViaStagingEnabled()) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterAttemptStaging"), new Object[]{
                    car.toString()}));
            return setCarDestinationTwoTrains(car, Track.STAGING);
        }
        return false;
    }

    private boolean setCarDestinationTwoTrains(Car car, String trackType) {
        Car testCar = clone(car); // reload
        log.debug("Two train routing, find {} track for car ({}) final destination ({}, {})", trackType, car,
                testCar.getDestinationName(), testCar.getDestinationTrackName());
        if (_addtoReport) {
            addLine(_buildReport, SEVEN, BLANK_LINE);
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterFindTrack"), new Object[]{
                    Track.getTrackTypeName(trackType), car.toString(), testCar.getDestinationName(),
                    testCar.getDestinationTrackName()}));
        }
        boolean foundRoute = false;
        // now search for a yard or interchange that a train can pick up and deliver the car to its destination
        List<Track> tracks = InstanceManager.getDefault(LocationManager.class).getTracksByMoves(trackType);
        for (Track track : tracks) {
            if (car.getTrack() == track) {
                continue; // don't use car's current track
            }
            String status = track.accepts(testCar);
            if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
                if (_addtoReportVeryDetailed) {
                    addLine(_buildReport, SEVEN, BLANK_LINE);
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCanNotDeliverCar"),
                            new Object[]{car.toString(), track.getLocation().getName(), track.getName(), status,
                                    track.getTrackTypeName()}));
                }
                continue;
            }
            if (debugFlag) {
                log.debug("Found {} track ({}, {}) for car ({})", trackType, track.getLocation().getName(), track
                        .getName(), car);
            }
            if (_addtoReport) {
                addLine(_buildReport, SEVEN, BLANK_LINE);
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterFoundTrack"), new Object[]{
                        Track.getTrackTypeName(trackType), track.getLocation().getName(), track.getName(),
                        car.toString()}));
            }
            // test to see if there's a train that can deliver the car to its final location
            testCar.setTrack(track);
            testCar.setDestination(car.getFinalDestination());
            testCar.setDestinationTrack(car.getFinalDestinationTrack());
            Train secondTrain = InstanceManager.getDefault(TrainManager.class).getTrainForCar(testCar, _buildReport);
            if (secondTrain == null) {
                if (debugFlag) {
                    log.debug("Could not find a train to service car from {} ({}, {}) to destination ({}, {})",
                            trackType, track.getLocation().getName(), track.getName(), testCar.getDestinationName(),
                            testCar.getDestinationTrackName()); // NOI18N
                }
                if (_addtoReport) {
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterNotFindTrain"),
                            new Object[]{Track.getTrackTypeName(trackType), track.getLocation().getName(),
                                    track.getName(), testCar.getDestinationName(), testCar.getDestinationTrackName()}));
                }
                continue;
            }
            if (debugFlag) {
                log.debug("Train ({}) can service car ({}) from {} ({}, {}) to final destination ({}, {})", secondTrain
                        .getName(), car, trackType, testCar.getLocationName(), testCar.getTrackName(),
                        testCar.getDestinationName(), testCar.getDestinationTrackName());
            }
            if (_addtoReport) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterTrainCanTransport"),
                        new Object[]{secondTrain.getName(), car.toString(), Track.getTrackTypeName(trackType),
                                testCar.getLocationName(), testCar.getTrackName(), testCar.getDestinationName(),
                                testCar.getDestinationTrackName()}));
            }
            // Save the "last" tracks for later use
            _lastLocationTracks.add(track);
            _lastLocationTrains.add(secondTrain);
            // now try to forward car to this interim location
            testCar.setTrack(car.getTrack()); // restore test car's location and track
            testCar.setDestination(track.getLocation()); // forward test car to this interim destination and track
            testCar.setDestinationTrack(track);
            // determine if car can be transported from current location to this yard or interchange
            // find a train that will transport the car to the interim track
            Train firstTrain = null;
            String specific = canSpecificTrainService(testCar);
            if (specific.equals(YES)) {
                firstTrain = _train;
            } else if (specific.equals(NOT_NOW)) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterTrainCanNotDueTo"),
                        new Object[]{_train.getName(), car.toString(), track.getLocation().getName(),
                                track.getName(), _train.getServiceStatus()}));
                foundRoute = true; // however, the issue is route moves or train length
            } else {
                firstTrain = InstanceManager.getDefault(TrainManager.class).getTrainForCar(testCar, _buildReport);
            }
            if (firstTrain == null && _addtoReport) {
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterNotFindTrain"),
                        new Object[]{testCar.getTrack().getTrackTypeName(),
                                testCar.getTrack().getLocation().getName(), testCar.getTrack().getName(),
                                testCar.getDestinationName(), testCar.getDestinationTrackName()}));
            }
            // Can the specific train carry this car out of staging?
            if (_train != null && car.getTrack().isStaging() && !specific.equals(YES)) {
                if (debugFlag) {
                    log.debug("Train ({}) can not deliver car to ({}, {})", _train.getName(), track.getLocation()
                            .getName(), track.getName());
                }
                if (_addtoReport) {
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterTrainCanNot"),
                            new Object[]{_train.getName(), car.toString(), car.getLocationName(), car.getTrackName(),
                                    track.getLocation().getName(), track.getName()}));
                }
                continue; // can't use this train
            }
            // Is the option for the specific train carry this car?
            if (firstTrain != null &&
                    _train != null &&
                    _train.isServiceAllCarsWithFinalDestinationsEnabled() &&
                    !specific.equals(YES)) {
                if (_addtoReport) {
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterOptionToCarry"),
                            new Object[]{_train.getName(), firstTrain.getName(), car.toString(),
                                    track.getLocation().getName(),
                                    track.getName()}));
                }
                continue; // can't use this train
            }
            if (firstTrain != null) {
                foundRoute = true; // found a route
                // found a two train route for this car, show the car's route
                if (_addtoReport) {
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterRoute2TrainsForCar"),
                            new Object[]{car.toString(), car.getLocationName(), car.getTrackName(),
                                    firstTrain.getName(),
                                    testCar.getDestinationName(), testCar.getDestinationTrackName(),
                                    secondTrain.getName(),
                                    car.getFinalDestinationName(), car.getFinalDestinationTrackName()}));
                } else {
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterRoute2ForCar"),
                            new Object[]{car.toString(), car.getLocationName(), car.getTrackName(),
                                    testCar.getDestinationName(), testCar.getDestinationTrackName(),
                                    car.getFinalDestinationName(), car.getFinalDestinationTrackName()}));
                }
                _status = car.testDestination(track.getLocation(), track);
                if (_status.startsWith(Track.LENGTH)) {
                    // if the issue is length at the interim track, add message to build report
                    addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCanNotDeliverCar"),
                            new Object[]{car.toString(), track.getLocation().getName(), track.getName(), _status,
                                    track.getTrackTypeName()}));
                    continue;
                }
                if (_status.equals(Track.OKAY)) {
                    // only set car's destination if specific train can service car
                    if (_train != null && _train != firstTrain) {
                        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("TrainDoesNotServiceCar"),
                                new Object[]{_train.getName(), car.toString(), testCar.getDestinationName(),
                                        testCar.getDestinationTrackName()}));
                        _status = STATUS_NOT_THIS_TRAIN;
                        continue;// found a route but it doesn't start with the specific train
                    }
                    // is this the staging track assigned to the specific train?
                    if (track.isStaging() &&
                            firstTrain.getTerminationTrack() != null &&
                            firstTrain.getTerminationTrack() != track) {
                        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterTrainIntoStaging"),
                                new Object[]{firstTrain.getName(),
                                        firstTrain.getTerminationTrack().getLocation().getName(),
                                        firstTrain.getTerminationTrack().getName()}));
                        continue;
                    }
                    _status = car.setDestination(track.getLocation(), track);
                    if (debugFlag) {
                        log.debug("Train ({}) can service car ({}) from current location ({}, {}) to {} ({}, {})",
                                firstTrain.getName(), car, car.getLocationName(), car.getTrackName(),
                                trackType, track.getLocation().getName(), track.getName()); // NOI18N
                    }
                    if (_addtoReport) {
                        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterTrainCanService"),
                                new Object[]{firstTrain.getName(), car.toString(), car.getLocationName(),
                                        car.getTrackName(), Track.getTrackTypeName(trackType),
                                        track.getLocation().getName(), track.getName()}));
                    }
                    return true; // the specific train and another train can carry the car to its destination
                }
            }
        }
        if (foundRoute) {
            _status = STATUS_NOT_ABLE;
        }
        return foundRoute;
    }

    /*
     * Note that "last" set of location/tracks was loaded by
     * setCarDestinationTwoTrains. The following code builds two additional sets
     * of location/tracks called "next" and "other". "next" is the next set of
     * location/tracks that the car can reach by a single train. "last" is the
     * last set of location/tracks that services the cars final destination. And
     * "other" is the remaining sets of location/tracks that are not "next" or
     * "last". The code then tries to connect the "next" and "last"
     * location/track sets with a train that can service the car. If successful,
     * that would be a three train route for the car. If not successful, the
     * code than tries combinations of "next", "other" and "last"
     * location/tracks to create a route for the car.
     */
    private boolean setCarDestinationMultipleTrains(Car car, boolean useStaging) {
        if (useStaging && !Setup.isCarRoutingViaStagingEnabled())
            return false; // routing via staging is disabled
        boolean foundRoute = false;
        if (_lastLocationTracks.isEmpty()) {
            if (_addtoReport)
                addLine(_buildReport, SEVEN, BLANK_LINE);
            if (useStaging)
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCouldNotFindStaging"),
                        new Object[]{car.getFinalDestinationName()}));
            else
                addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCouldNotFind"),
                        new Object[]{car.getFinalDestinationName()}));
            return false;
        }

        Car testCar = clone(car); // reload
        // build the "next" and "other" location/tracks
        // start with interchanges
        List<Track> tracks;
        if (!useStaging) {
            tracks = InstanceManager.getDefault(LocationManager.class).getTracksByMoves(Track.INTERCHANGE);
            loadTracks(car, testCar, tracks);
        }
        // next load yards if enabled
        if (!useStaging && Setup.isCarRoutingViaYardsEnabled()) {
            tracks = InstanceManager.getDefault(LocationManager.class).getTracksByMoves(Track.YARD);
            loadTracks(car, testCar, tracks);
        }
        // now staging if enabled
        if (useStaging) {
            tracks = InstanceManager.getDefault(LocationManager.class).getTracksByMoves(Track.STAGING);
            loadTracks(car, testCar, tracks);
        }

        if (_nextLocationTracks.isEmpty()) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCouldNotFindLoc"),
                    new Object[]{car.getLocationName()}));
            return false;
        }

        // state that routing begins using three trains
        if (_addtoReport)
            addLine(_buildReport, SEVEN, BLANK_LINE);
        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterThreeTrains"), new Object[]{car
                .getFinalDestinationName()}));

        if (log.isDebugEnabled()) {
            // tracks that could be the very next destination for the car
            for (Track t : _nextLocationTracks) {
                log.debug("Next location ({}, {}) can service car ({}) using train ({})", t.getLocation().getName(),
                        t.getName(), car, _nextLocationTrains.get(_nextLocationTracks.indexOf(t)));
            }
            // tracks that could be the next to last destination for the car
            for (Track t : _lastLocationTracks) {
                log.debug("Last location ({}, {}) can service car ({}) using train ({})", t.getLocation().getName(),
                        t.getName(), car, _lastLocationTrains.get(_lastLocationTracks.indexOf(t)));
            }
            // tracks that are not the next or the last list
            for (Track t : _otherLocationTracks) {
                log.debug("Other location ({}, {}) may be needed to service car ({})", t.getLocation().getName(), t
                        .getName(), car);
            }
            log.debug("Try to find route using 3 trains");
        }
        for (Track nlt : _nextLocationTracks) {
            testCar.setTrack(nlt); // set car to this location and track
            for (Track llt : _lastLocationTracks) {
                testCar.setDestinationTrack(llt); // set car to this destination and track
                // does a train service these two locations?
                Train middleTrain = InstanceManager.getDefault(TrainManager.class).getTrainForCar(testCar, null); // don't add to report
                if (middleTrain != null) {
                    log.debug("Found 3 train route, setting car destination ({}, {})", testCar.getLocationName(),
                            testCar.getTrackName());
                    foundRoute = true;
                    // show the route
                    if (_addtoReport) {
                        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterRoute3TrainsForCar"),
                                new Object[]{car.toString(), car.getLocationName(), car.getTrackName(),
                                        _nextLocationTrains.get(_nextLocationTracks.indexOf(nlt)).getName(),
                                        testCar.getLocationName(), testCar.getTrackName(),
                                        middleTrain.getName(),
                                        testCar.getDestinationName(), testCar.getDestinationTrackName(),
                                        _lastLocationTrains.get(_lastLocationTracks.indexOf(llt)).getName(),
                                        car.getFinalDestinationName(), car.getFinalDestinationTrackName()}));
                    } else {
                        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterRoute3ForCar"),
                                new Object[]{car.toString(), car.getLocationName(), car.getTrackName(),
                                        testCar.getLocationName(), testCar.getTrackName(), testCar.getDestinationName(),
                                        testCar.getDestinationTrackName(), car.getFinalDestinationName(),
                                        car.getFinalDestinationTrackName()}));
                    }
                    if (finshSettingRouteFor(car, nlt)) {
                        return true; // done 3 train routing
                    }
                    break; // there was an issue with the first stop in the route
                }
            }
        }
        if (foundRoute) {
            return foundRoute; // 3 train route, but there was an issue with the first stop in the route
        }
        log.debug("Using 3 trains to route car to ({}) was unsuccessful", car.getFinalDestinationName());
        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterFourTrains"), new Object[]{car
                .getFinalDestinationName()}));
        for (Track nlt : _nextLocationTracks) {
            otherloop: for (Track mlt : _otherLocationTracks) {
                testCar.setTrack(nlt); // set car to this location and track
                testCar.setDestinationTrack(mlt); // set car to this destination and track
                // does a train service these two locations?
                Train middleTrain2 = InstanceManager.getDefault(TrainManager.class).getTrainForCar(testCar, null); // don't add to report
                if (middleTrain2 != null) {
                    if (debugFlag) {
                        log.debug("Train 2 ({}) services car from ({}) to ({}, {})", middleTrain2.getName(), testCar
                                .getLocationName(), testCar.getDestinationName(), testCar.getDestinationTrackName());
                    }
                    for (Track llt : _lastLocationTracks) {
                        testCar.setTrack(mlt); // set car to this location and track
                        testCar.setDestinationTrack(llt); // set car to this destination and track
                        Train middleTrain3 =
                                InstanceManager.getDefault(TrainManager.class).getTrainForCar(testCar, null); // don't add to
                        // report
                        if (middleTrain3 != null) {
                            log.debug("Found 4 train route, setting car destination ({}, {})", nlt.getLocation()
                                    .getName(), nlt.getName());
                            foundRoute = true;
                            // show the route
                            if (_addtoReport) {
                                addLine(_buildReport, SEVEN,
                                        MessageFormat.format(Bundle.getMessage("RouterRoute4TrainsForCar"),
                                                new Object[]{car.toString(), car.getLocationName(), car.getTrackName(),
                                                        _nextLocationTrains.get(_nextLocationTracks.indexOf(nlt))
                                                                .getName(),
                                                        nlt.getLocation(), nlt.getName(),
                                                        middleTrain2.getName(),
                                                        mlt.getLocation().getName(), mlt.getName(),
                                                        middleTrain3.getName(),
                                                        llt.getLocation().getName(), llt.getName(),
                                                        _lastLocationTrains.get(_lastLocationTracks.indexOf(llt))
                                                                .getName(),
                                                        car.getFinalDestinationName(),
                                                        car.getFinalDestinationTrackName()}));
                            } else {
                                addLine(_buildReport, SEVEN,
                                        MessageFormat.format(Bundle.getMessage("RouterRoute4ForCar"),
                                                new Object[]{car.toString(), car.getLocationName(), car.getTrackName(),
                                                        nlt.getLocation(), nlt.getName(), mlt.getLocation().getName(),
                                                        mlt.getName(), llt.getLocation().getName(), llt.getName(),
                                                        car.getFinalDestinationName(),
                                                        car.getFinalDestinationTrackName()}));
                            }
                            if (finshSettingRouteFor(car, nlt)) {
                                return true; // done 4 train routing
                            }
                            break otherloop; // there was an issue with the first stop in the route
                        }
                    }
                }
            }
        }
        if (foundRoute) {
            return foundRoute; // 4 train route, but there was an issue with the first stop in the route
        }
        log.debug("Using 4 trains to route car to ({}) was unsuccessful", car.getFinalDestinationName());
        addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterFiveTrains"), new Object[]{car
                .getFinalDestinationName()}));
        for (Track nlt : _nextLocationTracks) {
            otherloop: for (Track mlt1 : _otherLocationTracks) {
                testCar.setTrack(nlt); // set car to this location and track
                testCar.setDestinationTrack(mlt1); // set car to this destination and track
                // does a train service these two locations?
                Train middleTrain2 = InstanceManager.getDefault(TrainManager.class).getTrainForCar(testCar, null); // don't add to report
                if (middleTrain2 != null) {
                    if (debugFlag) {
                        log.debug("Train 2 ({}) services car from ({}) to ({}, {})", middleTrain2.getName(), testCar
                                .getLocationName(), testCar.getDestinationName(), testCar.getDestinationTrackName());
                    }
                    for (Track mlt2 : _otherLocationTracks) {
                        if (mlt1 == mlt2) {
                            continue;
                        }
                        testCar.setTrack(mlt1); // set car to this location and track
                        testCar.setDestinationTrack(mlt2); // set car to this destination and track
                        // does a train service these two locations?
                        Train middleTrain3 =
                                InstanceManager.getDefault(TrainManager.class).getTrainForCar(testCar, null); // don't add to
                        // report
                        if (middleTrain3 != null) {
                            if (debugFlag) {
                                log.debug("Train 3 ({}) services car from ({}) to ({}, {})", middleTrain3.getName(),
                                        testCar.getLocationName(), testCar.getDestinationName(), testCar
                                                .getDestinationTrackName());
                            }
                            for (Track llt : _lastLocationTracks) {
                                testCar.setTrack(mlt2); // set car to this location and track
                                testCar.setDestinationTrack(llt); // set car to this destination and track
                                // does a train service these two locations?
                                Train middleTrain4 =
                                        InstanceManager.getDefault(TrainManager.class).getTrainForCar(testCar, null); // don't add
                                // to report
                                if (middleTrain4 != null) {
                                    log.debug("Found 5 train route, setting car destination ({}, {})", nlt
                                            .getLocation().getName(), nlt.getName());
                                    foundRoute = true;
                                    // show the car's route
                                    if (_addtoReport) {
                                        addLine(_buildReport, SEVEN,
                                                MessageFormat.format(Bundle.getMessage("RouterRoute5TrainsForCar"),
                                                        new Object[]{car.toString(), car.getLocationName(),
                                                                car.getTrackName(),
                                                                _nextLocationTrains
                                                                        .get(_nextLocationTracks.indexOf(nlt))
                                                                        .getName(),
                                                                nlt.getLocation().getName(), nlt.getName(),
                                                                middleTrain2.getName(),
                                                                mlt1.getLocation().getName(), mlt1.getName(),
                                                                middleTrain3.getName(),
                                                                mlt2.getLocation().getName(), mlt2.getName(),
                                                                middleTrain4.getName(),
                                                                llt.getLocation().getName(), llt.getName(),
                                                                _lastLocationTrains
                                                                        .get(_lastLocationTracks.indexOf(llt))
                                                                        .getName(),
                                                                car.getFinalDestinationName(),
                                                                car.getFinalDestinationTrackName()}));
                                    } else {
                                        addLine(_buildReport, SEVEN,
                                                MessageFormat.format(Bundle.getMessage("RouterRoute5ForCar"),
                                                        new Object[]{car.toString(), car.getLocationName(),
                                                                car.getTrackName(), nlt.getLocation().getName(),
                                                                nlt.getName(), mlt1.getLocation().getName(),
                                                                mlt1.getName(),
                                                                mlt2.getLocation().getName(), mlt2.getName(),
                                                                llt.getLocation().getName(),
                                                                llt.getName(), car.getFinalDestinationName(),
                                                                car.getFinalDestinationTrackName()}));
                                    }
                                    // only set car's destination if specific train can service car
                                    if (finshSettingRouteFor(car, nlt)) {
                                        return true; // done 5 train routing
                                    }
                                    break otherloop; // there was an issue with the first stop in the route
                                }
                            }
                        }
                    }
                }
            }
        }
        log.debug("Using 5 trains to route car to ({}) was unsuccessful", car.getFinalDestinationName());
        return foundRoute;
    }

    private boolean finshSettingRouteFor(Car car, Track track) {
        // only set car's destination if specific train can service car
        Car ts2 = clone(car);
        ts2.setDestinationTrack(track);
        String specific = canSpecificTrainService(ts2);
        if (specific.equals(NO)) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("TrainDoesNotServiceCar"),
                    new Object[]{_train.getName(), car.toString(), track.getLocation().getName(), track.getName()}));
            _status = STATUS_NOT_THIS_TRAIN;
            return true;
        } else if (specific.equals(NOT_NOW)) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterTrainCanNotDueTo"),
                    new Object[]{_train.getName(), car.toString(), track.getLocation().getName(), track.getName(),
                            _train.getServiceStatus()}));
            return true; // the issue is route moves or train length
        }
        // check to see if track is staging
        if (track.isStaging() &&
                _train != null &&
                _train.getTerminationTrack() != null &&
                _train.getTerminationTrack() != track) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterTrainIntoStaging"),
                    new Object[]{_train.getName(), _train.getTerminationTrack().getLocation().getName(),
                            _train.getTerminationTrack().getName()}));
            return false; // wrong track into staging
        }
        _status = car.setDestination(track.getLocation(), track);
        if (!_status.equals(Track.OKAY)) {
            addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCanNotDeliverCar"),
                    new Object[]{car.toString(), track.getLocation().getName(), track.getName(), _status,
                            track.getTrackTypeName()}));
            if (_status.startsWith(Track.LENGTH)) {
                return false;
            }
        }
        return true;
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
        // don't set the clone's final destination track, that will record the cars as being inbound
        // next two items is where the clone is different
        clone.setDestination(car.getFinalDestination()); // note that final destination track can be null
        clone.setDestinationTrack(car.getFinalDestinationTrack());
        return clone;
    }

    private void loadTracks(Car car, Car testCar, List<Track> tracks) {
        for (Track track : tracks) {
            if (track == car.getTrack()) {
                continue; // don't use car's current track
            } // note that last could equal next if this routine was used for two train routing
            if (_lastLocationTracks.contains(track)) {
                continue;
            }
            String status = track.accepts(testCar);
            if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
                continue; // track doesn't accept this car
            }
            if (debugFlag) {
                log.debug("Found {} track ({}, {}) for car ({})", track.getTrackType(), track.getLocation().getName(),
                        track.getName(), car);
            }
            // test to see if there's a train that can deliver the car to this destination
            testCar.setDestinationTrack(track);
            Train train = null;
            String specific = canSpecificTrainService(testCar);
            if (specific.equals(YES) || specific.equals(NOT_NOW)) {
                train = _train;
            } else {
                train = InstanceManager.getDefault(TrainManager.class).getTrainForCar(testCar, null); // don't add to report
            }
            // Can specific train carry this car out of staging?
            if (car.getTrack().isStaging() && !specific.equals(YES)) {
                train = null;
            }
            // is the option to car by specific enabled?
            if (train != null &&
                    _train != null &&
                    _train.isServiceAllCarsWithFinalDestinationsEnabled() &&
                    !specific.equals(YES)) {
                addLine(_buildReport, SEVEN, MessageFormat
                        .format(Bundle.getMessage("RouterOptionToCarry"),
                                new Object[]{_train.getName(), train.getName(),
                                        car.toString(), track.getLocation().getName(), track.getName()}));
                train = null;
            }
            if (train != null) {
                if (debugFlag) {
                    log.debug("Train ({}) can service car ({}) from {} ({}, {}) to final destination ({}, {})", train
                            .getName(), car, track.getTrackType(), testCar.getLocationName(),
                            testCar
                                    .getTrackName(),
                            testCar.getDestinationName(), testCar.getDestinationTrackName());
                }
                _nextLocationTracks.add(track);
                _nextLocationTrains.add(train);
            } else {
                // don't add to other if already in last location list
                if (debugFlag) {
                    log.debug("Adding location ({}, {}) to other locations", track.getLocation().getName(), track
                            .getName());
                }
                _otherLocationTracks.add(track);
            }
        }
    }

    private static final String NO = "no"; // NOI18N
    private static final String YES = "yes"; // NOI18N
    private static final String NOT_NOW = "not now"; // NOI18N
    private static final String NO_SPECIFIC_TRAIN = "no specific train"; // NOI18N

    private String canSpecificTrainService(Car car) {
        if (_train == null) {
            return NO_SPECIFIC_TRAIN;
        }
        if (_train.services(car)) {
            return YES;
        } // is the reason this train can't service route moves or train length?
        else if (!_train.getServiceStatus().equals(Train.NONE)) {
            return NOT_NOW; // the issue is route moves or train length
        }
        return NO;
    }

    private final static Logger log = LoggerFactory.getLogger(Router.class);

}
