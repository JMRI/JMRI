/** 
 * NceTrafficController.java
 *
 * Description:		Converts Stream-based I/O to/from NCE messages
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;

/** 
 * Converts Stream-based I/O to/from NCE messages.  The "NceInterface"
 * side sends/receives message objects.  The connection to 
 * a NcePortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is 
 * handled in an independent thread.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 */
public class NceTrafficController implements NceInterface, Runnable {

	public NceTrafficController() {
		if (log.isDebugEnabled()) log.debug("setting instance: "+this);
		self=this;
	}
	

// The methods to implement the NceInterface

	protected Vector cmdListeners = new Vector();
	
	public boolean status() { return (ostream != null & istream != null); 
		}

	public synchronized void addNceListener(NceListener l) { 
			// add only if not already registered
			if (l == null) throw new java.lang.NullPointerException();
			if (!cmdListeners.contains(l)) {
					cmdListeners.addElement(l);
				}
		}

	public synchronized void removeNceListener(NceListener l) {
			if (cmdListeners.contains(l)) {
					cmdListeners.removeElement(l);
				}
		}


	/**
	 * Forward a NceMessage to all registered NceInterface listeners.
	 */
	protected void notifyMessage(NceMessage m, NceListener notMe) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this)
			{
				v = (Vector) cmdListeners.clone();
			}
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			NceListener client = (NceListener) v.elementAt(i);
			if (notMe != client) {
				if (log.isDebugEnabled()) log.debug("notify client: "+client);
				try {
					client.message(m);
					}
				catch (Exception e)
					{
						log.warn("notify: During dispatch to "+client+"\nException "+e);
					}
				}
			}
	}

	NceListener lastSender = null;
	
	protected void notifyReply(NceReply r) {
		
		// make a copy of the listener vector to synchronized (not needed for transmit?)
		Vector v;
		synchronized(this)
			{
				v = (Vector) cmdListeners.clone();
			}
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			NceListener client = (NceListener) v.elementAt(i);
			if (log.isDebugEnabled()) log.debug("notify client: "+client);
			try {
					client.reply(r);
				}
			catch (Exception e)
				{
					log.warn("notify: During dispatch to "+client+"\nException "+e);
				}
			}

		// forward to the last listener who send a message
		// this is done _second_ so monitoring can have already stored the reply
		// before a response is sent
		if (lastSender != null) lastSender.reply(r);
	}
	

	/**
	 * Forward a preformatted message to the actual interface.
	 */
	public void sendNceMessage(NceMessage m, NceListener reply) {
		if (log.isDebugEnabled()) log.debug("sendNceMessage message: ["+m+"]");
		// remember who sent this
		lastSender = reply;
		
		// notify all _other_ listeners
		notifyMessage(m, reply);
		
		// stream to port in single write, as that's needed by serial
		int len = m.getNumDataElements();
		int cr = 0;
		if (! m.isBinary()) cr = 1;  // space for return

		byte msg[] = new byte[len+cr];

		for (int i=0; i< len; i++)
			msg[i] = (byte) m.getElement(i);
		if (! m.isBinary()) msg[len] = 0x0d;
		try {
			if (ostream != null) {
				if (log.isDebugEnabled()) log.debug("write message: "+msg);
				ostream.write(msg);
			}
			else {
				// no stream connected
				log.warn("sendMessage: no connection established");
				}
			}
		catch (Exception e) {
			log.warn("sendMessage: Exception: "+e.toString());
			}
		}

// methods to connect/disconnect to a source of data in a LnPortController
	private NcePortController controller = null;
	
	/**
	 * Make connection to existing PortController object.
	 */
	public void connectPort(NcePortController p) {
			istream = p.getInputStream();
			ostream = p.getOutputStream();
			if (controller != null)
				log.warn("connectPort: connect called while connected");
			controller = p;
		}
		
	/**
	 * Break connection to existing NcePortController object. Once broken,
	 * attempts to send via "message" member will fail.
	 */
	public void disconnectPort(NcePortController p) {
			istream = null;
			ostream = null;
			if (controller != p)
				log.warn("disconnectPort: disconnect called from non-connected LnPortController");
			controller = null;
		}
				
	/**
	 * static function returning the NceTrafficController instance to use.
	 * @return The registered NceTrafficController instance for general use,
	 *         if need be creating one.
	 */
	static public NceTrafficController instance() { 
		if (self == null) {
			if (log.isDebugEnabled()) log.debug("creating a new NceTrafficController object");
			self = new NceTrafficController();
		}
		return self;
	}
	
	static protected NceTrafficController self = null;
	
// data members to hold the streams
	DataInputStream istream = null;
	OutputStream ostream = null;

	
	/**
	 * Handle incoming characters.  This is a permanent loop,
	 * looking for input messages in character form on the 
	 * stream connected to the PortController via <code>connectPort</code>.
	 * Terminates with the input stream breaking out of the try block.
	 */
	public void run() {
		while (true) {   // loop permanently, stream close will exit via exception
			try {
				handleOneIncomingReply();
			}
			catch (java.io.IOException e) {
				log.warn("run: Exception: "+e.toString());
			}
		}
	}

	void handleOneIncomingReply() throws java.io.IOException {
		// we sit in this until the message is complete, relying on
		// threading to let other stuff happen
		
		// Create output message
		NceReply msg = new NceReply();
        // message exists, now fill it
        int i;
        for (i = 0; i < NceReply.maxSize; i++) {
        	byte char1 = istream.readByte();
            msg.setElement(i, char1);
        	if (endCOMMAND(msg)) break;
        }
              	
        // message is complete, dispatch it !!
        if (log.isDebugEnabled()) log.debug("dispatch reply of length "+i);
        notifyReply(msg);
	}
	
	boolean endCOMMAND(NceReply msg) {
		// detect that the reply buffer ends with "COMMAND: " (note ending space)
		int num = msg.getNumDataElements();
		if ( num >= 9) {
			// ptr is offset of last element in NceReply
			int ptr = num-1;
			if (msg.getElement(ptr-1) != ':') return false;
			if (msg.getElement(ptr)   != ' ') return false;
			if (msg.getElement(ptr-2) != 'D') return false;
			return true;
		} 
		else return false;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTrafficController.class.getName());
}


/* @(#)NceTrafficController.java */

