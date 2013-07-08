// IEEE802154Message.java

package jmri.jmrix.ieee802154;


/**
 * Contains the data payload of a serial
 * packet.
 * <P>
 * The transmission protocol can come in one of several forms:
 * <ul>
 * <li>If the interlocked parameter is false (default),
 * the packet is just sent.  If the response length is not zero,
 * a reply of that length is expected.
 * <li>If the interlocked parameter is true, the transmission
 * will require a CRC interlock, which will be automatically added.
 * (Design note: this is done to make sure that the messages
 * remain atomic)
 * </ul>
 *
 * @author    Bob Jacobsen  Copyright (C) 2001,2003, 2006, 2007, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * Modified for IEEE 802.15.4 connection
 * @author Paul Bender Copyright (C) 2013
 * @version   $Revision$
 */

public class IEEE802154Message extends jmri.jmrix.AbstractMRMessage {

    /** Suppress the default ctor, as the
     * length must always be specified
     */
    protected IEEE802154Message() {}
    
    public IEEE802154Message(int l) {
        super(l);
        setResponseLength(0);  // only polls require a response
        setBinary(true);
        setTimeout(5000);
    }

    /**
     * This ctor interprets the String as the exact
     * sequence to send, byte-for-byte.
     * @param m
     */
    public IEEE802154Message(String m, int l) {
        super(m);
        //setResponseLength(l);
        setBinary(true);
        setTimeout(5000);
        // gather bytes in result
        byte b[] = jmri.util.StringUtil.bytesFromHexString(m);
           if (b.length == 0)
           {
              // no such thing as a zero-length message
              _nDataChars=0;
              _dataChars = null;
              return;
           }
        _nDataChars = b.length;
        _dataChars = new int[_nDataChars];
        for (int i=0; i<b.length; i++) setElement(i, b[i]);
    }

    /**
     * This ctor interprets the byte array as
     * a sequence of characters to send.
     * @param a Array of bytes to send
     */
    public  IEEE802154Message(byte[] a, int l) {
        super(String.valueOf(a));
        setResponseLength(l);
        setBinary(true);
        setTimeout(5000);
    }

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



    int responseLength = -1;  // -1 is an invalid value, indicating it hasn't been set
    public void setResponseLength(int l) { responseLength = l; }
    public int getResponseLength() { return responseLength; }
        
    public String toMonitorString() { return toString(); }
    
    // static methods to recognize a message
    public boolean isPoll() { return getElement(1)==48;}
    public boolean isXmt()  { return getElement(1)==17;}
    public int getAddr() { return getElement(0); }

    // static methods to return a formatted message
    static public IEEE802154Message getPoll(int addr) {
        // IEEE802154 implementation does not currently poll
        return null;
    }
}

/* @(#)IEEE802154Message.java */
