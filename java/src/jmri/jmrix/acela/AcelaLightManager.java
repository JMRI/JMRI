package jmri.jmrix.acela;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement LightManager for Acela systems.
 * <p>
 * System names are "ALnnn", where A is the user configurable system prefix,
 * nnn is the bit number without padding.
 * <p>
 * Based in part on AcelaTurnoutManager.java
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Coleman Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaLightManager extends AbstractLightManager {

    public AcelaLightManager(AcelaSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    @Nonnull
    public AcelaSystemConnectionMemo getMemo() {
        return (AcelaSystemConnectionMemo) memo;
    }

    /**
     * Method to create a new Light based on the system name.
     * <p>
     * Assumes calling method has checked that a Light with this system
     * name does not already exist.
     *
     * @return null if the system name is not in a valid format
     */
    @Override
    @Nonnull
    protected Light createNewLight(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        Light lgt = null;
        // check if the output bit is available
        int nAddress = AcelaAddress.getNodeAddressFromSystemName(systemName, getMemo());
        if (nAddress == -1) {
            throw new IllegalArgumentException("Invalid Node Address from System Name: " + systemName);
        }
        int bitNum = AcelaAddress.getBitFromSystemName(systemName, getSystemPrefix());
        if (bitNum == -1) {
            throw new IllegalArgumentException("Invalid Bit from System Name: " + systemName);
        }

        // Validate the systemName
        if (AcelaAddress.validSystemNameFormat(systemName, 'L', getSystemPrefix()) == NameValidity.VALID) {
            lgt = new AcelaLight(systemName, userName, getMemo());
            if (!AcelaAddress.validSystemNameConfig(systemName, 'L', getMemo())) {
                log.warn("Light System Name does not refer to configured hardware: {}", systemName);
            }
        } else {
            log.error("Invalid Light System Name format: {}", systemName);
            throw new IllegalArgumentException("Invalid Light System Name format: " + systemName);
        }
        return lgt;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Verifies system name has valid prefix and is an integer from
     * {@value AcelaAddress#MINOUTPUTADDRESS} to
     * {@value AcelaAddress#MAXOUTPUTADDRESS}.
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String systemName, @Nonnull Locale locale) {
        return super.validateIntegerSystemNameFormat(systemName,
                AcelaAddress.MINOUTPUTADDRESS,
                AcelaAddress.MAXOUTPUTADDRESS,
                locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return (AcelaAddress.validSystemNameFormat(systemName, 'L', getSystemPrefix()));
    }

    /**
     * Public method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current configuration,
     * else returns 'false'
     */
    @Override
    public boolean validSystemNameConfig(@Nonnull String systemName) {
        return (AcelaAddress.validSystemNameConfig(systemName, 'L', getMemo()));
    }

    /**
     * Public method to convert system name to its alternate format.
     *
     * @return a normalized system name if system name is valid and has a valid
     * alternate representation, else return ""
     */
    @Override
    @Nonnull
    public String convertSystemNameToAlternate(@Nonnull String systemName) {
        return (AcelaAddress.convertSystemNameToAlternate(systemName, getSystemPrefix()));
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaLightManager.class);

}
