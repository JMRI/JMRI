// NceReply.java

package jmri.jmrix.nce;

/**
 * Carries the reply to an NceMessage.
 * <P>
 * Some rudimentary support is provided for the "binary" option.
 *
 * @author		Bob Jacobsen  Copyright (C) 2001
 * @version             $Revision: 1.7 $
 */
public class NceReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public  NceReply() {
        super();
    }
    public NceReply(String s) {
        super(s);
    }
    public NceReply(NceReply l) {
        super(l);
    }

    int replyLen;    
    /**
     * Set the number of characters expected back from the 
     * command station.  Used in binary mode, where there's
     * no end-of-reply string to look for
     */
    public void setReplyLen(int len) { replyLen = len; }
    public int getReplyLen() { return replyLen; }

    protected int skipPrefix(int index) {
        // start at index, passing any control characters at the start of the buffer
        int len = "COMMAND: ".length();
        if ( getNumDataElements() >= index+len-1
             && 'C'== (char)getElement(index)
             && 'O'== (char)getElement(index+1)
             && 'M'== (char)getElement(index+2)
             && 'M'== (char)getElement(index+3)
             && 'A'== (char)getElement(index+4)
             && 'N'== (char)getElement(index+5)
             && 'D'== (char)getElement(index+6)
             && ':'== (char)getElement(index+7)
             && ' '== (char)getElement(index+8)  ) {
            index = index +"COMMAND: ".length();
        }
        return index;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceReply.class.getName());

}


/* @(#)NceReply.java */
