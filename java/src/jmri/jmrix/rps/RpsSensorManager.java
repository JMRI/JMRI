package jmri.jmrix.rps;

import java.util.Locale;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Manage the RPS-specific Sensor implementation.
 * <p>
 * System names are "RSpppp", where ppp is a CSV representation of the region.
 *
 * @author	Bob Jacobsen Copyright (C) 2007, 2019
 */
public class RpsSensorManager extends jmri.managers.AbstractSensorManager {

    public RpsSensorManager(RpsSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public RpsSystemConnectionMemo getMemo() {
        return (RpsSystemConnectionMemo) memo;
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Create a new sensor if all checks are passed.
     * System name is normalized to ensure uniqueness.
     */
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
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
    @Nonnull
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        if (!prefix.equals(getSystemPrefix())) {
            log.warn("prefix does not match memo.prefix");
            return null;
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
    @Nonnull
    @Override
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
