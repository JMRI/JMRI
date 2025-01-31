package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21Reply;

/**
 * Z21 Serial Number Reply Formatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21SerialNumberReplyFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Reply && ((Z21Reply) m).getOpCode() == 0x0010;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Z21SerialNumberReplyFormatter cannot format message that it does not handle.");
        }
        int serialNo = (m.getElement(4)&0xff) + ((m.getElement(5)&0xff) << 8)
                + ((m.getElement(6)&0xff) << 16) + ((m.getElement(7)&0xff) << 24);
        return Bundle.getMessage("Z21ReplyStringSerialNo", serialNo);
    }

}
