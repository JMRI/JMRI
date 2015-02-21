// SprogPowerManager.java
package jmri.jmrix.sprog;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrix.AbstractMessage;

/**
 * PowerManager implementation for controlling SPROG layout power.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public class SprogPowerManager extends jmri.managers.AbstractPowerManager
        implements PowerManager, SprogListener {

    SprogTrafficController trafficController = null;

    public SprogPowerManager(SprogSystemConnectionMemo memo) {
        super(memo);
        // connect to the TrafficManager
        trafficController = SprogTrafficController.instance();
        trafficController.addSprogListener(this);
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
            // send "Enable main track"
            SprogMessage l = SprogMessage.getEnableMain();
            trafficController.sendSprogMessage(l, this);
        } else if (v == OFF) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.OFF;
            firePropertyChange("Power", null, null);
            // send "Kill main track"
            SprogMessage l = SprogMessage.getKillMain();
            for (int i = 0; i < 3; i++) {
                trafficController.sendSprogMessage(l, this);
            }
        }
        firePropertyChange("Power", null, null);
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
        trafficController.removeSprogListener(this);
        trafficController = null;
    }

    private void checkTC() throws JmriException {
        if (trafficController == null) {
            throw new JmriException("attempt to use SprogPowerManager after dispose");
        }
    }

    // to listen for status changes from Sprog system
    public void notifyReply(SprogReply m) {
        if (waiting) {
            power = onReply;
            firePropertyChange("Power", null, null);
        }
        waiting = false;
    }

    public void notifyMessage(SprogMessage m) {
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

    public void notify(AbstractMessage m) {
        if (m instanceof SprogMessage) {
            this.notifyMessage((SprogMessage) m);
        } else {
            this.notifyReply((SprogReply) m);
        }

    }

}

/* @(#)SprogPowerManager.java */
