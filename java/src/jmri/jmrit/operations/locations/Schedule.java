package jmri.jmrit.operations.locations;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import jmri.jmrit.operations.setup.Control;

import org.jdom.Element;

/**
 * Represents a car delivery schedule for a location
 * 
 * @author Daniel Boudreau Copyright (C) 2009, 2011
 * @version             $Revision$
 */
public class Schedule implements java.beans.PropertyChangeListener {

	protected String _id = "";
	protected String _name = "";
	protected String _comment = "";
	
	//	 stores ScheduleItems for this schedule
	protected Hashtable <String, ScheduleItem> _scheduleHashTable = new Hashtable<String, ScheduleItem>();   
	protected int _IdNumber = 0;			// each item in a schedule gets its own id
	protected int _sequenceNum = 0;			// each item has a unique sequence number
	
	public static final String LISTCHANGE_CHANGED_PROPERTY = "listChange";	// NOI18N
	public static final String DISPOSE = "dispose";	// NOI18N
	

	public Schedule(String id, String name){
		log.debug("New schedule (" + name + ") id: " + id);
		_name = name;
		_id = id;
	}

	public String getId() {
		return _id;
	}

	public void setName(String name){
		String old = _name;
		_name = name;
		if (!old.equals(name))
			firePropertyChange("ScheduleName", old, name);	// NOI18N
	}
	
	// for combo boxes
	public String toString(){
		return _name;
	}

	public String getName(){
		return _name;
	}
	
	public int getSize(){
		return _scheduleHashTable.size();
	}

	public void setComment(String comment){
		String old = _comment;
		_comment = comment;
		if (!old.equals(comment))
			firePropertyChange("ScheduleComment", old, comment);	// NOI18N
	}
	
	public String getComment(){
		return _comment;
	}

    public void dispose(){
    	firePropertyChange (DISPOSE, null, DISPOSE);
    }
 
