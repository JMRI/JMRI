// SpecificReply.java

package jmri.jmrix.rfid.generic.standalone;

import jmri.jmrix.rfid.RfidReply;
import jmri.jmrix.rfid.RfidTrafficController;
import jmri.jmrix.rfid.coreid.CoreIdRfidReply;

/**
 * Contains the data payload of a serial reply
 * packet.  Note that its _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006, 2007, 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class SpecificReply extends CoreIdRfidReply {

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="URF_UNREAD_FIELD", justification="Kept to conform with common RFID framework")
    RfidTrafficController tc = null;

    // create a new one
    public SpecificReply(RfidTrafficController tc) {
        super(tc);
        this.tc = tc;
        setBinary(true);
        setUnsolicited();
    }
    public SpecificReply(RfidTrafficController tc, String s) {
        super(tc, s);
        this.tc = tc;
        setBinary(true);
        setUnsolicited();
    }
    public SpecificReply(RfidTrafficController tc, RfidReply l) {
        super(tc, l);
        this.tc = tc;
        setBinary(true);
        setUnsolicited();
    }

    public String toMonitorString() {
        // check for valid length
        StringBuffer sb = new StringBuffer();

        if (getElement(0)==0x02) {
            sb.append("Reply from reader.");
            sb.append(" Tag read ");
            sb.append(getTag());
            sb.append(" checksum ");
            sb.append((char)(getElement(11)));
            sb.append((char)(getElement(12)));
            sb.append(" valid? ");
            sb.append(isCheckSumValid()?"yes":"no");
            return sb.toString();
        } else {
            // don't know, just show
            sb.append("Unknown reply of length ");
            sb.append(getNumDataElements());
            sb.append(" ");
            sb.append(toString()).append("\n");
            sb.append("\n");
            return sb.toString();
        }
    }

}

/* @(#)SpecificReply.java */
