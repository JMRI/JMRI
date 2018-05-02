package jmri.jmrix.secsi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the data payload of a serial packet.
 * <p>
 * Note that <i>only</i> the payload, not the header or trailer, nor the padding
 * DLE characters are included. These are added during transmission.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2006, 2007, 2008
 */
public class SerialMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    /**
     * Suppress the default ctor, as the response length must always be
     * specified.
     */
    @SuppressWarnings("unused")
    private SerialMessage() {
    }

    public SerialMessage(int l) {
        super(l);
        setResponseLength(0);  // only polls require a response
        setBinary(true);
        log.debug("secsi message generated");
    }

    /**
     * This ctor interprets the String as the exact sequence to send,
     * byte-for-byte.
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
        SerialMessage m = new SerialMessage(1);
        m.setResponseLength(2);
        m.setElement(0, addr);
        m.setTimeout(SHORT_TIMEOUT);    // minumum reasonable timeout
        log.debug("poll message generated");
        return m;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialMessage.class);

}
