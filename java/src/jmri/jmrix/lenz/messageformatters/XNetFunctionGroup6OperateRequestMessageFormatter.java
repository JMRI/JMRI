package jmri.jmrix.lenz.messageformatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.Message;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/** formatter for function group 6 operations request messages
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup6OperateRequestMessageFormatter implements XPressNetMessageFormatter {

    private static final String X_NET_MESSAGE_SET_FUNCTION_GROUP_X = "XNetMessageSetFunctionGroupX";
    private static final String POWER_STATE_ON = "PowerStateOn";
    private static final String POWER_STATE_OFF = "PowerStateOff";

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                m.getElement(0) == XNetConstants.LOCO_OPER_REQ &&
                m.getElement(1) == XNetConstants.LOCO_SET_FUNC_GROUP6;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "cast is checked in handlesMessage")
    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException( "XNetFunction6OperateRequestMessageFormatter: message type not supported");
        }
        return "Mobile Decoder Operations Request: " + buildSetFunctionGroup6MonitorString((XNetMessage ) m);
    }


    private String buildSetFunctionGroup6MonitorString(XNetMessage m) {
        String text = Bundle.getMessage(X_NET_MESSAGE_SET_FUNCTION_GROUP_X, 6)
                + " " + LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)) + " ";
        int element4 = m.getElement(4);
        if ((element4 & 0x01) != 0) {
            text += "F29 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F29 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F30 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F30 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F31 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F31 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F32 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F32 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F33 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F33 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F34 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F34 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F35 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F35 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F36 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F36 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        return text;
    }


}
