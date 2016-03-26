package jmri.jmrix.dccpp.dccppovertcp;

import java.util.NoSuchElementException;
import java.util.LinkedList;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppNetworkPortController;
import jmri.jmrix.dccpp.DCCppPacketizer;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppReplyParser;
import jmri.jmrix.dccpp.DCCppListener;
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
 *
 * Based on LnOverTcpPacketizer
 *
 */

// TODO: Consider ditching the LocoNet-inherited "RECEIVE" and "SEND" prefixes
// and just rely on the already-present "<" and ">" to mark start and end
// of frame.  This would pretty much make DCCppOverTCP redundant with the
// Network Port interface to the Base Station (that is, the "host" JMRI
// application would look just like a Network Base Station to the "client" JMRI
// application).
//
// However, at minimum, this would break backward compatibility for the interface,
// so there is that to consider.  Probably best to do this sooner than later,
// to minimize that impact.
// 
public class DCCppOverTcpPacketizer extends DCCppPacketizer {

    static final String RECEIVE_PREFIX = "RECEIVE";
    static final String SEND_PREFIX = "SEND";
    
    protected BufferedReader istreamReader = null;
    
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
    public LinkedList<DCCppMessage> xmtList = new LinkedList<DCCppMessage>();
    
    
    
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Only used during system initialization")
    public DCCppOverTcpPacketizer(DCCppCommandStation cs) {
        super(cs); // Don't need the command station (?)
    
        xmtHandler = new XmtHandler();
        rcvHandler = new RcvHandler(this);
        log.debug("DCCppOverTcpPacketizer created.");
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
        istreamReader = new BufferedReader(new InputStreamReader(istream));
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
     * Forward a preformatted LocoNetMessage to the actual interface.
     *
     * Checksum is computed and overwritten here, then the message is converted
     * to a byte array and queue for transmission
     *
     * @param m Message to send; will be updated with CRC
     */
    public void sendDCCppMessage(DCCppMessage m, DCCppListener reply) {
        // update statistics
        //transmittedMsgCount++;
    
        log.debug("queue DCCpp packet: " + m.toString());
        // in an atomic operation, queue the request and wake the xmit thread
        try {
            synchronized (xmtHandler) {
                xmtList.addLast(m);
                xmtHandler.notify();
            }
        } catch (Exception e) {
            log.warn("passing to xmit: unexpected exception: " + e);
        }
    }

    /**
     * Invoked at startup to start the threads needed here.
     */
    public void startThreads() {
        int priority = Thread.currentThread().getPriority();
        log.debug("startThreads current priority = " + priority
                  + " max available = " + Thread.MAX_PRIORITY
                  + " default = " + Thread.NORM_PRIORITY
                  + " min available = " + Thread.MIN_PRIORITY);
    
        // make sure that the xmt priority is no lower than the current priority
        int xmtpriority = (Thread.MAX_PRIORITY - 1 > priority ? Thread.MAX_PRIORITY - 1 : Thread.MAX_PRIORITY);
        // start the XmtHandler in a thread of its own
        if (xmtHandler == null) {
            xmtHandler = new XmtHandler();
        }
        Thread xmtThread = new Thread(xmtHandler, "DCC++ transmit handler");
        log.debug("Xmt thread starts at priority " + xmtpriority);
        xmtThread.setDaemon(true);
        xmtThread.setPriority(Thread.MAX_PRIORITY - 1);
        xmtThread.start();
    
        // start the RcvHandler in a thread of its own
        if (rcvHandler == null) {
            rcvHandler = new RcvHandler(this);
        }
        Thread rcvThread = new Thread(rcvHandler, "DCC++ receive handler");
        rcvThread.setDaemon(true);
        rcvThread.setPriority(Thread.MAX_PRIORITY);
        rcvThread.start();
    
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
        
                    if (istreamReader == null) { 
                        log.error("istreamReader not initialized!"); 
                        return;
                    }
                    rxLine = istreamReader.readLine(); // Note: This uses BufferedReader for safer data handling
                    if (rxLine == null) {
                        log.warn("run: input stream returned null, exiting loop");
                        return;
                    }
                
                    if (debug) {
                        log.debug("Received: " + rxLine);
                    }
                
                    if (!rxLine.startsWith(RECEIVE_PREFIX)) {
                        // Not a valid Tcp packet
                        log.debug("Wrong Prefix: {}", rxLine);
                        continue;
                    }
                
                    // Strip the prefix off.
                    final int trim = RECEIVE_PREFIX.length();
                    rxLine = rxLine.substring(trim);
                
                    int firstidx = rxLine.indexOf("<");
                    int lastidx = rxLine.lastIndexOf(">");
                    log.debug("String {} Index1 {} Index 2{}", rxLine, firstidx, lastidx);
                    //  Note: the substring call below also strips off the "< >"
                    DCCppReply msg = DCCppReplyParser.parseReply(rxLine.substring(rxLine.indexOf("<")+1,
                                                                                  rxLine.lastIndexOf(">")));
                
                    if (!msg.isValidReplyFormat()) {
                        log.warn("Invalid Reply Format: {}", msg.toString());
                        continue;
                    }
                    // message is complete, dispatch it !!
                    if (log.isDebugEnabled()) {
                        log.debug("queue reply for notification");
                    }
                
                    final DCCppReply thisMsg = msg;
                    //final DCCppPacketizer thisTC = trafficController;
                    // return a notification via the queue to ensure end
                    Runnable r = new Runnable() {
                            DCCppReply msgForLater = thisMsg;
            
                            public void run() {
                                notifyReply(msgForLater, null);
                            }
                        };
                    javax.swing.SwingUtilities.invokeLater(r);
                    // done with this one
                //} catch (DCCppMessageException e) {
                // just let it ride for now
                //  log.warn("run: unexpected DCCppMessageException: ", );
                } catch (java.io.EOFException e) {
                    // posted from idle port when enableReceiveTimeout used
                    if (debug) {
                        log.debug("EOFException, is DC++ serial I/O using timeouts?");
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
                    DCCppMessage msg = null;
                    synchronized (this) {
                        msg = xmtList.removeFirst();
                    }
                
                    // input - now send
                    try {
                        if (ostream != null) {
                            //Commented out as the origianl LnPortnetworkController always returned true.
                            //if (!networkController.okToSend()) log.warn("LocoNet port not ready to receive"); // TCP, not RS232, so message is a real warning
                            if (debug) {
                                log.debug("start write to network stream");
                            }
                            StringBuffer packet = new StringBuffer(msg.length() + SEND_PREFIX.length() + 2);
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

    private final static Logger log = LoggerFactory.getLogger(DCCppOverTcpPacketizer.class.getName());
}
