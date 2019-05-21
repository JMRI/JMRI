package jmri.jmrix.srcp;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRCP implementation of the Turnout interface.
 * <p>
 * This object doesn't listen to the SRCP communications. This is because it
 * should be the only object that is sending messages for this turnout; more
 * than one Turnout object pointing to a single device is not allowed.
 *
 * Description:	extend jmri.AbstractTurnout for SRCP layouts
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @author	Paul Bender Copyright (C) 2014
 */
public class SRCPTurnout extends AbstractTurnout {

    /**
     * SRCP turnouts use the NMRA number (0-511) as their numerical
     * identification.
     *
     * @param number the turnout number
     * @param memo   the associated connection
     */
    public SRCPTurnout(int number, SRCPBusConnectionMemo memo) {
        super(memo.getSystemPrefix() + memo.getTurnoutManager().typeLetter() + number);
        _number = number;
        _bus = memo.getBus();

        // set the traffic controller
        tc = memo.getTrafficController();

        // send message requesting initilization
        String text = "INIT " + _bus + " GA " + _number + " N\n";

        // create and send the message itself
        tc.sendSRCPMessage(new SRCPMessage(text), null);

        // At construction, register for messages
    }

    public int getNumber() {
        return _number;
    }

    // Handle a request to change state by sending a formatted DCC packet
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        // sort out states
        if ((s & Turnout.CLOSED) != 0) {
            // first look for the double case, which we can't handle
            if ((s & Turnout.THROWN) != 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN " + s);
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
            log.debug("Send command to " + (_pushButtonLockout ? "Lock" : "Unlock") + " Pushbutton ET" + _number);
        }
    }

    // data members
    int _number;   // turnout number
    int _bus;   // bus number
    SRCPTrafficController tc = null;   // traffic controller 

    protected void sendMessage(boolean closed) {
        // get the message text
        String text;
        if (closed) {
            text = "SET " + _bus + " GA " + _number + " 0 0 -1\n";
        } else // thrown
        {
            text = "SET " + _bus + " GA " + _number + " 0 1 -1\n";
        }

        // create and send the message itself
        tc.sendSRCPMessage(new SRCPMessage(text), null);

    }

    private final static Logger log = LoggerFactory.getLogger(SRCPTurnout.class);

}
