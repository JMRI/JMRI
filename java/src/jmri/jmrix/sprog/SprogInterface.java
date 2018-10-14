package jmri.jmrix.sprog;

/**
 * Define interface for sending and receiving messages to the SPROG command
 * station.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public interface SprogInterface {

    public void addSprogListener(SprogListener l);

    public void removeSprogListener(SprogListener l);

    /**
     * Test operational status of interface.
     *
     * @return true if interface implementation is operational.
     */
    boolean status();

    /**
     * Send a message through the interface.
     *
     * @param m Message to be sent.
     * @param l Listener to be notified of reply.
     */
    void sendSprogMessage(SprogMessage m, SprogListener l);
}



