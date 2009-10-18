// EngineManager.java

package jmri.jmrit.operations.rollingstock.engines;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import org.jdom.Element;

import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.trains.Train;


/**
 * Manages the engines.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.24 $
 */
public class EngineManager {
	
	// Edit engine frame attributes
	protected EngineEditFrame _engineEditFrame = null;
	protected Dimension _editFrameDimension = null;
	protected Point _editFramePosition = null;

	LocationManager locationManager = LocationManager.instance();
	protected Hashtable<String, Engine> _engineHashTable = new Hashtable<String, Engine>();   		// stores Engines by id
	protected Hashtable<String, Consist> _consistHashTable = new Hashtable<String, Consist>();   	// stores Consists by number

	public static final String LISTLENGTH_CHANGED_PROPERTY = "EngineListLength";
	public static final String CONSISTLISTLENGTH_CHANGED_PROPERTY = "KernelListLength";

    public EngineManager() {
    }
    
	/** record the single instance **/
	private static EngineManager _instance = null;

	public static synchronized EngineManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("EngineManager creating instance");
			// create and load
			_instance = new EngineManager();
			OperationsXml.instance();					// load setup
	    	// create manager to load engines and their attributes
	    	EngineManagerXml.instance();
			log.debug("Engines have been loaded!");
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("EngineManager returns instance "+_instance);
		return _instance;
	}

	public void setEngineEditFrame(EngineEditFrame frame){
		_engineEditFrame = frame;
	}

	public Dimension getEngineEditFrameSize(){
		return _editFrameDimension;
	}

	public Point getEngineEditFramePosition(){
		return _editFramePosition;
	}
	/**
	 * @return Number of engines in the Roster
	 */
    public int getNumEntries() {
		return _engineHashTable.size();
	}
    
    public void dispose() {
    	deleteAll();
    }

 
    /**
     * @return requested Engine object or null if none exists
     */
    public Engine getEngineById(String engineId) {
        return _engineHashTable.get(engineId);
    }
    
    public Engine getEngineByRoadAndNumber (String engineRoad, String engineNumber){
    	String engineId = Engine.createId (engineRoad, engineNumber);
    	return getEngineById (engineId);
    }
    
    /**
     * Get an engine by Radio Frequency Identification (RFID). 
     * @param rfid engine's RFID.
     * @return the engine with the specific RFID, or null if not found.
     */
    public Engine getEngineByRfid(String rfid){
    	Enumeration<String> en = _engineHashTable.keys();
    	while (en.hasMoreElements()) { 
    		Engine engine = getEngineById(en.nextElement());
    		if(engine.getRfid().equals(rfid))
    			return engine;
    	}
    	return null;
    }
 
    /**
     * Finds an existing engine or creates a new engine if needed
     * requires engine's road and number
     * @param engineRoad
     * @param engineNumber
     * @return new engine or existing engine
     */
    public Engine newEngine (String engineRoad, String engineNumber){
    	Engine engine = getEngineByRoadAndNumber(engineRoad, engineNumber);
    	if (engine == null){
    		engine = new Engine(engineRoad, engineNumber);
    		register(engine);
    	}
    	return engine;
    }
    
    /**
     * Load a engine.
 	 */
    public void register(Engine engine) {
    	Integer oldSize = new Integer(_engineHashTable.size());
        _engineHashTable.put(engine.getId(), engine);
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_engineHashTable.size()));
    }

    /**
     * Unload a engine.
     */
    public void deregister(Engine engine) {
    	engine.dispose();
        Integer oldSize = new Integer(_engineHashTable.size());
    	_engineHashTable.remove(engine.getId());
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_engineHashTable.size()));
    }
    
    /**
     * Remove all engines from roster
     */
    public void deleteAll(){
    	Integer oldSize = new Integer(_engineHashTable.size());
    	Enumeration<String> en = _engineHashTable.keys();
    	while (en.hasMoreElements()) { 
    		Engine engine = getEngineById(en.nextElement());
    		engine.dispose();
            _engineHashTable.remove(engine.getId());
    	}
    	firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_engineHashTable.size()));
    }
    
    public Consist newConsist(String name){
    	Consist consist = getConsistByName(name);
    	if (consist == null){
    		consist = new Consist(name);
    		Integer oldSize = new Integer(_consistHashTable.size());
    		_consistHashTable.put(name, consist);
    		firePropertyChange(CONSISTLISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_consistHashTable.size()));
    	}
    	return consist;
    }
    
    public void deleteConsist(String name){
    	Consist consist = getConsistByName(name);
    	if (consist != null){
    		consist.dispose();
    		Integer oldSize = new Integer(_consistHashTable.size());
    		_consistHashTable.remove(name);
    		firePropertyChange(CONSISTLISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_consistHashTable.size()));
    	}
    }
    
    public Consist getConsistByName(String name){
    	Consist consist = _consistHashTable.get(name);
    	return consist;
    }
    
    public JComboBox getConsistComboBox(){
    	JComboBox box = new JComboBox();
    	box.addItem("");
       	List<String> consistNames = getConsistNameList();
    	for (int i=0; i<consistNames.size(); i++) {
       		box.addItem(consistNames.get(i));
    	}
    	return box;
    }
    
    public void updateConsistComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
    	List<String> consistNames = getConsistNameList();
    	for (int i=0; i<consistNames.size(); i++) {
       		box.addItem(consistNames.get(i));
    	}
    }
    
    public List<String> getConsistNameList(){
    	String[] arr = new String[_consistHashTable.size()];
    	List<String> out = new ArrayList<String>();
       	Enumeration<String> en = _consistHashTable.keys();
       	int i=0;
    	while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
    	}
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
    	return out;
    }
    
   /**
     * Sort by engine id
     * @return list of engine ids ordered by id
     */
    public List<String> getEnginesByIdList() {        
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _engineHashTable.keys();
        String[] arr = new String[_engineHashTable.size()];
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    /**
     * Sort by engine road name
     * @return list of engine ids ordered by road name
     */
    public List<String> getEnginesByRoadNameList() {
       	// first get by id list
    	List<String> sortById = getEnginesByIdList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String engineRoad = "";
    	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortById.size(); i++){
    		engineAdded = false;
    		engine = getEngineById (sortById.get(i));
    		engineRoad = engine.getRoad();
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById (out.get(j));
    			String outEngineRoad = engine.getRoad();
    			if (engineRoad.compareToIgnoreCase(outEngineRoad)<0){
    				out.add(j, sortById.get(i));
    				engineAdded = true;
    				break;
    			}
    		}
    		if (!engineAdded){
    			out.add(sortById.get(i));
    		}
    	}
    	return out;
    }
    
    private static final int pageSize = 64;
    
    /**
     * Sort by engine number, number can alpha numeric
     * @return list of engine ids ordered by number
     */
    public List<String> getEnginesByNumberList() {
    	//log.debug("start Engine sort by number list");
    	// first get by road list
    	List<String> sortIn = getEnginesByRoadNameList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	int engineNumber = 0;
    	int outEngineNumber = 0;
    	int notInteger = -999999999;	// flag when Engine number isn't an integer
    	String[] number;
    	boolean engineAdded = false;

    	for (int i=0; i<sortIn.size(); i++){
    		engineAdded = false;
    		try{
    			engineNumber = Integer.parseInt(getEngineById(sortIn.get(i)).getNumber());
    			getEngineById(sortIn.get(i)).number = engineNumber;
    		}catch (NumberFormatException e) {
    			// maybe engine number in the format xxx-y
    	   		try{
        			number = getEngineById(sortIn.get(i)).getNumber().split("-");
        			engineNumber = Integer.parseInt(number[0]);
        			getEngineById(sortIn.get(i)).number = engineNumber;
        		}catch (NumberFormatException e2) {
        			getEngineById(sortIn.get(i)).number = notInteger;
        			// sort alpha numeric numbers at the end of the out list
        			String numberIn = getEngineById(sortIn.get(i)).getNumber(); 
        			//log.debug("engine in road number ("+numberIn+") isn't a number");
        			for (int k=(out.size()-1); k>=0; k--){
        				String numberOut = getEngineById(out.get(k)).getNumber();
        				try{
        					Integer.parseInt(numberOut);
        					// done, place engine with alpha numeric number after
        					// engines with real numbers.
        					out.add(k+1, sortIn.get(i));
        					engineAdded = true;
        					break;
        				}catch (NumberFormatException e3) {
        					if (numberIn.compareToIgnoreCase(numberOut)>=0){
            					out.add(k+1, sortIn.get(i));
            					engineAdded = true;
            					break;
        					}
        				}
        			}
        			if(!engineAdded)
        				out.add(0, sortIn.get(i));
        			continue;
        		}
    		}
 
    		int start = 0;
    		// page to improve sort performance. 
    		int divisor = out.size()/pageSize;
    		for (int k=divisor; k>0; k--){
    			outEngineNumber  = getEngineById(out.get((out.size()-1)*k/divisor)).number;
    			if(outEngineNumber == notInteger)
    				continue;
    			if (engineNumber >= outEngineNumber){
    				start = (out.size()-1)*k/divisor;
    				break;
    			}
    		}
    		for (int j=start; j<out.size(); j++ ){
    			outEngineNumber = getEngineById(out.get(j)).number;
    			if (outEngineNumber == notInteger){
    				try{
    					outEngineNumber = Integer.parseInt(getEngineById(out.get(j)).getNumber());
    				}catch (NumberFormatException e) {        			
    					try{      	   			
    						number = getEngineById(out.get(j)).getNumber().split("-");
    						outEngineNumber = Integer.parseInt(number[0]);
    					}catch (NumberFormatException e2) {
    						//Engine eng = getEngineById(out.get(j));
    						//log.debug("Engine ("+eng.getId()+") road number ("+eng.getNumber()+") isn't a number");
    						// force engine add
    						outEngineNumber = engineNumber+1;
    					}
    				}
    			}
        		if (engineNumber < outEngineNumber){
        			out.add(j, sortIn.get(i));
        			engineAdded = true;
        			break;
        		}
    		}
    		if (!engineAdded){
    			out.add(sortIn.get(i));
    		}
    	}
    	//log.debug("end engine sort by number list");
    	return out;
    }
    
    /**
     * Sort by engine model
     * @return list of engine ids ordered by engine model
     */
    public List<String> getEnginesByModelList() {
    	// first get by road list
    	List<String> sortByRoad = getEnginesByRoadNameList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String engineModel = "";
    	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortByRoad.size(); i++){
    		engineAdded = false;
    		engine = getEngineById (sortByRoad.get(i));
    		engineModel = engine.getModel();
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById (out.get(j));
    			String outEngineModel = engine.getModel();
    			if (engineModel.compareToIgnoreCase(outEngineModel)<0){
    				out.add(j, sortByRoad.get(i));
    				engineAdded = true;
    				break;
    			}
    		}
    		if (!engineAdded){
    			out.add(sortByRoad.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by engine consist
     * @return list of engine ids ordered by engine consist
     */
    public List<String> getEnginesByConsistList() {
    	// first get by road list
    	List<String> sortByRoad = getEnginesByRoadNameList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String engineConsistName = "";
    	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortByRoad.size(); i++){
    		engineAdded = false;
    		engine = getEngineById (sortByRoad.get(i));
    		engineConsistName = engine.getConsistName();
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById (out.get(j));
    			String outEngineConsistName = engine.getConsistName();
    			if (engineConsistName.compareToIgnoreCase(outEngineConsistName)<0){
    				out.add(j, sortByRoad.get(i));
    				engineAdded = true;
    				break;
    			}
    		}
    		if (!engineAdded){
    			out.add(sortByRoad.get(i));
    		}
    	}
    	return out;
    }

    
    /**
     * Sort by engine location
     * @return list of engine ids ordered by engine location
     */
    public List<String> getEnginesByLocationList() {
    	// first get by road list
    	List<String> sortByRoad = getEnginesByRoadNameList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String engineLocation = "";
    	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortByRoad.size(); i++){
    		engineAdded = false;
    		engine = getEngineById (sortByRoad.get(i));
    		engineLocation = engine.getLocationName()+engine.getTrackName();
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById (out.get(j));
    			String outEngineLocation = engine.getLocationName()+engine.getTrackName();
    			if (engineLocation.compareToIgnoreCase(outEngineLocation)<0){
    				out.add(j, sortByRoad.get(i));
    				engineAdded = true;
    				break;
    			}
    		}
    		if (!engineAdded){
    			out.add(sortByRoad.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by engine destination
     * @return list of engine ids ordered by engine destination
     */
    public List<String> getEnginesByDestinationList() {
    	// first get by location list
    	List<String> sortByLocation = getEnginesByLocationList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String engineDestination = "";
    	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortByLocation.size(); i++) {
			engineAdded = false;
			engine = getEngineById(sortByLocation.get(i));
			engineDestination = engine.getDestinationName()+engine.getDestinationTrackName();
			for (int j=0; j<out.size(); j++) {
				engine = getEngineById(out.get(j));
				String outEngineDestination = engine.getDestinationName()+engine.getDestinationTrackName();
				if (engineDestination.compareToIgnoreCase(outEngineDestination) < 0 ) {
					out.add(j, sortByLocation.get(i));
					engineAdded = true;
					break;
				}
			}
			if (!engineAdded) {
				out.add(sortByLocation.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by engines in trains
     * @return list of engine ids ordered by trains
     */
    public List<String> getEnginesByTrainList() {
    	// first get by road list
    	List<String> sortByRoad = getEnginesByLocationList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
     	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortByRoad.size(); i++){
    		engineAdded = false;
    		engine = getEngineById (sortByRoad.get(i));
    		String engineTrainName = "";
    		if(engine.getTrain() != null)
    			engineTrainName = engine.getTrain().getName();
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById (out.get(j));
    			String outEngineTrainName = "";
    			if(engine.getTrain() != null)
    				outEngineTrainName = engine.getTrain().getName();
    			if (engineTrainName.compareToIgnoreCase(outEngineTrainName)<0){
    				out.add(j,sortByRoad.get(i));
    				engineAdded = true;
    				break;
    			}
    		}
    		if (!engineAdded){
    			out.add(sortByRoad.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by engines moves
     * @return list of engine ids ordered by engine moves
     */
    public List<String> getEnginesByMovesList() {
    	// get random order of engine ids
    	Enumeration<String> en = _engineHashTable.keys();
    	List<String> sortIn = new ArrayList<String>();
        while (en.hasMoreElements()) {
        	sortIn.add(en.nextElement());
        }

    	// now re-sort
    	List<String> out = new ArrayList<String>();
     	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortIn.size(); i++){
    		engineAdded = false;
    		engine = getEngineById (sortIn.get(i));
				int inMoves = engine.getMoves();
				for (int j=0; j<out.size(); j++) {
					engine = getEngineById(out.get(j));
					int outMoves = engine.getMoves();
					if (inMoves < outMoves) {
						out.add(j,sortIn.get(i));
						engineAdded = true;
						break;
					}
				}
     		if (!engineAdded){
    			out.add(sortIn.get(i));
    		}
    	}
    	return out;
    }

    /**
     * Sort by engine owner
     * @return list of engine ids ordered by owner name
     */
    public List<String> getEnginesByOwnerList() {
       	// first get by id list
    	List<String> sortById = getEnginesByIdList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String engineOwner = "";
    	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortById.size(); i++){
    		engineAdded = false;
    		engine = getEngineById (sortById.get(i));
    		engineOwner = engine.getOwner();
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById (out.get(j));
    			String outEngineOwner = engine.getOwner();
    			if (engineOwner.compareToIgnoreCase(outEngineOwner)<0){
    				out.add(j, sortById.get(i));
    				engineAdded = true;
    				break;
    			}
    		}
    		if (!engineAdded){
    			out.add(sortById.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by engine built date
     * @return list of engine ids ordered by built date
     */
    public List<String> getEnginesByBuiltList() {
       	// first get by id list
    	List<String> sortById = getEnginesByIdList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String engineBuilt = "";
    	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortById.size(); i++){
    		engineAdded = false;
    		engine = getEngineById (sortById.get(i));
    		engineBuilt = engine.getBuilt();
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById (out.get(j));
    			String outEngineBuilt = engine.getBuilt();
    			if (engineBuilt.compareToIgnoreCase(outEngineBuilt)<0){
    				out.add(j, sortById.get(i));
    				engineAdded = true;
    				break;
    			}
    		}
    		if (!engineAdded){
    			out.add(sortById.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by engine RFID
     * @return list of engine ids ordered by RFIDs
     */
    public List<String> getEnginesByRfidList() {
      	// first get by id list
    	List<String> sortById = getEnginesByIdList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String engineRfid = "";
    	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortById.size(); i++){
    		engineAdded = false;
    		engine = getEngineById (sortById.get(i));
    		engineRfid = engine.getRfid();
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById (out.get(j));
    			String outEngineRfid = engine.getRfid();
    			if (engineRfid.compareToIgnoreCase(outEngineRfid)<0){
    				out.add(j, sortById.get(i));
    				engineAdded = true;
    				break;
    			}
    		}
    		if (!engineAdded){
    			out.add(sortById.get(i));
    		}
    	}
    	return out;
    }
   
    /**
	 * return a list available engines (no assigned train) on a route, engines are
	 * ordered least recently moved to most recently moved.
	 * 
	 * @param train
	 * @return Ordered list of engine ids not assigned to a train
	 */
    public List<String> getEnginesAvailableTrainList(Train train) {
    	Route route = train.getRoute();
    	// get a list of locations served by this route
    	List<String> routeList = route.getLocationsBySequenceList();
    	// don't include engines at route destination
    	RouteLocation destination = null;
    	if (routeList.size()>1){
    		destination = route.getLocationById(routeList.get(routeList.size()-1));
    		// However, if the destination is visited at least once, must include all engines
    		RouteLocation test;
    		for (int i=0; i<routeList.size()-1; i++){
    			test = route.getLocationById(routeList.get(i));
    			if (destination.getName().equals(test.getName())){
    				destination = null;
    				break;
    			}
    		}
    	}
    	// get engines by moves list
    	List<String> enginesSortByMoves = getEnginesByMovesList();
    	// now build list of available engines for this route
    	List<String> out = new ArrayList<String>();
    	Engine engine;
 
    	for (int i = 0; i < enginesSortByMoves.size(); i++) {
    		engine = getEngineById(enginesSortByMoves.get(i));
       		// only use engines with a location
    		if (engine.getLocationName().equals(""))
    			continue;
    		RouteLocation rl = route.getLastLocationByName(engine.getLocationName());
    		// get engines that don't have an assigned train, or the assigned train is this one 
    		if (rl != null && rl != destination && (engine.getTrain() == null || train.equals(engine.getTrain()))){
    			out.add(enginesSortByMoves.get(i));
    		}
    	}
    	return out;
    }
    
    /**
	 * return a list of engines assigned to a train sorted by destination.
	 * 
	 * @param train
	 * @return Ordered list of assigned engines
	 */
    public List<String> getEnginesByTrainList(Train train) {
    	// get engines available list
    	List<String> available = getEnginesAvailableTrainList(train);
    	List<String> inTrain = new ArrayList<String>();
    	Engine engine;

    	for (int i = 0; i < available.size(); i++) {
    		engine = getEngineById(available.get(i));
    		// get only engines that are assigned to this train
    		if(engine.getTrain() == train)
    			inTrain.add(available.get(i));
    	}
    	// now sort by track destination
    	List<String> out = new ArrayList<String>();
    	boolean engineAdded;	 
    	for (int i = 0; i < inTrain.size(); i++) {
    		engineAdded = false;
    		engine = getEngineById(inTrain.get(i));
    		String engineDestination = engine.getDestinationTrackName();
    		for (int j = 0; j < out.size(); j++) {
    			Engine engineOut = getEngineById (out.get(j));
    			String engineOutDest = engineOut.getDestinationTrackName();
    			if (engineDestination.compareToIgnoreCase(engineOutDest)<0){
    				out.add(j, inTrain.get(i));
    				engineAdded = true;
    				break;
    			}
    		}
    		if (!engineAdded){
    			out.add(inTrain.get(i));
     		}
    	}
    	return out;
    }
    
    /**
     * Get a list of engine road names.
     * @return List of engine road names.
     */
    public List<String> getEngineRoadNames(String model){
    	List<String> names = new ArrayList<String>();
       	Enumeration<String> en = _engineHashTable.keys();
    	while (en.hasMoreElements()) { 
    		Engine engine = getEngineById(en.nextElement());
    		if ((engine.getModel().equals(model) || model.equals(""))
    				&& !names.contains(engine.getRoad())){
    			names.add(engine.getRoad());
    		}
    	}
    	return sortList(names);
    }
    
    private List<String> sortList(List<String> list){
    	List<String> out = new ArrayList<String>();
    	for (int i=0; i<list.size(); i++){
    		int j;
    		for (j=0; j<out.size(); j++) {
    		if (list.get(i).compareToIgnoreCase(out.get(j))<0)
    			break;
    		}
    		out.add(j, list.get(i));
    	}
    	return out;
    }

	public void options (org.jdom.Element values) {
		if (log.isDebugEnabled()) log.debug("ctor from element "+values);
		// get Engine Edit attributes
		Element e = values.getChild("engineEditOptions");
		if (e != null){
			try {
				int x = e.getAttribute("x").getIntValue();
				int y = e.getAttribute("y").getIntValue();
				int height = e.getAttribute("height").getIntValue();
				int width = e.getAttribute("width").getIntValue();
				_editFrameDimension = new Dimension(width, height);
				_editFramePosition = new Point(x,y);
			} catch ( org.jdom.DataConversionException ee) {
				log.debug("Did not find engine edit frame attributes");
			} catch ( NullPointerException ne) {
				log.debug("Did not find engine edit frame attributes");
			}
		}
	}

	   /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-locations.dtd.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
    	Element values = new Element("options");
        // now save Engine Edit frame size and position
        Element e = new org.jdom.Element("engineEditOptions");
        Dimension size = getEngineEditFrameSize();
        Point posn = getEngineEditFramePosition();
        if (_engineEditFrame != null){
        	size = _engineEditFrame.getSize();
        	posn = _engineEditFrame.getLocation();
        	_editFrameDimension = size;
        	_editFramePosition = posn;
        }
        if (posn != null){
        	e.setAttribute("x", ""+posn.x);
        	e.setAttribute("y", ""+posn.y);
        }
        if (size != null){
        	e.setAttribute("height", ""+size.height);
        	e.setAttribute("width", ""+size.width); 
        }
        values.addContent(e);
        return values;
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EngineManager.class.getName());

}

