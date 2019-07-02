package jmri.jmrix.anyma;

import jmri.Light;
import jmri.Manager;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement LightManager for Anyma dmx usb systems.
 * <p>
 * System names are "DLnnn", where D is the user configurable system prefix,
 * nnn is the channel number without padding.
 *
 * @author George Warner Copyright (c) 2017-2018
 * @since 4.9.6
 */
public class UsbLightManager extends AbstractLightManager {

    private AnymaDMX_SystemConnectionMemo _memo = null;

    /**
     * constructor
     *
     * @param memo the system connection memo
     */
    public UsbLightManager(AnymaDMX_SystemConnectionMemo memo) {
        log.debug("*    UsbLightManager constructor called");
        _memo = memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSystemPrefix() {
        log.debug("*    UsbLightManager.getSystemPrefix() called");
        return _memo.getSystemPrefix();
    }

    /**
     * Method to create a new Light based on the system name.
     * <p>
     * Assumes calling method has checked that a Light with this system name
     * does not already exist.
     *
     * @param systemName the system name to use for this light
     * @param userName   the user name to use for this light
     * @return null if the system name is not in a valid format or if the system
     *         name does not correspond to a valid channel
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        log.debug("*    UsbLightManager.createNewLight() called");
        Light result = null;    // assume failure (pessimist!)

        int nAddress = _memo.getNodeAddressFromSystemName(systemName);
        if (nAddress != -1) {
            int channelNum = _memo.getChannelFromSystemName(systemName);
            if (channelNum != 0) {
                // Validate the systemName
                if (_memo.validSystemNameFormat(systemName, 'L') == Manager.NameValidity.VALID) {
                    if (_memo.validSystemNameConfig(systemName, 'L')) {
                        result = new AnymaDMX_UsbLight(systemName, userName, _memo);
                    } else {
                        log.warn("Light System Name does not refer to configured hardware: " + systemName);
                    }
                } else {
                    log.error("Invalid Light System Name format: " + systemName);
                }
            } else {
                log.error("Invalid channel number from System Name: " + systemName);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Manager.NameValidity validSystemNameFormat(String systemName) {
        log.debug("*    UsbLightManager.validSystemNameFormat() called");
        return _memo.validSystemNameFormat(systemName, 'L');
    }

    /**
     * Public method to validate system name for configuration.
     *
     * @param systemName the system name to validate
     * @return 'true' if system name has a valid meaning in current
     *         configuration, else return 'false'
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        log.debug("*    UsbLightManager.validSystemNameConfig() called");
        return _memo.validSystemNameConfig(systemName, 'L');
    }

    /**
     * Public method to convert system name to its alternate format
     *
     * @param systemName the system name to convert
     * @return a normalized system name if system name is valid and has a valid
     *         alternate representation, else returns ""
     */
    @Override
    public String convertSystemNameToAlternate(String systemName) {
        log.debug("*    UsbLightManager.convertSystemNameToAlternate() called");
        return _memo.convertSystemNameToAlternate(systemName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsVariableLights(String systemName) {
        return true;
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
        log.debug("*    UsbLightManager.getEntryToolTip() called");
        //TODO: Why doesn't this work?!?
        return null; //BundleBundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log
            = LoggerFactory.getLogger(UsbLightManager.class);
}
