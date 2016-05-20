// SprogReply.java
package jmri.jmrix.sprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.sprog.SprogConstants.SprogState;

/**
 * SprogReply.java
 *
 * Description:		Carries the reply to a SprogMessage
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @author			Andrew Berridge - refactored, cleaned up, Feb 2010
 * @version			$Revision$
 */
public class SprogReply extends AbstractMRReply {
	// Longest boot reply is 256bytes each preceded by DLE + 2xSTX + ETX
	static public final int maxSize = 515;
    private boolean _isBoot = false;
    
	// create a new one
	public  SprogReply() {
		super();
	}
	
    // no need to do anything
    protected int skipPrefix(int index) {
        return index;
    }

	/**
	 * Create a new SprogReply as a deep copy of an existing SprogReply
	 * @param m the SprogReply to copy
	 */
	@SuppressWarnings("null")
	public  SprogReply(SprogReply m) {
          this();
		if (m == null){
			log.error("copy ctor of null message");
                        return;
                }
		_nDataChars = m._nDataChars;
                _isBoot = m._isBoot;
                if (m.isUnsolicited()) super.setUnsolicited();
		for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
	}
	
    /**
     * Create a SprogReply from a String
     * @param replyString a String containing the contents of the reply
     * @param isBoot a boolean indicating if this is a boot reply
     */
    public SprogReply(String replyString, boolean isBoot) {
      this(replyString);
      _isBoot = isBoot;
    }
    
    public SprogReply(String replyString) {
    	super(replyString);
    }

    /**
     *  Is this reply indicating that an overload condition was detected?
     */
    public boolean isOverload() {
      return (this.toString().indexOf("!O") >= 0);
    }

    /**
     * Is this reply indicating that a general error has occurred?
     */
    public boolean isError() {
      return (this.toString().indexOf("!E") >= 0);
    }

    // Check and strip framing characters and DLE from a sprog bootloader reply
    public boolean strip() {
        char tmp[] = new char[_nDataChars];
        int j = 0;
        _isBoot = true; // definitely a boot message
        // Check framing characters
        if (_dataChars[0] != SprogMessage.STX) {return false;}
        if (_dataChars[1] != SprogMessage.STX) {return false;}
        if (_dataChars[_nDataChars-1] != SprogMessage.ETX) {return false;}

        // Ignore framing characters and strip DLEs
        for (int i = 2; i < _nDataChars - 1; i++) {
            if (_dataChars[i] == SprogMessage.DLE) {i++;}
            tmp[j++] = (char) _dataChars[i];
        }

        // Copy back to original SprogReply
        for (int i = 0; i < j; i++) {
            _dataChars[i] = tmp[i];
        }
        _nDataChars = j;
        return true;
    }

        // Check and strip checksum from a sprog bootloader reply
        // Assumes framing and DLE chars have been stripped
        public boolean getChecksum() {
            int checksum = 0;
            for (int i = 0; i < _nDataChars; i++) {
                checksum += _dataChars[i] & 0xff;
            }
            _nDataChars--;
            return ((checksum & 0xff) == 0);
        }

        /**
         * Returns a string representation of this SprogReply
         */
        public String toString() {
            //String s = "";
            StringBuffer buf = new StringBuffer();
            if (_isBoot || (_dataChars[0] == SprogMessage.STX)) {
              for (int i=0; i<_nDataChars; i++) {
                  //s+="<"+(((char)_dataChars[i]) & 0xff)+">";
                  buf.append("<");
                  buf.append(_dataChars[i]);
                  buf.append(">");
              }
            } else {
              for (int i=0; i<_nDataChars; i++) {
                  //s+=;
                  buf.append((char)_dataChars[i]);
              }
            }
            return buf.toString();
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

        /**
         * Returns the index of String s in the reply
         */
		public int match(String s) {
			// find a specific string in the reply
			String rep = new String(_dataChars, 0, _nDataChars);
			return rep.indexOf(s);
		}

        private int skipEqual(int index) {
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

        
        /*
         * Normal SPROG replies will end with the prompt for the next command
         * Bootloader will end with ETX with no preceding DLE
         * SPROG v4 bootloader replies "L>" on entry and replies "." at other
         * times
        */
        public boolean endNormalReply() {
            // Detect that the reply buffer ends with "P> " or "R> " (note ending space)
            int num = this.getNumDataElements();
            if ( num >= 3) {
              // ptr is offset of last element in SprogReply
              int ptr = num-1;
              if (this.getElement(ptr)   != ' ') return false;
              if (this.getElement(ptr-1) != '>') return false;
              if ((this.getElement(ptr-2) != 'P')&&(this.getElement(ptr-2) != 'R')) return false;
              // Now see if it's unsolicited !O for overload
              if ( num >= 5 ) {
                for (int i = 0; i < num-1; i++) {
                  if ((this.getElement(i) == '!')) super.setUnsolicited();
                }
              }
              return true;
            }
            else return false;
          }
        
        public boolean endBootReply() {
            // Detect that the reply buffer ends with ETX with no preceding DLE
            // This is the end of a SPROG II bootloader reply or the end of
            // a SPROG v4 echoing the botloader version request
            int num = this.getNumDataElements();
            if ( num >= 2) {
              // ptr is offset of last element in SprogReply
              int ptr = num-1;
              if ((this.getElement(ptr) & 0xff)   != SprogMessage.ETX) return false;
              if ((this.getElement(ptr-1) & 0xff) == SprogMessage.DLE) return false;
              return true;
            }
            else return false;
          }

          public boolean endBootloaderReply(SprogState sprogState) {
            // Detect that the reply buffer ends with "L>" or "." from a SPROG v4
            // bootloader
            int num = this.getNumDataElements();
            int ptr = num-1;
            if ((sprogState == SprogState.V4BOOTMODE) && ((this.getElement(ptr)   == '.')
            		|| (this.getElement(ptr)   == 'S'))) return true;
            if ( num >= 2) {
              // ptr is offset of last element in SprogReply
              if (this.getElement(ptr)   != '>') return false;
              if (this.getElement(ptr-1) != 'L') return false;
              return true;
            }
            else return false;
          }

        
    
   static Logger log = LoggerFactory.getLogger(SprogReply.class.getName());

}


/* @(#)SprogReply.java */
