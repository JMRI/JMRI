package jmri.jmrix.acela;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Acela systems.
 * <p>
 * System names are "ATnnn", where A is the user configurable system prefix,
 * nnn is the bit number without padding.
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Coleman Copyright (C) 2008 Based on CMRI serial example, modified
 * to establish Acela support.
 */
public class AcelaTurnoutManager extends AbstractTurnoutManager {

    public AcelaTurnoutManager(AcelaSystemConnectionMemo memo) {
       super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public AcelaSystemConnectionMemo getMemo() {
        return (AcelaSystemConnectionMemo) memo;
    }

    /**
     * Method to create a new Turnout based on the system name.
     * <p>
     * Assumes calling method has checked that a Turnout with this
     * system name does not already exist.
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        Turnout trn = null;
        // check if the output bit is available
        int nAddress = AcelaAddress.getNodeAddressFromSystemName(systemName, getMemo());
        if (nAddress == -1) {
            throw new IllegalArgumentException("Cannot get Node Address from System Name " + systemName);
        }
        int bitNum = AcelaAddress.getBitFromSystemName(systemName, getSystemPrefix());
        if (bitNum == -1) {
            throw new IllegalArgumentException("Cannot get Bit Number from System Name " + systemName);
        }

        // Validate the systemName
        if (AcelaAddress.validSystemNameFormat(systemName, 'T', getSystemPrefix()) == NameValidity.VALID) {
            trn = new AcelaTurnout(systemName, userName, getMemo());
            if (!AcelaAddress.validSystemNameConfig(systemName, 'T', getMemo())) {
                log.warn("Turnout System Name does not refer to configured hardware: {}", systemName);
            }
        } else {
            log.error("Invalid Turnout system Name format: {}", systemName);
            throw new IllegalArgumentException("Invalid Turnout System Name format: " + systemName);
        }
        return trn;
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
        return (AcelaAddress.validSystemNameFormat(systemName, 'T', getSystemPrefix()));
    }

    /**
     * Public method to validate system name for configuration.
     *
     * @param systemName system name to validate
     * @return 'true' if system name has a valid meaning in the current
     * configuration, else return 'false'
     */
    public boolean validSystemNameConfig(@Nonnull String systemName) {
        return (AcelaAddress.validSystemNameConfig(systemName, 'T', getMemo()));
    }

    /**
     * Public method to convert system name to its alternate format.
     * <p>
     * @param systemName system name to convert
     * @return a normalized system name if system name is valid and has a valid
     * alternate representation, else return ""
     */
    public String convertSystemNameToAlternate(String systemName) {
        return (AcelaAddress.convertSystemNameToAlternate(systemName, getSystemPrefix()));
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaTurnoutManager.class);

}
