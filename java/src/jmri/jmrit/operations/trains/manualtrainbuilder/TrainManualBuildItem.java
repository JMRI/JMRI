package jmri.jmrit.operations.trains.manualtrainbuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;

/**
 * Represents one manual build item of a manual build.
 *
 * @author Daniel Boudreau Copyright (C) 2026
 */
public class TrainManualBuildItem extends PropertyChangeSupport {

    public static final String NONE = ""; // NOI18N

    protected String _id = NONE;
    protected int _sequenceId = 0; // used to determine order in the manual build
    protected String _type = NONE; // the type of car
    protected String _road = NONE; // the car road
    protected String _load = NONE; // the car load
    protected RouteLocation _routeLocation = null; // car route location
    protected Track _trackLocation = null;// car location track
    protected Location _destination = null; // car destination
    protected Track _trackDestination = null;// car destination track
    protected String _trainScheduleId = NONE; // which day of the week to pickup car
    protected int _count = 1;
    protected boolean _warn = false; // when true issue warning
    protected boolean _fail = false; // when true issue build failure
    protected boolean _remove = false; // when true issue build failure

    public static final String TRAIN_SCHEDULE_CHANGED_PROPERTY = "trainScheduleProteryId"; // NOI18N
    public static final String TYPE_CHANGED_PROPERTY = "manualItemType"; // NOI18N
    public static final String ROAD_CHANGED_PROPERTY = "manualItemRoad"; // NOI18N
    public static final String LOAD_CHANGED_PROPERTY = "manualItemLoad"; // NOI18N
    public static final String ROUTE_LOCATION_CHANGED_PROPERTY = "manualItemRouteLocation"; // NOI18N
    public static final String LOCATION_TRACK_CHANGED_PROPERTY = "manualItemLocationTrack"; // NOI18N
    public static final String DESTINATION_CHANGED_PROPERTY = "manualItemDestination"; // NOI18N
    public static final String DESTINATION_TRACK_CHANGED_PROPERTY = "manualItemDestinationTrack"; // NOI18N
    public static final String COUNT_CHANGED_PROPERTY = "manualItemCount"; // NOI18N
    public static final String WARN_CHANGED_PROPERTY = "manualItemWarn"; // NOI18N
    public static final String FAIL_CHANGED_PROPERTY = "manualItemFail"; // NOI18N
    public static final String REMOVE_CHANGED_PROPERTY = "manualItemRemove"; // NOI18N
    public static final String DISPOSE = "manualItemDispose"; // NOI18N

    /**
     * @param id ManualItem string id
     */
    public TrainManualBuildItem(String id) {
        log.debug("New manual build item id: {}", id);
        _id = id;
    }

    public String getId() {
        return _id;
    }

    public int getSequenceId() {
        return _sequenceId;
    }

    public void setSequenceId(int sequence) {
        // property change not needed
        _sequenceId = sequence;
    }

    public String getTypeName() {
        return _type;
    }

    /**
     * Sets the type of car requested.
     *
     * @param type The car type requested.
     */
    public void setTypeName(String type) {
        String old = _type;
        _type = type;
        setDirtyAndFirePropertyChange(TYPE_CHANGED_PROPERTY, old, type);
    }

    public String getTrainScheduleId() {
        return _trainScheduleId;
    }

    public String getTrainScheduleName() {
        String name = NONE;
        TrainSchedule sch = InstanceManager.getDefault(TrainScheduleManager.class)
                .getScheduleById(getTrainScheduleId());
        if (sch != null) {
            name = sch.getName();
        }
        return name;
    }

    public void setTrainScheduleId(String id) {
        String old = _trainScheduleId;
        _trainScheduleId = id;
        setDirtyAndFirePropertyChange(TRAIN_SCHEDULE_CHANGED_PROPERTY, old, id);
    }

    public String getRoadName() {
        return _road;
    }

