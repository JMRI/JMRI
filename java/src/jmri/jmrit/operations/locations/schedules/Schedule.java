package jmri.jmrit.operations.locations.schedules;

import java.text.MessageFormat;
import java.util.*;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;

/**
 * Represents a car delivery schedule for a location
 *
 * @author Daniel Boudreau Copyright (C) 2009, 2011, 2013
 */
public class Schedule extends PropertyChangeSupport implements java.beans.PropertyChangeListener {

    protected String _id = "";
    protected String _name = "";
    protected String _comment = "";

    // stores ScheduleItems for this schedule
    protected Hashtable<String, ScheduleItem> _scheduleHashTable = new Hashtable<String, ScheduleItem>();
    protected int _IdNumber = 0; // each item in a schedule gets its own id
    protected int _sequenceNum = 0; // each item has a unique sequence number

    public static final String LISTCHANGE_CHANGED_PROPERTY = "scheduleListChange"; // NOI18N
    public static final String DISPOSE = "scheduleDispose"; // NOI18N
    
    public static final String SCHEDULE_OKAY = ""; // NOI18N

    public Schedule(String id, String name) {
        log.debug("New schedule ({}) id: {}", name, id);
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
            setDirtyAndFirePropertyChange("ScheduleName", old, name); // NOI18N
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

    public int getSize() {
        return _scheduleHashTable.size();
    }

    public void setComment(String comment) {
        String old = _comment;
        _comment = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange("ScheduleComment", old, comment); // NOI18N
        }
    }

    public String getComment() {
        return _comment;
    }

    public void dispose() {
        setDirtyAndFirePropertyChange(DISPOSE, null, DISPOSE);
    }

    public void resetHitCounts() {
        for (ScheduleItem si : getItemsByIdList()) {
            si.setHits(0);
        }
    }

