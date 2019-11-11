package jmri.jmrit.operations.rollingstock.cars;

import org.jdom2.Element;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import jmri.jmrit.operations.setup.Control;

/**
 * Represents the lengths that cars can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 */
public class CarLengths extends RollingStockAttribute implements InstanceManagerAutoDefault {

    private static final String LENGTHS = Bundle.getMessage("carLengths");
    public static final String CARLENGTHS_CHANGED_PROPERTY = "CarLengths"; // NOI18N
    public static final String CARLENGTHS_NAME_CHANGED_PROPERTY = "CarLengthsName"; // NOI18N
    
    protected static final int MIN_NAME_LENGTH = Control.max_len_string_length_name;

    public CarLengths() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized CarLengths instance() {
        return InstanceManager.getDefault(CarLengths.class);
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
        setDirtyAndFirePropertyChange(CARLENGTHS_CHANGED_PROPERTY, null, length);
    }

    @Override
    public void deleteName(String length) {
        super.deleteName(length);
        setDirtyAndFirePropertyChange(CARLENGTHS_CHANGED_PROPERTY, length, null);
    }

    public void replaceName(String oldName, String newName) {
        super.addName(newName);
        setDirtyAndFirePropertyChange(CARLENGTHS_NAME_CHANGED_PROPERTY, oldName, newName);
        // need to keep old name so location manager can replace properly
        super.deleteName(oldName);
    }
    
    @Override
    protected int getMinNameLength() {
        return MIN_NAME_LENGTH;
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     * @param root The common Element for operations-cars.dtd.
     *
     */
    public void store(Element root) {
        store(root, Xml.LENGTHS, Xml.LENGTH, Xml.CAR_LENGTHS);
    }

    public void load(Element root) {
        load(root, Xml.LENGTHS, Xml.LENGTH, Xml.CAR_LENGTHS);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(CarManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

//    private final static Logger log = LoggerFactory.getLogger(CarLengths.class);

}
