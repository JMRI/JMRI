package jmri.jmrit.operations.locations;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.setup.Control;

import org.jdom.Element;

/**
 * Represents a location on the layout
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.6 $
 */
public class Location implements java.beans.PropertyChangeListener {

	protected String _id = "";
	protected String _name = "";
	protected String _sortId = "";
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
	public static final String TYPES = "types";
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

	/**
	 * Set total length of all tracks for this location
	 * @param length
	 */
	public void setLength(int length) {
		int old = _length;
		_length = length;
		if (old != length)
			firePropertyChange("length", Integer.toString(old), Integer.toString(length));
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
			firePropertyChange("usedLength", Integer.toString(old), Integer.toString(length));
	}
	
	/**
	 * 
	 * @return The length of the track that is occupied by cars and engines
	 */
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
			firePropertyChange(SWITCHLIST, old?"true":"false", switchList?"true":"false");
	}
	
	public boolean getSwitchList() {
		return _switchList;
	}
	
	
	/**
	 * Adds rolling stock to a specific location.  
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
	 * 
	 * @return the number of cars and or engines that are scheduled for pickup at this
	 *         location.
	 */
	public int getPickupRSs() {
		return _pickupRS;
	}

	/**
	 * Increments the number of cars and or engines that will be droped off by trains at this
	 * location.
	 */
	public void addDropRS() {
		int old = _dropRS;
		_dropRS++;
		firePropertyChange("dropRS", Integer.toString(old), Integer.toString(_dropRS));
	}
	
	/**
	 * Decrements the number of cars and or engines that will be droped off by trains at this
	 * location.
	 */
	public void deleteDropRS() {
		int old = _dropRS;
		_dropRS--;
		firePropertyChange("dropRS", Integer.toString(old), Integer.toString(_dropRS));
	}
	
	public int getDropRSs() {
		return _dropRS;
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
    	firePropertyChange (TYPES, null, LENGTH);
    }
    
    public void deleteTypeName(String type){
    	list.remove(type);
    	log.debug("location ("+getName()+") delete rolling stock type "+type);
     	firePropertyChange (TYPES, null, LENGTH);
     }
    
    public boolean acceptsTypeName(String type){
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
		Track sl = getTrackByName(name, type);
		if (sl == null){
			_IdNumber++;
			String id = _id + "s"+ Integer.toString(_IdNumber);
			log.debug("adding new "+ type +" to "+getName()+ " id: " + id);
	   		sl = new Track(id, name, type);
	   		Integer old = new Integer(_subLocationHashTable.size());
    		_subLocationHashTable.put(sl.getId(), sl);
    		setLength(sl.getLength() + getLength());
    		if(type.equals(Track.YARD))
    			firePropertyChange(YARDLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    		if(type.equals(Track.SIDING))
    			firePropertyChange(SIDINGLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    		if(type.equals(Track.INTERCHANGE))
    			firePropertyChange(INTERCHANGELISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    		if(type.equals(Track.STAGING))
    			firePropertyChange(STAGINGLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
            // listen for change in track length
            sl.addPropertyChangeListener(this);
		}
		return sl;
	}
	
   /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(Track track) {
    	Integer old = new Integer(_subLocationHashTable.size());
        _subLocationHashTable.put(track.getId(), track);
        // add to the locations's available track length
        setLength(getLength() + track.getLength());
        // find last id created
        String[] getId = track.getId().split("s");
        int id = Integer.parseInt(getId[1]);
        if (id > _IdNumber)
        	_IdNumber = id;
        String type = track.getLocType();
        if (type.equals(Track.YARD))
        	firePropertyChange(YARDLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    	if(type.equals(Track.SIDING))
			firePropertyChange(SIDINGLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
		if(type.equals(Track.INTERCHANGE))
			firePropertyChange(INTERCHANGELISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    	if(type.equals(Track.STAGING))
			firePropertyChange(STAGINGLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
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
    		Integer old = new Integer(_subLocationHashTable.size());
    		_subLocationHashTable.remove(id);
            if (type.equals(Track.YARD))
            	firePropertyChange(YARDLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
        	if(type.equals(Track.SIDING))
    			firePropertyChange(SIDINGLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    		if(type.equals(Track.INTERCHANGE))
    			firePropertyChange(INTERCHANGELISTLENGTH, old, new Integer(_subLocationHashTable.size()));
        	if(type.equals(Track.STAGING))
    			firePropertyChange(STAGINGLISTLENGTH, old, new Integer(_subLocationHashTable.size()));
    	}
    }
    
	/**
	 * Get track location by name and type
	 * @param name
	 * @return track location
	 */
    
    public Track getTrackByName(String name, String type) {
    	Track track;
    	Enumeration en =_subLocationHashTable.elements();
    	for (int i = 0; i < _subLocationHashTable.size(); i++){
    		track = (Track)en.nextElement();
    		if (track.getName().equals(name) && track.getLocType().equals(type))
    			return track;
      	}
        return null;
    }
    
    public Track getTrackById (String id){
    	return (Track)_subLocationHashTable.get(id);
    }
    
    private List getTracksByIdList() {
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
     * Sort ids by track location name.  Returns a list of a given location type
     * if type is not null, otherwise all track locations are returned.  
     * @return list of track location ids ordered by name
     */
    public List getTracksByNameList(String type) {
		// first get id list
		List sortList = getTracksByIdList();
		// now re-sort
		List out = new ArrayList();
		String locName = "";
		boolean locAdded = false;
		Track track;
		Track trackOut;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			track = getTrackById((String) sortList.get(i));
			locName = track.getName();
			for (int j = 0; j < out.size(); j++) {
				trackOut = getTrackById((String) out.get(j));
				String outLocName = trackOut.getName();
				if (locName.compareToIgnoreCase(outLocName) < 0 && (type !=null && track.getLocType().equals(type) || type == null)) {
					out.add(j, sortList.get(i));
					locAdded = true;
					break;
				}
			}
			if (!locAdded && (type !=null && track.getLocType().equals(type) || type == null)) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}
    
    /**
     * Sort ids by track location moves.  Returns a list of a given locaation type
     * if type is not null, otherwise all track locations are returned.  
     * @return list of track location ids ordered by moves
     */
    public List getTracksByMovesList(String type) {
		// first get id list
		List sortList = getTracksByIdList();
		// now re-sort
		List out = new ArrayList();
		boolean locAdded = false;
		Track track;
		Track trackOut;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			track = getTrackById((String) sortList.get(i));
			int moves = track.getMoves();
			for (int j = 0; j < out.size(); j++) {
				trackOut = getTrackById((String) out.get(j));
				int outLocMoves = trackOut.getMoves();
				if (moves < outLocMoves && (type !=null && track.getLocType().equals(type) || type == null)) {
					out.add(j, sortList.get(i));
					locAdded = true;
					break;
				}
			}
			if (!locAdded && (type !=null && track.getLocType().equals(type) || type == null)) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}
      
    /**
     * returns a JComboBox with all of the track locations for
     * this location.
     * @param box
     */
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
    	List tracks = getTracksByNameList(null);
		for (int i = 0; i < tracks.size(); i++){
			String Id = (String)tracks.get(i);
			Track track = getTrackById(Id);
			box.addItem(track);
		}
    }
  	    
    public void dispose(){
    	firePropertyChange (DISPOSE, null, DISPOSE);
    }
 	
   /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in operations-location.dtd
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
//        	if (log.isDebugEnabled()) log.debug("rolling stock types: "+names);
        	setTypeNames(Types);
        }
        // early version of operations called tracks "secondary"
        if (e.getChildren("secondary") != null) {
            List l = e.getChildren("secondary");
            if (log.isDebugEnabled()) log.debug("location ("+getName()+") has "+l.size()+" secondary locations");
            for (int i=0; i<l.size(); i++) {
                register(new Track((Element)l.get(i)));
            }
        }
        if (e.getChildren("track") != null) {
            List l = e.getChildren("track");
            if (log.isDebugEnabled()) log.debug("location ("+getName()+") has "+l.size()+" track locations");
            for (int i=0; i<l.size(); i++) {
                register(new Track((Element)l.get(i)));
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
        // build list of rolling stock types for this location
        String[] types = getTypeNames();
        String typeNames ="";
        for (int i=0; i<types.length; i++){
        	typeNames = typeNames + types[i]+"%%";
        }
        e.setAttribute("carTypes", typeNames);
        
        e.setAttribute("comment", getComment());
        
        List tracks = getTracksByIdList();
        for (int i=0; i<tracks.size(); i++) {
        	String id = (String)tracks.get(i);
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
