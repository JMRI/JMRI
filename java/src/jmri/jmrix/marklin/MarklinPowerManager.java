package jmri.jmrix.marklin;

import jmri.JmriException;
import jmri.PowerManager;

import java.beans.PropertyChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling layout power.
 *
 * @author Kevin Dickerson (C) 2012
 */
public class MarklinPowerManager implements PowerManager, MarklinListener {

    public MarklinPowerManager(MarklinTrafficController etc) {
        // connect to the TrafficManager
        tc = etc;
        tc.addMarklinListener(this);

    }

    MarklinTrafficController tc;

    @Override
    public String getUserName() {
        return "Marklin";
    }

    int power = UNKNOWN;

    @Override
    public void setPower(int v) throws JmriException {
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // send message to turn on
            MarklinMessage l = MarklinMessage.getEnableMain();
            tc.sendMarklinMessage(l, this);
        } else if (v == OFF) {
            // send message to turn off
            MarklinMessage l = MarklinMessage.getKillMain();
            tc.sendMarklinMessage(l, this);
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
        tc.removeMarklinListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use MarklinPowerManager after dispose");
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

    // to listen for status changes from Marklin system
    @Override
    public void reply(MarklinReply m) {
        // power message?
        if (m.getPriority() == MarklinConstants.PRIO_1 && m.getCommand() == MarklinConstants.SYSCOMMANDSTART && m.getAddress() == 0x0000) {
            switch (m.getElement(9)) {
                case MarklinConstants.CMDGOSYS:
                    power = ON;
                    break;
                case MarklinConstants.CMDSTOPSYS:
                case MarklinConstants.CMDHALTSYS:
                    power = OFF;
                    break;
                default:
                    log.warn("Unknown sub command " + m.getElement(9));
            }
            firePropertyChange("Power", null, null);
        }
    }

    @Override
    public void message(MarklinMessage m) {
        // messages are ignored
    }

    private final static Logger log = LoggerFactory.getLogger(MarklinPowerManager.class);

}
