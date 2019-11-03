package jmri.jmrix.can;

import javax.annotation.Nonnull;
import jmri.jmrix.AbstractMRMessage;

/**
 * Base class for messages in a CANbus based message/reply protocol.
 * <p>
 * It is expected that any CAN based system will be based upon basic CANbus
 * concepts such as ID (standard or extended), Normal and RTR frames and a data
 * field.
 * <p>
 * The _dataChars[] and _nDataChars members refer to the data field, not the
 * entire message.
 * <p>
 * "header" refers to the full 11 or 29 bit header; which mode is separately set
 * via the "extended" parameter
 * <p>
 * CBUS uses a 2-bit "Pri" field and 7-bit "ID" ("CAN ID") field, with separate
 * accessors. CBUS ID is set as a layout connection preference and registered by
 * the traffic controller.
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2008, 2009, 2010
 */
public class CanMessage extends AbstractMRMessage implements CanMutableFrame {
    
    private boolean _translated = false;

    /**
     * Create a new CanMessage with 8 data bytes
     * @param header The CAN Frame header value
     */
    public CanMessage(int header) {
        _header = header;
        _isExtended = false;
        _isRtr = false;
        _nDataChars = 8;
        super.setBinary(true);
        _dataChars = new int[8];
    }
    
    /**
     * Create a new CanMessage of given length
     * @param i number of CAN Frame data bytes, max 8
     * @param header The CAN Frame header value
     */
    public CanMessage(int i, int header) {
        this(header);
        _nDataChars = (i <= 8) ? i : 8;
    }
    
    /**
     * Create a new CanMessage from an int array
     * @param d array of CAN Frame data bytes, max 8
     * @param header The CAN Frame header value
     */
    public CanMessage(int[] d, int header) {
        this(header);
        setData(d);
        setNumDataElements((d.length <= 8) ? d.length : 8);
    }

    /**
     * Create a new CanMessage from a byte array
     * @param d array of CAN Frame data bytes, max 8
     * @param header The CAN Frame header value
     */
    public CanMessage(byte[] d, int header) {
        this(header);
        setData(d);
        setNumDataElements((d.length <= 8) ? d.length : 8);
    }

    /**
     * Create a new CanMessage from an existing CanMessage
     * @param m The existing CanMessage
     */
    public CanMessage(@Nonnull CanMessage m) {
        this(m.getHeader());
        _isExtended = m.isExtended();
        _isRtr = m.isRtr();
        super.setBinary(true);
        setData(java.util.Arrays.copyOf(m.getData(),m.getNumDataElements()));
        setNumDataElements(m.getNumDataElements());
    }

    /**
     * Create a new CanMessage from an existing CanReply
     * @param m The existing CanReply
     */
    public CanMessage(@Nonnull CanReply m) {
        this(m.getHeader());
        _isExtended = m.isExtended();
        _isRtr = m.isRtr();
        super.setBinary(true);
        setData(m.getData());
        setNumDataElements(m.getNumDataElements());
    }
    
    /**
     * Tag whether translation is needed.
     * a "native" message has been converted already.
     * @param translated true or false to set flag as required
     */
    public void setTranslated(boolean translated) {
        _translated = translated;
    }
    
    /**
     * Check if translation flag has been set.
     * @return false by default
     */
    public boolean isTranslated() {
        return _translated;
    }

    /**
     * Hash on the header
     */
    @Override
    public int hashCode() {
        return _header;
    }

    /**
     * Note that a CanMessage and a CanReply can be tested for equality.
     * @param a CanMessage or CanReply to test against
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EQ_UNUSUAL",
        justification = "Equality test done in CanFrame")
    public boolean equals(Object a) {
        return isEqual(this,a);
    }

    /**
     * {@inheritDoc}
     * This format matches {@link CanReply}
     */
    @Override
    public String toString() {
        return getToString();
    }

    /**
     * {@inheritDoc}
     * This format matches @CanReply
     */
    @Override
    public String toMonitorString() {
        return monString();
    }

    /**
     * {@inheritDoc}
     * @return always false
     */
    @Override
    public boolean replyExpected() {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumDataElements() {
        return _nDataChars;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setNumDataElements(int n) {
        _nDataChars = n;
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
        _dataChars[n] = v;
    }

    /**
     * Get the data bytes in array form.
     * @return the actual int array
     */
    public int[] getData() {
        return _dataChars;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeader() {
        return _header;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHeader(int h) {
        _header = h;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExtended() {
        return _isExtended;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExtended(boolean b) {
        _isExtended = b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRtr() {
        return _isRtr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRtr(boolean b) {
        _isRtr = b;
    }

    // contents (package access)
    int _header;
    boolean _isExtended;
    boolean _isRtr;
}
