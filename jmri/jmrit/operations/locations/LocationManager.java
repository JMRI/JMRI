// LocationManager.java

package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import org.jdom.Element;

import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;


/**
 * Manages locations.
 * @author      Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008, 2009
 * @version	$Revision: 1.22 $
 */
public class LocationManager implements java.beans.PropertyChangeListener {
	public static final String LISTLENGTH_CHANGED_PROPERTY = "locationsListLength";
	
	// Edit Location frame attributes
	protected LocationEditFrame _locationEditFrame = null;
	protected Dimension _editFrameDimension = null;
	protected Point _editFramePosition = null;
    
	public LocationManager() {
		CarTypes.instance().addPropertyChangeListener(this);
		CarRoads.instance().addPropertyChangeListener(this);
		EngineTypes.instance().addPropertyChangeListener(this);
    }
    
	/** record the single instance **/
	private static LocationManager _instance = null;
	private int _id = 0;

	public static synchronized LocationManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("LocationManager creating instance");
			// create and load
			_instance = new LocationManager();
			OperationsXml.instance();					// load setup
			LocationManagerXml.instance();				// load locations
			RouteManagerXml.instance();					// load routes
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("LocationManager returns instance "+_instance);
		return _instance;
	}

	public void setLocationEditFrame(LocationEditFrame frame){
		_locationEditFrame = frame;
	}

	public Dimension getLocationEditFrameSize(){
		return _editFrameDimension;
	}

	public Point getLocationEditFramePosition(){
		return _editFramePosition;
	}

    public void dispose() {
        _locationHashTable.clear();
        _id = 0;
    }

    protected Hashtable<String, Location> _locationHashTable = new Hashtable<String, Location>();   // stores known Location instances by id

    /**
     * @return requested Location object or null if none exists
     */
     
    public Location getLocationByName(String name) {
    	Location l;
    	Enumeration<Location> en =_locationHashTable.elements();
    	for (int i = 0; i < _locationHashTable.size(); i++){
    		l = en.nextElement();
    		if (l.getName().equals(name))
    			return l;
      	}
        return null;
    }
    
    public Location getLocationById (String id){
    	return _locationHashTable.get(id);
    }
 
    /**
     * Finds an existing location or creates a new location if needed
     * requires location's name creates a unique id for this location
     * @param name
     * 
     * @return new location or existing location
     */
    public Location newLocation (String name){
    	Location location = getLocationByName(name);
    	if (location == null){
    		_id++;						
    		location = new Location(Integer.toString(_id), name);
    		Integer oldSize = Integer.valueOf(_locationHashTable.size());
    		_locationHashTable.put(location.getId(), location);
    		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_locationHashTable.size()));
    	}
    	return location;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(Location location) {
    	Integer oldSize = Integer.valueOf(_locationHashTable.size());
        _locationHashTable.put(location.getId(), location);
        // find last id created
        int id = Integer.parseInt(location.getId());
        if (id > _id)
        	_id = id;
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_locationHashTable.size()));
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     */
    public void deregister(Location location) {
    	if (location == null)
    		return;
        location.dispose();
        Integer oldSize = Integer.valueOf(_locationHashTable.size());
    	_locationHashTable.remove(location.getId());
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_locationHashTable.size()));
    }

    /**
     * Sort by location name
     * @return list of location ids ordered by name
     */
    public List<String> getLocationsByNameList() {
		// first get id list
		List<String> sortList = getList();
		// now re-sort
		List<String> out = new ArrayList<String>();
		String locName = "";
		boolean locAdded = false;
		Location l;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			l = getLocationById(sortList.get(i));
			locName = l.getName();
			for (int j = 0; j < out.size(); j++) {
				l = getLocationById(out.get(j));
				String outLocName = l.getName();
				if (locName.compareToIgnoreCase(outLocName) < 0) {
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
	 * Sort by location number, number can alpha numeric
	 * 
	 * @return list of location ids ordered by number
	 */
    public List<String> getLocationsByIdList() {
    	// first get id list
    	List<String> sortList = getList();
    	// now re-sort
    	List<String> out = new ArrayList<String>();
    	int locationNumber = 0;
    	boolean locationAdded = false;
    	Location l;
    	
    	for (int i=0; i<sortList.size(); i++){
    		locationAdded = false;
    		l = getLocationById (sortList.get(i));
    		try{
    			locationNumber = Integer.parseInt (l.getId());
    		}catch (NumberFormatException e) {
    			log.debug("location id number isn't a number");
    		}
    		for (int j=0; j<out.size(); j++ ){
    			l = getLocationById (out.get(j));
        		try{
        			int outLocationNumber = Integer.parseInt (l.getId());
        			if (locationNumber < outLocationNumber){
        				out.add(j, sortList.get(i));
        				locationAdded = true;
        				break;
        			}
        		}catch (NumberFormatException e) {
        			log.debug("list out id number isn't a number");
        		}
    		}
    		if (!locationAdded){
    			out.add( sortList.get(i));
    		}
    	}
        return out;
    }
    
    private List<String> getList() { 
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _locationHashTable.keys();
        String[] arr = new String[_locationHashTable.size()];
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
    	box.addItem("");
		List<String> locs = getLocationsByNameList();
		for (int i = 0; i < locs.size(); i++){
			String locId = locs.get(i);
			Location l = getLocationById(locId);
			box.addItem(l);
		}
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
		List<String> locs = getLocationsByNameList();
		for (int i = 0; i < locs.size(); i++){
			String locId = locs.get(i);
			Location l = getLocationById(locId);
			box.addItem(l);
		}
    }
    
    public void replaceType(String oldType, String newType){
		List<String> locs = getLocationsByIdList();
		for (int i=0; i<locs.size(); i++){
			Location loc = getLocationById(locs.get(i));
			if (loc.acceptsTypeName(oldType)){
				loc.deleteTypeName(oldType);
				loc.addTypeName(newType);
				// now adjust any track locations
				List<String> tracks = loc.getTracksByNameList(null);
				for (int j=0; j<tracks.size(); j++){
					Track track = loc.getTrackById(tracks.get(j));
					if (track.acceptsTypeName(oldType)){
						track.deleteTypeName(oldType);
						track.addTypeName(newType);
					}
				}
			}
		}
    }
    
	public void replaceRoad(String oldRoad, String newRoad){
		List<String> locs = getLocationsByIdList();
		for (int i=0; i<locs.size(); i++){
			Location loc = getLocationById(locs.get(i));
			// now adjust any track locations
			List<String> tracks = loc.getTracksByNameList(null);
			for (int j=0; j<tracks.size(); j++){
				Track track = loc.getTrackById(tracks.get(j));
				if(track.containsRoadName(oldRoad)){
					track.deleteRoadName(oldRoad);
					if(newRoad != null)
						track.addRoadName(newRoad);
				}
			}
		}
	}

	public void options (org.jdom.Element values) {
		if (log.isDebugEnabled()) log.debug("ctor from element "+values);
		// get Location Edit attributes
		Element e = values.getChild("locationEditOptions");
		if (e != null){
			try {
				int x = e.getAttribute("x").getIntValue();
				int y = e.getAttribute("y").getIntValue();
				int height = e.getAttribute("height").getIntValue();
				int width = e.getAttribute("width").getIntValue();
				_editFrameDimension = new Dimension(width, height);
				_editFramePosition = new Point(x,y);
			} catch ( org.jdom.DataConversionException ee) {
				log.debug("Did not find location edit frame attributes");
			} catch ( NullPointerException ne) {
				log.debug("Did not find location edit frame attributes");
			}
		}
	}

	   /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-locations.dtd.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
    	Element values = new Element("options");
        // now save Location Edit frame size and position
        Element e = new org.jdom.Element("locationEditOptions");
        Dimension size = getLocationEditFrameSize();
        Point posn = getLocationEditFramePosition();
        if (_locationEditFrame != null){
        	size = _locationEditFrame.getSize();
        	posn = _locationEditFrame.getLocation();
        	_editFrameDimension = size;
        	_editFramePosition = posn;
        }
        if (posn != null){
        	e.setAttribute("x", ""+posn.x);
        	e.setAttribute("y", ""+posn.y);
        }
        if (size != null){
        	e.setAttribute("height", ""+size.height);
        	e.setAttribute("width", ""+size.width); 
        }
        values.addContent(e);
        return values;
    }
    
	/**
	 * Check for car type and road name replacements. Also check for engine type
	 * replacement.
	 * 
	 */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	log.debug("LocationManager sees property change: " + e.getPropertyName() + " old: " + e.getOldValue() + " new: " + e.getNewValue());
    	if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY) ||
		e.getPropertyName().equals(EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY)){
    		replaceType((String)e.getOldValue(), (String)e.getNewValue());
    	}
    	if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)){
    		replaceRoad((String)e.getOldValue(), (String)e.getNewValue());
    	}
    }
  
    /**
     * @return Number of locations
     */
    public int numEntries() { return _locationHashTable.size(); }
    
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocationManager.class.getName());

}

/* @(#)LocationManager.java */
