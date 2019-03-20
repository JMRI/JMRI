package jmri.jmrix.tmcc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInputStream;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert Stream-based I/O to/from TMCC serial messages.
 * <p>
 * The "SerialInterface" side sends/receives message objects.
 * <p>
 * The connection to a SerialPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <p>
 * This handles the state transitions, based on the necessary state in each
 * message.
 * <p>
 * Handles initialization, polling, output, and input for multiple Serial Nodes.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006
 * @author Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 */
public class SerialTrafficController extends AbstractMRTrafficController implements SerialInterface {

    /**
     * Create a new TMCC SerialTrafficController instance.
     *
     * @param adaptermemo the associated SystemConnectionMemo
     */
    public SerialTrafficController(TmccSystemConnectionMemo adaptermemo) {
        super();
        mMemo = adaptermemo;
        // entirely poll driven, so reduce interval
        mWaitBeforePoll = 25; // default = 25
        log.debug("creating a new TMCCTrafficController object");
    }

    // The methods to implement the SerialInterface

    @Override
    public synchronized void addSerialListener(SerialListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeSerialListener(SerialListener l) {
        this.removeListener(l);
    }

    @Override
    protected AbstractMRMessage enterProgMode() {
        log.warn("enterProgMode doesn't make sense for TMCC serial");
        return null;
    }

    @Override
    protected AbstractMRMessage enterNormalMode() {
        return null;
    }

    /**
     * Reference to the system connection memo.
     */
    TmccSystemConnectionMemo mMemo = null;

    /**
     * Get access to the system connection memo associated with this traffic
     * controller.
     *
     * @return associated systemConnectionMemo object
     */
    public TmccSystemConnectionMemo getSystemConnectionMemo() {
        return mMemo;
    }

    /**
     * Set the system connection memo associated with this traffic controller.
     *
     * @param m associated systemConnectionMemo object
     */
    public void setSystemConnectionMemo(TmccSystemConnectionMemo m) {
        mMemo = m;
    }

    /**
     * Static function returning the SerialTrafficController instance to use.
     *
     * @return The registered SerialTrafficController instance for general use,
     *         if need be creating one.
     * @deprecated JMRI Since 4.4 instance() shouldn't be used
     */
    @Deprecated
    static public SerialTrafficController instance() {
        log.warn("deprecated instance() call for TMCCTrafficController");
        return null;
    }

    /**
     * Forward a SerialMessage to all registered SerialInterface listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((SerialListener) client).message((SerialMessage) m);
    }

    /**
     * Forward a SerialReply to all registered SerialInterface listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((SerialListener) client).reply((SerialReply) m);
    }

    /**
     * Handle initialization, output and polling for TMCC from within the
     * running thread.
     */
    @Override
    protected synchronized AbstractMRMessage pollMessage() {
        // no polling in this protocol
        return null;
    }

    @Override
    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    @Override
    public void sendSerialMessage(SerialMessage m, SerialListener reply) {
        sendMessage(m, reply);
    }

    @Override
    protected AbstractMRReply newReply() {
        return new SerialReply();
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        // our version of loadChars doesn't invoke this, so it shouldn't be called
        log.error("Not using endOfMessage, should not be called");
        return false;
    }

    @Override
    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        byte char1;
        char1 = readByteProtected(istream);
        msg.setElement(0, char1 & 0xFE);
        if ((char1 & 0xFF) != 0xFE) {
            log.warn("return short message as 1st byte is {}", char1 & 0xFF);
            return;
        }

        char1 = readByteProtected(istream);
        msg.setElement(1, char1 & 0xFF);

        char1 = readByteProtected(istream);
        msg.setElement(2, char1 & 0xFF);
    }

    @Override
    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
    }

    /**
     * No header needed
     *
     * @param msg The output byte stream
     * @return next location in the stream to fill
     */
    @Override
    protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
        return 0;
    }

    /**
     * Add trailer to the outgoing byte stream.
     *
     * @param msg    The output byte stream
     * @param offset the first byte not yet used
     */
    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
    }

    /**
     * Determine how much many bytes the entire message will take, including
     * space for header and trailer
     *
     * @param m The message to be sent
     * @return Number of bytes for msg (3) plus preceeding NOP (3)
     */
    @Override
    protected int lengthOfByteStream(AbstractMRMessage m) {
        return 6;
    }

    /**
     * Actually transmits the next message to the port
     */
    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        log.debug("forwardToPort message: [{}]", m);
        // remember who sent this
        mLastSender = reply;

        // forward the message to the registered recipients,
        // which includes the communications monitor, except the sender.
        // Schedule notification via the Swing event queue to ensure order
        Runnable r = new XmtNotifier(m, mLastSender, this);
        javax.swing.SwingUtilities.invokeLater(r);

        // stream to port in single write, as that's needed by serial
        byte msg[] = new byte[lengthOfByteStream(m)];
        // add header
        int offset = addHeaderToOutput(msg, m);

        // add data content
        int len = m.getNumDataElements();
        for (int i = 0; i < len; i++) {
            msg[i + offset] = (byte) m.getElement(i);
        }

        // add trailer
        addTrailerToOutput(msg, len + offset, m);

        // and stream the bytes
        try {
            if (ostream != null) {
                if (log.isDebugEnabled()) {
                    StringBuilder f = new StringBuilder("");
                    for (int i = 0; i < msg.length; i++) {
                        f.append(Integer.toHexString(0xFF & msg[i])).append(" ");
                    }
                    log.debug("write message: {}", f);
                }
                while (m.getRetries() >= 0) {
                    if (portReadyToSend(controller)) {
                        for (int i = 0; i < len; i++) {
                            ostream.write(msg[i]);
                            try {
                                synchronized (xmtRunnable) {
                                    xmtRunnable.wait(10);
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt(); // retain if needed later
                                log.error("char send wait interrupted");
                            }
                        }
                        break;
                    } else if (m.getRetries() >= 0) {
                        log.debug("Retry message: {} attempts remaining: {}", m, m.getRetries());
                        m.setRetries(m.getRetries() - 1);
                        try {
                            synchronized (xmtRunnable) {
                                xmtRunnable.wait(m.getTimeout());
                            }
                        } catch (InterruptedException e) {
                            log.error("retry wait interrupted");
                        }
                    } else {
                        log.warn("sendMessage: port not ready for data sending: {}", java.util.Arrays.toString(msg));
                    }
                }
            } else {
                // no stream connected
                log.warn("sendMessage: no connection established");
            }
        } catch (java.io.IOException | RuntimeException e) {
            log.warn("sendMessage: Exception:", e);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficController.class);

}
