package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;

/**
 * Z21 Broadcast Flags Request Message formatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21BroadcastFlagsRequestFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message) m).getOpCode() == 0x0051;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message m is not a Z21Message with OpCode 0x0051.");
        }
        return Bundle.getMessage("Z21MessageRequestBroadcastFlags");
    }

}
