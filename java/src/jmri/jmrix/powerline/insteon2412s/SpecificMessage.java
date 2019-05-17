package jmri.jmrix.powerline.insteon2412s;

import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.X10Sequence;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the data payload of a serial packet.
 * <p>
 * The transmission protocol can come in one of several forms:
 * <ul>
 * <li>If the interlocked parameter is false (default), the packet is just sent.
 * If the response length is not zero, a reply of that length is expected.
 * <li>If the interlocked parameter is true, the transmission will require a CRC
 * interlock, which will be automatically added. (Design note: this is done to
 * make sure that the messages remain atomic)
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2001,2003, 2006, 2007, 2008, 2009
 * @author Ken Cameron Copyright (C) 2010
 */
public class SpecificMessage extends SerialMessage {
    // is this logically an abstract class?

    /**
     * Suppress the default ctor, as the length must always be specified
     */
    @SuppressWarnings("unused")
    private SpecificMessage() {
    }

    public SpecificMessage(int l) {
        super(l);
        setResponseLength(0);  // only polls require a response
        setBinary(true);
        setTimeout(5000);
    }

    /**
     * This ctor interprets the String as the exact sequence to send,
     * byte-for-byte.
     *
     * @param m message
     * @param l response length in bytes
     */
    public SpecificMessage(String m, int l) {
        super(m, l);
    }

    boolean interlocked = false;

    @Override
    public void setInterlocked(boolean v) {
        interlocked = v;
    }

    @Override
    public boolean getInterlocked() {
        return interlocked;
    }

