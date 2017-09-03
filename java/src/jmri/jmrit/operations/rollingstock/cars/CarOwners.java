package jmri.jmrit.operations.rollingstock.cars;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import org.jdom2.Element;

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

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized CarOwners instance() {
        return InstanceManager.getDefault(CarOwners.class);
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
        super.deleteName(newName);
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     *
     * @param root The common Element for operations-cars.dtd.
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
        InstanceManager.getDefault(CarManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

//    private final static Logger log = LoggerFactory.getLogger(CarOwners.class);

}
