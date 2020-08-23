package jmri.jmrix.ieee802154;

/**
 * Contains the data payload of a serial reply packet. Note that it's _only_ the
 * payload.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011 Modified for IEEE 802.15.4 connection
 * @author Paul Bender Copyright (C) 2013
 */
public class IEEE802154Reply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public IEEE802154Reply() {
        super();
        setBinary(true);
    }

    public IEEE802154Reply(String s) {
        super(s);
        setBinary(true);
    }

    public IEEE802154Reply(IEEE802154Reply l) {
        super(l);
        setBinary(true);
    }

    /*
     * @return the destination address associated with the reply (need for 
     * matching to a node ).  The type and position of the sender 
     * address is indicated in the control byte.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
        justification = "Would take significant rework")
    public byte[] getDestinationAddress() {
        int destinationMode = getDestinationAddressMode();
        //int sourceMode = getSourceAddressMode(); // not used
        int offset = 4; // position of first byte of destination address if
        // it is present.
        int length = 0; // minimum destination address length.
        // both address may be either 0, 2, or 8 bytes, depending on
        // the addressing mode used.  destination mode determins the
        // source address offset.
        switch (destinationMode) {
            case 0x00:
                return null;  // no destination address.
            case 0x01:
                return null;  // this value is reserved.
            case 0x10:
                length += 2; // 16 bit address
                break;
            case 0x11:
                length += 8; // 64 bit address
                break;
            default:
                return null; // this should never actually happen.
        }

        if (!isIntraPanFrame()) {
            // this is an interpan frame. pan addresses
            // are included if both address fields
            // are present.
            if (destinationMode != 0) {
                offset += 2;
            }
        }

        byte address[] = new byte[length];
        for (int i = 0; i < length; i++) {
            address[i] = (byte) (0xff & getElement(i + offset));
        }
        return address;
    }

    /*
     * @return the sender address associated with the reply (need for 
     * matching to a node ).  The type and position of the sender 
     * address is indicated in the control byte.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
        justification = "Would take significant rework")
    public byte[] getSourceAddr() {
        int destinationMode = getDestinationAddressMode();
        int sourceMode = getSourceAddressMode();
        int offset = 4; // position of first byte of source address if
        // no destination address is present.
        int length = 0; // minimum source address length.
        // both address may be either 0, 2, or 8 bytes, depending on
        // the addressing mode used.  destination mode determins the
        // source address offset.
        switch (destinationMode) {
            case 0x00:
                break;  // no destination address.
            case 0x01:
                break;  // this value is reserved.
            case 0x10:
                offset += 2; // 16 bit address
                break;
            case 0x11:
                offset += 8; // 64 bit address
                break;
            default:
                return null; // this should never actually happen.
        }
        switch (sourceMode) {
            case 0x00:
                return null; // no source address.
            case 0x01:
                return null; // this value is reserved.
            case 0x10:
                length += 2; // 16 bit address
                break;
            case 0x11:
                length += 8; // 64 bit address
                break;
            default:
                return null; // this should never actually happen.
        }

        if (!isIntraPanFrame()) {
            // this is an interpan frame. pan addresses
            // are included if both address fields
            // are present.
            if (destinationMode != 0 && sourceMode != 0) {
                offset += 4;
            }
        }

        byte address[] = new byte[length];
        for (int i = 0; i < length; i++) {
            address[i] = (byte) (0xff & getElement(i + offset));
        }
        return address;
    }

    /*
     * @return the payload associated with the reply.  The position
     * of the data is determined from the control byte.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
        justification = "Would take significant rework")
    public byte[] getPayload() {
        int destinationMode = getDestinationAddressMode();
        int sourceMode = getSourceAddressMode();
        int offset = 4; // position of first byte of source address if
        // no destination address is present.
        // both address may be either 0, 2, or 8 bytes, depending on
        // the addressing mode used.  destination mode determins the
        // source address offset.
        switch (destinationMode) {
            case 0x00:
                break;  // no destination address.
            case 0x01:
                break;  // this value is reserved.
            case 0x10:
                offset += 2; // 16 bit address
                break;
            case 0x11:
                offset += 8; // 64 bit address
                break;
            default:
                return null; // this should never actually happen.
        }
        switch (sourceMode) {
            case 0x00:
                break; // no source address.
            case 0x01:
                break; // this value is reserved.
            case 0x10:
                offset += 2; // 16 bit address
                break;
            case 0x11:
                offset += 8; // 64 bit address
                break;
            default:
                return null; // this should never actually happen.
        }

        if (!isIntraPanFrame()) {
            // this is an interpan frame. pan addresses
            // are included if both address fields
            // are present.
            if (destinationMode != 0 && sourceMode != 0) {
                offset += 4;
            }
        }

        int length = getLength() - (offset + 2); // the payload starts after
        // the address and ends
        // at the 2 byte checksum.

        byte address[] = new byte[length];
        for (int i = 0; i < length; i++) {
            address[i] = (byte) (0xff & getElement(i + offset));
        }
        return address;
    }

    /*
     * @return length of reply.  length is the first byte after
     * the start byte.  We are not storing the start byte.
     * <p>
     * NOTE: this does not work correctly for packets received from
     * an XBee Node.  These devices do not provide raw packet 
     * information.
     */
    public int getLength() {
        return getElement(0);
    }

    /*
     * @return control information from the reply.  This is the 3rd and 4th 
     * byte after the start byte.
     * Format of the frame control field (FCF)
     *  according to IEEE 802.15.4 MAC standard
     *
     * bits 0-2   frame type
     *      3     security enabled
     *      4     frame pending
     *      5     acknowledge request
     *      6     intra pan
     *      7-9   reserved
     *      10-11 destination addressing mode
     *      12-13 reserved
     *      14-15 source addressing mode
     * 
     */
    public int getFrameControl() {
        return (getElement(1) << 8) + getElement(2);
    }

    // return the destination address mode (bits 10 and 11) of the frame
    // control field.
    public int getDestinationAddressMode() {
        return ((getFrameControl() & 0x0C00) >> 10);
    }

    // return the source address mode (bits 14 and 15) of the frame
    // control field.
    public int getSourceAddressMode() {
        return ((getFrameControl() & 0xC000) >> 14);
    }

    // return whether or not the intrapan frame bit (bit 6) is set in
    // the frame control field.
    public boolean isIntraPanFrame() {
        return ((getFrameControl() & 0x0020) != 0);
    }

    /*
     * @return the sequence number of the reply.  This is the 4th byte 
     * after the start byte.
     */
    public byte getSequenceByte() {
        return (byte) getElement(3);
    }

    @Override
    protected int skipPrefix(int index) {
        // doesn't have to do anything
        return index;
    }

    /**
     * check whether the message has a valid parity IEEE 802.15.4 messages have
     * a two byte parity.
     * @return true if parity valid
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

}
