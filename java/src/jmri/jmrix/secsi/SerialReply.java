// SerialReply.java

package jmri.jmrix.secsi;

import org.apache.log4j.Logger;


/**
 * Contains the data payload of a serial reply
 * packet.  Note that its _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006, 2007, 2008
 * @version     $Revision$
 */
public class SerialReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public  SerialReply() {
        super();
        setBinary(true);
    }
    public SerialReply(String s) {
        super(s);
        setBinary(true);
    }
    public SerialReply(SerialReply l) {
        super(l);
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

    static Logger log = Logger.getLogger(SerialReply.class.getName());

}

/* @(#)SerialReply.java */
