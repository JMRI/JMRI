package jmri.jmrix.can.cbus;

import java.beans.PropertyChangeListener;

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
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Andrew CRosland Copyright (C) 2009
 */
public class CbusPowerManager implements PowerManager, CanListener {

    public CbusPowerManager(CanSystemConnectionMemo memo) {
        // connect to the TrafficManager
        this.memo = memo;
        tc = memo.getTrafficController();
        addTc(tc);
    }

    CanSystemConnectionMemo memo;

    @Override
    public String getUserName() {
        return memo.getUserName();
    }

    int power = ON;

    @Override
    public void setPower(int v) throws JmriException {
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // send "Enable main track"
            tc.sendCanMessage(CbusMessage.getRequestTrackOn(tc.getCanid()), this);
        }
        if (v == OFF) {
            // send "Kill main track"
            tc.sendCanMessage(CbusMessage.getRequestTrackOff(tc.getCanid()), this);
        }
    }

    @Override
    public int getPower() {
        return power;
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
        if (tc !=null) {
            tc.removeCanListener(this);
        }
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use CbusPowerManager after dispose");
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

    TrafficController tc = null;

    // to listen for status changes from Cbus system
    @Override
    public void reply(CanReply m) {
        if ( m.isExtended() || m.isRtr() ) {
            return;
        }
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

    @Override
    public void message(CanMessage m) {
        // do nothing
    }

}
