// MergReply.java

package jmri.jmrix.can.adapters.gridconnect.canrs;

import jmri.jmrix.can.adapters.gridconnect.GridConnectReply;

/**
 * Class for replies in a MERG GridConnect based message/reply protocol.
 * <P>
 * The GridConnect protocol encodes messages as an ASCII string of up to 24
 * characters of the form:
 *      :ShhhhNd0d1d2d3d4d5d6d7;
 * hhhh is the two byte (11 useful bits) header
 * The S indicates a standard CAN frame
 *      :XhhhhhhhhNd0d1d2d3d4d5d6d7;
 * The X indicates an extended CAN frame
 * Strict Gridconnect protocol allows a variable number of header characters,
 * e.g., a header value of 0x123 could be encoded as S123, X123, S0123 or
 * X00000123. MERG hardware uses a fixed 4 byte header when sending
 * GridConnectMessages to the computer. Additionally, the 11 byte standard
 * header is left justified in these 4 bytes.
 * This GridConnectReply code assumes the true GridConnect protocol is in use
 * but detects a standard header sent in 4 bytes and assumes it needs right
 * justifying.
 * N or R indicates a normal or remote frame, in position 6 or 10
 * d0 - d7 are the (up to) 8 data bytes
 * <P>
 * 
 * @author          Andrew Crosland Copyright (C) 2008
 * @author          Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.2 $
 */
public class MergReply extends GridConnectReply {
    
    public MergReply() {
        super();
    }

    public MergReply(String s) {
        super(s);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MergReply.class.getName());
}

/* @(#)MergReply.java */
