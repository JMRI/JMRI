/**
 * DCCppPowerManager.java
 *
 * Description: PowerManager implementation for controlling layout power
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Mark Underwood Copyright (C) 2015
  *
 * Based on XNetPowerManager by Bob Jacobsen and Paul Bender
 */
package jmri.jmrix.dccpp;

import jmri.JmriException;
import jmri.PowerManager;

import java.beans.PropertyChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DCCppPowerManager implements PowerManager, DCCppListener {

    public DCCppPowerManager(DCCppSystemConnectionMemo memo) {
        // connect to the TrafficManager
        tc = memo.getDCCppTrafficController();
        tc.addDCCppListener(DCCppInterface.CS_INFO, this);
        userName = memo.getUserName();
        // request the current command station status
        tc.sendDCCppMessage(DCCppMessage.makeCSStatusMsg(), this);
    }

    @Override
    public String getUserName() {
        return "DCC++";
    }

    String userName = "DCC++";

    int power = UNKNOWN;

    @Override
    public void setPower(int v) throws JmriException {
        power = UNKNOWN;
        checkTC();
        if (v == ON) {
            // send TRACK_POWER_ON
            tc.sendDCCppMessage(DCCppMessage.makeTrackPowerOnMsg(), this);
        } else if (v == OFF) {
            // send TRACK_POWER_OFF
            tc.sendDCCppMessage(DCCppMessage.makeTrackPowerOffMsg(), this);
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
        tc.removeDCCppListener(DCCppInterface.CS_INFO, this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use PowerManager after dispose");
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

    DCCppTrafficController tc = null;

    // to listen for Broadcast messages related to track power.
    // There are 5 messages to listen for
    @Override
    public void message(DCCppReply m) {
        if (log.isDebugEnabled()) {
            log.debug("Message received: " + m.toString());
        }
        if (m.isPowerReply()) {
            if (m.getPowerBool()) {
                power = ON;
                firePropertyChange("Power", null, null);
            } else {
                power = OFF;
                firePropertyChange("Power", null, null);
            }
 }
 
    }

    // listen for the messages to the LI100/LI101
    @Override
    public void message(DCCppMessage l) {
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    // Initialize logging information
    private final static Logger log = LoggerFactory.getLogger(DCCppPowerManager.class);

}



