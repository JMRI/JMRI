package jmri.jmrix.oaktree;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement light manager for Oak Tree serial systems
 * <P>
 * System names are "TLnnn", where nnn is the bit number without padding.
 * <P>
 * Based in part on SerialTurnoutManager.java
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class SerialLightManager extends AbstractLightManager {

    OakTreeSystemConnectionMemo _memo = null;
    protected String prefix = "O";

    public SerialLightManager(OakTreeSystemConnectionMemo memo) {
        _memo = memo;
        prefix = getSystemPrefix();
    }

    /**
     * Return the Oak Tree system prefix
     */
    @Override
    public String getSystemPrefix() {
        return _memo.getSystemPrefix();

    }

    /**
     * Method to create a new Light based on the system name.
     * Assumes calling method has checked that a Light with this system name
     * does not already exist.
     *
     * @return null if the system name is not in a valid format or if the
     * system name does not correspond to a configured OakTree digital output bit
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
        // Validate the systemName
        if (SerialAddress.validSystemNameFormat(systemName, 'L', prefix) == NameValidity.VALID) {
            lgt = new SerialLight(systemName, userName, _memo);
            if (!SerialAddress.validSystemNameConfig(systemName, 'L', _memo)) {
                log.warn("Light system Name does not refer to configured hardware: {}", systemName);
            }
        } else {
            log.error("Invalid Light system Name format: {}", systemName);
        }
        return lgt;
    }

    /**
     * Public method to validate system name format.
     * @return 'true' if system name has a valid format, else return 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'L', prefix));
    }

    /**
     * Public method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current
     * configuration, else returns 'false'
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (SerialAddress.validSystemNameConfig(systemName, 'L', _memo));
    }

    /**
     * Public method to normalize a system name.
     *
     * @return a normalized system name if system name has a valid format,
     * else return ""
     */
    @Override
    public String normalizeSystemName(String systemName) {
        return (SerialAddress.normalizeSystemName(systemName, prefix));
    }

    /**
     * Public method to convert system name to its alternate format.
     *
     * @return a normalized system name if system name is valid and has a valid
     * alternate representation, else return ""
     */
    @Override
    public String convertSystemNameToAlternate(String systemName) {
        return (SerialAddress.convertSystemNameToAlternate(systemName, prefix));
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
