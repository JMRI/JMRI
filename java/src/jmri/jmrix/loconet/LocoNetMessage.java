package jmri.jmrix.loconet;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.loconet.messageinterp.LocoNetMessageInterpret;
/**
 * Represents a single command or response on the LocoNet.
 * <p>
 * Content is represented with ints to avoid the problems with sign-extension
 * that bytes have, and because a Java char is actually a variable number of
 * bytes in Unicode.
 * <p>
 * Note that this class does not manage the upper bit of the message. By
 * convention, most LocoNet messages have the upper bit set on the first byte,
 * and on no other byte; but not all of them do, and that must be managed
 * elsewhere.
 * <p>
 * Note that many specific message types are created elsewhere. In general, if
 * more than one tool will need to use a particular format, it's useful to
 * refactor it to here.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author B. Milhaupt Copyright (C) 2018
 * @see jmri.jmrix.nce.NceMessage
 * @see jmri.jmrix.AbstractMessage
 */
public class LocoNetMessage extends AbstractMessage implements Serializable {
    // Serializable, serialVersionUID used by jmrix.loconet.locormi, please do not remove
    static final long serialVersionUID = -7904918731667071828L;

    /**
     * Create a LocoNetMessage object without providing any
     * indication of its size or contents.
     * <p>
     * Because a LocoNet message requires at least a size, if
     * not actual contents, this constructor always logs an error.
     *
     */
    public LocoNetMessage() {
        _nDataChars = 0;
        _dataChars = new int[1];
        log.error("LocoNetMessage does not allow a constructor with no argument"); // NOI18N

    }

    /**
     * Create a new object, representing a specific-length message.
     * <p>
     * Logs an error if len is less than 2
     *
     * @param len Total bytes in message, including opcode and error-detection
     *            byte.
     */
    public LocoNetMessage(int len) {
        if (len < 2) {
            _nDataChars = 0;
            _dataChars = new int[1];
            log.error("LocoNetMessage does not allow object creation if length is less than 2."); // NOI18N
            return;
        }
        _nDataChars = len;
        _dataChars = new int[len];
    }

    /**
     * Create a LocoNetMessage from a String
     * <p>
     * Because it is difficult to create a complete LocoNet object using a string,
     * this method of AbstractMessage is not supported.
     * <p>
     * This constructor always logs an error
     * @param s an unused parameter
     */
    public LocoNetMessage(String s) {
        _nDataChars = 0;
        _dataChars = new int[1];
        log.error("LocoNetMessage does not allow a constructor with a 'String' argument"); // NOI18N
    }

    /**
     * Create a message with specified contents.
     * <p>
     * This method logs an error and returns if the contents are too short to
     * represent a valid LocoNet message.
     *
     * @param contents The array of contents for the message. The error check
     *                 word must be present, e.g. a 4-byte message must have
     *                 four values in the array
     */
    public LocoNetMessage(int[] contents) {
        if (contents.length < 2) {
            _nDataChars = 0;
            _dataChars = new int[1];
            log.error("Cannot create a LocoNet message of length shorter than two."); // NOI18N
        }
        _nDataChars = contents.length;
        _dataChars = new int[contents.length];
        for (int i = 0; i < contents.length; i++) {
            this.setElement(i, contents[i]);
        }
    }

    /**
     * Create a message with specified contents.  Each element is forced into an
     * 8-bit value.
     * <p>
     * This method logs an error and returns if the message length is too short
     * to represent a valid LocoNet message.
     *
     * @param contents The array of contents for the message. The error check
     *                 word must be present, e.g. a 4-byte message must have
     *                 four values in the array
     */
    public LocoNetMessage(byte[] contents) {
        if (contents.length < 2) {
            _nDataChars = 0;
            _dataChars = new int[1];
            log.error("Cannot create a LocoNet message of length shorter than two."); // NOI18N
        }
        _nDataChars = contents.length;
        _dataChars = new int[contents.length];
        for (int i = 0; i < contents.length; i++) {
            _dataChars[i] = contents[i] & 0xFF;
        }
    }

    public LocoNetMessage(LocoNetMessage original) {
        Objects.requireNonNull(original,
                "Unable to create message by copying a null message"); // NOI18N

        _nDataChars = original.getNumDataElements();
        _dataChars = new int[_nDataChars];

        for (int i = 0; i < original.getNumDataElements(); i++) {
            _dataChars[i] = original._dataChars[i];
        }
    }

    public void setOpCode(int i) {
        _dataChars[0] = i;
    }

    public int getOpCode() {
        return _dataChars[0];
    }

