// CarRoads.java
package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import jmri.jmrit.operations.setup.Control;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the road names that cars can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 * @version $Revision$
 */
public class CarRoads extends RollingStockAttribute {

    private static final String ROADS = Bundle.getMessage("carRoadNames");
    public static final String CARROADS_CHANGED_PROPERTY = "CarRoads Length"; // NOI18N
    public static final String CARROADS_NAME_CHANGED_PROPERTY = "CarRoads Name"; // NOI18N

    public CarRoads() {
    }

    /**
     * record the single instance *
     */
    private static CarRoads _instance = null;

    public static synchronized CarRoads instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("CarRoads creating instance");
            }
            // create and load
            _instance = new CarRoads();
        }
        if (Control.showInstance) {
            log.debug("CarRoads returns instance {}", _instance);
        }
        return _instance;
    }

    protected String getDefaultNames() {
        return ROADS;
    }

    public void addName(String road) {
        super.addName(road);
        setDirtyAndFirePropertyChange(CARROADS_CHANGED_PROPERTY, null, road);
    }

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
     * @return the maximum character length of a car type
     */
    public int getMaxNameLength() {
        if (maxNameLengthSubType == 0) {
            String maxName = "";
            maxNameLengthSubType = MIN_NAME_LENGTH;
            for (String name : getNames()) {
                String[] subString = name.split("-");
                if (subString[0].length() > maxNameLengthSubType) {
                    maxName = name;
                    maxNameLengthSubType = subString[0].length();
                }
            }
            log.info("Max road name ({}) length {}", maxName, maxNameLengthSubType);
        }
        return maxNameLengthSubType;
    }

    private int maxNameLengthSubType = 0;

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
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
        CarManagerXml.instance().setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(CarRoads.class.getName());
}
