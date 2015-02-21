// EasyDccPowerManager.java
package jmri.jmrix.easydcc;

import jmri.JmriException;
import jmri.PowerManager;

/**
 * PowerManager implementation for controlling layout power
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public class EasyDccPowerManager implements PowerManager, EasyDccListener {

    public EasyDccPowerManager(EasyDccSystemConnectionMemo memo) {
        this();
        this.userName = memo.getUserName();
    }

    public EasyDccPowerManager() {
        // connect to the TrafficManager
        tc = EasyDccTrafficController.instance();
        tc.addEasyDccListener(this);
    }

    String userName = "EasyDcc";

    public String getUserName() {
        return userName;
    }

    int power = UNKNOWN;

    boolean waiting = false;
    int onReply = UNKNOWN;

    public void setPower(int v) throws JmriException {
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.ON;
            // send "Enable main track"
            EasyDccMessage l = EasyDccMessage.getEnableMain();
            tc.sendEasyDccMessage(l, this);
        } else if (v == OFF) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.OFF;
            firePropertyChange("Power", null, null);
            // send "Kill main track"
            EasyDccMessage l = EasyDccMessage.getKillMain();
            tc.sendEasyDccMessage(l, this);
        }
        firePropertyChange("Power", null, null);
    }

    public int getPower() {
        return power;
    }

    // to free resources when no longer used
    public void dispose() throws JmriException {
        tc.removeEasyDccListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use EasyDccPowerManager after dispose");
        }
    }

    // to hear of changes
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    EasyDccTrafficController tc = null;

    // to listen for status changes from EasyDcc system
    public void reply(EasyDccReply m) {
        if (waiting) {
            power = onReply;
            firePropertyChange("Power", null, null);
        }
        waiting = false;
    }

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


/* @(#)EasyDccPowerManager.java */
