package jmri.jmrix.lenz;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement light manager for XPressNet systems.
 * <p>
 * System names are "XLnnnnn", where nnnnn is the bit number without padding.
 * <p>
 * Based in part on SerialLightManager.java
 *
 * @author Paul Bender Copyright (C) 2008
 * @navassoc 1 - * jmri.jmrix.lenz.XNetLight
 */
public class XNetLightManager extends AbstractLightManager {

    private XNetTrafficController tc = null;
    private String prefix = null;

    public XNetLightManager(XNetTrafficController tc, String prefix) {
        this.prefix = prefix;
        this.tc = tc;
    }

    /**
     * Return the system letter for XPressNet
     */
    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * Create a new Light based on the system name.
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
        lgt = new XNetLight(tc, this, sName, userName);
        return lgt;
    }

    /**
     * Get the bit address from the system name.
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(getSystemPrefix() + typeLetter()))) {
            // here if an illegal XPressNet light system name 
            log.error("illegal character in header field of XPressNet light system name: " + systemName);
            return (0);
        }
        // name must be in the XLnnnnn format (X is user configurable)
        int num = 0;
        try {
            num = Integer.valueOf(systemName.substring(
                    getSystemPrefix().length() + 1, systemName.length())).intValue();
        } catch (Exception e) {
            log.error("illegal character in number field of system name: " + systemName);
            return (0);
        }
        if (num <= 0) {
            log.error("invalid XPressNet light system name: " + systemName);
            return (0);
        } else if (num > 1024) {
            log.error("bit number out of range in XPressNet light system name: " + systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Validate system name format.
     *
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public boolean validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0);
    }

    /**
     * Validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current configuration, else returns
     * 'false'. For now, this method always returns 'true'; it is needed for the
     * Abstract Light class.
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

    /**
     * Determine if it is possible to add a range of lights in
     * numerical order eg 11 thru 18, primarily used to enable/disable the Add
     * range checkbox in the Add Light pane.
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
     * Allow access to XNetLightManager.
     */
    @Deprecated
    static public XNetLightManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(XNetLightManager.class);

}
