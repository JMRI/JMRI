package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;

/**
 * Formatter for Z21 CAN Detector Requests
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21CANDetectorRequestFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message)m).getOpCode() == 0x00C4;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message m is not a Z21 CAN Detector Request");
        }
        int networkID = ( m.getElement(4) & 0xFF) + ((m.getElement(5) & 0xFF) << 8);
        return Bundle.getMessage("Z21CANDetectorRequest", Integer.toHexString(networkID));
    }

}
