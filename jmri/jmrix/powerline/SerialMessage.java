// SerialMessage.java

package jmri.jmrix.powerline;


/**
 * Contains the data payload of a serial
 * packet.
 * <P>
 * The transmission protocol can come in one of several forms:
 * <ul>
 * <li>If the interlocked parameter is false (default),
 * the packet is just sent.  If the response length is not zero,
 * a reply of that length is expected.
 * <li>If the interlocked parameter is true, the transmission
 * will require a CRC interlock, which will be automatically added.
 * (Design note: this is done to make sure that the messages
 * remain atomic)
 * </ul>
 *
 * @author    Bob Jacobsen  Copyright (C) 2001,2003, 2006, 2007, 2008
 * @version   $Revision: 1.5 $
 */

public class SerialMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    /** Suppress the default ctor, as the
     * length must always be specified
     */
    private SerialMessage() {}
    
    public SerialMessage(int l) {
        super(l);
        setResponseLength(0);  // only polls require a response
        setBinary(true);
    }

    /**
     * This ctor interprets the String as the exact
     * sequence to send, byte-for-byte.
     * @param m
     */
    public  SerialMessage(String m,int l) {
        super(m);
        setResponseLength(l);
        setBinary(true);
    }

    boolean interlocked = false;
    public void setInterlocked(boolean v) { interlocked = v; }
    public boolean getInterlocked() { return interlocked; }
    
    /**
     * This ctor interprets the byte array as
     * a sequence of characters to send.
     * @param a Array of bytes to send
     */
    public  SerialMessage(byte[] a, int l) {
        super(String.valueOf(a));
        setResponseLength(l);
        setBinary(true);
    }

    int responseLength = -1;  // -1 is an invalid value, indicating it hasn't been set
    public void setResponseLength(int l) { responseLength = l; }
    public int getResponseLength() { return responseLength; }
        
    // static methods to recognize a message
    public boolean isPoll() { return getElement(1)==48;}
    public boolean isXmt()  { return getElement(1)==17;}
    public int getAddr() { return getElement(0); }

    // static methods to return a formatted message
    static public SerialMessage getPoll(int addr) {
        // eventually this will have to include logic for reading 
        // various bytes on the card, but our supported 
        // cards don't require that yet
        // SerialMessage m = new SerialMessage(1);
        // m.setResponseLength(2);
        // m.setElement(0, addr);
        //  m.setTimeout(SHORT_TIMEOUT);    // minumum reasonable timeout
        
        // Powerline implementation does not currently poll
        return null;
    }
    static public SerialMessage setCM11Time(int housecode) {
        SerialMessage msg = new SerialMessage(7);
        msg.setElement(0, 0x9B);
        msg.setElement(5, 0x01);
        msg.setElement(6, housecode<<4);
        return msg;
    }
    static public SerialMessage getAddress(int housecode, int devicecode) {
        SerialMessage m = new SerialMessage(2);
        m.setInterlocked(true);
        m.setElement(0,0x04);
        m.setElement(1,(X10.encode(housecode)<<4)+X10.encode(devicecode));
        return m;
    }
    static public SerialMessage getFunctionDim(int housecode, int function, int dimcode) {
        SerialMessage m = new SerialMessage(2);
        m.setInterlocked(true);
        if (dimcode > 0) {
        	m.setElement(0, 0x06 | ((dimcode & 0x1f) << 3));
        } else {
        	m.setElement(0, 0x06);
        }
        m.setElement(1,(X10.encode(housecode)<<4)+function);
        return m;
    }
    static public SerialMessage getFunction(int housecode, int function) {
        SerialMessage m = new SerialMessage(2);
        m.setInterlocked(true);
        m.setElement(0,0x06);
        m.setElement(1,(X10.encode(housecode)<<4)+function);
        //System.out.println("gf "+housecode+" "+X10.encode(housecode));
        return m;
    }
}

/* @(#)SerialMessage.java */
