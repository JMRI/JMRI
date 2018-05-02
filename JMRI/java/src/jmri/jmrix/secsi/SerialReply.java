package jmri.jmrix.secsi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the data payload of a serial reply packet. Note that it's _only_ the
 * payload.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2006, 2007, 2008
 */
public class SerialReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public SerialReply() {
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
     * Is reply to poll message.
     * @see SerialSensorManager#reply(SerialReply)
     */
    public int getAddr() {
        //log.error("getAddr should not be called", new Exception()); // will happen replying to Secsi Simulator
        return getElement(0);
    }

    @Override
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

    //private final static Logger log = LoggerFactory.getLogger(SerialReply.class);

}
