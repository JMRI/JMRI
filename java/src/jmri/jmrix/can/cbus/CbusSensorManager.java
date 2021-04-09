package jmri.jmrix.can.cbus;

import java.beans.PropertyChangeEvent;
import java.util.Locale;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            log.error(e.getMessage());
            throw e;
        }
        // OK, make
        Sensor s = new CbusSensor(getSystemPrefix(), newAddress, ((CanSystemConnectionMemo)getMemo()).getTrafficController());
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

    /**
     * Only increments by 1, which is fine for CBUS Sensors.
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected String getIncrement(String curAddress, int increment) throws JmriException {
        return CbusAddress.getIncrement(curAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        validateSystemNamePrefix(name, locale);
        try {
            name = CbusAddress.validateSysName(name.substring(getSystemNamePrefix().length()));
        } catch (IllegalArgumentException ex) {
            throw new jmri.NamedBean.BadSystemNameException(locale, "InvalidSystemNameCustom", ex.getMessage());
        }
        return getSystemNamePrefix() + name;
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

    /**
     * Update All Sensors by Requesting Event Status.
     * Sends a query message to each sensor using the active Sensor address.
     * e.g. for a CBUS address "-7;+5", the query will go to event 7.
     * Delay between sends is determined by the Connection Output Interval Setting.
     * {@inheritDoc}
     */
    @Override
    public void updateAll() {
        log.info("Requesting status for all sensors");
        int i = 0;
        for (Sensor nb : getNamedBeanSet()) {
            if (nb instanceof CbusSensor) {
                jmri.util.ThreadingUtil.runOnLayoutDelayed( () -> {
                    nb.requestUpdateFromLayout();
                }, (i * getMemo().getOutputInterval()) );
                i++;
            }
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        if (e.getPropertyName().equals("inverted")) {
            firePropertyChange("beaninverted", null, null); //IN18N
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CbusSensorManager.class);

}
