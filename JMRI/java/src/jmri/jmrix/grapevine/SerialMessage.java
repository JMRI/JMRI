package jmri.jmrix.grapevine;

import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the data payload of a serial packet. Note that it's _only_ the
 * payload.
 * <p>
 * See the Grapevine <a href="package-summary.html">Binary Message Format Summary</a>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2006, 2007, 2008
 * @author Egbert Broerse Copyright (C) 2018
 */
public class SerialMessage extends jmri.jmrix.AbstractMRMessage {

    /**
     * Create a new SerialMessage instance.
     */
    public SerialMessage() {
        super(4); // most Grapevine messages are four bytes, binary
        setBinary(true);
    }

    /**
     * Create a new SerialMessage instance of a given byte size.
     *
     * @param len number of elements in the message
     */
    public SerialMessage(int len) {
        super(len); // most Grapevine messages are four bytes, binary
        setBinary(true);
    }

    /**
     * Copy a SerialMessage to a new instance.
     *
     * @param m the message to copy
     */
    public SerialMessage(SerialMessage m) {
        super(m);
        setBinary(true);
    }

    /**
     * Create a new Message instance from a string.
     * Interprets the String as the exact sequence to send,
     * byte-for-byte.
     *
     * @param m String to use as message content
     */
    public SerialMessage(String m) {
        super(m);
        setBinary(true);
    }

    /**
     * Interpret the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     */
    public SerialMessage(byte[] a) {
        super(String.valueOf(a));
        setBinary(true);
    }

    // no replies expected, don't wait for them
    @Override
    public boolean replyExpected() {
        return false;
    }

    // static methods to recognize a message

    public int getAddr() {
        return getElement(0) & 0x7F;
    }

    // static methods to return a formatted message

    /**
     * For Grapevine, which doesn't have a data poll, the poll operation is only
     * used to see that the nodes are present. This is done by sending a "get
     * software version" command.
     */
    static public SerialMessage getPoll(int addr) {
        // eventually this will have to include logic for reading 
        // various bytes on the card, but our supported 
        // cards don't require that yet
        SerialMessage m = new SerialMessage();
        m.setElement(0, addr | 0x80);
        m.setElement(1, 119);  // get software version
        m.setElement(2, addr | 0x80);  // read first two bytes
        m.setElement(3, 119);  // send twice, without parity
        m.setReplyLen(2);      // only two bytes come back
        return m;
    }

    public void setBank(int b) {
        if ((b > 7) || (b < 0)) {
            log.error("Setting back to bad value: " + b);
        }
        int old = getElement(3) & 0xF;
        setElement(3, old | ((b & 0x7) << 4));
    }

    public void setParity() {
        setParity(0);
    }

    public void setParity(int start) {
        // leave unchanged if poll
        if ((getElement(1 + start) == 119) && (getElement(3 + start) == 119)) {
            return;
        }
        // error messages have zero parity
        if ((getElement(0 + start) & 0x7F) == 0) {
            setElement(3, getElement(3 + start) & 0xF0);
            return;
        }
        // nibble sum method
        int sum = getElement(0 + start) & 0x0F;
        sum += (getElement(0 + start) & 0x70) >> 4;
        sum += (getElement(1 + start) * 2) & 0x0F;
        sum += ((getElement(1 + start) * 2) & 0xF0) >> 4;
        sum += (getElement(3 + start) & 0x70) >> 4;

        int parity = 16 - (sum & 0xF);

        setElement(3 + start, (getElement(3 + start) & 0xF0) | (parity & 0xF));
    }

    // default to expecting four reply characters, a standard message
    int replyLen = 4;

    /**
     * Set the number of characters expected back from the command station.
     * Normally four, this is used to set other lengths for special cases, like
     * a reply to a poll (software version) message.
     */
    public void setReplyLen(int len) {
        replyLen = len;
    }

    public int getReplyLen() {
        return replyLen;
    }

    /**
     * Format the reply as human-readable text.
     */
    public String format() {
        if (getNumDataElements() == 8) {
            String result = "(2-part) ";
            result += staticFormat(getElement(0) & 0xff, getElement(1) & 0xff, getElement(2) & 0xff, getElement(3) & 0xff);
            result += "; ";
            result += staticFormat(getElement(4) & 0xff, getElement(5) & 0xff, getElement(6) & 0xff, getElement(7) & 0xff);
            return result;
        } else {
            return staticFormat(getElement(0) & 0xff, getElement(1) & 0xff, getElement(2) & 0xff, getElement(3) & 0xff);
        }
    }

