package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XPressNet messages that set speed and direction in 128 speed step mode.
 *
 * @see jmri.jmrix.lenz.XNetMessage
 * @see jmri.jmrix.lenz.XPressNetMessageFormatter
 *
 * @author Paul Bender Copyright (C) 2024
 */

public class XNet128SpeedStepModeSpeedAndDirectionFormatter implements XPressNetMessageFormatter {

    private static final String FORWARD = "Forward";
    private static final String REVERSE = "Reverse";
    private static final String X_NET_MESSAGE_SET_SPEED = "XNetMessageSetSpeed";
    private static final String X_NET_MESSAGE_SET_DIRECTION = "XNetMessageSetDirection";
    private static final String SPEED_STEP_MODE_X = "SpeedStepModeX";

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                ((XNetMessage) m).getElement(0) == XNetConstants.LOCO_OPER_REQ &&
                        ((XNetMessage) m).getElement(1) == XNetConstants.LOCO_SPEED_128;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message not supported by this formatter");
        }
        String direction;
        if ((m.getElement(4) & 0x80) != 0) {
            direction = Bundle.getMessage(FORWARD);
        } else {
            direction = Bundle.getMessage(REVERSE);
        }
        return "Mobile Decoder Operations Request: " +
                Bundle.getMessage(X_NET_MESSAGE_SET_SPEED,
                    LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)))
                + " "
                + (m.getElement(4) & 0x7F) + " " + Bundle.getMessage(X_NET_MESSAGE_SET_DIRECTION, direction) +
                " " + Bundle.getMessage(SPEED_STEP_MODE_X, 128) + ".";
    }

}
