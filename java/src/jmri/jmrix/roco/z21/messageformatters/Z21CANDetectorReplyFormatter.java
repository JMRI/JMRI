package jmri.jmrix.roco.z21.messageformatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21MessageUtils;
import jmri.jmrix.roco.z21.Z21Reply;

/**
 * Formats Z21 CAN Detector Replies.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21CANDetectorReplyFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Reply && ((Z21Reply) m).getOpCode() == 0x00C4;
    }

    @Override
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message is not a Z21 CAN Detector Reply");
        }
        int networkID = ( m.getElement(4)&0xFF) + ((m.getElement(5)&0xFF) << 8);
        int address = ( m.getElement(6)&0xFF) + ((m.getElement(7)&0xFF) << 8);
        int port = ( m.getElement(8) & 0xFF);
        int type = ( m.getElement(9) & 0xFF);
        int value1 = (m.getElement(10)&0xFF) + ((m.getElement(11)&0xFF) << 8);
        int value2 = (m.getElement(12)&0xFF) + ((m.getElement(13)&0xFF) << 8);
        String typeString = "";
        String value1String;
        String value2String = "";
        switch(type){
            case 0x01:
                typeString = "Input Status";
                switch(value1){
                    case 0x0000:
                        value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_FREE_WITHOUT");
                        break;
                    case 0x0100:
                        value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_FREE_WITH");
                        break;
                    case 0x1000:
                        value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_BUSY_WITHOUT");
                        break;
                    case 0x1100:
                        value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_BUSY_WITH");
                        break;
                    case 0x1201:
                        value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_OVERLOAD_1");
                        break;
                    case 0x1202:
                        value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_OVERLOAD_2");
                        break;
                    case 0x1203:
                        value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_OVERLOAD_3");
                        break;
                    default:
                        value1String = "<unknown>";
                }
                break;
            case 0x11:
            case 0x12:
            case 0x13:
            case 0x14:
            case 0x15:
            case 0x16:
            case 0x17:
            case 0x18:
            case 0x19:
            case 0x1A:
            case 0x1B:
            case 0x1C:
            case 0x1D:
            case 0x1E:
            case 0x1F:
                typeString = "Occupancy Info";
                value1String = getCanDetectorLocoAddressString(value1);
                value2String = getCanDetectorLocoAddressString(value2);
                break;
            default:
                value1String = "" + value1;
                value2String = "" + value2;
        }

        return Bundle.getMessage("Z21CANDetectorReply",Integer.toHexString(networkID),address,port,typeString,value1String,value2String);
    }

    // address value is the 16 bits of the two bytes containing the
    // address.  The most significan two bits represent the direction.
    private String getCanDetectorLocoAddressString(int addressValue) {
        String addressString;
        if(addressValue==0) {
            addressString="end of list";
        } else {
            addressString = "" + (Z21MessageUtils.getCanDetectorLocoAddress(addressValue)).toString();
            int direction = (0xC000&addressValue);
            switch (direction) {
                case 0x8000:
                    addressString += " direction forward";
                    break;
                case 0xC000:
                    addressString += " direction reverse";
                    break;
                default:
                    addressString += " direction unknown";
            }
        }
        return addressString;
    }

}
