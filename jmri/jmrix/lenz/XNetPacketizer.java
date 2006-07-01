/**
 * XNetPacketizer.java
 */

package jmri.jmrix.lenz;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;

import com.sun.java.util.collections.LinkedList;
import com.sun.java.util.collections.NoSuchElementException;

import java.util.Vector;

/**
 * Converts Stream-based I/O to/from XNet messages.  The "XNetInterface"
 * side sends/receives XNetMessage objects.  The connection to
 * a XNetPortController is via a pair of *Streams, which then carry sequences
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
 * @version 		$Revision: 1.3 $
 *
 */
public class XNetPacketizer extends XNetTrafficController {

	public XNetPacketizer(LenzCommandStation pCommandStation) {
        super(pCommandStation);
        self=this;
    }


// The methods to implement the XNetInterface

	public boolean status() { return (ostream != null & istream != null);
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
	 * Forward a preformatted XNetMessage to the actual interface.
	 *
	 * Checksum is computed and overwritten here, then the message
	 * is converted to a byte array and queue for transmission
     * @param m Message to send; will be updated with CRC
	 */
	public void sendXNetMessage(XNetMessage m, XNetListener reply) {
		// set the error correcting code byte
		int len = m.getNumDataElements();
		int chksum = 0x00;  /* the seed */
   		int loop;

    	for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
        	chksum ^= m.getElement(loop);
        }
		m.setElement(len-1, chksum);  // checksum is last element of message

        // notify all _other_ listeners
		notify(m, reply);

		// stream to port in single write, as that's needed by serial
		byte msg[] = new byte[len];
		for (int i=0; i< len; i++)
			msg[i] = (byte) m.getElement(i);

		if (log.isDebugEnabled()) log.debug("queue outgoing packet: "+m.toString());
		// in an atomic operation, queue the request and wake the xmit thread
		synchronized(xmtHandler) {
			xmtList.addLast(msg);
			xmtHandler.notify();
		}
	}

// methods to connect/disconnect to a source of data in a XNetPortController
	private XNetPortController controller = null;

	/**
	 * Make connection to existing XNetPortController object.
     * @param p Port controller for connected. Save this for a later
     *              disconnect call
	 */
	public void connectPort(XNetPortController p) {
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
	public void disconnectPort(XNetPortController p) {
			istream = null;
			ostream = null;
			if (controller != p)
				log.warn("disconnectPort: disconnect called from non-connected XNetPortController");
			controller = null;
		}

// data members to hold the streams
	DataInputStream istream = null;
	OutputStream ostream = null;


	/**
	 * Handle incoming characters.  This is a permanent loop,
	 * looking for input messages in character form on the
	 * stream connected to the XNetPortController via <code>connectPort</code>.
	 * Terminates with the input stream breaking out of the try block.
	 */
	public void run() {
		int opCode;
		while (true) {   // loop permanently, program close will exit
			try {
				// start by looking for command
				opCode = istream.readByte()&0xFF;
                // Create output message
				log.debug("Run: Start message with opcode: "+Integer.toHexString(opCode));
                int len = (opCode&0x0f)+2;  // opCode+Nbytes+ECC
				XNetMessage msg = new XNetMessage(len);
                msg.setElement(0, opCode);

                // message exists, now fill it
                //log.debug("len: "+len);
                for (int i = 1; i < len; i++)  {
                    int b = istream.readByte()&0xFF;
                    //log.debug("char "+i+" is: "+Integer.toHexString(b));
             		msg.setElement(i, b);
 				}
				// check parity
				if (!msg.checkParity()) {
					log.warn("Ignore packet with bad checksum: "+msg.toString());
					throw new XNetMessageException();
				}
             	// message is complete, dispatch it !!
             	{
             		final XNetMessage thisMsg = msg;
             		final XNetPacketizer thisTC = this;
 					// return a notification via the queue to ensure end
					Runnable r = new Runnable() {
						XNetMessage msgForLater = thisMsg;
						XNetPacketizer myTC = thisTC;
						public void run() {
           					myTC.notify(msgForLater,null);
						}
					};
					javax.swing.SwingUtilities.invokeLater(r);
				}

             	// done with this one
            }
 			catch (XNetMessageException e) {
				// just let it ride for now
			}
 			catch (java.io.EOFException e) {
				// posted from idle port when enableReceiveTimeout used
				log.debug("EOFException, is serial I/O using timeouts?");
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
		 * Remember the Packetizer object
		 */
		XNetPacketizer trafficController;
		public RcvHandler(XNetPacketizer lt) {
			trafficController = lt;
		}

	    public void run() {
		    int opCode;
		    while (true) {   // loop permanently, program close will exit
			    try {
				    // start by looking for command
				    opCode = istream.readByte()&0xFF;
                    // Create output message
				    log.debug("RcvHandler: Start message with opcode: "+Integer.toHexString(opCode));
                    int len = (opCode&0x0f)+2;  // opCode+Nbytes+ECC
				    XNetMessage msg = new XNetMessage(len);
                    msg.setElement(0, opCode);

                    // message exists, now fill it
                    //log.debug("len: "+len);
                    for (int i = 1; i < len; i++)  {
                        int b = istream.readByte()&0xFF;
                        log.debug("char "+i+" of "+len+" is: "+Integer.toHexString(b));
             		    msg.setElement(i, b);
 				    }
				    // check parity
				    if (!msg.checkParity()) {
					    log.warn("Ignore packet with bad checksum: "+msg.toString());
					    throw new XNetMessageException();
				    }
             	    // message is complete, dispatch it !!
             	    {
             		    final XNetMessage thisMsg = msg;
             		    final XNetPacketizer thisTC = trafficController;
 					    // return a notification via the queue to ensure end
					    Runnable r = new Runnable() {
						    XNetMessage msgForLater = thisMsg;
						    XNetPacketizer myTC = thisTC;
						    public void run() {
           					    myTC.notify(msgForLater, null);
						    }
					    };
                        log.debug("schedule notify of incoming packet");
					    javax.swing.SwingUtilities.invokeLater(r);
				    }

             	    // done with this one
                }
 			    catch (XNetMessageException e) {
				    // just let it ride for now
			    }
 			    catch (java.io.EOFException e) {
				    // posted from idle port when enableReceiveTimeout used
				    log.debug("EOFException, is serial I/O using timeouts?");
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
							log.warn("send message: no connection established");
						}
					}
					catch (java.io.IOException e) {
						log.warn("send message: IOException: "+e.toString());
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
		Thread xmtThread = new Thread(xmtHandler, "XNet transmit handler");
		log.debug("Xmt thread starts at priority "+xmtpriority);
		xmtThread.setPriority(Thread.MAX_PRIORITY-1);
		xmtThread.start();

		// start the RcvHandler in a thread of its own
		Thread rcvThread = new Thread(rcvHandler, "XNet receive handler");
		rcvThread.setPriority(Thread.MAX_PRIORITY);
		rcvThread.start();

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetPacketizer.class.getName());
}


/* @(#)XNetPacketizer.java */

