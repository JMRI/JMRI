// EasyDccReply.java

package jmri.jmrix.easydcc;

/**
 * EasyDccReply.java
 *
 * Description:		Carries the reply to an EasyDccMessage
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.6 $
 */
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

    /**
     * Extracts Read-CV returned value from a message.  Returns
     * -1 if message can't be parsed. Expects a message of the
     * formnat "CVnnnvv" where vv is the hexadecimal value
     * or "Vnvv" where vv is the hexadecimal value.
     */
	public int value() {
        int index = 0;
        if ( (char)getElement(index) == 'C') {
            // integer value of 6th, 7th digits in hex
		    index = 5;  // 5th position is index 5
        } else if ( (char)getElement(index) == 'V') {
            // integer value of 3rd, 4th digits in hex
		    index = 2;  // 2nd position is index 2
        } else {
            log.warn("Did not find recognizable format: "+this.toString());
        }
		String s1 = ""+(char)getElement(index);
        String s2 = ""+(char)getElement(index+1);
		int val = -1;
		try {
            int sum = Integer.valueOf(s2,16).intValue();
			sum += 16*Integer.valueOf(s1,16).intValue();
            val = sum;  // don't do this assign until now in case the conversion throws
		} catch (Exception e) {
			log.error("Unable to get number from reply: \""+s1+s2+"\" index: "+index
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

	static public int maxSize = 120;

	// contents (private)
	private int _nDataChars;
	private char _dataChars[] = new char[maxSize];

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccReply.class.getName());

}


/* @(#)EasyDccReply.java */
