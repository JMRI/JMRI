// SerialMessage.java

package jmri.jmrix.oaktree;


/**
 * Contains the data payload of a serial
 * packet.
 * <P>
 * Note that <i>only</i> the payload, not
 * the header or trailer, nor the padding DLE characters
 * are included. These are added during transmission.
 *
 * @author    Bob Jacobsen  Copyright (C) 2001,2003, 2006
 * @version   $Revision: 1.1 $
 */

public class SerialMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    public SerialMessage() {
        super(5);  // all messages are five bytes
        setBinary(true);
    }

    // copy one
    public  SerialMessage(SerialMessage m) {
        super(m);
        setBinary(true);
    }

    /**
     * This ctor interprets the String as the exact
     * sequence to send, byte-for-byte.
     * @param m
     */
    public  SerialMessage(String m) {
        super(m);
        setBinary(true);
    }

    /**
     * This ctor interprets the byte array as
     * a sequence of characters to send.
     * @param a Array of bytes to send
     */
    public  SerialMessage(byte[] a) {
        super(String.valueOf(a));
        setBinary(true);
    }

    /**
     * Override parent method to ensure that message always has
     * valid error check byte
     */
    public void setElement(int element, int value) {
        super.setElement(element, value);
        int ecb = getElement(0)^getElement(1)^getElement(2)^getElement(3);
        super.setElement(4, ecb);
    }
    
    // static methods to recognize a message
    public boolean isPoll() { return getElement(1)==48;}
    public boolean isXmt()  { return getElement(1)==0x54;}
    public boolean isInit() { return (getElement(1)==0x49); }
    public int getAddr() { return getElement(0); }

    // static methods to return a formatted message
    static public SerialMessage getPoll(int addr) {
        SerialMessage m = new SerialMessage();
        m.setElement(0, addr);
        m.setElement(1, 48);  // read formatted
        m.setElement(2, 0);  // read formatted
        m.setElement(3, 0);  
        m.setTimeout(SHORT_TIMEOUT);    // minumum reasonable timeout
        return m;
    }

}

/* @(#)SerialMessage.java */
