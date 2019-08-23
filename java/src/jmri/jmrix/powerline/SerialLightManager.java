package jmri.jmrix.powerline;

import java.util.Locale;
import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement LightManager for Powerline serial systems.
 * <p>
 * System names are "PLnnn", where P is the user configurable system prefix,
 * nnn is the bit number without padding.
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008 Converted to multiple
 * connection
 * @author Ken Cameron Copyright (C) 2011
 */
abstract public class SerialLightManager extends AbstractLightManager {

    SerialTrafficController tc = null;

    public SerialLightManager(SerialTrafficController tc) {
        super(tc.getAdapterMemo());
        this.tc = tc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SerialSystemConnectionMemo getMemo() {
        return (SerialSystemConnectionMemo) memo;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    /**
     * Method to create a new Light based on the system name Returns null if the
     * system name is not in a valid format Assumes calling method has checked
     * that a Light with this system name does not already exist
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
        // Validate the systemName
        if (tc.getAdapterMemo().getSerialAddress().validSystemNameFormat(systemName, 'L') == NameValidity.VALID) {
            lgt = createNewSpecificLight(systemName, userName);
            if (!tc.getAdapterMemo().getSerialAddress().validSystemNameConfig(systemName, 'L')) {
                log.warn("Light system Name does not refer to configured hardware: "
                        + systemName);
            }
        } else {
            log.error("Invalid Light system Name format: " + systemName);
        }
        return lgt;
    }

    /**
     * Create light of a specific type for the interface
     * @param systemName name encoding device
     * @param userName user name
     * @return light object
     */
    abstract protected Light createNewSpecificLight(String systemName, String userName);

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        return tc.getAdapterMemo().getSerialAddress().validateSystemNameFormat(name, typeLetter(), locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return tc.getAdapterMemo().getSerialAddress().validSystemNameFormat(systemName, typeLetter());
    }

    /**
     * Public method to validate system name for configuration
     *
     * @return 'true' if system name has a valid format, else return 'false'
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (tc.getAdapterMemo().getSerialAddress().validSystemNameConfig(systemName, 'L'));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    /**
     * @return 'true' to indicate this system can support variable lights
     */
    @Override
    public boolean supportsVariableLights(String systemName) {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLightManager.class);

}
