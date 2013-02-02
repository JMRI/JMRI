/**
 * Mx1Packetizer.java
 */

package jmri.jmrix.zimo;

import org.apache.log4j.Logger;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.NoSuchElementException;
  
/** 
 * Access to Zimo Mx1 messages
 * via stream-based I/O.  The "Mx1Interface" * side sends/receives Mx1Message objects.  The connection to
 * a Mx1PortController is via a pair of *Streams, which then carry sequences
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
 * @version 		$Revision$
 * 
 * Adapted by Sip Bosch for use with zimo Mx-1
 *
 */
public class Mx1Packetizer extends Mx1TrafficController {

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                        justification="temporary until mult-system; only set at startup")
	public Mx1Packetizer(Mx1CommandStation pCommandStation) {
        super(pCommandStation);
        self=this;
        }

        // The methods to implement the Mx1Interface
	public boolean status() { return (ostream != null & istream != null);
	}

	/**
	 * Synchronized list used as a transmit queue
	 */
         LinkedList<byte[]> xmtList = new LinkedList<byte[]>();

	/**
	 * XmtHandler (a local class) object to implement the transmit thread
	 */
	 XmtHandler xmtHandler = new XmtHandler();

	/**
	 * RcvHandler (a local class) object to implement the receive thread
	 */
	 RcvHandler rcvHandler = new RcvHandler(this);

	/**
	 * Forward a preformatted Mx1Message to the actual interface.
	 *
	 * End of Message is added here, then the message
	 * is converted to a byte array and queued for transmission
         * @param m Message to send; will be updated with CRC
	 */
	public void sendMx1Message(Mx1Message m, Mx1Listener reply) {
		// set the CR code byte
		int len = m.getNumDataElements();
                m.setElement(len-1, 0x0D);  // CR is last element of message
                // notify all _other_ listeners
		notify(m, reply);
		// stream to port in single write, as that's needed by serial
		byte msg[] = new byte[len];
		for (int i=0; i < len; i++)
                   msg[i] = (byte) m.getElement(i);
                if (log.isDebugEnabled()) log.debug("queue outgoing packet: "+m.toString());
		// in an atomic operation, queue the request and wake the xmit thread
		synchronized(xmtHandler) {
		   xmtList.addLast(msg);
		   xmtHandler.notify();
                }
	}

        // methods to connect/disconnect to a source of data in a Mx1PortController
	private Mx1PortController controller = null;

	/**
	* Make connection to existing Mx1PortController object.
        * @param p Port controller for connected. Save this for a later
        * disconnect call
        */
	public void connectPort(Mx1PortController p) {
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
	public void disconnectPort(Mx1PortController p) {
			istream = null;
			ostream = null;
			if (controller != p)
				log.warn("disconnectPort: disconnect called from non-connected Mx1PortController");
			controller = null;
		}

// data members to hold the streams
	DataInputStream istream = null;
	OutputStream ostream = null;


	/**
	 * Handle incoming characters.  This is a permanent loop,
	 * looking for input messages in character form on the
	 * stream connected to the Mx1PortController via <code>connectPort</code>.
	 * Terminates with the input stream breaking out of the try block.
	 */

	class RcvHandler implements Runnable {
		/**
		 * Remember the Packetizer object
		 */
		Mx1Packetizer trafficController;
		public RcvHandler(Mx1Packetizer lt) {
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
                               int len=1;
                               Mx1Message msgn = new Mx1Message(15);
                               msgn.setElement(0,opCode);
                               // message exists, now fill it
                               for (int i = 1; i < 15; i++)  {
                                 int b = istream.readByte()&0xFF;
                                 len = len + 1;
                                 //if end of message
                                 if (b == 0x0D | b == 0x0A) {
                                   msgn.setElement(i,b);
                                   break;
                                 }
                                 msgn.setElement(i, b);
                               }
                               //transfer to array with now known size
                               Mx1Message msg = new Mx1Message(len);
                               for (int i = 0; i < len; i++)
                                 msg.setElement(i,msgn.getElement(i)&0xFF);
                               // message is complete, dispatch it !!
                               {
                               final Mx1Message thisMsg = msg;
                               final Mx1Packetizer thisTC = trafficController;
                               // return a notification via the queue to ensure end
			       Runnable r = new Runnable() {
				  Mx1Message msgForLater = thisMsg;
				  Mx1Packetizer myTC = thisTC;
				  public void run() {
           			    myTC.notify(msgForLater, null);
				  }
				};
                               log.debug("schedule notify of incoming packet");
			       javax.swing.SwingUtilities.invokeLater(r);
                               }
                    }
             	    // done with this one
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
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="UW_UNCOND_WAIT",
                                        justification="while loop controls access")
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
                              msg = xmtList.removeFirst();
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
                            catch (java.lang.InterruptedException ei) {
                                Thread.currentThread().interrupt(); // retain if needed later
                            }
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

      	static Logger log = Logger.getLogger(Mx1Packetizer.class.getName());
}


/* @(#)Mx1Packetizer.java */

