// SpecificMessage.java

package jmri.jmrix.powerline.insteon2412s;

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
 * @author    Bob Jacobsen  Copyright (C) 2001,2003, 2006, 2007, 2008, 2009
 * @version   $Revision: 1.2 $
 */

public class SpecificMessage extends SerialMessage {
    // is this logically an abstract class?

    /** Suppress the default ctor, as the
     * length must always be specified
     */
    @SuppressWarnings("unused")
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

    boolean interlocked = false;
    public void setInterlocked(boolean v) { interlocked = v; }
    public boolean getInterlocked() { return interlocked; }
    
    @SuppressWarnings("fallthrough")
	public String toMonitorString() {
        // check for valid length
        int len = getNumDataElements();
        String text;
        switch (getElement(0)&0xFF) {
            case 0xFB : text = "Macro load reply"; break;
            case 0x9B : text = "Set CM11 time"; break;
            case 0xC3 : if (len == 1) {
            		text = "Poll Ack"; break;
            	} // else fall through
            case 0x00 : if (len == 1) {
                    text = "OK for transmission"; break;
                } // else fall through
            default: {
            	if (len == 4) {
                    if (((getElement(0)&0xFF) == 0x02) && ((getElement(1)&0xFF) == 0x63)) {
                        if ((getElement(3)&0xFF) == 0x80) {
                    	    text = Constants.formatHeaderByte(getElement(2) & 0xFF)
                    		    + ' ' + X10Sequence.formatCommandByte(getElement(2)&0xFF);
                        } else {
                    	    text = Constants.formatHeaderByte(getElement(2) & 0xFF)
                		    + ' ' + X10Sequence.formatAddressByte(getElement(2)&0xFF);
                        }
                    } else {
                        text = "Really in trouble now";
                    }
            	} else {
            		text = "Reply was short, len: " + len + " value: " + Constants.formatHeaderByte(getElement(0 & 0xFF));
            	}
            }
        }
        return text+"\n";
    }
    
    /**
     * This ctor interprets the byte array as
     * a sequence of characters to send.
     * @param a Array of bytes to send
     */
    public  SpecificMessage(byte[] a, int l) {
        super(a, l);
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
    static public SpecificMessage setCM11Time(int housecode) {
        SpecificMessage msg = new SpecificMessage(7);
        msg.setElement(0, 0x9B);
        msg.setElement(5, 0x01);
        msg.setElement(6, housecode<<4);
        return msg;
    }
    static public SpecificMessage getAddress(int housecode, int devicecode) {
        SpecificMessage m = new SpecificMessage(4);
//        m.setInterlocked(true);
        m.setInterlocked(false);
        m.setElement(0,0x02);
        m.setElement(1,0x63);
        m.setElement(2,(X10Sequence.encode(housecode)<<4)+X10Sequence.encode(devicecode));
        m.setElement(3,0x00);  //  0x00 Means address
        return m;
    }
    static public SpecificMessage getInsteonAddress(String address) {
        SpecificMessage m = new SpecificMessage(8);
//        m.setInterlocked(true);
        m.setInterlocked(false);
        m.setElement(0,0x02);
        m.setElement(1,0x62);
        m.setElement(2,0x12);
        m.setElement(3,0x48);
        m.setElement(4,0xF4);
        m.setElement(5,0x0F);
        m.setElement(6,0x11);
        m.setElement(7,0xFF);
        return m;
    }
    static public SpecificMessage getAddressDim(int housecode, int devicecode, int dimcode) {
        SpecificMessage m = new SpecificMessage(4);
        m.setInterlocked(true);
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
        m.setInterlocked(true);
        if (dimcode > 0) {
        	m.setElement(0, 0x06 | ((dimcode & 0x1f) << 3));
        } else {
        	m.setElement(0, 0x06);
        }
        m.setElement(1,(X10Sequence.encode(housecode)<<4)+function);
        return m;
    }
    static public SpecificMessage getFunction(int housecode, int function) {
        SpecificMessage m = new SpecificMessage(4);
//        m.setInterlocked(true);
        m.setInterlocked(false);
        m.setElement(0,0x02);
        m.setElement(1,0x63);
        m.setElement(2,(X10Sequence.encode(housecode)<<4)+function);
        m.setElement(3,0x80);  //  0x80 means function
        return m;
    }
    static public SpecificMessage getInsteonFunction(String address, int function) {
        String b0 = address.substring(0,2);
        String b1 = address.substring(2,4);
        String b2 = address.substring(4,6);
        SpecificMessage m = new SpecificMessage(8);
//        m.setInterlocked(true);
        m.setInterlocked(false);
        m.setElement(0,0x02);
        m.setElement(1,0x62);
        m.setElement(2,Integer.parseInt(b0, 16));
        m.setElement(3,Integer.parseInt(b1, 16));
        m.setElement(4,Integer.parseInt(b2, 16));
        m.setElement(5,0x0F);
        if (function == 2) {
            m.setElement(6,0x11);
            m.setElement(7,0xFF);
        } else {
            m.setElement(6,0x13);
            m.setElement(7,0x00);
        }
        return m;
    }
    static public SpecificMessage getInsteonFunctionDim(String address, int function, int dimcode) {
        String b0 = address.substring(0,2);
        String b1 = address.substring(2,4);
        String b2 = address.substring(4,6);
        SpecificMessage m = new SpecificMessage(8);
//        m.setInterlocked(true);
        m.setInterlocked(false);
        m.setElement(0,0x02);
        m.setElement(1,0x62);
        m.setElement(2,Integer.parseInt(b0, 16));
        m.setElement(3,Integer.parseInt(b1, 16));
        m.setElement(4,Integer.parseInt(b2, 16));
        m.setElement(5,0x0F);
        if (function == 2) {
            m.setElement(6,0x11);
        } else {
            m.setElement(6,0x13);
        }
        if (dimcode > 0) {
        	m.setElement(7, 0x50);
        } else {
        	m.setElement(7, 0x00);
        }
        return m;
    }
}

/* @(#)SpecificMessage.java */
