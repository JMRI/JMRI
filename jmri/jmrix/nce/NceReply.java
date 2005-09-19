// NceReply.java

package jmri.jmrix.nce;

/**
 * Carries the reply to an NceMessage.
 * <P>
 * Some rudimentary support is provided for the "binary" option.
 *
 * @author		Bob Jacobsen  Copyright (C) 2001
 * @version             $Revision: 1.10 $
 */
public class NceReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public  NceReply() {
        super();
    }
    public NceReply(String s) {
        super(s);
    }
    public NceReply(NceReply l) {
        super(l);
    }

    protected int skipPrefix(int index) {
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

    /**
     * Extract poll values from binary reply
     */
     
    public int pollValue() {  // integer value of first two bytes
        int first = 0xFF & ((byte)getElement(0));
        int second = 0xFF & ((byte)getElement(1));
        
        return first*256 + second;
    }
    
    /**
     * Examine message to see if it is an asynchronous sensor (AIU) state report
     * @return true if message asynch sensor message
     */
    
    public boolean isSensorMessage() {
    	return getElement(0)==0x61 && getNumDataElements()>=3;
    }
    
    public boolean isUnsolicited() {
    	if (isSensorMessage()) {
    		setUnsolicited();
    		return true;
    	} else {
    		return false;
    	}
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceReply.class.getName());

}


/* @(#)NceReply.java */
