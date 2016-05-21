// IEEE802154Reply.java

package jmri.jmrix.ieee802154;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Contains the data payload of a serial reply
 * packet.  Note that its _only_ the payload.
 *
 * @author	Bob Jacobsen  Copyright (C) 2002, 2006, 2007, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * Modified for IEEE 802.15.4 connection
 * @author Paul Bender Copyright (C) 2013
 * @version     $Revision$
 */
public class IEEE802154Reply extends jmri.jmrix.AbstractMRReply {

    IEEE802154TrafficController tc = null;
	
    // create a new one
    public  IEEE802154Reply(IEEE802154TrafficController tc) {
        super();
        this.tc = tc;
        setBinary(true);
    }
    public IEEE802154Reply(IEEE802154TrafficController tc, String s) {
        super(s);
        this.tc = tc;
        setBinary(true);
    }
    public IEEE802154Reply(IEEE802154TrafficController tc, IEEE802154Reply l) {
        super(l);
        this.tc = tc;
        setBinary(true);
    }

    /*
     * @return the sender address associated with the reply (need for 
     * matching to a node ).  The type and position of the sender 
     * address is indicated in the control byte.
     */
    public byte[] getAddr() {
        int control=getFrameControl();
        // address may be either 0, 2, or 8 bytes, depending on
        // the addressing mode used.
        return null;
    }

   /*
    * @return length of reply.  length is the first byte after
    * the start byte.  We are not storing the start byte.
    * <p>
    * NOTE: this does not work correctly for packets received from
    * an XBee Node.  These devices do not provide raw packet 
    * information.
    */
   public int getLength(){ return getElement(0); }

   /*
    * @return control information from the reply.  This is the 3rd and 4th 
    * byte after the start byte.
    */
   public int getFrameControl(){ return ( getElement(2) <<8 ) + getElement(3); }
   
   /*
    * @return the sequence number of the reply.  This is the 4th byte 
    * after the start byte.
    */
   public byte getSequenceByte(){ return (byte) getElement(4); }

    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

        /**
         * check whether the message has a valid parity
         * IEEE 802.15.4 messages have a two byte parity.
         */
        public boolean checkParity() {
                int len = getNumDataElements();
                int chksum = 0x0000;  /* the seed */
                int loop;

        for(loop = 0; loop < len-1; loop=loop+2) {  // calculate contents for data part
                chksum ^= (getElement(loop)<<8);
                chksum ^= getElement(loop+1);
        }
                return ((chksum&0xFFFF) == ((getElement(len-2)<<8 ) + getElement(len-1)));
        }

    public String toMonitorString() { return toString(); }
    
    static Logger log = LoggerFactory.getLogger(IEEE802154Reply.class);

}

/* @(#)IEEE802154Reply.java */
