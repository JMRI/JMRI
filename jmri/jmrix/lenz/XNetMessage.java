// XNetMessage.java

package jmri.jmrix.lenz;

import java.io.Serializable;

/**
 * Represents a single command or response on the XpressNet.
 *<P>
 * Content is represented with ints to avoid the problems with
 * sign-extension that bytes have, and because a Java char is
 * actually a variable number of bytes in Unicode.
 *
 * @author			Bob Jacobsen  Copyright (C) 2002
 * @version			$Revision: 2.2 $
 *
 */
public class XNetMessage extends jmri.jmrix.AbstractMRMessage implements Serializable {


	int _nDataChars = 0;
	/** Create a new object, representing a specific-length message.
	 * @param len Total bytes in message, including opcode and error-detection byte.
	 */
	public XNetMessage(int len) {
        super(len);
	setBinary(true);
        if (len>15||len<0) log.error("Invalid length in ctor: "+len);
	_nDataChars=len;
	}

	/** Create a new object, that is a copy of an existing message.
	 * @param message existing message.
	 */
	public XNetMessage(XNetMessage message) {
           super(message);
	   setBinary(true);
	}

	/**
	 * Create an XNetMessage from an XNetReply.
    	 */
	public XNetMessage(XNetReply message) {
	    super(message.getNumDataElements());                            
       	    setBinary(true);
            for(int i=0;i<message.getNumDataElements();i++)
       		{
          	   setElement(i,message.getElement(i));
       		}
    	}

    // note that the opcode is part of the message, so we treat it
    // directly
    // WARNING: use this only with opcodes that have the number of
    // arguments following included. Otherwise, just use setElement
	public void setOpCode(int i) {
        if (i>0xF || i<0) {
            log.error("Opcode invalid: "+i);
        }
        setElement(0,((i*16)&0xF0)|((getNumDataElements()-2)&0xF));
    }

	public int getOpCode() {return (getElement(0)/16)&0xF;}

	/** Get a String representation of the op code in hex */
	public String getOpCodeHex() { return "0x"+Integer.toHexString(getOpCode()); }

	/**
	 * check whether the message has a valid parity
	 */
	public boolean checkParity() {
		int len = getNumDataElements();
		int chksum = 0x00;  /* the seed */
   		int loop;

    	for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
        	chksum ^= getElement(loop);
        }
		return ((chksum&0xFF) == getElement(len-1));
	}

    public void setParity() {
		int len = getNumDataElements();
		int chksum = 0x00;  /* the seed */
   		int loop;

    	for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
        	chksum ^= getElement(loop);
        }
		setElement(len-1, chksum&0xFF);
    }

    /** Get an integer representation of a BCD value */
	public Integer getElementBCD(int n) { return Integer.decode(Integer.toHexString(getElement(n))); }

    /**   
      * return the message length 
      */
      public int length() { return _nDataChars; }

    // decode messages of a particular form

    // create messages of a particular form

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetMessage.class.getName());

}

/* @(#)XNetMessage.java */
