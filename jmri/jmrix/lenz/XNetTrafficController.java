// XNetTrafficController.java

package jmri.jmrix.lenz;

import java.util.Vector;

/**
 * Abstract base class for implementations of XNetInterface.
 *<P>
 * This provides just the basic interface, plus the "" static
 * method for locating the local implementation.
 *
 * @author			Bob Jacobsen  Copyright (C) 2002
 * @version 		$Revision: 1.1 $
 *
 */
public abstract class XNetTrafficController implements XNetInterface {

    /**
	 * static function returning the TrafficController instance to use.
	 * @return The registered TrafficController instance for general use,
	 *         if need be creating one.
	 */
	static public XNetTrafficController instance() {
		return self;
	}

	static protected XNetTrafficController self = null;

    /**
     * Must provide a LenzCommandStation reference at creation time
     * @return
     */
    XNetTrafficController(LenzCommandStation pCommandStation) {
        mCommandStation = pCommandStation;
    }

    // Abstract methods for the XNetInterface
    abstract public boolean status();

	/**
	 * Forward a preformatted XNetMessage to the actual interface.
     * @param m Message to send; will be updated with CRC
	 */
	abstract public void sendXNetMessage(XNetMessage m);

    // The methods to implement adding and removing listeners
	protected Vector listeners = new Vector();

	public synchronized void addXNetListener(int mask, XNetListener l) {
        // add only if not already registered
        if (l == null) throw new java.lang.NullPointerException();
        if (!listeners.contains(l)) {
    		listeners.addElement(l);
		}
    }

	public synchronized void removeXNetListener(int mask, XNetListener l) {
    	if (listeners.contains(l)) {
    		listeners.removeElement(l);
    	}
    }

	/**
	 * Forward a message to all registered listeners.
     * @param m Message to forward. Listeners should not modify it!
	 */
	protected void notify(XNetMessage m) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this) {
			v = (Vector) listeners.clone();
		}
		if (log.isDebugEnabled()) log.debug("notify of incoming LocoNet packet: "+m.toString());
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			XNetListener client = (XNetListener) listeners.elementAt(i);
			client.message(m);
		}
	}

    /** Reference to the command station in communication here */
    LenzCommandStation mCommandStation;

    /** get access to communicating command station object */
    public LenzCommandStation getCommandStation() { return mCommandStation; }

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTrafficController.class.getName());
}


/* @(#)XNetTrafficController.java */

