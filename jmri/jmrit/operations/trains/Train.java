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

import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;

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
 * @version             $Revision: 1.12 $
 */
public class Train implements java.beans.PropertyChangeListener {
	
	// WARNING DO NOT LOAD CAR OR ENGINE MANAGERS WHEN Train.java IS CREATED
	// IT CAUSES A RECURSIVE LOOP AT LOAD TIME
	//CarManager carManager = CarManager.instance();
	//EngineManager engineManager = EngineManager.instance();
	
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
	
	protected RouteLocation getTrainDepartsRouteLocation(){
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
	
	protected RouteLocation getTrainTerminatesRouteLocation(){
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
	
	// Train's next route location name
	public String getNextLocationName(){
		List routeList = getRoute().getLocationsBySequenceList();
		for (int i=0; i<routeList.size(); i++){
			RouteLocation rl = getRoute().getLocationById((String)routeList.get(i));
			if (rl == getCurrent()){
				i++;
				if (i < routeList.size()){
					rl = getRoute().getLocationById((String)routeList.get(i));
					return rl.getName();
				} else {
					break;
				}
			}
		}
		return getCurrentName();	// At end of route
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
     * @return Array of road names as Strings
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
     */
    public int getNumberCarsWorked(){
    	int NumCars = 0;
    	CarManager carManager = CarManager.instance();
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

	public void build(){
		TrainBuilder tb = new TrainBuilder();
		tb.build(this);
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

	/**
	 * Sets the panel position for the train icon   
	 * for the current route location.
	 */
	public void setTrainIconCordinates(){
		if (Setup.isTrainIconCordEnabled()){
			trainIconRl.setTrainIconX(_locoIcon.getX());
			trainIconRl.setTrainIconY(_locoIcon.getY());
			RouteManagerXml.setDirty(true);
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
	protected void moveTrainIcon(RouteLocation rl){
		trainIconRl = rl;
		// create train icon if at departure or if program has been restarted
		if (rl == getTrainDepartsRouteLocation() || _locoIcon == null){
			createTrainIcon();
		}
		if (_locoIcon != null && _locoIcon.isActive()){
			setTrainIconColor();
			if (getCurrentName().equals(""))
				_locoIcon.setToolTipText(getDescription() + " "+TERMINATED+" ("+ getTrainTerminatesName()+")");
			else
				_locoIcon.setToolTipText(getDescription() + " at " + getCurrentName() + " next "+getNextLocationName());
			if (rl.getTrainIconX()!=0 || rl.getTrainIconY()!=0){
				_locoIcon.setLocation(rl.getTrainIconX(), rl.getTrainIconY());
			}
		} 
	}
	
	public String getIconName(){
		String name = getName();
		if (getBuilt() && iconEngine != null && Setup.isTrainIconAppendEnabled())
			name += " "+iconEngine.getNumber();
		return name;
	}
	
	Engine iconEngine; // lead engine for icon

	private void createTrainIcon() {
		if (_locoIcon != null && _locoIcon.isActive()) {
			_locoIcon.remove();
			_locoIcon.dispose();
		}
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
			_locoIcon.setText(getIconName());
			_locoIcon.setTrain(this);
			setTrainIconColor();
			if (getIconName().length() > 9) {
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
		if (getRoute().getLocationsBySequenceList().size()==1)
			_locoIcon.setLocoColor(Setup.getTrainIconColorLocal());
		// Terminated train?
		if (getCurrentName().equals(""))
			_locoIcon.setLocoColor(Setup.getTrainIconColorTerminate());
	}
	
	LocationManager locationManager = LocationManager.instance();

	private void moveEngines(RouteLocation old, RouteLocation next){
		EngineManager engineManager = EngineManager.instance();
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
		CarManager carManager = CarManager.instance();
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
		EngineManager engineManager = EngineManager.instance();
		List engines = engineManager.getEnginesByTrainList();
		for (int i=0; i<engines.size(); i++){
			Engine engine = engineManager.getEngineById((String)engines.get(i));
			if(this == engine.getTrain() && engine.getRouteLocation()!=null){
				engine.setTrain(null);
				engine.setDestination(null, null);
			}
		}
		// remove all cars assigned to this train
		CarManager carManager = CarManager.instance();
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
