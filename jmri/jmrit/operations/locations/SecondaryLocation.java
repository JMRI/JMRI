package jmri.jmrit.operations.locations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import jmri.jmrit.operations.cars.CarManager;
import jmri.jmrit.operations.cars.CarRoads;
import jmri.jmrit.operations.cars.Car;

import org.jdom.Element;

/**
 * Represents a secondary location on the layout
 * Can be a siding, yard, staging, etc.
 * 
 * @author Daniel Boudreau
 * @version             $Revision: 1.1 $
 */
public class SecondaryLocation implements java.beans.PropertyChangeListener {

	protected String _id = "";
	protected String _name = "";
	protected String _locType = "";					// yard, siding or staging
	protected String _sortId = "";
	protected String _roadOption = ALLROADS;
	protected int _trainDir = EAST+WEST+NORTH+SOUTH; //train direction served by this location
	protected int _numberCars = 0;
	protected int _pickupCars = 0;
	protected int _dropCars = 0;
	protected int _length = 0;				//length of tracks at this location
	protected int _reserved = 0;			//length of track reserved by trains
	protected int _usedLength = 0;			//length of track filled by cars and engines 
	protected int _moves = 0;
	protected String _comment = "";
	
	public static final String STAGING = "Staging";	// the four types of secondary locations
	public static final String INTERCHANGE = "Interchange";
	public static final String YARD = "Yard";
	public static final String SIDING = "Siding";
	
	public static final String DISPOSE = "dispose";
	
	public static final int EAST = 1;		// train direction serviced by this location
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;
	
	public static final String ALLROADS = "All";
	public static final String INCLUDEROADS = "Include";
	public static final String EXCLUDEROADS = "Exclude";

	
	public SecondaryLocation(String id, String name, String type) {
		log.debug("New secondary location " + name + " " + id);
		_locType = type;
		_name = name;
		_id = id;
	}
	
	// for combo boxes
	public String toString(){
		return _name;
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

	public String getName() {
		return _name;
	}
	
	public String getLocType() {
		return _locType;
	}

	public void setLength(int length) {
		int old = _length;
		_length = length;
		if (old != length)
			firePropertyChange("length", Integer.toString(old), Integer.toString(length));
	}

	public int getLength() {
		return _length;
	}
	
	public void setReserved(int reserved) {
		int old = _reserved;
		_reserved = reserved;
		if (old != reserved)
			firePropertyChange("reserved", Integer.toString(old), Integer.toString(reserved));
	}

	public int getReserved() {
		return _reserved;
	}
	
	public void setUsedLength(int length) {
		int old = _usedLength;
		_usedLength = length;
		if (old != length)
			firePropertyChange("usedLength", Integer.toString(old), Integer.toString(length));
	}
	
	public int getUsedLength() {
		return _usedLength;
	}
	
	public void setNumberCars(int cars) {
		int old = _numberCars;
		_numberCars = cars;
		if (old != cars)
			firePropertyChange("numberCars", Integer.toString(old), Integer.toString(cars));
	}
	
	public int getNumberCars() {
		return _numberCars;
	}
	
	
	/**
	 * Adds a car to a specific location.  
	 * @param car
	 */	
	public void addCar (Car car){
   		int numberOfCars = getNumberCars();
		numberOfCars++;
		setNumberCars(numberOfCars);
		int usedLength = getUsedLength() + Integer.parseInt(car.getLength())+ car.COUPLER;
		setUsedLength(usedLength);
		return;
	}
	
	public void deleteCar (Car car){
   		int numberOfCars = getNumberCars();
		numberOfCars--;
		setNumberCars(numberOfCars);
		int usedLength = getUsedLength() - (Integer.parseInt(car.getLength())+ car.COUPLER);
		setUsedLength(usedLength);
	}

	public void addPickupCar(Car car) {
		int old = _pickupCars;
		_pickupCars++;
		_moves++;
//		int reserved = getReserved() + (Integer.parseInt(car.getLength()) + car.COUPLER);
//		setReserved(reserved);
		firePropertyChange("pickupCars", Integer.toString(old), Integer.toString(_pickupCars));
	}
	
	public void deletePickupCar(Car car) {
		int old = _pickupCars;
		_pickupCars--;
//		int reserved = getReserved() - (Integer.parseInt(car.getLength()) + car.COUPLER);
//		setReserved(reserved);
		firePropertyChange("pickupCars", Integer.toString(old), Integer.toString(_pickupCars));
	}
	
	public int getPickupCars() {
		return _pickupCars;
	}

	public void addDropCar(Car car) {
		int old = _dropCars;
		_dropCars++;
		_moves++;
		int reserved = getReserved() + Integer.parseInt(car.getLength()) + car.COUPLER;
		setReserved(reserved);
		firePropertyChange("dropCars", Integer.toString(old), Integer.toString(_dropCars));
	}
	
	public void deleteDropCar(Car car) {
		int old = _dropCars;
		_dropCars--;
		int reserved = getReserved() - (Integer.parseInt(car.getLength()) + car.COUPLER);
		setReserved(reserved);
		firePropertyChange("dropCars", Integer.toString(old), Integer.toString(_dropCars));
	}
	
	public int getDropCars() {
		return _dropCars;
	}

	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
	}
	
