package jmri.jmrit.operations.locations;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.setup.Control;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Represents a location on the layout
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.31 $
 */
public class Location implements java.beans.PropertyChangeListener {

	protected String _id = "";
	protected String _name = "";
	protected int _IdNumber = 0;
	protected int _numberRS = 0;
	protected int _pickupRS = 0;
	protected int _dropRS = 0;
	protected int _locationOps = NORMAL;	//type of operations at this location
	protected int _trainDir = EAST+WEST+NORTH+SOUTH; //train direction served by this location
	protected int _length = 0;				//length of all tracks at this location
	protected int _usedLength = 0;			//length of track filled by cars and engines 
	protected String _comment = "";
	protected boolean _switchList = true;	//when true print switchlist for this location 
	protected Point _trainIconEast = new Point();	//coordinates of east bound train icons
	protected Point _trainIconWest = new Point();
	protected Point _trainIconNorth = new Point();
	protected Point _trainIconSouth = new Point();
	protected Hashtable<String, Track> _trackHashTable = new Hashtable<String, Track>();
	
	public static final int NORMAL = 1;		// ops mode for this location
	public static final int STAGING = 2;
	
	public static final int EAST = 1;		// train direction serviced by this location
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;
	
	// For property change
	public static final String YARDLISTLENGTH_CHANGED_PROPERTY = "yardListLength";
	public static final String SIDINGLISTLENGTH_CHANGED_PROPERTY = "sidingListLength";
	public static final String INTERCHANGELISTLENGTH_CHANGED_PROPERTY = "sidingListLength";
	public static final String STAGINGLISTLENGTH_CHANGED_PROPERTY = "sidingListLength";
	public static final String TYPES_CHANGED_PROPERTY = "types";
	public static final String TRAINDIRECTION_CHANGED_PROPERTY = "trainDirection";
	public static final String LENGTH_CHANGED_PROPERTY = "length";
	public static final String USEDLENGTH_CHANGED_PROPERTY = "usedLength";
	public static final String NAME_CHANGED_PROPERTY = "name";
	public static final String SWITCHLIST_CHANGED_PROPERTY = "switchList";
	public static final String DISPOSE_CHANGED_PROPERTY = "dispose";

	public Location(String id, String name) {
		log.debug("New location " + name + " " + id);
		_name = name;
		_id = id;
		// a new location accepts all types
		setTypeNames(CarTypes.instance().getNames());
		setTypeNames(EngineTypes.instance().getNames());
	}

	public String getId() {
		return _id;
	}

	public void setName(String name) {
		String old = _name;
		_name = name;
		if (!old.equals(name)){
			firePropertyChange(NAME_CHANGED_PROPERTY, old, name);
		}
	}
	
	// for combo boxes
	public String toString(){
		return _name;
	}

	public String getName() {
		return _name;
	}

	/**
	 * Set total length of all tracks for this location
	 * @param length
	 */
	public void setLength(int length) {
		int old = _length;
		_length = length;
		if (old != length)
			firePropertyChange(LENGTH_CHANGED_PROPERTY, Integer.toString(old), Integer.toString(length));
	}

	/**
	 * 
	 * @return total length of all tracks for this location
	 */
	public int getLength() {
		return _length;
	}
	
	public void setUsedLength(int length) {
		int old = _usedLength;
		_usedLength = length;
		if (old != length)
			firePropertyChange(USEDLENGTH_CHANGED_PROPERTY, Integer.toString(old), Integer.toString(length));
	}
	
	/**
	 * 
	 * @return The length of the track that is occupied by cars and engines
	 */
	public int getUsedLength() {
		return _usedLength;
	}
	
