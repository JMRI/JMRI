package jmri.jmrix.can.cbus;

import java.util.Locale;
import javax.annotation.*;
import jmri.*;
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
     * @param memo System Connection
     */
    public CbusLightManager(CanSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public CanSystemConnectionMemo getMemo() {
        return (CanSystemConnectionMemo) memo;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @return never null
     */
    @Nonnull
    @Override
    protected Light createNewLight(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        String addr;
        // first, check validity
        try {
            validateSystemNameFormat(systemName);
            addr = systemName.substring(getSystemNamePrefix().length());
        } catch (IllegalArgumentException e) {
            log.error("Unable to create CbusLight, {}", e.getMessage());
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
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        validateSystemNamePrefix(name, locale);
        try {
            CbusAddress.validateSysName(name.substring(getSystemNamePrefix().length()));
        } catch (IllegalArgumentException ex) {
            throw new jmri.NamedBean.BadSystemNameException(locale, "InvalidSystemNameCustom", ex.getMessage());
        }
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        String addr;
        try {
            addr = systemName.substring(getSystemPrefix().length() + 1); // get only the address part
            CbusAddress.validateSysName(addr);
        } catch (StringIndexOutOfBoundsException | IllegalArgumentException e) {
            return NameValidity.INVALID;
        }
        return NameValidity.VALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
        try {
            return prefix + typeLetter() + CbusAddress.validateSysName(curAddress);
        } catch (IllegalArgumentException e) {
            throw new jmri.JmriException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validSystemNameConfig(@Nonnull String systemName) {
        return validSystemNameFormat(systemName) == NameValidity.VALID;
    }

    @Override
    @Nonnull
    @CheckReturnValue
    public String getNextValidSystemName(@Nonnull NamedBean currentBean) throws JmriException {
        if (!allowMultipleAdditions(currentBean.getSystemName())) throw new UnsupportedOperationException("Not supported");

        String currentName = currentBean.getSystemName();
        String suffix = Manager.getSystemSuffix(currentName);
        String type = Manager.getTypeLetter(currentName);
        String prefix = Manager.getSystemPrefix(currentName);

        String nextName = CbusAddress.getIncrement(suffix);

        if (nextName==null) {
            throw new JmriException("No existing number found when incrementing " + currentName);
        }
        return prefix+type+nextName;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddCbusLightEntryToolTip");
    }

    private static final Logger log = LoggerFactory.getLogger(CbusLightManager.class);

}
