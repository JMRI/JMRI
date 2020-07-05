package jmri.jmrix.marklin;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Define the interface for sending and receiving messages to the Marklin command
 * station.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
@API(status = EXPERIMENTAL)
public interface MarklinInterface {

    public void addMarklinListener(MarklinListener l);

    public void removeMarklinListener(MarklinListener l);

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
    void sendMarklinMessage(MarklinMessage m, MarklinListener l);

}
