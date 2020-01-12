package jmri.jmrix.nce.ncemon;

import java.text.MessageFormat;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for formatting NCE binary command and replies into
 * human-readable text. The text for the display comes from NCE's Bincmds.txt
 * published November 2007 and is used with NCE's permission.
 *
 * @author Daniel Boudreau Copyright (C) 2012
 */
public class NceMonBinary {

    private static final Logger log = LoggerFactory.getLogger(NceMonBinary.class);

    private int replyType = REPLY_UNKNOWN;

    private static final int REPLY_UNKNOWN = 0;
    private static final int REPLY_STANDARD = 1;
    private static final int REPLY_DATA = 2;
    private static final int REPLY_ENTER_PROGRAMMING_MODE = 3;

    // The standard replies
    private static final int REPLY_ZERO = '0'; // command not supported
    private static final int REPLY_ONE = '1';  // loco/accy/signal address out of range
    private static final int REPLY_TWO = '2';  // cab address or op code out of range
    private static final int REPLY_THREE = '3';// CV address or data out of range
    private static final int REPLY_FOUR = '4'; // byte count out of range
    private static final int REPLY_OK = '!';   // command completed successfully

    /**
     * Creates a command message for the log, in a human-friendly form if
     * possible.
     *
     * @param m the raw command message
     * @return the displayable message string
     */
    public String displayMessage(NceMessage m) {
        return parseMessage(m);
    }

