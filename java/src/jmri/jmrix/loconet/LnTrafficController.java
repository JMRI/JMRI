// LnTrafficController.java

package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Vector;

/**
 * Abstract base class for implementations of LocoNetInterface.
 *<P>
 * This provides just the basic interface, plus the "" static
 * method for locating the local implementation and some
 * statistics support.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version 		$Revision$
 *
 */
public abstract class LnTrafficController implements LocoNetInterface {

    /**
     * static function returning the LnTrafficController instance to use.
     * @return The registered LnTrafficController instance for general use,
     *         if need be creating one.
     * @deprecated 2.13.4
     */
    @Deprecated
    static public LnTrafficController instance() {
        return self;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MS_PKGPROTECT")
    // FindBugs wants this package protected, but we're removing it when multi-connection
    // migration is complete
    static protected LnTrafficController self = null;

    // Abstract methods for the LocoNetInterface
    abstract public boolean status();

    /**
     * Forward a preformatted LocoNetMessage to the actual interface.
     *<P>
     * Implementations should update the transmit count statistic.
     *
     * @param m Message to send; will be updated with CRC
     */
    abstract public void sendLocoNetMessage(LocoNetMessage m);

    // The methods to implement adding and removing listeners
    protected Vector<LocoNetListener> listeners = new Vector<LocoNetListener>();

    public synchronized void addLocoNetListener(int mask, LocoNetListener l) {
        // add only if not already registered
        if (l == null) throw new java.lang.NullPointerException();
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
    }

    public synchronized void removeLocoNetListener(int mask, LocoNetListener l) {
    	if (listeners.contains(l)) {
            listeners.removeElement(l);
    	}
    }
    
    // The methods to implement adding and removing listeners
    protected Vector<LnTrafficListenerFilter> trafficListeners = new Vector<LnTrafficListenerFilter>();

    public synchronized void addTrafficListener(int mask, LnTrafficListener l) {
        if (l == null) throw new java.lang.NullPointerException();

        // add only if not already registered
    	LnTrafficListenerFilter adapter = new LnTrafficListenerFilter(mask, l);
        if (!trafficListeners.contains(adapter)) {
            trafficListeners.addElement(adapter);
        }
    }

    public synchronized void removeTrafficListener(int mask, LnTrafficListener l) {
        if (l == null) throw new java.lang.NullPointerException();

        LnTrafficListenerFilter filter = new LnTrafficListenerFilter(mask, l);
    	if (trafficListeners.contains(filter)) {
    		trafficListeners.remove(trafficListeners.indexOf(filter)).setFilter(mask);
    	}
    }

    public synchronized void changeTrafficListener(int mask, LnTrafficListener l) {
        if (l == null) throw new java.lang.NullPointerException();

        LnTrafficListenerFilter filter = new LnTrafficListenerFilter(mask, l);
    	if (trafficListeners.contains(filter)) {
    		trafficListeners.get(trafficListeners.indexOf(filter)).setFilter(mask);
    	}
    }

    /**
     * Forward a LocoNetMessage to all registered listeners.
     * <P>
     * this needs to have public access, as 
     * {@link jmri.jmrix.loconet.loconetovertcp.LnOverTcpPacketizer}
     * and
     * {@link jmri.jmrix.loconet.Intellibox.IBLnPacketizer} invoke it,
     * but don't inherit from it
     * @param m Message to forward. Listeners should not modify it!
     */
    @SuppressWarnings("unchecked")
	public void notify(LocoNetMessage m) {
        // record statistics
        receivedMsgCount++;
        receivedByteCount += m.getNumDataElements();
        
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<LocoNetListener> v;
        synchronized(this) {
            v = (Vector<LocoNetListener>) listeners.clone();
        }
        if (log.isDebugEnabled()) log.debug("notify of LocoNet packet: "+m.toString());
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            LocoNetListener client = listeners.elementAt(i);
            client.message(m);
        }
    }
    
	@SuppressWarnings("unchecked")
	public void notifyRcv(Date timestamp, LocoNetMessage m) {
        
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<LnTrafficListenerFilter> v;
        synchronized(this) {
            v = (Vector<LnTrafficListenerFilter>) trafficListeners.clone();
        }
        if (log.isDebugEnabled()) log.debug("notify of incoming LocoNet packet: " + m.toString());
        
        // forward to all listeners
        for (LnTrafficListenerFilter adapter : v) {
        	adapter.fireRcv(timestamp, m);
        }

        // call the old notify for other listeners
        notify(m);
    }

	@SuppressWarnings("unchecked")
	public void notifyXmit(Date timestamp, LocoNetMessage m) {
        
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<LnTrafficListenerFilter> v;
        synchronized(this) {
            v = (Vector<LnTrafficListenerFilter>) trafficListeners.clone();
        }
        if (log.isDebugEnabled()) log.debug("notify of send LocoNet packet: " + m.toString());
        
        // forward to all listeners
        for (LnTrafficListenerFilter adapter : v) {
        	adapter.fireXmit(timestamp, m);
        }
        
        // call the old notify for other listeners
        notify(m);
    }

	/**
     * Is there a backlog of information for the outbound link?
     * This includes both in the program (e.g. the outbound queue)
     * and in the command station interface (e.g. flow control from the port)
     * @return true if busy, false if nothing waiting to send
     */
    abstract public boolean isXmtBusy();

    /**
     * Reset statistics (received message count, transmitted message count,
     * received byte count)
     */
    public void resetStatistics() {
        receivedMsgCount = 0;
        transmittedMsgCount = 0;
        receivedByteCount = 0;
    }
    
    /**
     * Monitor the number of LocoNet messaages received across the interface.
     * This includes the messages this client has sent.
     */
    public int getReceivedMsgCount() {
        return receivedMsgCount;
    }
    protected int receivedMsgCount = 0;
    
    /**
     * Monitor the number of bytes in LocoNet messaages received across the interface.
     * This includes the messages this client has sent.
     */
    public int getReceivedByteCount() {
        return receivedByteCount;
    }
    protected int receivedByteCount = 0;
    
    /**
     * Monitor the number of LocoNet messaages transmitted across the interface.
     */
    public int getTransmittedMsgCount() {
        return transmittedMsgCount;
    }
    protected int transmittedMsgCount = 0;
    
    static Logger log = LoggerFactory.getLogger(LnTrafficController.class.getName());
}


/* @(#)LnTrafficController.java */

