// NcePowerManager.java

package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.PowerManager;

/**
 * PowerManager implementation for controlling layout power.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.2 $
 */
public class NcePowerManager implements PowerManager, NceListener {

    public NcePowerManager() {
        // connect to the TrafficManager
        tc = NceTrafficController.instance();
        tc.addNceListener(this);
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
            NceMessage l = NceMessage.getEnableMain();
            tc.sendNceMessage(l, this);
        } else if (v==OFF) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.OFF;
            firePropertyChange("Power", null, null);
            // send "Kill main track"
            NceMessage l = NceMessage.getKillMain();
            tc.sendNceMessage(l, this);
        }
        firePropertyChange("Power", null, null);
    }

    public int getPower() { return power;}

    // to free resources when no longer used
    public void dispose() throws JmriException {
        tc.removeNceListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) throw new JmriException("attempt to use NcePowerManager after dispose");
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

    NceTrafficController tc = null;

    // to listen for status changes from NCE system
    public void reply(NceReply m) {
        if (waiting) {
            power = onReply;
            firePropertyChange("Power", null, null);
        }
        waiting = false;
    }

    public void message(NceMessage m) {
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


/* @(#)NcePowerManager.java */
