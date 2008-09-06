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
 * Represents a train on the layout
 * 
 * @author Daniel Boudreau
 * @version             $Revision: 1.3 $
 */
public class Train implements java.beans.PropertyChangeListener {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

	protected String _id = "";
	protected String _name = "";		
	protected String _description = "";
	protected RouteLocation _current = null;// where the train is located in its route
	protected String _status = "";
	protected boolean _built = false;		// when true, a train manifest has been built
	protected boolean _build = true;		// when true, build this train
	protected Route _route = null;
	protected TrainIcon _locoIcon = null;
	protected String _roadOption = ALLROADS;// train road name restrictions
	protected int _requires = 0;			// train requirements, caboose, fred
	protected String _numberEngines = "0";	// number of engines this train requires
	protected String _engineRoad = "";		// required road name for engines assigned to this train 
	protected String _engineModel = "";		// required model of engines assigned to this train
	protected String _cabooseRoad = "";		// required road name for cabooses assigned to this train

	protected String _comment = "";
	
	// property change names
	public static final String DISPOSE = "dispose";
	public static final String STOPS = "stops";
	public static final String CARTYPES = "carTypes";
	public static final String LENGTH = "length";
	public static final String ENGINELOCATION = "EngineLocation";
	public static final String NUMBERCARS = "numberCarsMoves";
	
	// Train status
	private static final String BUILDFAILED = rb.getString("BuildFailed");
	private static final String BUILDING = rb.getString("Building");
	private static final String BUILT = rb.getString("Built") + " ";
	private static final String PARTIALBUILT = rb.getString("Partial") + " ";
	private static final String TERMINATED = rb.getString("Terminated");
	private static final String TRAINRESET = rb.getString("TrainReset");
	
	public static final int NONE = 0;		// train requirements
	public static final int CABOOSE = 1;
	public static final int FRED = 2;
	
	public static final String ALLROADS = rb.getString("All");			// train services all road names 
	public static final String INCLUDEROADS = rb.getString("Include");
	public static final String EXCLUDEROADS = rb.getString("Exclude");
	
	public static final String AUTO = rb.getString("Auto");				// how engines are assigned to this train
	
	public Train(String id, String name) {
		log.debug("New train " + name + " " + id);
		_name = name;
		_id = id;
	}

	public String getId() {
		return _id;
	}

	public void setName(String name) {
		String old = _name;
		_name = name;
		if (!old.equals(name)){
			firePropertyChange("name", old, name);
		}
	}
	
	// for combo boxes
	public String toString(){
		return _name;
	}

	public String getName() {
		return _name;
	}
	
	/**
	 * Set train requirements
	 * @param requires NONE CABOOSE FRED
	 */	
	public void setRequirements(int requires){
		int old = _requires;
		_requires = requires;
		if (old != requires)
			firePropertyChange("requires", Integer.toString(old), Integer.toString(requires));
	}
	
	public int getRequirements(){
		return _requires;
	}
	
	public void setRoute(Route route) {
		Route old = _route;
		String oldRoute = "";
		String newRoute = "";
		if (old != null){
			old.removePropertyChangeListener(this);
			oldRoute = old.toString();
		}
		if (route != null){
			route.addPropertyChangeListener(this);
			newRoute = route.toString();
		}
		_route = route;
		_skipLocationsList.clear();
		if (old == null || !old.equals(route)){
			firePropertyChange("route", oldRoute, newRoute);
		}
	}
	
	public Route getRoute() {
		return _route;
	}
	
	public String getTrainRouteName (){
    	if (_route == null)
    		return "";
    	else
    		return _route.getName();
    }
    
	/**
	 * 
	 * @return train's departure location's name
	 */
	public String getTrainDepartsName(){
    	if (getTrainDepartsRouteLocation() != null ){
     		return getTrainDepartsRouteLocation().getName();
     	}else{
    		return "";
    	}
    }
	
	private RouteLocation getTrainDepartsRouteLocation(){
    	if (_route == null){
    		return null;
    	}else{
    		List list = _route.getLocationsBySequenceList();
    		if (list.size()>0){
    			RouteLocation rl = _route.getLocationById((String)list.get(0));
    			return rl;
    		}else{
    			return null;
    		}
    	}
    }
    
	public String getTrainTerminatesName(){
		if (getTrainTerminatesRouteLocation() != null){
			return getTrainTerminatesRouteLocation().getName();
		}else{
			return "";
		}
	}
	
	private RouteLocation getTrainTerminatesRouteLocation(){
    	if (_route == null){
    		return null;
    	}else{
    		List list = _route.getLocationsBySequenceList();
    		if (list.size()>0){
    			RouteLocation rl = _route.getLocationById((String)list.get(list.size()-1));
    			return rl;
    		}else{
    			return null;
    		}
    	}
    }

    //  Train's current route location
	public void setCurrent(RouteLocation current) {
		RouteLocation old = _current;
		_current = current;
		if (old == null || !old.equals(current)){
			firePropertyChange("current", old, current);
		}
	}
	
	// Train's current route location name
	public String getCurrentName() {
		if (_current == null)
			return "";
		else
			return _current.getName();
	}
	
	// Train's current route location
	public RouteLocation getCurrent(){
		return _current;
	}
	
	// Train status
	public void setStatus(String status) {
		String old = _status;
		_status = status;
		if (!old.equals(status)){
			firePropertyChange("status", old, status);
		}
	}
	
	public String getStatus() {
		return _status;
	}
	

	List _skipLocationsList = new ArrayList();

	private String[] getTrainSkipsLocations(){
		String[] locationIds = new String[_skipLocationsList.size()];
		for (int i=0; i<_skipLocationsList.size(); i++)
			locationIds[i] = (String)_skipLocationsList.get(i);
		return locationIds;
	}

