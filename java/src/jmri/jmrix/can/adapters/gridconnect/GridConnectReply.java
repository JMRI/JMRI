package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.can.CanReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for replies in a GridConnect based message/reply protocol.
 * <p>
 * The GridConnect protocol encodes messages as an ASCII string of up to 24
 * characters of the form: :ShhhhNd0d1d2d3d4d5d6d7;
 * <p>
 * hhhh is the two byte (11
 * useful bits) header The S indicates a standard CAN frame
 * :XhhhhhhhhNd0d1d2d3d4d5d6d7; The X indicates an extended CAN frame N or R
 * indicates a normal or remote frame, in position 6 or 10 d0 - d7 are the (up
 * to) 8 data bytes
 *
 * @author Andrew Crosland Copyright (C) 2008, 2009
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class GridConnectReply extends AbstractMRReply {

    static final int MAXLEN = 27;

    /**
     * Creates a new instance of GridConnectReply.
     */
    public GridConnectReply() {
        _nDataChars = 0;
        _dataChars = new int[MAXLEN];
    }

    /**
     * Creates a new GridConnectReply from String.
     * @param s String to use as basis for the GCReply.
     */
    public GridConnectReply(String s) {
        _nDataChars = s.length();
        for (int i = 0; i < s.length(); i++) {
            _dataChars[i] = s.charAt(i);
        }
    }

    /**
     * Create a CanReply from a GridConnectReply.
     * @return new CanReply Outgoing message.
     */
    public CanReply createReply() {
        CanReply ret = new CanReply();

        log.debug("createReply converts from {}", this);

        // basic checks drop out the frame
        if (!basicFormatCheck()) {
            ret.setHeader(0);
            ret.setNumDataElements(0);
            return ret;
        }

        // Is it an Extended frame?
        if (isExtended()) {
            ret.setExtended(true);
        }

        // Copy the header
        ret.setHeader(getHeader());

        // Is it an RTR frame?
        if (isRtr()) {
            ret.setRtr(true);
        }

        // Get the data
        for (int i = 0; i < getNumBytes(); i++) {
            ret.setElement(i, getByte(i));
        }
        ret.setNumDataElements(getNumBytes());
        log.debug("createReply converted to {}", ret);
        return ret;
    }

    /**
     * Check if this GCReply contains an Extended or Standard flag.
     * @return true if contains a flag, else false.
     */
    protected boolean basicFormatCheck() {
        return !((getElement(1) != 'X') && (getElement(1) != 'S'));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int skipPrefix(int index) {
        while (_dataChars[index] == ':') {
            index++;
        }
        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumDataElements() {
        return _nDataChars;
    }

    /**
     * Set Number of Data Elements.
     * Max. length set by the MAXLEN constant.
     * @param n Number Elements.
     */
    public void setNumDataElements(int n) {
        _nDataChars = (n <= MAXLEN) ? n : MAXLEN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getElement(int n) {
        return _dataChars[n];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setElement(int n, int v) {
        if (n < MAXLEN) {
            _dataChars[n] = v;
            _nDataChars = Math.max(_nDataChars, n + 1);
        }
    }

    /**
     * Get if the GridConnectReply is Extended.
     * @return true if extended, else false.
     */
    public boolean isExtended() {
        return (getElement(1) == 'X');
    }

    /**
     * Get if the GridConnectReply is RtR.
     * @return true if RtR, else false.
     */
    public boolean isRtr() {
        return (getElement(_RTRoffset) == 'R');
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int maxSize() {
        return MAXLEN;
    }

    /**
     * Set the GridConnectReply data by Array.
     * @param d data array.
     */
    public void setData(int[] d) {
        int len = (d.length <= MAXLEN) ? d.length : MAXLEN;
        System.arraycopy(d, 0, _dataChars, 0, len);
    }

    // pointer to the N or R character
    int _RTRoffset = -1;

    /**
     * Get the CAN header by using chars from 2 to up to 9.
     * <p>
     * Right justify standard headers that had 4 digits.
     *
     * @return the CAN header as an int
     */
    public int getHeader() {
        int val = 0;
        for (int i = 2; i <= 10; i++) {
            _RTRoffset = i;
            if (_dataChars[i] == 'N') {
                break;
            }
            if (_dataChars[i] == 'R') {
                break;
            }
            val = val * 16 + getHexDigit(i);
        }
        return val;
    }

    /**
     * Get the number of data bytes.
     * @return number of bytes in reply.
     */
    public int getNumBytes() {
        // subtract framing and ID bytes, etc and each byte is two ASCII hex digits
        return (_nDataChars - (_RTRoffset + 1)) / 2;
    }

    /**
     * Get a hex data byte from the message.
     * <p>
     * Data bytes are encoded as two ASCII hex digits starting at byte 7 of the
     * message.
     *
     * @param b The byte offset (0 - 7)
     * @return The value
     */
    public int getByte(int b) {
        if ((b >= 0) && (b <= 7)) {
            int index = b * 2 + _RTRoffset + 1;
            int hi = getHexDigit(index++);
            int lo = getHexDigit(index);
            if ((hi < 16) && (lo < 16)) {
                return (hi * 16 + lo);
            }
        }
        return 0;
    }

    // Get a single hex digit. returns 0 if digit is invalid
    private int getHexDigit(int index) {
        int b = _dataChars[index];
        if ((b >= '0') && (b <= '9')) {
            b = b - '0';
        } else if ((b >= 'A') && (b <= 'F')) {
            b = b - 'A' + 10;
        } else if ((b >= 'a') && (b <= 'f')) {
            b = b - 'a' + 10;
        } else {
            b = 0;
        }
        return (byte) b;
    }

    private final static Logger log = LoggerFactory.getLogger(GridConnectReply.class);
}


