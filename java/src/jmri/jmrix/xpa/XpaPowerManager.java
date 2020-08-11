package jmri.jmrix.xpa;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.managers.AbstractPowerManager;

/**
 * PowerManager implementation for controlling layout power from an XPA+modem
 * connected to an XpressNet based system.
 *
 * @author Paul Bender Copyright (C) 2004
  *
 */
public class XpaPowerManager extends AbstractPowerManager<XpaSystemConnectionMemo> implements XpaListener {

    XpaTrafficController tc;
    boolean waiting = false;
    int onReply = UNKNOWN;

    public XpaPowerManager(XpaSystemConnectionMemo memo) {
        super(memo);
        // connect to the TrafficManager
        tc = memo.getXpaTrafficController();
        tc.addXpaListener(this);
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
        } else if (v == OFF) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.OFF;
        }
        // send "Emergency Off/Emergency Stop"
        XpaMessage l = XpaMessage.getEStopMsg();
        tc.sendXpaMessage(l, this);
        firePowerPropertyChange(old, power);
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        tc.removeXpaListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use XpaPowerManager after dispose");
        }
    }

    // to listen for status changes from Xpa system
    @Override
    public void reply(XpaMessage m) {
        if (waiting) {
            int old = power;
            power = onReply;
            firePowerPropertyChange(old, power);
        }
        waiting = false;
    }

    @Override
    public void message(XpaMessage m) {
    }

}
