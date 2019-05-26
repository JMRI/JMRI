package jmri.jmrix.loconet;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement LightManager for LocoNet systems.
 * <p>
 * System names are "LLnnnnn", where the first L is the user configurable
 * system prefix, nnnnn is the bit number without padding.
 * <p>
 * Based in part on SerialLightManager.java
 *
 * @author Dave Duchamp Copyright (C) 2006
 */
public class LnLightManager extends AbstractLightManager {

    public LnLightManager(LnTrafficController tc, String prefix) {
        _trafficController = tc;
        this.prefix = prefix;
    }

    LnTrafficController _trafficController;
    String prefix;

    /**
     * Get the system letter for LocoNet.
     */
    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * Create a new Light based on the system name.
     * <p>
     * Assumes calling method has checked that a Light with
     * this system name does not already exist.
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
        String sName = getSystemPrefix() + "L" + bitNum;   // removes any leading zeros
        // make the new Light object
        lgt = new LnLight(sName, userName, _trafficController, this);
        return lgt;
    }

    /**
     * Get the bit address from the system name.
     * @param systemName the systemName to be checked for validity
     * @return the bit address number from the system name
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(getSystemPrefix())) || (!systemName.startsWith(getSystemPrefix() + "L"))) {
            // here if an illegal LocoNet Light system name
            log.error("invalid character in header field of loconet light system name: " + systemName);
            return (0);
        }
        // name must be in the LLnnnnn format (first L (system prefix) is user configurable)
        int num = 0;
        try {
            num = Integer.parseInt(systemName.substring(
                    getSystemPrefix().length() + 1, systemName.length())
                  );
        } catch (Exception e) {
            log.warn("invalid character in number field of system name: " + systemName);
            return (0);
        }
        if (num <= 0) {
            log.warn("invalid loconet light system name: " + systemName);
            return (0);
        } else if (num > 4096) {
            log.warn("bit number out of range in loconet light system name: " + systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Validate system name format.
     *
     * @param systemName the systemName to be validated
     * @return NameValidity.VALID if system name has a valid format,
     * else returns NameValidity.INVALID
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * Validate system name for configuration.
     * Needed for the Abstract Light class.
     *
     * @param systemName the systemName to be validated
     * @return 'true' if system name has a valid meaning in current configuration,
     * else returns 'false'. For now this method always returns 'true';
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

    /**
     * Determine if it is possible to add a range of Lights in
     * numerical order eg. 11 thru 18, primarily used to show/not show the add
     * range box in the Add Light pane.
     * @param systemName  an ignored parameter
     * @return true, always
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

    private final static Logger log = LoggerFactory.getLogger(LnLightManager.class);

}
