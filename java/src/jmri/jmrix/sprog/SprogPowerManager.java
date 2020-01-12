package jmri.jmrix.sprog;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrix.AbstractMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling SPROG layout power.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SprogPowerManager extends jmri.managers.AbstractPowerManager
        implements SprogListener {

    SprogTrafficController trafficController = null;

    public SprogPowerManager(SprogSystemConnectionMemo memo) {
        super(memo);
        // connect to the TrafficManager
        trafficController = memo.getSprogTrafficController();
        trafficController.addSprogListener(this);
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
            log.debug("setPower ON");
            waiting = true;
            onReply = PowerManager.ON;
            // send "Enable main track"
            SprogMessage l = SprogMessage.getEnableMain();
            trafficController.sendSprogMessage(l, this);
        } else if (v == OFF) {
            // configure to wait for reply
            log.debug("setPower OFF");
            waiting = true;
            onReply = PowerManager.OFF;
//            firePropertyChange("Power", null, null);
            // send "Kill main track"
            SprogMessage l = SprogMessage.getKillMain();
            for (int i = 0; i < 3; i++) {
                trafficController.sendSprogMessage(l, this);
            }
        }
        firePropertyChange("Power", null, null);
    }

    /**
     * Update power state after service mode programming operation
     * without sending a message to the SPROG.
     */
    public void notePowerState(int v) {
        power = v;
        firePropertyChange("Power", null, null);
    }

    @Override
    public int getPower() {
        return power;
    }

    /**
     * Free resources when no longer used.
     */
    @Override
    public void dispose() throws JmriException {
        trafficController.removeSprogListener(this);
        trafficController = null;
    }

    private void checkTC() throws JmriException {
        if (trafficController == null) {
            throw new JmriException("attempt to use SprogPowerManager after dispose");
        }
    }

    /**
     * Listen for status changes from Sprog system.
     */
    @Override
    public void notifyReply(SprogReply m) {
        if (waiting) {
            log.debug("Reply while waiting");
            power = onReply;
            firePropertyChange("Power", null, null);
        }
        waiting = false;
    }

    @Override
    public void notifyMessage(SprogMessage m) {
        if (m.isKillMain()) {
            // configure to wait for reply
            log.debug("Seen Kill Main");
            waiting = true;
            onReply = PowerManager.OFF;
        } else if (m.isEnableMain()) {
            // configure to wait for reply
            log.debug("Seen Enable Main");
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


    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SprogPowerManager.class);
}
