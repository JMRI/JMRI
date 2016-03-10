// CarOwners.java
package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import jmri.jmrit.operations.setup.Control;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the owner names that cars can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 * @version $Revision$
 */
public class CarOwners extends RollingStockAttribute {

    public static final String CAROWNERS_NAME_CHANGED_PROPERTY = "CarOwners Name"; // NOI18N
    public static final String CAROWNERS_CHANGED_PROPERTY = "CarOwners Length"; // NOI18N

    public CarOwners() {
    }

    /**
     * record the single instance *
     */
    private static CarOwners _instance = null;

    public static synchronized CarOwners instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("CarOwners creating instance");
            }
            // create and load
            _instance = new CarOwners();
        }
        if (Control.showInstance) {
            log.debug("CarOwners returns instance {}", _instance);
        }
        return _instance;
    }

    protected String getDefaultNames() {
        return ""; // there aren't any
    }

    public void addName(String owner) {
        super.addName(owner);
        setDirtyAndFirePropertyChange(CAROWNERS_CHANGED_PROPERTY, null, owner);
    }

    public void deleteName(String owner) {
        super.deleteName(owner);
        setDirtyAndFirePropertyChange(CAROWNERS_CHANGED_PROPERTY, owner, null);
    }

    public void replaceName(String oldName, String newName) {
        super.addName(newName);
        setDirtyAndFirePropertyChange(CAROWNERS_NAME_CHANGED_PROPERTY, oldName, newName);
        super.deleteName(newName);
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     *
     */
    public void store(Element root) {
        store(root, Xml.OWNERS, Xml.OWNER, Xml.CAR_OWNERS);
    }

    public void load(Element root) {
        load(root, Xml.OWNERS, Xml.OWNER, Xml.CAR_OWNERS);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        CarManagerXml.instance().setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(CarOwners.class.getName());

}
