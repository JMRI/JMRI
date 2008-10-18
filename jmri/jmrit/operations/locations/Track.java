package jmri.jmrit.operations.locations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.routes.Route;

import org.jdom.Element;

/**
 * Represents a location (track) on the layout
 * Can be a siding, yard, staging, or interchange track.
 * 
 * @author Daniel Boudreau
 * @version             $Revision: 1.4 $
 */
public class Track implements java.beans.PropertyChangeListener {

	protected String _id = "";
	protected String _name = "";
	protected String _locType = "";					// yard, siding or staging
	protected String _sortId = "";
	protected String _roadOption = ALLROADS;
	protected String _dropOption = ANY;
	protected String _pickupOption = ANY;
	protected int _trainDir = EAST+WEST+NORTH+SOUTH; //train direction served by this location
	protected int _numberRS = 0;
	protected int _pickupRS = 0;
	protected int _dropRS = 0;
	protected int _length = 0;				//length of tracks at this location
	protected int _reserved = 0;			//length of track reserved by trains
	protected int _usedLength = 0;			//length of track filled by cars and engines 
	protected int _moves = 0;
	protected String _comment = "";
	
	public static final String STAGING = "Staging";			// the four types of tracks
	public static final String INTERCHANGE = "Interchange";
	public static final String YARD = "Yard";
	public static final String SIDING = "Siding";
	
	public static final String DISPOSE = "dispose";
	
	public static final int EAST = 1;		// train direction serviced by this location
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;
	
	public static final String ALLROADS = "All";			// track accepts all roads
	public static final String INCLUDEROADS = "Include";	// track accepts only certain roads
	public static final String EXCLUDEROADS = "Exclude";	// track does not accept certain roads
	
	public static final String ANY = "Any";			// track accepts any train or route
	public static final String TRAINS = "trains";	// track only accepts certain trains
	public static final String ROUTES = "routes";	// track only accepts certain routes
	
