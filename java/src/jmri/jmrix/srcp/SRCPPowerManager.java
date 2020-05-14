package jmri.jmrix.srcp;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.managers.AbstractPowerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling layout power
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class SRCPPowerManager extends AbstractPowerManager<SRCPBusConnectionMemo> implements SRCPListener {

    boolean waiting = false;
    int onReply = UNKNOWN;
    int _bus = 0;
    SRCPTrafficController tc = null;

    public SRCPPowerManager(SRCPBusConnectionMemo memo, int bus) {
        super(memo);
        // connect to the TrafficManager
        tc = memo.getTrafficController();
        tc.addSRCPListener(this);
        _bus = bus;
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
            SRCPMessage l = SRCPMessage.getEnableMain();
            tc.sendSRCPMessage(l, this);
        } else if (v == OFF) {
            // configure to wait for reply
            waiting = true;
            onReply = PowerManager.OFF;
            firePowerPropertyChange(old, power);
            // send "Kill main track"
            SRCPMessage l = SRCPMessage.getKillMain();
            tc.sendSRCPMessage(l, this);
        }
        firePowerPropertyChange(old, power);
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

    // to listen for status changes from SRCP system
    @Override
    public void reply(SRCPReply m) {
        if (waiting) {
            int old = power;
            power = onReply;
            firePowerPropertyChange(old, power);
        }
        waiting = false;
    }

    // to listen for status changes from SRCP system
    @Override
    public void reply(jmri.jmrix.srcp.parser.SimpleNode n) {
        log.debug("reply called with simpleNode {}", n.jjtGetValue());
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



