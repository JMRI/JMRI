// CanMessage.java

package jmri.jmrix.can;

import jmri.jmrix.AbstractMRMessage;

/**
 * Base class for messages in a CANbus based message/reply protocol.
 * <P>
 * It is expected that any CAN based system will be based upon basic CANbus
 * concepts such as ID (standard or extended), Normal and RTR frames and
 * a data field.
 * <P>
 * The _dataChars[] and _nDataChars members refer to the data field, not the
 * entire message.
 *
 * @author                      Andrew Crosland Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */
public class CanMessage extends AbstractMRMessage {
    
    // *** Needs toString() method to handle _id etc
    
    // tag whether translation is needed.
    // a "native" message has been converted already
    boolean _translated = false;
    public void setTranslated(boolean translated) { _translated = translated; }
    public boolean isTranslated() { return _translated; }
    
    // Creates a new instance of CanMessage
    public CanMessage() {
	    _id = 0x7aa;            // can't have too many ones in header
	    _isExtended = false;
	    _isRtr = false;
        _nDataChars = 8;
        setBinary(true);
        _dataChars = new int[8];
    }

    // create a new one of given length
    public CanMessage(int i) {
	    this();
        _nDataChars = (i <= 8) ? i : 8;
    }

    // create a new one from an array
    public CanMessage(int [] d) {
	    this();
	    _nDataChars = (d.length <= 8) ? d.length : 8;
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = d[i];
        }
    }

    // copy one
    public  CanMessage(CanMessage m) {
        if (m == null)
            log.error("copy ctor of null message");
	    _id = m._id;
	    _isExtended = m._isExtended;
	    _isRtr = m._isRtr;
        setBinary(true);
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
    }

    // accessors to the bulk data
    public int getNumDataElements() { return _nDataChars;}
    public int getElement(int n) {return _dataChars[n];}
    public void setElement(int n, int v) {
      _dataChars[n] = v;
    }

    public void setData(int [] d) {
        int len = (d.length <=8) ? d.length : 8;
        for (int i = 0; i < len; i++) {
            _dataChars[i] = d[i];
        }
    }

    public boolean replyExpected() { return false; }
    
    public int getId() { return _id; }
    public void setId(int id) { _id = id; }
    public void setId(int id, boolean b) { _id = id; _isExtended = b; }
    public boolean isExtended() { return _isExtended; }
    public void setExtended(boolean b) { _isExtended = b; }
    public boolean isRtr() { return _isRtr; }
    public void setRtr(boolean b) { _isRtr = b; }
    public static int getProtocol() { return _protocol; }
    public static void setProtocol(int p) { _protocol = p; }

    // contents (private)
    protected int _id;
    protected boolean _isExtended;
    protected boolean _isRtr;
    
    // CAN protocols supported
    protected static int _protocol;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CanMessage.class.getName());
}

/* @(#)CanMessage.java */