    /**
     * Get a String representation of the op code in hex.
     *
     * @return string containing a hexadecimal representation of the message OpCode
     */
    public String getOpCodeHex() {
        return "0x" + Integer.toHexString(getOpCode()); // NOI18N
    }

    /**
     * Get a specific byte from the message
     * <p>
     * Logs an error and aborts if the index is beyond the length of the message.
     *
     * @param n  the byte index within the message
     * @return integer value of the byte at the index within the message
     */
    @Override
    public int getElement(int n) {
        if (n < 0 || n >= _dataChars.length) {
            log.error("reference element {} in message of {} elements: {}", // NOI18N
                    n, _dataChars.length, this.toString()); // NOI18N
            return -1;
        }
        return _dataChars[n] & 0xFF;
    }

    /**
     * set a specific byte at a specific index in the message
     * <p>
     * Logs an error and aborts if the index is beyond the length of the message.
     *
     * @param n  the byte index within the message
     * @param v  the value to be set
     */
    @Override
    public void setElement(int n, int v) {
        if (n < 0 || n >= _dataChars.length) {
            log.error("reference element {} in message of {} elements: {}", // NOI18N
                    n, _dataChars.length, this.toString()); // NOI18N
            return;
        }
        _dataChars[n] = v & 0xFF;
    }

