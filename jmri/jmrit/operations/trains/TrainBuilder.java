package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Schedule;
import jmri.jmrit.operations.locations.ScheduleItem;
import jmri.jmrit.operations.locations.ScheduleManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.router.Router;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a train and creates the train's manifest. 
 * 
 * @author Daniel Boudreau  Copyright (C) 2008, 2009, 2010
 * @version             $Revision: 1.108 $
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
	Train train;				// the train being built
	int numberEngines = 0;		// number of engines assigned to this train
	int numberCars = 0;			// how many cars are moved by this train
	int reqNumEngines = 0; 		// the number of engines required for this train
	List<String> engineList;	// list of engines available for this train
	int carIndex;				// index for carList
	List<String> carList;		// list of cars available for this train
	List<String> routeList;		// list of locations from departure to termination served by this train
	int moves;					// the number of pickup car moves for a location
	double maxWeight = 0;			// the maximum weight of cars in train
	int reqNumOfMoves;			// the requested number of car moves for a location
	Location departLocation;	// train departs this location
	Track departStageTrack;		// departure staging track (null if not staging)	
	Location terminateLocation; // train terminate at this location
	Track terminateStageTrack; 	// terminate staging track (null if not staging)
	boolean success;			// true when enough cars have been picked up from a location
	PrintWriter buildReport;	// build report for this train
	boolean noMoreMoves;
	
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
	 * 5. All cars and engines must leave staging tracks
	 * 6  When departing staging find a track matching train requirements
	 * 7. If a train is assigned to staging, all cars and engines must go there  
	 * 8. Service locations based on train direction, location car types and roads
	 * 9. Ignore track direction when train is a local (serves one location)
	 *
	 * @param train the train that is to be built
	 * 
	 * message windows.
	 */
	public void build(Train train){
		log.debug("Building train "+train.getName());
		this.train = train;
		train.setStatus(BUILDING);
		train.setBuilt(false);
		train.setLeadEngine(null);
		
		// create build status file
		File file = TrainManagerXml.instance().createTrainBuildReportFile(train.getName());
		try {
			buildReport = new PrintWriter(new BufferedWriter(new FileWriter(file)),
					true);
		} catch (IOException e) {
			log.error("can not open build status file");
			return;
		}		
		addLine(buildReport, ONE, MessageFormat.format(rb.getString("BuildReportMsg"),new Object[]{train.getName(), new Date()}));
		
		if (train.getRoute() == null){
			buildFailed(MessageFormat.format(rb.getString("buildErrorRoute"),new Object[]{train.getName()}));
			return;
		}
		// get the train's route
		routeList = train.getRoute().getLocationsBySequenceList();
		if (routeList.size() < 1){
			buildFailed(MessageFormat.format(rb.getString("buildErrorNeedRoute"),new Object[]{train.getName()}));
			return;
		}
		// train departs
		departLocation = locationManager.getLocationByName(train.getTrainDepartsName());
		if (departLocation == null){
			buildFailed(MessageFormat.format(rb.getString("buildErrorNeedDepLoc"),new Object[]{train.getName()}));
			return;
		}
		// train terminates
		terminateLocation = locationManager.getLocationByName(train.getTrainTerminatesName());
		if (terminateLocation == null){
			buildFailed(MessageFormat.format(rb.getString("buildErrorNeedTermLoc"),new Object[]{train.getName()}));
			return;
		}		  
		// TODO: DAB control minimal build by each train
		if (train.getTrainDepartsRouteLocation().getMaxCarMoves() > departLocation.getNumberRS() && Control.fullTrainOnly){
			buildFailed(MessageFormat.format(rb.getString("buildErrorCars"),new Object[]{Integer.toString(departLocation.getNumberRS()),
				train.getTrainDepartsName(), train.getName()}));
			return;
		}
		// get the number of requested car moves for this train
		int requested = 0;
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(i));
			// check to see if there's a location for each stop in the route
			Location l = locationManager.getLocationByName(rl.getName());
			if (l == null){
				buildFailed(MessageFormat.format(rb.getString("buildErrorLocMissing"),new Object[]{train.getRoute().getName()}));
				return;
			}
			// train doesn't drop or pickup cars from staging locations found in middle of a route
			List<String> slStage = l.getTracksByMovesList(Track.STAGING);
			if (slStage.size() > 0 && i!=0 && i!=routeList.size()-1){
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildLocStaging"),new Object[]{rl.getName()}));
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			}
			// if a location is skipped, no car drops or pickups
			else if(train.skipsLocation(rl.getId())){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildLocSkipped"),new Object[]{rl.getName(), train.getName()}));
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			}
			// skip if a location doesn't allow drops or pickups
			else if(!rl.canDrop() && !rl.canPickup()){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildLocNoDropsOrPickups"),new Object[]{rl.getName()}));
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			}
			else{
				// we're going to use this location, so initialize the location
				requested = requested + rl.getMaxCarMoves(); // add up the total number of car moves requested
				rl.setCarMoves(0);					// clear the number of moves
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildLocRequestMoves"),new Object[]{rl.getName(), rl.getMaxCarMoves()}));
			}
			rl.setTrainWeight(0);					// clear the total train weight 
			rl.setTrainLength(0);					// and length
		}
		int numMoves = requested;	// number of car moves
		if(routeList.size()> 1)
			requested = requested/2;  // only need half as many cars to meet requests
		addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildRouteRequest"),new Object[]{train.getRoute().getName(), Integer.toString(requested), Integer.toString(numMoves)}));
			
    	// show road names that this train will service
		if (!train.getRoadOption().equals(Train.ALLROADS)){
			String[] roads = train.getRoadNames();
			StringBuffer sbuf = new StringBuffer("");    	
	    	for (int i=0; i<roads.length; i++){
	    		sbuf = sbuf.append(roads[i]+", ");
	    	}	       	
	        if (sbuf.length() > 2) sbuf.setLength(sbuf.length()-2);	// remove trailing separators
	    	addLine(buildReport, FIVE, "Train ("+train.getName()+") "+train.getRoadOption()+" roads: "+sbuf.toString());
		}
		// show load names that this train will service
		if (!train.getLoadOption().equals(Train.ALLLOADS)){
			String[] loads = train.getLoadNames();
			StringBuffer sbuf = new StringBuffer("");    	
	    	for (int i=0; i<loads.length; i++){
	    		sbuf = sbuf.append(loads[i]+", ");
	    	}
	        if (sbuf.length() > 2) sbuf.setLength(sbuf.length()-2); // remove trailing separators
	    	addLine(buildReport, FIVE, "Train ("+train.getName()+") "+train.getLoadOption()+" loads: "+sbuf.toString());
		}
		// show owner names that this train will service
		if (!train.getOwnerOption().equals(Train.ALLOWNERS)){
			String[] owners = train.getOwnerNames();
			StringBuffer sbuf = new StringBuffer("");    	
	    	for (int i=0; i<owners.length; i++){
	    		sbuf = sbuf.append(owners[i]+", ");
	    	}
	        if (sbuf.length() > 2) sbuf.setLength(sbuf.length()-2); // remove trailing separators
	    	addLine(buildReport, FIVE, "Train ("+train.getName()+") "+train.getOwnerOption()+" owners: "+sbuf.toString());
		}
		// show built date serviced
		if (!train.getBuiltStartYear().equals(""))
			addLine(buildReport, FIVE, "Train ("+train.getName()+") will service rolling stock built after "+train.getBuiltStartYear());
		if (!train.getBuiltEndYear().equals(""))
			addLine(buildReport, FIVE, "Train ("+train.getName()+") will service rolling stock built before "+train.getBuiltEndYear());
		
		// show engine info
		if (train.getNumberEngines().equals(Train.AUTO)){
			reqNumEngines = getAutoEngines();
		} else {
			reqNumEngines = Integer.parseInt(train.getNumberEngines());
		}	
		// show engine types that this train will service
		if (reqNumEngines >0){
			addLine(buildReport, FIVE, "Train ("+train.getName()+") services engine types:");
			String[] engineTypes = EngineTypes.instance().getNames();
			StringBuffer sbuf = new StringBuffer("");
			for (int i=0; i<engineTypes.length; i++){
				if (train.acceptsTypeName(engineTypes[i]))
					sbuf = sbuf.append(engineTypes[i]+", ");
				if (sbuf.length() > 77){
					addLine(buildReport, FIVE, sbuf.toString());
					sbuf.delete(0, sbuf.length());
				}
			}
			// remove trailing separators
	        if (sbuf.length() > 2){
	        	sbuf.setLength(sbuf.length()-2);	
	        	addLine(buildReport, FIVE, sbuf.toString());
	        }
		}		
		// show engine requirements for this train
		if (reqNumEngines == 0)
			addLine(buildReport, ONE, rb.getString("buildTrainReq0Engine"));
		else if (reqNumEngines == 1)
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTrainReq1Engine"),new Object[]{train.getEngineModel(), train.getEngineRoad()}));
		else
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTrainReqEngine"),new Object[]{train.getNumberEngines(), train.getEngineModel(), train.getEngineRoad()}));
	
		// does train terminate into staging?
		terminateStageTrack = null;
		List<String> stagingTracksTerminate = terminateLocation.getTracksByMovesList(Track.STAGING);
		if (stagingTracksTerminate.size() > 0){
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTerminateStaging"),new Object[]{terminateLocation.getName(), Integer.toString(stagingTracksTerminate.size())}));
			for (int i=0; i<stagingTracksTerminate.size(); i++){
				terminateStageTrack = terminateLocation.getTrackById(stagingTracksTerminate.get(i));
				if (checkTerminateStagingTrack()){
					addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildStagingAvail"),new Object[]{terminateStageTrack.getName(), terminateLocation.getName()}));
					break;
				} 
				terminateStageTrack = null;
			}
			if (terminateStageTrack == null){
				addLine(buildReport, ONE, rb.getString("buildErrorStagingFullNote"));
				buildFailed(MessageFormat.format(rb.getString("buildErrorStagingFull"),new Object[]{terminateLocation.getName()}));
				return;
			}
		}
		
		if (!removeEngines()){	// remove engines not serviced by this train
			buildFailed(MessageFormat.format(rb.getString("buildErrorEngines"),new Object[]{reqNumEngines, train.getTrainDepartsName()}));
			return;
		}
		
		// determine if train is departing staging
		departStageTrack = null;
		List<String> stagingTracks = departLocation.getTracksByMovesList(Track.STAGING);
		if (stagingTracks.size()>0){
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildDepartStaging"),new Object[]{departLocation.getName(), Integer.toString(stagingTracks.size())}));
			for (int i=0; i<stagingTracks.size(); i++ ){
				departStageTrack = departLocation.getTrackById(stagingTracks.get(i));
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildStagingHas"),new Object[]{
						departStageTrack.getName(), Integer.toString(departStageTrack.getNumberEngines()),
						Integer.toString(departStageTrack.getNumberCars())}));
				// is the departure track available?
				if (!checkDepartureStagingTrack()){
					departStageTrack = null;
					continue;
				}	
				// try each departure track for the required engines
				if (getEngines()){
					addLine(buildReport, SEVEN, "Done assigning engines from staging");
					break;	// done!
				} 
				departStageTrack = null;
			}
			if (departStageTrack == null){
				buildFailed(MessageFormat.format(rb.getString("buildErrorStagingReq"),new Object[]{departLocation.getName()}));
				return;
			}
		} else {
			// load engines for this train
			if (getEngines()){
				addLine(buildReport, SEVEN, "Done assigning engines to train ("+train.getName()+")");
			} else {
				buildFailed(MessageFormat.format(rb.getString("buildErrorEngines"),new Object[]{reqNumEngines, train.getTrainDepartsName()}));
				return; // there was a failure
			}
		}
		// show car types that this train will service
		addLine(buildReport, FIVE, "Train ("+train.getName()+") services car types:");
		String[] carTypes = CarTypes.instance().getNames();
		StringBuffer sbuf = new StringBuffer("");
		for (int i=0; i<carTypes.length; i++){
			if (train.acceptsTypeName(carTypes[i]))
				sbuf = sbuf.append(carTypes[i]+", ");
			if (sbuf.length() > 77){
				addLine(buildReport, FIVE, sbuf.toString());
				sbuf.delete(0, sbuf.length());
			}
		}
		// remove trailing separators
        if (sbuf.length() > 2){
        	sbuf.setLength(sbuf.length()-2);	
        	addLine(buildReport, FIVE, sbuf.toString());
        }
		// remove unwanted cars
		if (!removeCars(requested))
			return;	// there was a failure

		// get caboose or car with FRED if needed for train
		if(!getCabooseOrFred())
			return;	// there was a failure
		
		// now find destinations for cars 
		addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildTrain"),new Object[]{requested, train.getName(), carList.size()}));
		if (Setup.isBuildAggressive()){
			// perform a two pass build for this train
			// find destination for 50% of the available moves
			if (!placeCars(50))
				return;	// build failed
		}
		if (!placeCars(100))
			return;	// build failed

		// done finding cars for this train!
		train.setCurrentLocation(train.getTrainDepartsRouteLocation());
		if (numberCars < requested){
			train.setStatus(PARTIALBUILT + train.getNumberCarsWorked() +"/" + requested + " "+ rb.getString("cars"));
			addLine(buildReport, ONE, PARTIALBUILT + train.getNumberCarsWorked() +"/" + requested + " "+ rb.getString("cars"));
		}else{
			train.setStatus(BUILT + train.getNumberCarsWorked() + " "+ rb.getString("cars"));
			addLine(buildReport, ONE, BUILT + train.getNumberCarsWorked() + " "+ rb.getString("cars"));
		}
		train.setBuilt(true);
		buildReport.flush();
		buildReport.close();

		// now make manifest
		makeManifest();
		// now create and place train icon
		train.moveTrainIcon(train.getTrainDepartsRouteLocation());
		log.debug("Done building train "+train.getName());
	}
	
	private boolean removeEngines(){
		// get list of engines for this route		
		engineList = engineManager.getAvailableTrainList(train);
		// remove engines that are the wrong type, wrong track, wrong road name, or part of consist (not lead)
		addLine(buildReport, SEVEN, "Begin search for engines");
		for (int indexEng=0; indexEng<engineList.size(); indexEng++){
			Engine engine = engineManager.getById(engineList.get(indexEng));
			// remove engines types that train does not service
			if (!train.acceptsTypeName(engine.getType())){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeEngineType"),new Object[]{engine.toString(), engine.getType()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines models that train does not service
			if (!train.getEngineModel().equals("") && !engine.getModel().equals(train.getEngineModel())){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeEngineModel"),new Object[]{engine.toString(), engine.getModel()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// Does the train have a very specific engine road name requirement?
			if (!train.getEngineRoad().equals("") && !engine.getRoad().equals(train.getEngineRoad())){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeEngineRoad"),new Object[]{engine.toString(), engine.getRoad()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove rolling stock with roads that train does not service
			if (train.getEngineRoad().equals("") && !train.acceptsRoadName(engine.getRoad())){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeEngineRoad"),new Object[]{engine.toString(), engine.getRoad()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines with owners that train does not service
			if (!train.acceptsOwnerName(engine.getOwner())){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeEngineOwner"),new Object[]{engine.toString(), engine.getOwner()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines with built dates that train does not service
			if (!train.acceptsBuiltDate(engine.getBuilt())){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeEngineBuilt"),new Object[]{engine.toString(), engine.getBuilt()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines on tracks that don't service the train's departure direction
			if (!checkPickUpTrainDirection(engine, train.getTrainDepartsRouteLocation())){
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines that have been assigned destinations that don't match the terminal 
			if (engine.getDestination() != null && !engine.getDestination().equals(terminateLocation)){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeEngineDestination"),new Object[]{engine.toString(), engine.getDestinationName()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines that are out of service
			if (engine.isOutOfService()){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeEngineOutOfService"),new Object[]{engine.toString()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// is this engine part of a consist?  
			if (engine.getConsist() == null){
				// single engine, but does the train require a consist?
				if (reqNumEngines > 1){
					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeEngineSingle"),new Object[]{engine.toString(), reqNumEngines}));
					engineList.remove(indexEng);
					indexEng--;
					continue;
				}
			// engine is part of a consist
			}else{
				// Keep only lead engines in consist if required number is correct.
				if (!engine.getConsist().isLead(engine)){
					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildEnginePartConsist"),new Object[]{engine.toString(), engine.getConsist().getName(), engine.getConsist().getEngines().size()}));
					if (engine.getConsist().getSize() != reqNumEngines && reqNumEngines != 0)
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeEngConsistNumber"),new Object[]{engine.toString(), engine.getConsist().getName(), engine.getConsist().getSize()}));
					// remove non-lead engines
					engineList.remove(indexEng);
					indexEng--;
					continue;
				// lead engine in consist
				}else{
					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildEngineLeadConsist"),new Object[]{engine.toString(), engine.getConsist().getName(), engine.getConsist().getSize()}));				
					if (engine.getConsist().getSize() == reqNumEngines){
						log.debug("Consist ("+engine.getConsist().getName()+") has the required number of engines");
					}else if (reqNumEngines != 0){
						log.debug("Consist ("+engine.getConsist().getName()+") doesn't have the required number of engines");
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeEngConsistNumber"),new Object[]{engine.toString(), engine.getConsist().getName(), engine.getConsist().getSize()}));
						engineList.remove(indexEng);
						indexEng--;
						continue;
					}
				}
			} 
		}
		if (engineList.size() == 0 && reqNumEngines > 0)
			return false;	// build error no engines available
		return true;
	}
	
	/**
	 * Get the engines for this train. If departing from staging
	 * (departStageTrack != null) engines must come from that track.
	 * 
	 * @return true if engines found.
	 */
	private boolean getEngines(){
		// if not departing staging track and engines aren't required done!
		if (departStageTrack == null && reqNumEngines == 0)
			return true;
		
		// show how many engines were available
		if (engineList.size()>0){
			if (reqNumEngines > 1)
				addLine(buildReport, THREE, "Found "+engineList.size()+" consist(s) for this train");
			else
				addLine(buildReport, THREE, "Found "+engineList.size()+" engine(s) for this train");
		}
			
		// if leaving staging, use any number of engines if required number is 0
		boolean leavingStaging = false;
		if (departStageTrack != null){
			if (reqNumEngines == 0)
				leavingStaging = true;
			else if (departStageTrack.getNumberEngines() != reqNumEngines){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingNotEngines"),new Object[]{departStageTrack.getName()}));
				return false;	// done, wrong number of engines on staging track
			}
		}

		for (int indexEng=0; indexEng<engineList.size(); indexEng++){
			Engine engine = engineManager.getById(engineList.get(indexEng));

			// use engines that are departing from the selected staging track (departStageTrack != null if staging)
			if (departStageTrack != null && !engine.getTrack().equals(departStageTrack)){
				continue;
			}

			// now find terminal track for engine(s)
			addLine(buildReport, FIVE, "Engine ("+engine.toString()+") road ("+engine.getRoad()+") model ("+engine.getModel()+") type ("+engine.getType()+")");
			addLine(buildReport, SEVEN, " at location ("+engine.getLocationName()+", "+engine.getTrackName()+")");
			// is there a staging track?
			if (terminateStageTrack != null){
				String status = engine.testDestination(terminateLocation, terminateStageTrack);
				if (status.equals(Engine.OKAY)){
					if (addEngineToTrain(engine, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), terminateStageTrack))				
						return true;	// done
				} else {
					addLine(buildReport, FIVE, "Can't drop engine ("+engine.toString()+") to track (" +terminateStageTrack.getName()+") due to "+status);
				}
			// find a destination track for this engine
			} else {
				List<String> destTracks = terminateLocation.getTracksByMovesList(null);
				for (int s = 0; s < destTracks.size(); s++){
					Track track = terminateLocation.getTrackById(destTracks.get(s));
					String status = engine.testDestination(terminateLocation, track);
					if(status.equals(Engine.OKAY)){
						if (addEngineToTrain(engine, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), track))				
							return true;	// done
					} else {
						addLine(buildReport, FIVE, "Can't drop engine ("+engine.toString()+") to track (" +track.getName()+") due to "+status);
					}
				}
			}		
		}
		return leavingStaging;
	}
	
	// returns the number of engines needed for this train, minimum 1, 
	// maximum user specified in setup.
	// Based on maximum allowable train length and grade between locations,
	// and the maximum cars that the train can have at the maximum train length.
	// One engine per sixteen 40' cars for 1% grade.
	// TODO Currently ignores the cars weight and engine horsepower
	private int getAutoEngines(){
		double numberEngines = 1;
		int moves = 0;
		
		for (int i=0; i<routeList.size()-1; i++){
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(i));
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
		addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildAutoBuildMsg"),new Object[]{Integer.toString(nE)}));
		if (nE > Setup.getEngineSize()){
			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildMaximumNumberEngines"),new Object[]{Setup.getEngineSize()}));
			nE = Setup.getEngineSize();
		} 
		return nE;
	}
	
	/**
	 * Adds a caboose or car with FRED to the train if needed.  Caboose or
	 * car with FRED must travel with the train to the last location in the
	 * route.  Also removes all cabooses and cars with FRED that aren't needed by train.
	 * @return true if the correct caboose or car with FRED is found and is also accepted by
	 * the last location in route.
	 */
	private boolean getCabooseOrFred(){
		// get any requirements for this train
		boolean requiresCaboose = false;		// start off without any requirements
		boolean requiresFred = false;
		boolean foundFred = true;
		boolean foundCaboose = true;
		String textRequires = rb.getString("None");
		if (train.getRequirements()>0){
			if ((train.getRequirements()& Train.FRED) > 0){
				requiresFred = true;
				foundFred = false;
				textRequires = rb.getString("FRED");
			} 
			if ((train.getRequirements()& Train.CABOOSE) > 0){
				requiresCaboose = true;
				foundCaboose = false;
				textRequires = rb.getString("Caboose");
			}
			if (!train.getCabooseRoad().equals("")){
				textRequires += " road ("+train.getCabooseRoad()+")";
			}
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTrainRequires"),new Object[]{train.getName(), textRequires}));
		} else {
			addLine(buildReport, SEVEN, "Train does not require caboose or car with FRED");
		}
		// now go through the car list and find a caboose or FRED if required
		// first pass, try and find a caboose that matches the engine's road
		if(requiresCaboose && train.getCabooseRoad().equals("") && train.getLeadEngine() != null){
			for (carIndex=0; carIndex<carList.size(); carIndex++){
				Car car = carManager.getById(carList.get(carIndex));
				if (car.isCaboose() && car.getLocationName().equals(train.getTrainDepartsName()) && car.getRoad().equals(train.getLeadEngine().getRoad())
						&& checkPickUpTrainDirection(car, train.getTrainDepartsRouteLocation())){
					if (car.getDestination() == null || car.getDestination() == terminateLocation){
						addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildFoundCaboose"),new Object[]{car.toString()}));
						// found a caboose with road that matches engine, now remove all other cabooses from list
						addLine(buildReport, SEVEN, "Remove remaining cabooses");
						for (int i=0; i<carList.size(); i++){
							Car testCar = carManager.getById(carList.get(i));
							if (testCar.isCaboose() && testCar != car){
								// need to keep caboose if departing staging
								if (departStageTrack == null || testCar.getTrack() != departStageTrack){
									addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildExcludeCarTypeAtLoc"),new Object[]{testCar.toString(), testCar.getType(), (testCar.getLocationName()+", "+testCar.getTrackName())}));
									carList.remove(carList.get(i));		// remove this car from the list
									i--;
								}
							}
						}
						break;	// found a caboose matches engine and removed all others from list
					}
				}
			}
		}
		// second pass looking for caboose or car with FRED and if not needed remove
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car c = carManager.getById(carList.get(carIndex));
			// find a caboose or card with FRED for this train if needed
			if (!c.isCaboose() && !c.hasFred())
				continue;	// only work with cabooses or cars with FRED
			// check for caboose or car with FRED
			if (c.isCaboose()){
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCarIsCaboose"),new Object[]{c.toString(), c.getRoad()}));
				if (departStageTrack != null && c.getTrack() == departStageTrack) 
					foundCaboose = false;		// must move caboose from staging   
			}
			if (c.hasFred()){
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCarHasFRED"),new Object[]{c.toString(), c.getRoad()}));
				if (departStageTrack != null && c.getTrack() == departStageTrack) 
					foundFred = false;		// must move car with FRED from staging
			}
			
			// remove cabooses and cars with FRED if not needed for train
			if (c.isCaboose() && foundCaboose || c.hasFred() && foundFred){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarTypeAtLoc"),new Object[]{c.toString(), c.getType(), (c.getLocationName()+", "+c.getTrackName())}));
				carList.remove(carList.get(carIndex));		// remove this car from the list
				carIndex--;
				continue;
			}
			// caboose or car with FRED is needed for train, search for one
			if (c.isCaboose() && !foundCaboose || c.hasFred() && !foundFred){
				// remove cars with the wrong road
				if (!train.getCabooseRoad().equals("") && !train.getCabooseRoad().equals(c.getRoad()) && departStageTrack == null){
					addLine(buildReport, THREE,  MessageFormat.format(rb.getString("buildExcludeCarWrongRoad"),new Object[]{c.toString(), c.getType(), c.getRoad()}));
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
				// remove cars not at departure
				if(!c.getLocationName().equals(train.getTrainDepartsName())){
					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarWrongLoc"),new Object[]{c.toString(), c.getType(), c.getLocation()}));
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
				// remove cars that can't be picked up due to train and track directions
				if(!checkPickUpTrainDirection(c, train.getTrainDepartsRouteLocation())){
					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarTypeAtLoc"),new Object[]{c.toString(), c.getType(), (c.getLocationName()+" "+c.getTrackName())}));
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
				// has the car been assigned a destination?
				if (c.getDestination() != null && c.getDestination() != terminateLocation && departStageTrack == null){
					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarWrongDest"),new Object[]{c.toString(), c.getType(), c.getDestinationName()}));
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
				// does the car have a destination and track?
				if (c.getDestination() == terminateLocation && c.getTrack() != null){
					String status = c.testDestination(terminateLocation, c.getTrack());
					if (status.equals(Car.OKAY)){
						boolean carAdded = addCarToTrain(c, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), c.getTrack());
						if (carAdded && c.isCaboose())
							foundCaboose = true;
						if (carAdded && c.hasFred())
							foundFred = true;
						continue;
					} else {
						addLine(buildReport, SEVEN, "Can't drop car ("+c.toString()+") to track (" +c.getTrackName()+") due to "+status);
						carList.remove(carList.get(carIndex));		// remove this car from the list
						carIndex--;
					}
					if (departStageTrack != null && c.getTrack() == departStageTrack){
						buildFailed(rb.getString("buildErrorCaboose"));
						return false;
					} 
					continue;
				}
				// car meets all requirements, now find a destination track to place car
				if (terminateStageTrack == null){
					List<String> sls = terminateLocation.getTracksByMovesList(null);
					// loop through the destination tracks to find one that accepts caboose or car with FRED
					for (int s = 0; s < sls.size(); s++){
						Track destTrack = terminateLocation.getTrackById(sls.get(s));
						String status = c.testDestination(terminateLocation, destTrack);
						if (status.equals(Car.OKAY)){
							boolean carAdded = addCarToTrain(c, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), destTrack);
							if (carAdded && c.isCaboose())
								foundCaboose = true;
							if (carAdded && c.hasFred())
								foundFred = true;
							break;
						} 
						addLine(buildReport, SEVEN, "Can't drop car ("+c.toString()+") to track (" +destTrack.getName()+") due to "+status);
					}
					if ((c.isCaboose() && !foundCaboose) || (c.hasFred() && !foundFred)){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarNoDestination"),new Object[]{c.toString()}));
						// if departing staging this is a build failure
						if (departStageTrack != null && c.getTrack() == departStageTrack){
							buildFailed(rb.getString("buildErrorCaboose"));
							return false;
						}
					}
				// terminate into staging
				} else {
					String status = c.testDestination(terminateLocation, terminateStageTrack);
					if (status.equals(Car.OKAY)){
						boolean carAdded = addCarToTrain(c, train.getTrainDepartsRouteLocation(), train.getTrainTerminatesRouteLocation(), terminateStageTrack);
						if (carAdded && c.isCaboose())
							foundCaboose = true;
						if (carAdded && c.hasFred())
							foundFred = true;
					} else {
						addLine(buildReport, SEVEN, "Can't drop car ("+c.toString()+") to track (" +terminateStageTrack.getName()+") due to "+status);
					}
				} 
				// remove caboose or FRED from list couldn't find a destination track
				if((c.isCaboose() && !foundCaboose) || (c.hasFred() && !foundFred)) {
					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarTypeAtLoc"),new Object[]{c.toString(), c.getType(), (c.getLocationName()+" "+c.getTrackName())}));
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
				}
			}
		}
		// did we find a needed caboose or FRED?
		if (requiresFred && !foundFred || requiresCaboose && !foundCaboose){
			if (requiresCaboose)
				addLine(buildReport, SEVEN, "NOTE! To make a car a caboose you must select the caboose check box in Edit Car");
			buildFailed(MessageFormat.format(rb.getString("buildErrorRequirements"),
					new Object[]{train.getName(), textRequires, train.getTrainDepartsName(), train.getTrainTerminatesName()}));
			return false;
		}
		return true;
	}
	
	/**
	 * Remove unwanted cars from the car list.
	 * @return true if no errors
	 */
	private boolean removeCars(int requested){
		// get list of cars for this route
		carList = carManager.getAvailableTrainList(train);
		// TODO: DAB this needs to be controlled by each train
		if (requested > carList.size() && Control.fullTrainOnly){
			buildFailed(MessageFormat.format(rb.getString("buildErrorNumReq"),new Object[]{Integer.toString(requested),
				train.getName(), Integer.toString(carList.size())}));
			return false;
		}

    	// remove cars that don't have a valid track, interchange, road, load, owner, or type for this train
		addLine(buildReport, SEVEN, "Remove cars not serviced by this train");
		for (carIndex=0; carIndex<carList.size(); carIndex++){
    		Car c = carManager.getById(carList.get(carIndex));
    		// remove cars that don't have a valid track
    		if (c.getTrack() == null){
    			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildErrorRsNoLoc"),new Object[]{c.toString(), c.getLocationName()}));
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
    		}
    		// remove cars that have been reported as missing
    		if (c.isLocationUnknown()){
       			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarLocUnknown"),new Object[]{c.toString()}));
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
    		}
    		// remove cars that are out of service
    		if (c.isOutOfService()){
       			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarOutOfService"),new Object[]{c.toString()}));
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
    		}
    		// remove cabooses that have a destination that isn't the terminal
    		if ((c.isCaboose() || c.hasFred()) && c.getDestination() != null && c.getDestination() != terminateLocation){
   				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildExcludeCarWrongDest"),new Object[]{c.toString(), c.getType()}));
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
    		}
    		// all cars in staging must be accepted, so don't exclude if in staging
    		// note that for trains departing staging the engine and car roads and types were
    		// checked in the routine checkDepartureStagingTrack().
    		if (departStageTrack == null || c.getTrack() != departStageTrack){
    			if (!train.acceptsRoadName(c.getRoad())){
    				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildExcludeCarWrongRoad"),new Object[]{c.toString(), c.getType(), c.getRoad()}));
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (!train.acceptsTypeName(c.getType())){
    				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildExcludeCarTypeAtLoc"),new Object[]{c.toString(), c.getType(), (c.getLocationName()+", "+c.getTrackName())}));
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (!c.isCaboose() && !train.acceptsLoadName(c.getLoad())){
    				addLine(buildReport, FIVE, "Exclude car ("+c.toString()+") load ("+c.getLoad()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (!train.acceptsOwnerName(c.getOwner())){
    				addLine(buildReport, FIVE, "Exclude car ("+c.toString()+") owner ("+c.getOwner()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
       			if (!train.acceptsBuiltDate(c.getBuilt())){
    				addLine(buildReport, FIVE, "Exclude car ("+c.toString()+") built ("+c.getBuilt()+") at location ("+c.getLocationName()+", "+c.getTrackName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    		}
    		// is car at interchange?
    		if (c.getTrack().getLocType().equals(Track.INTERCHANGE)){
    			// don't service a car at interchange and has been dropped of by this train
    			if (c.getTrack().getPickupOption().equals(Track.ANY) && c.getSavedRouteId().equals(train.getRoute().getId())){
    				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarDropByTrain"),new Object[]{c.toString(), (c.getLocationName()+", "+c.getTrackName())}));
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (c.getTrack().getPickupOption().equals(Track.TRAINS)){
    				if (c.getTrack().acceptsPickupTrain(train)){
    					log.debug("Car ("+c.toString()+") can be picked up by this train");
    				} else {
    					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarByTrain"),new Object[]{c.toString(), (c.getLocationName()+", "+c.getTrackName())}));
    					carList.remove(carList.get(carIndex));
    					carIndex--;
    					continue;
    				}
    			}
    			else if (c.getTrack().getPickupOption().equals(Track.ROUTES)){
    				if (c.getTrack().acceptsPickupRoute(train.getRoute())){
    					log.debug("Car ("+c.toString()+") can be picked up by this route");
    				} else {
    					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarByRoute"),new Object[]{c.toString(), (c.getLocationName()+", "+c.getTrackName())}));
    					carList.remove(carList.get(carIndex));
    					carIndex--;
    					continue;
    				}
    			}
    		}
    		// does car have a wait count?
    		if (c.getWait() > 0){
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildExcludeCarWait"),new Object[]{c.toString(), c.getType(), (c.getLocationName()+", "+c.getTrackName()), c.getWait()}));
				c.setWait(c.getWait()-1);	// decrement wait count
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
    		}
		}

		addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildFoundCars"),new Object[]{Integer.toString(carList.size()), train.getName()}));

		// adjust car list to only have cars from one staging track
		if (departStageTrack != null){
			// Make sure that all cars in staging are moved
			train.getTrainDepartsRouteLocation().setCarMoves(train.getTrainDepartsRouteLocation().getMaxCarMoves()-departStageTrack.getNumberCars());  // negative number moves more cars
			int numCarsFromStaging = 0; 
			for (carIndex=0; carIndex<carList.size(); carIndex++){
				Car c = carManager.getById(carList.get(carIndex));
//				addLine(buildReport, "Check car ("+c.toString()+") at location ("+c.getLocationName()+" "+c.getTrackName()+")");
				if (c.getLocationName().equals(departLocation.getName())){
					if (c.getTrackName().equals(departStageTrack.getName())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingCarAtLoc"),new Object[]{c.toString(), (c.getLocationName()+", "+c.getTrackName())}));
						numCarsFromStaging++;
					} else {
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarAtLoc"),new Object[]{c.toString(), (c.getLocationName()+", "+c.getTrackName())}));
						carList.remove(carList.get(carIndex));
						carIndex--;
					}
				}
			}
			// error if all of the cars and engines from staging aren't available
			if (numCarsFromStaging + numberEngines != departStageTrack.getNumberRS()){
				buildFailed(MessageFormat.format(rb.getString("buildErrorNotAll"),
						new Object[]{Integer.toString(departStageTrack.getNumberRS()- (numCarsFromStaging + numberEngines))}));
				return false;
			}
		}
		// now go through the car list and remove non-lead cars in kernels, destinations that aren't part of this route
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car c = carManager.getById(carList.get(carIndex));
			// only print out the first 500 cars
			if (carIndex < 500)
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCarAtLocWithMoves"),new Object[]{c.toString(), (c.getLocationName()+ ", " +c.getTrackName()), c.getMoves(), c.getPriority()}));
			if (carIndex == 500)
				addLine(buildReport, FIVE, " ************* Only the first 500 cars are shown ************* ");
			// use only the lead car in a kernel for building trains
			if (c.getKernel() != null){
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCarPartOfKernel"),new Object[]{c.toString(), c.getKernelName()}));
				if (!c.getKernel().isLead(c)){
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
			}
			if (train.equals(c.getTrain())){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarAlreadyAssigned"),new Object[]{c.toString()}));
			}
			// does car have a destination that is part of this train's route?
			if (c.getDestination() != null) {
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarHasAssignedDest"),new Object[]{c.toString(), (c.getDestinationName()+", "+c.getDestinationTrackName())}));
				RouteLocation rld = train.getRoute().getLastLocationByName(c.getDestinationName());
				if (rld == null){
					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarDestNotPartRoute"),new Object[]{c.toString(), c.getDestinationName(), train.getRoute().getName()}));
					// build failure if car departing staging
					if (c.getLocation().equals(departLocation) && departStageTrack != null){
						// The following code should not be executed, departing staging tracks are checked before this routine.
						buildFailed(MessageFormat.format(rb.getString("buildErrorCarNotPartRoute"),
								new Object[]{c.toString()}));
						return false;
					}
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
				}
			}
		}
		return true;
	}
	
	boolean multipass = false;
	/**
	 * Main routine to place cars into the train.  Can be called multiple times, percent
	 * controls how many cars are placed in any given pass.
	 * @return true if there were no errors
	 */
	private boolean placeCars(int percent){
		if (percent < 100){
			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildMultiplePass"),new Object[]{percent}));
			multipass = true;
		}
		if (percent == 100 && multipass)
			addLine(buildReport, THREE, rb.getString("buildFinalPass"));
		// determine how many locations are serviced by this train
		int numLocs = routeList.size();
		if (numLocs > 1)  // don't find car destinations for the last location in the route
			numLocs--;
		// now go through each location starting at departure and place cars as requested
		for (int routeIndex=0; routeIndex<numLocs; routeIndex++){
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(routeIndex));
			if (train.skipsLocation(rl.getId())){
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildLocSkipped"),new Object[]{rl.getName(), train.getName()}));
				continue;
			}
			if (!rl.canPickup()){
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildLocNoPickups"),new Object[]{train.getRoute().getName(), rl.getName()}));
				continue;
			}
			if (!checkPickUpTrainDirection(rl)){
				continue;
			}
			moves = 0;			// the number of moves for this location
			success = false;	// true when done with this location
			reqNumOfMoves = rl.getMaxCarMoves()-rl.getCarMoves();	// the number of moves requested
			int saveReqMoves = reqNumOfMoves;	// save a copy for status message
			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildLocReqMoves"),new Object[]{rl.getName(), reqNumOfMoves, rl.getMaxCarMoves()}));
			// multiple pass build?
			if (percent < 100)
				reqNumOfMoves = reqNumOfMoves*percent/100;
			if (reqNumOfMoves <= 0)
				success = true;
			while (reqNumOfMoves > 0){
				for (carIndex=0; carIndex<carList.size(); carIndex++){
					Car c = carManager.getById(carList.get(carIndex));
					// find a car at this location
					if (!c.getLocationName().equals(rl.getName()))
						continue;
					// can this car be picked up?
					if(!checkPickUpTrainDirection(c, rl))
						continue; // no
					// does car have a next destination, but no destination
					if (c.getNextDestination() != null && c.getDestination() == null){
						addLine(buildReport, FIVE, "Car ("+c.toString()+") has next destination ("+c.getNextDestinationName()+", "+c.getNextDestTrackName()+"), routing begins");
						if (!Router.instance().setDestination(c)){
							addLine(buildReport, FIVE, "Not able to set destination for car ("+c.toString()+") due to "+Router.instance().getStatus());
							continue;	// couldn't set car's destination
						}
					}
					// does car have a destination?
					if (c.getDestination() != null) {
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarHasAssignedDest"),new Object[]{c.toString(), (c.getDestinationName()+", "+c.getDestinationTrackName())}));
						RouteLocation rld = train.getRoute().getLastLocationByName(c.getDestinationName());
						if (rld == null){
							// The following code should not be executed, removeCars() is called before placeCars()
							addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildExcludeCarDestNotPartRouteCar"),new Object[]{c.toString(), c.getDestinationName(), train.getRoute().getName()}));
						} else {
							if (c.getRouteLocation() != null){
								// The following code should not be executed, this should not occur if train was reset before a build!
								addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarAlreadyAssigned"),new Object[]{c.toString()}));
							} 
							// now go through the route and try and find a location with
							// the correct destination name
							boolean carAdded = false;
							int locCount = 0;
							for (int k = routeIndex; k<routeList.size(); k++){
								rld = train.getRoute().getLocationById(routeList.get(k));
								if (rld.getName().equals(c.getDestinationName())){
									locCount++;	// show when this car would be dropped at location
									// are drops allows at this location?
									if (!rld.canDrop()){
										addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildRouteNoDropsStop"),new Object[]{train.getRoute().getName(), rld.getName(), locCount}));
										continue;
									}
									if (rld.getCarMoves() >= rld.getMaxCarMoves()){
										addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildNoAvailableMovesStop"),new Object[]{rld.getName(), locCount}));
										continue;
									}
									// check for valid destination track
									if (c.getDestinationTrack() == null){
										addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarDoesNotHaveDest"),new Object[]{c.toString()}));
										// is there a destination track assigned for staging cars?
										if (rld == train.getTrainTerminatesRouteLocation() && terminateStageTrack != null){
											String status = c.testDestination(c.getDestination(), terminateStageTrack);
											if (status.equals(Car.OKAY)){
												addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarAssignedToStaging"),new Object[]{c.toString(), terminateStageTrack.getName()}));
												carAdded = addCarToTrain(c, rl, rld, terminateStageTrack);
											} else {
												addLine(buildReport, SEVEN, "Can't drop car ("+c.toString()+") to track (" +terminateStageTrack.getName()+") due to "+status);
												continue;
											}
											// no, find a destination track this this car
										} else {
											List<String> tracks = c.getDestination().getTracksByMovesList(null);
											for (int s = 0; s < tracks.size(); s++){
												Track testTrack = c.getDestination().getTrackById(tracks.get(s));
												// log.debug("track (" +testTrack.getName()+ ") has "+ testTrack.getMoves() + " moves");
												// dropping to the same track isn't allowed
												if (testTrack == c.getTrack()){
													addLine(buildReport, SEVEN, "Can't drop car ("+c.toString()+") to same track (" +testTrack.getName()+")");
													continue;
												}
												// is train direction correct?
												if (!checkDropTrainDirection(c, rld, testTrack))
													continue;
												String status = c.testDestination(c.getDestination(), testTrack);
												// is the testTrack a siding with a Schedule?
												if (testTrack.getLocType().equals(Track.SIDING) && status.contains(Car.SCHEDULE) 
														&& status.contains(Car.LOAD)){
													addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCanNotDropCarBecause"),new Object[]{c.toString(), testTrack.getName(), status}));
													continue;
												}
												if (!status.equals(Car.OKAY)){
													addLine(buildReport, SEVEN, "Can't drop car ("+c.toString()+") to track (" +testTrack.getName()+") due to "+status);
													continue;
												}
												carAdded = addCarToTrain(c, rl, rld, testTrack);
												break;
											}
										}
										// car has a destination track
									} else {
										// going into the correct staging track?
										if (terminateStageTrack == null  || terminateStageTrack == c.getDestinationTrack()){
											String status = c.testDestination(c.getDestination(), c.getDestinationTrack());
											if (status.equals(Car.OKAY) && checkDropTrainDirection(c, rld, c.getDestinationTrack()))
												carAdded = addCarToTrain(c, rl, rld, c.getDestinationTrack());
											else if (!status.equals(Car.OKAY))
												addLine(buildReport, SEVEN, "Can't drop car ("+c.toString()+") to track (" +c.getDestinationTrack().getName()+") due to "+status);
										}
									}
									// done?
									if (carAdded)
										break;	//yes
									else
										addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCanNotDropCar"),new Object[]{c.toString(), c.getDestination(), locCount}));
								}
							}
						}
					// car does not have a destination, search for the best one	
					} else {
						if (!findDestinationAndTrack(c, rl, routeIndex)){
							return false;
						}
					}
					if (success){
						//log.debug("done with location ("+destinationSave.getName()+")");
						break;
					}
					// build failure if car departing staging without a destination and a train
					if (c.getLocationName().equals(departLocation.getName()) && departStageTrack != null && 
							(c.getDestination() == null || c.getDestinationTrack() == null || c.getTrain() == null)){
						//log.debug("car "+c.toString()+" at location ("+c.getLocationName()+") destination ("+c.getDestinationName()+") dest track ("+c.getDestinationTrackName()+")");
						buildFailed(MessageFormat.format(rb.getString("buildErrorCarStageDest"),
								new Object[]{c.toString()}));
						return false;
					}
					// are there still moves available?
					if (noMoreMoves) {
						addLine(buildReport, FIVE, "No available destinations for any car");
						reqNumOfMoves = 0;
						break;
					}
				}
				// could not find enough cars
				reqNumOfMoves = 0;
				// don't use this location again
				//rl.setCarMoves(rl.getMaxCarMoves());
			}
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildStatusMsg"),new Object[]{(success? rb.getString("Success"): rb.getString("Partial")),
				Integer.toString(moves), Integer.toString(saveReqMoves), rl.getName(), train.getName()}));
		}
		return true;
	}
	
	private boolean addEngineToTrain(Engine engine, RouteLocation rl, RouteLocation rld, Track track){
		Location destination = locationManager.getLocationByName(rld.getName());
		int engineLength = 0;
		int engineWeight = 0;
		train.setLeadEngine(engine);	//load lead engine
		// engine in consist?
		if (engine.getConsist() != null){
			List<Engine> cEngines = engine.getConsist().getEngines();
			engineLength = engine.getConsist().getLength();
			engineWeight = engine.getConsist().getAdjustedWeightTons();
			numberEngines = numberEngines + cEngines.size();
			for (int j=0; j<cEngines.size(); j++){
				Engine cEngine = cEngines.get(j);
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildEngineAssigned"),new Object[]{cEngine.toString(), rld.getName(), track.getName()}));
				cEngine.setTrain(train);
				cEngine.setRouteLocation(rl);
				cEngine.setRouteDestination(rld);
				cEngine.setDestination(destination, track, true); // force destination
			}
		} else {
			// engine isn't part of a consist
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildEngineAssigned"),new Object[]{engine.toString(), rld.getName(), track.getName()}));
			engine.setTrain(train);
			engine.setRouteLocation(rl);
			engine.setRouteDestination(rld);
			engine.setDestination(destination, track);
			numberEngines++;
			engineLength = Integer.parseInt(engine.getLength());
			engineWeight = engine.getAdjustedWeightTons();
		}
		// TODO code doesn't check for engines being in train at all locations
		// set the engine length and weight for locations
		for (int i=0; i<routeList.size(); i++){
			RouteLocation r = train.getRoute().getLocationById(routeList.get(i));
			r.setTrainLength(r.getTrainLength()+engineLength);		// load the engine(s) length
			r.setTrainWeight(r.getTrainWeight()+engineWeight);		// load the engine(s) weight
		}
		return true;
	}
	
	/**
	 * Add car to train
	 * @param car
	 * @param rl the planned origin for this car
	 * @param rld the planned destination for this car
	 * @param destination
	 * @param track the final destination for car
	 * @return true if car was successfully added to train.  Also makes
	 * the boolean "success" true if location doesn't need any more pickups. 
	 */
	private boolean addCarToTrain(Car car, RouteLocation rl, RouteLocation rld, Track track){
		if (!checkTrainLength(car, rl, rld))
			return false;
		Location destination = locationManager.getLocationByName(rld.getName());
		// car could be part of a kernel
		if (car.getKernel()!=null){
			List<Car> kCars = car.getKernel().getCars();
			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarPartOfKernel"),new Object[]{car.toString(), car.getKernelName(), kCars.size()}));
			// log.debug("kernel length "+car.getKernel().getLength());
			for(int i=0; i<kCars.size(); i++){
				Car kCar = kCars.get(i);
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarAssignedDest"),new Object[]{kCar.toString(), (destination.getName()+", "+track.getName())}));
				kCar.setTrain(train);
				kCar.setRouteLocation(rl);
				kCar.setRouteDestination(rld);
				kCar.setDestination(destination, track, true);	//force destination
			}
			// not part of kernel, add one car	
		} else {
			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarAssignedDest"),new Object[]{car.toString(), (destination.getName()+", "+track.getName())}));
			car.setTrain(train);
			car.setRouteLocation(rl);
			car.setRouteDestination(rld);
			car.setDestination(destination, track);
		}
		numberCars++;		// bump number of cars moved by this train
		moves++;			// bump number of car pickup moves for the location
		reqNumOfMoves--; 	// decrement number of moves left for the location
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
			RouteLocation rlt = train.getRoute().getLocationById(routeList.get(i));
			if (rl == rlt){
				carInTrain = true;
			}
			if (rld == rlt){
				carInTrain = false;
			}
			if (carInTrain){
				int length = Integer.parseInt(car.getLength())+ Car.COUPLER;
				int weightTons = car.getAdjustedWeightTons();
				// car could be part of a kernel
				if (car.getKernel() != null){
					length = car.getKernel().getLength();
					weightTons = car.getKernel().getAdjustedWeightTons();
				}
				rlt.setTrainLength(rlt.getTrainLength()+length);	// couplers are included
				rlt.setTrainWeight(rlt.getTrainWeight()+weightTons);
			}
			if (rlt.getTrainWeight() > maxWeight){
				maxWeight = rlt.getTrainWeight();		// used for AUTO engines
			}
		}
		return true;
	}

	private boolean checkPickUpTrainDirection(RollingStock rs, RouteLocation rl){
		// check that car or engine is located on a track
		if (rs.getTrack() == null){
			addLine(buildReport, ONE,  MessageFormat.format(rb.getString("buildErrorRsNoLoc"),new Object[]{rs.toString(), rs.getLocationName()}));
			return false;
		}
		if (routeList.size() == 1) // ignore local train direction
			return true;
		if ((rl.getTrainDirection() & rs.getLocation().getTrainDirections() & rs.getTrack().getTrainDirections()) > 0)
			return true;
		
		// Only track direction can cause the following message.  Location direction has already been checked
		addLine(buildReport, FIVE, "Can't pick up rolling stock ("+rs.toString()+") using "
				+rl.getTrainDirectionString()+"bound train, track ("+rs.getTrack().getName()+")");
		addLine(buildReport, FIVE, "at location (" +rs.getLocation().getName()+ ") does not service this train's direction");
		return false;
	}
	
	private boolean checkPickUpTrainDirection(RouteLocation rl){
		if (routeList.size() == 1) // ignore local train direction
			return true;
		Location location = locationManager.getLocationByName(rl.getName());
		if ((rl.getTrainDirection() & location.getTrainDirections()) > 0)
			return true;
		
		addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildLocDirection"),new Object[]{location.getName(), rl.getTrainDirectionString()}));
		return false;
	}
	
	
	/**
	 * @param car
	 * @param rl the planned origin for this car
	 * @param rld the planned destination for this car
	 * @return true if car can be added to train
	 */
	private boolean checkTrainLength(Car car, RouteLocation rl, RouteLocation rld) {
		boolean carInTrain = false;
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rlt = train.getRoute().getLocationById(routeList.get(i));
			if (rl == rlt){
				carInTrain = true;
			}
			if (rld == rlt){
				carInTrain = false;
			}
			// car can be a kernel so get total length
			int length = Integer.parseInt(car.getLength())+ Car.COUPLER;
			if (car.getKernel() != null)
				length = car.getKernel().getLength();
			if (carInTrain && rlt.getTrainLength()+ length > rlt.getMaxTrainLength()){
				addLine(buildReport, FIVE, "Can't pick up car ("+car.toString()+") length ("+length+") using this route,");
				addLine(buildReport, FIVE, " it would exceed train's maximum length when departing "+rlt.getName());
				return false;
			}
		}
		return true;
	}
	
	private boolean ignoreTrainDirectionIfLastLoc = false;
	private boolean checkDropTrainDirection (Car car, RouteLocation rld, Track track){
		// local?
		if (routeList.size()==1)
			return true;
		// is the destination the last location on the route? 
		if (ignoreTrainDirectionIfLastLoc && rld == train.getTrainTerminatesRouteLocation())
			return true;	// yes, ignore train direction
		Location destination = locationManager.getLocationByName(rld.getName());
		// this location only services trains with these directions
		int serviceTrainDir = destination.getTrainDirections();
		if (track != null)
			serviceTrainDir = serviceTrainDir & track.getTrainDirections(); 
		if ((rld.getTrainDirection() & serviceTrainDir) >0){
			return true;
		}
		if (car == null){
			addLine(buildReport, FIVE, "Destination ("+destination.getName()+") does not service "+rld.getTrainDirectionString()+"bound trains");
			return false;
		}
		addLine(buildReport, FIVE, "Can't drop car ("+car.toString()+") using "+rld.getTrainDirectionString()+"bound train,");
		if (track != null)
			addLine(buildReport, FIVE, " destination track ("+track+") does not service this direction");
		else 
			addLine(buildReport, FIVE, " destination ("+destination.getName()+") does not service this direction");
		return false;
	}
	
	private boolean checkDropTrainDirection (RouteLocation rld, Location destination){
		return (checkDropTrainDirection (null, rld, null));
	}
	
	
	/**
	 * Check departure staging track to see if engines and cars are available to
	 * a new train.  Also confirms that the engine and car type and road are accepted by the train.
	 * 
	 * @return true is there are engines and cars available.
	 */
	private boolean checkDepartureStagingTrack(){
		if (departStageTrack.getNumberRS()==0)
			return false;
		// is the staging track direction correct for this train?
		if ((departStageTrack.getTrainDirections() & train.getTrainDepartsRouteLocation().getTrainDirection()) == 0){
			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingNotDirection"),new Object[]{departStageTrack.getName()}));
			return false;
		}
		if (departStageTrack.getNumberEngines()>0){
			List<String> engs = engineManager.getByIdList();
			for (int i=0; i<engs.size(); i++){
				Engine eng = engineManager.getById(engs.get(i));
				if (eng.getTrack() == departStageTrack){
					// has engine been assigned to another train?
					if (eng.getRouteLocation() != null){
						addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildStagingDepart"),
								new Object[]{departStageTrack.getName(), eng.getTrainName()}));
						return false;
					}
					// does the train accept the engine type from the staging track?
					if (!train.acceptsTypeName(eng.getType())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartEngineType"),
								new Object[]{departStageTrack.getName(), eng.toString()+" "+eng.getNumber(), eng.getType(), train.getName()}));
						return false;
					}
					// does the train accept the engine road from the staging track?
					if (!train.acceptsRoadName(eng.getRoad())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartEngineRoad"),
								new Object[]{departStageTrack.getName(), eng.toString(), eng.getRoad(), train.getName()}));
						return false;				
					}
					// does the train accept the engine owner from the staging track?
					if (!train.acceptsOwnerName(eng.getOwner())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartEngineOwner"),
								new Object[]{departStageTrack.getName(), eng.toString(), eng.getOwner(), train.getName()}));
						return false;				
					}
					// does the train accept the engine built date from the staging track?
					if (!train.acceptsBuiltDate(eng.getBuilt())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartEngineBuilt"),
								new Object[]{departStageTrack.getName(), eng.toString(), eng.getBuilt(), train.getName()}));
						return false;				
					}
				}
			}
		}
		if (departStageTrack.getNumberCars()>0){
			List<String> cars = carManager.getByIdList();
			for (int i=0; i<cars.size(); i++){
				Car car = carManager.getById(cars.get(i));
				if (car.getTrack() == departStageTrack){
					// has car been assigned to another train?
					if (car.getRouteLocation() != null){
						addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildStagingDepart"),
								new Object[]{departStageTrack.getName(), car.getTrainName()}));
						return false;
					}
					// does the train accept the car type from the staging track?
					if (!train.acceptsTypeName(car.getType())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartCarType"),
								new Object[]{departStageTrack.getName(), car.toString(), car.getType(), train.getName()}));
						return false;
					}
					// does the train accept the car road from the staging track?
					if (!train.acceptsRoadName(car.getRoad())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartCarRoad"),
								new Object[]{departStageTrack.getName(), car.toString(), car.getRoad(), train.getName()}));
						return false;
					}
					// does the train accept the car owner from the staging track?
					if (!train.acceptsOwnerName(car.getOwner())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartCarOwner"),
								new Object[]{departStageTrack.getName(), car.toString(), car.getOwner(), train.getName()}));
						return false;
					}
					// does the train accept the car built date from the staging track?
					if (!train.acceptsBuiltDate(car.getBuilt())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartCarBuilt"),
								new Object[]{departStageTrack.getName(), car.toString(), car.getBuilt(), train.getName()}));
						return false;
					}
					// does the car have a destination serviced by this train?
					if (car.getDestination()!= null){
						log.debug("Car ("+car.toString()+") has a destination ("+car.getDestinationName()+")");
						if (!train.servicesCar(car)){
							addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartCarDestination"),
									new Object[]{departStageTrack.getName(), car.toString(), car.getDestinationName(), train.getName()}));
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Checks to see if staging track can accept train.
	 * @return true if staging track is empty, not reserved, and accepts
	 * car and engine types and roads.
	 */
	private boolean checkTerminateStagingTrack(){
		if (terminateStageTrack.getNumberRS() != 0){
			addLine(buildReport, FIVE, "Staging track ("+terminateStageTrack.getName()+") is occupied, found "+terminateStageTrack.getNumberEngines()+" engines and "+terminateStageTrack.getNumberCars()+" cars");
			return false;
		}
		if (terminateStageTrack.getDropRS() != 0){
			addLine(buildReport, FIVE, "Staging track ("+terminateStageTrack.getName()+") is reserved, drop count: "+terminateStageTrack.getDropRS());
			return false;
		}
		if (!Setup.isTrainIntoStagingCheckEnabled())
			return true;
		// check go see if location/track will accept the train's car and engine types
		String[] types = train.getTypeNames();
		for (int i=0; i<types.length; i++){
			if (!terminateLocation.acceptsTypeName(types[i])){
				addLine(buildReport, FIVE, "Destination ("+terminateLocation.getName()+") does not accept type ("+types[i]+")");
				return false;			
			}
			if (!terminateStageTrack.acceptsTypeName(types[i])){
				addLine(buildReport, FIVE, "Staging track ("+terminateStageTrack.getName()+") does not accept type ("+types[i]+")");
				return false;			
			}
		}
		// check go see if track will accept the train's car and engine roads
		if (train.getRoadOption().equals(Train.ALLROADS) && !terminateStageTrack.getRoadOption().equals(Track.ALLROADS)){
			addLine(buildReport, FIVE, "Staging track ("+terminateStageTrack.getName()+") does not accept all roads");
			return false;

		}
		else if (train.getRoadOption().equals(Train.INCLUDEROADS)){
			String[] roads = train.getRoadNames();
			for (int i=0; i<roads.length; i++){
				if (!terminateStageTrack.acceptsRoadName(roads[i])){
					addLine(buildReport, FIVE, "Staging track ("+terminateStageTrack.getName()+") does not accept road ("+roads[i]+")");
					return false;
				}
			}
		}
		else if (train.getRoadOption().equals(Train.EXCLUDEROADS)){
			String[] excludeRoads = train.getRoadNames();
			String[] allroads = CarRoads.instance().getNames();
			List<String> roads = new ArrayList<String>();
			for (int i=0; i<allroads.length; i++){
				roads.add(allroads[i]);
			}
			for (int i=0; i<excludeRoads.length; i++){
				roads.remove(excludeRoads[i]);
			}
			for (int i=0; i<roads.size(); i++){
				if (!terminateStageTrack.acceptsRoadName(roads.get(i))){
					addLine(buildReport, FIVE, "Staging track ("+terminateStageTrack.getName()+") does not accept road ("+roads.get(i)+")");
					return false;
				}
			}
		}
		return true;	
	}
	
	private boolean findDestinationAndTrack(Car car, RouteLocation rl, int routeIndex){
		addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildFindDestinationForCar"),new Object[]{car.toString(), (car.getLocationName()+", " +car.getTrackName())}));
		int start = routeIndex;					// start looking after car's current location
		RouteLocation rld = null;				// the route location destination being checked for the car
		RouteLocation rldSave = null;			// holds the best route location destination for the car
		Track trackSave = null;					// holds the best track at destination for the car
		noMoreMoves = true;  					// false when there are are locations with moves

		// more than one location in this route?
		if (routeList.size()>1)
			start++;		//yes!, no car drops at departure
		for (int k = start; k<routeList.size(); k++){
			rld = train.getRoute().getLocationById(routeList.get(k));
			if (rld.canDrop()){
				addLine(buildReport, SEVEN, "Searching location ("+rld.getName()+") for possible destination");
			} else {
				addLine(buildReport, FIVE, "Route ("+train.getRoute().getName()+") does not allow drops at location ("+rld.getName()+")");
				continue;
			}							
			// don't move car to same location unless the route only has one location (local moves) or is passenger
			if (rl.getName().equals(rld.getName()) && routeList.size() != 1 && !car.isPassenger()){
				addLine(buildReport, SEVEN, "Car ("+car.toString()+") location is equal to destination ("+rld.getName()+"), skiping this destination");
				continue;
			}
			// any moves left at this location?
			if (rld.getCarMoves() >= rld.getMaxCarMoves()){
				addLine(buildReport, FIVE, "No available moves for destination ("+rld.getName()+")");
				continue;
			}
			noMoreMoves = false;
			
			// get a "test" destination and a list of the track locations available			
			Location destinationTemp = null;
			Track trackTemp = null;
			Location testDestination = locationManager.getLocationByName(rld.getName());
			if (testDestination == null){
				// The following code should not be executed, all locations in the route have been already tested
				buildFailed(MessageFormat.format(rb.getString("buildErrorRouteLoc"),
						new Object[]{train.getRoute().getName(), rld.getName()}));
				return false;
			}
			// can this location service this train's direction
			if (!checkDropTrainDirection(rld, testDestination))
				continue;
			// is there a track assigned for staging cars?				
			if (rld == train.getTrainTerminatesRouteLocation() && terminateStageTrack != null){						
				String status = car.testDestination(testDestination, terminateStageTrack); 	// will staging accept this car?
				if (status.equals(Car.OKAY)){
					trackTemp = terminateStageTrack;
					destinationTemp = testDestination;
				} else {
					addLine(buildReport, SEVEN, "Can't drop car ("+car.toString()+") to track (" +terminateStageTrack.getName()+") due to "+status);
					continue;
				} 
				// no staging track assigned, start track search
			} else {								
				List<String> tracks = testDestination.getTracksByMovesList(null);
				for (int s = 0; s < tracks.size(); s++){
					Track testTrack = testDestination.getTrackById(tracks.get(s));
					// log.debug("track (" +testTrack.getName()+ ") has "+ testTrack.getMoves() + " moves");
					// dropping to the same track isn't allowed
					if (testTrack == car.getTrack()){
						addLine(buildReport, SEVEN, "Can't drop car ("+car.toString()+") to same track (" +testTrack.getName()+")");
						continue;
					}
					// is train direction correct?
					if (!checkDropTrainDirection(car, rld, testTrack))
						continue;
					// train length okay?
					if (!checkTrainLength(car, rl, rld)){
						if (trackSave == null){
							addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildNoDestForCar"),new Object[]{car.toString()}));
							return true;	// No available destination for this car
						}
						break;	//done with this location
					}
					String status = car.testDestination(testDestination, testTrack);
					// is the testTrack a siding with a Schedule?
					if(testTrack.getLocType().equals(Track.SIDING) && status.contains(Car.SCHEDULE) 
							&& status.contains(Car.LOAD)){
						log.debug("Siding ("+testTrack.getName()+") status: "+status);
						// is car departing a staging track that can generate schedule loads?
						if (car.getTrack().isAddLoadsEnabled() && car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())){
							Schedule sch = ScheduleManager.instance().getScheduleByName(testTrack.getScheduleName());
							ScheduleItem si = sch.getItemById(testTrack.getScheduleItemId());
							addLine(buildReport, FIVE, "Adding schedule load ("+si.getLoad()+") to car ("+car.toString()+")");
							car.setLoad(si.getLoad());
							// force car to this destination
							return addCarToTrain(car, rl, rld, testTrack);	// should always be true
						}
					}
					// okay to drop car?
					if(!status.equals(Car.OKAY)){
						if (status.contains(Car.SCHEDULE))
							addLine(buildReport, SEVEN, "Can't drop car ("+car.toString()+") load ("+car.getLoad()+") to track (" +testTrack.getName()+") due to "+status);
						else
							addLine(buildReport, SEVEN, "Can't drop car ("+car.toString()+") to track (" +testTrack.getName()+") due to "+status);
						continue;
					}		
					// No local moves from siding to siding
					if (routeList.size() == 1 && !Setup.isLocalSidingMovesEnabled() && testTrack.getLocType().equals(Track.SIDING) && car.getTrack().getLocType().equals(Track.SIDING)){
						addLine(buildReport, FIVE, "Local siding to siding move not allowed (" +testTrack.getName()+ ")");
						continue;
					}
					// No local moves from yard to yard
					if (routeList.size() == 1 && !Setup.isLocalYardMovesEnabled() && testTrack.getLocType().equals(Track.YARD) && car.getTrack().getLocType().equals(Track.YARD)){
						addLine(buildReport, FIVE, "Local yard to yard move not allowed (" +testTrack.getName()+ ")");
						continue;
					}
					// No local moves from interchange to interchange
					if (routeList.size() == 1 && !Setup.isLocalInterchangeMovesEnabled() && testTrack.getLocType().equals(Track.INTERCHANGE) && car.getTrack().getLocType().equals(Track.INTERCHANGE)){
						addLine(buildReport, FIVE, "Local interchange to interchange move not allowed (" +testTrack.getName()+ ")");
						continue;
					}
					// drop to interchange?
					if (testTrack.getLocType().equals(Track.INTERCHANGE)){
						if (testTrack.getDropOption().equals(Track.TRAINS)){
							if (testTrack.acceptsDropTrain(train)){
								log.debug("Car ("+car.toString()+") can be droped by train to interchange (" +testTrack.getName()+")");
							} else {
								addLine(buildReport, FIVE, "Can't drop car ("+car.toString()+") by train to interchange (" +testTrack.getName()+")");
								continue;
							}
						}
						if (testTrack.getDropOption().equals(Track.ROUTES)){
							if (testTrack.acceptsDropRoute(train.getRoute())){
								log.debug("Car ("+car.toString()+") can be droped by route to interchange (" +testTrack.getName()+")");
							} else {
								addLine(buildReport, FIVE, "Can't drop car ("+car.toString()+") by route to interchange (" +testTrack.getName()+")");
								continue;
							}
						}
					}
					// not staging, then use
					if (!testTrack.getLocType().equals(Track.STAGING)){
						trackTemp = testTrack;
						destinationTemp = testDestination;
						break;
					}			
				}
			}
			// did we find a destination?
			if(destinationTemp != null){
				// check for programming error
				if(trackTemp == null){
					// The following code should not be executed
					buildFailed("Build Failure, trackTemp is null!");
					return false;
				}
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarCanDropMoves"),new Object[]{car.toString(), (destinationTemp.getName()+ ", " +trackTemp.getName()), 
					+rld.getCarMoves(), rld.getMaxCarMoves()}));
				// if there's more than one available destination use the one with the least moves
				if (rldSave != null){
					double saveCarMoves = rldSave.getCarMoves();
					double saveRatio = saveCarMoves/rldSave.getMaxCarMoves();
					double nextCarMoves = rld.getCarMoves();
					double nextRatio = nextCarMoves/rld.getMaxCarMoves();
					// bias cars to the terminal 
					if (rld.getName().equals(terminateLocation.getName())){
						log.debug("Location "+rld.getName()+" is terminate location "+Double.toString(nextRatio));
						nextRatio = nextRatio * nextRatio;
					}
					// bias cars to a track with a schedule
					if (!trackTemp.getScheduleName().equals("")){
						log.debug("Track ("+trackTemp.getName()+") has schedule ("+trackTemp.getScheduleName()+") adjust nextRatio = "+Double.toString(nextRatio));
						nextRatio = nextRatio * nextRatio;
					}
					log.debug(rldSave.getName()+" = "+Double.toString(saveRatio)+ " " + rld.getName()+" = "+Double.toString(nextRatio));
					if (saveRatio < nextRatio){
						rld = rldSave;					// the saved is better than the last found
						trackTemp = trackSave;
					}
				}
				// every time through, save the best route destination, and track
				rldSave = rld;
				trackSave = trackTemp;
			} else {
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCouldNotFindDestForCar"),new Object[]{car.toString(), rld.getName()}));
			}
		} 
		if (trackSave != null){
			return addCarToTrain(car, rl, rldSave, trackSave); // should always be true
		} 
		if (routeList.size()>1)
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildNoDestForCar"),new Object[]{car.toString()}));
		return true;	// no build errors, but car not given destination
	}

	private void buildFailed(String string){
		train.setStatus(BUILDFAILED);
		train.setBuildFailed(true);
		if(log.isDebugEnabled())
			log.debug(string);
		if(TrainManager.instance().isBuildMessagesEnabled()){
			JOptionPane.showMessageDialog(null, string,
					MessageFormat.format(rb.getString("buildErrorMsg"),new Object[]{train.getName(), train.getDescription()}),
					JOptionPane.ERROR_MESSAGE);
		}
		if (buildReport != null){
			addLine(buildReport, ONE, string);
			// Write to disk and close buildReport
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildFailedMsg"),new Object[]{train.getName()}));
			buildReport.flush();
			buildReport.close();
		}
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
		if (!train.getRailroadName().equals(""))
			addLine(fileOut, train.getRailroadName());
		else
			addLine(fileOut, Setup.getRailroadName());
		newLine(fileOut);
		addLine(fileOut, rb.getString("ManifestForTrain")+" (" + train.getName() + ") "+ train.getDescription());
		addLine(fileOut, MessageFormat.format(rb.getString("Valid"), new Object[]{new Date()}));
		if (!train.getComment().equals("")){
			addLine(fileOut, train.getComment());
		}
		
		List<String> engineList = engineManager.getByTrainList(train);
		Engine engine = null;
		for (int i =0; i < engineList.size(); i++){
			engine = engineManager.getById(engineList.get(i));	
			pickupEngine(fileOut, engine);
		}
		/* TODO not sure if I want to reuse the following code
		if (engine != null){
			String pickupText = rb.getString("PickupEngineAt");
			if (engineList.size() > 1)
				pickupText= rb.getString("PickupEnginesAt");
			addLine(fileOut, pickupText+" "+splitString(engine.getLocationName())+", "+engine.getTrackName());
		}
		*/
		
		List<String> carList = carManager.getByTrainDestinationList(train);
		log.debug("Train has " + carList.size() + " cars assigned to it");
		int cars = 0;
		List<String> routeList = train.getRoute().getLocationsBySequenceList();
		for (int r = 0; r < routeList.size(); r++) {
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(r));
			newLine(fileOut);
			String routeLocationName = splitString(rl.getName());
			if (r == 0){
				addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + routeLocationName 
						+", "+rb.getString("departureTime")+" "+train.getDepartureTime());
			} else if (!rl.getDepartureTime().equals("")){
				addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + routeLocationName 
						+", "+rb.getString("departureTime")+" "+rl.getDepartureTime());
			} else {
				addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + routeLocationName 
						+", "+rb.getString("estimatedArrival")+" "+train.getExpectedArrivalTime(rl));
			}
			// add location comment
			if (Setup.isPrintLocationCommentsEnabled()){
				Location l = locationManager.getLocationByName(rl.getName());
				if (!l.getComment().equals(""))
					addLine(fileOut, l.getComment());				
			}
			// add route comment
			if (!rl.getComment().equals(""))
				addLine(fileOut, rl.getComment());
			// block cars by destination
			for (int j = r; j < routeList.size(); j++) {
				RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
				for (int k = 0; k < carList.size(); k++) {
					Car car = carManager.getById(carList.get(k));
					if (car.getRouteLocation() == rl
							&& car.getRouteDestination() == rld) {
						pickupCar(fileOut, car);
						cars++;
					}
				}
			}
			for (int j = 0; j < carList.size(); j++) {
				Car car = carManager.getById(carList.get(j));
				if (car.getRouteDestination() == rl) {
					dropCar(fileOut, car);
					cars--;
				}
			}
			if (r != routeList.size() - 1) {
				addLine(fileOut, rb.getString("TrainDeparts")+ " " + routeLocationName +" "+ rl.getTrainDirectionString()
						+ rb.getString("boundWith") +" " + cars + " " +rb.getString("cars")+", " +rl.getTrainLength()
						+" "+rb.getString("feet")+", "+rl.getTrainWeight()+" "+rb.getString("tons"));
			} else {
				if(engine != null){
					String dropText = rb.getString("DropEngineTo");
					if (engineList.size() > 1)
						dropText = rb.getString("DropEnginesTo");
					addLine(fileOut, BOX +dropText+ " "+ splitString(engine.getDestinationTrackName())); 
				}
				addLine(fileOut, rb.getString("TrainTerminatesIn")+ " " + routeLocationName);
			}
		}
		// Are there any cars that need to be found?
		getCarsLocationUnknown(fileOut);
		
		fileOut.flush();
		fileOut.close();
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainBuilder.class.getName());

}
