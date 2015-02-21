// CbusPowerManager.java
package jmri.jmrix.can.cbus;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;

/**
 * PowerManager implementation for controlling CBUS layout power.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author	Andrew CRosland Copyright (C) 2009
 * @version	$Revision$
 */
public class CbusPowerManager implements PowerManager, CanListener {

    public CbusPowerManager(CanSystemConnectionMemo memo) {
        // connect to the TrafficManager
        this.memo = memo;
        tc = memo.getTrafficController();
        tc.addCanListener(this);
    }

    CanSystemConnectionMemo memo;

    public String getUserName() {
        if (memo != null) {
            return memo.getUserName();
        }
        return "CBUS";
    }

    int power = ON;

    public void setPower(int v) throws JmriException {
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // send "Enable main track"
            tc.sendCanMessage(CbusMessage.getRequestTrackOn(tc.getCanid()), this);
        } else if (v == OFF) {
            // send "Kill main track"
            tc.sendCanMessage(CbusMessage.getRequestTrackOff(tc.getCanid()), this);
        }
    }

    /*
     * Used to update power state after service mode programming operation
     * without sending a message to the SPROG
     */
    public void notePowerState(int v) {
        power = v;
        firePropertyChange("Power", null, null);
    }

    public int getPower() {
        return power;
    }

    // to free resources when no longer used
    public void dispose() throws JmriException {
        tc.removeCanListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use CbusPowerManager after dispose");
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

    TrafficController tc = null;

    // to listen for status changes from Cbus system
    public void reply(CanReply m) {
        if (CbusMessage.isTrackOff(m)) {
            power = OFF;
            firePropertyChange("Power", null, null);
        } else if (CbusMessage.isTrackOn(m)) {
            power = ON;
            firePropertyChange("Power", null, null);
        } else if (CbusMessage.isArst(m)) {
            power = ON;
            firePropertyChange("Power", null, null);
        }
    }

    public void message(CanMessage m) {
        // do nothing
    }

}

/* @(#)CbusPowerManager.java */
