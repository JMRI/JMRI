/**
 * EasyDccReply.java
 *
 * Description:		Carries the reply to an EasyDccMessage
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Id: EasyDccReply.java,v 1.2 2002-03-30 19:22:53 jacobsen Exp $
 */

package jmri.jmrix.easydcc;


// Note:  This handles the "binary" form of command in the EasyDcc spec

public class EasyDccReply {
	// is this logically an abstract class?

	// create a new one
	public  EasyDccReply() {
	}

	// copy one
	public  EasyDccReply(EasyDccReply m) {
		if (m == null)
			log.error("copy ctor of null message");
		_nDataChars = m._nDataChars;
		for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
	}

	// from String
	public EasyDccReply(String s) {
		_nDataChars = s.length();
		for (int i = 0; i<_nDataChars; i++)
			_dataChars[i] = s.charAt(i);
	}

	public void setOpCode(int i) { _dataChars[0]= (char)i;}
	public int getOpCode() {return _dataChars[0];}

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
				s+=(char)_dataChars[i];
		}
		return s;
	}

	public int value() {  // integer value of 8 and 9th digits
		int index = 7;  // 8th position is index 7
		//index = skipWhiteSpace(index);
		String s = ""+(char)getElement(index)+(char)getElement(index+1);
		int val = -1;
		try {
			val = Integer.parseInt(s);
		} catch (Exception e) {
			log.error("Unable to get number from reply: \""+s+"\" index: "+index
					+" message: \""+toString()+"\"");
		}
		return val;
	}

	int match(String s) {
		// find a specific string in the reply
		String rep = new String(_dataChars, 0, _nDataChars);
		return rep.indexOf(s);
	}

	int skipWhiteSpace(int index) {
		// start at index, passing any whitespace & control characters at the start of the buffer
		while (index < getNumDataElements()-1 &&
			((char)getElement(index) <= ' '))
				index++;
		return index;
	}

	int skipCOMMAND(int index) {
		// start at index, passing any control characters at the start of the buffer
		int len = "COMMAND: ".length();
		if ( getNumDataElements() >= index+len-1
			&& 'C'== (char)getElement(index)
			&& 'O'== (char)getElement(index+1)
			&& 'M'== (char)getElement(index+2)
			&& 'M'== (char)getElement(index+3)
			&& 'A'== (char)getElement(index+4)
			&& 'N'== (char)getElement(index+5)
			&& 'D'== (char)getElement(index+6)
			&& ':'== (char)getElement(index+7)
			&& ' '== (char)getElement(index+8)  ) {
				index = index +"COMMAND: ".length();
		}
		return index;
	}

	static public int maxSize = 120;

	// contents (private)
	private int _nDataChars;
	private char _dataChars[] = new char[maxSize];

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccReply.class.getName());

}


/* @(#)EasyDccReply.java */