	private void setTrainSkipsLocations(String[] locationIds){
		if (locationIds.length == 0) return;
		jmri.util.StringUtil.sort(locationIds);
		for (int i=0; i<locationIds.length; i++)
			_skipLocationsList.add(locationIds[i]);
	}

	public void addTrainSkipsLocation(String locationId){
		// insert at start of _skipLocationsList, sort later
		if (_skipLocationsList.contains(locationId))
			return;
		_skipLocationsList.add(0,locationId);
		log.debug("train does not stop at "+locationId);
		firePropertyChange (STOPS, null, LENGTH);
	}

	public void deleteTrainSkipsLocation(String locationId){
		_skipLocationsList.remove(locationId);
		log.debug("train will stop at "+locationId);
		firePropertyChange (STOPS, null, LENGTH);
	}

	public boolean skipsLocation(String locationId){
		return _skipLocationsList.contains(locationId);
	}

    List _typeList = new ArrayList();
    public String[] getTypeNames(){
      	String[] types = new String[_typeList.size()];
     	for (int i=0; i<_typeList.size(); i++)
     		types[i] = (String)_typeList.get(i);
   		return types;
    }
    
    /**
     * Set the type of cars this will service, see types in
     * Cars.
     * @param types 
     */
    public void setTypeNames(String[] types){
    	if (types.length == 0) return;
    	jmri.util.StringUtil.sort(types);
 		for (int i=0; i<types.length; i++)
 			_typeList.add(types[i]);
    }
    
    public void addTypeName(String type){
    	// insert at start of list, sort later
    	if (_typeList.contains(type))
    		return;
    	_typeList.add(0,type);
    	log.debug("train add car type "+type);
    	firePropertyChange (CARTYPES, null, LENGTH);
    }
    
    public void deleteTypeName(String type){
    	_typeList.remove(type);
    	log.debug("train delete car type "+type);
     	firePropertyChange (CARTYPES, null, LENGTH);
     }
    
    public boolean acceptsTypeName(String type){
    	return _typeList.contains(type);
    }
    
	public String getRoadOption (){
    	return _roadOption;
    }
    
 	/**
 	 * Set how this train deals with car road names
 	 * @param option ALLROADS INCLUDEROADS EXCLUDEROADS
 	 */
    public void setRoadOption (String option){
    	_roadOption = option;
    }

    List _roadList = new ArrayList();
    private void setRoadNames(String[] roads){
    	if (roads.length == 0) return;
    	jmri.util.StringUtil.sort(roads);
 		for (int i=0; i<roads.length; i++)
 			_roadList.add(roads[i]);
    }
    
    /**
     * Provides a list of road names that the train will
     * either service or exclude.  See setRoadOption
     * @return
     */
    public String[] getRoadNames(){
      	String[] roads = new String[_roadList.size()];
     	for (int i=0; i<_roadList.size(); i++)
     		roads[i] = (String)_roadList.get(i);
     	if (_roadList.size() == 0)
     		return roads;
     	jmri.util.StringUtil.sort(roads);
   		return roads;
    }
    
    public void addRoadName(String road){
     	if (_roadList.contains(road))
    		return;
    	_roadList.add(road);
    	log.debug("train (" +getName()+ ") add car road "+road);
    }
    
    public void deleteRoadName(String road){
    	_roadList.remove(road);
    	log.debug("train (" +getName()+ ") delete car road "+road);
    }
    
    public boolean acceptsRoadName(String road){
    	if (_roadOption.equals(ALLROADS)){
    		return true;
    	}
       	if (_roadOption.equals(INCLUDEROADS)){
       		return _roadList.contains(road);
       	}
       	// exclude!
       	return !_roadList.contains(road);
    }
    /**
     * The number of cars worked by this train
     * @return
     */
    public int getNumberCarsWorked(){
    	int NumCars = 0;
    	List cars = carManager.getCarsByTrainList();
    	for (int i=0; i<cars.size(); i++){
    		Car car = carManager.getCarById((String)cars.get(i));
    		if(this == car.getTrain() && car.getRouteLocation() != null)
    			NumCars++;
    	}
    	return NumCars;
    }
    
    public void setDescription(String description) {
		String old = _description;
		_description = description;
		if (!old.equals(description)){
			firePropertyChange("description", old, description);
		}
	}
	
	public String getDescription() {
		return _description;
	}
	
	public void setNumberEngines(String number) {
		_numberEngines = number;
	}

	public String getNumberEngines() {
		return _numberEngines;
	}
	
	public void setEngineRoad(String road) {
		_engineRoad = road;
	}

	public String getEngineRoad() {
		return _engineRoad;
	}
	
	public void setEngineModel(String model) {
		_engineModel = model;
	}

	public String getEngineModel() {
		return _engineModel;
	}
	
	public void setCabooseRoad(String road) {
		_cabooseRoad = road;
	}

