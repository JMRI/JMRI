package jmri.jmrix.rps;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the RPS-specific Sensor implementation.
 * <p>
 * System names are "RSpppp", where ppp is a CSV representation of the region.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2019
 */
public class RpsSensorManager extends jmri.managers.AbstractSensorManager {

    public RpsSensorManager(RpsSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public RpsSystemConnectionMemo getMemo() {
        return (RpsSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     * <p>
     * System Name is normalized.
     * Assumes calling method has checked that a Sensor with this system
     * name does not already exist.
     *
     * @throws IllegalArgumentException if the system name is not in a valid format
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        try {
           RpsSensor r = new RpsSensor(systemName, userName, getSystemPrefix());
           Distributor.instance().addMeasurementListener(r);
           return r;
       } catch(java.lang.StringIndexOutOfBoundsException sioe){
            throw new IllegalArgumentException("Invalid System Name: " + systemName);
       }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        if (!prefix.equals(getSystemPrefix())) {
            log.warn("prefix does not match memo.prefix");
            throw new JmriException("Unable to convert " + curAddress + ", Prefix does not match");
        }
        String sys = getSystemPrefix() + typeLetter() + curAddress;
        // first, check validity
        try {
            validSystemNameFormat(sys);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        return sys;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        return getMemo().validateSystemNameFormat(name, this, locale);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return getMemo().validSystemNameFormat(systemName, typeLetter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(RpsSensorManager.class);

}
