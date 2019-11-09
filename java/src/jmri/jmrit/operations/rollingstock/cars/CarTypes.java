package jmri.jmrit.operations.rollingstock.cars;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import jmri.jmrit.operations.setup.Setup;

/**
 * Represents the types of cars a railroad can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 */
public class CarTypes extends RollingStockAttribute implements InstanceManagerAutoDefault {

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
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized CarTypes instance() {
        return InstanceManager.getDefault(CarTypes.class);
    }

    @Override
    protected String getDefaultNames() {
        if (Setup.getCarTypes().equals(Setup.AAR)) {
            return ARR_TYPES;
        }
        return TYPES;
    }

    /**
     * Changes the car types from descriptive to AAR, or the other way. Only
     * removes the default car type names from the list
     * @param type Setup.DESCRIPTIVE or Setup.AAR
     */
    public void changeDefaultNames(String type) {
        String[] convert = CONVERT_TYPES.split(","); // NOI18N
        String[] types = TYPES.split(","); // NOI18N
        // this conversion has internationalization problems, so we can't call
        // this an error.
        if (convert.length != types.length) {
            log.warn(
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

    @Override
    public void addName(String type) {
        super.addName(type);
        setDirtyAndFirePropertyChange(CARTYPES_CHANGED_PROPERTY, null, type);
    }

    @Override
    public void deleteName(String type) {
        super.deleteName(type);
        setDirtyAndFirePropertyChange(CARTYPES_CHANGED_PROPERTY, type, null);
    }

    public void replaceName(String oldName, String newName) {
        super.addName(newName);
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
    @Override
    public int getMaxNameLength() {
        if (maxNameLength == 0) {
            getMaxNameSubStringLength();
            log.info("Max car type name ({}) length {}", maxName, maxNameLength);
        }
        return maxNameLength;
    }

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
     * @param root The common Element for operations-cars.dtd.
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
        InstanceManager.getDefault(CarManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(CarTypes.class);

}
