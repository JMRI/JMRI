package jmri.jmrit.operations.rollingstock.cars;

import org.jdom2.Element;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;

/**
 * Represents the owner names that cars can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 */
public class CarOwners extends RollingStockAttribute implements InstanceManagerAutoDefault {

    public static final String CAROWNERS_NAME_CHANGED_PROPERTY = "CarOwners Name"; // NOI18N
    public static final String CAROWNERS_CHANGED_PROPERTY = "CarOwners Length"; // NOI18N

    public CarOwners() {
    }

    @Override
    protected String getDefaultNames() {
        return ""; // there aren't any
    }

    @Override
    public void addName(String owner) {
        super.addName(owner);
        setDirtyAndFirePropertyChange(CAROWNERS_CHANGED_PROPERTY, null, owner);
    }

    @Override
    public void deleteName(String owner) {
        super.deleteName(owner);
        setDirtyAndFirePropertyChange(CAROWNERS_CHANGED_PROPERTY, owner, null);
    }

    public void replaceName(String oldName, String newName) {
        super.addName(newName);
        setDirtyAndFirePropertyChange(CAROWNERS_NAME_CHANGED_PROPERTY, oldName, newName);
        super.deleteName(oldName);
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     *
     * @param root The common Element for operations-cars.dtd.
     *
     */
    public void store(Element root) {
        store(root, Xml.OWNERS, Xml.OWNER);
    }

    public void load(Element root) {
        load(root, Xml.OWNERS, Xml.OWNER, Xml.CAR_OWNERS);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(CarManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

//    private final static Logger log = LoggerFactory.getLogger(CarOwners.class);

}
