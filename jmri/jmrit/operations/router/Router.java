package jmri.jmrit.operations.router;

import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Router for car movement. This code attempts to find a way to move a
 * car to its final destination through the use of two or more trains.
 * Currently the router is limited to two trains.
 * 
 * @author Daniel Boudreau  Copyright (C) 2010
 * @version             $Revision: 1.1 $
 */

public class Router {
	
	TrainManager trainManager = TrainManager.instance();
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
	 * Sets a car's next destination to an interchange.
	 * @param car
	 * @return true if car's destination has been modified to an interchange.  False if an interchange track
	 * wasn't found that could service the car's final destination.
	 */
	public boolean setCarDestinationInterchange(Car car){
		return setCarDestinationTrack (car, Track.INTERCHANGE);
	}
	
	/**
	 * Sets a car's next destination to a yard.
	 * @param car
	 * @return true if car's destination has been modified to a yard.  False if a yard track
	 * wasn't found that could service the car's final destination.
	 */
	public boolean setCarDestinationYard(Car car){
		return setCarDestinationTrack (car, Track.YARD);
	}
	
	public boolean setCarDestinationTrack(Car car, String type){
		if (!Setup.isCarRoutingEnabled()){
			log.debug("Car routing is disabled");
			return false;
		}
		log.debug("Find an "+type+" track for car ("+car.toString()+") final destination ("+car.getDestinationName()+", "+car.getDestinationTrackName()+")");
		// save car's location, track, destination, and destination track
		Location saveLocation = car.getLocation();
		Track saveTrack = car.getTrack();
		Location saveDestation = car.getDestination();
		Track saveDestTrack = car.getDestinationTrack();
		if (saveTrack == null){
			log.debug("Car's track is null! Can't route");
			return false;
		}
		// now search for a track
		List<String> locations = LocationManager.instance().getLocationsByIdList();
		for (int i=0; i<locations.size(); i++){
			Location location = LocationManager.instance().getLocationById(locations.get(i));
			List<String> tracks = location.getTracksByNameList(type);
			for (int j=0; j<tracks.size(); j++){
				Track track = location.getTrackById(tracks.get(j));
				if (location.acceptsTypeName(car.getType()) 
						&& track.acceptsTypeName(car.getType()) 
						&& track.acceptsRoadName(car.getRoad())){
					log.debug("Found "+type+" track ("+location.getName()+", "+track.getName()+") for car ("+car.toString()+")");
					// test to see if there's a train that can deliver the car to its final location
					String status = car.setLocation(location, track);
					if (status.equals(Car.OKAY)){
						Train nextTrain = trainManager.getTrainForCar(car);
						if (nextTrain != null){
							log.debug("Train ("+nextTrain.getName()+") can service car ("+car.toString()+") from "+type+" ("+car.getLocationName()+", "+car.getTrackName()+") to final destination ("+car.getDestinationName()+", "+car.getDestinationTrackName()+")");
							car.setLocation(saveLocation, saveTrack);	// restore car's location and track
							car.setDestination(location, track); // forward car to this track.
							// test to see if car can be transported from current location to track
							Train firstTrain = trainManager.getTrainForCar(car);
							if (firstTrain != null){
								log.debug("Train ("+firstTrain.getName()+") can service car ("+car.toString()+") from current location ("+car.getLocationName()+", "+car.getTrackName()+") to "+type+" ("+car.getDestinationName()+", "+car.getDestinationTrackName()+")");
								return true;
							} else {
								// restore car's destination
								car.setDestination(saveDestation, saveDestTrack);
							}
						} else{
							log.debug("Could not find a train service car from "+type+" ("+track.getName()+") to destination ("+car.getDestinationName()+")");
						}
					} else {
						log.debug("Could not place car at "+type+" because "+status);
					}
				}
			}
		}
		// restore car's location and track
		car.setLocation(saveLocation, saveTrack);
		return false;
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Router.class.getName());


}
