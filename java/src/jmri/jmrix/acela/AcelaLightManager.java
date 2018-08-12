package jmri.jmrix.acela;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement light manager for Acela systems.
 * <p>
 * System names are "ALnnn", where nnn is the bit number without padding.
 * <p>
 * Based in part on AcelaTurnoutManager.java
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Coleman Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaLightManager extends AbstractLightManager {

    private AcelaSystemConnectionMemo _memo = null;

    public AcelaLightManager(AcelaSystemConnectionMemo memo) {
        _memo = memo;
    }

    /**
     * Get the configured system prefix for this connection.
     */
    @Override
    public String getSystemPrefix() {
        return _memo.getSystemPrefix();
    }

    /**
     * Method to create a new Light based on the system name.
     * <p>
     * Assumes calling method has checked that a Light with this system
     * name does not already exist.
     * </p>
     *
     * @return null if the system name is not in a valid format
     */
    @Override
    protected Light createNewLight(String systemName, String userName) {
        Light lgt = null;
        // check if the output bit is available
        int nAddress = -1;
        nAddress = AcelaAddress.getNodeAddressFromSystemName(systemName, _memo);
        if (nAddress == -1) {
            return (null);
        }
        int bitNum = AcelaAddress.getBitFromSystemName(systemName, getSystemPrefix());
        if (bitNum == -1) {
            return (null);
        }

        // Validate the systemName
        if (AcelaAddress.validSystemNameFormat(systemName, 'L', getSystemPrefix()) == NameValidity.VALID) {
            lgt = new AcelaLight(systemName, userName, _memo);
            if (!AcelaAddress.validSystemNameConfig(systemName, 'L', _memo)) {
                log.warn("Light System Name does not refer to configured hardware: {}", systemName);
            }
        } else {
            log.error("Invalid Light System Name format: {}", systemName);
            throw new IllegalArgumentException("Invalid Light System Name format: " + systemName);
        }
        return lgt;
    }

    /**
     * Public method to validate system name format.
     *
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (AcelaAddress.validSystemNameFormat(systemName, 'L', getSystemPrefix()));
    }

    /**
     * Public method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current configuration,
     * else returns 'false'
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (AcelaAddress.validSystemNameConfig(systemName, 'L', _memo));
    }

    /**
     * Public method to normalize a system name.
     *
     * @return a normalized system name if system name has a valid format,
     * else return ""
     */
    @Override
    public String normalizeSystemName(String systemName) {
        return (AcelaAddress.normalizeSystemName(systemName, getSystemPrefix()));
    }

    /**
     * Public method to convert system name to its alternate format.
     *
     * @return a normalized system name if system name is valid and has a valid
     * alternate representation, else return ""
     */
    @Override
    public String convertSystemNameToAlternate(String systemName) {
        return (AcelaAddress.convertSystemNameToAlternate(systemName, getSystemPrefix()));
    }

    /**
     * Allow access to AcelaLightManager.
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public AcelaLightManager instance() {
        return null; 
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaLightManager.class);

}
