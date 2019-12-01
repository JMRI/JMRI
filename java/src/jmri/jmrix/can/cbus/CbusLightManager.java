package jmri.jmrix.can.cbus;

import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.Light;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement LightManager for CAN CBUS systems.
 * <p>
 * System names are "ML+n;-m", where M is the user configurable system prefix, n
 * and m are the events (signed for on/off, separated by ;).
 * <p>
 * Lights must be explicitly created, they are not polled.
 *
 * @author Matthew Harris Copyright (C) 2015
 * @author Egbert Broerse Copyright (C) 2019
 * @since 3.11.7
 */
public class CbusLightManager extends AbstractLightManager {

    /**
     * Ctor using a given system connection memo
     */
    public CbusLightManager(CanSystemConnectionMemo memo) {
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
    public Light provideLight(@Nonnull String name) {
        //log.debug("passed name {}", name);
        Light result = getLight(name);
        if (result == null) {
            return newLight(makeSystemName(name), null); // checks for validity
        }
        return result;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @return never null
     */
    @Override
    protected Light createNewLight(String systemName, String userName) {
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
        Light l = new CbusLight(getSystemPrefix(), newAddress, getMemo().getTrafficController());
        l.setUserName(userName);
        return l;
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
        } catch (StringIndexOutOfBoundsException e) {
            return NameValidity.INVALID;
        }
        try {
            validateAddressFormat(addr);
        } catch (IllegalArgumentException e) {
            return NameValidity.INVALID;
        }
        return NameValidity.VALID;
    }

    /**
     * Work out the details for Cbus hardware address validation. Logging of
     * handled cases no higher than WARN.
     *
     * @param address the hardware address to check
     * @throws IllegalArgumentException when delimiter is not found
     */
    void validateAddressFormat(String address) throws IllegalArgumentException {
        String newAddress = CbusAddress.validateSysName(address);
        log.debug("validated system name {}", newAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        try {
            validateAddressFormat(addr);
        } catch (IllegalArgumentException e) {
            log.debug("Warning: {}", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public Light getBySystemName(@Nonnull String key) {
        return _tsys.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private static final Logger log = LoggerFactory.getLogger(CbusLightManager.class);

}
