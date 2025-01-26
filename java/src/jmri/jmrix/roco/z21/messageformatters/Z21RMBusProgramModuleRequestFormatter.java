package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;

/**
 * Formatter for Z21 RM Bus Program Module Requests..
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21RMBusProgramModuleRequestFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message) m).getOpCode() == 0x0082;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            return Bundle.getMessage("Z21MessageNotSupported", m.toString());
        }
        return Bundle.getMessage("Z21RMBusProgramModuleRequest", m.getElement(4));
    }

}
