// Message.java

package jmri.jmrix.can.adapters.lawicell;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.can.CanMessage;

/**
 * Class for messages for a LAWICELL CAN hardware adapter.
 * <P>
 * The GridConnect protocol encodes messages as an ASCII string of up to 24
 * characters of the form:
 *      ;ShhhhNd0d1d2d3d4d5d6d7:
 * The S indicates a standard CAN frame
 * hhhh is the two byte header
 * N or R indicates a normal or remote frame
 * d0 - d7 are the (up to) 8 data bytes
 * <P>
 *
 * @author      Andrew Crosland Copyright (C) 2008
 * @author      Bob Jacobsen Copyright (C) 2008
 * @version	    $Revision: 1.1 $
 */
public class Message extends AbstractMRMessage {
    
    // Creates a new instance of GridConnectMessage
    public Message() {
        _nDataChars = 24;
        _dataChars = new int[24];
    }

    // create a new one of given length
    public Message(int i) {
	this();
        _nDataChars = (i <= 24) ? i : 24;
    }

    // create a new one from an array
    public Message(int [] d) {
	this();
	int _nDataChars = (d.length <= 24) ? d.length : 24;
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = d[i];
        }
    }

    // copy one
    public  Message(Message m) {
        if (m == null)
            log.error("copy ctor of null message");
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
    }

    // accessors to the bulk data
    public int getNumDataElements() { return _nDataChars;}
    public void setNumDataElements(int n) { _nDataChars = (n <= 24) ? n : 24;}
    public int getElement(int n) {return _dataChars[n];}
    public void setElement(int n, int v) {
      _dataChars[n] = v;
    }

    public void setData(int [] d) {
        int len = (d.length <=24) ? d.length : 24;
        for (int i = 0; i < len; i++) {
            _dataChars[i] = d[i];
        }
    }
    
    /**
     * Set the ID as ASCII hex digits.
     * Handles extended/standard internally
     *
     * @param id A valid CAN ID
     * @return index to next bytes, after this
     */
    public int setID(int id, boolean extended, int index) {
        if (extended) {
            // extended MSB part
            setHexDigit((id>>28)&0xF, index++);
            setHexDigit((id>>24)&0xF, index++);
            setHexDigit((id>>20)&0xF, index++);
            setHexDigit((id>>16)&0xF, index++);
            setHexDigit((id>>12)&0xF, index++);
        }
        // standard part
        setHexDigit((id>>8)&0xF, index++);
        setHexDigit((id>>4)&0xF, index++);
        setHexDigit((id>>0)&0xF, index++);
        
        return index;
    }

    /**
     * Set a byte as two ASCII hex digits
     * <P>
     * Data bytes are encoded as two ASCII hex digits starting at byte 7 of the
     * message.
     *
     * @param val the value to set
     * @param n the index of the byte to be set
     */
    public void setByte(int val, int n) {
        if ((n >= 0) && (n <= 7)) {
            int index = n*2 + 7;
            setHexDigit((val/16)&0xF, index++);
            setHexDigit( val    &0xF, index);
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
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Message.class.getName());
}

/* @(#)Message.java */
