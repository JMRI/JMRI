package jmri.jmrix.internal;

import javax.annotation.Nonnull;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import jmri.util.PreferNumericComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the InternalSensorManager interface.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2006
 */
public class InternalSensorManager extends jmri.managers.AbstractSensorManager {

    public InternalSensorManager(InternalSystemConnectionMemo memo) {
        super(memo);
    }

    /** {@inheritDoc} */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return a new (dummy) Internal sensor
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) {
        Sensor sen = new AbstractSensor(systemName, userName) {

            @Override
            public void requestUpdateFromLayout() {
                // nothing to do
            }

            @Override
            public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, NamedBean n) {
                return (new PreferNumericComparator()).compare(suffix1, suffix2);
            }
        };
        try {
            sen.setKnownState(getDefaultStateForNewSensors());
        } catch (jmri.JmriException ex) {
            log.error("An error occurred while trying to set initial state for sensor {}", sen.getDisplayName());
            log.error(ex.toString());
        }
        log.debug("Internal Sensor \"{}\", \"{}\" created", systemName, userName);
        return sen;
    }

    static int defaultState = NamedBean.UNKNOWN;

    public static synchronized void setDefaultStateForNewSensors(int defaultSetting) {
        log.debug("Default new-Sensor state set to {}", defaultSetting);
        defaultState = defaultSetting;
    }

    public static synchronized int getDefaultStateForNewSensors() {
        return defaultState;
    }

    protected String prefix = "I";

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public InternalSystemConnectionMemo getMemo() {
        return (InternalSystemConnectionMemo) memo;
    }
    
    /**
     * No validation for Internal Sensors.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
        return prefix + typeLetter() + curAddress;
    }

    private static final Logger log = LoggerFactory.getLogger(InternalSensorManager.class);

}
