package jmri.jmrix.jmriclient;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.managers.AbstractPowerManager;

/**
 * PowerManager implementation for controlling layout power
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Paul Bender Copyright (C) 2010
 */
public class JMRIClientPowerManager extends AbstractPowerManager<JMRIClientSystemConnectionMemo> implements JMRIClientListener {

    JMRIClientTrafficController tc = null;

    public JMRIClientPowerManager(JMRIClientSystemConnectionMemo memo) {
        super(memo);
        // connect to the TrafficManager
        tc = memo.getJMRIClientTrafficController();
        tc.addJMRIClientListener(this);
    }

    @Override
    public void setPower(int v) throws JmriException {
        int old = power;
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // send "Enable main track"
            JMRIClientMessage l = JMRIClientMessage.getEnableMain();
            tc.sendJMRIClientMessage(l, this);
        } else if (v == OFF) {
            // send "Kill main track"
            JMRIClientMessage l = JMRIClientMessage.getKillMain();
            tc.sendJMRIClientMessage(l, this);
        }
        firePowerPropertyChange(old, power);
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
        tc.removeJMRIClientListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use JMRIClientPowerManager after dispose");
        }
    }

    // to listen for status changes from JMRIClient system
    @Override
    public void reply(JMRIClientReply m) {
        int old = power;
        if (m.toString().contains("ON")) {
            power = PowerManager.ON;
        } else {
            power = PowerManager.OFF;
        }
        firePowerPropertyChange(old, power);
    }

    @Override
    public void message(JMRIClientMessage m) {
    }

}
