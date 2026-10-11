package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.lenz.XPressNetMessageFormatter;
import jmri.jmrix.roco.z21.Z21Constants;
import jmri.jmrix.roco.z21.Z21XNetMessage;

public class Z21XNetTurnoutMessageFormatter implements XPressNetMessageFormatter {

    @Override
    public String formatMessage(jmri.jmrix.Message m) {
        if(m.getElement(0) == Z21Constants.LAN_X_SET_TURNOUT) {
            int address = (m.getElement(1) << 8) + m.getElement(2) + 1;
            int element = m.getElement(3);
            boolean queue = (element & 0x20) == 0x20;
            String active = ((element & 0x08) == 0x08)? "activate":"deactivate";
            return Bundle.getMessage("Z21LAN_X_SET_TURNOUT", address, active, element & 0x01, queue);
        }
        if(m.getElement(0) == Z21Constants.LAN_X_GET_TURNOUT_INFO) {
            int address = (m.getElement(1) << 8) + m.getElement(2) + 1;
            return Bundle.getMessage("Z21LAN_X_GET_TURNOUT_INFO", address);
        }
        throw new IllegalArgumentException("Unknown message type");
    }

    @Override
    public boolean handlesMessage(jmri.jmrix.Message m) {
        return (m instanceof Z21XNetMessage &&
                (m.getElement(0) == Z21Constants.LAN_X_SET_TURNOUT || m.getElement(0) == Z21Constants.LAN_X_GET_TURNOUT_INFO));
    }

}
