package jmri.jmrix.powerline;

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
 * @author Bob Jacobsen Copyright (C) 2001,2003, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
abstract public class SerialMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    /**
     * Suppress the default ctor, as the length must always be specified
     */
    protected SerialMessage() {
    }

    public SerialMessage(int l) {
        super(l);
        setResponseLength(0);  // only polls require a response
        setBinary(true);
        setTimeout(5000);
    }

    /**
     * This ctor interprets the String as the exact sequence to send,
     * byte-for-byte.
     * @param m sequence to send
     * @param l expected reply length
     *
     */
    public SerialMessage(String m, int l) {
        super(m);
        setResponseLength(l);
        setBinary(true);
        setTimeout(5000);
    }

    boolean interlocked = false;

    public void setInterlocked(boolean v) {
        interlocked = v;
    }

    public boolean getInterlocked() {
        return interlocked;
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     * @param l expected reply length
     */
    public SerialMessage(byte[] a, int l) {
        super(String.valueOf(a));
        setResponseLength(l);
        setBinary(true);
        setTimeout(5000);
    }

    int responseLength = -1;  // -1 is an invalid value, indicating it hasn't been set

    public void setResponseLength(int l) {
        responseLength = l;
    }

    public int getResponseLength() {
        return responseLength;
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
        // Powerline implementation does not currently poll
        return null;
    }
}


