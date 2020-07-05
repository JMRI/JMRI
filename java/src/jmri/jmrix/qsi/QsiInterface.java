package jmri.jmrix.qsi;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Define interface for sending and receiving messages to the QSI command
 * station.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public interface QsiInterface {

    public void addQsiListener(QsiListener l);

    public void removeQsiListener(QsiListener l);

    /**
     * Test operational status of interface.
     *
     * @return true is interface implementation is operational
     */
    boolean status();

    /**
     * Send a message through the interface.
     *
     * @param m Message to be sent.
     * @param l Listener to be notified of reply.
     */
    void sendQsiMessage(QsiMessage m, QsiListener l);

}
