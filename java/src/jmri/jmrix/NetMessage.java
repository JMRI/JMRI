package jmri.jmrix;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single general command or response.
 * <p>
 * Content is represented with ints to avoid the problems with sign-extension
 * that bytes have, and because a Java char is actually a variable number of
 * bytes in Unicode.
 * <p>
 * Both a set of indexed contents, an opcode, and a length field are available.
 * Different implementations will map the opcode and length into the contents in
 * different ways. They may not appear at all...
 *
 * @author Bob Jacobsen Copyright (C) 2002
 */
public abstract class NetMessage implements Serializable {

    /**
     * Create a new object, representing a specific-length message.
     *
     * @param len Total bytes in message, including opcode and error-detection
     *            byte.
     */
    public NetMessage(int len) {
        if (len < 0) {
            log.error("invalid length in call to ctor: " + len);
        }
        mNDataBytes = len;
        mDataBytes = new int[len];
    }

    public void setOpCode(int i) {
        mOpCode = i;
    }

    public int getOpCode() {
        return mOpCode;
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
        return mNDataBytes;
    }

    public int getElement(int n) {
        if (n < 0 || n >= mDataBytes.length) {
            log.error("illegal get element " + n
                    + " in message of " + mDataBytes.length
                    + " elements: " + this.toString());
        }
        return mDataBytes[n];
    }

    public void setElement(int n, int v) {
        if (n < 0 || n >= mDataBytes.length) {
            log.error("illegal set element " + n
                    + " in message of " + mDataBytes.length
                    + " elements: " + this.toString());
        }
        mDataBytes[n] = v;
    }

    /**
     * Get a String representation of the entire message in hex. This is not
     * intended to be human-readable!
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < mNDataBytes; i++) {
            s.append(Integer.toHexString(mDataBytes[i])).append(" ");
        }
        return s.toString();
    }

    /**
     * check whether the message has a valid parity
     */
    public abstract boolean checkParity();

    /**
     * Set parity to be correct for this implementation. Note that parity is
     * really a stand-in for whatever error checking, etc needs to be done
     */
    public abstract void setParity();

    // manipulate various things in a convenient way
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
     * Number of contained data bytes. This is not necessarily the same as the
     * length of the data array, depending on implementation details, so is kept
     * separate
     */
    private int mNDataBytes = 0;
    private int mDataBytes[] = null;
    private int mOpCode = 0;

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(NetMessage.class);

}
