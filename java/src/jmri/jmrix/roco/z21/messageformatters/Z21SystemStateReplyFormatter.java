package jmri.jmrix.roco.z21.messageformatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21Reply;

/**
 * Formatter for Z21 System State Reply.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21SystemStateReplyFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Reply && ((Z21Reply) m).getOpCode() == 0x0084;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "cast is checked in handlesMessage")
    @Override
    public String formatMessage(Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        Z21Reply r = (Z21Reply) m;
        int mainCurrent = r.getSystemDataMainCurrent();
        int progCurrent = r.getSystemDataProgCurrent();
        int filteredMainCurrent = r.getSystemDataFilteredMainCurrent();
        int temperature = r.getSystemDataTemperature();
        int supplyVolts = r.getSystemDataSupplyVoltage();
        int internalVolts = r.getSystemDataVCCVoltage();
        int state = r.getElement(16);
        int extendedState = r.getElement(17);
        // data bytes 14 and 15 (offset 18 and 19) are reserved.
        return Bundle.getMessage("Z21SystemStateReply",mainCurrent,
                progCurrent,filteredMainCurrent,temperature,
                supplyVolts,internalVolts,state,extendedState);
    }

}