    List _typeList = new ArrayList();
    
    public String[] getTypeNames(){
      	String[] types = new String[_typeList.size()];
     	for (int i=0; i<_typeList.size(); i++)
     		types[i] = (String)_typeList.get(i);
   		return types;
    }
    
    private void setTypeNames(String[] types){
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
    	log.debug("secondary location " +getName()+ " add car type "+type);
//    	firePropertyChange (CARTYPES, null, LENGTH);
    }
    
    public void deleteTypeName(String type){
    	_typeList.remove(type);
    	log.debug("secondary location " +getName()+ " delete car type "+type);
//    	firePropertyChange (CARTYPES, null, LENGTH);
     }
    
    public boolean acceptsTypeName(String type){
    	return _typeList.contains(type);
    }
    
    public String getRoadOption (){
    	return _roadOption;
    }
    
    public void setRoadOption (String option){
    	_roadOption = option;
    }
    
	public void setTrainDirections(int direction){
		int old = _trainDir;
		_trainDir = direction;
		if (old != direction)
			firePropertyChange("trainDirection", Integer.toString(old), Integer.toString(direction));
	}
	
	public int getTrainDirections(){
		return _trainDir;
	}
 
    List _roadList = new ArrayList();
    
    public String[] getRoadNames(){
      	String[] roads = new String[_roadList.size()];
     	for (int i=0; i<_roadList.size(); i++)
     		roads[i] = (String)_roadList.get(i);
     	if (_roadList.size() == 0)
     		return roads;
     	jmri.util.StringUtil.sort(roads);
   		return roads;
    }
    
    private void setRoadNames(String[] roads){
    	if (roads.length == 0) return;
    	jmri.util.StringUtil.sort(roads);
 		for (int i=0; i<roads.length; i++)
 			_roadList.add(roads[i]);
    }
    
    public void addRoadName(String road){
     	if (_roadList.contains(road))
    		return;
    	_roadList.add(road);
    	log.debug("secondary location " +getName()+ " add car road "+road);
    }
    
    public void deleteRoadName(String road){
    	_roadList.remove(road);
    	log.debug("secondary location " +getName()+ " delete car road "+road);
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
    
    public boolean containsRoadName(String road){
      		return _roadList.contains(road);
    }  
    
    public int getMoves(){
    	return _moves;
    }
    
    private void setMoves(int moves){
    	_moves = moves;
    }
    
    public void dispose(){
    	firePropertyChange (DISPOSE, null, DISPOSE);
    }
    
	
	   /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml
     *
     * @param e  Consist XML element
     */
    public SecondaryLocation(org.jdom.Element e) {
        //if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
        if ((a = e.getAttribute("id")) != null )  _id = a.getValue();
        else log.warn("no id attribute in secondary location element when reading operations");
        if ((a = e.getAttribute("name")) != null )  _name = a.getValue();
        if ((a = e.getAttribute("locType")) != null )  _locType = a.getValue();
        if ((a = e.getAttribute("length")) != null )  _length = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("moves")) != null )  _moves = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("dir")) != null )  _trainDir = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
        if ((a = e.getAttribute("carTypes")) != null ) {
        	String names = a.getValue();
           	String[] types = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("Secondary location (" +getName()+ ") accepts car types: "+ names);
        	setTypeNames(types);
        }
        if ((a = e.getAttribute("carRoadOperation")) != null )  _roadOption = a.getValue();
        if ((a = e.getAttribute("carRoads")) != null ) {
        	String names = a.getValue();
           	String[] roads = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("Secondary location (" +getName()+ ") " +getRoadOption()+  " car roads: "+ names);
        	setRoadNames(roads);
        }
 
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
    	org.jdom.Element e = new org.jdom.Element("secondary");
    	e.setAttribute("id", getId());
    	e.setAttribute("name", getName());
    	e.setAttribute("locType", getLocType());
    	e.setAttribute("dir", Integer.toString(getTrainDirections()));
    	e.setAttribute("length", Integer.toString(getLength()));
    	e.setAttribute("moves", Integer.toString(getMoves()));
    	// build list of car types for this secondary location
    	String[] types = getTypeNames();
    	String typeNames ="";
    	for (int i=0; i<types.length; i++){
    		typeNames = typeNames + types[i]+"%%";
    	}
    	e.setAttribute("carTypes", typeNames);
    	// build list of car roads for this secondary location
    	String[] roads = getRoadNames();
    	String roadNames ="";
    	for (int i=0; i<roads.length; i++){
    		roadNames = roadNames + roads[i]+"%%";
    	}
    	e.setAttribute("carRoadOperation", getRoadOption());
    	e.setAttribute("carRoads", roadNames);
    	e.setAttribute("comment", getComment());

    	return e;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	log.debug("secondary location "+getName()+ " sees property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
				+ e.getNewValue());
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
			.getInstance(SecondaryLocation.class.getName());

}
