package jmri.jmrix.cmri.serial;

/**
 * Contains the data payload of a CMRI serial packet.
 * <p>
 * Note that <i>only</i> the payload, not the header or trailer, nor the padding
 * DLE characters are included. These are added during transmission.
 *
 * @author Bob Jacobsen Copyright (C) 2001,2003
 */
public class SerialMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    final static int POLL_TIMEOUT = 250;

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
     *
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
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < getNumDataElements(); i++) {
            if (i != 0) {
                s.append(" ");
            }
            s.append(jmri.util.StringUtil.twoHexFromInt(getElement(i)));
        }
        return s.toString();
    }

    // static methods to recognize a message
    public boolean isPoll() {
        return getElement(1) == 0x50;
    }

    public boolean isXmt() {
        return getElement(1) == 0x54;
    }

    public boolean isInit() {
        return (getElement(1) == 0x49);
    }

    public int getUA() {
        return getElement(0) - 65;
    }

    // static methods to return a formatted message
    static public SerialMessage getPoll(int UA) {
        SerialMessage m = new SerialMessage(2);
        m.setElement(0, 65 + UA);
        m.setElement(1, 0x50); // 'P'
        m.setTimeout(POLL_TIMEOUT);    // minumum reasonable timeout
        return m;
    }

}
