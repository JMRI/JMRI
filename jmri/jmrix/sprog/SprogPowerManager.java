// SprogPowerManager.java

package jmri.jmrix.sprog;

import jmri.JmriException;
import jmri.PowerManager;

/**
 * PowerManager implementation for controlling SPROG layout power.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.3 $
 */
public class SprogPowerManager implements PowerManager, SprogListener {

    public SprogPowerManager() {
        // connect to the TrafficManager
        tc = SprogTrafficController.instance();
        tc.addSprogListener(this);
    }

    int power = UNKNOWN;

    boolean waiting = false;
    int onReply = UNKNOWN;

    public void setPower(int v) throws JmriException {
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v==ON) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.ON;
            // send "Enable main track"
            SprogMessage l = SprogMessage.getEnableMain();
            tc.sendSprogMessage(l, this);
        } else if (v==OFF) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.OFF;
            firePropertyChange("Power", null, null);
            // send "Kill main track"
            SprogMessage l = SprogMessage.getKillMain();
            tc.sendSprogMessage(l, this);
        }
        firePropertyChange("Power", null, null);
    }

    public int getPower() { return power;}

    // to free resources when no longer used
    public void dispose() throws JmriException {
        tc.removeSprogListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) throw new JmriException("attempt to use SprogPowerManager after dispose");
    }

    // to hear of changes
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    SprogTrafficController tc = null;

    // to listen for status changes from Sprog system
    public void reply(SprogReply m) {
        if (waiting) {
            power = onReply;
            firePropertyChange("Power", null, null);
        }
        waiting = false;
    }

    public void message(SprogMessage m) {
        if (m.isKillMain() ) {
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


/* @(#)SprogPowerManager.java */
