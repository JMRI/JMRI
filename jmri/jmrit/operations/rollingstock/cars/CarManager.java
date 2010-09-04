// CarManager.java

package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockManager;

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
 * @version	$Revision: 1.35 $
 */
public class CarManager extends RollingStockManager{

	protected Hashtable<String, Kernel> _kernelHashTable = new Hashtable<String, Kernel>(); // stores Kernels by number

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
     * Finds an existing Car or creates a new Car if needed
     * requires car's road and number
     * @param road car road
     * @param number car number
     * @return new car or existing Car
     */
    public Car newCar (String road, String number){
    	Car car = getByRoadAndNumber(road, number);
    	if (car == null){
    		car = new Car(road, number);
    		register(car); 
    	}
    	return car;
    }
    
    /**
     * @return requested Car object or null if none exists
     */
    public Car getById(String id) {
        return (Car)super.getById(id);
    }
    
    /**
     * Get Car by road and number
     * @param road Car road
     * @param number Car number
     * @return requested Car object or null if none exists
     */
    public Car getByRoadAndNumber(String road, String number){
    	return (Car)super.getByRoadAndNumber(road, number);
    }
    
    /**
     * Get a Car by type and road. Used to test that a car with a specific
     * type and road exists. 
     * @param type car type.
     * @param road car road.
     * @return the first car found with the specified type and road.
     */
    public Car getByTypeAndRoad(String type, String road){
    	return (Car)super.getByTypeAndRoad(type, road);
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
    		Integer oldSize = Integer.valueOf(_kernelHashTable.size());
    		_kernelHashTable.put(name, kernel);
    		firePropertyChange(KERNELLISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_kernelHashTable.size()));
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
    		Integer oldSize = Integer.valueOf(_kernelHashTable.size());
    		_kernelHashTable.remove(name);
    		firePropertyChange(KERNELLISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_kernelHashTable.size()));
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
     * Sort by car kernel names
     * @return list of car ids ordered by car kernel
     */
    public List<String> getByKernelList() {
    	return getByList(getByNumberList(), BY_KERNEL);
    }
 
    /**
     * Sort by car loads
     * @return list of car ids ordered by car loads
     */
    public List<String> getByLoadList() {
    	return getByList(getByLocationList(), BY_LOAD);
    }
    
    /**
     * Sort by car return when empty location and track
     * @return list of RollingStock ids ordered by car return when empty
     */
    public List<String> getByRweList() {
    	return getByList(getByLocationList(), BY_RWE);
    }
    
    // The special sort options for cars
    private static final int BY_LOAD = 4;
    private static final int BY_KERNEL = 5;
    private static final int BY_RWE = 13;	// Return When Empty
    
    // add load and kernel options to sort list
    protected Object getRsAttribute(RollingStock rs, int attribute){
    	Car car = (Car)rs;
    	switch (attribute){
    	case BY_LOAD: return car.getLoad();
    	case BY_KERNEL: return car.getKernelName();
    	case BY_RWE: return car.getReturnWhenEmptyDestName();
    	default: return super.getRsAttribute(car, attribute);
    	}
    }
    
    /**
	 * Get a list of Cars assigned to a train sorted by destination.
	 * Caboose or car with FRED will be the last car in the list 
	 * 
	 * @param train
	 * @return Ordered list of Car ids assigned to the train
	 */
    public List<String> getByTrainDestinationList(Train train) {
     	List<String> inTrain = getByTrainList(train);
    	Car car;

     	// now sort by track destination
    	List<String> out = new ArrayList<String>();
    	boolean carAdded;
    	boolean lastCarAdded = false;	// true if caboose or car with FRED added to train 
    	for (int i = 0; i < inTrain.size(); i++) {
    		carAdded = false;
    		car = getById(inTrain.get(i));
    		String carDestination = car.getDestinationTrackName();
    		for (int j = 0; j < out.size(); j++) {
    			Car carOut = getById (out.get(j));
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
     * Get a list of car road names where the car was flagged as a caboose.
     * @return List of caboose road names.
     */
    public List<String> getCabooseRoadNames(){
    	List<String> names = new ArrayList<String>();
       	Enumeration<String> en = _hashTable.keys();
    	while (en.hasMoreElements()) { 
    		Car car = getById(en.nextElement());
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
       	Enumeration<String> en = _hashTable.keys();
    	while (en.hasMoreElements()) { 
    		Car car = getById(en.nextElement());
    		if (car.hasFred() && !names.contains(car.getRoad())){
    			names.add(car.getRoad());
    		}
    	}
    	return sortList(names);
    }
    
    /**
     * Replace car loads
     * @param type type of car
     * @param oldLoadName old load name
     * @param newLoadName new load name
     */
	public void replaceLoad(String type, String oldLoadName, String newLoadName) {
		List<String> cars = getByIdList();
		for (int i = 0; i < cars.size(); i++) {
			Car car = getById(cars.get(i));
			if (car.getType().equals(type) && car.getLoad().equals(oldLoadName))
				car.setLoad(newLoadName);
		}
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
        Dimension size = getEditFrameSize();
        Point posn = getEditFramePosition();
        if (_editFrame != null){
        	size = _editFrame.getSize();
        	posn = _editFrame.getLocation();
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarManager.class.getName());

}