    /**
     * Adds a car type to the end of this schedule
     * @param type The string car type to add.
     *
     * @return ScheduleItem created for the car type added
     */
    public ScheduleItem addItem(String type) {
        _IdNumber++;
        _sequenceNum++;
        String id = _id + "c" + Integer.toString(_IdNumber);
        log.debug("Adding new item to ({}) id: {}", getName(), id);
        ScheduleItem si = new ScheduleItem(id, type);
        si.setSequenceId(_sequenceNum);
        Integer old = Integer.valueOf(_scheduleHashTable.size());
        _scheduleHashTable.put(si.getId(), si);

        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_scheduleHashTable.size()));
        // listen for set out and pick up changes to forward
        si.addPropertyChangeListener(this);
        return si;
    }

    /**
     * Add a schedule item at a specific place (sequence) in the schedule
     * Allowable sequence numbers are 0 to max size of schedule. 0 = start of
     * list.
     * @param carType The string car type name to add.
     * @param sequence Where in the schedule to add the item.
     *
     * @return schedule item
     */
    public ScheduleItem addItem(String carType, int sequence) {
        ScheduleItem si = addItem(carType);
        if (sequence < 0 || sequence > _scheduleHashTable.size()) {
            return si;
        }
        for (int i = 0; i < _scheduleHashTable.size() - sequence - 1; i++) {
            moveItemUp(si);
        }
        return si;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     * @param si The schedule item to add.
     */
    public void register(ScheduleItem si) {
        Integer old = Integer.valueOf(_scheduleHashTable.size());
        _scheduleHashTable.put(si.getId(), si);

        // find last id created
        String[] getId = si.getId().split("c");
        int id = Integer.parseInt(getId[1]);
        if (id > _IdNumber) {
            _IdNumber = id;
        }
        // find highest sequence number
        if (si.getSequenceId() > _sequenceNum) {
            _sequenceNum = si.getSequenceId();
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_scheduleHashTable.size()));
        // listen for set out and pick up changes to forward
        si.addPropertyChangeListener(this);
    }

    /**
     * Delete a ScheduleItem
     * @param si The scheduleItem to delete.
     *
     */
    public void deleteItem(ScheduleItem si) {
        if (si != null) {
            si.removePropertyChangeListener(this);
            // subtract from the items's available track length
            String id = si.getId();
            si.dispose();
            Integer old = Integer.valueOf(_scheduleHashTable.size());
            _scheduleHashTable.remove(id);
            resequenceIds();
            setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_scheduleHashTable.size()));
        }
    }

    /**
     * Reorder the item sequence numbers for this schedule
     */
    private void resequenceIds() {
        List<ScheduleItem> scheduleItems = getItemsBySequenceList();
        for (int i = 0; i < scheduleItems.size(); i++) {
            scheduleItems.get(i).setSequenceId(i + 1); // start sequence numbers at 1
            _sequenceNum = i + 1;
        }
    }

    /**
     * Get item by car type (gets last schedule item with this type)
     * @param carType The string car type to search for.
     *
     * @return schedule item
     */
    public ScheduleItem getItemByType(String carType) {
        List<ScheduleItem> scheduleSequenceList = getItemsBySequenceList();
        ScheduleItem si;

        for (int i = scheduleSequenceList.size() - 1; i >= 0; i--) {
            si = scheduleSequenceList.get(i);
            if (si.getTypeName().equals(carType)) {
                return si;
            }
        }
        return null;
    }

    /**
     * Get a ScheduleItem by id
     * @param id The string id of the ScheduleItem.
     *
     * @return schedule item
     */
    public ScheduleItem getItemById(String id) {
        return _scheduleHashTable.get(id);
    }

    private List<ScheduleItem> getItemsByIdList() {
        String[] arr = new String[_scheduleHashTable.size()];
        List<ScheduleItem> out = new ArrayList<ScheduleItem>();
        Enumeration<String> en = _scheduleHashTable.keys();
        int i = 0;
        while (en.hasMoreElements()) {
            arr[i++] = en.nextElement();
        }
        Arrays.sort(arr);
        for (i = 0; i < arr.length; i++) {
            out.add(getItemById(arr[i]));
        }
        return out;
    }

    /**
     * Get a list of ScheduleItems sorted by schedule order
     *
     * @return list of ScheduleItems ordered by sequence
     */
    public List<ScheduleItem> getItemsBySequenceList() {
        // first get id list
        List<ScheduleItem> sortList = getItemsByIdList();
        // now re-sort
        List<ScheduleItem> out = new ArrayList<ScheduleItem>();

        for (ScheduleItem si : sortList) {
            for (int j = 0; j < out.size(); j++) {
                if (si.getSequenceId() < out.get(j).getSequenceId()) {
                    out.add(j, si);
                    break;
                }
            }
            if (!out.contains(si)) {
                out.add(si);
            }
        }
        return out;
    }

    /**
     * Places a ScheduleItem earlier in the schedule
     * @param si The ScheduleItem to move.
     *
     */
    public void moveItemUp(ScheduleItem si) {
        int sequenceId = si.getSequenceId();
        if (sequenceId - 1 <= 0) {
            si.setSequenceId(_sequenceNum + 1); // move to the end of the list
            resequenceIds();
        } else {
            // adjust the other item taken by this one
            ScheduleItem replaceSi = getItemBySequenceId(sequenceId - 1);
            if (replaceSi != null) {
                replaceSi.setSequenceId(sequenceId);
                si.setSequenceId(sequenceId - 1);
            } else {
                resequenceIds(); // error the sequence number is missing
            }
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, Integer.toString(sequenceId));
    }

    /**
     * Places a ScheduleItem later in the schedule
     * @param si The ScheduleItem to move.
     *
     */
    public void moveItemDown(ScheduleItem si) {
        int sequenceId = si.getSequenceId();
        if (sequenceId + 1 > _sequenceNum) {
            si.setSequenceId(0); // move to the start of the list
            resequenceIds();
        } else {
            // adjust the other item taken by this one
            ScheduleItem replaceSi = getItemBySequenceId(sequenceId + 1);
            if (replaceSi != null) {
                replaceSi.setSequenceId(sequenceId);
                si.setSequenceId(sequenceId + 1);
            } else {
                resequenceIds(); // error the sequence number is missing
            }
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, Integer.toString(sequenceId));
    }

    public ScheduleItem getItemBySequenceId(int sequenceId) {
        for (ScheduleItem si : getItemsByIdList()) {
            if (si.getSequenceId() == sequenceId) {
                return si;
            }
        }
        return null;
    }
    
    /**
     * Check to see if schedule is valid for the track.
     * @param track The track associated with this schedule
     *
     * @return SCHEDULE_OKAY if schedule okay, otherwise an error message.
     */
    public String checkScheduleValid(Track track) {
        String status = SCHEDULE_OKAY;
        List<ScheduleItem> scheduleItems = getItemsBySequenceList();
        if (scheduleItems.size() == 0) {
            return Bundle.getMessage("empty");
        }
        for (ScheduleItem si : scheduleItems) {
            // check train schedules
            if (!si.getSetoutTrainScheduleId().equals(ScheduleItem.NONE) &&
                    InstanceManager.getDefault(TrainScheduleManager.class)
                            .getScheduleById(si.getSetoutTrainScheduleId()) == null) {
                status = MessageFormat.format(Bundle.getMessage("NotValid"),
                        new Object[] { si.getSetoutTrainScheduleId() });
                break;
            }
            if (!si.getPickupTrainScheduleId().equals(ScheduleItem.NONE) &&
                    InstanceManager.getDefault(TrainScheduleManager.class)
                            .getScheduleById(si.getPickupTrainScheduleId()) == null) {
                status = MessageFormat.format(Bundle.getMessage("NotValid"),
                        new Object[] { si.getPickupTrainScheduleId() });
                break;
            }
            if (!track.getLocation().acceptsTypeName(si.getTypeName())) {
                status = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si.getTypeName() });
                break;
            }
            if (!track.isTypeNameAccepted(si.getTypeName())) {
                status = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si.getTypeName() });
                break;
            }
            // check roads, accepted by track, valid road, and there's at least one car with
            // that road
            if (!si.getRoadName().equals(ScheduleItem.NONE) &&
                    (!track.isRoadNameAccepted(si.getRoadName()) ||
                            !InstanceManager.getDefault(CarRoads.class).containsName(si.getRoadName()) ||
                            InstanceManager.getDefault(CarManager.class).getByTypeAndRoad(si.getTypeName(),
                                    si.getRoadName()) == null)) {
                status = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si.getRoadName() });
                break;
            }
            // check loads
            List<String> loads = InstanceManager.getDefault(CarLoads.class).getNames(si.getTypeName());
            if (!si.getReceiveLoadName().equals(ScheduleItem.NONE) &&
                    (!track.isLoadNameAndCarTypeAccepted(si.getReceiveLoadName(), si.getTypeName()) ||
                            !loads.contains(si.getReceiveLoadName()))) {
                status = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si.getReceiveLoadName() });
                break;
            }
            if (!si.getShipLoadName().equals(ScheduleItem.NONE) && !loads.contains(si.getShipLoadName())) {
                status = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si.getShipLoadName() });
                break;
            }
            // check destination
            if (si.getDestination() != null &&
                    (!si.getDestination().acceptsTypeName(si.getTypeName()) ||
                            InstanceManager.getDefault(LocationManager.class)
                                    .getLocationById(si.getDestination().getId()) == null)) {
                status = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si.getDestination() });
                break;
            }
            // check destination track
            if (si.getDestination() != null && si.getDestinationTrack() != null) {
                if (!si.getDestination().isTrackAtLocation(si.getDestinationTrack())) {
                    status = MessageFormat.format(Bundle.getMessage("NotValid"),
                            new Object[] { si.getDestinationTrack() + " (" + Bundle.getMessage("Track") + ")" });
                    break;
                }
                if (!si.getDestinationTrack().isTypeNameAccepted(si.getTypeName())) {
                    status = MessageFormat.format(Bundle.getMessage("NotValid"),
                            new Object[] { si.getDestinationTrack() + " (" + Bundle.getMessage("Type") + ")" });
                    break;
                }
                if (!si.getRoadName().equals(ScheduleItem.NONE) &&
                        !si.getDestinationTrack().isRoadNameAccepted(si.getRoadName())) {
                    status = MessageFormat.format(Bundle.getMessage("NotValid"),
                            new Object[] { si.getDestinationTrack() + " (" + Bundle.getMessage("Road") + ")" });
                    break;
                }
                if (!si.getShipLoadName().equals(ScheduleItem.NONE) &&
                        !si.getDestinationTrack().isLoadNameAndCarTypeAccepted(si.getShipLoadName(),
                                si.getTypeName())) {
                    status = MessageFormat.format(Bundle.getMessage("NotValid"),
                            new Object[] { si.getDestinationTrack() + " (" + Bundle.getMessage("Load") + ")" });
                    break;
                }
            }
        }
        return status;
    }
    
    private static boolean debugFlag = false;
    
    /*
     * Match mode search
     */
    public String searchSchedule(Car car, Track track) {
        if (debugFlag) {
            log.debug("Search match for car ({}) type ({}) load ({})", car.toString(), car.getTypeName(),
                    car.getLoadName());
        }
        // has the car already been assigned a schedule item? Then verify that its still
        // okay
        if (!car.getScheduleItemId().equals(Track.NONE)) {
            ScheduleItem si = getItemById(car.getScheduleItemId());
            if (si != null) {
                String status = checkScheduleItem(si, car, track);
                if (status.equals(Track.OKAY)) {
                    return Track.OKAY;
                }
                log.debug("Car ({}) with schedule id ({}) failed check, status: {}", car.toString(),
                        car.getScheduleItemId(), status);
            }
        }
        // search schedule for a match
        for (int i = 0; i < getSize(); i++) {
            ScheduleItem si = track.getNextScheduleItem();
            if (debugFlag) {
                log.debug("Item id: ({}) requesting type ({}) load ({}) final dest ({}, {})", si.getId(),
                        si.getTypeName(), si.getReceiveLoadName(), si.getDestinationName(),
                        si.getDestinationTrackName()); // NOI18N
            }
            String status = checkScheduleItem(si, car, track);
            if (status.equals(Track.OKAY)) {
                log.debug("Found item match ({}) car ({}) type ({}) load ({}) ship ({}) destination ({}, {})",
                        si.getId(), car.toString(), car.getTypeName(), si.getReceiveLoadName(), si.getShipLoadName(),
                        si.getDestinationName(), si.getDestinationTrackName()); // NOI18N
                car.setScheduleItemId(si.getId()); // remember which item was a match
                return Track.OKAY;
            } else {
                if (debugFlag) {
                    log.debug("Item id: ({}) status ({})", si.getId(), status);
                }
            }
        }
        if (debugFlag) {
            log.debug("No Match");
        }
        car.setScheduleItemId(Car.NONE); // clear the car's schedule id
        return Track.SCHEDULE + " " + Bundle.getMessage("noMatch");
    }
    
    public String checkScheduleItem(ScheduleItem si, Car car, Track track) {
        // if car is already assigned to this schedule item allow it to be dropped off
        // on the wrong day (car arrived late)
        if (!car.getScheduleItemId().equals(si.getId()) &&
                !si.getSetoutTrainScheduleId().equals(ScheduleItem.NONE) &&
                !InstanceManager.getDefault(TrainScheduleManager.class).getTrainScheduleActiveId()
                        .equals(si.getSetoutTrainScheduleId())) {
            TrainSchedule trainSch = InstanceManager.getDefault(TrainScheduleManager.class)
                    .getScheduleById(si.getSetoutTrainScheduleId());
            if (trainSch != null) {
                return Track.SCHEDULE +
                        " (" +
                        getName() +
                        ") " +
                        Bundle.getMessage("requestCarOnly") +
                        " (" +
                        trainSch.getName() +
                        ")";
            }
        }
        // Check for correct car type, road, load
        if (!car.getTypeName().equals(si.getTypeName())) {
            return Track.SCHEDULE +
                    " (" +
                    getName() +
                    ") " +
                    Bundle.getMessage("requestCar") +
                    " " +
                    Track.TYPE +
                    " (" +
                    si.getTypeName() +
                    ")";
        }
        if (!si.getRoadName().equals(ScheduleItem.NONE) && !car.getRoadName().equals(si.getRoadName())) {
            return Track.SCHEDULE +
                    " (" +
                    getName() +
                    ") " +
                    Bundle.getMessage("requestCar") +
                    " " +
                    Track.TYPE +
                    " (" +
                    si.getTypeName() +
                    ") " +
                    Track.ROAD +
                    " (" +
                    si.getRoadName() +
                    ")";
        }
        if (!si.getReceiveLoadName().equals(ScheduleItem.NONE) && !car.getLoadName().equals(si.getReceiveLoadName())) {
            return Track.SCHEDULE +
                    " (" +
                    getName() +
                    ") " +
                    Bundle.getMessage("requestCar") +
                    " " +
                    Track.TYPE +
                    " (" +
                    si.getTypeName() +
                    ") " +
                    Track.LOAD +
                    " (" +
                    si.getReceiveLoadName() +
                    ")";
        }
        // don't try the random feature if car is already assigned to this schedule item
        if (car.getFinalDestinationTrack() != track &&
                !si.getRandom().equals(ScheduleItem.NONE) &&
                !car.getScheduleItemId().equals(si.getId())) {
            try {
                int value = Integer.parseInt(si.getRandom());
                double random = 100 * Math.random();
                log.debug("Selected random {}, created random {}", si.getRandom(), random);
                if (random > value) {
                    return MessageFormat.format(Bundle.getMessage("scheduleRandom"),
                            new Object[] { Track.SCHEDULE, getName(), si.getId(), value, random });
                }
            } catch (NumberFormatException e) {
                log.error("Random value {} isn't a number", si.getRandom());
            }
        }
        return Track.OKAY;
    }
    
    public boolean checkScheduleAttribute(String attribute, String carType, Car car) {
        List<ScheduleItem> scheduleItems = getItemsBySequenceList();
        for (ScheduleItem si : scheduleItems) {
            if (si.getTypeName().equals(carType)) {
                // check to see if schedule services car type
                if (attribute.equals(Track.TYPE)) {
                    return true;
                }
                // check to see if schedule services car type and load
                if (attribute.equals(Track.LOAD) &&
                        (si.getReceiveLoadName().equals(ScheduleItem.NONE) ||
                                car == null ||
                                si.getReceiveLoadName().equals(car.getLoadName()))) {
                    return true;
                }
                // check to see if schedule services car type and road
                if (attribute.equals(Track.ROAD) &&
                        (si.getRoadName().equals(ScheduleItem.NONE) ||
                                car == null ||
                                si.getRoadName().equals(car.getRoadName()))) {
                    return true;
                }
                // check to see if train schedule allows delivery
                if (attribute.equals(Track.TRAIN_SCHEDULE) &&
                        (si.getSetoutTrainScheduleId().isEmpty() ||
                                InstanceManager.getDefault(TrainScheduleManager.class).getTrainScheduleActiveId()
                                        .equals(si.getSetoutTrainScheduleId()))) {
                    return true;
                }
                // check to see if at least one schedule item can service car
                if (attribute.equals(Track.ALL) &&
                        (si.getReceiveLoadName().equals(ScheduleItem.NONE) ||
                                car == null ||
                                si.getReceiveLoadName().equals(car.getLoadName())) &&
                        (si.getRoadName().equals(ScheduleItem.NONE) ||
                                car == null ||
                                si.getRoadName().equals(car.getRoadName())) &&
                        (si.getSetoutTrainScheduleId().equals(ScheduleItem.NONE) ||
                                InstanceManager.getDefault(TrainScheduleManager.class).getTrainScheduleActiveId()
                                        .equals(si.getSetoutTrainScheduleId()))) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in operations-config.xml
     *
     * @param e Consist XML element
     */
    public Schedule(Element e) {
        org.jdom2.Attribute a;
        if ((a = e.getAttribute(Xml.ID)) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in schedule element when reading operations");
        }
        if ((a = e.getAttribute(Xml.NAME)) != null) {
            _name = a.getValue();
        }
        if ((a = e.getAttribute(Xml.COMMENT)) != null) {
            _comment = a.getValue();
        }
        if (e.getChildren(Xml.ITEM) != null) {
            List<Element> eScheduleItems = e.getChildren(Xml.ITEM);
            log.debug("schedule: {} has {} items", getName(), eScheduleItems.size());
            for (Element eScheduleItem : eScheduleItems) {
                register(new ScheduleItem(eScheduleItem));
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-config.xml.
     *
     * @return Contents in a JDOM Element
     */
    public org.jdom2.Element store() {
        Element e = new org.jdom2.Element(Xml.SCHEDULE);
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.NAME, getName());
        e.setAttribute(Xml.COMMENT, getComment());
        for (ScheduleItem si : getItemsBySequenceList()) {
            e.addContent(si.store());
        }

        return e;
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        // forward all schedule item changes
        setDirtyAndFirePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // set dirty
        InstanceManager.getDefault(LocationManagerXml.class).setDirty(true);
        firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(Schedule.class);

}
