package jmri.jmrit.operations.routes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InstanceManagerAutoInitialize;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the routes
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010
 */
public class RouteManager implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize {

    public static final String LISTLENGTH_CHANGED_PROPERTY = "routesListLengthChanged"; // NOI18N

    public RouteManager() {
    }

    private int _id = 0;

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized RouteManager instance() {
        return InstanceManager.getDefault(RouteManager.class);
    }

    public void dispose() {
        _routeHashTable.clear();
        _id = 0;
    }

    // stores known Route instances by id
    protected Hashtable<String, Route> _routeHashTable = new Hashtable<>();

    /**
     * @param name The string name of the Route.
     * @return requested Route object or null if none exists
     */
    public Route getRouteByName(String name) {
        Route l;
        Enumeration<Route> en = _routeHashTable.elements();
        while (en.hasMoreElements()) {
            l = en.nextElement();
            if (l.getName().equals(name)) {
                return l;
            }
        }
        return null;
    }

    public Route getRouteById(String id) {
        return _routeHashTable.get(id);
    }

    /**
     * Finds an existing route or creates a new route if needed requires route's
     * name creates a unique id for this route
     *
     * @param name The string name of the new Route.
     *
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
            setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
                    Integer.valueOf(_routeHashTable.size()));
        }
        return route;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     *
     * @param route The Route to add.
     */
    public void register(Route route) {
        Integer oldSize = Integer.valueOf(_routeHashTable.size());
        _routeHashTable.put(route.getId(), route);
        // find last id created
        int id = Integer.parseInt(route.getId());
        if (id > _id) {
            _id = id;
        }
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_routeHashTable.size()));
        // listen for name and state changes to forward
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     *
     * @param route The Route to delete.
     */
    public void deregister(Route route) {
        if (route == null) {
            return;
        }
        route.dispose();
        Integer oldSize = Integer.valueOf(_routeHashTable.size());
        _routeHashTable.remove(route.getId());
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_routeHashTable.size()));
    }

    /**
     * Sort by route name
     *
     * @return list of routes ordered by name
     */
    public List<Route> getRoutesByNameList() {
        List<Route> sortList = getList();
        // now re-sort
        List<Route> out = new ArrayList<>();
        for (Route route : sortList) {
            for (int j = 0; j < out.size(); j++) {
                if (route.getName().compareToIgnoreCase(out.get(j).getName()) < 0) {
                    out.add(j, route);
                    break;
                }
            }
            if (!out.contains(route)) {
                out.add(route);
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
        List<Route> out = new ArrayList<>();
        for (Route route : sortList) {
            for (int j = 0; j < out.size(); j++) {
                try {
                    if (Integer.parseInt(route.getId()) < Integer.parseInt(out.get(j).getId())) {
                        out.add(j, route);
                        break;
                    }
                } catch (NumberFormatException e) {
                    log.error("list id number isn't a number");
                }
            }
            if (!out.contains(route)) {
                out.add(route);
            }
        }
        return out;
    }

    private List<Route> getList() {
        List<Route> out = new ArrayList<>();
        Enumeration<Route> en = _routeHashTable.elements();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    public JComboBox<Route> getComboBox() {
        JComboBox<Route> box = new JComboBox<>();
        box.addItem(null);
        List<Route> routes = getRoutesByNameList();
        for (Route route : routes) {
            box.addItem(route);
        }
        return box;
    }

    public void updateComboBox(JComboBox<Route> box) {
        box.removeAllItems();
        box.addItem(null);
        List<Route> routes = getRoutesByNameList();
        for (Route route : routes) {
            box.addItem(route);
        }
    }

    /**
     * Copy route, returns a new route named routeName. If invert is true the
     * reverse of the route is returned.
     *
     * @param route     The route to be copied
     * @param routeName The name of the new route
     * @param invert    If true, return the inversion of route
     * @return A copy of the route
     */
    public Route copyRoute(Route route, String routeName, boolean invert) {
        Route newRoute = newRoute(routeName);
        List<RouteLocation> routeList = route.getLocationsBySequenceList();
        if (!invert) {
            for (RouteLocation rl : routeList) {
                copyRouteLocation(newRoute, rl, null, invert);
            }
            // invert route order
        } else {
            for (int i = routeList.size() - 1; i >= 0; i--) {
                int y = i - 1;
                if (y < 0) {
                    y = 0;
                }
                copyRouteLocation(newRoute, routeList.get(i), routeList.get(y), invert);
            }
        }
        newRoute.setComment(route.getComment());
        return newRoute;
    }

    private void copyRouteLocation(Route newRoute, RouteLocation rl, RouteLocation rlNext, boolean invert) {
        Location loc = InstanceManager.getDefault(LocationManager.class).getLocationByName(rl.getName());
        RouteLocation rlNew = newRoute.addLocation(loc);
        // now copy the route location objects we want
        rlNew.setMaxCarMoves(rl.getMaxCarMoves());
        rlNew.setRandomControl(rl.getRandomControl());
        rlNew.setWait(rl.getWait());
        rlNew.setDepartureTime(rl.getDepartureTime());
        rlNew.setComment(rl.getComment());
        rlNew.setCommentColor(rl.getCommentColor());
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
            if (oldDirection == RouteLocation.NORTH) {
                rlNew.setTrainDirection(RouteLocation.SOUTH);
            } else if (oldDirection == RouteLocation.SOUTH) {
                rlNew.setTrainDirection(RouteLocation.NORTH);
            } else if (oldDirection == RouteLocation.EAST) {
                rlNew.setTrainDirection(RouteLocation.WEST);
            } else if (oldDirection == RouteLocation.WEST) {
                rlNew.setTrainDirection(RouteLocation.EAST);
            }
            // get the max length between location
            if (rlNext == null) {
                log.error("Can not copy route, rlNext is null!");
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
            List<Element> eRoutes = root.getChild(Xml.ROUTES).getChildren(Xml.ROUTE);
            log.debug("readFile sees {} routes", eRoutes.size());
            for (Element eRoute : eRoutes) {
                register(new Route(eRoute));
            }
        }
    }

    public void store(Element root) {
        Element values = new Element(Xml.ROUTES);
        root.addContent(values);
        for (Route route : getRoutesByIdList()) {
            values.addContent(route.store());
        }
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        InstanceManager.getDefault(RouteManagerXml.class).setDirty(true);
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(RouteManager.class);

    @Override
    public void initialize() {
        InstanceManager.getDefault(OperationsSetupXml.class); // load setup
        InstanceManager.getDefault(RouteManagerXml.class); // load routes
    }

}
