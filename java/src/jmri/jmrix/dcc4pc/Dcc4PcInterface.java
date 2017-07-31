package jmri.jmrix.dcc4pc;

/**
 * Define interface for sending and receiving messages to the DCC4PC computer
 * interface device.
 *
 * @author Kevin Dickerson Copyright 2012
 * @author Bob Jacobsen Copyright (C) 2001
 * 
 */
public interface Dcc4PcInterface {

    public void addDcc4PcListener(Dcc4PcListener l);

    public void removeDcc4PcListener(Dcc4PcListener l);

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
    void sendDcc4PcMessage(Dcc4PcMessage m, Dcc4PcListener l);
}
