package jmri.jmrix.grapevine;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement light manager for Grapevine serial systems
 * <P>
 * System names are "TLnnn", where nnn is the bit number without padding.
 * <P>
 * Based in part on SerialTurnoutManager.java
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007
  */
public class SerialLightManager extends AbstractLightManager {

    public SerialLightManager() {

    }

    /**
     * Returns the system letter for grapevine
     */
    @Override
    public String getSystemPrefix() {
        return "G";
    }

    /**
     * Method to create a new Light based on the system name Returns null if the
     * system name is not in a valid format or if the system name does not
     * correspond to a configured C/MRI digital output bit Assumes calling
     * method has checked that a Light with this system name does not already
     * exist
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
        // Validate the systemName
        if (SerialAddress.validSystemNameFormat(systemName, 'L')) {
            lgt = new SerialLight(systemName, userName);
            if (!SerialAddress.validSystemNameConfig(systemName, 'L')) {
                log.warn("Light system Name does not refer to configured hardware: "
                        + systemName);
            }
        } else {
            log.error("Invalid Light system Name format: " + systemName);
        }
        return lgt;
    }

    /**
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'
     */
    @Override
    public boolean validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'L'));
    }

    /**
     * Public method to validate system name for configuration returns 'true' if
     * system name has a valid meaning in current configuration, else returns
     * 'false'
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (SerialAddress.validSystemNameConfig(systemName, 'L'));
    }

    /**
     * Public method to normalize a system name
     * <P>
     * Returns a normalized system name if system name has a valid format, else
     * returns "".
     */
    @Override
    public String normalizeSystemName(String systemName) {
        return (SerialAddress.normalizeSystemName(systemName));
    }

    /**
     * Public method to convert system name to its alternate format
     * <P>
     * Returns a normalized system name if system name is valid and has a valid
     * alternate representation, else return "".
     */
    @Override
    public String convertSystemNameToAlternate(String systemName) {
        return (SerialAddress.convertSystemNameToAlternate(systemName));
    }

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddOutputEntryToolTip");
        return entryToolTip;
    }

    /**
     * Provide a manager-specific regex for the Add new item beantable pane.
     */
    @Override
    public String getEntryRegex() {
        return "^[0-9]{2,6}[aAmMpPsS]{0,1}[0-9]{1,3}$"; // examples 4B3, 4:3, see tooltip
    }

    /**
     * Allow access to SerialLightManager
     */
    static public SerialLightManager instance() {
        if (_instance == null) {
            _instance = new SerialLightManager();
        }
        return _instance;
    }
    static SerialLightManager _instance = null;

    private final static Logger log = LoggerFactory.getLogger(SerialLightManager.class.getName());

}
