/** 
 * NceMessage.java
 *
 * Description:		<describe the NceMessage class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce;

public class NceMessage {
	// is this logically an abstract class?

	// create a new one
	public  NceMessage(int i) {
		if (i<1)
			log.error("invalid length in call to ctor");
		_nDataChars = i;
		_dataChars = new char[i];
	}

	public void setOpCode(char i) { _dataChars[0]=i;}
	public char getOpCode() {return _dataChars[0];}
	public String getOpCodeHex() { return "0x"+Integer.toHexString(getOpCode()); }

	// accessors to the bulk data
	public int getNumDataElements() {return _nDataChars;}
	public int getElement(int n) {return _dataChars[n];}
	public void setElement(int n, char v) { _dataChars[n] = v; }

	// display format
	public String toString() {
		String s = "";
		for (int i=0; i<_nDataChars; i++) s+=_dataChars[i];
		return s;
	}
	
	// contents (private)
	private int _nDataChars = 0;
	private char _dataChars[] = null;

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMessage.class.getName());

}


/* @(#)NceMessage.java */
