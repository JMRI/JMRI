package jmri.jmrit.operations.routes;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.LocationManager;

import org.jdom.Element;

/**
 * Represents a location in a route, a location can appear more
 * than once in a route.
 * 
 * @author Daniel Boudreau
 * @version             $Revision: 1.4 $
 */
public class RouteLocation implements java.beans.PropertyChangeListener {

	protected String _id = "";
	protected Location _location = null;
	protected Track _trackLocation = null;  // used for staging cars
	protected String _trainDir = (Setup.getTrainDirection()== Setup.EAST+Setup.WEST )?EAST:NORTH; 	//train direction when arriving at this location
	protected String _comment = "";
	protected int _maxTrainLength = Setup.getTrainLength();
	protected int _trainLength = 0;			// used during build
	protected int _maxCarMoves = Setup.getCarMoves();
	protected int _carMoves = 0;			// used during build
	protected int _trainWeight = 0;			// used during build
	protected int _sequenceId = 0;			// used to determine location order
	protected double _grade = 0;				// maximum grade between locations
	protected int _trainIconX = 0;			// the x & y coordinates for the train icon
	protected int _trainIconY = 0;
	
	public static final String EAST = "East";		// train directions
	public static final String WEST = "West";
	public static final String NORTH = "North";
	public static final String SOUTH = "South";
	
	public static final String DISPOSE = "dispose";
	private static final String DELETED = "<location deleted>";
	
	
	public RouteLocation(String id, Location location) {
		log.debug("New route location " + location.getName() + " " + id);
		_location = location;
		_id = id;
		// listen for name change or delete
		location.addPropertyChangeListener(this);
	}
	
	// for combo boxes
	public String toString(){
		if (_location != null)
			return _location.getName();
		else
			return DELETED;
	}

	public String getId() {
		return _id;
	}

	public String getName() {
		if (_location != null)
			return _location.getName();
		else
			return DELETED;
	}
	
	public int getSequenceId(){
		return _sequenceId;
	}
	
	public void setSequenceId(int sequence) {
		// property change not needed
//		int old = _sequenceId;
		_sequenceId = sequence;
//		if (old != sequence){
//			firePropertyChange("sequence", Integer.toString(old), Integer.toString(sequence));
//		}
	}
	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
	}

    
	public void setTrainDirection(String direction){
		String old = _trainDir;
		_trainDir = direction;
		if (old != direction)
			firePropertyChange("trainDirection", old, direction);
	}
	
	public String getTrainDirection(){
		return _trainDir;
	}
	
	public void setMaxTrainLength(int length){
		int old = _maxTrainLength;
		_maxTrainLength = length;
		if (old != length)
			firePropertyChange("maxTrainLength", Integer.toString(old), Integer.toString(length));
	}
	
	public int getMaxTrainLength(){
		return _maxTrainLength;
	}
	
	public void setTrainLength(int length){
		int old = _trainLength;
		_trainLength = length;
		if (old != length)
			firePropertyChange("trainLength", Integer.toString(old), Integer.toString(length));
	}
	
	public int getTrainLength(){
		return _trainLength;
	}
	
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
			firePropertyChange("maxCarMoves", Integer.toString(old), Integer.toString(moves));
	}
	
	public int getMaxCarMoves(){
		return _maxCarMoves;
	}
	
	public void setCarMoves(int moves){
		int old = _carMoves;
		_carMoves = moves;
		if (old != moves)
			firePropertyChange("carMoves", Integer.toString(old), Integer.toString(moves));
	}
	
	public int getCarMoves(){
		return _carMoves;
	}
	
	public void setGrade(double grade){
		double old = _grade;
		_grade = grade;
		if (old != grade)
			firePropertyChange("grade", Double.toString(old), Double.toString(grade));
	}
	
	public double getGrade(){
		return _grade;
	}
	
	public void setTrainIconX(int x){
		int old = _trainIconX;
		_trainIconX = x;
		if (old != x)
			firePropertyChange("trainIconX", Integer.toString(old), Integer.toString(x));
	}
	
	public int getTrainIconX(){
		return _trainIconX;
	}
	
	public void setTrainIconY(int y){
		int old = _trainIconY;
		_trainIconY = y;
		if (old != y)
			firePropertyChange("trainIconY", Integer.toString(old), Integer.toString(y));
	}
	
	public int getTrainIconY(){
		return _trainIconY;
	}
	
	public void setTrack(Track track){
		_trackLocation = track;
	}
	
	public Track getTrack(){
		return _trackLocation;
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
        else log.warn("no id attribute in track location element when reading operations");
        if ((a = e.getAttribute("name")) != null ){
        	_location = LocationManager.instance().getLocationByName(a.getValue());
        	if (_location != null)
        		_location.addPropertyChangeListener(this);
        }
        if ((a = e.getAttribute("trainDirection")) != null )  _trainDir = a.getValue();
        if ((a = e.getAttribute("maxTrainLength")) != null )  _maxTrainLength = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("grade")) != null )  _grade = Double.parseDouble(a.getValue());
        if ((a = e.getAttribute("maxCarMoves")) != null )  _maxCarMoves = Integer.parseInt(a.getValue());
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
    	e.setAttribute("sequenceId", Integer.toString(getSequenceId()));
    	e.setAttribute("trainDirection", getTrainDirection());
    	e.setAttribute("maxTrainLength", Integer.toString(getMaxTrainLength()));
    	e.setAttribute("grade", Double.toString(getGrade()));
       	e.setAttribute("maxCarMoves", Integer.toString(getMaxCarMoves()));
      	e.setAttribute("trainIconX", Integer.toString(getTrainIconX()));
      	e.setAttribute("trainIconY", Integer.toString(getTrainIconY()));
    	e.setAttribute("comment", getComment());

    	return e;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled())
    		log.debug("route location ("+getName()+ ") id (" +getId()+ ") sees property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
				+ e.getNewValue());
    	if (e.getPropertyName().equals(Location.DISPOSE)){
        	if (_location != null)
        		_location.removePropertyChangeListener(this);
    		_location = null;
    	}
    	// forward property name change
    	if (e.getPropertyName().equals(Location.NAME)){
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

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(RouteLocation.class.getName());

}
