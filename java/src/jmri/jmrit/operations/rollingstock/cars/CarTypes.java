// CarTypes.java
package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the types of cars a railroad can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 * @version $Revision$
 */
public class CarTypes extends RollingStockAttribute {

    private static final String TYPES = Bundle.getMessage("carTypeNames");
    private static final String CONVERT_TYPES = Bundle.getMessage("carTypeConvert"); // Used to convert from ARR to
    // Descriptive
    private static final String ARR_TYPES = Bundle.getMessage("carTypeARR");
    // for property change
    public static final String CARTYPES_CHANGED_PROPERTY = "CarTypes Length"; // NOI18N
    public static final String CARTYPES_NAME_CHANGED_PROPERTY = "CarTypes Name"; // NOI18N

    public CarTypes() {
    }

    /**
     * record the single instance *
     */
    private static CarTypes _instance = null;

    public static synchronized CarTypes instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("CarTypes creating instance");
            }
            // create and load
            _instance = new CarTypes();
        }
        if (Control.showInstance) {
            log.debug("CarTypes returns instance {}", _instance);
        }
        return _instance;
    }

    protected String getDefaultNames() {
        if (Setup.getCarTypes().equals(Setup.AAR)) {
            return ARR_TYPES;
        }
        return TYPES;
    }

    /**
     * Changes the car types from descriptive to AAR, or the other way. Only
     * removes the default car type names from the list
     */
    public void changeDefaultNames(String type) {
        String[] convert = CONVERT_TYPES.split(","); // NOI18N
        String[] types = TYPES.split(","); // NOI18N
        if (convert.length != types.length) {
            log.error(
                    "Properties file doesn't have equal length conversion strings, carTypeNames {}, carTypeConvert {}", // NOI18N
                    types.length, convert.length);
            return;
        }
        if (type.equals(Setup.DESCRIPTIVE)) {
            // first replace the types
            for (int i = 0; i < convert.length; i++) {
                replaceName(convert[i], types[i]);
            }
            // remove AAR types
            String[] aarTypes = ARR_TYPES.split(","); // NOI18N
            for (int i = 0; i < aarTypes.length; i++) {
                list.remove(aarTypes[i]);
            }
            // add descriptive types
            for (int i = 0; i < types.length; i++) {
                if (!list.contains(types[i])) {
                    list.add(types[i]);
                }
            }
        } else {
            // first replace the types
            for (int i = 0; i < convert.length; i++) {
                replaceName(types[i], convert[i]);
            }
            // remove descriptive types
            for (int i = 0; i < types.length; i++) {
                list.remove(types[i]);
            }
            // add AAR types
            types = ARR_TYPES.split(","); // NOI18N
            for (int i = 0; i < types.length; i++) {
                if (!list.contains(types[i])) {
                    list.add(types[i]);
                }
            }
        }
    }

    public void addName(String type) {
        super.addName(type);
        maxNameLengthSubType = 0; // reset
        setDirtyAndFirePropertyChange(CARTYPES_CHANGED_PROPERTY, null, type);
    }

    public void deleteName(String type) {
        super.deleteName(type);
        maxNameLengthSubType = 0; // reset
        setDirtyAndFirePropertyChange(CARTYPES_CHANGED_PROPERTY, type, null);
    }

    public void replaceName(String oldName, String newName) {
        super.addName(newName);
        maxNameLengthSubType = 0; // reset
        setDirtyAndFirePropertyChange(CARTYPES_NAME_CHANGED_PROPERTY, oldName, newName);
        // need to keep old name so location manager can replace properly
        super.deleteName(oldName);
    }

    /**
     * Get the maximum character length of a car type when printing on a
     * manifest or switch list. Car subtypes or characters after the "-" are
     * ignored.
     *
     * @return the maximum character length of a car type
     */
    public int getMaxNameLength() {
        if (maxNameLengthSubType == 0) {
            String maxName = "";
            maxNameLengthSubType = MIN_NAME_LENGTH;
            for (String name : getNames()) {
                String[] subString = name.split("-");
                if (subString[0].length() > maxNameLengthSubType) {
                    maxName = name;
                    maxNameLengthSubType = subString[0].length();
                }
            }
            log.info("Max car type name ({}) length {}", maxName, maxNameLengthSubType);
        }
        return maxNameLengthSubType;
    }

    private int maxNameLengthSubType = 0;

    /**
     * Get the maximum character length of a car type including the sub type
     * characters.
     *
     * @return the maximum character length of a car type
     */
    public int getMaxFullNameLength() {
        return super.getMaxNameLength();
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     *
     */
    public void store(Element root) {
        store(root, Xml.TYPES, Xml.TYPE, Xml.CAR_TYPES);
    }

    public void load(Element root) {
        load(root, Xml.TYPES, Xml.TYPE, Xml.CAR_TYPES);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        CarManagerXml.instance().setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(CarTypes.class.getName());

}
