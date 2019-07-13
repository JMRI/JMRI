package jmri.jmrix.cmri.serial;

import jmri.Light;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement LightManager for CMRI serial systems.
 * <p>
 * System names are "CLnnn", where C is the user-configurable system prefix,
 * nnn is the bit number without padding.
 * <p>
 * Based in part on SerialTurnoutManager.java
 *
 * @author Dave Duchamp Copyright (C) 2004
 */
public class SerialLightManager extends AbstractLightManager {

    private CMRISystemConnectionMemo _memo = null;

    public SerialLightManager(CMRISystemConnectionMemo memo) {
        _memo = memo;
    }

    /**
     * {@inheritDoc}
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
     *
     * @return null if the
     * system name is not in a valid format or if the system name does not
     * correspond to a configured C/MRI digital output bit
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
        // check if the output bit is available
        int nAddress = -1;
        nAddress = _memo.getNodeAddressFromSystemName(systemName);
        if (nAddress == -1) {
            return (null);
        }
        int bitNum = _memo.getBitFromSystemName(systemName);
        if (bitNum == 0) {
            return (null);
        }
        String conflict = "";
        conflict = _memo.isOutputBitFree(nAddress, bitNum);
        if (!conflict.equals("")) {
            log.error("Assignment conflict with " + conflict + ".  Light not created.");
            notifyLightCreationError(conflict, bitNum);
            return (null);
        }
        // Validate the systemName
        if (_memo.validSystemNameFormat(systemName, 'L') == NameValidity.VALID) {
            lgt = new SerialLight(systemName, userName,_memo);
            if (!_memo.validSystemNameConfig(systemName, 'L',_memo.getTrafficController())) {
                log.warn("Light system Name does not refer to configured hardware: "
                        + systemName);
            }
        } else {
            log.error("Invalid Light system Name format: " + systemName);
        }
        return lgt;
    }

    /**
     * Public method to notify user of Light creation error.
     */
    public void notifyLightCreationError(String conflict, int bitNum) {
        javax.swing.JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorAssignDialog", bitNum, conflict) + "\n" +
                Bundle.getMessage("ErrorAssignLine2L"), Bundle.getMessage("ErrorAssignTitle"),
                javax.swing.JOptionPane.INFORMATION_MESSAGE, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
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
        return _memo.validSystemNameConfig(systemName, 'L',_memo.getTrafficController());
    }

    /**
     * Public method to convert system name to its alternate format
     *
     * @return a normalized system name if system name is valid and has a valid
     * alternate representation, else returns ""
     */
    @Override
    public String convertSystemNameToAlternate(String systemName) {
        return _memo.convertSystemNameToAlternate(systemName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLightManager.class);

}
