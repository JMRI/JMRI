/** 
 * LnTrafficController.java
 *
 * Description:		Converts Stream-based I/O to/from LocoNet messages
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;

/** 
 * Converts Stream-based I/O to/from LocoNet messages.  The "LocoNetInterface"
 * side sends/receives LocoNetMessage objects.  The connection to 
 * a LnPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is 
 * handled in an independent thread.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 */
public class LnTrafficController implements LocoNetInterface, Runnable {

	public LnTrafficController() {self=this;}
	

// The methods to implement the LocoNetInterface

	protected Vector listeners = new Vector();
	
	public boolean status() { return (ostream != null & istream != null); 
		}

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
	 * Forward a preformatted LocoNetMessage to the actual interface.
	 *
	 * Checksum is computed and overwritten here.
	 */
	public void sendLocoNetMessage(LocoNetMessage m) {
		// set the error correcting code byte
		int len = m.getNumDataElements();
		int chksum = 0xff;  /* the seed */
   		int loop;

    	for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
        	chksum ^= m.getElement(loop);
        }
		m.setElement(len-1, chksum);  // checksum is last element of message
		
		// stream to port in single write, as that's needed by serial
		byte msg[] = new byte[len];
		for (int i=0; i< len; i++)
			msg[i] = (byte) m.getElement(i);
		if (log.isDebugEnabled()) log.debug("send LocoNet packet: "+m.toString());
		try {
			if (ostream != null)
				ostream.write(msg);
			else {
				// no stream connected
				log.warn("sendLocoNetMessage: no connection established");
				}
			}
		catch (Exception e) {
			log.warn("sendLocoNetMessage: Exception: "+e.toString());
			}
		}

// methods to connect/disconnect to a source of data in a LnPortController
	private LnPortController controller = null;
	
	/**
	 * Make connection to existing LnPortController object.
	 */
	public void connectPort(LnPortController p) {
			istream = p.getInputStream();
			ostream = p.getOutputStream();
			if (controller != null)
				log.warn("connectPort: connect called while connected");
			controller = p;
		}
		
	/**
	 * Break connection to existing LnPortController object. Once broken,
	 * attempts to send via "message" member will fail.
	 */
	public void disconnectPort(LnPortController p) {
			istream = null;
			ostream = null;
			if (controller != p)
				log.warn("disconnectPort: disconnect called from non-connected LnPortController");
			controller = null;
		}
				
	/**
	 * static function returning the LnTrafficController instance to use.
	 * @return The registered LnTrafficController instance for general use,
	 *         if need be creating one.
	 */
	static public LnTrafficController instance() { 
		if (self == null) self = new LnTrafficController();
		return self;
	}
	
	static protected LnTrafficController self = null;
	
// data members to hold the streams
	DataInputStream istream = null;
	OutputStream ostream = null;

	/**
	 * Forward a LocoNetMessage to all registered listeners.
	 */
	protected void notify(LocoNetMessage m) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this)
			{
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
				// start by looking for command
				while ( ((opCode = (istream.readByte()&0xFF)) & 0x80) ==0 )  {};  // skip if bit not set
				// here opCode is OK. Create output message
				LocoNetMessage msg = null;
				// Capture 2nd byte, always present
				int byte2 = istream.readByte()&0xFF;
				// Decide length
				switch((opCode & 0x60) >> 5)
                    {
                        case 0:     /* 2 byte message */
                            msg = new LocoNetMessage(2);
                            break;

                        case 1:     /* 4 byte message */
                            msg = new LocoNetMessage(4);
                            break;

                        case 2:     /* 6 byte message */
                            msg = new LocoNetMessage(6);
                            break;

                        case 3:     /* N byte message */
                            msg = new LocoNetMessage(byte2);
                            break;
                    }
             	// message exists, now fill it
             	msg.setOpCode(opCode);
             	msg.setElement(1, byte2);
             	int len = msg.getNumDataElements();
             	for (int i = 2; i < len; i++) msg.setElement(i, istream.readByte()&0xFF);
             	// confirm you've got the message right...
             	
             	// message is complete, dispatch it !!
             	{ 
             		final LocoNetMessage thisMsg = msg;
             		final LnTrafficController thisTC = this;
 					// return a notification via the queue to ensure end
					Runnable r = new Runnable() {
						LocoNetMessage msgForLater = thisMsg;
						LnTrafficController myTC = thisTC;
						public void run() { 
							log.debug("Delayed notify starts");
           					myTC.notify(msgForLater);
						}
					};
					javax.swing.SwingUtilities.invokeLater(r);
				}
              	
             	// done with this one
            	}  // end loop until no data available
            // at this point, input stream is not available
            // so we just fall off end to stop running
            
			} // end of try
		catch (Exception e) {
			log.warn("run: Exception: "+e.toString());
			}
		}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnTrafficController.class.getName());
}


/* @(#)LnTrafficController.java */

