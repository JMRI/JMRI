// DCCppOverTcpPacketizer.java
package jmri.jmrix.dccpp.dccppovertcp;

import java.util.NoSuchElementException;
import java.util.LinkedList;
import jmri.jmrix.dccpp.DCCppNetworkPortController;
import jmri.jmrix.dccpp.DCCppPacketizer;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppMessageException; // TODO: we don't have this one!
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from LocoNet messages. The "LocoNetInterface"
 * side sends/receives LocoNetMessage objects. The connection to a
 * LnPortnetworkController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.
 * <P>
 * Messages come to this via the main GUI thread, and are forwarded back to
 * listeners in that same thread. Reception and transmission are handled in
 * dedicated threads by RcvHandler and XmtHandler objects. Those are internal
 * classes defined here. The thread priorities are:
 * <P>
 * <UL>
 * <LI> RcvHandler - at highest available priority
 * <LI> XmtHandler - down one, which is assumed to be above the GUI
 * <LI> (everything else)
 * </UL>
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Alex Shepherd Copyright (C) 2003, 2006
 * @author Mark Underwood Copyright (C) 2015
 * @version $Revision$
 *
 * Based on LnOverTcpPacketizer
 *
 */
public class DCCppOverTcpPacketizer extends DCCppPacketizer {

    static final String RECEIVE_PREFIX = "RECEIVE";
    static final String SEND_PREFIX = "SEND";

    /**
     * XmtHandler (a local class) object to implement the transmit thread
     */
    protected Runnable xmtHandler;

    /**
     * RcvHandler (a local class) object to implement the receive thread
     */
    protected Runnable rcvHandler;

    /**
     * Synchronized list used as a transmit queue.
     * <P>
     * This is public to allow access from the internal class(es) when compiling
     * with Java 1.1
     */
    public LinkedList<byte[]> xmtList = new LinkedList<byte[]>();



    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Only used during system initialization")
    public DCCppOverTcpPacketizer() {
	super(null); // Don't need the command station (?)

        xmtHandler = new XmtHandler();
        rcvHandler = new RcvHandler(this);
    }

    public DCCppNetworkPortController networkController = null;

    public boolean isXmtBusy() {
        if (networkController == null) {
            return false;
        }

        return true;
    }

    /**
     * Make connection to existing DCCppPortnetworkController object.
     *
     * @param p Port networkController for connected. Save this for a later
     *          disconnect call
     */
    public void connectPort(DCCppNetworkPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (networkController != null) {
            log.warn("connectPort: connect called while connected");
        }
        networkController = p;
    }

    /**
     * Break connection to existing LnPortnetworkController object. Once broken,
     * attempts to send via "message" member will fail.
     *
     * @param p previously connected port
     */
    public void disconnectPort(DCCppNetworkPortController p) {
        istream = null;
        ostream = null;
        if (networkController != p) {
            log.warn("disconnectPort: disconnect called from non-connected DCCppNetworkPortController");
        }
        networkController = null;
    }

    /**
     * Captive class to handle incoming characters. This is a permanent loop,
     * looking for input messages in character form on the stream connected to
     * the LnPortnetworkController via <code>connectPort</code>.
     */
    class RcvHandler implements Runnable {

        /**
         * Remember the DCCppPacketizer object
         */
        DCCppOverTcpPacketizer trafficController;

        public RcvHandler(DCCppOverTcpPacketizer lt) {
            trafficController = lt;
        }

