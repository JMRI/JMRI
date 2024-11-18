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
                ((m.getElement(0) != Z21Constants.LAN_X_CV_RESULT_XHEADER &&
                m.getElement(1) != Z21Constants.LAN_X_TURNOUT_INFO) ||
                m.getElement(0) != XNetConstants.ACC_OPER_REQ ||
                m.getElement(0) != XNetConstants.ACC_INFO_REQ);
    }

    @Override
    public String formatMessage(Message m) {
        return m.toMonitorString();
    }
}
