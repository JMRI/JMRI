// XNetTrafficRouter.java

package jmri.jmrix.lenz;

import com.sun.java.util.collections.LinkedList;
import com.sun.java.util.collections.NoSuchElementException;

import java.util.Vector;

/**
 * Implements a XNetInterface by doing a scatter-gather to
 * another, simpler implementation.
 * <P>
 * This is intended for remote operation, where only one copy of
 * each message should go to/from another node.  By putting a
 * LnTrafficRouter implementation at the remote node,
 * all of the routing of messages to multiple consumers can be done
 * without traffic over the connection.
 *
 * @author			Bob Jacobsen  Copyright (C) 2002
 * @version 		$Revision: 2.0 $
 *
 */
public class XNetTrafficRouter extends XNetTrafficController implements XNetListener {

	public XNetTrafficRouter(LenzCommandStation pCommandStation) {
        super(pCommandStation);
        // set the instance to point here
        self=this;
    }

    // The methods to implement the XNetInterface for clients.
    // These use the parent implementations of listeners, addXNetListener,
    // removeXNetListener, notify

    boolean connected = false;
	public boolean status() { return connected; }

	/**
	 * Forward a preformatted XNetMessage to the actual interface.
	 *
     * @param m Message to send; will be updated with CRC
	 */
	public void sendXNetMessage(XNetMessage m, XNetListener replyTo) {
        destination.sendXNetMessage(m, replyTo);
	}

    /**
     * Receive a XNet message from upstream and forward it to
     * all the local clients.
     */
    public void message(XNetReply m) {
        notify(m);
    }

    // methods to connect/disconnect to a source of data in another
    // XNetInterface
	private XNetInterface destination = null;

	/**
	 * Make connection to existing XNetInterface object
     * for upstream communication.
     * @param i Interface to be connected
	 */
	public void connect(XNetInterface i) {
        destination = i;
        connected = true;
        i.addXNetListener(XNetInterface.ALL, this);
    }

	/**
	 * Break connection to upstream LocoNetInterface object. Once broken,
	 * attempts to send via "message" member will fail.
     * @param i previously connected interface
	 */
	public void disconnectPort(XNetInterface i) {
			if (destination != i)
				log.warn("disconnectPort: disconnect called from non-connected PortController");
			destination = null;
            connected = false;
		}

	/**
	 * Forward a XNetMessage to all registered listeners.
     * @param m Message to forward. Listeners should not modify it!
	 */
	protected void notify(XNetReply m) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this) {
			v = (Vector) listeners.clone();
		}
		if (log.isDebugEnabled()) log.debug("notify of incoming packet: "+m.toString());
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			XNetListener client = (XNetListener) listeners.elementAt(i);
			client.message(m);
		}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTrafficRouter.class.getName());
}


/* @(#)XNetTrafficRouter.java */

