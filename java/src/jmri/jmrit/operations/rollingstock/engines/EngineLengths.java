package jmri.jmrit.operations.rollingstock.engines;

import java.util.Comparator;

import org.jdom2.Element;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;

/**
 * Represents the lengths that engines can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 */
public class EngineLengths extends RollingStockAttribute implements InstanceManagerAutoDefault {

    private static final String LENGTHS = Bundle.getMessage("engineDefaultLengths");
    public static final String ENGINELENGTHS_CHANGED_PROPERTY = "EngineLengths"; // NOI18N
    public static final String ENGINELENGTHS_NAME_CHANGED_PROPERTY = "EngineLengthsName"; // NOI18N

    public EngineLengths() {
    }

    @Override
    protected String getDefaultNames() {
        return LENGTHS;
    }

    // override, need to perform a number sort
    @Override
    public void setNames(String[] lengths) {
        setValues(lengths);
    }

    @Override
    public void addName(String length) {
        super.addName(length);
        setDirtyAndFirePropertyChange(ENGINELENGTHS_CHANGED_PROPERTY, null, length);
    }

    @Override
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
    
    @Override
    public void sort() {
        list.sort(Comparator.comparingInt(Integer::parseInt));
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-engines.dtd.
     * @param root The common Element for operations-engines.dtd.
     *
     */
    public void store(Element root) {
        store(root, Xml.LENGTHS, Xml.LENGTH);
    }

    public void load(Element root) {
        load(root, Xml.LENGTHS, Xml.LENGTH, Xml.ENGINE_LENGTHS);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(EngineManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

//    private final static Logger log = LoggerFactory.getLogger(EngineLengths.class);

}
