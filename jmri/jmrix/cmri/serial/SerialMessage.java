// SerialMessage.java

package jmri.jmrix.cmri.serial;


/**
 * Contains the data payload of a CMRI serial
 * packet.  Note that _only_ the payload, not
 * the header or trailer, nor the padding DLE characters
 * are included. These are added during transmission.
 * @author    Bob Jacobsen  Copyright (C) 2001
 * @version   $Revision: 1.5 $
 */

public class SerialMessage extends jmri.jmrix.AbstractMRMessage {
    // is this logically an abstract class?

    public SerialMessage() {
        super();
    }

    // create a new one
    public  SerialMessage(int i) {
        super(i);
    }

    // copy one
    public  SerialMessage(SerialMessage m) {
        super(m);
    }

    // from String
    public  SerialMessage(String m) {
        super(m);
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

    // static methods to recognize a message
    public boolean isPoll() { return getElement(1)==0x50;}
    public boolean isXmt()  { return getElement(1)==0x54;}
    public boolean isInit() { return (getElement(1)==0x49); }
    public int getUA() { return getElement(0)-65; }

    // static methods to return a formatted message
    static public SerialMessage getPoll(int UA) {
        SerialMessage m = new SerialMessage(2);
        m.setElement(0, 65+UA);
        m.setElement(1, 0x50); // 'P'
        return m;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMessage.class.getName());

}


/* @(#)SerialMessage.java */
