package jmri.jmrix.maple;

import java.util.Locale;
import javax.annotation.Nonnull;
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
    @Nonnull
    public MapleSystemConnectionMemo getMemo() {
        return (MapleSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected Light createNewLight(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        
        // check if the output bit is available
        int bitNum = SerialAddress.getBitFromSystemName(systemName, getSystemPrefix());
        if (bitNum == 0) {
            throw new IllegalArgumentException("Invalid Bit from System Name: " + systemName);
        }
        String conflict = SerialAddress.isOutputBitFree(bitNum, getSystemPrefix());
        if (!conflict.isEmpty()) {
            log.error("Assignment conflict with '{}'. Light not created.", conflict);
            throw new IllegalArgumentException("The output bit, " + bitNum + ", is currently assigned to " + conflict);
        }
        // Validate the System Name
        String sysName = SerialAddress.normalizeSystemName(systemName, getSystemPrefix());
        if (sysName.isEmpty()) {
            log.error("error when normalizing system name {}", systemName);
            throw new IllegalArgumentException("Error when normalizing system name: "+systemName);
        }
        if (SerialAddress.validSystemNameFormat(systemName, 'L', getSystemPrefix()) == NameValidity.VALID) {
            Light lgt = new SerialLight(sysName, userName, getMemo());
            if (!SerialAddress.validSystemNameConfig(sysName, 'L', getMemo())) {
                log.warn("Light system Name '{}' does not refer to configured hardware.", sysName);
                javax.swing.JOptionPane.showMessageDialog(null, "WARNING - The Light just added, " + sysName
                        + ", refers to an unconfigured output bit.", "Configuration Warning",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE, null);
            }
            return lgt;
        } else {
            log.error("Invalid Light system Name format: {}", systemName);
            throw new IllegalArgumentException("Invalid Light system Name format: " + systemName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        return SerialAddress.validateSystemNameFormat(name, this, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, typeLetter(), getSystemPrefix()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validSystemNameConfig(@Nonnull String systemName) {
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
