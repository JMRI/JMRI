// CarManager.java

package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;

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
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.2 $
 */
public class CarManager implements java.beans.PropertyChangeListener {
	
	LocationManager locationManager = LocationManager.instance();
	protected Hashtable _carHashTable = new Hashtable();   		// stores Cars by id
	protected Hashtable _kernelHashTable = new Hashtable();   	// stores Kernels by number

	public static final String LISTLENGTH = "CarListLength";
	public static final String KERNELLISTLENGTH = "KernelListLength";

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
	 * @return Number of cars in the Roster
	 */
    public int getNumEntries() {
		return _carHashTable.size();
	}
    
    public void dispose() {
        _carHashTable.clear();
    }

 
    /**
     * @return requested Car object or null if none exists
     */
    public Car getCarById(String carId) {
        return (Car)_carHashTable.get(carId);
    }
    
    public Car getCarByRoadAndNumber (String carRoad, String carNumber){
    	String carId = Car.createId (carRoad, carNumber);
    	return getCarById (carId);
    }
 
    /**
     * Finds an exsisting car or creates a new car if needed
     * requires car's road and number
     * @param carRoad
     * @param carNumber
     * @return new car or existing car
     */
    public Car newCar (String carRoad, String carNumber){
    	Car car = getCarByRoadAndNumber(carRoad, carNumber);
    	if (car == null){
    		car = new Car(carRoad, carNumber);
    		Integer oldSize = new Integer(_carHashTable.size());
    		_carHashTable.put(car.getId(), car);
    		firePropertyChange(LISTLENGTH, oldSize, new Integer(_carHashTable.size()));
    	}
    	return car;
    }
    
    /**
     * Load a car.
 	 */
    public void register(Car car) {
    	Integer oldSize = new Integer(_carHashTable.size());
        _carHashTable.put(car.getId(), car);
        firePropertyChange(LISTLENGTH, oldSize, new Integer(_carHashTable.size()));
    }

    /**
     * Unload a car.
     */
    public void deregister(Car car) {
    	car.setKernel(null);
    	car.setDestination(null, null);
        car.setLocation(null, null);
        Integer oldSize = new Integer(_carHashTable.size());
    	_carHashTable.remove(car.getId());
        firePropertyChange(LISTLENGTH, oldSize, new Integer(_carHashTable.size()));
    }
    
    public Kernel newKernel(String name){
    	Kernel kernel = getKernelByName(name);
    	if (kernel == null){
    		kernel = new Kernel(name);
    		Integer oldSize = new Integer(_kernelHashTable.size());
    		_kernelHashTable.put(name, kernel);
    		firePropertyChange(KERNELLISTLENGTH, oldSize, new Integer(_kernelHashTable.size()));
    	}
    	return kernel;
    }
    
    public void deleteKernel(String name){
    	Kernel kernel = getKernelByName(name);
    	if (kernel != null){
    		kernel.dispose();
    		Integer oldSize = new Integer(_kernelHashTable.size());
    		_kernelHashTable.remove(name);
    		firePropertyChange(KERNELLISTLENGTH, oldSize, new Integer(_kernelHashTable.size()));
    	}
    }
    
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
    
