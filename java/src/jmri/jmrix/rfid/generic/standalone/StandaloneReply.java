package jmri.jmrix.rfid.generic.standalone;

import jmri.jmrix.rfid.RfidProtocol;
import jmri.jmrix.rfid.RfidReply;
import jmri.jmrix.rfid.RfidTrafficController;

/**
 * Contains the data payload of a serial reply packet. Note that it's _only_ the
 * payload.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2006, 2007, 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class StandaloneReply extends RfidReply {

    RfidProtocol pr = null;

    // create a new one
    public StandaloneReply(RfidTrafficController tc) {
        super(tc);
        this.pr = this.tc.getAdapterMemo().getProtocol();
        setBinary(true);
        setUnsolicited();
    }

    public StandaloneReply(RfidTrafficController tc, String s) {
        super(tc, s);
        this.pr = this.tc.getAdapterMemo().getProtocol();
        setBinary(true);
        setUnsolicited();
    }

    public StandaloneReply(RfidTrafficController tc, RfidReply l) {
        super(tc, l);
        this.pr = this.tc.getAdapterMemo().getProtocol();
        setBinary(true);
        setUnsolicited();
    }

    @Override
    public String toMonitorString() {
        return pr.toMonitorString(this);
    }

}
