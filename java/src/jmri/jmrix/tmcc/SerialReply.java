// SerialReply.java

package jmri.jmrix.tmcc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Contains the data payload of a TMCC serial reply
 * packet.  Note that _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006
 * @version     $Revision$
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

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
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

    static Logger log = LoggerFactory.getLogger(SerialReply.class.getName());

}

/* @(#)SerialReply.java */
