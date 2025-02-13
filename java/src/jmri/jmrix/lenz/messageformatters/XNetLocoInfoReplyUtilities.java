package jmri.jmrix.lenz.messageformatters;


/**
 * Utility methods for parsing the Loco Info Reply messages.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLocoInfoReplyUtilities {

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
    public static String parseSpeedAndDirection(int element1, int element2) {
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
    public static String parseFunctionStatus(int element3, int element4) {
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

    /**
     * Parse the status of functions F13-F28.
     *
     * @param element3 contains F20,F19,F18,F17,F16,F15,F14,F13
     * @param element4 contains F28,F27,F26,F25,F24,F23,F22,F21
     * @return readable version of message
     */
    public static String parseFunctionHighStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x01) != 0) {
            text += "F13 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F13 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F14 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F14 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F15 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F15 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F16 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F16 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x10) != 0) {
            text += "F17 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F17 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x20) != 0) {
            text += "F18 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F18 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x40) != 0) {
            text += "F19 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F19 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x80) != 0) {
            text += "F20 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F20 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F21 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F21 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F22 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F22 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F23 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F23 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F24 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F24 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F25 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F25 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F26 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F26 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F27 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F27 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F28 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F28 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        return (text);
    }
}
