// CarManager.java

package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;

import jmri.jmrit.operations.trains.Train;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import org.jdom.Element;


/**
 * Manages the cars.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.30 $
 */
public class CarManager {
	
	// Edit car frame attributes
	protected CarEditFrame _carEditFrame = null;
	protected Dimension _editFrameDimension = null;
	protected Point _editFramePosition = null;

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

	public void setCarEditFrame(CarEditFrame frame){
		_carEditFrame = frame;
	}

	public Dimension getCarEditFrameSize(){
		return _editFrameDimension;
	}

	public Point getCarEditFramePosition(){
		return _editFramePosition;
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
     * Get a car by Radio Frequency Identification (RFID)
     * @param rfid car's RFID.
     * @return the car with the specific RFID, or null if not found
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
    		register(car); 
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
    
    /**
     * Get a comboBox loaded with current Kernel names
     * @return comboBox with Kernel names.
     */
    public JComboBox getKernelComboBox(){
    	JComboBox box = new JComboBox();
    	box.addItem("");
      	List<String> kernelNames = getKernelNameList();
    	for (int i=0; i<kernelNames.size(); i++) {
       		box.addItem(kernelNames.get(i));
    	}
    	return box;
    }
    
    /**
     * Update an existing comboBox with the current kernel names
     * @param box comboBox requesting update
     */
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
    	Enumeration<String> en = _carHashTable.keys();
        String[] arr = new String[_carHashTable.size()];
        List<String> out = new ArrayList<String>();     
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
    	return getCarsByList(getCarsByIdList(), CARS_BY_ROAD);
    }
    
    /**
     * Sort by car number, number can alpha numeric
     * @return list of car ids ordered by number
     */
    public List<String> getCarsByNumberList() {
    	//log.debug("start car sort by number list");
    	// first get by road list
    	List<String> sortIn = getCarsByRoadNameList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	int carNumber = 0;
    	int outCarNumber = 0;
    	int notInteger = -999999999;	// flag when car number isn't an integer
    	String[] number;
    	boolean carAdded = false;

    	for (int i=0; i<sortIn.size(); i++){
    		carAdded = false;
    		try{
    			carNumber = Integer.parseInt(getCarById(sortIn.get(i)).getNumber());
    			getCarById(sortIn.get(i)).number = carNumber;
    		}catch (NumberFormatException e) {
    			// maybe car number in the format xxx-y
    	   		try{
        			number = getCarById(sortIn.get(i)).getNumber().split("-");
        			carNumber = Integer.parseInt(number[0]);
        			getCarById(sortIn.get(i)).number = carNumber;
        		}catch (NumberFormatException e2) {
        			getCarById(sortIn.get(i)).number = notInteger;
        			// sort alpha numeric numbers at the end of the out list
        			String numberIn = getCarById(sortIn.get(i)).getNumber(); 
        			//log.debug("car in road number ("+numberIn+") isn't a number");
        			for (int k=(out.size()-1); k>=0; k--){
        				String numberOut = getCarById(out.get(k)).getNumber();
        				try{
        					Integer.parseInt(numberOut);
        					// done, place car with alpha numeric number after
        					// cars with real numbers.
        					out.add(k+1, sortIn.get(i));
        					carAdded = true;
        					break;
        				}catch (NumberFormatException e3) {
        					if (numberIn.compareToIgnoreCase(numberOut)>=0){
            					out.add(k+1, sortIn.get(i));
            					carAdded = true;
            					break;
        					}
        				}
        			}
        			if(!carAdded)
        				out.add(0, sortIn.get(i));
        			continue;
        		}
    		}
 
    		int start = 0;
    		// page to improve sort performance. 
    		int divisor = out.size()/pageSize;
    		for (int k=divisor; k>0; k--){
    			outCarNumber  = getCarById(out.get((out.size()-1)*k/divisor)).number;
    			if(outCarNumber == notInteger)
    				continue;
    			if (carNumber >= outCarNumber){
    				start = (out.size()-1)*k/divisor;
    				break;
    			}
    		}
    		for (int j=start; j<out.size(); j++ ){
    			outCarNumber = getCarById(out.get(j)).number;
    			if (outCarNumber == notInteger){
    				try{
    					outCarNumber = Integer.parseInt(getCarById(out.get(j)).getNumber());
    				}catch (NumberFormatException e) {        			
    					try{      	   			
    						number = getCarById(out.get(j)).getNumber().split("-");
    						outCarNumber = Integer.parseInt(number[0]);
    					}catch (NumberFormatException e2) {
    						//Car c = getCarById(out.get(j));
    						//log.debug("Car ("+c.getId()+") road number ("+c.getNumber()+") isn't a number");
    						// force car add
    						outCarNumber = carNumber+1;
    					}
    				}
    			}
        		if (carNumber < outCarNumber){
        			out.add(j, sortIn.get(i));
        			carAdded = true;
        			break;
        		}
    		}
    		if (!carAdded){
    			out.add(sortIn.get(i));
    		}
    	}
    	//log.debug("end car sort by number list");
    	return out;
    }
    
