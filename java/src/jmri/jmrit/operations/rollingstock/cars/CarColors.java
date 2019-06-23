package jmri.jmrit.operations.rollingstock.cars;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;

/**
 * Represents the colors that cars can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 */
public class CarColors extends RollingStockAttribute implements InstanceManagerAutoDefault {

    private static final String COLORS = Bundle.getMessage("carColors");
    public static final String CARCOLORS_CHANGED_PROPERTY = "CarColors"; // NOI18N
    public static final String CARCOLORS_NAME_CHANGED_PROPERTY = "CarColorsName"; // NOI18N

    public CarColors() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized CarColors instance() {
        return InstanceManager.getDefault(CarColors.class);
    }

    @Override
    protected String getDefaultNames() {
        return COLORS;
    }

    @Override
    public void addName(String color) {
        super.addName(color);
        setDirtyAndFirePropertyChange(CARCOLORS_CHANGED_PROPERTY, null, color);
    }

    @Override
    public void deleteName(String color) {
        super.deleteName(color);
        setDirtyAndFirePropertyChange(CARCOLORS_CHANGED_PROPERTY, color, null);
    }

    public void replaceName(String oldName, String newName) {
        super.addName(newName);
        setDirtyAndFirePropertyChange(CARCOLORS_NAME_CHANGED_PROPERTY, oldName, newName);
        // need to keep old name so location manager can replace properly
        super.deleteName(oldName);
    }
    
    @Override
    public int getMaxNameLength() {
        if (maxNameLength == 0) {
            super.getMaxNameLength();
            log.info("Max color name ({}) length {}", maxName, maxNameLength);
        }
        return maxNameLength;
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     *
     * @param root The common Element for operations-cars.dtd.
     */
    public void store(Element root) {
        store(root, Xml.COLORS, Xml.COLOR, Xml.CAR_COLORS);
    }

    public void load(Element root) {
        load(root, Xml.COLORS, Xml.COLOR, Xml.CAR_COLORS);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(CarManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }
        
    private final static Logger log = LoggerFactory.getLogger(CarColors.class);

}
