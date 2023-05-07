package jmri.jmrix.powerline.dmx512;

import jmri.jmrix.powerline.SerialMessage;

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
 * @author Ken Cameron Copyright (C) 2023
 */
public class SpecificMessage extends SerialMessage {
    // is this logically an abstract class?

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
        StringBuilder text = new StringBuilder();
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

}


