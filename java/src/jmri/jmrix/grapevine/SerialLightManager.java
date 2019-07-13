package jmri.jmrix.grapevine;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement LightManager for Grapevine serial systems.
 * <p>
 * System names are "GLnnn", where G is the (multichar) system connection prefix,
 * nnn is the bit number without padding.
 * <p>
 * Based in part on SerialTurnoutManager.java
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007
 */
public class SerialLightManager extends AbstractLightManager {

    GrapevineSystemConnectionMemo memo = null;

    public SerialLightManager(GrapevineSystemConnectionMemo _memo) {
        memo = _memo;
    }

    /**
     * Return the Grapevine system prefix.
     */
    @Override
    public String getSystemPrefix() {
        return memo.getSystemPrefix();
    }

    /**
     * Method to create a new Light based on the system name.
     * <p>
     * Assumes calling method has checked that a Light with this
     * system name does not already exist.
     *
     * @return null if the system name is not in a valid format or if
     * the system name does not correspond to a configured Grapevine
     * digital output bit
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        String prefix = memo.getSystemPrefix();
        Light lgt = null;
        // Validate the systemName
        if (SerialAddress.validSystemNameFormat(systemName, 'L', prefix) == NameValidity.VALID) {
            lgt = new SerialLight(systemName, userName, memo);
            if (!SerialAddress.validSystemNameConfig(systemName, 'L', memo.getTrafficController())) {
                log.warn("Light system Name does not refer to configured hardware: {}", systemName);
            }
        } else {
            log.warn("Invalid Light system Name format: {}", systemName);
        }
        log.debug("new light {} for prefix {}", systemName, prefix);
        return lgt;
    }

    /**
     * Public method to validate system name format.
     *
     * @return 'true' if system name has a valid format,
     * else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'L', getSystemPrefix()));
    }

    /**
     * Public method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current
     * configuration, else returns 'false'
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (SerialAddress.validSystemNameConfig(systemName, 'L', memo.getTrafficController()));
    }

    /**
     * Public method to convert system name to its alternate format.
     *
     * @return a normalized system name if system name is valid and has a valid
     * alternate representation, else return ""
     */
    @Override
    public String convertSystemNameToAlternate(String systemName) {
        return (SerialAddress.convertSystemNameToAlternate(systemName, getSystemPrefix()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    /**
     * Allow access to SerialLightManager.
     * @deprecated  Since JMRI 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SerialLightManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLightManager.class);

}
