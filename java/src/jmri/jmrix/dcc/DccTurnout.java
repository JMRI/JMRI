package jmri.jmrix.dcc;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.NmraPacket;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dcc-only implementation of the Turnout interface.
 * <p>
 * This object can't listen to the DCC communications. This is because it should
 * be the only object that is sending messages for this turnout; more than one
 * Turnout object pointing to a single device is not allowed.
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
public class DccTurnout extends AbstractTurnout {

    /**
     * DCC turnouts use the NMRA number (0-511) as their numerical
     * identification.
     */
    public DccTurnout(int number) {
        super("BT" + number); // default prefix "B"
        _number = number;
        // At construction, register for messages
    }

    public int getNumber() {
        return _number;
    }

    // Turnouts do support inversion
    @Override
    public boolean canInvert() {
        return true;
    }

    // Handle a request to change state by sending a formatted DCC packet
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        // sort out states
        if ((s & Turnout.CLOSED) != 0) {
            // first look for the double case, which we can't handle
            if ((s & Turnout.THROWN) != 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN {}", s);
                return;
            } else {
                // send a CLOSED command
                sendMessage(true ^ getInverted());
            }
        } else {
            // send a THROWN command
            sendMessage(false ^ getInverted());
        }
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout) {
        if (log.isDebugEnabled()) {
            log.debug("Send command to {} Pushbutton BT{}", (_pushButtonLockout ? "Lock" : "Unlock"), _number);
        }
    }

    // data members
    int _number;   // turnout number

    protected void sendMessage(boolean closed) {
        // get the packet
        byte[] bl = NmraPacket.accDecoderPkt(_number, closed);
        if (log.isDebugEnabled()) {
            log.debug("packet: "
                    + Integer.toHexString(0xFF & bl[0])
                    + " " + Integer.toHexString(0xFF & bl[1])
                    + " " + Integer.toHexString(0xFF & bl[2]));
        }

        InstanceManager.getDefault(CommandStation.class).sendPacket(bl, 1);
    }

    private final static Logger log = LoggerFactory.getLogger(DccTurnout.class);

}
