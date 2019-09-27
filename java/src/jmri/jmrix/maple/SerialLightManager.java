package jmri.jmrix.maple;

import java.util.Locale;
import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement LightManager for Maple serial systems.
 * <p>
 * System names are "KLnnn", where K is the user configurable system prefix,
 * nnn is the bit number without padding.
 * <p>
 * Based in part on SerialTurnoutManager.java
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Dave Duchamp Copyright (C) 2004, 2010
 */
public class SerialLightManager extends AbstractLightManager {

    public SerialLightManager(MapleSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapleSystemConnectionMemo getMemo() {
        return (MapleSystemConnectionMemo) memo;
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
            lgt = new SerialLight(sysName, userName, getMemo());
            if (!SerialAddress.validSystemNameConfig(sysName, 'L', getMemo())) {
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
    public String validateSystemNameFormat(String name, Locale locale) {
        return SerialAddress.validateSystemNameFormat(name, this, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, typeLetter(), getSystemPrefix()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (SerialAddress.validSystemNameConfig(systemName, 'L', getMemo()));
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
