package jmri.jmrix.cmri.serial;

import java.util.Locale;
import javax.annotation.Nonnull;
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

    public SerialLightManager(CMRISystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public CMRISystemConnectionMemo getMemo() {
        return (CMRISystemConnectionMemo) memo;
    }

    /**
     * Create a new Light based on the system name and optional user name.
     * <p>
     * Assumes calling method has checked that a Light with this system
     * name does not already exist.
     *
     * @throws IllegalArgumentException if the
     * system name is not in a valid format or if the system name does not
     * correspond to a configured C/MRI digital output bit.
     * @return New Light.
     */
    @Override
    @Nonnull
    protected Light createNewLight(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        Light lgt = null;
        // check if the output bit is available
        int nAddress;
        nAddress = getMemo().getNodeAddressFromSystemName(systemName);
        if (nAddress == -1) {
            throw new IllegalArgumentException("Invalid Node Address from System Name: " + systemName);
        }
        int bitNum = getMemo().getBitFromSystemName(systemName);
        if (bitNum == 0) {
            throw new IllegalArgumentException("Invalid Bit from System Name: " + systemName);
        }
        String conflict;
        conflict = getMemo().isOutputBitFree(nAddress, bitNum);
        if (!conflict.isEmpty()) {
            log.error("Assignment conflict with {}.  Light not created.", conflict);
            throw new IllegalArgumentException(Bundle.getMessage("ErrorAssignDialog", bitNum, conflict));
        }
        // Validate the systemName
        if (getMemo().validSystemNameFormat(systemName, 'L') == NameValidity.VALID) {
            lgt = new SerialLight(systemName, userName,getMemo());
            if (!getMemo().validSystemNameConfig(systemName, 'L',getMemo().getTrafficController())) {
                log.warn("Light system Name does not refer to configured hardware: {}", systemName);
            }
        } else {
            log.error("Invalid Light system Name format: {}", systemName);
            throw new IllegalArgumentException("Invalid Light system Name format: " + systemName);
        }
        return lgt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String systemName, @Nonnull Locale locale) {
        return getMemo().validateSystemNameFormat(super.validateSystemNameFormat(systemName, locale), typeLetter(), locale);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return getMemo().validSystemNameFormat(systemName, typeLetter());
    }

    /**
     * Public method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current configuration,
     * else return 'false'
     */
    @Override
    public boolean validSystemNameConfig(@Nonnull String systemName) {
        return getMemo().validSystemNameConfig(systemName, 'L',getMemo().getTrafficController());
    }

    /**
     * Public method to convert system name to its alternate format
     *
     * @return a normalized system name if system name is valid and has a valid
     * alternate representation, else returns ""
     */
    @Override
    @Nonnull
    public String convertSystemNameToAlternate(@Nonnull String systemName) {
        return getMemo().convertSystemNameToAlternate(systemName);
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
