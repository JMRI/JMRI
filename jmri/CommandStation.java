// CommandStation.java

package jmri;

/**
 * Interface for objects representing DCC command stations.
 * <P>
 * System-specific implementations can be obtained via the InstanceManager
 * class.
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public interface CommandStation {

    /**
     * Send a specific packet to the rails.
     *
     * @param packet Byte array representing the packet, including
     * the error-correction byte.  Must not be null.
     * @param repeats Number of times to repeat the transmission.
     */
    public void sendPacket(byte[] packet, int repeats);

}

/* @(#)CommandStation.java */
