package jmri.jmrix.maple;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement light manager for Maple serial systems
 * <P>
 * System names are "KLnnn", where nnn is the bit number without padding.
 * <P>
 * Based in part on SerialTurnoutManager.java
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Dave Duchamp Copyright (C) 2004, 2010
  */
public class SerialLightManager extends AbstractLightManager {

//    public SerialLightManager() {
//
//    }

    public SerialLightManager(MapleSystemConnectionMemo memo) {
        _memo = memo;

    }

    MapleSystemConnectionMemo _memo = null;

    /**
     * Get the configured system prefix for this connection.
     */
    @Override
    public String getSystemPrefix() {
        return _memo.getSystemPrefix();
    }

    /**
     * Method to create a new Light based on the system name.
     * Assumes calling method has
     * checked that a Light with this system name does not already exist.
     *
     * @return null if the system name is not in a valid format or if the
     * system name does not correspond to a configured digital output bit
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
        // check if the output bit is available
        int bitNum = SerialAddress.getBitFromSystemName(systemName, getSystemPrefix());
        if (bitNum == 0) {
            return (null);
        }
        String conflict = "";
        conflict = SerialAddress.isOutputBitFree(bitNum, getSystemPrefix());
        if (!conflict.equals("")) {
            log.error("Assignment conflict with '{}'. Light not created.", conflict);
            notifyLightCreationError(conflict, bitNum);
            return (null);
        }
        // Validate the System Name
        String sysName = SerialAddress.normalizeSystemName(systemName, getSystemPrefix());
        if (sysName.equals("")) {
            log.error("error when normalizing system name {}", systemName);
            return null;
        }
        if (SerialAddress.validSystemNameFormat(systemName, 'L', getSystemPrefix())) {
            lgt = new SerialLight(sysName, userName, _memo);
            if (!SerialAddress.validSystemNameConfig(sysName, 'L', _memo)) {
                log.warn("Light system Name '{}' does not refer to configured hardware.", sysName);
                javax.swing.JOptionPane.showMessageDialog(null, "WARNING - The Light just added, " + sysName
                        + ", refers to an unconfigured output bit.", "Configuration Warning",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE, null);
            }
        } else {
            log.error("Invalid Light system Name format: {}", systemName);
        }
        return lgt;
    }

    /**
     * Public method to notify user of Light creation error.
     */
    public void notifyLightCreationError(String conflict, int bitNum) {
        javax.swing.JOptionPane.showMessageDialog(null, "The output bit, " + bitNum
                + ", is currently assigned to " + conflict + ". Light cannot be created as "
                + "you specified.", "Assignment Conflict",
                javax.swing.JOptionPane.INFORMATION_MESSAGE, null);
    }

    /**
     * Public method to validate system name format.
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public boolean validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'L', getSystemPrefix()));
    }

    /**
     * Public method to validate system name for configuration returns 'true' if
     * system name has a valid meaning in current configuration, else returns
     * 'false'
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (SerialAddress.validSystemNameConfig(systemName, 'L', _memo));
    }

    /**
     * Public method to normalize a system name.
     * <P>
     * Returns a normalized system name if system name has a valid format, else
     * returns "".
     */
    @Override
    public String normalizeSystemName(String systemName) {
        return (SerialAddress.normalizeSystemName(systemName, getSystemPrefix()));
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
     * Allow access to SerialLightManager
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SerialLightManager instance() {
        return null;
    }
    static SerialLightManager _instance = null;

    private final static Logger log = LoggerFactory.getLogger(SerialLightManager.class);

}
