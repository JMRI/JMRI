package jmri.jmrix.secsi;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement light manager for SECSI serial systems
 * <P>
 * System names are "TLnnn", where nnn is the bit number without padding.
 * <P>
 * Based in part on SerialTurnoutManager.java
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Bob Jacobsen Copyright (C) 2006, 2007
  */
public class SerialLightManager extends AbstractLightManager {

    private SecsiSystemConnectionMemo memo = null;

    public SerialLightManager(SecsiSystemConnectionMemo _memo) {
        memo = _memo;
    }

    /**
     * Returns the system letter for SECSI
     */
    @Override
    public String getSystemPrefix() {
        return memo.getSystemPrefix();
    }

    /**
     * Method to create a new Light based on the system name.
     * Assumes calling method has checked that a Light with this system
     * name does not already exist.
     *
     * @return null if memo.getSystemPrefix() system name is not in a valid format or if the
     * system name does not correspond to a configured C/MRI digital output bit
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
        // Validate the systemName
        if (SerialAddress.validSystemNameFormat(systemName, 'L') == NameValidity.VALID) {
            lgt = new SerialLight(systemName, userName,memo);
            if (!SerialAddress.validSystemNameConfig(systemName, 'L', memo.getTrafficController())) {
                log.warn("Light system Name does not refer to configured hardware: "
                        + systemName);
            }
        } else {
            log.error("Invalid Light system Name format: " + systemName);
        }
        return lgt;
    }

    /**
     * Public method to validate system name format.
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'L'));
    }

    /**
     * Public method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current
     * configuration, else returns 'false'
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (SerialAddress.validSystemNameConfig(systemName, 'L',memo.getTrafficController()));
    }

    /**
     * Public method to normalize a system name.
     *
     * @return a normalized system name if system name has a valid format, else
     * returns ""
     */
    @Override
    public String normalizeSystemName(String systemName) {
        return (SerialAddress.normalizeSystemName(systemName));
    }

    /**
     * Public method to convert system name to its alternate format
     *
     * @return a normalized system name if system name is valid and has a valid
     * alternate representation, else returns ""
     */
    @Override
    public String convertSystemNameToAlternate(String systemName) {
        return (SerialAddress.convertSystemNameToAlternate(systemName));
    }

    /**
     * Allow access to SerialLightManager.
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SerialLightManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLightManager.class);

}
