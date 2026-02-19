package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;

/**
 * Z21 RailCom Data Request Message Formatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21RailComDataRequestFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message) m).getOpCode() == 0x0089;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message m is not a valid Z21 RailCom Data Request Message");
        }
        int address = ((Z21Message)m).getElement(5) & 0xFF + (((Z21Message)m).getElement(6) & 0xFF) << 8;
        return Bundle.getMessage("Z21_RAILCOM_GETDATA",address);
    }

}
