package jmri.jmrix.ieee802154;

/**
 * Contains the data payload of an IEEE 802.15.4 packet.
 *
 * @author Bob Jacobsen Copyright (C) 2001,2003, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011 Modified for IEEE 802.15.4 connection
 * @author Paul Bender Copyright (C) 2013
 */
public class IEEE802154Message extends jmri.jmrix.AbstractMRMessage {

    /**
     * Suppress the default ctor, as the length must always be specified
     */
    protected IEEE802154Message() {
    }

    public IEEE802154Message(int l) {
        super(l);
        setResponseLength(0);  // only polls require a response
        setBinary(true);
        setTimeout(5000);
    }

    /**
     * This ctor interprets the String as the exact sequence to send,
     * byte-for-byte.
     * @param m msg to send
     * @param l length of expected response (not used)
     *
     */
    public IEEE802154Message(String m, int l) {
        super(m);
        //setResponseLength(l);
        setBinary(true);
        setTimeout(5000);
        // gather bytes in result
        byte b[] = jmri.util.StringUtil.bytesFromHexString(m);
        if (b.length == 0) {
            // no such thing as a zero-length message
            _nDataChars = 0;
            _dataChars = null;
            return;
        }
        _nDataChars = b.length;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i < b.length; i++) {
            setElement(i, b[i]);
        }
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     * @param l length of expected response
     */
    public IEEE802154Message(byte[] a, int l) {
        super(String.valueOf(a));
        setResponseLength(l);
        setBinary(true);
        setTimeout(5000);
    }

    /**
     * Check whether the message has a valid parity IEEE 802.15.4 messages have
     * a two byte parity.
     * @return true if parity is valid
     */
    public boolean checkParity() {
        int len = getNumDataElements();
        int chksum = 0x0000;  /* the seed */

        int loop;

        for (loop = 0; loop < len - 1; loop = loop + 2) {  // calculate contents for data part
            chksum ^= (getElement(loop) << 8);
            chksum ^= getElement(loop + 1);
        }
        return ((chksum & 0xFFFF) == ((getElement(len - 2) << 8) + getElement(len - 1)));
    }

    public void setParity() {
        int len = getNumDataElements();
        int chksum = 0x00;  /* the seed */

        int loop;

        for (loop = 0; loop < len - 1; loop++) {  // calculate contents for data part
            chksum ^= (getElement(loop) << 8);
            chksum ^= getElement(loop + 1);
        }
        setElement(len - 1, chksum & 0xFF);
        setElement(len - 2, ((chksum & 0xFF00) >> 8));
    }

    int responseLength = -1;  // -1 is an invalid value, indicating it hasn't been set

    public void setResponseLength(int l) {
        responseLength = l;
    }

    public int getResponseLength() {
        return responseLength;
    }

}

