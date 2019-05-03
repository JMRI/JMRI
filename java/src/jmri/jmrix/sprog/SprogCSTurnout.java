package jmri.jmrix.sprog;

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
public class SprogCSTurnout extends AbstractTurnout {

    private SprogSystemConnectionMemo _memo = null;

    /**
     * Create SPROG Turnout object.
     * <p>
     * Sprog turnouts use the NMRA number (0-511) as their numerical
     * identification.
     *
     * @param number address for turnout
     * @param memo connection to get prefix from
     */
    public SprogCSTurnout(int number, SprogSystemConnectionMemo memo) {
        super(memo.getSystemPrefix() + "T" + number);
        _number = number;
        _memo = memo;
        commandStation = _memo.getCommandStation();
    }

    public int getNumber() {
        return _number;
    }

    /**
     * Handle a request to change state by sending a formatted DCC packet.
     *
     * @param s commanded state to set
     */
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
                commandStation.forwardCommandChangeToLayout(_number, true ^ getInverted());
            }
        } else {
            // send a THROWN command
            commandStation.forwardCommandChangeToLayout(_number, false ^ getInverted());
        }
    }

    private SprogCommandStation commandStation;

    public void setCommandStation(SprogCommandStation command) {
        commandStation = command;
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

    @Override
    public boolean canInvert() {
        return true;
    }

    int _number; // turnout number

    private final static Logger log = LoggerFactory.getLogger(SprogCSTurnout.class);

}
