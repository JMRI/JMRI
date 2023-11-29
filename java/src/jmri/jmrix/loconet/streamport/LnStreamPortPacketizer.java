package jmri.jmrix.loconet.streamport;

import jmri.jmrix.loconet.LnPacketizer;
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
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class LnStreamPortPacketizer extends LnPacketizer {

    public LnStreamPortPacketizer(LocoNetSystemConnectionMemo m) {
        super(m);
    }

    public LnStreamPortController streamController = null;

    @Override
    public boolean isXmtBusy() {
        if (streamController == null) {
            return false;
        }
        return true;
    }

    /**
     * Make connection to existing LnPortController object.
     *
     * @param p Port controller to connect to. Save this for a later disconnect
     *          call
     */
    public void connectPort(LnStreamPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null) {
            log.warn("connectPort: connect called while connected");
        }
        streamController = p;
    }

    /**
     * Break connection to existing LnPortController object. Once broken,
     * attempts to send via "message" member will fail.
     *
     * @param p previously connected port
     */
    public void disconnectPort(LnStreamPortController p) {
        istream = null;
        ostream = null;
        if (streamController != p) {
            log.warn("disconnectPort: disconnect called from non-connected LnStreamPortController");
        }
        streamController = null;
    }

    /**
     * Captive class to handle transmission
     */
    class XmtHandler implements Runnable {

        @Override
        public void run() {

            while (!threadStopRequest) {   // loop until asked to stop
                // any input?
                try {
                    // get content; blocks until present
                    log.trace("check for input"); // NOI18N

                    byte msg[] = xmtList.take();

                    // input - now send
                    try {
                        if (ostream != null) {
                            if (!streamController.okToSend()) {
                                log.debug("LocoNet port not ready to receive"); // NOI18N
                            }
                            if (log.isDebugEnabled()) { // avoid String building if not needed
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
                } catch (InterruptedException ie) {
                    return; // ending the thread
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
        log.debug("startThreads current priority = {} max available = " + Thread.MAX_PRIORITY + " default = " + Thread.NORM_PRIORITY + " min available = " + Thread.MIN_PRIORITY, priority); // NOI18N

        // make sure that the xmt priority is no lower than the current priority
        int xmtpriority = (Thread.MAX_PRIORITY - 1 > priority ? Thread.MAX_PRIORITY - 1 : Thread.MAX_PRIORITY);
        // start the XmtHandler in a thread of its own
        if (xmtHandler == null) {
            xmtHandler = new XmtHandler();
        }
        xmtThread = new Thread(xmtHandler, "LocoNet transmit handler"); // NOI18N
        log.debug("Xmt thread starts at priority {}", xmtpriority); // NOI18N
        xmtThread.setDaemon(true);
        xmtThread.setPriority(Thread.MAX_PRIORITY - 1);
        xmtThread.start();

        // start the RcvHandler in a thread of its own
        if (rcvHandler == null) {
            rcvHandler = new RcvHandler(this);
        }
        rcvThread = new Thread(rcvHandler, "LocoNet receive handler"); // NOI18N
        rcvThread.setDaemon(true);
        rcvThread.setPriority(Thread.MAX_PRIORITY);
        rcvThread.start();
    }

    private final static Logger log = LoggerFactory.getLogger(LnStreamPortPacketizer.class);

}
