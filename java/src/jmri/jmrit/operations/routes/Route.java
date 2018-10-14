package jmri.jmrit.operations.routes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a route on the layout
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 */
public class Route implements java.beans.PropertyChangeListener {

    public static final String NONE = "";

    protected String _id = NONE;
    protected String _name = NONE;
    protected String _comment = NONE;

    // stores location names for this route
    protected Hashtable<String, RouteLocation> _routeHashTable = new Hashtable<>();
    protected int _IdNumber = 0; // each location in a route gets its own id
    protected int _sequenceNum = 0; // each location has a unique sequence number

    public static final int EAST = 1; // train direction
    public static final int WEST = 2;
    public static final int NORTH = 4;
    public static final int SOUTH = 8;

    public static final String LISTCHANGE_CHANGED_PROPERTY = "routeListChange"; // NOI18N
    public static final String ROUTE_STATUS_CHANGED_PROPERTY = "routeStatusChange"; // NOI18N
    public static final String DISPOSE = "routeDispose"; // NOI18N

    public static final String OKAY = Bundle.getMessage("ButtonOK");
    public static final String TRAIN_BUILT = Bundle.getMessage("TrainBuilt");
    public static final String ORPHAN = Bundle.getMessage("Orphan");
    public static final String ERROR = Bundle.getMessage("ErrorTitle");

    public static final int START = 1; // add location at start of route

    public Route(String id, String name) {
        log.debug("New route ({}) id: {}", name, id);
        _name = name;
        _id = id;
    }

    public String getId() {
        return _id;
    }

    public void setName(String name) {
        String old = _name;
        _name = name;
        if (!old.equals(name)) {
            setDirtyAndFirePropertyChange("nameChange", old, name); // NOI18N
        }
    }

    // for combo boxes
    @Override
    public String toString() {
        return _name;
    }

    public String getName() {
        return _name;
    }

