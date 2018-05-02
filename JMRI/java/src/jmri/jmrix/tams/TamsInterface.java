package jmri.jmrix.tams;

/**
 * Define interface for sending and receiving messages to the Tams command
 * station.
 *
 * Based on work by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public interface TamsInterface {

    public void addTamsListener(TamsListener l);

    public void removeTamsListener(TamsListener l);

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
    void sendTamsMessage(TamsMessage m, TamsListener l);
}