    public List getKernelNameList(){
    	String[] arr = new String[_kernelHashTable.size()];
    	List out = new ArrayList();
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
     * Sort by car road name
     * @return list of car ids ordered by road name
     */
    public List getCarsByRoadNameList() {
        String[] arr = new String[_carHashTable.size()];
        List out = new ArrayList();
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
     * Sort by car number, number can alpha numeric
     * @return list of car ids ordered by number
     */
    public List getCarsByNumberList() {
    	// first get by road list
    	List sortByRoad = getCarsByRoadNameList();
    	// now re-sort
    	List out = new ArrayList();
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
        				out.add(j, sortByRoad.get(i));
        				carAdded = true;
        				break;
        			}
        		}catch (NumberFormatException e) {
 //       			log.debug("list out road number isn't a number");
        		}
    		}
    		if (!carAdded){
    			out.add( sortByRoad.get(i));
    		}
    	}
        return out;
    }
    
    /**
     * Sort by car type
     * @return list of car ids ordered by car type
     */
    public List getCarsByTypeList() {
    	// first get by number list
    	List sortByNumber = getCarsByNumberList();

    	// now re-sort
    	List out = new ArrayList();
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
     * Sort by car kernel
     * @return list of car ids ordered by car kernel
     */
    public List getCarsByKernelList() {
    	// first get by number list
    	List sortByNumber = getCarsByNumberList();

    	// now re-sort
    	List out = new ArrayList();
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
    public List getCarsByLocationList() {
    	// first get by number list
    	List sortByNumber = getCarsByNumberList();

    	// now re-sort
    	List out = new ArrayList();
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
    public List getCarsByDestinationList() {
    	// first get by location list
    	List sortByLocation = getCarsByLocationList();

    	// now re-sort
    	List out = new ArrayList();
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
    public List getCarsByTrainList() {
    	// first get by location list
    	List sortByLocation = getCarsByLocationList();

    	// now re-sort
    	List out = new ArrayList();
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
     * Sort by cars moves
     * @return list of car ids ordered by car moves
     */
    public List getCarsByMovesList() {
    	// first get by road list
    	List sortByRoad = getCarsByRoadNameList();

    	// now re-sort
    	List out = new ArrayList();
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
	 * Return a list available cars (no assigned train) on a route, cars are
	 * ordered least recently moved to most recently moved.
	 * 
	 * @param train
	 * @return List of cars with no assigned train on a route
	 */
    public List getCarsAvailableTrainList(Train train) {
    	Route route = train.getRoute();
    	// get a list of locations served by this route
    	List routeList = route.getLocationsBySequenceList();
    	// don't include cars at route destination
    	RouteLocation destination = null;
    	if (routeList.size()>1){
    		destination = route.getLocationById((String)routeList.get(routeList.size()-1));
    		// However, if the destination is visited at least once, must include all cars
    		RouteLocation test;
    		for (int i=0; i<routeList.size()-1; i++){
    			test = route.getLocationById((String)routeList.get(i));
    			if (destination.getName().equals(test.getName())){
    				destination = null;
    				break;
    			}
    		}
    	}
    	// get cars by number list
    	List carsSortByNum = getCarsByNumberList();
    	// now build list of available cars for this route
    	List out = new ArrayList();
    	boolean carAdded = false;
    	Car c;
 
    	for (int i = 0; i < carsSortByNum.size(); i++) {
    		carAdded = false;
    		c = getCarById((String) carsSortByNum.get(i));
    		RouteLocation rl = route.getLocationByName(c.getLocationName());
    		// get cars that don't have an assigned train, or the assigned train is this one 
    		if (rl != null && rl != destination && (c.getTrain() == null || train.equals(c.getTrain()))){
    			// sort by car moves
    			int inMoves = c.getMoves();
    			for (int j = 0; j < out.size(); j++) {
    				c = getCarById((String) out.get(j));
    				int outMoves = c.getMoves();
    				if (inMoves < outMoves) {
    					out.add(j, carsSortByNum.get(i));
    					carAdded = true;
    					break;
    				}
    			}
    			if (!carAdded) {
    				out.add(carsSortByNum.get(i));
    			}
    		}
    	}
    	return out;
    }
    
    /**
	 * return a list of cars assigned to a train sorted by destination.
	 * Caboose or car with FRED will be the last car in the list 
	 * 
	 * @param train
	 * @return Ordered list of cars assigned to the train
	 */
    public List getCarsByTrainList(Train train) {
    	// get cars available list
    	List available = getCarsAvailableTrainList(train);
    	List inTrain = new ArrayList();
    	Car car;

    	for (int i = 0; i < available.size(); i++) {
    		car = getCarById((String) available.get(i));
    		// get only cars that are assigned to this train
    		if(car.getTrain() == train)
    			inTrain.add(available.get(i));
    	}
    	// now sort by track destination
    	List out = new ArrayList();
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
    				out.add(j, inTrain.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			if (lastCarAdded)
    				out.add(out.size()-1,inTrain.get(i));
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