	/**
	 * Set the operations mode for this location 
	 * @param ops NORMAL STAGING
	 */
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
			firePropertyChange(TRAINDIRECTION_CHANGED_PROPERTY, Integer.toString(old), Integer.toString(direction));
	}
	
	public int getTrainDirections(){
		return _trainDir;
	}
	
	/**
	 * Sets the number of cars and or engines on for this location
	 * @param number
	 */
	public void setNumberRS(int number) {
		int old = _numberRS;
		_numberRS = number;
		if (old != number)
			firePropertyChange("numberRS", Integer.toString(old), Integer.toString(number));
	}
	
	/**
	 * Gets the number of cars and engines at this location
	 * @return number of cars at this location
	 */
	public int getNumberRS() {
		return _numberRS;
	}
	
	/**
	 * When true, a switchlist is desired for this location.
	 * Used for preview and printing a manifest for a single location
	 * @param switchList
	 */
	public void setSwitchList(boolean switchList) {
		boolean old = _switchList;
		_switchList = switchList;
		if (old != switchList)
			firePropertyChange(SWITCHLIST_CHANGED_PROPERTY, old?"true":"false", switchList?"true":"false");
	}
	
	public boolean getSwitchList() {
		return _switchList;
	}
	
	public void setTrainIconEast(Point point){
		_trainIconEast = point;
	}
	
	public Point getTrainIconEast(){
		return _trainIconEast;
	}
	
	public void setTrainIconWest(Point point){
		_trainIconWest = point;
	}
	
	public Point getTrainIconWest(){
		return _trainIconWest;
	}
	
	public void setTrainIconNorth(Point point){
		_trainIconNorth = point;
	}
	
	public Point getTrainIconNorth(){
		return _trainIconNorth;
	}
	
	public void setTrainIconSouth(Point point){
		_trainIconSouth = point;
	}
	
	public Point getTrainIconSouth(){
		return _trainIconSouth;
	}
	
	
	/**
	 * Adds rolling stock to a specific location.  
	 * @param rs
	 */	
	public void addRS (RollingStock rs){
   		int numberOfRS = getNumberRS();
		numberOfRS++;
		setNumberRS(numberOfRS);
		setUsedLength(getUsedLength() + Integer.parseInt(rs.getLength())+ RollingStock.COUPLER);
	}
	
	public void deleteRS (RollingStock rs){
   		int numberOfRS = getNumberRS();
		numberOfRS--;
		setNumberRS(numberOfRS);
		setUsedLength(getUsedLength() - (Integer.parseInt(rs.getLength())+ RollingStock.COUPLER));
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
	
	/**
	 * Decrements the number of cars and or engines that will be picked up by a train
	 * at this location.
	 */
	public void deletePickupRS() {
		int old = _pickupRS;
		_pickupRS--;
		firePropertyChange("pickupRS", Integer.toString(old), Integer.toString(_pickupRS));
	}
	
	/**
	 * Increments the number of cars and or engines that will be dropped off by trains at this
	 * location.
	 */
	public void addDropRS() {
		int old = _dropRS;
		_dropRS++;
		firePropertyChange("dropRS", Integer.toString(old), Integer.toString(_dropRS));
	}
	
	/**
	 * Decrements the number of cars and or engines that will be dropped off by trains at this
	 * location.
	 */
	public void deleteDropRS() {
		int old = _dropRS;
		_dropRS--;
	   	// set dirty
    	LocationManagerXml.instance().setDirty(true);
		firePropertyChange("dropRS", Integer.toString(old), Integer.toString(_dropRS));
	}
	
	/**
	 * 
	 * @return the number of cars and engines that are scheduled for pickup at this
	 *         location.
	 */
	public int getPickupRS() {
		return _pickupRS;
	}

	/**
	 * 
	 * @return the number of cars and engines that are scheduled for drop at this
	 *         location.
	 */
	public int getDropRS() {
		return _dropRS;
	}


	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
	}
	
    List<String> list  = new ArrayList<String>();
    
    private String[] getTypeNames(){
      	String[] types = new String[list.size()];
     	for (int i=0; i<list.size(); i++)
     		types[i] = list.get(i);
   		return types;
    }
    
    private void setTypeNames(String[] types){
    	if (types.length == 0) return;
    	jmri.util.StringUtil.sort(types);
 		for (int i=0; i<types.length; i++)
 			list.add(types[i]);
    }
    
    /**
     * Adds the specific type of rolling stock to the will service list
     * @param type of rolling stock that location will service
     */
    public void addTypeName(String type){
    	// insert at start of list, sort later
    	if (list.contains(type))
    		return;
    	list.add(0,type);
    	log.debug("location ("+getName()+") add rolling stock type "+type);
    	firePropertyChange (TYPES_CHANGED_PROPERTY, list.size()-1, list.size());
    }
    
    public void deleteTypeName(String type){
    	if (!list.contains(type))
    		return;
    	list.remove(type);
    	log.debug("location ("+getName()+") delete rolling stock type "+type);
     	firePropertyChange (TYPES_CHANGED_PROPERTY, list.size()+1, list.size());
     }
    
    public boolean acceptsTypeName(String type){
    	if (!CarTypes.instance().containsName(type) && !EngineTypes.instance().containsName(type))
    		return false;
    	return list.contains(type);
    }
  
	/** 
	 * Adds a track to this location.  Valid tracks are
	 * sidings, yards, staging and interchange tracks.
	 *  @param name of track
	 * @param type of track
	 * @return Track
	 */
    public Track addTrack (String name, String type){
		Track track = getTrackByName(name, type);
		if (track == null){
			_IdNumber++;
			String id = _id + "s"+ Integer.toString(_IdNumber);
			log.debug("adding new "+ type +" to "+getName()+ " id: " + id);
	   		track = new Track(id, name, type);
	   		register(track);
 		}
		resetMoves();	// give all of the tracks equal weighting
		return track;
	}
    
 
   /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(Track track) {
    	Integer old = Integer.valueOf(_trackHashTable.size());
        _trackHashTable.put(track.getId(), track);
        // add to the locations's available track length
        setLength(getLength() + track.getLength());
        // find last id created
        String[] getId = track.getId().split("s");
        int id = Integer.parseInt(getId[1]);
        if (id > _IdNumber)
        	_IdNumber = id;
        String type = track.getLocType();
        if (type.equals(Track.YARD))
        	firePropertyChange(YARDLISTLENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_trackHashTable.size()));
    	if(type.equals(Track.SIDING))
			firePropertyChange(SIDINGLISTLENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_trackHashTable.size()));
		if(type.equals(Track.INTERCHANGE))
			firePropertyChange(INTERCHANGELISTLENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_trackHashTable.size()));
    	if(type.equals(Track.STAGING))
			firePropertyChange(STAGINGLISTLENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_trackHashTable.size()));
        // listen for name and state changes to forward
        track.addPropertyChangeListener(this);
    }

	
    public void deleteTrack (Track track){
    	if (track != null){
    		track.removePropertyChangeListener(this);
    		// subtract from the locations's available track length
            setLength(getLength() - track.getLength());
    		String type = track.getLocType();
    		String id = track.getId();
    		track.dispose();
    		Integer old = Integer.valueOf(_trackHashTable.size());
    		_trackHashTable.remove(id);
            if (type.equals(Track.YARD))
            	firePropertyChange(YARDLISTLENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_trackHashTable.size()));
        	if(type.equals(Track.SIDING))
    			firePropertyChange(SIDINGLISTLENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_trackHashTable.size()));
    		if(type.equals(Track.INTERCHANGE))
    			firePropertyChange(INTERCHANGELISTLENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_trackHashTable.size()));
        	if(type.equals(Track.STAGING))
    			firePropertyChange(STAGINGLISTLENGTH_CHANGED_PROPERTY, old, Integer.valueOf(_trackHashTable.size()));
    	}
    }
    
	/**
	 * Get track location by name and type
	 * @param name track's name
	 * @param type track type
	 * @return track location
	 */
    
    public Track getTrackByName(String name, String type) {
    	Track track;
    	Enumeration<Track> en =_trackHashTable.elements();
    	for (int i = 0; i < _trackHashTable.size(); i++){
    		track = en.nextElement();
    		if (type == null){
    			if (track.getName().equals(name))
    				return track;
    		} else if (track.getName().equals(name) && track.getLocType().equals(type))
    			return track;
    	}
    	return null;
    }
    
    public Track getTrackById (String id){
    	return _trackHashTable.get(id);
    }
    
    private List<String> getTracksByIdList() {
		String[] arr = new String[_trackHashTable.size()];
		List<String> out = new ArrayList<String>();
		Enumeration<String> en = _trackHashTable.keys();
		int i = 0;
		while (en.hasMoreElements()) {
			arr[i] = en.nextElement();
			i++;
		}
		jmri.util.StringUtil.sort(arr);
		for (i = 0; i < arr.length; i++)
			out.add(arr[i]);
		return out;
	}
    
    /**
	 * Sort ids by track location name. Returns a list of a given location type
	 * if type is not null, otherwise all track locations are returned.
	 * 
	 * @return list of track location ids ordered by name
	 */
    public List<String> getTracksByNameList(String type) {
		// first get id list
		List<String> sortList = getTracksByIdList();
		// now re-sort
		List<String> out = new ArrayList<String>();
		String locName = "";
		boolean locAdded = false;
		Track track;
		Track trackOut;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			track = getTrackById(sortList.get(i));
			locName = track.getName();
			for (int j = 0; j < out.size(); j++) {
				trackOut = getTrackById(out.get(j));
				String outLocName = trackOut.getName();
				if (locName.compareToIgnoreCase(outLocName) < 0
						&& (type != null && track.getLocType().equals(type) || type == null)) {
					out.add(j, sortList.get(i));
					locAdded = true;
					break;
				}
			}
			if (!locAdded
					&& (type != null && track.getLocType().equals(type) || type == null)) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}
    
    /**
     * Sort ids by track location moves.  Returns a list of a given location type
     * if type is not null, otherwise all track locations are returned.  
     * @return list of track location ids ordered by moves
     */
    public List<String> getTracksByMovesList(String type) {
		// first get id list
		List<String> sortList = getTracksByIdList();
		// now re-sort
		List<String> moveList = new ArrayList<String>();
		boolean locAdded = false;
		Track track;
		Track trackOut;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			track = getTrackById(sortList.get(i));
			int moves = track.getMoves();
			for (int j = 0; j < moveList.size(); j++) {
				trackOut = getTrackById(moveList.get(j));
				int outLocMoves = trackOut.getMoves();
				if (moves < outLocMoves
						&& (type != null && track.getLocType().equals(type) || type == null)) {
					moveList.add(j, sortList.get(i));
					locAdded = true;
					break;
				}
			}
			if (!locAdded
					&& (type != null && track.getLocType().equals(type) || type == null)) {
				moveList.add(sortList.get(i));
			}
		}
		// bias tracks with schedules
		List<String> out = new ArrayList<String>();
		for (int i=0; i<moveList.size(); i++){
			track = getTrackById(moveList.get(i));
			if (!track.getScheduleName().equals("")){
				out.add(moveList.get(i));
				moveList.remove(i);
				i--;
			}
		}
		for (int i=0; i<moveList.size(); i++){
			out.add(moveList.get(i));
		}
		return out;
	}
    
    public boolean isTrackAtLocation(Track track){
    	if (track == null)
    		return true;
    	return _trackHashTable.contains(track);  	
    }
    
    /**
     * Reset the move count for all tracks at this location
     */
    public void resetMoves(){
    	List<String> tracks = getTracksByIdList();
    	for (int i=0; i<tracks.size(); i++) {
    		Track track = getTrackById(tracks.get(i));
    		track.setMoves(0);
    	}
    }
	
      
    /**
     * Updates a JComboBox with all of the track locations for
     * this location.
     * @param box JComboBox to be updated.
     */
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
    	List<String> tracks = getTracksByNameList(null);
		for (int i = 0; i < tracks.size(); i++){
			box.addItem(getTrackById(tracks.get(i)));
		}
    }
    
    /**
     * Updates a JComboBox with tracks that can service the rolling stock.
     * @param box JComboBox to be updated.
     * @param rs Rolling Stock to be serviced
     * @param filter When true, remove tracks not able to service rs.
     * @param destination When true, the tracks are destinations for the rs.
     */
    public void updateComboBox(JComboBox box, RollingStock rs, boolean filter, boolean destination){
    	updateComboBox(box);
    	if (!filter || rs == null)
    		return;
       	List<String> tracks = getTracksByNameList(null);
		for (int i = 0; i < tracks.size(); i++){
			Track track = getTrackById(tracks.get(i));
			String status = "";
			if (destination){
				status = rs.testDestination(this, track);
			} else {
				status = rs.testLocation(this, track);
			}
			if (status.equals(RollingStock.OKAY) && (!destination || !track.getLocType().equals(Track.STAGING))){
				box.setSelectedItem(track);
				log.debug("Available track: "+track.getName()+" for location: "+getName());
			} else {
				box.removeItem(track);
			}
		}   	
    }
  	    
    public void dispose(){
    	firePropertyChange (DISPOSE_CHANGED_PROPERTY, null, "Dispose");
    }
 	
   /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in operations-locations.dtd
     *
     * @param e  Consist XML element
     */
    public Location(Element e) {
//        if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        Attribute a;
        if ((a = e.getAttribute("id")) != null )  _id = a.getValue();
        else log.warn("no id attribute in location element when reading operations");
        if ((a = e.getAttribute("name")) != null )  _name = a.getValue();
        if ((a = e.getAttribute("ops")) != null )  _locationOps = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("dir")) != null )  _trainDir = Integer.parseInt(a.getValue());
        if ((a = e.getAttribute("switchList")) != null )  _switchList = (a.getValue().equals("true"));
        // load train icon coordinates
        Attribute x;
        Attribute y;
        if ((x = e.getAttribute("eastTrainIconX")) != null && (y = e.getAttribute("eastTrainIconY"))!= null){
        	setTrainIconEast(new Point(Integer.parseInt(x.getValue()),Integer.parseInt(y.getValue())));
        }
        if ((x = e.getAttribute("westTrainIconX")) != null && (y = e.getAttribute("westTrainIconY"))!= null){
        	setTrainIconWest(new Point(Integer.parseInt(x.getValue()),Integer.parseInt(y.getValue())));
        }
        if ((x = e.getAttribute("northTrainIconX")) != null && (y = e.getAttribute("northTrainIconY"))!= null){
        	setTrainIconNorth(new Point(Integer.parseInt(x.getValue()),Integer.parseInt(y.getValue())));
        }
        if ((x = e.getAttribute("southTrainIconX")) != null && (y = e.getAttribute("southTrainIconY"))!= null){
        	setTrainIconSouth(new Point(Integer.parseInt(x.getValue()),Integer.parseInt(y.getValue())));
        }      
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
        if ((a = e.getAttribute("carTypes")) != null ) {
        	String names = a.getValue();
           	String[] Types = names.split("%%");
//        	if (log.isDebugEnabled()) log.debug("rolling stock types: "+names);
        	setTypeNames(Types);
        }
        // early version of operations called tracks "secondary"
        if (e.getChildren("secondary") != null) {
        	@SuppressWarnings("unchecked")
            List<Element> l = e.getChildren("secondary");
            if (log.isDebugEnabled()) log.debug("location ("+getName()+") has "+l.size()+" secondary locations");
            for (int i=0; i<l.size(); i++) {
                register(new Track(l.get(i)));
            }
        }
        if (e.getChildren("track") != null) {
        	@SuppressWarnings("unchecked")
            List<Element> l = e.getChildren("track");
            if (log.isDebugEnabled()) log.debug("location ("+getName()+") has "+l.size()+" tracks");
            for (int i=0; i<l.size(); i++) {
                register(new Track(l.get(i)));
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-locations.dtd.
     * @return Contents in a JDOM Element
     */
    public Element store() {
        Element e = new Element("location");
        e.setAttribute("id", getId());
        e.setAttribute("name", getName());
        e.setAttribute("ops", Integer.toString(getLocationOps()));
        e.setAttribute("dir", Integer.toString(getTrainDirections()));
        e.setAttribute("switchList", getSwitchList()?"true":"false");
        if (!getTrainIconEast().equals(new Point())){
        	e.setAttribute("eastTrainIconX", Integer.toString(getTrainIconEast().x));
        	e.setAttribute("eastTrainIconY", Integer.toString(getTrainIconEast().y));
        }
        if (!getTrainIconWest().equals(new Point())){
        	e.setAttribute("westTrainIconX", Integer.toString(getTrainIconWest().x));
        	e.setAttribute("westTrainIconY", Integer.toString(getTrainIconWest().y));
        }
        if (!getTrainIconNorth().equals(new Point())){
        	e.setAttribute("northTrainIconX", Integer.toString(getTrainIconNorth().x));
        	e.setAttribute("northTrainIconY", Integer.toString(getTrainIconNorth().y));
        }
        if (!getTrainIconSouth().equals(new Point())){
        	e.setAttribute("southTrainIconX", Integer.toString(getTrainIconSouth().x));
        	e.setAttribute("southTrainIconY", Integer.toString(getTrainIconSouth().y));
        }
        // build list of rolling stock types for this location
        String[] types = getTypeNames();
        CarTypes ct = CarTypes.instance();
        EngineTypes et = EngineTypes.instance();
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<types.length; i++){
    		// remove types that have been deleted by user
    		if (ct.containsName(types[i]) || et.containsName(types[i]))
    			buf.append(types[i]+"%%");
        }
        e.setAttribute("carTypes", buf.toString());
        
        e.setAttribute("comment", getComment());
        
        List<String> tracks = getTracksByIdList();
        for (int i=0; i<tracks.size(); i++) {
        	String id = tracks.get(i);
        	Track track = getTrackById(id);
	            e.addContent(track.store());
        }
  
        return e;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled())
    		log.debug("location (" + getName() + ") sees property change: "
    				+ e.getPropertyName() + " from track (" +e.getSource()+") old: " + e.getOldValue() + " new: "
    				+ e.getNewValue());
    	// update length of tracks at this location if track length changes
    	if(e.getPropertyName().equals(Track.LENGTH_CHANGED_PROPERTY)){
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

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(Location.class.getName());

}
