// SerialReply.java

package jmri.jmrix.oaktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Contains the data payload of a serial reply
 * packet.  Note that its _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006
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
    public int getAddr() { return getElement(0); }

    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

    static Logger log = LoggerFactory.getLogger(SerialReply.class.getName());

}

/* @(#)SerialReply.java */
