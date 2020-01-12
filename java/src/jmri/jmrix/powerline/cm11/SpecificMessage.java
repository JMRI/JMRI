package jmri.jmrix.powerline.cm11;

import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.X10Sequence;
import jmri.util.StringUtil;

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
 * @author Bob Jacobsen Copyright (C) 2001,2003, 2006, 2007, 2008
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

    @SuppressWarnings("fallthrough")
    @Override
    public String toMonitorString() {
        // check for valid length
        int len = getNumDataElements();
        StringBuilder text = new StringBuilder();
        switch (getElement(0) & 0xFF) {
            case Constants.MACRO_LOAD:
                text.append("Macro load reply");
                break;
            case Constants.MACRO_INITIATED:
                text.append("Macro Poll");
                break;
            case Constants.TIME_REQ_CP11:
                text.append("Power Fail Poll");
                break;
            case Constants.TIMER_DOWNLOAD:
                text.append("Set CM11 time");
                break;
            case Constants.EXT_CMD_HEADER: // extended command
                text.append("Extended Cmd");
                if (len == 5) {
                    text.append(" house ");
                    text.append(X10Sequence.houseValueToText(X10Sequence.decode((getElement(1) >> 4) & 0x0F)));
                    text.append(" address device ");
                    text.append(X10Sequence.decode(getElement(2) & 0x0F));
                    int d = getElement(3) & 0xFF;
                    switch (getElement(4) & 0xFF) {
                        case X10Sequence.EXTCMD_DIM:
                            text.append(" Direct Dim: ");
                            if ((d & 0x3F) <= 0x3E) {
                                text.append(((d & 0x3F) / 0.63) + "%");
                            } else if (d == 0x3F) {
                                text.append("Full On");
                            } else {
                                text.append(" data: 0x");
                                text.append(StringUtil.twoHexFromInt(d));
                            }
//               switch ((d >> 6) & 0x03) {
//               case 0:
//                text.append(" 3.7 Sec");
//                break;
//               case 1:
//                text.append(" 30 Sec");
//                break;
//               case 2:
//                text.append(" 1 Min");
//                break;
//               case 3:
//                text.append(" 5 Min");
//                break;
//               }
                            break;
                        default:
                            text.append(" cmd: 0x");
                            text.append(StringUtil.twoHexFromInt(getElement(4) & 0xFF));
                            text.append(" data: 0x");
                            text.append(StringUtil.twoHexFromInt(getElement(3) & 0xFF));
                    }
                } else {
                    text.append(" wrong length: " + len);
                }
                break;
            case Constants.POLL_ACK:
                if (len == 1) {
                    text.append("Poll Ack");
                    break;
                } // else fall through
            case Constants.CHECKSUM_OK:
                if (len == 1) {
                    text.append("OK for transmission");
                    break;
                } // else fall through
            default: {
                if (len == 2) {
                    text.append(Constants.formatHeaderByte(getElement(0 & 0xFF)));
                    if ((getElement(0) & 0x02) == 0x02) {
                        text.append(" ");
                        text.append(X10Sequence.formatCommandByte(getElement(1) & 0xFF));
                    } else {
                        text.append(" ");
                        text.append(X10Sequence.formatAddressByte(getElement(1) & 0xFF));
                    }
                } else {
                    text.append("Reply was not expected, len: " + len);
                    text.append(" value: " + Constants.formatHeaderByte(getElement(0 & 0xFF)));
                }
            }
        }
        return text + "\n";
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     * @param l lenght of expected reply
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
    @Override
    public boolean isPoll() {
        return getElement(1) == 48;
    }

    @Override
    public boolean isXmt() {
        return getElement(1) == 17;
    }

    @Override
    public int getAddr() {
        return getElement(0);
    }

    // static methods to return a formatted message
    static public SerialMessage getPoll(int addr) {
        // eventually this will have to include logic for reading 
        // various bytes on the card, but our supported 
        // cards don't require that yet
        // SerialMessage m = new SerialMessage(1);
        // m.setResponseLength(2);
        // m.setElement(0, addr);
        //  m.setTimeout(SHORT_TIMEOUT);    // minumum reasonable timeout

        // Powerline implementation does not currently poll
        return null;
    }

    static public SpecificMessage setCM11Time(int housecode) {
        SpecificMessage msg = new SpecificMessage(7);
        msg.setElement(0, 0x9B);
        msg.setElement(5, 0x01);
        msg.setElement(6, housecode << 4);
        return msg;
    }

    static public SpecificMessage getAddress(int housecode, int devicecode) {
        SpecificMessage m = new SpecificMessage(2);
        m.setInterlocked(true);
        m.setElement(0, 0x04);
        m.setElement(1, (X10Sequence.encode(housecode) << 4) + X10Sequence.encode(devicecode));
        return m;
    }

    static public SpecificMessage getAddressDim(int housecode, int devicecode, int dimcode) {
        SpecificMessage m = new SpecificMessage(2);
        m.setInterlocked(true);
        if (dimcode > 0) {
            m.setElement(0, 0x04 | ((dimcode & 0x1f) << 3));
        } else {
            m.setElement(0, 0x04);
        }
        m.setElement(1, (X10Sequence.encode(housecode) << 4) + X10Sequence.encode(devicecode));
        return m;
    }

    static public SpecificMessage getFunctionDim(int housecode, int function, int dimcode) {
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

    static public SpecificMessage getFunction(int housecode, int function) {
        SpecificMessage m = new SpecificMessage(2);
        m.setInterlocked(true);
        m.setElement(0, 0x06);
        m.setElement(1, (X10Sequence.encode(housecode) << 4) + function);
        return m;
    }

    static public SpecificMessage getExtCmd(int housecode, int devicecode, int function, int dimcode) {
        SpecificMessage m = new SpecificMessage(5);
        m.setInterlocked(true);
        m.setElement(0, 0x07);
        m.setElement(1, (X10Sequence.encode(housecode) << 4) + X10Sequence.FUNCTION_EXTENDED_CODE);
        m.setElement(2, X10Sequence.encode(devicecode));
        m.setElement(3, dimcode);
        m.setElement(4, function);
        return m;
    }
}


