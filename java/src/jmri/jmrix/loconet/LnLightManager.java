package jmri.jmrix.loconet;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement light manager for LocoNet systems
 * <P>
 * System names are "LLnnnnn", where nnnnn is the bit number without padding.
 * <P>
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
     * Returns the system letter for Loconet
     */
    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * Method to create a new Light based on the system name Returns null if the
     * system name is not in a valid format Assumes calling method has checked
     * that a Light with this system name does not already exist
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
     * Get the bit address from the system name
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(getSystemPrefix())) || (!systemName.startsWith(getSystemPrefix() + "L"))) {
            // here if an illegal loconet light system name 
            log.error("invalid character in header field of loconet light system name: " + systemName);
            return (0);
        }
        // name must be in the LLnnnnn format (first L (system prefix) is user configurable)
        int num = 0;
        try {
            num = Integer.valueOf(systemName.substring(
                    getSystemPrefix().length() + 1, systemName.length())
            ).intValue();
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
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * Public method to validate system name for configuration returns 'true' if
     * system name has a valid meaning in current configuration, else returns
     * 'false' for now, this method always returns 'true'; it is needed for the
     * Abstract Light class
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

    /**
     * A method that determines if it is possible to add a range of lights in
     * numerical order eg 11 thru 18, primarily used to show/not show the add
     * range box in the add Light window.
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

    private final static Logger log = LoggerFactory.getLogger(LnLightManager.class);

}