    /**
     * Sort by car type names
     * @return list of car ids ordered by car type
     */
    public List<String> getCarsByTypeList() {
    	return getCarsByList(getCarsByRoadNameList(), CARS_BY_TYPE);
    }
    
    /**
     * Sort by car color names
     * @return list of car ids ordered by car color
     */
    public List<String> getCarsByColorList() {
    	return getCarsByList(getCarsByTypeList(), CARS_BY_COLOR);
    }
    
    /**
     * Sort by car kernel names
     * @return list of car ids ordered by car kernel
     */
    public List<String> getCarsByKernelList() {
    	return getCarsByList(getCarsByNumberList(), CARS_BY_KERNEL);
    }
 
    /**
     * Sort by car location
     * @return list of car ids ordered by car location
     */
    public List<String> getCarsByLocationList() {
    	return getCarsByList(getCarsByNumberList(), CARS_BY_LOCATION);
    }
    
    /**
     * Sort by car destination
     * @return list of car ids ordered by car destination
     */
    public List<String> getCarsByDestinationList() {
    	return getCarsByList(getCarsByLocationList(), CARS_BY_DESTINATION);
    }
    
    /**
     * Sort by cars in trains
     * @return list of car ids ordered by trains
     */
    public List<String> getCarsByTrainList() {
    	return getCarsByList(getCarsByLocationList(), CARS_BY_TRAIN);
    }
    
    /**
     * Sort by car loads
     * @return list of car ids ordered by car loads
     */
    public List<String> getCarsByLoadList() {
    	return getCarsByList(getCarsByLocationList(), CARS_BY_LOAD);
    }
    
    /**
     * Sort by car moves
     * @return list of car ids ordered by car moves
     */
    public List<String> getCarsByMovesList() {
    	// get random order of car ids
    	Enumeration<String> en = _carHashTable.keys();
    	List<String> sortIn = new ArrayList<String>();
        while (en.hasMoreElements()) {
        	sortIn.add(en.nextElement());
        }

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	int inMoves = 0;
    	int outMoves = 0;
     	boolean carAdded = false;

    	for (int i=0; i<sortIn.size(); i++){
    		carAdded = false;
    		inMoves = getCarById (sortIn.get(i)).getMoves();
    		int start = 0;
    		// page to improve performance.
      		int divisor = out.size()/pageSize;
      		for (int k=divisor; k>0; k--){
      			outMoves = getCarById(out.get((out.size()-1)*k/divisor)).getMoves();
      			if (inMoves>=outMoves){
      				start = (out.size()-1)*k/divisor;
      				break;
      			}
      		}
      		for (int j=start; j<out.size(); j++ ){
					outMoves = getCarById(out.get(j)).getMoves();
					if (inMoves < outMoves) {
						out.add(j, sortIn.get(i));
						carAdded = true;
						break;
					}
				}
     		if (!carAdded){
    			out.add(sortIn.get(i));
    		}
    	}
    	return out;
    }

    /**
     * Sort by when car was built
     * @return list of car ids ordered by car built date
     */
    public List<String> getCarsByBuiltList() {
    	return getCarsByList(getCarsByIdList(), CARS_BY_BUILT);
    }
    
    /**
     * Sort by car owner
     * @return list of car ids ordered by car owner
     */
    public List<String> getCarsByOwnerList() {
    	return getCarsByList(getCarsByIdList(), CARS_BY_OWNER);
    }
   
