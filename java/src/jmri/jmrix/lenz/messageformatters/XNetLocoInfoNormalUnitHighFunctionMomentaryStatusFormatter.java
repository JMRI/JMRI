package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XpressNet Locomotive Information Normal Unit High Function Momentary Status messages.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLocoInfoNormalUnitHighFunctionMomentaryStatusFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply && ((XNetReply) m).getElement(0) == XNetConstants.LOCO_INFO_NORMAL_UNIT &&
                ((XNetReply) m).getElement(1) == XNetConstants.LOCO_FUNCTION_STATUS_HIGH_MOM;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            return Bundle.getMessage("XNetReplyLocoStatus13Label");
        }
        return Bundle.getMessage("XNetReplyLocoStatus13Label") + " " +
               parseFunctionHighMomentaryStatus(m.getElement(2), m.getElement(3));
    }


    private static final String FUNCTION_MOMENTARY = "FunctionMomentary";
    private static final String FUNCTION_CONTINUOUS = "FunctionContinuous";

    /**
     * Parse the Momentary sytatus of functions F13-F28.
     *
     * @param element3 contains F20,F19,F18,F17,F16,F15,F14,F13
     * @param element4 contains F28,F27,F26,F25,F24,F23,F22,F21
     * @return readable version of message
     */
    protected String parseFunctionHighMomentaryStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x01) != 0) {
            text += "F13 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F13 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F14 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F14 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F15 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F15 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F16 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F16 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x10) != 0) {
            text += "F17 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F17 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x20) != 0) {
            text += "F18 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F18 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x40) != 0) {
            text += "F19 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F19 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x80) != 0) {
            text += "F20 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F20 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F21 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F21 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F22 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F22 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F23 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F23 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F24 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F24 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F25 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F25 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F26 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F26 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F27 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F27 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F28 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F28 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        return (text);
    }

}
