package jmri.jmrix.anyma_dmx.usb;

import jmri.Light;
import jmri.Manager;
import jmri.jmrix.anyma_dmx.AnymaDMX_SystemConnectionMemo;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement light manager for anyma dmx usb systems
 * <P>
 * System names are "DXLnnn", where nnn is the channel number without padding.
 * <P>
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class UsbLightManager extends AbstractLightManager {

    private AnymaDMX_SystemConnectionMemo _memo = null;

    public UsbLightManager(AnymaDMX_SystemConnectionMemo memo) {
        log.info("*	UsbLightManager constructor called");
        _memo = memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSystemPrefix() {
        log.info("*	UsbLightManager.getSystemPrefix() called");
        return _memo.getSystemPrefix();
    }

    /**
     * Method to create a new Light based on the system name.
     * <p>
     * Assumes calling method has checked that a Light with this system
     * name does not already exist.
     *
     * @return null if the
     * system name is not in a valid format or if the system name does not
     * correspond to a configured anyma dmx digital output channel
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        log.info("*	UsbLightManager.createNewLight() called");
        Light lgt = null;

        int nAddress = -1;
        nAddress = _memo.getNodeAddressFromSystemName(systemName);
        if (nAddress == -1) {
            return (null);
        }

        int channelNum = _memo.getChannelFromSystemName(systemName);
        if (channelNum == 0) {
            return (null);
        }

        // Validate the systemName
        if (_memo.validSystemNameFormat(systemName, 'L') == Manager.NameValidity.VALID) {
            lgt = new UsbLight(systemName, userName,_memo);
//            if (!_memo.validSystemNameConfig(systemName, 'DX',_memo.getTrafficController())) {
//                log.warn("Light system Name does not refer to configured hardware: "
//                        + systemName);
//            }
        } else {
            log.error("Invalid Light system Name format: " + systemName);
        }
        return lgt;
    }

    /**
     * Public method to notify user of Light creation error.
     */
    public void notifyLightCreationError(String conflict, int channelNum) {
        log.info("*	UsbLightManager.notifyLightCreationError() called");
//        javax.swing.JOptionPane.showMessageDialog(null,
//                Bundle.getMessage("ErrorAssignDialog", "" + channelNum, conflict) + "\n" +
//                Bundle.getMessage("ErrorAssignLine2L"),
//                Bundle.getMessage("ErrorAssignTitle"),
//                javax.swing.JOptionPane.INFORMATION_MESSAGE, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Manager.NameValidity validSystemNameFormat(String systemName) {
        log.info("*	UsbLightManager.validSystemNameFormat() called");
        return _memo.validSystemNameFormat(systemName, 'L');
    }

    /**
     * Public method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current configuration,
     * else return 'false'
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        log.info("*	UsbLightManager.validSystemNameConfig() called");
        return _memo.validSystemNameConfig(systemName, 'L');
    }

    /**
     * Public method to normalize a system name.
     *
     * @return a normalized system name if system name has a valid format, else returns ""
     */
    @Override
    public String normalizeSystemName(String systemName) {
        log.info("*	UsbLightManager.normalizeSystemName() called");
        return _memo.normalizeSystemName(systemName);
    }

    /**
     * Public method to convert system name to its alternate format
     *
     * @return a normalized system name if system name is valid and has a valid
     * alternate representation, else returns ""
     */
    @Override
    public String convertSystemNameToAlternate(String systemName) {
        log.info("*	UsbLightManager.convertSystemNameToAlternate() called");
        return _memo.convertSystemNameToAlternate(systemName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        log.info("*	UsbLightManager.getEntryToolTip() called");
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(UsbLightManager.class);
}
