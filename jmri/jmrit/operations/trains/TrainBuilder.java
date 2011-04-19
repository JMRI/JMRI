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
//import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import jmri.Version;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.ScheduleItem;
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
 * @author Daniel Boudreau  Copyright (C) 2008, 2009, 2010, 2011
 * @version             $Revision: 1.149 $
 */
public class TrainBuilder extends TrainCommon{
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	// report levels
	protected static final String ONE = Setup.BUILD_REPORT_MINIMAL;
	protected static final String THREE = Setup.BUILD_REPORT_NORMAL;
	protected static final String FIVE = Setup.BUILD_REPORT_DETAILED;
	protected static final String SEVEN = Setup.BUILD_REPORT_VERY_DETAILED;
			
	// build variables shared between local routines
	Train train;				// the train being built
	int numberEngines = 0;		// number of engines assigned to this train
	int numberCars = 0;			// how many cars are moved by this train
	int reqNumEngines = 0; 		// the number of engines required for this train
	List<String> engineList;	// list of engines available for this train
	int carIndex;				// index for carList
	List<String> carList;		// list of cars available for this train
	List<String> routeList;		// list of locations from departure to termination served by this train
	int moves;					// the number of pick up car moves for a location
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
		this.train = train;
		try {
			build();
		} catch (BuildFailedException e){
			buildFailed(e.getMessage());
		}
	}
	
	private void build() throws BuildFailedException {
		log.debug("Building train "+train.getName());
		
		train.setStatus(Train.BUILDING);
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
		addLine(buildReport, ONE, MessageFormat.format(rb.getString("BuildReportVersion"),new Object[]{Version.name()}));
		
		if (train.getRoute() == null){
			throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorRoute"),new Object[]{train.getName()}));
		}
		// get the train's route
		routeList = train.getRoute().getLocationsBySequenceList();
		if (routeList.size() < 1){
			throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorNeedRoute"),new Object[]{train.getName()}));
		}
		// train departs
		departLocation = locationManager.getLocationByName(train.getTrainDepartsName());
		if (departLocation == null){
			throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorNeedDepLoc"),new Object[]{train.getName()}));
		}
		// train terminates
		terminateLocation = locationManager.getLocationByName(train.getTrainTerminatesName());
		if (terminateLocation == null){
			throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorNeedTermLoc"),new Object[]{train.getName()}));
		}		  
		// TODO: DAB control minimal build by each train
		if (train.getTrainDepartsRouteLocation().getMaxCarMoves() > departLocation.getNumberRS() && Control.fullTrainOnly){
			throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorCars"),new Object[]{Integer.toString(departLocation.getNumberRS()),
				train.getTrainDepartsName(), train.getName()}));
		}
		// get the number of requested car moves for this train
		int requested = 0;
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(i));
			// check to see if there's a location for each stop in the route
			Location l = locationManager.getLocationByName(rl.getName());
			if (l == null){
				throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorLocMissing"),new Object[]{train.getRoute().getName()}));
			}				
			// train doesn't drop or pick up cars from staging locations found in middle of a route
			List<String> slStage = l.getTracksByMovesList(Track.STAGING);
			if (slStage.size() > 0 && i!=0 && i!=routeList.size()-1){
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildLocStaging"),new Object[]{rl.getName()}));
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			}
			// if a location is skipped, no car drops or pick ups
			else if(train.skipsLocation(rl.getId())){
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildLocSkipped"),new Object[]{rl.getName(), train.getName()}));
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			}
			// skip if a location doesn't allow drops or pick ups
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
	    	addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildTrainRoads"),new Object[]{train.getName(), train.getRoadOption(), sbuf.toString()}));
		}
		// show load names that this train will service
		if (!train.getLoadOption().equals(Train.ALLLOADS)){
			String[] loads = train.getLoadNames();
			StringBuffer sbuf = new StringBuffer("");    	
	    	for (int i=0; i<loads.length; i++){
	    		sbuf = sbuf.append(loads[i]+", ");
	    	}
	        if (sbuf.length() > 2) sbuf.setLength(sbuf.length()-2); // remove trailing separators
	    	addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildTrainLoads"),new Object[]{train.getName(), train.getLoadOption(), sbuf.toString()}));
		}
		// show owner names that this train will service
		if (!train.getOwnerOption().equals(Train.ALLOWNERS)){
			String[] owners = train.getOwnerNames();
			StringBuffer sbuf = new StringBuffer("");    	
	    	for (int i=0; i<owners.length; i++){
	    		sbuf = sbuf.append(owners[i]+", ");
	    	}
	        if (sbuf.length() > 2) sbuf.setLength(sbuf.length()-2); // remove trailing separators
	    	addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildTrainOwners"),new Object[]{train.getName(), train.getOwnerOption(), sbuf.toString()}));
		}
		// show built date serviced
		if (!train.getBuiltStartYear().equals(""))
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildTrainBuiltAfter"),new Object[]{train.getName(),train.getBuiltStartYear()}));
		if (!train.getBuiltEndYear().equals(""))
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildTrainBuiltBefore"),new Object[]{train.getName(),train.getBuiltEndYear()}));
		
		// show engine info
		if (train.getNumberEngines().equals(Train.AUTO)){
			reqNumEngines = getAutoEngines();
		} else {
			reqNumEngines = Integer.parseInt(train.getNumberEngines());
		}	
		// show engine types that this train will service
		if (reqNumEngines >0){
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildTrainServicesEngineTypes"),new Object[]{train.getName()}));
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
		
		// allow up to two engine and caboose swaps in the train's route
		RouteLocation engineTerminatesFirstLeg = train.getTrainTerminatesRouteLocation();
		RouteLocation cabooseOrFredTerminatesFirstLeg = train.getTrainTerminatesRouteLocation();
		RouteLocation engineTerminatesSecondLeg = train.getTrainTerminatesRouteLocation();
		RouteLocation cabooseOrFredTerminatesSecondLeg = train.getTrainTerminatesRouteLocation();
		RouteLocation engineTerminatesThirdLeg = train.getTrainTerminatesRouteLocation();
		RouteLocation cabooseOrFredTerminatesThirdLeg = train.getTrainTerminatesRouteLocation();
		
		// show any engine changes or helper services
		if ((train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES){
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTrainEngineChange"),new Object[]{train.getSecondLegStartLocationName(), 
				train.getSecondLegNumberEngines(), train.getSecondLegEngineModel(), train.getSecondLegEngineRoad()}));
			if (train.getSecondLegStartLocation() != null){
				engineTerminatesFirstLeg = train.getSecondLegStartLocation();
				if ((train.getSecondLegOptions() & Train.CHANGE_CABOOSE) == Train.CHANGE_CABOOSE){
					cabooseOrFredTerminatesFirstLeg = train.getSecondLegStartLocation();
				}
			}
		}
		if ((train.getSecondLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES){
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTrainHelperEngines"),new Object[]{train.getSecondLegNumberEngines(), train.getSecondLegStartLocationName(), 
				train.getSecondLegEndLocationName(), train.getSecondLegEngineModel(), train.getSecondLegEngineRoad()}));
		}
		if ((train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES){
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTrainEngineChange"),new Object[]{train.getThirdLegStartLocationName(), 
				train.getThirdLegNumberEngines(), train.getThirdLegEngineModel(), train.getThirdLegEngineRoad()}));
			if (train.getThirdLegStartLocation() != null){
				engineTerminatesSecondLeg = train.getThirdLegStartLocation();
				if ((train.getThirdLegOptions() & Train.CHANGE_CABOOSE) == Train.CHANGE_CABOOSE){
					cabooseOrFredTerminatesSecondLeg = train.getThirdLegStartLocation();
				}
				// No engine or caboose change at first leg?
				if ((train.getSecondLegOptions() & Train.CHANGE_ENGINES) != Train.CHANGE_ENGINES){
					engineTerminatesFirstLeg = train.getThirdLegStartLocation();
					if ((train.getThirdLegOptions() & Train.CHANGE_CABOOSE) == Train.CHANGE_CABOOSE){
						cabooseOrFredTerminatesFirstLeg = train.getThirdLegStartLocation();
					}
				}
			}
		}
		if ((train.getThirdLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES){
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTrainHelperEngines"),new Object[]{train.getThirdLegNumberEngines(), train.getThirdLegStartLocationName(), 
				train.getThirdLegEndLocationName(), train.getThirdLegEngineModel(), train.getThirdLegEngineRoad()}));
		}
				
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
				throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorStagingFull"),new Object[]{terminateLocation.getName()}));
			}
		}
		
		// get list of engines for this route		
		engineList = engineManager.getAvailableTrainList(train);
		
		// determine if train is departing staging
		departStageTrack = null;
		List<String> stagingTracks = departLocation.getTracksByMovesList(Track.STAGING);
		if (stagingTracks.size()>0){
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildDepartStaging"),new Object[]{departLocation.getName(), Integer.toString(stagingTracks.size())}));
			if (stagingTracks.size()>1 && Setup.isPromptFromStagingEnabled())
				departStageTrack = PromptFromStagingDialog();
			else for (int i=0; i<stagingTracks.size(); i++ ){
				departStageTrack = departLocation.getTrackById(stagingTracks.get(i));
				addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildStagingHas"),new Object[]{
						departStageTrack.getName(), Integer.toString(departStageTrack.getNumberEngines()),
						Integer.toString(departStageTrack.getNumberCars())}));
				// is the departure track available?
				if (!checkDepartureStagingTrack(departStageTrack)){
					departStageTrack = null;
					continue;
				}	
				// try each departure track for the required engines
				if (getEngines(reqNumEngines, train.getEngineModel(), train.getEngineRoad(), train.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)){
					addLine(buildReport, SEVEN, rb.getString("buildDoneAssignEnginesStaging"));
					break;	// done!
				} 
				departStageTrack = null;
			}
			if (departStageTrack == null){
				throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorStagingReq"),new Object[]{departLocation.getName()}));
			}
		} else {
			// load engines for this train
			if (getEngines(reqNumEngines, train.getEngineModel(), train.getEngineRoad(), train.getTrainDepartsRouteLocation(), engineTerminatesFirstLeg)){
			} else {
				throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorEngines"),new Object[]{reqNumEngines, train.getTrainDepartsName(), engineTerminatesFirstLeg.getName()}));
			}
		}
		// First engine change in route?
		if ((train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES){
			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildTrainEngineChange"),new Object[]{train.getSecondLegStartLocationName(), 
				train.getSecondLegNumberEngines(), train.getSecondLegEngineModel(), train.getSecondLegEngineRoad()}));
			if (getEngines(Integer.parseInt(train.getSecondLegNumberEngines()), train.getSecondLegEngineModel(), train.getSecondLegEngineRoad(), train.getSecondLegStartLocation(), engineTerminatesSecondLeg)){
			} else {
				throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorEngines"),new Object[]{Integer.parseInt(train.getSecondLegNumberEngines()), train.getSecondLegStartLocation(), engineTerminatesSecondLeg}));
			}
		}
		// Second engine change in route?
		if ((train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES){
			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildTrainEngineChange"),new Object[]{train.getThirdLegStartLocationName(), 
				train.getThirdLegNumberEngines(), train.getThirdLegEngineModel(), train.getThirdLegEngineRoad()}));
			if (getEngines(Integer.parseInt(train.getThirdLegNumberEngines()), train.getThirdLegEngineModel(), train.getThirdLegEngineRoad(), train.getThirdLegStartLocation(), engineTerminatesThirdLeg)){
			} else {
				throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorEngines"),new Object[]{Integer.parseInt(train.getThirdLegNumberEngines()), train.getThirdLegStartLocation(), engineTerminatesThirdLeg}));
			}
		}
		addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildDoneAssingEnginesTrain"),new Object[]{train.getName()}));
		
		// show car types that this train will service
		addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildTrainServicesCarTypes"),new Object[]{train.getName()}));
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
		removeCars(requested);

		// get caboose or car with FRED if needed for train
		getCaboose(train.getCabooseRoad(), train.getLeadEngine(), train.getTrainDepartsRouteLocation(), cabooseOrFredTerminatesFirstLeg);
		getCarWithFred(train.getCabooseRoad(), train.getTrainDepartsRouteLocation(), cabooseOrFredTerminatesFirstLeg);
		
		// first caboose change?
		if ((train.getSecondLegOptions() & Train.CHANGE_CABOOSE) > 0){
			getCaboose(train.getSecondLegCabooseRoad(), null, train.getSecondLegStartLocation(), cabooseOrFredTerminatesSecondLeg);
		}
		// second caboose change?
		if ((train.getThirdLegOptions() & Train.CHANGE_CABOOSE) > 0){
			getCaboose(train.getThirdLegCabooseRoad(), null, train.getThirdLegStartLocation(), cabooseOrFredTerminatesThirdLeg);
		}
		
		removeCaboosesAndCarsWithFred();
		
		// now find destinations for cars 
		addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildTrain"),new Object[]{requested, train.getName(), carList.size()}));
		if (Setup.isBuildAggressive()){
			// perform a two pass build for this train
			placeCars(50);	// find destination for 50% of the available moves
		}		
		placeCars(100);		// done finding cars for this train!

		train.setCurrentLocation(train.getTrainDepartsRouteLocation());
		if (numberCars < requested){
			train.setStatus(Train.PARTIALBUILT +" "+ train.getNumberCarsWorked() +"/" + requested + " "+ rb.getString("cars"));
			addLine(buildReport, ONE, Train.PARTIALBUILT +" "+ train.getNumberCarsWorked() +"/" + requested + " "+ rb.getString("cars"));
		}else{
			train.setStatus(Train.BUILT +" "+ train.getNumberCarsWorked() +" "+ rb.getString("cars"));
			addLine(buildReport, ONE, Train.BUILT +" "+ train.getNumberCarsWorked() +" "+ rb.getString("cars"));
		}
		train.setBuilt(true);
		buildReport.flush();
		buildReport.close();

		// now make manifest
		makeManifest();
		if (Setup.isGenerateCsvManifestEnabled())
			new TrainCvsManifest(train);
		// now create and place train icon
		train.moveTrainIcon(train.getTrainDepartsRouteLocation());
		log.debug("Done building train "+train.getName());
	}
	
	private Track PromptFromStagingDialog() {		
		List<String> trackIds = departLocation.getTracksByNameList(null);
		List<String> validTrackIds = new ArrayList<String>();
		// only show valid tracks
		for (int i=0; i<trackIds.size(); i++){
			Track track = departLocation.getTrackById(trackIds.get(i));
			if (checkDepartureStagingTrack(track))
				validTrackIds.add(trackIds.get(i));
		}
		Object[] tracks = new Object[validTrackIds.size()];
		for (int i=0; i<validTrackIds.size(); i++)
			tracks[i] = departLocation.getTrackById(validTrackIds.get(i));
		if (validTrackIds.size()>1){
			Track selected = (Track)JOptionPane.showInputDialog(null, rb.getString("SelectDepartureTrack"), rb.getString("TrainDepartingStaging"), 
					JOptionPane.QUESTION_MESSAGE, null, tracks, null);
			return selected;
		} else if (validTrackIds.size() == 1)
			return (Track)tracks[0];
		return null;	// no tracks available
	}
	
	/**
	 * Get the engines for this train. If departing from staging
	 * (departStageTrack != null) engines must come from that track.
	 * 
	 * @return true if correct number of engines found.
	 * @throws BuildFailedException 
	 */
	private boolean getEngines(int numberOfEngines, String model, String road, RouteLocation rl, RouteLocation rld) throws BuildFailedException{
		// load departure track if staging
		Track departTrack = null;
		if (rl == train.getTrainDepartsRouteLocation())
			departTrack = departStageTrack;
		// load termination track if staging
		Track terminateTrack = null;
		if (rld == train.getTrainTerminatesRouteLocation())
			terminateTrack = terminateStageTrack;
		
		// if not departing staging track and engines aren't required done!
		if (departTrack == null && numberOfEngines == 0)
			return true;
		
		// if departing staging and no engines required
		if (departTrack != null && numberOfEngines == 0 && departTrack.getNumberEngines() == 0)
			return true;
		
		// if leaving staging, use any number of engines if required number is 0
		if (departTrack != null && numberOfEngines != 0 && departTrack.getNumberEngines() != numberOfEngines){
			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingNotEngines"),new Object[]{departTrack.getName()}));
			return false;	// done, wrong number of engines on staging track
		}
		
		if (rl == null || rld == null)
			throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorEngLocUnknown"),
					new Object[]{}));		
					
		addLine(buildReport, FIVE,  MessageFormat.format(rb.getString("buildBegineSearchEngines"), new Object[]{numberOfEngines, model, road, rl.getName(), rld.getName()}));
		for (int indexEng=0; indexEng<engineList.size(); indexEng++){
			Engine engine = engineManager.getById(engineList.get(indexEng));
			log.debug("Engine ("+engine.toString()+") at location ("+engine.getLocationName()+")");

			// use engines that are departing from the selected staging track (departTrack != null if staging)
			if (departTrack != null && !departTrack.equals(engine.getTrack())){
				continue;
			}
			
			// use engines that are departing from the correct location
			if (!engine.getLocationName().equals(rl.getName())){
				log.debug("Skipping engine ("+engine.toString()+") at location ("+engine.getLocationName()+")");
				continue;
			}
			
			// remove engines types that train does not service
			if (!train.acceptsTypeName(engine.getType())){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeEngineType"),new Object[]{engine.toString(), engine.getType()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// skip engines models that train does not service
			if (!train.getEngineModel().equals("") && !engine.getModel().equals(train.getEngineModel())){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeEngineModel"),new Object[]{engine.toString(), engine.getModel()}));
				continue;
			}
			// Does the train have a very specific engine road name requirement?
			if (!road.equals("") && !engine.getRoad().equals(road)){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeEngineRoad"),new Object[]{engine.toString(), engine.getRoad()}));
				continue;
			}
			// remove rolling stock with roads that train does not service
			if (road.equals("") && !train.acceptsRoadName(engine.getRoad())){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeEngineRoad"),new Object[]{engine.toString(), engine.getRoad()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines with owners that train does not service
			if (!train.acceptsOwnerName(engine.getOwner())){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeEngineOwner"),new Object[]{engine.toString(), engine.getOwner()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// remove engines with built dates that train does not service
			if (!train.acceptsBuiltDate(engine.getBuilt())){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeEngineBuilt"),new Object[]{engine.toString(), engine.getBuilt()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// skip engines on tracks that don't service the train's departure direction
			if (!checkPickUpTrainDirection(engine, rl)){
				continue;
			}
			// skip engines that have been assigned destinations that don't match the terminal 
			if (engine.getDestination() != null && !engine.getDestinationName().equals(rl.getName())){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeEngineDestination"),new Object[]{engine.toString(), engine.getDestinationName()}));
				continue;
			}
			// remove engines that are out of service
			if (engine.isOutOfService()){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeEngineOutOfService"),new Object[]{engine.toString()}));
				engineList.remove(indexEng);
				indexEng--;
				continue;
			}
			// is this engine part of a consist?  
			if (engine.getConsist() == null){
				// single engine, but does the train require a consist?
				if (numberOfEngines > 1){
					addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeEngineSingle"),new Object[]{engine.toString(), numberOfEngines}));
					continue;
				}
			// engine is part of a consist
			}else{
				// Keep only lead engines in consist if required number is correct.
				if (!engine.getConsist().isLead(engine)){
					addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildEnginePartConsist"),new Object[]{engine.toString(), engine.getConsist().getName(), engine.getConsist().getEngines().size()}));
					if (engine.getConsist().getSize() != numberOfEngines)
						addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeEngConsistNumber"),new Object[]{engine.toString(), engine.getConsist().getName(), engine.getConsist().getSize()}));
					// remove non-lead engines
					engineList.remove(indexEng);
					indexEng--;
					continue;
				// lead engine in consist
				}else{
					addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildEngineLeadConsist"),new Object[]{engine.toString(), engine.getConsist().getName(), engine.getConsist().getSize()}));				
					if (engine.getConsist().getSize() == numberOfEngines){
						log.debug("Consist ("+engine.getConsist().getName()+") has the required number of engines");
					}else if (numberOfEngines != 0){
						//log.debug("Consist ("+engine.getConsist().getName()+") doesn't have the required number of engines");
						addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeEngConsistNumber"),new Object[]{engine.toString(), engine.getConsist().getName(), engine.getConsist().getSize()}));
						continue;
					}
				}
			}

			// now find terminal track for engine(s)
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildEngineRoadModelType"),new Object[]{engine.toString(), engine.getRoad(), engine.getModel(), engine.getType()}));
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildAtLocation"),new Object[]{(engine.getLocationName()+", "+engine.getTrackName()), rld.getName()}));
			// is there a staging track?
			if (terminateTrack != null){
				String status = engine.testDestination(terminateTrack.getLocation(), terminateTrack);
				if (status.equals(Engine.OKAY)){
					if (addEngineToTrain(engine, rl, rld, terminateTrack))				
						return true;	// done
				} else {
					addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropEngineToTrack"),new Object[]{engine.toString(), terminateTrack.getName(), status}));
				}
			// find a destination track for this engine
			} else {
				Location location = locationManager.getLocationByName(rld.getName());
				List<String> destTracks = location.getTracksByMovesList(null);
				for (int s = 0; s < destTracks.size(); s++){
					Track track = location.getTrackById(destTracks.get(s));
					if (!checkDropTrainDirection(engine, rld, track))
						continue;
					String status = engine.testDestination(location, track);
					if(status.equals(Engine.OKAY)){
						if (addEngineToTrain(engine, rl, rld, track))				
							return true;	// done
					} else {
						addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropEngineToTrack"),new Object[]{engine.toString(), track.getName(), status}));
					}
				}
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCanNotDropEngToDest"),new Object[]{engine.toString(), rld.getName()}));
			}		
		}
		// not able to find engines
		return false;
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
	
	private void getCarWithFred(String road, RouteLocation rl, RouteLocation rld) throws BuildFailedException{
		// load departure track if staging
		Track departTrack = null;
		if (rl == train.getTrainDepartsRouteLocation())
			departTrack = departStageTrack;
		boolean foundCar = false;
		boolean requiresCar = false;
		if ((train.getRequirements() & Train.FRED) == 0){
			addLine(buildReport, FIVE, rb.getString("buildTrainNoFred"));
			if (departTrack == null)
				return;
		} else {		
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTrainReqFred"),new Object[]{train.getName(), road, rl.getName(), rld.getName()}));
			requiresCar = true;
		}
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car car = carManager.getById(carList.get(carIndex));
			if (car.hasFred()){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCarHasFRED"),new Object[]{car.toString(), car.getRoad(), car.getLocationName()}));
				// car departing staging must leave with train
				if (car.getTrack() == departTrack){
					foundCar = false;
					if (checkCarForDestinationAndTrack(car, rl, rld)){
						if (car.getTrain() == train)
							foundCar = true;
					} else if (findDestinationAndTrack(car, rl, rld)){
						foundCar = true;
					}
					if (!foundCar)
						throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorCarStageDest"),
								new Object[]{car.toString()}));
				}
				// is there a specific road requirement for the car with FRED?
				else if (!road.equals("") && !road.equals(car.getRoad())){
					addLine(buildReport, SEVEN,  MessageFormat.format(rb.getString("buildExcludeCarWrongRoad"),new Object[]{car.toString(), car.getType(), car.getRoad()}));
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
					continue;
				}
				else if (!foundCar && car.getLocationName().equals(rl.getName())){
					// remove cars that can't be picked up due to train and track directions
					if(!checkPickUpTrainDirection(car, rl)){
						addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarTypeAtLoc"),new Object[]{car.toString(), car.getType(), (car.getLocationName()+" "+car.getTrackName())}));
						carList.remove(carList.get(carIndex));		// remove this car from the list
						carIndex--;
						continue;
					}
					if (checkCarForDestinationAndTrack(car, rl, rld)){
						if (car.getTrain() == train)
							foundCar = true;
					} else if (findDestinationAndTrack(car, rl, rld)){
						foundCar = true;
					}
					if (foundCar && departTrack == null)
						break;
				}
			}
		}
		if (requiresCar && !foundCar)
			throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorRequirements"),
					new Object[]{train.getName(), rb.getString("FRED"), rl.getName(), rld.getName()}));
	}
	
	private void getCaboose(String roadCaboose, Engine leadEngine, RouteLocation rl, RouteLocation rld) throws BuildFailedException{
		// load departure track if staging
		Track departTrack = null;
		if (rl == train.getTrainDepartsRouteLocation())
			departTrack = departStageTrack;
		boolean foundCar = false;
		boolean requiresCar = false;
		if ((train.getRequirements() & Train.CABOOSE) == 0){
			addLine(buildReport, FIVE, rb.getString("buildTrainNoCaboose"));
			if (departTrack == null)
				return;
		} else {		
			addLine(buildReport, ONE, MessageFormat.format(rb.getString("buildTrainReqCaboose"),new Object[]{train.getName(), roadCaboose, rl.getName(), rld.getName()}));
			requiresCar = true;
		}
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car car = carManager.getById(carList.get(carIndex));
			if (car.isCaboose()){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCarIsCaboose"),new Object[]{car.toString(), car.getRoad(), car.getLocationName()}));
				// car departing staging must leave with train
				if (car.getTrack() == departTrack){
					foundCar = false;
					if (checkCarForDestinationAndTrack(car, rl, rld)){
						if (car.getTrain() == train)
							foundCar = true;
					} else if (findDestinationAndTrack(car, rl, rld)){
						foundCar = true;
					}
					if (!foundCar)
						throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorCarStageDest"),
								new Object[]{car.toString()}));
				}
				// is there a specific road requirement for the caboose?
				else if (!roadCaboose.equals("") && !roadCaboose.equals(car.getRoad())){
					continue;
				}
				else if (!foundCar && car.getLocationName().equals(rl.getName())){
					// remove cars that can't be picked up due to train and track directions
					if(!checkPickUpTrainDirection(car, rl)){
						addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarTypeAtLoc"),new Object[]{car.toString(), car.getType(), (car.getLocationName()+" "+car.getTrackName())}));
						carList.remove(carList.get(carIndex));		// remove this car from the list
						carIndex--;
						continue;
					}
					// first pass only take a caboose that matches the engine
					if (leadEngine != null && car.getRoad().equals(leadEngine.getRoad())){
						if (checkCarForDestinationAndTrack(car, rl, rld)){
							if (car.getTrain() == train)
								foundCar = true;
						} else if (findDestinationAndTrack(car, rl, rld)){
							foundCar = true;
						}
					}
					if (foundCar && departTrack == null)
						break;
				}
			}
		}
		if (requiresCar && !foundCar){
			log.debug("Second pass looking for caboose");
			// second pass, take any caboose available
			for (carIndex=0; carIndex<carList.size(); carIndex++){
				Car car = carManager.getById(carList.get(carIndex));
				if (car.isCaboose() && car.getLocationName().equals(rl.getName())){
					// is there a specific road requirement for the caboose?
					if (!roadCaboose.equals("") && !roadCaboose.equals(car.getRoad())){
						continue;	//yes
					}
					if (checkCarForDestinationAndTrack(car, rl, rld)){
						if (car.getTrain() == train){
							foundCar = true;
							break;
						}
					} else if (findDestinationAndTrack(car, rl, rld)){
						foundCar = true;
						break;
					}
				}
			}
		}
		if (requiresCar && !foundCar)
			throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorRequirements"),
					new Object[]{train.getName(), rb.getString("Caboose"), rl.getName(), rld.getName()}));
	}
	
	private void removeCaboosesAndCarsWithFred(){
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car car = carManager.getById(carList.get(carIndex));
			if (car.isCaboose() || car.hasFred()){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarTypeAtLoc"),new Object[]{car.toString(), car.getType(), (car.getLocationName()+", "+car.getTrackName())}));
				carList.remove(carList.get(carIndex));		// remove this car from the list
				carIndex--;
			}
		}
	}
	
	/**
	 * Remove unwanted cars from the car list.
	 *
	 */
	private void removeCars(int requested) throws BuildFailedException{
		// get list of cars for this route
		carList = carManager.getAvailableTrainList(train);
		// TODO: DAB this needs to be controlled by each train
		if (requested > carList.size() && Control.fullTrainOnly){
			throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorNumReq"),new Object[]{Integer.toString(requested),
				train.getName(), Integer.toString(carList.size())}));
		}

    	// remove cars that don't have a valid track, interchange, road, load, owner, or type for this train
		addLine(buildReport, SEVEN, rb.getString("buildRemoveCars"));
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
       			addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarLocUnknown"),new Object[]{c.toString()}));
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
    		}
    		// remove cars that are out of service
    		if (c.isOutOfService()){
       			addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarOutOfService"),new Object[]{c.toString()}));
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
    		}
 
    		// remove cabooses that have a destination that isn't the terminal
    		if ((c.isCaboose() || c.hasFred()) && c.getDestination() != null && c.getDestination() != terminateLocation){
   				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildExcludeCarWrongDest"),new Object[]{c.toString(), c.getType(), c.getDestinationName()}));
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
    		}
    		
    		// all cars in staging must be accepted, so don't exclude if in staging
    		// note that for trains departing staging the engine and car roads and types were
    		// checked in the routine checkDepartureStagingTrack().
    		if (departStageTrack == null || c.getTrack() != departStageTrack){
    			if (!train.acceptsRoadName(c.getRoad())){
    				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarWrongRoad"),new Object[]{c.toString(), c.getType(), c.getRoad()}));
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (!train.acceptsTypeName(c.getType())){
    				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarTypeAtLoc"),new Object[]{c.toString(), c.getType(), (c.getLocationName()+", "+c.getTrackName())}));
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (!c.isCaboose() && !train.acceptsLoadName(c.getLoad())){
    				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarLoadAtLoc"),new Object[]{c.toString(), c.getLoad(), (c.getLocationName()+", "+c.getTrackName())}));
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (!train.acceptsOwnerName(c.getOwner())){
    				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarOwnerAtLoc"),new Object[]{c.toString(), c.getOwner(), (c.getLocationName()+", "+c.getTrackName())}));
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
       			if (!train.acceptsBuiltDate(c.getBuilt())){
    				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarBuiltAtLoc"),new Object[]{c.toString(), c.getBuilt(), (c.getLocationName()+", "+c.getTrackName())}));
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
        		// remove cabooses if not needed by train
        		if (c.isCaboose() && (train.getRequirements() & Train.CABOOSE) == 0){
    				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCabooseAtLoc"),new Object[]{c.toString(), c.getType(), (c.getLocationName()+", "+c.getTrackName())}));
    				carList.remove(carList.get(carIndex));		// remove this car from the list
    				carIndex--;
    				continue;
        		}
        		// remove cars with FRED if not needed by train
        		if (c.hasFred() && (train.getRequirements() & Train.FRED) == 0){
    				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarWithFredAtLoc"),new Object[]{c.toString(), c.getType(), (c.getLocationName()+", "+c.getTrackName())}));
    				carList.remove(carList.get(carIndex));		// remove this car from the list
    				carIndex--;
    				continue;
        		}
    		}
    		// is car at interchange?
    		if (c.getTrack().getLocType().equals(Track.INTERCHANGE)){
    			// don't service a car at interchange and has been dropped of by this train
    			if (c.getTrack().getPickupOption().equals(Track.ANY) && c.getSavedRouteId().equals(train.getRoute().getId())){
    				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarDropByTrain"),new Object[]{c.toString(), (c.getLocationName()+", "+c.getTrackName())}));
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    		}
    		if (c.getTrack().getLocType().equals(Track.INTERCHANGE) || c.getTrack().getLocType().equals(Track.SIDING)){
    			if (c.getTrack().getPickupOption().equals(Track.TRAINS)){
    				if (c.getTrack().acceptsPickupTrain(train)){
    					log.debug("Car ("+c.toString()+") can be picked up by this train");
    				} else {
    					addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarByTrain"),new Object[]{c.toString(), (c.getLocationName()+", "+c.getTrackName())}));
    					carList.remove(carList.get(carIndex));
    					carIndex--;
    					continue;
    				}
    			}
    			else if (c.getTrack().getPickupOption().equals(Track.ROUTES)){
    				if (c.getTrack().acceptsPickupRoute(train.getRoute())){
    					log.debug("Car ("+c.toString()+") can be picked up by this route");
    				} else {
    					addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarByRoute"),new Object[]{c.toString(), (c.getLocationName()+", "+c.getTrackName())}));
    					carList.remove(carList.get(carIndex));
    					carIndex--;
    					continue;
    				}
    			}
    		}
    		// does car have a wait count?
    		if (c.getWait() > 0){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildExcludeCarWait"),new Object[]{c.toString(), c.getType(), (c.getLocationName()+", "+c.getTrackName()), c.getWait()}));
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
						addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildExcludeCarAtLoc"),new Object[]{c.toString(), (c.getLocationName()+", "+c.getTrackName())}));
						carList.remove(carList.get(carIndex));
						carIndex--;
					}
				}
			}
			// error if all of the cars and engines from staging aren't available
			if (numCarsFromStaging + departStageTrack.getNumberEngines() != departStageTrack.getNumberRS()){
				throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorNotAll"),
						new Object[]{Integer.toString(departStageTrack.getNumberRS()- (numCarsFromStaging + departStageTrack.getNumberEngines()))}));
			}
		}
		// now go through the car list and remove non-lead cars in kernels, destinations that aren't part of this route
		for (carIndex=0; carIndex<carList.size(); carIndex++){
			Car c = carManager.getById(carList.get(carIndex));
			// only print out the first 500 cars
			if (carIndex < 500)
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCarAtLocWithMoves"),new Object[]{c.toString(), (c.getLocationName()+ ", " +c.getTrackName()), c.getMoves(), c.getPriority()}));
			if (carIndex == 500)
				addLine(buildReport, FIVE, rb.getString("buildOnlyFirst500Cars"));
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
					addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildExcludeCarDestNotPartRoute"),new Object[]{c.toString(), c.getDestinationName(), train.getRoute().getName()}));
					// build failure if car departing staging
					if (c.getLocation().equals(departLocation) && departStageTrack != null){
						// The following code should not be executed, departing staging tracks are checked before this routine.
						throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorCarNotPartRoute"),
								new Object[]{c.toString()}));
					}
					carList.remove(carList.get(carIndex));		// remove this car from the list
					carIndex--;
				}
			}
		}
		return;
	}
	
	boolean multipass = false;
	/**
	 * Main routine to place cars into the train.  Can be called multiple times, percent
	 * controls how many cars are placed in any given pass.
	 */
	private void placeCars(int percent) throws BuildFailedException{
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
			if (percent < 100){
				reqNumOfMoves = reqNumOfMoves*percent/100;
				// Departing staging?
				if (routeIndex == 0 && departStageTrack != null){
					reqNumOfMoves = 0;	// Move cars out of staging after working other locations
					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildDepartStagingAggressive"),new Object[]{}));
				}
			}
			if (reqNumOfMoves <= 0)
				success = true;
			while (reqNumOfMoves > 0){
				for (carIndex=0; carIndex<carList.size(); carIndex++){
					Car car = carManager.getById(carList.get(carIndex));
					// find a car at this location
					if (!car.getLocationName().equals(rl.getName()))
						continue;
					// can this car be picked up?
					if(!checkPickUpTrainDirection(car, rl))
						continue; // no
					// check for car order?
					car = getCarOrder(car);
					// is car departing staging and generate custom load?
					if (car.getTrack() != null && car.getTrack().isAddLoadsEnabledAnySiding()
							&& car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
							&& car.getDestination() == null && car.getNextDestination() == null){
						searchForCarLoad(car);
					}
					// does car have a custom load without a destination?
					if (!car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
							&& !car.getLoad().equals(CarLoads.instance().getDefaultLoadName())
							&& car.getDestination() == null && car.getNextDestination() == null){
						findNextDestinationForCarLoad(car);
					}
					// does car have a next destination, but no destination
					if (car.getNextDestination() != null && car.getDestination() == null){
						addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCarRoutingBegins"),new Object[]{car.toString(),(car.getNextDestinationName()+", "+car.getNextDestTrackName())}));
						if (!Router.instance().setDestination(car, train)){
							addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildNotAbleToSetDestination"),new Object[]{car.toString(), Router.instance().getStatus()}));
							car.setDestination(null, null);
							// don't move car if routing issue was track space
							if (Router.instance().getStatus().contains(Car.LENGTH))
								continue;
						}
					}
					// does car have a destination?
					if (checkCarForDestinationAndTrack(car, rl, routeIndex)){
					// car does not have a destination, search for the best one	
					} else {
						findDestinationAndTrack(car, rl, routeIndex, routeList.size());
					}
					if (success){
						//log.debug("done with location ("+destinationSave.getName()+")");
						break;
					}
					// build failure if car departing staging without a destination and a train
					if (car.getLocationName().equals(departLocation.getName()) && departStageTrack != null && 
							(car.getDestination() == null || car.getDestinationTrack() == null || car.getTrain() == null)){
						//log.debug("car "+car.toString()+" at location ("+car.getLocationName()+") destination ("+car.getDestinationName()+") dest track ("+car.getDestinationTrackName()+")");
						throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorCarStageDest"),
								new Object[]{car.toString()}));
					}
					// are there still moves available?
					if (noMoreMoves) {
						addLine(buildReport, FIVE, rb.getString("buildNoAvailableDestinations"));
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
		return;
	}
	
	private boolean addEngineToTrain(Engine engine, RouteLocation rl, RouteLocation rld, Track track){
		Location location = locationManager.getLocationByName(rl.getName());
		location.setStatus();
		Location destination = locationManager.getLocationByName(rld.getName());
		destination.setStatus();
		int engineLength = 0;
		int engineWeight = 0;
		if (train.getLeadEngine() == null)
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
	 * the boolean "success" true if location doesn't need any more pick ups. 
	 */
	private boolean addCarToTrain(Car car, RouteLocation rl, RouteLocation rld, Track track){
		if (!checkTrainLength(car, rl, rld))
			return false;
		Location location = locationManager.getLocationByName(rl.getName());
		location.setStatus();
		Location destination = locationManager.getLocationByName(rld.getName());
		destination.setStatus();
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
		moves++;			// bump number of car pick up moves for the location
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
		addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildRsCanNotPickupUsingTrain"),new Object[]{rs.toString(), rl.getTrainDirectionString(), rs.getTrack().getName()}));
		addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildRsCanNotPickupUsingTrain2"),new Object[]{rs.getLocation().getName()}));
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
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCanNotPickupCarLength"),new Object[]{car.toString(), length}));
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCanNotPickupCarLength2"),new Object[]{rlt.getMaxTrainLength(), rlt.getName()}));
				return false;
			}
		}
		return true;
	}
	
	private final boolean ignoreTrainDirectionIfLastLoc = false;
	private boolean checkDropTrainDirection (RollingStock rs, RouteLocation rld, Track track){
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
		if (rs == null){
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildDestinationDoesNotService"),new Object[]{destination.getName(), rld.getTrainDirectionString()}));
			return false;
		}
		addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCanNotDropRsUsingTrain"),new Object[]{rs.toString(), rld.getTrainDirectionString()}));
		if (track != null)
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCanNotDropRsUsingTrain2"),new Object[]{track.getName()}));
		else 
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCanNotDropRsUsingTrain3"),new Object[]{destination.getName()}));
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
	private boolean checkDepartureStagingTrack(Track departStageTrack){
		if (departStageTrack.getNumberRS()==0)
			return false;
		// is the staging track direction correct for this train?
		if ((departStageTrack.getTrainDirections() & train.getTrainDepartsRouteLocation().getTrainDirection()) == 0){
			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingNotDirection"),new Object[]{departStageTrack.getName()}));
			return false;
		}
		// does this staging track service this train?
		if (!departStageTrack.acceptsPickupTrain(train)){
			addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingNotTrain"),new Object[]{departStageTrack.getName()}));
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
								new Object[]{departStageTrack.getName(), eng.toString(), eng.getType(), train.getName()}));
						return false;
					}
					// does the train accept the engine model from the staging track?
					if (!train.getEngineModel().equals("") && !train.getEngineModel().equals(eng.getModel())){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildStagingDepartEngineModel"),
								new Object[]{departStageTrack.getName(), eng.toString(), eng.getModel(), train.getName()}));
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
						log.debug("Car ("+car.toString()+") has a destination ("+car.getDestinationName()+", "+car.getDestinationTrackName()+")");
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
	 * car and engine types, roads, and loads.
	 */
	private boolean checkTerminateStagingTrack(){
		if (terminateStageTrack.getNumberRS() != 0){
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildStagingTrackOccupied"),new Object[]{terminateStageTrack.getName(), terminateStageTrack.getNumberEngines(), terminateStageTrack.getNumberCars()}));
			return false;
		}
		if (terminateStageTrack.getDropRS() != 0){
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildStagingTrackReserved"),new Object[]{terminateStageTrack.getName(), terminateStageTrack.getDropRS()}));
			return false;
		}
		if (!terminateStageTrack.acceptsDropTrain(train)){
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildStagingNotTrain"),new Object[]{terminateStageTrack.getName()}));
			return false;
		} else if (!terminateStageTrack.getDropOption ().equals(Track.ANY)){
			return true;	// train can drop to this track, ignore other track restrictions
		}
		if (!Setup.isTrainIntoStagingCheckEnabled())
			return true;
		// check go see if location/track will accept the train's car and engine types
		String[] types = train.getTypeNames();
		for (int i=0; i<types.length; i++){
			if (!terminateLocation.acceptsTypeName(types[i])){
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildDestinationType"),new Object[]{terminateLocation.getName(), types[i]}));
				return false;			
			}
			if (!terminateStageTrack.acceptsTypeName(types[i])){
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildStagingTrackType"),new Object[]{terminateStageTrack.getName(),types[i]}));
				return false;			
			}
		}
		// check go see if track will accept the train's car and engine roads
		if (train.getRoadOption().equals(Train.ALLROADS) && !terminateStageTrack.getRoadOption().equals(Track.ALLROADS)){
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildStagingTrackAllRoads"),new Object[]{terminateStageTrack.getName()}));
			return false;

		}
		else if (train.getRoadOption().equals(Train.INCLUDEROADS)){
			String[] roads = train.getRoadNames();
			for (int i=0; i<roads.length; i++){
				if (!terminateStageTrack.acceptsRoadName(roads[i])){
					addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildStagingTrackRoad"),new Object[]{terminateStageTrack.getName(), roads[i]}));
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
					addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildStagingTrackRoad"),new Object[]{terminateStageTrack.getName(), roads.get(i)}));
					return false;
				}
			}
		}
		// check go see if track will accept the train's car loads
		if (train.getLoadOption().equals(Train.ALLLOADS) && !terminateStageTrack.getLoadOption().equals(Track.ALLLOADS)){
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildStagingTrackAllLoads"),new Object[]{terminateStageTrack.getName()}));
			return false;

		}
		else if (train.getLoadOption().equals(Train.INCLUDELOADS)){
			String[] loads = train.getLoadNames();
			for (int i=0; i<loads.length; i++){
				if (!terminateStageTrack.acceptsLoadName(loads[i])){
					addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildStagingTrackLoad"),new Object[]{terminateStageTrack.getName(), loads[i]}));
					return false;
				}
			}
		}
		else if (train.getLoadOption().equals(Train.EXCLUDELOADS)){
			// build a list of loads that the staging track must accept
			List<String> loads = new ArrayList<String>();
			for (int i=0; i<types.length; i++){
				List<String> allLoads = CarLoads.instance().getNames(types[i]);
				for (int j=0; j<allLoads.size(); j++){
					if (!loads.contains(allLoads.get(j)))
						loads.add(allLoads.get(j));
				}
			}
			// remove the loads that the train won't carry
			String[] excludeLoads = train.getLoadNames();
			for (int i=0; i<excludeLoads.length; i++){
				loads.remove(excludeLoads[i]);
			}
			for (int i=0; i<loads.size(); i++){
				if (!terminateStageTrack.acceptsLoadName(loads.get(i))){
					addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildStagingTrackLoad"),new Object[]{terminateStageTrack.getName(), loads.get(i)}));
					return false;
				}
			}
		}
		return true;	
	}
	
	/**
	 * Find a next destination and track for a car with a schedule load.
	 * @param car the car with the load
	 * @throws BuildFailedException 
	 */
	private void findNextDestinationForCarLoad(Car car) throws BuildFailedException{
		//log.debug("Car ("+car.toString()+ ") has load ("+car.getLoad()+") without a destination");
		addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildSearchForSiding"),new Object[]{car.toString(), car.getLoad()}));
		// don't find a next destination for cars departing staging
		//if (departStageTrack != null)
		//	return;
		List<Track> tracks = locationManager.getTracks(Track.SIDING);
		log.debug("Found "+tracks.size()+" sidings");
		for (int i=0; i<tracks.size(); i++){
			Track track = tracks.get(i);
			if (car.getTrack() != track && !track.getScheduleId().equals("")){
				ScheduleItem si = track.getCurrentScheduleItem();
				if (si == null)
					throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorNoScheduleItem"),
							new Object[]{track.getScheduleItemId(), track.getScheduleName(), track.getName(), track.getLocation().getName()}));
				log.debug("Track ("+track.getName()+") has schedule ("+track.getScheduleName()+") item id ("+si.getId()+") requesting type ("+si.getType()+") " +
						"load ("+si.getLoad()+") next dest ("+si.getDestinationName()+") track ("+si.getDestinationTrackName()+")");
				if (car.testDestination(track.getLocation(), track).equals(Car.OKAY)){
					// check the number of in bound cars to this track
					if (!track.isSpaceAvailable(car)){
						addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildNoDestTrackSpace"),
								new Object[]{car.toString(), track.getLocation().getName(), track.getName(), track.getNumberOfCarsInRoute(), track.getReservedInRoute(), track.getReservationFactor()}));
					} else {
						addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildSetFinalDestination"),
								new Object[]{car.toString(), car.getLoad(), track.getLocation().getName(), track.getName()}));
						// send car to this destination
						car.setNextDestination(track.getLocation());
						car.setNextDestTrack(track);
						// test to see if destination is reachable
						if (Router.instance().setDestination(car, train)){
							car.setScheduleId(track.getCurrentScheduleItem().getId());
							// is car part of kernel?
							car.updateKernel();
							if (car.getDestination() != track.getLocation())
								track.bumpSchedule();
							return;	//done
						} else {
							addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildNotAbleToSetDestination"),new Object[]{car.toString(), Router.instance().getStatus()}));
							car.setNextDestination(null);
							car.setNextDestTrack(null);
							car.setDestination(null, null);
						}
					}
				}
			}
		}
		addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCouldNotFindSiding"),new Object[]{car.toString(), car.getLoad()}));
	}
	
	/**
	 * Search for a siding with a schedule and load car if needed.
	 * @param car the car 
	 * @throws BuildFailedException 
	 */
	private void searchForCarLoad(Car car) throws BuildFailedException{
		addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildSearchTrackNewLoad"),
				new Object[]{car.toString(), car.getType(), car.getLoad()}));
		List<Track> tracks = locationManager.getTracks(Track.SIDING);
		log.debug("Found "+tracks.size()+" sidings");
		for (int i=0; i<tracks.size(); i++){
			Track track = tracks.get(i);
			if (!track.getScheduleId().equals("")){
				ScheduleItem si = getScheduleItem(car, track);
				if (si == null)
					continue;	// no match
				// need to set car load so testDestination will work properly
				String oldCarLoad = car.getLoad();
				car.setLoad(si.getLoad());
				String status = car.testDestination(track.getLocation(), track);
				if (!status.equals(Car.OKAY) && !status.contains(Car.LENGTH)){
					addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildNoDestTrackNewLoad"),
							new Object[]{track.getLocation().getName(), track.getName(), car.toString(), si.getLoad(), status}));
					// restore car's load
					car.setLoad(oldCarLoad);
					continue;
				}
				if (!track.isSpaceAvailable(car)){
					addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildNoDestTrackSpace"),
							new Object[]{car.toString(), track.getLocation().getName(), track.getName(), 
						track.getNumberOfCarsInRoute(), track.getReservedInRoute(), track.getReservationFactor()}));
					// restore car's load
					car.setLoad(oldCarLoad);
					continue;
				}
				car.setNextDestination(track.getLocation());
				car.setNextDestTrack(track);
				// try routing car
				if (Router.instance().setDestination(car, train)){
					// return car with this custom load and destination
					addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCreateNewLoadForCar"),
							new Object[]{car.toString(), si.getLoad(), track.getLocation().getName(), track.getName()}));
					car.setScheduleId(track.getCurrentScheduleItem().getId());
					car.setLoadGeneratedFromStaging(true);
					// is car part of kernel?
					car.updateKernel();
					track.bumpSchedule();
					return;	//done
				} else {
					addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotRouteCar"),
							new Object[]{car.toString(), si.getLoad(), track.getLocation().getName(), track.getName()}));
					car.setDestination(null, null);
				}
				// restore load and next destination and track
				car.setLoad(oldCarLoad);
				car.setNextDestination(null);
				car.setNextDestTrack(null);
			}
		}
		addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildUnableNewLoad"),
				new Object[]{car.toString()}));
	}
	
	private ScheduleItem getScheduleItem(Car car, Track track) throws BuildFailedException {
		ScheduleItem si = null;
		if (track.getScheduleMode() == Track.SEQUENTIAL){
			si = track.getCurrentScheduleItem();
			if (si == null)
				throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorNoScheduleItem"),
						new Object[]{track.getScheduleItemId(), track.getScheduleName(), track.getName(), track.getLocation().getName()}));
			return checkScheduleItem(si, car, track);
		}
		log.debug("Track ("+track.getName()+") in match mode");
		for (int i=0; i<track.getSchedule().getSize(); i++){
			si = track.getNextScheduleItem();
			if (si == null)
				throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorNoScheduleItem"),
						new Object[]{track.getScheduleItemId(), track.getScheduleName(), track.getName(), track.getLocation().getName()}));
			si = checkScheduleItem(si, car, track);
			if (si != null)
				return si;
		}
		return si;
	}
	
	// checks a schedule item to see if the car type matches, and the train and staging track can service the schedule item's load
	private ScheduleItem checkScheduleItem(ScheduleItem si, Car car, Track track){
		if (!car.getType().equals(si.getType()) || si.getLoad().equals("") || si.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
				|| si.getLoad().equals(CarLoads.instance().getDefaultLoadName()))
			return null;					
		if (!train.acceptsLoadName(si.getLoad())){
			addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildTrainNotNewLoad"),
					new Object[]{train.getName(), si.getLoad(), track.getLocation().getName(), track.getName()}));
			return null;
		}
		if (!car.getTrack().acceptsLoadName(si.getLoad())){
			addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildTrackNotNewLoad"),
					new Object[]{car.getTrackName(), si.getLoad(), track.getLocation().getName(), track.getName()}));
			return null;
		}
		return si;
	}
	
	/**
	 * Checks all of the cars on an interchange track and returns
	 * the oldest (FIFO) or newest (LIFO) car residing on that track.  Note high
	 * priority cars will be serviced first, then low.
	 * @param car the car being pulled from the interchange track
	 * @return The FIFO car at this interchange
	 */
	private Car getCarOrder(Car car){
		if (car.getTrack().getServiceOrder().equals(Track.NORMAL))
			return car;
		log.debug("Get "+car.getTrack().getServiceOrder()+" car ("+car.toString()+") from "+car.getTrack().getLocType()+" ("+car.getTrackName()+"), order: "+car.getOrder());
		Car bestCar = car;
		for (int i=carIndex+1; i<carList.size(); i++){
			Car testCar = carManager.getById(carList.get(i));
			if (testCar.getTrack() == car.getTrack()){
				log.debug(car.getTrack().getLocType()+" car ("+testCar.toString()+") has order: "+testCar.getOrder());
				if (car.getTrack().getServiceOrder().equals(Track.FIFO)
						&& bestCar.getOrder() > testCar.getOrder() 
						&& bestCar.getPriority().equals(testCar.getPriority()))
					bestCar = testCar;
				if (car.getTrack().getServiceOrder().equals(Track.LIFO)
						&& bestCar.getOrder() < testCar.getOrder() 
						&& bestCar.getPriority().equals(testCar.getPriority()))
					bestCar = testCar;
			}
		}
		return bestCar;
	}
	
	private boolean checkCarForDestinationAndTrack(Car car, RouteLocation rl, RouteLocation rld) throws BuildFailedException {
		int index;
		for (index = 0; index<routeList.size(); index++){
			if (rld == train.getRoute().getLocationById(routeList.get(index)))
					break;			
		}
		return checkCarForDestinationAndTrack(car, rl, index-1);
	}
	
	/**
	 * Checks to see if car has a destination and tries to add car to train
	 * @param car
	 * @param rl the car's route location
	 * @param routeIndex  where in the route the car pick up is
	 * @return true if car has a destination.
	 * @throws BuildFailedException if destination was staging and can't place car there
	 */
	private boolean checkCarForDestinationAndTrack(Car car, RouteLocation rl, int routeIndex) throws BuildFailedException{
		if (car.getDestination() == null)
			return false;
		addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarHasAssignedDest"),new Object[]{car.toString(), (car.getDestinationName()+", "+car.getDestinationTrackName())}));
		RouteLocation rld = train.getRoute().getLastLocationByName(car.getDestinationName());
		if (rld == null){
			// The following code should not be executed, removeCars() is called before placeCars()
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildExcludeCarDestNotPartRoute"),new Object[]{car.toString(), car.getDestinationName(), train.getRoute().getName()}));
		} else {
			if (car.getRouteLocation() != null){
				// The following code should not be executed, this should not occur if train was reset before a build!
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarAlreadyAssigned"),new Object[]{car.toString()}));
			} 
			// now go through the route and try and find a location with
			// the correct destination name
			boolean carAdded = false;
			int locCount = 0;
			for (int k = routeIndex; k<routeList.size(); k++){
				rld = train.getRoute().getLocationById(routeList.get(k));
				// if car can be picked up later at same location, skip
				if (rl != rld && rld.getName().equals(car.getLocationName())
						&& !rld.getName().equals(terminateLocation.getName())
						&& (rld.getMaxCarMoves()-rld.getCarMoves()>0) 
						&& rld.canPickup() && checkPickUpTrainDirection(car, rld)){
					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarHasSecond"),new Object[]{car.toString(), car.getLocationName()}));
					break;
				}
				if (rld.getName().equals(car.getDestinationName())){
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
					if (car.getDestinationTrack() == null){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarDoesNotHaveDest"),new Object[]{car.toString()}));
						// is there a destination track assigned for staging cars?
						if (rld == train.getTrainTerminatesRouteLocation() && terminateStageTrack != null){
							String status = car.testDestination(car.getDestination(), terminateStageTrack);
							if (status.equals(Car.OKAY)){
								addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarAssignedToStaging"),new Object[]{car.toString(), terminateStageTrack.getName()}));
								carAdded = addCarToTrain(car, rl, rld, terminateStageTrack);
							} else {
								addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropCarBecause"),new Object[]{car.toString(), terminateStageTrack.getName(), status}));
								continue;
							}
							// no, find a destination track this this car
						} else {
							List<String> tracks = car.getDestination().getTracksByMovesList(null);
							for (int s = 0; s < tracks.size(); s++){
								Track testTrack = car.getDestination().getTrackById(tracks.get(s));
								// log.debug("track (" +testTrack.getName()+ ") has "+ testTrack.getMoves() + " moves");
								// dropping to the same track isn't allowed
								if (testTrack == car.getTrack()){
									addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropCarSameTrack"),new Object[]{car.toString(), testTrack.getName()}));
									continue;
								}
								// is train direction correct?
								if (!checkDropTrainDirection(car, rld, testTrack))
									continue;
								String status = car.testDestination(car.getDestination(), testTrack);
								// is the testTrack a siding with a Schedule?
								if (testTrack.getLocType().equals(Track.SIDING) && status.contains(Car.SCHEDULE) 
										&& status.contains(Car.LOAD)){
									addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCanNotDropCarBecause"),new Object[]{car.toString(), testTrack.getName(), status}));
									continue;
								}
								if (!status.equals(Car.OKAY)){
									addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropCarBecause"),new Object[]{car.toString(), testTrack.getName(), status}));
									continue;
								}
								carAdded = addCarToTrain(car, rl, rld, testTrack);
								break;
							}
						}
						// car has a destination track
					} else {
						// going into the correct staging track?
						if (!rld.equals(train.getTrainTerminatesRouteLocation()) || terminateStageTrack == null  || terminateStageTrack == car.getDestinationTrack()){
							String status = car.testDestination(car.getDestination(), car.getDestinationTrack());
							if (status.equals(Car.OKAY) && checkDropTrainDirection(car, rld, car.getDestinationTrack()))
								carAdded = addCarToTrain(car, rl, rld, car.getDestinationTrack());
							else if (!status.equals(Car.OKAY))
								addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropCarBecause"),new Object[]{car.toString(), car.getDestinationTrackName(), status}));
						} else {
							throw new BuildFailedException(MessageFormat.format(rb.getString("buildCarDestinationStaging"),new Object[]{car.toString(), car.getDestinationName(), car.getDestinationTrackName()}));
						}
					}
					// done?
					if (carAdded)
						break;	//yes
					else
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCanNotDropCar"),new Object[]{car.toString(), car.getDestination(), locCount}));
				}
			}
		}
		return true;
	}
	
	private boolean findDestinationAndTrack(Car car, RouteLocation rl, RouteLocation rld) throws BuildFailedException {
		int index;
		for (index = 0; index<routeList.size(); index++){
			if (rld == train.getRoute().getLocationById(routeList.get(index)))
					break;			
		}
		return findDestinationAndTrack(car, rl, index-1, index+1);
	}
	
	/**
	 * Find a destination and track for a car.
	 * 
	 * @param car
	 *            The car that is looking for a destination and destination
	 *            track.
	 * @param rl
	 *            The route location for this car.
	 * @param routeIndex
	 *            Where in the train's route to begin a search for a destination
	 *            for this car.
	 * @throws BuildFailedException
	 */
	private boolean findDestinationAndTrack(Car car, RouteLocation rl, int routeIndex, int routeEnd) throws BuildFailedException {
		addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildFindDestinationForCar"),new Object[]{car.toString(), (car.getLocationName()+", " +car.getTrackName())}));
		int start = routeIndex;					// start looking after car's current location
		RouteLocation rld = null;				// the route location destination being checked for the car
		RouteLocation rldSave = null;			// holds the best route location destination for the car
		Track trackSave = null;					// holds the best track at destination for the car
		noMoreMoves = true;  					// false when there are are locations with moves
		boolean multiplePickup = false;			// true when car can be picked up from two or more locations in the route

		// more than one location in this route?
		if (routeList.size()>1)
			start++;		//yes!, no car drops at departure
		for (int k = start; k<routeEnd; k++){
			rld = train.getRoute().getLocationById(routeList.get(k));
			// if car can be picked up later at same location, set flag	
			if (rl != rld && rld.getName().equals(car.getLocationName())
					&& !rld.getName().equals(terminateLocation.getName())
					&& (rld.getMaxCarMoves()-rld.getCarMoves()>0) 
					&& rld.canPickup() && checkPickUpTrainDirection(car, rld)){
				log.debug("Car ("+car.toString()+") can be picked up later!");
				multiplePickup = true;
			}
			if (rld.canDrop() || car.hasFred() || car.isCaboose()){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildSearchingLocation"),new Object[]{rld.getName(),}));
			} else {
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildRouteNoDropLocation"),new Object[]{train.getRoute().getName(), rld.getName()}));
				continue;
			}
			// don't move car to same location unless the route only has one location (local moves) or is passenger, caboose or car with FRED
			if (rl.getName().equals(rld.getName()) && routeList.size() != 1 && !car.isPassenger() && !car.isCaboose() && !car.hasFred()){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCarLocEqualDestination"),new Object[]{car.toString(), rld.getName()}));
				continue;
			}
			// any moves left at this location?
			if (rld.getCarMoves() >= rld.getMaxCarMoves()){
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildNoAvailableMovesDest"),new Object[]{rld.getName()}));
				continue;
			}

			noMoreMoves = false;
			
			// get a "test" destination and a list of the track locations available			
			Location destinationTemp = null;
			Track trackTemp = null;
			Location testDestination = locationManager.getLocationByName(rld.getName());
			if (testDestination == null){
				// The following code should not be executed, all locations in the route have been already tested
				throw new BuildFailedException(MessageFormat.format(rb.getString("buildErrorRouteLoc"),
						new Object[]{train.getRoute().getName(), rld.getName()}));
			}
			if (!testDestination.acceptsTypeName(car.getType())){
				addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropLocation"),new Object[]{car.toString(), car.getType(), testDestination.getName()}));
				continue;
			}
			// can this location service this train's direction
			if (!checkDropTrainDirection(rld, testDestination))
				continue;
			// is the train length okay?
			if (!checkTrainLength(car, rl, rld)){
				break;	// done with this route
			}
			// is there a track assigned for staging cars?				
			if (rld == train.getTrainTerminatesRouteLocation() && terminateStageTrack != null){						
				// no need to check train and track direction into staging, already done
				String status = car.testDestination(testDestination, terminateStageTrack); 	// will staging accept this car?
				if (status.equals(Car.OKAY)){
					trackTemp = terminateStageTrack;
					destinationTemp = testDestination;
				} else {
					addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropCarBecause"),new Object[]{car.toString(), terminateStageTrack.getName(), status}));
					continue;
				} 
			// no staging track assigned, start track search
			} else {								
				List<String> tracks = testDestination.getTracksByMovesList(null);
				for (int s = 0; s < tracks.size(); s++){
					Track testTrack = testDestination.getTrackById(tracks.get(s));
					// log.debug("track (" +testTrack.getName()+ ") has "+ testTrack.getMoves() + " moves");
					// dropping to the same track isn't allowed
					if (testTrack == car.getTrack() && !car.isPassenger() && !car.isCaboose() && !car.hasFred()){
						addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropCarSameTrack"),new Object[]{car.toString(), testTrack.getName()}));
						continue;
					}
					// Can the train service this track?
					if (!checkDropTrainDirection(car, rld, testTrack))
						continue;
					String status = car.testDestination(testDestination, testTrack);
					// is the testTack a siding with a schedule demanding this car's load?
					if (status.equals(Car.OKAY) && !testTrack.getScheduleId().equals("") 
							&& !car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
							&& !car.getLoad().equals(CarLoads.instance().getDefaultLoadName())){
						addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildSidingScheduleLoad"),new Object[]{testTrack.getName(), car.getLoad()}));
						// is car part of kernel?
						car.updateKernel();
						boolean carAdded = addCarToTrain(car, rl, rld, testTrack);	// should always be true
						if (!carAdded)
							log.error ("Couldn't add car "+car.toString()+" to train ("+train.getName()+"), location ("+rl.getName()+ ") destination (" +rld.getName()+")");
						return carAdded;	// done, no build errors					
					}					
					// is the testTrack a siding with a Schedule?
					if(testTrack.getLocType().equals(Track.SIDING) && status.contains(Car.SCHEDULE) 
							&& status.contains(Car.LOAD)){
						log.debug("Siding ("+testTrack.getName()+") status: "+status);
						// is car departing a staging track that can generate schedule loads?
						if (car.getTrack().isAddLoadsEnabled() && car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())){
							ScheduleItem si = testTrack.getCurrentScheduleItem();
							// departing track and train must accept the schedule's load
							if (car.getTrack().acceptsLoadName(si.getLoad()) && train.acceptsLoadName(si.getLoad())){
								addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildAddingScheduleLoad"),new Object[]{si.getLoad(), car.toString()}));
								car.setLoad(si.getLoad());
								car.setLoadGeneratedFromStaging(true);
								// is car part of kernel?
								car.updateKernel();
								// force car to this destination
								boolean carAdded = addCarToTrain(car, rl, rld, testTrack);	// should always be true
								if (!carAdded)
									log.error ("Couldn't add car "+car.toString()+" to train ("+train.getName()+"), location ("+rl.getName()+ ") destination (" +rld.getName()+")");
								return carAdded;	// done, no build errors
							}
						}
					}
					// okay to drop car?
					if(!status.equals(Car.OKAY)){
						if (status.contains(Car.SCHEDULE))
							addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropCarLoad"),new Object[]{car.toString(), car.getLoad(), testTrack.getName(), status}));
						else
							addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropCarBecause"),new Object[]{car.toString(), testTrack.getName(), status}));
						continue;
					}		
					// No local moves from siding to siding
					if (routeList.size() == 1 && !Setup.isLocalSidingMovesEnabled() && testTrack.getLocType().equals(Track.SIDING) && car.getTrack().getLocType().equals(Track.SIDING)){
						addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildNoSidingToSidingMove"),new Object[]{testTrack.getName()}));
						continue;
					}
					// No local moves from yard to yard
					if (routeList.size() == 1 && !Setup.isLocalYardMovesEnabled() && testTrack.getLocType().equals(Track.YARD) && car.getTrack().getLocType().equals(Track.YARD)){
						addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildNoYardToYardMove"),new Object[]{testTrack.getName()}));
						continue;
					}
					// No local moves from interchange to interchange
					if (routeList.size() == 1 && !Setup.isLocalInterchangeMovesEnabled() && testTrack.getLocType().equals(Track.INTERCHANGE) && car.getTrack().getLocType().equals(Track.INTERCHANGE)){
						addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildNoInterchangeToInterchangeMove"),new Object[]{testTrack.getName()}));
						continue;
					}
					// drop to interchange or siding?
					if (testTrack.getLocType().equals(Track.INTERCHANGE) || testTrack.getLocType().equals(Track.SIDING)){
						if (testTrack.getDropOption().equals(Track.TRAINS)){
							if (testTrack.acceptsDropTrain(train)){
								log.debug("Car ("+car.toString()+") can be droped by train to this track (" +testTrack.getName()+")");
							} else {
								addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropCarInterchange"),new Object[]{car.toString(), testTrack.getName()}));
								continue;
							}
						}
						if (testTrack.getDropOption().equals(Track.ROUTES)){
							if (testTrack.acceptsDropRoute(train.getRoute())){
								log.debug("Car ("+car.toString()+") can be droped by route to this track (" +testTrack.getName()+")");
							} else {
								addLine(buildReport, SEVEN, MessageFormat.format(rb.getString("buildCanNotDropCarRoute"),new Object[]{car.toString(), testTrack.getName()}));
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
			// did we find a new destination?
			if(destinationTemp != null){
				// check for programming error
				if(trackTemp == null){
					// The following code should not be executed
					throw new BuildFailedException("Build Failure, trackTemp is null!");
				}
				addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarCanDropMoves"),new Object[]{car.toString(), (destinationTemp.getName()+ ", " +trackTemp.getName()), 
					+rld.getCarMoves(), rld.getMaxCarMoves()}));
				if (rldSave == null && multiplePickup){
					addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarHasSecond"),new Object[]{car.toString(), car.getLocationName()}));
					trackSave = null;
					break; 	//done
				}
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
					if (!trackTemp.getScheduleId().equals("")){
						log.debug("Track ("+trackTemp.getName()+") has schedule ("+trackTemp.getScheduleName()+") adjust nextRatio = "+Double.toString(nextRatio));
						nextRatio = nextRatio * nextRatio;
					}
					// try and drop off to the first location if there's more than one
					// TODO need to improve how the code detects and selects more than one drop location
					if (rldSave.getName().equals(rld.getName()) && trackTemp.equals(trackSave)){
						log.debug("Adjusting second location in route ratio to 1.0");
						nextRatio = 1;
					}
					log.debug(rldSave.getName()+" = "+Double.toString(saveRatio)+ " " + rld.getName()+" = "+Double.toString(nextRatio));
					if (saveRatio < nextRatio){
						rld = rldSave;					// the saved is better than the last found
						trackTemp = trackSave;
					} else if (multiplePickup){
						addLine(buildReport, THREE, MessageFormat.format(rb.getString("buildCarHasSecond"),new Object[]{car.toString(), car.getLocationName()}));
						trackSave = null;
						break; 	//done
					}
				}
				// every time through, save the best route destination, and track
				rldSave = rld;
				trackSave = trackTemp;
			} else {
				addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildCouldNotFindDestForCar"),new Object[]{car.toString(), rld.getName()}));
			}
		}
		// did we find a destination?
		if (trackSave != null){
			boolean carAdded = addCarToTrain(car, rl, rldSave, trackSave); // should always be true
			if (!carAdded)
				log.error ("Couldn't add car "+car.toString()+" to train "+train.getName());
			return carAdded;
		} 
		if (routeList.size()>1)
			addLine(buildReport, FIVE, MessageFormat.format(rb.getString("buildNoDestForCar"),new Object[]{car.toString()}));
		return false;	// no build errors, but car not given destination
	}

	private void buildFailed(String string){
		train.setStatus(Train.BUILDFAILED);
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
		
		addLine(fileOut, MessageFormat.format(rb.getString("Valid"), new Object[]{getDate()}));
		if (!train.getComment().equals("")){
			addLine(fileOut, train.getComment());
		}
		
		List<String> engineList = engineManager.getByTrainList(train);
		
		pickupEngines(fileOut, engineList, train.getTrainDepartsRouteLocation());
		
		List<String> carList = carManager.getByTrainDestinationList(train);
		log.debug("Train has " + carList.size() + " cars assigned to it");
		int cars = 0;
		int emptyCars = 0;
		boolean work = false;
		boolean newWork = false;
		String previousRouteLocationName = null;
		List<String> routeList = train.getRoute().getLocationsBySequenceList();
		for (int r = 0; r < routeList.size(); r++) {
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(r));
			// add line break between locations without work and ones with work
			boolean oldWork = work;
			work = isThereWorkAtLocation(carList, rl);
			if (oldWork == false && work == true)
				newLine(fileOut);		
			
			// print info only if new location
			String routeLocationName = splitString(rl.getName());
			if (!routeLocationName.equals(previousRouteLocationName) ||
					(routeLocationName.equals(previousRouteLocationName) && oldWork == false && work == true && newWork == false)){
				if (work){
					newWork = true;
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
				} 
				// add location comment
				if (Setup.isPrintLocationCommentsEnabled()){
					Location l = locationManager.getLocationByName(rl.getName());
					if (!l.getComment().equals(""))
						addLine(fileOut, l.getComment());				
				}
			}
			// add route comment
			if (!rl.getComment().equals(""))
				addLine(fileOut, rl.getComment());
			
			// engine change or helper service?
			if (train.getSecondLegOptions() != Train.NONE){
				if (rl == train.getSecondLegStartLocation()){
					engineChange(fileOut, rl, train.getSecondLegOptions());
					dropEngines(fileOut, engineList, rl);
					pickupEngines(fileOut, engineList, rl);
				}
				if (rl == train.getSecondLegEndLocation())
					addLine(fileOut, MessageFormat.format(rb.getString("RemoveHelpersAt"), new Object[]{rl.getName()}));
			}
			if (train.getThirdLegOptions() != Train.NONE){
				if (rl == train.getThirdLegStartLocation()){
					engineChange(fileOut, rl, train.getThirdLegOptions());
					dropEngines(fileOut, engineList, rl);
					pickupEngines(fileOut, engineList, rl);
				}
				if (rl == train.getThirdLegEndLocation())
					addLine(fileOut, MessageFormat.format(rb.getString("RemoveHelpersAt"), new Object[]{rl.getName()}));
			}

			// block cars by destination
			for (int j = r; j < routeList.size(); j++) {
				RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
				for (int k = 0; k < carList.size(); k++) {
					Car car = carManager.getById(carList.get(k));
					if (car.getRouteLocation() == rl
							&& car.getRouteDestination() == rld) {
						pickupCar(fileOut, car);
						cars++;
						newWork = true;
						if (car.getLoad().equals(CarLoads.instance().getDefaultEmptyName()))
							emptyCars++;
					}
				}
			}
			for (int j = 0; j < carList.size(); j++) {
				Car car = carManager.getById(carList.get(j));
				if (car.getRouteDestination() == rl) {
					dropCar(fileOut, car);
					cars--;
					newWork = true;
					if (car.getLoad().equals(CarLoads.instance().getDefaultEmptyName()))
						emptyCars--;
				}
			}
			if (r != routeList.size() - 1) {
				// Is the next location the same as the previous?
				RouteLocation rlNext = train.getRoute().getLocationById(routeList.get(r+1));
				String nextRouteLocationName = splitString(rlNext.getName());
				if (!routeLocationName.equals(nextRouteLocationName)){
					if (newWork){
						if (Setup.isPrintLoadsAndEmptiesEnabled()){
							addLine(fileOut, rb.getString("TrainDeparts")+ " " + routeLocationName +" "+ rl.getTrainDirectionString()
									+ rb.getString("boundWith") +" " + (cars-emptyCars) + " " +rb.getString("Loads")
									+", " + emptyCars + " " + rb.getString("Empties")+ ", " +rl.getTrainLength()
									+" "+rb.getString("feet")+", "+rl.getTrainWeight()+" "+rb.getString("tons"));
						} else {
							addLine(fileOut, rb.getString("TrainDeparts")+ " " + routeLocationName +" "+ rl.getTrainDirectionString()
									+ rb.getString("boundWith") +" " + cars + " " +rb.getString("cars")+", " +rl.getTrainLength()
									+" "+rb.getString("feet")+", "+rl.getTrainWeight()+" "+rb.getString("tons"));
						}
						newWork = false;
						newLine(fileOut);
					} else {
						if (r == 0){
							addLine(fileOut, MessageFormat.format(rb.getString("NoScheduledWorkAt"), new Object[]{routeLocationName})
									+", "+rb.getString("departureTime")+" "+train.getDepartureTime());
						} else if (!rl.getDepartureTime().equals("")){
							addLine(fileOut, MessageFormat.format(rb.getString("NoScheduledWorkAt"), new Object[]{routeLocationName}) 
									+", "+rb.getString("departureTime")+" "+rl.getDepartureTime());
						} else {
							addLine(fileOut, MessageFormat.format(rb.getString("NoScheduledWorkAt"), new Object[]{routeLocationName}));
						}
					}
				}
			} else {
				dropEngines(fileOut, engineList, rl);
				addLine(fileOut, rb.getString("TrainTerminatesIn")+ " " + routeLocationName);
			}
			previousRouteLocationName = routeLocationName;
		}
		// Are there any cars that need to be found?
		getCarsLocationUnknown(fileOut);
		
		fileOut.flush();
		fileOut.close();
	}
	
	// returns true if there's work at location
	private boolean isThereWorkAtLocation(List<String> carList, RouteLocation rl){
		for (int i = 0; i < carList.size(); i++) {
			Car car = carManager.getById(carList.get(i));
			if (car.getRouteLocation() == rl || car.getRouteDestination() == rl)
				return true;
		}
		return false;
	}
	
	private void engineChange(PrintWriter fileOut, RouteLocation rl, int legOptions){
		if ((legOptions & Train.HELPER_ENGINES) == Train.HELPER_ENGINES)
			addLine(fileOut, MessageFormat.format(rb.getString("AddHelpersAt"), new Object[]{rl.getName()}));
		else if ((legOptions & Train.CHANGE_CABOOSE) == Train.CHANGE_CABOOSE)
			addLine(fileOut, MessageFormat.format(rb.getString("EngineAndCabooseChangeAt"), new Object[]{rl.getName()}));
		else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES)
			addLine(fileOut, MessageFormat.format(rb.getString("EngineChangeAt"), new Object[]{rl.getName()}));
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainBuilder.class.getName());

}

class BuildFailedException extends Exception {
	public BuildFailedException(String s){
		super(s);
	}
}

