// CanReply.java

package jmri.jmrix.can;

import jmri.jmrix.AbstractMRReply;

/**
 * Base class for replies in a CANbus based message/reply protocol.
 * <P>
 * It is expected that any CAN based system will be based upon basic CANbus
 * concepts such as ID (standard or extended), Normal and RTR frames and
 * a data field.
 * <P>
 * The _dataChars[] and _nDataChars members refer to the data field, not the
 * entire message.
 * <p>
 * @author                      Andrew Crosland Copyright (C) 2008
 * @version         $Revision: 1.6 $
 */
public class CanReply extends AbstractMRReply {
        
    // Creates a new instance of CanMessage
    public CanReply() {
        _pri = 0;
        _id = 0x7a;
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
    public CanReply(int [] d) {
        this();
        _nDataChars = (d.length <= 8) ? d.length : 8;
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = d[i];
        }
    }
    
    // copy one
    public  CanReply(CanReply m) {
        if (m == null)
            log.error("copy ctor of null message");
        _pri = m._pri;
        _id = m._id;
        _isExtended = m._isExtended;
        _isRtr = m._isRtr;
        _nDataChars = m._nDataChars;
        setBinary(true);
        _dataChars = new int[_nDataChars];
        for (int i = 0; i<_nDataChars; i++)
            _dataChars[i] = m._dataChars[i];
    }
    
    /**
     * The following is really CBUS. 
     * It should be refactored to a separate CBUS place,
     * and also combined with the CanMessage version
     */
    public String toAddress() {
        if (getElement(0) == 0x90) {
            // + form
            return "+n"+(getElement(1)*256+getElement(2))+"e"+(getElement(3)*256+getElement(4));
        } else if (getElement(0) == 0x90) {
            // - form
            return "-n"+(getElement(1)*256+getElement(2))+"e"+(getElement(3)*256+getElement(4));
        } else {
            // hex form
            return "x"+toString().replaceAll(" ","");
        }      
    }
    
    protected int skipPrefix(int index) { return index; }
    
    // accessors to the bulk data
    public int getNumDataElements() { return _nDataChars;}
    public void setNumDataElements(int n) { _nDataChars = (n <= 8) ? n : 8;}
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
    
    public int getId() { return _id; }
    public void setId(int id) { _id = id; }
    public void setId(int id, boolean b) { _id = id; _isExtended = b; }
    public int getPri() { return _pri; }
    public void setPri(int pri) { _pri = pri; }
    public boolean isExtended() { return _isExtended; }
    public void setExtended(boolean b) { _isExtended = b; }
    public boolean isRtr() { return _isRtr; }
    public void setRtr(boolean b) { _isRtr = b; }
    
    // contents (private)
    protected int _pri;
    protected int _id;
    protected boolean _isExtended;
    protected boolean _isRtr;
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CanMessage.class.getName());
}

/* @(#)CanMessage.java */
