package jmri.jmrix.tmcc;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractTurnout for TMCC serial layouts.
 *
 * This object doesn't listen to the TMCC communications. This is because it
 * should be the only object that is sending messages for this turnout; more
 * than one Turnout object pointing to a single device is not allowed.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006
  */
public class SerialTurnout extends AbstractTurnout {

    // data members
    int _number; // turnout number
    private SerialTrafficController tc = null;
    protected String _prefix = "T"; // default to "T"

    /**
     * Create a turnout. TMCC turnouts use the NMRA number (0-511) as their
     * numerical identification.
     *
     * @param number the NMRA turnout number from 0 to 511
     */
    public SerialTurnout(String prefix, int number, TmccSystemConnectionMemo memo) {
        super(prefix + "T" + number);
        tc = memo.getTrafficController();
        _number = number;
        _prefix = prefix;
        // At construction, don't register for messages (see package doc)
    }

    /**
     * Handle a request to change state by sending a turnout command.
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
            log.debug("Send command to {} Pushbutton {}T{}", (_pushButtonLockout ? "Lock" : "Unlock"), _prefix, _number);
        }
    }

    protected void sendMessage(boolean closed) {
        SerialMessage m = new SerialMessage();
        m.setOpCode(0xFE);
        if (closed) {
            m.putAsWord(0x4000 + _number * 128);
        } else {
            m.putAsWord(0x401F + _number * 128);
        }
        tc.sendSerialMessage(m, null);
        tc.sendSerialMessage(m, null);
        tc.sendSerialMessage(m, null);
        tc.sendSerialMessage(m, null);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnout.class);

}
