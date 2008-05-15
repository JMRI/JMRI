/*
 * SerialReply.java
 *
 * Created on August 17, 2007, 8:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial;

/**
 *
 * @author tim
 */
/**
 * Contains the data payload of a TCH Technology NIC serial reply
 * packet.  Note that _only_ the payload, not
 * the header or trailer, nor the padding DLE characters
 * are included. But it does include addressing characters,
 * etc.
 * @author	Bob Jacobsen  Copyright (C) 2002
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
    public boolean isRcv()  { return getElement(1)==0xD9;} //R for RX
    public int getNA() { return getElement(0);}//Node Address '@'return getElement (0)-64;

    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialReply.class.getName());

}

/* @(#)SerialReply.java */
