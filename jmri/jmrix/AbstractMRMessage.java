// AbstractMRMessage.java

package jmri.jmrix;

/**
 * Abstract base class for messages in a message/reply protocol.
 *
 * Carries a sequence of characters, with accessors.
 *
 * @author	        Bob Jacobsen  Copyright (C) 2003
 * @version             $Revision: 1.4 $
 */
abstract public class AbstractMRMessage {

    public AbstractMRMessage() {
        setBinary(false);
        setNeededMode(AbstractMRTrafficController.NORMALMODE);
        setTimeout(SHORT_TIMEOUT);  // default value is the short timeout
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
        setTimeout(m.getTimeout());
        setNeededMode(m.getNeededMode());
    }

    // from String
    public AbstractMRMessage(String s) {
        this(s.length());
        for (int i = 0; i<_nDataChars; i++)
            _dataChars[i] = s.charAt(i);
    }

    public void setOpCode(int i) { _dataChars[0]=i;}
    public int getOpCode() {return _dataChars[0];}
    public String getOpCodeHex() { return "0x"+Integer.toHexString(getOpCode()); }

    // accessors to the bulk data
    public int getNumDataElements() {return _nDataChars;}
    public int getElement(int n) {return _dataChars[n];}
    public void setElement(int n, int v) { _dataChars[n] = v; }

    // state info
    int mNeededMode;
    public void setNeededMode(int pMode) {mNeededMode = pMode; }
    public int getNeededMode() { return mNeededMode; }

    // mode accessors
    boolean _isBinary;
    public boolean isBinary() { return _isBinary; }
    public void setBinary(boolean b) { _isBinary = b; }

    /**
     * Minimum timeout that's acceptable.
     * <P>
     * Also used as default for normal operations.  Don't shorten
     * this "to make recovery faster", as sometimes <i>internal</i> delays
     * can slow processing down.
     * <P>
     * Units are milliseconds.
     */
    static protected int SHORT_TIMEOUT=2000;
    static protected int LONG_TIMEOUT=60000;  // e.g. for programming options
    int mTimeout;  // in milliseconds
    public void setTimeout(int t) { mTimeout = t; }
    public int getTimeout() { return mTimeout; }

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
