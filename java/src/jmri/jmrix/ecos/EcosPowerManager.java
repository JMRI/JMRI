// EcosPowerManager.java
package jmri.jmrix.ecos;

import jmri.JmriException;
import jmri.PowerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling layout power.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision$
 */
public class EcosPowerManager implements PowerManager, EcosListener {

    public EcosPowerManager(EcosTrafficController etc) {
        // connect to the TrafficManager
        tc = etc;
        tc.addEcosListener(this);

        // ask to be notified
        EcosMessage m = new EcosMessage("request(1, view)");
        tc.sendEcosMessage(m, this);

        // get initial state
        m = new EcosMessage("get(1, status)");
        tc.sendEcosMessage(m, this);

    }

    EcosTrafficController tc;

    public String getUserName() {
        return "Ecos";
    }

    int power = UNKNOWN;

    public void setPower(int v) throws JmriException {
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // send message to turn on
            EcosMessage l = new EcosMessage("set(1, go)");
            tc.sendEcosMessage(l, this);
        } else if (v == OFF) {
            // send message to turn off
            EcosMessage l = new EcosMessage("set(1, stop)");
            tc.sendEcosMessage(l, this);
        }
        firePropertyChange("Power", null, null);
    }

    public int getPower() {
        return power;
    }

    // to free resources when no longer used
    public void dispose() throws JmriException {
        tc.removeEcosListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use EcosPowerManager after dispose");
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

    // to listen for status changes from Ecos system
    public void reply(EcosReply m) {
        // power message?
        String msg = m.toString();
        if (msg.contains("<EVENT 1>") || msg.contains("REPLY get(1,") || msg.contains("REPLY set(1,")) {
            if (msg.contains("status[GO]") || msg.contains("et(1, go)")) {
                log.debug("POWER ON DETECTED");
                power = ON;
                firePropertyChange("Power", null, null);
            } else if (msg.contains("status[STOP]") || msg.contains("et(1, stop)")) {
                log.debug("POWER OFF DETECTED");
                power = OFF;
                firePropertyChange("Power", null, null);
            }
        }
    }

    public void message(EcosMessage m) {
        // messages are ignored
    }

    private final static Logger log = LoggerFactory.getLogger(EcosPowerManager.class.getName());
}


/* @(#)EcosPowerManager.java */
