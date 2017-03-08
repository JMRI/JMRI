package jmri.jmrix.oaktree;

/**
 * Contains the data payload of a serial packet.
 * <P>
 * Note that <i>only</i> the payload, not the header or trailer, nor the padding
 * DLE characters are included. These are added during transmission.
 *
 * @author Bob Jacobsen Copyright (C) 2001,2003, 2006
 */
public class SerialMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    /**
     * Suppress the default ctor, as the response length must always be
     * specified
     */
    @SuppressWarnings("unused")
    private SerialMessage() {
    }

    public SerialMessage(int l) {
        super(5);  // all messages are five bytes
        setResponseLength(l);
        setBinary(true);
    }

    // copy one
    public SerialMessage(SerialMessage m, int l) {
        super(m);
        setResponseLength(l);
        setBinary(true);
    }

    /**
     * This ctor interprets the String as the exact sequence to send,
     * byte-for-byte.
     *
     */
    public SerialMessage(String m, int l) {
        super(m);
        setResponseLength(l);
        setBinary(true);
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     */
    public SerialMessage(byte[] a, int l) {
        super(String.valueOf(a));
        setResponseLength(l);
        setBinary(true);
    }

    int responseLength = -1;  // -1 is an invalid value, indicating it hasn't been set

    public void setResponseLength(int l) {
        responseLength = l;
    }

    public int getResponseLength() {
        return responseLength;
    }

    /**
     * Override parent method to ensure that message always has valid error
     * check byte
     */
    @Override
    public void setElement(int element, int value) {
        super.setElement(element, value);
        int ecb = getElement(0) ^ getElement(1) ^ getElement(2) ^ getElement(3);
        super.setElement(4, ecb);
    }

    // static methods to recognize a message
    public boolean isPoll() {
        return getElement(1) == 48;
    }

    public boolean isXmt() {
        return getElement(1) == 17;
    }

    public int getAddr() {
        return getElement(0);
    }

    // static methods to return a formatted message
    static public SerialMessage getPoll(int addr) {
        // eventually this will have to include logic for reading 
        // various bytes on the card, but our supported 
        // cards don't require that yet
        SerialMessage m = new SerialMessage(5);
        m.setElement(0, addr);
        m.setElement(1, 48);  // read processed data
        m.setElement(2, 0);  // read first two bytes
        m.setElement(3, 0);
        m.setTimeout(SHORT_TIMEOUT);    // minumum reasonable timeout
        return m;
    }

}