    /**
     * Provide a human-readable form of a message.
     * <p>
     * Used by both SerialMessage and SerialReply, because so much of it is
     * common. That forces the passing of arguments as numbers. Short messages
     * are marked by having missing bytes put to -1 in the arguments.
     * See the Grapevine <a href="package-summary.html">Binary Message Format Summary</a>
     */
    static String staticFormat(int b1, int b2, int b3, int b4) {
        String result;

        // short reply is special case
        if (b3 < 0) {
            return "Node " + (b1 & 0x7F) + " reports software version " + b2;
        }
        // address == 0 is a special case
        if ((b1 & 0x7F) == 0) {
            // error report
            result = "Error report from node " + b2 + ": ";
            switch (((b4 & 0x70) >> 4) - 1) {  // the -1 is an observed offset
                case 0:
                    result += "Parity Error";
                    break;
                case 1:
                    result += "First Byte Data";
                    break;
                case 2:
                    result += "Second Byte Address";
                    break;
                case 3:
                    result += "error 3";
                    break;
                case 4:
                    result += "Software UART Overflow";
                    break;
                case 5:
                    result += "Serial Detector Power Failure";
                    break;
                case 6:
                    result += "Printer Busy";
                    break;
                case 7:
                    result += "I/O Configuration Not Set";
                    break;
                default:
                    result += "error number " + ((b4 & 0x70) >> 4);
                    break;
            }
            return result;
        }

        // normal message
        result = "address: " + (b1 & 0x7F)
                + ", data bytes: 0x" + StringUtil.twoHexFromInt(b2)
                + " 0x" + StringUtil.twoHexFromInt(b4)
                + " => ";

        if ((b2 == 122) && ((b4 & 0x70) == 0x10)) {
            result += "Shift to high 24 outputs";
            return result;
        } else if ((b2 == b4) && (b2 == 0x77)) {
            result += "software version query";
            return result;
        } else if ((b2 == 0x70) && ((b4 & 0xF0) == 0x10)) {
            result += "Initialize parallel sensors";
            return result;
        } else if ((b2 == 0x71) && ((b4 & 0xF0) == 0x00)) {
            result += "Initialize ASD sensors";
            return result;
        } else // check various bank forms 
        if ((b4 & 0xF0) <= 0x30) {
            // Bank 0-3 - signal command
            result += "bank " + ((b4 & 0xF0) >> 4) + " signal " + ((b2 & 0x78) >> 3);
            int cmd = b2 & 0x07;
            result += " cmd " + cmd;
            result += " (set " + colorAsString(cmd);
            if (cmd == 0) {
                result += "/closed";
            }
            if (cmd == 6) {
                result += "/thrown";
            }
            result += ")";
            return result;
        } else if ((b4 & 0xF0) == 0x40) {
            // bank 4 - new serial sensor message
            result += "serial sensor bit " + (((b2 & 0x7E) >> 1) + 1) + " is " + (((b2 & 0x01) == 0) ? "active" : "inactive");
            return result;
        } else if ((b4 & 0xF0) == 0x50) {
            // bank 5 - sensor message
            if ((b2 & 0x20) == 0) {
                // parallel sensor
                if ((b2 & 0x40) != 0) {
                    result += "2nd connector ";
                }
                result += "parallel sensor " + ((b2 & 0x10) != 0 ? "high" : "low") + " nibble:";
            } else {
                // old serial sensor
                result += "older serial sensor " + ((b2 & 0x10) != 0 ? "high" : "low") + " nibble:";
            }
            // add bits
            result += ((b2 & 0x08) == 0) ? " A" : " I";
            result += ((b2 & 0x04) == 0) ? " A" : " I";
            result += ((b2 & 0x02) == 0) ? " A" : " I";
            result += ((b2 & 0x01) == 0) ? " A" : " I";
            return result;
        } else {
            // other banks
            return result + "bank " + ((b4 & 0xF0) >> 4) + ", unknown message";
        }
    }

    static String[] colors = new String[]{"green", "flashing green", "yellow", "flashing yellow", "off", "flashing off", "red", "flashing red"};

    static String colorAsString(int color) {
        return colors[color];
    }

    private final static Logger log = LoggerFactory.getLogger(SerialMessage.class);

}
