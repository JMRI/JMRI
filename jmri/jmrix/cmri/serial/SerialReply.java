/**
 * SerialReply.java
 *
 * Description:		Contains the data payload of a CMRI serial reply
 *                  packet.  Note that _only_ the payload, not
 *                  the header or trailer, nor the padding DLE characters
 *                  are included. But it does include addressing characters,
 *                  etc.
 * @author			Bob Jacobsen  Copyright (C) 2002
 * @version         $Id: SerialReply.java,v 1.2 2002-03-11 04:36:24 jacobsen Exp $
 */

package jmri.jmrix.cmri.serial;


// Note:  This handles the "serial" CMRI

public class SerialReply {
	// is this logically an abstract class?

	// create a new one
	public  SerialReply() {
	}

	// copy one
	public  SerialReply(SerialReply m) {
		if (m == null)
			log.error("copy ctor of null message");
		_nDataChars = m._nDataChars;
		for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
	}

	// from String
	public SerialReply(String s) {
		_nDataChars = s.length();
		for (int i = 0; i<_nDataChars; i++)
			_dataChars[i] = s.charAt(i);
	}

	// accessors to the bulk data
	public int getNumDataElements() {return _nDataChars;}
	public int getElement(int n) {return _dataChars[n];}
	public void setElement(int n, int v) {
		_dataChars[n] = (char) v;
		_nDataChars = Math.max(_nDataChars, n+1);
	}

	// display format
	public String toString() {
		String s = "";
		for (int i=0; i<_nDataChars; i++) {
            if (i!=0) s+=" ";
            if (_dataChars[i] < 16) s+="0";
            s+=Integer.toHexString(_dataChars[i]);
		}
		return s;
	}

    // recognize format
    public boolean isRcv()  { return getElement(1)==0x52;}
    public int getUA() { return getElement(0)-65; }

	static public int maxSize = 120;

	// contents (private)
	private int _nDataChars;
	private char _dataChars[] = new char[maxSize];

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialReply.class.getName());

}


/* @(#)SerialReply.java */
