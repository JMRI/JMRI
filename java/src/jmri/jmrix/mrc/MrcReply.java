// MrcReply.java

package jmri.jmrix.mrc;

import org.apache.log4j.Logger;

/**
 * Carries the reply to an MrcMessage.
 *
 * @author		Bob Jacobsen  Copyright (C) 2001, 2004
 * @version             $Revision$
 */
public class MrcReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public  MrcReply() {
        super();
    }
    public MrcReply(String s) {
        super(s);
    }
    public MrcReply(MrcReply l) {
        super(l);
    }

    protected int skipPrefix(int index) {
		// start at index, passing any whitespace & control characters at the start of the buffer
		while (index < getNumDataElements()-1 &&
			((char)getElement(index) <= ' '))
				index++;
		return index;
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

    static Logger log = Logger.getLogger(MrcReply.class.getName());

}


/* @(#)MrcReply.java */
