package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;

/**
 * MessageFormatter for Z21 CAN Booster Set Track Power messages.
 * <p>
 * These messages are sent to the Z21 to set the track power output state of a CAN booster.
 *
 * @author Paul Bender Copyright (C) 2026
 */
public class Z21CANBoosterSetTrackPowerFormatter implements Z21MessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Message && ((Z21Message)m).getOpCode() == 0x00CB;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message m is not a Z21 CAN Booster Set Track Power Request");
        }
        int networkID = ( m.getElement(4) & 0xFF) + ((m.getElement(5) & 0xFF) << 8);
        int outputState = m.getElement(6) & 0xFF;
        return Bundle.getMessage("Z21CANBoosterSetTrackPowerRequest", Integer.toHexString(networkID),
                getOutputStateString(outputState));
    }

    private String getOutputStateString(int outputState){
        switch(outputState){
            case 0x00: return Bundle.getMessage("Z21_CAN_BOOSTER_STATUS_STATE_ALL_OFF");
            case 0xFF: return Bundle.getMessage("Z21_CAN_BOOSTER_STATUS_STATE_ALL_ON");
            case 0x10: return Bundle.getMessage("Z21_CAN_BOOSTER_STATUS_STATE_FIRST_OFF");
            case 0x11: return Bundle.getMessage("Z21_CAN_BOOSTER_STATUS_STATE_FIRST_ON");
            case 0x20: return Bundle.getMessage("Z21_CAN_BOOSTER_STATUS_STATE_SECOND_OFF");
            case 0x22: return Bundle.getMessage("Z21_CAN_BOOSTER_STATUS_STATE_SECOND_ON");
            default: return "Unknown (" + outputState + ")";
        }
    }


}
