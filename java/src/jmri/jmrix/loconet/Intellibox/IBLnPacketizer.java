package jmri.jmrix.loconet.Intellibox;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.NoSuchElementException;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetMessageException;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

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
 * @author Bob Jacobsen Copyright (C) 2001, 2010
 */
public class IBLnPacketizer extends LnPacketizer {

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Only used during system initialization")
    public IBLnPacketizer() {
        super(new LocoNetSystemConnectionMemo());
        echo = true;
    }

    /**
     * Captive class to handle incoming characters. This is a permanent loop,
     * looking for input messages in character form on the stream connected to
     * the LnPortController via <code>connectPort</code>.
     */
    class RcvHandler implements Runnable {

        /**
         * Remember the LnPacketizer object
         */
        LnPacketizer trafficController;

        @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                justification = "single threaded during init; will eventually be replaced for multi-connection support")
        public RcvHandler(LnPacketizer lt) {
            trafficController = lt;
        }

        private byte readNextByteFromUSB() {
            byte inbyte;
            while (true) {
                try {
                    inbyte = istream.readByte();
                    return inbyte;
                } catch (java.io.IOException e) {
                    continue;
                }
            }
        }

        @Override
        public void run() {

            int opCode;
            while (true) {   // loop permanently, program close will exit
                try {
                    // start by looking for command -  skip if bit not set
                    while (((opCode = (readNextByteFromUSB() & 0xFF)) & 0x80) == 0) {
                        if (log.isDebugEnabled()) { // Avoid building unneeded Strings
                            log.debug("Skipping: {}", Integer.toHexString(opCode));
                        }
                    }
                    // here opCode is OK. Create output message
                    if (log.isDebugEnabled()) { // Avoid building unneeded Strings
                        log.debug("Start message with opcode: {}", Integer.toHexString(opCode));
                    }
                    LocoNetMessage msg = null;
                    while (msg == null) {
                        try {
                            // Capture 2nd byte, always present
                            int byte2 = readNextByteFromUSB() & 0xFF;
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
                                int b = readNextByteFromUSB() & 0xFF;
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
                    // message is complete, dispatch it !!
                    {
                        if (log.isDebugEnabled()) {
                            log.debug("queue message for notification");
                        }
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
                    log.warn("run: unexpected LocoNetMessageException: " + e);
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

        @Override
        public void run() {

            while (true) {   // loop permanently
                // any input?
                try {
                    // get content; failure is a NoSuchElementException
                    log.debug("check for input");
                    byte msg[] = null;
                    synchronized (this) {
                        msg = xmtList.removeFirst();
                    }

                    // input - now send
                    try {
                        if (ostream != null) {
                            if (!controller.okToSend()) {
                                log.debug("LocoNet port not ready to receive");
                            }
                            log.debug("start write to stream");

                            // The Intellibox cannot handle messges over 4 bytes without
                            // stopping the sender via CTS/RTS hardware handshake
                            // While this should work already by using the normal hardware
                            // handshake - it doesn't seem to so we need to check/send/flush
                            // each byte to make sure we don't overflow the IB input buffer
                            for (int i = 0; i < msg.length; i++) {
                                while (!controller.okToSend()) {
                                    Thread.yield();
                                }

                                ostream.write(msg[i]);
                                ostream.flush();
                            }

                            log.debug("end write to stream");
                            messageTransmitted(msg);
                        } else {
                            // no stream connected
                            log.warn("sendLocoNetMessage: no connection established");
                        }
                    } catch (java.io.IOException e) {
                        log.warn("sendLocoNetMessage: IOException: " + e.toString());
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
        Thread xmtThread = new Thread(xmtHandler, "LocoNet Intellibox transmit handler");
        log.debug("Xmt thread starts at priority " + xmtpriority);
        xmtThread.setDaemon(true);
        xmtThread.setPriority(Thread.MAX_PRIORITY - 1);
        xmtThread.start();

        // start the RcvHandler in a thread of its own
        if (rcvHandler == null) {
            rcvHandler = new RcvHandler(this);
        }
        Thread rcvThread = new Thread(rcvHandler, "LocoNet Intellibox receive handler");
        rcvThread.setDaemon(true);
        rcvThread.setPriority(Thread.MAX_PRIORITY);
        rcvThread.start();

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IBLnPacketizer.class);
}
