// EngineTypes.java
package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import jmri.jmrit.operations.setup.Control;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the types of engines a railroad can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 * @version $Revision$
 */
public class EngineTypes extends RollingStockAttribute {

    private static final String TYPES = Bundle.getMessage("engineDefaultTypes");

    // for property change
    public static final String ENGINETYPES_CHANGED_PROPERTY = "EngineTypesLength"; // NOI18N
    public static final String ENGINETYPES_NAME_CHANGED_PROPERTY = "EngineTypesName"; // NOI18N

    public EngineTypes() {
    }

    /**
     * record the single instance *
     */
    private static EngineTypes _instance = null;

    public static synchronized EngineTypes instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("EngineTypes creating instance");
            }
            // create and load
            _instance = new EngineTypes();
        }
        if (Control.showInstance) {
            log.debug("EngineTypes returns instance {}", _instance);
        }
        return _instance;
    }

    protected String getDefaultNames() {
        return TYPES;
    }

    public void addName(String type) {
        super.addName(type);
        setDirtyAndFirePropertyChange(ENGINETYPES_CHANGED_PROPERTY, null, type);
    }

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
        EngineManagerXml.instance().setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(EngineTypes.class.getName());

}
