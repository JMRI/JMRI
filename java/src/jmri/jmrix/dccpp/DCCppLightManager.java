// DCCppLightManager.java
package jmri.jmrix.dccpp;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement light manager for DCC++ systems
 * <P>
 * System names are "DCCppLnnnnn", where nnnnn is the bit number without padding.
 * <P>
 * Based in part on SerialLightManager.java
 *
 * @author	Paul Bender Copyright (C) 2008
 * @author	Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 */
public class DCCppLightManager extends AbstractLightManager {

    private DCCppTrafficController tc = null;
    private String prefix = null;

    public DCCppLightManager(DCCppTrafficController tc, String prefix) {
        this.prefix = prefix;
        this.tc = tc;
    }

    /**
     * Returns the system letter for XPressNet
     */
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * Method to create a new Light based on the system name Returns null if the
     * system name is not in a valid format Assumes calling method has checked
     * that a Light with this system name does not already exist
     */
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
     * Get the bit address from the system name
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(getSystemPrefix() + typeLetter()))) {
            // here if an illegal DCC++ light system name 
            log.error("illegal character in header field of DCC++ light system name: {} prefix {} type {}", 
		      systemName, getSystemPrefix(), typeLetter());
            return (0);
        }
        // name must be in the DCCppLnnnnn format
        int num = 0;
        try {
            num = Integer.valueOf(systemName.substring(
                    getSystemPrefix().length() + 1, systemName.length())).intValue();
        } catch (Exception e) {
            log.error("illegal character in number field of system name: " + systemName);
            return (0);
        }
        if (num <= 0) {
            log.error("invalid DCC++ light system name: " + systemName);
            return (0);
        } else if (num > DCCppConstants.MAX_ACC_DECODER_JMRI_ADDR) {
            log.error("bit number out of range in DCC++ light system name: " + systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'
     */
    public boolean validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0);
    }

    /**
     * Public method to validate system name for configuration returns 'true' if
     * system name has a valid meaning in current configuration, else returns
     * 'false' for now, this method always returns 'true'; it is needed for the
     * Abstract Light class
     */
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

    /**
     * A method that determines if it is possible to add a range of lights in
     * numerical order eg 11 thru 18, primarily used to enable/disable the add
     * range box in the add Light window
     *
     */
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * Allow access to DCCppLightManager
     */
    @Deprecated
    static public DCCppLightManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppLightManager.class.getName());

}

/* @(#)DCCppLightManager.java */
