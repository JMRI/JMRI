// CarManager.java

package jmri.jmrit.operations.rollingstock.cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockManager;

import jmri.jmrit.operations.trains.Train;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import org.jdom.Attribute;
import org.jdom.Element;


/**
 * Manages the cars.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision$
 */
public class CarManager extends RollingStockManager{

	// Cars frame table column widths (12), starts with Number column and ends with Edit
	private int[] _carsTableColumnWidths = {60, 60, 65, 35, 75, 65, 190, 190, 65, 50, 65, 70};
	
	protected Hashtable<String, Kernel> _kernelHashTable = new Hashtable<String, Kernel>(); // stores Kernels by number

	public static final String KERNEL_LISTLENGTH_CHANGED_PROPERTY = "KernelListLength"; // NOI18N

    public CarManager() {
    }
    
	/** record the single instance **/
	private static CarManager _instance = null;

	public static synchronized CarManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("CarManager creating instance");
			// create and load
			_instance = new CarManager();
			OperationsSetupXml.instance();					// load setup
	    	// create manager to load cars and their attributes
	    	CarManagerXml.instance();
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
    		firePropertyChange(KERNEL_LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_kernelHashTable.size()));
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
    		firePropertyChange(KERNEL_LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_kernelHashTable.size()));
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
     * Sort by rolling stock location
     * @return list of car ids ordered by RollingStock location
     */
    public List<String> getByLocationList() {
    	return getByList(getByKernelList(), BY_LOCATION);
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
     * @return list of car ids ordered by car return when empty
     */
    public List<String> getByRweList() {
    	return getByList(getByLocationList(), BY_RWE);
    }
    
    public List<String> getByFinalDestinationList() {
    	return getByList(getByDestinationList(), BY_FINAL_DEST);
    }
    
    /**
     * Sort by car kernel names
     * @return list of car ids ordered by car kernel
     */
    public List<String> getByWaitList() {
    	return getByIntList(getByIdList(), BY_WAIT);
    }
    
    // The special sort options for cars
    private static final int BY_LOAD = 4;
    private static final int BY_KERNEL = 5;
    private static final int BY_RWE = 13;		// Return When Empty
    private static final int BY_FINAL_DEST = 14;// Next destination
    private static final int BY_WAIT = 16;
    
    // add car options to sort list
    protected Object getRsAttribute(RollingStock rs, int attribute){
    	Car car = (Car)rs;
    	switch (attribute){
    	case BY_LOAD: return car.getLoadName();
    	case BY_KERNEL: return car.getKernelName();
    	case BY_RWE: return car.getReturnWhenEmptyDestName();
    	case BY_FINAL_DEST: return car.getFinalDestinationName() + car.getFinalDestinationTrackName();
    	case BY_WAIT: return car.getWait();	// returns an integer
    	default: return super.getRsAttribute(car, attribute);
    	}
    }
    
    /**
	 * Get a list of Cars assigned to a train sorted by destination.
	 * Passenger cars will be placed at the end of the list. 
	 * Caboose or car with FRED will be the last car(s) in the list
	 * 
	 * @param train
	 * @return Ordered list of Car ids assigned to the train
	 */
    public List<String> getByTrainDestinationList(Train train) {
     	List<String> inTrain = getByTrainList(train);
     	// now sort by track destination
    	List<String> out = new ArrayList<String>();
    	boolean carAdded;
    	int lastCarsIndex = 0;	// incremented each time a car is added to the end of the train 
    	for (int i = 0; i < inTrain.size(); i++) {
    		carAdded = false;
    	   	Car car = getById(inTrain.get(i));
    		String carDestination = car.getDestinationTrackName();
    		for (int j = 0; j < out.size(); j++) {
    			Car carOut = getById (out.get(j));
    			String carOutDest = carOut.getDestinationTrackName();
    			if (carDestination.compareToIgnoreCase(carOutDest)<0 && !car.isCaboose() && !car.hasFred() && !car.isPassenger()){
    				out.add(j, inTrain.get(i));
    				carAdded = true;
    				break;
    			}
    		}
    		if (!carAdded){
    			if (car.isCaboose()||car.hasFred()){
    				out.add(inTrain.get(i));	// place at end of list
    				lastCarsIndex++;
    			} else {
    				out.add(out.size()-lastCarsIndex, inTrain.get(i));
    			}
    			if (car.isPassenger()){
    				lastCarsIndex++;
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
    		if (car.isCaboose() && !names.contains(car.getRoadName())){
    			names.add(car.getRoadName());
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
    		if (car.hasFred() && !names.contains(car.getRoadName())){
    			names.add(car.getRoadName());
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
    public void replaceLoad(String type, String oldLoadName, String newLoadName){
    	List<String> cars = getList();
    	for (int i = 0; i < cars.size(); i++) {
    		Car car = getById(cars.get(i));
    		if (car.getTypeName().equals(type) && car.getLoadName().equals(oldLoadName))
    			if (newLoadName != null)
    				car.setLoadName(newLoadName);
    			else
    				car.setLoadName(CarLoads.instance().getDefaultEmptyName());
    	}
    }
	
	public List<String> getCarsLocationUnknown(){
		List<String> mias = new ArrayList<String>();
		List<String> cars = getByIdList();
		for (int i = 0; i < cars.size(); i++) {
			Car car = getById(cars.get(i));
			if (car.isLocationUnknown())
				mias.add(cars.get(i));	// return unknown location car ids
		}
		return mias;
	}

	/**
    * 
    * @return get an array of table column widths for the trains frame
    */
   public int[] getCarsFrameTableColumnWidths(){
   	return _carsTableColumnWidths.clone();
   }
   
   @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP2")
   public void setCarsFrameTableColumnWidths(int[] tableColumnWidths){
   	_carsTableColumnWidths = tableColumnWidths;
   }
   
	public void load(Element root) {
		// new format using elements starting version 3.3.1
		if (root.getChild(Xml.NEW_KERNELS)!= null) {
			@SuppressWarnings("unchecked")
			List<Element> l = root.getChild(Xml.NEW_KERNELS).getChildren(Xml.KERNEL);
			if (log.isDebugEnabled()) log.debug("Car manager sees "+l.size()+" kernels");
			Attribute a;
			for (int i=0; i<l.size(); i++) {
				Element kernel = l.get(i);
				if ((a = kernel.getAttribute(Xml.NAME)) != null) {
					newKernel(a.getValue());
				}
			}
		}
		// old format
		else if (root.getChild(Xml.KERNELS) != null) {
			String names = root.getChildText(Xml.KERNELS);
			if (!names.equals("")) {
				String[] kernelNames = names.split("%%"); // NOI18N
				if (log.isDebugEnabled())
					log.debug("kernels: " + names);
				for (int i = 0; i < kernelNames.length; i++) {
					newKernel(kernelNames[i]);
				}
			}
		}

		if (root.getChild(Xml.OPTIONS) != null) {
			Element options = root.getChild(Xml.OPTIONS);
			if (log.isDebugEnabled())
				log.debug("ctor from element " + options);
			// get Cars Table Frame attributes
			Element e = options.getChild(Xml.CARS_OPTIONS);
			if (e != null) {
				org.jdom.Attribute a;
				// backwards compatible TODO remove in 2013 after production release
				if ((a = e.getAttribute(Xml.COLUMN_WIDTHS)) != null) {
					String[] widths = a.getValue().split(" ");
					for (int i = 0; i < widths.length; i++) {
						try {
							_carsTableColumnWidths[i] = Integer.parseInt(widths[i]);
						} catch (NumberFormatException ee) {
							log.error("Number format exception when reading trains column widths");
						}
					}
				}
			}
		}
		
        if (root.getChild(Xml.CARS) != null) {
        	@SuppressWarnings("unchecked")
            List<Element> l = root.getChild(Xml.CARS).getChildren(Xml.CAR);
            if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" cars");
            for (int i=0; i<l.size(); i++) {
                register(new Car(l.get(i)));
            }
        }
	}

	   /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-cars.dtd.
     */
    public void store(Element root) {
    	root.addContent(new Element(Xml.OPTIONS));     	// nothing to save under options

    	Element values;  
    	List<String> names = getKernelNameList();
    	if (Control.backwardCompatible) {
    		root.addContent(values = new Element(Xml.KERNELS));
    		for (int i=0; i<names.size(); i++){
    			String kernelNames = names.get(i)+"%%"; // NOI18N
    			values.addContent(kernelNames);
    		}
    	}
        // new format using elements
        Element kernels = new Element(Xml.NEW_KERNELS);
        for (int i=0; i<names.size(); i++){
        	Element kernel = new Element(Xml.KERNEL);
        	kernel.setAttribute(new Attribute(Xml.NAME, names.get(i)));
        	kernels.addContent(kernel);
        }
        root.addContent(kernels);
        root.addContent(values = new Element(Xml.CARS));
        // add entries
        List<String> carList = getList();
        for (int i=0; i<carList.size(); i++) {
        	Car car = getById(carList.get(i));
            values.addContent(car.store());
        }
    }
    
    protected void firePropertyChange(String p, Object old, Object n){
		// Set dirty
		CarManagerXml.instance().setDirty(true);
    	super.firePropertyChange(p, old, n);
    }

    static Logger log = LoggerFactory.getLogger(CarManager.class.getName());

}

