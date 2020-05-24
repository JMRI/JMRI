package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.can.CanMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for GridConnect messages for a CAN hardware adapter.
 * <p>
 * The GridConnect protocol encodes messages as an ASCII string of up to 24
 * characters of the form: :ShhhhNd0d1d2d3d4d5d6d7;
 * <p>
 * The S indicates a standard
 * CAN frame :XhhhhhhhhNd0d1d2d3d4d5d6d7; The X indicates an extended CAN frame
 * hhhh is the two byte header N or R indicates a normal or remote frame, in
 * position 6 or 10 d0 - d7 are the (up to) 8 data bytes
 * <p>
 *
 * @author Andrew Crosland Copyright (C) 2008
 */
public class GridConnectMessage extends AbstractMRMessage {

    /**
     * Create a new instance of GridConnectMessage.
     */
    public GridConnectMessage() {
        _nDataChars = 28;
        _dataChars = new int[_nDataChars];
        setElement(0, ':');
    }

    /**
     * Create a new GridConnectMessage from CanMessage.
     * @param m CanMessage outgoing from JMRI.
     */
    public GridConnectMessage(CanMessage m) {
        this();

        // Standard or extended frame
        setExtended(m.isExtended());

        // Copy the header
        setHeader(m.getHeader());

        // Normal or Remote frame?
        setRtr(m.isRtr());

        // Data payload
        for (int i = 0; i < m.getNumDataElements(); i++) {
            setByte(m.getElement(i), i);
        }
        // Terminator
        int offset = isExtended() ? 11 : 6;
        setElement(offset + m.getNumDataElements() * 2, ';');
        setNumDataElements(offset + 1 + m.getNumDataElements() * 2);
        log.debug("encoded as {}", this.toString());
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
     * @param n number Elements.  Max 28.
     */
    public void setNumDataElements(int n) {
        _nDataChars = (n <= 28) ? n : 28;
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
     * Set data from array.
     * @param d array, max length 24.
     */
    public void setData(int[] d) {
        int len = (d.length <= 24) ? d.length : 24;
        System.arraycopy(d, 0, _dataChars, 0, len);
    }

    /**
     * Set the GC Message as Extended.
     * @param extended true for extended, else false
     */
    public void setExtended(boolean extended) {
        // Standard or extended frame
        if (extended) {
            setElement(1, 'X');
        } else {
            setElement(1, 'S');
        }
    }

    /**
     * Get if the GC Message is Extended.
     * @return true for extended, else false
     */
    public boolean isExtended() {
        return getElement(1) == 'X';
    }

    /**
     * Set the header.
     *
     * @param header A valid CAN header value.
     */
    public void setHeader(int header) {
        if (isExtended()) {
            setHexDigit((header >> 28) & 0xF, 2);
            setHexDigit((header >> 24) & 0xF, 3);
            setHexDigit((header >> 20) & 0xF, 4);
            setHexDigit((header >> 16) & 0xF, 5);
            setHexDigit((header >> 12) & 0xF, 6);
            setHexDigit((header >> 8) & 0xF, 7);
            setHexDigit((header >> 4) & 0xF, 8);
            setHexDigit(header & 0xF, 9);
        } else {
            setHexDigit((header >> 8) & 0xF, 2);
            setHexDigit((header >> 4) & 0xF, 3);
            setHexDigit(header & 0xF, 4);
        }
    }

    /**
     * Set CAN Frame as RtR.
     * @param rtr true to set rtr, else false.
     */
    public void setRtr(boolean rtr) {
        int offset = isExtended() ? 10 : 5;
        setElement(offset, rtr ? 'R' : 'N');
    }

    /**
     * Set a byte as two ASCII hex digits.
     * <p>
     * Data bytes are encoded as two ASCII hex digits starting at byte 7 of the
     * message.
     *
     * @param val the value to set.
     * @param n   the index of the byte to be set.
     */
    public void setByte(int val, int n) {
        if ((n >= 0) && (n <= 7)) {
            int index = n * 2 + (isExtended() ? 11 : 6);
            setHexDigit((val / 16) & 0xF, index++);
            setHexDigit(val & 0xF, index);
        }
    }

    /**
     * Set a hex digit at offset n in _dataChars.
     * @param val min 0, max value 15.
     * @param n _dataChars Array Index.
     */
    protected void setHexDigit(int val, int n) {
        if ((val >= 0) && (val <= 15)) {
            if (val < 10) {
                _dataChars[n] = val + '0';
            } else {
                _dataChars[n] = val - 10 + 'A';
            }
        } else {
            _dataChars[n] = '0';
        }
    }

    private final static Logger log = LoggerFactory.getLogger(GridConnectMessage.class);
}