    public void setComment(String comment) {
        String old = _comment;
        _comment = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange("commentChange", old, comment); // NOI18N
        }
    }

    public String getComment() {
        return _comment;
    }

    public void dispose() {
        removeTrainListeners();
        setDirtyAndFirePropertyChange(DISPOSE, null, DISPOSE);
    }

    /**
     * Adds a location to the end of this route
     * 
     * @param location The Location.
     *
     * @return RouteLocation created for the location added
     */
    public RouteLocation addLocation(Location location) {
        _IdNumber++;
        _sequenceNum++;
        String id = _id + "r" + Integer.toString(_IdNumber);
        log.debug("adding new location to ({}) id: {}", getName(), id);
        RouteLocation rl = new RouteLocation(id, location);
        rl.setSequenceNumber(_sequenceNum);
        Integer old = Integer.valueOf(_routeHashTable.size());
        _routeHashTable.put(rl.getId(), rl);

        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_routeHashTable.size()));
        // listen for drop and pick up changes to forward
        rl.addPropertyChangeListener(this);
        return rl;
    }

    /**
     * Add a location at a specific place (sequence) in the route Allowable
     * sequence numbers are 1 to max size of route. 1 = start of route, or Route.START
     * 
     * @param location The Location to add.
     * @param sequence Where in the route to add the location.
     *
     * @return route location
     */
    public RouteLocation addLocation(Location location, int sequence) {
        RouteLocation rl = addLocation(location);
        if (sequence < 1 || sequence > _routeHashTable.size()) {
            return rl;
        }
        for (int i = 0; i < _routeHashTable.size() - sequence; i++) {
            moveLocationUp(rl);
        }
        return rl;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     * @param rl The RouteLocation to add to this route.
     */
    public void register(RouteLocation rl) {
        Integer old = Integer.valueOf(_routeHashTable.size());
        _routeHashTable.put(rl.getId(), rl);

        // find last id created
        String[] getId = rl.getId().split("r");
        int id = Integer.parseInt(getId[1]);
        if (id > _IdNumber) {
            _IdNumber = id;
        }
        // find and save the highest sequence number
        if (rl.getSequenceNumber() > _sequenceNum) {
            _sequenceNum = rl.getSequenceNumber();
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_routeHashTable.size()));
        // listen for drop and pick up changes to forward
        rl.addPropertyChangeListener(this);
    }

    /**
     * Delete a RouteLocation
     * @param rl The RouteLocation to remove from the route.
     *
     */
    public void deleteLocation(RouteLocation rl) {
        if (rl != null) {
            rl.removePropertyChangeListener(this);
            // subtract from the locations's available track length
            String id = rl.getId();
            rl.dispose();
            Integer old = Integer.valueOf(_routeHashTable.size());
            _routeHashTable.remove(id);
            resequence();
            setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_routeHashTable.size()));
        }
    }

    public int size() {
        return _routeHashTable.size();
    }

    /**
     * Reorder the location sequence numbers for this route
     */
    private void resequence() {
        List<RouteLocation> routeList = getLocationsBySequenceList();
        for (int i = 0; i < routeList.size(); i++) {
            _sequenceNum = i + 1; // start sequence numbers at 1
            routeList.get(i).setSequenceNumber(_sequenceNum);
        }
    }

    /**
     * Get the first location in a route
     *
     * @return the first route location
     */
    public RouteLocation getDepartsRouteLocation() {
        List<RouteLocation> list = getLocationsBySequenceList();
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Get the last location in a route
     *
     * @return the last route location
     */
    public RouteLocation getTerminatesRouteLocation() {
        List<RouteLocation> list = getLocationsBySequenceList();
        if (list.size() > 0) {
            return list.get(list.size() - 1);
        }
        return null;
    }

    /**
     * Gets the next route location in a route
     *
     * @param rl the current route location
     * @return the next route location, null if rl is the last location in a
     *         route.
     */
    public RouteLocation getNextRouteLocation(RouteLocation rl) {
        List<RouteLocation> list = getLocationsBySequenceList();
        for (int i = 0; i < list.size() - 1; i++) {
            if (rl == list.get(i)) {
                return list.get(i + 1);
            }
        }
        return null;
    }

    /**
     * Get location by name (gets last route location with name)
     * @param name The string location name.
     *
     * @return route location
     */
    public RouteLocation getLastLocationByName(String name) {
        List<RouteLocation> routeList = getLocationsBySequenceList();
        RouteLocation rl;

        for (int i = routeList.size() - 1; i >= 0; i--) {
            rl = routeList.get(i);
            if (rl.getName().equals(name)) {
                return rl;
            }
        }
        return null;
    }

    /**
     * Get a RouteLocation by id
     * @param id The string id.
     *
     * @return route location
     */
    public RouteLocation getLocationById(String id) {
        return _routeHashTable.get(id);
    }

    private List<RouteLocation> getLocationsByIdList() {
        List<RouteLocation> out = new ArrayList<>();
        Enumeration<RouteLocation> en = _routeHashTable.elements();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    /**
     * Get a list of RouteLocations sorted by route order
     *
     * @return list of RouteLocations ordered by sequence
     */
    public List<RouteLocation> getLocationsBySequenceList() {
        // now re-sort
        List<RouteLocation> out = new ArrayList<>();
        for (RouteLocation rl : getLocationsByIdList()) {
            for (int j = 0; j < out.size(); j++) {
                if (rl.getSequenceNumber() < out.get(j).getSequenceNumber()) {
                    out.add(j, rl);
                    break;
                }
            }
            if (!out.contains(rl)) {
                out.add(rl);
            }
        }
        return out;
    }

    /**
     * Places a RouteLocation earlier in the route.
     * @param rl The RouteLocation to move.
     *
     */
    public void moveLocationUp(RouteLocation rl) {
        int sequenceNum = rl.getSequenceNumber();
        if (sequenceNum - 1 <= 0) {
            rl.setSequenceNumber(_sequenceNum + 1); // move to the end of the list
            resequence();
        } else {
            // adjust the other item taken by this one
            RouteLocation replaceRl = getRouteLocationBySequenceNumber(sequenceNum - 1);
            if (replaceRl != null) {
                replaceRl.setSequenceNumber(sequenceNum);
                rl.setSequenceNumber(sequenceNum - 1);
            } else {
                resequence(); // error the sequence number is missing
            }
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, Integer.toString(sequenceNum));
    }

    /**
     * Moves a RouteLocation later in the route.
     * @param rl The RouteLocation to move.
     *
     */
    public void moveLocationDown(RouteLocation rl) {
        int sequenceNum = rl.getSequenceNumber();
        if (sequenceNum + 1 > _sequenceNum) {
            rl.setSequenceNumber(0); // move to the start of the list
            resequence();
        } else {
            // adjust the other item taken by this one
            RouteLocation replaceRl = getRouteLocationBySequenceNumber(sequenceNum + 1);
            if (replaceRl != null) {
                replaceRl.setSequenceNumber(sequenceNum);
                rl.setSequenceNumber(sequenceNum + 1);
            } else {
                resequence(); // error the sequence number is missing
            }
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, Integer.toString(sequenceNum));
    }

    /**
     * 1st RouteLocation in a route starts at 1.
     * @param sequence selects which RouteLocation is to be returned
     * @return RouteLocation selected
     */
    public RouteLocation getRouteLocationBySequenceNumber(int sequence) {
        for (RouteLocation rl : getLocationsByIdList()) {
            if (rl.getSequenceNumber() == sequence) {
                return rl;
            }
        }
        return null;
    }

    /**
     * Gets the status of the route: OKAY ORPHAN ERROR TRAIN_BUILT
     *
     * @return string with status of route.
     */
    public String getStatus() {
        removeTrainListeners();
        addTrainListeners(); // and add them right back in
        List<RouteLocation> routeList = getLocationsByIdList();
        if (routeList.size() == 0) {
            return ERROR;
        }
        for (RouteLocation rl : routeList) {
            if (rl.getName().equals(RouteLocation.DELETED)) {
                return ERROR;
            }
        }
        // check to see if this route is used by a train that is built
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByIdList()) {
            if (train.getRoute() == this && train.isBuilt()) {
                return TRAIN_BUILT;
            }
        }
        // check to see if this route is used by a train
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByIdList()) {
            if (train.getRoute() == this) {
                return OKAY;
            }
        }
        return ORPHAN;
    }
    
    private void addTrainListeners() {
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByIdList()) {
            if (train.getRoute() == this) {
                train.addPropertyChangeListener(this);
            }
        }
    }
    
    private void removeTrainListeners() {
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByIdList()) {
            train.removePropertyChangeListener(this);
        }
    }

    /**
     * Gets the shortest train length specified in the route.
     * 
     * @return the minimum scale train length for this route.
     */
    public int getRouteMinimumTrainLength() {
        int min = getRouteMaximumTrainLength();
        for (RouteLocation rl : getLocationsByIdList()) {
            if (rl.getMaxTrainLength() < min)
                min = rl.getMaxTrainLength();
        }
        return min;
    }

    /**
     * Gets the longest train length specified in the route.
     * 
     * @return the maximum scale train length for this route.
     */
    public int getRouteMaximumTrainLength() {
        int max = 0;
        for (RouteLocation rl : getLocationsByIdList()) {
            if (rl.getMaxTrainLength() > max)
                max = rl.getMaxTrainLength();
        }
        return max;
    }

    public JComboBox<RouteLocation> getComboBox() {
        JComboBox<RouteLocation> box = new JComboBox<>();
        for (RouteLocation rl : getLocationsBySequenceList()) {
            box.addItem(rl);
        }
        return box;
    }

    public void updateComboBox(JComboBox<RouteLocation> box) {
        box.removeAllItems();
        box.addItem(null);
        for (RouteLocation rl : getLocationsBySequenceList()) {
            box.addItem(rl);
        }
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in operations-config.xml
     *
     * @param e Consist XML element
     */
    public Route(Element e) {
        Attribute a;
        if ((a = e.getAttribute(Xml.ID)) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in route element when reading operations");
        }
        if ((a = e.getAttribute(Xml.NAME)) != null) {
            _name = a.getValue();
        }
        if ((a = e.getAttribute(Xml.COMMENT)) != null) {
            _comment = a.getValue();
        }
        if (e.getChildren(Xml.LOCATION) != null) {
            List<Element> eRouteLocations = e.getChildren(Xml.LOCATION);
            log.debug("route: ({}) has {} locations", getName(), eRouteLocations.size());
            for (Element eRouteLocation : eRouteLocations) {
                register(new RouteLocation(eRouteLocation));
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-config.xml.
     *
     * @return Contents in a JDOM Element
     */
    public Element store() {
        Element e = new Element(Xml.ROUTE);
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.NAME, getName());
        e.setAttribute(Xml.COMMENT, getComment());
        for (RouteLocation rl : getLocationsBySequenceList()) {
            e.addContent(rl.store());
        }
        return e;
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        // forward drops, pick ups, train direction, max moves, and max length as a list change
        if (e.getPropertyName().equals(RouteLocation.DROP_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(RouteLocation.PICKUP_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(RouteLocation.TRAIN_DIRECTION_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(RouteLocation.MAX_MOVES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(RouteLocation.MAX_LENGTH_CHANGED_PROPERTY)) {
            setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, "RouteLocation"); // NOI18N
        }
        if (e.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY)) {
            pcs.firePropertyChange(ROUTE_STATUS_CHANGED_PROPERTY, true, false);
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

    private final static Logger log = LoggerFactory.getLogger(Route.class);

}