    private String parseMessage(NceMessage m) {
        // first check for messages that have a standard reply
        replyType = REPLY_STANDARD;
        switch (m.getOpCode() & 0xFF) {
            case (NceMessage.NOP_CMD):
                return Bundle.getMessage("NOP_CMD");
            case (NceMessage.STOP_CLOCK_CMD):
                return Bundle.getMessage("STOP_CLOCK_CMD");
            case (NceMessage.START_CLOCK_CMD):
                return Bundle.getMessage("START_CLOCK_CMD");
            case (NceMessage.SET_CLOCK_CMD):
                if (m.getNumDataElements() == 3) {
                    return MessageFormat.format(Bundle.getMessage("SET_CLOCK_CMD"),
                            new Object[]{m.getElement(1), m.getElement(2)});
                }
                break;
            case (NceMessage.CLOCK_1224_CMD):
                if (m.getNumDataElements() == 2) {
                    String hr = "12";
                    if (m.getElement(1) == 1) {
                        hr = "24";
                    }
                    return MessageFormat.format(Bundle.getMessage("CLOCK_1224_CMD"),
                            new Object[]{hr});
                }
                break;
            case (NceMessage.CLOCK_RATIO_CMD):
                if (m.getNumDataElements() == 2) {
                    return MessageFormat.format(Bundle.getMessage("CLOCK_RATIO_CMD"),
                            new Object[]{m.getElement(1)});
                }
                break;
            case (NceMessage.ENABLE_MAIN_CMD):
                return Bundle.getMessage("ENABLE_MAIN_CMD");
            case (NceMessage.KILL_MAIN_CMD):
                return Bundle.getMessage("KILL_MAIN_CMD");
            case (NceMessage.WRITE_N_CMD):
                if (m.getNumDataElements() == 20) {
                    return MessageFormat.format(Bundle.getMessage("WRITEn_CMD"),
                            new Object[]{m.getElement(3), getAddress(m), getDataBytes(m, 4, 16)});
                }
                break;
            // Send n bytes commands 0x93 - 0x96
            case (NceMessage.SENDn_BYTES_CMD + 3):
                if (m.getNumDataElements() == 5) {
                    return MessageFormat.format(Bundle.getMessage("SENDn_BYTES_CMD"),
                            new Object[]{"3", m.getElement(1), getDataBytes(m, 2, 3)});
                }
                break;
            case (NceMessage.SENDn_BYTES_CMD + 4):
                if (m.getNumDataElements() == 6) {
                    return MessageFormat.format(Bundle.getMessage("SENDn_BYTES_CMD"),
                            new Object[]{"4", m.getElement(1), getDataBytes(m, 2, 4)});
                }
                break;
            case (NceMessage.SENDn_BYTES_CMD + 5):
                if (m.getNumDataElements() == 7) {
                    return MessageFormat.format(Bundle.getMessage("SENDn_BYTES_CMD"),
                            new Object[]{"5", m.getElement(1), getDataBytes(m, 2, 5)});
                }
                break;
            case (NceMessage.SENDn_BYTES_CMD + 6):
                if (m.getNumDataElements() == 8) {
                    return MessageFormat.format(Bundle.getMessage("SENDn_BYTES_CMD"),
                            new Object[]{"6", m.getElement(1), getDataBytes(m, 2, 6)});
                }
                break;
            case (NceMessage.WRITE1_CMD):
                if (m.getNumDataElements() == 4) {
                    return MessageFormat.format(Bundle.getMessage("WRITE1_CMD"),
                            new Object[]{getAddress(m), getDataBytes(m, 3, 1)});
                }
                break;
            case (NceMessage.WRITE2_CMD):
                if (m.getNumDataElements() == 5) {
                    return MessageFormat.format(Bundle.getMessage("WRITE2_CMD"),
                            new Object[]{getAddress(m), getDataBytes(m, 3, 2)});
                }
                break;
            case (NceMessage.WRITE4_CMD):
                if (m.getNumDataElements() == 7) {
                    return MessageFormat.format(Bundle.getMessage("WRITE4_CMD"),
                            new Object[]{getAddress(m), getDataBytes(m, 3, 4)});
                }
                break;
            case (NceMessage.WRITE8_CMD):
                if (m.getNumDataElements() == 11) {
                    return MessageFormat.format(Bundle.getMessage("WRITE8_CMD"),
                            new Object[]{getAddress(m), getDataBytes(m, 3, 8)});
                }
                break;
            case (NceMessage.MACRO_CMD):
                if (m.getNumDataElements() == 2) {
                    return MessageFormat.format(Bundle.getMessage("MACRO_CMD"),
                            new Object[]{m.getElement(1)});
                }
                break;
            case (NceMessage.ENTER_PROG_CMD): {
                replyType = REPLY_ENTER_PROGRAMMING_MODE;
                return Bundle.getMessage("ENTER_PROG_CMD");
            }
            case (NceMessage.EXIT_PROG_CMD):
                return Bundle.getMessage("EXIT_PROG_CMD");
            case (NceMessage.WRITE_PAGED_CV_CMD):
                if (m.getNumDataElements() == 4) {
                    return MessageFormat.format(Bundle.getMessage("WRITE_PAGED_CV_CMD"),
                            new Object[]{getNumber(m), getDataBytes(m, 3, 1)});
                }
                break;
            case (NceMessage.LOCO_CMD):
                if (m.getNumDataElements() == 5) {
                    // byte three is the Op_1
                    switch (m.getElement(3)) {
                        case (1):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_01"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (2):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_02"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (3):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_03"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (4):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_04"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (5):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_05"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (6):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_06"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (7):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_07"),
                                    new Object[]{getLocoAddress(m), m.getElement(4), getFunctionNumber(m)});
                        case (8):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_08"),
                                    new Object[]{getLocoAddress(m), m.getElement(4), getFunctionNumber(m)});
                        case (9):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_09"),
                                    new Object[]{getLocoAddress(m), m.getElement(4), getFunctionNumber(m)});
                        case (0x0A):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_0A"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (0x0b):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_0B"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (0x0C):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_0C"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (0x0D):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_0D"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (0x0E):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_0E"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (0x0F):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_0F"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (0x10):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_10"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (0x11):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_11"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (0x12):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_12"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (0x15):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_15"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (0x16):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_16"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        case (0x17):
                            return MessageFormat.format(Bundle.getMessage("LOCO_CMD_Op1_17"),
                                    new Object[]{getLocoAddress(m), m.getElement(4)});
                        default:
                            log.error("Unhandled loco cmd op1 code: {}", m.getElement(3));
                            break;
                    }
                }
                break;
            // Queue commands 0xA3 - 0xA5
            case (NceMessage.QUEUEn_BYTES_CMD + 3):
                if (m.getNumDataElements() == 5) {
                    return MessageFormat.format(Bundle.getMessage("QUEUEn_BYTES_CMD"),
                            new Object[]{"3", m.getElement(1), getDataBytes(m, 2, 3)});
                }
                break;
            case (NceMessage.QUEUEn_BYTES_CMD + 4):
                if (m.getNumDataElements() == 6) {
                    return MessageFormat.format(Bundle.getMessage("QUEUEn_BYTES_CMD"),
                            new Object[]{"4", m.getElement(1), getDataBytes(m, 2, 4)});
                }
                break;
            case (NceMessage.QUEUEn_BYTES_CMD + 5):
                if (m.getNumDataElements() == 7) {
                    return MessageFormat.format(Bundle.getMessage("QUEUEn_BYTES_CMD"),
                            new Object[]{"5", m.getElement(1), getDataBytes(m, 2, 5)});
                }
                break;
            case (NceMessage.WRITE_REG_CMD):
                if (m.getNumDataElements() == 3) {
                    return MessageFormat.format(Bundle.getMessage("WRITE_REG_CMD"),
                            new Object[]{m.getElement(1), getDataBytes(m, 2, 1)});
                }
                break;
            case (NceMessage.WRITE_DIR_CV_CMD):
                if (m.getNumDataElements() == 4) {
                    return MessageFormat.format(Bundle.getMessage("WRITE_DIR_CV_CMD"),
                            new Object[]{getNumber(m), getDataBytes(m, 3, 1)});
                }
                break;
            case (NceMessage.SEND_ACC_SIG_MACRO_CMD):
                if (m.getNumDataElements() == 5) {
                    // byte three is the Op_1
                    switch (m.getElement(3)) {
                        case (1):
                            return MessageFormat.format(Bundle.getMessage("ACC_CMD_Op1_01"),
                                    new Object[]{m.getElement(4)});
                        case (3):
                            return MessageFormat.format(Bundle.getMessage("ACC_CMD_Op1_03"),
                                    new Object[]{getNumber(m)});
                        case (4):
                            return MessageFormat.format(Bundle.getMessage("ACC_CMD_Op1_04"),
                                    new Object[]{getNumber(m)});
                        case (5):
                            return MessageFormat.format(Bundle.getMessage("ACC_CMD_Op1_05"),
                                    new Object[]{getNumber(m), m.getElement(4)});
                        default:
                            log.error("Unhandled acc cmd op1 code: {}", m.getElement(3));
                            break;
                    }
                }
                break;
            case (NceMessage.USB_SET_CAB_CMD):
                if (m.getNumDataElements() == 2) {
                    return MessageFormat.format(Bundle.getMessage("Usb_Set_Cab_Op1"),
                            new Object[]{m.getElement(1)});
                }
                break;
            case (NceMessage.USB_MEM_POINTER_CMD):
                if (m.getNumDataElements() == 3) {
                    return MessageFormat.format(Bundle.getMessage("Usb_Set_Mem_Ptr_Cmd"),
                            new Object[]{m.getElement(1), m.getElement(2)});
                }
                break;
            case (NceMessage.USB_MEM_WRITE_CMD):
                if (m.getNumDataElements() == 2) {
                    return MessageFormat.format(Bundle.getMessage("Usb_Mem_Write_Cmd"),
                            new Object[]{m.getElement(1)});
                }
                break;
            default:
