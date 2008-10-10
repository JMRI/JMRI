package jmri.jmrit.operations.trains;

import java.awt.Font;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.SecondaryLocation;

import jmri.jmrit.operations.cars.CarManager;
import jmri.jmrit.operations.cars.Car;
import jmri.jmrit.operations.cars.Kernel;

import jmri.jmrit.operations.engines.EngineManager;
import jmri.jmrit.operations.engines.Engine;
import jmri.jmrit.operations.engines.Consist;

import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.setup.Control;

import jmri.util.davidflanagan.HardcopyWriter;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.LocoIcon;


import org.jdom.Element;

/**
 * Utilities to build trains and move them. 
 * 
 * @author Daniel Boudreau  Copyright (C) 2008
 * @version             $Revision: 1.5 $
 */
public class TrainBuilder{
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

	// build status
	private static final String BUILDFAILED = rb.getString("BuildFailed");
	private static final String BUILDING = rb.getString("Building");
	private static final String BUILT = rb.getString("Built") + " ";
	private static final String PARTIALBUILT = rb.getString("Partial") + " ";
	
	// build variables shared between local routines
	Train train;		// the train being built
	int numberCars;		// how many cars are moved by this train
	int numberEngines;	// the number of engines assigned to this train
	int carIndex;		// index for carList
	List carList;		// list of cars available for this train
	List routeList;		// list of locations from departure to termination served by this train
	int moves;			// the number of pickup car moves for a location
	double maxWeight;	// the maximum weight of cars in train
	int reqNumOfMoves;	// the requested number of car moves for a location
	Location departLocation;	// train departs this location
	Location terminateLocation; // train terminate at this location
	boolean success;	// true when enough cars have been picked up from a location
	
	// managers 
	CarManager carManager = CarManager.instance();
	LocationManager locationManager = LocationManager.instance();
	EngineManager engineManager = EngineManager.instance();
		
