// SpecificReply.java

package jmri.jmrix.rfid.merg.concentrator;

import jmri.jmrix.rfid.RfidProtocol;
import jmri.jmrix.rfid.RfidReply;
import jmri.jmrix.rfid.RfidTrafficController;
import jmri.jmrix.rfid.protocol.coreid.CoreIdRfidProtocol;

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

    protected boolean isInRange() {
        return ((tc.getRange().equals("A-H") && (getElement(0)>=0x41 || getElement(0)<=0x48)) ||
                (tc.getRange().equals("I-P") && (getElement(0)>=0x49 || getElement(0)<=0x50)));
    }

    protected String getReaderPort() {
//        if (isInRange())
            return new StringBuffer().append((char) getElement(0)).toString();
//        return null;
    }

    @Override
    public String toMonitorString() {
        StringBuilder sb = new StringBuilder();

        // check for range
        if (pr instanceof CoreIdRfidProtocol && isInRange()) {
            sb.append("Reply from port ");
            sb.append((char)(getElement(0)));
        }
        sb.append(pr.toMonitorString(this));
        return sb.toString();
    }
}

/* @(#)SpecificReply.java */
