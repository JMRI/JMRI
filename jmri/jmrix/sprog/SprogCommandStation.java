// SprogCommandStation.java

package jmri.jmrix.sprog;

import jmri.CommandStation;

/**
 * Implement CommandStation for SPROG communications.
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class SprogCommandStation implements CommandStation {

    /**
     * Send a specific packet to the rails.
     *
     * @param packet Byte array representing the packet, including
     * the error-correction byte.  Must not be null.
     * @param repeats Number of times to repeat the transmission,
     *      but is ignored in the current implementation
     */
    public void sendPacket(byte[] packet, int repeats) {

        if (repeats!=1) log.warn("Only single transmissions currently available");

        SprogMessage m = new SprogMessage(1+(packet.length*3));
        int i = 0; // counter of byte in output message
        int j = 0; // counter of byte in input packet

        m.setElement(i++, 'O');  // "O " starts output packet

        // add each byte of the input message
        for (j=0; j<packet.length; j++) {
            m.setElement(i++,' ');
            String s = Integer.toHexString((int)packet[j]&0xFF).toUpperCase();
            if (s.length() == 1) {
                m.setElement(i++, '0');
                m.setElement(i++, s.charAt(0));
            } else {
                m.setElement(i++, s.charAt(0));
                m.setElement(i++, s.charAt(1));
            }
        }

        SprogTrafficController.instance().sendSprogMessage(m, null);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogCommandStation.class.getName());
}

/* @(#)SprogCommandStation.java */
