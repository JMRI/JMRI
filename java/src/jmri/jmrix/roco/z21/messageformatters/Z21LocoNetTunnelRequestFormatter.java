package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;

/**
 * Z21 LocoNet Tunnel Request Formatter
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21LocoNetTunnelRequestFormatter implements Z21MessageFormatter {

    @Override
    public Boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message) m).getOpCode() == 0x00A2;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message is not a Z21 LocoNet Tunnel Request Message");
        }
        return Bundle.getMessage("Z21LocoNetLanMessage", ((Z21Message) m).getLocoNetMessage().toMonitorString());
    }

}
