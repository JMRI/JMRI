package jmri.jmrix.can;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nonnull;
import jmri.jmrix.AbstractMRReply;
import jmri.util.StringUtil;

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

    // Creates a new instance of CanMessage
    public CanReply() {
        _isExtended = false;
        _isRtr = false;
        _nDataChars = 8;
        setBinary(true);
        _dataChars = new int[8];
    }

    // create a new one of given length
    public CanReply(int i) {
        this();
        _nDataChars = (i <= 8) ? i : 8;
    }

    // create a new one from an array
    public CanReply(int[] d) {
        this();
        _nDataChars = (d.length <= 8) ? d.length : 8;
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = d[i];
        }
    }
    
    // create a new one from an array, with header
    public CanReply(int[] d, int header) {
        this(header);
        _nDataChars = (d.length <= 8) ? d.length : 8;
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = d[i];
        }
    }

    // copy one
    public CanReply(@Nonnull CanReply m) {
        _header = m._header;
        _isExtended = m._isExtended;
        _isRtr = m._isRtr;
        _nDataChars = m._nDataChars;
        setBinary(true);
        _dataChars = new int[_nDataChars];
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = m._dataChars[i];
        }
    }

    // copy type
    public CanReply(@Nonnull CanMessage m) {
        _header = m._header;
        _isExtended = m._isExtended;
        _isRtr = m._isRtr;
        _nDataChars = m.getNumDataElements();
        setBinary(true);
        _dataChars = new int[_nDataChars];
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = m.getElement(i);
        }
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
    public boolean equals(Object a) {
        if (a == null) {
            return false;
        }
        // check for CanFrame equality, that's sufficient
        if (a instanceof CanFrame) {
            CanFrame m = (CanFrame) a;
            if ((_header != m.getHeader()) || (_isRtr != m.isRtr()) || (_isExtended != m.isExtended())) {
                return false;
            }
            if (_nDataChars != m.getNumDataElements()) {
                return false;
            }
            for (int i = 0; i < _nDataChars; i++) {
                if (_dataChars[i] != m.getElement(i)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected int skipPrefix(int index) {
        return index;
    }

    // accessors to the bulk data
    @Override
    public int getNumDataElements() {
        return _nDataChars;
    }

    @Override
    public void setNumDataElements(int n) {
        _nDataChars = (n <= 8) ? n : 8;
    }

    @Override
    public int getElement(int n) {
        return _dataChars[n];
    }

    @Override
    public void setElement(int n, int v) {
        _dataChars[n] = v;
    }

    public void setData(int[] d) {
        int len = (d.length <= 8) ? d.length : 8;
        for (int i = 0; i < len; i++) {
            _dataChars[i] = d[i];
        }
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK to expose array, can be directly manipulated
    public int[] getData() {
        return _dataChars;
    }

    // CAN header
    @Override
    public int getHeader() {
        return _header;
    }

    @Override
    public void setHeader(int h) {
        _header = h;
    }

    @Override
    public boolean isExtended() {
        return _isExtended;
    }

    @Override
    public void setExtended(boolean b) {
        _isExtended = b;
    }

    @Override
    public boolean isRtr() {
        return _isRtr;
    }

    @Override
    public void setRtr(boolean b) {
        _isRtr = b;
    }

    /**
     * {@inheritDoc}
     * default toString does not contain the header
     * this format matches @CanMessage
     */
    @Override
    public String toString() {
        String s = String.format("[%x] ", _header);
        for (int i = 0; i < _nDataChars; i++) {
            if (i != 0) {
                s += " ";
            }
            s = StringUtil.appendTwoHexFromInt(_dataChars[i] & 255, s);
        }
        return s;
    }

    @Override
    public String toMonitorString() {
        StringBuffer buf = new StringBuffer();
        buf.append("(" + Integer.toHexString(getHeader())
                + (isExtended() ? " ext)" : ")"));
        for (int i = 0; i < getNumDataElements(); i++) 
        {
            buf.append(" " + jmri.util.StringUtil.twoHexFromInt(getElement(i)));
        }
	return buf.toString();
    }

    // contents (package access)
    int _header;
    boolean _isExtended;
    boolean _isRtr;
}
