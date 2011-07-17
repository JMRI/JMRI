// EcosReply.java

package jmri.jmrix.ecos;

/**
 * Carries the reply to an EcosMessage.
 * <P>
 * Some rudimentary support is provided for the "binary" option.
 *
 * @author		Bob Jacobsen  Copyright (C) 2001, 2008
 * @author Daniel Boudreau Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */
public class EcosReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public  EcosReply() {
        super();
    }
    public EcosReply(String s) {
        super(s);
    }
    public EcosReply(EcosReply l) {
        super(l);
    }

    // these can be very large
    public int maxSize() { return 5000; }


    // no need to do anything
    protected int skipPrefix(int index) {
        return index;
    }

    public int value() {
    	if (isBinary()) {
    		return getElement(0) & 0xFF;  // avoid stupid sign extension
    	} else {
    	    return super.value();
    	}
    }

    /**
     * Check for last line starts with 
     * "<END "
     */
    public boolean containsEnd() {
        for (int i = 0; i<getNumDataElements()-6; i++) {
            if ( (getElement(i) == 0x0A) &&
                 (getElement(i+1) == '<') &&
                 (getElement(i+2) == 'E') &&
                 (getElement(i+3) == 'N') &&
                 (getElement(i+4) == 'D') &&
                 (getElement(i+5) == ' ') )
                    return true;
        }
        return false;
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
     * Is this EcosReply actually an independ <EVENT message?
     */
    boolean isEvent() {
        if (getNumDataElements()<8) return false;
        if (getElement(0)!='<') return false;
        if (getElement(1)!='E') return false;
        if (getElement(2)!='V') return false;
        if (getElement(3)!='E') return false;
        if (getElement(4)!='N') return false;
        if (getElement(5)!='T') return false;
        if (getElement(6)!=' ') return false;
        return true;
    }
    
    /**
	 * Examine message to see if it is an asynchronous sensor (AIU) state report
	 * 
	 * @return true if message asynch sensor message 
	 * Boudreau: Improved detection to check three bytes and message length
	 * of exactly 3
	 */
    
    public boolean isSensorMessage() {
		return getElement(0) == 0x61 && getElement(1) >= 0x30
				&& getElement(2) >= 0x41 && getElement(2) <= 0x6F
				&& getNumDataElements() == 3;
	}
    
    public boolean isUnsolicited() {
    	if (isSensorMessage()) {
    		setUnsolicited();
    		return true;
    	} else {
    		return false;
    	}
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosReply.class.getName());

}


/* @(#)EcosReply.java */


