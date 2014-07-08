// SpecificReply.java

package jmri.jmrix.rfid.generic.standalone;

import jmri.jmrix.rfid.RfidProtocol;
import jmri.jmrix.rfid.RfidReply;
import jmri.jmrix.rfid.RfidTrafficController;

/**
 * Contains the data payload of a serial reply
 * packet.  Note that its _only_ the payload.
 *
 * @author      Bob Jacobsen  Copyright (C) 2002, 2006, 2007, 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class SpecificReply extends RfidReply {

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="URF_UNREAD_FIELD", justification="Kept to conform with common RFID framework")
    RfidTrafficController tc = null;
    RfidProtocol pr = null;

    // create a new one
    public SpecificReply(RfidTrafficController tc) {
        super(tc);
        this.tc = tc;
        this.pr = tc.getAdapterMemo().getProtocol();
        setBinary(true);
        setUnsolicited();
    }
    public SpecificReply(RfidTrafficController tc, String s) {
        super(tc, s);
        this.tc = tc;
        this.pr = tc.getAdapterMemo().getProtocol();
        setBinary(true);
        setUnsolicited();
    }
    public SpecificReply(RfidTrafficController tc, RfidReply l) {
        super(tc, l);
        this.tc = tc;
        this.pr = tc.getAdapterMemo().getProtocol();
        setBinary(true);
        setUnsolicited();
    }

    @Override
    public String toMonitorString() {
        return pr.toMonitorString(this);
    }


}

/* @(#)SpecificReply.java */
