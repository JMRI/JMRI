package jmri.jmrix.cmri.serial;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Implement light manager for CMRI serial systems
 * <P>
 * System names are "CLnnn", where nnn is the bit number without padding.
 * <P>
 * Based in part on SerialTurnoutManager.java
 *
 * @author	Dave Duchamp Copyright (C) 2004
 */
public class SerialLightManager extends AbstractLightManager {

    private CMRISystemConnectionMemo _memo = null;

    public SerialLightManager(CMRISystemConnectionMemo memo) {
        _memo = memo;
    }

    /**
     * Returns the system letter for CMRI
     */
    @Override
    public String getSystemPrefix() {
        return _memo.getSystemPrefix();
    }

    /**
     * Method to create a new Light based on the system name Returns null if the
     * system name is not in a valid format or if the system name does not
     * correspond to a configured C/MRI digital output bit Assumes calling
     * method has checked that a Light with this system name does not already
     * exist
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
        // check if the output bit is available
        int nAddress = -1;
        nAddress = SerialAddress.getNodeAddressFromSystemName(systemName);
        if (nAddress == -1) {
            return (null);
        }
        int bitNum = SerialAddress.getBitFromSystemName(systemName);
        if (bitNum == 0) {
            return (null);
        }
        String conflict = "";
        conflict = SerialAddress.isOutputBitFree(nAddress, bitNum);
        if (!conflict.equals("")) {
            log.error("Assignment conflict with " + conflict + ".  Light not created.");
            notifyLightCreationError(conflict, bitNum);
            return (null);
        }
        // Validate the systemName
        if (SerialAddress.validSystemNameFormat(systemName, 'L')) {
            lgt = new SerialLight(systemName, userName,_memo);
            if (!SerialAddress.validSystemNameConfig(systemName, 'L',_memo.getTrafficController())) {
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
        javax.swing.JOptionPane.showMessageDialog(null, "The output bit, " + bitNum
                + ", is currently assigned to " + conflict + ". Light cannot be created as "
                + "you specified.", "C/MRI Assignment Conflict",
                javax.swing.JOptionPane.INFORMATION_MESSAGE, null);
    }

    /**
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'
     */
    @Override
    public boolean validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'L'));
    }

    /**
     * Public method to validate system name for configuration returns 'true' if
     * system name has a valid meaning in current configuration, else returns
     * 'false'
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (SerialAddress.validSystemNameConfig(systemName, 'L',_memo.getTrafficController()));
    }

    /**
     * Public method to normalize a system name
     * <P>
     * Returns a normalized system name if system name has a valid format, else
     * returns "".
     */
    @Override
    public String normalizeSystemName(String systemName) {
        return (SerialAddress.normalizeSystemName(systemName));
    }

    /**
     * Public method to convert system name to its alternate format
     * <P>
     * Returns a normalized system name if system name is valid and has a valid
     * alternate representation, else return "".
     */
    @Override
    public String convertSystemNameToAlternate(String systemName) {
        return (SerialAddress.convertSystemNameToAlternate(systemName));
    }

    /**
     * Allow access to SerialLightManager
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SerialLightManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLightManager.class.getName());

}
