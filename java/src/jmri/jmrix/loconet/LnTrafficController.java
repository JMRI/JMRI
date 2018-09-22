package jmri.jmrix.loconet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for implementations of LocoNetInterface.
 * <p>
 * This provides just the basic interface and some statistics support.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public abstract class LnTrafficController implements LocoNetInterface {

    /**
     * Reference to the system connection memo.
     */
    LocoNetSystemConnectionMemo memo = null;

    /**
     * Set the system connection memo associated with this traffic controller.
     *
     * @param m associated systemConnectionMemo object
     */
    public void setSystemConnectionMemo(LocoNetSystemConnectionMemo m) {
        log.debug("LnTrafficController set memo to {}", m.getUserName());
        memo = m;
    }

    // Abstract methods for the LocoNetInterface
    @Override
    abstract public boolean status();

    /**
     * Forward a preformatted LocoNetMessage to the actual interface.
     * <p>
     * Implementations should update the transmit count statistic.
     *
     * @param m message to send; will be updated with CRC
     */
    @Override
    abstract public void sendLocoNetMessage(LocoNetMessage m);

    // The methods to implement adding and removing listeners

    protected Vector<LocoNetListener> listeners = new Vector<LocoNetListener>();

    @Override
    public synchronized void addLocoNetListener(int mask, LocoNetListener l) {
        // add only if not already registered
        if (l == null) {
            throw new java.lang.NullPointerException();
        }
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
    }

    @Override
    public synchronized void removeLocoNetListener(int mask, LocoNetListener l) {
        if (listeners.contains(l)) {
            listeners.removeElement(l);
        }
    }

    /**
     * Forward a LocoNetMessage to all registered listeners.
     * <p>
     * Needs to have public access, as
     * {@link jmri.jmrix.loconet.loconetovertcp.LnOverTcpPacketizer} and
     * {@link jmri.jmrix.loconet.Intellibox.IBLnPacketizer} invoke it, but don't
     * inherit from it.
     *
     * @param m message to forward. Listeners should not modify it!
     */
    @SuppressWarnings("unchecked")
    public void notify(LocoNetMessage m) {
        // record statistics
        receivedMsgCount++;
        receivedByteCount += m.getNumDataElements();

        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<LocoNetListener> v;
        synchronized (this) {
            v = (Vector<LocoNetListener>) listeners.clone();
        }
        log.debug("notify of incoming LocoNet packet: {}", m.toString());
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            LocoNetListener client = listeners.elementAt(i);
            client.message(m);
        }
    }

    /**
     * Is there a backlog of information for the outbound link? This includes
     * both in the program (e.g. the outbound queue) and in the Command Station
     * interface (e.g. flow control from the port).
     *
     * @return true if busy, false if nothing waiting to send
     */
    abstract public boolean isXmtBusy();

    /**
     * Reset statistics (received message count, transmitted message count,
     * received byte count).
     */
    public void resetStatistics() {
        receivedMsgCount = 0;
        transmittedMsgCount = 0;
        receivedByteCount = 0;
    }

    /**
     * Monitor the number of LocoNet messages received across the interface.
     * This includes the messages this client has sent.
     *
     * @return the number of messages received
     */
    public int getReceivedMsgCount() {
        return receivedMsgCount;
    }
    protected int receivedMsgCount = 0;

    /**
     * Monitor the number of bytes in LocoNet messages received across the
     * interface. This includes the bytes in messages this client has sent.
     *
     * @return the number of bytes received
     */
    public int getReceivedByteCount() {
        return receivedByteCount;
    }
    protected int receivedByteCount = 0;

    /**
     * Monitor the number of LocoNet messages transmitted across the interface.
     *
     * @return the number of messages transmitted
     */
    public int getTransmittedMsgCount() {
        return transmittedMsgCount;
    }
    protected int transmittedMsgCount = 0;

    private final static Logger log = LoggerFactory.getLogger(LnTrafficController.class);

}
