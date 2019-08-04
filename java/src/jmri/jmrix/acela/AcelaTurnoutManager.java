package jmri.jmrix.acela;

import java.util.Locale;
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
    public AcelaSystemConnectionMemo getMemo() {
        return (AcelaSystemConnectionMemo) memo;
    }

    /**
     * Method to create a new Turnout based on the system name.
     * <p>
     * Assumes calling method has checked that a Turnout with this
     * system name does not already exist.
     *
     * @return null if the system name is not in a valid format
     */
    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout trn = null;
        // check if the output bit is available
        int nAddress = -1;
        nAddress = AcelaAddress.getNodeAddressFromSystemName(systemName, getMemo());
        if (nAddress == -1) {
            return (null);
        }
        int bitNum = AcelaAddress.getBitFromSystemName(systemName, getSystemPrefix());
        if (bitNum == -1) {
            return (null);
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
    public String validateSystemNameFormat(String systemName, Locale locale) {
        return super.validateIntegerSystemNameFormat(systemName,
                AcelaAddress.MINOUTPUTADDRESS,
                AcelaAddress.MAXOUTPUTADDRESS,
                locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (AcelaAddress.validSystemNameFormat(systemName, 'T', getSystemPrefix()));
    }

    /**
     * Public method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in the current
     * configuration, else return 'false'
     */
    public boolean validSystemNameConfig(String systemName) {
        return (AcelaAddress.validSystemNameConfig(systemName, 'T', getMemo()));
    }

    /**
     * Public method to convert system name to its alternate format
     * <p>
     * Returns a normalized system name if system name is valid and has a valid
     * alternate representation, else return "".
     */
    public String convertSystemNameToAlternate(String systemName) {
        return (AcelaAddress.convertSystemNameToAlternate(systemName, getSystemPrefix()));
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaTurnoutManager.class);

}
