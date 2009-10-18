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
 * @version	$Revision: 1.29 $
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
    
    private static final int pageSize = 64;
    /**
     * Sort by car road name
     * @return list of car ids ordered by road name
     */
    public List<String> getCarsByRoadNameList() {
    	//log.debug("start car sort by road name list");
      	// first get by id list
    	List<String> sortIn = getCarsByIdList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carRoad = "";
    	String outCarRoad = "";
    	boolean carAdded = false;

    	for (int i=0; i<sortIn.size(); i++){
    		carAdded = false;
    		carRoad = getCarById(sortIn.get(i)).getRoad();
    		int start = 0;
    		// page to improve performance.  Most cars have id = road+number
      		int divisor = out.size()/pageSize;
      		for (int k=divisor; k>0; k--){
      			outCarRoad = getCarById(out.get((out.size()-1)*k/divisor)).getRoad();
      			if (carRoad.compareToIgnoreCase(outCarRoad)>=0){
      				start = (out.size()-1)*k/divisor;
      				break;
      			}
      		}
      		for (int j=start; j<out.size(); j++ ){
    			outCarRoad = getCarById(out.get(j)).getRoad();
    			if (carRoad.compareToIgnoreCase(outCarRoad)<0){
    				out.add(j, sortIn.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortIn.get(i));
    		}
    	}
    	//log.debug("end car sort by road name list");
    	return out;
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
    	//log.debug("start car sort by type list");
    	// first get by road list
    	List<String> sortIn = getCarsByRoadNameList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carType = "";
    	String outCarType = "";
    	boolean carAdded = false;

    	for (int i=0; i<sortIn.size(); i++){
    		carAdded = false;
    		carType = getCarById(sortIn.get(i)).getType();
       		int start = 0;
    		// page to improve sort performance. 
       		int divisor = out.size()/pageSize;
       		for (int k=divisor; k>0; k--){
       			outCarType = getCarById(out.get((out.size()-1)*k/divisor)).getType();
       			if (carType.compareToIgnoreCase(outCarType)>=0){
       				start = (out.size()-1)*k/divisor;
       				break;
       			}
       		}
    		for (int j=start; j<out.size(); j++ ){
    			outCarType = getCarById(out.get(j)).getType();
    			if (carType.compareToIgnoreCase(outCarType)<0){
    				out.add(j, sortIn.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			out.add(sortIn.get(i));
    		}
    	}
    	//log.debug("end car sort by type list");
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
    	String outCarColor = "";
    	boolean carAdded = false;

    	for (int i=0; i<sortByType.size(); i++){
    		carAdded = false;
    		carColor = getCarById(sortByType.get(i)).getColor();
      		int start = 0;
       		// page to improve sort performance. 
      		int divisor = out.size()/pageSize;
      		for (int k=divisor; k>0; k--){
      			outCarColor = getCarById(out.get((out.size()-1)*k/divisor)).getColor();
      			if (carColor.compareToIgnoreCase(outCarColor)>=0){
      				start = (out.size()-1)*k/divisor;
      				break;
      			}
      		}
      		for (int j=start; j<out.size(); j++ ){
    			outCarColor = getCarById(out.get(j)).getColor();
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
    	List<String> sortIn = getCarsByNumberList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carKernelName = "";
    	String outCarKernelName = "";
    	boolean carAdded = false;

    	for (int i=0; i<sortIn.size(); i++){
    		carAdded = false;
    		carKernelName = getCarById(sortIn.get(i)).getKernelName();
     		int start = 0;
       		// page to improve sort performance. 
      		int divisor = out.size()/pageSize;
      		for (int k=divisor; k>0; k--){
      			outCarKernelName = getCarById(out.get((out.size()-1)*k/divisor)).getKernelName();
      			if (carKernelName.compareToIgnoreCase(outCarKernelName)>=0){
      				start = (out.size()-1)*k/divisor;
      				break;
      			}
      		}
      		for (int j=start; j<out.size(); j++ ){
    			outCarKernelName = getCarById(out.get(j)).getKernelName();
    			if (carKernelName.compareToIgnoreCase(outCarKernelName)<0){
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
     * Sort by car location
     * @return list of car ids ordered by car location
     */
    public List<String> getCarsByLocationList() {
    	// first get by number list
    	List<String> sortIn = getCarsByNumberList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carLocation = "";
    	String outCarLocation = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortIn.size(); i++){
    		carAdded = false;
    		c = getCarById (sortIn.get(i));
    		carLocation = c.getLocationName()+c.getTrackName();
     		int start = 0;
       		// page to improve sort performance. 
      		int divisor = out.size()/pageSize;
      		for (int k=divisor; k>0; k--){
      			c = getCarById (out.get((out.size()-1)*k/divisor));
      			outCarLocation = c.getLocationName()+c.getTrackName();
      			if (carLocation.compareToIgnoreCase(outCarLocation)>=0){
      				start = (out.size()-1)*k/divisor;
      				break;
      			}
      		}
      		for (int j=start; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
    			outCarLocation = c.getLocationName()+c.getTrackName();
    			if (carLocation.compareToIgnoreCase(outCarLocation)<0){
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
     * Sort by car destination
     * @return list of car ids ordered by car destination
     */
    public List<String> getCarsByDestinationList() {
    	// first get by location list
    	List<String> sortIn = getCarsByLocationList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carDestination = "";
    	String outCarDestination = "";
    	boolean carAdded = false;
    	Car c;

    	for (int i = 0; i < sortIn.size(); i++) {
			carAdded = false;
			c = getCarById(sortIn.get(i));
			carDestination = c.getDestinationName()+c.getDestinationTrackName();
    		int start = 0;
       		// page to improve sort performance. 
      		int divisor = out.size()/pageSize;
      		for (int k=divisor; k>0; k--){
      			c = getCarById (out.get((out.size()-1)*k/divisor));
      			outCarDestination = c.getDestinationName()+c.getDestinationTrackName();
      			if (carDestination.compareToIgnoreCase(outCarDestination)>=0){
      				start = (out.size()-1)*k/divisor;
      				break;
      			}
      		}
      		for (int j=start; j<out.size(); j++ ){
				c = getCarById(out.get(j));
				outCarDestination = c.getDestinationName()+c.getDestinationTrackName();
				if (carDestination.compareToIgnoreCase(outCarDestination) < 0 ) {
					out.add(j, sortIn.get(i));
					carAdded = true;
					break;
				}
			}
			if (!carAdded) {
				out.add(sortIn.get(i));
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
    	List<String> sortIn = getCarsByLocationList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
     	boolean carAdded = false;
    	Car c;

    	for (int i=0; i<sortIn.size(); i++){
    		carAdded = false;
    		c = getCarById (sortIn.get(i));
    		String carTrainName = "";
    		if(c.getTrain() != null)
    			carTrainName = c.getTrain().getName();
       		int start = 0;
       		// page to improve sort performance. 
      		int divisor = out.size()/pageSize;
      		for (int k=divisor; k>0; k--){
      			c = getCarById(out.get((out.size()-1)*k/divisor));
      			String outCarTrainName = "";
      			if(c.getTrain() != null)
      				outCarTrainName = c.getTrain().getName();
      			if (carTrainName.compareToIgnoreCase(outCarTrainName)>=0){
      				start = (out.size()-1)*k/divisor;
      				break;
      			}
      		}
    		for (int j=start; j<out.size(); j++ ){
    			c = getCarById (out.get(j));
    			String outCarTrainName = "";
    			if(c.getTrain() != null)
    				outCarTrainName = c.getTrain().getName();
    			if (carTrainName.compareToIgnoreCase(outCarTrainName)<0){
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
     * Sort by car loads
     * @return list of car ids ordered by car loads
     */
    public List<String> getCarsByLoadList() {
    	// first get by location list
    	List<String> sortIn = getCarsByLocationList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carLoadName = "";
    	String outCarLoadName = "";
     	boolean carAdded = false;

    	for (int i=0; i<sortIn.size(); i++){
    		carAdded = false;
    		carLoadName = getCarById(sortIn.get(i)).getLoad();
    		int start = 0;
       		// page to improve sort performance. 
      		int divisor = out.size()/pageSize;
      		for (int k=divisor; k>0; k--){
      			outCarLoadName = getCarById(out.get((out.size()-1)*k/divisor)).getLoad();
      			if (carLoadName.compareToIgnoreCase(outCarLoadName)>=0){
      				start = (out.size()-1)*k/divisor;
      				break;
      			}
      		}
      		for (int j=start; j<out.size(); j++ ){
    			outCarLoadName = getCarById(out.get(j)).getLoad();
    			if (carLoadName.compareToIgnoreCase(outCarLoadName)<0){
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
    	// first get by type list
    	List<String> sortIn = getCarsByIdList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carBuilt = "";
    	String outCarBuilt = "";
    	boolean carAdded = false;

    	for (int i=0; i<sortIn.size(); i++){
    		carAdded = false;
    		carBuilt = getCarById (sortIn.get(i)).getBuilt();
      		int start = 0;
    		// page to improve sort performance. 
      		int divisor = out.size()/pageSize;
      		for (int k=divisor; k>0; k--){
      			outCarBuilt = getCarById(out.get((out.size()-1)*k/divisor)).getBuilt();
      			if (carBuilt.compareToIgnoreCase(outCarBuilt)>=0){
      				start = (out.size()-1)*k/divisor;
      				break;
      			}
      		}
      		for (int j=start; j<out.size(); j++ ){
    			outCarBuilt = getCarById (out.get(j)).getBuilt();
    			if (carBuilt.compareToIgnoreCase(outCarBuilt)<0){
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
     * Sort by car owner
     * @return list of car ids ordered by car owner
     */
    public List<String> getCarsByOwnerList() {
    	// first get by type list
    	List<String> sortIn = getCarsByIdList();

    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carOwner = "";
    	String outCarOwner = "";
    	boolean carAdded = false;

    	for (int i=0; i<sortIn.size(); i++){
    		carAdded = false;
    		carOwner = getCarById(sortIn.get(i)).getOwner();
      		int start = 0;
    		// page to improve sort performance. 
      		int divisor = out.size()/pageSize;
      		for (int k=divisor; k>0; k--){
      			outCarOwner = getCarById(out.get((out.size()-1)*k/divisor)).getOwner();
      			if (carOwner.compareToIgnoreCase(outCarOwner)>=0){
      				start = (out.size()-1)*k/divisor;
      				break;
      			}
      		}
      		for (int j=start; j<out.size(); j++ ){
    			outCarOwner = getCarById(out.get(j)).getOwner();
    			if (carOwner.compareToIgnoreCase(outCarOwner)<0){
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
     * Sort by car RFID
     * @return list of car ids ordered by RFIDs
     */
    public List<String> getCarsByRfidList() {
      	// first get by id list
    	List<String> sortIn = getCarsByIdList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	String carRfid = "";
    	String outCarRfid = "";
    	boolean carAdded = false;

    	for (int i=0; i<sortIn.size(); i++){
    		carAdded = false;
    		carRfid = getCarById (sortIn.get(i)).getRfid();
      		int start = 0;
    		// page to improve sort performance. 
      		int divisor = out.size()/pageSize;
      		for (int k=divisor; k>0; k--){
      			outCarRfid = getCarById(out.get((out.size()-1)*k/divisor)).getRfid();
      			if (carRfid.compareToIgnoreCase(outCarRfid)>=0){
      				start = (out.size()-1)*k/divisor;
      				break;
      			}
      		}
    		for (int j=start; j<out.size(); j++ ){
    			outCarRfid = getCarById(out.get(j)).getRfid();
    			if (carRfid.compareToIgnoreCase(outCarRfid)<0){
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

