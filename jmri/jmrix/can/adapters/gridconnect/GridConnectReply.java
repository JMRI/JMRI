// GridConnectReply.java

package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.can.CanReply;

/**
 * Class for replies in a GridConnect based message/reply protocol.
 * <P>
 *
 * @author                      Andrew Crosland Copyright (C) 2008
 * @version			$Revision: 1.4 $
 */
public class GridConnectReply extends AbstractMRReply {
    
    // Creates a new instance of GridConnectReply
    public GridConnectReply() {
        _nDataChars = 0;
        _dataChars = new int[24];
    }

    // create a new one of given length
    public GridConnectReply(int i) {
	this();
        _nDataChars = (i <= 24) ? i : 24;
    }

    // create a new one from an array
    public GridConnectReply(int [] d) {
	this();
	int _nDataChars = (d.length <= 24) ? d.length : 24;
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = d[i];
        }
    }

    // copy one
    public  GridConnectReply(GridConnectReply m) {
        if (m == null)
            log.error("copy ctor of null message");
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
    }

    protected int skipPrefix(int index) {
        while (_dataChars[index] == ':') { index++; }
        return index;
    }

    // accessors to the bulk data
    public int getNumDataElements() { return _nDataChars;}
    public void setNumDataElements(int n) { _nDataChars = (n <= 24) ? n : 24;}
    public int getElement(int n) {return _dataChars[n];}
    public void setElement(int n, int v) {
        if (n<24) {
            _dataChars[n] = v;
            _nDataChars = Math.max(_nDataChars, n+1);
        }
    }

    public int maxSize() { return 24; }
    
    public void setData(int [] d) {
        int len = (d.length <=24) ? d.length : 24;
        for (int i = 0; i < len; i++) {
            _dataChars[i] = d[i];
        }
    }

    /**
     * Get the CBUS ID as an int
     *
     * @return int the CBUS ID
     */        
    public int getID() {
        return (getHexDigit(3)<<3) + (getHexDigit(4)>>1);
    }
    
    /**
     * Get the CBUS Priority as an int
     *
     * @return int the CBUS priority
     */        
    public int getPri() {
        return getHexDigit(2);
    }
    
    /**
     * Get the number of data bytes
     *
     * @return int the number of bytes
     */
    public int getNumBytes() {
        // subtract framing and ID bytes, etc and each byte is two ASCII hex digits
        return (_nDataChars - 8)/2;
    }
    
    /**
     * Get a hex data byte from the message
     * <P>
     * Data bytes are encoded as two ASCII hex digits starting at byte 7 of the
     * message.
     *
     * @param b The byte offset (0 - 7)
     * @return The value
     */
    public int getByte(int b) {
        if ((b >= 0) && (b <= 7)) {
            int index = b*2 + 7;
            int hi = getHexDigit(index++);
            int lo = getHexDigit(index);
            if ((hi < 16) && (lo < 16)) {
                return (hi*16 + lo);
            }
        }
        return 0;
    }
    
    // Get a single hex digit. returns 0 if digit is invalid
    private int getHexDigit(int index) {
        int b = 0;
        b = _dataChars[index];
        if ((b >= '0') && (b <= '9')) {
            b = b - '0';
        } else if ((b >= 'A') && (b <= 'F')) {
            b = b - 'A' + 10;
        } else if ((b >= 'a') && (b <= 'f')) {
            b = b - 'a' + 10;
        } else {
            b = 0;
        }
        return (byte)b;
    }
   
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(GridConnectReply.class.getName());
}

/* @(#)GridConnectReply.java */
