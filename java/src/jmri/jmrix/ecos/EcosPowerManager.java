package jmri.jmrix.ecos;

import jmri.JmriException;
import jmri.managers.AbstractPowerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling ECoS layout power.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class EcosPowerManager extends AbstractPowerManager<EcosSystemConnectionMemo> implements EcosListener {

    private EcosTrafficController tc;

    public EcosPowerManager(EcosTrafficController etc) {
        super(etc.adaptermemo);
        // connect to the TrafficManager
        tc = etc;
        init();
    }

    private void init() {
        tc.addEcosListener(this);

        // ask to be notified
        EcosMessage m = new EcosMessage("request(1, view)");
        tc.sendEcosMessage(m, this);

        // get initial state
        m = new EcosMessage("get(1, status)");
        tc.sendEcosMessage(m, this);

    }

    @Override
    public void setPower(int v) throws JmriException {
        int old = power;
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
        firePowerPropertyChange(old, power);
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
        tc.removeEcosListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use EcosPowerManager after dispose");
        }
    }

    // to listen for status changes from Ecos system
    @Override
    public void reply(EcosReply m) {
        // power message?
        String msg = m.toString();
        if (msg.contains("<EVENT 1>") || msg.contains("REPLY get(1,") || msg.contains("REPLY set(1,")) {
            int old = power;
            if (msg.contains("status[GO]") || msg.contains("et(1, go)")) {
                log.debug("POWER ON DETECTED");
                power = ON;
            } else if (msg.contains("status[STOP]") || msg.contains("et(1, stop)")) {
                log.debug("POWER OFF DETECTED");
                power = OFF;
            }
            firePowerPropertyChange(old, power);
        }
    }

    @Override
    public void message(EcosMessage m) {
        // messages are ignored
    }

    private final static Logger log = LoggerFactory.getLogger(EcosPowerManager.class);

}
