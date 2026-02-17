package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.MessageFormatter;
import jmri.jmrix.roco.z21.Z21Message;

/**
 * Formatter for Z21 CAN Get Description Requests
 * @author Paul Bender Copyright (C) 2026
 */
public class Z21CANGetDescriptionRequestFormatter implements MessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message)m).getOpCode() == 0x00C8;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message m is not a Z21 CAN Description Request");
        }
        int networkID = ( m.getElement(4) & 0xFF) + ((m.getElement(5) & 0xFF) << 8);
        return Bundle.getMessage("Z21CANGetDescriptionRequest", Integer.toHexString(networkID));
    }
}
