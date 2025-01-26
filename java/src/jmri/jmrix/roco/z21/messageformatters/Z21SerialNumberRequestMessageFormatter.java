package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;

/**
 * Message formatter for Z21 Serial Number Request Messages.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21SerialNumberRequestMessageFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message) m).getOpCode() == 0x0010;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not a Z21 Serial Number Request Message");
        }
        return Bundle.getMessage("Z21MessageStringSerialNoRequest");    }

}
