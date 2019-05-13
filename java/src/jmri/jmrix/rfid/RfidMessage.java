package jmri.jmrix.rfid;

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
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
abstract public class RfidMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    /**
     * Suppress the default ctor, as the length must always be specified
     */
    protected RfidMessage() {
    }

    public RfidMessage(int l) {
        super(l);
        setResponseLength(0);  // only polls require a response
        setBinary(true);
        setTimeout(5000);
    }

    /**
     * This ctor interprets the String as the exact sequence to send,
     * byte-for-byte.
     *
     * @param m String to send
     * @param l length of expected response
     */
    public RfidMessage(String m, int l) {
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
     * @param l length of expected response
     */
    public RfidMessage(byte[] a, int l) {
        super(String.valueOf(a));
        setResponseLength(l);
        setBinary(true);
        setTimeout(5000);
    }

    int responseLength = -1;  // -1 is an invalid value, indicating it hasn't been set

    /**
     * Sets the length of an expected response
     *
     * @param l length of expected response
     */
    public final void setResponseLength(int l) {
        responseLength = l;
    }

    /**
     * Returns the length of an expected response
     *
     * @return length of expected response
     */
    public int getResponseLength() {
        return responseLength;
    }

}
