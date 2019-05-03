package jmri.jmrix.srcp;

import jmri.JmriException;
import jmri.PowerManager;

import java.beans.PropertyChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling layout power
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 */
public class SRCPPowerManager implements PowerManager, SRCPListener {

    int _bus = 0;
    SRCPBusConnectionMemo _memo;

    public SRCPPowerManager(SRCPBusConnectionMemo memo, int bus) {
        // connect to the TrafficManager
        _memo = memo;
        tc = memo.getTrafficController();
        tc.addSRCPListener(this);
        _bus = bus;
    }

    @Override
    public String getUserName() {
        return _memo.getUserName();
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
            SRCPMessage l = SRCPMessage.getEnableMain();
            tc.sendSRCPMessage(l, this);
        } else if (v == OFF) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.OFF;
            firePropertyChange("Power", null, null);
            // send "Kill main track"
            SRCPMessage l = SRCPMessage.getKillMain();
            tc.sendSRCPMessage(l, this);
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
        tc.removeSRCPListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use SRCPPowerManager after dispose");
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

    SRCPTrafficController tc = null;

    // to listen for status changes from SRCP system
    @Override
    public void reply(SRCPReply m) {
        if (waiting) {
            power = onReply;
            firePropertyChange("Power", null, null);
        }
        waiting = false;
    }

    // to listen for status changes from SRCP system
    @Override
    public void reply(jmri.jmrix.srcp.parser.SimpleNode n) {
        if (log.isDebugEnabled()) {
            log.debug("reply called with simpleNode " + n.jjtGetValue());
        }
        reply(new SRCPReply(n));
    }

    @Override
    public void message(SRCPMessage m) {
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

    private final static Logger log = LoggerFactory.getLogger(SRCPPowerManager.class);

}



