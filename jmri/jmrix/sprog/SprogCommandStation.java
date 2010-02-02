// SprogCommandStation.java

package jmri.jmrix.sprog;

import jmri.CommandStation;

/**
 * Implement CommandStation for SPROG communications.
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version     $Revision: 1.5 $
 */
public class SprogCommandStation implements CommandStation {

    /**
     * Send a specific packet to the rails.
     *
     * @param packet Byte array representing the packet, including
     * the error-correction byte.  Must not be null.
     * @param repeats number of times to repeat the packet
     */
    public void sendPacket(byte[] packet, int repeats) {

        SprogMessage m = new SprogMessage(packet);
        for (int i = 0; i < repeats; i++) {
        		SprogTrafficController.instance().sendSprogMessage(m, null);
        }
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SprogCommandStation.class.getName());
}

/* @(#)SprogCommandStation.java */
