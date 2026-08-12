package jmri.jmrit.operations.trains.manualtrainbuilder;

import java.beans.PropertyChangeListener;
import java.util.*;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.*;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;

/**
 * Manages train manual builds
 *
 * @author Daniel Boudreau Copyright (C) 2026
 */
public class TrainManualBuildManager extends PropertyChangeSupport implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize, PropertyChangeListener {

    public static final String LISTLENGTH_CHANGED_PROPERTY = "manualBuildListLength"; // NOI18N

    public TrainManualBuildManager() {
    }

    private int _id = 0;

    public void dispose() {
        _manualBuildHashTable.clear();
    }

    // stores known ManualBuild instances by id
    protected Hashtable<String, TrainManualBuild> _manualBuildHashTable = new Hashtable<String, TrainManualBuild>();

    /**
     * @return Number of manual builds
     */
    public int numEntries() {
        return _manualBuildHashTable.size();
    }

    /**
     * @param trainId The train id for the manual build
     * @return requested ManualBuild object or null if none exists
     */
    public TrainManualBuild getManualBuildByTrainId(String trainId) {
        TrainManualBuild mb;
        Enumeration<TrainManualBuild> en = _manualBuildHashTable.elements();
        while (en.hasMoreElements()) {
            mb = en.nextElement();
            if (mb.getTrainId().equals(trainId)) {
                return mb;
            }
        }
        return null;
    }

    public TrainManualBuild getManualBuildById(String id) {
        return _manualBuildHashTable.get(id);
    }

