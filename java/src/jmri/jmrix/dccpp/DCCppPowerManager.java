/**
 * DCCppPowerManager.java
 *
 * PowerManager implementation for controlling layout power
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Mark Underwood Copyright (C) 2015
  *
 * Based on XNetPowerManager by Bob Jacobsen and Paul Bender
 */
package jmri.jmrix.dccpp;

import jmri.JmriException;
import jmri.managers.AbstractPowerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DCCppPowerManager extends AbstractPowerManager<DCCppSystemConnectionMemo> implements DCCppListener {

    DCCppTrafficController tc = null;

    public DCCppPowerManager(DCCppSystemConnectionMemo memo) {
        super(memo);
        // connect to the TrafficManager
        tc = memo.getDCCppTrafficController();
        tc.addDCCppListener(DCCppInterface.CS_INFO, this);
        // request the current command station status
        tc.sendDCCppMessage(DCCppMessage.makeCSStatusMsg(), this);
    }

    @Override
    public void setPower(int v) throws JmriException {
        int old = power;
        power = UNKNOWN;
        checkTC();
        if (v == ON) {
            // send TRACK_POWER_ON
            tc.sendDCCppMessage(DCCppMessage.makeTrackPowerOnMsg(), this);
        } else if (v == OFF) {
            // send TRACK_POWER_OFF
            tc.sendDCCppMessage(DCCppMessage.makeTrackPowerOffMsg(), this);
        }
        firePowerPropertyChange(old, power);
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
        tc.removeDCCppListener(DCCppInterface.CS_INFO, this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use PowerManager after dispose");
        }
    }

    // listen for power and status messages
    @Override
    public void message(DCCppReply m) {
        if (m.isPowerReply()) {
            log.debug("Power Reply message received: {}", m);
            int old = power;
            if (m.getPowerBool()) {
                power = ON;
            } else {
                power = OFF;
            }
            firePowerPropertyChange(old, power);
        // if status reply 's', then update the command station info (version, etc.)
        } else if (m.isStatusReply()) {
            log.debug("Version Info Received: {}", m);
            tc.getCommandStation().setCommandStationInfo(m);
        }
    }

    // listen for the messages to the CommandStation
    @Override
    public void message(DCCppMessage l) {
    }

    // Handle message timeout notification
    // If the message still has retries available, reduce retries and send it back to the traffic controller.
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        log.debug("Notified of timeout on message '{}' , {} retries available.", msg, msg.getRetries());
        if (msg.getRetries() > 0) {
            msg.setRetries(msg.getRetries() - 1);
            tc.sendDCCppMessage(msg, this);
        }        
    }

    // Initialize logging information
    private final static Logger log = LoggerFactory.getLogger(DCCppPowerManager.class);

}