	/**
	 * Build rules:
	 * 1. Need at least one location in route to build train
	 * 2. Select only engines and cars the that train can service
	 * 3. Optional, train must depart with the required number of moves (cars)
	 * 4. Add caboose or car with FRED to train if required
	 * 5. All cars must leave stagging tracks
	 * 6. If a train is assigned to stagging, all cars must go there  
	 * 7. Service locations based on car types and roads
	 *
	 */
	public void build(Train train){
		this.train = train;
		train.setStatus(BUILDING);
		train.setBuilt(false);
		numberCars = 0;
		maxWeight = 0;
		train.iconEngine = null;
		
		// create build status file
		File file = TrainManagerXml.instance().createTrainBuildReportFile(train.getName());
		PrintWriter fileOut = null;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file)),
					true);
		} catch (IOException e) {
			log.error("can not open build status file");
			return;
		}

		Date now = new Date();
		addLine(fileOut, "Build report for train ("+train.getName()+") built on "+now);
		
		if (train.getRoute() == null){
			buildFailed(fileOut, "Can't build train ("+train.getName()+"), needs a route");
			return;
		}
		routeList = train.getRoute().getLocationsBySequenceList();
		if (routeList.size() < 1){
			buildFailed(fileOut, "Route needs at least one location to build train ("+train.getName()+")");
			return;
		}
		// train departs
		departLocation = locationManager.getLocationByName(train.getTrainDepartsName());
		if (departLocation == null){
			buildFailed(fileOut, "Route departure location missing for train ("+train.getName()+")");
			return;
		}
		// train terminates
		terminateLocation = locationManager.getLocationByName(train.getTrainTerminatesName());
		if (terminateLocation == null){
			buildFailed(fileOut, "Route terminate location missing for train ("+train.getName()+")");
			return;
		}
		// TODO: DAB control minimal build by each train
		if (train.getTrainDepartsRouteLocation().getMaxCarMoves() > departLocation.getNumberCars() && Control.fullTrainOnly){
			buildFailed(fileOut, "Not enough cars ("+departLocation.getNumberCars()+") at departure ("+train.getTrainDepartsName()+") to build train ("+train.getName()+")");
			return;
		}
		// get the number of requested car moves
		int requested = 0;
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = train.getRoute().getLocationById((String)routeList.get(i));
			// check to see if there's a location for each stop in the route
			Location l = locationManager.getLocationByName(rl.getName());
			if (l == null){
				buildFailed(fileOut, "Location missing in route ("+train.getRoute().getName()+")");
				return;
			}
			// train doesn't drop or pickup cars from staging locations found in middle of a route
			List slStage = l.getSecondaryLocationsByMovesList(SecondaryLocation.STAGING);
			if (slStage.size() > 0 && i!=0 && i!=routeList.size()-1){
				addLine(fileOut, "Location ("+rl.getName()+") has only staging tracks");
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			}
			// if a location is skipped, no drops or pickups
			else if(train.skipsLocation(rl.getId())){
				addLine(fileOut, "Location (" +rl.getName()+ ") is skipped by train "+train.getName());
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			// we're going to use this location, so initialize the location
			}else{
				requested = requested + rl.getMaxCarMoves();
				rl.setCarMoves(0);					// clear the number of moves
				rl.setSecondaryLocation(null);		// used for staging only
				addLine(fileOut, "Location (" +rl.getName()+ ") requests " +rl.getMaxCarMoves()+ " moves");
			}
			rl.setTrainWeight(0);					// clear the total train weight 
		}
		int carMoves = requested;
		if(routeList.size()> 1)
			requested = requested/2;  // only need half as many cars to meet requests
		addLine(fileOut, "Route (" +train.getRoute().getName()+ ") requests " + requested + " cars and " + carMoves +" moves");

		// determine if train is departing staging
		SecondaryLocation departStage = null;
		List slStage = departLocation.getSecondaryLocationsByMovesList(SecondaryLocation.STAGING);
		if (slStage.size()>0){
			addLine(fileOut, "Train will depart staging, there are "+slStage.size()+" tracks");
			for (int i=0; i<slStage.size(); i++ ){
				departStage = departLocation.getSecondaryLocationById((String)slStage.get(i));
				addLine(fileOut, "Staging track ("+departStage.getName()+") has "+departStage.getNumberCars()+" cars");
				if (departStage.getNumberCars()>0 && getEngines(fileOut, departStage)){
					break;
				} else {
					departStage = null;
				}
			}
		}
		if (slStage.size()>0 && departStage == null){
			buildFailed(fileOut, "Could not meet train requirements from staging ("+departLocation.getName()+")");
			return;
		}
		// load engines for this train
		if (departStage == null && !getEngines(fileOut, null)){
			buildFailed(fileOut, "Could not get the required engines for this train");
			return;
		}

		// get list of cars for this route
		carList = carManager.getCarsAvailableTrainList(train);
		// DAB this needs to be controled by each train
		if (requested > carList.size() && Control.fullTrainOnly){
			buildFailed(fileOut, "The number of requested cars (" +requested+ ") for train (" +train.getName()+ ") is greater than the number available (" +carList.size()+ ")");
			return;
		}
		// get any requirements for this train
		boolean caboose = false;		// start off without any requirements
		boolean fred = false;
		boolean foundFred = true;
		boolean foundCaboose = true;
		String textRequires = "none";
		if (train.getRequirements()>0){
			if ((train.getRequirements()& train.FRED) > 0){
				fred = true;
				foundFred = false;
				textRequires = "FRED";
			} 
			if ((train.getRequirements()& train.CABOOSE) > 0){
				caboose = true;
				foundCaboose = false;
				textRequires = "caboose";
			}
			if (!train.getCabooseRoad().equals("")){
				textRequires += " road ("+train.getCabooseRoad()+")";
			}
			addLine(fileOut, "Train ("+train.getName()+") requires "+textRequires);
		}
		// show road names that this train will service
		if (!train.getRoadOption().equals(train.ALLROADS)){
			String[] roads = train.getRoadNames();
	    	String roadNames ="";
	    	for (int i=0; i<roads.length; i++){
	    		roadNames = roadNames + roads[i]+" ";
	    	}
	    	addLine(fileOut, "Train ("+train.getName()+") "+train.getRoadOption()+" roads "+roadNames);
		}
		// show car types that this train will service
		String[] types =train.getTypeNames();
		String typeNames ="";
    	for (int i=0; i<types.length; i++){
    		typeNames = typeNames + types[i]+" ";
    	}
    	addLine(fileOut, "Train ("+train.getName()+") services car types: "+typeNames);
    	for (carIndex=0; carIndex<carList.size(); carIndex++){
    		Car c = carManager.getCarById((String) carList.get(carIndex));
    		// remove cars that don't have a valid secondary location
    		if (c.getSecondaryLocation() == null){
    			addLine(fileOut, "ERROR Exclude car ("+c.getId()+") at location ("+c.getLocationName()+", "+c.getSecondaryLocationName()+") no secondary location");
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
    		}
    		// all cars in staging must be accepted, so don't exclude if in staging
    		if (departStage == null || !c.getSecondaryLocation().getName().equals(departStage.getName())){
    			if (!train.acceptsRoadName(c.getRoad())){
    				addLine(fileOut, "Exclude car ("+c.getId()+") road ("+c.getRoad()+") at location ("+c.getLocationName()+", "+c.getSecondaryLocationName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (!train.acceptsTypeName(c.getType())){
    				addLine(fileOut, "Exclude car ("+c.getId()+") type ("+c.getType()+") at location ("+c.getLocationName()+", "+c.getSecondaryLocationName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    		}
    		// don't service a car at interchange and has been dropped of by this train
			if (c.getSecondaryLocation().getLocType().equals(SecondaryLocation.INTERCHANGE) && c.getSavedRouteId().equals(train.getRoute().getId())){
				addLine(fileOut, "Exclude car ("+c.getId()+") at interchange ("+c.getLocationName()+", "+c.getSecondaryLocationName()+")");
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
			}
		}

		addLine(fileOut, "Found " +carList.size()+ " cars for train (" +train.getName()+ ")");

		// adjust carlist to only have cars from one staging track
		if (departStage != null){
			// Make sure that all cars in staging are moved
			train.getTrainDepartsRouteLocation().setCarMoves(train.getTrainDepartsRouteLocation().getMaxCarMoves()-departStage.getNumberCars());  // neg number moves more cars
			int numberCarsFromStaging = 0; 
			for (carIndex=0; carIndex<carList.size(); carIndex++){
				Car c = carManager.getCarById((String) carList.get(carIndex));
//				addLine(fileOut, "Check car ("+c.getId()+") at location ("+c.getLocationName()+" "+c.getSecondaryLocationName()+")");
				if (c.getLocationName().equals(departLocation.getName())){
					if (c.getSecondaryLocationName().equals(departStage.getName())){
						addLine(fileOut, "Staging car ("+c.getId()+") at location ("+c.getLocationName()+", "+c.getSecondaryLocationName()+")");
						numberCarsFromStaging++;
					} else {
						addLine(fileOut, "Exclude car ("+c.getId()+") at location ("+c.getLocationName()+", "+c.getSecondaryLocationName()+") from car list");
						carList.remove(carList.get(carIndex));
						carIndex--;
					}
				}
			}
			// error if all of the cars in staging aren't available
			if (numberCarsFromStaging+numberEngines != departStage.getNumberCars()){
				buildFailed(fileOut, "ERROR not all cars or engines in staging can be serviced by this train, " +(departStage.getNumberCars()-numberCarsFromStaging)+" cars or engines can't be serviced");
				return;
			}
		}
		// now go through the car list and remove any that don't belong
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car c = carManager.getCarById((String) carList.get(carIndex));
			addLine(fileOut, "Car (" +c.getId()+ ") at location (" +c.getLocationName()+ ", " +c.getSecondaryLocationName()+ ") with " + c.getMoves()+ " moves");
			// use only the lead car in a kernel for building trains
			if (c.getKernel() != null){
				addLine(fileOut, "Car (" +c.getId()+ ") is part of kernel ("+c.getKernelName()+")");
				if (!c.getKernel().isLeadCar(c)){
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
			}
			if (this.equals(c.getTrain())){
				addLine(fileOut, "Car (" +c.getId()+ ") already assigned to this train");
			}
			// does car have a destination that is part of this train's route?
			if (c.getDestination() != null) {
				addLine(fileOut, "Car (" + c.getId()+ ") has a destination (" +c.getDestination().getName()+ ")");
				RouteLocation rld = train.getRoute().getLocationByName(c.getDestination().getName());
				if (rld == null){
					addLine(fileOut, "Exclude car (" + c.getId()+ ") destination (" +c.getDestination().getName()+ ") not part of this train's route (" +train.getRoute().getName() +")");
					// is this car departing staging?
					if (c.getLocationName().equals(departLocation.getName()) && departStage != null){
						buildFailed(fileOut, "Car (" + c.getId()+ ") departing staging with destination that isn't part of this train's route");
						return;
					}
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
				}
			}
			// check for caboose or car with FRED
			if (c.isCaboose()){
				addLine(fileOut, "Car (" +c.getId()+ ") is a caboose");
				if (departStage != null) foundCaboose = false;		// must move caboose from staging   
			}
			if (c.hasFred()){
				addLine(fileOut, "Car (" +c.getId()+ ") has a FRED");
				if (departStage != null) foundFred = false;			// must move car with FRED from staging
			}
			
			// remove cabooses and cars with FRED if not needed for train
			if (c.isCaboose() && foundCaboose || c.hasFred() && foundFred){
				addLine(fileOut, "Exclude car ("+c.getId()+") at location ("+c.getLocationName()+", "+c.getSecondaryLocationName()+") from car list");
				carList.remove(carList.get(carIndex));		// remove this car from the list
				carIndex--;
				continue;
			}
			// find a caboose or card with FRED for this train if needed
			if (c.isCaboose() && !foundCaboose || c.hasFred() && !foundFred){	
				if(c.getLocationName().equals(train.getTrainDepartsName())){
					if (c.getDestination() == null || c.getDestination() == terminateLocation || departStage != null){
						if (train.getCabooseRoad().equals("") || train.getCabooseRoad().equals(c.getRoad()) || departStage != null){
							// find a secondary location
							if (train.getTrainTerminatesRouteLocation().getSecondaryLocation() == null){
								List sls = terminateLocation.getSecondaryLocationsByMovesList(null);
								for (int s = 0; s < sls.size(); s++){
									SecondaryLocation sld = terminateLocation.getSecondaryLocationById((String)sls.get(s));
									if (c.testDestination(terminateLocation, sld).equals(c.OKAY)){
										addCarToTrain(fileOut, c, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), terminateLocation, sld);
										if (c.isCaboose())
											foundCaboose = true;
										if (c.hasFred())
											foundFred = true;
										break;
									}
								}
								addLine(fileOut,"Could not find a destination for ("+c.getId()+")");
							// terminate into staging	
							} else if (c.testDestination(terminateLocation, train.getTrainTerminatesRouteLocation().getSecondaryLocation()).equals(c.OKAY)){
								addCarToTrain(fileOut, c, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), terminateLocation, train.getTrainTerminatesRouteLocation().getSecondaryLocation());
								if (c.isCaboose())
									foundCaboose = true;
								if (c.hasFred())
									foundFred = true;
							}
						}
					}
				} // caboose or FRED not at departure locaton so remove from list
				if(!foundCaboose || !foundFred) {
					addLine(fileOut, "Exclude car ("+c.getId()+") at location ("+c.getLocationName()+" "+c.getSecondaryLocationName()+") from car list");
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
				}
			}
		}
		if (fred && !foundFred || caboose && !foundCaboose){
			buildFailed(fileOut, "Train ("+train.getName()+") requires "+textRequires+", none found at departure ("+train.getTrainDepartsName()+")");
			return;
		}
		addLine(fileOut, "Requested cars (" +requested+ ") for train (" +train.getName()+ ") the number available (" +carList.size()+ ") building train!");

		// now find destinations for cars 
		int numLocs = routeList.size();
		if (numLocs > 1)  // don't find car destinations for the last location in the route
			numLocs--;
		for (int locationIndex=0; locationIndex<numLocs; locationIndex++){
			RouteLocation rl = train.getRoute().getLocationById((String)routeList.get(locationIndex));
			if(train.skipsLocation(rl.getId())){
				addLine(fileOut, "Location (" +rl.getName()+ ") is skipped by train (" +train.getName()+ ")");
			}else{
				moves = 0;
				success = false;
				reqNumOfMoves = rl.getMaxCarMoves()-rl.getCarMoves();
				int saveReqMoves = reqNumOfMoves;
				addLine(fileOut, "Location (" +rl.getName()+ ") needs " +reqNumOfMoves+ " moves");
				if (reqNumOfMoves <= 0)
					success = true;
				while (reqNumOfMoves > 0){
					for (carIndex=0; carIndex<carList.size(); carIndex++){
						boolean noMoreMoves = true;  // false when there are are locations with moves
						Car c = carManager.getCarById((String) carList.get(carIndex));
						// find a car at this location
						if (c.getLocationName().equals(rl.getName())){
							// can this car be picked up?
							if(!checkPickUpTrainDirection(fileOut, c, rl))
								continue; // no
							// does car have a destination?
							if (c.getDestination() != null) {
								addLine(fileOut, "Car (" + c.getId()+ ") at location (" +c.getLocation()+ ") has a destination (" +c.getDestination()+ ")");
								RouteLocation rld = train.getRoute().getLocationByName(c.getDestination().getName());
								if (rld == null){
									addLine(fileOut, "Car (" + c.getId()+ ") destination not part of route (" +train.getRoute().getName() +")");
								} else {
									if (c.getRouteLocation() != null){ 
										addLine(fileOut, "Car (" + c.getId()+ ") already assigned to this train");
									} 
									if (rld.getCarMoves() < rld.getMaxCarMoves() && 
											addCarToTrain(fileOut, c, rl, rld, c.getDestination(), c.getSecondaryDestination())&& success){
										break;
									}
								}
							// car does not have a destination, search for one	
							} else {
								addLine(fileOut, "Find destinations for car ("+c.getId()+") at location (" +c.getLocationName()+", " +c.getSecondaryLocationName()+ ")");
								int start = locationIndex;				// start looking after car's current location
								RouteLocation rld = null;				// the route location destination being checked for the car
								RouteLocation rldSave = null;			// holds the best route location destination for the car
								SecondaryLocation secondarySave = null;	// holds the best secondary destination for the car
								Location destinationSave = null;		// holds the best destination for the car
						
								// more than one location in this route?
								if (routeList.size()>1)
									start++;		//yes!, no car drops at departure"
								for (int k = start; k<routeList.size(); k++){
									rld = train.getRoute().getLocationById((String)routeList.get(k));
									addLine(fileOut, "Searching location ("+rld.getName()+") for possible destination");
									// don't move car to same location unless the route only has one location (local moves)
									if (!rl.getName().equals(rld.getName()) || routeList.size() == 1){
										Location destinationTemp = null;
										SecondaryLocation secondaryTemp = null;
										// any moves left at this location?
										if (rld.getMaxCarMoves()-rld.getCarMoves()>0){
											// get a "test" destination and a list of the secondary locations available
											noMoreMoves = false;
											Location testDestination = locationManager.getLocationByName(rld.getName());
											if (testDestination == null){
												buildFailed(fileOut, "Route ("+train.getRoute().getName()+") missing location ("+rld.getName()+")");
												return;
											}
											// is there a secondary location assigned for staging cars?
											if (rld.getSecondaryLocation() == null){
												List sls = testDestination.getSecondaryLocationsByMovesList(null);
												for (int s = 0; s < sls.size(); s++){
													SecondaryLocation testSecondary = testDestination.getSecondaryLocationById((String)sls.get(s));
//													log.debug("secondary location (" +testSecondary.getName()+ ") has "+ testSecondary.getMoves() + " moves");
													// need to find a secondary location that is isn't the same as the car's
													String status = c.testDestination(testDestination, testSecondary);
													if (testSecondary != c.getSecondaryLocation() 
															&& status.equals(c.OKAY) 
															&& checkDropTrainDirection(fileOut, c, rld, testDestination, testSecondary)){
														// staging track with zero cars?
														if (testSecondary.getLocType().equals(testSecondary.STAGING) && testSecondary.getNumberCars() == 0){
															rld.setSecondaryLocation(testSecondary);	// Use this location for all cars
															secondaryTemp = testSecondary;
															destinationTemp = testDestination;
															break;
														}
														// No local moves from siding to siding
														if (routeList.size() == 1 && testSecondary.getLocType().equals(testSecondary.SIDING) && c.getSecondaryLocation().getLocType().equals(testSecondary.SIDING)){
															log.debug("Local siding to siding move not allowed (" +testSecondary.getName()+ ")");
															continue;
														}
														// No local moves from yard to yard
														if (routeList.size() == 1 && testSecondary.getLocType().equals(testSecondary.YARD) && c.getSecondaryLocation().getLocType().equals(testSecondary.YARD)){
															log.debug("Local yard to yard move not allowed (" +testSecondary.getName()+ ")");
															continue;
														}
														// No local moves from interchange to interchange
														if (routeList.size() == 1 && testSecondary.getLocType().equals(testSecondary.INTERCHANGE) && c.getSecondaryLocation().getLocType().equals(testSecondary.INTERCHANGE)){
															log.debug("Local interchange to interchange move not allowed (" +testSecondary.getName()+ ")");
															continue;
														}
														// not staging, then use
														if (!testSecondary.getLocType().equals(testSecondary.STAGING)){
															secondaryTemp = testSecondary;
															destinationTemp = testDestination;
															break;
														}
													}
													// car's secondary location is equal to test secondary location or car can't be dropped
												}
											// all cars in this train go to one staging track
											} else {
												// will staging accept this car?
												String status = c.testDestination(testDestination, rld.getSecondaryLocation());
												if (status.equals(c.OKAY)){
													secondaryTemp = rld.getSecondaryLocation();
													destinationTemp = testDestination;
												}
											}
											if(destinationTemp != null){
												addLine(fileOut, "car ("+c.getId()+") has available destination (" +destinationTemp.getName()+ ", " +secondaryTemp.getName()+ ") with " +rld.getCarMoves()+ "/" +rld.getMaxCarMoves()+" moves");
												// if there's more than one available destination use the one with the least moves
												if (rldSave != null && (rldSave.getCarMoves()-rldSave.getMaxCarMoves())<(rld.getCarMoves()-rld.getMaxCarMoves())){
													rld = rldSave;					// the saved is better than the last found
													destinationTemp = destinationSave;
													secondaryTemp = secondarySave;
												}
												// every time through, save the best location
												rldSave = rld;
												destinationSave = destinationTemp;
												secondarySave = secondaryTemp;
											} else {
												addLine(fileOut, "Could not find a valid destination for car ("+c.getId()+") at location (" + rld.getName()+")");
											}
										} else {
											addLine(fileOut, "No available moves for destination ("+rld.getName()+")");
										}
									} else{
										addLine(fileOut, "Car ("+c.getId()+") location is equal to destination ("+rld.getName()+"), skiping this destination");
									}
								}
								if (destinationSave != null){
									if (addCarToTrain(fileOut, c, rl, rldSave, destinationSave, secondarySave) && success){
										break;
									}
								// car leaving staging without a destinaton
								} else if (c.getSecondaryLocation().getLocType().equals(SecondaryLocation.STAGING)){
									buildFailed(fileOut, "could not find a destination for car ("+c.getId()+") at location (" + rld.getName()+")");
									return;
								// are there still moves available?
								} else if (noMoreMoves) {
									log.debug("No available destinations for any car");
									reqNumOfMoves = 0;
									break;
								}
							}
						}
						// car not at location or has fred or caboose
					}
					// could not find enough cars
					reqNumOfMoves = 0;
				}
				addLine(fileOut, (success?"Success, ":"Partial, ") +moves+ "/" +saveReqMoves+ " cars at location (" +rl.getName()+ ") assigned to train ("+train.getName()+")");
			}
		}

		train.setCurrent(train.getTrainDepartsRouteLocation());
		if (numberCars < requested){
			train.setStatus(PARTIALBUILT + train.getNumberCarsWorked() +"/" + requested + " "+ rb.getString("moves"));
			addLine(fileOut, PARTIALBUILT + train.getNumberCarsWorked() +"/" + requested + " "+ rb.getString("moves"));
		}else{
			train.setStatus(BUILT + train.getNumberCarsWorked() + " "+ rb.getString("moves"));
			addLine(fileOut, BUILT + train.getNumberCarsWorked() + " "+ rb.getString("moves"));
		}
		train.setBuilt(true);
		if (fileOut != null){
			fileOut.flush();
			fileOut.close();
		}

		// now build manifest
		makeManifest();
		// now create and place train icon
		train.moveTrainIcon(train.getTrainDepartsRouteLocation());

	}
	
	// get the engines for this train. If secondary != null, then engines must
	// come from that secondary location (staging).  Returns true if engines found, else false.
	// This method will also setup the secondary location if the train is
	// terminating into staging, therefore this routine should only be called once when return is true.
	private boolean getEngines(PrintWriter fileOut, SecondaryLocation secondary){
		// show engine requirements for this train
		addLine(fileOut, "Train requires "+train.getNumberEngines()+" engine(s) type ("+train.getEngineModel()+") road (" +train.getEngineRoad()+")");
				
		numberEngines = 0;
		int reqEngines = 0; 	
		int engineLength = 0;
		
		if (train.getNumberEngines().equals(train.AUTO)){
			reqEngines = getAutoEngines(fileOut);
		} else {
			reqEngines = Integer.parseInt(train.getNumberEngines());
		}

		// get list of engines for this route
		
		List engineList = engineManager.getEnginesAvailableTrainList(train);
		// remove engines not at departure, wrong road name, or part of consist (not lead)
		for (int indexEng=0; indexEng<engineList.size(); indexEng++){
			Engine engine = engineManager.getEngineById((String) engineList.get(indexEng));
			addLine(fileOut, "Engine ("+engine.getId()+") road ("+engine.getRoad()+") type ("+engine.getModel()+") at location ("+engine.getLocationName()+", "+engine.getSecondaryLocationName()+")");
			// remove engines that have been assigned destinations
			if (engine.getDestination() != null && !engine.getDestination().equals(terminateLocation)){
				addLine(fileOut, "Exclude engine ("+engine.getId()+") it has an assigned destination ("+engine.getDestination().getName()+")");
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// determine if engine is departing from staging track (secondary != null) 
			if(engine.getLocationName().equals(train.getTrainDepartsName()) && (secondary == null || engine.getSecondaryLocationName().equals(secondary.getName()))){
				if ((train.getEngineRoad().equals("") || engine.getRoad().equals(train.getEngineRoad())) && (train.getEngineModel().equals("") || engine.getModel().equals(train.getEngineModel()))){
					// is this engine part of a consist?  Keep only lead engines in consist.
					if (engine.getConsist() != null){
						addLine(fileOut, "Engine ("+engine.getId()+") is part of consist ("+engine.getConsist().getName()+")");
						if (!engine.getConsist().isLeadEngine(engine)){
							// only use lead engines
							engineList.remove(indexEng);
							indexEng--;
						}
					}
					continue;
				} 
			}
			addLine(fileOut, "Exclude engine ("+engine.getId()+")");
			engineList.remove(indexEng);
			indexEng--;
		}
		// if leaving staging, use any number of engines if required number is 0
		boolean leavingStaging = false;
		if (secondary != null && reqEngines == 0)
			leavingStaging = true;

		// now load the number of engines into the train
		SecondaryLocation terminateSecondary = null;
		for (int indexEng=0; indexEng<engineList.size(); indexEng++){
			Engine engine = engineManager.getEngineById((String) engineList.get(indexEng));
			train.iconEngine = engine;		//load Icon
			// find secondary location for engines at destination
			List sls = terminateLocation.getSecondaryLocationsByMovesList(null);
			for (int s = 0; s < sls.size(); s++){
				terminateSecondary = terminateLocation.getSecondaryLocationById((String)sls.get(s));
				if (terminateSecondary.getLocType().equals(terminateSecondary.STAGING) && terminateSecondary.getNumberCars()>0){
					terminateSecondary = null;
					continue;
				}
				String status = engine.testDestination(terminateLocation, terminateSecondary);
				if(status == engine.OKAY){
					break;
				} else {
					terminateSecondary = null;
				}
			}
			if (terminateSecondary == null && indexEng == engineList.size()-1 && (reqEngines>0 || leavingStaging)){
				addLine(fileOut, "Could not find valid destination for engines at (" +terminateLocation.getName()+ ") for train (" +train.getName()+ ")");
				return false;
			}
			if (terminateSecondary != null){
				if (engine.getConsist() != null){
					List cEngines = engine.getConsist().getEngines();
					if (cEngines.size() == reqEngines || leavingStaging){
						engineLength = engine.getConsist().getLength();
						for (int j=0; j<cEngines.size(); j++){
							numberEngines++;
							Engine cEngine = (Engine)cEngines.get(j);
							addLine(fileOut, "Engine ("+cEngine.getId()+") assigned destination ("+terminateLocation.getName()+", "+terminateSecondary.getName()+")");
							cEngine.setTrain(train);
							cEngine.setRouteLocation(train.getTrainDepartsRouteLocation());
							cEngine.setRouteDestination(train.getTrainTerminatesRouteLocation());
							cEngine.setDestination(terminateLocation, terminateSecondary);
						}
						break;  // done with loading engines
						// consist has the wrong number of engines, remove 	
					} else {
						addLine(fileOut, "Exclude engine ("+engine.getId()+") consist ("+engine.getConsist().getName()+") number of engines (" +cEngines.size()+ ")");
						engineList.remove(indexEng);
						indexEng--;
					}
					// engine isn't part of a consist
				} else if (reqEngines ==1 || leavingStaging){
					numberEngines++;
					addLine(fileOut, "Engine ("+engine.getId()+") assigned destination ("+terminateLocation.getName()+", "+terminateSecondary.getName()+")");
					engine.setTrain(train);
					engine.setRouteLocation(train.getTrainDepartsRouteLocation());
					engine.setRouteDestination(train.getTrainTerminatesRouteLocation());
					engine.setDestination(terminateLocation, terminateSecondary);
					engineLength = Integer.parseInt(engine.getLength());
					break;  // done with loading engine
				}
			}
		}
		if (numberEngines < reqEngines){
			addLine(fileOut, "Could not find the proper engines at departure location");
			return false;
		}
		
		// set the engine length for locations
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = train.getRoute().getLocationById((String)routeList.get(i));
			rl.setTrainLength(engineLength);		// load the engine(s) length
		}
		// terminating into staging?
		if (terminateSecondary != null && terminateSecondary.getLocType().equals(terminateSecondary.STAGING)){
			train.getTrainTerminatesRouteLocation().setSecondaryLocation(terminateSecondary);
		}
		return true;
	}
	
	// returns the number of engines needed for this train, minimum 1, 
	// maximum user specified in setup.
	// Based on maximum allowable train length and grade between locations,
	// and the maximum cars that the train can have at the maximum train length.
	// Currently ignores the cars weight and engine horsepower
	private int getAutoEngines(PrintWriter fileOut){
		double numberEngines = 1;
		int moves = 0;
		
		for (int i=0; i<routeList.size()-1; i++){
			RouteLocation rl = train.getRoute().getLocationById((String)routeList.get(i));
			moves += rl.getMaxCarMoves();
			double carDivisor = 16;	// number of 40' cars per engine 1% grade
			// change engine requirements based on grade
			if (rl.getGrade()>1){
				double grade = rl.getGrade();
				carDivisor = carDivisor/grade;
			}
			if (rl.getMaxTrainLength()/(carDivisor*40) > numberEngines){
				numberEngines = rl.getMaxTrainLength()/(carDivisor*(40+Car.COUPLER));
				// round up to next whole integer
				numberEngines = Math.ceil(numberEngines);
				if (numberEngines > moves/carDivisor)
					numberEngines = Math.ceil(moves/carDivisor);
				if (numberEngines < 1)
					numberEngines = 1;
			}
		}
		int nE = (int)numberEngines;
		addLine(fileOut, "Auto engines calculates that "+nE+ " engines are required for this train");
		if (nE > Setup.getEngineSize()){
			addLine(fileOut, "The maximum number of engines that can be assigned is "+Setup.getEngineSize());
			nE = Setup.getEngineSize();
		} 
		return nE;
	}
	
	/**
	 * Add car to train
	 * @param file
	 * @param car
	 * @param rl the planned origin for this car
	 * @param rld the planned destination for this car
	 * @param destination
	 * @param secondary the final destination for car
	 * @return true if car was successfully added to train.  Also makes boolean
	 * success true if location doesn't need any more pickups. 
	 */
	private boolean addCarToTrain(PrintWriter file, Car car, RouteLocation rl, RouteLocation rld, Location destination, SecondaryLocation secondary){
		if (checkTrainLength(file, car, rl, rld)){
			int oldNum = moves;
			// car could be part of a kernel
			if (car.getKernel()!=null){
				List kCars = car.getKernel().getCars();
				addLine(file, "Car ("+car.getId()+") is part of kernel ("+car.getKernelName()+") with "+ kCars.size() +" cars");
				// log.debug("kernel length "+car.getKernel().getLength());
				for(int i=0; i<kCars.size(); i++){
					Car kCar = (Car)kCars.get(i);
					addLine(file, "Car ("+kCar.getId()+") assigned destination ("+destination.getName()+", "+secondary.getName()+")");
					kCar.setTrain(train);
					kCar.setRouteLocation(rl);
					kCar.setRouteDestination(rld);
					kCar.setDestination(destination, secondary);
				}
				// not part of kernel, add one car	
			} else {
				addLine(file, "Car ("+car.getId()+") assigned destination ("+destination.getName()+", "+secondary.getName()+")");
				car.setTrain(train);
				car.setRouteLocation(rl);
				car.setRouteDestination(rld);
				car.setDestination(destination, secondary);
			}
			numberCars++;		// bump number of cars moved by this train
			moves++;			// bump number of car pickup moves for the location
			reqNumOfMoves--; 	// dec number of moves left for the location
			if(reqNumOfMoves <= 0)
				success = true;	// done with this location!
			carList.remove(car.getId());
			carIndex--;  		// removed car from list, so backup pointer 

			rl.setCarMoves(rl.getCarMoves() + 1);
			if (rl != rld)
				rld.setCarMoves(rld.getCarMoves() + 1);
			// now adjust train length and weight for each location that car is in the train
			boolean carInTrain = false;
			for (int i=0; i<routeList.size(); i++){
				int weightTons = 0;
				RouteLocation rlt = train.getRoute().getLocationById((String)routeList.get(i));
				if (rl == rlt){
					carInTrain = true;
				}
				if (rld == rlt){
					carInTrain = false;
				}
				if (carInTrain){
					// car could be part of a kernel
					int length = Integer.parseInt(car.getLength())+ car.COUPLER;
					try {
						weightTons = weightTons + Integer.parseInt(car.getWeightTons());
					} catch (Exception e){
						log.debug ("car ("+car.getId()+") weight not set");
					}
					if (car.getKernel() != null){
						length = car.getKernel().getLength();
						weightTons = car.getKernel().getWeightTons();
					}
					rlt.setTrainLength(rlt.getTrainLength()+length);
					rlt.setTrainWeight(rlt.getTrainWeight()+weightTons);
				}
				if (weightTons > maxWeight){
					maxWeight = weightTons;		// used for AUTO engines
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean checkPickUpTrainDirection (PrintWriter file, Car car, RouteLocation rl){
		if (routeList.size() == 1) // ignore local train direction
			return true;
		String trainDirection = rl.getTrainDirection();	// train direction North, South, East and West
		int trainDir = 0;
		if (trainDirection.equals(rl.NORTH))
			trainDir = SecondaryLocation.NORTH;
		if (trainDirection.equals(rl.SOUTH))
			trainDir = SecondaryLocation.SOUTH;
		if (trainDirection.equals(rl.EAST))
			trainDir = SecondaryLocation.EAST;
		if (trainDirection.equals(rl.WEST))
			trainDir = SecondaryLocation.WEST;
		
		if ((trainDir & car.getLocation().getTrainDirections() & car.getSecondaryLocation().getTrainDirections()) >0)
			return true;
		else {
			addLine(file, "Can't add car ("+car.getId()+") to "
					+trainDirection+"bound train, location ("+car.getLocation().getName()
					+", "+car.getSecondaryLocation().getName()+") does not service this direction");
			return false;
		}
	}
	
	
	/**
	 * 
	 * @param file
	 * @param car
	 * @param rl the planned origin for this car
	 * @param rld the planned destination for this car
	 * @return true if car can be added to train
	 */
	private boolean checkTrainLength(PrintWriter file, Car car,
			RouteLocation rl, RouteLocation rld) {
		boolean carInTrain = false;
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rlt = train.getRoute().getLocationById((String)routeList.get(i));
			if (rl == rlt){
				carInTrain = true;
			}
			if (rld == rlt){
				carInTrain = false;
			}
			// car can be a kernel so get total length
			int length = Integer.parseInt(car.getLength())+ car.COUPLER;
			if (car.getKernel() != null)
				length = car.getKernel().getLength();
			if (carInTrain && rlt.getTrainLength()+ length > rlt.getMaxTrainLength()){
				addLine(file, "Can't add car ("+car.getId()+") length ("+length+") to train, it would exceed train length restrication at "+rlt.getName());
				return false;
			}
		}
		return true;
	}
	
	private boolean checkDropTrainDirection (PrintWriter file, Car car, RouteLocation rld, Location destination, SecondaryLocation secondary){
		// is the destination the last location on the route? 
		if (rld == train.getTrainTerminatesRouteLocation())
			return true;	// yes, ignore train direction
		String trainDirection = rld.getTrainDirection();	// train direction North, South, East and West
		int trainDir = 0;
		if (trainDirection.equals(rld.NORTH))
			trainDir = SecondaryLocation.NORTH;
		if (trainDirection.equals(rld.SOUTH))
			trainDir = SecondaryLocation.SOUTH;
		if (trainDirection.equals(rld.EAST))
			trainDir = SecondaryLocation.EAST;
		if (trainDirection.equals(rld.WEST))
			trainDir = SecondaryLocation.WEST;
		int serviceTrainDir = (destination.getTrainDirections() & secondary.getTrainDirections()); // this location only services trains with these directions
		if ((serviceTrainDir & trainDir) >0){
			return true;
		} else {
			addLine(file, "Can't add car ("+car.getId()+") to "+trainDirection+"bound train, destination ("+secondary+") does not service this direction");
			return false;
		}
	}

	// writes string to console and file
	private void addLine (PrintWriter file, String string){
		if(log.isDebugEnabled())
			log.debug(string);
		if (file != null)
			file.println(string);
	}
	
	private void newLine (PrintWriter file){
		if (file != null)
			file.println(" ");
	}
	
	private void buildFailed(PrintWriter file, String string){
		train.setStatus(BUILDFAILED);
		if(log.isDebugEnabled())
			log.debug(string);
		JOptionPane.showMessageDialog(null, string,
				"Can not build train ("+train.getName()+") " +train.getDescription(),
				JOptionPane.ERROR_MESSAGE);
		if (file != null){
			file.println(string);
			// Write to disk and close file
			file.println("Build failed for train ("+train.getName()+")");
			file.flush();
			file.close();
			if(TrainManager.instance().getBuildReport()){
				File buildFile = TrainManagerXml.instance().getTrainBuildReportFile(train.getName());
				printReport(buildFile, "Train Build Failure Report", true);
			}
		}
	}
	
	private static void printReport(File file, String name, boolean isPreview){
		Train.printReport(file, name, isPreview, "");
 	}
	
	private void makeManifest() {
		// create manifest file
		File file = TrainManagerXml.instance().createTrainManifestFile(
				train.getName());
		PrintWriter fileOut;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file)),
					true);
		} catch (IOException e) {
			log.error("can not open train manifest file");
			return;
		}
		// build header
		addLine(fileOut, Setup.getRailroadName());
		newLine(fileOut);
		addLine(fileOut, rb.getString("ManifestForTrain")+" (" + train.getName() + ") "+ train.getDescription());
		addLine(fileOut, "Valid " + new Date());
		if (!train.getComment().equals("")){
			addLine(fileOut, train.getComment());
		}
		
		List engineList = engineManager.getEnginesByTrainList(train);
		Engine engine = null;
		for (int i =0; i < engineList.size(); i++){
			engine = engineManager.getEngineById((String) engineList.get(i));
			addLine(fileOut, rb.getString("Engine")+" "+ engine.getRoad() + " " + engine.getNumber() + " (" +engine.getModel()+  ") "+rb.getString("assignedToThisTrain"));
		}
		
		if (engine != null)
			addLine(fileOut, "Pickup engine(s) at "+engine.getLocationName()+", "+engine.getSecondaryLocationName());
		
		List carList = carManager.getCarsByTrainList(train);
		log.debug("Train has " + carList.size() + " cars assigned to it");
		int cars = 0;
		List routeList = train.getRoute().getLocationsBySequenceList();
		for (int i = 0; i < routeList.size(); i++) {
			RouteLocation rl = train.getRoute().getLocationById((String) routeList.get(i));
			newLine(fileOut);
			addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + rl.getName());
			// block cars by destination
			for (int j = i; j < routeList.size(); j++) {
				RouteLocation rld = train.getRoute().getLocationById((String) routeList.get(j));
				for (int k = 0; k < carList.size(); k++) {
					Car car = carManager.getCarById((String) carList.get(k));
					if (car.getRouteLocation() == rl
							&& car.getRouteDestination() == rld) {
						pickupCar(fileOut, car);
						cars++;
					}
				}
			}
			for (int j = 0; j < carList.size(); j++) {
				Car car = carManager.getCarById((String) carList.get(j));
				if (car.getRouteDestination() == rl) {
					dropCar(fileOut, car);
					cars--;
				}
			}
			if (i != routeList.size() - 1) {
				addLine(fileOut, rb.getString("TrainDeparts")+ " " + rl.getName() +" "+ rl.getTrainDirection()
						+ rb.getString("boundWith") +" " + cars + " " +rb.getString("cars")+", " +rl.getTrainLength()
						+" "+rb.getString("feet")+", "+rl.getTrainWeight()+" "+rb.getString("tons"));
			} else {
				if(engine != null)
					addLine(fileOut, rb.getString("DropEngineTo")+ " "+ engine.getSecondaryDestinationName()); 
				addLine(fileOut, rb.getString("TrainTerminatesIn")+ " " + rl.getName());
			}
		}
		fileOut.flush();
		fileOut.close();
	}
	
	private void  pickupCar(PrintWriter file, Car car){
		String[] carNumber = car.getNumber().split("-"); // ignore any duplicate car numbers
		String carComment = (Setup.isAppendCarCommentEnabled() ? " "+car.getComment() : "");
		addLine(file, rb.getString("Pickup")+" " + car.getRoad() + " "
				+ carNumber[0] + " " + car.getType() + " "
				+ car.getLength() + " " + car.getColor()
				+ (car.isHazardous() ? " ("+rb.getString("Hazardous")+") " : " ")
				+ (car.hasFred() ? " ("+rb.getString("fred")+") " : " ") + rb.getString("from")+ " "
				+ car.getSecondaryLocationName() + carComment);
	}
	
	private void dropCar(PrintWriter file, Car car){
		String[] carNumber = car.getNumber().split("-"); // ignore any duplicate car numbers
		String carComment = (Setup.isAppendCarCommentEnabled() ? " "+car.getComment() : "");
		addLine(file, rb.getString("Drop")+ " " + car.getRoad() + " "
				+ carNumber[0] + " " + car.getType() + " "
				+ car.getLength() + " " + car.getColor()
				+ (car.isHazardous() ? " ("+rb.getString("Hazardous")+") " : " ")
				+ rb.getString("to") + " " + car.getSecondaryDestinationName()
				+ carComment);
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(TrainBuilder.class.getName());

}
