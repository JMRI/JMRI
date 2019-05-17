package jmri.jmrix.can.adapters.lawicell;

import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.can.CanReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for replies in a LAWICELL message/reply protocol.
 * <p>
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class Reply extends AbstractMRReply {

    // Creates a new instance of ConnectReply
    public Reply() {
        super();
    }

    /*     // copy one */
    /*     public  Reply(Reply m) { */
    /*         super(m); */
    /*     } */
    public Reply(String s) {
        _nDataChars = s.length();
        for (int i = 0; i < s.length(); i++) {
            _dataChars[i] = s.charAt(i);
        }
    }

    public CanReply createReply() {
        // is this just an ACK to e.g. a send?
        if (_dataChars[0] != 't' && _dataChars[0] != 'T') {
            if (log.isDebugEnabled()) {
                log.debug("non-frame reply skipped: " + this);
            }
            return null;
        }
        // carries a frame
        CanReply ret = new CanReply();

        ret.setExtended(isExtended());

        // Copy the header
        ret.setHeader(getHeader());

        // Get the data
        for (int i = 0; i < getNumBytes(); i++) {
            ret.setElement(i, getByte(i));
        }
        ret.setNumDataElements(getNumBytes());
        return ret;
    }

    @Override
    protected int skipPrefix(int index) {
        while (_dataChars[index] == ':') {
            index++;
        }
        return index;
    }

    public void setData(int[] d) {
        int len = (d.length <= 24) ? d.length : 24;
        for (int i = 0; i < len; i++) {
            _dataChars[i] = d[i];
        }
    }

    public boolean isExtended() {
        return _dataChars[0] == 'T';
    }

    /**
     * Get the CAN header as an int
     *
     * @return int the CAN ID
     */
    public int getHeader() {
        if (isExtended()) {
            // 11 bit header
            int val = 0;
            for (int i = 1; i <= 8; i++) {
                val = val * 16 + getHexDigit(i);
            }
            return val;
        } else {
            // 11 bit header
            return getHexDigit(1) * 256
                    + getHexDigit(2) * 16 + getHexDigit(3);
        }
    }

    /**
     * Get the number of data bytes
     *
     * @return int the number of bytes
     */
    public int getNumBytes() {
        if (isExtended()) {
            return _dataChars[9] - '0';
        } else {
            return _dataChars[4] - '0';
        }
    }

    /**
     * Get a hex data byte from the message
     * <p>
     * Data bytes are encoded as two ASCII hex digits. The starting position is
     * byte 10 or byte 5, depending on whether this is an extended or standard
     * message
     *
     * @param b The byte offset (0 - 7)
     * @return The value
     */
    public int getByte(int b) {
        if ((b >= 0) && (b <= 7)) {
            int index = b * 2 + (isExtended() ? 10 : 5);
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
        int b = 0;
        b = _dataChars[index];
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

    private final static Logger log = LoggerFactory.getLogger(Reply.class);
}


