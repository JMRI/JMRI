// LnTrafficController.java

package jmri.jmrix.loconet;

import java.util.Vector;

/**
 * Abstract base class for implementations of LocoNetInterface.
 *<P>
 * This provides just the basic interface, plus the "" static
 * method for locating the local implementation.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version 		$Revision: 1.5 $
 *
 */
public abstract class LnTrafficController implements LocoNetInterface {

    /**
     * static function returning the LnTrafficController instance to use.
     * @return The registered LnTrafficController instance for general use,
     *         if need be creating one.
     */
    static public LnTrafficController instance() {
        return self;
    }

    static protected LnTrafficController self = null;

    // Abstract methods for the LocoNetInterface
    abstract public boolean status();

    /**
     * Forward a preformatted LocoNetMessage to the actual interface.
     * @param m Message to send; will be updated with CRC
     */
    abstract public void sendLocoNetMessage(LocoNetMessage m);

    // The methods to implement adding and removing listeners
    protected Vector listeners = new Vector();

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

    /**
     * Forward a LocoNetMessage to all registered listeners.
     * @param m Message to forward. Listeners should not modify it!
     */
    protected void notify(LocoNetMessage m) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector v;
        synchronized(this) {
            v = (Vector) listeners.clone();
        }
        if (log.isDebugEnabled()) log.debug("notify of incoming LocoNet packet: "+m.toString());
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            LocoNetListener client = (LocoNetListener) listeners.elementAt(i);
            client.message(m);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnTrafficController.class.getName());
}


/* @(#)LnTrafficController.java */

