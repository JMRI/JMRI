package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.managers.AbstractPowerManager;

/**
 * PowerManager implementation for controlling layout power.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class NcePowerManager extends AbstractPowerManager<NceSystemConnectionMemo> implements NceListener {

    public NcePowerManager(NceSystemConnectionMemo memo) {
        this(memo.getNceTrafficController(), memo.getSystemPrefix());// connect to the TrafficManager
    }

    public NcePowerManager(NceTrafficController tc, String p) {
        super(tc.getAdapterMemo());
        // connect to the TrafficManager
        this.tc = tc;
        tc.addNceListener(this);
    }

    boolean waiting = false;
    int onReply = UNKNOWN;

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
            NceMessage l = NceMessage.getEnableMain(tc);
            tc.sendNceMessage(l, this);
        } else if (v == OFF) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.OFF;
            firePowerPropertyChange(old, power);
            // send "Kill main track"
            NceMessage l = NceMessage.getKillMain(tc);
            tc.sendNceMessage(l, this);
        }
        firePowerPropertyChange(old, power);
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
        tc.removeNceListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use NcePowerManager after dispose");
        }
    }

    NceTrafficController tc = null;

    // to listen for status changes from NCE system
    @Override
    public void reply(NceReply m) {
        if (waiting) {
            int old = power;
            power = onReply;
            firePowerPropertyChange(old, power);
        }
        waiting = false;
    }

    @Override
    public void message(NceMessage m) {
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



