/**
 * SerialMessage.java
 *
 * Description:		Contains the data payload of a CMRI serial
 *                  packet.  Note that _only_ the payload, not
 *                  the header or trailer, nor the padding DLE characters
 *                  are included.
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Id: SerialMessage.java,v 1.2 2002-03-11 04:36:24 jacobsen Exp $
 */

package jmri.jmrix.cmri.serial;


// Note:  This handles the "serial" form of CMRI

public class SerialMessage {
	// is this logically an abstract class?

	// create a new one
	public  SerialMessage(int i) {
		if (i<1)
			log.error("invalid length in call to ctor");
		_nDataChars = i;
		_dataChars = new int[i];
	}

	// copy one
	public  SerialMessage(SerialMessage m) {
		if (m == null)
			log.error("copy ctor of null message");
		_nDataChars = m._nDataChars;
		_dataChars = new int[_nDataChars];
		for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
	}

	// accessors to the bulk data
	public int getNumDataElements() {return _nDataChars;}
	public int getElement(int n) {return _dataChars[n];}
	public void setElement(int n, int v) { _dataChars[n] = v; }

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

    // static methods to recognize a message
    public boolean isPoll() { return getElement(1)==0x50;}
    public boolean isXmt()  { return getElement(1)==0x54;}
    public boolean isInit() { return (getElement(1)==0x49)&(getNumDataElements()>=7);}
    public int getUA() { return getElement(0)-65; }

	// static methods to return a formatted message
	static public SerialMessage getPoll(int UA) {
		SerialMessage m = new SerialMessage(2);
        m.setElement(0, 65+UA);
		m.setElement(1, 0x50); // 'P'
		return m;
	}

	// contents (private)
	private int _nDataChars = 0;
	private int _dataChars[] = null;

	private static String addIntAsThree(int val, SerialMessage m, int offset) {
		String s = ""+val;
		if (s.length() != 3) s = "0"+s;  // handle <10
		if (s.length() != 3) s = "0"+s;  // handle <100
		m.setElement(offset,s.charAt(0));
		m.setElement(offset+1,s.charAt(1));
		m.setElement(offset+2,s.charAt(2));
		return s;
	}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMessage.class.getName());

}


/* @(#)SerialMessage.java */
