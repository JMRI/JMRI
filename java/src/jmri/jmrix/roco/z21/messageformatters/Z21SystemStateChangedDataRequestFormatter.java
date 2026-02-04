package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;

/**
 * Z21 Get System State Changed Data Request Formatter.
 *
 * @author Paul Bender Cpoyright (C) 2025
 */
public class Z21SystemStateChangedDataRequestFormatter implements Z21MessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message) m).getOpCode()==0x0085;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message m is not a valid Z21 System State Changed Data Request Message");
        }
        return Bundle.getMessage("Z21MessageSystemStateChangeDataRequest");
    }

}
