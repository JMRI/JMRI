package jmri.jmrix.rfid;

import jmri.jmrix.AbstractMRReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic interface to be implemented for each tag protocol.
 *
 * @author Matthew Harris Copyright (C) 2014
 * @since 3.9.2
 */
abstract public class RfidProtocol {

    protected boolean isConcentrator;
    protected char concentratorFirst;
    protected char concentratorLast;
    protected int portPosition;

    /**
     * Constructor for an RFID Protocol. Used when a single reader is connected
     * directly to a port, not via a concentrator.
     */
    public RfidProtocol() {
        this('\u0000', '\u0000', 0);
    }

    /**
     * Constructor for an RFID Protocol. Supports the use of concentrators where
     * a character range is used to determine the specific reader port.
     *
     * @param concentratorFirst  character representing first concentrator port
     * @param concentratorLast   character representing last concentrator port
     * @param portPosition       position of port character in reply string;
     *                            1 for first character
     */
    public RfidProtocol(char concentratorFirst, char concentratorLast, int portPosition) {
        isConcentrator = concentratorFirst != '\u0000' && concentratorLast != '\u0000' && portPosition != 0;
        this.concentratorFirst = concentratorFirst;
        this.concentratorLast = concentratorLast;
        this.portPosition = portPosition - 1; // needs to be zero-based;
    }

    /**
     * Retrieves RFID Tag information from message
     *
     * @param msg Message to decode
     * @return String representation of tag
     */
    public String getTag(AbstractMRReply msg) {
        log.error("getTag should not be called");
        return String.valueOf(msg.getElement(0));
    }

    /**
     * Determines if this protocol provides checksum values Default is false.
     * Protocols that do provide them should override this method.
     *
     * @return true if provided
     */
    public boolean providesChecksum() {
        return false;
    }

    /**
     * When available, returns the checksum portion of an RFID reply
     *
     * @param msg RFID reply to process
     * @return checksum value
     */
    abstract public String getCheckSum(AbstractMRReply msg);

    /**
     * Determines if this RFID reply is valid
     *
     * @param msg RFID reply to process
     * @return true if valid
     */
    abstract public boolean isValid(AbstractMRReply msg);

    /**
     * Determines if at the end of this RFID reply
     *
     * @param msg RFID reply to process
     * @return true if at end
     */
    abstract public boolean endOfMessage(AbstractMRReply msg);

    /**
     * Returns the initialisation string to be sent to an adapter implementing
     * the protocol. For those protocols that do not require one, return a blank
     * string
     *
     * @return initialisation string
     */
    abstract public String initString();

    public char getReaderPort(AbstractMRReply msg) {
        if (isConcentrator) {
            Character p = (char) msg.getElement(portPosition);
            if (p.toString().matches("[" + this.concentratorFirst + "-" + this.concentratorLast + "]")) {
                return p;
            }
        }
        return 0x00;
    }

    /**
     * Provides a textual representation of this message for the monitor
     *
     * @param msg RFID reply to process
     * @return textual representation
     */
    public String toMonitorString(AbstractMRReply msg) {
        StringBuilder sb = new StringBuilder();

        // don't know, just show
        sb.append("Unknown reply of length ");
        sb.append(msg.getNumDataElements());
        sb.append(": ");
        sb.append(msg.toString()).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Precomputed translation table for hex characters 0..f
     */
    private static final byte[] hexCodes = new byte['f' + 1];

    /**
     * Static method to initialise translation table
     */
    static {
        // Only 0..9, A..F & a..f are valid hex characters
        // all others are invalid

        // Set everything to invalid initially
        for (int i = 0; i <= 'f'; i++) {
            hexCodes[i] = -1;
        }

        // Now set values for 0..9
        for (int i = '0'; i <= '9'; i++) {
            hexCodes[i] = (byte) (i - '0');
        }

        // Now set values for A..F
        for (int i = 'A'; i <= 'F'; i++) {
            hexCodes[i] = (byte) (i - 'A' + 10);
        }

        // Finally, set values for a..f
        for (int i = 'a'; i <= 'f'; i++) {
            hexCodes[i] = (byte) (i - 'a' + 10);
        }
    }

    /**
     * Convert a single hex character to its corresponding hex value using
     * pre-calculated translation table.
     *
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
     * Converts a hex string to an unsigned byte array. Both upper and lower
     * case hex codes are permitted.
     *
     * @param s String representation of a hex number. Must be a whole number of
     *          bytes (i.e. an even number of characters) and be formed only of
     *          digits 0..9, a..f or A..F
     * @return corresponding unsigned byte array
     * @throws IllegalArgumentException when s is not a valid hex string
     */
    protected static byte[] convertHexString(String s) {

        // Check the length of the string to convert
        // is a whole number of bytes
        int stringLength = s.length();
        if ((stringLength & 0x1) != 0) {
            throw new IllegalArgumentException("convertHexString requires an even number of hex characters");
        }

        // Create byte array to store the converted string
        byte[] bytes = new byte[stringLength / 2];

        // Loop through the string converting individual bytes
        for (int i = 0, j = 0; i < stringLength; i += 2, j++) {
            // Convert the high and low nibbles
            int high = charToNibble(s.charAt(i));
            int low = charToNibble(s.charAt(i + 1));

            // Combine both nibbles into a byte
            bytes[j] = (byte) ((high << 4) | low);
        }
        return bytes;
    }

    private static final Logger log = LoggerFactory.getLogger(RfidProtocol.class);

}
