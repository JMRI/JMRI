package jmri.jmrit.operations.rollingstock.cars;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;

/**
 * Represents the road names that cars can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 */
public class CarRoads extends RollingStockAttribute implements InstanceManagerAutoDefault {

    private static final String ROADS = Bundle.getMessage("carRoadNames");
    public static final String CARROADS_CHANGED_PROPERTY = "CarRoads Length"; // NOI18N
    public static final String CARROADS_NAME_CHANGED_PROPERTY = "CarRoads Name"; // NOI18N

    public CarRoads() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized CarRoads instance() {
        return InstanceManager.getDefault(CarRoads.class);
    }

    @Override
    protected String getDefaultNames() {
        return ROADS;
    }

    @Override
    public void addName(String road) {
        super.addName(road);
        setDirtyAndFirePropertyChange(CARROADS_CHANGED_PROPERTY, null, road);
    }

    @Override
    public void deleteName(String road) {
        super.deleteName(road);
        setDirtyAndFirePropertyChange(CARROADS_CHANGED_PROPERTY, road, null);
    }

    public void replaceName(String oldName, String newName) {
        super.addName(newName);
        setDirtyAndFirePropertyChange(CARROADS_NAME_CHANGED_PROPERTY, oldName, newName);
        super.deleteName(oldName);
        if (newName == null) {
            setDirtyAndFirePropertyChange(CARROADS_CHANGED_PROPERTY, list.size() + 1, list.size());
        }
    }

    /**
     * Get the maximum character length of a road name when printing on a
     * manifest or switch list. Characters after the "-" are ignored.
     *
     * @return the maximum character length of a car road name
     */
    @Override
    public int getMaxNameLength() {
        if (maxNameLength == 0) {
            getMaxNameSubStringLength();
            log.info("Max road name ({}) length {}", maxName, maxNameLength);
        }
        return maxNameLength;
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     *
     * @param root The common Element for operations-cars.dtd.
     *
     */
    public void store(Element root) {
        store(root, Xml.ROADS, Xml.ROAD, Xml.ROAD_NAMES);
    }

    public void load(Element root) {
        load(root, Xml.ROADS, Xml.ROAD, Xml.ROAD_NAMES);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(CarManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(CarRoads.class);
}