    /**
     * Get a String representation of the entire message in hex.
     *
     * @return a string representation containing a space-delimited set of hexadecimal
     *      values.
     */
    @Override
    public String toString() {
        int val;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < _nDataChars; i++) {
            if (i > 0) {
                sb.append(' ');
            }

            val = _dataChars[i] & 0xFF;
            sb.append(hexChars[val >> 4]);
            sb.append(hexChars[val & 0x0F]);
        }
        return sb.toString();
    }

    /**
     * Set the checksum byte(s) of this message.
     */
    public void setParity() {
        // check for the D3 special case
        if ((getOpCode() == LnConstants.RE_OPC_PR3_MODE) && (getNumDataElements() > 6)) {
            // sum the D3 header separately
            int sum = 0xFF;
            for (int i = 0; i < 5; i++) {
                sum = sum ^ getElement(i);
            }
            setElement(5, sum);
            // sum back half to 0xFF
            sum = 0xFF;
            for (int i = 6; i < getNumDataElements() - 1; i++) {
                sum = sum ^ getElement(i);
            }
            setElement(getNumDataElements() - 1, sum);
            return;
        }

        // normal case - just sum entire message
        int len = getNumDataElements();
        int chksum = 0xff;  /* the seed */

        int loop;

        for (loop = 0; loop < len - 1; loop++) {  // calculate contents for data part
            chksum ^= getElement(loop);
        }
        setElement(len - 1, chksum);  // checksum is last element of message    }
    }

    /**
     * Check whether the message has a valid checksum.
     *
     * @return true if checksum is correct, else false
     */
    public boolean checkParity() {
        int len = getNumDataElements();
        int chksum = 0xff;  /* the seed */

        int loop;

        // check for the D3 special case
        if ((getOpCode() == LnConstants.RE_OPC_PR3_MODE) && (len > 6)) {
            // sum the D3 header separately
            int sum = 0xFF;
            for (loop = 0; loop < 5; loop++) {
                sum = sum ^ getElement(loop);
            }
            if (getElement(5) != sum) {
                return false;
            }
            // sum back half to 0xFF
            sum = 0xFF;
            for (loop = 6; loop < len - 1; loop++) {
                sum = sum ^ getElement(loop);
            }
            if (getElement(len - 1) != sum) {
                return false;
            }
            return true;
        }

        // normal case - just sum entire message
        for (loop = 0; loop < len - 1; loop++) {  // calculate contents for data part
            chksum ^= getElement(loop);
        }
        return (chksum == getElement(len - 1));
    }

    // decode messages of a particular form
    // create messages of a particular form
    /**
     * Get the 8 data bytes from an OPC_PEER_XFR message.
     *
     * @return int[8] data bytes
     */
    public int[] getPeerXfrData() {
        if (getOpCode() != LnConstants.OPC_PEER_XFER) {
            log.error("getPeerXfrData called with wrong opcode {}", // NOI18N
                    getOpCode());
        }
        if (getElement(1) != 0x10) {
            log.error("getPeerXfrData called with wrong secondary code {}", // NOI18N
                    getElement(1));
        }
        if (getNumDataElements() != 16) {
            log.error("getPeerXfrData called with wrong length {}",  // NOI18N
                    getNumDataElements());
            return new int[] {0};
        }

        int[] data = new int[]{0, 0, 0, 0, 0, 0, 0, 0};

        int pxct1 = getElement(5);
        int pxct2 = getElement(10);

        // fill the 8 data items
        data[0] = (getElement(6) & 0x7F) + ((pxct1 & 0x01) != 0 ? 0x80 : 0);
        data[1] = (getElement(7) & 0x7F) + ((pxct1 & 0x02) != 0 ? 0x80 : 0);
        data[2] = (getElement(8) & 0x7F) + ((pxct1 & 0x04) != 0 ? 0x80 : 0);
        data[3] = (getElement(9) & 0x7F) + ((pxct1 & 0x08) != 0 ? 0x80 : 0);

        data[4] = (getElement(11) & 0x7F) + ((pxct2 & 0x01) != 0 ? 0x80 : 0);
        data[5] = (getElement(12) & 0x7F) + ((pxct2 & 0x02) != 0 ? 0x80 : 0);
        data[6] = (getElement(13) & 0x7F) + ((pxct2 & 0x04) != 0 ? 0x80 : 0);
        data[7] = (getElement(14) & 0x7F) + ((pxct2 & 0x08) != 0 ? 0x80 : 0);

        return data;
    }

    /**
     * Two messages are the same if their entire data content is the same. We
     * ignore the error-check byte to ease comparisons before a message is
     * transmitted.
     *
     * @return true if objects contain the same message contents
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;   // basic contract
        }
        if (!(o instanceof LocoNetMessage)) {
            return false;
        }
        LocoNetMessage m = (LocoNetMessage) o;
        if (m._nDataChars != this._nDataChars) {
            return false;
        }
        for (int i = 0; i < _nDataChars - 1; i++) {
            if ((m._dataChars[i] & 0xFF) != (this._dataChars[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int r = _nDataChars;
        if (_nDataChars > 0) {
            r += _dataChars[0];
        }
        if (_nDataChars > 1) {
            r += _dataChars[1] * 128;
        }
        if (_nDataChars > 2) {
            r += _dataChars[2] * 128 * 128;
        }
        return r;
    }

    @Override
    /**
     * Interprets a LocoNet message into a string describing the
     * message.
     * <p>
     * Where appropriate, this method presents both the JMRI "System Name" and
     * the JMRI "User Name" (where available) for messages which contain control 
     * or status information for a Turnout, Sensor or Reporter.
     * <p>
     * Display of "User Name" information is acquired from the appropriate "manager",
     * via a reference to an object with an assembled "System Name".  This method 
     * assumes a system connection "prefix" of "L" when assembling that system name.
     * The remainder of the assembled system name depends on the message contents - 
     * message type determines which JMRI object type letter to add - "T" for turnouts,
     * "S" for sensors, and "R" for transponding reporters.
     * <p>
     * If the appropriate manager already has an object for the system name being
     * referenced, the method requests the associated user name.  If a user name is
     * returned, then the method uses that user name as part of the message.  If 
     * there is no associated JMRI object configured, or if the associated JMRI
     * object does not have a user name assigned, then the method does not display 
     * a user name.
     * <p>
     * The method is not appropriate when the user has multiple LocoNet connections
     * or when the user has a single LocoNet connection but has changed the connection
     * prefix to something other than the default of "L".
     *
     * @return a human readable representation of the message.
     */
    public String toMonitorString(){
          return toMonitorString("L"); // NOI18N
    }

    /**
     * Interprets a LocoNet message into a string describing the
     * message when a specific connection prefix is known.
     * <p>
     * Where appropriate, this method presents both the JMRI "System Name" and
     * the JMRI "User Name" (where available) for messages which contain control 
     * or status information for a Turnout, Sensor or Reporter.
     * <p>
     * Display of "User Name" information is acquired from the appropriate "manager",
     * via a reference to an object with an assembled "System Name".  This method 
     * uses system connection "prefix" as specified in the "prefix" argument when 
     * assembling that system name.  The remainder of the assembled system name 
     * depends on the message contents.  Message type determines which JMRI 
     * object type letter is added after the "prefix" - "T" for turnouts, * "S" 
     * for sensors, and "R" for transponding reporters.  The item number 
     * specified in the LocoNet message is appended to finish the system name. 
     * <p>
     * If the appropriate manager already has an object for the system name being
     * referenced, the method requests the associated user name.  If a user name is
     * returned, then the method uses that user name as part of the message.  If 
     * there is no associated JMRI object configured, or if the associated JMRI
     * object does not have a user name assigned, then the method does not display 
     * a user name.
     *
     * @param prefix  the "System Name" prefix denoting the connection
     * @return a human readable representation of the message.
     */
    public String toMonitorString(@Nonnull String prefix){
          return LocoNetMessageInterpret.interpretMessage(this, 
                  prefix+"T", prefix+"S", prefix+"R");
    }

    /**
     * Return a newly created OPC_PEER_XFR message.
     *
     * @param src  Source address
     * @param dst  Destination address
     * @param d    int[8] for the data contents or null
     * @param code The instruction code placed in the pcxt1 pcxt2 bytes
     * @return The formatted message
     */
    static public LocoNetMessage makePeerXfr(int src, int dst, int[] d, int code) {
        LocoNetMessage msg = new LocoNetMessage(16);
        msg.setOpCode(LnConstants.OPC_PEER_XFER);
        msg.setElement(1, 0x10);  // 2nd part of op code

        // accumulate the pxct1,2 bytes
        int pxct1 = 0;
        int pxct2 = 0;

        // install the "CODE" in pxct1, pxct2
        pxct1 |= (code & 0x7) * 0x10;       // lower 3 bits
        pxct2 |= ((code & 0x38) / 8) * 0x10; // next 4 bits

        // store the addresses
        msg.setElement(2, src & 0x7F); //src
        msg.setElement(3, dst & 0x7F); //dstl
        msg.setElement(4, highByte(dst) & 0x7F); //dsth

        // store the data bytes
        msg.setElement(6, d[0] & 0x7F);
        if (highBit(d[0])) {
            pxct1 |= 0x01;
        }
        msg.setElement(7, d[1] & 0x7F);
        if (highBit(d[1])) {
            pxct1 |= 0x02;
        }
        msg.setElement(8, d[2] & 0x7F);
        if (highBit(d[2])) {
            pxct1 |= 0x04;
        }
        msg.setElement(9, d[3] & 0x7F);
        if (highBit(d[3])) {
            pxct1 |= 0x08;
        }

        msg.setElement(11, d[4] & 0x7F);
        if (highBit(d[4])) {
            pxct2 |= 0x01;
        }
        msg.setElement(12, d[5] & 0x7F);
        if (highBit(d[5])) {
            pxct2 |= 0x02;
        }
        msg.setElement(13, d[6] & 0x7F);
        if (highBit(d[6])) {
            pxct2 |= 0x04;
        }
        msg.setElement(14, d[7] & 0x7F);
        if (highBit(d[7])) {
            pxct2 |= 0x08;
        }

        // store the pxct1,2 values
        msg.setElement(5, pxct1);
        msg.setElement(10, pxct2);

        return msg;
    }

    /**
     * Check if a high bit is set, usually used to store it in some other
     * location (LocoNet does not allow the high bit to be set in data bytes).
     *
     * @param val  value to be checked
     * @return True if the argument has the high bit set
     */
    static protected boolean highBit(int val) {
        if ((val & (~0xFF)) != 0) {
            log.error("highBit called with too large value: 0x{}", // NOI18N
                    Integer.toHexString(val));
        }
        return (0 != (val & 0x80));
    }

    static protected int lowByte(int val) {
        return val & 0xFF;
    }

    static protected int highByte(int val) {
        if ((val & (~0xFFFF)) != 0) {
            log.error("highByte called with too large value: {}", // NOI18N
                    Integer.toHexString(val));
        }
        return (val & 0xFF00) / 256;
    }

    /**
     * Extract sensor address from a sensor message.  Does not verify
     * that the message is a sensor message.
     *
     * @return address (in range 0 to n-1)
     */
    public int sensorAddr() {
        int sw1 = getElement(1);
        int sw2 = getElement(2);
        int as = sw2 & 0x20;  // should be a LocoNet constant?
        int high = sw2 & 0x0F;
        int low = sw1 & 0x7F;
        return high * 256 + low * 2 + (as != 0 ? 1 : 0);
    }

    /**
     * If this is an OPC_INPUT_REP, get the 0-n address, else -1
     *
     * @return address (in range 0 to n-1)
     */
    public int inputRepAddr() {
        if (getOpCode() == LnConstants.OPC_INPUT_REP) {
            return sensorAddr();
        } else {
            return -1;
        }
    }

    /**
     * Get turnout address.  Does not check to see that the message is
     * a turnout message.
     *
     * @return address (in range 1 to n )
     */
    public int turnoutAddr() {
        int a1 = getElement(1);
        int a2 = getElement(2);
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f)) + 1;
    }


    // Hex char array for toString conversion
    static char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LocoNetMessage.class);

}
