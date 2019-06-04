package jmri.managers;

import jmri.JmriException;
import jmri.PowerManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;

/**
 * Base PowerManager implementation for controlling layout power.
 * <p>
 * These are registered when they are added to the InstanceManager
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2003, 2010
 */
abstract public class AbstractPowerManager implements PowerManager {

    public AbstractPowerManager(jmri.jmrix.SystemConnectionMemo memo) {
        this.userName = memo.getUserName();
        TimeKeeper tk = new TimeKeeper();
        addPropertyChangeListener(tk);
    }

    private int powerState = UNKNOWN;
    private Instant lastOn;

    /** {@inheritDoc} */
    @Override
    public String getUserName() {
        return userName;
    }

    String userName;

    // to hear of changes
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    /** {@inheritDoc} */
    @Override
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /** {@inheritDoc} */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /** {@inheritDoc} */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    /** {@inheritDoc} */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    // a class for listening for power state changes
    public class TimeKeeper implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if ("Power".equals(e.getPropertyName())) {
                int newPowerState;
                try {
                    newPowerState = getPower();
                } catch (JmriException ex) {
                    return;
                }
                if (newPowerState != powerState) {
                    powerState = newPowerState;
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
    
}
