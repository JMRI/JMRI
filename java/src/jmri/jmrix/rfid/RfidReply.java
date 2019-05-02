package jmri.jmrix.rfid;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Contains the data payload of a serial reply packet. Note that it's _only_ the
 * payload.
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2006, 2007, 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
abstract public class RfidReply extends jmri.jmrix.AbstractMRReply {

    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "used by derived classes to fetch protocol in use")
    protected RfidTrafficController tc = null;

    // create a new one
    public RfidReply(RfidTrafficController tc) {
        super();
        this.tc = tc;
        setBinary(true);
    }

    public RfidReply(RfidTrafficController tc, String s) {
        super(s);
        this.tc = tc;
        setBinary(true);
    }

    public RfidReply(RfidTrafficController tc, RfidReply l) {
        super(l);
        this.tc = tc;
        setBinary(true);
    }

    @Override
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

}
