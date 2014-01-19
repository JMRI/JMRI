package jmri.jmrit.operations.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Router for car movement. This code attempts to find a way (a route) to move a car to its final destination through
 * the use of two or more trains. Code t tries to move car using a single train. If that fails, attempts are made to
 * use two trains via an interchange track , then a yard. Next attempts are made using three or more trains using any
 * combination of interchanges and yards. Currently the router is limited to five trains.
 * 
 * @author Daniel Boudreau Copyright (C) 2010, 2011, 2012, 2013
 * @version $Revision$
 */

public class Router extends TrainCommon {

	private List<Track> _nextLocationTracks = new ArrayList<Track>();
	private List<Track> _lastLocationTracks = new ArrayList<Track>();
	private List<Track> _otherLocationTracks = new ArrayList<Track>();

	private static final String STATUS_NOT_THIS_TRAIN = Bundle.getMessage("RouterTrain");
	private static final String STATUS_NOT_ABLE = Bundle.getMessage("RouterNotAble");
	private static final String STATUS_CAR_AT_DESINATION = Bundle.getMessage("RouterCarAtDestination");
	private static final String STATUS_NO_TRAINS = Bundle.getMessage("RouterNoTrains");
	private static final String STATUS_ROUTER_DISABLED = Bundle.getMessage("RouterDisabled");

	private String _status = "";
	private Train _train = null;
	PrintWriter _buildReport = null; // build report

	public boolean _enable_yard_search = false; // search for yard track even if an interchange track was found
	private static boolean debugFlag = false;

	protected static final String SEVEN = Setup.BUILD_REPORT_VERY_DETAILED;
	private boolean _addtoReport = false;
	private boolean _addtoReportVeryDetailed = false;

	/** record the single instance **/
	private static Router _instance = null;

