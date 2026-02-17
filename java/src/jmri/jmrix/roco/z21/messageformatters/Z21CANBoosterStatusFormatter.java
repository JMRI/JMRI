package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21Reply;

public class Z21CANBoosterStatusFormatter implements Z21MessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Reply && ((Z21Reply)m).getOpCode() == 0x00CA;
    }

    private void log(String s, Message m) {
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message m is not a Z21 CAN Booster Status Reply");
        }
        int networkID = ( m.getElement(4) & 0xFF) + ((m.getElement(5) & 0xFF) << 8);
        int output = ( m.getElement(6) & 0xFF) + ((m.getElement(7) & 0xFF) << 8);
        int stateFlags = ( m.getElement(8) & 0xFF) + ((m.getElement(9) & 0xFF) << 8);
        int voltage = ( m.getElement(10) & 0xFF) + ((m.getElement(11) & 0xFF) << 8);
        int amperage = ( m.getElement(12) & 0xFF) + ((m.getElement(13) & 0xFF) << 8);
        String hexAddress= Integer.toHexString(networkID);
        StringBuilder statusBuilder = new StringBuilder();
        if((stateFlags & 0x0001) != 0) {
            statusBuilder.append("\t\t")
                    .append(Bundle.getMessage("Z21_CAN_BOOSTER_STATUS_FLAG_BRAKE_GENERATOR"))
                    .append(System.lineSeparator());
        }
        if((stateFlags & 0x0020) != 0) {
            statusBuilder.append("\t\t")
                    .append(Bundle.getMessage("Z21_CAN_BOOSTER_STATUS_FLAG_SHORT_CIRCUIT"))
                    .append(System.lineSeparator());
        }
        if((stateFlags & 0x0080) != 0) {
            statusBuilder.append("\t\t")
                    .append(Bundle.getMessage("Z21_CAN_BOOSTER_STATUS_FLAG_TRACK_OFF"))
                    .append(System.lineSeparator());
        }
        if((stateFlags & 0x0800) != 0) {
            statusBuilder.append("\t\t")
                    .append(Bundle.getMessage("Z21_CAN_BOOSTER_STATUS_FLAG_RAILCOM_ACTIVE"))
                    .append(System.lineSeparator());
        }
        if((stateFlags & 0x0100) != 0) {
            statusBuilder.append("\t\t")
                    .append(Bundle.getMessage("Z21_CAN_BOOSTER_STATUS_FLAG_OUTPUT_DISABLED"))
                    .append(System.lineSeparator());
        }
        statusBuilder.delete(statusBuilder.length() - System.lineSeparator().length(), statusBuilder.length());
        return Bundle.getMessage("Z21CANBoosterStatusReply", hexAddress, output, statusBuilder.toString(), voltage, amperage);
    }
}
