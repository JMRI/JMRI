package jmri.jmrix.can.cbus;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Turnout;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement TurnoutManager for CAN CBUS systems.
 * <p>
 * System names are "MT+n;-m", where M is the user configurable system prefix,
 * n and m are the events (signed for on/off, separated by ;).
 * <p>
 * Turnouts must be explicitly created, they are not polled.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @since 2.3.1
 */
public class CbusTurnoutManager extends AbstractTurnoutManager {

    /**
     * Ctor using a given system connection memo
     */
    public CbusTurnoutManager(CanSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CanSystemConnectionMemo getMemo() {
        return (CanSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Turnout provideTurnout(@Nonnull String name) {
        Turnout result = getTurnout(name);
        if (result == null) {
            result = newTurnout(makeSystemName(name), null);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Turnout createNewTurnout(String systemName, String userName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        // first, check validity
        try {
            validateAddressFormat(addr);
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            throw e;
        }
        // validate (will add "+" to unsigned int)
        String newAddress = CbusAddress.validateSysName(addr);
        // OK, make
        Turnout t = new CbusTurnout(getSystemPrefix(), newAddress, getMemo().getTrafficController());
        t.setUserName(userName);
        return t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        // first, check validity
        try {
            validateAddressFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        // prefix unsigned int with "+" as service to user
        String newAddress = CbusAddress.validateSysName(curAddress);
        return prefix + typeLetter() + newAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextValidAddress(String curAddress, String prefix) throws JmriException {
        String testAddr = curAddress;
        // make sure starting name is valid
        try {
            validateAddressFormat(testAddr);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        testAddr = CbusAddress.validateSysName(testAddr); // normalize Merg address
        Turnout t = getBySystemName(prefix + typeLetter() + testAddr);
        if (t != null) {
            // build local addresses
            for (int x = 1; x < 10; x++) {
                testAddr = CbusAddress.getIncrement(testAddr); // getIncrement will perform a max check on the numbers
                t = getBySystemName(prefix + typeLetter() + testAddr);
                if (t == null) {
                    // If the hardware address + x does not already exist,
                    // then this can be considered the next valid address.
                    return testAddr;
                }
            }
            // feedback when next address is also in use
            log.warn("10 hardware addresses starting at {} already in use. No new {} Turnouts added", curAddress, getMemo().getUserName());
            return null;
        } else {
            // If the initially requested hardware address does not already exist,
            // then this can be considered the next valid address.
            return testAddr;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        validateSystemNamePrefix(name, locale);
        try {
            validateAddressFormat(name.substring(getSystemNamePrefix().length()));
        } catch (IllegalArgumentException ex) {
            throw new jmri.NamedBean.BadSystemNameException(locale, "InvalidSystemNameCBUS", ex.getMessage());
        }
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        String addr;
        try {
            addr = systemName.substring(getSystemPrefix().length() + 1); // get only the address part
        } catch (StringIndexOutOfBoundsException e){
            return NameValidity.INVALID;
        }
        try {
            validateAddressFormat(addr);
        } catch (IllegalArgumentException e){
            return NameValidity.INVALID;
        }
        return NameValidity.VALID;
    }

    /**
     * Work out the details for Cbus hardware address validation.
     * Logging of handled cases no higher than WARN.
     *
     * @param address the hardware address to check
     * @throws IllegalArgumentException when delimiter is not found
     */
    void validateAddressFormat(String address) throws IllegalArgumentException {
        String newAddress = CbusAddress.validateSysName(address);
        log.debug("validated system name {}", newAddress);
    }


    /** {@inheritDoc} */
    @Override
    public Turnout getBySystemName(@Nonnull String key) {
        return _tsys.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(CbusTurnoutManager.class);

}
