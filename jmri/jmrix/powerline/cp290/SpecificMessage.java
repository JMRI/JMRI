// SpecificMessage.java

package jmri.jmrix.powerline.cp290;

import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.X10Sequence;

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
 * @version   $Revision: 1.3 $
 */

public class SpecificMessage extends SerialMessage {
    // is this logically an abstract class?

    /** Suppress the default ctor, as the
     * length must always be specified
     */
    private SpecificMessage() {}
    
    public SpecificMessage(int l) {
        super(l);
        setResponseLength(0);  // only polls require a response
        setBinary(true);
        setTimeout(5000);
    }

    /**
     * This ctor interprets the String as the exact
     * sequence to send, byte-for-byte.
     * @param m message
     * @param l response length in bytes
     */
    public  SpecificMessage(String m,int l) {
        super(m,l);
    }

    /**
     * This ctor interprets the byte array as
     * a sequence of characters to send.
     * @param a Array of bytes to send
     */
    public  SpecificMessage(byte[] a, int l) {
        super(a, l);
    }

    /**
     * Find 1st byte that's not 0xFF, or -1 if none
     */
    int startIndex() {
        int len = getNumDataElements();
        for (int i = 0; i<len; i++) {
            if ( (getElement(i)&0xFF) != 0xFF ) return i;
        }
        return -1;
    }
    
    public String toMonitorString() {
        return toString()+"\n";
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
    static public SpecificMessage getAddress(int housecode, int devicecode) {
        SpecificMessage m = new SpecificMessage(2);
        m.setElement(0,0x04);
        m.setElement(1,(X10Sequence.encode(housecode)<<4)+X10Sequence.encode(devicecode));
        return m;
    }
    static public SpecificMessage getAddressDim(int housecode, int devicecode, int dimcode) {
        SpecificMessage m = new SpecificMessage(2);
        if (dimcode > 0) {
        	m.setElement(0, 0x04 | ((dimcode & 0x1f) << 3));
        } else {
        	m.setElement(0, 0x04);
        }
        m.setElement(1,(X10Sequence.encode(housecode)<<4)+X10Sequence.encode(devicecode));
        return m;
    }
    static public SpecificMessage getFunctionDim(int housecode, int function, int dimcode) {
        SpecificMessage m = new SpecificMessage(2);
        if (dimcode > 0) {
        	m.setElement(0, 0x06 | ((dimcode & 0x1f) << 3));
        } else {
        	m.setElement(0, 0x06);
        }
        m.setElement(1,(X10Sequence.encode(housecode)<<4)+function);
        return m;
    }
    static public SpecificMessage getFunction(int housecode, int function) {
        SpecificMessage m = new SpecificMessage(2);
        m.setElement(0,0x06);
        m.setElement(1,(X10Sequence.encode(housecode)<<4)+function);
        return m;
    }
}

/* @(#)SpecificMessage.java */
