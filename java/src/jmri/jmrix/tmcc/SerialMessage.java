package jmri.jmrix.tmcc;

import jmri.util.StringUtil;

/**
 * Contains the data payload of a TMCC serial packet.
 * <p>
 * Note that <i>only</i> the payload, not the header or trailer, nor the padding
 * DLE characters are included. These are added during transmission.
 *
 * @author Bob Jacobsen Copyright (C) 2001,2003, 2006
 */
public class SerialMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    public SerialMessage() {
        super(3);
        setOpCode(0xFE);
        setTimeout(100);
    }

    // copy one
    public SerialMessage(SerialMessage m) {
        super(m);
        setTimeout(100);
    }

    /**
     * This ctor interprets the String as the exact sequence to send,
     * byte-for-byte.
     *
     * @param m string form of bytes to send
     *
     */
    public SerialMessage(String m) {
        super(m);
        setTimeout(100);
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     */
    public SerialMessage(byte[] a) {
        super(String.valueOf(a));
        setTimeout(100);
    }

    /**
     * This ctor takes an int value for the 16 bit data content
     *
     * @param value The value stored in the content of the packet
     */
    public SerialMessage(int value) {
        super(3);
        setOpCode(0xFE);
        putAsWord(value);
        setTimeout(100);
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

    public void putAsWord(int val) {
        setElement(1, (val / 256) & 0xFF);
        setElement(2, val & 0xFF);
    }

    public int getAsWord() {
        return (getElement(1) & 0xFF) * 256 + (getElement(2) & 0xFF);
    }
}
