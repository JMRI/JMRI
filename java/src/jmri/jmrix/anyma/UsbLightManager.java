package jmri.jmrix.anyma;

import java.util.Locale;
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

    /**
     * constructor
     *
     * @param memo the system connection memo
     */
    public UsbLightManager(AnymaDMX_SystemConnectionMemo memo) {
        super(memo);
        log.debug("*    UsbLightManager constructor called");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnymaDMX_SystemConnectionMemo getMemo() {
        return (AnymaDMX_SystemConnectionMemo) memo;
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

        int nAddress = getMemo().getNodeAddressFromSystemName(systemName);
        if (nAddress != -1) {
            int channelNum = getMemo().getChannelFromSystemName(systemName);
            if (channelNum != 0) {
                // Validate the systemName
                if (getMemo().validSystemNameFormat(systemName, 'L') == Manager.NameValidity.VALID) {
                    if (getMemo().validSystemNameConfig(systemName, 'L')) {
                        result = new AnymaDMX_UsbLight(systemName, userName, getMemo());
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
        return getMemo().validSystemNameFormat(systemName, 'L');
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String systemName, Locale locale) {
        return validateIntegerSystemNameFormat(systemName, 1, 512, locale);
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
        return getMemo().validSystemNameConfig(systemName, 'L');
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
        return getMemo().convertSystemNameToAlternate(systemName);
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
