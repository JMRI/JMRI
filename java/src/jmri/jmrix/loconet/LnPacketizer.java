package jmri.jmrix.loconet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from LocoNet messages. The "LocoNetInterface"
 * side sends/receives LocoNetMessage objects. The connection to a
 * LnPortController is via a pair of *Streams, which then carry sequences of
 * characters for transmission.
 * <p>
 * Messages come to this via the main GUI thread, and are forwarded back to
 * listeners in that same thread. Reception and transmission are handled in
 * dedicated threads by RcvHandler and XmtHandler objects. Those are internal
 * classes defined here. The thread priorities are:
 * <ul>
 *   <li> RcvHandler - at highest available priority
 *   <li> XmtHandler - down one, which is assumed to be above the GUI
 *   <li> (everything else)
 * </ul>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2018
 */
public class LnPacketizer extends LnTrafficController {

    /**
     * True if the external hardware is not echoing messages, so we must.
     */
    protected boolean echo = false;  // true = echo messages here, instead of in hardware

    /**
     * Create a default LnPacketizer instance without a SystemConnectionMemo.
     * Not compatible with multi connections.
     *
     * @deprecated since 4.11.6, use LnPacketizer(LocoNetSystemConnectionMemo) instead
     */
    @Deprecated
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Only used during system initialization") // NOI18N
    public LnPacketizer() {
        jmri.util.Log4JUtil.deprecationWarning(log, "LnPacketizer");  
    }

    public LnPacketizer(LocoNetSystemConnectionMemo m) {
        // set the memo to point here
        memo = m;
        m.setLnTrafficController(this);
    }

    // The methods to implement the LocoNetInterface

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean status() {
        return (ostream != null && istream != null && xmtThread != null && rcvThread != null);
    }

    /**
     * Synchronized list used as a transmit queue.
     */
    protected LinkedList<byte[]> xmtList = new LinkedList<byte[]>();

    /**
     * XmtHandler (a local class) object to implement the transmit thread.
     * <p>
     * We create this object in startThreads() as each packetizer uses different handlers.
     * So long as the object is created before using it to sync it works.
     *
     */
    protected Runnable xmtHandler = null;

    /**
     * RcvHandler (a local class) object to implement the receive thread
     */
    protected Runnable rcvHandler;

    /**
     * Forward a preformatted LocoNetMessage to the actual interface.
     * <p>
     * Checksum is computed and overwritten here, then the message is converted
     * to a byte array and queued for transmission.
     *
     * @param m Message to send; will be updated with CRC
     */
    @Override
    public void sendLocoNetMessage(LocoNetMessage m) {

        // update statistics
        transmittedMsgCount++;

        // set the error correcting code byte(s) before transmittal
        m.setParity();

        // stream to port in single write, as that's needed by serial
        int len = m.getNumDataElements();
        byte msg[] = new byte[len];
        for (int i = 0; i < len; i++) {
            msg[i] = (byte) m.getElement(i);
        }

        log.debug("queue LocoNet packet: {}", m);
        // We need to queue the request and wake the xmit thread in an atomic operation
        // But the thread might not be running, in which case the request is just 
        // queued up.
        try {
            synchronized (xmtHandler) {
                xmtList.addLast(msg);
                xmtHandler.notifyAll(); 
            }
        } catch (RuntimeException e) {
            log.warn("passing to xmit: unexpected exception: ", e);
        }
    }

    /**
     * Implement abstract method to signal if there's a backlog of information
     * waiting to be sent.
     *
     * @return true if busy, false if nothing waiting to send
     */
    @Override
    public boolean isXmtBusy() {
        if (controller == null) {
            return false;
        }

        return (!controller.okToSend());
    }

    // methods to connect/disconnect to a source of data in a LnPortController

    protected LnPortController controller = null;

