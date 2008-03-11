// SpecificReply.java

package jmri.jmrix.powerline.cp290;

import jmri.jmrix.powerline.X10Sequence;
import jmri.jmrix.powerline.SerialReply;

/**
 * Contains the data payload of a serial reply
 * packet.  Note that its _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006, 2007, 2008
 * @version     $Revision: 1.3 $
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
    
    public String toMonitorString() {
        return toString()+"\n";
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SpecificReply.class.getName());

}

/* @(#)SpecificReply.java */