	public static synchronized Router instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("Router creating instance");
			// create and load
			_instance = new Router();
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("Router returns instance " + _instance);
		return _instance;
	}

	/**
	 * Returns the status of the router when using the setDestination() for a car.
	 * 
	 * @return Track.OKAY, STATUS_NOT_THIS_TRAIN, STATUS_NOT_ABLE, STATUS_CAR_AT_DESINATION, or STATUS_ROUTER_DISABLED
	 */
	public String getStatus() {
		return _status;
	}

	/**
	 * Attempts to set the car's destination if a final destination exists. Only sets the car's destination if the train
	 * is part of the car's route.
	 * 
	 * @param car
	 *            the car to route
	 * @param train
	 *            the first train to carry this car, can be null
	 * @param buildReport
	 *            PrintWriter for build report, and can be null
	 * @return true if car can be routed.
	 */
	public boolean setDestination(Car car, Train train, PrintWriter buildReport) {
		if (car.getTrack() == null || car.getFinalDestination() == null)
			return false;
		_status = Track.OKAY;
		_train = train;
		_buildReport = buildReport;
		_addtoReport = !Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL)
				&& !Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_MINIMAL);
		_addtoReportVeryDetailed = Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED);
		log.debug("Car (" + car.toString() + ") at location (" + car.getLocationName() + ", " + car.getTrackName()
				+ ") " + "final destination (" + car.getFinalDestinationName() // NOI18N
				+ ", " + car.getFinalDestinationTrackName() + ") car routing begins"); // NOI18N
		if (_train != null)
			log.debug("Routing using train (" + train.getName() + ")");
		// Has the car arrived at the car's final destination?
		if (car.getLocation() != null && car.getLocation().equals(car.getFinalDestination())
				&& (car.getTrack().equals(car.getFinalDestinationTrack()) || car.getFinalDestinationTrack() == null)) {
			log.debug("Car (" + car.toString() + ") has arrived at final destination");
			_status = STATUS_CAR_AT_DESINATION;
			car.setFinalDestination(null);
			car.setFinalDestinationTrack(null);
			return false;
		}
		// is car part of kernel?
		if (car.getKernel() != null && !car.getKernel().isLead(car))
			return false;
		// note clone car has the car's "final destination" as its destination
		Car clone = clone(car);
		// Note the following test doesn't check for car length which is what we want. Also ignores if track has a
		// schedule.
		_status = clone.testDestination(clone.getDestination(), clone.getDestinationTrack());
		if (!_status.equals(Track.OKAY)) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterNextDestFailed"), new Object[] {
					car.getFinalDestinationName(), car.getFinalDestinationTrackName(), car.toString(), _status }));
			return false;
		}
		// check to see if car will move to destination using a single train
		if (checkForSingleTrain(car, clone)) {
			return true;	// a single train can service this car
		} else if (Setup.isCarRoutingEnabled()) {
			log.debug("Car (" + car.toString() + ") final destination (" + car.getFinalDestinationName()
					+ ") is not served by a single train"); // NOI18N
			// was the request for a local move?
			if (car.getLocationName().equals(car.getFinalDestinationName())) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCouldNotFindTrain"),
						new Object[] { car.getLocationName(), car.getTrackName(), car.getFinalDestinationName(),
								car.getFinalDestinationTrackName() }));
				_status = STATUS_NO_TRAINS;
				return false; // maybe next time
			}
			if (_addtoReport)
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterBeginTwoTrain"),
						new Object[] { car.toString(), car.getLocationName(), car.getFinalDestinationName() }));
			_nextLocationTracks.clear();
			_lastLocationTracks.clear();
			_otherLocationTracks.clear();
			// first try using 2 trains and an interchange track to route the car
			if (setCarDestinationInterchange(car)) {
				if (_enable_yard_search && _status.equals(STATUS_NOT_THIS_TRAIN)) {
					log.debug("Was able to find a route via classification/interchange track, but not using train ("
							+ _train.getName() + ") try again using yard tracks"); // NOI18N
					if (setCarDestinationYard(car)) {
						log.debug("Was able to find route via yard (" + car.getDestinationName() + ", "
								+ car.getDestinationTrackName() + ") for car (" + car.toString() + ")"); // NOI18N
					}
				} else {
					log.debug("Was able to find route via interchange (" + car.getDestinationName() + ", "
							+ car.getDestinationTrackName() + ") for car (" + car.toString() // NOI18N
							+ ")");
				}
				// now try 2 trains and a yard track
			} else if (setCarDestinationYard(car)) {
				log.debug("Was able to find route via yard (" + car.getDestinationName() + ", "
						+ car.getDestinationTrackName() + ") for car (" + car.toString() + ")"); // NOI18N
				// now try 2 trains and a staging track
			} else if (setCarDestinationStaging(car)) {
				log.debug("Was able to find route via staging (" + car.getDestinationName() + ", "
						+ car.getDestinationTrackName() + ") for car (" + car.toString() + ")"); // NOI18N
				// now try 3 or more trains to route car
			} else if (setCarDestinationMultipleTrains(car)) {
				log.debug("Was able to find multiple train route for car (" + car.toString() + ")");
			} else {
				log.debug("Wasn't able to set route for car (" + car.toString() + ")");
				_status = STATUS_NOT_ABLE;
				return false; // maybe next time
			}
		} else {
			log.warn("Car (" + car.toString() + ") final destination (" + car.getFinalDestinationName()
					+ ") is not served directly by any train"); // NOI18N
			_status = STATUS_ROUTER_DISABLED;
			car.setFinalDestination(null);
			car.setFinalDestinationTrack(null);
			return false;
		}
		return true; // car's destination has been set
	}
	
	/**
	 * Checks to see if a single train can transport car to its final destination.
	 * @param car
	 * @param clone
	 * @return true if single train can transport car to its final destination.
	 */
	private boolean checkForSingleTrain(Car car, Car clone) {
		boolean trainServicesCar = false; // specific train
		Train testTrain = null;
		if (_train != null)
			trainServicesCar = _train.services(_buildReport, clone);
		if (trainServicesCar)
			testTrain = _train; // use the specific train
		// can specific train can service car out of staging. Note that the router code will try to route the car using
		// two or more trains just to get the car out of staging.
		if (car.getTrack().getTrackType().equals(Track.STAGING) && _train != null && !trainServicesCar) {
			log.debug("Car (" + car.toString() + ") destination (" + clone.getDestinationName() + ", "
					+ clone.getDestinationTrackName() + ") is not serviced by train (" // NOI18N
					+ _train.getName() + ") out of staging"); // NOI18N
			if (!_train.getServiceStatus().equals(""))
				addLine(_buildReport, SEVEN, _train.getServiceStatus());
		} else if (!trainServicesCar) {
			testTrain = TrainManager.instance().getTrainForCar(clone, _train, _buildReport);
		}
		if (testTrain != null && _train != null && !trainServicesCar
				&& _train.isServiceAllCarsWithFinalDestinationsEnabled()) {
//			log.debug("Option to service all cars with a final destination is enabled");
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterOptionToCarry"), new Object[] {
					testTrain.getName(), car.toString(), clone.getDestinationName(), clone.getDestinationTrackName() }));
			testTrain = null;
		}
		if (testTrain != null) {
			return routeUsingOneTrain(testTrain, car, clone);
		}
		return false;
	}
	
	/**
	 * A single train can service the car.  Provide various messages to build report detailing which train
	 * can service the car.  Also checks to see if the needs to go the alternate track or yard track if the
	 * car's final destination track is full.
	 * @param testTrain
	 * @param car
	 * @param clone
	 * @return always returns true.
	 */
	private boolean routeUsingOneTrain(Train testTrain, Car car, Car clone) {
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCarSingleTrain"), new Object[] {
				testTrain.getName(), car.toString(), clone.getDestinationName(), clone.getDestinationTrackName() }));
		// now check to see if specific train can service car directly
		if (_train != null && _train != testTrain) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("TrainDoesNotServiceCar"),
					new Object[] { _train.getName(), car.toString(), clone.getDestinationName(),
							clone.getDestinationTrackName() }));
			_status = STATUS_NOT_THIS_TRAIN;
			return true; // car can be routed, but not by this train!
		}
		_status = car.setDestination(clone.getDestination(), clone.getDestinationTrack());
		if (_status.equals(Track.OKAY)) {
			return true; // done, car has new destination
		}
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCanNotDeliverCar"), new Object[] {
				car.toString(), clone.getDestinationName(), clone.getDestinationTrackName(), _status }));
		// check to see if an alternative track was specified
		if ((_status.startsWith(Track.LENGTH) || _status.startsWith(Track.SCHEDULE))
				&& clone.getDestinationTrack() != null && clone.getDestinationTrack().getAlternateTrack() != null
				&& clone.getDestinationTrack().getAlternateTrack() != car.getTrack()) {
			String status = car.setDestination(clone.getDestination(), clone.getDestinationTrack().getAlternateTrack());
			if (status.equals(Track.OKAY)) {
				if (_train == null || _train.services(car)) {
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterSendCarToAlternative"),
							new Object[] { car.toString(), clone.getDestinationTrack().getAlternateTrack().getName(),
									clone.getDestination().getName() }));
					return true; // car is going to alternate track
				}
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterNotSendCarToAlternative"),
						new Object[] { _train.getName(), car.toString(),
								clone.getDestinationTrack().getAlternateTrack().getName(),
								clone.getDestination().getName() }));
			} else {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterAlternateFailed"),
						new Object[] { clone.getDestinationTrack().getAlternateTrack().getName(), status }));
			}
		} else if (clone.getDestinationTrack() != null && clone.getDestinationTrack().getAlternateTrack() != null
				&& clone.getDestinationTrack().getAlternateTrack() == car.getTrack()) {
			// state that car is spotted at the alternative track
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterAtAlternate"), new Object[] {
					car.toString(), clone.getDestinationTrack().getAlternateTrack().getName(), clone.getLocationName(),
					clone.getDestinationTrackName() }));
		} else if (car.getLocation() == clone.getDestination()) {
			// state that alternative and yard track options are not available if car is at final destination
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterIgnoreAlternate"), new Object[] {
					car.toString(), car.getLocationName() }));
		}
		// check to see if spur was full, if so, forward to yard if possible
		if (Setup.isForwardToYardEnabled() && _status.startsWith(Track.LENGTH)
				&& car.getLocation() != clone.getDestination()) {
			// log.debug("Spur full, searching for a yard at destination ("+clone.getDestinationName()+")");
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterSpurFull"), new Object[] {
					clone.getDestinationTrackName(), clone.getDestinationName() }));
			Location dest = clone.getDestination();
			List<String> yards = dest.getTrackIdsByMovesList(Track.YARD);
			log.debug("Found " + yards.size() + " yard(s) at destination (" + clone.getDestinationName() + ")");
			for (int i = 0; i < yards.size(); i++) {
				Track track = dest.getTrackById(yards.get(i));
				String status = car.setDestination(dest, track);
				if (status.equals(Track.OKAY)) {
					if (_train != null && !_train.services(car)) {
						log.debug("Train (" + _train.getName() + ") can not deliver car (" + car.toString()
								+ ") to yard (" + track.getName() + ")"); // NOI18N
						continue;
					}
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterSendCarToYard"),
							new Object[] { car.toString(), track.getName(), dest.getName() }));
					return true; // car is going to a yard
				}
			}
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterNoYardTracks"), new Object[] {
					dest.getName(), car.toString() }));
		}
		car.setDestination(null, null);
		return true; // able to route, but not able to set the car's destination
	}

	/**
	 * Sets a car's final destination to an interchange track if two trains can route the car.
	 * 
	 * @param car
	 *            the car to be routed
	 * @return true if car's destination has been modified to an interchange. False if an interchange track wasn't found
	 *         that could service the car's final destination.
	 */
	private boolean setCarDestinationInterchange(Car car) {
		return setCarDestinationTwoTrains(car, Track.INTERCHANGE);
	}

	/**
	 * Sets a car's final destination to a yard track if two train can route the car.
	 * 
	 * @param car
	 *            the car to be routed
	 * @return true if car's destination has been modified to a yard. False if a yard track wasn't found that could
	 *         service the car's final destination.
	 */
	private boolean setCarDestinationYard(Car car) {
		if (Setup.isCarRoutingViaYardsEnabled())
			return setCarDestinationTwoTrains(car, Track.YARD);
		return false;
	}

	/**
	 * Sets a car's final destination to a staging track if two train can route the car.
	 * 
	 * @param car
	 *            the car to be routed
	 * @return true if car's destination has been modified to a staging track. False if a staging track wasn't found
	 *         that could service the car's final destination.
	 */
	private boolean setCarDestinationStaging(Car car) {
		if (Setup.isCarRoutingViaStagingEnabled())
			return setCarDestinationTwoTrains(car, Track.STAGING);
		return false;
	}

	private boolean setCarDestinationTwoTrains(Car car, String trackType) {
		Car testCar = clone(car); // reload
		log.debug("Two train routing, find " + trackType + " track for car (" + car.toString() 
				+ ") final destination (" + testCar.getDestinationName() + ", " + testCar.getDestinationTrackName() // NOI18N
				+ ")"); // NOI18N
		if (_addtoReport)
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterFindTrack"), new Object[] {
					trackType, car.toString(), testCar.getDestinationName(), testCar.getDestinationTrackName() }));
		// save car's location, track, destination, and destination track
		Track saveTrack = testCar.getTrack();
		Location saveDestination = testCar.getDestination();
		Track saveDestinationTrack = testCar.getDestinationTrack();
		if (saveTrack == null) {
			log.debug("Car's track is null! Can't route");
			return false;
		}
		boolean foundRoute = false;
		// now search for a yard or interchange that a train can pick up and deliver the car to its destination
		List<Track> tracks = LocationManager.instance().getTracksByMoves(trackType);
		for (int i = 0; i < tracks.size(); i++) {
			Track track = tracks.get(i);
			if (saveTrack == track)
				continue; // don't use car's current track
			String status = track.accepts(testCar);
			if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH)) {
				if (_addtoReportVeryDetailed)
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCanNotDeliverCar"),
							new Object[] { car.toString(), track.getLocation().getName(), track.getName(), status }));
				continue;
			}
			if (debugFlag)
				log.debug("Found " + trackType + " track (" + track.getLocation().getName() + ", " + track.getName()
						+ ") for car (" + car.toString() + ")"); // NOI18N
			if (_addtoReport)
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterFoundTrack"), new Object[] {
						trackType, track.getLocation().getName(), track.getName(), car.toString() }));
			// test to see if there's a train that can deliver the car to its final location
			testCar.setTrack(track);
			testCar.setDestination(saveDestination);
			testCar.setDestinationTrack(saveDestinationTrack);
			Train nextTrain = TrainManager.instance().getTrainForCar(testCar, _buildReport);
			if (nextTrain == null) {
				if (debugFlag)
					log.debug("Could not find a train to service car from " + trackType + " ("
							+ track.getLocation().getName() + ", " + track.getName() + ") to destination (" // NOI18N
							+ testCar.getDestinationName() + " ," + testCar.getDestinationTrackName() + ")"); // NOI18N
				if (_addtoReport)
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterNotFindTrain"),
							new Object[] { trackType, track.getLocation().getName(), track.getName(),
									testCar.getDestinationName(), testCar.getDestinationTrackName() }));
				continue;
			}
			if (debugFlag)
				log.debug("Train (" + nextTrain.getName() + ") can service car (" + car.toString() + ") from "
						+ trackType // NOI18N
						+ " (" // NOI18N
						+ testCar.getLocationName() + ", " + testCar.getTrackName() // NOI18N
						+ ") to final destination (" + testCar.getDestinationName() // NOI18N
						+ ", " + testCar.getDestinationTrackName() + ")");
			if (_addtoReport)
				addLine(_buildReport, SEVEN, MessageFormat
						.format(Bundle.getMessage("RouterTrainCanTransport"), new Object[] { nextTrain.getName(),
								car.toString(), trackType, testCar.getLocationName(), testCar.getTrackName(),
								testCar.getDestinationName(), testCar.getDestinationTrackName() }));
			// Save the "last" tracks for later use
			_lastLocationTracks.add(track);
			// now try to forward car to this interim location
			testCar.setTrack(saveTrack); // restore car's location and track
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
						new Object[] { _train.getName(), car.toString(), track.getLocation().getName(), track.getName(),
								_train.getServiceStatus() }));
				foundRoute = true; // however, the issue is route moves or train length
			} else {
				firstTrain = TrainManager.instance().getTrainForCar(testCar, _buildReport);
			}
			// Can the specific train carry this car out of staging?
			if (car.getTrack().getTrackType().equals(Track.STAGING) && !specific.equals(YES)) {
				if (debugFlag)
					log.debug("Train (" + _train.getName() + ") can not deliver car to ("
							+ track.getLocation().getName() + ", " // NOI18N
							+ track.getName() + ")");
				if (_addtoReport)
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterTrainCanNot"),
							new Object[] { _train.getName(), car.toString(), car.getLocationName(), car.getTrackName(),
									track.getLocation().getName(), track.getName() }));
				continue; // can't use this train
			}
			// Is the option for the specific train carry this car?
			if (firstTrain != null && _train != null && _train.isServiceAllCarsWithFinalDestinationsEnabled()
					&& !specific.equals(YES)) {
				if (_addtoReport)
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterOptionToCarry"),
							new Object[] { firstTrain.getName(), car.toString(), track.getLocation().getName(),
									track.getName() }));
				continue; // can't use this train
			}
			if (firstTrain != null) {
				foundRoute = true; // found a route
				// found a two train route for this car, show the car's route
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterRoute2ForCar"),
						new Object[] { car.toString(), car.getLocationName(), car.getTrackName(),
								testCar.getDestinationName(), testCar.getDestinationTrackName(),
								car.getFinalDestinationName(), car.getFinalDestinationTrackName() }));
				_status = car.testDestination(track.getLocation(), track);
				if (_status.startsWith(Track.LENGTH)) {
					// if the issue is length at the interim track, add message to build report
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCanNotDeliverCar"),
							new Object[] { car.toString(), track.getLocation().getName(), track.getName(), _status }));
					continue;
				}
				if (_status.equals(Track.OKAY)) {
					// only set car's destination if specific train can service car
					if (_train != null && _train != firstTrain) {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("TrainDoesNotServiceCar"),
								new Object[] { _train.getName(), car.toString(), testCar.getDestinationName(),
										testCar.getDestinationTrackName() }));
						_status = STATUS_NOT_THIS_TRAIN;
						continue;// found a route but it doesn't start with the specific train
					}
					// If staging only set the cars destination, no track assignment
					if (track.getTrackType().equals(Track.STAGING))
						_status = car.setDestination(track.getLocation(), null); // don't specify which track in staging
																					// is to be used, decide later
					else
						_status = car.setDestination(track.getLocation(), track); // forward car to this intermediate
																					// destination and track.
					if (debugFlag)
						log.debug("Train (" + firstTrain.getName() + ") can service car (" + car.toString()
								+ ") from current location (" // NOI18N
								+ car.getLocationName() + ", " + car.getTrackName() + ") to " // NOI18N
								+ trackType + " (" + track.getLocation().getName() // NOI18N
								+ ", " + track.getName() + ")"); // NOI18N
					if (_addtoReport)
						addLine(_buildReport, SEVEN,
								MessageFormat
										.format(Bundle.getMessage("RouterTrainCanService"), new Object[] {
												firstTrain.getName(), car.toString(), car.getLocationName(),
												car.getTrackName(), trackType, track.getLocation().getName(),
												track.getName() }));
					return true; // the specific train and another train can carry the car to its destination
				}
			}
		}
		return foundRoute;
	}

	/*
	 * Note that "last" set of location/tracks was loaded by setCarDestinationTwoTrains. The following code builds two
	 * additional sets of location/tracks called "next" and "other". "next" is the next set of location/tracks that
	 * the car can reach by a single train. "last" is the last set of location/tracks that services the cars final
	 * destination. And "other" is the remaining sets of location/tracks that are not "next" or "last". The code then
	 * tries to connect the "next" and "last" location/track sets with a train that can service the car. If successful,
	 * that would be a three train route for the car. If not successful, the code than tries combinations of "next",
	 * "other" and "last" location/tracks to create a route for the car.
	 */
	private boolean setCarDestinationMultipleTrains(Car car) {
		boolean foundRoute = false;
		if (_lastLocationTracks.size() == 0) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCouldNotFind"),
					new Object[] { car.getFinalDestinationName() }));
			return false;
		}

		Car testCar = clone(car); // reload
		// build the "next" and "other" location/tracks
		// start with interchanges
		List<Track> tracks = LocationManager.instance().getTracksByMoves(Track.INTERCHANGE);
		loadTracks(car, testCar, tracks);
		// next load yards if enabled
		if (Setup.isCarRoutingViaYardsEnabled()) {
			tracks = LocationManager.instance().getTracksByMoves(Track.YARD);
			loadTracks(car, testCar, tracks);
		}
		// now staging if enabled
		if (Setup.isCarRoutingViaStagingEnabled()) {
			tracks = LocationManager.instance().getTracksByMoves(Track.STAGING);
			loadTracks(car, testCar, tracks);
		}
		
		if (_nextLocationTracks.size() == 0) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCouldNotFindLoc"),
					new Object[] { car.getLocationName() }));
			return false;
		}
		
		// state that routing begins using three or more trains
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterMultipleTrains"), new Object[] { car
			.getFinalDestinationName() }));
		
		if (log.isDebugEnabled()) {
			// tracks that could be the very next destination for the car
			for (int i = 0; i < _nextLocationTracks.size(); i++) {
				Track t = _nextLocationTracks.get(i);
				log.debug("Next location (" + t.getLocation().getName() + ", " + t.getName() + ") can service car ("
						+ car.toString() + ")"); // NOI18N
			}
			// tracks that could be the next to last destination for the car
			for (int i = 0; i < _lastLocationTracks.size(); i++) {
				Track t = _lastLocationTracks.get(i);
				log.debug("Last location (" + t.getLocation().getName() + ", " + t.getName() + ") can service car ("
						+ car.toString() + ")"); // NOI18N
			}
			// tracks that are not the next or the last list
			for (int i = 0; i < _otherLocationTracks.size(); i++) {
				Track t = _otherLocationTracks.get(i);
				log.debug("Other location (" + t.getLocation().getName() + ", " + t.getName()
						+ ") may be needed to service car (" + car.toString() + ")"); // NOI18N
			}
			log.debug("Try to find route using 3 trains");
		}
		for (int i = 0; i < _nextLocationTracks.size(); i++) {
			Track nlt = _nextLocationTracks.get(i);
			testCar.setTrack(nlt); // set car to this location and track
			for (int j = 0; j < _lastLocationTracks.size(); j++) {
				testCar.setDestinationTrack(_lastLocationTracks.get(j)); // set car to this destination and track
				// does a train service these two locations?
				Train middleTrain = TrainManager.instance().getTrainForCar(testCar, null); // don't add to report
				if (middleTrain != null) {
					log.debug("Found 3 train route, setting car destination (" + testCar.getLocationName() + ", "
							+ testCar.getTrackName() + ")");
					// show the route
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterRoute3ForCar"),
							new Object[] { car.toString(), car.getLocationName(), car.getTrackName(),
									testCar.getLocationName(), testCar.getTrackName(), testCar.getDestinationName(),
									testCar.getDestinationTrackName(), car.getFinalDestinationName(),
									car.getFinalDestinationTrackName() }));
					if (finshSettingRouteFor(car, nlt))
						return true; // done 3 train routing
					foundRoute = true;
					continue;	// there was an issue with the first stop in the route
				}
			}
		}
		if (foundRoute)
			return foundRoute;
		log.debug("Using 3 trains to route car to (" + car.getFinalDestinationName() + ") was unsuccessful");
		log.debug("Try to find route using 4 trains");
		for (int i = 0; i < _nextLocationTracks.size(); i++) {
			Track nlt = _nextLocationTracks.get(i);
			for (int j = 0; j < _otherLocationTracks.size(); j++) {
				Track mlt = _otherLocationTracks.get(j);
				testCar.setTrack(nlt); // set car to this location and track
				testCar.setDestinationTrack(mlt); // set car to this destination and track
				// does a train service these two locations?
				Train middleTrain2 = TrainManager.instance().getTrainForCar(testCar, null); // don't add to report
				if (middleTrain2 != null) {
					if (debugFlag)
						log.debug("Train 2 (" + middleTrain2.getName() + ") services car from "
								+ testCar.getLocationName() + " to " + testCar.getDestinationName() // NOI18N
								+ ", " + testCar.getDestinationTrackName());
					for (int k = 0; k < _lastLocationTracks.size(); k++) {
						Track llt = _lastLocationTracks.get(k);
						testCar.setTrack(mlt); // set car to this location and track
						testCar.setDestinationTrack(llt); // set car to this destination and track
						Train middleTrain3 = TrainManager.instance().getTrainForCar(testCar, null); // don't add to
																									// report
						if (middleTrain3 != null) {
							log.debug("Found 4 train route, setting car destination (" + nlt.getLocation().getName()
									+ ", " + nlt.getName() + ")");
							// show the route
							addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterRoute4ForCar"),
									new Object[] { car.toString(), car.getLocationName(), car.getTrackName(),
											nlt.getLocation(), nlt.getName(), mlt.getLocation().getName(),
											mlt.getName(), llt.getLocation().getName(), llt.getName(),
											car.getFinalDestinationName(), car.getFinalDestinationTrackName() }));
							if (finshSettingRouteFor(car, nlt))
								return true; // done 4 train routing
							foundRoute = true;
							continue; // there was an issue with the first stop in the route
						}
					}
				}
			}
		}
		if (foundRoute)
			return foundRoute;
		log.debug("Using 4 trains to route car to (" + car.getFinalDestinationName() + ") was unsuccessful");
		log.debug("Try to find route using 5 trains");
		for (int i = 0; i < _nextLocationTracks.size(); i++) {
			Track nlt = _nextLocationTracks.get(i);
			for (int j = 0; j < _otherLocationTracks.size(); j++) {
				Track mlt1 = _otherLocationTracks.get(j);
				testCar.setTrack(nlt); // set car to this location and track
				testCar.setDestinationTrack(mlt1); // set car to this destination and track
				// does a train service these two locations?
				Train middleTrain2 = TrainManager.instance().getTrainForCar(testCar, null); // don't add to report
				if (middleTrain2 != null) {
					if (debugFlag)
						log.debug("Train 2 (" + middleTrain2.getName() + ") services car from "
								+ testCar.getLocationName() + " to " + testCar.getDestinationName() // NOI18N
								+ ", " + testCar.getDestinationTrackName());
					for (int k = 0; k < _otherLocationTracks.size(); k++) {
						Track mlt2 = _otherLocationTracks.get(k);
						if (mlt1 == mlt2)
							continue;
						testCar.setTrack(mlt1); // set car to this location and track
						testCar.setDestinationTrack(mlt2); // set car to this destination and track
						// does a train service these two locations?
						Train middleTrain3 = TrainManager.instance().getTrainForCar(testCar, null); // don't add to
																									// report
						if (middleTrain3 != null) {
							if (debugFlag)
								log.debug("Train 3 (" + middleTrain3.getName() + ") services car from "
										+ testCar.getLocationName() // NOI18N
										+ " to " + testCar.getDestinationName() + ", " // NOI18N
										+ testCar.getDestinationTrackName());
							for (int n = 0; n < _lastLocationTracks.size(); n++) {
								Track llt = _lastLocationTracks.get(n);
								testCar.setTrack(mlt2); // set car to this location and track
								testCar.setDestinationTrack(llt); // set car to this destination and track
								// does a train service these two locations?
								Train middleTrain4 = TrainManager.instance().getTrainForCar(testCar, null); // don't add
																											// to report
								if (middleTrain4 != null) {
									log.debug("Found 5 train route, setting car destination ("
											+ nlt.getLocation().getName() + ", " + nlt.getName() + ")");
									// show the car's route
									addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
											.getMessage("RouterRoute5ForCar"), new Object[] { car.toString(),
											car.getLocationName(), car.getTrackName(), nlt.getLocation().getName(),
											nlt.getName(), mlt1.getLocation().getName(), mlt1.getName(),
											mlt2.getLocation().getName(), mlt2.getName(), llt.getLocation().getName(),
											llt.getName(), car.getFinalDestinationName(),
											car.getFinalDestinationTrackName() }));
									// only set car's destination if specific train can service car
									if (finshSettingRouteFor(car, nlt))
										return true; // done 5 train routing
									foundRoute = true;
									continue; // there was an issue with the first stop in the route
								}
							}
						}
					}
				}
			}
		}
		log.debug("Using 5 trains to route car to (" + car.getFinalDestinationName() + ") was unsuccessful");
		return foundRoute;
	}

	private boolean finshSettingRouteFor(Car car, Track track) {
		// only set car's destination if specific train can service car
		Car ts2 = clone(car);
		ts2.setDestinationTrack(track);
		String specific = canSpecificTrainService(ts2);
		if (specific.equals(NO)) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("TrainDoesNotServiceCar"),
					new Object[] { _train.getName(), car.toString(), track.getLocation().getName(), track.getName() }));
			_status = STATUS_NOT_THIS_TRAIN;
			return true;
		} else if (specific.equals(NOT_NOW)) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterTrainCanNotDueTo"),
					new Object[] { _train.getName(), car.toString(), track.getLocation().getName(), track.getName(),
							_train.getServiceStatus() }));
			return true; // the issue is route moves or train length
		}
		// check to see if track is staging
		if (track.getTrackType().equals(Track.STAGING))
			_status = car.setDestination(track.getLocation(), null);
		else
			_status = car.setDestination(track.getLocation(), track);
		if (!_status.equals(Track.OKAY)) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCanNotDeliverCar"),
					new Object[] { car.toString(), track.getLocation().getName(), track.getName(), _status }));
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
		if (car.getKernel() != null)
			clone.setLength(Integer.toString(car.getKernel().getTotalLength() - RollingStock.COUPLER));
		clone.setTrack(car.getTrack());
		clone.setFinalDestination(car.getFinalDestination());
		// don't set the clone's final destination track, that will record the cars as being inbound
		// next two items is where the clone is different
		clone.setDestination(car.getFinalDestination());  // note that final destination track can be null
		clone.setDestinationTrack(car.getFinalDestinationTrack());
		return clone;
	}

	private void loadTracks(Car car, Car testCar, List<Track> tracks) {
		for (int i = 0; i < tracks.size(); i++) {
			Track track = tracks.get(i);
			if (track == car.getTrack())
				continue; // don't use car's current track
			String status = track.accepts(testCar);
			if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH))
				continue; // track doesn't accept this car
			if (debugFlag)
				log.debug("Found " + track.getTrackType() + " track (" + track.getLocation().getName() + ", "
						+ track.getName() + ") for car (" + car.toString() + ")"); // NOI18N
			// test to see if there's a train that can deliver the car to this destination
			testCar.setDestinationTrack(track);
			Train train = null;
			String specific = canSpecificTrainService(testCar);
			if (specific.equals(YES) || specific.equals(NOT_NOW)) {
				train = _train;
			} else {
				train = TrainManager.instance().getTrainForCar(testCar, null); // don't add to report
			}
			// Can specific train carry this car out of staging?
			if (car.getTrack().getTrackType().equals(Track.STAGING) && !specific.equals(YES))
				train = null;
			// is the option to car by specific enabled?
			if (train != null && _train != null && _train.isServiceAllCarsWithFinalDestinationsEnabled()
					&& !specific.equals(YES)) {
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterOptionToCarry"),
						new Object[] { train.getName(), car.toString(), track.getLocation().getName(), track.getName() }));
				train = null;
			}
			if (train != null) {
				if (debugFlag)
					log.debug("Train (" + train.getName() + ") can service car (" + car.toString() + ") from "
							+ track.getTrackType() // NOI18N
							+ " (" // NOI18N
							+ testCar.getLocationName() + ", " + testCar.getTrackName() // NOI18N
							+ ") to final destination (" + testCar.getDestinationName() // NOI18N
							+ ", " + testCar.getDestinationTrackName() + ")");
				// note that last could equal next if this routine was used for two train routing
				if (!_lastLocationTracks.contains(track))
					_nextLocationTracks.add(track);
			} else {
				// don't add to other if already in last location list
				if (!_lastLocationTracks.contains(track)) {
					if (debugFlag)
						log.debug("Adding location (" + track.getLocation().getName() + ", " + track.getName()
								+ ") to other locations"); // NOI18N
					_otherLocationTracks.add(track);
				}
			}
		}
	}

	private static final String NO = "no"; // NOI18N
	private static final String YES = "yes"; // NOI18N
	private static final String NOT_NOW = "not now"; // NOI18N
	private static final String NO_SPECIFIC_TRAIN = "no specific train"; // NOI18N

	private String canSpecificTrainService(Car car) {
		if (_train == null)
			return NO_SPECIFIC_TRAIN;
		if (_train.services(car))
			return YES;
		// is the reason this train can't service route moves or train length?
		else if (!_train.getServiceStatus().equals(""))
			return NOT_NOW; // the issue is route moves or train length
		return NO;
	}

	static Logger log = LoggerFactory.getLogger(Router.class.getName());

}