    /**
     * Sets the requested car road name.
     *
     * @param road The car road requested.
     */
    public void setRoadName(String road) {
        String old = _road;
        _road = road;
        setDirtyAndFirePropertyChange(ROAD_CHANGED_PROPERTY, old, road);
    }

    /**
     * Sets the car load requested.
     *
     * @param load The load name requested.
     */
    public void setLoadName(String load) {
        String old = _load;
        _load = load;
        setDirtyAndFirePropertyChange(LOAD_CHANGED_PROPERTY, old, load);
    }

    public String getLoadName() {
        return _load;
    }

    public RouteLocation getRouteLocation() {
        return _routeLocation;
    }

    public void setRouteLocation(RouteLocation rl) {
        RouteLocation old = _routeLocation;
        _routeLocation = rl;
        setDirtyAndFirePropertyChange(ROUTE_LOCATION_CHANGED_PROPERTY, old, rl);
    }

    public String getLocationName() {
        if (_routeLocation != null) {
            return _routeLocation.getName();
        }
        return NONE;
    }

    public String getRouteLocationId() {
        if (_routeLocation != null) {
            return _routeLocation.getId();
        }
        return NONE;
    }

    public Track getLocationTrack() {
        return _trackLocation;
    }

    public void setLocationTrack(Track track) {
        Track old = _trackLocation;
        _trackLocation = track;
        setDirtyAndFirePropertyChange(LOCATION_TRACK_CHANGED_PROPERTY, old, track);
    }

    public String getLocationTrackName() {
        if (_trackLocation != null) {
            return _trackLocation.getName();
        }
        return NONE;
    }

    public String getLocationTrackId() {
        if (_trackLocation != null) {
            return _trackLocation.getId();
        }
        return NONE;
    }

    public Location getDestination() {
        return _destination;
    }

    public void setDestination(Location destination) {
        Location old = _destination;
        _destination = destination;
        setDirtyAndFirePropertyChange(DESTINATION_CHANGED_PROPERTY, old, destination);
    }

    public String getDestinationName() {
        if (_destination != null) {
            return _destination.getName();
        }
        return NONE;
    }

    public String getDestinationId() {
        if (_destination != null) {
            return _destination.getId();
        }
        return NONE;
    }

    public Track getDestinationTrack() {
        return _trackDestination;
    }

    public void setDestinationTrack(Track track) {
        Track old = _trackDestination;
        _trackDestination = track;
        setDirtyAndFirePropertyChange(DESTINATION_TRACK_CHANGED_PROPERTY, old, track);
    }

    public String getDestinationTrackName() {
        if (_trackDestination != null) {
            return _trackDestination.getName();
        }
        return NONE;
    }

    public String getDestinationTrackId() {
        if (_trackDestination != null) {
            return _trackDestination.getId();
        }
        return NONE;
    }
    
    public int getCount() {
        return _count;
    }

    public void setCount(int count) {
        int old = _count;
        _count = count;
        setDirtyAndFirePropertyChange(COUNT_CHANGED_PROPERTY, old, count);
    }
    
    public boolean isWarnEnabled() {
        return _warn;
    }
    
    public void setWarnEnabled(boolean warn) {
        boolean old = _warn;
        _warn = warn;
        setDirtyAndFirePropertyChange(WARN_CHANGED_PROPERTY, old, warn);
    }
    
    public boolean isFailEnabled() {
        return _fail;
    }
    
    public void setFailEnabled(boolean fail) {
        boolean old = _fail;
        _fail = fail;
        setDirtyAndFirePropertyChange(FAIL_CHANGED_PROPERTY, old, fail);
    }
    
    public boolean isRemoveEnabled() {
        return _remove;
    }
    
