package jmri.jmrix.roco.z21.messageformatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21Reply;


/**
 * Formats the Z21 Hardware Info Reply message.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21HardwareInfoReplyFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Reply && ((Z21Reply)m).getOpCode() == 0x001A;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "cast is checked in handlesMessage")
    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Z21HardwareInfoReplyFormatter cannot format message that it does not handle");
        }
        Z21Reply r = (Z21Reply) m;
        int hwversion = r.getElement(4) + (r.getElement(5) << 8) +
                (r.getElement(6) << 16 ) + (r.getElement(7) << 24 );
        float swversion = (r.getElementBCD(8)/100.0f)+
                (r.getElementBCD(9))+
                (r.getElementBCD(10)*100)+
                (r.getElementBCD(11))*10000;
        return Bundle.getMessage("Z21ReplyStringVersion",java.lang.Integer.toHexString(hwversion), swversion);
    }

}
