/**
 * LnPacketizer.java
 *
 * Description:		Converts Stream-based I/O to/from LocoNet messages
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version
 */

package jmri.jmrix.loconet;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;

import com.sun.java.util.collections.LinkedList;
import com.sun.java.util.collections.NoSuchElementException;

import java.util.Vector;

/**
 * Converts Stream-based I/O to/from LocoNet messages.  The "LocoNetInterface"
 * side sends/receives LocoNetMessage objects.  The connection to
 * a LnPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.
 *<P>
 * Messages come to this via the main GUI thread, and are forwarded back to
 * listeners in that same thread.  Reception and transmission are handled in
 * dedicated threads by RcvHandler and XmtHandler objects.  Those are internal
 * classes defined here. The thread priorities are:
 *<P><UL>
 *<LI>  RcvHandler - at highest available priority
 *<LI>  XmtHandler - down one, which is assumed to be above the GUI
 *<LI>  (everything else)
 *</UL>
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version 		$Id: LnPacketizer.java,v 1.1 2002-03-18 04:52:43 jacobsen Exp $
 *
 */
public class LnPacketizer extends LnTrafficController {

	public LnPacketizer() {self=this;}


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
	 * Synchronized list used as a transmit queue
	 */
    LinkedList xmtList = new LinkedList();

	/**
	 * XmtHandler (a local class) object to implement the transmit thread
	 */
	XmtHandler xmtHandler = new XmtHandler();

	/**
	 * RcvHandler (a local class) object to implement the receive thread
	 */
	RcvHandler rcvHandler = new RcvHandler(this);

	/**
	 * Forward a preformatted LocoNetMessage to the actual interface.
	 *
	 * Checksum is computed and overwritten here, then the message
	 * is converted to a byte array and queue for transmission
     * @param m Message to send; will be updated with CRC
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

		if (log.isDebugEnabled()) log.debug("queue LocoNet packet: "+m.toString());
		// in an atomic operation, queue the request and wake the xmit thread
		synchronized(xmtHandler) {
			xmtList.addLast(msg);
			xmtHandler.notify();
		}
	}

// methods to connect/disconnect to a source of data in a LnPortController
	private LnPortController controller = null;

	/**
	 * Make connection to existing LnPortController object.
     * @param p Port controller for connected. Save this for a later
     *              disconnect call
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
     * @param p previously connected port
	 */
	public void disconnectPort(LnPortController p) {
			istream = null;
			ostream = null;
			if (controller != p)
				log.warn("disconnectPort: disconnect called from non-connected LnPortController");
			controller = null;
		}

// data members to hold the streams
	DataInputStream istream = null;
	OutputStream ostream = null;

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

