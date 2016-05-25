// XpaPowerManager.java
package jmri.jmrix.xpa;

import jmri.JmriException;
import jmri.PowerManager;

/**
 * PowerManager implementation for controlling layout power from an XPA+modem
 * connected to an XPressNet based system.
 *
 * @author	Paul Bender Copyright (C) 2004
 * @version	$Revision$
 *
 */
public class XpaPowerManager implements PowerManager, XpaListener {

    public XpaPowerManager(XpaTrafficController t) {
        // connect to the TrafficManager
        tc = t;
        tc.addXpaListener(this);
    }

    public String getUserName() {
        return "XPA";
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
        } else if (v == OFF) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.OFF;
            firePropertyChange("Power", null, null);
        }
        // send "Emergency Off/Emergency Stop"
        XpaMessage l = XpaMessage.getEStopMsg();
        tc.sendXpaMessage(l, this);
        firePropertyChange("Power", null, null);
    }

    public int getPower() {
        return power;
    }

    // to free resources when no longer used
    public void dispose() throws JmriException {
        tc.removeXpaListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use XpaPowerManager after dispose");
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

    XpaTrafficController tc = null;

    // to listen for status changes from Xpa system
    public void reply(XpaMessage m) {
        if (waiting) {
            power = onReply;
            firePropertyChange("Power", null, null);
        }
        waiting = false;
    }

    public void message(XpaMessage m) {
    }

}

/* @(#)XpaPowerManager.java */
