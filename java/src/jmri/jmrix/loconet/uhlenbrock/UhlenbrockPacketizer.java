package jmri.jmrix.loconet.uhlenbrock;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LocoNetInterface;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetMessageException;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
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
 *
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2010
 */
public class UhlenbrockPacketizer extends LnPacketizer {

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Only used during system initialization")
    public UhlenbrockPacketizer() {
        super(new LocoNetSystemConnectionMemo());
        log.debug("UhlenbrockPacketizer instantiated");
    }

    public static final int NOTIFIEDSTATE = 15;    // xmt notified, will next wake
    public static final int WAITMSGREPLYSTATE = 25;  // xmt has sent, await reply to message

    static int defaultWaitTimer = 10000;

    /**
     * Forward a preformatted LocoNetMessage to the actual interface.
     *
     * Checksum is computed and overwritten here, then the message is converted
     * to a byte array and queued for transmission.
     *
     * @param m Message to send; will be updated with CRC
     */
    @Override
    public void sendLocoNetMessage(LocoNetMessage m) {
        log.debug("add to queue message {}", m.toString());
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

        if (log.isDebugEnabled()) {
            log.debug("queue LocoNet packet: {}", m.toString());
        }
        // in an atomic operation, queue the request and wake the xmit thread
        try {
            synchronized (xmtHandler) {
                xmtLocoNetList.addLast(m);
                xmtList.addLast(msg);
                xmtHandler.notify();
            }
        } catch (RuntimeException e) {
            log.warn("passing to xmit: unexpected exception: ", e);
        }
    }

    /**
     * Synchronized list used as a transmit queue.
     * <p>
     * This is public to allow access from the internal class(es) when compiling
     * with Java 1.1
     */
    public LinkedList<LocoNetMessage> xmtLocoNetList = new LinkedList<LocoNetMessage>();

    /**
     * Captive class to handle incoming characters. This is a permanent loop,
     * looking for input messages in character form on the stream connected to
     * the LnPortController via <code>connectPort</code>.
     */
    class RcvHandler implements Runnable {

        /**
         * Remember the LnPacketizer object.
         */
        LnPacketizer trafficController;

        public RcvHandler(LnPacketizer lt) {
            trafficController = lt;
        }

        @Override
        public void run() {

            int opCode;
            while (true) {   // loop permanently, program close will exit
                try {
                    // start by looking for command -  skip if bit not set
                    int inbyte = readByteProtected(istream) & 0xFF;
                    while (((opCode = (inbyte)) & 0x80) == 0) {
                        log.debug("Skipping: {}", Integer.toHexString(opCode));
                        inbyte = readByteProtected(istream) & 0xFF;
                    }
                    // here opCode is OK. Create output message
                    log.debug("Start message with opcode: {}", Integer.toHexString(opCode));
                    LocoNetMessage msg = null;
                    while (msg == null) {
                        try {
                            // Capture 2nd byte, always present
                            int byte2 = readByteProtected(istream) & 0xFF;
                            //log.debug("Byte2: "+Integer.toHexString(byte2));
                            if ((byte2 & 0x80) != 0) {
                                log.warn("LocoNet message with opCode: "
                                        + Integer.toHexString(opCode)
                                        + " ended early. Byte2 is also an opcode: "
                                        + Integer.toHexString(byte2));
                                opCode = byte2;
                                throw new LocoNetMessageException();
                            }

                            // Decide length
                            switch ((opCode & 0x60) >> 5) {
                                case 0:
                                    /* 2 byte message */

                                    msg = new LocoNetMessage(2);
                                    break;

                                case 1:
                                    /* 4 byte message */

                                    msg = new LocoNetMessage(4);
                                    break;

                                case 2:
                                    /* 6 byte message */

                                    msg = new LocoNetMessage(6);
                                    break;

                                case 3:
                                    /* N byte message */

                                    if (byte2 < 2) {
                                        log.error("LocoNet message length invalid: " + byte2
                                                + " opcode: " + Integer.toHexString(opCode));
                                    }
                                    msg = new LocoNetMessage(byte2);
                                    break;
                                default: // can't happen with this code, but just in case...
                                    throw new LocoNetMessageException("decode failure " + byte2);
                            }
                            // message exists, now fill it
                            msg.setOpCode(opCode);
                            msg.setElement(1, byte2);
                            int len = msg.getNumDataElements();
                            //log.debug("len: "+len);
                            for (int i = 2; i < len; i++) {
                                // check for message-blocking error
                                int b = readByteProtected(istream) & 0xFF;
                                //log.debug("char "+i+" is: "+Integer.toHexString(b));
                                if ((b & 0x80) != 0) {
                                    log.warn("LocoNet message with opCode: "
                                            + Integer.toHexString(opCode)
                                            + " ended early. Expected length: " + len
                                            + " seen length: " + i
                                            + " unexpected byte: "
                                            + Integer.toHexString(b));
                                    opCode = b;
                                    throw new LocoNetMessageException();
                                }
                                msg.setElement(i, b);
                            }
                        } catch (LocoNetMessageException e) {
                            // retry by going around again
                            // opCode is set for the newly-started packet
                            msg = null;
                            continue;
                        }
                    }
                    // check parity
                    if (!msg.checkParity()) {
                        log.warn("Ignore LocoNet packet with bad checksum: " + msg.toString());
                        throw new LocoNetMessageException();
                    }

                    if (msg.equals(lastMessage)) {
                        log.debug("We have our returned message and can send back out our next instruction");
                        mCurrentState = NOTIFIEDSTATE;
                        synchronized (xmtHandler) {
                            xmtHandler.notify();
                        }
                    }

                    // message is complete, dispatch it !!
                    {
                        log.debug("queue message for notification");
//log.info("-------------------Uhlenbrock IB-COM LocoNet message RECEIVED: "+msg.toString());
                        final LocoNetMessage thisMsg = msg;
                        final LnPacketizer thisTc = trafficController;
                        // return a notification via the queue to ensure end
                        Runnable r = new Runnable() {
                            LocoNetMessage msgForLater = thisMsg;
                            LnPacketizer myTc = thisTc;

                            @Override
                            public void run() {
                                myTc.notify(msgForLater);
                            }
                        };
                        javax.swing.SwingUtilities.invokeLater(r);
                    }

                    // done with this one
                } catch (LocoNetMessageException e) {
                    // just let it ride for now
                    log.warn("run: unexpected LocoNetMessageException: ", e);
                } catch (java.io.EOFException e) {
                    // posted from idle port when enableReceiveTimeout used
                    log.debug("EOFException, is LocoNet serial I/O using timeouts?");
                } catch (java.io.IOException e) {
                    // fired when write-end of HexFile reaches end
                    log.debug("IOException, should only happen with HexFile: {}", e);
                    log.debug("End of file");
                    disconnectPort(controller);
                    return;
                } // normally, we don't catch the unnamed Exception, but in this
                // permanently running loop it seems wise.
                catch (RuntimeException e) {
                    log.warn("run: unexpected Exception", e);
                }
            } // end of permanent loop
        }
    }

