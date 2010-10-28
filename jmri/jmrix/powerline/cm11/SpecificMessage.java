// SpecificMessage.java

package jmri.jmrix.powerline.cm11;

import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.X10Sequence;
import jmri.util.StringUtil;

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
 * @version   $Revision: 1.4 $
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
        StringBuilder text = new StringBuilder();
        switch (getElement(0)&0xFF) {
            case 0xFB : text.append("Macro load reply"); break;
            case 0x9B : text.append("Set CM11 time"); break;
            case 0xC3 : if (len == 1) {
            		text.append("Poll Ack"); break;
            	} // else fall through
            case 0x00 : if (len == 1) {
                    text.append("OK for transmission"); break;
                } // else fall through
            default: {
            	if ((len == 2) || (len == 5)) {
                	text.append(Constants.formatHeaderByte(getElement(0 & 0xFF)));
                    if ((getElement(0)& 0x02) == 0x02) {
                    	text.append(" ");
                    	text.append(X10Sequence.formatCommandByte(getElement(1) & 0xFF));
                    } else {
                    	text.append(" ");
                		text.append(X10Sequence.formatAddressByte(getElement(1) & 0xFF));
                    }
                    if (len == 5) {
                    	text.append(" ");
                		text.append(X10Sequence.formatAddressByte(getElement(2) & 0xFF));
                    	text.append(" cmd: 0x");
                    	text.append(StringUtil.twoHexFromInt(getElement(3) & 0xFF));
                    	text.append(" data: 0x");
                    	text.append(StringUtil.twoHexFromInt(getElement(4) & 0xFF));
                    }
            	} else {
            		text.append("Reply was not expected, len: " + len);
            		text.append(" value: " + Constants.formatHeaderByte(getElement(0 & 0xFF)));
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
        SpecificMessage m = new SpecificMessage(2);
        m.setInterlocked(true);
        m.setElement(0,0x04);
        m.setElement(1,(X10Sequence.encode(housecode)<<4)+X10Sequence.encode(devicecode));
        return m;
    }
    static public SpecificMessage getAddressDim(int housecode, int devicecode, int dimcode) {
        SpecificMessage m = new SpecificMessage(2);
        m.setInterlocked(true);
        if (dimcode > 0) {
        	m.setElement(0, 0x04 | ((dimcode & 0x1f) << 3));
        } else {
        	m.setElement(0, 0x04);
        }
        m.setElement(1,(X10Sequence.encode(housecode)<<4) + X10Sequence.encode(devicecode));
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
        m.setElement(1,(X10Sequence.encode(housecode) << 4) + function);
        return m;
    }
    static public SpecificMessage getFunction(int housecode, int function) {
        SpecificMessage m = new SpecificMessage(2);
        m.setInterlocked(true);
        m.setElement(0,0x06);
        m.setElement(1,(X10Sequence.encode(housecode) << 4) + function);
        return m;
    }
    static public SpecificMessage getExtCmd(int housecode, int devicecode, int function, int dimcode) {
        SpecificMessage m = new SpecificMessage(5);
        m.setInterlocked(true);
        m.setElement(0, 0x07);
        m.setElement(1,(X10Sequence.encode(housecode)<<4) + X10Sequence.FUNCTION_EXTENDED_CODE);
        m.setElement(2,X10Sequence.encode(devicecode));
        m.setElement(4, function);
        m.setElement(3, dimcode);
        return m;
    }
}

/* @(#)SpecificMessage.java */
