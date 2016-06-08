// CarColors.java
package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import jmri.jmrit.operations.setup.Control;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the colors that cars can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 * @version $Revision$
 */
public class CarColors extends RollingStockAttribute {

    private static final String COLORS = Bundle.getMessage("carColors");
    public static final String CARCOLORS_CHANGED_PROPERTY = "CarColors"; // NOI18N
    public static final String CARCOLORS_NAME_CHANGED_PROPERTY = "CarColorsName"; // NOI18N

    public CarColors() {
    }

    /**
     * record the single instance *
     */
    private static CarColors _instance = null;

    public static synchronized CarColors instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("CarColors creating instance");
            }
            // create and load
            _instance = new CarColors();
        }
        if (Control.SHOW_INSTANCE) {
            log.debug("CarColors returns instance {}", _instance);
        }
        return _instance;
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

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     *
     */
    public void store(Element root) {
        store(root, Xml.COLORS, Xml.COLOR, Xml.CAR_COLORS);
    }

    public void load(Element root) {
        load(root, Xml.COLORS, Xml.COLOR, Xml.CAR_COLORS);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        CarManagerXml.instance().setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(CarColors.class.getName());

}
