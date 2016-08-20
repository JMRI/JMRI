// LocoNetMessage.java
package jmri.jmrix.loconet;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single command or response on the LocoNet.
 * <P>
 * Content is represented with ints to avoid the problems with sign-extension
 * that bytes have, and because a Java char is actually a variable number of
 * bytes in Unicode.
 * <P>
 * Note that this class does not manage the upper bit of the message. By
 * convention, most LocoNet messages have the upper bit set on the first byte,
 * and on no other byte; but not all of them do, and that must be managed
 * elsewhere.
 * <P>
 * Note that many specific message types are created elsewhere. In general, if
 * more than one tool will need to use a particular format, it's useful to
 * refactor it to here.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 * <P>
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 * @see jmri.jmrix.nce.NceMessage
 *
 */
public class LocoNetMessage implements Serializable {

    static final long serialVersionUID = -7904918731667071828L;

    /**
     * Create a new object, representing a specific-length message.
     *
     * @param len Total bytes in message, including opcode and error-detection
     *            byte.
     */
    public LocoNetMessage(int len) {
        if (len < 1) {
            log.error("invalid length in call to ctor: " + len);
        }
        _nDataBytes = len;
        _dataBytes = new int[len];
    }

    /**
     * Create a message with specified contents
     *
     * @param contents The array of contents for the message. The error check
     *                 word must be present, e.g. a 4-byte message must have
     *                 four values in the array
     */
    public LocoNetMessage(int[] contents) {
        this(contents.length);
        for (int i = 0; i < contents.length; i++) {
            this.setElement(i, contents[i]);
        }
    }

    public LocoNetMessage(byte[] contents) {
        this(contents.length);
        for (int i = 0; i < contents.length; i++) {
            this.setElement(i, contents[i] & 0xFF);
        }
    }

    public LocoNetMessage(LocoNetMessage original) {
        this(original._dataBytes);
    }

    public void setOpCode(int i) {
        _dataBytes[0] = i;
    }

    public int getOpCode() {
        return _dataBytes[0];
    }

    /**
     * Get a String representation of the op code in hex
     */
    public String getOpCodeHex() {
        return "0x" + Integer.toHexString(getOpCode());
    }

    /**
     * Get length, including op code and error-detection byte
     */
    public int getNumDataElements() {
        return _nDataBytes;
    }

    public int getElement(int n) {
        if (n < 0 || n >= _dataBytes.length) {
            log.error("reference element " + n
                    + " in message of " + _dataBytes.length
                    + " elements: " + this.toString());
        }
        return _dataBytes[n] & 0xFF;
    }

    public void setElement(int n, int v) {
        if (n < 0 || n >= _dataBytes.length) {
            log.error("reference element " + n
                    + " in message of " + _dataBytes.length
                    + " elements: " + this.toString());
        }
        _dataBytes[n] = v;
    }

    /**
     * Get a String representation of the entire message in hex
     */
    public String toString() {
        int val;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < _nDataBytes; i++) {
            if (i > 0) {
                sb.append(' ');
            }

            val = _dataBytes[i] & 0xFF;
            sb.append(hexChars[val >> 4]);
            sb.append(hexChars[val & 0x0F]);
        }
        return sb.toString();
    }

    /**
     * Set the parity byte(s) of this message
     */
    public void setParity() {
        // check for the D3 special case
        if (getOpCode() == 0xD3 && getNumDataElements() > 6) {
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
     * check whether the message has a valid parity
     */
    public boolean checkParity() {
        int len = getNumDataElements();
        int chksum = 0xff;  /* the seed */

        int loop;

        // check for the D3 special case
        if (getOpCode() == 0xD3 && len > 6) {
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
     * Get the 8 data bytes from an OPC_PEER_XFR message
     *
     * @return int[8] data bytes
     */
    public int[] getPeerXfrData() {
        if (getOpCode() != LnConstants.OPC_PEER_XFER) {
            log.error("getPeerXfrData called with wrong opcode " + getOpCode());
        }
        if (getElement(1) != 0x10) {
            log.error("getPeerXfrData called with wrong secondary code " + getElement(1));
        }
        if (getNumDataElements() != 16) {
            log.error("getPeerXfrData called with wrong length " + getNumDataElements());
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
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof LocoNetMessage)) {
            return false;
        }
        LocoNetMessage m = (LocoNetMessage) o;
        if (m._nDataBytes != this._nDataBytes) {
            return false;
        }
        for (int i = 0; i < _nDataBytes - 1; i++) {
            if ((m._dataBytes[i] & 0xFF) != (this._dataBytes[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int r = _nDataBytes;
        if (_nDataBytes > 0) {
            r += _dataBytes[0];
        }
        if (_nDataBytes > 1) {
            r += _dataBytes[1] * 128;
        }
        if (_nDataBytes > 2) {
            r += _dataBytes[2] * 128 * 128;
        }
        return r;
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
     * location (LocoNet does not allow the high bit to be set in data bytes)
     *
     * @return True if the argument has the high bit set
     */
    static protected boolean highBit(int val) {
        if ((val & (~0xFF)) != 0) {
            log.error("highBit called with too large value: 0x"
                    + Integer.toHexString(val));
        }
        return (0 != (val & 0x80));
    }

    static protected int lowByte(int val) {
        return val & 0xFF;
    }

    static protected int highByte(int val) {
        if ((val & (~0xFFFF)) != 0) {
            log.error("highByte called with too large value: "
                    + Integer.toHexString(val));
        }
        return (val & 0xFF00) / 256;
    }

    /**
     * Sensor-format 0-n address
     *
     * @return 0 to n-1 address
     */
    public int sensorAddr() {
        int sw1 = getElement(1);
        int sw2 = getElement(2);
        int as = sw2 & 0x20;		// should be a LocoNet constant?
        int high = sw2 & 0x0F;
        int low = sw1 & 0x7F;
        return high * 256 + low * 2 + (as != 0 ? 1 : 0);
    }

    /**
     * If this is an OPC_INPUT_REP, return the 0-n address, else -1
     *
     * @return 0 to n-1 address
     */
    public int inputRepAddr() {
        if (getOpCode() == LnConstants.OPC_INPUT_REP) {
            return sensorAddr();
        } else {
            return -1;
        }
    }

    /**
     * Return the 1-N turnout address
     *
     * @return 1-N address
     */
    public int turnoutAddr() {
        int a1 = getElement(1);
        int a2 = getElement(2);
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f)) + 1;
    }

    // contents (private)
    private int _nDataBytes = 0;
    private int _dataBytes[] = null;

    // Hex char array for toString conversion
    static char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LocoNetMessage.class.getName());

}

/* @(#)LocoNetMessage.java */
