package jmri.jmrix;

import java.util.Objects;
import javax.annotation.Nonnull;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for messages in a message/reply protocol.
 * <p>
 * Carries a sequence of characters, with accessors.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
abstract public class AbstractMRMessage extends AbstractMessage {

    /**
     * Create a new AbstractMRMessage instance.
     */
    public AbstractMRMessage() {
        setBinary(false);
        setNeededMode(AbstractMRTrafficController.NORMALMODE);
        setTimeout(SHORT_TIMEOUT);  // default value is the short timeout
        setRetries(0); // default to no retries
    }

    /**
     * Create a new AbstractMRMessage instance of a given byte size.
     *
     * @param i number of elements in message
     */
    public AbstractMRMessage(int i) {
        this();
        if (i < 1) {
            log.error("invalid length {} in call to ctor", i);
            throw new IllegalArgumentException("invalid length in call to ctor");
        }
        _nDataChars = i;
        _dataChars = new int[i];
    }

    /**
     * Copy an AbstractMRMessage to a new instance.
     *
     * @param m the message to copy
     */
    public AbstractMRMessage(@Nonnull AbstractMRMessage m) {
        this();
        Objects.requireNonNull(m, "copy ctor of null message");
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        System.arraycopy(m._dataChars, 0, _dataChars, 0, _nDataChars);
        setTimeout(m.getTimeout());
        setRetries(m.getRetries());
        setNeededMode(m.getNeededMode());
    }

    /**
     * Create a new Message instance from a string.
     *
     * @param s String to use as message content
     */
    public AbstractMRMessage(String s) {
        this(s.length());
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = s.charAt(i);
        }
    }

    public void setOpCode(int i) {
        _dataChars[0] = i;
    }

    public int getOpCode() {
        try {
            return _dataChars[0];
        } catch(ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    public String getOpCodeHex() {
        return "0x" + Integer.toHexString(getOpCode());
    }

    // accessors to the bulk data

    // state info
    private int mNeededMode;

    /**
     * Set a needed mode.
     * Final so that it can be called in constructors.
     * @param pMode required mode value.
     */
    final public void setNeededMode(int pMode) {
        mNeededMode = pMode;
    }

    /**
     * Get needed mode.
     * Final so that it can be called in constructors.
     * @return mNeededMode required mode value.
     */
    final public int getNeededMode() {
        return mNeededMode;
    }

    /**
     * Is a reply expected to this message?
     * <p>
     * By default, a reply is expected to every message; either a reply or a
     * timeout is needed before the next message can be sent.
     * <p>
     * If this returns false, the transmit queue will immediately go on to
     * transmit the next message (if any).
     * @return true by default in Abstract MR message.
     */
    public boolean replyExpected() {
        return true;
    }

    // mode accessors
    private boolean _isBinary;

    /**
     * Get if is binary.
     * Final so that it can be called in constructors.
     * @return true if binary, else false.
     */
    final public boolean isBinary() {
        return _isBinary;
    }

    /**
     * Set if Binary.
     * final so that it can be called in constructors.
     * @param b true if binary, else false.
     */
    final public void setBinary(boolean b) {
        _isBinary = b;
    }

    /**
     * Minimum timeout that's acceptable.
     * <p>
     * Also used as default for normal operations. Don't shorten this "to make
     * recovery faster", as sometimes <i>internal</i> delays can slow processing
     * down.
     * <p>
     * Units are milliseconds.
     */
    static protected final int SHORT_TIMEOUT = 2000;
    static protected final int LONG_TIMEOUT = 60000;  // e.g. for programming options
    
    private int mTimeout;  // in milliseconds

    /**
     * Set Timeout.
     * Final so that it can be called in constructors.
     * @param t timeout value.
     */
    final public void setTimeout(int t) {
        mTimeout = t;
    }

    /**
     * Get Timeout.
     * Final so that it can be called in constructors.
     * @return timeout value.
     */
    final public int getTimeout() {
        return mTimeout;
    }

    /* For some systems, we want to retry sending a message if the port
     isn't ready for them. */
    private int mRetries = 0; // number of retries, default = 0;

    /**
     * Set number of retries.
     * Final so that it can be called in constructors
     * @param i number of retries, actual value to set, not an increment.
     */
    final public void setRetries(int i) {
        mRetries = i;
    }

    /**
     * Get number of retries.
     * final so that it can be called in constructors
     * @return number of retries count.
     */
    final public int getRetries() {
        return mRetries;
    }

    // display format

    // contents (private)
    public void addIntAsThree(int val, int offset) {
        String s = "" + val;
        if (s.length() != 3) {
            s = "0" + s;  // handle <10
        }
        if (s.length() != 3) {
            s = "0" + s;  // handle <100
        }
        setElement(offset, s.charAt(0));
        setElement(offset + 1, s.charAt(1));
        setElement(offset + 2, s.charAt(2));
    }

    /**
     * Put an int value into the message as 
     * two ASCII upper-case hex characters.
     * @param val value to convert.
     * @param offset offset in message.
     */
    public void addIntAsTwoHex(int val, int offset) {
        String s = ("" + Integer.toHexString(val)).toUpperCase();
        if (s.length() < 2) {
            s = "0" + s;  // handle one digit
        }
        if (s.length() > 2) {
            log.error("can't add as two hex digits: {}", s);
        }
        setElement(offset, s.charAt(0));
        setElement(offset + 1, s.charAt(1));
    }

    /**
     * Put an int value into the message as 
     * three ASCII upper-case hex characters.
     * @param val value to convert.
     * @param offset offset in message.
     */
    public void addIntAsThreeHex(int val, int offset) {
        String s = ("" + Integer.toHexString(val)).toUpperCase();
        if (s.length() > 3) {
            log.error("can't add as three hex digits: {}", s);
        }
        if (s.length() != 3) {
            s = "0" + s;
        }
        if (s.length() != 3) {
            s = "0" + s;
        }
        setElement(offset, s.charAt(0));
        setElement(offset + 1, s.charAt(1));
        setElement(offset + 2, s.charAt(2));
    }

    /**
     * Put an int value into the message as 
     * four ASCII upper-case hex characters.
     * @param val value to convert.
     * @param offset offset in message.
     */
    public void addIntAsFourHex(int val, int offset) {
        String s = ("" + Integer.toHexString(val)).toUpperCase();
        if (s.length() > 4) {
            log.error("can't add as three hex digits: {}", s);
        }
        if (s.length() != 4) {
            s = "0" + s;
        }
        if (s.length() != 4) {
            s = "0" + s;
        }
        if (s.length() != 4) {
            s = "0" + s;
        }
        setElement(offset, s.charAt(0));
        setElement(offset + 1, s.charAt(1));
        setElement(offset + 2, s.charAt(2));
        setElement(offset + 3, s.charAt(3));
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < _nDataChars; i++) {
            if (_isBinary) {
                if (i != 0) {
                    s += " ";
                }
                s = StringUtil.appendTwoHexFromInt(_dataChars[i] & 255, s);
            } else {
                s += (char) _dataChars[i];
            }
        }
        return s;
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractMRMessage.class);

}
