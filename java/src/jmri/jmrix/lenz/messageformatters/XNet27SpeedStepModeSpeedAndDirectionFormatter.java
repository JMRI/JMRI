package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XPressNet messages that set speed and direction in 27 speed step mode.
 *
 * @see XNetMessage
 * @see XPressNetMessageFormatter
 *
 * @author Paul Bender Copyright (C) 2024
 */

public class XNet27SpeedStepModeSpeedAndDirectionFormatter implements XPressNetMessageFormatter {

    private static final String FORWARD = "Forward";
    private static final String REVERSE = "Reverse";
    private static final String X_NET_MESSAGE_SET_SPEED = "XNetMessageSetSpeed";
    private static final String X_NET_MESSAGE_SET_DIRECTION = "XNetMessageSetDirection";
    private static final String SPEED_STEP_MODE_X = "SpeedStepModeX";

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                ((XNetMessage) m).getElement(0) == XNetConstants.LOCO_OPER_REQ &&
                        ((XNetMessage) m).getElement(1) == XNetConstants.LOCO_SPEED_27;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message not supported by this formatter");
        }
        int speed = (((m.getElement(4) & 0x10) >> 4) + ((m.getElement(4) & 0x0F) << 1));
        if (speed >= 3) {
            speed -= 3;
        }
        String direction;
        if ((m.getElement(4) & 0x80) != 0) {
            direction = " " + Bundle.getMessage(X_NET_MESSAGE_SET_DIRECTION, Bundle.getMessage(FORWARD));
        } else {
            direction = " " + Bundle.getMessage(X_NET_MESSAGE_SET_DIRECTION, Bundle.getMessage(REVERSE));
        }
        return "Mobile Decoder Operations Request: " + Bundle.getMessage(X_NET_MESSAGE_SET_SPEED,
                   LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)))
                + " " + speed +direction
                + " " + Bundle.getMessage(SPEED_STEP_MODE_X, 27) + ".";
    }

}
