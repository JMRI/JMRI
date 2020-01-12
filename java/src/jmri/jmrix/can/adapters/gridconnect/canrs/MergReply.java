package jmri.jmrix.can.adapters.gridconnect.canrs;

import jmri.jmrix.can.adapters.gridconnect.GridConnectReply;

/**
 * Class for replies in a MERG GridConnect based message/reply protocol.
 * <p>
 * The GridConnect protocol encodes messages as an ASCII string of up to 24
 * characters of the form: :ShhhhNd0d1d2d3d4d5d6d7; hhhh is the two byte (11
 * useful bits) header The S indicates a standard CAN frame
 * :XhhhhhhhhNd0d1d2d3d4d5d6d7; The X indicates an extended CAN frame Strict
 * Gridconnect protocol allows a variable number of header characters, e.g., a
 * header value of 0x123 could be encoded as S123, X123, S0123 or X00000123.
 * MERG hardware uses a fixed 4 or 8 byte header when sending
 * GridConnectMessages to the computer. The 11 bit standard header is left
 * justified in these 4 bytes. The 29 bit standard header is sent as
 * {@code <11 bit SID><0><1><0>< 18 bit EID>}
 * N or R indicates a normal or remote frame, in position 6 or 10 d0 - d7 are
 * the (up to) 8 data bytes
 * <p>
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class MergReply extends GridConnectReply {

    public MergReply() {
        super();
    }

    public MergReply(String s) {
        super(s);
    }

    /**
     * Get the CAN header from MERG format in digits 2 to 9
     *
     * @return the CAN header as an int
     */
    @Override
    public int getHeader() {
        int val = super.getHeader();
        // Adjust standard header from MERG adapter received as 11 bits left
        // justified in four bytes
        if (_dataChars[1] == 'S') {
            val = (val >> 5) & 0x07FF;
        }
        // Adjust extended header from MERG adapter received as
        // <11 bit SID><0><1><0><18 bit EID> in four bytes
        if (_dataChars[1] == 'X') {
            val = ((val >> 3) & 0x1FFC0000) | (val & 0x3FFFF);
        }
        return val;
    }
}
