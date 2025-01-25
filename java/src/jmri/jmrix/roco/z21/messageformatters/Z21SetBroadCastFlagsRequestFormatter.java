package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21MessageUtils;

/**
 * Formatter for Z21 Set BroadCast Flags Request.
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21SetBroadCastFlagsRequestFormatter implements Z21MessageFormatter {

    @Override
    public Boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message) m).getOpCode() == 0x0050;
    }

    @Override
    public String formatMessage(Message m) {
        if (!handlesMessage(m)) {
            return Bundle.getMessage("Z21MessageTypeMismatch", "Set BroadCast Flags");
        }
        return Bundle.getMessage("Z21MessageSetBroadcastFlags", Z21MessageUtils.interpretBroadcastFlags((m)));

    }

}
