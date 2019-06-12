package jmri.jmrix.powerline.cp290;

/**
 * Constants and functions specific to the CP290 interface
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Ken Cameron, (C) 2009, add sensors from poll replies
 */
public class Constants {

    public static final int CMD_ON = 0x02;
    public static final int CMD_OFF = 0x03;
    public static final int CMD_DIM_R = 0x04;
    public static final int CMD_DIM_S = 0x05;

    /**
     * Translate Function to Text
     * @param cmd   cmd value
     * @param level dim value
     * @return      formated command as text
     */
    public static String commandToText(int cmd, int level) {
        String cmdText = "";
        switch (cmd) {
            case CMD_ON:
                cmdText = "ON";
                break;
            case CMD_OFF:
                cmdText = "OFF";
                break;
            case CMD_DIM_R:
                cmdText = "recvDIM";
                cmdText = cmdText + " ";
                break;
            case CMD_DIM_S:
                cmdText = "sendDIM";
                int v2 = (level * 100) / 16;
                cmdText = cmdText + " " + v2 + "%";
                break;
            default:
                cmdText = "Unk Cmd: " + cmd;
                break;
        }
        return (cmdText);
    }

    /**
     * Translate House Code to text
     * @param hC housecode value
     * @return   housecode as text
     */
    public static String houseCodeToText(int hC) {
        String hCode = "";
        switch (hC) {
            case 0x06:
                hCode = "A";
                break;
            case 0x0E:
                hCode = "B";
                break;
            case 0x02:
                hCode = "C";
                break;
            case 0x0A:
                hCode = "D";
                break;
            case 0x01:
                hCode = "E";
                break;
            case 0x09:
                hCode = "F";
                break;
            case 0x05:
                hCode = "G";
                break;
            case 0x0D:
                hCode = "H";
                break;
            case 0x07:
                hCode = "I";
                break;
            case 0x0F:
                hCode = "J";
                break;
            case 0x03:
                hCode = "K";
                break;
            case 0x0B:
                hCode = "L";
                break;
            case 0x00:
                hCode = "M";
                break;
            case 0x08:
                hCode = "N";
                break;
            case 0x04:
                hCode = "O";
                break;
            case 0x0C:
                hCode = "P";
                break;
            default:
                hCode = "Unk hC:" + hC;
                break;
        }
        return hCode;
    }

    /**
     * Translate Device Bits to Text
     * @param hByte high byte
     * @param lByte low byte
     * @return      text version of bytes
     */
    public static String deviceToText(int hByte, int lByte) {
        int mask = 0x01;
        int x = lByte;
        StringBuilder dev = new StringBuilder();
        for (int i = 8; i > 0; i--) {
            if ((x & mask) != 0) {
                dev.append(" " + i);
            }
            mask = mask << 1;
        }
        mask = 0x01;
        x = hByte;
        for (int i = 16; i > 8; i--) {
            if ((x & mask) != 0) {
                dev.append(" " + i);
            }
            mask = mask << 1;
        }
        return dev.toString();
    }

    /**
     * Translate status to text
     * @param s status value
     * @return  status value as text
     */
    public static String statusToText(int s) {
        String stat = "";
        switch (s) {
            case 0:
                stat = "Interface Powered Off";
                break;
            case 1:
                stat = "Cmd Ok";
                break;
            default:
                stat = "Unk Status: " + s;
                break;
        }
        return (stat);
    }

    /**
     * Format a message nicely
     * @param m message
     * @return  message contents as text
     */
    public static String toMonitorString(jmri.jmrix.Message m) {
        // check for valid length
        String val = "???";
        int len = m.getNumDataElements();
        boolean goodSync = true;
        boolean goodCheckSum = true;
        int sum = 0;
        String cmd;
        String stat;
        String hCode;
        String bCode;
        String dev;
        switch (len) {
            case 7:
                for (int i = 0; i < 6; i++) {
                    if ((m.getElement(i) & 0xFF) != 0xFF) {
                        goodSync = false;
                    }
                }
                val = statusToText(m.getElement(6));
                break;
            case 12:
                for (int i = 0; i < 6; i++) {
                    if ((m.getElement(i) & 0xFF) != 0xFF) {
                        goodSync = false;
                    }
                }
                for (int i = 7; i < 11; i++) {
                    sum = (sum + (m.getElement(i) & 0xFF)) & 0xFF;
                }
                stat = statusToText(m.getElement(6));
                cmd = commandToText(m.getElement(7) & 0x0F, -1);
                hCode = houseCodeToText((m.getElement(7) >> 4) & 0x0F);
                dev = deviceToText(m.getElement(8), m.getElement(9));
                bCode = houseCodeToText((m.getElement(10) >> 4) & 0x0F);
                if (sum != (m.getElement(11) & 0xFF)) {
                    goodCheckSum = false;
                }
                val = "Cmd Echo: " + cmd + " stat: " + stat + " House: " + hCode + " Device:" + dev + " base: " + bCode;
                if (!goodSync) {
                    val = val + " BAD SYNC";
                }
                if (!goodCheckSum) {
                    val = val + " BAD CHECKSUM: " + (m.getElement(11) & 0xFF) + " vs " + sum;
                }
                break;
            case 22:
                for (int i = 0; i < 16; i++) {
                    if ((m.getElement(i) & 0xFF) != 0xFF) {
                        goodSync = false;
                    }
                }
                for (int i = 17; i < 21; i++) {
                    sum = (sum + (m.getElement(i) & 0xFF)) & 0xFF;
                }
                cmd = commandToText((m.getElement(17) & 0x0F), ((m.getElement(17) & 0xF0) >> 4));
                hCode = houseCodeToText((m.getElement(18) >> 4) & 0x0F);
                dev = deviceToText(m.getElement(19), m.getElement(20));
                if (sum != (m.getElement(21) & 0xFF)) {
                    goodCheckSum = false;
                }
                val = cmd + " House: " + hCode + " Device:" + dev;
                if (!goodSync) {
                    val = val + " BAD SYNC";
                }
                if (!goodCheckSum) {
                    val = val + " BAD CHECKSUM: " + (m.getElement(21) & 0xFF) + " vs " + sum;
                }
                break;
            default:
                val = "UNK " + m.toString();
                break;
        }
        return val;
    }

}