    /**
     * Finds an existing manual build or creates a new manual build if needed.
     *
     * @param trainId The train id for this manual build
     *
     *
     * @return new manual build or existing manual build
     */
    public TrainManualBuild newManualBuild(String trainId) {
        TrainManualBuild mb = getManualBuildByTrainId(trainId);
        if (mb == null && !trainId.isBlank()) {
            _id++;
            mb = new TrainManualBuild(Integer.toString(_id), trainId);
            int oldSize = _manualBuildHashTable.size();
            _manualBuildHashTable.put(mb.getId(), mb);
            setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, _manualBuildHashTable.size());
        }
        return mb;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     *
     * @param manualBuild The ManualBuild to add.
     */
    public void register(TrainManualBuild manualBuild) {
        int oldSize = _manualBuildHashTable.size();
        _manualBuildHashTable.put(manualBuild.getId(), manualBuild);
        // find last id created
        int id = Integer.parseInt(manualBuild.getId());
        if (id > _id) {
            _id = id;
        }
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, _manualBuildHashTable.size());
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     *
     * @param manualBuild The ManualBuild to delete.
     */
    public void deregister(TrainManualBuild manualBuild) {
        if (manualBuild == null) {
            return;
        }
        manualBuild.dispose();
        int oldSize = _manualBuildHashTable.size();
        _manualBuildHashTable.remove(manualBuild.getId());
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, _manualBuildHashTable.size());
    }

    /**
     * Sort by manual build train names
     *
     * @return list of manual builds ordered by train names
     */
    public List<TrainManualBuild> getManualBuildsByTrainNameList() {
        List<TrainManualBuild> sortList = getList();
        // now re-sort
        List<TrainManualBuild> out = new ArrayList<TrainManualBuild>();
        for (TrainManualBuild mb : sortList) {
            for (int j = 0; j < out.size(); j++) {
                if (mb.getTrainName().compareToIgnoreCase(out.get(j).getTrainName()) < 0) {
                    out.add(j, mb);
                    break;
                }
            }
            if (!out.contains(mb)) {
                out.add(mb);
            }
        }
        return out;

    }

    /**
     * Sort by manual build id number
     *
     * @return list of manual builds ordered by id number
     */
    public List<TrainManualBuild> getManualBuildsByIdList() {
        List<TrainManualBuild> sortList = getList();
        // now re-sort
        List<TrainManualBuild> out = new ArrayList<TrainManualBuild>();
        for (TrainManualBuild mb : sortList) {
            for (int j = 0; j < out.size(); j++) {
                try {
                    if (Integer.parseInt(mb.getId()) < Integer.parseInt(out.get(j).getId())) {
                        out.add(j, mb);
                        break;
                    }
                } catch (NumberFormatException e) {
                    log.debug("list id number isn't a number");
                }
            }
            if (!out.contains(mb)) {
                out.add(mb);
            }
        }
        return out;
    }

    private List<TrainManualBuild> getList() {
        List<TrainManualBuild> out = new ArrayList<TrainManualBuild>();
        Enumeration<TrainManualBuild> en = _manualBuildHashTable.elements();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    public TrainManualBuild copyManualBuild(TrainManualBuild manualBuild, String newManualBuildName) {
        TrainManualBuild newManualBuild = newManualBuild(newManualBuildName);
        for (TrainManualBuildItem mbi : manualBuild.getItemsBySequenceList()) {
            TrainManualBuildItem newMbi = newManualBuild.addItem();
            newMbi.copyManualBuildItem(mbi);
        }
        return newManualBuild;
    }

 
    /**
     * Replaces car type in all manual builds.
     *
     * @param oldType car type to be replaced.
     * @param newType replacement car type.
     */
    public void replaceType(String oldType, String newType) {
        for (TrainManualBuild sch : getList()) {
            for (TrainManualBuildItem si : sch.getItemsBySequenceList()) {
                if (si.getTypeName().equals(oldType)) {
                    si.setTypeName(newType);
                }
            }
        }
    }

    /**
     * Replaces car roads in all manual builds.
     *
     * @param oldRoad car road to be replaced.
     * @param newRoad replacement car road.
     */
    public void replaceRoad(String oldRoad, String newRoad) {
        if (newRoad == null) {
            return;
        }
        for (TrainManualBuild mb : getList()) {
            for (TrainManualBuildItem mbi : mb.getItemsBySequenceList()) {
                if (mbi.getRoadName().equals(oldRoad)) {
                    mbi.setRoadName(newRoad);
                }
            }
        }
    }

    /**
     * Replaces car loads in all manual builds with specific car type.
     *
     * @param type    car type.
     * @param oldLoad car load to be replaced.
     * @param newLoad replacement car load.
     */
    public void replaceLoad(String type, String oldLoad, String newLoad) {
        for (TrainManualBuild mb : getList()) {
            for (TrainManualBuildItem mbi : mb.getItemsBySequenceList()) {
                if (mbi.getTypeName().equals(type) && mbi.getLoadName().equals(oldLoad)) {
                    if (newLoad != null) {
                        mbi.setLoadName(newLoad);
                    } else {
                        mbi.setLoadName(TrainManualBuildItem.NONE);
                    }
                }
            }
        }
    }

    public void replaceTrack(Track oldTrack, Track newTrack) {
        for (TrainManualBuild mb : getList()) {
            for (TrainManualBuildItem mbi : mb.getItemsBySequenceList()) {
                if (mbi.getDestinationTrack() == oldTrack) {
                    mbi.setDestination(newTrack.getLocation());
                    mbi.setDestinationTrack(newTrack);
                }
            }
        }
    }

    public void load(Element root) {
        if (root.getChild(Xml.MANUAL_BUILDS) != null) {
            List<Element> eManualBuilds = root.getChild(Xml.MANUAL_BUILDS).getChildren(Xml.MANUAL_BUILD);
            log.debug("readFile sees {} manual builds", eManualBuilds.size());
            for (Element eManualBuild : eManualBuilds) {
                register(new TrainManualBuild(eManualBuild));
            }
        }
    }

    public void store(Element root) {
        Element values;
        root.addContent(values = new Element(Xml.MANUAL_BUILDS));
        // add entries
        for (TrainManualBuild manualBuild : getManualBuildsByIdList()) {
            values.addContent(manualBuild.store());
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

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // set dirty
        InstanceManager.getDefault(LocationManagerXml.class).setDirty(true);
        firePropertyChange(p, old, n);
    }

    private static final Logger log = LoggerFactory.getLogger(TrainManualBuildManager.class);

    @Override
    public void initialize() {
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);
    }

}