	public Track(String id, String name, String type) {
		log.debug("New track " + name + " " + id);
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
	
	/**
	 * Sets the number of cars and or engines on this track
	 * @param number
	 */
	public void setNumberRS(int number) {
		int old = _numberRS;
		_numberRS = number;
		if (old != number)
			firePropertyChange("numberRS", Integer.toString(old), Integer.toString(number));
	}
	
	public int getNumberRS() {
		return _numberRS;
	}
	
	/**
	 * Adds rolling stock to a specific track.  
	 * @param rs
	 */	
	public void addRS (RollingStock rs){
   		int numberOfRS = getNumberRS();
		numberOfRS++;
		setNumberRS(numberOfRS);
		setUsedLength(getUsedLength() + Integer.parseInt(rs.getLength())+ rs.COUPLER);
	}
	
	public void deleteRS (RollingStock rs){
   		int numberOfRS = getNumberRS();
		numberOfRS--;
		setNumberRS(numberOfRS);
		setUsedLength(getUsedLength() - (Integer.parseInt(rs.getLength())+ rs.COUPLER));
	}

	/**
	 * Increments the number of cars and or engines that will be picked up by a train
	 * at this location.
	 */
	public void addPickupRS() {
		int old = _pickupRS;
		_pickupRS++;
		firePropertyChange("pickupRS", Integer.toString(old), Integer.toString(_pickupRS));
	}
	
	public void deletePickupRS() {
		int old = _pickupRS;
		_pickupRS--;
		firePropertyChange("pickupRS", Integer.toString(old), Integer.toString(_pickupRS));
	}
	
	/**
	 * 
	 * @return the number of cars and or engines that are scheduled for pickup at this
	 *         location.
	 */
	public int getPickupRS() {
		return _pickupRS;
	}

	public int getDropRS() {
		return _dropRS;
	}
	
	public void addDropRS(RollingStock rs) {
		int old = _dropRS;
		_dropRS++;
		_moves++;
		int reserved = getReserved() + Integer.parseInt(rs.getLength()) + rs.COUPLER;
		setReserved(reserved);
		firePropertyChange("dropRS", Integer.toString(old), Integer.toString(_dropRS));
	}
	
	public void deleteDropRS(RollingStock rs) {
		int old = _dropRS;
		_dropRS--;
		int reserved = getReserved() - (Integer.parseInt(rs.getLength()) + rs.COUPLER);
		setReserved(reserved);
		firePropertyChange("dropRS", Integer.toString(old), Integer.toString(_dropRS));
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
    	log.debug("track " +getName()+ " add car type "+type);
    }
    
    public void deleteTypeName(String type){
    	_typeList.remove(type);
    	log.debug("track " +getName()+ " delete car type "+type);
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
    	log.debug("track " +getName()+ " add car road "+road);
    }
    
    public void deleteRoadName(String road){
    	_roadList.remove(road);
    	log.debug("track " +getName()+ " delete car road "+road);
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
    
    public String getDropOption (){
    	return _dropOption;
    }
    
    public void setDropOption (String option){
    	String old = _dropOption;
    	_dropOption = option;
    	if (!old.equals(option))
    		_dropList.clear();
    }
    
    public String getPickupOption (){
    	return _pickupOption;
    }
    
    public void setPickupOption (String option){
       	String old = _pickupOption;
       	_pickupOption = option;
    	if (!old.equals(option))
    		_pickupList.clear();
     }
    
    List _dropList = new ArrayList();
    
    public String[] getDropIds(){
      	String[] names = new String[_dropList.size()];
     	for (int i=0; i<_dropList.size(); i++)
     		names[i] = (String)_dropList.get(i);
     	if (_dropList.size() == 0)
     		return names;
     	jmri.util.StringUtil.sort(names);
   		return names;
    }
    
    private void setDropIds(String[] names){
    	if (names.length == 0) return;
    	jmri.util.StringUtil.sort(names);
 		for (int i=0; i<names.length; i++)
 			_dropList.add(names[i]);
    }
    
    public void addDropId(String name){
     	if (_dropList.contains(name))
    		return;
    	_dropList.add(name);
    	log.debug("track " +getName()+ " add drop "+name);
    }
    
    public void deleteDropId(String name){
    	_dropList.remove(name);
    	log.debug("track " +getName()+ " delete drop "+name);
     }
    
    public boolean acceptsDropTrain(Train train){
    	if (_dropOption.equals(ANY))
    		return true;
    	if (_dropOption.equals(TRAINS))
    		return containsDropId(train.getId());
    	else if (train.getRoute() == null)
    		return false;
   		return acceptsDropRoute(train.getRoute());
    }
    
    public boolean acceptsDropRoute(Route route){
      	if (_dropOption.equals(ANY))
    		return true;
      	if (_dropOption.equals(TRAINS))
      		return false;
      	return containsDropId(route.getId());
    }
    
    public boolean containsDropId(String name){
      		return _dropList.contains(name);
    }  
    
    List _pickupList = new ArrayList();
    
    public String[] getPickupIds(){
      	String[] names = new String[_pickupList.size()];
     	for (int i=0; i<_pickupList.size(); i++)
     		names[i] = (String)_pickupList.get(i);
     	if (_pickupList.size() == 0)
     		return names;
     	jmri.util.StringUtil.sort(names);
   		return names;
    }
    
    private void setPickupIds(String[] names){
    	if (names.length == 0) return;
    	jmri.util.StringUtil.sort(names);
 		for (int i=0; i<names.length; i++)
 			_pickupList.add(names[i]);
    }
    
    public void addPickupId(String name){
     	if (_pickupList.contains(name))
    		return;
    	_pickupList.add(name);
    	log.debug("track " +getName()+ " add pickup "+name);
    }
    
    public void deletePickupId(String name){
    	_pickupList.remove(name);
    	log.debug("track " +getName()+ " delete pickup "+name);
     }
    
    public boolean acceptsPickupTrain(Train train){
    	if (_pickupOption.equals(ANY))
    		return true;
    	if (_pickupOption.equals(TRAINS))
    		return containsPickupId(train.getId());
    	else if (train.getRoute() == null)
    		return false;
   		return acceptsPickupRoute(train.getRoute());
    }
    
    public boolean acceptsPickupRoute(Route route){
      	if (_pickupOption.equals(ANY))
    		return true;
      	if (_pickupOption.equals(TRAINS))
      		return false;
      	return containsPickupId(route.getId());
    }
    
    public boolean containsPickupId(String name){
      		return _pickupList.contains(name);
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
    public Track(org.jdom.Element e) {
        //if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
        if ((a = e.getAttribute("id")) != null )  _id = a.getValue();
        else log.warn("no id attribute in track element when reading operations");
        if ((a = e.getAttribute("name")) != null )  _name = a.getValue();
        if ((a = e.getAttribute("locType")) != null )  _locType = a.getValue();
        if ((a = e.getAttribute("length")) != null )  _length = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("moves")) != null )  _moves = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("dir")) != null )  _trainDir = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
        if ((a = e.getAttribute("carTypes")) != null ) {
        	String names = a.getValue();
           	String[] types = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("track (" +getName()+ ") accepts car types: "+ names);
        	setTypeNames(types);
        }
        if ((a = e.getAttribute("carRoadOperation")) != null )  _roadOption = a.getValue();
        if ((a = e.getAttribute("dropIds")) != null ) {
        	String names = a.getValue();
           	String[] ids = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("track (" +getName()+ ") has drop ids : "+ names);
        	setDropIds(ids);
        }
        if ((a = e.getAttribute("dropOption")) != null )  _dropOption = a.getValue();
        if ((a = e.getAttribute("pickupIds")) != null ) {
        	String names = a.getValue();
           	String[] ids = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("track (" +getName()+ ") has pickup ids : "+ names);
        	setPickupIds(ids);
        }
        if ((a = e.getAttribute("pickupOption")) != null )  _pickupOption = a.getValue();
        if ((a = e.getAttribute("carRoads")) != null ) {
        	String names = a.getValue();
           	String[] roads = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("track (" +getName()+ ") " +getRoadOption()+  " car roads: "+ names);
        	setRoadNames(roads);
        }
 
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-location.dtd.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
    	org.jdom.Element e = new org.jdom.Element("track");
    	e.setAttribute("id", getId());
    	e.setAttribute("name", getName());
    	e.setAttribute("locType", getLocType());
    	e.setAttribute("dir", Integer.toString(getTrainDirections()));
    	e.setAttribute("length", Integer.toString(getLength()));
    	e.setAttribute("moves", Integer.toString(getMoves()));
    	// build list of car types for this track
    	String[] types = getTypeNames();
    	String typeNames ="";
    	for (int i=0; i<types.length; i++){
    		typeNames = typeNames + types[i]+"%%";
    	}
    	e.setAttribute("carTypes", typeNames);
     	e.setAttribute("carRoadOperation", getRoadOption());
       	// build list of car roads for this track
    	String[] roads = getRoadNames();
    	String roadNames ="";
    	for (int i=0; i<roads.length; i++){
    		roadNames = roadNames + roads[i]+"%%";
    	}
    	e.setAttribute("carRoads", roadNames);
    	e.setAttribute("dropOption", getDropOption());
      	// build list of drop ids for this track
    	String[] dropIds = getDropIds();
    	String ids ="";
    	for (int i=0; i<dropIds.length; i++){
    		ids = ids + dropIds[i]+"%%";
    	}
    	e.setAttribute("dropIds", ids);
    	e.setAttribute("pickupOption", getPickupOption());
     	// build list of pickup ids for this track
    	String[] pickupIds = getPickupIds();
    	ids ="";
    	for (int i=0; i<pickupIds.length; i++){
    		ids = ids + pickupIds[i]+"%%";
    	}
    	e.setAttribute("pickupIds", ids);
    	e.setAttribute("comment", getComment());

    	return e;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	log.debug("track "+getName()+ " sees property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
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
			.getInstance(Track.class.getName());

}
