// EngineManager.java

package jmri.jmrit.operations.engines;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.SecondaryLocation;

import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;

import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.cars.Car;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;


/**
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.2 $
 */
public class EngineManager implements java.beans.PropertyChangeListener {
	
	LocationManager locationManager = LocationManager.instance();
	protected Hashtable _engineHashTable = new Hashtable();   		// stores Engines by id
	protected Hashtable _consistHashTable = new Hashtable();   	// stores Kernels by number

	public static final String LISTLENGTH = "EngineListLength";
	public static final String CONSISTLISTLENGTH = "KernelListLength";

    public EngineManager() {
    }
    
	/** record the single instance **/
	private static EngineManager _instance = null;

	public static synchronized EngineManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("EngineManager creating instance");
			// create and load
			_instance = new EngineManager();
	    	// create manager to load engines and their attributes
	    	EngineManagerXml.instance();
			log.debug("Engines have been loaded!");
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("EngineManager returns instance "+_instance);
		return _instance;
	}

	/**
	 * @return Number of engines in the Roster
	 */
    public int getNumEntries() {
		return _engineHashTable.size();
	}
    
    public void dispose() {
        _engineHashTable.clear();
    }

 
    /**
     * @return requested Engine object or null if none exists
     */
    public Engine getEngineById(String engineId) {
        return (Engine)_engineHashTable.get(engineId);
    }
    
    public Engine getEngineByRoadAndNumber (String engineRoad, String engineNumber){
    	String engineId = Car.createId (engineRoad, engineNumber);
    	return getEngineById (engineId);
    }
 
    /**
     * Finds an exsisting engine or creates a new engine if needed
     * requires engine's road and number
     * @param engineRoad
     * @param engineNumber
     * @return new engine or existing engine
     */
    public Engine newEngine (String engineRoad, String engineNumber){
    	Engine engine = getEngineByRoadAndNumber(engineRoad, engineNumber);
    	if (engine == null){
    		engine = new Engine(engineRoad, engineNumber);
    		Integer oldSize = new Integer(_engineHashTable.size());
    		_engineHashTable.put(engine.getId(), engine);
    		firePropertyChange(LISTLENGTH, oldSize, new Integer(_engineHashTable.size()));
    	}
    	return engine;
    }
    
    /**
     * Load a engine.
 	 */
    public void register(Engine engine) {
    	Integer oldSize = new Integer(_engineHashTable.size());
        _engineHashTable.put(engine.getId(), engine);
        firePropertyChange(LISTLENGTH, oldSize, new Integer(_engineHashTable.size()));
    }

    /**
     * Unload a engine.
     */
    public void deregister(Engine engine) {
        engine.setLocation(null, null);
        engine.setDestination(null, null);
        Integer oldSize = new Integer(_engineHashTable.size());
    	_engineHashTable.remove(engine.getId());
        firePropertyChange(LISTLENGTH, oldSize, new Integer(_engineHashTable.size()));
    }
    
    public Consist newConsist(String name){
    	Consist consist = getConsistByName(name);
    	if (consist == null){
    		consist = new Consist(name);
    		Integer oldSize = new Integer(_consistHashTable.size());
    		_consistHashTable.put(name, consist);
    		firePropertyChange(CONSISTLISTLENGTH, oldSize, new Integer(_consistHashTable.size()));
    	}
    	return consist;
    }
    
    public void deleteConsist(String name){
    	Consist consist = getConsistByName(name);
    	if (consist != null){
    		consist.dispose();
    		Integer oldSize = new Integer(_consistHashTable.size());
    		_consistHashTable.remove(name);
    		firePropertyChange(CONSISTLISTLENGTH, oldSize, new Integer(_consistHashTable.size()));
    	}
    }
    
    public Consist getConsistByName(String name){
    	Consist consist = (Consist)_consistHashTable.get(name);
    	return consist;
    }
    
    public JComboBox getConsistComboBox(){
    	JComboBox box = new JComboBox();
    	box.addItem("");
       	List consistNames = getConsistNameList();
    	for (int i=0; i<consistNames.size(); i++) {
       		box.addItem((String)consistNames.get(i));
    	}
    	return box;
    }
    
    public void updateConsistComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
    	List consistNames = getConsistNameList();
    	for (int i=0; i<consistNames.size(); i++) {
       		box.addItem((String)consistNames.get(i));
    	}
    }
    
    public List getConsistNameList(){
    	String[] arr = new String[_consistHashTable.size()];
    	List out = new ArrayList();
       	Enumeration en = _consistHashTable.keys();
       	int i=0;
    	while (en.hasMoreElements()) {
            arr[i] = (String)en.nextElement();
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
    public List getEnginesByRoadNameList() {
        String[] arr = new String[_engineHashTable.size()];
        List out = new ArrayList();
        Enumeration en = _engineHashTable.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = (String)en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    /**
     * Sort by engine number, number can alpha numeric
     * @return list of engine ids ordered by number
     */
    public List getEnginesByNumberList() {
    	// first get by road list
    	List sortByRoad = getEnginesByRoadNameList();
    	// now re-sort
    	List out = new ArrayList();
    	int engineNumber = 0;
    	boolean engineAdded = false;
    	Engine engine;
    	
    	for (int i=0; i<sortByRoad.size(); i++){
    		engineAdded = false;
    		engine = getEngineById ((String)sortByRoad.get(i));
    		try{
    			engineNumber = Integer.parseInt (engine.getNumber());
    		}catch (NumberFormatException e) {
 //   			log.debug("Road number isn't a number");
    		}
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById ((String)out.get(j));
        		try{
        			int outEngineNumber = Integer.parseInt (engine.getNumber());
        			if (engineNumber < outEngineNumber){
        				out.add(j, sortByRoad.get(i));
        				engineAdded = true;
        				break;
        			}
        		}catch (NumberFormatException e) {
 //       			log.debug("list out road number isn't a number");
        		}
    		}
    		if (!engineAdded){
    			out.add( sortByRoad.get(i));
    		}
    	}
        return out;
    }
    
    /**
     * Sort by engine type
     * @return list of engine ids ordered by engine type
     */
    public List getEnginesByTypeList() {
    	// first get by road list
    	List sortByRoad = getEnginesByRoadNameList();

    	// now re-sort
    	List out = new ArrayList();
    	String engineModel = "";
    	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortByRoad.size(); i++){
    		engineAdded = false;
    		engine = getEngineById ((String)sortByRoad.get(i));
    		engineModel = engine.getModel();
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById ((String)out.get(j));
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
    public List getEnginesByConsistList() {
    	// first get by road list
    	List sortByRoad = getEnginesByRoadNameList();

    	// now re-sort
    	List out = new ArrayList();
    	String engineConsistName = "";
    	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortByRoad.size(); i++){
    		engineAdded = false;
    		engine = getEngineById ((String)sortByRoad.get(i));
    		engineConsistName = engine.getConsistName();
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById ((String)out.get(j));
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
    public List getEnginesByLocationList() {
    	// first get by road list
    	List sortByRoad = getEnginesByRoadNameList();

    	// now re-sort
    	List out = new ArrayList();
    	String engineLocation = "";
    	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortByRoad.size(); i++){
    		engineAdded = false;
    		engine = getEngineById ((String)sortByRoad.get(i));
    		engineLocation = engine.getLocationName()+engine.getSecondaryLocationName();
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById ((String)out.get(j));
    			String outEngineLocation = engine.getLocationName()+engine.getSecondaryLocationName();
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
    public List getEnginesByDestinationList() {
    	// first get by location list
    	List sortByLocation = getEnginesByLocationList();

    	// now re-sort
    	List out = new ArrayList();
    	String engineDestination = "";
    	boolean engineAdded = false;
    	Engine engine;

    	for (int i = 0; i < sortByLocation.size(); i++) {
			engineAdded = false;
			engine = getEngineById((String) sortByLocation.get(i));
			engineDestination = engine.getDestinationName()+engine.getSecondaryDestinationName();
			for (int j = 0; j < out.size(); j++) {
				engine = getEngineById((String) out.get(j));
				String outEngineDestination = engine.getDestinationName()+engine.getSecondaryDestinationName();
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
    public List getEnginesByTrainList() {
    	// first get by road list
    	List sortByRoad = getEnginesByLocationList();

    	// now re-sort
    	List out = new ArrayList();
     	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortByRoad.size(); i++){
    		engineAdded = false;
    		engine = getEngineById ((String)sortByRoad.get(i));
    		String engineTrainName = "";
    		if(engine.getTrain() != null)
    			engineTrainName = engine.getTrain().getName();
    		for (int j=0; j<out.size(); j++ ){
    			engine = getEngineById ((String)out.get(j));
    			String outEngineTrainName = "";
    			if(engine.getTrain() != null)
    				outEngineTrainName = engine.getTrain().getName();
    			if (engineTrainName.compareToIgnoreCase(outEngineTrainName)<0){
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
     * Sort by engines moves
     * @return list of engine ids ordered by engine moves
     */
    public List getEnginesByMovesList() {
    	// first get by road list
    	List sortByRoad = getEnginesByRoadNameList();

    	// now re-sort
    	List out = new ArrayList();
     	boolean engineAdded = false;
    	Engine engine;

    	for (int i=0; i<sortByRoad.size(); i++){
    		engineAdded = false;
    		engine = getEngineById ((String)sortByRoad.get(i));
				int inMoves = engine.getMoves();
				for (int j = 0; j < out.size(); j++) {
					engine = getEngineById((String) out.get(j));
					int outMoves = engine.getMoves();
					if (inMoves < outMoves) {
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
	 * return a list available engines (no assigned train) on a route, engines are
	 * ordered least recently moved to most recently moved.
	 * 
	 * @param train
	 * @return Ordered list of engines not assigned to a train
	 */
    public List getEnginesAvailableTrainList(Train train) {
    	Route route = train.getRoute();
    	// get a list of locations served by this route
    	List routeList = route.getLocationsBySequenceList();
    	// don't include engines at route destination
    	RouteLocation destination = null;
    	if (routeList.size()>1){
    		destination = route.getLocationById((String)routeList.get(routeList.size()-1));
    		// However, if the destination is visited at least once, must include all engines
    		RouteLocation test;
    		for (int i=0; i<routeList.size()-1; i++){
    			test = route.getLocationById((String)routeList.get(i));
    			if (destination.getName().equals(test.getName())){
    				destination = null;
    				break;
    			}
    		}
    	}
    	// get engines by number list
    	List enginesSortByNum = getEnginesByNumberList();
    	// now build list of available engines for this route
    	List out = new ArrayList();
    	boolean engineAdded = false;
    	Engine engine;
 
    	for (int i = 0; i < enginesSortByNum.size(); i++) {
    		engineAdded = false;
    		engine = getEngineById((String) enginesSortByNum.get(i));
    		RouteLocation rl = route.getLocationByName(engine.getLocationName());
    		// get engines that don't have an assigned train, or the assigned train is this one 
    		if (rl != null && rl != destination && (engine.getTrain() == null || train.equals(engine.getTrain()))){
    			// sort by engine moves
    			int inMoves = engine.getMoves();
    			for (int j = 0; j < out.size(); j++) {
    				engine = getEngineById((String) out.get(j));
    				int outMoves = engine.getMoves();
    				if (inMoves < outMoves) {
    					out.add(j, enginesSortByNum.get(i));
    					engineAdded = true;
    					break;
    				}
    			}
    			if (!engineAdded) {
    				out.add(enginesSortByNum.get(i));
    			}
    		}
    	}
    	return out;
    }
    
    /**
	 * return a list of engines assigned to a train sorted by destination.
	 * Caboose or engine with FRED will be the last engine in the list 
	 * 
	 * @param train
	 * @return Ordered list of assigned engines
	 */
    public List getEnginesByTrainList(Train train) {
    	// get engines available list
    	List available = getEnginesAvailableTrainList(train);
    	List inTrain = new ArrayList();
    	Engine engine;

    	for (int i = 0; i < available.size(); i++) {
    		engine = getEngineById((String) available.get(i));
    		// get only engines that are assigned to this train
    		if(engine.getTrain() == train)
    			inTrain.add(available.get(i));
    	}
    	// now sort by secondary destination
    	List out = new ArrayList();
    	boolean engineAdded;
    	boolean lastEngineAdded = false;	// true if caboose or engine with FRED added to train 
    	for (int i = 0; i < inTrain.size(); i++) {
    		engineAdded = false;
    		engine = getEngineById((String) inTrain.get(i));
    		String engineDestination = engine.getSecondaryDestinationName();
    		for (int j = 0; j < out.size(); j++) {
    			Engine engineOut = getEngineById ((String)out.get(j));
    			String engineOutDest = engineOut.getSecondaryDestinationName();
    			if (engineDestination.compareToIgnoreCase(engineOutDest)<0 && !engine.isCaboose() && !engine.hasFred()){
    				out.add(j, inTrain.get(i));
    				engineAdded = true;
    				break;
    			}
    		}
    		if (!engineAdded){
    			if (lastEngineAdded)
    				out.add(out.size()-1,inTrain.get(i));
    			else
    				out.add(inTrain.get(i));
    			if (engine.isCaboose()||engine.hasFred()){
    				lastEngineAdded = true;
    			}
    		}
    	}
    	return out;
    }

    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of user name changes to individual NamedBeans.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	log.debug("EngineManager sees property change: " + e.getPropertyName() + " old: " + e.getOldValue() + " new " + e.getNewValue());
    }

   
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EngineManager.class.getName());

}

