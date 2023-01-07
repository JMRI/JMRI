package jmri.jmrix.powerline.dmx512;

import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialTrafficController;

/**
 * Contains the data payload of a serial reply packet. Note that it's _only_ the
 * payload.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author Ken Cameron Copyright (C) 2023
 */
public class SpecificReply extends SerialReply {

    // create a new one
    public SpecificReply(SerialTrafficController tc) {
        super(tc);
        setBinary(true);
    }

    public SpecificReply(String s, SerialTrafficController tc) {
        super(tc, s);
        setBinary(true);
    }

    public SpecificReply(SerialReply l, SerialTrafficController tc) {
        super(tc, l);
        setBinary(true);
    }

    @Override
    public String toMonitorString() {
        // check for valid length
        StringBuilder sb = new StringBuilder();
            return sb.toString();
    }

}


