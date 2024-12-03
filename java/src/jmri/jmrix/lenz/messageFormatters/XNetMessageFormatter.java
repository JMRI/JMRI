package jmri.jmrix.lenz.messageFormatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;
import jmri.jmrix.roco.z21.Z21Constants;

 /** Class that translates @Ref{XNetMessage} objects into strings
  *
  * NOTE: This is a placeholder until individual message classes are completed.
  *
  * @author Paul Bender Copyright (C) 2024
  */
public class XNetMessageFormatter implements XPressNetMessageFormatter {
    @Override
    public Boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                (m.getElement(0) != Z21Constants.LAN_X_CV_RESULT_XHEADER ||
                m.getElement(0) != Z21Constants.LAN_X_TURNOUT_INFO ||
                m.getElement(0) != Z21Constants.LAN_X_SET_TURNOUT ||
                m.getElement(0) != Z21Constants.LAN_X_GET_TURNOUT_INFO ||
                m.getElement(0) != XNetConstants.ACC_OPER_REQ ||
                m.getElement(0) != XNetConstants.ACC_INFO_REQ ||
                (m.getElement(0) & 0xF0 )!= (XNetConstants.OPS_MODE_PROG_WRITE_REQ & 0xF0) ||
                m.getElement(0) != XNetConstants.ALL_ESTOP ||
                m.getElement(0) != XNetConstants.EMERGENCY_STOP ||
                m.getElement(0) != XNetConstants.LI101_REQUEST ||
                m.getElement(0) != XNetConstants.CS_REQUEST ||
                m.getElement(0) != XNetConstants.CS_SET_POWERMODE ||
                (m.getElement(0) != XNetConstants.LOCO_DOUBLEHEAD
                      || m.getElement(1) != XNetConstants.LOCO_DOUBLEHEAD_BYTE2) ||
                m.getElement(0) != XNetConstants.CS_MULTI_UNIT_REQ ||
                m.getElement(0) != XNetConstants.LI_VERSION_REQUEST ||
                m.getElement(0) != XNetConstants.LOCO_STATUS_REQ ||
                m.getElement(0) != XNetConstants.PROG_READ_REQUEST ||
                m.getElement(0) != XNetConstants.PROG_WRITE_REQUEST
                );
    }

    @Override
    public String formatMessage(Message m) {
        return m.toMonitorString();
    }
}
