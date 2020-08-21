package jmri.jmrix.lenz;

import jmri.JmriException;
import jmri.managers.AbstractPowerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling layout power.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Paul Bender Copyright (C) 2003-2010
 */
public class XNetPowerManager extends AbstractPowerManager<XNetSystemConnectionMemo> implements XNetListener {

    XNetTrafficController tc;

    public XNetPowerManager(XNetSystemConnectionMemo memo) {
        super(memo);
        // connect to the TrafficManager
        tc = memo.getXNetTrafficController();
        tc.addXNetListener(XNetInterface.CS_INFO, this);
        // request the current command station status
        tc.sendXNetMessage(XNetMessage.getCSStatusRequestMessage(), this);
    }

    @Override
    public boolean implementsIdle() {
        // XPressNet implements idle via the broadcast emergency stop commands. 
        return true;
    }

    @Override
    public void setPower(int v) throws JmriException {
        int old = power;
        power = UNKNOWN;
        checkTC();
        switch (v) {
            case ON:
                // send RESUME_OPS
                tc.sendXNetMessage(XNetMessage.getResumeOperationsMsg(), this);
                break;
            case OFF:
                // send EMERGENCY_OFF
                tc.sendXNetMessage(XNetMessage.getEmergencyOffMsg(), this);
                break;
            case IDLE:
                // send EMERGENCY_STOP
                tc.sendXNetMessage(XNetMessage.getEmergencyStopMsg(), this);
                break;
            default:
                break;
        }
        firePowerPropertyChange(old, power);
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        tc.removeXNetListener(XNetInterface.CS_INFO, this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use PowerManager after dispose");
        }
    }

    // to listen for Broadcast messages related to track power.
    // There are 5 messages to listen for
    @Override
    public void message(XNetReply m) {
        int old = power;
        log.debug("Message received: {}", m);
        if (m.getElement(0) == XNetConstants.CS_INFO
                && m.getElement(1) == XNetConstants.BC_NORMAL_OPERATIONS) {
            // First, we check for a "normal operations resumed message"
            // This indicates the power to the track is ON
            power = ON;
        } else if (m.getElement(0) == XNetConstants.CS_INFO
                && m.getElement(1) == XNetConstants.BC_EVERYTHING_OFF) {
            // Next, we check for a Track Power Off message
            // This indicates the power to the track is OFF
            power = OFF;
        } else if (m.getElement(0) == XNetConstants.BC_EMERGENCY_STOP
                && m.getElement(1) == XNetConstants.BC_EVERYTHING_OFF) {
            // Then, we check for an "Emergency Stop" message
            // This indicates the track power is ON, but all 
            // locomotives are stopped
            power = IDLE;
        } else if (m.getElement(0) == XNetConstants.CS_INFO
                && m.getElement(1) == XNetConstants.BC_SERVICE_MODE_ENTRY) {
            // Next we check for a "Service Mode Entry" message
            // This indicatse track power is off on the mainline.
            power = OFF;
        } else if (m.getElement(0) == XNetConstants.CS_REQUEST_RESPONSE
                && m.getElement(1) == XNetConstants.CS_STATUS_RESPONSE) {
            // Finally, we look at for the response to a Command 
            // Station Status Request
            int statusByte = m.getElement(2);
            if ((statusByte & 0x01) == 0x01) {
                // Command station is in Emergency Off Mode
                power = OFF;
            } else if ((statusByte & 0x02) == 0x02) {
                // Command station is in Emergency Stop Mode
                power = IDLE;
            } else if ((statusByte & 0x08) == 0x08) {
                // Command station is in Service Mode, power to the 
                // track is off
                power = OFF;
            } else if ((statusByte & 0x40) == 0x40) {
                // Command station is in Power Up Mode, and not yet on
                power = OFF;
            } else {
                power = ON;
            }
        }
        firePowerPropertyChange(old, power);
    }

    /**
     * Listen for the messages to the LI100/LI101.
     * 
     * @param l the message
     */
    @Override
    public void message(XNetMessage l) {
    }

    /**
     * Handle a timeout notification.
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        log.debug("Notified of timeout on message{}", msg);
    }

    // Initialize logging information
    private static final Logger log = LoggerFactory.getLogger(XNetPowerManager.class);

}
