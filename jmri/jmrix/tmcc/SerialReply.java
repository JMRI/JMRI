// SerialReply.java

package jmri.jmrix.tmcc;


/**
 * Contains the data payload of a TMCC serial reply
 * packet.  Note that _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006
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
            s+=jmri.util.StringUtil.twoHexFromInt(getElement(i));
        }
        return s;
    }

    public int getAsWord() {
        return (getElement(1)&0xFF)*256+(getElement(2)&0xFF);
    }

    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialReply.class.getName());

}

/* @(#)SerialReply.java */