    LocoNetMessage lastMessage;

    /**
     * Captive class to handle transmission
     */
    class XmtHandler implements Runnable {

        @Override
        public void run() {

            while (true) {   // loop permanently
                // any input?
                try {
                    // get content; failure is a NoSuchElementException
                    log.debug("check for input");
                    byte msg[] = null;
                    lastMessage = null;
                    synchronized (this) {
                        lastMessage = xmtLocoNetList.removeFirst();
                        msg = xmtList.removeFirst();
                    }
//log.info("-------------------Uhlenbrock IB-COM LocoNet message to SEND: "+msg.toString());

                    // input - now send
                    try {
                        if (ostream != null) {
                            if (!controller.okToSend()) {
                                log.debug("LocoNet port not ready to receive");
                            }
                            log.debug("start write to stream");
                            while (!controller.okToSend()) {
                                Thread.yield();
                            }
                            ostream.write(msg);
                            ostream.flush();
                            log.debug("end write to stream");
                            messageTransmitted(msg);
                            mCurrentState = WAITMSGREPLYSTATE;
                            transmitWait(defaultWaitTimer, WAITMSGREPLYSTATE);
                        } else {
                            // no stream connected
                            log.warn("sendLocoNetMessage: no connection established");
                        }
                    } catch (java.io.IOException e) {
                        log.warn("sendLocoNetMessage: IOException: {}", e.toString());
                    }
                } catch (NoSuchElementException e) {
                    // message queue was empty, wait for input
                    log.debug("start wait");

                    new jmri.util.WaitHandler(this);  // handle synchronization, spurious wake, interruption

                    log.debug("end wait");
                }
            }
        }
    }

    protected void transmitWait(int waitTime, int state/*, String InterruptMessage*/) {
        // wait() can have spurious wakeup!
        // so we protect by making sure the entire timeout time is used
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long endTime = currentTime + waitTime;
        while (endTime > (currentTime = Calendar.getInstance().getTimeInMillis())) {
            long wait = endTime - currentTime;
            try {
                synchronized (xmtHandler) {
                    // Do not wait if the current state has changed since we
                    // last set it.
                    if (mCurrentState != state) {
                        return;
                    }
                    xmtHandler.wait(wait); // rcvr normally ends this w state change
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                log.error("transmitLoop interrupted");
            }
        }
        log.debug("Timeout in transmitWait, mCurrentState: {}", mCurrentState);
    }

    volatile protected int mCurrentState;

    /**
     * Invoked at startup to start the threads needed here.
     */
    @Override
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
        Thread xmtThread = new Thread(xmtHandler, "LocoNet Uhlenbrock transmit handler");
        log.debug("Xmt thread starts at priority {}", xmtpriority);
        xmtThread.setDaemon(true);
        xmtThread.setPriority(Thread.MAX_PRIORITY - 1);
        xmtThread.start();

        // start the RcvHandler in a thread of its own
        if (rcvHandler == null) {
            rcvHandler = new RcvHandler(this);
        }
        Thread rcvThread = new Thread(rcvHandler, "LocoNet Uhlenbrock receive handler");
        rcvThread.setDaemon(true);
        rcvThread.setPriority(Thread.MAX_PRIORITY);
        rcvThread.start();

    }

    private final static Logger log = LoggerFactory.getLogger(UhlenbrockPacketizer.class);

}
