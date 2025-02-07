package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.RocoZ21CommandStation;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21MessageUtils;
import jmri.jmrix.roco.z21.Z21Reply;

/**
 * Formatter for Z21 Broadcast Flags Reply
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21BroadcastFlagsReplyFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Reply && ((Z21Reply) m).getOpCode() == 0x0051;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Z21BroadcastFlagsReplyFormatter cannot format message");
        }
        return Bundle.getMessage("Z21ReplyBroadcastFlags", RocoZ21CommandStation.getZ21BroadcastFlagsString(Z21MessageUtils.interpretBroadcastFlags(m)));
    }

}
