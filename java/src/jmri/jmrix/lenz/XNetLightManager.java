package jmri.jmrix.lenz;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement LightManager for XpressNet systems.
 * <p>
 * System names are "XLnnn", where X is the user configurable system prefix,
 * nnn is the bit number without padding.
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
     * Return the system letter for XpressNet.
     */
    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    // XNet-specific methods

    /**
     * Create a new Light based on the system name.
     * Assumes calling method has checked that a Light with this
     * system name does not already exist.
     *
     * @return null if the system name is not in a valid format
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        // check if the output bit is available
        int bitNum = XNetAddress.getBitFromSystemName(systemName, prefix);
        if (bitNum == -1) {
            return (null);
        }
        Light lgt = null;
        // Normalize the System Name
        String sName = prefix + typeLetter() + bitNum; // removes any leading zeros
        // create the new Light object
        lgt = new XNetLight(tc, this, sName, userName);
        return lgt;
    }

    /**
     * Get the bit address from the system name.
     *
     * @param systemName system name for turnout
     * @return index value for light, -1 if an error occurred
     */
    public int getBitFromSystemName(String systemName) {
        return XNetAddress.getBitFromSystemName(systemName, prefix);
    }

    /**
     * Validate Light system name format.
     * Logging of handled cases no higher than WARN.
     *
     * @return 'true' if system name has a valid format, else return 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
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
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    /**
     * Allow access to XNetLightManager.
     */
    @Deprecated
    static public XNetLightManager instance() {
        return null;
    }

    // private final static Logger log = LoggerFactory.getLogger(XNetLightManager.class);

}
