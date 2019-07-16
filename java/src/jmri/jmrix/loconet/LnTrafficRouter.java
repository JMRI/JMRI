package jmri.jmrix.loconet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a LocoNetInterface by doing a scatter-gather to another, simpler
 * implementation.
 * <p>
 * This is intended for remote operation, where only one copy of each message
 * should go to/from another node. By putting an LnTrafficRouter implementation
 * at the remote node, all of the routing of messages to multiple consumers can
 * be done without traffic over the connection.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 */
public class LnTrafficRouter extends LnTrafficController implements LocoNetListener {

    /**
     * Create a default LnTrafficRouter instance without a SystemConnectionMemo.
     * Not compatible with multi connections.
     *
     * @deprecated since 4.11.6, use LnTrafficRouter(LocoNetSystemConnectionMemo) instead
     */
    @Deprecated
    public LnTrafficRouter() {
        jmri.util.Log4JUtil.deprecationWarning(log, "LnTrafficRouter"); 
    }

    /**
     * Create a default instance connected to a given SystemConnectionMemo.
     *
     * @since 4.11.6
     * @param m the connected LocoNetSystemConnectionMemo
     */
    public LnTrafficRouter(LocoNetSystemConnectionMemo m) {
        // set the memo to point here
        memo = m;
        m.setLnTrafficController(this);
    }

    // Methods to implement the LocoNetInterface for clients.
    // These use the parent implementations of listeners, addLocoNetListener,
    // removeLocoNetListener, notify
    boolean connected = false;

    @Override
    public boolean status() {
        return connected;
    }

    /**
     * Forward a preformatted LocoNetMessage to the actual interface.
     *
     * @param m Message to send; will be updated with CRC
     */
    @Override
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
    @Override
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
    @Override
    public boolean isXmtBusy() {
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(LnTrafficRouter.class);

}
