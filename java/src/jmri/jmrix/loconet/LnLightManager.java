package jmri.jmrix.loconet;

import java.util.Locale;
import jmri.Light;
import jmri.managers.AbstractLightManager;

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

    public LnLightManager(LocoNetSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocoNetSystemConnectionMemo getMemo() {
        return (LocoNetSystemConnectionMemo) memo;
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
        lgt = new LnLight(sName, userName, getMemo().getLnTrafficController(), this);
        return lgt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String systemName, Locale locale) {
        return validateIntegerSystemNameFormat(systemName, 1, 4096, locale);
    }

    /**
     * Get the bit address from the system name.
     * @param systemName a valid LocoNet-based Light System Name
     * @return the turnout number extracted from the system name
     */
    public int getBitFromSystemName(String systemName) {
        try {
            validateSystemNameFormat(systemName, Locale.getDefault());
        } catch (IllegalArgumentException ex) {
            return 0;
        }
        return Integer.parseInt(systemName.substring(getSystemNamePrefix().length()));
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

}
