package jmri.jmrix.maple;

import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the data payload of a serial packet.
 * <p>
 * Note that <i>only</i> the payload, not the header or trailer, nor the padding
 * DLE characters are included. These are added during transmission.
 *
 * @author Bob Jacobsen Copyright (C) 2001,2003
 */
public class SerialMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    public SerialMessage() {
        super();
    }

    // create a new one
    public SerialMessage(int i) {
        super(i);
    }

    // copy one
    public SerialMessage(SerialMessage m) {
        super(m);
    }

    /**
     * This ctor interprets the String as the exact sequence to send,
     * byte-for-byte.
     */
    public SerialMessage(String m) {
        super(m);
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     */
    public SerialMessage(byte[] a) {
        super(String.valueOf(a));
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("");
        for (int i = 0; i < getNumDataElements(); i++) {
            if (i != 0) {
                s.append(" ");
            }
            s.append(StringUtil.twoHexFromInt(getElement(i)));
        }
        return s.toString();
    }

    // control when reply is expected
    private boolean _replyExpected = true;

    protected void setNoReply() {
        _replyExpected = false;
    }

    @Override
    public boolean replyExpected() {
        return _replyExpected;
    }

    // static methods to recognize a message
    public boolean isPoll() {
        return getElement(3) == 'R' && getElement(4) == 'C';
    }

    public boolean isXmt() {
        return getElement(3) == 'W' && getElement(4) == 'C';
    }

    public boolean isInit() {
        return (false);
    }  // initialization is not used in Maple

    public int getUA() {
        return ((getElement(1) - '0') * 10) + (getElement(2) - '0');
    }

    public int getAddress() {
        return (((getElement(5) - '0') * 1000) + ((getElement(6) - '0') * 100) + ((getElement(7) - '0') * 10) + (getElement(8) - '0'));
    }

    public int getNumItems() {
        return ((getElement(9) - '0') * 10) + (getElement(10) - '0');
    }

    // static methods to return a formatted message

    static public SerialMessage getPoll(int UA, int startAdd, int count) {
        if ((count <= 0) || (count > 99)) {
            log.error("Illegal count in Maple poll message - {}", count);
            return null;
        }
        SerialMessage m = new SerialMessage(14);
        m.setElement(0, 02);
        m.setElement(1, '0' + (UA / 10));
        m.setElement(2, '0' + (UA - ((UA / 10) * 10)));
        m.setElement(3, 'R');
        m.setElement(4, 'C');
        m.setElement(5, '0' + (startAdd / 1000));    // read starting at 0001
        m.setElement(6, '0' + ((startAdd - ((startAdd / 1000) * 1000)) / 100));
        m.setElement(7, '0' + ((startAdd - ((startAdd / 100) * 100)) / 10));
        m.setElement(8, '0' + (startAdd - ((startAdd / 10) * 10)));
        m.setElement(9, '0' + (count / 10));
        m.setElement(10, '0' + (count - ((count / 10) * 10)));
        m.setElement(11, 03);

        m.setChecksum(12);

        m.setTimeout(InputBits.getTimeoutTime());
        return m;
    }

    void setChecksum(int index) {
        int sum = 0;
        for (int i = 1; i < index; i++) {
            sum += getElement(i);
        }
        sum = sum & 0xFF;

        char firstChar;
        int firstVal = (sum / 16) & 0xF;
        if (firstVal > 9) {
            firstChar = (char) ('A' - 10 + firstVal);
        } else {
            firstChar = (char) ('0' + firstVal);
        }
        setElement(index, firstChar);

        char secondChar;
        int secondVal = sum & 0xf;
        if (secondVal > 9) {
            secondChar = (char) ('A' - 10 + secondVal);
        } else {
            secondChar = (char) ('0' + secondVal);
        }
        setElement(index + 1, secondChar);
    }

//    public int maxSize() {
//        return DEFAULTMAXSIZE;
//    }
//    static public final int DEFAULTMAXSIZE = 404; // Maple RR Request Docs page 9

    private final static Logger log = LoggerFactory.getLogger(SerialMessage.class);

}
