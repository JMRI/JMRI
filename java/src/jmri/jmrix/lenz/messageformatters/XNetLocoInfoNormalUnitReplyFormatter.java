package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * XPressNet message formatter for Loco Info Normal Unit Reply.
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetLocoInfoNormalUnitReplyFormatter implements XPressNetMessageFormatter {

    @Override
    public Boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                ((XNetReply) m).getElement(0) == XNetConstants.LOCO_INFO_NORMAL_UNIT
                && ((XNetReply) m).getElement(1) != XNetConstants.LOCO_FUNCTION_STATUS_HIGH;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message is not supported");
        }
        XNetReply r = (XNetReply) m;
        // message byte 4, contains F0,F1,F2,F3,F4
        int element3 = r.getElement(3);
        // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
        int element4 = r.getElement(4);
        return Bundle.getMessage("XNetReplyLocoNormalLabel") + ","+
                parseSpeedAndDirection(r.getElement(1), r.getElement(2)) + " " +
                parseFunctionStatus(element3, element4);
    }

    private static final String POWER_STATE_ON = "PowerStateOn";
    private static final String POWER_STATE_OFF = "PowerStateOff";
    private static final String SPEED_STEP_MODE_X = "SpeedStepModeX";

    /**
     * Parse the speed step and the direction information for a locomotive.
     *
     * @param element1 contains the speed step mode designation and
     * availability information
     * @param element2 contains the data byte including the step mode and
     * availability information
     * @return readable version of message
     */
    protected String parseSpeedAndDirection(int element1, int element2) {
        String text = "";
        int speedVal;
        if ((element2 & 0x80) == 0x80) {
            text += Bundle.getMessage("Forward") + ",";
        } else {
            text += Bundle.getMessage("Reverse") + ",";
        }

        if ((element1 & 0x04) == 0x04) {
            // We're in 128 speed step mode
            speedVal = element2 & 0x7f;
            // The first speed step used is actually at 2 for 128
            // speed step mode.
            if (speedVal >= 1) {
                speedVal -= 1;
            } else {
                speedVal = 0;
            }
            text += Bundle.getMessage(SPEED_STEP_MODE_X, 128) + ",";
        } else if ((element1 & 0x02) == 0x02) {
            // We're in 28 speed step mode
            // We have to re-arange the bits, since bit 4 is the LSB,
            // but other bits are in order from 0-3
            speedVal = ((element2 & 0x0F) << 1) + ((element2 & 0x10) >> 4);
            // The first speed step used is actually at 4 for 28
            // speed step mode.
            if (speedVal >= 3) {
                speedVal -= 3;
            } else {
                speedVal = 0;
            }
            text += Bundle.getMessage(SPEED_STEP_MODE_X, 28) + ",";
        } else if ((element1 & 0x01) == 0x01) {
            // We're in 27 speed step mode
            // We have to re-arange the bits, since bit 4 is the LSB,
            // but other bits are in order from 0-3
            speedVal = ((element2 & 0x0F) << 1) + ((element2 & 0x10) >> 4);
            // The first speed step used is actually at 4 for 27
            // speed step mode.
            if (speedVal >= 3) {
                speedVal -= 3;
            } else {
                speedVal = 0;
            }
            text += Bundle.getMessage(SPEED_STEP_MODE_X, 27) + ",";
        } else {
            // Assume we're in 14 speed step mode.
            speedVal = (element2 & 0x0F);
            if (speedVal >= 1) {
                speedVal -= 1;
            } else {
                speedVal = 0;
            }
            text += Bundle.getMessage(SPEED_STEP_MODE_X, 14) + ",";
        }

        text += Bundle.getMessage("SpeedStepLabel") + " " + speedVal + ". ";

        if ((element1 & 0x08) == 0x08) {
            text += "" + Bundle.getMessage("XNetReplyAddressInUse");
        } else {
            text += "" + Bundle.getMessage("XNetReplyAddressFree");
        }
        return (text);
    }

    /**
     * Parse the status of functions F0-F12.
     *
     * @param element3 contains the data byte including F0,F1,F2,F3,F4
     * @param element4 contains F12,F11,F10,F9,F8,F7,F6,F5
     * @return readable version of message
     */
    protected String parseFunctionStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x10) != 0) {
            text += "F0 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F0 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x01) != 0) {
            text += "F1 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F1 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F2 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F2 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F3 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F3 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F4 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F4 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F5 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F5 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F6 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F6 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F7 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F7 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F8 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F8 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F9 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F9 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F10 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F10 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F11 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F11 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F12 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F12 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        return (text);
    }

}

