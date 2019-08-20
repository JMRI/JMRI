package jmri.jmrix.nce;

import java.util.Locale;
import jmri.Light;
import jmri.NmraPacket;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement LightManager for NCE systems
 * <p>
 * System names are "NLnnnnn", where N is the user configurable system prefix,
 * nnnnn is the stationary decoder address.
 * <p>
 * Based in part on SerialLightManager.java
 *
 * @author Dave Duchamp Copyright (C) 2010
 */
public class NceLightManager extends AbstractLightManager {

    public NceLightManager(NceSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NceSystemConnectionMemo getMemo() {
        return (NceSystemConnectionMemo) memo;
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
        lgt = new NceLight(sName, userName, getMemo().getNceTrafficController(), this);
        return lgt;
    }

    /**
     * Get the bit address from the system name.
     *
     * @param systemName system name for light
     * @return index value for light
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(getSystemPrefix())) || (!systemName.startsWith(getSystemPrefix() + "L"))) {
            // here if an illegal nce light system name 
            log.error("illegal character in header field of nce light system name: " + systemName);
            return (0);
        }
        // name must be in the NLnnnnn format (N is user configurable)
        int num = 0;
        try {
            num = Integer.parseInt(systemName.substring(
                    getSystemPrefix().length() + 1, systemName.length())
                  );
        } catch (NumberFormatException e) {
            log.debug("illegal character in number field of system name: " + systemName);
            return (0);
        }
        if (num < NmraPacket.accIdLowLimit) {
            log.error("invalid nce light system name: " + systemName);
            return (0);
        } else if (num > NmraPacket.accIdHighLimit) {
            log.warn("bit number out of range in nce light system name: " + systemName);
            return (0);
        }
        return (num);
    }

    /**
     * A method that determines if it is possible to add a range of lights in
     * numerical order eg 11 thru 18, primarily used to show/not show the add
     * range box in the add Light window
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        return super.validateNmraAccessorySystemNameFormat(name, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * Public method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current configuration,
     * else returns 'false'. For now, this method always returns 'true'; it is needed for the
     * Abstract Light class.
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(NceLightManager.class);

}
