// LnTrafficRouter.java
package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a LocoNetInterface by doing a scatter-gather to another, simpler
 * implementation.
 * <P>
 * This is intended for remote operation, where only one copy of each message
 * should go to/from another node. By putting a LnTrafficRouter implementation
 * at the remote node, all of the routing of messages to multiple consumers can
 * be done without traffic over the connection.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version $Revision$
 *
 */
public class LnTrafficRouter extends LnTrafficController implements LocoNetListener {

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Only used during system initialization")
    public LnTrafficRouter() {
        // set the instance to point here
        self = this;
    }

    // The methods to implement the LocoNetInterface for clients.
    // These use the parent implementations of listeners, addLocoNetListener,
    // removeLocoNetListener, notify
    boolean connected = false;

    public boolean status() {
        return connected;
    }

    /**
     * Forward a preformatted LocoNetMessage to the actual interface.
     *
     * @param m Message to send; will be updated with CRC
     */
    public void sendLocoNetMessage(LocoNetMessage m) {
        // update statistics
        transmittedMsgCount++;

        // forward message
        destination.sendLocoNetMessage(m);
    }

    /**
     * Receive a LocoNet message from upstream and forward it to all the local
     * clients.
     */
    public void message(LocoNetMessage m) {
        notify(m);
    }

    // methods to connect/disconnect to a source of data in another
    // LocoNetInterface
    private LocoNetInterface destination = null;

    /**
     * Make connection to existing LocoNetInterface object for upstream
     * communication.
     *
     * @param i Interface to be connected
     */
    public void connect(LocoNetInterface i) {
        destination = i;
        connected = true;
        i.addLocoNetListener(LocoNetInterface.ALL, this);
    }

    /**
     * Break connection to upstream LocoNetInterface object. Once broken,
     * attempts to send via "message" member will fail.
     *
     * @param i previously connected interface
     */
    public void disconnectPort(LocoNetInterface i) {
        if (destination != i) {
            log.warn("disconnectPort: disconnect called from non-connected LnPortController");
        }
        destination = null;
        connected = false;
    }

    /**
     * Implement abstract method to signal if there's a backlog of information
     * waiting to be sent.
     *
     * @return true if busy, false if nothing waiting to send
     */
    public boolean isXmtBusy() {
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(LnTrafficRouter.class.getName());
}


/* @(#)LnTrafficRouter.java */
