package jmri.jmrix.dccpp;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement light manager for DCC++ systems
 * <p>
 * System names are "DCCppLnnnnn", where nnnnn is the bit number without padding.
 * <p>
 * Based in part on SerialLightManager.java
 *
 * @author Paul Bender Copyright (C) 2008
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppLightManager extends AbstractLightManager {

    private DCCppTrafficController tc = null;
    private String prefix = null;

    public DCCppLightManager(DCCppTrafficController tc, String prefix) {
        this.prefix = prefix;
        this.tc = tc;
    }

    /**
     * Returns the system letter for DCC++.
     */
    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * Method to create a new Light based on the system name.
     * Assumes calling method has checked that a Light with this
     * system name does not already exist.
     *
     * @return null if the system name is not in a valid format
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
        // check if the output bit is available
        int bitNum = getBitFromSystemName(systemName);
        if (bitNum == 0) {
            return (null);
        }
        // Normalize the systemName
        String sName = prefix + typeLetter() + bitNum;   // removes any leading zeros
        // make the new Light object
        lgt = new DCCppLight(tc, this, sName, userName);
        return lgt;
    }

    /**
     * Get the bit address from the system name.
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(getSystemPrefix() + typeLetter()))) {
            // here if an illegal DCC++ light system name 
            log.error("illegal character in header field of DCC++ light system name: {} prefix {} type {}", 
        systemName, getSystemPrefix(), typeLetter());
            return (0);
        }
        // name must be in the DCCppLnnnnn format (DCCPP is user configurable)
        int num = 0;
        try {
            num = Integer.valueOf(systemName.substring(
                    getSystemPrefix().length() + 1, systemName.length())).intValue();
        } catch (Exception e) {
            log.debug("invalid character in number field of system name: {}", systemName);
            return (0);
        }
        if (num <= 0) {
            log.debug("invalid DCC++ light system name: " + systemName);
            return (0);
        } else if (num > DCCppConstants.MAX_ACC_DECODER_JMRI_ADDR) {
            log.debug("bit number out of range in DCC++ light system name: {}", systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Public method to validate system name format.
     *
     * @return VALID if system name has a valid format, else returns INVALID
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * Public method to validate system name for configuration.
     * Needed for the Abstract Light class.
     *
     * @return 'true' if system name has a valid meaning in current configuration,
     * else returns 'false' for now, this method always returns 'true'
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

    /**
     * A method that determines if it is possible to add a range of lights in
     * numerical order eg 11 thru 18, primarily used to enable/disable the add
     * range box in the add Light window
     *
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
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
     * Allow access to DCCppLightManager.
     */
    @Deprecated
    static public DCCppLightManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppLightManager.class);

}
