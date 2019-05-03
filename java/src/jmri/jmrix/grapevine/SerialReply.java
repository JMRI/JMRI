package jmri.jmrix.grapevine;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the data payload of a serial reply packet. Note that it's _only_ the
 * payload.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2006, 2007, 2008
 */
public class SerialReply extends jmri.jmrix.AbstractMRReply {

    /**
     * Create a new SerialReply instance.
     */
    public SerialReply() {
        super(); // normal Grapevine replies are four bytes, binary
        setBinary(true);
    }

    /**
     * Copy a Reply to a new SerialReply instance.
     *
     * @param l the reply to copy
     */
    public SerialReply(SerialReply l) {
        super(l);
        setBinary(true);
    }

    /**
     * Create a new SerialReply instance from a string.
     *
     * @param s String to use as reply content
     */
    public SerialReply(String s) {
        super(s);
        setBinary(true);
    }

    /**
     * Is reply to poll message.
     */
    public int getAddr() {
        return getElement(0) & 0x7F;
    }

    @Override
    public boolean isUnsolicited() {
        return true;
    } // always unsolicited!

    @Override
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

    public int getBank() {
        return ((getElement(3) & 0x70) >> 4);
    }

    public boolean isError() {
        return (getElement(0) & 0x7F) == 0;
    }

    public boolean isFromParallelSensor() {
        // bank 5?
        if ((getElement(3) & 0x70) != 0x50) {
            return false;
        }
        if ((getElement(1) & 0x20) != 0x00) {
            return false;
        }
        return true;
    }

    public boolean isFromOldSerialSensor() {
        // bank 5?
        if ((getElement(3) & 0x70) != 0x50) {
            return false;
        }
        if ((getElement(1) & 0x20) != 0x20) {
            return false;
        }
        return true;
    }

    public boolean isFromNewSerialSensor() {
        // bank 4?
        if ((getElement(3) & 0x70) != 0x40) {
            return false;
        }
        return true;
    }

    public void setNumDataElements(int len) {
        if (len > _nDataChars) {
            log.error("Can't shorten reply from {} to {}", _nDataChars, len);
            return;
        }
        _nDataChars = len;
    }

    /**
     * Format the reply as human-readable text.
     * <p>
     * Since Grapevine doesn't distinguish between message and reply, this uses
     * the Message method.
     */
    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    public String format() {
        int b1 = -1;
        int b2 = -1;
        int b3 = -1;
        int b4 = -1;
        switch (getNumDataElements()) {
            case 4:
                b4 = getElement(3) & 0xff;
            // fall through
            case 3:
                b3 = getElement(2) & 0xff;
            // fall through
            case 2:
                b2 = getElement(1) & 0xff;
            // fall through
            case 1:
                b1 = getElement(0) & 0xff;
                break;
            default:
                log.warn("Unhandled number of elements: {}", getNumDataElements());
                break;
        }

        return SerialMessage.staticFormat(b1, b2, b3, b4);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialReply.class);

}


