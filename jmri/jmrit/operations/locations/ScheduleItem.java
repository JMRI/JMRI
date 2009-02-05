package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents a car type to be scheduled for a location
 * 
 * @author Daniel Boudreau Copyright (C) 2009
 * @version             $Revision: 1.5 $
 */
public class ScheduleItem implements java.beans.PropertyChangeListener {

	protected String _id = "";
	protected int _sequenceId = 0;			// used to determine order in schedule
	protected String _type = "";			// the type of car
	protected String _road = "";			// the car road
	protected String _load ="";				// the car load requested
	protected String _ship ="";				// the car load shipped
	protected int _count = 1;				// the number of times this type of car must be dropped
	protected String _comment = "";
			
	public static final String NUMBER_CHANGED_PROPERTY = "number";
	public static final String TYPE_CHANGED_PROPERTY = "type";
	public static final String ROAD_CHANGED_PROPERTY = "road";
	public static final String LOAD_CHANGED_PROPERTY = "load";
	public static final String DISPOSE = "dispose";
	
	/**
	 * 
	 * @param id
	 * @param type car type to schedule
	 */
	public ScheduleItem(String id, String type) {
		log.debug("New Schedule Item " + type + " " + id);
		_type = type;
		_id = id;
	}

	public String getId() {
		return _id;
	}

	public String getType() {
		return _type;
	}
	
	public void setType(String type){
		String old = _type;
		_type = type;
		firePropertyChange (TYPE_CHANGED_PROPERTY, old, type);
	}
	
	public String getRoad() {
		return _road;
	}
	
	public void setRoad(String road){
		String old = _road;
		_road = road;
		firePropertyChange (ROAD_CHANGED_PROPERTY, old, road);
	}
	
	public void setLoad(String load){
		String old = _load;
		_load = load;
		firePropertyChange (LOAD_CHANGED_PROPERTY, old, load);
	}
	
	public String getLoad() {
		return _load;
	}
	
	public void setShip(String load){
		String old = _ship;
		_ship = load;
		firePropertyChange (LOAD_CHANGED_PROPERTY, old, load);
	}
	
	public String getShip() {
		return _ship;
	}
	
	public int getSequenceId(){
		return _sequenceId;
	}
	
	public void setSequenceId(int sequence) {
		// property change not needed
		_sequenceId = sequence;
	}
	
	public int getCount(){
		return _count;
	}
	
	public void setCount(int count){
		int old = _count;
		_count = count;
		firePropertyChange (NUMBER_CHANGED_PROPERTY, old, count);
	}
	
	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
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
    public ScheduleItem(org.jdom.Element e) {
        //if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
        if ((a = e.getAttribute("id")) != null )  _id = a.getValue();
        else log.warn("no id attribute in Schedule Item element when reading operations");
        if ((a = e.getAttribute("sequenceId")) != null )  _sequenceId = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("count")) != null )  _count = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("type")) != null )  _type = a.getValue();
        if ((a = e.getAttribute("road")) != null )  _road = a.getValue();
        if ((a = e.getAttribute("load")) != null )  _load = a.getValue();
        if ((a = e.getAttribute("ship")) != null )  _ship = a.getValue();
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
    	org.jdom.Element e = new org.jdom.Element("item");
    	e.setAttribute("id", getId());
    	e.setAttribute("sequenceId", Integer.toString(getSequenceId()));
    	e.setAttribute("count", Integer.toString(getCount()));
    	e.setAttribute("type", getType());
    	e.setAttribute("road", getRoad());
    	e.setAttribute("load", getLoad());
    	e.setAttribute("ship", getShip());  	
       	e.setAttribute("comment", getComment());
    	return e;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled())
    		log.debug("ScheduleItem ("+getType()+ ") id (" +getId()+ ") sees property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
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
			.getInstance(ScheduleItem.class.getName());

}
