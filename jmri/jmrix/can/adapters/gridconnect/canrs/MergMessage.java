// MergMessage.java

package jmri.jmrix.can.adapters.gridconnect.canrs;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.can.CanMessage;

import jmri.jmrix.can.adapters.gridconnect.GridConnectMessage;

/**
 * Class for messages for a MERG CAN-RS hardware adapter.
 * <P>
 * The MERG varient of the GridConnect protocol encodes messages 
 * as an ASCII string of up to 24
 * characters of the form:
 *      :ShhhhNd0d1d2d3d4d5d6d7;
 * hhhh is the two byte (11 useful bits) header
 * The S indicates a standard CAN frame
 *      :XhhhhhhhhNd0d1d2d3d4d5d6d7;
 * The X indicates an extended CAN frame
 * Strict Gridconnect protocol allows a variable number of header characters,
 * e.g., a header value of 0x123 could be encoded as S123 rather than S0123 or
 * X00000123. We choose a fixed number, either 4 or 8 bytes when sending
 * GridConnectMessages to keep MERG CAN-RS/USB adapters happy. At this time, the
 * MERG adapter ignores the header as it has a default value in EEPROM
 * N or R indicates a normal or remote frame, in position 6 or 10
 * d0 - d7 are the (up to) 8 data bytes
 * <P>
 *
 * @author          Andrew Crosland Copyright (C) 2008
 * @version			$Revision: 1.1 $
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
        for (int i = 0 ; i < m.getNumDataElements(); i++) {
            setByte(m.getElement(i), i);
        }
        // Terminator
        int offset = isExtended() ? 11 : 7;  // differs here from superclass
        setElement(offset + m.getNumDataElements()*2, ';');
        setNumDataElements(offset + 1 + m.getNumDataElements()*2);
        if (log.isDebugEnabled()) log.debug("encoded as "+this.toString());
    }
        
    /**
     * Set the header
     *
     * @param header A valid CAN header value
     */
    public void setHeader(int header) {
        if (isExtended()) {
            if (log.isDebugEnabled()) log.debug("Extended header is "+header);
            setHexDigit((header>>28)&0xF, 2);
            setHexDigit((header>>24)&0xF, 3);
            setHexDigit((header>>20)&0xF, 4);
            setHexDigit((header>>16)&0xF, 5);
            setHexDigit((header>>12)&0xF, 6);
            setHexDigit((header>>8)&0xF, 7);
            setHexDigit((header>>4)&0xF, 8);
            setHexDigit( header&0xF, 9);
        } else {
            if (log.isDebugEnabled()) log.debug("Standard header is "+header);
            // 11 header bits are left justified
            setHexDigit( 0, 2);
            setHexDigit((header>>8)&0xF, 3);
            setHexDigit((header>>4)&0xF, 4);
            setHexDigit( header&0xF, 5);
        }
    }
    
    public void setRtr(boolean rtr) {
        int offset = isExtended() ? 10 : 6;
        setElement(offset, rtr ? 'R' : 'N');
    }

    /**
     * Set a byte as two ASCII hex digits
     * <P>
     * Data bytes are encoded as two ASCII hex digits starting at byte 7 of the
     * message.
     *
     * @param val the value to set
     * @param n the index of the byte to be set
     */
    public void setByte(int val, int n) {
        if ((n >= 0) && (n <= 7)) {
            int index = n*2 + (isExtended() ? 11 : 7);  // differs here from superclass
            setHexDigit((val/16)&0xF, index++);
            setHexDigit( val    &0xF, index);
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MergMessage.class.getName());
}

/* @(#)MergMessage.java */
