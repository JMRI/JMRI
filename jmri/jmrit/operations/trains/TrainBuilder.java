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
import java.text.MessageFormat;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;



import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.setup.Control;

import jmri.util.davidflanagan.HardcopyWriter;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.LocoIcon;


import org.jdom.Element;

/**
 * Builds a train and creates the train's manifest. 
 * 
 * @author Daniel Boudreau  Copyright (C) 2008
 * @version             $Revision: 1.29 $
 */
public class TrainBuilder extends TrainCommon{
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	private static final String BOX = " [ ] ";
	
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
	Track departStageTrack;		// departure staging track (null if not staging)
	Location terminateLocation; // train terminate at this location
	Track terminateStageTrack; 	// terminate staging track (null if not staging)
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
	 * 5. All cars and engines must leave stagging tracks
	 * 6. If a train is assigned to stagging, all cars and engines must go there  
	 * 7. Service locations based on train direction, location car types and roads
	 * 8. Ignore train/track direction when servicing the last location in a route
	 * 9. Ignore track direction when train is a local (serves one location)
	 *
	 */
	public void build(Train train){
		this.train = train;
		train.setStatus(BUILDING);
		train.setBuilt(false);
		train.setLeadEngine(null);
		numberCars = 0;
		maxWeight = 0;
		
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
		
		addLine(fileOut, ONE, MessageFormat.format(rb.getString("BuildReportMsg"),new Object[]{train.getName(), new Date()}));
		
		if (train.getRoute() == null){
			buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorRoute"),new Object[]{train.getName()}));
			return;
		}
		routeList = train.getRoute().getLocationsBySequenceList();
		if (routeList.size() < 1){
			buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorNeedRoute"),new Object[]{train.getName()}));
			return;
		}
		// train departs
		departLocation = locationManager.getLocationByName(train.getTrainDepartsName());
		if (departLocation == null){
			buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorNeedDepLoc"),new Object[]{train.getName()}));
			return;
		}
		// train terminates
		terminateLocation = locationManager.getLocationByName(train.getTrainTerminatesName());
		if (terminateLocation == null){
			buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorNeedTermLoc"),new Object[]{train.getName()}));
			return;
		}
		  
		// TODO: DAB control minimal build by each train
		if (train.getTrainDepartsRouteLocation().getMaxCarMoves() > departLocation.getNumberRS() && Control.fullTrainOnly){
			buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorCars"),new Object[]{Integer.toString(departLocation.getNumberRS()),
				train.getTrainDepartsName(), train.getName()}));
			return;
		}
		// get the number of requested car moves
		int requested = 0;
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = train.getRoute().getLocationById((String)routeList.get(i));
			// check to see if there's a location for each stop in the route
			Location l = locationManager.getLocationByName(rl.getName());
			if (l == null){
				buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorLocMissing"),new Object[]{train.getRoute().getName()}));
				return;
			}
			// train doesn't drop or pickup cars from staging locations found in middle of a route
			List slStage = l.getTracksByMovesList(Track.STAGING);
			if (slStage.size() > 0 && i!=0 && i!=routeList.size()-1){
				addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildLocStaging"),new Object[]{rl.getName()}));
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			}
			// if a location is skipped, no drops or pickups
			else if(train.skipsLocation(rl.getId())){
				addLine(fileOut, THREE, "Location (" +rl.getName()+ ") is skipped by train "+train.getName());
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			// skip if a location doesn't allow drops or pickups
			}
			else if(!rl.canDrop() && !rl.canPickup()){
				addLine(fileOut, THREE, "Location (" +rl.getName()+ ") does not allow drops or pickups");
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
				// we're going to use this location, so initialize the location
			}
			else{
				requested = requested + rl.getMaxCarMoves();
				rl.setCarMoves(0);					// clear the number of moves
				rl.setStagingTrack(null);		// used for staging only
				addLine(fileOut, THREE, "Location (" +rl.getName()+ ") requests " +rl.getMaxCarMoves()+ " moves");
			}
			rl.setTrainWeight(0);					// clear the total train weight 
		}
		int carMoves = requested;
		if(routeList.size()> 1)
			requested = requested/2;  // only need half as many cars to meet requests
		addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildRouteRequest"),new Object[]{train.getRoute().getName(), Integer.toString(requested), Integer.toString(moves)}));
		
		// does train terminate into staging?
		terminateStageTrack = null;
		List stagingTracksTerminate = terminateLocation.getTracksByMovesList(Track.STAGING);
		if (stagingTracksTerminate.size() > 0){
			addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildTerminateStaging"),new Object[]{terminateLocation.getName(), Integer.toString(stagingTracksTerminate.size())}));
			for (int i=0; i<stagingTracksTerminate.size(); i++){
				terminateStageTrack = terminateLocation.getTrackById((String)stagingTracksTerminate.get(i));
				if (terminateStageTrack.getNumberRS() == 0 && terminateStageTrack.getDropRS() == 0){
					addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildStagingAvail"),new Object[]{terminateStageTrack.getName(), terminateLocation.getName()}));
					break;
				} else {
					terminateStageTrack = null;
				}
			}
			if (terminateStageTrack == null){
				buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorStagingFull"),new Object[]{terminateLocation.getName()}));
				return;
			}
		}
		
		// determine if train is departing staging
		departStageTrack = null;
		List stagingTracks = departLocation.getTracksByMovesList(Track.STAGING);
		if (stagingTracks.size()>0){
			addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildDepartStaging"),new Object[]{Integer.toString(stagingTracks.size())}));
			for (int i=0; i<stagingTracks.size(); i++ ){
				departStageTrack = departLocation.getTrackById((String)stagingTracks.get(i));
				addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildStagingHas"),new Object[]{
						departStageTrack.getName(), Integer.toString(departStageTrack.getNumberEngines()),
						Integer.toString(departStageTrack.getNumberCars())}));
				if (departStageTrack.getNumberRS()>0 && getEngines(fileOut)){
					break;
				} else {
					departStageTrack = null;
				}
			}
			if (departStageTrack == null){
				buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorStagingReq"),new Object[]{departLocation.getName()}));
				return;
			}
		}

		// load engines for this train
		if (departStageTrack == null && !getEngines(fileOut)){
			buildFailed(fileOut, rb.getString("buildErrorEngines"));
			return;
		}

		// get list of cars for this route
		carList = carManager.getCarsAvailableTrainList(train);
		// TODO: DAB this needs to be controled by each train
		if (requested > carList.size() && Control.fullTrainOnly){
			buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorNumReq"),new Object[]{Integer.toString(requested),
				train.getName(), Integer.toString(carList.size())}));
			return;
		}
		// get any requirements for this train
		boolean requiresCaboose = false;		// start off without any requirements
		boolean requiresFred = false;
		boolean foundFred = true;
		boolean foundCaboose = true;
		String textRequires = rb.getString("None");
		if (train.getRequirements()>0){
			if ((train.getRequirements()& train.FRED) > 0){
				requiresFred = true;
				foundFred = false;
				textRequires = rb.getString("FRED");
			} 
			if ((train.getRequirements()& train.CABOOSE) > 0){
				requiresCaboose = true;
				foundCaboose = false;
				textRequires = rb.getString("Caboose");
			}
			if (!train.getCabooseRoad().equals("")){
				textRequires += " road ("+train.getCabooseRoad()+")";
			}
			addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildTrainRequires"),new Object[]{train.getName(), textRequires}));
		}
		// show road names that this train will service
		if (!train.getRoadOption().equals(train.ALLROADS)){
			String[] roads = train.getRoadNames();
	    	String roadNames ="";
	    	for (int i=0; i<roads.length; i++){
	    		roadNames = roadNames + roads[i]+" ";
	    	}
	    	addLine(fileOut, FIVE, "Train ("+train.getName()+") "+train.getRoadOption()+" roads "+roadNames);
		}
		// show car types that this train will service
		String[] types =train.getTypeNames();
		String typeNames ="";
    	for (int i=0; i<types.length; i++){
    		typeNames = typeNames + types[i]+" ";
    	}
    	addLine(fileOut, FIVE, "Train ("+train.getName()+") services rolling stock types: "+typeNames);
    	for (carIndex=0; carIndex<carList.size(); carIndex++){
    		Car c = carManager.getCarById((String) carList.get(carIndex));
    		// remove cars that don't have a valid track
    		if (c.getTrack() == null){
    			addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildErrorCarNoLoc"),new Object[]{c.getId(), c.getLocationName(), c.getTrackName()}));
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
    		}
    		// all cars in staging must be accepted, so don't exclude if in staging
    		if (departStageTrack == null || !c.getTrack().getName().equals(departStageTrack.getName())){
    			if (!train.acceptsRoadName(c.getRoad())){
    				addLine(fileOut, THREE, "Exclude car ("+c.getId()+") road ("+c.getRoad()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (!train.acceptsTypeName(c.getType())){
    				addLine(fileOut, THREE, "Exclude car ("+c.getId()+") type ("+c.getType()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    		}
    		// is car at interchange?
    		if (c.getTrack().getLocType().equals(Track.INTERCHANGE)){
    			// don't service a car at interchange and has been dropped of by this train
    			if (c.getTrack().getPickupOption().equals(Track.ANY) && c.getSavedRouteId().equals(train.getRoute().getId())){
    				addLine(fileOut, THREE, "Exclude car ("+c.getId()+") previously droped by this train at interchange ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (c.getTrack().getPickupOption().equals(Track.TRAINS)){
    				if (c.getTrack().acceptsPickupTrain(train)){
    					log.debug("Car ("+c.getId()+") can be picked up by this train");
    				} else {
    					addLine(fileOut, THREE, "Exclude car ("+c.getId()+") by train, can't pickup this car at interchange ("+c.getLocationName()+", "+c.getTrackName()+")");
    					carList.remove(carList.get(carIndex));
    					carIndex--;
    					continue;
    				}
    			}
    			else if (c.getTrack().getPickupOption().equals(Track.ROUTES)){
    				if (c.getTrack().acceptsPickupRoute(train.getRoute())){
    					log.debug("Car ("+c.getId()+") can be picked up by this route");
    				} else {
    					addLine(fileOut, THREE, "Exclude car ("+c.getId()+") by route, can't pickup this car at interchange ("+c.getLocationName()+", "+c.getTrackName()+")");
    					carList.remove(carList.get(carIndex));
    					carIndex--;
    					continue;
    				}
    			}
    		}
		}

		addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildFoundCars"),new Object[]{Integer.toString(carList.size()), train.getName()}));

		// adjust carlist to only have cars from one staging track
		if (departStageTrack != null){
			// Make sure that all cars in staging are moved
			train.getTrainDepartsRouteLocation().setCarMoves(train.getTrainDepartsRouteLocation().getMaxCarMoves()-departStageTrack.getNumberCars());  // neg number moves more cars
			int numCarsFromStaging = 0; 
			for (carIndex=0; carIndex<carList.size(); carIndex++){
				Car c = carManager.getCarById((String) carList.get(carIndex));
//				addLine(fileOut, "Check car ("+c.getId()+") at location ("+c.getLocationName()+" "+c.getTrackName()+")");
				if (c.getLocationName().equals(departLocation.getName())){
					if (c.getTrackName().equals(departStageTrack.getName())){
						addLine(fileOut, THREE, "Staging car ("+c.getId()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
						numCarsFromStaging++;
					} else {
						addLine(fileOut, THREE, "Exclude car ("+c.getId()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
						carList.remove(carList.get(carIndex));
						carIndex--;
					}
				}
			}
			// error if all of the cars in staging aren't available
			if (numCarsFromStaging + numberEngines != departStageTrack.getNumberRS()){
				buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorNotAll"),
						new Object[]{Integer.toString(departStageTrack.getNumberRS()- (numCarsFromStaging + numberEngines))}));
				return;
			}
		}
		// now go through the car list and remove any that don't belong
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car c = carManager.getCarById((String) carList.get(carIndex));
			addLine(fileOut, FIVE, "Car (" +c.getId()+ ") at location (" +c.getLocationName()+ ", " +c.getTrackName()+ ") with " + c.getMoves()+ " moves");
			// use only the lead car in a kernel for building trains
			if (c.getKernel() != null){
				addLine(fileOut, FIVE, "Car (" +c.getId()+ ") is part of kernel ("+c.getKernelName()+")");
				if (!c.getKernel().isLeadCar(c)){
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
			}
			if (this.equals(c.getTrain())){
				addLine(fileOut, THREE, "Car (" +c.getId()+ ") already assigned to this train");
			}
			// does car have a destination that is part of this train's route?
			if (c.getDestination() != null) {
				addLine(fileOut, THREE, "Car (" + c.getId()+ ") has assigned destination (" +c.getDestination().getName()+ ")");
				RouteLocation rld = train.getRoute().getLocationByName(c.getDestination().getName());
				if (rld == null){
					addLine(fileOut, THREE, "Exclude car (" + c.getId()+ ") destination (" +c.getDestination().getName()+ ") not part of this train's route (" +train.getRoute().getName() +")");
					// is this car departing staging?
					if (c.getLocationName().equals(departLocation.getName()) && departStageTrack != null){
						buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorCarNotPartRoute"),
								new Object[]{c.getId()}));
						return;
					}
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
				}
			}
		}
		// now go through the car list and find a caboose or fred if required
		// try and find a caboose that matches the engine's road
		if(requiresCaboose && train.getCabooseRoad().equals("") && train.getLeadEngine() != null){
			for (carIndex=0; carIndex<carList.size(); carIndex++){
				Car car = carManager.getCarById((String) carList.get(carIndex));
				if (car.isCaboose() && car.getLocationName().equals(train.getTrainDepartsName()) && car.getRoad().equals(train.getLeadEngine().getRoad())){
					if (car.getDestination() == null || car.getDestination() == terminateLocation){
						addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildFoundCaboose"),new Object[]{car.getId()}));
						// remove all other cabooses from list
						for (int i=0; i<carList.size(); i++){
							Car testCar = carManager.getCarById((String) carList.get(i));
							if (testCar.isCaboose() && testCar != car){
								// need to keep it if departing staging
								if (departStageTrack == null || testCar.getTrack() != departStageTrack){
									addLine(fileOut, FIVE, "Exclude caboose ("+testCar.getId()+") at location ("+testCar.getLocationName()+", "+testCar.getTrackName()+")");
									carList.remove(carList.get(i));		// remove this car from the list
									i--;
								}
							}
						}
						break;
					}
				}
			}
		}
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car c = carManager.getCarById((String) carList.get(carIndex));
			// find a caboose or card with FRED for this train if needed
			// check for caboose or car with FRED
			if (c.isCaboose()){
				addLine(fileOut, FIVE, "Car (" +c.getId()+ ") is a caboose");
				if (departStageTrack != null && c.getTrack() == departStageTrack) 
					foundCaboose = false;		// must move caboose from staging   
			}
			if (c.hasFred()){
				addLine(fileOut, FIVE, "Car (" +c.getId()+ ") has a FRED");
				if (departStageTrack != null && c.getTrack() == departStageTrack) 
					foundFred = false;		// must move car with FRED from staging
			}
			
			// remove cabooses and cars with FRED if not needed for train
			if (c.isCaboose() && foundCaboose || c.hasFred() && foundFred){
				addLine(fileOut, THREE, "Exclude car ("+c.getId()+ ") type ("+c.getType()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
				carList.remove(carList.get(carIndex));		// remove this car from the list
				carIndex--;
				continue;
			}
			if (c.isCaboose() && !foundCaboose || c.hasFred() && !foundFred){	
				if(c.getLocationName().equals(train.getTrainDepartsName())){
					if (c.getDestination() == null || c.getDestination() == terminateLocation || departStageTrack != null){
						if (train.getCabooseRoad().equals("") || train.getCabooseRoad().equals(c.getRoad()) || departStageTrack != null){
							// find a track to place car
							if (train.getTrainTerminatesRouteLocation().getStagingTrack() == null){
								List sls = terminateLocation.getTracksByMovesList(null);
								for (int s = 0; s < sls.size(); s++){
									Track destTrack = terminateLocation.getTrackById((String)sls.get(s));
									String status = c.testDestination(terminateLocation, destTrack);
									if (status.equals(c.OKAY)){
										//TODO check to see if the caboose or car with FRED would exceed train length
										addCarToTrain(fileOut, c, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), terminateLocation, destTrack);
										if (c.isCaboose())
											foundCaboose = true;
										if (c.hasFred())
											foundFred = true;
										break;
									} else {
										addLine(fileOut, SEVEN, "Can not drop car ("+c.getId()+") to track (" +destTrack.getName()+") because of "+status);
									}
								}
								if (!foundCaboose || !foundFred)
									addLine(fileOut, THREE, "Could not find a destination for ("+c.getId()+")");
							} else {
								// terminate into staging
								String status = c.testDestination(terminateLocation, train.getTrainTerminatesRouteLocation().getStagingTrack());
								if (status.equals(c.OKAY)){
									//TODO check to see if the caboose or car with FRED would exceed train length
									addCarToTrain(fileOut, c, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), terminateLocation, train.getTrainTerminatesRouteLocation().getStagingTrack());
									if (c.isCaboose())
										foundCaboose = true;
									if (c.hasFred())
										foundFred = true;
								} else {
									addLine(fileOut, SEVEN, "Can not drop car ("+c.getId()+") to track (" +train.getTrainTerminatesRouteLocation().getStagingTrack().getName()+") because of "+status);
								}
							}
						}
					}
				} // caboose or FRED not at departure locaton so remove from list
				if(!foundCaboose || !foundFred) {
					addLine(fileOut, THREE, "Exclude car ("+c.getId()+ ") type ("+c.getType()+ ") at location ("+c.getLocationName()+" "+c.getTrackName()+")");
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
				}
			}
		}
		if (requiresFred && !foundFred || requiresCaboose && !foundCaboose){
			buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorRequirements"),
					new Object[]{train.getName(), textRequires, train.getTrainDepartsName(), train.getTrainTerminatesName()}));
			return;
		}
		addLine(fileOut, THREE, "Requested cars (" +requested+ ") for train (" +train.getName()+ ") the number available (" +carList.size()+ ") building train!");

		// now find destinations for cars 
		int numLocs = routeList.size();
		if (numLocs > 1)  // don't find car destinations for the last location in the route
			numLocs--;
		for (int locationIndex=0; locationIndex<numLocs; locationIndex++){
			RouteLocation rl = train.getRoute().getLocationById((String)routeList.get(locationIndex));
			if(train.skipsLocation(rl.getId())){
				addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildLocSkipped"),new Object[]{rl.getName(), train.getName()}));
			}else if(!rl.canPickup()){
				addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildLocNoPickups"),new Object[]{train.getRoute().getName(), rl.getName()}));
			}else{
				moves = 0;
				success = false;
				reqNumOfMoves = rl.getMaxCarMoves()-rl.getCarMoves();
				int saveReqMoves = reqNumOfMoves;
				addLine(fileOut, THREE, "Location (" +rl.getName()+ ") requests " +reqNumOfMoves+ " moves");
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
								addLine(fileOut, THREE, "Car (" + c.getId()+ ") at location (" +c.getLocation()+ ") has assigned destination (" +c.getDestination()+ ")");
								RouteLocation rld = train.getRoute().getLocationByName(c.getDestination().getName());
								if (rld == null){
									addLine(fileOut, THREE, "Car (" + c.getId()+ ") destination not part of route (" +train.getRoute().getName() +")");
								} else {
									if (c.getRouteLocation() != null){ 
										addLine(fileOut, THREE, "Car (" + c.getId()+ ") already assigned to this train");
									} 
									boolean carAdded = false;
									// are drops allows at this location?
									if (!rld.canDrop()){
										addLine(fileOut, THREE, "Route ("+train.getRoute().getName()+") does not allow drops at location ("+rld.getName()+")");
									} else if (rld.getCarMoves() < rld.getMaxCarMoves()){  
										carAdded = addCarToTrain(fileOut, c, rl, rld, c.getDestination(), c.getDestinationTrack());
										// done with this location?
										if (carAdded && success)
											break;	//yes
									} else {
										addLine(fileOut, THREE, "No available moves for destination ("+rld.getName()+")");
									}
									if (!carAdded)
										addLine(fileOut, THREE, "Car (" + c.getId()+ ") can not be dropped to track (" + c.getDestinationTrack() + ")");
									// is this car departing staging?
									if (!carAdded && c.getLocationName().equals(departLocation.getName()) && departStageTrack != null){
										buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorCarStageDest"),
												new Object[]{c.getId()}));
										return;
									}
								}
							// car does not have a destination, search for one	
							} else {
								addLine(fileOut, FIVE, "Find destinations for car ("+c.getId()+") at location (" +c.getLocationName()+", " +c.getTrackName()+ ")");
								int start = locationIndex;				// start looking after car's current location
								RouteLocation rld = null;				// the route location destination being checked for the car
								RouteLocation rldSave = null;			// holds the best route location destination for the car
								Track trackSave = null;					// holds the best track at destination for the car
								Location destinationSave = null;		// holds the best destination for the car
						
								// more than one location in this route?
								if (routeList.size()>1)
									start++;		//yes!, no car drops at departure
								for (int k = start; k<routeList.size(); k++){
									rld = train.getRoute().getLocationById((String)routeList.get(k));
									if (rld.canDrop()){
										addLine(fileOut, FIVE, "Searching location ("+rld.getName()+") for possible destination");
									}else{
										addLine(fileOut, FIVE, "Route ("+train.getRoute().getName()+") does not allow drops at location ("+rld.getName()+")");
										continue;
									}							
									// don't move car to same location unless the route only has one location (local moves)
									if (!rl.getName().equals(rld.getName()) || routeList.size() == 1){
										Location destinationTemp = null;
										Track trackTemp = null;
										// any moves left at this location?
										if (rld.getMaxCarMoves()-rld.getCarMoves()>0){
											// get a "test" destination and a list of the track locations available
											noMoreMoves = false;
											Location testDestination = locationManager.getLocationByName(rld.getName());
											if (testDestination == null){
												buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorRouteLoc"),
														new Object[]{train.getRoute().getName(), rld.getName()}));
												return;
											}
											// is there a track assigned for staging cars?
											if (rld.getStagingTrack() == null){
												List sls = testDestination.getTracksByMovesList(null);
												for (int s = 0; s < sls.size(); s++){
													Track testTrack = testDestination.getTrackById((String)sls.get(s));
													// log.debug("track (" +testTrack.getName()+ ") has "+ testTrack.getMoves() + " moves");
													// need to find a track that is isn't the same as the car's current
													String status = c.testDestination(testDestination, testTrack);
													if (testTrack != c.getTrack() 
															&& status.equals(c.OKAY) 
															&& checkDropTrainDirection(fileOut, c, rld, testDestination, testTrack)){
														// staging track with zero cars?
														if (testTrack.getLocType().equals(testTrack.STAGING) && (testTrack.getNumberRS() == 0 || testTrack.getDropRS()>0)){
															rld.setStagingTrack(testTrack);	// Use this location for all cars
															trackTemp = testTrack;
															destinationTemp = testDestination;
															break;
														}
														// No local moves from siding to siding
														if (routeList.size() == 1 && testTrack.getLocType().equals(testTrack.SIDING) && c.getTrack().getLocType().equals(testTrack.SIDING)){
															addLine(fileOut, FIVE, "Local siding to siding move not allowed (" +testTrack.getName()+ ")");
															continue;
														}
														// No local moves from yard to yard
														if (routeList.size() == 1 && testTrack.getLocType().equals(testTrack.YARD) && c.getTrack().getLocType().equals(testTrack.YARD)){
															addLine(fileOut, FIVE, "Local yard to yard move not allowed (" +testTrack.getName()+ ")");
															continue;
														}
														// No local moves from interchange to interchange
														if (routeList.size() == 1 && testTrack.getLocType().equals(testTrack.INTERCHANGE) && c.getTrack().getLocType().equals(testTrack.INTERCHANGE)){
															addLine(fileOut, FIVE, "Local interchange to interchange move not allowed (" +testTrack.getName()+ ")");
															continue;
														}
														// drop to interchange?
														if (testTrack.getLocType().equals(testTrack.INTERCHANGE)){
															if (testTrack.getDropOption().equals(testTrack.TRAINS)){
																if (testTrack.acceptsDropTrain(train)){
																	log.debug("Car ("+c.getId()+") can be droped by train to interchange (" +testTrack.getName()+")");
																} else {
																	addLine(fileOut, FIVE, "Can't drop car ("+c.getId()+") by train to interchange (" +testTrack.getName()+")");
																	continue;
																}
															}
															if (testTrack.getDropOption().equals(testTrack.ROUTES)){
																if (testTrack.acceptsDropRoute(train.getRoute())){
																	log.debug("Car ("+c.getId()+") can be droped by route to interchange (" +testTrack.getName()+")");
																} else {
																	addLine(fileOut, FIVE, "Can't drop car ("+c.getId()+") by route to interchange (" +testTrack.getName()+")");
																	continue;
																}
															}
														}
														// not staging, then use
														if (!testTrack.getLocType().equals(testTrack.STAGING)){
															trackTemp = testTrack;
															destinationTemp = testDestination;
															break;
														}
													}
													// car's current track is the test track or car can't be dropped
													if(!status.equals(c.OKAY)){
														addLine(fileOut, SEVEN, "Can not drop car ("+c.getId()+") to track (" +testTrack.getName()+") because of "+status);
													}
												}
											// all cars in this train go to one staging track
											} else {
												// will staging accept this car?
												String status = c.testDestination(testDestination, rld.getStagingTrack());
												if (status.equals(c.OKAY)){
													trackTemp = rld.getStagingTrack();
													destinationTemp = testDestination;
												} else {
													addLine(fileOut, SEVEN, "Can not drop car ("+c.getId()+") to track (" +rld.getStagingTrack().getName()+") because of "+status);
												}
											}
											if(destinationTemp != null){
												addLine(fileOut, THREE, "Car ("+c.getId()+") can drop to (" +destinationTemp.getName()+ ", " +trackTemp.getName()+ ") with " 
														+rld.getCarMoves()+ "/" +rld.getMaxCarMoves()+" moves");
												// if there's more than one available destination use the one with the least moves
												if (rldSave != null){
													double saveCarMoves = rldSave.getCarMoves();
													double saveRatio = saveCarMoves/rldSave.getMaxCarMoves();
													double nextCarMoves = rld.getCarMoves();
													double nextRatio = nextCarMoves/rld.getMaxCarMoves();
													log.debug(rldSave.getName()+" = "+Double.toString(saveRatio)+ " " + rld.getName()+" = "+Double.toString(nextRatio));
													if (saveRatio < nextRatio){
														rld = rldSave;					// the saved is better than the last found
														destinationTemp = destinationSave;
														trackTemp = trackSave;
													}
												}
												// every time through, save the best route location, destination, and track
												rldSave = rld;
												destinationSave = destinationTemp;
												trackSave = trackTemp;
											} else {
												addLine(fileOut, FIVE, "Could not find a valid destination for car ("+c.getId()+") at location (" + rld.getName()+")");
											}
										} else {
											addLine(fileOut, FIVE, "No available moves for destination ("+rld.getName()+")");
										}
									} else{
										addLine(fileOut, FIVE, "Car ("+c.getId()+") location is equal to destination ("+rld.getName()+"), skiping this destination");
									}
								}
								boolean carAdded = false; // all cars departing staging must be included or build failure
								if (destinationSave != null){
									carAdded = addCarToTrain(fileOut, c, rl, rldSave, destinationSave, trackSave);
									if (carAdded && success){
										//log.debug("done with location ("+destinationSave.getName()+")");
										break;
									}
								} 
								// car leaving staging without a destinaton?
								if (c.getTrack().getLocType().equals(Track.STAGING) && (!carAdded  || destinationSave == null)){
									buildFailed(fileOut, MessageFormat.format(rb.getString("buildErrorCarDest"),
											new Object[]{c.getId(), c.getLocationName()}));
									return;
									// are there still moves available?
								} 
								if (noMoreMoves) {
									addLine(fileOut, FIVE, "No available destinations for any car");
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
				addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildStatusMsg"),new Object[]{(success? rb.getString("Success"): rb.getString("Partial")),
				Integer.toString(moves), Integer.toString(saveReqMoves), rl.getName(), train.getName()}));
			}
		}

		train.setCurrentLocation(train.getTrainDepartsRouteLocation());
		if (numberCars < requested){
			train.setStatus(PARTIALBUILT + train.getNumberCarsWorked() +"/" + requested + " "+ rb.getString("cars"));
			addLine(fileOut, ONE, PARTIALBUILT + train.getNumberCarsWorked() +"/" + requested + " "+ rb.getString("cars"));
		}else{
			train.setStatus(BUILT + train.getNumberCarsWorked() + " "+ rb.getString("cars"));
			addLine(fileOut, ONE, BUILT + train.getNumberCarsWorked() + " "+ rb.getString("cars"));
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
	
	// get the engines for this train. If departStageTrack != null, then engines must
	// come from that track location (staging).  Returns true if engines found, else false.
	// This routine will also pick the destination track if the train is
	// terminating into staging, therefore this routine should only be called once when return is true.
	private boolean getEngines(PrintWriter fileOut){
		// show engine types that this train will service
		String[] engineTypes = EngineTypes.instance().getNames();
		String typeNames ="";
    	for (int i=0; i<engineTypes.length; i++){
    		if (train.acceptsTypeName(engineTypes[i]))
    			typeNames = typeNames + engineTypes[i]+" ";
    	}
    	addLine(fileOut, FIVE, "Train ("+train.getName()+") services engine types: "+typeNames);
		// show engine requirements for this train
		addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildTrainReqEngine"),new Object[]{train.getNumberEngines(), train.getEngineModel(), train.getEngineRoad()}));
				
		numberEngines = 0;
		int reqNumEngines = 0; 	
		int engineLength = 0;
		
		if (train.getNumberEngines().equals(train.AUTO)){
			reqNumEngines = getAutoEngines(fileOut);
		} else {
			reqNumEngines = Integer.parseInt(train.getNumberEngines());
		}
		// if leaving staging, use any number of engines if required number is 0
		boolean leavingStaging = false;
		if (departStageTrack != null && reqNumEngines == 0)
			leavingStaging = true;
		if (!leavingStaging && reqNumEngines == 0)
			return true;

		// get list of engines for this route
		
		List engineList = engineManager.getEnginesAvailableTrainList(train);
		// remove engines not at departure, wrong road name, or part of consist (not lead)
		for (int indexEng=0; indexEng<engineList.size(); indexEng++){
			Engine engine = engineManager.getEngineById((String) engineList.get(indexEng));
			addLine(fileOut, FIVE, "Engine ("+engine.getId()+") road ("+engine.getRoad()+") model ("+engine.getModel()+") type ("+engine.getType()+")");
			addLine(fileOut, FIVE, " at location ("+engine.getLocationName()+", "+engine.getTrackName()+")");
			// remove engines with types that train does not service
			if (!train.acceptsTypeName(engine.getType())){
				addLine(fileOut, THREE, "Exclude engine ("+engine.getId()+"), type ("+engine.getType()+") is not serviced by this train");
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines that have been assigned destinations
			if (engine.getDestination() != null && !engine.getDestination().equals(terminateLocation)){
				addLine(fileOut, THREE, "Exclude engine ("+engine.getId()+") it has an assigned destination ("+engine.getDestination().getName()+")");
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// determine if engine is departing from staging track (departStageTrack != null if staging) 
			if(engine.getLocationName().equals(train.getTrainDepartsName()) && (departStageTrack == null || engine.getTrackName().equals(departStageTrack.getName()))){
				if ((train.getEngineRoad().equals("") || engine.getRoad().equals(train.getEngineRoad())) && (train.getEngineModel().equals("") || engine.getModel().equals(train.getEngineModel()))){
					// is this engine part of a consist?  Keep only lead engines in consist if required number is correct.
					if (engine.getConsist() != null){
						if (!engine.getConsist().isLeadEngine(engine)){
							addLine(fileOut, THREE, "Engine ("+engine.getId()+") is part of consist ("+engine.getConsist().getName()+") and has " + engine.getConsist().getEngines().size() + " engines");
							// only use lead engines
							engineList.remove(indexEng);
							indexEng--;
						}else{
							addLine(fileOut, THREE, "Engine ("+engine.getId()+") is lead engine for consist ("+engine.getConsist().getName()+") and has " + engine.getConsist().getEngines().size() + " engines");
							List cEngines = engine.getConsist().getEngines();
							if (cEngines.size() == reqNumEngines || leavingStaging){
								log.debug("Consist ("+engine.getConsist().getName()+") has the required number of engines");
							}else{
								log.debug("Consist ("+engine.getConsist().getName()+") doesn't have the required number of engines");
								addLine(fileOut, THREE, "Exclude consist ("+engine.getConsist().getName()+")");
								engineList.remove(indexEng);
								indexEng--;
							}
						}
						continue;
						// Single engine, does train require a consist?
					} else if (reqNumEngines == 1)
						continue;	// no keep this engine
				} 
			}
			addLine(fileOut, THREE, "Exclude engine ("+engine.getId()+")");
			engineList.remove(indexEng);
			indexEng--;
		}

		// now load the number of engines into the train
		Track terminateTrack = null;
		for (int indexEng=0; indexEng<engineList.size(); indexEng++){
			Engine engine = engineManager.getEngineById((String) engineList.get(indexEng));
			train.setLeadEngine(engine);	//load lead engine
			// find a track for engine(s) at destination
			List destTracks = terminateLocation.getTracksByMovesList(null);
			for (int s = 0; s < destTracks.size(); s++){
				terminateTrack = terminateLocation.getTrackById((String)destTracks.get(s));
				if (terminateTrack.getLocType().equals(Track.STAGING) && (terminateTrack.getNumberRS()>0 || terminateTrack.getDropRS()>0)){
					terminateTrack = null;
					continue;
				}
				String status = engine.testDestination(terminateLocation, terminateTrack);
				if(status == Engine.OKAY){
					break;
				} else {
					addLine(fileOut, FIVE, "Can not drop engine ("+engine.getId()+") to track (" +terminateTrack.getName()+") because of "+status);
					terminateTrack = null;
				}
			}
			if (terminateTrack == null && (reqNumEngines>0 || leavingStaging)){
				addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildNoDestEngine"),new Object[]{engine.getId(),
				terminateLocation.getName(), train.getName()}));
			}
			if (terminateTrack != null){
				if (engine.getConsist() != null){
					List cEngines = engine.getConsist().getEngines();
					if (cEngines.size() == reqNumEngines || leavingStaging){
						engineLength = engine.getConsist().getLength();
						for (int j=0; j<cEngines.size(); j++){
							numberEngines++;
							Engine cEngine = (Engine)cEngines.get(j);
							addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildEngineAssigned"),new Object[]{cEngine.getId(), terminateLocation.getName(), terminateTrack.getName()}));
							cEngine.setTrain(train);
							cEngine.setRouteLocation(train.getTrainDepartsRouteLocation());
							cEngine.setRouteDestination(train.getTrainTerminatesRouteLocation());
							cEngine.setDestination(terminateLocation, terminateTrack);
						}
						break;  // done with loading engines
						// consist has the wrong number of engines, remove 	
					} else {
						addLine(fileOut, THREE, "Exclude engine ("+engine.getId()+") consist ("+engine.getConsist().getName()+") number of engines (" +cEngines.size()+ ")");
						engineList.remove(indexEng);
						indexEng--;
					}
					// engine isn't part of a consist
				} else if (reqNumEngines ==1 || leavingStaging){
					numberEngines++;
					addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildEngineAssigned"),new Object[]{engine.getId(), terminateLocation.getName(), terminateTrack.getName()}));
					engine.setTrain(train);
					engine.setRouteLocation(train.getTrainDepartsRouteLocation());
					engine.setRouteDestination(train.getTrainTerminatesRouteLocation());
					engine.setDestination(terminateLocation, terminateTrack);
					engineLength = Integer.parseInt(engine.getLength());
					break;  // done with loading engine
				}
			}
		}
		if (numberEngines < reqNumEngines){
			addLine(fileOut, ONE, rb.getString("buildCouldNotFindEng"));
			return false;
		}
		
		// set the engine length for locations
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = train.getRoute().getLocationById((String)routeList.get(i));
			rl.setTrainLength(engineLength);		// load the engine(s) length
		}
		// terminating into staging?
		if (terminateTrack != null && terminateTrack.getLocType().equals(terminateTrack.STAGING)){
			train.getTrainTerminatesRouteLocation().setStagingTrack(terminateTrack);
		}
		return true;
	}
	
	// returns the number of engines needed for this train, minimum 1, 
	// maximum user specified in setup.
	// Based on maximum allowable train length and grade between locations,
	// and the maximum cars that the train can have at the maximum train length.
	// TODO Currently ignores the cars weight and engine horsepower
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
		addLine(fileOut, ONE, MessageFormat.format(rb.getString("buildAutoBuildMsg"),new Object[]{Integer.toString(nE)}));
		if (nE > Setup.getEngineSize()){
			addLine(fileOut, THREE, "The maximum number of engines that can be assigned is "+Setup.getEngineSize());
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
	 * @param track the final destination for car
	 * @return true if car was successfully added to train.  Also makes boolean
	 * boolean "success" true if location doesn't need any more pickups. 
	 */
	private boolean addCarToTrain(PrintWriter file, Car car, RouteLocation rl, RouteLocation rld, Location destination, Track track){
		if (checkTrainLength(file, car, rl, rld)){
			int oldNum = moves;
			// car could be part of a kernel
			if (car.getKernel()!=null){
				List kCars = car.getKernel().getCars();
				addLine(file, THREE, "Car ("+car.getId()+") is part of kernel ("+car.getKernelName()+") with "+ kCars.size() +" cars");
				// log.debug("kernel length "+car.getKernel().getLength());
				for(int i=0; i<kCars.size(); i++){
					Car kCar = (Car)kCars.get(i);
					addLine(file, THREE, "Car ("+kCar.getId()+") assigned destination ("+destination.getName()+", "+track.getName()+")");
					kCar.setTrain(train);
					kCar.setRouteLocation(rl);
					kCar.setRouteDestination(rld);
					kCar.setDestination(destination, track);
				}
				// not part of kernel, add one car	
			} else {
				addLine(file, THREE, "Car ("+car.getId()+") assigned destination ("+destination.getName()+", "+track.getName()+")");
				car.setTrain(train);
				car.setRouteLocation(rl);
				car.setRouteDestination(rld);
				car.setDestination(destination, track);
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
						log.debug ("Car ("+car.getId()+") weight not set");
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
			trainDir = Track.NORTH;
		else if (trainDirection.equals(rl.SOUTH))
			trainDir = Track.SOUTH;
		else if (trainDirection.equals(rl.EAST))
			trainDir = Track.EAST;
		else if (trainDirection.equals(rl.WEST))
			trainDir = Track.WEST;
		
		if ((trainDir & car.getLocation().getTrainDirections() & car.getTrack().getTrainDirections()) >0)
			return true;
		else {
			addLine(file, FIVE, "Can not pick up car ("+car.getId()+") using "
					+trainDirection+"bound train, location");
			addLine(file, FIVE, " ("+car.getLocation().getName()
					+", "+car.getTrack().getName()+") does not service this direction");
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
				addLine(file, FIVE, "Can not pick up car ("+car.getId()+") length ("+length+") using train,");
				addLine(file, FIVE, " it would exceed train length restrication at "+rlt.getName());
				return false;
			}
		}
		return true;
	}
	
	private boolean checkDropTrainDirection (PrintWriter file, Car car, RouteLocation rld, Location destination, Track track){
		// is the destination the last location on the route? 
		if (rld == train.getTrainTerminatesRouteLocation())
			return true;	// yes, ignore train direction
		String trainDirection = rld.getTrainDirection();	// train direction North, South, East or West
		// convert train direction to binary bit map, only one bit set 
		int trainDir = 0;
		if (trainDirection.equals(rld.NORTH))
			trainDir = Track.NORTH;
		else if (trainDirection.equals(rld.SOUTH))
			trainDir = Track.SOUTH;
		else if (trainDirection.equals(rld.EAST))
			trainDir = Track.EAST;
		else if (trainDirection.equals(rld.WEST))
			trainDir = Track.WEST;
		int serviceTrainDir = (destination.getTrainDirections() & track.getTrainDirections()); // this location only services trains with these directions
		if ((serviceTrainDir & trainDir) >0){
			return true;
		} else {
			addLine(file, FIVE, "Can not drop car ("+car.getId()+") using "+trainDirection+"bound train,");
			addLine(file, FIVE, " destination track ("+track+") does not service this direction");
			return false;
		}
	}

	private void buildFailed(PrintWriter file, String string){
		train.setStatus(BUILDFAILED);
		if(log.isDebugEnabled())
			log.debug(string);
		JOptionPane.showMessageDialog(null, string,
				MessageFormat.format(rb.getString("buildErrorMsg"),new Object[]{train.getName(), train.getDescription()}),
				JOptionPane.ERROR_MESSAGE);
		if (file != null){
			addLine(file, ONE, string);
			// Write to disk and close file
			addLine(file, ONE, MessageFormat.format(rb.getString("buildFailedMsg"),new Object[]{train.getName()}));
			file.flush();
			file.close();
			if(TrainManager.instance().getBuildReport()){
				File buildFile = TrainManagerXml.instance().getTrainBuildReportFile(train.getName());
				printBuildReport(buildFile, MessageFormat.format(rb.getString("buildFailureReport"),new Object[]{train.getDescription()}), true);
			}
		}
	}
	
	private static void printBuildReport(File file, String name, boolean isPreview){
		Train.printReport(file, name, isPreview, "", true);
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
		String comment = "";
		for (int i =0; i < engineList.size(); i++){
			engine = engineManager.getEngineById((String) engineList.get(i));
			comment = (Setup.isAppendCarCommentEnabled() ? " "+engine.getComment() : "");
			addLine(fileOut, BOX + rb.getString("Engine")+" "+ engine.getRoad() + " " + engine.getNumber() + " (" +engine.getModel()+  ") "+rb.getString("assignedToThisTrain") + comment);
		}
		
		if (engine != null)
			addLine(fileOut, rb.getString("PickupEngineAt")+ " "+engine.getLocationName()+", "+engine.getTrackName());
		
		List carList = carManager.getCarsByTrainDestinationList(train);
		log.debug("Train has " + carList.size() + " cars assigned to it");
		int cars = 0;
		List routeList = train.getRoute().getLocationsBySequenceList();
		for (int r = 0; r < routeList.size(); r++) {
			RouteLocation rl = train.getRoute().getLocationById((String) routeList.get(r));
			newLine(fileOut);
			if (r == 0)
				addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + rl.getName() +", "+rb.getString("departureTime")+" "+train.getDepartureTime());
			else
				addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + rl.getName() +", "+rb.getString("estimatedArrival")+" "+train.getExpectedArrivalTime(rl));
			// block cars by destination
			for (int j = r; j < routeList.size(); j++) {
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
			if (r != routeList.size() - 1) {
				addLine(fileOut, rb.getString("TrainDeparts")+ " " + rl.getName() +" "+ rl.getTrainDirection()
						+ rb.getString("boundWith") +" " + cars + " " +rb.getString("cars")+", " +rl.getTrainLength()
						+" "+rb.getString("feet")+", "+rl.getTrainWeight()+" "+rb.getString("tons"));
			} else {
				if(engine != null)
					addLine(fileOut, BOX +rb.getString("DropEngineTo")+ " "+ engine.getDestinationTrackName()); 
				addLine(fileOut, rb.getString("TrainTerminatesIn")+ " " + rl.getName());
			}
		}
		fileOut.flush();
		fileOut.close();
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(TrainBuilder.class.getName());

}
