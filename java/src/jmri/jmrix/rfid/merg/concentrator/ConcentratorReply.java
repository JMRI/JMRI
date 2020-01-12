package jmri.jmrix.rfid.merg.concentrator;

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
public class ConcentratorReply extends RfidReply {

    RfidProtocol pr = null;

    // create a new one
    public ConcentratorReply(RfidTrafficController tc) {
        super(tc);
        this.pr = this.tc.getAdapterMemo().getProtocol();
        setBinary(true);
        setUnsolicited();
    }

    public ConcentratorReply(RfidTrafficController tc, String s) {
        super(tc, s);
        this.pr = this.tc.getAdapterMemo().getProtocol();
        setBinary(true);
        setUnsolicited();
    }

    public ConcentratorReply(RfidTrafficController tc, RfidReply l) {
        super(tc, l);
        this.pr = this.tc.getAdapterMemo().getProtocol();
        setBinary(true);
        setUnsolicited();
    }

    protected boolean isInRange() {
        return ((tc.getRange().equals("A-H") && (getElement(0) >= 0x41 || getElement(0) <= 0x48))
                || (tc.getRange().equals("I-P") && (getElement(0) >= 0x49 || getElement(0) <= 0x50)));
    }

    protected String getReaderPort() {
//        if (isInRange())
        return new StringBuffer().append((char) getElement(0)).toString();
//        return null;
    }

    @Override
    public String toMonitorString() {
        return pr.toMonitorString(this);
    }

}
