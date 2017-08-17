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
 * @author Dave Duchamp Copyright (C) 2004
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
        if (_memo.validSystemNameFormat(systemName, 'L')) {
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
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'
     */
    @Override
    public boolean validSystemNameFormat(String systemName) {
        return _memo.validSystemNameFormat(systemName, 'L');
    }

    /**
     * Public method to validate system name for configuration returns 'true' if
     * system name has a valid meaning in current configuration, else returns
     * 'false'
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return _memo.validSystemNameConfig(systemName, 'L',_memo.getTrafficController());
    }

    /**
     * Public method to normalize a system name
     * <P>
     * Returns a normalized system name if system name has a valid format, else
     * returns "".
     */
    @Override
    public String normalizeSystemName(String systemName) {
        return _memo.normalizeSystemName(systemName);
    }

    /**
     * Public method to convert system name to its alternate format
     * <P>
     * Returns a normalized system name if system name is valid and has a valid
     * alternate representation, else return "".
     */
    @Override
    public String convertSystemNameToAlternate(String systemName) {
        return _memo.convertSystemNameToAlternate(systemName);
    }

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddOutputEntryToolTip");
        return entryToolTip;
    }

    /**
     * Provide a manager-specific regex for the Add new item beantable pane.
     */
    @Override
    public String getEntryRegex() {
        return "^[0-9]{1,6}[:Bb]{0,1}[0-9]{1,3}$"; // examples 4B3, 4:3, see tooltip
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLightManager.class.getName());

}