    public void setRemoveEnabled(boolean fail) {
        boolean old = _remove;
        _remove = fail;
        setDirtyAndFirePropertyChange(REMOVE_CHANGED_PROPERTY, old, fail);
    }
    
    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        InstanceManager.getDefault(TrainManagerXml.class).setDirty(true);
        firePropertyChange(p, old, n);
    }

    public void copyManualBuildItem(TrainManualBuildItem mbi) {
        setCount(mbi.getCount());
        setTypeName(mbi.getTypeName());
        setLoadName(mbi.getLoadName());
        setRoadName(mbi.getRoadName());
        setRouteLocation(mbi.getRouteLocation());
        setLocationTrack(mbi.getLocationTrack());
        setDestination(mbi.getDestination());
        setDestinationTrack(mbi.getDestinationTrack());
        setTrainScheduleId(mbi.getTrainScheduleId());
        setFailEnabled(mbi.isFailEnabled());
        setWarnEnabled(mbi.isWarnEnabled());
    }

    public void dispose() {
        firePropertyChange(DISPOSE, null, DISPOSE);
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in operations-config.xml
     *
     * @param e Consist XML element
     */
    public TrainManualBuildItem(org.jdom2.Element e) {
        org.jdom2.Attribute a;
        if ((a = e.getAttribute(Xml.ID)) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in Schedule Item element when reading operations");
        }
        if ((a = e.getAttribute(Xml.SEQUENCE_ID)) != null) {
            _sequenceId = Integer.parseInt(a.getValue());
        }
        if ((a = e.getAttribute(Xml.TRAIN_SCHEDULE_ID)) != null) {
            _trainScheduleId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.TYPE)) != null) {
            _type = a.getValue();
        }
        if ((a = e.getAttribute(Xml.ROAD)) != null) {
            _road = a.getValue();
        }
        if ((a = e.getAttribute(Xml.LOAD)) != null) {
            _load = a.getValue();
        }
        if ((a = e.getAttribute(Xml.ROUTE_LOCATION_ID)) != null) {
            _routeLocation = InstanceManager.getDefault(RouteManager.class).getRouteLocationById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.LOC_TRACK_ID)) != null && _routeLocation != null) {
            _trackLocation = _routeLocation.getLocation().getTrackById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.DESTINATION_ID)) != null) {
            _destination = InstanceManager.getDefault(LocationManager.class).getLocationById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.DEST_TRACK_ID)) != null && _destination != null) {
            _trackDestination = _destination.getTrackById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.COUNT)) != null) {
            _count = Integer.parseInt(a.getValue());
        }
        if ((a = e.getAttribute(Xml.WARN)) != null) {
            _warn = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.FAIL)) != null) {
            _fail = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.REMOVE)) != null) {
            _remove = a.getValue().equals(Xml.TRUE);
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-config.xml.
     *
     * @return Contents in a JDOM Element
     */
    public org.jdom2.Element store() {
        org.jdom2.Element e = new org.jdom2.Element(Xml.MANUAL_BUILD_ITEM);
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.SEQUENCE_ID, Integer.toString(getSequenceId()));
        e.setAttribute(Xml.TRAIN_SCHEDULE_ID, getTrainScheduleId());
        e.setAttribute(Xml.TYPE, getTypeName());
        e.setAttribute(Xml.ROAD, getRoadName());
        e.setAttribute(Xml.LOAD, getLoadName());
        if (!getRouteLocationId().equals(NONE)) {
            e.setAttribute(Xml.ROUTE_LOCATION_ID, getRouteLocationId());
        }
        if (!getLocationTrackId().equals(NONE)) {
            e.setAttribute(Xml.LOC_TRACK_ID, getLocationTrackId());
        }
        if (!getDestinationId().equals(NONE)) {
            e.setAttribute(Xml.DESTINATION_ID, getDestinationId());
        }
        if (!getDestinationTrackId().equals(NONE)) {
            e.setAttribute(Xml.DEST_TRACK_ID, getDestinationTrackId());
        }
        e.setAttribute(Xml.COUNT, Integer.toString(getCount()));
        e.setAttribute(Xml.WARN, isWarnEnabled() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.FAIL, isFailEnabled() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.REMOVE, isRemoveEnabled() ? Xml.TRUE : Xml.FALSE);
        return e;
    }

    private static final Logger log = LoggerFactory.getLogger(TrainManualBuildItem.class);

}
