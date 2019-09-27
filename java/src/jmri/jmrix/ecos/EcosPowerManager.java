package jmri.jmrix.ecos;

import jmri.JmriException;
import jmri.PowerManager;

import java.beans.PropertyChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling ECoS layout power.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class EcosPowerManager implements PowerManager, EcosListener {

    public EcosPowerManager(EcosTrafficController etc) {
        // connect to the TrafficManager
        tc = etc;
        tc.addEcosListener(this);

        // ask to be notified
        EcosMessage m = new EcosMessage("request(1, view)");
        tc.sendEcosMessage(m, this);

        // get initial state
        m = new EcosMessage("get(1, status)");
        tc.sendEcosMessage(m, this);

    }

    EcosTrafficController tc;

    @Override
    public String getUserName() {
        return "ECoS";
    }

    int power = UNKNOWN;

    @Override
    public void setPower(int v) throws JmriException {
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // send message to turn on
            EcosMessage l = new EcosMessage("set(1, go)");
            tc.sendEcosMessage(l, this);
        } else if (v == OFF) {
            // send message to turn off
            EcosMessage l = new EcosMessage("set(1, stop)");
            tc.sendEcosMessage(l, this);
        }
        firePropertyChange("Power", null, null);
    }

    @Override
    public int getPower() {
        return power;
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
        tc.removeEcosListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use EcosPowerManager after dispose");
        }
    }

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

    // to listen for status changes from Ecos system
    @Override
    public void reply(EcosReply m) {
        // power message?
        String msg = m.toString();
        if (msg.contains("<EVENT 1>") || msg.contains("REPLY get(1,") || msg.contains("REPLY set(1,")) {
            if (msg.contains("status[GO]") || msg.contains("et(1, go)")) {
                log.debug("POWER ON DETECTED");
                power = ON;
                firePropertyChange("Power", null, null);
            } else if (msg.contains("status[STOP]") || msg.contains("et(1, stop)")) {
                log.debug("POWER OFF DETECTED");
                power = OFF;
                firePropertyChange("Power", null, null);
            }
        }
    }

    @Override
    public void message(EcosMessage m) {
        // messages are ignored
    }

    private final static Logger log = LoggerFactory.getLogger(EcosPowerManager.class);

}
