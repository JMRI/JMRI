package jmri.jmrix.can.cbus;

import java.beans.PropertyChangeEvent;
import java.util.Locale;

import javax.annotation.*;

import jmri.*;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Implement SensorManager for CAN CBUS systems.
 * <p>
 * System names are "MS+n;-m", where M is the user configurable system prefix, n
 * and m are the events (signed for on/off, separated by ;).
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class CbusSensorManager extends jmri.managers.AbstractSensorManager {

    /**
     * Ctor using a given system connection memo
     * @param memo System Connection
     */
    public CbusSensorManager(CanSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the system name is not in a valid format
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        String addr = systemName.substring(getSystemNamePrefix().length());
        // first, check validity
        String newAddress;
        try {
            newAddress = CbusAddress.validateSysName(addr);
        } catch (IllegalArgumentException e) {
            log.error("Unable to create CbusSensor, {}", e.getMessage());
            throw e;
        }
        // OK, make
        Sensor s = new CbusSensor(getSystemPrefix(), newAddress,
            ((CanSystemConnectionMemo)getMemo()).getTrafficController());
        s.setUserName(userName);
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        // first, check validity
        String newAddress;
        try {
            validateSystemNamePrefix(prefix + typeLetter() + curAddress, Locale.getDefault());
            newAddress = CbusAddress.validateSysName(curAddress);
         } catch (IllegalArgumentException  e) {
             throw new JmriException(e.getMessage());
        }
        return prefix + typeLetter() + newAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    @Override
    @Nonnull
    @CheckReturnValue
    public String getNextValidSystemName(@Nonnull NamedBean currentBean) throws JmriException {
        if (!allowMultipleAdditions(currentBean.getSystemName())) {
            throw new UnsupportedOperationException("Not supported");
        }

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
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        validateSystemNamePrefix(name, locale);
        try {
            return getSystemNamePrefix() + CbusAddress.validateSysName(name.substring(getSystemNamePrefix().length()));
        } catch (IllegalArgumentException ex) {
            throw new jmri.NamedBean.BadSystemNameException(locale, "InvalidSystemNameCustom", ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        String addr;
        try {
            addr = systemName.substring(getSystemNamePrefix().length()); // get only the address part
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
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        if (e.getPropertyName().equals("inverted")) {
            firePropertyChange("beaninverted", null, null); //IN18N
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusSensorManager.class);

}
