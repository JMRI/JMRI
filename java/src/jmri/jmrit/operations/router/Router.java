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
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Router for car movement. This code attempts to find a way to move a car to its final destination through the use of
 * two or more trains. Code first tries to move car using a single train. If that fails, attempts are made to use two
 * trains via an interchange track , then a yard. Next attempts are made using three or more trains using any
 * combination of interchanges and yards. Currently the router is limited to five trains.
 * 
 * @author Daniel Boudreau Copyright (C) 2010, 2011, 2012, 2013
 * @version $Revision$
 */

public class Router extends TrainCommon {

	private List<Track> firstLocationTracks = new ArrayList<Track>();
	private List<Track> lastLocationTracks = new ArrayList<Track>();
	private List<Track> otherLocationTracks = new ArrayList<Track>();

	private static final String STATUS_NOT_THIS_TRAIN = Bundle.getMessage("RouterTrain");
	private static final String STATUS_NOT_ABLE = Bundle.getMessage("RouterNotAble");
	private static final String STATUS_CAR_AT_DESINATION = Bundle.getMessage("RouterCarAtDestination");
	private static final String STATUS_NO_TRAINS = Bundle.getMessage("RouterNoTrains");
	private static final String STATUS_ROUTER_DISABLED = Bundle.getMessage("RouterDisabled");

	private String _status = "";
	private Train _train = null;
	PrintWriter _buildReport = null; // build report

	public boolean enable_yard_search = false; // search for yard track even if an interchange track was found
	private static boolean debugFlag = false;

