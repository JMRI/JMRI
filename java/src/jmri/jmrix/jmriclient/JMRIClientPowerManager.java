// JMRIClientPowerManager.java
package jmri.jmrix.jmriclient;

import jmri.JmriException;
import jmri.PowerManager;

/**
 * PowerManager implementation for controlling layout power
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @author	Paul Bender Copyright (C) 2010
 * @version	$Revision$
 */
public class JMRIClientPowerManager implements PowerManager, JMRIClientListener {

    private JMRIClientSystemConnectionMemo memo = null;

    public JMRIClientPowerManager(JMRIClientSystemConnectionMemo memo) {
        // connect to the TrafficManager
        this.memo = memo;
        tc = this.memo.getJMRIClientTrafficController();
        tc.addJMRIClientListener(this);
    }

    public String getUserName() {
        return this.memo.getUserName();
    }

    int power = UNKNOWN;

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

    public int getPower() {
        return power;
    }

    // to free resources when no longer used
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

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    JMRIClientTrafficController tc = null;

    // to listen for status changes from JMRIClient system
    public void reply(JMRIClientReply m) {
        if (m.toString().contains("ON")) {
            power = PowerManager.ON;
        } else {
            power = PowerManager.OFF;
        }
        firePropertyChange("Power", null, null);
    }

    public void message(JMRIClientMessage m) {
    }

}


/* @(#)JMRIClientPowerManager.java */
