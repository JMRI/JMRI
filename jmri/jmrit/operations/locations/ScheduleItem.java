package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Represents a car type to be scheduled for a location
 * 
 * @author Daniel Boudreau Copyright (C) 2009
 * @version             $Revision: 1.1 $
 */
public class ScheduleItem implements java.beans.PropertyChangeListener {

	protected String _id = "";
	protected int _sequenceId = 0;			// used to determine order in schedule
	protected String _type = "";			// the type of car
	protected String _comment = "";
			
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

	public String getName() {
		return _type;
	}
	
	public int getSequenceId(){
		return _sequenceId;
	}
	
	public void setSequenceId(int sequence) {
		// property change not needed
		_sequenceId = sequence;
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
       	e.setAttribute("comment", getComment());
    	return e;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled())
    		log.debug("route location ("+getName()+ ") id (" +getId()+ ") sees property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
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
