// IEEE802154Reply.java

package jmri.jmrix.ieee802154;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Contains the data payload of a serial reply
 * packet.  Note that its _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006, 2007, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * Modified for IEEE 802.15.4 connection
 * @author Paul Bender Copyright (C) 2013
 * @version     $Revision$
 */
public class IEEE802154Reply extends jmri.jmrix.AbstractMRReply {

	IEEE802154TrafficController tc = null;
	
    // create a new one
    public  IEEE802154Reply(IEEE802154TrafficController tc) {
        super();
        this.tc = tc;
        setBinary(true);
    }
    public IEEE802154Reply(IEEE802154TrafficController tc, String s) {
        super(s);
        this.tc = tc;
        setBinary(true);
    }
    public IEEE802154Reply(IEEE802154TrafficController tc, IEEE802154Reply l) {
        super(l);
        this.tc = tc;
        setBinary(true);
    }

    /**
     * Is reply to poll message
     */
    public int getAddr() { 
        log.error("getAddr should not be called");
        new Exception().printStackTrace();
        return getElement(0);
    }

    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

    public String toMonitorString() { return toString(); }
    
    static Logger log = LoggerFactory.getLogger(IEEE802154Reply.class);

}

/* @(#)IEEE802154Reply.java */
