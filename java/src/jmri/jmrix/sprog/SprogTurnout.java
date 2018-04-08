package jmri.jmrix.sprog;

import jmri.NmraPacket;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sprog implementation of the Turnout interface.
 * <p>
 * This object doesn't listen to the Sprog communications. This is because it
 * should be the only object that is sending messages for this turnout; more
 * than one Turnout object pointing to a single device is not allowed.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2005
 * @author J.M. (Mark) Knox Copyright (C) 2005
 */
public class SprogTurnout extends AbstractTurnout {

    private SprogSystemConnectionMemo _memo = null;

    /**
     * Create a SPROG Turnout object.
     * <p>
     * Sprog turnouts use the NMRA number (0-511) as their numerical
     * identification.
     */
    public SprogTurnout(int number, SprogSystemConnectionMemo memo) {
        super(memo.getSystemPrefix() + "T" + number);
        _number = number;
        _memo = memo;
    }

    public int getNumber() {
        return _number;
    }

    /**
     * Handle a request to change state by sending a formatted DCC packet.
     */
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
            log.debug("Send command to {} Pushbutton {}T{}",
                    (_pushButtonLockout ? "Lock" : "Unlock"),
                    _memo.getSystemPrefix(),
                    _number);
        }
    }

    // data members
    int _number; // turnout number

    protected void sendMessage(boolean closed) {
        // get the packet
        byte[] bl = NmraPacket.accDecoderPkt(_number, closed);
        if (log.isDebugEnabled()) {
            log.debug("packet: "
                    + Integer.toHexString(0xFF & bl[0])
                    + " " + Integer.toHexString(0xFF & bl[1])
                    + " " + Integer.toHexString(0xFF & bl[2]));
        }

        SprogMessage m = new SprogMessage(10);
        int i = 0; // counter to make it easier to format the message
        m.setElement(i++, 'O');  // "S02 " means send it twice
        m.setElement(i++, ' ');
        // m.setElement(i++, '2'); // not required?
        String s = Integer.toHexString(bl[0] & 0xFF).toUpperCase();
        if (s.length() == 1) {
            m.setElement(i++, '0');
            m.setElement(i++, s.charAt(0));
        } else {
            m.setElement(i++, s.charAt(0));
            m.setElement(i++, s.charAt(1));
        }
        m.setElement(i++, ' ');
        s = Integer.toHexString(bl[1] & 0xFF).toUpperCase();
        if (s.length() == 1) {
            m.setElement(i++, '0');
            m.setElement(i++, s.charAt(0));
        } else {
            m.setElement(i++, s.charAt(0));
            m.setElement(i++, s.charAt(1));
        }
        m.setElement(i++, ' ');
        s = Integer.toHexString(bl[2] & 0xFF).toUpperCase();
        if (s.length() == 1) {
            m.setElement(i++, '0');
            m.setElement(i++, s.charAt(0));
        } else {
            m.setElement(i++, s.charAt(0));
            m.setElement(i++, s.charAt(1));
        }

        _memo.getSprogTrafficController().sendSprogMessage(m, null);
    }

    @Override
    public boolean canInvert() {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(SprogTurnout.class);

}
