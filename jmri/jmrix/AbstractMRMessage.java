// AbstractMRMessage.java

package jmri.jmrix;

/**
 * Abstract base class for messages in a message/reply protocol.
 *
 * Carries a sequence of characters, with accessors.
 *
 * @author	        Bob Jacobsen  Copyright (C) 2003
 * @version             $Revision: 1.1 $
 */
abstract public class AbstractMRMessage {

    public AbstractMRMessage() {
        setBinary(false);
        setNeededState(AbstractMRTrafficController.NORMAL);
    }

    // create a new one
    public  AbstractMRMessage(int i) {
        this();
        if (i<1)
            log.error("invalid length in call to ctor");
        _nDataChars = i;
        _dataChars = new int[i];
    }

    // copy one
    public  AbstractMRMessage(AbstractMRMessage m) {
        this();
        if (m == null)
            log.error("copy ctor of null message");
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
    }

    public void setOpCode(int i) { _dataChars[0]=i;}
    public int getOpCode() {return _dataChars[0];}
    public String getOpCodeHex() { return "0x"+Integer.toHexString(getOpCode()); }

    // accessors to the bulk data
    public int getNumDataElements() {return _nDataChars;}
    public int getElement(int n) {return _dataChars[n];}
    public void setElement(int n, int v) { _dataChars[n] = v; }

    // state info
    int mNeededState;
    public void setNeededState(int pState) {mNeededState = pState; }

    // mode accessors
    boolean _isBinary;
    public boolean isBinary() { return _isBinary; }
    public void setBinary(boolean b) { _isBinary = b; }

    // display format
    public String toString() {
        String s = "";
        for (int i=0; i<_nDataChars; i++) {
            if (_isBinary) {
                if (i!=0) s+=" ";
                if (_dataChars[i] < 16) s+="0";
                s+=Integer.toHexString(_dataChars[i]);
            } else {
                s+=(char)_dataChars[i];
            }
        }
        return s;
    }

    // contents (private)
    private int _nDataChars = 0;
    private int _dataChars[] = null;

    public void addIntAsThree(int val, int offset) {
        String s = ""+val;
        if (s.length() != 3) s = "0"+s;  // handle <10
        if (s.length() != 3) s = "0"+s;  // handle <100
        setElement(offset,s.charAt(0));
        setElement(offset+1,s.charAt(1));
        setElement(offset+2,s.charAt(2));
        return;
    }

    public void addIntAsTwoHex(int val, int offset) {
        String s = (""+Integer.toHexString(val)).toUpperCase();
        if (s.length() < 2) s = "0"+s;  // handle one digit
        if (s.length() > 2) log.error("can't add as two hex digits: "+s);
        setElement(offset,s.charAt(0));
        setElement(offset+1,s.charAt(1));
        return;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractMRMessage.class.getName());

}


/* @(#)AbstractMRMessage.java */
