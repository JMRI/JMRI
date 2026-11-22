package jmri.jmrix.can.adapters.gridconnect;

import java.io.DataInputStream;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traffic controller for the GridConnect protocol.
 * <p>
 * GridConnect uses messages transmitted as an ASCII string of up to 24
 * characters of the form: :ShhhhNd0d1d2d3d4d5d6d7;
 * <p>
 * The S indicates a standard
 * CAN frame hhhh is the two byte header (11 useful bits) N or R indicates a
 * normal or remote frame d0 - d7 are the (up to) 8 data bytes
 *
 * @author Andrew Crosland Copyright (C) 2008
 */
public class GcTrafficController extends TrafficController {

    /**
     * Create new GridConnect Traffic Controller.
     */
    public GcTrafficController() {
        super();
        this.setSynchronizeRx(false);
    }

    /**
     * Least time between frames written to the port, in milliseconds; 0 for
     * none. Some gateways hold back a frame that arrives while they are still
     * handling the one before, until another frame arrives.
     */
    private volatile int frameGapMillis = 0;
    private long lastFrameSentNanos = 0; // used on the transmit thread only

    /**
     * Set the least time between frames written to the port.
     *
     * @param millis milliseconds, 0 (the default) for none
     */
    public void setFrameGapMillis(int millis) {
        frameGapMillis = Math.max(0, millis);
    }

    /**
     * Get the least time between frames written to the port.
     *
     * @return milliseconds, 0 for none
     */
    public int getFrameGapMillis() {
        return frameGapMillis;
    }

