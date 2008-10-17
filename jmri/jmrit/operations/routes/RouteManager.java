// RouteManager.java

package jmri.jmrit.operations.routes;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;
import java.awt.Dimension;

import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.setup.Control;


/**
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.2 $
 */
public class RouteManager implements java.beans.PropertyChangeListener {
	public static final String LISTLENGTH = "listLength"; 
    
	public RouteManager() {
    }
    
	/** record the single instance **/
	private static RouteManager _instance = null;
	private static int _id = 0;

	public static synchronized RouteManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("RouteManager creating instance");
			// create and load
			_instance = new RouteManager();
			RouteManagerXml.instance();				// load routes
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("RouteManager returns instance "+_instance);
		return _instance;
	}

 
    public void dispose() {
        _routeHashTable.clear();
    }

    protected Hashtable _routeHashTable = new Hashtable();   // stores known Route instances by id

    /**
     * @return requested Route object or null if none exists
     */
     
    public Route getRouteByName(String name) {
    	Route l;
    	Enumeration en =_routeHashTable.elements();
    	for (int i = 0; i < _routeHashTable.size(); i++){
    		l = (Route)en.nextElement();
    		if (l.getName().equals(name))
    			return l;
      	}
        return null;
    }
    
    public Route getRouteById (String id){
    	return (Route)_routeHashTable.get(id);
    }
 
    /**
     * Finds an exsisting route or creates a new route if needed
     * requires route's name creates a unique id for this route
     * @param name
     * 
     * @return new route or existing route
     */
    public Route newRoute (String name){
    	Route route = getRouteByName(name);
    	if (route == null){
    		_id++;						
    		route = new Route(Integer.toString(_id), name);
    		Integer oldSize = new Integer(_routeHashTable.size());
    		_routeHashTable.put(route.getId(), route);
    		firePropertyChange(LISTLENGTH, oldSize, new Integer(_routeHashTable.size()));
    	}
    	return route;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(Route route) {
    	Integer oldSize = new Integer(_routeHashTable.size());
        _routeHashTable.put(route.getId(), route);
        // find last id created
        int id = Integer.parseInt(route.getId());
        if (id > _id)
        	_id = id;
        firePropertyChange(LISTLENGTH, oldSize, new Integer(_routeHashTable.size()));
        // listen for name and state changes to forward
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     */
    public void deregister(Route route) {
    	if (route == null)
    		return;
        route.dispose();
        Integer oldSize = new Integer(_routeHashTable.size());
    	_routeHashTable.remove(route.getId());
        firePropertyChange(LISTLENGTH, oldSize, new Integer(_routeHashTable.size()));
    }

    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of user name changes to individual NamedBeans.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {

    }

    /**
     * Sort by route name
     * @return list of route ids ordered by name
     */
    public List getRoutesByNameList() {
		// first get id list
		List sortList = getList();
		// now re-sort
		List out = new ArrayList();
		String routeName = "";
		boolean routeAdded = false;
		Route route;

		for (int i = 0; i < sortList.size(); i++) {
			routeAdded = false;
			route = getRouteById((String) sortList.get(i));
			routeName = route.getName();
			for (int j = 0; j < out.size(); j++) {
				route = getRouteById((String) out.get(j));
				String outRouteName = route.getName();
				if (routeName.compareToIgnoreCase(outRouteName) < 0) {
					out.add(j, sortList.get(i));
					routeAdded = true;
					break;
				}
			}
			if (!routeAdded) {
				out.add(sortList.get(i));
			}
		}
		return out;

	}
    
    /**
	 * Sort by route number, number can alpha numeric
	 * 
	 * @return list of route ids ordered by number
	 */
    public List getRoutesByIdList() {
    	// first get id list
    	List sortList = getList();
    	// now re-sort
    	List out = new ArrayList();
    	int routeNumber = 0;
    	boolean routeAdded = false;
    	Route route;
    	
    	for (int i=0; i<sortList.size(); i++){
    		routeAdded = false;
    		route = getRouteById ((String)sortList.get(i));
    		try{
    			routeNumber = Integer.parseInt (route.getId());
    		}catch (NumberFormatException e) {
    			log.debug("route id number isn't a number");
    		}
    		for (int j=0; j<out.size(); j++ ){
    			route = getRouteById ((String)out.get(j));
        		try{
        			int outRouteNumber = Integer.parseInt (route.getId());
        			if (routeNumber < outRouteNumber){
        				out.add(j, sortList.get(i));
        				routeAdded = true;
        				break;
        			}
        		}catch (NumberFormatException e) {
        			log.debug("list out id number isn't a number");
        		}
    		}
    		if (!routeAdded){
    			out.add( sortList.get(i));
    		}
    	}
        return out;
    }
    
    private List getList() {
        String[] arr = new String[_routeHashTable.size()];
        List out = new ArrayList();
        Enumeration en = _routeHashTable.keys();
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
		List routes = getRoutesByNameList();
		for (int i = 0; i < routes.size(); i++){
			Route route = getRouteById((String)routes.get(i));
			box.addItem(route);
		}
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
		List routes = getRoutesByNameList();
		for (int i = 0; i < routes.size(); i++){
			Route route = getRouteById((String)routes.get(i));
			box.addItem(route);
		}
    }
  
    /**
     * @return Number of routes
     */
    public int numEntries() { return _routeHashTable.size(); }
    
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RouteManager.class.getName());

}

/* @(#)RouteManager.java */
