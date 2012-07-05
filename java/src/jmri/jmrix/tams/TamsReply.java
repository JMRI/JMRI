// TamsReply.java

package jmri.jmrix.tams;

/**
 * Carries the reply to an TamsMessage.
 * <P>
 * Some rudimentary support is provided for the "binary" option.
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version             $Revision: 18574 $
 */
public class TamsReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public  TamsReply() {
        super();
    }
    public TamsReply(String s) {
        super(s);
    }
    public TamsReply(TamsReply l) {
        super(l);
    }

    // these can be very large
    public int maxSize() { return 1000; }


    // no need to do anything
    protected int skipPrefix(int index) {
        return index;
    }
    //tams reply for a cv value, returns the decimal, hex then binary value.
    // 15 = $0F = %00001111
    public int value() {
        int index = 0;
        String s = ""+(char)getElement(index);
        if((char)getElement(index++) != ' '){
            s = s + (char)getElement(index);
        }
        if((char)getElement(index++) != ' '){
            s = s + (char)getElement(index);
        }
        s = s.trim();
        int val = -1;
        try {
            val = Integer.parseInt(s);
        } catch (Exception e) {
            log.error("Unable to get number from reply: \""+s+"\" index: "+index
                      +" message: \""+toString()+"\"");
        }
        log.info(val);
        return val;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TamsReply.class.getName());

}


/* @(#)TamsReply.java */


