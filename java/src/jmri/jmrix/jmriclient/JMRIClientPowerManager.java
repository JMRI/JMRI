package jmri.jmrix.jmriclient;

import java.beans.PropertyChangeListener;

import jmri.JmriException;
import jmri.PowerManager;

/**
 * PowerManager implementation for controlling layout power
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Paul Bender Copyright (C) 2010
 */
public class JMRIClientPowerManager implements PowerManager, JMRIClientListener {

    private JMRIClientSystemConnectionMemo memo = null;

    public JMRIClientPowerManager(JMRIClientSystemConnectionMemo memo) {
        // connect to the TrafficManager
        this.memo = memo;
        tc = this.memo.getJMRIClientTrafficController();
        tc.addJMRIClientListener(this);
    }

    @Override
    public String getUserName() {
        return this.memo.getUserName();
    }

    int power = UNKNOWN;

    @Override
    public void setPower(int v) throws JmriException {
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // send "Enable main track"
            JMRIClientMessage l = JMRIClientMessage.getEnableMain();
            tc.sendJMRIClientMessage(l, this);
        } else if (v == OFF) {
            // send "Kill main track"
            JMRIClientMessage l = JMRIClientMessage.getKillMain();
            tc.sendJMRIClientMessage(l, this);
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
        tc.removeJMRIClientListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use JMRIClientPowerManager after dispose");
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

    JMRIClientTrafficController tc = null;

    // to listen for status changes from JMRIClient system
    @Override
    public void reply(JMRIClientReply m) {
        if (m.toString().contains("ON")) {
            power = PowerManager.ON;
        } else {
            power = PowerManager.OFF;
        }
        firePropertyChange("Power", null, null);
    }

    @Override
    public void message(JMRIClientMessage m) {
    }

}
