// LocationManager.java

package jmri.jmrit.operations.locations;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;
import java.awt.Dimension;


import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;


/**
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.2 $
 */
public class LocationManager implements java.beans.PropertyChangeListener {
	public static final String LISTLENGTH = "listLength"; 
    
	public LocationManager() {
    }
    
	/** record the single instance **/
	private static LocationManager _instance = null;
	private static int _id = 0;

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

 
    public void dispose() {
        _locationHashTable.clear();
    }

    protected Hashtable _locationHashTable = new Hashtable();   // stores known Location instances by id

    /**
     * @return requested Location object or null if none exists
     */
     
    public Location getLocationByName(String name) {
    	Location l;
    	Enumeration en =_locationHashTable.elements();
    	for (int i = 0; i < _locationHashTable.size(); i++){
    		l = (Location)en.nextElement();
    		if (l.getName().equals(name))
    			return l;
      	}
        return null;
    }
    
    public Location getLocationById (String id){
    	return (Location)_locationHashTable.get(id);
    }
 
    /**
     * Finds an exsisting location or creates a new location if needed
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
    		Integer oldSize = new Integer(_locationHashTable.size());
    		_locationHashTable.put(location.getId(), location);
    		firePropertyChange(LISTLENGTH, oldSize, new Integer(_locationHashTable.size()));
    	}
    	return location;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(Location location) {
    	Integer oldSize = new Integer(_locationHashTable.size());
        _locationHashTable.put(location.getId(), location);
        // find last id created
        int id = Integer.parseInt(location.getId());
        if (id > _id)
        	_id = id;
        firePropertyChange(LISTLENGTH, oldSize, new Integer(_locationHashTable.size()));
        // listen for name and state changes to forward
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     */
    public void deregister(Location location) {
    	if (location == null)
    		return;
        location.dispose();
        Integer oldSize = new Integer(_locationHashTable.size());
    	_locationHashTable.remove(location.getId());
        firePropertyChange(LISTLENGTH, oldSize, new Integer(_locationHashTable.size()));
    }

    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of user name changes to individual NamedBeans.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {

    }

    /**
     * Sort by location name
     * @return list of location ids ordered by name
     */
    public List getLocationsByNameList() {
		// first get id list
		List sortList = getList();
		// now re-sort
		List out = new ArrayList();
		String locName = "";
		boolean locAdded = false;
		Location l;

		for (int i = 0; i < sortList.size(); i++) {
			locAdded = false;
			l = getLocationById((String) sortList.get(i));
			locName = l.getName();
			for (int j = 0; j < out.size(); j++) {
				l = getLocationById((String) out.get(j));
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
    public List getLocationsByIdList() {
    	// first get id list
    	List sortList = getList();
    	// now re-sort
    	List out = new ArrayList();
    	int locationNumber = 0;
    	boolean locationAdded = false;
    	Location l;
    	
    	for (int i=0; i<sortList.size(); i++){
    		locationAdded = false;
    		l = getLocationById ((String)sortList.get(i));
    		try{
    			locationNumber = Integer.parseInt (l.getId());
    		}catch (NumberFormatException e) {
    			log.debug("location id number isn't a number");
    		}
    		for (int j=0; j<out.size(); j++ ){
    			l = getLocationById ((String)out.get(j));
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
    
    private List getList() {
        String[] arr = new String[_locationHashTable.size()];
        List out = new ArrayList();
        Enumeration en = _locationHashTable.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = (String)en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
    	box.addItem("");
		List locs = getLocationsByNameList();
		for (int i = 0; i < locs.size(); i++){
			String locId = (String)locs.get(i);
			Location l = getLocationById(locId);
			box.addItem(l);
		}
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
		List locs = getLocationsByNameList();
		for (int i = 0; i < locs.size(); i++){
			String locId = (String)locs.get(i);
			Location l = getLocationById(locId);
			box.addItem(l);
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocationManager.class.getName());

}

/* @(#)LocationManager.java */
