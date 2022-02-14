package jmri.managers;

import jmri.JmriException;
import jmri.PowerManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;
import jmri.beans.PropertyChangeSupport;
import jmri.SystemConnectionMemo;

/**
 * Base PowerManager implementation for controlling layout power.
 * <p>
 * These are registered when they are added to the InstanceManager
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2010
 * @author Randall Wood Copyright 2020
 * @param <M> the type of SystemConnectionMemo supported by this PowerManager
 */
abstract public class AbstractPowerManager<M extends SystemConnectionMemo> extends PropertyChangeSupport implements PowerManager {

    protected final M memo;
    /**
     * Note that all changes must fire a property change with the old and new values
     */
    protected int power = UNKNOWN;
    private Instant lastOn;

    public AbstractPowerManager(M memo) {
        this.memo = memo;
        TimeKeeper tk = new TimeKeeper();
        AbstractPowerManager.this.addPropertyChangeListener(tk);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPower() {
        return power;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPower(int state) throws JmriException {
        int old = power;
        power = state;
        firePowerPropertyChange(old, power);
    }

    /** {@inheritDoc} */
    @Override
    public final String getUserName() {
        return memo.getUserName();
    }

    // a class for listening for power state changes
    public class TimeKeeper implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (POWER.equals(e.getPropertyName())) {
                int newPowerState = getPower();
                if (newPowerState != power) {
                    power = newPowerState;
                    if (newPowerState == ON) {
                        lastOn = Instant.now();
                    }
                }
            }
        }
    }

    /**
     * Returns the amount of time since the layout was last powered up,
     * in milliseconds. If the layout has not been powered up as far as
     * JMRI knows it returns a very long time indeed.
     *
     * @return long int
     */
    public long timeSinceLastPowerOn() {
        if (lastOn == null) {
            return Long.MAX_VALUE;
        }
        return Instant.now().toEpochMilli() - lastOn.toEpochMilli();
    }

    /**
     * Fires a {@link java.beans.PropertyChangeEvent} for the power state using
     * property name "power".
     *
     * @param old the old power state
     * @param current the new power state
     */
    protected final void firePowerPropertyChange(int old, int current) {
        firePropertyChange(POWER, old, current);
    }
}
