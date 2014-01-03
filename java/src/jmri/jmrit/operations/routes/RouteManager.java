// RouteManager.java

package jmri.jmrit.operations.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import org.jdom.Element;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;

/**
 * Manages the routes
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010
 * @version $Revision$
 */
public class RouteManager {
	public static final String LISTLENGTH_CHANGED_PROPERTY = "routesListLengthChanged"; // NOI18N

	public RouteManager() {
	}

	/** record the single instance **/
	private static RouteManager _instance = null;
	private int _id = 0;

	public static synchronized RouteManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("RouteManager creating instance");
			// create and load
			_instance = new RouteManager();
			OperationsSetupXml.instance(); // load setup
			RouteManagerXml.instance(); // load routes
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("RouteManager returns instance " + _instance);
		return _instance;
	}

	public void dispose() {
		_routeHashTable.clear();
		_id = 0;
	}

	// stores known Route instances by id
	protected Hashtable<String, Route> _routeHashTable = new Hashtable<String, Route>();

	/**
	 * @return requested Route object or null if none exists
	 */
	public Route getRouteByName(String name) {
		Route l;
		Enumeration<Route> en = _routeHashTable.elements();
		while (en.hasMoreElements()) {
			l = en.nextElement();
			if (l.getName().equals(name))
				return l;
		}
		return null;
	}

	public Route getRouteById(String id) {
		return _routeHashTable.get(id);
	}

	/**
	 * Finds an existing route or creates a new route if needed requires route's name creates a unique id for this route
	 * 
	 * @param name
	 * 
	 * @return new route or existing route
	 */
	public Route newRoute(String name) {
		Route route = getRouteByName(name);
		if (route == null) {
			_id++;
			route = new Route(Integer.toString(_id), name);
			Integer oldSize = Integer.valueOf(_routeHashTable.size());
			_routeHashTable.put(route.getId(), route);
			firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_routeHashTable.size()));
		}
		return route;
	}

	/**
	 * Remember a NamedBean Object created outside the manager.
	 */
	public void register(Route route) {
		Integer oldSize = Integer.valueOf(_routeHashTable.size());
		_routeHashTable.put(route.getId(), route);
		// find last id created
		int id = Integer.parseInt(route.getId());
		if (id > _id)
			_id = id;
		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_routeHashTable.size()));
		// listen for name and state changes to forward
	}

	/**
	 * Forget a NamedBean Object created outside the manager.
	 */
	public void deregister(Route route) {
		if (route == null)
			return;
		route.dispose();
		Integer oldSize = Integer.valueOf(_routeHashTable.size());
		_routeHashTable.remove(route.getId());
		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_routeHashTable.size()));
	}

	/**
	 * Sort by route name
	 * 
	 * @return list of routes ordered by name
	 */
	public List<Route> getRoutesByNameList() {
		List<Route> sortList = getList();
		// now re-sort
		List<Route> out = new ArrayList<Route>();
		for (int i = 0; i < sortList.size(); i++) {
			for (int j = 0; j < out.size(); j++) {
				if (sortList.get(i).getName().compareToIgnoreCase(out.get(j).getName()) < 0) {
					out.add(j, sortList.get(i));
					break;
				}
			}
			if (!out.contains(sortList.get(i))) {
				out.add(sortList.get(i));
			}
		}
		return out;

	}

	/**
	 * Sort by route number, number can alpha numeric
	 * 
	 * @return list of routes ordered by id numbers
	 */
	public List<Route> getRoutesByIdList() {
		List<Route> sortList = getList();
		// now re-sort
		List<Route> out = new ArrayList<Route>();
		for (int i = 0; i < sortList.size(); i++) {
			for (int j = 0; j < out.size(); j++) {
				try {
					if (Integer.parseInt(sortList.get(i).getId()) < Integer.parseInt(out.get(j).getId())) {
						out.add(j, sortList.get(i));
						break;
					}
				} catch (NumberFormatException e) {
					log.error("list id number isn't a number");
				}
			}
			if (!out.contains(sortList.get(i))) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}

	private List<Route> getList() {
		List<Route> out = new ArrayList<Route>();
		Enumeration<Route> en = _routeHashTable.elements();
		while (en.hasMoreElements()) {
			out.add(en.nextElement());
		}
		return out;
	}

	public JComboBox getComboBox() {
		JComboBox box = new JComboBox();
		box.addItem("");
		List<Route> routes = getRoutesByNameList();
		for (int i = 0; i < routes.size(); i++) {
			box.addItem(routes.get(i));
		}
		return box;
	}

	public void updateComboBox(JComboBox box) {
		box.removeAllItems();
		box.addItem("");
		List<Route> routes = getRoutesByNameList();
		for (int i = 0; i < routes.size(); i++) {
			box.addItem(routes.get(i));
		}
	}

	/**
	 * Copy route, returns a new route named routeName. If invert is true the reverse of the route is returned.
	 * 
	 * @param route
	 *            The route to be copied
	 * @param routeName
	 *            The name of the new route
	 * @param invert
	 *            If true, return the inversion of route
	 * @return A copy of the route
	 */
	public Route copyRoute(Route route, String routeName, boolean invert) {
		Route newRoute = newRoute(routeName);
		List<RouteLocation> routeList = route.getLocationsBySequenceList();
		if (!invert) {
			for (int i = 0; i < routeList.size(); i++) {
				copyRouteLocation(newRoute, routeList.get(i), null, invert);
			}
			// invert route order
		} else {
			for (int i = routeList.size() - 1; i >= 0; i--) {
				int y = i - 1;
				if (y < 0)
					y = 0;
				copyRouteLocation(newRoute, routeList.get(i), routeList.get(y), invert);
			}
		}
		return newRoute;
	}

	private void copyRouteLocation(Route newRoute, RouteLocation rl, RouteLocation rlNext, boolean invert) {
		Location loc = LocationManager.instance().getLocationByName(rl.getName());
		RouteLocation rlNew = newRoute.addLocation(loc);
		// now copy the route location objects we want
		rlNew.setMaxCarMoves(rl.getMaxCarMoves());
		rlNew.setWait(rl.getWait());
		rlNew.setDepartureTime(rl.getDepartureTime());
		rlNew.setComment(rl.getComment());
		if (!invert) {
			rlNew.setDropAllowed(rl.isDropAllowed());
			rlNew.setPickUpAllowed(rl.isPickUpAllowed());
			rlNew.setGrade(rl.getGrade());
			rlNew.setTrainDirection(rl.getTrainDirection());
			rlNew.setMaxTrainLength(rl.getMaxTrainLength());
		} else {
			// flip set outs and pick ups
			rlNew.setDropAllowed(rl.isPickUpAllowed());
			rlNew.setPickUpAllowed(rl.isDropAllowed());
			// invert train directions
			int oldDirection = rl.getTrainDirection();
			if (oldDirection == RouteLocation.NORTH)
				rlNew.setTrainDirection(RouteLocation.SOUTH);
			else if (oldDirection == RouteLocation.SOUTH)
				rlNew.setTrainDirection(RouteLocation.NORTH);
			else if (oldDirection == RouteLocation.EAST)
				rlNew.setTrainDirection(RouteLocation.WEST);
			else if (oldDirection == RouteLocation.WEST)
				rlNew.setTrainDirection(RouteLocation.EAST);
			// get the max length between location
			if (rlNext == null) {
				log.error("Can not copy route, oldNextRl is null!");
				return;
			}
			rlNew.setMaxTrainLength(rlNext.getMaxTrainLength());
		}
		rlNew.setTrainIconX(rl.getTrainIconX());
		rlNew.setTrainIconY(rl.getTrainIconY());
	}

	/**
	 * @return Number of routes
	 */
	public int numEntries() {
		return _routeHashTable.size();
	}

	public void load(Element root) {
		// decode type, invoke proper processing routine if a decoder file
		if (root.getChild(Xml.ROUTES) != null) {
			@SuppressWarnings("unchecked")
			List<Element> l = root.getChild(Xml.ROUTES).getChildren(Xml.ROUTE);
			if (log.isDebugEnabled())
				log.debug("readFile sees " + l.size() + " routes");
			for (int i = 0; i < l.size(); i++) {
				register(new Route(l.get(i)));
			}
		}
	}

	public void store(Element root) {
		Element values = new Element(Xml.ROUTES);
		root.addContent(values);
		List<Route> routeList = getRoutesByIdList();
		for (int i = 0; i < routeList.size(); i++) {
			values.addContent(routeList.get(i).store());
		}
	}

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String p, Object old, Object n) {
		RouteManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static Logger log = LoggerFactory.getLogger(RouteManager.class.getName());

}

/* @(#)RouteManager.java */
