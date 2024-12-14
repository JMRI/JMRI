package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.lenz.XPressNetMessageFormatter;
import jmri.jmrix.roco.z21.Z21Constants;

public class Z21XNetReplyFormatter implements XPressNetMessageFormatter {
    public Z21XNetReplyFormatter() {
    }

    public Boolean handlesMessage(jmri.jmrix.Message m) {
        return (m instanceof jmri.jmrix.roco.z21.Z21XNetReply && (m.getElement(0) == Z21Constants.LAN_X_CV_RESULT_XHEADER) ||
                (m.getElement(0) == Z21Constants.LAN_X_TURNOUT_INFO));
    }

    public String formatMessage(jmri.jmrix.Message m) {
        return m.toMonitorString();
    }
}