    /**
     * Make connection to an existing LnPortController object.
     *
     * @param p Port controller for connected. Save this for a later disconnect
     *          call
     */
    public void connectPort(LnPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null) {
            log.warn("connectPort: connect called while connected");
        }
        controller = p;
    }

    /**
     * Break connection to an existing LnPortController object. Once broken,
     * attempts to send via "message" member will fail.
     *
     * @param p previously connected port
     */
    public void disconnectPort(LnPortController p) {
        istream = null;
        ostream = null;
        if (controller != p) {
            log.warn("disconnectPort: disconnect called from non-connected LnPortController");
        }
        controller = null;
    }

    // data members to hold the streams. These are public so the inner classes defined here
    // can access them with a Java 1.1 compiler
    public DataInputStream istream = null;
    public OutputStream ostream = null;

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <p>
     * When a port is set to have a receive timeout (via the
     * enableReceiveTimeout() method), some will return zero bytes or an
     * EOFException at the end of the timeout. In that case, the read should be
     * repeated to get the next real character.
     *
     * @param istream stream to read from
     * @return buffer of received data
     * @throws java.io.IOException failure during stream read
     *
     */
    protected byte readByteProtected(DataInputStream istream) throws java.io.IOException {
        while (true) { // loop will repeat until character found
            int nchars;
            nchars = istream.read(rcvBuffer, 0, 1);
            if (nchars > 0) {
                return rcvBuffer[0];
            }
        }
    }
    // Defined this way to reduce new object creation
    private byte[] rcvBuffer = new byte[1];

    /**
     * Captive class to handle incoming characters. This is a permanent loop,
     * looking for input messages in character form on the stream connected to
     * the LnPortController via <code>connectPort</code>.
     */
    protected class RcvHandler implements Runnable {

        /**
         * Remember the LnPacketizer object
         */
        LnTrafficController trafficController;

        public RcvHandler(LnTrafficController lt) {
            trafficController = lt;
        }

        /**
         * Handle incoming characters. This is a permanent loop, looking for
         * input messages in character form on the stream connected to the
         * LnPortController via <code>connectPort</code>. Terminates with the
         * input stream breaking out of the try block.
         */
        @Override
        public void run() {

            int opCode;
            while (!threadStopRequest) {   // loop until asked to stop
                try {
                    // start by looking for command -  skip if bit not set
                    while (((opCode = (readByteProtected(istream) & 0xFF)) & 0x80) == 0) { // the real work is in the loop check
                        if (log.isTraceEnabled()) { // avoid building string 
                            log.trace("Skipping: {}", Integer.toHexString(opCode)); // NOI18N
                        }
                    }
                    // here opCode is OK. Create output message
                    if (log.isTraceEnabled()) { // avoid building string 
                        log.trace(" (RcvHandler) Start message with opcode: {}", Integer.toHexString(opCode)); // NOI18N
                    }
                    LocoNetMessage msg = null;
                    while (msg == null) {
                        try {
                            // Capture 2nd byte, always present
                            int byte2 = readByteProtected(istream) & 0xFF;
                            if (log.isTraceEnabled()) { // avoid building string 
                                log.trace("Byte2: {}", Integer.toHexString(byte2)); // NOI18N
                            }                            // Decide length
                            int len = 2;
                            switch ((opCode & 0x60) >> 5) {
                                case 0:
                                    /* 2 byte message */

                                    len = 2;
                                    break;

                                case 1:
                                    /* 4 byte message */

                                    len = 4;
                                    break;

                                case 2:
                                    /* 6 byte message */

                                    len = 6;
                                    break;

                                case 3:
                                    /* N byte message */

                                    if (byte2 < 2) {
                                        log.error("LocoNet message length invalid: " + byte2
                                                + " opcode: " + Integer.toHexString(opCode)); // NOI18N
                                    }
                                    len = byte2;
                                    break;
                                default:
                                    log.warn("Unhandled code: {}", (opCode & 0x60) >> 5);
                                    break;
                            }
                            msg = new LocoNetMessage(len);
                            // message exists, now fill it
                            msg.setOpCode(opCode);
                            msg.setElement(1, byte2);
                            log.trace("len: {}", len); // NOI18N
                            for (int i = 2; i < len; i++) {
                                // check for message-blocking error
                                int b = readByteProtected(istream) & 0xFF;
                                if (log.isTraceEnabled()) {
                                    log.trace("char {} is: {}", i, Integer.toHexString(b)); // NOI18N
                                }
                                if ((b & 0x80) != 0) {
                                    log.warn("LocoNet message with opCode: " // NOI18N
                                            + Integer.toHexString(opCode)
                                            + " ended early. Expected length: " + len // NOI18N
                                            + " seen length: " + i // NOI18N
                                            + " unexpected byte: " // NOI18N
                                            + Integer.toHexString(b)); // NOI18N
                                    opCode = b;
                                    throw new LocoNetMessageException();
                                }
                                msg.setElement(i, b);
                            }
                        } catch (LocoNetMessageException e) {
                            // retry by destroying the existing message
                            // opCode is set for the newly-started packet
                            msg = null;
                        }
                    }
                    // check parity
                    if (!msg.checkParity()) {
                        log.warn("Ignore LocoNet packet with bad checksum: {}", msg);
                        throw new LocoNetMessageException();
                    }
                    // message is complete, dispatch it !!
                    {
                        log.debug("queue message for notification: {}", msg);

                        jmri.util.ThreadingUtil.runOnLayoutEventually(new RcvMemo(msg, trafficController));
                    }

                    // done with this one
                } catch (LocoNetMessageException e) {
                    // just let it ride for now
                    log.warn("run: unexpected LocoNetMessageException: {}", e); // NOI18N
                } catch (java.io.EOFException e) {
                    // posted from idle port when enableReceiveTimeout used
                    log.trace("EOFException, is LocoNet serial I/O using timeouts?"); // NOI18N
                } catch (java.io.IOException e) {
                    // fired when write-end of HexFile reaches end
                    log.debug("IOException, should only happen with HexFIle: {}", e); // NOI18N
                    log.info("End of file"); // NOI18N
                    disconnectPort(controller);
                    return;
                } // normally, we don't catch RuntimeException, but in this
                  // permanently running loop it seems wise.
                catch (RuntimeException e) {
                    log.warn("run: unexpected Exception: {}", e); // NOI18N
                }
            } // end of permanent loop
        }
    }

    /**
     * Captive class to notify of one message.
     */
    private static class RcvMemo implements jmri.util.ThreadingUtil.ThreadAction {

        public RcvMemo(LocoNetMessage msg, LnTrafficController trafficController) {
            thisMsg = msg;
            thisTc = trafficController;
        }
        LocoNetMessage thisMsg;
        LnTrafficController thisTc;

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            thisTc.notify(thisMsg);
        }
    }

    /**
     * Captive class to handle transmission.
     */
    class XmtHandler implements Runnable {

        /**
         * Loops forever, looking for message to send and processing them.
         */
        @Override
        public void run() {

            while (!threadStopRequest) {   // loop until asked to stop
                // any input?
                try {
                    // get content; failure is a NoSuchElementException
                    log.trace("check for input"); // NOI18N
                    byte msg[] = null;
                    synchronized (this) {
                        msg = xmtList.removeFirst();
                    }

                    // input - now send
                    try {
                        if (ostream != null) {
                            if (log.isDebugEnabled()) { // avoid work if not needed
                                if (isXmtBusy()) log.debug("LocoNet port not ready to receive"); // NOI18N
                                log.debug("start write to stream: {}", jmri.util.StringUtil.hexStringFromBytes(msg)); // NOI18N
                            }
                            ostream.write(msg);
                            ostream.flush();
                            if (log.isTraceEnabled()) { // avoid String building if not needed
                                log.trace("end write to stream: {}", jmri.util.StringUtil.hexStringFromBytes(msg)); // NOI18N
                            }
                            messageTransmitted(msg);
                        } else {
                            // no stream connected
                            log.warn("sendLocoNetMessage: no connection established"); // NOI18N
                        }
                    } catch (java.io.IOException e) {
                        log.warn("sendLocoNetMessage: IOException: {}", e.toString()); // NOI18N
                    }
                } catch (NoSuchElementException e) {
                    // message queue was empty, wait for input
                    log.trace("start wait"); // NOI18N

                    new jmri.util.WaitHandler(this); // handle synchronization, spurious wake, interruption

                    log.trace("end wait"); // NOI18N
                }
            }
        }
    }

    /**
     * When a message is finally transmitted, forward it to listeners if echoing
     * is needed.
     *
     * @param msg message sent
     */
    protected void messageTransmitted(byte[] msg) {
        log.debug("message transmitted (echo {})", echo);
        if (!echo) {
            return;
        }
        // message is queued for transmit, echo it when needed
        // return a notification via the queue to ensure end
        javax.swing.SwingUtilities.invokeLater(new Echo(this, new LocoNetMessage(msg)));
    }

    static class Echo implements Runnable {

        Echo(LnPacketizer t, LocoNetMessage m) {
            myTc = t;
            msgForLater = m;
        }
        LocoNetMessage msgForLater;
        LnPacketizer myTc;

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            myTc.notify(msgForLater);
        }
    }

    /**
     * Invoked at startup to start the threads needed here.
     */
    public void startThreads() {
        int priority = Thread.currentThread().getPriority();
        log.debug("startThreads current priority = {} max available = {} default = {} min available = {}", // NOI18N
                priority, Thread.MAX_PRIORITY, Thread.NORM_PRIORITY, Thread.MIN_PRIORITY);

        // start the RcvHandler in a thread of its own
        if (rcvHandler == null) {
            rcvHandler = new RcvHandler(this);
        }
        rcvThread = new Thread(rcvHandler, "LocoNet receive handler"); // NOI18N
        rcvThread.setDaemon(true);
        rcvThread.setPriority(Thread.MAX_PRIORITY);
        rcvThread.start();

        if (xmtHandler == null) {
            xmtHandler = new XmtHandler();
        }
        // make sure that the xmt priority is no lower than the current priority
        int xmtpriority = (Thread.MAX_PRIORITY - 1 > priority ? Thread.MAX_PRIORITY - 1 : Thread.MAX_PRIORITY);
        // start the XmtHandler in a thread of its own
        xmtThread = new Thread(xmtHandler, "LocoNet transmit handler"); // NOI18N
        log.debug("Xmt thread starts at priority {}", xmtpriority); // NOI18N
        xmtThread.setDaemon(true);
        xmtThread.setPriority(Thread.MAX_PRIORITY - 1);
        xmtThread.start();

        log.info("lnPacketizer Started");
    }

    Thread rcvThread;
    Thread xmtThread;
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation") // stop() is deprecated, but it's not going away
    @Override
    public void dispose() {
        if (xmtThread != null) {
            xmtThread.stop(); // interrupt not sufficient?
            try {
                xmtThread.join();
            } catch (InterruptedException e) { log.warn("unexpected InterruptedException", e);}
        }
        if (rcvThread != null) {
            rcvThread.stop(); // interrupt not sufficient, because jtermios hangs in select via purejavacomm.PureJavaSerialPort$2.read
            try {
                rcvThread.join();
            } catch (InterruptedException e) { log.warn("unexpected InterruptedException", e);}
        }
        super.dispose();
    }

    /**
     * Terminate the receive and transmit threads.
     * <p>
     * This is intended to be used only by testing subclasses.
     */
    public void terminateThreads() {
        threadStopRequest = true;
        if (xmtThread != null) {
            xmtThread.interrupt();
            try {
                xmtThread.join();
            } catch (InterruptedException ie){
                // interrupted during cleanup.
            }
        }
        
        if (rcvThread != null) {
            rcvThread.interrupt();
            try {
                rcvThread.join();
            } catch (InterruptedException ie){
                // interrupted during cleanup.
            }
        }    
    }
    
    /**
     * Flag that threads should terminate as soon as they can.
     */
    protected volatile boolean threadStopRequest = false;
    
    private final static Logger log = LoggerFactory.getLogger(LnPacketizer.class);

}
