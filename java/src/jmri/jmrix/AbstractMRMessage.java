package jmri.jmrix;

import java.util.Objects;
import javax.annotation.CheckForNull;
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
    public final void setNeededMode(int pMode) {
        mNeededMode = pMode;
    }

    /**
     * Get needed mode.
     * Final so that it can be called in constructors.
     * @return mNeededMode required mode value.
     */
    public final int getNeededMode() {
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
    public final boolean isBinary() {
        return _isBinary;
    }

    /**
     * Set if Binary.
     * final so that it can be called in constructors.
     * @param b true if binary, else false.
     */
    public final void setBinary(boolean b) {
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
    protected static final int SHORT_TIMEOUT = 2000;
    protected static final int LONG_TIMEOUT = 60000;  // e.g. for programming options
    
    private int mTimeout;  // in milliseconds

    /**
     * Set Timeout.
     * Final so that it can be called in constructors.
     * @param t timeout value.
     */
    public final void setTimeout(int t) {
        mTimeout = t;
    }

    /**
     * Get Timeout.
     * Final so that it can be called in constructors.
     * @return timeout value.
     */
    public final int getTimeout() {
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
    public final void setRetries(int i) {
        mRetries = i;
    }

    /**
     * Get number of retries.
     * final so that it can be called in constructors
     * @return number of retries count.
     */
    public final int getRetries() {
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

    protected String sourceLetter = "S";
    /** 
     * Get the letter representing where this message originated.
     * This is used by e.g. some Monitor implementations.
     * Defaults to "S" for "Sent"
     */
    public String getSourceLetter() {
        return sourceLetter;
    } 

    /**
     * Told when a message has been written to the connection.
     *
     * @since 5.19.1
     */
    @FunctionalInterface
    public interface SentListener {

        /**
         * The message has been written to the connection. Called on the
         * layout thread (Swing thread); keep it short.
         *
         * @param message   the message
         * @param timeNanos when it was written, on the
         *                  {@link System#nanoTime()} scale, as
         *                  {@link AbstractMRMessage#getSentTime()} gives
         *                  it. Only differences between such times have a
         *                  meaning.
         */
        void messageSent(@Nonnull AbstractMRMessage message, long timeNanos);
    }

    // Set on the thread that wrote the message, read on others: the time is
    // written before the volatile flag, so a reader that sees the flag sees
    // the time.
    private long sentTime = 0L;
    private volatile boolean hasSentTime = false;
    private volatile SentListener sentListener = null;

    /**
     * Record when this message was written to the connection, on the
     * {@link System#nanoTime()} scale. Set by the traffic controller as soon
     * as the write has returned, on the thread that wrote it. Not kept by a
     * copy, which is a new message still to be sent.
     *
     * @param timeNanos the time
     * @since 5.19.1
     */
    public void setSentTime(long timeNanos) {
        sentTime = timeNanos;
        hasSentTime = true;
    }

    /**
     * @return true if the time this message was written to the connection
     *         has been recorded; false while it waits to be sent, and for a
     *         connection that doesn't record it
     * @since 5.19.1
     */
    public boolean hasSentTime() {
        return hasSentTime;
    }

    /**
     * Get when this message was written to the connection, on the
     * {@link System#nanoTime()} scale; only differences between such times
     * have a meaning. If the message is written more than once, the latest
     * time.
     *
     * @return the time, or 0 if it was not recorded; see
     *         {@link #hasSentTime()}
     * @since 5.19.1
     */
    public long getSentTime() {
        return hasSentTime ? sentTime : 0L;
    }

    /**
     * Ask to be told when this message has been written to the connection,
     * each time it is. Not told if it is never written, for example if there
     * is no connection or the write fails, nor by a connection that doesn't
     * record when it writes. Not kept by a copy.
     *
     * @param listener the listener, or null for none
     * @since 5.19.1
     */
    public void setSentListener(@CheckForNull SentListener listener) {
        sentListener = listener;
    }

    /**
     * @return the listener to tell when this message has been written, or
     *         null
     * @since 5.19.1
     */
    @CheckForNull
    public SentListener getSentListener() {
        return sentListener;
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractMRMessage.class);

}
