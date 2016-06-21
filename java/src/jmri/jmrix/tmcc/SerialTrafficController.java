package jmri.jmrix.tmcc;

import java.io.DataInputStream;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from TMCC serial messages.
 * <P>
 * The "SerialInterface" side sends/receives message objects.
 * <P>
 * The connection to a SerialPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the necessary state in each
 * message.
 * <P>
 * Handles initialization, polling, output, and input for multiple Serial Nodes.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006
 * @author Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 */
public class SerialTrafficController extends AbstractMRTrafficController implements SerialInterface {

    public SerialTrafficController() {
        super();

        // entirely poll driven, so reduce interval
        mWaitBeforePoll = 25;  // default = 25

    }

    // The methods to implement the SerialInterface
    public synchronized void addSerialListener(SerialListener l) {
        this.addListener(l);
    }

    public synchronized void removeSerialListener(SerialListener l) {
        this.removeListener(l);
    }

    protected AbstractMRMessage enterProgMode() {
        log.warn("enterProgMode doesnt make sense for TMCC serial");
        return null;
    }

    protected AbstractMRMessage enterNormalMode() {
        return null;
    }

    /**
     * Forward a SerialMessage to all registered SerialInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((SerialListener) client).message((SerialMessage) m);
    }

    /**
     * Forward a SerialReply to all registered SerialInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((SerialListener) client).reply((SerialReply) m);
    }

    /**
     * Handles initialization, output and polling for TMCC from within the
     * running thread
     */
    protected synchronized AbstractMRMessage pollMessage() {
        // no polling in this protocol
        return null;
    }

    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendSerialMessage(SerialMessage m, SerialListener reply) {
        sendMessage(m, reply);
    }

    /**
     * static function returning the SerialTrafficController instance to use.
     *
     * @return The registered SerialTrafficController instance for general use,
     *         if need be creating one.
     */
    static public SerialTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) {
                log.debug("creating a new SerialTrafficController object");
            }
            self = new SerialTrafficController();
        }
        return self;
    }

    static volatile protected SerialTrafficController self = null;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "temporary until mult-system; only set at startup")
    protected void setInstance() {
        self = this;
    }

    protected AbstractMRReply newReply() {
        return new SerialReply();
    }

    protected boolean endOfMessage(AbstractMRReply msg) {
        // our version of loadChars doesn't invoke this, so it shouldn't be called
        log.error("Not using endOfMessage, should not be called");
        return false;
    }

    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        byte char1;
        char1 = readByteProtected(istream);
        msg.setElement(0, char1 & 0xFE);
        if ((char1 & 0xFF) != 0xFE) {
            log.warn("return short message as 1st byte is " + (char1 & 0xFF));
            return;
        }

        char1 = readByteProtected(istream);
        msg.setElement(1, char1 & 0xFF);

        char1 = readByteProtected(istream);
        msg.setElement(2, char1 & 0xFF);
    }

    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
    }

    /**
     * No header needed
     *
     * @param msg The output byte stream
     * @return next location in the stream to fill
     */
    protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
        return 0;
    }

    /**
     * Add trailer to the outgoing byte stream.
     *
     * @param msg    The output byte stream
     * @param offset the first byte not yet used
     */
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
    }

    /**
     * Determine how much many bytes the entire message will take, including
     * space for header and trailer
     *
     * @param m The message to be sent
     * @return Number of bytes for msg (3) plus preceeding NOP (3)
     */
    protected int lengthOfByteStream(AbstractMRMessage m) {
        return 6;
    }

    /**
     * Actually transmits the next message to the port
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used in debug log, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (log.isDebugEnabled()) {
            log.debug("forwardToPort message: [" + m + "]");
        }
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
                    String f = "write message: ";
                    for (int i = 0; i < msg.length; i++) {
                        f = f + Integer.toHexString(0xFF & msg[i]) + " ";
                    }
                    log.debug(f);
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
                                log.error("char send wait interupted");
                            }
                        }
                        break;
                    } else if (m.getRetries() >= 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("Retry message: " + m.toString() + " attempts remaining: " + m.getRetries());
                        }
                        m.setRetries(m.getRetries() - 1);
                        try {
                            synchronized (xmtRunnable) {
                                xmtRunnable.wait(m.getTimeout());
                            }
                        } catch (InterruptedException e) {
                            log.error("retry wait interupted");
                        }
                    } else {
                        log.warn("sendMessage: port not ready for data sending: " + java.util.Arrays.toString(msg));
                    }
                }
            } else {
                // no stream connected
                log.warn("sendMessage: no connection established");
            }
        } catch (Exception e) {
            log.warn("sendMessage: Exception: " + e.toString());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficController.class.getName());
}
