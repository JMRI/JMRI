package jmri.jmrix.easydcc;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.managers.AbstractPowerManager;

/**
 * PowerManager implementation for controlling layout power
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class EasyDccPowerManager extends AbstractPowerManager<EasyDccSystemConnectionMemo> implements EasyDccListener {

    private EasyDccTrafficController trafficController = null;
    boolean waiting = false;
    int onReply = UNKNOWN;

    public EasyDccPowerManager(EasyDccSystemConnectionMemo memo) {
        super(memo);
        // connect to the TrafficManager
        trafficController = memo.getTrafficController();
        trafficController.addEasyDccListener(this);
    }

    @Override
    public void setPower(int v) throws JmriException {
        int old = power;
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.ON;
            // send "Enable main track"
            EasyDccMessage l = EasyDccMessage.getEnableMain();
            trafficController.sendEasyDccMessage(l, this);
        } else if (v == OFF) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.OFF;
            // send "Kill main track"
            EasyDccMessage l = EasyDccMessage.getKillMain();
            trafficController.sendEasyDccMessage(l, this);
        }
        firePowerPropertyChange(old, power);
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
        trafficController.removeEasyDccListener(this);
        trafficController = null;
    }

    private void checkTC() throws JmriException {
        if (trafficController == null) {
            throw new JmriException("attempt to use EasyDccPowerManager after dispose");
        }
    }

    // to listen for status changes from EasyDcc system
    @Override
    public void reply(EasyDccReply m) {
        int old = power;
        if (waiting) {
            power = onReply;
            firePowerPropertyChange(old, power); // NOI18N
        }
        waiting = false;
    }

    @Override
    public void message(EasyDccMessage m) {
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

}
