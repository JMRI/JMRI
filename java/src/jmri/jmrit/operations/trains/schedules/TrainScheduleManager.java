package jmri.jmrit.operations.trains.schedules;

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
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.trains.TrainSwitchLists;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages train schedules. The default is the days of the week, but can be
 * anything the user wants when defining when trains will run.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class TrainScheduleManager implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize, PropertyChangeListener {

    public TrainScheduleManager() {
    }

    public static final String NONE = "";
    private String _trainScheduleActiveId = NONE;
    private int _id = 0;
    
    public static final String LISTLENGTH_CHANGED_PROPERTY = "trainScheduleListLength"; // NOI18N
    public static final String SCHEDULE_ID_CHANGED_PROPERTY = "ActiveTrainScheduleId"; // NOI18N

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized TrainScheduleManager instance() {
        return InstanceManager.getDefault(TrainScheduleManager.class);
    }

    public void dispose() {
        _scheduleHashTable.clear();
    }

    // stores known TrainSchedule instances by id
    protected Hashtable<String, TrainSchedule> _scheduleHashTable = new Hashtable<>();

    /**
     * @return Number of schedules
     */
    public int numEntries() {
        return _scheduleHashTable.size();
    }
    
    /**
     * Sets the selected schedule id
     *
     * @param id Selected schedule id
     */
    public void setTrainScheduleActiveId(String id) {
        String old = _trainScheduleActiveId;
        _trainScheduleActiveId = id;
        if (!old.equals(id)) {
            setDirtyAndFirePropertyChange(SCHEDULE_ID_CHANGED_PROPERTY, old, id);
        }
    }

    public String getTrainScheduleActiveId() {
        return _trainScheduleActiveId;
    }
    
    public TrainSchedule getActiveSchedule() {
        return getScheduleById(getTrainScheduleActiveId());
    }

    /**
     * @param name The schedule string name to search for.
     * @return requested TrainSchedule object or null if none exists
     */
    public TrainSchedule getScheduleByName(String name) {
        TrainSchedule s;
        Enumeration<TrainSchedule> en = _scheduleHashTable.elements();
        while (en.hasMoreElements()) {
            s = en.nextElement();
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public TrainSchedule getScheduleById(String id) {
        return _scheduleHashTable.get(id);
    }

    /**
     * Finds an existing schedule or creates a new schedule if needed requires
     * schedule's name creates a unique id for this schedule
     *
     * @param name The string name of the schedule.
     *
     *
     * @return new TrainSchedule or existing TrainSchedule
     */
    public TrainSchedule newSchedule(String name) {
        TrainSchedule schedule = getScheduleByName(name);
        if (schedule == null) {
            _id++;
            schedule = new TrainSchedule(Integer.toString(_id), name);
            Integer oldSize = Integer.valueOf(_scheduleHashTable.size());
            _scheduleHashTable.put(schedule.getId(), schedule);
            setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
                    Integer.valueOf(_scheduleHashTable.size()));
        }
        return schedule;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     *
     * @param schedule The TrainSchedule to add.
     */
    public void register(TrainSchedule schedule) {
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
     * @param schedule The TrainSchedule to delete.
     */
    public void deregister(TrainSchedule schedule) {
        if (schedule == null) {
            return;
        }
        Integer oldSize = Integer.valueOf(_scheduleHashTable.size());
        _scheduleHashTable.remove(schedule.getId());
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_scheduleHashTable.size()));
    }

    /**
     * Sort by train schedule name
     *
     * @return list of train schedules ordered by name
     */
    public List<TrainSchedule> getSchedulesByNameList() {
        List<TrainSchedule> sortList = getList();
        // now re-sort
        List<TrainSchedule> out = new ArrayList<>();
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
     * Sort by train schedule id numbers
     *
     * @return list of train schedules ordered by id numbers
     */
    public List<TrainSchedule> getSchedulesByIdList() {
        List<TrainSchedule> sortList = getList();
        // now re-sort
        List<TrainSchedule> out = new ArrayList<>();
        for (int i = 0; i < sortList.size(); i++) {
            for (int j = 0; j < out.size(); j++) {
                try {
                    if (Integer.parseInt(sortList.get(i).getId()) < Integer.parseInt(out.get(j).getId())) {
                        out.add(j, sortList.get(i));
                        break;
                    }
                } catch (NumberFormatException e) {
                    log.debug("list id number isn't a number");
                }
            }
            if (!out.contains(sortList.get(i))) {
                out.add(sortList.get(i));
            }
        }
        return out;
    }

    private List<TrainSchedule> getList() {
        // no schedules? then load defaults
        if (numEntries() == 0) {
            createDefaultSchedules();
        }
        List<TrainSchedule> out = new ArrayList<>();
        Enumeration<TrainSchedule> en = _scheduleHashTable.elements();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    /**
     * Gets a JComboBox loaded with schedules starting with null.
     *
     * @return JComboBox with a list of schedules.
     */
    public JComboBox<TrainSchedule> getComboBox() {
        JComboBox<TrainSchedule> box = new JComboBox<>();
        updateComboBox(box);
        return box;
    }

    /**
     * Gets a JComboBox loaded with schedules starting with null.
     *
     * @return JComboBox with a list of schedules starting with null.
     */
    public JComboBox<TrainSchedule> getSelectComboBox() {
        JComboBox<TrainSchedule> box = new JComboBox<>();
        box.addItem(null);
        for (TrainSchedule sch : getSchedulesByIdList()) {
            box.addItem(sch);
        }
        return box;
    }

    /**
     * Update a JComboBox with the latest schedules.
     *
     * @param box the JComboBox needing an update.
     * @throws IllegalArgumentException if box is null
     */
    public void updateComboBox(JComboBox<TrainSchedule> box) {
        if (box == null) {
            throw new IllegalArgumentException("Attempt to update non-existant comboBox");
        }
        box.removeAllItems();
        for (TrainSchedule sch : getSchedulesByNameList()) {
            box.addItem(sch);
        }
    }

    public void buildSwitchLists() {
        TrainSwitchLists trainSwitchLists = new TrainSwitchLists();
        String locationName = ""; // only create switch lists once for locations with similar names
        for (Location location : InstanceManager.getDefault(LocationManager.class).getLocationsByNameList()) {
            if (location.isSwitchListEnabled() && !locationName.equals(TrainCommon.splitString(location.getName()))) {
                trainSwitchLists.buildSwitchList(location);
                // print switch lists for locations that have changes
                if (Setup.isSwitchListRealTime() && location.getStatus().equals(Location.MODIFIED)) {
                    trainSwitchLists.printSwitchList(location, InstanceManager.getDefault(TrainManager.class).isPrintPreviewEnabled());
                    locationName = TrainCommon.splitString(location.getName());
                }
            }
        }
        // set trains switch lists printed
        InstanceManager.getDefault(TrainManager.class).setTrainsSwitchListStatus(Train.PRINTED);
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-trains.dtd.
     *
     * @param root The common Element for operations-trains.dtd.
     *
     */
    public void store(Element root) {
        Element e = new Element(Xml.TRAIN_SCHEDULE_OPTIONS);
        e.setAttribute(Xml.ACTIVE_ID, InstanceManager.getDefault(TrainScheduleManager.class).getTrainScheduleActiveId());
        root.addContent(e);
        Element values = new Element(Xml.SCHEDULES);
        // add entries
        List<TrainSchedule> schedules = getSchedulesByIdList();
        for (TrainSchedule schedule : schedules) {
            values.addContent(schedule.store());
        }
        root.addContent(values);
    }

    public void load(Element root) {
        Element e = root.getChild(Xml.TRAIN_SCHEDULE_OPTIONS);
        Attribute a;
        if (e != null) {
            if ((a = e.getAttribute(Xml.ACTIVE_ID)) != null) {
                InstanceManager.getDefault(TrainScheduleManager.class).setTrainScheduleActiveId(a.getValue());
            }
        }

        e = root.getChild(Xml.SCHEDULES);
        if (e != null) {
            List<Element> eSchedules = root.getChild(Xml.SCHEDULES).getChildren(Xml.SCHEDULE);
            log.debug("TrainScheduleManager sees {} train schedules", eSchedules.size());
            for (Element eSchedule : eSchedules) {
                register(new TrainSchedule(eSchedule));
            }
        }
    }

    public void createDefaultSchedules() {
        log.debug("creating default schedules");
        newSchedule(Bundle.getMessage("Sunday"));
        newSchedule(Bundle.getMessage("Monday"));
        newSchedule(Bundle.getMessage("Tuesday"));
        newSchedule(Bundle.getMessage("Wednesday"));
        newSchedule(Bundle.getMessage("Thursday"));
        newSchedule(Bundle.getMessage("Friday"));
        newSchedule(Bundle.getMessage("Saturday"));
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("ScheduleManager sees property change: ({}) old: ({}) new ({})",
                e.getPropertyName(), e.getOldValue(), e.getNewValue());
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        InstanceManager.getDefault(TrainManagerXml.class).setDirty(true);
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(TrainScheduleManager.class);

    @Override
    public void initialize() {
        InstanceManager.getDefault(TrainManagerXml.class); // load trains
    }

}
