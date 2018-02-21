package jmri.managers;

import jmri.JmriException;
import jmri.PowerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;

/**
 * Base PowerManager implementation for controlling layout power.
 * <p>
 * These are registered when they are added to the InstanceManager
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2001, 2003, 2010
 */
abstract public class AbstractPowerManager implements PowerManager {

    public AbstractPowerManager(jmri.jmrix.SystemConnectionMemo memo) {
        this.userName = memo.getUserName();
        TimeKeeper tk = new TimeKeeper();
        addPropertyChangeListener(tk);
    }

    int power = UNKNOWN;
    private Instant lastOn;

    @Override
    public String getUserName() {
        return userName;
    }

    String userName;

    // to hear of changes
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    @Override
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    @Override
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

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
                if (newPowerState != power) {
                    power = newPowerState;
                    if (newPowerState == ON) {
                        lastOn = Instant.now();
                    }
                    if (newPowerState == OFF) {
                        log.info("power has been on for " + timeSinceLastPowerOn() + " millisecs");
                    }

                }
            } else {
                log.info("property changed: " + e.getPropertyName());
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

    private final static Logger log = LoggerFactory.getLogger(PowerManager.class);
}
