// CanInterface.java
package jmri.jmrix.can;

/**
 * Define interface for sending and receiving CAN messages
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @version	$Revision$
 */
public interface CanInterface {

    public void addCanListener(CanListener l);

    public void removeCanListener(CanListener l);

    /**
     * Test operational status of interface.
     *
     * @return true is interface implementation is operational.
     */
    boolean status();

    /**
     * Send a message through the interface.
     *
     * @param m Message to be sent.
     * @param l Listener to be notified of reply.
     */
    void sendCanMessage(CanMessage m, CanListener l);
}


/* @(#)CanInterface.java */
