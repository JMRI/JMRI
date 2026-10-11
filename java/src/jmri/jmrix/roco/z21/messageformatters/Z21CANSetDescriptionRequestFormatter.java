package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;

/**
 * Formatter for Z21 CAN Set Description Requests
 * @author Paul Bender Copyright (C) 2026
 */
public class Z21CANSetDescriptionRequestFormatter implements Z21MessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message)m).getOpCode() == 0x00C9;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message m is not a Z21 CAN Description Request");
        }
        int networkID = ( m.getElement(4) & 0xFF) + ((m.getElement(5) & 0xFF) << 8);
        byte[] description = new byte[16];
        for(int i = 0; i < 16; i++) {
            description[i] = (byte) m.getElement(6 + i);
        }
        String hexAddress= Integer.toHexString(networkID);
        String descString = new String(description).trim();
        return Bundle.getMessage("Z21CANSetDescriptionRequest", hexAddress, descString);
    }
}
