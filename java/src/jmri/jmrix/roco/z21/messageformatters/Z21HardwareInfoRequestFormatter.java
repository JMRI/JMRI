package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;

/**
 * Formatter for Z21 Hardware Info Request Messages
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21HardwareInfoRequestFormatter implements Z21MessageFormatter{

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message) m).getOpCode() == 0x001A;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message m is not a valid Z21 Hardware Info Request Message");
        }
        return Bundle.getMessage("Z21MessageStringVersionRequest");
    }

}