    /**
     * Sort by car RFID
     * @return list of car ids ordered by RFIDs
     */
    public List<String> getCarsByRfidList() {
    	return getCarsByList(getCarsByIdList(), CARS_BY_RFID);
    }
    
    private static final int pageSize = 64;
    
    private List<String> getCarsByList(List<String> sortIn, int attribute) {
    	List<String> out = new ArrayList<String>();
    	String carIn;
    	for (int i=0; i<sortIn.size(); i++){
    		boolean carAdded = false;
    		carIn = (String)getCarAttribute(getCarById(sortIn.get(i)), attribute);
    		int start = 0;
    		// page to improve performance.  Most cars have id = road+number
      		int divisor = out.size()/pageSize;
      		for (int k=divisor; k>0; k--){
      			String carOut = (String)getCarAttribute(getCarById(out.get((out.size()-1)*k/divisor)), attribute);
      			if (carIn.compareToIgnoreCase(carOut)>=0){
      				start = (out.size()-1)*k/divisor;
      				break;
      			}
      		}
      		for (int j=start; j<out.size(); j++ ){
    			String carOut = (String)getCarAttribute(getCarById(out.get(j)), attribute);
    			if (carIn.compareToIgnoreCase(carOut)<0){
    				out.add(j, sortIn.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortIn.get(i));
    		}
    	}
    	return out;
    }
    
    // The various sort options for cars
    private static final int CARS_BY_NUMBER = 0;
    private static final int CARS_BY_ROAD = 1;
    private static final int CARS_BY_TYPE = 2;
    private static final int CARS_BY_COLOR = 3;
    private static final int CARS_BY_LOAD = 4;
    private static final int CARS_BY_KERNEL = 5;
    private static final int CARS_BY_LOCATION = 6;
    private static final int CARS_BY_DESTINATION = 7;
    private static final int CARS_BY_TRAIN = 8;
    private static final int CARS_BY_MOVES = 9;
    private static final int CARS_BY_BUILT = 10;
    private static final int CARS_BY_OWNER = 11;
    private static final int CARS_BY_RFID = 12;
    
    private Object getCarAttribute(Car car, int attribute){
    	switch (attribute){
    	case CARS_BY_NUMBER: return car.getNumber();
    	case CARS_BY_ROAD: return car.getRoad();
    	case CARS_BY_TYPE: return car.getType();
    	case CARS_BY_COLOR: return car.getColor();
    	case CARS_BY_LOAD: return car.getLoad();
    	case CARS_BY_KERNEL: return car.getKernelName();
    	case CARS_BY_LOCATION: return car.getLocationName() + car.getTrackName();
    	case CARS_BY_DESTINATION: return car.getDestinationName() + car.getDestinationTrackName();
    	case CARS_BY_TRAIN: return car.getTrainName();
    	case CARS_BY_MOVES: return car.getMoves(); // returns an integer
    	case CARS_BY_BUILT: return car.getBuilt();
    	case CARS_BY_OWNER: return car.getOwner();
    	case CARS_BY_RFID: return car.getRfid();
    	default: return "unknown";	
    	}
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
    		// only use cars with a location
    		if (car.getLocationName().equals(""))
    			continue;
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
     * Get a list of car road names where the car was flagged with FRED
     * @return List of road names of cars with FREDs
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

	public void options (org.jdom.Element values) {
		if (log.isDebugEnabled()) log.debug("ctor from element "+values);
		// get Car Edit attributes
		Element e = values.getChild("carEditOptions");
		if (e != null){
			try {
				int x = e.getAttribute("x").getIntValue();
				int y = e.getAttribute("y").getIntValue();
				int height = e.getAttribute("height").getIntValue();
				int width = e.getAttribute("width").getIntValue();
				_editFrameDimension = new Dimension(width, height);
				_editFramePosition = new Point(x,y);
			} catch ( org.jdom.DataConversionException ee) {
				log.debug("Did not find car edit frame attributes");
			} catch ( NullPointerException ne) {
				log.debug("Did not find car edit frame attributes");
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
        // now save Car Edit frame size and position
        Element e = new org.jdom.Element("carEditOptions");
        Dimension size = getCarEditFrameSize();
        Point posn = getCarEditFramePosition();
        if (_carEditFrame != null){
        	size = _carEditFrame.getSize();
        	posn = _carEditFrame.getLocation();
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarManager.class.getName());

}