	/**
	 * Handle incoming characters.  This is a permanent loop,
	 * looking for input messages in character form on the
	 * stream connected to the LnPortController via <code>connectPort</code>.
	 * Terminates with the input stream breaking out of the try block.
	 */
	public void run() {
		int opCode;
		while (true) {   // loop permanently, program close will exit
			try {
				// start by looking for command -  skip if bit not set
				while ( ((opCode = (istream.readByte()&0xFF)) & 0x80) == 0 )  {
					//log.debug("Skipping: "+Integer.toHexString(opCode));
				}
				// here opCode is OK. Create output message
				// log.debug("Start message with opcode: "+Integer.toHexString(opCode));
				LocoNetMessage msg = null;
				while (msg == null) {
					try {
						// Capture 2nd byte, always present
						int byte2 = istream.readByte()&0xFF;
						//log.debug("Byte2: "+Integer.toHexString(byte2));
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
    	                    		if (byte2<2) log.error("LocoNet message length invalid: "+byte2
    	                    						+" opcode: "+Integer.toHexString(opCode));
        	                    	msg = new LocoNetMessage(byte2);
            	                	break;
                	    	}
             			// message exists, now fill it
             			msg.setOpCode(opCode);
             			msg.setElement(1, byte2);
             			int len = msg.getNumDataElements();
						//log.debug("len: "+len);
             			for (int i = 2; i < len; i++)  {
             				// check for message-blocking error
             				int b = istream.readByte()&0xFF;
 							//log.debug("char "+i+" is: "+Integer.toHexString(b));
            				if ( (b&0x80) != 0) {
             					log.warn("LocoNet message with opCode: "
             							+Integer.toHexString(opCode)
             							+" ended early. Expected length: "+len
             							+" seen length: "+i
             							+" unexpected byte: "
             							+Integer.toHexString(b));
             					opCode = b;
             					throw new LocoNetMessageException();
             				}
             				msg.setElement(i, b);
             			}
 					}
 					catch (LocoNetMessageException e) {
 						// retry by destroying the existing message
 						// opCode is set for the newly-started packet
 						msg = null;
 					}
 				}
				// check parity
				if (!msg.checkParity()) {
					log.warn("Ignore Loconet packet with bad checksum: "+msg.toString());
					throw new LocoNetMessageException();
				}
             	// message is complete, dispatch it !!
             	{
             		final LocoNetMessage thisMsg = msg;
             		final LnPacketizer thisTC = this;
 					// return a notification via the queue to ensure end
					Runnable r = new Runnable() {
						LocoNetMessage msgForLater = thisMsg;
						LnPacketizer myTC = thisTC;
						public void run() {
           					myTC.notify(msgForLater);
						}
					};
					javax.swing.SwingUtilities.invokeLater(r);
				}

             	// done with this one
            }
 			catch (LocoNetMessageException e) {
				// just let it ride for now
			}
 			catch (java.io.EOFException e) {
				// posted from idle port when enableReceiveTimeout used
				log.debug("EOFException, is LocoNet serial I/O using timeouts?");
			}
 			catch (java.io.IOException e) {
				// fired when write-end of HexFile reaches end
				log.debug("IOException, should only happen with HexFIle: "+e);
				log.info("End of file");
				disconnectPort(controller);
				return;
			}
 			catch (Exception e) {
				log.warn("run: unexpected exception: "+e);
			}
		} // end of permanent loop
	}

	/**
	 * Captive class to handle incoming characters.  This is a permanent loop,
	 * looking for input messages in character form on the
	 * stream connected to the LnPortController via <code>connectPort</code>.
	 */
	class RcvHandler implements Runnable {
		/**
		 * Remember the LnPacketizer object
		 */
		LnPacketizer trafficController;
		public RcvHandler(LnPacketizer lt) {
			trafficController = lt;
		}

		public void run() {
			boolean debug = log.isDebugEnabled();

			int opCode;
			while (true) {   // loop permanently, program close will exit
				try {
					// start by looking for command -  skip if bit not set
					while ( ((opCode = (istream.readByte()&0xFF)) & 0x80) == 0 )  {
						if (debug) log.debug("Skipping: "+Integer.toHexString(opCode));
					}
					// here opCode is OK. Create output message
					if (debug) log.debug("Start message with opcode: "+Integer.toHexString(opCode));
					LocoNetMessage msg = null;
					while (msg == null) {
						try {
							// Capture 2nd byte, always present
							int byte2 = istream.readByte()&0xFF;
							//log.debug("Byte2: "+Integer.toHexString(byte2));
							// Decide length
							switch((opCode & 0x60) >> 5) {
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
    	                    		if (byte2<2) log.error("LocoNet message length invalid: "+byte2
    	                    						+" opcode: "+Integer.toHexString(opCode));
        	                    	msg = new LocoNetMessage(byte2);
            	                	break;
                	    		}
             				// message exists, now fill it
             				msg.setOpCode(opCode);
             				msg.setElement(1, byte2);
             				int len = msg.getNumDataElements();
							//log.debug("len: "+len);
             				for (int i = 2; i < len; i++)  {
             					// check for message-blocking error
             					int b = istream.readByte()&0xFF;
 								//log.debug("char "+i+" is: "+Integer.toHexString(b));
            					if ( (b&0x80) != 0) {
             						log.warn("LocoNet message with opCode: "
             							+Integer.toHexString(opCode)
             							+" ended early. Expected length: "+len
             							+" seen length: "+i
             							+" unexpected byte: "
             							+Integer.toHexString(b));
             						opCode = b;
             						throw new LocoNetMessageException();
             					}
             					msg.setElement(i, b);
             				}
 						}
 						catch (LocoNetMessageException e) {
 							// retry by destroying the existing message
 							// opCode is set for the newly-started packet
 							msg = null;
 						}
 					}
					// check parity
					if (!msg.checkParity()) {
						log.warn("Ignore Loconet packet with bad checksum: "+msg.toString());
						throw new LocoNetMessageException();
					}
             		// message is complete, dispatch it !!
             		{
             			if (log.isDebugEnabled()) log.debug("queue message for notification");
             			final LocoNetMessage thisMsg = msg;
             			final LnPacketizer thisTC = trafficController;
 						// return a notification via the queue to ensure end
						Runnable r = new Runnable() {
							LocoNetMessage msgForLater = thisMsg;
							LnPacketizer myTC = thisTC;
							public void run() {
           						myTC.notify(msgForLater);
							}
						};
						javax.swing.SwingUtilities.invokeLater(r);
					}

             		// done with this one
            	}
 				catch (LocoNetMessageException e) {
					// just let it ride for now
					log.warn("run: unexpected LocoNetMessageException: "+e);
				}
 				catch (java.io.EOFException e) {
					// posted from idle port when enableReceiveTimeout used
					if (debug) log.debug("EOFException, is LocoNet serial I/O using timeouts?");
				}
 				catch (java.io.IOException e) {
					// fired when write-end of HexFile reaches end
					if (debug) log.debug("IOException, should only happen with HexFIle: "+e);
					log.info("End of file");
					disconnectPort(controller);
					return;
				}
 				// normally, we don't catch the unnamed Exception, but in this
 				// permanently running loop it seems wise.
 				catch (Exception e) {
					log.warn("run: unexpected Exception: "+e);
				}
			} // end of permanent loop
		}
	}

	/**
	 * Captive class to handle transmission
	 */
	class XmtHandler implements Runnable {
		public void run() {
			boolean debug = log.isDebugEnabled();

			while (true) {   // loop permanently
				// any input?
				try {
					// get content; failure is a NoSuchElementException
					if (debug) log.debug("check for input");
					byte msg[] = null;
					synchronized (this) {
						 msg = (byte[])xmtList.removeFirst();
					}

					// input - now send
					try {
						if (ostream != null) {
							if (!controller.okToSend()) log.warn("LocoNet port not ready to receive");
							if (debug) log.debug("start write to stream");
							ostream.write(msg);
							if (debug) log.debug("end write to stream");
						} else {
							// no stream connected
							log.warn("sendLocoNetMessage: no connection established");
						}
					}
					catch (java.io.IOException e) {
						log.warn("sendLocoNetMessage: IOException: "+e.toString());
					}
				}
				catch (NoSuchElementException e) {
					// message queue was empty, wait for input
					if (debug) log.debug("start wait");
					try {
						synchronized(this) {
							wait();
						}
					}
					catch (java.lang.InterruptedException ei) {}
					if (debug) log.debug("end wait");
				}
			}
		}
	}

	/**
	 * Invoked at startup to start the threads needed here.
	 */
	public void startThreads() {
		int priority = Thread.currentThread().getPriority();
		log.debug("startThreads current priority = "+priority+
					" max available = "+Thread.MAX_PRIORITY+
					" default = "+Thread.NORM_PRIORITY+
					" min available = "+Thread.MIN_PRIORITY);

		// make sure that the xmt priority is no lower than the current priority
		int xmtpriority = (Thread.MAX_PRIORITY-1>priority ? Thread.MAX_PRIORITY-1 : Thread.MAX_PRIORITY);
		// start the XmtHandler in a thread of its own
		Thread xmtThread = new Thread(xmtHandler, "LocoNet transmit handler");
		log.debug("Xmt thread starts at priority "+xmtpriority);
		xmtThread.setPriority(Thread.MAX_PRIORITY-1);
		xmtThread.start();

		// start the RcvHandler in a thread of its own
		Thread rcvThread = new Thread(rcvHandler, "LocoNet receive handler");
		rcvThread.setPriority(Thread.MAX_PRIORITY);
		rcvThread.start();

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnPacketizer.class.getName());
}


/* @(#)LnPacketizer.java */

