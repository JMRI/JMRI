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
 * @version	$Revision: 1.10 $
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
        _carHashTable.clear();
    }

 
    /**
     * Get Car by id
     * @return requested Car object or null if none exists
     */
    public Car getCarById(String carId) {
        return (Car)_carHashTable.get(carId);
    }
    
    /**
     * Get Car by road and number
     * @param carRoad car road
     * @param carNumber car number
     * @return requested Car object or null if none exists
     */
    
    public Car getCarByRoadAndNumber (String carRoad, String carNumber){
    	String carId = Car.createId (carRoad, carNumber);
    	return getCarById (carId);
    }
 
    /**
     * Finds an exsisting Car or creates a new Car if needed
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
    	car.setKernel(null);
    	car.setDestination(null, null);
        car.setLocation(null, null);
        Integer oldSize = new Integer(_carHashTable.size());
    	_carHashTable.remove(car.getId());
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
    	Kernel kernel = (Kernel)_kernelHashTable.get(name);
    	return kernel;
    }
    
    public JComboBox getKernelComboBox(){
    	JComboBox box = new JComboBox();
    	box.addItem("");
      	List kernelNames = getKernelNameList();
    	for (int i=0; i<kernelNames.size(); i++) {
       		box.addItem((String)kernelNames.get(i));
    	}
    	return box;
    }
    
    public void updateKernelComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
      	List kernelNames = getKernelNameList();
    	for (int i=0; i<kernelNames.size(); i++) {
       		box.addItem((String)kernelNames.get(i));
    	}
    }
    
    /**
     * Get a list of kernel names
     * @return ordered list of kernel names
     */
    public List<String> getKernelNameList(){
    	String[] arr = new String[_kernelHashTable.size()];
    	List<String> out = new ArrayList<String>();
       	Enumeration en = _kernelHashTable.keys();
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
     * Sort by car id
     * @return list of car ids ordered by id
     */
    public List<String> getCarsByIdList() {
        String[] arr = new String[_carHashTable.size()];
        List<String> out = new ArrayList<String>();
        Enumeration en = _carHashTable.keys();
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
     * Sort by car road name
     * @return list of car ids ordered by road name
     */
    public List<String> getCarsByRoadNameList() {
      	// first get by id list
    	List sortById = getCarsByIdList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carRoad = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortById.size(); i++){
    		carAdded = false;
    		c = getCarById ((String)sortById.get(i));
    		carRoad = c.getRoad();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById ((String)out.get(j));
    			String outCarRoad = c.getRoad();
    			if (carRoad.compareToIgnoreCase(outCarRoad)<0){
    				out.add(j, (String)sortById.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add((String)sortById.get(i));
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
    	List sortByRoad = getCarsByRoadNameList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	int carNumber = 0;
    	boolean carAdded = false;
    	Car c;
    	
    	for (int i=0; i<sortByRoad.size(); i++){
    		carAdded = false;
    		c = getCarById ((String)sortByRoad.get(i));
    		try{
    			carNumber = Integer.parseInt (c.getNumber());
    		}catch (NumberFormatException e) {
 //   			log.debug("Road number isn't a number");
    		}
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById ((String)out.get(j));
        		try{
        			int outCarNumber = Integer.parseInt (c.getNumber());
        			if (carNumber < outCarNumber){
        				out.add(j, (String)sortByRoad.get(i));
        				carAdded = true;
        				break;
        			}
        		}catch (NumberFormatException e) {
 //       			log.debug("list out road number isn't a number");
        		}
    		}
    		if (!carAdded){
    			out.add((String) sortByRoad.get(i));
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
    	List sortByNumber = getCarsByNumberList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carType = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortByNumber.size(); i++){
    		carAdded = false;
    		c = getCarById ((String)sortByNumber.get(i));
    		carType = c.getType();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById ((String)out.get(j));
    			String outCarType = c.getType();
    			if (carType.compareToIgnoreCase(outCarType)<0){
    				out.add(j, (String)sortByNumber.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add((String)sortByNumber.get(i));
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
    	List sortByNumber = getCarsByNumberList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carKernelName = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortByNumber.size(); i++){
    		carAdded = false;
    		c = getCarById ((String)sortByNumber.get(i));
    		carKernelName = c.getKernelName();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById ((String)out.get(j));
    			String outCarKernelName = c.getKernelName();
    			if (carKernelName.compareToIgnoreCase(outCarKernelName)<0){
    				out.add(j, (String)sortByNumber.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add((String)sortByNumber.get(i));
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
    		c = getCarById ((String)sortByNumber.get(i));
    		carLocation = c.getLocationName()+c.getTrackName();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById ((String)out.get(j));
    			String outCarLocation = c.getLocationName()+c.getTrackName();
    			if (carLocation.compareToIgnoreCase(outCarLocation)<0){
    				out.add(j, (String)sortByNumber.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add((String)sortByNumber.get(i));
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
    	List sortByLocation = getCarsByLocationList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carDestination = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i = 0; i < sortByLocation.size(); i++) {
			carAdded = false;
			c = getCarById((String) sortByLocation.get(i));
			carDestination = c.getDestinationName()+c.getDestinationTrackName();
			for (int j = 0; j < out.size(); j++) {
				c = getCarById((String) out.get(j));
				String outCarDestination = c.getDestinationName()+c.getDestinationTrackName();
				if (carDestination.compareToIgnoreCase(outCarDestination) < 0 ) {
					out.add(j, (String)sortByLocation.get(i));
					carAdded = true;
					break;
				}
			}
			if (!carAdded) {
				out.add((String)sortByLocation.get(i));
    		}
    	}
    	return out;
    }
    
    /**
     * Sort by cars in trains
     * @return list of car ids ordered by trains
     */
    public List getCarsByTrainList() {
    	// first get by location list
    	List sortByLocation = getCarsByLocationList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
     	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortByLocation.size(); i++){
    		carAdded = false;
    		c = getCarById ((String)sortByLocation.get(i));
    		String carTrainName = "";
    		if(c.getTrain() != null)
    			carTrainName = c.getTrain().getName();
    		for (int j=0; j<out.size(); j++ ){
    			c = getCarById ((String)out.get(j));
    			String outCarTrainName = "";
    			if(c.getTrain() != null)
    				outCarTrainName = c.getTrain().getName();
    			if (carTrainName.compareToIgnoreCase(outCarTrainName)<0){
    				out.add(j, (String)sortByLocation.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add((String)sortByLocation.get(i));
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
    		c = getCarById ((String)sortByRoad.get(i));
				int inMoves = c.getMoves();
				for (int j = 0; j < out.size(); j++) {
					c = getCarById((String) out.get(j));
					int outMoves = c.getMoves();
					if (inMoves < outMoves) {
						out.add(j, (String)sortByRoad.get(i));
						carAdded = true;
						break;
					}
				}
     		if (!carAdded){
    			out.add((String)sortByRoad.get(i));
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
	 * @return List of cars with no assigned train on a route
	 */
    public List getCarsAvailableTrainList(Train train) {
    	List<String> out = new ArrayList<String>();
    	Route route = train.getRoute();
    	if (route == null)
    		return out;
    	// get a list of locations served by this route
    	List routeList = route.getLocationsBySequenceList();
    	// don't include cars at route destination
    	RouteLocation destination = null;
    	if (routeList.size()>1){
    		destination = route.getLocationById((String)routeList.get(routeList.size()-1));
    		// However, if the destination is visited more than once, must include all cars
    		RouteLocation test;
    		for (int i=0; i<routeList.size()-1; i++){
    			test = route.getLocationById((String)routeList.get(i));
    			if (destination.getName().equals(test.getName())){
    				destination = null;
    				break;
    			}
    		}
    	}
    	// get cars by moves list
    	List carsSortByMoves = getCarsByMovesList();
    	// now build list of available cars for this route
     	Car car;
     	for (int i = 0; i < carsSortByMoves.size(); i++) {
    		car = getCarById((String) carsSortByMoves.get(i));
    		RouteLocation rl = route.getLocationByName(car.getLocationName());
    		// get cars that don't have an assigned train, or the assigned train is this one 
    		if (rl != null && rl != destination && (car.getTrain() == null || train.equals(car.getTrain()))){
    			out.add((String)carsSortByMoves.get(i));
    		}
    	}
    	return out;
    }
    
    /**
	 * Get a list of Cars assigned to a train sorted by destination.
	 * Caboose or car with FRED will be the last car in the list 
	 * 
	 * @param train
	 * @return Ordered list of Cars assigned to the train
	 */
    public List getCarsByTrainDestinationList(Train train) {
     	List inTrain = getCarsByTrainList(train);
    	Car car;

     	// now sort by track destination
    	List<String> out = new ArrayList<String>();
    	boolean carAdded;
    	boolean lastCarAdded = false;	// true if caboose or car with FRED added to train 
    	for (int i = 0; i < inTrain.size(); i++) {
    		carAdded = false;
    		car = getCarById((String) inTrain.get(i));
    		String carDestination = car.getDestinationTrackName();
    		for (int j = 0; j < out.size(); j++) {
    			Car carOut = getCarById ((String)out.get(j));
    			String carOutDest = carOut.getDestinationTrackName();
    			if (carDestination.compareToIgnoreCase(carOutDest)<0 && !car.isCaboose() && !car.hasFred()){
    				out.add(j, (String)inTrain.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			if (lastCarAdded)
    				out.add(out.size()-1,(String)inTrain.get(i));
    			else
    				out.add((String)inTrain.get(i));
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
	 * @return List of Cars assigned to the train
	 */
    public List<String> getCarsByTrainList(Train train) {
    	// get cars available list
    	List<String> byId = getCarsByIdList();
    	List<String> inTrain = new ArrayList<String>();
    	Car car;

    	for (int i = 0; i < byId.size(); i++) {
    		car = getCarById((String) byId.get(i));
    		// get only cars that are assigned to this train
    		if(car.getTrain() == train)
    			inTrain.add((String)byId.get(i));
    	}
    	return inTrain;
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CarManager.class.getName());

}

