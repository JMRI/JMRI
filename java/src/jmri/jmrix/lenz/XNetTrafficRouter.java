// XNetTrafficRouter.java

package jmri.jmrix.lenz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author			Paul Bender Copyright (C) 2004-2010
 * @version 		$Revision$
 *
 */
public class XNetTrafficRouter extends XNetTrafficController implements XNetListener {

	public XNetTrafficRouter(LenzCommandStation pCommandStation) {
        super(pCommandStation);
    }

    // The methods to implement the XNetInterface for clients.
    // These use the parent implementations of listeners, addXNetListener,
    // removeXNetListener, notify

    boolean connected = false;
	public boolean status() { return connected; }


    /* store the last sender */
    XNetListener lastSender=null;

	/**
	 * Forward a preformatted XNetMessage to the actual interface.
	 *
     * @param m Message to send; will be updated with CRC
	 */
	public void sendXNetMessage(XNetMessage m, XNetListener replyTo) {
		lastSender=replyTo;
	        destination.sendXNetMessage(m, replyTo);
	}

    /**
     * Receive a XNet message from upstream and forward it to
     * all the local clients.
     */
    public void message(XNetReply m) {
        notify(m);
    }
   
     // listen for the messages to the LI100/LI101
     public void message(XNetMessage l) {
     }

     // Handle a timeout notification
     public void notifyTimeout(XNetMessage msg)
     {
        if(log.isDebugEnabled()) log.debug("Notified of timeout on message" + msg.toString());
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
		notifyReply(m,lastSender);
	        lastSender=null;
	}

	static Logger log = LoggerFactory.getLogger(XNetTrafficRouter.class.getName());
}


/* @(#)XNetTrafficRouter.java */

