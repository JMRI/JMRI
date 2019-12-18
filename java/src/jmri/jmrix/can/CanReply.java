package jmri.jmrix.can;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nonnull;
import jmri.jmrix.AbstractMRReply;

/**
 * Base class for replies in a CANbus based message/reply protocol.
 * <p>
 * It is expected that any CAN based system will be based upon basic CANbus
 * concepts such as ID (standard or extended), Normal and RTR frames and a data
 * field.
 * <p>
 * "header" refers to the full 11 or 29 bit header; which mode is separately set
 * via the "extended" parameter
 * <p>
 * CBUS uses a 2-bit "Pri" field and 7-bit "ID" ("CAN ID") field, with separate
 * accessors.
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2008, 2009, 2010
 */
public class CanReply extends AbstractMRReply implements CanMutableFrame {

    /**
     * Create a new CanReply
     */
    public CanReply() {
        _isExtended = false;
        _isRtr = false;
        _nDataChars = 8;
        super.setBinary(true);
        _dataChars = new int[8];
    }

    /**
     * Create a new CanReply of given data length
     * @param i number of data bytes, 0-8
     */
    public CanReply(int i) {
        this();
        setNumDataElements((i <= 8) ? i : 8);
    }

    /**
     * Create a new CanReply from an int array
     * @param d array of CAN Frame data bytes, max 8
     */
    public CanReply(int[] d) {
        this();
        setData(d);
        setNumDataElements((d.length <= 8) ? d.length : 8);
    }
    
    /**
     * Create a new CanReply from an int array, with header
     * @param d array of CAN Frame data bytes, max 8
     * @param header the Frame header value
     */
    public CanReply(int[] d, int header) {
        this();
        setHeader(header);
        setData(d);
        setNumDataElements((d.length <= 8) ? d.length : 8);
    }

    /**
     * Create a new CanReply from an existing CanReply
     * @param m The existing CanReply
     */
    public CanReply(@Nonnull CanReply m) {
        this();
        _header = m.getHeader();
        _isExtended = m.isExtended();
        _isRtr = m.isRtr();
        super.setBinary(true);
        setData(m.getData());
        setNumDataElements(m.getNumDataElements());
    }

    /**
     * Create a new CanReply from an existing CanMessage
     * @param m The existing CanMessage
     */
    public CanReply(@Nonnull CanMessage m) {
        this();
        _header = m.getHeader();
        _isExtended = m.isExtended();
        _isRtr = m.isRtr();
        super.setBinary(true);
        setData(java.util.Arrays.copyOf(m.getData(),m.getNumDataElements()));
        setNumDataElements(m.getNumDataElements());
    }

    /**
     * Hash on the header
     */
    @Override
    public int hashCode() {
        return _header;
    }

    /**
     * Note that a CanMessage and a CanReply can be tested for equality
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EQ_UNUSUAL",
        justification = "Equality test done in CanFrame")
    public boolean equals(Object a) {
        return isEqual(a,this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected int skipPrefix(int index) {
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
     * {@inheritDoc}
     */
    @Override
    public final void setNumDataElements(int n) {
        _nDataChars = (n <= 8) ? n : 8;
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
     * Get the data byte array.
     * @return the actual byte array, not a Copy Of
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
    public final void setHeader(int h) {
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

    /**
     * {@inheritDoc}
     * this format matches @CanMessage
     */
    @Override
    public String toString() {
        return getToString();
    }

    /**
     * {@inheritDoc}
     * this format matches @CanMessage
     */
    @Override
    public String toMonitorString() {
        return monString();
    }

    // contents (package access)
    int _header;
    boolean _isExtended;
    boolean _isRtr;
}
