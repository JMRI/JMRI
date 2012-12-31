package jmri.jmrit.operations.routes;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Represents a location in a route, a location can appear more
 * than once in a route.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision$
 */
public class RouteLocation implements java.beans.PropertyChangeListener {
	
	protected String _id = "";
	protected Location _location = null;	// the location in the route
	protected String _locationId = "";		// the location's id
	protected int _trainDir = (Setup.getTrainDirection()== Setup.EAST+Setup.WEST )?EAST:NORTH; 	//train direction when arriving at this location
	protected int _maxTrainLength = Setup.getTrainLength();
	protected int _maxCarMoves = Setup.getCarMoves();
	protected boolean _drops = true;		// when true set outs allowed at this location
	protected boolean _pickups = true;		// when true pick ups allowed at this location
	protected int _sequenceId = 0;			// used to determine location order in route
	protected double _grade = 0;			// maximum grade between locations
	protected int _wait = 0;				// wait time at this location
	protected String _departureTime = "";	// departure time from this location
	protected int _trainIconX = 0;			// the x & y coordinates for the train icon
	protected int _trainIconY = 0;
	protected String _comment = "";
	
	protected int _carMoves = 0;			// number of moves at this location 
	protected int _trainWeight = 0;			// total car weight departing this location
	protected int _trainLength = 0;			// train length departing this location
	
	public static final int EAST = 1;		// train direction 
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;
	
	public static final String EAST_DIR = Setup.EAST_DIR;		// train directions text
	public static final String WEST_DIR = Setup.WEST_DIR;
	public static final String NORTH_DIR = Setup.NORTH_DIR;
	public static final String SOUTH_DIR = Setup.SOUTH_DIR;
	
	public static final String DISPOSE = "dispose";
	public static final String DELETED = Bundle.getString("locationDeleted");
	
	public static final String DROP_CHANGED_PROPERTY = "dropChange";
	public static final String PICKUP_CHANGED_PROPERTY = "pickupChange";
	public static final String MAXMOVES_CHANGED_PROPERTY = "maxMovesChange";
	public static final String TRAIN_DIRECTION_CHANGED_PROPERTY = "trainDirection";
	public static final String DEPARTURE_TIME_CHANGED_PROPERTY = "routeDepartureTime";
	
	public RouteLocation(String id, Location location) {
		log.debug("New route location (" + location.getName() + ") id: " + id);
		_location = location;
		_id = id;
		// listen for name change or delete
		location.addPropertyChangeListener(this);
	}
	
	// for combo boxes
	public String toString(){
		if (_location != null)
			return _location.getName();
		return DELETED;
	}

	public String getId() {
		return _id;
	}

	public String getName() {
		if (_location != null)
			return _location.getName();
		return DELETED;
	}
	
	private String getNameId(){
		if (_location != null)
			return _location.getId();
		return _locationId;
	}
	
	public Location getLocation(){
		return _location;
	}
	
	public int getSequenceId(){
		return _sequenceId;
	}
	
	public void setSequenceId(int sequence) {
		// property change not needed
		_sequenceId = sequence;
	}
	
	public void setComment(String comment) {
		String old = _comment;
		_comment = comment;
		if (!old.equals(_comment))
			setDirtyAndFirePropertyChange("RouteLocationComment", old, comment);
	}

	public String getComment() {
		return _comment;
	}

	public void setTrainDirection(int direction){
		int old = _trainDir;
		_trainDir = direction;
		if (old != direction)
			setDirtyAndFirePropertyChange(TRAIN_DIRECTION_CHANGED_PROPERTY, Integer.toString(old), Integer.toString(direction));
	}
	
	/**
	 * Gets the binary representation of the train's direction at this location
	 * 
	 * @return int representing train direction EAST WEST NORTH SOUTH
	 */
	public int getTrainDirection() {
		return _trainDir;
	}
	
	/**
	 * Gets the String representation of the train's direction at this location
	 * 
	 * @return String representing train direction at this location
	 */
	public String getTrainDirectionString(){
		return Setup.getDirectionString(getTrainDirection());
	}
	
	public void setMaxTrainLength(int length){
		int old = _maxTrainLength;
		_maxTrainLength = length;
		if (old != length)
			setDirtyAndFirePropertyChange("maxTrainLength", Integer.toString(old), Integer.toString(length));
	}
	
	public int getMaxTrainLength(){
		return _maxTrainLength;
	}
	