	protected static final String SEVEN = Setup.BUILD_REPORT_VERY_DETAILED;
	private boolean addtoReport = false;

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
		addtoReport = !Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL) 
				&& !Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_MINIMAL);
		log.debug("Car (" + car.toString() + ") at location (" + car.getLocationName() + ", "
				+ car.getTrackName() + ") " + "final destination (" + car.getFinalDestinationName() // NOI18N
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
		// Note the following test doesn't check for car length which is what we want! Also ignores if track has a
		// schedule.
		_status = clone.testDestination(clone.getDestination(), clone.getDestinationTrack());
		if (!_status.equals(Track.OKAY)) {
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterNextDestFailed"),
					new Object[] { car.getFinalDestinationName(), car.getFinalDestinationTrackName(), car.toString(),
							_status }));
			return false;
		}
		// check to see if car will move to destination using a single train
		boolean trainServicesCar = false;	// specific train
		Train testTrain = null;				// any train
		if (_train != null)
			trainServicesCar = _train.services(buildReport, clone);
		if (trainServicesCar)
			testTrain = _train;	// use the specific train
		// can specific train can service car out of staging
		if (car.getTrack().getLocType().equals(Track.STAGING) && _train != null && !trainServicesCar) {
			log.debug("Car (" + car.toString() + ") destination (" + clone.getDestinationName() + ", "
					+ clone.getDestinationTrackName() + ") is not serviced by train (" // NOI18N
					+ _train.getName() + ") out of staging"); // NOI18N
		} else if (!trainServicesCar) {
			testTrain = TrainManager.instance().getTrainForCar(clone, _buildReport);
		}
		if (testTrain != null) {
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCarSingleTrain"),
					new Object[] { testTrain.getName(), car.toString(), clone.getDestinationName(),
							clone.getDestinationTrackName() }));
			// now check to see if specific train can service car directly
			if (_train != null && !trainServicesCar) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("TrainDoesNotServiceCar"),
						new Object[] { _train.getName(), car.toString(), clone.getDestinationName(),
								clone.getDestinationTrackName() }));
				_status = STATUS_NOT_THIS_TRAIN;
				return true; // car can be routed, but not by this train!
			}
			_status = car.setDestination(clone.getDestination(), clone.getDestinationTrack());
			if (_status.equals(Track.OKAY)) {
				return true; // done, car has new destination
			}
			addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCanNotDeliverCar"),
					new Object[] { car.toString(), clone.getDestinationName(),
				clone.getDestinationTrackName(), _status }));
			// TODO should we move a car to the alternate or yard track if already at the final destination?
			// state that alternative and yard track options are not available if car is at final destination
			if (car.getLocation() == clone.getDestination())
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterIgnoreAlternate"),
						new Object[] { car.toString(), car.getLocationName() }));
			// check to see if an alternative track was specified
			if ((_status.startsWith(Track.LENGTH) || _status.startsWith(Track.SCHEDULE)) && car.getLocation() != clone.getDestination()
					&& clone.getDestinationTrack() != null
					&& clone.getDestinationTrack().getAlternateTrack() != null) {
				String status = car.setDestination(clone.getDestination(), clone.getDestinationTrack()
						.getAlternateTrack());
				if (status.equals(Track.OKAY)) {
					if (_train == null || _train.services(car)) {
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("RouterSendCarToAlternative"), new Object[] { car.toString(),
							clone.getDestinationTrack().getAlternateTrack().getName(),
							clone.getDestination().getName() }));
						return true;
					}
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("RouterNotSendCarToAlternative"), new Object[] {
						_train.getName(), car.toString(),
						clone.getDestinationTrack().getAlternateTrack().getName(),
						clone.getDestination().getName() }));
				} else {
					addLine(buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("RouterAlternateFailed"), new Object[] {
						clone.getDestinationTrack().getAlternateTrack().getName(), status }));
				}
			}
			// check to see if spur was full, if so, forward to yard if possible
			if (Setup.isForwardToYardEnabled() && _status.startsWith(Track.LENGTH)
					&& car.getLocation() != clone.getDestination()) {
				// log.debug("Spur full, searching for a yard at destination ("+clone.getDestinationName()+")");
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterSpurFull"),
						new Object[] { clone.getDestinationTrackName(), clone.getDestinationName() }));
				Location dest = clone.getDestination();
				List<String> yards = dest.getTrackIdsByMovesList(Track.YARD);
				log.debug("Found " + yards.size() + " yard(s) at destination ("
						+ clone.getDestinationName() + ")");
				for (int i = 0; i < yards.size(); i++) {
					Track track = dest.getTrackById(yards.get(i));
					String status = car.setDestination(dest, track);
					if (status.equals(Track.OKAY)) {
						if (_train != null && !_train.services(car)) {
							log.debug("Train (" + _train.getName() + ") can not deliver car ("
									+ car.toString() + ") to yard (" + track.getName() + ")"); // NOI18N
							continue;
						}
						addLine(buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("RouterSendCarToYard"), new Object[] { car.toString(),
							track.getName(), dest.getName() }));
						return true;
					}
				}
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterNoYardTracks"),
						new Object[] { dest.getName(), car.toString() }));
			}
			car.setDestination(null, null);
			return true; // able to route, but not able to set the car's destination
		} else if (Setup.isCarRoutingEnabled()) {
			log.debug("Car (" + car.toString() + ") final destination (" + car.getFinalDestinationName()
					+ ") is not served by a single train"); // NOI18N
			// was the request for a local move?
			if (car.getLocationName().equals(car.getFinalDestinationName())) {
				addLine(buildReport, SEVEN, MessageFormat.format(Bundle
						.getMessage("RouterCouldNotFindTrain"), new Object[] {
					car.getLocationName(), car.getTrackName(), car.getFinalDestinationName(), car.getFinalDestinationTrackName() }));
				_status = STATUS_NO_TRAINS;
				return false; // maybe next time
			}
			if (addtoReport)
				addLine(_buildReport, SEVEN, MessageFormat
						.format(Bundle.getMessage("RouterBeginTwoTrain"), new Object[] { car.toString(),
								car.getLocationName(), car.getFinalDestinationName() }));
			firstLocationTracks.clear();
			lastLocationTracks.clear();
			otherLocationTracks.clear();
			// first try using 2 trains and an interchange track to route the car
			if (setCarDestinationInterchange(car)) {
				if (enable_yard_search && _status.equals(STATUS_NOT_THIS_TRAIN)) {
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
		log.debug("Find " + trackType + " track for car (" + car.toString() + ") final destination ("
				+ testCar.getDestinationName() + ", " // NOI18N
				+ testCar.getDestinationTrackName() + ")");
		if (addtoReport)
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterFindTrack"),
					new Object[] { trackType, car.toString(), testCar.getDestinationName(),
							testCar.getDestinationTrackName() }));
		// save car's location, track, destination, and destination track
		Location saveLocation = testCar.getLocation();
		Track saveTrack = testCar.getTrack();
		Location saveDestation = testCar.getDestination();
		Track saveDestTrack = testCar.getDestinationTrack();

		// setup the test car with the save location and destination
		if (saveTrack == null) {
			log.debug("Car's track is null! Can't route");
			return false;
		}
		boolean foundRoute = false;
		// now search for a yard or interchange that a train can pick up and deliver the car to its destination
		List<Track> tracks = LocationManager.instance().getTracks(trackType);// restrict to yards, interchanges, or staging
		for (int i = 0; i < tracks.size(); i++) {
			Track track = tracks.get(i);
			String status = track.accepts(testCar);
			if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH))
				continue;
			if (saveTrack == track)
				continue;	// don't use car's current track
			if (debugFlag)
				log.debug("Found " + trackType + " track (" + track.getLocation().getName() + ", "
						+ track.getName() + ") for car (" + car.toString() + ")"); // NOI18N
			if (addtoReport)
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterFoundTrack"),
						new Object[] { trackType, track.getLocation().getName(), track.getName(),
								car.toString() }));
			// test to see if there's a train that can deliver the car to its final location
			testCar.setLocation(track.getLocation());
			testCar.setTrack(track);
			testCar.setDestination(saveDestation);
			testCar.setDestinationTrack(saveDestTrack);
			Train nextTrain = TrainManager.instance().getTrainForCar(testCar, _buildReport);
			if (nextTrain == null) {
				if (debugFlag)
					log.debug("Could not find a train to service car from " + trackType + " ("
							+ track.getLocation().getName() + ", " + track.getName() + ") to destination ("	// NOI18N
							+ testCar.getDestinationName() + " ," + testCar.getDestinationTrackName()+")"); // NOI18N
				if (addtoReport)
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("RouterNotFindTrain"), new Object[] { trackType,
							track.getLocation().getName(), track.getName(), testCar.getDestinationName(),
							testCar.getDestinationTrackName() }));
				continue;
			}
			if (debugFlag)
				log.debug("Train (" + nextTrain.getName() + ") can service car ("
						+ car.toString() + ") from " + trackType	// NOI18N
						+ " (" // NOI18N
						+ testCar.getLocationName() + ", " + testCar.getTrackName()	// NOI18N
						+ ") to final destination (" + testCar.getDestinationName() // NOI18N
						+ ", " + testCar.getDestinationTrackName() + ")");
			if (addtoReport)
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
						.getMessage("RouterTrainCanTransport"), new Object[] { nextTrain.getName(),
						car.toString(), trackType, testCar.getLocationName(), testCar.getTrackName(),
						testCar.getDestinationName(), testCar.getDestinationTrackName() }));
			// Save the "last" tracks for later use
			lastLocationTracks.add(track);
			// now try to forward car to this interim location
			testCar.setLocation(saveLocation); // restore car's location and track
			testCar.setTrack(saveTrack);
			testCar.setDestination(track.getLocation()); // forward test car to this interim destination and track
			testCar.setDestinationTrack(track);
			// determine if car can be transported from current location to this yard or interchange
			// Is there a "first" train for this car out of staging?
			if (car.getTrack().getLocType().equals(Track.STAGING) && _train != null
					&& !_train.services(testCar)) {
				if (debugFlag)
					log.debug("Train (" + _train.getName() + ") can not deliver car to ("
							+ track.getLocation().getName() + ", " // NOI18N
							+ track.getName() + ")");
				if (addtoReport)
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterTrainCanNot"),
							new Object[] { _train.getName(), car.toString(), car.getLocationName(),
									car.getTrackName(), track.getLocation().getName(), track.getName() }));
				continue; // can't use this train
			}
			// find a train that will transport the car to the interim track
			Train firstTrain = null;
			if (_train != null && _train.services(testCar))
				firstTrain = _train;
			else
				firstTrain = TrainManager.instance().getTrainForCar(testCar, _buildReport);
			if (firstTrain != null) {
				foundRoute = true;	// found a route
				// found a two train route for this car, show the car's route
				addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
						.getMessage("RouterRoute2ForCar"), new Object[] { car.toString(),
					car.getLocationName(), car.getTrackName(), testCar.getDestinationName(),
					testCar.getDestinationTrackName(), car.getFinalDestinationName(),
					car.getFinalDestinationTrackName() }));
				status = car.testDestination(track.getLocation(), track);
				if (status.startsWith(Track.LENGTH)) {
					// if the issue is length at the interim track, add message to build report
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("RouterCanNotDeliverCar"), new Object[] { car.toString(),
							track.getLocation().getName(), track.getName(), status }));
					continue;
				}
				if (status.equals(Track.OKAY)) {
					// only set car's destination if specific train can service car
					if (_train != null && _train != firstTrain) {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("TrainDoesNotServiceCar"), new Object[] { _train.getName(),
								car.toString(), testCar.getDestinationName(),
								testCar.getDestinationTrackName() }));
						_status = STATUS_NOT_THIS_TRAIN;
						continue;// found a route but it doesn't start with the specific train
					}
					// check to see if intermediate track is staging
					if (track.getLocType().equals(Track.STAGING))
						_status = car.setDestination(track.getLocation(), null); // don't specify which track in staging is to be used, decide later
					else
						_status = car.setDestination(track.getLocation(), track); // forward car to this intermediate destination and track.
					if (debugFlag)
						log.debug("Train (" + firstTrain.getName() + ") can service car ("
								+ car.toString()
								+ ") from current location (" // NOI18N
								+ car.getLocationName() + ", " + car.getTrackName() + ") to "	// NOI18N
								+ trackType + " (" + track.getLocation().getName() // NOI18N
								+ ", " + track.getName() + ")"); // NOI18N
					if (addtoReport)
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("RouterTrainCanService"), new Object[] { firstTrain.getName(),
								car.toString(), car.getLocationName(), car.getTrackName(), trackType,  
								track.getLocation().getName(), track.getName() }));
					return true;	// the specific train and another train can carry the car to its destination
				}
			}
		}
		return foundRoute;
	}

	/*
	 * Note that "last" set of location/tracks was loaded by setCarDestinationTwoTrains. The following code builds two
	 * additional sets of location/tracks called "first" and "other". "first" is the first set of location/tracks that
	 * the car can reach by a single train. "last" is the last set of location/tracks that services the cars final
	 * destination. And "other" is the remaining sets of location/tracks that are not "first" or "last". The code then
	 * tries to connect the "first" and "last" location/track sets with a train that can service the car. If successful,
	 * that would be a three train route for the car. If not successful, the code than tries combinations of "first",
	 * "other" and "last" location/tracks to create a route for the car.
	 */
	private boolean setCarDestinationMultipleTrains(Car car) {
		if (lastLocationTracks.size() == 0) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCouldNotFind"), new Object[] {car.getFinalDestinationName()}));
			return false;
		}
		
		addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterMultipleTrains"), new Object[] {car.getFinalDestinationName()}));
		Car testCar = clone(car); // reload
		// build the "first" and "other" location/tracks
		// start with interchanges
		List<Track> tracks = LocationManager.instance().getTracks(Track.INTERCHANGE);
		loadTracks(car, testCar, tracks);
		// next load yards if enabled
		if (Setup.isCarRoutingViaYardsEnabled()) {
			tracks = LocationManager.instance().getTracks(Track.YARD);
			loadTracks(car, testCar, tracks);
		}
		// now staging if enabled
		if (Setup.isCarRoutingViaStagingEnabled()) {
			tracks = LocationManager.instance().getTracks(Track.STAGING);
			loadTracks(car, testCar, tracks);	
		}
		if (firstLocationTracks.size() == 0) {
			addLine(_buildReport, SEVEN, MessageFormat.format(Bundle.getMessage("RouterCouldNotFindLoc"), new Object[] {car.getLocationName()}));
			return false;
		}
		// tracks that could be the very next destination for the car
		for (int i = 0; i < firstLocationTracks.size(); i++) {
			Track ltp = firstLocationTracks.get(i);
			log.debug("First location (" + ltp.getLocation().getName() + ", " + ltp.getName()
					+ ") can service car (" + car.toString() + ")"); // NOI18N
		}
		// tracks that could be the next to last destination for the car
		for (int i = 0; i < lastLocationTracks.size(); i++) {
			Track ltp = lastLocationTracks.get(i);
			log.debug("Last location (" + ltp.getLocation().getName() + ", " + ltp.getName()
					+ ") can service car (" + car.toString() + ")"); // NOI18N
		}
		// tracks that are not the first or the last
		for (int i = 0; i < otherLocationTracks.size(); i++) {
			Track ltp = otherLocationTracks.get(i);
			log.debug("Other location (" + ltp.getLocation().getName() + ", " + ltp.getName()
					+ ") may be needed to service car (" + car.toString() + ")"); // NOI18N
		}
		log.debug("Try to find route using 3 trains");
		for (int i = 0; i < firstLocationTracks.size(); i++) {
			Track fltp = firstLocationTracks.get(i);
			testCar.setLocation(fltp.getLocation()); // set car to this location and track
			testCar.setTrack(fltp);
			for (int j = 0; j < lastLocationTracks.size(); j++) {
				Track lltp = lastLocationTracks.get(j);
				testCar.setDestination(lltp.getLocation()); // set car to this destination and track
				testCar.setDestinationTrack(lltp);
				// does a train service these two locations?
				Train middleTrain = TrainManager.instance().getTrainForCar(testCar, null);	// don't add to report
				if (middleTrain != null) {
					// check to see if track is staging
					if (testCar.getTrack().getLocType().equals(Track.STAGING))
						testCar.setTrack(null); // don't specify which track in staging is to be used, decide later
					log.debug("Found 3 train route, setting car destination ("
							+ testCar.getLocationName() + ", " + testCar.getTrackName() + ")");
					// show the route
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("RouterRoute3ForCar"), new Object[] { car.toString(),
						car.getLocationName(), car.getTrackName(), testCar.getLocationName(),
						testCar.getTrackName(), testCar.getDestinationName(),
						testCar.getDestinationTrackName(), car.getFinalDestinationName(),
						car.getFinalDestinationTrackName() }));
					// only set car's destination if specific train can service car
					Car ts2 = clone(car);
					ts2.setDestination(fltp.getLocation());
					ts2.setDestinationTrack(fltp);
					if (_train != null && !_train.services(ts2)) {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("TrainDoesNotServiceCar"), new Object[] { _train.getName(),
							car.toString(), fltp.getLocation().getName(), fltp.getName() }));
						_status = STATUS_NOT_THIS_TRAIN;
						return true;
					}
					_status = car.setDestination(testCar.getLocation(), testCar.getTrack());
					if (!_status.equals(Track.OKAY)) {
						addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
								.getMessage("RouterCanNotDeliverCar"), new Object[] { car.toString(),
							testCar.getLocation().getName(), testCar.getTrackName(), _status }));
					}
					return true; // done 3 train routing
				}
			}
		}
		log.debug("Using 3 trains to route car to ("+car.getFinalDestinationName()+") was unsuccessful");
		log.debug("Try to find route using 4 trains");
		for (int i = 0; i < firstLocationTracks.size(); i++) {
			Track fltp = firstLocationTracks.get(i);
			for (int j = 0; j < otherLocationTracks.size(); j++) {
				Track mltp = otherLocationTracks.get(j);
				testCar.setLocation(fltp.getLocation()); // set car to this location and track
				testCar.setTrack(fltp);
				testCar.setDestination(mltp.getLocation()); // set car to this destination and track
				testCar.setDestinationTrack(mltp);
				// does a train service these two locations?
				Train middleTrain2 = TrainManager.instance().getTrainForCar(testCar, null);	// don't add to report
				if (middleTrain2 != null) {
					if (debugFlag)
						log.debug("Train 2 (" + middleTrain2.getName() + ") services car from "
								+ testCar.getLocationName() + " to " + testCar.getDestinationName() // NOI18N
								+ ", " + testCar.getDestinationTrackName());
					for (int k = 0; k < lastLocationTracks.size(); k++) {
						Track lltp = lastLocationTracks.get(k);
						testCar.setLocation(mltp.getLocation()); // set car to this location and track
						testCar.setTrack(mltp);
						testCar.setDestination(lltp.getLocation()); // set car to this destination and track
						testCar.setDestinationTrack(lltp);
						Train middleTrain3 = TrainManager.instance().getTrainForCar(testCar, null); // don't add to report
						if (middleTrain3 != null) {
							if (debugFlag)
								log.debug("Train 3 (" + middleTrain3.getName() + ") services car from "
										+ testCar.getLocationName() // NOI18N
										+ " to " + testCar.getDestinationName() + ", " // NOI18N
										+ testCar.getDestinationTrackName());
							// show the route
							addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
									.getMessage("RouterRoute4ForCar"), new Object[] { car.toString(),
								car.getLocationName(), car.getTrackName(), fltp.getLocation(),
								fltp.getName(), mltp.getLocation().getName(), mltp.getName(),
								lltp.getLocation().getName(), lltp.getName(),
								car.getFinalDestinationName(), car.getFinalDestinationTrackName() }));
							// only set car's destination if specific train can service car
							Car ts2 = clone(car);
							ts2.setDestination(fltp.getLocation());
							ts2.setDestinationTrack(fltp);
							if (_train != null && !_train.services(ts2)) {
								addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("TrainDoesNotServiceCar"), new Object[] {
									_train.getName(), car.toString(),
									fltp.getLocation().getName(), fltp.getName() }));
								_status = STATUS_NOT_THIS_TRAIN;
								return true;
							}
							// check to see if track is staging
							if (fltp.getLocType().equals(Track.STAGING))
								_status = car.setDestination(fltp.getLocation(), null);
							else
								_status = car.setDestination(fltp.getLocation(), fltp);
							log.debug("Found 4 train route, setting car destination ("
									+ fltp.getLocation().getName() + ", " + fltp.getName() + ")");
							if (!_status.equals(Track.OKAY)) {
								addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
										.getMessage("RouterCanNotDeliverCar"), new Object[] { car.toString(),
									fltp.getLocation().getName(), fltp.getName(), _status }));
							}
							return true; // done 4 train routing
						}
					}
				}
			}
		}
		log.debug("Using 4 trains to route car to ("+car.getFinalDestinationName()+") was unsuccessful");
		log.debug("Try to find route using 5 trains");
		for (int i = 0; i < firstLocationTracks.size(); i++) {
			Track fltp = firstLocationTracks.get(i);
			for (int j = 0; j < otherLocationTracks.size(); j++) {
				Track mltp1 = otherLocationTracks.get(j);
				testCar.setLocation(fltp.getLocation()); // set car to this location and track
				testCar.setTrack(fltp);
				testCar.setDestination(mltp1.getLocation()); // set car to this destination and track
				testCar.setDestinationTrack(mltp1);
				// does a train service these two locations?
				Train middleTrain2 = TrainManager.instance().getTrainForCar(testCar, null); // don't add to report
				if (middleTrain2 != null) {
					if (debugFlag)
						log.debug("Train 2 (" + middleTrain2.getName() + ") services car from "
								+ testCar.getLocationName() + " to " + testCar.getDestinationName() // NOI18N
								+ ", " + testCar.getDestinationTrackName());
					for (int k = 0; k < otherLocationTracks.size(); k++) {
						Track mltp2 = otherLocationTracks.get(k);
						testCar.setLocation(mltp1.getLocation()); // set car to this location and track
						testCar.setTrack(mltp1);
						testCar.setDestination(mltp2.getLocation()); // set car to this destination and track
						testCar.setDestinationTrack(mltp2);
						// does a train service these two locations?
						Train middleTrain3 = TrainManager.instance().getTrainForCar(testCar, null); // don't add to report
						if (middleTrain3 != null) {
							if (debugFlag)
								log.debug("Train 3 (" + middleTrain3.getName() + ") services car from "
										+ testCar.getLocationName() // NOI18N
										+ " to " + testCar.getDestinationName() + ", " // NOI18N
										+ testCar.getDestinationTrackName());
							for (int n = 0; n < lastLocationTracks.size(); n++) {
								Track lltp = lastLocationTracks.get(n);
								testCar.setLocation(mltp2.getLocation()); // set car to this location and track
								testCar.setTrack(mltp2);
								testCar.setDestination(lltp.getLocation()); // set car to this destination and track
								testCar.setDestinationTrack(lltp);
								// does a train service these two locations?
								Train middleTrain4 = TrainManager.instance().getTrainForCar(testCar, null); // don't add to report
								if (middleTrain4 != null) {
									if (debugFlag)
										log.debug("Train 4 ("
												+ middleTrain4.getName()
												+ ") services car from " // NOI18N
												+ testCar.getLocationName()
												+ " to " // NOI18N
												+ testCar.getDestinationName() + ", "
												+ testCar.getDestinationTrackName());
									// show the car's route
									addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
											.getMessage("RouterRoute5ForCar"), new Object[] {
										car.toString(), car.getLocationName(), car.getTrackName(),
										fltp.getLocation().getName(), fltp.getName(),
										mltp1.getLocation().getName(), mltp1.getName(),
										mltp2.getLocation().getName(), mltp2.getName(),
										lltp.getLocation().getName(), lltp.getName(),
										car.getFinalDestinationName(), car.getFinalDestinationTrackName() }));
									// only set car's destination if specific train can service car
									Car ts2 = clone(car);
									ts2.setDestination(fltp.getLocation());
									ts2.setDestinationTrack(fltp);
									if (_train != null && !_train.services(ts2)) {
										addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
												.getMessage("TrainDoesNotServiceCar"), new Object[] {
											_train.getName(), car.toString(),
											fltp.getLocation().getName(), fltp.getName() }));
										_status = STATUS_NOT_THIS_TRAIN;
										return true;
									}
									// check to see if track is staging

									if (fltp.getLocType().equals(Track.STAGING))					
										_status = car.setDestination(fltp.getLocation(), null);
									else
										_status = car.setDestination(fltp.getLocation(), fltp);
									log.debug("Found 5 train route, setting car destination ("
											+ fltp.getLocation().getName() + ", " + fltp.getName() + ")");
									if (!_status.equals(Track.OKAY)) {
										addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
												.getMessage("RouterCanNotDeliverCar"), new Object[] { car.toString(),
											fltp.getLocation().getName(), fltp.getName(), _status }));
									}
									return true; // done 5 train routing
								}
							}
						}
					}
				}
			}
		}
		log.debug("Using 5 trains to route car to ("+car.getFinalDestinationName()+") was unsuccessful");
		return false;
	}

	// sets clone car destination to final destination and track
	private Car clone(Car car) {
		Car clone = car.copy();
		// modify clone car length if car is part of kernel
		if (car.getKernel() != null)
			clone.setLength(Integer.toString(car.getKernel().getTotalLength()));
		clone.setLocation(car.getLocation());
		clone.setTrack(car.getTrack());
		clone.setFinalDestination(car.getFinalDestination());
		// don't set the clone's final destination track, that will record the cars as being inbound
		// next two items is where the clone is different
		clone.setDestination(car.getFinalDestination());
		clone.setDestinationTrack(car.getFinalDestinationTrack());
		return clone;
	}
	
	private void loadTracks(Car car, Car testCar, List<Track> tracks) {
		for (int i = 0; i < tracks.size(); i++) {
			Track track = tracks.get(i);
			if (track == car.getTrack())
				continue;	// don't use car's current track
			String status = track.accepts(testCar);
			if (!status.equals(Track.OKAY) && !status.startsWith(Track.LENGTH))
				continue;	// track doesn't accept this car
			if (debugFlag)
				log.debug("Found " + track.getLocType() + " track (" + track.getLocation().getName() + ", "
						+ track.getName() + ") for car (" + car.toString() + ")"); // NOI18N
			// test to see if there's a train that can deliver the car to this location
			testCar.setDestination(track.getLocation());
			testCar.setDestinationTrack(track);
			Train firstTrain = TrainManager.instance().getTrainForCar(testCar, null); // don't add to report
			// Is there a train assigned to carry this car out of staging?
			if (car.getTrack().getLocType().equals(Track.STAGING) && _train != null
					&& !_train.services(testCar))
				firstTrain = null;
			if (firstTrain != null) {
				if (debugFlag)
					log.debug("Train (" + firstTrain.getName() + ") can service car (" + car.toString()
							+ ") from " + track.getLocType() // NOI18N
							+ " (" // NOI18N
							+ testCar.getLocationName() + ", " + testCar.getTrackName() // NOI18N
							+ ") to final destination (" + testCar.getDestinationName() // NOI18N
							+ ", " + testCar.getDestinationTrackName() + ")");
				if (status.equals(Track.OKAY))
					firstLocationTracks.add(track);
				else
					addLine(_buildReport, SEVEN, MessageFormat.format(Bundle
							.getMessage("RouterCanNotDeliverCar"), new Object[] { car.toString(),
							track.getLocation().getName(), track.getName(), status }));
			} else {
				// don't add to other if already in last location list
				if (!lastLocationTracks.contains(track)) {
					if (debugFlag)
						log.debug("Adding location (" + track.getLocation().getName() + ", "
								+ track.getName() + ") to other locations"); // NOI18N
					otherLocationTracks.add(track);
				}
			}
		}
	}

	static Logger log = LoggerFactory.getLogger(Router.class.getName());

}