	public String getCabooseRoad() {
		return _cabooseRoad;
	}
	
	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
	}
	
	public void setBuilt(boolean built) {
		boolean old = _built;
		_built = built;
		if (old != built){
			firePropertyChange("built", old?"true":"false", built?"true":"false");
		}
	}
	
	public boolean getBuilt() {
		return _built;
	}
	
	public void setBuild(boolean build) {
		boolean old = _build;
		_build = build;
		if (old != build){
			firePropertyChange("build", old?"true":"false", build?"true":"false");
		}
	}
	
	public boolean getBuild() {
		return _build;
	}
	
	public void buildIfSelected(){
		if(_build && !_built)
			build();
		else
			log.debug("Train ("+getName()+") not selected or already built, skipping build");
	}
	
	// build variables shared between local routines
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
	public void build(){
		setStatus(BUILDING);
		setBuilt(false);
		numberCars = 0;
		maxWeight = 0;
		iconEngine = null;
		
		// create build status file
		File file = TrainManagerXml.instance().createTrainBuildReportFile(getName());
		PrintWriter fileOut = null;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file)),
					true);
		} catch (IOException e) {
			log.error("can not open build status file");
			return;
		}

		Date now = new Date();
		addLine(fileOut, "Build report for train ("+getName()+") built on "+now);
		
		if (getRoute() == null){
			buildFailed(fileOut, "Can't build train ("+getName()+"), needs a route");
			return;
		}
		routeList = getRoute().getLocationsBySequenceList();
		if (routeList.size() < 1){
			buildFailed(fileOut, "Route needs at least one location to build train ("+getName()+")");
			return;
		}
		// train departs
		departLocation = locationManager.getLocationByName(getTrainDepartsName());
		if (departLocation == null){
			buildFailed(fileOut, "Route departure location missing for train ("+getName()+")");
			return;
		}
		// train terminates
		terminateLocation = locationManager.getLocationByName(getTrainTerminatesName());
		if (terminateLocation == null){
			buildFailed(fileOut, "Route terminate location missing for train ("+getName()+")");
			return;
		}
		// DAB this needs to be controled by each train
		if (getTrainDepartsRouteLocation().getMaxCarMoves() > departLocation.getNumberCars() && Control.fullTrainOnly){
			buildFailed(fileOut, "Not enough cars ("+departLocation.getNumberCars()+") at departure ("+getTrainDepartsName()+") to build train ("+getName()+")");
			return;
		}
		// get the number of requested car moves
		int requested = 0;
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = getRoute().getLocationById((String)routeList.get(i));
			// check to see if there's a location for each stop in the route
			Location l = locationManager.getLocationByName(rl.getName());
			if (l == null){
				buildFailed(fileOut, "Location missing in route ("+getRoute().getName()+")");
				return;
			}
			// train doesn't drop or pickup cars from staging locations found in middle of a route
			List slStage = l.getSecondaryLocationsByMovesList(SecondaryLocation.STAGING);
			if (slStage.size() > 0 && i!=0 && i!=routeList.size()-1){
				addLine(fileOut, "Location ("+rl.getName()+") has only staging tracks");
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			}
			// if a location is skipped, no drops or pickups
			else if(skipsLocation(rl.getId())){
				addLine(fileOut, "Location (" +rl.getName()+ ") is skipped by train "+getName());
				rl.setCarMoves(rl.getMaxCarMoves());	// don't allow car moves for this location
			// we're going to use this location, so initialize the location
			}else{
				requested = requested + rl.getMaxCarMoves();
				rl.setCarMoves(0);					// clear the number of moves
				rl.setSecondaryLocation(null);		// used for staging only
				addLine(fileOut, "Location (" +rl.getName()+ ") requests " +rl.getMaxCarMoves()+ " moves");
			}
		}
		int carMoves = requested;
		if(routeList.size()> 1)
			requested = requested/2;  // only need half as many cars to meet requests
		addLine(fileOut, "Route (" +getRoute().getName()+ ") requests " + requested + " cars and " + carMoves +" moves");

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
		carList = carManager.getCarsAvailableTrainList(this);
		// DAB this needs to be controled by each train
		if (requested > carList.size() && Control.fullTrainOnly){
			buildFailed(fileOut, "The number of requested cars (" +requested+ ") for train (" +getName()+ ") is greater than the number available (" +carList.size()+ ")");
			return;
		}
		// get any requirements for this train
		boolean caboose = false;		// start off without any requirements
		boolean fred = false;
		boolean foundFred = true;
		boolean foundCaboose = true;
		String textRequires = "none";
		if (getRequirements()>0){
			if ((getRequirements()& FRED) > 0){
				fred = true;
				foundFred = false;
				textRequires = "FRED";
			} 
			if ((getRequirements()& CABOOSE) > 0){
				caboose = true;
				foundCaboose = false;
				textRequires = "caboose";
			}
			if (!getCabooseRoad().equals("")){
				textRequires += " road ("+getCabooseRoad()+")";
			}
			addLine(fileOut, "Train ("+getName()+") requires "+textRequires);
		}
		// show road names that this train will service
		if (!getRoadOption().equals(ALLROADS)){
			String[] roads = getRoadNames();
	    	String roadNames ="";
	    	for (int i=0; i<roads.length; i++){
	    		roadNames = roadNames + roads[i]+" ";
	    	}
	    	addLine(fileOut, "Train ("+getName()+") "+getRoadOption()+" roads "+roadNames);
		}
		// show car types that this train will service
		String[] types =getTypeNames();
		String typeNames ="";
    	for (int i=0; i<types.length; i++){
    		typeNames = typeNames + types[i]+" ";
    	}
    	addLine(fileOut, "Train ("+getName()+") services car types: "+typeNames);
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
    			if (!acceptsRoadName(c.getRoad())){
    				addLine(fileOut, "Exclude car ("+c.getId()+") road ("+c.getRoad()+") at location ("+c.getLocationName()+", "+c.getSecondaryLocationName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    			if (!acceptsTypeName(c.getType())){
    				addLine(fileOut, "Exclude car ("+c.getId()+") type ("+c.getType()+") at location ("+c.getLocationName()+", "+c.getSecondaryLocationName()+")");
    				carList.remove(carList.get(carIndex));
    				carIndex--;
    				continue;
    			}
    		}
    		// don't service a car at interchange and has been dropped of by this train
			if (c.getSecondaryLocation().getLocType().equals(SecondaryLocation.INTERCHANGE) && c.getSavedRouteId().equals(getRoute().getId())){
				addLine(fileOut, "Exclude car ("+c.getId()+") at interchange ("+c.getLocationName()+", "+c.getSecondaryLocationName()+")");
				carList.remove(carList.get(carIndex));
				carIndex--;
				continue;
			}
		}

		addLine(fileOut, "Found " +carList.size()+ " cars for train (" +getName()+ ")");

		// adjust carlist to only have cars from one staging track
		if (departStage != null){
			// Make sure that all cars in staging are moved
			getTrainDepartsRouteLocation().setCarMoves(getTrainDepartsRouteLocation().getMaxCarMoves()-departStage.getNumberCars());  // neg number moves more cars
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
				buildFailed(fileOut, "Not all cars in staging can be serviced by this train, " +(departStage.getNumberCars()-numberCarsFromStaging)+" cars have road or types that can't be serviced");
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
				RouteLocation rld = getRoute().getLocationByName(c.getDestination().getName());
				if (rld == null){
					addLine(fileOut, "Car (" + c.getId()+ ") destination (" +c.getDestination().getName()+ ") not part of this train's route (" +getRoute().getName() +"), removed from car list");
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
				if(c.getLocationName().equals(getTrainDepartsName())){
					if (c.getDestination() == null || c.getDestination() == terminateLocation){
						if (getCabooseRoad().equals("") || getCabooseRoad().equals(c.getRoad())){
							// find a secondary location
							if (getTrainTerminatesRouteLocation().getSecondaryLocation() == null){
								List sls = terminateLocation.getSecondaryLocationsByMovesList(null);
								for (int s = 0; s < sls.size(); s++){
									SecondaryLocation sld = terminateLocation.getSecondaryLocationById((String)sls.get(s));
									if (c.testDestination(terminateLocation, sld).equals(c.OKAY)){
										addCarToTrain(fileOut, c, getTrainDepartsRouteLocation(), getTrainTerminatesRouteLocation(), terminateLocation, sld);
										if (c.isCaboose())
											foundCaboose = true;
										if (c.hasFred())
											foundFred = true;
										break;
									}
								}
								addLine(fileOut,"Could not find a destination for ("+c.getId()+")");
							// terminate into staging	
							} else if (c.testDestination(terminateLocation, getTrainTerminatesRouteLocation().getSecondaryLocation()).equals(c.OKAY)){
								addCarToTrain(fileOut, c, getTrainDepartsRouteLocation(), getTrainTerminatesRouteLocation(), terminateLocation, getTrainTerminatesRouteLocation().getSecondaryLocation());
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
			buildFailed(fileOut, "Train ("+getName()+") requires "+textRequires+", none found at departure ("+getTrainDepartsName()+")");
			return;
		}
		addLine(fileOut, "Requested cars (" +requested+ ") for train (" +getName()+ ") the number available (" +carList.size()+ ") building train!");

		// now find destinations for cars 
		int numLocs = routeList.size();
		if (numLocs > 1)  // don't find car destinations for the last location in the route
			numLocs--;
		for (int locationIndex=0; locationIndex<numLocs; locationIndex++){
			RouteLocation rl = getRoute().getLocationById((String)routeList.get(locationIndex));
			if(skipsLocation(rl.getId())){
				addLine(fileOut, "Location (" +rl.getName()+ ") is skipped by train (" +getName()+ ")");
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
								RouteLocation rld = getRoute().getLocationByName(c.getDestination().getName());
								if (rld == null){
									addLine(fileOut, "Car (" + c.getId()+ ") destination not part of route (" +getRoute().getName() +")");
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
								addLine(fileOut, "Find destinations for car ("+c.getId()+") with " + c.getMoves() + " car moves at location (" +c.getLocationName()+", " +c.getSecondaryLocationName()+ ")");
								int start = locationIndex;				// start looking after car's current location
								RouteLocation rld = null;				// the route location destination being checked for the car
								RouteLocation rldSave = null;			// holds the best route location destination for the car
								SecondaryLocation secondarySave = null;	// holds the best secondary destination for the car
								Location destinationSave = null;		// holds the best destination for the car
						
								// more than one location in this route?
								if (routeList.size()>1)
									start++;		//yes!, no car drops at departure"
								for (int k = start; k<routeList.size(); k++){
									rld = getRoute().getLocationById((String)routeList.get(k));
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
												buildFailed(fileOut, "Route ("+getRoute().getName()+") missing location ("+rld.getName()+")");
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
				addLine(fileOut, (success?"Success, ":"Partial, ") +moves+ "/" +saveReqMoves+ " cars at location (" +rl.getName()+ ") assigned to train ("+getName()+")");
			}
		}

		setCurrent(getTrainDepartsRouteLocation());
		if (numberCars < requested){
			setStatus(PARTIALBUILT + getNumberCarsWorked() +"/" + requested + " "+ rb.getString("moves"));
			addLine(fileOut, PARTIALBUILT + getNumberCarsWorked() +"/" + requested + " "+ rb.getString("moves"));
		}else{
			setStatus(BUILT + getNumberCarsWorked() + " "+ rb.getString("moves"));
			addLine(fileOut, BUILT + getNumberCarsWorked() + " "+ rb.getString("moves"));
		}
		setBuilt(true);
		if (fileOut != null){
			fileOut.flush();
			fileOut.close();
		}

		// now build manifest
		makeManifest();
		// now create and place train icon
		moveTrainIcon(getTrainDepartsRouteLocation());

	}
	
	// get the engines for this train, if secondary != null, then engines must
	// come from that secondary location (staging).  Returns true if engines found, else false.
	// This routine will also setup the secondary location if the train is
	// terminating into staging, therefore this routine should only be called once when return is true.
	private boolean getEngines(PrintWriter fileOut, SecondaryLocation secondary){
		// show engine requirements for this train
		addLine(fileOut, "Train requires "+getNumberEngines()+" engine(s) type ("+getEngineModel()+") road (" +getEngineRoad()+")");
				
		numberEngines = 0;
		int reqEngines = 0; 	
		int engineLength = 0;
		
		// DAB this doesn't work yet!
		if (getNumberEngines().equals(AUTO)){
			// how many engines required last time?

			// determine number of engines required by train weight!
			double tons = maxWeight*Setup.getScaleRatio();

		} else {
			reqEngines = Integer.parseInt(getNumberEngines());
		}

		// get list of engines for this route
		List engineList = engineManager.getEnginesAvailableTrainList(this);
		// remove engines not at departure, wrong road name, or part of consist
		for (int i=0; i<engineList.size(); i++){
			Engine engine = engineManager.getEngineById((String) engineList.get(i));
			addLine(fileOut, "Engine ("+engine.getId()+") road ("+engine.getRoad()+") type ("+engine.getModel()+") at location ("+engine.getLocationName()+", "+engine.getSecondaryLocationName()+")");
			if(engine.getLocationName().equals(getTrainDepartsName()) && (secondary == null || engine.getSecondaryLocationName().equals(secondary.getName()))){
				if ((getEngineRoad().equals("") || engine.getRoad().equals(getEngineRoad())) && (getEngineModel().equals("") || engine.getModel().equals(getEngineModel()))){
					// is this engine part of a consist?  Keep only lead engines in consist.
					if (engine.getConsist() != null){
						addLine(fileOut, "Engine ("+engine.getId()+") is part of consist ("+engine.getConsist().getName()+")");
						if (!engine.getConsist().isLeadEngine(engine)){
							// only use lead engines
							engineList.remove(i);
							i--;
						}
					}
					continue;
				} 
			}
			addLine(fileOut, "Exclude engine ("+engine.getId()+")");
			engineList.remove(i);
			i--;
		}
		// if leaving staging, use any number of engines if required number is 0
		boolean leavingStaging = false;
		if (secondary != null && reqEngines == 0)
			leavingStaging = true;

		// now load the number of engines into the train
		SecondaryLocation terminateSecondary = null;
		for (int indexEng=0; indexEng<engineList.size(); indexEng++){
			Engine engine = engineManager.getEngineById((String) engineList.get(indexEng));
			iconEngine = engine;		//load Icon
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
				addLine(fileOut, "Could not find valid destination for engines at (" +terminateLocation.getName()+ ") for train (" +getName()+ ")");
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
							cEngine.setTrain(this);
							cEngine.setRouteLocation(getTrainDepartsRouteLocation());
							cEngine.setRouteDestination(getTrainTerminatesRouteLocation());
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
					engine.setTrain(this);
					engine.setRouteLocation(getTrainDepartsRouteLocation());
					engine.setRouteDestination(getTrainTerminatesRouteLocation());
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
			RouteLocation rl = getRoute().getLocationById((String)routeList.get(i));
			rl.setTrainLength(engineLength);		// load the engine(s) length
		}
		// terminating into staging?
		if (terminateSecondary != null && terminateSecondary.getLocType().equals(terminateSecondary.STAGING)){
			getTrainTerminatesRouteLocation().setSecondaryLocation(terminateSecondary);
		}
		return true;
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
					kCar.setTrain(this);
					kCar.setRouteLocation(rl);
					kCar.setRouteDestination(rld);
					kCar.setDestination(destination, secondary);
				}
				// not part of kernel, add one car	
			} else {
				addLine(file, "Car ("+car.getId()+") assigned destination ("+destination.getName()+", "+secondary.getName()+")");
				car.setTrain(this);
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
				double weight = 0;
				RouteLocation rlt = getRoute().getLocationById((String)routeList.get(i));
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
						weight = weight + Double.parseDouble(car.getWeight());
					} catch (Exception e){
						log.debug ("car ("+car.getId()+") weight not set");
					}
					if (car.getKernel() != null){
						length = car.getKernel().getLength();
						weight = car.getKernel().getWeight();
					}
					rlt.setTrainLength(rlt.getTrainLength()+length);
				}
				if (weight > maxWeight){
					maxWeight = weight;		// used for AUTO engines
				}
			}
			firePropertyChange(NUMBERCARS, Integer.toString(oldNum), Integer.toString(moves));
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
			RouteLocation rlt = getRoute().getLocationById((String)routeList.get(i));
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
		if (rld == getTrainTerminatesRouteLocation())
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
		setStatus(BUILDFAILED);
		if(log.isDebugEnabled())
			log.debug(string);
		JOptionPane.showMessageDialog(null, string,
				"Can not build train ("+getName()+") " +getDescription(),
				JOptionPane.ERROR_MESSAGE);
		if (file != null){
			file.println(string);
			// Write to disk and close file
			file.println("Build failed for train ("+getName()+")");
			file.flush();
			file.close();
			if(TrainManager.instance().getBuildReport()){
				File buildFile = TrainManagerXml.instance().getTrainBuildReportFile(getName());
				printReport(buildFile, "Train Build Failure Report", true);
			}
		}
	}
	
	public static void printReport (File file, String name, boolean isPreview, String fontName){
	    // obtain a HardcopyWriter to do this
		HardcopyWriter writer = null;
		Frame mFrame = new Frame();
        try {
            writer = new HardcopyWriter(mFrame, name, 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        // set font
        if (!fontName.equals(""))
        	writer.setFontName(fontName);
        
        // now get the build file to print

		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			return;
		}
		String newLine = "\n";
		String line = " ";

		while (line != null) {
			try {
				line = in.readLine();
			} catch (IOException e) {
				log.debug("Print read failed");
				break;
			}
			if (line == null)
				break;
			try {
				writer.write(line + newLine);
			} catch (IOException e) {
				log.debug("Print write failed");
				break;
			}
		}
        // and force completion of the printing
		try {
			in.close();
		} catch (IOException e) {
			log.debug("Print close failed");
		}
        writer.close();
	}
	
	public static void printReport(File file, String name, boolean isPreview){
		printReport(file, name, isPreview, "");
 	}
	
	public void makeManifest() {
		// create manifest file
		File file = TrainManagerXml.instance().createTrainManifestFile(
				getName());
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
		addLine(fileOut, rb.getString("ManifestForTrain")+" (" + getName() + ") "+ getDescription());
		addLine(fileOut, "Valid " + new Date());
		if (!getComment().equals("")){
			addLine(fileOut, getComment());
		}
		
		List engineList = engineManager.getEnginesByTrainList(this);
		Engine engine = null;
		for (int i =0; i < engineList.size(); i++){
			engine = engineManager.getEngineById((String) engineList.get(i));
			addLine(fileOut, rb.getString("Engine")+" "+ engine.getRoad() + " " + engine.getNumber() + " (" +engine.getModel()+  ") "+rb.getString("assignedToThisTrain"));
		}
		
		if (engine != null)
			addLine(fileOut, "Pickup engine(s) at "+engine.getLocationName()+", "+engine.getSecondaryLocationName());
		
		List carList = carManager.getCarsByTrainList(this);
		log.debug("Train will move " + carList.size() + " cars");
		int cars = 0;
		List routeList = getRoute().getLocationsBySequenceList();
		for (int i = 0; i < routeList.size(); i++) {
			RouteLocation rl = getRoute().getLocationById((String) routeList.get(i));
			newLine(fileOut);
			addLine(fileOut, rb.getString("ScheduledWorkIn")+" " + rl.getName());
			// block cars by destination
			for (int j = i; j < routeList.size(); j++) {
				RouteLocation rld = getRoute().getLocationById((String) routeList.get(j));
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
						+ rb.getString("boundWith") +" " + cars + " " +rb.getString("cars")+", " +rl.getTrainLength()+" "+rb.getString("feet"));
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
		addLine(file, rb.getString("Pickup")+" " + car.getRoad() + " "
				+ carNumber[0] + " " + car.getType() + " "
				+ car.getLength() + " " + car.getColor()
				+ (car.isHazardous() ? " ("+rb.getString("Hazardous")+") " : " ")
				+ (car.hasFred() ? " ("+rb.getString("fred")+") " : " ") + rb.getString("from")+ " "
				+ car.getSecondaryLocationName());
	}
	
	private void dropCar(PrintWriter file, Car car){
		String[] carNumber = car.getNumber().split("-"); // ignore any duplicate car numbers
		addLine(file, rb.getString("Drop")+ " " + car.getRoad() + " "
				+ carNumber[0] + " " + car.getType() + " "
				+ car.getLength() + " " + car.getColor()
				+ (car.isHazardous() ? " ("+rb.getString("Hazardous")+") " : " ")
				+ rb.getString("to") + " " + car.getSecondaryDestinationName());
	}
	
	public void printBuildReport(){
		if(_built && TrainManager.instance().getBuildReport()){
			File buildFile = TrainManagerXml.instance().getTrainBuildReportFile(getName());
			boolean isPreview = TrainManager.instance().getPrintPreview();
			printReport(buildFile, "Train Build Report", isPreview);
		}
	}
	
	public void printManifest(){
		if(_built){
			File file = TrainManagerXml.instance().getTrainManifestFile(getName());
			boolean isPreview = TrainManager.instance().getPrintPreview();
			printReport(file, "Train Manifest", isPreview, Setup.getFontName());
		}else{
			String string = "Need to build train (" +getName()+ ") before printing manifest";
			log.debug(string);
			JOptionPane.showMessageDialog(null, string,
					"Can not print manifest",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void printIfSelected(){
		if(_build)
			printManifest();
		else
			log.debug("Train ("+getName()+") not selected, skipping printing manifest");
	}
	
	Engine iconEngine; // lead engine for icon

	private void createTrainIcon() {
		if (_locoIcon != null && _locoIcon.isActive()) {
			_locoIcon.remove();
			_locoIcon.dispose();
		}

		String name = getName();
		if (iconEngine != null && Setup.isTrainIconAppendEnabled())
			name += iconEngine.getNumber();

		PanelEditor pe = PanelMenu.instance().getPanelEditorByName(
				Setup.getPanelName());
		LayoutEditor le = PanelMenu.instance().getLayoutEditorByName(
				Setup.getPanelName());

		if (pe != null) {
			_locoIcon = new TrainIcon();
			pe.putLocoIcon(_locoIcon);
			// try layout editor
		} else if (le != null) {
			_locoIcon = new TrainIcon();
			le.putLocoIcon(_locoIcon);
		}
		if (pe != null || le != null) {
			_locoIcon.setText(name);
			_locoIcon.setTrain(this);
			setTrainIconColor();
			if (name.length() > 9) {
				_locoIcon.setFontSize(8.f);
			}
		}
	}
	
	private void setTrainIconColor(){
		// set color based on train direction at current location
		String dir = trainIconRl.getTrainDirection();
		if (dir.equals(trainIconRl.NORTH))
			_locoIcon.setLocoColor(Setup.getTrainIconColorNorth());
		if (dir.equals(trainIconRl.SOUTH))
			_locoIcon.setLocoColor(Setup.getTrainIconColorSouth());
		if (dir.equals(trainIconRl.EAST))
			_locoIcon.setLocoColor(Setup.getTrainIconColorEast());
		if (dir.equals(trainIconRl.WEST))
			_locoIcon.setLocoColor(Setup.getTrainIconColorWest());
		// local train?
		if (routeList.size()==1)
			_locoIcon.setLocoColor(Setup.getTrainIconColorLocal());
		// Terminated train?
		if (getCurrentName().equals(""))
			_locoIcon.setLocoColor(Setup.getTrainIconColorTerminate());
	}
	
	/**
	 * Sets the panel position for the train icon   
	 * for the current route location.
	 */
	public void setTrainIconCordinates(){
		if (Setup.isTrainIconCordEnabled()){
			trainIconRl.setTrainIconX(_locoIcon.getX());
			trainIconRl.setTrainIconY(_locoIcon.getY());
		} else{
			JOptionPane.showMessageDialog(null, "See Operations -> Settings to enable Set X&Y",
					"Set X&Y is disabled",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Move train to next location in route.  Will move
	 * engines, cars, and train icon.
	 */
	public void move() {
		if (getRoute() == null || getCurrent() == null)
			return;
		List routeList = getRoute().getLocationsBySequenceList();
		for (int i = 0; i < routeList.size(); i++) {
			RouteLocation rl = getRoute().getLocationById((String) routeList.get(i));
			if (getCurrent() == rl) {
				i++;
				RouteLocation rlNew = rl;	// use current if end of route
				if (i < routeList.size()) {
					rlNew = getRoute().getLocationById((String) routeList.get(i));
					setCurrent(rlNew);		// note, _current becomes null after all cars moved at terminate
				} 
				moveEngines(rl, rlNew);
				moveCars(rl, rlNew);
				moveTrainIcon(rlNew);
				break;
			}
		}
	}
		
	RouteLocation trainIconRl = null;  // saves the icon current route location
	private void moveTrainIcon(RouteLocation rl){
		trainIconRl = rl;
		// create train icon if at departure or if program has been restarted
		routeList = getRoute().getLocationsBySequenceList();		// reload routeList in case builds were saved
		if (rl == getTrainDepartsRouteLocation() || _locoIcon == null){
			createTrainIcon();
		}
		if (_locoIcon != null && _locoIcon.isActive()){
			setTrainIconColor();
			if (getCurrentName().equals(""))
				_locoIcon.setToolTipText(getDescription() + " terminated");
			else
				_locoIcon.setToolTipText(getDescription() + " at " + getCurrentName());
			if (rl.getTrainIconX()!=0 || rl.getTrainIconY()!=0){
				_locoIcon.setLocation(rl.getTrainIconX(), rl.getTrainIconY());
			}
		} 
	}

	private void moveEngines(RouteLocation old, RouteLocation next){
		List engines = engineManager.getEnginesByTrainList();
		for (int i=0; i<engines.size(); i++){
			Engine engine = engineManager.getEngineById((String)engines.get(i));
			if(this == engine.getTrain() && old == engine.getRouteLocation()){
				log.debug("engine ("+engine.getId()+") is in train (" +getName()+") leaves location ("+old.getName()+") arrives ("+next.getName()+")");
				if(engine.getRouteLocation() == engine.getRouteDestination()){
					log.debug("engine ("+engine.getId()+") has arrived at destination");
					engine.setLocation(engine.getDestination(), engine.getSecondaryDestination());
					engine.setDestination(null, null); 	// this also clears the route locations
					engine.setTrain(null);
				}else{
					Location nextLocation = locationManager.getLocationByName(next.getName());
					engine.setLocation(nextLocation, null);
					engine.setRouteLocation(next);
				}
			}
		}
		firePropertyChange(ENGINELOCATION, old.getName(), next.getName());
	}

	private void moveCars(RouteLocation old, RouteLocation next){
		int oldNum = getNumberCarsWorked();
		List cars = carManager.getCarsByTrainList();
		int dropCars = 0;
		int pickupCars = 0;
		int departCars = 0;
		for (int i=0; i<cars.size(); i++){
			Car car = carManager.getCarById((String)cars.get(i));
			if(this == car.getTrain() && old == car.getRouteLocation()){
				log.debug("car ("+car.getId()+") is in train (" +getName()+") leaves location ("+old.getName()+") arrives ("+next.getName()+")");
				if(car.getRouteLocation() == car.getRouteDestination()){
					log.debug("car ("+car.getId()+") has arrived at destination");
					car.setLocation(car.getDestination(), car.getSecondaryDestination());
					car.setDestination(null, null); 	// this also clears the route locations
					car.setTrain(null);
					dropCars++;
				}else{
					Location nextLocation = locationManager.getLocationByName(next.getName());
					if (car.getSecondaryLocation() != null)
						pickupCars++;
					car.setLocation(nextLocation, null);
					car.setRouteLocation(next);
					departCars++;
				}
			}
		}
		if (old == next && dropCars == 0 && pickupCars == 0 && departCars == 0){
			setStatus(TERMINATED);
			setCurrent(null);
			setBuilt(false);
		}else{
			log.debug(dropCars+ " Drop " +pickupCars+ " Add " +departCars+ " Cars");
			if (departCars > 0 || dropCars == 0)
				setStatus(departCars+ " "+rb.getString("Cars"));
			else
				setStatus(rb.getString("Drop")+" " +dropCars+ " "+rb.getString("Cars"));
		}
		firePropertyChange(NUMBERCARS, Integer.toString(oldNum), Integer.toString(getNumberCarsWorked()));
	}
	
	public void reset(){
		// is this train in route?
		if (getCurrentName() != "" && getTrainDepartsRouteLocation() != getCurrent()){
			log.error("Train has started its route, can not reset");
			JOptionPane.showMessageDialog(null,
					"Train is in route to "+getTrainTerminatesName(), "Can not reset train!",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// remove engines assigned to this train
		List engines = engineManager.getEnginesByTrainList();
		for (int i=0; i<engines.size(); i++){
			Engine engine = engineManager.getEngineById((String)engines.get(i));
			if(this == engine.getTrain() && engine.getRouteLocation()!=null){
				engine.setTrain(null);
				engine.setDestination(null, null);
			}
		}
		// remove all cars assigned to this train
		List cars = carManager.getCarsByTrainList();
		int oldNum = getNumberCarsWorked();
		for (int i=0; i<cars.size(); i++){
			Car car = carManager.getCarById((String)cars.get(i));
			if(this == car.getTrain() && car.getRouteLocation()!=null){
				car.setTrain(null);
				car.setDestination(null, null);
			}
		}
		setStatus(TRAINRESET);
		setCurrent(null);
		setBuilt(false);
		firePropertyChange(NUMBERCARS, Integer.toString(oldNum), Integer.toString(0));
	}
    
    public void dispose(){
    	if (getRoute() != null)
    		getRoute().removePropertyChangeListener(this);
    	firePropertyChange (DISPOSE, null, DISPOSE);
    }
  
 	
   /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml
     *
     * @param e  Consist XML element
     */
    public Train(org.jdom.Element e) {
//        if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
        if ((a = e.getAttribute("id")) != null )  _id = a.getValue();
        else log.warn("no id attribute in train element when reading operations");
        if ((a = e.getAttribute("name")) != null )  _name = a.getValue();
        if ((a = e.getAttribute("description")) != null )  _description = a.getValue();
        if ((a = e.getAttribute("route")) != null ) {
       		setRoute(RouteManager.instance().getRouteByName(a.getValue()));
        }
        if ((a = e.getAttribute("skip")) != null ) {
        	String locationIds = a.getValue();
           	String[] locs = locationIds.split("%%");
//        	if (log.isDebugEnabled()) log.debug("Train skips : "+locationIds);
           	setTrainSkipsLocations(locs);
        }
        if ((a = e.getAttribute("carTypes")) != null ) {
        	String names = a.getValue();
           	String[] Types = names.split("%%");
//        	if (log.isDebugEnabled()) log.debug("Car types: "+names);
        	setTypeNames(Types);
        }
        if ((a = e.getAttribute("current")) != null ){
        	if (_route != null)
        		_current = _route.getLocationById(a.getValue());
        }
        if ((a = e.getAttribute("carRoadOperation")) != null )  _roadOption = a.getValue();
        if ((a = e.getAttribute("carRoads")) != null ) {
        	String names = a.getValue();
           	String[] roads = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("Secondary location (" +getName()+ ") " +getRoadOption()+  " car roads: "+ names);
        	setRoadNames(roads);
        }
        if ((a = e.getAttribute("numberEngines")) != null)
        	_numberEngines = a.getValue();
        if ((a = e.getAttribute("engineRoad")) != null)
        	_engineRoad = a.getValue();
        if ((a = e.getAttribute("engineModel")) != null)
        	_engineModel = a.getValue();
        if ((a = e.getAttribute("requires")) != null)
        	_requires = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("cabooseRoad")) != null)
        	_cabooseRoad = a.getValue();
 		if ((a = e.getAttribute("built")) != null)
			_built = a.getValue().equals("true");
		if ((a = e.getAttribute("build")) != null)
			_build = a.getValue().equals("true");
		if ((a = e.getAttribute("status")) != null )  _status = a.getValue();
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
 
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
        org.jdom.Element e = new org.jdom.Element("train");
        e.setAttribute("id", getId());
        e.setAttribute("name", getName());
        e.setAttribute("description", getDescription());
        Route route = getRoute();
        if (route != null)
        	e.setAttribute("route", getRoute().toString());
        else
        	e.setAttribute("route", "");
        // build list of location that this train skips
        String[] locationIds = getTrainSkipsLocations();
        String names ="";
        for (int i=0; i<locationIds.length; i++){
        	names = names + locationIds[i]+"%%";
        }
        // build list of car types for this train
        String[] types = getTypeNames();
        String typeNames ="";
        for (int i=0; i<types.length; i++){
        	typeNames = typeNames + types[i]+"%%";
        }
        e.setAttribute("carTypes", typeNames);
        e.setAttribute("skip", names);
        if (getCurrent() != null)
        	e.setAttribute("current", getCurrent().getId());
       	// build list of car roads for this train
    	String[] roads = getRoadNames();
    	String roadNames ="";
    	for (int i=0; i<roads.length; i++){
    		roadNames = roadNames + roads[i]+"%%";
    	}
    	e.setAttribute("carRoadOperation", getRoadOption());
    	e.setAttribute("carRoads", roadNames);
        e.setAttribute("numberEngines", getNumberEngines());
        e.setAttribute("engineRoad", getEngineRoad());
        e.setAttribute("engineModel", getEngineModel());
        e.setAttribute("requires", Integer.toString(getRequirements()));
        e.setAttribute("cabooseRoad", getCabooseRoad());
        e.setAttribute("built", getBuilt()?"true":"false");
        e.setAttribute("build", getBuild()?"true":"false");
        e.setAttribute("status", getStatus());
        e.setAttribute("comment", getComment());
        return e;
    }
    

    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled())
    		log.debug("train (" + getName() + ") sees property change: "
    				+ e.getPropertyName() + " old: " + e.getOldValue() + " new: "
    				+ e.getNewValue());
    	if (e.getPropertyName().equals(Route.DISPOSE)){
    		setRoute(null);
    	}
    	// forward any property changes in this train's route
    	firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
    }

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(
			this);

	public synchronized void addPropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String p, Object old, Object n) {
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(Train.class.getName());

}
