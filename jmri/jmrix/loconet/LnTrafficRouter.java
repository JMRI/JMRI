// LnTrafficRouter.java

package jmri.jmrix.loconet;

import com.sun.java.util.collections.LinkedList;
import com.sun.java.util.collections.NoSuchElementException;

import java.util.Vector;

/**
 * Implements a LocoNetInterface by doing a scatter-gather to
 * another, simpler implementation.
 * <P>
 * This is intended for remote operation, where only one copy of
 * each message should go to/from another node.  By putting a
 * LnTrafficRouter implementation at the remote node,
 * all of the routing of messages to multiple consumers can be done
 * without traffic over the connection.
 *
 * @author			Bob Jacobsen  Copyright (C) 2002
 * @version 		$Id: LnTrafficRouter.java,v 1.1 2002-03-18 04:52:44 jacobsen Exp $
 *
 */
public class LnTrafficRouter extends LnTrafficController implements LocoNetListener {

	public LnTrafficRouter() {
        // set the instance to point here
        self=this;
    }

    // The methods to implement the LocoNetInterface for clients.
    // These use the parent implementations of listeners, addLocoNetListener,
    // removeLocoNetListener, notify

    boolean connected = false;
	public boolean status() { return connected; }

	/**
	 * Forward a preformatted LocoNetMessage to the actual interface.
	 *
	 * Checksum is computed and overwritten here, then the message
	 * is converted to a byte array and queue for transmission
     * @param m Message to send; will be updated with CRC
	 */
	public void sendLocoNetMessage(LocoNetMessage m) {
        destination.sendLocoNetMessage(m);
	}

    /**
     * Receive a LocoNet message from upstream and forward it to
     * all the local clients.
     */
    public void message(LocoNetMessage m) {
        notify(m);
    }

    // methods to connect/disconnect to a source of data in another
    // LocoNetInterface
	private LocoNetInterface destination = null;

	/**
	 * Make connection to existing LocoNetInterface object
     * for upstream communication.
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
     * @param i previously connected interface
	 */
	public void disconnectPort(LocoNetInterface i) {
			if (destination != i)
				log.warn("disconnectPort: disconnect called from non-connected LnPortController");
			destination = null;
            connected = false;
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

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnTrafficRouter.class.getName());
}


/* @(#)LnTrafficRouter.java */

