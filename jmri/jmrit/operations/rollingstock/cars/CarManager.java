// CarManager.java

package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;

import jmri.jmrit.operations.trains.Train;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;


/**
 * Manages the cars.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.21 $
 */
public class CarManager implements java.beans.PropertyChangeListener {
	
	LocationManager locationManager = LocationManager.instance();
	protected Hashtable<String, Car> _carHashTable = new Hashtable<String, Car>();   		// stores Cars by id
	protected Hashtable<String, Kernel> _kernelHashTable = new Hashtable<String, Kernel>(); // stores Kernels by number

	public static final String LISTLENGTH_CHANGED_PROPERTY = "CarListLength";
	public static final String KERNELLISTLENGTH_CHANGED_PROPERTY = "KernelListLength";

    public CarManager() {
    }
    
	/** record the single instance **/
	private static CarManager _instance = null;

	public static synchronized CarManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("CarManager creating instance");
			// create and load
			_instance = new CarManager();
			OperationsXml.instance();					// load setup
	    	// create manager to load cars and their attributes
	    	CarManagerXml.instance();
			log.debug("Cars have been loaded!");
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("CarManager returns instance "+_instance);
		return _instance;
	}

	/**
	 * Get the number of cars in the roster
	 * @return Number of cars in the Roster
	 */
    public int getNumEntries() {
		return _carHashTable.size();
	}
    
    public void dispose() {
    	deleteAll();
        //_carHashTable.clear();
    }

 
    /**
     * Get Car by id
     * @return requested Car object or null if none exists
     */
    public Car getCarById(String carId) {
        return _carHashTable.get(carId);
    }
    
    /**
     * Get Car by road and number
     * @param carRoad car road
     * @param carNumber car number
     * @return requested Car object or null if none exists
     */
    public Car getCarByRoadAndNumber(String carRoad, String carNumber){
    	String carId = Car.createId (carRoad, carNumber);
    	return getCarById (carId);
    }
    
    /**
     * Get a car by type and road. Used to test that a car with a specific
     * type and road exists. 
     * @param carType car type.
     * @param carRoad car road.
     * @return the first car found with the specified type and road.
     */
    public Car getCarByTypeAndRoad(String carType, String carRoad){
    	Enumeration<String> en = _carHashTable.keys();
    	while (en.hasMoreElements()) { 
    		Car car = getCarById(en.nextElement());
    		if(car.getType().equals(carType) && car.getRoad().equals(carRoad))
    			return car;
    	}
    	return null;
    }
    
    /**
     * Get a car by Radio Frequency Identification (RFID). 
     * @param rfid car's RFID.
     * @return the car with the specific RFID, or null if not found.
     */
    public Car getCarByRfid(String rfid){
    	Enumeration<String> en = _carHashTable.keys();
    	while (en.hasMoreElements()) { 
    		Car car = getCarById(en.nextElement());
    		if(car.getRfid().equals(rfid))
    			return car;
    	}
    	return null;
    }
 
    /**
     * Finds an existing Car or creates a new Car if needed
     * requires car's road and number
     * @param carRoad car road
     * @param carNumber car number
     * @return new car or existing Car
     */
    public Car newCar (String carRoad, String carNumber){
    	Car car = getCarByRoadAndNumber(carRoad, carNumber);
    	if (car == null){
    		car = new Car(carRoad, carNumber);
    		Integer oldSize = new Integer(_carHashTable.size());
    		_carHashTable.put(car.getId(), car);
    		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_carHashTable.size()));
    	}
    	return car;
    }
    
    /**
     * Load a Car.
 	 */
    public void register(Car car) {
    	Integer oldSize = new Integer(_carHashTable.size());
        _carHashTable.put(car.getId(), car);
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_carHashTable.size()));
    }

    /**
     * Unload a Car.
     */
    public void deregister(Car car) {
    	car.dispose();
        Integer oldSize = new Integer(_carHashTable.size());
    	_carHashTable.remove(car.getId());
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_carHashTable.size()));
    }
    
    /**
     * Remove all cars from roster
     */
    public void deleteAll(){
    	Integer oldSize = new Integer(_carHashTable.size());
    	Enumeration<String> en = _carHashTable.keys();
    	while (en.hasMoreElements()) { 
    		Car car = getCarById(en.nextElement());
    		car.dispose();
            _carHashTable.remove(car.getId());
    	}
    	firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_carHashTable.size()));
    }
    
    /**
     * Create a new Kernel
     * @param name 
     * @return Kernel
     */
    public Kernel newKernel(String name){
    	Kernel kernel = getKernelByName(name);
    	if (kernel == null){
    		kernel = new Kernel(name);
    		Integer oldSize = new Integer(_kernelHashTable.size());
    		_kernelHashTable.put(name, kernel);
    		firePropertyChange(KERNELLISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_kernelHashTable.size()));
    	}
    	return kernel;
    }
    
    /**
     * Delete a Kernel by name
     * @param name
     */
    public void deleteKernel(String name){
    	Kernel kernel = getKernelByName(name);
    	if (kernel != null){
    		kernel.dispose();
    		Integer oldSize = new Integer(_kernelHashTable.size());
    		_kernelHashTable.remove(name);
    		firePropertyChange(KERNELLISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_kernelHashTable.size()));
    	}
    }
    
   /**
    * Get a Kernel by name
    * @param name
    * @return named Kernel
    */
    public Kernel getKernelByName(String name){
    	Kernel kernel = _kernelHashTable.get(name);
    	return kernel;
    }
    
    public JComboBox getKernelComboBox(){
    	JComboBox box = new JComboBox();
    	box.addItem("");
      	List<String> kernelNames = getKernelNameList();
    	for (int i=0; i<kernelNames.size(); i++) {
       		box.addItem(kernelNames.get(i));
    	}
    	return box;
    }
    
    public void updateKernelComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
      	List<String> kernelNames = getKernelNameList();
    	for (int i=0; i<kernelNames.size(); i++) {
       		box.addItem(kernelNames.get(i));
    	}
    }
    
    /**
     * Get a list of kernel names
     * @return ordered list of kernel names
     */
    public List<String> getKernelNameList(){
    	String[] arr = new String[_kernelHashTable.size()];
    	List<String> out = new ArrayList<String>();
       	Enumeration<String> en = _kernelHashTable.keys();
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
     * Sort by car id
     * @return list of car ids ordered by id
     */
    public List<String> getCarsByIdList() {
        String[] arr = new String[_carHashTable.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _carHashTable.keys();
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
     * Sort by car road name
     * @return list of car ids ordered by road name
     */
    public List<String> getCarsByRoadNameList() {
      	// first get by id list
    	List<String> sortById = getCarsByIdList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carRoad = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortById.size(); i++){
    		carAdded = false;
    		c = getCarById (sortById.get(i));
    		carRoad = c.getRoad();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
    			String outCarRoad = c.getRoad();
    			if (carRoad.compareToIgnoreCase(outCarRoad)<0){
    				out.add(j, sortById.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortById.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by car number, number can alpha numeric
     * @return list of car ids ordered by number
     */
    public List<String> getCarsByNumberList() {
    	// first get by road list
    	List<String> sortByRoad = getCarsByRoadNameList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	int carNumber = 0;
    	String[] number;
    	boolean carAdded = false;
    	Car c;
    	
    	for (int i=0; i<sortByRoad.size(); i++){
    		carAdded = false;
    		c = getCarById (sortByRoad.get(i));
    		try{
    			number = c.getNumber().split("-");
    			carNumber = Integer.parseInt(number[0]);
    		}catch (NumberFormatException e) {
 //   			log.debug("Road number isn't a number");
    		}
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
        		try{
        			number = c.getNumber().split("-");
        			int outCarNumber = Integer.parseInt(number[0]);
        			if (carNumber < outCarNumber){
        				out.add(j, sortByRoad.get(i));
        				carAdded = true;
        				break;
        			}
        		}catch (NumberFormatException e) {
 //       			log.debug("list out road number isn't a number");
        		}
    		}
    		if (!carAdded){
    			out.add(sortByRoad.get(i));
    		}
    	}
        return out;
    }
    
    /**
     * Sort by car type names
     * @return list of car ids ordered by car type
     */
    public List<String> getCarsByTypeList() {
    	// first get by number list
    	List<String> sortByNumber = getCarsByNumberList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carType = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortByNumber.size(); i++){
    		carAdded = false;
    		c = getCarById (sortByNumber.get(i));
    		carType = c.getType();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
    			String outCarType = c.getType();
    			if (carType.compareToIgnoreCase(outCarType)<0){
    				out.add(j, sortByNumber.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortByNumber.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by car color names
     * @return list of car ids ordered by car color
     */
    public List<String> getCarsByColorList() {
    	// first get by type list
    	List<String> sortByType = getCarsByTypeList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carColor = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortByType.size(); i++){
    		carAdded = false;
    		c = getCarById (sortByType.get(i));
    		carColor = c.getColor();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
    			String outCarColor = c.getColor();
    			if (carColor.compareToIgnoreCase(outCarColor)<0){
    				out.add(j, sortByType.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortByType.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by car kernel names
     * @return list of car ids ordered by car kernel
     */
    public List<String> getCarsByKernelList() {
    	// first get by number list
    	List<String> sortByNumber = getCarsByNumberList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carKernelName = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortByNumber.size(); i++){
    		carAdded = false;
    		c = getCarById (sortByNumber.get(i));
    		carKernelName = c.getKernelName();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
    			String outCarKernelName = c.getKernelName();
    			if (carKernelName.compareToIgnoreCase(outCarKernelName)<0){
    				out.add(j, sortByNumber.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortByNumber.get(i));
    		}
    	}
    	return out;
    }

    
    /**
     * Sort by car location
     * @return list of car ids ordered by car location
     */
    public List<String> getCarsByLocationList() {
    	// first get by number list
    	List<String> sortByNumber = getCarsByNumberList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carLocation = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortByNumber.size(); i++){
    		carAdded = false;
    		c = getCarById (sortByNumber.get(i));
    		carLocation = c.getLocationName()+c.getTrackName();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
    			String outCarLocation = c.getLocationName()+c.getTrackName();
    			if (carLocation.compareToIgnoreCase(outCarLocation)<0){
    				out.add(j, sortByNumber.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortByNumber.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by car destination
     * @return list of car ids ordered by car destination
     */
    public List<String> getCarsByDestinationList() {
    	// first get by location list
    	List<String> sortByLocation = getCarsByLocationList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carDestination = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i = 0; i < sortByLocation.size(); i++) {
			carAdded = false;
			c = getCarById(sortByLocation.get(i));
			carDestination = c.getDestinationName()+c.getDestinationTrackName();
			for (int j = 0; j < out.size(); j++) {
				c = getCarById(out.get(j));
				String outCarDestination = c.getDestinationName()+c.getDestinationTrackName();
				if (carDestination.compareToIgnoreCase(outCarDestination) < 0 ) {
					out.add(j, sortByLocation.get(i));
					carAdded = true;
					break;
				}
			}
			if (!carAdded) {
				out.add(sortByLocation.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by cars in trains
     * @return list of car ids ordered by trains
     */
    public List<String> getCarsByTrainList() {
    	// first get by location list
    	List<String> sortByLocation = getCarsByLocationList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
     	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortByLocation.size(); i++){
    		carAdded = false;
    		c = getCarById (sortByLocation.get(i));
    		String carTrainName = "";
    		if(c.getTrain() != null)
    			carTrainName = c.getTrain().getName();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
    			String outCarTrainName = "";
    			if(c.getTrain() != null)
    				outCarTrainName = c.getTrain().getName();
    			if (carTrainName.compareToIgnoreCase(outCarTrainName)<0){
    				out.add(j, sortByLocation.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortByLocation.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by car loads
     * @return list of car ids ordered by car loads
     */
    public List<String> getCarsByLoadList() {
    	// first get by location list
    	List<String> sortByLocation = getCarsByLocationList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
     	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortByLocation.size(); i++){
    		carAdded = false;
    		c = getCarById (sortByLocation.get(i));
    		String carLoadName = "";
    		if(c.getLoad() != null)
    			carLoadName = c.getLoad();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
    			String outCarLoadName = "";
    			if(c.getLoad() != null)
    				outCarLoadName = c.getLoad();
    			if (carLoadName.compareToIgnoreCase(outCarLoadName)<0){
    				out.add(j, sortByLocation.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortByLocation.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by car moves
     * @return list of car ids ordered by car moves
     */
    public List<String> getCarsByMovesList() {
    	// first get by road list
    	List<String> sortByRoad = getCarsByRoadNameList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
     	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortByRoad.size(); i++){
    		carAdded = false;
    		c = getCarById (sortByRoad.get(i));
				int inMoves = c.getMoves();
				for (int j = 0; j < out.size(); j++) {
					c = getCarById(out.get(j));
					int outMoves = c.getMoves();
					if (inMoves < outMoves) {
						out.add(j, sortByRoad.get(i));
						carAdded = true;
						break;
					}
				}
     		if (!carAdded){
    			out.add(sortByRoad.get(i));
    		}
    	}
    	return out;
    }

    /**
     * Sort by when car was built
     * @return list of car ids ordered by car built date
     */
    public List<String> getCarsByBuiltList() {
    	// first get by type list
    	List<String> sortById = getCarsByIdList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carBuilt = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortById.size(); i++){
    		carAdded = false;
    		c = getCarById (sortById.get(i));
    		carBuilt = c.getBuilt();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
    			String outCarBuilt = c.getBuilt();
    			if (carBuilt.compareToIgnoreCase(outCarBuilt)<0){
    				out.add(j, sortById.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortById.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by car owner
     * @return list of car ids ordered by car owner
     */
    public List<String> getCarsByOwnerList() {
    	// first get by type list
    	List<String> sortById = getCarsByIdList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carOwner = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortById.size(); i++){
    		carAdded = false;
    		c = getCarById (sortById.get(i));
    		carOwner = c.getOwner();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
    			String outCarOwner = c.getOwner();
    			if (carOwner.compareToIgnoreCase(outCarOwner)<0){
    				out.add(j, sortById.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortById.get(i));
    		}
    	}
    	return out;
    }
   
    /**
     * Sort by car RFID
     * @return list of car ids ordered by RFIDs
     */
    public List<String> getCarsByRfidList() {
      	// first get by id list
    	List<String> sortById = getCarsByIdList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carRfid = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortById.size(); i++){
    		carAdded = false;
    		c = getCarById (sortById.get(i));
    		carRfid = c.getRfid();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
    			String outCarRfid = c.getRfid();
    			if (carRfid.compareToIgnoreCase(outCarRfid)<0){
    				out.add(j, sortById.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortById.get(i));
    		}
    	}
    	return out;
    }
    
    /**
	 * Return a list available cars (no assigned train or cars already assigned
	 * to this train) on a route, cars are ordered least recently moved to most
	 * recently moved.
	 * 
	 * @param train
	 * @return List of car ids with no assigned train on a route
	 */
    public List<String> getCarsAvailableTrainList(Train train) {
    	List<String> out = new ArrayList<String>();
    	Route route = train.getRoute();
    	if (route == null)
    		return out;
    	// get a list of locations served by this route
    	List<String> routeList = route.getLocationsBySequenceList();
    	// don't include cars at route destination
    	RouteLocation destination = null;
    	if (routeList.size()>1){
    		destination = route.getLocationById(routeList.get(routeList.size()-1));
    		// However, if the destination is visited more than once, must include all cars
    		RouteLocation test;
    		for (int i=0; i<routeList.size()-1; i++){
    			test = route.getLocationById(routeList.get(i));
    			if (destination.getName().equals(test.getName())){
    				destination = null;
    				break;
    			}
    		}
    	}
    	// get cars by moves list
    	List<String> carsSortByMoves = getCarsByMovesList();
    	// now build list of available cars for this route
     	Car car;
     	for (int i = 0; i < carsSortByMoves.size(); i++) {
    		car = getCarById(carsSortByMoves.get(i));
    		RouteLocation rl = route.getLastLocationByName(car.getLocationName());
    		// get cars that don't have an assigned train, or the assigned train is this one 
    		if (rl != null && rl != destination && (car.getTrain() == null || train.equals(car.getTrain()))){
    			out.add(carsSortByMoves.get(i));
    		}
    	}
    	return out;
    }
    
    /**
	 * Get a list of Cars assigned to a train sorted by destination.
	 * Caboose or car with FRED will be the last car in the list 
	 * 
	 * @param train
	 * @return Ordered list of Car ids assigned to the train
	 */
    public List<String> getCarsByTrainDestinationList(Train train) {
     	List<String> inTrain = getCarsByTrainList(train);
    	Car car;

     	// now sort by track destination
    	List<String> out = new ArrayList<String>();
    	boolean carAdded;
    	boolean lastCarAdded = false;	// true if caboose or car with FRED added to train 
    	for (int i = 0; i < inTrain.size(); i++) {
    		carAdded = false;
    		car = getCarById(inTrain.get(i));
    		String carDestination = car.getDestinationTrackName();
    		for (int j = 0; j < out.size(); j++) {
    			Car carOut = getCarById (out.get(j));
    			String carOutDest = carOut.getDestinationTrackName();
    			if (carDestination.compareToIgnoreCase(carOutDest)<0 && !car.isCaboose() && !car.hasFred()){
    				out.add(j, inTrain.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			if (lastCarAdded)
    				out.add(out.size()-1, inTrain.get(i));
    			else
    				out.add(inTrain.get(i));
    			if (car.isCaboose()||car.hasFred()){
    				lastCarAdded = true;
    			}
    		}
    	}
    	return out;
    }
    
    /**
	 * Get a list of Cars assigned to a train 
	 * 
	 * @param train
	 * @return List of car ids assigned to the train
	 */
    public List<String> getCarsByTrainList(Train train) {
    	// get cars available list
    	List<String> byId = getCarsByIdList();
    	List<String> inTrain = new ArrayList<String>();
    	Car car;

    	for (int i = 0; i < byId.size(); i++) {
    		car = getCarById(byId.get(i));
    		// get only cars that are assigned to this train
    		if(car.getTrain() == train)
    			inTrain.add(byId.get(i));
    	}
    	return inTrain;
    }
    
    /**
     * Get a list of car road names where the car was flagged as a caboose.
     * @return List of caboose road names.
     */
    public List<String> getCabooseRoadNames(){
    	List<String> names = new ArrayList<String>();
       	Enumeration<String> en = _carHashTable.keys();
    	while (en.hasMoreElements()) { 
    		Car car = getCarById(en.nextElement());
    		if (car.isCaboose() && !names.contains(car.getRoad())){
    			names.add(car.getRoad());
    		}
    	}
    	return sortList(names);
    }
    
    /**
     * Get a list of car road names where the car was flagged with FRED.
     * @return List of road names of cars with FREDs.
     */
    public List<String> getFredRoadNames(){
    	List<String> names = new ArrayList<String>();
       	Enumeration<String> en = _carHashTable.keys();
    	while (en.hasMoreElements()) { 
    		Car car = getCarById(en.nextElement());
    		if (car.hasFred() && !names.contains(car.getRoad())){
    			names.add(car.getRoad());
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


    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of user name changes to individual NamedBeans.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	log.debug("CarManager sees property change: " + e.getPropertyName() + " old: " + e.getOldValue() + " new " + e.getNewValue());
    }

   
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarManager.class.getName());

}

