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

	public NceTrafficController() {self=this;}
	

// The methods to implement the NceInterface

	protected Vector listeners = new Vector();
	
	public boolean status() { return (ostream != null & istream != null); 
		}

	public synchronized void addNceListener(int mask, NceListener l) { 
			// add only if not already registered
			if (l == null) throw new java.lang.NullPointerException();
			if (!listeners.contains(l)) {
					listeners.addElement(l);
				}
		}

	public synchronized void removeNceListener(int mask, NceListener l) {
			if (listeners.contains(l)) {
					listeners.removeElement(l);
				}
		}

	/**
	 * Forward a preformatted message to the actual interface.
	 *
	 * Checksum is computed and overwritten here.
	 */
	public void sendNceMessage(NceMessage m) {
		// stream to port in single write, as that's needed by serial
		int len = 0;
		byte msg[] = new byte[len];
		for (int i=0; i< len; i++)
			msg[i] = (byte) m.getElement(i);
		try {
			if (ostream != null)
				ostream.write(msg);
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
	 * Make connection to existing LnPortController object.
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
		if (self == null) self = new NceTrafficController();
		return self;
	}
	
	static private NceTrafficController self = null;
	
// data members to hold the streams
	DataInputStream istream = null;
	OutputStream ostream = null;

	/**
	 * Forward a NceMessage to all registered listeners.
	 */
	protected void notify(NceMessage m) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this)
			{
				v = (Vector) listeners.clone();
			}
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			NceListener client = (NceListener) listeners.elementAt(i);
			try {
				client.message(m);
				}
			catch (Exception e)
				{
					log.warn("notify: During dispatch to "+client+"\nException "+e);
				}
			}
	}
	
	/**
	 * Handle incoming characters.  This is a permanent loop,
	 * looking for input messages in character form on the 
	 * stream connected to the LnPortController via <code>connectPort</code>.
	 * Terminates with the input stream breaking out of the try block.
	 */
	public void run() {
			int opCode;
			try {
			 while (true) {   // loop permanently, stream close will exit
				// read first byte, assumed to start reply
				char char1 = istream.readChar();
				// here opCode is OK. Create output message
				NceMessage msg = new NceMessage(1);
             	// message exists, now fill it
             	msg.setOpCode(char1);
               	int len = msg.getNumDataElements();
             	for (int i = 2; i < len; i++) msg.setElement(i, istream.readChar());
             	// confirm you've got the message right...
             	
             	// message is complete, dispatch it !!
             	notify(msg);
             	
             	// done with this one
            	}  // end loop until no data available
            // at this point, input stream is not available
            // so we just fall off end to stop running
            
			} // end of try
		catch (Exception e) {
			log.warn("run: Exception: "+e.toString());
			}
		}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTrafficController.class.getName());
}


/* @(#)NceTrafficController.java */

