// EngineLengths.java
package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import jmri.jmrit.operations.setup.Control;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the lengths that engines can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 * @version $Revision$
 */
public class EngineLengths extends RollingStockAttribute {

    private static final String LENGTHS = Bundle.getMessage("engineDefaultLengths");
    public static final String ENGINELENGTHS_CHANGED_PROPERTY = "EngineLengths"; // NOI18N
    public static final String ENGINELENGTHS_NAME_CHANGED_PROPERTY = "EngineLengthsName"; // NOI18N

    public EngineLengths() {
    }

    /**
     * record the single instance *
     */
    private static EngineLengths _instance = null;

    public static synchronized EngineLengths instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("EngineLengths creating instance");
            }
            // create and load
            _instance = new EngineLengths();
        }
        if (Control.SHOW_INSTANCE) {
            log.debug("EngineLengths returns instance {}", _instance);
        }
        return _instance;
    }

    protected String getDefaultNames() {
        return LENGTHS;
    }

    // override, need to perform a number sort
    public void setNames(String[] lengths) {
        setValues(lengths);
    }

    public void addName(String length) {
        super.addName(length);
        setDirtyAndFirePropertyChange(ENGINELENGTHS_CHANGED_PROPERTY, null, length);
    }

    public void deleteName(String length) {
        super.deleteName(length);
        setDirtyAndFirePropertyChange(ENGINELENGTHS_CHANGED_PROPERTY, length, null);
    }

    public void replaceName(String oldName, String newName) {
        super.addName(newName);
        setDirtyAndFirePropertyChange(ENGINELENGTHS_NAME_CHANGED_PROPERTY, oldName, newName);
        // need to keep old name so location manager can replace properly
        super.deleteName(oldName);
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-engines.dtd.
     *
     */
    public void store(Element root) {
        store(root, Xml.LENGTHS, Xml.LENGTH, Xml.ENGINE_LENGTHS);
    }

    public void load(Element root) {
        load(root, Xml.LENGTHS, Xml.LENGTH, Xml.ENGINE_LENGTHS);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        EngineManagerXml.instance().setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(EngineLengths.class.getName());

}
