// SprogReply.java

package jmri.jmrix.sprog;

/**
 * SprogReply.java
 *
 * Description:		Carries the reply to an SprogMessage
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */
public class SprogReply {
	// is this logically an abstract class?

	// create a new one
	public  SprogReply() {
	}

	// copy one
	public  SprogReply(SprogReply m) {
		if (m == null)
			log.error("copy ctor of null message");
		_nDataChars = m._nDataChars;
		for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
	}

	// from String
	public SprogReply(String s) {
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
         * -1 if message can't be parsed.
         *
         * SPROG is assumed to not be echoing commands. A reply to a command
         * may include the prompt that was printed after the previous command
         * Reply to a CV read is of the form " = hvv" where vv is the CV value
         * in hex
         */
        public int value() {
          int index = 0;
          index = skipWhiteSpace(index);
          index = skipEqual(index);
          index = skipWhiteSpace(index);
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

        int skipEqual(int index) {
          // start at index, skip over the equals and hex prefix
          int len = "= h".length();
          if ( getNumDataElements() >= index+len-1
               && '='== (char)getElement(index)
               && ' '== (char)getElement(index+1)
               && 'h'== (char)getElement(index+2)  ) {
            index += len;
          }
          return index;
        }

	static public int maxSize = 120;

	// contents (private)
	private int _nDataChars;
	private char _dataChars[] = new char[maxSize];

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogReply.class.getName());

}


/* @(#)SprogReply.java */
