package jmri.jmrix.easydcc;

import java.beans.PropertyChangeListener;

import jmri.JmriException;
import jmri.PowerManager;

/**
 * PowerManager implementation for controlling layout power
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class EasyDccPowerManager implements PowerManager, EasyDccListener {

    public EasyDccPowerManager(EasyDccSystemConnectionMemo memo) {
        this.userName = memo.getUserName();
        // connect to the TrafficManager
        trafficController = memo.getTrafficController();
        trafficController.addEasyDccListener(this);
    }

    String userName = "EasyDcc"; // NOI18N
    private EasyDccTrafficController trafficController = null;

    @Override
    public String getUserName() {
        return userName;
    }

    int power = UNKNOWN;

    boolean waiting = false;
    int onReply = UNKNOWN;

    @Override
    public void setPower(int v) throws JmriException {
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.ON;
            // send "Enable main track"
            EasyDccMessage l = EasyDccMessage.getEnableMain();
            trafficController.sendEasyDccMessage(l, this);
        } else if (v == OFF) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.OFF;
            firePropertyChange("Power", null, null); // NOI18N
            // send "Kill main track"
            EasyDccMessage l = EasyDccMessage.getKillMain();
            trafficController.sendEasyDccMessage(l, this);
        }
        firePropertyChange("Power", null, null); // NOI18N
    }

    @Override
    public int getPower() {
        return power;
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
        trafficController.removeEasyDccListener(this);
        trafficController = null;
    }

    private void checkTC() throws JmriException {
        if (trafficController == null) {
            throw new JmriException("attempt to use EasyDccPowerManager after dispose");
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

    // to listen for status changes from EasyDcc system
    @Override
    public void reply(EasyDccReply m) {
        if (waiting) {
            power = onReply;
            firePropertyChange("Power", null, null); // NOI18N
        }
        waiting = false;
    }

    @Override
    public void message(EasyDccMessage m) {
        if (m.isKillMain()) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.OFF;
        } else if (m.isEnableMain()) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.ON;
        }
    }

}
