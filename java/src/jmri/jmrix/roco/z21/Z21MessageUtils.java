package jmri.jmrix.roco.z21;

import jmri.jmrix.Message;

/**
 * Package protected class containing common methods for Z21 Messages and Replies.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class Z21MessageUtils {

    public static String interpretBroadcastFlags(Message m) {
        int flags = m.getElement(4) + (m.getElement(5) << 8) + (m.getElement(6) << 16) + (m.getElement(7) << 24);
        return Integer.toString(flags);
    }

    static int integer16BitFromOffeset(int[] elements,int offset){
        return ((0xff&elements[offset+1])<<8) +
                       (0xff&(elements[offset]));
    }
}
