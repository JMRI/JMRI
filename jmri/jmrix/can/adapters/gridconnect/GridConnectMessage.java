// GridConnectMessage.java

package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.can.CanMessage;

/**
 * Class for GridConnect messages for a CAN hardware adapter.
 * <P>
 * The GridConnect protocol encodes messages as an ASCII string of up to 24
 * characters of the form:
 *      :ShhhhNd0d1d2d3d4d5d6d7;
 * The S indicates a standard CAN frame
 * hhhh is the two byte header
 * N or R indicates a normal or remote frame
 * d0 - d7 are the (up to) 8 data bytes
 * <P>
 *
 * @author                      Andrew Crosland Copyright (C) 2008
 * @version			$Revision: 1.4 $
 */
public class GridConnectMessage extends AbstractMRMessage {
    
    // Creates a new instance of GridConnectMessage
    public GridConnectMessage() {
        _nDataChars = 24;
        _dataChars = new int[24];
    }

    // create a new one of given length
    public GridConnectMessage(int i) {
	this();
        _nDataChars = (i <= 24) ? i : 24;
    }

    // create a new one from an array
    public GridConnectMessage(int [] d) {
	this();
	int _nDataChars = (d.length <= 24) ? d.length : 24;
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = d[i];
        }
    }

    // copy one
    public  GridConnectMessage(GridConnectMessage m) {
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
     * Set the ID as three ASCII hex digits
     *
     * @param id A valid CBus ID
     */
    public void setID(int id) {
        setHexDigit((id>>3)&0xF, 3);
        setHexDigit((id<<1)&0xF, 4);
        setHexDigit( 0, 5);
    }

    /**
     * Set the Priority
     *
     * @param pri A valid CBUS Priority
     */
    public void setPri(int pri) {
        setHexDigit(pri&0xF, 2);
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
    private void setHexDigit(int val, int n) {
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
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(GridConnectMessage.class.getName());
}

/* @(#)GridConnectMessage.java */
