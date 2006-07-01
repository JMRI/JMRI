// AbstractMRReply.java

package jmri.jmrix;

/**
 * Abstract base class for replies in a message/reply protocol.
 * <P>
 * Handles the character manipulation.
 *
 * @author		Bob Jacobsen  Copyright (C) 2003
 * @version             $Revision: 1.3 $
 */
abstract public class AbstractMRReply {
    // is this logically an abstract class?

    // create a new one
    public  AbstractMRReply() {
        setBinary(false);
    }

    // copy one
    public  AbstractMRReply(AbstractMRReply m) {
        this();
        if (m == null)
            log.error("copy ctor of null message");
        _nDataChars = m._nDataChars;
        for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
    }

    // from String
    public AbstractMRReply(String s) {
        this();
        _nDataChars = s.length();
        for (int i = 0; i<_nDataChars; i++)
            _dataChars[i] = s.charAt(i);
    }

    public void setOpCode(int i) { _dataChars[0]= (char)i;}
    public int getOpCode() {return _dataChars[0];}

    // accessors to the bulk data
    public int getNumDataElements() {return _nDataChars;}
    public int getElement(int n) {return _dataChars[n];}
    public void setElement(int n, int v) {
        _dataChars[n] = (char) v;
        _nDataChars = Math.max(_nDataChars, n+1);
    }

    // mode accessors
    private boolean _isBinary;
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

    abstract protected int skipPrefix(int index);

    public int value() {  // integer value of 1st three digits
        int index = 0;
        index = skipWhiteSpace(index);
        index = skipPrefix(index);
        index = skipWhiteSpace(index);
        String s = ""+(char)getElement(index)+(char)getElement(index+1)+(char)getElement(index+2);
        int val = -1;
        try {
            val = Integer.parseInt(s);
        } catch (Exception e) {
            log.error("Unable to get number from reply: \""+s+"\" index: "+index
                      +" message: \""+toString()+"\"");
        }
        return val;
    }

    public int pollValue() {  // integer value of HHHH
        int index = 0;
        index = skipWhiteSpace(index);
        index = skipPrefix(index);
        index = skipWhiteSpace(index);
        String s = ""+(char)getElement(index)+(char)getElement(index+1)
                    +(char)getElement(index+2)+(char)getElement(index+3);
        int val = -1;
        try {
            val = Integer.parseInt(s,16);
        } catch (Exception e) {
            log.error("Unable to get number from reply: \""+s+"\" index: "+index
                      +" message: \""+toString()+"\"");
        }
        return val;
    }

    public int match(String s) {
        // find a specific string in the reply
        String rep = new String(_dataChars, 0, _nDataChars);
        return rep.indexOf(s);
    }

    public int skipWhiteSpace(int index) {
        // start at index, passing any whitespace & control characters at the start of the buffer
        while (index < getNumDataElements()-1 &&
               ((char)getElement(index) <= ' '))
            index++;
        return index;
    }

    static public final int maxSize = 120;

    // contents (private)
    private int _nDataChars;
    private char _dataChars[] = new char[maxSize];

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractMRReply.class.getName());

}


/* @(#)AbstractMRReply.java */
