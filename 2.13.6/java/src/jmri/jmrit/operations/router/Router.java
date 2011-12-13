package jmri.jmrit.operations.router;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationTrackPair;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Router for car movement. This code attempts to find a way to move a car to
 * its final destination through the use of two or more trains. Code first tries
 * to move car using a single train. If that fails, attempts are made to use two
 * trains via an interchange track , then a yard. Next attempts are made using
 * three or more trains using any combination of interchanges and yards.
 * Currently the router is limited to five trains.
 * 
 * @author Daniel Boudreau Copyright (C) 2010, 2011
 * @version $Revision$
 */

public class Router extends TrainCommon {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.router.JmritOperationsRouterBundle");
	
	private List<LocationTrackPair> firstLocationTrackPairs = new ArrayList<LocationTrackPair>();
	private List<LocationTrackPair> lastLocationTrackPairs = new ArrayList<LocationTrackPair>();
	private List<LocationTrackPair> otherLocationTrackPairs = new ArrayList<LocationTrackPair>();
	
	private String _status = "";
	private Train _train = null;
	PrintWriter _buildReport = null;	// build report
	
	public boolean enable_staging = false; 	// using staging to route cars can be tricky, not recommended
	private static boolean debugFlag = false;
	
	protected static final String SEVEN = Setup.BUILD_REPORT_VERY_DETAILED;
	
	/** record the single instance **/
	private static Router _instance = null;
	
