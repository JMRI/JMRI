package jmri.jmrix.roco.z21;

import jmri.DccLocoAddress;
import jmri.jmrix.Message;

/**
 * Package protected class containing common methods for Z21 Messages and Replies.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class Z21MessageUtils {

    public static int interpretBroadcastFlags(Message m) {
        return m.getElement(4) + (m.getElement(5) << 8) + (m.getElement(6) << 16) + (m.getElement(7) << 24);
    }

    static int integer16BitFromOffeset(int[] elements,int offset){
        return ((0xff&elements[offset+1])<<8) +
                       (0xff&(elements[offset]));
    }

    // address value is the 16 bits of the two bytes containing the
    // address.  The most significant two bits represent the direction.
    public static DccLocoAddress getCanDetectorLocoAddress(int addressValue) {
        if(addressValue==0) {
            return null;
        } else {
            int locoAddress = (0x3FFF&addressValue);
            return new DccLocoAddress(locoAddress,locoAddress>=100);
        }
    }
}
