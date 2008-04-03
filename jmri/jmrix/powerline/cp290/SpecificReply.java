// SpecificReply.java

package jmri.jmrix.powerline.cp290;

import jmri.jmrix.powerline.X10Sequence;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.cp290.Constants;

/**
 * Contains the data payload of a serial reply
 * packet.  Note that its _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006, 2007, 2008
 * @version     $Revision: 1.4 $
 */
public class SpecificReply extends jmri.jmrix.powerline.SerialReply {

    // create a new one
    public  SpecificReply() {
        super();
        setBinary(true);
    }
    public SpecificReply(String s) {
        super(s);
        setBinary(true);
    }
    public SpecificReply(SerialReply l) {
        super(l);
        setBinary(true);
    }

    /**
     * Find 1st byte that's not 0xFF, or -1 if none
     */
    int startIndex() {
        int len = getNumDataElements();
        for (int i = 0; i<len; i++) {
            if ( (getElement(i)&0xFF) != 0xFF ) return i;
        }
        return -1;
    }

	/**
	 * Translate packet to text
	 */
    public String toMonitorString() {
        // check for valid length
    	String val = "???";
    	int len = getNumDataElements();
    	boolean goodSync = true;
    	boolean goodCheckSum = true;
    	int sum = 0;
    	String cmd;
    	String stat;
    	String hCode;
    	String bCode;
    	String dev;
        switch (len) {
        case 7:
        	for (int i = 0; i < 6; i++) {
        		if ((getElement(i) & 0xFF) != 0xFF) {
        			goodSync = false;
        		}
        	}
        	val = Constants.statusToText(getElement(6));
        	break;
        case 12:
        	for (int i = 0; i < 6; i++) {
        		if ((getElement(i) & 0xFF) != 0xFF) {
        			goodSync = false;
        		}
        	}
        	for (int i = 7; i < 11; i++) {
        		sum = (sum + (getElement(i) &0xFF)) & 0xFF;
        	}
        	stat = Constants.statusToText(getElement(6));
        	cmd = Constants.commandToText(getElement(7) & 0x0F, -1);
        	hCode = Constants.houseCodeToText((getElement(7) >> 4) & 0x0F);
        	dev = Constants.deviceToText(getElement(8), getElement(9));
        	bCode = Constants.houseCodeToText((getElement(10) >> 4) & 0x0F);
        	if (sum != (getElement(11) & 0xFF)) {
        		goodCheckSum = false;
        	}
        	val = "Cmd Echo: " + cmd + " stat: " + stat + " House: " + hCode + " Device:" + dev + " Base: " + bCode;
        	if (!goodSync) {
        		val = val + " BAD SYNC";
        	}
        	if (!goodCheckSum) {
        		val = val + " BAD CHECKSUM: " + (getElement(11) & 0xFF) + " vs " + sum;
        	}
        	break;
        case 22:
        	for (int i = 0; i < 16; i++) {
        		if ((getElement(i) & 0xFF) != 0xFF) {
        			goodSync = false;
        		}
        	}
        	for (int i = 17; i < 21; i++) {
        		sum = (sum + (getElement(i) &0xFF)) & 0xFF;
        	}
        	cmd = Constants.commandToText((getElement(17) & 0x0F), ((getElement(17) & 0xF0) >> 4));
        	hCode = Constants.houseCodeToText((getElement(18) >> 4) & 0x0F);
        	dev = Constants.deviceToText(getElement(19), getElement(20));
        	if (sum != (getElement(21) & 0xFF)) {
        		goodCheckSum = false;
        	}
        	val = cmd + "House: " + hCode + " Device:" + dev;
        	if (!goodSync) {
        		val = val + " BAD SYNC";
        	}
        	if (!goodCheckSum) {
        		val = val + " BAD CHECKSUM: " + (getElement(21) & 0xFF) + " vs " + sum;
        	}
        	break;
        default:
        	val = "UNK " + toString();
        	break;
        }
        return "Recv[" + len + "]: " + val + "\n";
	}
	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SpecificReply.class.getName());

}

/* @(#)SpecificReply.java */
