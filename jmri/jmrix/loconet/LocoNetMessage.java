// LocoNetMessage.java

package jmri.jmrix.loconet;

/** 
 * Represents a single command or response on the LocoNet.
 *<P>
 * Content is represented with ints to avoid the problems with
 * sign-extension that bytes have, and because a Java char is
 * actually a variable number of bytes in Unicode.
 *<P>
 * At present, this is primarily a wrapper class for the 
 * raw message contents.  Only rudimentary tools for 
 * creating and parsing messages are provided here, in part because
 * this class was created early in the project.  For a more recent
 * implementation of a similar idea, see the NceMessage class.  Many of the
 * ideas being tested there will eventually be moved back to here.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Id: LocoNetMessage.java,v 1.1 2002-03-01 00:01:38 jacobsen Exp $
 * @see             jmri.jrmix.nce.NceMessage
 *
 */
public class LocoNetMessage {

	/** Create a new object, representing a specific-length message.
	 * @parameter len Total bytes in message, including opcode and error-detection byte.
	 */
	public LocoNetMessage(int len) {
		if (len<1)
			log.error("invalid length in call to ctor: "+len);
		_nDataBytes = len;
		_dataBytes = new int[len];
	}

	public void setOpCode(int i) { _dataBytes[0]=i;}
	public int getOpCode() {return _dataBytes[0];}

	/** Get a String representation of the op code in hex */
	public String getOpCodeHex() { return "0x"+Integer.toHexString(getOpCode()); }

	/** Get length, including op code and error-detection byte */
	public int getNumDataElements() {return _nDataBytes;}
	public int getElement(int n) {
		if (n < 0 || n >= _dataBytes.length) 
					log.error("reference element "+n
								+" in message of "+_dataBytes.length
								+" elements: "+this.toString());
		return _dataBytes[n];
	}
	public void setElement(int n, int v) { 
		if (n < 0 || n >= _dataBytes.length) 
					log.error("reference element "+n
								+" in message of "+_dataBytes.length
								+" elements: "+this.toString());
		_dataBytes[n] = v; 
	}

	/** Get a String representation of the entire message in hex */
	public String toString() {
		String s = "";
		for (int i=0; i<_nDataBytes; i++) s+=Integer.toHexString(_dataBytes[i])+" ";
		return s;
	}
	
	/**
	 * check whether the message has a valid parity
	 */
	public boolean checkParity() {
		int len = getNumDataElements();
		int chksum = 0xff;  /* the seed */
   		int loop;

    	for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
        	chksum ^= getElement(loop);
        }
		return (chksum == getElement(len-1));
	}
	
	// contents (private)
	private int _nDataBytes = 0;
	private int _dataBytes[] = null;

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoNetMessage.class.getName());

}


/* @(#)LocoNetMessage.java */
