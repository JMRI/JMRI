package jmri.jmrix.xpa;

/**
 * Define interface for sending and receiving messages from an XpressNet System
 * using an XPA and a modem.
 *
 * @author	Paul Bender Copyright (C) 2004
 */
public interface XpaInterface {

    public void addXpaListener(XpaListener l);

    public void removeXpaListener(XpaListener l);

    /**
     * Test operational status of interface.
     *
     * @return true if the interface implementation is operational.
     */
    boolean status();

    /**
     * Send a message through the interface.
     *
     * @param m Message to be sent.
     * @param l Listener to be notified of reply.
     */
    void sendXpaMessage(XpaMessage m, XpaListener l);
}
