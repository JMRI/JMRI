// SerialReply.java

package jmri.jmrix.maple;


/**
 * Contains the data payload of a serial reply
 * packet.  Note that _only_ the payload, not
 * the header or trailer, nor the padding DLE characters
 * are included. But it does include addressing characters,
 * etc.
 * @author	Bob Jacobsen  Copyright (C) 2002, 2008
 * @version     $Revision: 1.1 $
 */
public class SerialReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public  SerialReply() {
        super();
    }
    public SerialReply(String s) {
        super(s);
    }
    public SerialReply(SerialReply l) {
        super(l);
    }

    // display format
    public String toString() {
        String s = "";
        for (int i=0; i<getNumDataElements(); i++) {
            if (i!=0) s+=" ";
            if (getElement(i) < 16) s+="0";
            s+=Integer.toHexString(getElement(i)&0xFF);
        }
        return s;
    }

    // recognize format
    public boolean isRcv()  { return getElement(0)==0x02;}
    public int getUA() { 
        int addr = (getElement(1)-'0')*10 + (getElement(2)-'0');
        return addr;
    }

    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialReply.class.getName());

}

/* @(#)SerialReply.java */
