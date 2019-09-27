package jmri.jmrix.zimo;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for implementations of MX-1 Interface.
 * <p>
 * This provides just the basic interface, plus the "" static method for
 * locating the local implementation.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 *
 * Adapted by Sip Bosch for use with zimo Mx-1
 *
 */
public abstract class Mx1TrafficController implements Mx1Interface {

    /**
     * Create a new TrafficController instance. Simple implementation.
     */
    public Mx1TrafficController() {
        super();
    }

    /**
     * Must provide a ZimoCommandStation reference at creation time
     *
     * @param pCommandStation reference to associated command station object,
     *                        preserved for later.
     * @param prot            false if {@link #ASCII}, true if {@link #BINARY}
     */
    Mx1TrafficController(Mx1CommandStation pCommandStation, boolean prot) {
        mCommandStation = pCommandStation;
        protocol = prot;
    }

    public final static boolean ASCII = false;
    public final static boolean BINARY = true;

    boolean protocol = ASCII;

    public boolean getProtocol() {
        return protocol;
    }

    // Abstract methods for the Mx1Interface
    @Override
    abstract public boolean status();

    /**
     * Forward a pre-formatted Mx1Message to the actual interface.
     *
     * @param m     Message to send; will be updated with CRC
     * @param reply the listener to notify of a response
     */
    @Override
    abstract public void sendMx1Message(Mx1Message m, Mx1Listener reply);

    // The methods to implement adding and removing listeners
    protected Vector<Mx1Listener> listeners = new Vector<>();

    @Override
    public synchronized void addMx1Listener(int mask, Mx1Listener l) {
        // add only if not already registered
        if (l == null) {
            throw new java.lang.NullPointerException();
        }
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
    }

    @Override
    public synchronized void removeMx1Listener(int mask, Mx1Listener l) {
        if (listeners.contains(l)) {
            listeners.removeElement(l);
        }
    }

    /**
     * Forward a message to all registered listeners.
     *
     * @param m       Message to forward. Listeners should not modify it!
     * @param replyTo Listener for the reply to this message, doesn't get the
     *                echo of it.
     */
    @SuppressWarnings("unchecked")
    protected void notify(Mx1Message m, Mx1Listener replyTo) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<Mx1Listener> v;
        synchronized (this) {
            v = (Vector<Mx1Listener>) listeners.clone();
        }
        if (log.isDebugEnabled()) {
            log.debug("notify of incoming packet: " + m.toString());
        }
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            Mx1Listener client = listeners.elementAt(i);
            if (client != replyTo) {
                client.message(m);
            }
        }
    }

    /**
     * Reference to the command station in communication here
     */
    Mx1CommandStation mCommandStation;

    /**
     * Get access to communicating command station object
     *
     * @return associated Command Station object
     */
    public Mx1CommandStation getCommandStation() {
        return mCommandStation;
    }

    public Mx1SystemConnectionMemo getAdapterMemo() {
        return adaptermemo;
    }

    public void setAdapterMemo(Mx1SystemConnectionMemo memo) {
        adaptermemo = memo;
    }

    Mx1SystemConnectionMemo adaptermemo;

    public String getUserName() {
        if (adaptermemo == null) {
            return "Zimo"; //IN18N
        }
        return adaptermemo.getUserName();
    }

    public String getSystemPrefix() {
        if (adaptermemo == null) {
            return "Z"; // NOI18N
        }
        return adaptermemo.getSystemPrefix();
    }

    private final static Logger log = LoggerFactory.getLogger(Mx1TrafficController.class);

}
