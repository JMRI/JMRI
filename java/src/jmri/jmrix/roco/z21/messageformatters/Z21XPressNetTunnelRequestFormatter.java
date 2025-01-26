package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21XNetMessage;

/**
 * Formatter for Z21 XPressNet Tunnel Requests.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21XPressNetTunnelRequestFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message)m).getOpCode() == 0x0040;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message m is not a Z21XNetMessage Tunnel Request");
        }
        return Bundle.getMessage("Z21MessageXpressNetTunnelRequest",new Z21XNetMessage((Z21Message)m).toMonitorString());
    }

}
