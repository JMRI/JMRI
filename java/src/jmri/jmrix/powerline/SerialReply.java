package jmri.jmrix.powerline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the data payload of a serial reply packet. Note that it's _only_ the
 * payload.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
abstract public class SerialReply extends jmri.jmrix.AbstractMRReply {

    SerialTrafficController tc = null;

    // create a new one
    public SerialReply(SerialTrafficController tc) {
        super();
        this.tc = tc;
        setBinary(true);
    }

    public SerialReply(SerialTrafficController tc, String s) {
        super(s);
        this.tc = tc;
        setBinary(true);
    }

    public SerialReply(SerialTrafficController tc, SerialReply l) {
        super(l);
        this.tc = tc;
        setBinary(true);
    }

    /**
     * Is reply to poll message
     * @return value of poll message
     */
    public int getAddr() {
        log.error("getAddr should not be called", new Exception());
        return getElement(0);
    }

    @Override
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialReply.class);

}


