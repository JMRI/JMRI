package jmri.jmrit.operations.trains.manualtrainbuilder;

import java.util.*;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Train Manual Build. Allows a user to manually assign cars to a train.
 *
 * @author Daniel Boudreau Copyright (C) 2026
 */
public class TrainManualBuild extends PropertyChangeSupport implements java.beans.PropertyChangeListener {

    protected String _id = "";
    protected String _trainId = "";
    protected String _comment = "";

    // stores manual build items for this manual build
    protected Hashtable<String, TrainManualBuildItem> _manualBuildHashTable =
            new Hashtable<String, TrainManualBuildItem>();
    protected int _IdNumber = 0; // each item in a manual build gets its own id
    protected int _sequenceNum = 0; // each item has a unique sequence number

    public static final String LISTCHANGE_CHANGED_PROPERTY = "manualBuildListChange"; // NOI18N
    public static final String DISPOSE = "manualBuildDispose"; // NOI18N

    public TrainManualBuild(String id, String trainId) {
        log.debug("New manual build ({}) id: {}", trainId, id);
        _trainId = trainId;
        _id = id;
    }

    public String getId() {
        return _id;
    }

    public void setTrainId(String trainId) {
        String old = _trainId;
        _trainId = trainId;
        if (!old.equals(trainId)) {
            setDirtyAndFirePropertyChange("ManualBuildId", old, trainId); // NOI18N
        }
    }

    public String getTrainId() {
        return _trainId;
    }

    public String getTrainName() {
        TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
        Train train = trainManager.getTrainById(getTrainId());
        if (train != null) {
            return train.getName();
        }
        return "";
    }

    public int getSize() {
        return _manualBuildHashTable.size();
    }