    /**
     * {@inheritDoc}
     * <p>
     * When a frame gap is set, waits as needed so that frames are written at
     * least {@link #getFrameGapMillis()} apart. Runs on the transmit thread.
     */
    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        int gap = frameGapMillis;
        if (gap > 0 && lastFrameSentNanos != 0) {
            long waitNanos = lastFrameSentNanos + gap * 1_000_000L - System.nanoTime();
            if (waitNanos > 0) {
                try {
                    Thread.sleep(waitNanos / 1_000_000L, (int) (waitNanos % 1_000_000L));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // keep it for the transmit loop
                }
            }
        }
        super.forwardToPort(m, reply);
        lastFrameSentNanos = System.nanoTime();
    }

    /**
     * Forward a CanMessage to all registered CanInterface listeners.
     * {@inheritDoc}
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((CanListener) client).message((CanMessage) m);
    }

    /**
     * Forward a CanReply to all registered CanInterface listeners.
     * {@inheritDoc}
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        if (r != null) {
            ((CanListener) client).reply((CanReply) r);
        } else {
            // null shouldn't happen, but we've seen it; try to track if down
            log.error("Found unexpected null message", new Exception("traceback") );
        }
    }

    // Current state
    public static final int NORMAL = 0;
    public static final int BOOTMODE = 1;

    /**
     * Get the GridConnect State.
     * @return NORMAL or BOOTMODE
     */
    public int getgcState() {
        return gcState;
    }

    /**
     * Set the GridConnect State.
     * @param state NORMAL or BOOTMODE
     */
    public void setgcState(int state) {
        gcState = state;
        log.debug("Setting gcState {}", state);
    }

    /**
     * Get if GcTC is in Boot Mode.
     * @return true if in Boot Mode, else False.
     */
    public boolean isBootMode() {
        return gcState == BOOTMODE;
    }

    /**
     * Forward a preformatted message to the actual interface.
     * {@inheritDoc}
     */
    @Override
    public void sendCanMessage(CanMessage m, CanListener reply) {
        log.debug("GcTrafficController sendCanMessage() {}", m);
        sendMessage(m, reply);
    }

    /**
     * Forward a preformatted reply to the actual interface.
     * {@inheritDoc}
     */
    @Override
    public void sendCanReply(CanReply r, CanListener reply) {
        log.debug("TrafficController sendCanReply() {}", r);
        notifyReply(r, reply);
    }

    /**
     * Does nothing.
     * {@inheritDoc}
     */
    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
    }

    /**
     * Determine how much many bytes the entire message will take,
     * including space for header and trailer.
     *
     * @param m The message to be sent
     * @return Number of bytes
     */
    @Override
    protected int lengthOfByteStream(AbstractMRMessage m) {
        return m.getNumDataElements();
    }

    /**
     * Get new message for hardware protocol.
     * @return New GridConnect Message.
     */
    @Override
    protected AbstractMRMessage newMessage() {
        log.debug("New GridConnectMessage created");
        return new GridConnectMessage();
    }

    /**
     * Make a CanReply from a GridConnect reply.
     * {@inheritDoc}
     */
    @Override
    public CanReply decodeFromHardware(AbstractMRReply m) {
        log.debug("Decoding from hardware");
        // no throwaway GridConnectReply is allocated for every incoming frame
        GridConnectReply gc;
        if (m instanceof GridConnectReply) {
            gc = (GridConnectReply) m;
        } else {
            log.error("{} cannot cast to a GridConnectReply",m);
            gc = new GridConnectReply();
        }
        return gc.createReply();
    }

    /**
     * Encode a CanMessage for the hardware.
     * {@inheritDoc}
     */
    @Override
    public AbstractMRMessage encodeForHardware(CanMessage m) {
        //log.debug("Encoding for hardware");
        return new GridConnectMessage(m);
    }

    /**
     * New reply from hardware.
     * {@inheritDoc}
     */
    @Override
    protected AbstractMRReply newReply() {
        log.debug("New GridConnectReply created");
        return new GridConnectReply();
    }

    /*
     * Normal CAN-RS replies will end with ";"
     * Bootloader will end with ETX with no preceding DLE
     */
    @Override
    protected boolean endOfMessage(AbstractMRReply r) {
        if (endNormalReply(r)) {
            return true;
        }
//        if (endBootReply(r)) return true;
        return false;
    }

    /**
     * Detect if the reply buffer ends with ";".
     * @param r Reply
     * @return true if contains end, else false.
     */
    boolean endNormalReply(AbstractMRReply r) {
        int num = r.getNumDataElements() - 1;
        //log.debug("endNormalReply checking "+(num+1)+" of "+(r.getNumDataElements()));
        if (r.getElement(num) == ';') {
            log.debug("End of normal message detected");
            return true;
        }
        return false;
    }

    /**
     * Get characters from the input source, and file a message.
     * <p>
     * Returns only when the message is complete.
     * <p>
     * This is over-ridden from AbstractMRTrafficController so we can add
     * suppression of the characters before ':'. We can't use
     * waitForStartOfReply() because that strips the 1st character also.
     * <p>
     * Handles timeouts on read by ignoring zero-length reads.
     *
     * @param msg     message to fill
     * @param istream character source.
     * @throws java.io.IOException when presented by the input source.
     */
    @Override
    protected void loadChars(AbstractMRReply msg, DataInputStream istream)
            throws java.io.IOException {
        int i;
        for (i = 0; i < msg.maxSize(); i++) {
            byte char1 = readByteProtected(istream);
            if (i == 0) {
                // skip until you find ':'
                while (char1 != ':') {
                    char1 = readByteProtected(istream);
                }
            } else if (char1 == ':') {
                // Start of a new frame before the end of the current one,
                // the end of the current frame ( ';' ) has been lost, eg. a serial overrun.
                // Discard the partial frame and restart with this ':' so that the
                // two frames are not merged into one corrupt frame.
                log.warn("Incomplete frame discarded: {}", msg);
                msg.flush();
                i = 0;
            }
            //if (log.isDebugEnabled()) log.debug("char: "+(char1&0xFF)+" i: "+i);
            // if there was a timeout, flush any char received and start over
            if (flushReceiveChars) {
                log.warn("timeout flushes receive buffer: {}", msg.toString());
                msg.flush();
                i = 0;  // restart
                flushReceiveChars = false;
            }
            if (canReceive()) {
                msg.setElement(i, char1);
                if (endOfMessage(msg)) {
                    break;
                }
            } else {
                i--; // flush char
                log.error("unsolicited character received: {}", Integer.toHexString(char1));
            }
        }
    }

    private int gcState;

    private static final Logger log = LoggerFactory.getLogger(GcTrafficController.class);
}