        // readline is deprecated, but there are no problems
        // with multi-byte characters here.
        @SuppressWarnings({"deprecation", "null"})
        public void run() {
            boolean debug = log.isDebugEnabled();

            String rxLine;
            while (true) {   // loop permanently, program close will exit
                try {
                    // start by looking for a complete line
		    
                    rxLine = istream.readLine();
                    if (rxLine == null) {
                        log.warn("run: input stream returned null, exiting loop");
                        return;
                    }

                    if (debug) {
                        log.debug("Received: " + rxLine);
                    }
		    if (!rxLine.startsWith(RECEIVE_PREFIX)) {
			// Not a valid Tcp packet
			continue;
		    }

		    //Strip the prefix off.
		    final int trim = RECEIVE_PREFIX.length();
		    rxLine = rxLine.substring(trim);

		    DCCppMessage msg = new DCCppMessage(rxLine.substring(rxLine.indexOf("<"),
							    rxLine.lastIndexOf(">")));
		    
		    if (!msg.isValidMessageFormat()) {
			log.warn("Invalid Message Format: {}", msg.toString());
			continue;
		    }
		    // message is complete, dispatch it !!
		    if (log.isDebugEnabled()) {
			log.debug("queue message for notification");
		    }

		    final DCCppMessage thisMsg = msg;
		    final DCCppPacketizer thisTC = trafficController;
		    // return a notification via the queue to ensure end
		    Runnable r = new Runnable() {
                            DCCppMessage msgForLater = thisMsg;
                            DCCppPacketizer myTC = thisTC;
			    
                            public void run() {
                                myTC.sendDCCppMessage(msgForLater, null);
                            }
                        };
		    javax.swing.SwingUtilities.invokeLater(r);
                    // done with this one
		    //} catch (DCCppMessageException e) {
                    // just let it ride for now
                    //log.warn("run: unexpected DCCppMessageException: " + e);
                } catch (java.io.EOFException e) {
                    // posted from idle port when enableReceiveTimeout used
                    if (debug) {
                        log.debug("EOFException, is LocoNet serial I/O using timeouts?");
                    }
                } catch (java.io.IOException e) {
                    // fired when write-end of HexFile reaches end
                    if (debug) {
                        log.debug("IOException, should only happen with HexFIle: " + e);
                    }
                    log.info("End of file");
		    //  disconnectPort(networkController);
                    return;
                } // normally, we don't catch the unnamed Exception, but in this
                // permanently running loop it seems wise.
                catch (Exception e) {
                    log.warn("run: unexpected Exception: " + e);
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
                    if (debug) {
                        log.debug("check for input");
                    }
                    byte msg[] = null;
                    synchronized (this) {
                        msg = xmtList.removeFirst();
                    }

                    // input - now send
                    try {
                        if (ostream != null) {
                            //Commented out as the origianl LnPortnetworkController always returned true.
                            //if (!networkController.okToSend()) log.warn("LocoNet port not ready to receive"); // TCP, not RS232, so message is a real warning
                            if (debug) {
                                log.debug("start write to stream");
                            }
                            StringBuffer packet = new StringBuffer(msg.length * 3 + SEND_PREFIX.length() + 2);
                            packet.append(SEND_PREFIX);
                            String hexString = new String();
			    hexString += "<" + msg.toString() + ">";
			    packet.append(hexString);
                            if (debug) {
                                log.debug("Write to LbServer: " + packet.toString());
                            }
                            packet.append("\r\n");
                            ostream.write(packet.toString().getBytes());
                            ostream.flush();
                            if (debug) {
                                log.debug("end write to stream");
                            }
                        } else {
                            // no stream connected
                            log.warn("sendLocoNetMessage: no connection established");
                        }
                    } catch (java.io.IOException e) {
                        log.warn("sendLocoNetMessage: IOException: " + e.toString());
                    }
                } catch (NoSuchElementException e) {
                    // message queue was empty, wait for input
                    if (debug) {
                        log.debug("start wait");
                    }

                    new jmri.util.WaitHandler(this);  // handle synchronization, spurious wake, interruption

                    if (debug) {
                        log.debug("end wait");
                    }
                }
            }
        }
    }

    static Logger log = LoggerFactory.getLogger(DCCppOverTcpPacketizer.class.getName());
}

/* @(#)LnOverTcpPacketizer.java */