    public void setComment(String comment) {
        String old = _comment;
        _comment = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange("ManualBuildComment", old, comment); // NOI18N
        }
    }

    public String getComment() {
        return _comment;
    }

    public void dispose() {
        setDirtyAndFirePropertyChange(DISPOSE, null, DISPOSE);
    }

    /**
     * Adds build item to the end of this manual build
     * 
     * @return ManualBuildItem created
     */
    public TrainManualBuildItem addItem() {
        _IdNumber++;
        _sequenceNum++;
        String id = _id + "m" + Integer.toString(_IdNumber);
        log.debug("Adding new item to ({}) id: {}", getTrainId(), id);
        TrainManualBuildItem mbi = new TrainManualBuildItem(id);
        mbi.setSequenceId(_sequenceNum);
        int old = _manualBuildHashTable.size();
        _manualBuildHashTable.put(mbi.getId(), mbi);

        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, _manualBuildHashTable.size());
        // listen for set out and pick up changes to forward
        mbi.addPropertyChangeListener(this);
        return mbi;
    }

    /**
     * Add a manual build item at a specific place (sequence) in the manual
     * build. Allowable sequence numbers are 0 to max size of manual build. 0 =
     * start of list.
     * 
     * @param sequence Where in the Manual Build to add the item.
     * @return manual build item
     */
    public TrainManualBuildItem addItem(int sequence) {
        TrainManualBuildItem mbi = addItem();
        if (sequence < 0 || sequence > _manualBuildHashTable.size()) {
            return mbi;
        }
        for (int i = 0; i < _manualBuildHashTable.size() - sequence - 1; i++) {
            moveItemUp(mbi);
        }
        return mbi;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     * 
     * @param mbi The manual build item to add.
     */
    public void register(TrainManualBuildItem mbi) {
        int old = _manualBuildHashTable.size();
        _manualBuildHashTable.put(mbi.getId(), mbi);

        // find last id created
        String[] getId = mbi.getId().split("m");
        int id = Integer.parseInt(getId[1]);
        if (id > _IdNumber) {
            _IdNumber = id;
        }
        // find highest sequence number
        if (mbi.getSequenceId() > _sequenceNum) {
            _sequenceNum = mbi.getSequenceId();
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, _manualBuildHashTable.size());
        // listen for set out and pick up changes to forward
        mbi.addPropertyChangeListener(this);
    }

    /**
     * Delete a manual build item
     * 
     * @param mbi The manual build item to delete.
     */
    public void deleteItem(TrainManualBuildItem mbi) {
        if (mbi != null) {
            mbi.removePropertyChangeListener(this);
            // subtract from the items's available track length
            String id = mbi.getId();
            mbi.dispose();
            int old = _manualBuildHashTable.size();
            _manualBuildHashTable.remove(id);
            resequenceIds();
            setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, _manualBuildHashTable.size());
        }
    }

    /**
     * Reorder the item sequence numbers for this manual build
     */
    private void resequenceIds() {
        List<TrainManualBuildItem> manualBuildItems = getItemsBySequenceList();
        for (int i = 0; i < manualBuildItems.size(); i++) {
            // start sequence numbers at 1
            manualBuildItems.get(i).setSequenceId(i + 1);
            _sequenceNum = i + 1;
        }
    }

    /**
     * Get a ManualBuildItem by id
     * 
     * @param id The string id of the ManualBuildItem.
     * @return manual build item
     */
    public TrainManualBuildItem getItemById(String id) {
        return _manualBuildHashTable.get(id);
    }

    private List<TrainManualBuildItem> getItemsByIdList() {
        String[] arr = new String[_manualBuildHashTable.size()];
        List<TrainManualBuildItem> out = new ArrayList<TrainManualBuildItem>();
        Enumeration<String> en = _manualBuildHashTable.keys();
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
     * Get a list of ManualBuildItems sorted by sequence order
     *
     * @return list of ManualBuildItems ordered by sequence
     */
    public List<TrainManualBuildItem> getItemsBySequenceList() {
        // first get id list
        List<TrainManualBuildItem> sortList = getItemsByIdList();
        // now re-sort
        List<TrainManualBuildItem> out = new ArrayList<TrainManualBuildItem>();

        for (TrainManualBuildItem mbi : sortList) {
            for (int j = 0; j < out.size(); j++) {
                if (mbi.getSequenceId() < out.get(j).getSequenceId()) {
                    out.add(j, mbi);
                    break;
                }
            }
            if (!out.contains(mbi)) {
                out.add(mbi);
            }
        }
        return out;
    }

    /**
     * Places a ManualBuildItem earlier in the manual build
     * 
     * @param mbi The ManualBuildItem to move.
     */
    public void moveItemUp(TrainManualBuildItem mbi) {
        int sequenceId = mbi.getSequenceId();
        if (sequenceId - 1 <= 0) {
            mbi.setSequenceId(_sequenceNum + 1); // move to the end of the list
            resequenceIds();
        } else {
            // adjust the other item taken by this one
            TrainManualBuildItem replaceSi = getItemBySequenceId(sequenceId - 1);
            if (replaceSi != null) {
                replaceSi.setSequenceId(sequenceId);
                mbi.setSequenceId(sequenceId - 1);
            } else {
                resequenceIds(); // error the sequence number is missing
            }
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, sequenceId);
    }

    /**
     * Places a ManualBuildItem later in the manualBuild
     * 
     * @param mbi The ManualBuildItem to move.
     */
    public void moveItemDown(TrainManualBuildItem mbi) {
        int sequenceId = mbi.getSequenceId();
        if (sequenceId + 1 > _sequenceNum) {
            mbi.setSequenceId(0); // move to the start of the list
            resequenceIds();
        } else {
            // adjust the other item taken by this one
            TrainManualBuildItem replaceSi = getItemBySequenceId(sequenceId + 1);
            if (replaceSi != null) {
                replaceSi.setSequenceId(sequenceId);
                mbi.setSequenceId(sequenceId + 1);
            } else {
                resequenceIds(); // error the sequence number is missing
            }
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, sequenceId);
    }

    public TrainManualBuildItem getItemBySequenceId(int sequenceId) {
        for (TrainManualBuildItem mbi : getItemsByIdList()) {
            if (mbi.getSequenceId() == sequenceId) {
                return mbi;
            }
        }
        return null;
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in operations-config.xml
     *
     * @param e Consist XML element
     */
    public TrainManualBuild(Element e) {
        org.jdom2.Attribute a;
        if ((a = e.getAttribute(Xml.ID)) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in manualBuild element when reading operations");
        }
        if ((a = e.getAttribute(Xml.TRAIN_ID)) != null) {
            _trainId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.COMMENT)) != null) {
            _comment = a.getValue();
        }
        if (e.getChildren(Xml.MANUAL_BUILD_ITEM) != null) {
            List<Element> eManualBuildItems = e.getChildren(Xml.MANUAL_BUILD_ITEM);
            log.debug("manualBuild: {} has {} items", getTrainId(), eManualBuildItems.size());
            for (Element eManualBuildItem : eManualBuildItems) {
                register(new TrainManualBuildItem(eManualBuildItem));
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
        Element e = new org.jdom2.Element(Xml.MANUAL_BUILD);
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.TRAIN_ID, getTrainId());
        e.setAttribute(Xml.NAME, getTrainName());
        e.setAttribute(Xml.COMMENT, getComment());
        for (TrainManualBuildItem mbi : getItemsBySequenceList()) {
            e.addContent(mbi.store());
        }

        return e;
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        // forward all manualBuild item changes
        setDirtyAndFirePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // set dirty
        InstanceManager.getDefault(LocationManagerXml.class).setDirty(true);
        firePropertyChange(p, old, n);
    }

    private static final Logger log = LoggerFactory.getLogger(TrainManualBuild.class);

}