	public static synchronized Router instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("Router creating instance");
			// create and load
			_instance = new Router();
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("Router returns instance "+_instance);
		return _instance;
	}
	
	/**
	 * Attempts to set the car's destination if a next destination exists.
	 * Only sets the car's destination if the train is part of the car's route.
	 * @param car the car to route
	 * @param train the first train to carry this car, can be null
	 * @param buildReport PrintWriter for build report, and can be null
	 * @return true if car can be routed.
	 */
	public boolean setDestination(Car car, Train train, PrintWriter buildReport){
		if (car.getTrack() == null || car.getNextDestination() == null)
			return false;
		_status = Track.OKAY;
		_train = train;
		_buildReport = buildReport;
		// Only keep train if car is departing staging
		/*
		if (!car.getTrack().getLocType().equals(Track.STAGING))
			_train = null;
		 */

		log.debug("Car ("+car.toString()+") at location ("+car.getLocationName()+", "+car.getTrackName()+") " +
				"next destination ("+car.getNextDestinationName()+", "+car.getNextDestTrackName()+") car routing begins");
		if (_train != null)
			log.debug("Routing using train ("+train.getName()+")");
		// Has the car arrived at the car's next destination?
		if (car.getLocation() != null && car.getLocation().equals(car.getNextDestination()) 
				&& (car.getTrack().equals(car.getNextDestTrack()) || car.getNextDestTrack() == null)){
			log.debug("Car ("+car.toString()+") has arrived at next destination");
			car.setNextDestination(null);
			car.setNextDestTrack(null);
			return false;
		}
		// is car part of kernel?
		if (car.getKernel() != null && !car.getKernel().isLead(car))
			return false;
		// note clone car has the car "next destination" as its destination
		Car clone = clone(car);
		//Note the following test doesn't check for car length which is what we want!
		_status = clone.testDestination(clone.getDestination(), clone.getDestinationTrack());
		if (!_status.equals(Track.OKAY)){
			//log.info("Next destination ("+car.getNextDestinationName()+", "+car.getNextDestTrackName()+"+) failed for car ("+car.toString()+") due to "+_status);
			addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("RouterNextDestFailed"),new Object[]{car.getNextDestinationName(),car.getNextDestTrackName(),car.toString(),_status}));
			return false;
		}
		// check to see if car will move with new destination and a single train
		Train testTrain = TrainManager.instance().getTrainForCar(clone);
		// check to see if specific train can service car out of staging
		if (_train != null && !_train.servicesCar(clone) && car.getTrack().getLocType().equals(Track.STAGING)){
			log.debug("Car ("+car.toString()+") destination ("+clone.getDestinationName()+", "+clone.getDestinationTrackName()
					+") is not serviced by train ("+_train.getName()+") out of staging");
			testTrain = null;
		}
		if (testTrain != null){
			addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("RouterCarSingleTrain"),new Object[]{car.toString(),clone.getDestinationName(),clone.getDestinationTrackName(),testTrain.getName()}));
			// now check to see if specific train can service car directly
			if (_train != null && !_train.servicesCar(clone)){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("TrainDoesNotServiceCar"),new Object[]{_train.getName(), car.toString(), (clone.getDestinationName()+", "+clone.getDestinationTrackName())}));
				return true;	// car can be routed, but not by this train!
			}
			_status = car.setDestination(clone.getDestination(), clone.getDestinationTrack());
			if (!_status.equals(Track.OKAY)){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("RouterCanNotDeliverCar"),new Object[]{car.toString(), clone.getDestinationName(), clone.getDestinationTrackName(), _status}));
				// check to see if an alternative track was specified
				if (_status.contains(Track.LENGTH) && car.getLocation() != clone.getDestination() 
						&& clone.getDestinationTrack() != null && clone.getDestinationTrack().getAlternativeTrack() != null){
					String status = car.setDestination(clone.getDestination(), clone.getDestinationTrack().getAlternativeTrack());
					if (status.equals(Track.OKAY)){
						if (_train == null || (_train != null && _train.servicesCar(car))){
							addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("RouterSendCarToAlternative"),new Object[]{car.toString(), clone.getDestinationTrack().getAlternativeTrack().getName(), clone.getDestination().getName()}));
							return true;
						}
						addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("RouterNotSendCarToAlternative"),new Object[]{_train.getName(), car.toString(), clone.getDestinationTrack().getAlternativeTrack().getName(), clone.getDestination().getName()}));
					}
				}
				// check to see if spur was full, if so, forward to yard if possible
				if (Setup.isForwardToYardEnabled() && _status.contains(Track.LENGTH) && car.getLocation() != clone.getDestination()){
					//log.debug("Siding full, searching for a yard at destination ("+clone.getDestinationName()+")");
					addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("RouterSidingFull"),new Object[]{clone.getDestinationName()}));
					Location dest = clone.getDestination();
					List<String> yards = dest.getTracksByMovesList(Track.YARD);
					log.debug("Found "+yards.size()+" yard(s) at destination ("+clone.getDestinationName()+")");
					for (int i=0; i<yards.size(); i++){
						Track track = dest.getTrackById(yards.get(i));
						String status = car.setDestination(dest, track);
						if (status.equals(Track.OKAY)){
							if (_train != null && !_train.servicesCar(car)){
								log.debug("Train ("+_train.getName()+") can not deliver car ("+car.toString()+") to yard ("+track.getName()+")");
								continue;
							}
							addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("RouterSendCarToYard"),new Object[]{car.toString(), track.getName(), dest.getName()}));
							return true;
						}
					}
				}
				car.setDestination(null, null);
				return false;
			}
			return true;
		} else if (Setup.isCarRoutingEnabled()) {
			log.debug("Car ("+car.toString()+") next destination ("+car.getNextDestinationName()+") is not served by a single train");
			firstLocationTrackPairs.clear();
			lastLocationTrackPairs.clear();
			otherLocationTrackPairs.clear();
			// first try using 2 trains and an interchange track to route the car
			if (setCarDestinationInterchange(car)){
				log.debug("Was able to find route via interchange ("+car.getDestinationName()+", "+car.getDestinationTrackName()+") for car ("+car.toString()+")");
				// now try 2 trains and a yard track
			} else if (setCarDestinationYard(car)){
				log.debug("Was able to find route via yard ("+car.getDestinationName()+", "+car.getDestinationTrackName()+") for car ("+car.toString()+")");
				// now try 2 trains and a staging track
			} else if (setCarDestinationStaging(car)){
				log.debug("Was able to find route via staging ("+car.getDestinationName()+", "+car.getDestinationTrackName()+") for car ("+car.toString()+")");
				// now try 3 or more trains to route car
			} else if (setCarDestinationMultipleTrains(car)){
				log.debug("Was able to find multiple train route for car ("+car.toString()+")");
			} else {
				log.debug("Wasn't able to set route for car ("+car.toString()+")");
				_status = "not able to route car";
				return false;	// maybe next time
			}
		} else {
			log.warn("Car ("+car.toString()+") next destination ("+car.getNextDestinationName()+") is not served directly by any train");
			car.setNextDestination(null);
			car.setNextDestTrack(null);
			return false;
		}
		return true; // car's destination has been set
	}
		
	
	public String getStatus(){
		return _status;
	}

	/**
	 * Sets a car's next destination to an interchange track if two trains can
	 * route the car.
	 * 
	 * @param car the car to be routed
	 * @return true if car's destination has been modified to an interchange.
	 *         False if an interchange track wasn't found that could service the
	 *         car's final destination.
	 */
	private boolean setCarDestinationInterchange(Car car){
		return setCarDestinationTwoTrains (car, Track.INTERCHANGE);
	}
	
	/**
	 * Sets a car's next destination to a yard track if two train can route the
	 * car.
	 * 
	 * @param car the car to be routed
	 * @return true if car's destination has been modified to a yard. False if a
	 *         yard track wasn't found that could service the car's final
	 *         destination.
	 */
	private boolean setCarDestinationYard(Car car){
		return setCarDestinationTwoTrains (car, Track.YARD);
	}
	
	/**
	 * Sets a car's next destination to a staging track if two train can route the
	 * car.
	 * 
	 * @param car the car to be routed
	 * @return true if car's destination has been modified to a staging track. False if a
	 *         staging track wasn't found that could service the car's final
	 *         destination.
	 */
	private boolean setCarDestinationStaging(Car car){
		enable_staging = Setup.isCarRoutingViaStagingEnabled();
		if (enable_staging)
			return setCarDestinationTwoTrains (car, Track.STAGING);
		return false;
	}
	
	private boolean setCarDestinationTwoTrains(Car car, String trackType){
		if (!Setup.isCarRoutingEnabled()){
			log.debug("Car routing is disabled");
			return false;
		}
		Car ts = clone(car);	// reload
		log.debug("Find an "+trackType+" track for car ("+car.toString()+") final destination ("+ts.getDestinationName()+", "+ts.getDestinationTrackName()+")");

		// save car's location, track, destination, and destination track
		Location saveLocation = ts.getLocation();
		Track saveTrack = ts.getTrack();
		Location saveDestation = ts.getDestination();
		Track saveDestTrack = ts.getDestinationTrack();
		
		// setup the test car with the save location and destination
		if (saveTrack == null){
			log.debug("Car's track is null! Can't route");
			return false;
		}
		// now search for a yard or interchange that a train can pick up and deliver the car to its destination
		List<String> locations = LocationManager.instance().getLocationsByIdList();
		for (int i=0; i<locations.size(); i++){
			Location location = LocationManager.instance().getLocationById(locations.get(i));
			List<String> tracks = location.getTracksByNameList(trackType);	// restrict to yards, interchanges, or staging
			for (int j=0; j<tracks.size(); j++){
				Track track = location.getTrackById(tracks.get(j));
				if (ts.testLocation(location, track).equals(Track.OKAY)){
					if (debugFlag)
						log.debug("Found "+trackType+" track ("+location.getName()+", "+track.getName()+") for car ("+car.toString()+")");
					// test to see if there's a train that can deliver the car to its final location
					ts.setLocation(location);
					ts.setTrack(track);
					Train nextTrain = TrainManager.instance().getTrainForCar(ts);
					if (nextTrain != null){
						if (debugFlag)
							log.debug("Train ("+nextTrain.getName()+") can service car ("+car.toString()+") from "+trackType+" ("+ts.getLocationName()+", "+ts.getTrackName()+") to final destination ("+ts.getDestinationName()+", "+ts.getDestinationTrackName()+")");
						// Save the "last" location/track pairs for later use
						lastLocationTrackPairs.add(new LocationTrackPair(location, track));
						ts.setLocation(saveLocation); // restore car's location and track
						ts.setTrack(saveTrack);	
						ts.setDestination(location); // forward test car to this intermediate destination and track
						ts.setDestinationTrack(track);
						// test to see if car can be transported from current location to this yard or interchange
						Train firstTrain = TrainManager.instance().getTrainForCar(ts);
						String status = ts.testDestination(location, track);
						// Is there a "first" train for this car out of staging?
						if (_train != null && !_train.servicesCar(ts) && car.getTrack().getLocType().equals(Track.STAGING))
							firstTrain = null;	// can't use this train
						if (status.equals(Track.OKAY) && firstTrain != null){
							addLine(_buildReport, SEVEN, MessageFormat.format(rb.getString("RouterRoute2ForCar"),new Object[]{car.toString(),car.getLocationName(),ts.getDestinationName(),car.getNextDestinationName()}));
							// only set car's destination if specific train can service car
							if (_train != null && !_train.servicesCar(ts)){
								addLine(_buildReport, SEVEN, MessageFormat.format(rb.getString("TrainDoesNotServiceCar"),new Object[]{_train.getName(), car.toString(), (ts.getDestinationName()+", "+ts.getDestinationTrackName())}));
								return true;
							}
							// check to see if intermediate track is staging
							if (track.getLocType().equals(Track.STAGING))
								track = null;	// don't specify which track in staging is to be used, decide later
							car.setDestination(location, track);  // forward car to this intermediate destination and track.
							if (debugFlag)
								log.debug("Train ("+firstTrain.getName()+") can service car ("+car.toString()+") from current location ("+car.getLocationName()+", "+car.getTrackName()+") to "+trackType+" ("+car.getDestinationName()+", "+car.getDestinationTrackName()+")");
							return true;
						}
						// restore car's destination
						ts.setDestination(saveDestation);
						ts.setDestinationTrack(saveDestTrack);
					} else if (debugFlag){
						log.debug("Could not find a train to service car from "+trackType+" ("+location.getName()+", "+track.getName()+") to destination ("+car.getNextDestinationName()+")");
					}
				}
			}
		}
		return false;	// couldn't find two trains to service this car
	}

	// Note that "last" set of location/track pairs was loaded by
	// setCarDestinationTwoTrains.
	// The following code builds two additional sets of location/track pairs called
	// "first" and "other".
	// "first" is the first set of location/track pairs that the car can reach
	// by a single train.
	// "last" is the last set of location/track pairs that services the cars
	// final destination.
	// And "other" is the remaining sets of location/track pairs that are not "first" or
	// "last".
	// The code then tries to connect the "first" and "last" track pair sets
	// with a train that can service the car.
	// If successful, that would be a three train route for the car. If not
	// successful, the code than tries combinations of "first", "other" and "last"
	// location/track pairs to create a route for the car.
	// 
	private boolean setCarDestinationMultipleTrains(Car car){
		if (lastLocationTrackPairs.size()== 0)
			return false;
		log.debug("Multiple train routing begins");
		Car ts = clone(car);	//reload
		// build the "first" and "other" location/track pairs
		List<String> locations = LocationManager.instance().getLocationsByIdList();
		for (int i=0; i<locations.size(); i++){
			Location location = LocationManager.instance().getLocationById(locations.get(i));
			List<String> tracks = location.getTracksByNameList(null);	
			for (int j=0; j<tracks.size(); j++){
				Track track = location.getTrackById(tracks.get(j));
				if ((track.getLocType().equals(Track.INTERCHANGE)
						|| track.getLocType().equals(Track.YARD) 
						|| (enable_staging && track.getLocType().equals(Track.STAGING)))
						&& ts.testLocation(location, track).equals(Track.OKAY)) {
					if (debugFlag)
						log.debug("Found "+track.getLocType()+" track ("+location.getName()+", "+track.getName()+") for car ("+car.toString()+")");
					// test to see if there's a train that can deliver the car to this location
					ts.setDestination(location);
					ts.setDestinationTrack(track);
					Train firstTrain = TrainManager.instance().getTrainForCar(ts);
					// Is there a train assigned to carry this car out of staging?
					if (_train != null && !_train.servicesCar(ts) && car.getTrack().getLocType().equals(Track.STAGING))
						firstTrain = null;
					if (firstTrain != null){
						if (debugFlag)
							log.debug("Train ("+firstTrain.getName()+") can service car ("+car.toString()+") from "+track.getLocType()+" ("+ts.getLocationName()+", "+ts.getTrackName()+") to next destination ("+ts.getDestinationName()+", "+ts.getDestinationTrackName()+")");
						firstLocationTrackPairs.add(new LocationTrackPair(location, track));
					} else {
						// don't add to other if already in last location list
						boolean match = false;
						for (int k=0; k<lastLocationTrackPairs.size(); k++){
							LocationTrackPair ltp = lastLocationTrackPairs.get(k);
							if (ltp.getLocation().equals(location)){
								match = true;
								break;
							}
						} if (!match){
							if (debugFlag)
								log.debug("Adding location ("+location.getName()+", "+track.getName()+") to other locations");
							otherLocationTrackPairs.add(new LocationTrackPair(location, track));
						}
					}
				}
			}
		}
		// location track pairs that could be the very next destination for the car
		for (int i=0; i<firstLocationTrackPairs.size(); i++){
			LocationTrackPair ltp = firstLocationTrackPairs.get(i);
			log.debug("First location ("+ltp.getLocation().getName()+", "+ltp.getTrack().getName()+") can service car ("+car.toString()+")");
		}
		// location track pairs that could be the next to last destination for the car
		for (int i=0; i<lastLocationTrackPairs.size(); i++){
			LocationTrackPair ltp = lastLocationTrackPairs.get(i);
			log.debug("Last location ("+ltp.getLocation().getName()+", "+ltp.getTrack().getName()+") can service car ("+car.toString()+")");
		}
		// location track pairs that are not the first or the last
		for (int i=0; i<otherLocationTrackPairs.size(); i++){
			LocationTrackPair ltp = otherLocationTrackPairs.get(i);
			log.debug("Other location ("+ltp.getLocation().getName()+", "+ltp.getTrack().getName()+") may be needed to service car ("+car.toString()+")");
		}
		if (firstLocationTrackPairs.size()>0){
			log.debug("Try to find route using 3 trains");
			for (int i=0; i<firstLocationTrackPairs.size(); i++){
				LocationTrackPair fltp = firstLocationTrackPairs.get(i);
				ts.setLocation(fltp.getLocation()); // set car to this location and track
				ts.setTrack(fltp.getTrack());
				for (int j=0; j<lastLocationTrackPairs.size(); j++){
					LocationTrackPair lltp = lastLocationTrackPairs.get(j);
					ts.setDestination(lltp.getLocation());	// set car to this destination and track
					ts.setDestinationTrack(lltp.getTrack());
					// does a train service these two locations?
					Train middleTrain = TrainManager.instance().getTrainForCar(ts);
					if (middleTrain != null){
						// check to see if track is staging
						if (ts.getTrack().getLocType().equals(Track.STAGING))
							ts.setTrack(null);	// don't specify which track in staging is to be used, decide later
						log.debug("Found 3 train route, setting car destination ("+ts.getLocationName()+", "+ts.getTrackName()+")");
						addLine(_buildReport, SEVEN, MessageFormat.format(rb.getString("RouterRoute3ForCar"),new Object[]{car.toString(),car.getLocationName(),ts.getLocationName(),ts.getDestinationName(),car.getNextDestinationName()}));
						// only set car's destination if specific train can service car
						Car ts2 = clone(car);
						ts2.setDestination(fltp.getLocation());
						ts2.setDestinationTrack(fltp.getTrack());
						if (_train != null && !_train.servicesCar(ts2)){
							addLine(_buildReport, SEVEN, MessageFormat.format(rb.getString("TrainDoesNotServiceCar"),new Object[]{_train.getName(), car.toString(), (fltp.getLocation().getName()+", "+fltp.getTrack().getName())}));
							return true;
						}
						String status = car.setDestination(ts.getLocation(), ts.getTrack());
						if (status.equals(Track.OKAY)){							
							return true;	// done 3 train routing
						} else {
							log.debug("Could not set car ("+car.toString()+") destination ("+ts.getLocation()+", "+ts.getTrack()+") status "+status);
						}
					}
				}
			}
			log.debug("Using 3 trains to route car was unsuccessful");
			log.debug("Try to find route using 4 trains");
			for (int i=0; i<firstLocationTrackPairs.size(); i++){
				LocationTrackPair fltp = firstLocationTrackPairs.get(i);			
				for (int j=0; j<otherLocationTrackPairs.size(); j++){
					LocationTrackPair mltp = otherLocationTrackPairs.get(j);
					ts.setLocation(fltp.getLocation()); // set car to this location and track
					ts.setTrack(fltp.getTrack());
					ts.setDestination(mltp.getLocation()); // set car to this destination and track
					ts.setDestinationTrack(mltp.getTrack());
					// does a train service these two locations?
					Train middleTrain2 = TrainManager.instance().getTrainForCar(ts);
					if (middleTrain2 != null){
						log.debug("Train 2 ("+middleTrain2.getName()+") services car from "+ts.getLocationName()+" to "+ts.getDestinationName()+", "+ts.getDestinationTrackName());										
						for (int k=0; k<lastLocationTrackPairs.size(); k++){
							LocationTrackPair lltp = lastLocationTrackPairs.get(k);
							ts.setLocation(mltp.getLocation()); // set car to this location and track
							ts.setTrack(mltp.getTrack());
							ts.setDestination(lltp.getLocation()); // set car to this destination and track
							ts.setDestinationTrack(lltp.getTrack());
							Train middleTrain3 = TrainManager.instance().getTrainForCar(ts);
							if (middleTrain3 != null){
								log.debug("Train 3 ("+middleTrain3.getName()+") services car from "+ts.getLocationName()+" to "+ts.getDestinationName()+", "+ts.getDestinationTrackName());
								addLine(_buildReport, SEVEN, MessageFormat.format(rb.getString("RouterRoute4ForCar"),new Object[]{car.toString(),car.getLocationName(),fltp.getLocation(),mltp.getLocation().getName(),lltp.getLocation().getName(),car.getNextDestinationName()}));
								// only set car's destination if specific train can service car
								Car ts2 = clone(car);
								ts2.setDestination(fltp.getLocation());
								ts2.setDestinationTrack(fltp.getTrack());
								if (_train != null && !_train.servicesCar(ts2)){
									addLine(_buildReport, SEVEN, MessageFormat.format(rb.getString("TrainDoesNotServiceCar"),new Object[]{_train.getName(), car.toString(), (fltp.getLocation().getName()+", "+fltp.getTrack().getName())}));
									return true;
								}
								// check to see if track is staging
								Track track = fltp.getTrack();
								if (track.getLocType().equals(Track.STAGING))
									track = null;	// don't specify which track in staging is to be used, decide later
								String status = car.setDestination(fltp.getLocation(), track);
								log.debug("Found 4 train route, setting car destination ("+fltp.getLocation().getName()+", "+fltp.getTrack().getName()+")");					
								if (status.equals(Track.OKAY)){
									//log.info("Route for car ("+car.toString()+") "+car.getLocationName()+"->"+car.getDestinationName()+"->"+mltp.getLocation().getName()+"->"+lltp.getLocation().getName()+"->"+car.getNextDestinationName());
									return true;	// done 4 train routing
								} else {
									log.debug("Could not set car ("+car.toString()+") destination");
								}
							}
						}			
					}
				}
			}
			log.debug("Using 4 trains to route car was unsuccessful");
			log.debug("Try to find route using 5 trains");
			for (int i=0; i<firstLocationTrackPairs.size(); i++){
				LocationTrackPair fltp = firstLocationTrackPairs.get(i);			
				for (int j=0; j<otherLocationTrackPairs.size(); j++){
					LocationTrackPair mltp1 = otherLocationTrackPairs.get(j);
					ts.setLocation(fltp.getLocation()); // set car to this location and track
					ts.setTrack(fltp.getTrack());
					ts.setDestination(mltp1.getLocation()); // set car to this destination and track
					ts.setDestinationTrack(mltp1.getTrack());
					// does a train service these two locations?
					Train middleTrain2 = TrainManager.instance().getTrainForCar(ts);
					if (middleTrain2 != null){		
						log.debug("Train 2 ("+middleTrain2.getName()+") services car from "+ts.getLocationName()+" to "+ts.getDestinationName()+", "+ts.getDestinationTrackName());										
						for (int k=0; k<otherLocationTrackPairs.size(); k++){
							LocationTrackPair mltp2 = otherLocationTrackPairs.get(k);
							ts.setLocation(mltp1.getLocation()); // set car to this location and track
							ts.setTrack(mltp1.getTrack());
							ts.setDestination(mltp2.getLocation()); // set car to this destination and track
							ts.setDestinationTrack(mltp2.getTrack());
							// does a train service these two locations?
							Train middleTrain3 = TrainManager.instance().getTrainForCar(ts);
							if (middleTrain3 != null){
								log.debug("Train 3 ("+middleTrain3.getName()+") services car from "+ts.getLocationName()+" to "+ts.getDestinationName()+", "+ts.getDestinationTrackName());										
								for (int n=0; n<lastLocationTrackPairs.size(); n++){
									LocationTrackPair lltp = lastLocationTrackPairs.get(n);
									ts.setLocation(mltp2.getLocation()); // set car to this location and track
									ts.setTrack( mltp2.getTrack());
									ts.setDestination(lltp.getLocation()); // set car to this destination and track
									ts.setDestinationTrack(lltp.getTrack());
									// does a train service these two locations?
									Train middleTrain4 = TrainManager.instance().getTrainForCar(ts);
									if (middleTrain4 != null){
										log.debug("Train 4 ("+middleTrain4.getName()+") services car from "+ts.getLocationName()+" to "+ts.getDestinationName()+", "+ts.getDestinationTrackName());	
										addLine(_buildReport, SEVEN, MessageFormat.format(rb.getString("RouterRoute5ForCar"),new Object[]{car.toString(),car.getLocationName(),fltp.getLocation().getName(),mltp1.getLocation().getName(),mltp2.getLocation().getName(),lltp.getLocation().getName(),car.getNextDestinationName()}));
										// only set car's destination if specific train can service car
										Car ts2 = clone(car);
										ts2.setDestination(fltp.getLocation());
										ts2.setDestinationTrack(fltp.getTrack());
										if (_train != null && !_train.servicesCar(ts2)){
											addLine(_buildReport, SEVEN, MessageFormat.format(rb.getString("TrainDoesNotServiceCar"),new Object[]{_train.getName(), car.toString(), (fltp.getLocation().getName()+", "+fltp.getTrack().getName())}));
											return true;
										}
										// check to see if track is staging
										Track track = fltp.getTrack();
										if (track.getLocType().equals(Track.STAGING))
											track = null;	// don't specify which track in staging is to be used, decide later
										String status = car.setDestination(fltp.getLocation(), track);
										log.debug("Found 5 train route, setting car destination ("+fltp.getLocation().getName()+", "+fltp.getTrack().getName()+")");			
										if (status.equals(Track.OKAY)){
											//log.info("Route for car ("+car.toString()+") "+car.getLocationName()+"->"+car.getDestinationName()+"->"+mltp1.getLocation().getName()+"->"+mltp2.getLocation().getName()+"->"+lltp.getLocation().getName()+"->"+car.getNextDestinationName());
											return true;	// done 5 train routing
										} else {
											log.debug("Could not set car ("+car.toString()+") destination");
										}
									}
								}
							}
						}			
					}
				}
			}
			log.debug("Using 5 trains to route car was unsuccessful");
		}
		return false;
	}
	
	// sets clone car destination to next destination and track
	private Car clone(Car car){
		Car clone = new Car();
		clone.setBuilt(car.getBuilt());
		// modify clone car length if car is part of kernel
		if (car.getKernel() != null)
			clone.setLength(Integer.toString(car.getKernel().getLength()));
		else
			clone.setLength(car.getLength());
		clone.setLoad(car.getLoad());
		clone.setNumber(car.getNumber());
		clone.setOwner(car.getOwner());
		clone.setRoad(car.getRoad());
		clone.setType(car.getType());
		clone.setLocation(car.getLocation());
		clone.setTrack(car.getTrack());
		// next two items is where the clone is different
		clone.setDestination(car.getNextDestination());
		clone.setDestinationTrack(car.getNextDestTrack());
		return clone;
	}
		
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Router.class.getName());


}
