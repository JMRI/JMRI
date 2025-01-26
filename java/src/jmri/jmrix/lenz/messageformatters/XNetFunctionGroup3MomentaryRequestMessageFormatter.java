package jmri.jmrix.lenz.messageformatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.Message;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/** formatter for function group 3 momentary request messages
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup3MomentaryRequestMessageFormatter implements XPressNetMessageFormatter {

    private static final String X_NET_MESSAGE_SET_FUNCTION_GROUP_X_MOMENTARY = "XNetMessageSetFunctionGroupXMomentary";
    private static final String FUNCTION_CONTINUOUS = "FunctionContinuous";
    private static final String FUNCTION_MOMENTARY = "FunctionMomentary";

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                m.getElement(0) == XNetConstants.LOCO_OPER_REQ &&
                m.getElement(1) == XNetConstants.LOCO_SET_FUNC_GROUP3_MOMENTARY;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "cast is checked in handlesMessage")
    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException( "XNetFunction10MomentaryRequestMessageFormatter: message type not supported");
        }
        return "Mobile Decoder Operations Request: " + buildSetFunctionGroup3MomentaryMonitorString((XNetMessage ) m);
    }

    private String buildSetFunctionGroup3MomentaryMonitorString(XNetMessage m) {
        String text = Bundle.getMessage(X_NET_MESSAGE_SET_FUNCTION_GROUP_X_MOMENTARY, 3)
                + " " + LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)) + " ";
        int element4 = m.getElement(4);
        if ((element4 & 0x01) == 0) {
            text += "F9 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        } else {
            text += "F9 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        }
        if ((element4 & 0x02) == 0) {
            text += "F10 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        } else {
            text += "F10 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        }
        if ((element4 & 0x04) == 0) {
            text += "F11 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        } else {
            text += "F11 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        }
        if ((element4 & 0x08) == 0) {
            text += "F12 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        } else {
            text += "F12 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        }
        return text;
    }
}
