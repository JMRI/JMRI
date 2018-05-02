package jmri.jmrit.operations.rollingstock.engines;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import org.jdom2.Element;

/**
 * Represents the types of engines a railroad can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 */
public class EngineTypes extends RollingStockAttribute implements InstanceManagerAutoDefault {

    private static final String TYPES = Bundle.getMessage("engineDefaultTypes");

    // for property change
    public static final String ENGINETYPES_CHANGED_PROPERTY = "EngineTypesLength"; // NOI18N
    public static final String ENGINETYPES_NAME_CHANGED_PROPERTY = "EngineTypesName"; // NOI18N

    public EngineTypes() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized EngineTypes instance() {
        return InstanceManager.getDefault(EngineTypes.class);
    }

    @Override
    protected String getDefaultNames() {
        return TYPES;
    }

    @Override
    public void addName(String type) {
        super.addName(type);
        setDirtyAndFirePropertyChange(ENGINETYPES_CHANGED_PROPERTY, null, type);
    }

    @Override
    public void deleteName(String type) {
        super.deleteName(type);
        setDirtyAndFirePropertyChange(ENGINETYPES_CHANGED_PROPERTY, type, null);
    }

    public void replaceName(String oldName, String newName) {
        super.addName(newName);
        setDirtyAndFirePropertyChange(ENGINETYPES_NAME_CHANGED_PROPERTY, oldName, newName);
        // need to keep old name so location manager can replace properly
        super.deleteName(oldName);
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-engines.dtd.
     * @param root The common Element for operations-engines.dtd.
     *
     */
    public void store(Element root) {
        store(root, Xml.TYPES, Xml.TYPE, Xml.ENGINE_TYPES);
    }

    public void load(Element root) {
        load(root, Xml.TYPES, Xml.TYPE, Xml.ENGINE_TYPES);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(EngineManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

//    private final static Logger log = LoggerFactory.getLogger(EngineTypes.class);

}