	/**
	 * Set the train length departing this location when building a train
	 * @param length
	 */
	public void setTrainLength(int length){
		int old = _trainLength;
		_trainLength = length;
		if (old != length)
			firePropertyChange("trainLength", Integer.toString(old), Integer.toString(length));
	}
	
	public int getTrainLength(){
		return _trainLength;
	}
	
	/**
	 * Set the train weight departing this location when building a train
	 * @param weight
	 */
	public void setTrainWeight(int weight){
		int old = _trainWeight;
		_trainWeight = weight;
		if (old != weight)
			firePropertyChange("trainWeight", Integer.toString(old), Integer.toString(weight));
	}
	
	public int getTrainWeight(){
		return _trainWeight;
	}
	
	public void setMaxCarMoves(int moves){
		int old = _maxCarMoves;
		_maxCarMoves = moves;
		if (old != moves)
			setDirtyAndFirePropertyChange(MAXMOVES_CHANGED_PROPERTY, Integer.toString(old), Integer.toString(moves));
	}
	
	/**
	 * Get the maximum number of moves for this location
	 * @return maximum number of moves
	 */
	public int getMaxCarMoves(){
		return _maxCarMoves;
	}
	
	/**
	 * When true allow car drops at this location
	 * @param drops when true drops allowed at this location
	 */
	public void setCanDrop (boolean drops){
		boolean old = _drops;
		_drops = drops;
		if (old != drops)
			setDirtyAndFirePropertyChange(DROP_CHANGED_PROPERTY, old?"true":"false", drops?"true":"false");
	}
	
	public boolean canDrop(){
		return _drops;
	}
	
	/**
	 * When true allow car pick ups at this location
	 * @param pickups when true pick ups allowed at this location
	 */
	public void setCanPickup (boolean pickups){
		boolean old = _pickups;
		_pickups = pickups;
		if (old != pickups)
			setDirtyAndFirePropertyChange(PICKUP_CHANGED_PROPERTY, old?"true":"false", pickups?"true":"false");

	}
	
	public boolean canPickup(){
		return _pickups;
	}
	
	/**
	 * Set the number of moves that this location has when building a train
	 * @param moves
	 */
	public void setCarMoves(int moves){
		int old = _carMoves;
		_carMoves = moves;
		if (old != moves)
			firePropertyChange("carMoves", Integer.toString(old), Integer.toString(moves));
	}
	
	public int getCarMoves(){
		return _carMoves;
	}
	
	public void setWait(int time){
		int old = _wait;
		_wait = time;
		if (old != time)
			setDirtyAndFirePropertyChange("waitTime", Integer.toString(old), Integer.toString(time));
	}
	
	public int getWait(){
		return _wait;
	}
	
	public void setDepartureTime(String time){
		String old = _departureTime;
		_departureTime = time;
		if (!old.equals(time))
			setDirtyAndFirePropertyChange(DEPARTURE_TIME_CHANGED_PROPERTY, old, time);
	}
	
	public String getDepartureTime(){
		return _departureTime;
	}
	
	public String getFormatedDepartureTime(){
		if (getDepartureTime().equals("") || !Setup.is12hrFormatEnabled())
			return _departureTime;
		String AM_PM = " AM";
		String[] time = getDepartureTime().split(":");
		int hour = Integer.parseInt(time[0]);
		if (hour >= 12){
			AM_PM = " PM";
			hour = hour - 12;
		}
		if (hour == 0)
			hour = 12;
		time[0] = Integer.toString(hour);
		return time[0]+":"+time[1]+AM_PM;
	}
	
	public void setGrade(double grade){
		double old = _grade;
		_grade = grade;
		if (old != grade)
			setDirtyAndFirePropertyChange("grade", Double.toString(old), Double.toString(grade));
	}
	
	public double getGrade(){
		return _grade;
	}
	
	public void setTrainIconX(int x){
		int old = _trainIconX;
		_trainIconX = x;
		if (old != x)
			setDirtyAndFirePropertyChange("trainIconX", Integer.toString(old), Integer.toString(x));
	}
	
	public int getTrainIconX(){
		return _trainIconX;
	}
	
	public void setTrainIconY(int y){
		int old = _trainIconY;
		_trainIconY = y;
		if (old != y)
			setDirtyAndFirePropertyChange("trainIconY", Integer.toString(old), Integer.toString(y));
	}
	
	public int getTrainIconY(){
		return _trainIconY;
	}
	
