package jmri.jmrit.operations.locations;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import jmri.jmrit.operations.cars.CarManager;
import jmri.jmrit.operations.cars.CarRoads;
import jmri.jmrit.operations.cars.Car;
import jmri.jmrit.operations.setup.Control;

import org.jdom.Element;

/**
 * Represents a location on the layout
 * 
 * @author Daniel Boudreau
 * @version             $Revision: 1.1 $
 */
public class Location implements java.beans.PropertyChangeListener {

	protected String _id = "";
	protected String _name = "";
	protected String _sortId = "";
	protected int _IdNumber = 0;
	protected int _numberCars = 0;
	protected int _pickupCars = 0;
	protected int _dropCars = 0;
	protected int _locationOps = NORMAL;	//type of operations at this location
	protected int _trainDir = EAST+WEST+NORTH+SOUTH; //train direction served by this location
	protected int _length = 0;				//length of all tracks at this location
	protected int _usedLength = 0;			//length of track filled by cars and engines 
	protected String _comment = "";
	protected boolean _switchList = true;	//when true print switchlist for this location 
	protected Hashtable _subLocationHashTable = new Hashtable();   // stores sublocations 
	
	public static final int NORMAL = 1;		// ops mode for this location
	public static final int STAGING = 2;
	
	public static final int EAST = 1;		// train direction serviced by this location
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;
	
	// For property change
	public static final String YARDLISTLENGTH = "yardListLength";
	public static final String SIDINGLISTLENGTH = "sidingListLength";
	public static final String INTERCHANGELISTLENGTH = "sidingListLength";
	public static final String STAGINGLISTLENGTH = "sidingListLength";
	public static final String CARTYPES = "carTypes";
	public static final String TRAINDIRECTION = "trainDirection";
	public static final String LENGTH = "length";
	public static final String NAME = "name";
	public static final String SWITCHLIST = "switchList";
	public static final String DISPOSE = "dispose";
	

	public Location(String id, String name) {
		log.debug("New location " + name + " " + id);
		_name = name;
		_id = id;
	}

	public String getId() {
		return _id;
	}

	public void setName(String name) {
		String old = _name;
		_name = name;
		if (!old.equals(name)){
			firePropertyChange(NAME, old, name);
		}
	}
	
	// for combo boxes
	public String toString(){
		return _name;
	}

