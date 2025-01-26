package jmri.jmrix.roco.z21.messageformatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21Reply;

/**
 * Formats Z21 XPressNet Tunnel Reply messages.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21XPressNetTunnelReplyFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Reply && ((Z21Reply) m).getOpCode() == 0x0040;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "cast is checked in handlesMessage")
    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not a Z21 XPressNet Tunnel Reply");
        }
        return Bundle.getMessage("Z21XpressNetTunnelReply", ((Z21Reply) m).getXNetReply().toMonitorString());
    }

}
