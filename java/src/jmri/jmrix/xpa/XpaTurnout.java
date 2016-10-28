package jmri.jmrix.xpa;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xpa+Modem implementation of the Turnout interface.
 * <P>
 *
 * @author	Paul Bender Copyright (C) 2004
 */
public class XpaTurnout extends AbstractTurnout {

    /**
     *
     */
    // Private data member to keep track of what turnout we control.
    private int _number;
    private XpaTrafficController tc = null;

    /**
     * Xpa turnouts use any addres allowed as an accessory decoder address on
     * the particular command station.
     */
    public XpaTurnout(int number,XpaSystemConnectionMemo m) {
        super(m.getSystemPrefix() + "T" + number);
        _number = number;
        tc = m.getXpaTrafficController();
    }

    public int getNumber() {
        return _number;
    }

    // Handle a request to change state by sending a formatted DCC packet
    protected void forwardCommandChangeToLayout(int s) {
        XpaMessage m = null;
        // sort out states
        if ((s & Turnout.CLOSED) != 0) {
            // first look for the double case, which we can't handle
            if ((s & Turnout.THROWN) != 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN " + s);
                return;
            } else {
                // send a CLOSED command
                m = XpaMessage.getSwitchNormalMsg(_number);
            }
        } else {
            // send a THROWN command
            m = XpaMessage.getSwitchReverseMsg(_number);
        }
        tc.sendXpaMessage(m, null);
    }

    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout) {
        if (log.isDebugEnabled()) {
            log.debug("Send command to " + (_pushButtonLockout ? "Lock" : "Unlock") + " Pushbutton PT" + _number);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(XpaTurnout.class.getName());

}

/* @(#)XpaTurnout.java */