	public String getName() {
		return _name;
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
	
	public void setUsedLength(int length) {
		int old = _usedLength;
		_usedLength = length;
		if (old != length)
			firePropertyChange("usedLength", Integer.toString(old), Integer.toString(length));
	}
	
	public int getUsedLength() {
		return _usedLength;
	}
	
	public void setLocationOps(int ops){
		int old = _locationOps;
		_locationOps = ops;
		if (old != ops)
			firePropertyChange("locationOps", Integer.toString(old), Integer.toString(ops));
	}
	
	public int getLocationOps() {
		return _locationOps;
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
	
	public void setNumberCars(int cars) {
		int old = _numberCars;
		_numberCars = cars;
		if (old != cars)
			firePropertyChange("numberCars", Integer.toString(old), Integer.toString(cars));
	}
	
	public int getNumberCars() {
		return _numberCars;
	}
	
	public void setSwitchList(boolean switchList) {
		boolean old = _switchList;
		_switchList = switchList;
		if (old != switchList)
			firePropertyChange(SWITCHLIST, old?"true":"false", switchList?"true":"false");
	}
	
	public boolean getSwitchList() {
		return _switchList;
	}
	
	
	/**
	 * Adds a car to a specific location.  
	 * @param car
	 */	
	public void addCar (Car car){
   		int numberOfCars = getNumberCars();
		numberOfCars++;
		setNumberCars(numberOfCars);
		setUsedLength(getUsedLength() + Integer.parseInt(car.getLength())+ car.COUPLER);
	}
	
	public void deleteCar (Car car){
   		int numberOfCars = getNumberCars();
		numberOfCars--;
		setNumberCars(numberOfCars);
		setUsedLength(getUsedLength() - (Integer.parseInt(car.getLength())+ car.COUPLER));
	}

	public void addPickupCar() {
		int old = _pickupCars;
		_pickupCars++;
		firePropertyChange("pickupCars", Integer.toString(old), Integer.toString(_pickupCars));
	}
	
	public void deletePickupCar() {
		int old = _pickupCars;
		_pickupCars--;
		firePropertyChange("pickupCars", Integer.toString(old), Integer.toString(_pickupCars));
	}
	
	public int getPickupCars() {
		return _pickupCars;
	}

	public void addDropCar() {
		int old = _dropCars;
		_dropCars++;
		firePropertyChange("dropCars", Integer.toString(old), Integer.toString(_dropCars));
	}
	
	public void deleteDropCar() {
		int old = _dropCars;
		_dropCars--;
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
	
    List list = new ArrayList();
    
    private String[] getTypeNames(){
      	String[] types = new String[list.size()];
     	for (int i=0; i<list.size(); i++)
     		types[i] = (String)list.get(i);
   		return types;
    }
    
    private void setTypeNames(String[] types){
    	if (types.length == 0) return;
    	jmri.util.StringUtil.sort(types);
 		for (int i=0; i<types.length; i++)
 			list.add(types[i]);
    }
    
    public void addTypeName(String type){
    	// insert at start of list, sort later
    	if (list.contains(type))
    		return;
    	list.add(0,type);
    	log.debug("location ("+getName()+") add car type "+type);
    	firePropertyChange (CARTYPES, null, LENGTH);
    }
    
    public void deleteTypeName(String type){
    	list.remove(type);
    	log.debug("location ("+getName()+") delete car type "+type);
     	firePropertyChange (CARTYPES, null, LENGTH);
     }
    
    public boolean acceptsTypeName(String type){
    	return list.contains(type);
    }
  
	public SecondaryLocation addSecondaryLocation (String name, String type){
		SecondaryLocation sl = getSecondaryLocationByName(name, type);
		if (sl == null){
			_IdNumber++;
			String id = _id + "s"+ Integer.toString(_IdNumber);
			log.debug("adding new "+ type +" to "+getName()+ " id: " + id);
	   		sl = new SecondaryLocation(id, name, type);
	   		Integer old = new Integer(_subLocationHashTable.size());
    		_subLocationHashTable.put(sl.getId(), sl);
    		setLength(sl.getLength() + getLength());
    		if(type.equals(SecondaryLocation.YARD))
    			firePropertyChange(YARDLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    		if(type.equals(SecondaryLocation.SIDING))
    			firePropertyChange(SIDINGLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    		if(type.equals(SecondaryLocation.INTERCHANGE))
    			firePropertyChange(INTERCHANGELISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    		if(type.equals(SecondaryLocation.STAGING))
    			firePropertyChange(STAGINGLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
            // listen for change in track length
            sl.addPropertyChangeListener(this);
		}
		return sl;
	}
	
   /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(SecondaryLocation sl) {
    	Integer old = new Integer(_subLocationHashTable.size());
        _subLocationHashTable.put(sl.getId(), sl);
        // add to the locations's available track length
        setLength(getLength() + sl.getLength());
        // find last id created
        String[] getId = sl.getId().split("s");
        int id = Integer.parseInt(getId[1]);
        if (id > _IdNumber)
        	_IdNumber = id;
        String type = sl.getLocType();
        if (type.equals(SecondaryLocation.YARD))
        	firePropertyChange(YARDLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    	if(type.equals(SecondaryLocation.SIDING))
			firePropertyChange(SIDINGLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
		if(type.equals(SecondaryLocation.INTERCHANGE))
			firePropertyChange(INTERCHANGELISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    	if(type.equals(SecondaryLocation.STAGING))
			firePropertyChange(STAGINGLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
        // listen for name and state changes to forward
        sl.addPropertyChangeListener(this);
    }

	
    public void deleteSecondaryLocation (SecondaryLocation sl){
    	if (sl != null){
    		sl.removePropertyChangeListener(this);
    		// subtract from the locations's available track length
            setLength(getLength() - sl.getLength());
    		String type = sl.getLocType();
    		String id = sl.getId();
    		sl.dispose();
    		Integer old = new Integer(_subLocationHashTable.size());
    		_subLocationHashTable.remove(id);
            if (type.equals(SecondaryLocation.YARD))
            	firePropertyChange(YARDLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
        	if(type.equals(SecondaryLocation.SIDING))
    			firePropertyChange(SIDINGLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    		if(type.equals(SecondaryLocation.INTERCHANGE))
    			firePropertyChange(INTERCHANGELISTLENGTH, old, new Integer(_subLocationHashTable.size()));
        	if(type.equals(SecondaryLocation.STAGING))
    			firePropertyChange(STAGINGLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    	}
    }
    
	/**
	 * Get secondary location by name and type
	 * @param name
	 * @return secondary location
	 */
    
    public SecondaryLocation getSecondaryLocationByName(String name, String type) {
    	SecondaryLocation sl;
    	Enumeration en =_subLocationHashTable.elements();
    	for (int i = 0; i < _subLocationHashTable.size(); i++){
    		sl = (SecondaryLocation)en.nextElement();
    		if (sl.getName().equals(name) && sl.getLocType().equals(type))
    			return sl;
      	}
        return null;
    }
    
    public SecondaryLocation getSecondaryLocationById (String id){
    	return (SecondaryLocation)_subLocationHashTable.get(id);
    }
    
    private List getSecondaryLocationsByIdList() {
        String[] arr = new String[_subLocationHashTable.size()];
        List out = new ArrayList();
        Enumeration en = _subLocationHashTable.keys();
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
     * Sort ids by secondary location name.  Returns a list of a given location type
     * if type is not null, otherwise all secondary locations are returned.  
     * @return list of secondary location ids ordered by name
     */
    public List getSecondaryLocationsByNameList(String type) {
		// first get id list
		List sortList = getSecondaryLocationsByIdList();
		// now re-sort
		List out = new ArrayList();
		String locName = "";
		boolean locAdded = false;
		SecondaryLocation sl;
		SecondaryLocation slout;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			sl = getSecondaryLocationById((String) sortList.get(i));
			locName = sl.getName();
			for (int j = 0; j < out.size(); j++) {
				slout = getSecondaryLocationById((String) out.get(j));
				String outLocName = slout.getName();
				if (locName.compareToIgnoreCase(outLocName) < 0 && (type !=null && sl.getLocType().equals(type) || type == null)) {
					out.add(j, sortList.get(i));
					locAdded = true;
					break;
				}
			}
			if (!locAdded && (type !=null && sl.getLocType().equals(type) || type == null)) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}
    
    /**
     * Sort ids by secondary location moves.  Returns a list of a given locaation type
     * if type is not null, otherwise all secondary locations are returned.  
     * @return list of secondary location ids ordered by moves
     */
    public List getSecondaryLocationsByMovesList(String type) {
		// first get id list
		List sortList = getSecondaryLocationsByIdList();
		// now re-sort
		List out = new ArrayList();
		boolean locAdded = false;
		SecondaryLocation sl;
		SecondaryLocation slout;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			sl = getSecondaryLocationById((String) sortList.get(i));
			int moves = sl.getMoves();
			for (int j = 0; j < out.size(); j++) {
				slout = getSecondaryLocationById((String) out.get(j));
				int outLocMoves = slout.getMoves();
				if (moves < outLocMoves && (type !=null && sl.getLocType().equals(type) || type == null)) {
					out.add(j, sortList.get(i));
					locAdded = true;
					break;
				}
			}
			if (!locAdded && (type !=null && sl.getLocType().equals(type) || type == null)) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}
      
    /**
     * returns a JComboBox with all of the secondary locations for
     * this location.
     * @param box
     */
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
    	List sls = getSecondaryLocationsByNameList(null);
		for (int i = 0; i < sls.size(); i++){
			String Id = (String)sls.get(i);
			SecondaryLocation sl = getSecondaryLocationById(Id);
			box.addItem(sl);
		}
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
    public Location(org.jdom.Element e) {
//        if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
        if ((a = e.getAttribute("id")) != null )  _id = a.getValue();
        else log.warn("no id attribute in location element when reading operations");
        if ((a = e.getAttribute("name")) != null )  _name = a.getValue();
        if ((a = e.getAttribute("ops")) != null )  _locationOps = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("dir")) != null )  _trainDir = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("switchList")) != null )  _switchList = (a.getValue().equals("true"));
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
        if ((a = e.getAttribute("carTypes")) != null ) {
        	String names = a.getValue();
           	String[] Types = names.split("%%");
//        	if (log.isDebugEnabled()) log.debug("Car types: "+names);
        	setTypeNames(Types);
        }
        if (e.getChildren("secondary") != null) {
            List l = e.getChildren("secondary");
            if (log.isDebugEnabled()) log.debug("location ("+getName()+") has "+l.size()+" secondary locations");
            for (int i=0; i<l.size(); i++) {
                register(new SecondaryLocation((Element)l.get(i)));
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
        org.jdom.Element e = new org.jdom.Element("location");
        e.setAttribute("id", getId());
        e.setAttribute("name", getName());
        e.setAttribute("ops", Integer.toString(getLocationOps()));
        e.setAttribute("dir", Integer.toString(getTrainDirections()));
        e.setAttribute("switchList", getSwitchList()?"true":"false");
        // build list of car types for this location
        String[] types = getTypeNames();
        String typeNames ="";
        for (int i=0; i<types.length; i++){
        	typeNames = typeNames + types[i]+"%%";
        }
        e.setAttribute("carTypes", typeNames);
        
        e.setAttribute("comment", getComment());
        
        List sls = getSecondaryLocationsByIdList();
        for (int i=0; i<sls.size(); i++) {
        	String id = (String)sls.get(i);
        	SecondaryLocation sl = getSecondaryLocationById(id);
	            e.addContent(sl.store());
        }
  
        return e;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled())
    		log.debug("location (" + getName() + ") sees property change: "
    				+ e.getPropertyName() + " from secondary (" +e.getSource()+") old: " + e.getOldValue() + " new: "
    				+ e.getNewValue());
    	// update length of tracks if secondary location track length changes
    	if(e.getPropertyName().equals("length")){
    		setLength(getLength() - Integer.parseInt((String)e.getOldValue()) + Integer.parseInt((String)e.getNewValue()));
    	}
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
			.getInstance(Location.class.getName());

}