    /**
     * Adds a car type to the end of this schedule
     * @param type
     * @return ScheduleItem created for the car type added
     */
    public ScheduleItem addItem (String type){
    	_IdNumber++;
    	_sequenceNum++;
    	String id = _id + "c"+ Integer.toString(_IdNumber);
    	log.debug("Adding new item to ("+getName()+ ") id: " + id);
    	ScheduleItem si = new ScheduleItem(id, type);
    	si.setSequenceId(_sequenceNum);
    	Integer old = Integer.valueOf(_scheduleHashTable.size());
    	_scheduleHashTable.put(si.getId(), si);

    	firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_scheduleHashTable.size()));
    	// listen for set out and pick up changes to forward
    	si.addPropertyChangeListener(this);
    	return si;
    }
    
    /**
     * Add a schedule item at a specific place (sequence) in the schedule
     * Allowable sequence numbers are 0 to max size of schedule;
     * @param item
     * @param sequence
     * @return schedule item
     */
    public ScheduleItem addItem (String item, int sequence){
    	ScheduleItem si = addItem (item);
    	if (sequence < 0 || sequence > _scheduleHashTable.size())
    		return si;
    	for (int i = 0; i < _scheduleHashTable.size()- sequence; i++)
    		moveItemUp(si);
    	return si;
    }
	
   /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(ScheduleItem si) {
    	Integer old = Integer.valueOf(_scheduleHashTable.size());
        _scheduleHashTable.put(si.getId(), si);

        // find last id created
        String[] getId = si.getId().split("c");
        int id = Integer.parseInt(getId[1]);
        if (id > _IdNumber)
        	_IdNumber = id;
        // find highest sequence number
        if (si.getSequenceId() > _sequenceNum)
        	_sequenceNum = si.getSequenceId();
       	firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_scheduleHashTable.size()));
        // listen for set out and pick up changes to forward
        si.addPropertyChangeListener(this);
    }

	/**
	 * Delete a ScheduleItem
	 * @param si
	 */
    public void deleteItem (ScheduleItem si){
    	if (si != null){
    		si.removePropertyChangeListener(this);
    		// subtract from the items's available track length
    		String id = si.getId();
    		si.dispose();
    		Integer old = Integer.valueOf(_scheduleHashTable.size());
    		_scheduleHashTable.remove(id);
    		resequenceIds();
           	firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_scheduleHashTable.size()));
     	}
    }
    
    /**
     * Reorder the item sequence numbers for this schedule
     */
    private void resequenceIds(){
    	List<String> l = getItemsBySequenceList();
    	int i;
    	for (i=0; i<l.size(); i++){
    		ScheduleItem si = getItemById(l.get(i));
    		si.setSequenceId(i+1);	// start sequence numbers at 1
    	}
    	_sequenceNum = i;
    }
    
	/**
	 * Get item by car type (gets last schedule item with this type)
	 * @param type
	 * @return schedule item
	 */
    public ScheduleItem getItemByType(String type) {
    	List<String> scheduleSequenceList = getItemsBySequenceList();
    	ScheduleItem si;
    	
    	for (int i = scheduleSequenceList.size()-1; i >= 0; i--){
    		si = getItemById(scheduleSequenceList.get(i));
    		if (si.getType().equals(type))
    			return si;
      	}
        return null;
    }
    
    /**
     * Get a ScheduleItem by id
     * @param id
     * @return schedule item
     */
    public ScheduleItem getItemById (String id){
    	return _scheduleHashTable.get(id);
    }
    
    private List<String> getItemsByIdList() {
        String[] arr = new String[_scheduleHashTable.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _scheduleHashTable.keys();
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
     * Get a list of ScheduleItem ids sorted by schedule order  
     * @return list of ScheduleItem ids ordered by sequence
     */
    public List<String> getItemsBySequenceList() {
		// first get id list
		List<String> sortList = getItemsByIdList();
		// now re-sort
		List<String> out = new ArrayList<String>();
		int locNum;
		boolean locAdded = false;
		ScheduleItem si;
		ScheduleItem siout;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			si = getItemById(sortList.get(i));
			locNum = si.getSequenceId();
			for (int j = 0; j < out.size(); j++) {
				siout = getItemById(out.get(j));
				int outLocNum = siout.getSequenceId();
				if (locNum < outLocNum) {
					out.add(j, sortList.get(i));
					locAdded = true;
					break;
				}
			}
			if (!locAdded) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}
    
    /**
     * Moves a ScheduleItem earlier in the schedule by decrementing the
     * sequenceId for the ScheduleItem
     * @param si
     */
    public void moveItemUp(ScheduleItem si){
    	int sequenceId = si.getSequenceId();
    	sequenceId--;
    	if(sequenceId <= 0)
    		return;
    	si.setSequenceId(sequenceId);
    	int searchId = sequenceId;
    	sequenceId++;
    	//now find and adjust the other item taken by this one
    	boolean found = false;
    	List<String> sortList = getItemsByIdList();
    	ScheduleItem siadjust;
    	while (!found){
    		for (int i = 0; i < sortList.size(); i++) {
    			siadjust = getItemById(sortList.get(i));
    			if (siadjust.getSequenceId() == searchId && siadjust != si){
    				siadjust.setSequenceId(sequenceId);
    				found = true;
    				break;
    			}
    		}
    		searchId--;
    		if (searchId < 1)
    			found = true;
    	}
    	firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, Integer.toString(sequenceId));
    }
    
    /**
     * Moves a ScheduleItem later in the schedule by incrementing the
     * sequenceId for the ScheduleItem
     * @param si
     */
    public void moveItemDown(ScheduleItem si){
    	int sequenceId = si.getSequenceId();
    	sequenceId++;
    	if(sequenceId > _sequenceNum)
    		return;
    	si.setSequenceId(sequenceId);
    	int searchId = sequenceId;
    	sequenceId--;
    	//now find and adjust the other item taken by this one
    	boolean found = false;
    	List<String> sortList = getItemsByIdList();
    	ScheduleItem siadjust;
    	while (!found){
    		for (int i = 0; i < sortList.size(); i++) {
    			siadjust = getItemById(sortList.get(i));
    			if (siadjust.getSequenceId() == searchId && siadjust != si){
    				siadjust.setSequenceId(sequenceId);
    				found = true;
    				break;
    			}
    		}
    		searchId++;
    		if (searchId > _sequenceNum)
    			found = true;
    	}
    	firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, Integer.toString(sequenceId));
    }

    static final String ID = "id";		// NOI18N
    static final String NAME = "name";	// NOI18N
    static final String COMMENT = "comment";	// NOI18N
 	
   /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml
     *
     * @param e  Consist XML element
     */
    public Schedule(org.jdom.Element e) {
//        if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
        if ((a = e.getAttribute(ID)) != null )  _id = a.getValue();
        else log.warn("no id attribute in schedule element when reading operations");
        if ((a = e.getAttribute(NAME)) != null )  _name = a.getValue();
        if ((a = e.getAttribute(COMMENT)) != null )  _comment = a.getValue();
        if (e.getChildren(ScheduleItem.ITEM) != null) {
        	@SuppressWarnings("unchecked")
            List<Element> l = e.getChildren(ScheduleItem.ITEM);
            if (log.isDebugEnabled()) log.debug("schedule: "+getName()+" has "+l.size()+" items");
            for (int i=0; i<l.size(); i++) {
                register(new ScheduleItem(l.get(i)));
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
        org.jdom.Element e = new org.jdom.Element("schedule");
        e.setAttribute(ID, getId());
        e.setAttribute(NAME, getName());
        e.setAttribute(COMMENT, getComment());
        List<String> l = getItemsBySequenceList();
        for (int i=0; i<l.size(); i++) {
        	String id = l.get(i);
        	ScheduleItem si = getItemById(id);
	            e.addContent(si.store());
        }
 
        return e;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled())
    		log.debug("schedule (" + getName() + ") sees property change: "
    				+ e.getPropertyName() + " from (" +e.getSource()+ ") old: " + e.getOldValue() + " new: "  // NOI18N
    				+ e.getNewValue());
    	// forward all schedule item changes
    	firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
    }

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String p, Object old, Object n) {
   	   	// set dirty
    	LocationManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Schedule.class.getName());

}
