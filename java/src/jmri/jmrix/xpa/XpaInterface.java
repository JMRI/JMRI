package jmri.jmrix.xpa;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Define interface for sending and receiving messages from an XpressNet System
 * using an XPA and a modem.
 *
 * @author Paul Bender Copyright (C) 2004
 */
@API(status = EXPERIMENTAL)
public interface XpaInterface {

    void addXpaListener(XpaListener l);

    void removeXpaListener(XpaListener l);

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
