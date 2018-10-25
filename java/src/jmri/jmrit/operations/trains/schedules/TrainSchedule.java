package jmri.jmrit.operations.trains.schedules;

import java.util.ArrayList;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.TrainManagerXml;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a schedule for trains. For example, can be a day of the week. Useful when
 * determining which trains to build and run.
 *
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class TrainSchedule {
    
    public static final String NONE = "";
    public static final String ANY = "ANY"; // allow cars to be picked up any day of the week NOI18N

    public static final String NAME_CHANGED_PROPERTY = "trainScheduleName"; // NOI18N
    public static final String SCHEDULE_CHANGED_PROPERTY = "trainScheduleChanged"; // NOI18N

    protected String _id = NONE;
    protected String _name = NONE;
    protected String _comment = NONE;
    protected List<String> _trainIds = new ArrayList<>();

    public TrainSchedule(String id, String name) {
        log.debug("New train schedule ({}) id: {}", name, id);
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
            setDirtyAndFirePropertyChange(NAME_CHANGED_PROPERTY, old, name);
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
            setDirtyAndFirePropertyChange("AddTrainScheduleComment", old, comment); // NOI18N
        }
    }

    public String getComment() {
        return _comment;
    }

    /**
     * Adds the train id for a train that needs to be built
     * @param id The train id
     */
    public void addTrainId(String id) {
        if (!_trainIds.contains(id)) {
            _trainIds.add(id);
            setDirtyAndFirePropertyChange(SCHEDULE_CHANGED_PROPERTY, null, id);
        }
    }

    /**
     * Removes the train id for a train that needs to be built
     * @param id The train id
     */
    public void removeTrainId(String id) {
        _trainIds.remove(id);
        setDirtyAndFirePropertyChange(SCHEDULE_CHANGED_PROPERTY, id, null);
    }

    /**
     * Used to determine if train is to be built using this schedule
     * @param id the id of the train to be tested
     * @return true if this train's build enable should be set
     */
    public boolean containsTrainId(String id) {
        return _trainIds.contains(id);
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in operations-trains.xml
     *
     * @param e Consist XML element
     */
    public TrainSchedule(Element e) {
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
        if ((a = e.getAttribute(Xml.TRAIN_IDS)) != null) {
            String ids = a.getValue();
            String[] trainIds = ids.split(",");
            for (String id : trainIds) {
                _trainIds.add(id);
            }
//    log.debug("Train schedule " + getName() + " trainIds: " + ids);
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-config.xml.
     *
     * @return Contents in a JDOM Element
     */
    public Element store() {
        Element e = new org.jdom2.Element(Xml.SCHEDULE);
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.NAME, getName());
        if (!getComment().equals(NONE)) {
            e.setAttribute(Xml.COMMENT, getComment());
        }
        // store train ids
        StringBuilder buf = new StringBuilder();
        for (String id : _trainIds) {
            buf.append(id + ",");
        }
        e.setAttribute(Xml.TRAIN_IDS, buf.toString());
        return e;
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
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
        InstanceManager.getDefault(TrainManagerXml.class).setDirty(true);
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(TrainSchedule.class);

}
