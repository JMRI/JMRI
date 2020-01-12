package jmri.jmrix.loconet;

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
 * <li> RcvHandler - at highest available priority
 * <li> XmtHandler - down one, which is assumed to be above the GUI
 * <li> (everything else)
 * </ul>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2018
 */
public class LnPacketizerStrict extends LnPacketizer {

    // waiting for this echo
    private LocoNetMessage waitForMsg;
    // waiting on LACK
    private boolean waitingOnLack;
    // wait this, CS gone busy
    private int waitBusy;
    // retry required, lost echo, bad IMM, general busy
    private boolean reTryRequired;

    public LnPacketizerStrict(LocoNetSystemConnectionMemo m) {
        super(m);
    }

    /**
     * Captive class to handle incoming characters. This is a permanent loop,
     * looking for input messages in character form on the stream connected to
     * the LnPortController via <code>connectPort</code>.
     */
    protected class RcvHandlerStrict implements Runnable {

        /**
         * Remember the LnPacketizer object.
         */
        LnTrafficController trafficController;

        public RcvHandlerStrict(LnTrafficController lt) {
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
            while (true) {  // loop permanently, program close will exit
                try {
                    // start by looking for command -  skip if bit not set
                    while (((opCode = (readByteProtected(istream) & 0xFF)) & 0x80) == 0) {
                        log.trace("Skipping: {}", Integer.toHexString(opCode)); // NOI18N
                    }
                    // here opCode is OK. Create output message
                    if (log.isTraceEnabled()) {
                        log.trace(" (RcvHandler) Start message with opcode: {}", Integer.toHexString(opCode)); // NOI18N
                    }
                    LocoNetMessage msg = null;
                    while (msg == null) {
                        try {
                            // Capture 2nd byte, always present
                            int byte2 = readByteProtected(istream) & 0xFF;
                            if (log.isTraceEnabled()) {
                                log.trace("Byte2: {}", Integer.toHexString(byte2)); // NOI18N
                            }   // Decide length
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
                                        log.error("LocoNet message length invalid: {} opcode: {}",
                                                byte2, Integer.toHexString(opCode)); // NOI18N
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
                        log.warn("Ignore LocoNet packet with bad checksum: [{}]", msg.toString());  // NOI18N
                        throw new LocoNetMessageException();
                    }
                    // message is complete, dispatch it !!
                    {
                        if (log.isDebugEnabled()) { // avoid String building if not needed
                            log.debug("queue message for notification: {}", msg.toString());  // NOI18N
                        }
                        // check for XmtHandler waiting on return values
                        if (waitForMsg != null) {
                            if (waitForMsg.equals(msg)) {
                                waitForMsg = null;
                            }
                        }
                        if (waitingOnLack) {
                            if (msg.getOpCode() == LnConstants.OPC_LONG_ACK) {
                                waitingOnLack = false;
                                // check bad IMM
                                if ((msg.getElement(1) & 0xff) == 0x6d && (msg.getElement(2) & 0xff) == 0) {
                                    reTryRequired = true;
                                    waitBusy = 100;
                                    log.warn("IMM Back off");  // NOI18N
                                } else {
                                    reTryRequired = false;
                                }
                            } else if (msg.getOpCode() == LnConstants.OPC_SL_RD_DATA) {
                                waitingOnLack = false;
                            } else if ( msg.getOpCode() == LnConstants.OPC_ALM_READ ) { // Extended slot status
                                waitingOnLack = false;
                            }
                            // check for CS busy
                        } else if (msg.getOpCode() == LnConstants.OPC_GPBUSY) {
                            waitBusy = 100;
                            log.warn("CS Busy Back off");  // NOI18N
                            reTryRequired = true;
                            // check for waiting on echo
                        }
                        jmri.util.ThreadingUtil.runOnLayoutEventually(new RcvMemo(msg, trafficController));
                    }
                    // done with this one
                } catch (LocoNetMessageException e) {
                    // just let it ride for now
                    log.warn("run: unexpected LocoNetMessageException: " + e); // NOI18N
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
                    log.warn("run: unexpected Exception: " + e); // NOI18N
                }
            } // end of permanent loop
        }
    }

