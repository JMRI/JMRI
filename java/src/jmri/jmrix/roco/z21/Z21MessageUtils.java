package jmri.jmrix.roco.z21;

/**
 * Package protected class containing common methods for Z21 Messages and Replies.
 *
 * @author	Paul Bender Copyright (C) 2019
 */
class Z21MessageUtils {

    static String interpretBroadcastFlags(int[] elements) {
        int flags = elements[4] + (elements[5] << 8) + (elements[6] << 16) + (elements[7] << 24);
        return Integer.toString(flags);
    }

    static int integer16BitFromOffeset(int[] elements,int offset){
        return ((0xff&elements[offset+1])<<8) +
                       (0xff&(elements[offset]));
    }
}
