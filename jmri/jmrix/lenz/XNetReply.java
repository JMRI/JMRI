// XNetMessage.java

package jmri.jmrix.lenz;

/**
 * Represents a single response from the XpressNet.
 *<P>
 *
 * @author			Paul Bender Copyright (C) 2004
 * @version			$Revision: 1.1 $
 *
 */
public class XNetReply extends jmri.jmrix.AbstractMRReply {

   // Create a new reply.
    public XNetReply() {
       super();
       setBinary(true);
    }

   // Create a new reply from an existing reply
    public XNetReply(XNetReply reply) {
       super(reply);
       setBinary(true);
    }

   /** 
    * Create a reply from an XNetMessage.
    */
    public XNetReply(XNetMessage message) {
       super(message.toString()); 
       setBinary(true);
    }


    /* Get the opcode as a string in hex format */
    public String getOpCodeHex() { return "0x"+Integer.toHexString(this.getOpCode()); }

        /**
         * check whether the message has a valid parity
         */
        public boolean checkParity() {
                int len = getNumDataElements();
                int chksum = 0x00;  /* the seed */
                int loop;
        
        for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
                chksum ^= getElement(loop);
        }
                return ((chksum&0xFF) == getElement(len-1));
        }
    
    public void setParity() {
                int len = getNumDataElements();   
                int chksum = 0x00;  /* the seed */

                int loop;

        for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
                chksum ^= getElement(loop);
        }
                setElement(len-1, chksum&0xFF);
    }
 
    /** Get an integer representation of a BCD value */
        public Integer getElementBCD(int n) { return Integer.decode(Integer.toHexString(getElement(n))); }

    /* 
     * skipPrefix is not used at this point in time, but is 
     *  defined as abstract in AbstractMRReply 
     */
    protected int skipPrefix(int index) { return -1;}
 
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetReply.class.getName());

}

/* @(#)XNetMessage.java */