    @Override
    public String toMonitorString() {
        // check for valid length
        int len = getNumDataElements();
        StringBuilder text = new StringBuilder();
        if ((getElement(0) & 0xFF) != Constants.HEAD_STX) {
            text.append("INVALID HEADER: " + String.format("0x%1X", getElement(0) & 0xFF));
            text.append(" len: " + len);
        } else {
            switch (getElement(1) & 0xFF) {
                case Constants.FUNCTION_REQ_STD:
                    text.append("Send Cmd ");
                    if (len == 8 || len == 22) {
                        if ((getElement(5) & Constants.FLAG_BIT_STDEXT) == Constants.FLAG_STD) {
                            text.append(" Std");
                        } else if (len == 22) {
                            text.append(" Ext");
                        }
                        switch (getElement(5) & Constants.FLAG_MASK_MSGTYPE) {
                            case Constants.FLAG_TYPE_P2P:
                                text.append(" Direct");
                                break;
                            case Constants.FLAG_TYPE_ACK:
                                text.append(" ACK");
                                break;
                            case Constants.FLAG_TYPE_NAK:
                                text.append(" NAK");
                                break;
                            case Constants.FLAG_TYPE_GBCAST:
                                text.append(" Group Broadcast");
                                break;
                            case Constants.FLAG_TYPE_GBCLEANUP:
                                text.append(" Group Broadcast Cleanup");
                                break;
                            case Constants.FLAG_TYPE_GBCLEANACK:
                                text.append(" Group Broadcast Cleanup ACK");
                                break;
                            case Constants.FLAG_TYPE_GBCLEANNAK:
                                text.append(" Group Broadcast Cleanup NAK");
                                break;
                            default:
                                log.warn("Unhandled flag type: {}", getElement(5) & Constants.FLAG_MASK_MSGTYPE);
                                break;
                        }
                        text.append(" message,");
                        text.append(String.format(" %d hops left", (getElement(5) & Constants.FLAG_MASK_HOPSLEFT >> Constants.FLAG_SHIFT_HOPSLEFT)));
                        text.append(String.format(" , %d max hops", (getElement(5) & Constants.FLAG_MASK_MAXHOPS)));
                        text.append(" addr " + String.format("%1$X.%2$X.%3$X", (getElement(2) & 0xFF), (getElement(3) & 0xFF), (getElement(4) & 0xFF)));
                        switch (getElement(6) & 0xFF) {
                            case Constants.CMD_LIGHT_ON_RAMP:
                                text.append(" ON RAMP ");
                                text.append((getElement(7) & 0xFF) / 256.0);
                                break;
                            case Constants.CMD_LIGHT_ON_FAST:
                                text.append(" ON FAST ");
                                text.append((getElement(7) & 0xFF) / 256.0);
                                break;
                            case Constants.CMD_LIGHT_OFF_FAST:
                                text.append(" OFF FAST ");
                                text.append((getElement(7) & 0xFF) / 256.0);
                                break;
                            case Constants.CMD_LIGHT_OFF_RAMP:
                                text.append(" OFF ");
                                text.append((getElement(7) & 0xFF) / 256.0);
                                break;
                            case Constants.CMD_LIGHT_CHG:
                                text.append(" CHG ");
                                text.append((getElement(7) & 0xFF) / 256.0);
                                break;
                            default:
                                text.append(" Unknown cmd: " + StringUtil.twoHexFromInt(getElement(6) & 0xFF));
                                break;
                        }
                    } else {
                        text.append(" !! Length wrong: " + len);
                    }
                    break;
                // i wrote this then figured the POLL are replies
//             case Constants.POLL_REQ_BUTTON :
//              text.append("Poll Button ");
//              int button = ((getElement(2) & Constants.BUTTON_BITS_ID) >> 4) + 1;
//              text.append(button);
//              int op = getElement(2) & Constants.BUTTON_BITS_OP;
//              if (op == Constants.BUTTON_HELD) {
//               text.append(" HELD");
//              } else if (op == Constants.BUTTON_REL) {
//               text.append(" RELEASED");
//              } else if (op == Constants.BUTTON_TAP) {
//               text.append(" TAP");
//              }
//              break;
//             case Constants.POLL_REQ_BUTTON_RESET :
//              text.append("Reset by Button at Power Cycle");
//              break;
                case Constants.FUNCTION_REQ_X10:
                    text.append("Send Cmd X10 ");
                    if ((getElement(3) & Constants.FLAG_BIT_X10_CMDUNIT) == Constants.FLAG_X10_RECV_CMD) {
                        text.append(X10Sequence.formatCommandByte(getElement(2) & 0xFF));
                    } else {
                        text.append(X10Sequence.formatAddressByte(getElement(2) & 0xFF));
                    }
                    break;
//             case Constants.POLL_REQ_X10 :
//              text.append("Poll Cmd X10 ");
//                    if ((getElement(3)& Constants.FLAG_BIT_X10_CMDUNIT) == Constants.FLAG_X10_RECV_CMD) {
//                     text.append(X10Sequence.formatCommandByte(getElement(2) & 0xFF));
//                    } else {
//                     text.append(X10Sequence.formatAddressByte(getElement(2)& 0xFF));
//                    }
//              break;
                default: {
                    text.append(" Unknown command: " + StringUtil.twoHexFromInt(getElement(1) & 0xFF));
                    text.append(" len: " + len);
                }
            }
        }
        return text + "\n";
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     * @param l length of expected reply
     */
    public SpecificMessage(byte[] a, int l) {
        super(a, l);
    }

    int responseLength = -1;  // -1 is an invalid value, indicating it hasn't been set

    @Override
    public void setResponseLength(int l) {
        responseLength = l;
    }

    @Override
    public int getResponseLength() {
        return responseLength;
    }

    // static methods to recognize a message
//    public boolean isPoll() { return getElement(1)==48;}
//    public boolean isXmt()  { return getElement(1)==17;}
//    public int getAddr() { return getElement(0); }
    // static methods to return a formatted message
    static public SerialMessage getPoll(int addr) {
        // Powerline implementation does not currently poll
        return null;
    }

    /**
     * create an Insteon message with the X10 address
     * @param housecode  X10 housecode
     * @param devicecode X10 devicecode
     *
     * @return message   formated message
     */
    static public SpecificMessage getX10Address(int housecode, int devicecode) {
        SpecificMessage m = new SpecificMessage(4);
        m.setInterlocked(false);
        m.setElement(0, Constants.HEAD_STX);
        m.setElement(1, Constants.FUNCTION_REQ_X10);
        m.setElement(2, (X10Sequence.encode(housecode) << 4) + X10Sequence.encode(devicecode));
        m.setElement(3, 0x00);  //  0x00 Means address
        return m;
    }

    /**
     * create an Insteon message with the X10 address and dim steps
     *
     * @param housecode  X10 housecode
     * @param devicecode X10 devicecode
     * @param dimcode    value for dimming
     *
     * @return message   formated message
     */
    static public SpecificMessage getX10AddressDim(int housecode, int devicecode, int dimcode) {
        SpecificMessage m = new SpecificMessage(4);
        m.setInterlocked(false);
        m.setElement(0, Constants.HEAD_STX);
        m.setElement(1, Constants.FUNCTION_REQ_X10);
        if (dimcode > 0) {
            m.setElement(2, 0x04 | ((dimcode & 0x1f) << 3));
        } else {
            m.setElement(2, 0x04);
        }
        m.setElement(3, (X10Sequence.encode(housecode) << 4) + X10Sequence.encode(devicecode));
        m.setElement(3, 0x80);  //  0x00 Means address
        return m;
    }

    static public SpecificMessage getX10FunctionDim(int housecode, int function, int dimcode) {
        SpecificMessage m = new SpecificMessage(2);
        m.setInterlocked(true);
        if (dimcode > 0) {
            m.setElement(0, 0x06 | ((dimcode & 0x1f) << 3));
        } else {
            m.setElement(0, 0x06);
        }
        m.setElement(1, (X10Sequence.encode(housecode) << 4) + function);
        return m;
    }

    static public SpecificMessage getX10Function(int housecode, int function) {
        SpecificMessage m = new SpecificMessage(4);
//        m.setInterlocked(true);
        m.setInterlocked(false);
        m.setElement(0, Constants.HEAD_STX);
        m.setElement(1, Constants.FUNCTION_REQ_X10);
        m.setElement(2, (X10Sequence.encode(housecode) << 4) + function);
        m.setElement(3, 0x80);  //  0x80 means function
        return m;
    }

    static public SpecificMessage getInsteonAddress(int idhighbyte, int idmiddlebyte, int idlowbyte) {
        SpecificMessage m = new SpecificMessage(8);
//        m.setInterlocked(true);
        m.setInterlocked(false);
        m.setElement(0, Constants.HEAD_STX);
        m.setElement(1, Constants.FUNCTION_REQ_STD);
        m.setElement(2, idhighbyte);
        m.setElement(3, idmiddlebyte);
        m.setElement(4, idlowbyte);
        m.setElement(5, 0x0F);
        m.setElement(6, 0x11);
        m.setElement(7, 0xFF);
        return m;
    }

    static public SpecificMessage getInsteonFunction(int idhighbyte, int idmiddlebyte, int idlowbyte, int function, int flag, int cmd1, int cmd2) {
        SpecificMessage m = new SpecificMessage(8);
//        m.setInterlocked(true);
        m.setInterlocked(false);
        m.setElement(0, Constants.HEAD_STX);
        m.setElement(1, Constants.FUNCTION_REQ_STD);
        m.setElement(2, idhighbyte);
        m.setElement(3, idmiddlebyte);
        m.setElement(4, idlowbyte);
        m.setElement(5, flag);
        m.setElement(6, cmd1);
        m.setElement(7, cmd2);
        return m;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SpecificMessage.class);

}