    /**
     * Captive class to notify of one message
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
     * Captive class to handle transmission
     */
    class XmtHandlerStrict implements Runnable {

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            int waitCount;
            while (true) { // loop permanently
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
                            if (!controller.okToSend()) {
                                log.debug("LocoNet port not ready to receive"); // NOI18N
                            }
                            if (log.isDebugEnabled()) { // avoid String building if not needed
                                log.debug("start write to stream: {}", jmri.util.StringUtil.hexStringFromBytes(msg)); // NOI18N
                            }
                            // get it started
                            reTryRequired = true;
                            int reTryCount = 0;
                            while (reTryRequired) {
                                // assert its going to work
                                reTryRequired = false;
                                waitForMsg = new LocoNetMessage(msg);
                                if ((msg[0] & 0x08) != 0) {
                                    waitingOnLack = true;
                                }
                                while (waitBusy != 0) {
                                    // we do it this way as during our sleep the waitBusy time can be reset
                                    int waitTime = waitBusy;
                                    waitBusy = 0;
                                    //if (log.isDebugEnabled()) {
                                    //    log.debug("waitBusy");
                                    //}
                                    // for now so we know how prevalent this is over a long time span
                                    log.warn("Waitbusy");
                                    try {
                                        Thread.sleep(waitTime);
                                    } catch (InterruptedException ee) {
                                        log.warn("waitBusy sleep Interrupted", ee); // NOI18N
                                    }
                                }
                                ostream.write(msg);
                                ostream.flush();
                                if (log.isTraceEnabled()) {
                                    log.trace("end write to stream: {}", jmri.util.StringUtil.hexStringFromBytes(msg)); // NOI18N
                                }
                                // loop waiting for echo message and or LACK
                                // minimal sleeps so as to exit fast
                                waitCount = 0;
                                // echo as really fast
                                while ((waitForMsg != null) && waitCount < 20) {
                                    try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException ee) {
                                        log.error("waitForMsg sleep Interrupted", ee); // NOI18N
                                    }
                                    waitCount++;
                                }
                                // Oh my lost the echo...
                                if (waitCount > 19) {
                                    log.warn("Retry Send for Lost Packet [{}] Count[{}]", waitForMsg,
                                                reTryCount); // NOI18N
                                    if (reTryCount < 5) {
                                        reTryRequired = true;
                                        reTryCount++;
                                    } else {
                                        reTryRequired = false;
                                        reTryCount = 0;
                                        log.warn("Give up on lost packet");
                                    }
                                } else {
                                    // LACKs / a response can be slow
                                    while (waitingOnLack && waitCount < 50) {
                                        try {
                                            Thread.sleep(1);
                                        } catch (InterruptedException ee) {
                                            log.error("waitingOnLack sleep Interrupted", ee); // NOI18N
                                        }
                                        waitCount++;
                                    }
                                    // Oh my lost the LACK / response...
                                    if (waitCount > 49) {
                                        try {
                                            log.warn("Retry Send for Lost Response Count[{}]", reTryCount); // NOI18N
                                        } catch (NullPointerException npe) {
                                            log.warn("Retry Send for waitingOnLack null?  Count[{}]", reTryCount); // NOI18N
                                        }
                                        if (reTryCount < 5) {
                                            reTryRequired = true;
                                            reTryCount++;
                                        } else {
                                            log.warn("Give up on Lost Response."); // NOI18N
                                            reTryRequired = false;
                                            reTryCount = 0;
                                        }
                                    }
                                }
                            }
                            messageTransmitted(msg);
                        } else {
                            // no stream connected
                            log.warn("sendLocoNetMessage: no connection established"); // NOI18N
                        }
                    } catch (java.io.IOException e) {
                        log.warn("sendLocoNetMessage: IOException: " + e.toString()); // NOI18N
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
     * Invoked at startup to start the threads needed here.
     */
    @Override
    public void startThreads() {
        int priority = Thread.currentThread().getPriority();
        log.debug("startThreads current priority = {} max available = {} default = {} min available = {}", // NOI18N
                priority, Thread.MAX_PRIORITY, Thread.NORM_PRIORITY, Thread.MIN_PRIORITY);

        // make sure that the xmt priority is no lower than the current priority
        int xmtpriority = (Thread.MAX_PRIORITY - 1 > priority ? Thread.MAX_PRIORITY - 1 : Thread.MAX_PRIORITY);
        // start the XmtHandler in a thread of its own
        if (xmtHandler == null) {
            xmtHandler = new XmtHandlerStrict();
        }
        xmtThread = new Thread(xmtHandler, "LocoNet transmit handler"); // NOI18N
        log.debug("Xmt thread starts at priority {}", xmtpriority); // NOI18N
        xmtThread.setDaemon(true);
        xmtThread.setPriority(Thread.MAX_PRIORITY - 1);
        xmtThread.start();

        // start the RcvHandler in a thread of its own
        if (rcvHandler == null) {
            rcvHandler = new RcvHandlerStrict(this);
        }
        rcvThread = new Thread(rcvHandler, "LocoNet receive handler"); // NOI18N
        rcvThread.setDaemon(true);
        rcvThread.setPriority(Thread.MAX_PRIORITY);
        rcvThread.start();

        log.info("Strict Packetizer in use");

    }

    private final static Logger log = LoggerFactory.getLogger(LnPacketizerStrict.class);

}