	/**
	 * Set the train icon panel coordinates to the location defaults.  Coordinates
	 * are dependent on the train's departure direction.
	 */
	public void setTrainIconCoordinates(){
		Location l = LocationManager.instance().getLocationByName(getName());
		if ((getTrainDirection() & Location.EAST) > 0){
			setTrainIconX(l.getTrainIconEast().x);
			setTrainIconY(l.getTrainIconEast().y);
		}
		if ((getTrainDirection() & Location.WEST) > 0){
			setTrainIconX(l.getTrainIconWest().x);
			setTrainIconY(l.getTrainIconWest().y);
		}
		if ((getTrainDirection() & Location.NORTH) > 0){
			setTrainIconX(l.getTrainIconNorth().x);
			setTrainIconY(l.getTrainIconNorth().y);
		}
		if ((getTrainDirection() & Location.SOUTH) > 0){
			setTrainIconX(l.getTrainIconSouth().x);
			setTrainIconY(l.getTrainIconSouth().y);
		}
	}

    public void dispose(){
    	if (_location != null)
    		_location.removePropertyChangeListener(this);
    	firePropertyChange (DISPOSE, null, DISPOSE);
    }
    
	
	   /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml
     *
     * @param e  Consist XML element
     */
    public RouteLocation(org.jdom.Element e) {
        //if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
        if ((a = e.getAttribute("id")) != null )  _id = a.getValue();
        else log.warn("no id attribute in route location element when reading operations");
        if ((a = e.getAttribute("locationId")) != null ){
        	_locationId = a.getValue();
        	_location = LocationManager.instance().getLocationById(a.getValue());
        	if (_location != null)
        		_location.addPropertyChangeListener(this);
        }
        // old way of storing a route location
        else if ((a = e.getAttribute("name")) != null ){
        	_location = LocationManager.instance().getLocationByName(a.getValue());
        	if (_location != null)
        		_location.addPropertyChangeListener(this);
        	// force rewrite of route file
        	RouteManagerXml.instance().setDirty(true);
        }
        if ((a = e.getAttribute("trainDirection")) != null ){
        	// early releases had text for train direction
        	if (Setup.getList().contains(a.getValue())){
        		_trainDir = Setup.getDirectionInt(a.getValue());
        		log.debug("found old train direction "+a.getValue()+" new direction "+_trainDir);
        	} else {
        		try {
        			_trainDir = Integer.parseInt(a.getValue());
        		} catch (NumberFormatException ee){
        			log.error("Route location ("+getName()+") direction ("+a.getValue()+") is unknown");
        		}
        		
        	}
        }
        if ((a = e.getAttribute("maxTrainLength")) != null )  _maxTrainLength = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("grade")) != null )  _grade = Double.parseDouble(a.getValue());
        if ((a = e.getAttribute("maxCarMoves")) != null )  _maxCarMoves = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("pickups")) != null ) _pickups = a.getValue().equals("yes");
        if ((a = e.getAttribute("drops")) != null ) _drops = a.getValue().equals("yes");
        if ((a = e.getAttribute("wait")) != null )  _wait = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("departTime")) != null )  _departureTime = a.getValue();
        if ((a = e.getAttribute("trainIconX")) != null )  _trainIconX = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("trainIconY")) != null )  _trainIconY = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("sequenceId")) != null )  _sequenceId = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
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
    	e.setAttribute("locationId", getNameId());
    	e.setAttribute("sequenceId", Integer.toString(getSequenceId()));
    	e.setAttribute("trainDirection", Integer.toString(getTrainDirection()));
    	e.setAttribute("maxTrainLength", Integer.toString(getMaxTrainLength()));
    	e.setAttribute("grade", Double.toString(getGrade()));
       	e.setAttribute("maxCarMoves", Integer.toString(getMaxCarMoves()));
       	e.setAttribute("pickups", canPickup()?"yes":"no");
       	e.setAttribute("drops", canDrop()?"yes":"no");
    	e.setAttribute("wait", Integer.toString(getWait()));
    	e.setAttribute("departTime", getDepartureTime());
       	e.setAttribute("trainIconX", Integer.toString(getTrainIconX()));
      	e.setAttribute("trainIconY", Integer.toString(getTrainIconY()));
    	e.setAttribute("comment", getComment());

    	return e;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled())
    		log.debug("route location ("+getName()+ ") id (" +getId()+ ") sees property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
				+ e.getNewValue());
    	if (e.getPropertyName().equals(Location.DISPOSE_CHANGED_PROPERTY)){
        	if (_location != null)
        		_location.removePropertyChangeListener(this);
    		_location = null;
    	}
    	// forward property name change
    	if (e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY)){
    		firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
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
	
	protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
		RouteManagerXml.instance().setDirty(true);
		firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RouteLocation.class.getName());

}
