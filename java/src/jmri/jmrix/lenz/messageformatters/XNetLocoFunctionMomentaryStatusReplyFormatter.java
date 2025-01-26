package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format replies for XPressNet Command Station reply for Loco Function Status.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLocoFunctionMomentaryStatusReplyFormatter implements XPressNetMessageFormatter {
    private static final String RS_TYPE = "rsType";
    private static final String FUNCTION_MOMENTARY = "FunctionMomentary";
    private static final String FUNCTION_CONTINUOUS = "FunctionContinuous";

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                m.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE &&
                m.getElement(1) == XNetConstants.LOCO_FUNCTION_STATUS;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) { 
            throw new IllegalArgumentException("Message is not supported");
        }
        return Bundle.getMessage("XNetReplyLocoLabel") + " " + 
               Bundle.getMessage(RS_TYPE) + " " + // "Locomotive", key in NBBundle, shared with Operations
               Bundle.getMessage("XNetReplyFStatusLabel") + " " +
               parseFunctionMomentaryStatus(m.getElement(2), m.getElement(3));
    }

    /**
     * Parse the Momentary status of functions.
     *
     * @param element3 contains the data byte including F0,F1,F2,F3,F4
     * @param element4 contains F12,F11,F10,F9,F8,F7,F6,F5
     * @return readable version of message
     */
    protected String parseFunctionMomentaryStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x10) != 0) {
            text += "F0 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F0 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x01) != 0) {
            text += "F1 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F1 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F2 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F2 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F3 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F3 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F4 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F4 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F5 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F5 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F6 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F6 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F7 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F7 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F8 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F8 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F9 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F9 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F10 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F10 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F11 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F11 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F12 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F12 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        return (text);
    }


}
