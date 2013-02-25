// RfidReply.java

package jmri.jmrix.rfid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Contains the data payload of a serial reply
 * packet.  Note that its _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006, 2007, 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
abstract public class RfidReply extends jmri.jmrix.AbstractMRReply {

    RfidTrafficController tc = null;

    // create a new one
    public  RfidReply(RfidTrafficController tc) {
        super();
        this.tc = tc;
        setBinary(true);
    }
    public RfidReply(RfidTrafficController tc, String s) {
        super(s);
        this.tc = tc;
        setBinary(true);
    }
    public RfidReply(RfidTrafficController tc, RfidReply l) {
        super(l);
        this.tc = tc;
        setBinary(true);
    }

    /**
     * Is reply to poll message
     */
    public String getTag() {
        log.error("getTag should not be called");
        new Exception().printStackTrace();
        return String.valueOf(getElement(0));
    }

    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

    abstract public String toMonitorString();

    /**
     * Precomputed translation table for hex characters 0..f
     */
    private static final byte[] hexCodes = new byte[ 'f' + 1 ];

    /**
     * Static method to initialise translation table
     */
    static {
        // Only 0..9, A..F & a..f are valid hex characters
        // all others are invalid

        // Set everything to invalid initially
        for (int i=0; i<= 'f'; i++) {
            hexCodes[i] = -1;
        }

        // Now set values for 0..9
        for (int i='0'; i<='9'; i++) {
            hexCodes[i] = (byte) (i - '0');
        }

        // Now set values for A..F
        for (int i='A'; i<='F'; i++) {
            hexCodes[i] = (byte) (i - 'A' + 10);
        }

        // Finally, set values for a..f
        for (int i='a'; i<='f'; i++) {
            hexCodes[i] = (byte) (i - 'a' + 10);
        }
    }

    /**
     * Convert a single hex character to it's corresponding hex value
     * using pre-calculated translation table.
     * @param c character to convert (0..9, a..f or A..F)
     * @return corresponding integer value (0..15)
     * @throws IllegalArgumentException when c is not a hex character
     */
    private static int charToNibble(char c) {
        if (c > 'f') {
            throw new IllegalArgumentException("Invalid hex character: " + c);
        }
        int nibble = hexCodes[c];
        if (nibble < 0) {
            throw new IllegalArgumentException("Invalid hex character: " + c);
        }
        return nibble;
    }

    /**
     * Converts a hex string to an unsigned byte array.
     * Both upper and lower case hex codes are permitted.
     * @param s String representation of a hex number.
     *          Must be a whole number of bytes
     *          (i.e. an even number of characters)
     *          and be formed only of digits 0..9, a..f or A..F
     * @return corresponding unsigned byte array
     * @throws IllegalArgumentException when s is not a valid hex string
     */
    protected static byte[] convertHexString(String s) {

        // Check the length of the string to convert
        // is a whole number of bytes
        int stringLength = s.length();
        if ((stringLength & 0x1) !=0) {
            throw new IllegalArgumentException("convertHexString requires an even number of hex characters");
        }

        // Create byte array to store the converted string
        byte[] bytes = new byte[stringLength / 2];

        // Loop through the string converting individual bytes
        for (int i=0, j=0; i<stringLength; i+=2, j++) {
            // Convert the high and low nibbles
            int high = charToNibble(s.charAt(i));
            int low = charToNibble(s.charAt(i+1));

            // Combine both nibbles into a byte
            bytes[j] = (byte) ((high<<4) | low);
        }
        return bytes;
    }
    
    private static final Logger log = LoggerFactory.getLogger(RfidReply.class.getName());

}

/* @(#)RfidReply.java */
