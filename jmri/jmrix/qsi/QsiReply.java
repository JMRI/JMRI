// QsiReply.java

package jmri.jmrix.qsi;

/**
 * Carries the reply to an QsiMessage
 * @author			Bob Jacobsen  Copyright (C) 2007
 * @version			$Revision: 1.3 $
 */
public class QsiReply extends jmri.jmrix.AbstractMessage {
    static final int MAXREPLYLENGTH = 200;

    // create a new one
    public  QsiReply() {
        super(MAXREPLYLENGTH);
        _isBoot = false;
        _nDataChars = 0;
    }
    
    // copy one
    public  QsiReply(QsiReply m) {
        super(m);
        _isBoot = m._isBoot;
    }
    
    // from String
    public QsiReply(String s) {
        super(s);
    }
    
    // from String
    public QsiReply(String s, boolean b) {
        super(s);
        _isBoot = b;
    }
    
    public void setOpCode(int i) { _dataChars[0]= (char)i;}
    public int getOpCode() {return _dataChars[0];}
    
    // accessors to the bulk data
    public void setElement(int n, int v) {
        _dataChars[n] = v;
        _nDataChars = Math.max(_nDataChars, n+1);
    }
    
    // Check and strip framing characters and DLE from a QSI bootloader reply
    public boolean strip() {
        int tmp[] = new int[_nDataChars];
        int j = 0;
        _isBoot = true; // definitely a boot message
        // Check framing characters
        if (_dataChars[0] != QsiMessage.STX) {return false;}
        if (_dataChars[1] != QsiMessage.STX) {return false;}
        if (_dataChars[_nDataChars-1] != QsiMessage.ETX) {return false;}
        
        // Ignore framing characters and strip DLEs
        for (int i = 2; i < _nDataChars - 1; i++) {
            if (_dataChars[i] == QsiMessage.DLE) {i++;}
            tmp[j++] = _dataChars[i];
        }
        
        // Copy back to original QsiReply
        for (int i = 0; i < j; i++) {
            _dataChars[i] = tmp[i];
        }
        _nDataChars = j;
        return true;
    }
    
    // Check and strip checksum from a QSI bootloader reply
    // Assumes framing and DLE chars have been stripped
    public boolean getChecksum() {
        int checksum = 0;
        for (int i = 0; i < _nDataChars; i++) {
            checksum += (int)(_dataChars[i] & 0xff);
        }
        _nDataChars--;
        return ((checksum & 0xff) == 0);
    }
    
    // display format
    public String toString() {
        String s = "";
        if (!QsiTrafficController.instance().isSIIBootMode()) {
            for (int i=0; i<_nDataChars; i++) {
                s+=jmri.util.StringUtil.twoHexFromInt(_dataChars[i])+" ";
            }
        } else {
            for (int i=0; i<_nDataChars; i++) {
                s+="<"+(int)_dataChars[i]+">";
            }
        }
        return s;
    }
    
    /**
     * Extracts Read-CV returned value from a message.  Returns
     * -1 if message can't be parsed.
     *
     * QSI is assumed to not be echoing commands. A reply to a command
     * may include the prompt that was printed after the previous command
     * Reply to a CV read is of the form " = hvv" where vv is the CV value
     * in hex
     */
    public int value() {
        int index = 0;
        index = skipWhiteSpace(index);
        index = skipEqual(index);
        index = skipWhiteSpace(index);
        String s1 = ""+(char)getElement(index);
        String s2 = ""+(char)getElement(index+1);
        int val = -1;
        try {
            int sum = Integer.valueOf(s2,16).intValue();
            sum += 16*Integer.valueOf(s1,16).intValue();
            val = sum;  // don't do this assign until now in case the conversion throws
        } catch (Exception e) {
            log.error("Unable to get number from reply: \""+s1+s2+"\" index: "+index
                      +" message: \""+toString()+"\"");
        }
        return val;
    }
    
    int match(String s) {
        // find a specific string in the reply
        String rep = new String(_dataChars, 0, _nDataChars);
        return rep.indexOf(s);
    }
    
    int skipWhiteSpace(int index) {
        // start at index, passing any whitespace & control characters at the start of the buffer
        while (index < getNumDataElements()-1 &&
               ((char)getElement(index) <= ' '))
            index++;
        return index;
    }
    
    int skipEqual(int index) {
        // start at index, skip over the equals and hex prefix
        int len = "= h".length();
        if ( getNumDataElements() >= index+len-1
             && '='== (char)getElement(index)
             && ' '== (char)getElement(index+1)
             && 'h'== (char)getElement(index+2)  ) {
            index += len;
        }
        return index;
    }
    
    // Longest boot reply is 256bytes each preceded by DLE + 2xSTX + ETX
    static public int maxSize = 515;
    
    // contents (private)
    private boolean _isBoot = false;
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(QsiReply.class.getName());
}

/* @(#)QsiReply.java */