//                log.debug("Unhandled command code: {} after 1st pass", Integer.toHexString(m.getOpCode() & 0xFF));
                break;
        }
        // 2nd pass, check for messages that have a data reply
        replyType = REPLY_DATA;
        switch (m.getOpCode() & 0xFF) {
            case (NceMessage.READ_CLOCK_CMD):
                return Bundle.getMessage("READ_CLOCK_CMD");
            case (NceMessage.READ_AUI4_CMD):
                if (m.getNumDataElements() == 2) {
                    return MessageFormat.format(Bundle.getMessage("READ_AUI4_CMD"),
                            new Object[]{m.getElement(1)});
                }
                break;
            case (NceMessage.DUMMY_CMD):
                return Bundle.getMessage("DUMMY_CMD");
            case (NceMessage.READ16_CMD):
                if (m.getNumDataElements() == 3) {
                    return MessageFormat.format(Bundle.getMessage("READ16_CMD"),
                            new Object[]{getAddress(m)});
                }
                break;
            case (NceMessage.USB_MEM_READ_CMD):
                if (m.getNumDataElements() == 2) {
                    return MessageFormat.format(Bundle.getMessage("Usb_Mem_Read_Cmd"),
                            new Object[]{m.getElement(1)});
                }
                break;
            case (NceMessage.READ_AUI2_CMD):
                if (m.getNumDataElements() == 2) {
                    return MessageFormat.format(Bundle.getMessage("READ_AUI2_CMD"),
                            new Object[]{m.getElement(1)});
                }
                break;
            case (NceMessage.READ1_CMD):
                if (m.getNumDataElements() == 3) {
                    return MessageFormat.format(Bundle.getMessage("READ1_CMD"),
                            new Object[]{getAddress(m)});
                }
                break;
            case (NceMessage.READ_PAGED_CV_CMD):
                if (m.getNumDataElements() == 3) {
                    return MessageFormat.format(Bundle.getMessage("READ_PAGED_CV_CMD"),
                            new Object[]{getNumber(m)});
                }
                break;
            case (NceMessage.READ_REG_CMD):
                if (m.getNumDataElements() == 2) {
                    return MessageFormat.format(Bundle.getMessage("READ_REG_CMD"),
                            new Object[]{m.getElement(1)});
                }
                break;
            case (NceMessage.READ_DIR_CV_CMD):
                if (m.getNumDataElements() == 3) {
                    return MessageFormat.format(Bundle.getMessage("READ_DIR_CV_CMD"),
                            new Object[]{getNumber(m)});
                }
                break;
            case (NceMessage.SW_REV_CMD):
                return Bundle.getMessage("SW_REV_CMD");
            default:
                log.debug("Unhandled command code: {} after 2nd pass", Integer.toHexString(m.getOpCode() & 0xFF));
                break;
        }
        // this is one we don't know about or haven't coded it up
        replyType = REPLY_UNKNOWN;
        log.debug("Unhandled command code: {}, display as raw", Integer.toHexString(m.getOpCode() & 0xFF));
        return MessageFormat.format(Bundle.getMessage("BIN_CMD"), new Object[]{m.toString()});
    }

    private String getAddress(NceMessage m) {
        return StringUtil.twoHexFromInt(m.getElement(1)) + StringUtil.twoHexFromInt(m.getElement(2));
    }

    private String getDataBytes(NceMessage m, int start, int number) {
        StringBuilder sb = new StringBuilder(" ");
        for (int i = start; i < start + number; i++) {
            sb.append(StringUtil.twoHexFromInt(m.getElement(i))).append(" ");
        }
        return sb.toString();
    }

    private String getNumber(NceMessage m) {
        return Integer.toString(((m.getElement(1) & 0xFF) << 8) | (m.getElement(2) & 0xFF));
    }

    private String getLocoAddress(NceMessage m) {
        // show address type
        String appendix = " (short)";
        if ((m.getElement(1) & 0xE0) != 0) {
            appendix = " (long)";
        }
        return Integer.toString(((m.getElement(1) & 0x3F) << 8) | (m.getElement(2) & 0xFF)) + appendix;
    }

    private String getFunctionNumber(NceMessage m) {
        // byte three is the Op_1
        switch (m.getElement(3)) {
            case (7): {
                StringBuilder buf = new StringBuilder();
                if ((m.getElement(4) & 0x10) != 0) {
                    buf.append(Bundle.getMessage("F0_ON")).append(", ");
                } else {
                    buf.append(Bundle.getMessage("F0_OFF")).append(", ");
                }
                if ((m.getElement(4) & 0x01) != 0) {
                    buf.append(Bundle.getMessage("F1_ON")).append(", ");
                } else {
                    buf.append(Bundle.getMessage("F1_OFF")).append(", ");
                }
                if ((m.getElement(4) & 0x02) != 0) {
                    buf.append(Bundle.getMessage("F2_ON")).append(", ");
                } else {
                    buf.append(Bundle.getMessage("F2_OFF")).append(", ");
                }
                if ((m.getElement(4) & 0x04) != 0) {
                    buf.append(Bundle.getMessage("F3_ON")).append(", ");
                } else {
                    buf.append(Bundle.getMessage("F3_OFF")).append(", ");
                }
                if ((m.getElement(4) & 0x08) != 0) {
                    buf.append(Bundle.getMessage("F4_ON"));
                } else {
                    buf.append(Bundle.getMessage("F4_OFF"));
                }
                return buf.toString();
            }
            case (8): {
                StringBuilder buf = new StringBuilder();
                if ((m.getElement(4) & 0x01) != 0) {
                    buf.append(Bundle.getMessage("F5_ON")).append(", ");
                } else {
                    buf.append(Bundle.getMessage("F5_OFF")).append(", ");
                }
                if ((m.getElement(4) & 0x02) != 0) {
                    buf.append(Bundle.getMessage("F6_ON")).append(", ");
                } else {
                    buf.append(Bundle.getMessage("F6_OFF")).append(", ");
                }
                if ((m.getElement(4) & 0x04) != 0) {
                    buf.append(Bundle.getMessage("F7_ON")).append(", ");
                } else {
                    buf.append(Bundle.getMessage("F7_OFF")).append(", ");
                }
                if ((m.getElement(4) & 0x08) != 0) {
                    buf.append(Bundle.getMessage("F8_ON"));
                } else {
                    buf.append(Bundle.getMessage("F8_OFF"));
                }
                return buf.toString();
            }
            case (9): {
                StringBuilder buf = new StringBuilder();
                if ((m.getElement(4) & 0x01) != 0) {
                    buf.append(Bundle.getMessage("F9_ON")).append(", ");
                } else {
                    buf.append(Bundle.getMessage("F9_OFF")).append(", ");
                }
                if ((m.getElement(4) & 0x02) != 0) {
                    buf.append(Bundle.getMessage("F10_ON")).append(", ");
                } else {
                    buf.append(Bundle.getMessage("F10_OFF")).append(", ");
                }
                if ((m.getElement(4) & 0x04) != 0) {
                    buf.append(Bundle.getMessage("F11_ON")).append(", ");
                } else {
                    buf.append(Bundle.getMessage("F11_OFF")).append(", ");
                }
                if ((m.getElement(4) & 0x08) != 0) {
                    buf.append(Bundle.getMessage("F12_ON"));
                } else {
                    buf.append(Bundle.getMessage("F12_OFF"));
                }
                return buf.toString();
            }
            default:
                return ("Error");
        }
    }

    /**
     * Creates a reply message for the log, in a human-friendly form if
     * possible.
     *
     * @param r the raw reply message
     * @return the displayable message string
     */
    public String displayReply(NceReply r) {
        return parseReply(r);
    }

    private String parseReply(NceReply r) {
        switch (replyType) {
            case (REPLY_STANDARD):
                /* standard reply is a single byte
                 * Errors returned:
                 * '0'= command not supported
                 * '1'= loco/accy/signal address out of range
                 * '2'= cab address or op code out of range
                 * '3'= CV address or data out of range
                 * '4'= byte count out of range
                 * '!'= command completed successfully
                 */
                if (r.getNumDataElements() == 1) {
                    switch (r.getOpCode() & 0xFF) {
                        case (REPLY_ZERO):
                            return Bundle.getMessage("NceReplyZero");
                        case (REPLY_ONE):
                            return Bundle.getMessage("NceReplyOne");
                        case (REPLY_TWO):
                            return Bundle.getMessage("NceReplyTwo");
                        case (REPLY_THREE):
                            return Bundle.getMessage("NceReplyThree");
                        case (REPLY_FOUR):
                            return Bundle.getMessage("NceReplyFour");
                        case (REPLY_OK):
                            return Bundle.getMessage("NceReplyOK");
                        default:
                            log.error("Unhandled reply code: {}", Integer.toHexString(r.getOpCode() & 0xFF));
                            break;
                    }
                }
                break;
            case (REPLY_ENTER_PROGRAMMING_MODE):
                /* enter programming mode reply is a single byte
                 * '3'= short circuit
                 * '!'= command completed successfully
                 */
                if (r.getNumDataElements() == 1) {
                    switch (r.getOpCode() & 0xFF) {
                        case (REPLY_THREE):
                            return Bundle.getMessage("NceReplyThreeProg");
                        case (REPLY_OK):
                            return Bundle.getMessage("NceReplyOK");
                        default:
                            log.error("Unhandled programming reply code: {}", Integer.toHexString(r.getOpCode() & 0xFF));
                            break;
                    }
                }
                break;
            case (REPLY_DATA):
                break;
            default:
                log.debug("Unhandled reply type code: {}, display as raw", replyType);
                break;
        }
        return MessageFormat.format(Bundle.getMessage("NceReply"), new Object[]{r.toString()});
    }

}
