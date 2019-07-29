package jmri.jmrix.dccpp;

import static jmri.jmrix.dccpp.DCCppConstants.MAX_TURNOUT_ADDRESS;

import java.util.Locale;
import jmri.Light;
import jmri.managers.AbstractLightManager;

/**
 * Implement LightManager for DCC++ systems.
 * <p>
 * System names are "DxppSnnn", where Dx is the system prefix and nnn is the sensor number without padding.
 * <p>
 * Based in part on SerialLightManager.java
 *
 * @author Paul Bender Copyright (C) 2008
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppLightManager extends AbstractLightManager {

    private DCCppTrafficController tc = null;

    /**
     * Create an new DCC++ LightManager.
     * Has to register for DCC++ events.
     *
     * @param memo the supporting system connection memo
     */
    public DCCppLightManager(DCCppSystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getDCCppTrafficController();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DCCppSystemConnectionMemo getMemo() {
        return (DCCppSystemConnectionMemo) memo;
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
        // check if the output bit is available
        int bitNum = getBitFromSystemName(systemName);
        if (bitNum == 0) {
            return null;
        }
        // Normalize the systemName
        String sName = getSystemNamePrefix() + bitNum;   // removes any leading zeros
        // make the new Light object
        Light lgt = new DCCppLight(tc, this, sName, userName);
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
        return validateIntegerSystemNameFormat(systemName, 1, MAX_TURNOUT_ADDRESS, locale);
    }

    /**
     * Get the bit address from the system name.
     * @param systemName a valid LocoNet-based Turnout System Name
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
     * {@inheritDoc}
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

    /**
     * {@inheritDoc}
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
