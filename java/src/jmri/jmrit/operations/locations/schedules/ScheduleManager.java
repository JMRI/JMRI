package jmri.jmrit.operations.locations.schedules;

import java.beans.PropertyChangeListener;
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
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages schedules.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008, 2013
 */
public class ScheduleManager implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize, PropertyChangeListener {

    public static final String LISTLENGTH_CHANGED_PROPERTY = "scheduleListLength"; // NOI18N

    public ScheduleManager() {
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
    public static synchronized ScheduleManager instance() {
        return InstanceManager.getDefault(ScheduleManager.class);
    }

    public void dispose() {
        _scheduleHashTable.clear();
    }

    // stores known Schedule instances by id
    protected Hashtable<String, Schedule> _scheduleHashTable = new Hashtable<String, Schedule>();

    /**
     * @return Number of schedules
     */
    public int numEntries() {
        return _scheduleHashTable.size();
    }

    /**
     * @param name The string name for the schedule
     * @return requested Schedule object or null if none exists
     */
    public Schedule getScheduleByName(String name) {
        Schedule s;
        Enumeration<Schedule> en = _scheduleHashTable.elements();
        while (en.hasMoreElements()) {
            s = en.nextElement();
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public Schedule getScheduleById(String id) {
        return _scheduleHashTable.get(id);
    }

    /**
     * Finds an existing schedule or creates a new schedule if needed requires
     * schedule's name creates a unique id for this schedule
     *
     * @param name The string name for this schedule
     *
     *
     * @return new schedule or existing schedule
     */
    public Schedule newSchedule(String name) {
        Schedule schedule = getScheduleByName(name);
        if (schedule == null) {
            _id++;
            schedule = new Schedule(Integer.toString(_id), name);
            Integer oldSize = Integer.valueOf(_scheduleHashTable.size());
            _scheduleHashTable.put(schedule.getId(), schedule);
            setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable
                    .size()));
        }
        return schedule;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     *
     * @param schedule The Schedule to add.
     */
    public void register(Schedule schedule) {
        Integer oldSize = Integer.valueOf(_scheduleHashTable.size());
        _scheduleHashTable.put(schedule.getId(), schedule);
        // find last id created
        int id = Integer.parseInt(schedule.getId());
        if (id > _id) {
            _id = id;
        }
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable.size()));
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     *
     * @param schedule The Schedule to delete.
     */
    public void deregister(Schedule schedule) {
        if (schedule == null) {
            return;
        }
        schedule.dispose();
        Integer oldSize = Integer.valueOf(_scheduleHashTable.size());
        _scheduleHashTable.remove(schedule.getId());
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable.size()));
    }

    /**
     * Sort by schedule name
     *
     * @return list of schedules ordered by name
     */
    public List<Schedule> getSchedulesByNameList() {
        List<Schedule> sortList = getList();
        // now re-sort
        List<Schedule> out = new ArrayList<Schedule>();
        for (Schedule sch : sortList) {
            for (int j = 0; j < out.size(); j++) {
                if (sch.getName().compareToIgnoreCase(out.get(j).getName()) < 0) {
                    out.add(j, sch);
                    break;
                }
            }
            if (!out.contains(sch)) {
                out.add(sch);
            }
        }
        return out;

    }

    /**
     * Sort by schedule id number
     *
     * @return list of schedules ordered by id number
     */
    public List<Schedule> getSchedulesByIdList() {
        List<Schedule> sortList = getList();
        // now re-sort
        List<Schedule> out = new ArrayList<Schedule>();
        for (Schedule sch : sortList) {
            for (int j = 0; j < out.size(); j++) {
                try {
                    if (Integer.parseInt(sch.getId()) < Integer.parseInt(out.get(j).getId())) {
                        out.add(j, sch);
                        break;
                    }
                } catch (NumberFormatException e) {
                    log.debug("list id number isn't a number");
                }
            }
            if (!out.contains(sch)) {
                out.add(sch);
            }
        }
        return out;
    }

    private List<Schedule> getList() {
        List<Schedule> out = new ArrayList<Schedule>();
        Enumeration<Schedule> en = _scheduleHashTable.elements();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    public Schedule copySchedule(Schedule schedule, String newScheduleName) {
        Schedule newSchedule = newSchedule(newScheduleName);
        for (ScheduleItem si : schedule.getItemsBySequenceList()) {
            ScheduleItem newSi = newSchedule.addItem(si.getTypeName());
            newSi.copyScheduleItem(si);
        }
        return newSchedule;
    }

    public void resetHitCounts() {
        for (Schedule schedule : getList()) {
            schedule.resetHitCounts();
        }
    }

    /**
     * Gets a JComboBox loaded with schedules.
     *
     * @return JComboBox with a list of schedules.
     */
    public JComboBox<Schedule> getComboBox() {
        JComboBox<Schedule> box = new JComboBox<>();
        updateComboBox(box);
        return box;
    }

    /**
     * Update a JComboBox with the latest schedules.
     *
     * @param box the JComboBox needing an update.
     */
    public void updateComboBox(JComboBox<Schedule> box) {
        box.removeAllItems();
        box.addItem(null);
        for (Schedule schedule : getSchedulesByNameList()) {
            box.addItem(schedule);
        }
    }

    /**
     * Replaces car type in all schedules.
     *
     * @param oldType car type to be replaced.
     * @param newType replacement car type.
     */
    public void replaceType(String oldType, String newType) {
        for (Schedule sch : getSchedulesByIdList()) {
            for (ScheduleItem si : sch.getItemsBySequenceList()) {
                if (si.getTypeName().equals(oldType)) {
                    si.setTypeName(newType);
                }
            }
        }
    }

    /**
     * Replaces car roads in all schedules.
     *
     * @param oldRoad car road to be replaced.
     * @param newRoad replacement car road.
     */
    public void replaceRoad(String oldRoad, String newRoad) {
        if (newRoad == null) {
            return;
        }
        for (Schedule sch : getSchedulesByIdList()) {
            for (ScheduleItem si : sch.getItemsBySequenceList()) {
                if (si.getRoadName().equals(oldRoad)) {
                    si.setRoadName(newRoad);
                }
            }
        }
    }

    /**
     * Replaces car loads in all schedules with specific car type.
     *
     * @param type    car type.
     * @param oldLoad car load to be replaced.
     * @param newLoad replacement car load.
     */
    public void replaceLoad(String type, String oldLoad, String newLoad) {
        for (Schedule sch : getSchedulesByIdList()) {
            for (ScheduleItem si : sch.getItemsBySequenceList()) {
                if (si.getTypeName().equals(type) && si.getReceiveLoadName().equals(oldLoad)) {
                    if (newLoad != null) {
                        si.setReceiveLoadName(newLoad);
                    } else {
                        si.setReceiveLoadName(ScheduleItem.NONE);
                    }
                }
                if (si.getTypeName().equals(type) && si.getShipLoadName().equals(oldLoad)) {
                    if (newLoad != null) {
                        si.setShipLoadName(newLoad);
                    } else {
                        si.setShipLoadName(ScheduleItem.NONE);
                    }
                }
            }
        }
    }

    public void replaceTrack(Track oldTrack, Track newTrack) {
        for (Schedule sch : getSchedulesByIdList()) {
            for (ScheduleItem si : sch.getItemsBySequenceList()) {
                if (si.getDestinationTrack() == oldTrack) {
                    si.setDestination(newTrack.getLocation());
                    si.setDestinationTrack(newTrack);
                }
            }
        }
    }

    /**
     * Gets a JComboBox with a list of spurs that use this schedule.
     *
     * @param schedule The schedule for this JComboBox.
     * @return JComboBox with a list of spurs using schedule.
     */
    public JComboBox<LocationTrackPair> getSpursByScheduleComboBox(Schedule schedule) {
        JComboBox<LocationTrackPair> box = new JComboBox<>();
        // search all spurs for that use schedule
        for (Location location : InstanceManager.getDefault(LocationManager.class).getLocationsByNameList()) {
            for (Track spur : location.getTrackByNameList(Track.SPUR)) {
                if (spur.getScheduleId().equals(schedule.getId())) {
                    LocationTrackPair ltp = new LocationTrackPair(location, spur);
                    box.addItem(ltp);
                }
            }
        }
        return box;
    }

    public void load(Element root) {
        if (root.getChild(Xml.SCHEDULES) != null) {
            List<Element> eSchedules = root.getChild(Xml.SCHEDULES).getChildren(Xml.SCHEDULE);
            log.debug("readFile sees {} schedules", eSchedules.size());
            for (Element eSchedule : eSchedules) {
                register(new Schedule(eSchedule));
            }
        }
    }

    public void store(Element root) {
        Element values;
        root.addContent(values = new Element(Xml.SCHEDULES));
        // add entries
        for (Schedule schedule : getSchedulesByIdList()) {
            values.addContent(schedule.store());
        }
    }

    /**
     * Check for car type and road name changes.
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY)) {
            replaceType((String) e.getOldValue(), (String) e.getNewValue());
        }
        if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)) {
            replaceRoad((String) e.getOldValue(), (String) e.getNewValue());
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
        // set dirty
        InstanceManager.getDefault(LocationManagerXml.class).setDirty(true);
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(ScheduleManager.class);

    @Override
    public void initialize() {
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);
    }

}
