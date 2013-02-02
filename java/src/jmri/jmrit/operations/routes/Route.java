package jmri.jmrit.operations.routes;

import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

import org.jdom.Element;

/**
 * Represents a route on the layout
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version             $Revision$
 */
public class Route implements java.beans.PropertyChangeListener {
	
	protected String _id = "";
	protected String _name = "";
	protected String _comment = "";
	
	//	 stores location names for this route
	protected Hashtable <String, RouteLocation> _routeHashTable = new Hashtable<String, RouteLocation>();   
	protected int _IdNumber = 0;			// each location in a route gets its own id
	protected int _sequenceNum = 0;			// each location has a unique sequence number

	public static final int EAST = 1;		// train direction 
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;
	
	public static final String LISTCHANGE_CHANGED_PROPERTY = "routeListChange"; // NOI18N
	public static final String DISPOSE = "dispose"; // NOI18N
	
	public static final String OKAY = Bundle.getMessage("Okay");
	public static final String ORPHAN = Bundle.getMessage("Orphan");
	public static final String ERROR = Bundle.getMessage("Error");
	

	public Route(String id, String name) {
		log.debug("New route " + name + " " + id);
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
			setDirtyAndFirePropertyChange("nameChange", old, name); // NOI18N
		}
	}
	
	// for combo boxes
	public String toString(){
		return _name;
	}

	public String getName() {
		return _name;
	}

	public void setComment(String comment) {
		String old = _comment;
		_comment = comment;
		if (!old.equals(comment)){
			setDirtyAndFirePropertyChange("commentChange", old, comment); // NOI18N
		}
	}

	public String getComment() {
		return _comment;
	}

    public void dispose(){
    	setDirtyAndFirePropertyChange (DISPOSE, null, DISPOSE);
    }
 
    /**
     * Adds a location to the end of this route
     * @param location
     * @return RouteLocation created for the location added
     */
    public RouteLocation addLocation (Location location){
    	_IdNumber++;
    	_sequenceNum++;
    	String id = _id + "r"+ Integer.toString(_IdNumber);
    	log.debug("adding new location to (" +getName()+ ") id: " + id);
    	RouteLocation rl = new RouteLocation(id, location);
    	rl.setSequenceId(_sequenceNum);
    	Integer old = Integer.valueOf(_routeHashTable.size());
    	_routeHashTable.put(rl.getId(), rl);

    	setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_routeHashTable.size()));
    	// listen for drop and pick up changes to forward
    	rl.addPropertyChangeListener(this);
    	return rl;
    }
    
    /**
     * Add a route location at a specific place (sequence) in the route
     * Allowable sequence numbers are 0 to max size of route;
     * @param location
     * @param sequence
     * @return route location
     */
    public RouteLocation addLocation (Location location, int sequence){
    	RouteLocation rl = addLocation (location);
    	if (sequence < 0 || sequence > _routeHashTable.size())
    		return rl;
    	for (int i = 0; i < _routeHashTable.size()- sequence; i++)
    		moveLocationUp(rl);
    	return rl;
    }
	
   /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(RouteLocation rl) {
    	Integer old = Integer.valueOf(_routeHashTable.size());
        _routeHashTable.put(rl.getId(), rl);

        // find last id created
        String[] getId = rl.getId().split("r");
        int id = Integer.parseInt(getId[1]);
        if (id > _IdNumber)
        	_IdNumber = id;
        // find highest sequence number
        if (rl.getSequenceId() > _sequenceNum)
        	_sequenceNum = rl.getSequenceId();
       	setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_routeHashTable.size()));
        // listen for drop and pick up changes to forward
        rl.addPropertyChangeListener(this);
    }

	/**
	 * Delete a RouteLocation
	 * @param rl
	 */
    public void deleteLocation (RouteLocation rl){
    	if (rl != null){
    		rl.removePropertyChangeListener(this);
    		// subtract from the locations's available track length
    		String id = rl.getId();
    		rl.dispose();
    		Integer old = Integer.valueOf(_routeHashTable.size());
    		_routeHashTable.remove(id);
    		resequenceIds();
           	setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_routeHashTable.size()));
     	}
    }
    
    /**
     * Reorder the location sequence numbers for this route
     */
    private void resequenceIds(){
    	List<String> l = getLocationsBySequenceList();
    	int i;
    	for (i=0; i<l.size(); i++){
    		RouteLocation rl = getLocationById(l.get(i));
    		rl.setSequenceId(i+1);	// start sequence numbers at 1
    	}
    	_sequenceNum = i;
    }
    
    /**
     * Get the first location in a route
     * @return the first route location
     */
    public RouteLocation getDepartsRouteLocation(){
    	List<String> list = getLocationsBySequenceList();
    	if (list.size()>0){
    		RouteLocation rl = getLocationById(list.get(0));
    		return rl;
    	}
    	return null;
    }
    
	/**
	 * Get location by name (gets last route location with name)
	 * @param name
	 * @return route location
	 */
    public RouteLocation getLastLocationByName(String name) {
    	List<String> routeSequenceList = getLocationsBySequenceList();
    	RouteLocation rl;
    	
    	for (int i = routeSequenceList.size()-1; i >= 0; i--){
    		rl = getLocationById(routeSequenceList.get(i));
    		if (rl.getName().equals(name))
    			return rl;
      	}
        return null;
    }
    
    /**
     * Get a RouteLocation by id
     * @param id
     * @return route location
     */
    public RouteLocation getLocationById (String id){
    	return _routeHashTable.get(id);
    }
    
    private List<String> getLocationsByIdList() {
        String[] arr = new String[_routeHashTable.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _routeHashTable.keys();
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
     * Get a list of RouteLocation ids sorted by route order  
     * @return list of RouteLocation ids ordered by sequence
     */
    public List<String> getLocationsBySequenceList() {
		// first get id list
		List<String> sortList = getLocationsByIdList();
		// now re-sort
		List<String> out = new ArrayList<String>();
		int locNum;
		boolean locAdded = false;
		RouteLocation rl;
		RouteLocation rlout;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			rl = getLocationById(sortList.get(i));
			locNum = rl.getSequenceId();
			for (int j = 0; j < out.size(); j++) {
				rlout = getLocationById(out.get(j));
				int outLocNum = rlout.getSequenceId();
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
     * Moves a RouteLocation earlier in the route by decrementing the
     * sequenceId for the RouteLocation
     * @param rl
     */
    public void moveLocationUp(RouteLocation rl){
    	int sequenceId = rl.getSequenceId();
    	sequenceId--;
    	if(sequenceId <= 0)
    		return;
    	rl.setSequenceId(sequenceId);
    	int searchId = sequenceId;
    	sequenceId++;
    	//now find and adjust the other location taken by this one
    	boolean found = false;
    	List<String> sortList = getLocationsByIdList();
    	RouteLocation rladjust;
    	while (!found){
    		for (int i = 0; i < sortList.size(); i++) {
    			rladjust = getLocationById(sortList.get(i));
    			if (rladjust.getSequenceId() == searchId && rladjust != rl){
    				rladjust.setSequenceId(sequenceId);
    				found = true;
    				break;
    			}
    		}
    		searchId--;
    		if (searchId < 1)
    			found = true;
    	}
    	setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, Integer.toString(sequenceId));
    }
    
    /**
     * Moves a RouteLocation later in the route by incrementing the
     * sequenceId for the RouteLocation
     * @param rl
     */
    public void moveLocationDown(RouteLocation rl){
    	int sequenceId = rl.getSequenceId();
    	sequenceId++;
    	if(sequenceId > _sequenceNum)
    		return;
    	rl.setSequenceId(sequenceId);
    	int searchId = sequenceId;
    	sequenceId--;
    	//now find and adjust the other location taken by this one
    	boolean found = false;
    	List<String> sortList = getLocationsByIdList();
    	RouteLocation rladjust;
    	while (!found){
    		for (int i = 0; i < sortList.size(); i++) {
    			rladjust = getLocationById(sortList.get(i));
    			if (rladjust.getSequenceId() == searchId && rladjust != rl){
    				rladjust.setSequenceId(sequenceId);
    				found = true;
    				break;
    			}
    		}
    		searchId++;
    		if (searchId > _sequenceNum)
    			found = true;
    	}
    	setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, Integer.toString(sequenceId));
    }
    
    /**
     * Gets the status of the route: OKAY ORPHAN ERROR
     * @return string with status of route.
     */
    public String getStatus(){
    	List<String> routeIds = getLocationsByIdList();
    	if(routeIds.size() == 0)
    		return ERROR;
    	for (int i=0; routeIds.size()>i; i++){
    		RouteLocation rl = getLocationById(routeIds.get(i));
    		if (rl.getName().equals(RouteLocation.DELETED))
    			return ERROR;
    	}
    	// check to see if this route is used by a train
		List<String> trains = TrainManager.instance().getTrainsByIdList();
		for (int i=0; i<trains.size(); i++){
			Train train = TrainManager.instance().getTrainById(trains.get(i));
			if (train.getRoute() == this)
				return OKAY;
		}
	   	return ORPHAN;
    }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
    	List<String> routeIds = getLocationsBySequenceList();
    	for (int i=0; i<routeIds.size(); i++){
    		RouteLocation rl = getLocationById(routeIds.get(i));
    		box.addItem(rl);
    	}
    	return box;
    }
    
    public void updateComboBox(JComboBox box){
       	box.removeAllItems();
    	box.addItem("");
       	List<String> routeIds = getLocationsBySequenceList();
    	for (int i=0; i<routeIds.size(); i++){
    		RouteLocation rl = getLocationById(routeIds.get(i));
    		box.addItem(rl);
    	}
    }

 	
   /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml
     *
     * @param e  Consist XML element
     */
    public Route(org.jdom.Element e) {
//        if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
        if ((a = e.getAttribute(Xml.ID)) != null )  _id = a.getValue();
        else log.warn("no id attribute in route element when reading operations");
        if ((a = e.getAttribute(Xml.NAME)) != null )  _name = a.getValue();
        if ((a = e.getAttribute(Xml.COMMENT)) != null )  _comment = a.getValue();
        if (e.getChildren(Xml.LOCATION) != null) {
        	@SuppressWarnings("unchecked")
            List<Element> l = e.getChildren(Xml.LOCATION);
            if (log.isDebugEnabled()) log.debug("route: "+getName()+" has "+l.size()+" locations");
            for (int i=0; i<l.size(); i++) {
                register(new RouteLocation(l.get(i)));
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-config.xml.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
        org.jdom.Element e = new org.jdom.Element(Xml.ROUTE);
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.NAME, getName());
        e.setAttribute(Xml.COMMENT, getComment());
        List<String> l = getLocationsBySequenceList();
        for (int i=0; i<l.size(); i++) {
        	String id = l.get(i);
        	RouteLocation rl = getLocationById(id);
	            e.addContent(rl.store());
        }
 
        return e;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled())
    		log.debug("route (" + getName() + ") sees property change: "
    				+ e.getPropertyName() + " from (" +e.getSource()+ ") old: " + e.getOldValue() + " new: " // NOI18N
    				+ e.getNewValue());
    	// forward drops, pick ups, train direction, and max moves as a list change
    	if (e.getPropertyName().equals(RouteLocation.DROP_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(RouteLocation.PICKUP_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(RouteLocation.TRAIN_DIRECTION_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(RouteLocation.MAXMOVES_CHANGED_PROPERTY)) {
			setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null,
					"RouteLocation"); // NOI18N
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

	protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
		RouteManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static Logger log = org.apache.log4j.Logger
	.getLogger(Route.class.getName());

}
