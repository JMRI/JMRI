package jmri.jmrix.xpa;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xpa+Modem implementation of the Turnout interface.
 * <p>
 * Based on XNetTurnout.java
 *
 * @author	Paul Bender Copyright (C) 2004
 */
public class XpaTurnout extends AbstractTurnout {

    // Private data member to keep track of what turnout we control
    private final int _number;
    private XpaTrafficController tc = null;

    /**
     * Xpa turnouts use any address allowed as an accessory decoder address on
     * the particular command station.
     *
     * @param number turnout number
     * @param m      connection turnout is associated with
     */
    public XpaTurnout(int number, XpaSystemConnectionMemo m) {
        super(m.getSystemPrefix() + "T" + number);
        _number = number;
        tc = m.getXpaTrafficController();
    }

    public int getNumber() {
        return _number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized protected void forwardCommandChangeToLayout(int newState) {
        XpaMessage m;
        // sort out states
        if ((newState & Turnout.CLOSED) != 0) {
            if (statesOk(newState)) {
                // send a CLOSED command (or THROWN if inverted)
                m = XpaMessage.getSwitchMsg(_number, getInverted());
            } else {
                log.error("Cannot command both CLOSED and THROWN {}", newState);
                return;
            }
        } else {
            // send a THROWN command (or CLOSED if inverted)
            m = XpaMessage.getSwitchMsg(_number, !getInverted());
        }
        tc.sendXpaMessage(m, null);
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout) {
        log.debug("Send command to {} Pushbutton {}", (_pushButtonLockout ? "Lock" : "Unlock"), getSystemName());
    }

    @Override
    public boolean canInvert() {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(XpaTurnout.class);

}
