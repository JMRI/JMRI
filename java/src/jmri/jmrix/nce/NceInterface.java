package jmri.jmrix.nce;

/**
 * Define interface for sending and receiving messages to the NCE command
 * station.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface NceInterface {

    public void addNceListener(NceListener l);

    public void removeNceListener(NceListener l);

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
    void sendNceMessage(NceMessage m, NceListener l);
}


