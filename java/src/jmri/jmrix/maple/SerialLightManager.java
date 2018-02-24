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

    MapleSystemConnectionMemo _memo = null;
    protected String prefix = "M";

    public SerialLightManager() {

    }

    public SerialLightManager(MapleSystemConnectionMemo memo) {
        _memo = memo;
        prefix = memo.getSystemPrefix();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * {@inheritDoc}
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
        if (SerialAddress.validSystemNameFormat(systemName, 'L', getSystemPrefix()) == NameValidity.VALID) {
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
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'L', getSystemPrefix()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (SerialAddress.validSystemNameConfig(systemName, 'L', _memo));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String normalizeSystemName(String systemName) {
        return (SerialAddress.normalizeSystemName(systemName, getSystemPrefix()));
    }

    /**
     * {@inheritDoc}
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

    private final static Logger log = LoggerFactory.getLogger(SerialLightManager.class);

}
