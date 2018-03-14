package jmri.jmrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for replies in a message/reply protocol.
 * <p>
 * Handles the character manipulation.
 * <p>
 * This is a variable length reply, which can grow as needed. The length is
 * given by the largest index written so far.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
abstract public class AbstractMRReply extends AbstractMessage {
    // is this logically an abstract class?

    /**
     * Create a new AbstractMRReply instance.
     */
    public AbstractMRReply() {
        setBinary(false);
        unsolicited = false;
        _dataChars = new int[maxSize()];
    }

    /**
     * Copy a Reply to a new AbstractMRReply instance.
     *
     * @param m the reply to copy
     */
    public AbstractMRReply(AbstractMRReply m) {
        this();
        if (m == null) {
            log.error("copy ctor of null message");
        } else {
            _nDataChars = m._nDataChars;
            for (int i = 0; i < _nDataChars; i++) {
                _dataChars[i] = m._dataChars[i];
            }
        }
    }

    /**
     * Create a new AbstractMRReply instance from a string.
     *
     * @param s String to use as reply content
     */
    public AbstractMRReply(String s) {
        this();
        _nDataChars = s.length();
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = s.charAt(i);
        }
    }

    // keep track of length
    @Override
    public void setElement(int n, int v) {
        _dataChars[n] = (char) v;
        _nDataChars = Math.max(_nDataChars, n + 1);
    }

    public void setOpCode(int i) {
        _dataChars[0] = (char) i;
    }

    public int getOpCode() {
        return _dataChars[0];
    }

    // accessors to the bulk data
    public void flush() {
        _nDataChars = 0;
    }

    // mode accessors
    private boolean _isBinary;

    public boolean isBinary() {
        return _isBinary;
    }

    public void setBinary(boolean b) {
        _isBinary = b;
    }

    public final void setUnsolicited() {
        unsolicited = true;
    }

    public boolean isUnsolicited() {
        return unsolicited;
    }

    /*
     * Override in system specific classes if required.
     *
     * @return 'true' if the message is an error and we can automatically
     * recover by retransmitting the message.
     */
    public boolean isRetransmittableErrorMsg() {
        return false;
    }

    // display format
    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < _nDataChars; i++) {
            if (_isBinary) {
                if (i != 0) {
                    s += " ";
                }
                s = jmri.util.StringUtil.appendTwoHexFromInt(_dataChars[i] & 0xFF, s);
            } else {
                s += (char) _dataChars[i];
            }
        }
        return s;
    }

    abstract protected int skipPrefix(int index);

    public int value() { // integer value of 1st three digits
        int index = 0;
        index = skipWhiteSpace(index);
        index = skipPrefix(index);
        index = skipWhiteSpace(index);
        String s = "" + (char) getElement(index) + (char) getElement(index + 1) + (char) getElement(index + 2);
        int val = -1;
        try {
            val = Integer.parseInt(s);
        } catch (RuntimeException e) {
            log.error("Unable to get number from reply: \"{}\" index: {} message: \"{}\"", s, index, toString());
        }
        return val;
    }

    public int pollValue() { // integer value of HHHH
        int index = 0;
        index = skipWhiteSpace(index);
        index = skipPrefix(index);
        index = skipWhiteSpace(index);
        String s = "" + (char) getElement(index) + (char) getElement(index + 1)
                + (char) getElement(index + 2) + (char) getElement(index + 3);
        int val = -1;
        try {
            val = Integer.parseInt(s, 16);
        } catch (RuntimeException e) {
            log.error("Unable to get number from reply: \"{}\" index: {} message: \"{}\"", s, index, toString());
        }
        return val;
    }

    public int match(String s) {
        // loop over starting positions
        outer:
        for (int i = 0; i < _nDataChars - s.length() + 1; i++) {
            // loop to check each start position
            for (int j = 0; j < s.length(); j++) {
                if (_dataChars[i + j] != s.charAt(j)) {
                    continue outer;
                }
            }
            // here we succeed
            return i;
        }

        return -1;
    }

    public int skipWhiteSpace(int index) {
        // start at index, passing any whitespace & control characters at the start of the buffer
        while (index < getNumDataElements() - 1
                && ((char) getElement(index) <= ' ')) {
            index++;
        }
        return index;
    }

    public int maxSize() {
        return DEFAULTMAXSIZE;
    }
    static public final int DEFAULTMAXSIZE = 120;

    // contents
    private boolean unsolicited;

    private final static Logger log = LoggerFactory.getLogger(AbstractMRReply.class);

}
