package jmri.jmrix.can.adapters.gridconnect.canrs;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.adapters.gridconnect.GridConnectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for messages for a MERG CAN-RS hardware adapter.
 * <p>
 * The MERG variant of the GridConnect protocol encodes messages as an ASCII
 * string of up to 24 characters of the form: :ShhhhNd0d1d2d3d4d5d6d7; hhhh is
 * the two byte (11 useful bits) header The S indicates a standard CAN frame
 * :XhhhhhhhhNd0d1d2d3d4d5d6d7; The X indicates an extended CAN frame Strict
 * Gridconnect protocol allows a variable number of header characters, e.g., a
 * header value of 0x123 could be encoded as S123 rather than S0123 or
 * X00000123. We choose a fixed number, either 4 or 8 bytes when sending
 * GridConnectMessages to keep MERG CAN-RS/USB adapters happy. The 11 bit
 * standard header is left justified in these 4 bytes. The 29 bit standard
 * header is sent as {@code <11 bit SID><0><1><0>< 18 bit EID>}
 * N or R indicates a normal or remote frame, in position 6 or 10 d0 - d7 are
 * the (up to) 8 data bytes
 * <p>
 *
 * @author Andrew Crosland Copyright (C) 2008
 */
public class MergMessage extends GridConnectMessage {

    // Creates a new instance of GridConnectMessage
    public MergMessage() {
        super();
    }

    public MergMessage(CanMessage m) {
        this();

        // Standard or extended frame
        setExtended(m.isExtended());

        // Copy the header
        setHeader(m.getHeader());

        // Normal or Remote frame?
        setRtr(m.isRtr());

        // Data payload
        for (int i = 0; i < m.getNumDataElements(); i++) {
            setByte(m.getElement(i), i);
        }
        // Terminator
        int offset = isExtended() ? 11 : 7;  // differs here from superclass
        setElement(offset + m.getNumDataElements() * 2, ';');
        setNumDataElements(offset + 1 + m.getNumDataElements() * 2);
        if (log.isDebugEnabled()) {
            log.debug("encoded as " + this.toString());
        }
    }

    /**
     * Set the header in MERG format
     *
     * @param header A valid CAN header value
     */
    @Override
    public void setHeader(int header) {
        int munged;
        if (isExtended()) {
            munged = ((header << 3) & 0xFFE00000) | 0x80000 | (header & 0x3FFFF);
            if (log.isDebugEnabled()) {
                log.debug("Extended header is " + header);
            }
            if (log.isDebugEnabled()) {
                log.debug("Munged header is " + munged);
            }
            super.setHeader(munged);
        } else {
            // 11 header bits are left justified
            munged = header << 5;
            if (log.isDebugEnabled()) {
                log.debug("Standard header is " + header);
            }
            if (log.isDebugEnabled()) {
                log.debug("Munged header is " + munged);
            }
            // Can't use super() as we want to send 4 digits
            setHexDigit((munged >> 12) & 0xF, 2);
            setHexDigit((munged >> 8) & 0xF, 3);
            setHexDigit((munged >> 4) & 0xF, 4);
            setHexDigit(0, 5);
        }
    }

    @Override
    public void setRtr(boolean rtr) {
        int offset = isExtended() ? 10 : 6;
        setElement(offset, rtr ? 'R' : 'N');
    }

    /**
     * Set a byte as two ASCII hex digits
     * <p>
     * Data bytes are encoded as two ASCII hex digits starting at byte 7 of the
     * message.
     *
     * @param val the value to set
     * @param n   the index of the byte to be set
     */
    @Override
    public void setByte(int val, int n) {
        if ((n >= 0) && (n <= 7)) {
            int index = n * 2 + (isExtended() ? 11 : 7);  // differs here from superclass
            setHexDigit((val / 16) & 0xF, index++);
            setHexDigit(val & 0xF, index);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(MergMessage.class);
}
