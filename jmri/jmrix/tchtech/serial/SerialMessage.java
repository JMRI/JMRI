/*
 * SerialMessage.java
 *
 * Created on August 17, 2007, 6:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial;

/**
 *
 * @author Tim Hatch
 */
/**
 * Contains the data payload of a TCH Technology serial
 * packet.
 * <P>
 * Note that <i>only</i> the payload, not
 * the header or trailer, nor the padding DLE characters
 * are included. These are added during transmission.
 *
 * @author    Bob Jacobsen  Copyright (C) 2001,2003
 * @version   $Revision: 1.1 $
 */

public class SerialMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    final static int POLL_TIMEOUT = 250;
    
    public SerialMessage() {
        super();
    }

    // create a new one
    public  SerialMessage(int i) {
        super(i);
    }

    // copy one
    public  SerialMessage(SerialMessage m) {
        super(m);
    }

    /**
     * This ctor interprets the String as the exact
     * sequence to send, byte-for-byte.
     * @param m
     */
    public  SerialMessage(String m) {
        super(m);
    }

    /**
     * This ctor interprets the byte array as
     * a sequence of characters to send.
     * @param a Array of bytes to send
     */
    public  SerialMessage(byte[] a) {
        super(String.valueOf(a));
    }

    // display format
    public String toString() {
        String s = "";
        for (int i=0; i<getNumDataElements(); i++) {
            if (i!=0) s+=" ";
            s+=jmri.util.StringUtil.twoHexFromInt(getElement(i));
        }
        return s;
    }

    // static methods to recognize a message
    public boolean isErr() { return getElement(1)==0x45;}
    public boolean isInq() { return getElement(1)==0x2D;}
    public boolean isPoll() { return getElement(1)==0xD7;}
    public boolean isXmt()  { return getElement(1)==0x53;}
    public boolean isInit() { return (getElement(1)==0xC9);}
    public int getNA() { return getElement(0); }

    // static methods to return a formatted message
    static public SerialMessage getPoll(int Addr) {
        SerialMessage m = new SerialMessage(2);
        m.setElement(0, Addr);
        m.setElement(1, 0xD7); //0xD7 ext 'P'
        m.setTimeout(POLL_TIMEOUT);    // minumum reasonable timeout
        return m;
    }

}

/* @(#)SerialMessage.java */
