package jmri.jmrix.can.adapters.lawicell;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.can.CanMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for messages for a LAWICELL CAN hardware adapter.
 * <p>
 * The Lawicell adapter protocol encodes messages as an ASCII string of up to 24
 * characters of the form: tiiildd...[CR] Tiiiiiiiildd...[CR] The t or T
 * indicates a standard or extended CAN frame iiiiiiii is the header as hex
 * digits l is the number of bytes of data dd are the (up to) 8 data bytes
 * <p>
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2008, 2009
 */
public class Message extends AbstractMRMessage {

    static final int MAXLEN = 27;

    public Message() {
        _nDataChars = MAXLEN;
        _dataChars = new int[_nDataChars];
    }

    public Message(CanMessage m) {
        this();

        // Standard or extended frame?
        setExtended(m.isExtended());

        // CAN header
        int index = 1;
        index = setHeader(m.getHeader(), index);

        // don't know how to assert RTR in this protocol
        if (m.isRtr()) {
            log.error("Lawicell protocol cannot assert RTR");
        }

        // length
        setHexDigit(m.getNumDataElements(), index++);

        // Data payload
        for (int i = 0; i < m.getNumDataElements(); i++) {
            setHexDigit((m.getElement(i) >> 4) & 0x0F, index++);
            setHexDigit(m.getElement(i) & 0x0F, index++);
        }
        // Terminator
        setElement(index++, 0x0D);
        setNumDataElements(index);
    }

    // accessors to the bulk data
    @Override
    public int getNumDataElements() {
        return _nDataChars;
    }

    public void setNumDataElements(int n) {
        _nDataChars = (n <= MAXLEN) ? n : MAXLEN;
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
        int len = (d.length <= MAXLEN) ? d.length : MAXLEN;
        for (int i = 0; i < len; i++) {
            _dataChars[i] = d[i];
        }
    }

    public void setExtended(boolean extended) {
        if (extended) {
            // extended
            setElement(0, 'T');
        } else {
            // standard
            setElement(0, 't');
        }
    }

    public boolean isExtended() {
        return getElement(0) == 'T';
    }

    /**
     * Set the CAN header as ASCII hex digits. Handles extended/standard
     * internally
     *
     * @param header A valid CAN header value
     * @return index to next bytes, after this
     */
    public int setHeader(int header, int index) {
        if (isExtended()) {
            // extended MSB part
            setHexDigit((header >> 28) & 0xF, index++);
            setHexDigit((header >> 24) & 0xF, index++);
            setHexDigit((header >> 20) & 0xF, index++);
            setHexDigit((header >> 16) & 0xF, index++);
            setHexDigit((header >> 12) & 0xF, index++);
        }
        // standard part
        setHexDigit((header >> 8) & 0xF, index++);
        setHexDigit((header >> 4) & 0xF, index++);
        setHexDigit((header >> 0) & 0xF, index++);

        return index;
    }

    /**
     * Set a byte as two ASCII hex digits
     * <p>
     * Data bytes are encoded as two ASCII hex digits starting at byte 7 of the
     * message.
     *
     * @param val the value to set
     * @param n   the index of the byte to be set
     */
    public void setByte(int val, int n) {
        if ((n >= 0) && (n <= 7)) {
            int index = n * 2 + 7;
            setHexDigit((val / 16) & 0xF, index++);
            setHexDigit(val & 0xF, index);
        }
    }

    // Set a hex digit at offset n in _dataChars
    void setHexDigit(int val, int n) {
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

    private final static Logger log = LoggerFactory.getLogger(Message.class);
}


