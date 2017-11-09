package jmri.jmrix.grapevine;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInputStream;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRNodeTrafficController;
import jmri.jmrix.AbstractMRReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from Grapevine serial messages.
 * <P>
 * The "SerialInterface" side sends/receives message objects.
 * <P>
 * The connection to a SerialPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <P>
 * This handles the state transitions, based on the necessary state in each
 * message.
 * <P>
 * Handles initialization, polling, output, and input for multiple Serial Nodes.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2008
 * @author Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 */
public class SerialTrafficController extends AbstractMRNodeTrafficController implements SerialInterface {

    public SerialTrafficController() {
        super();

        logDebug = log.isDebugEnabled();

        // set node range
        init(0, 255);

        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 100;  // default = 25
    }

    // have several debug statements in tight loops, e.g. every character;
    // only want to check once
    boolean logDebug = false;

    // The methods to implement the SerialInterface
    @Override
    public synchronized void addSerialListener(SerialListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeSerialListener(SerialListener l) {
        this.removeListener(l);
    }

    /**
     * Public method to set up for initialization of a Serial node
     */
    public void initializeSerialNode(SerialNode node) {
        synchronized (this) {
            // find the node in the registered node list
            for (int i = 0; i < getNumNodes(); i++) {
                if (getNode(i) == node) {
                    // found node - set up for initialization
                    setMustInit(i, true);
                    return;
                }
            }
        }
    }

    @Override
    protected AbstractMRMessage enterProgMode() {
        log.warn("enterProgMode doesn't make sense for grapevine serial");
        return null;
    }

    @Override
    protected AbstractMRMessage enterNormalMode() {
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

    SerialSensorManager mSensorManager = null;

    public void setSensorManager(SerialSensorManager m) {
        mSensorManager = m;
        // also register this to be notified
        addSerialListener(m);
    }

    /**
     * Handles initialization, output and polling for Grapevine from within the
     * running thread
     */
    @Override
    protected synchronized AbstractMRMessage pollMessage() {
        // ensure validity of call
        if (getNumNodes() <= 0) {
            return null;
        }

        // move to a new node
        curSerialNodeIndex++;
        if (curSerialNodeIndex >= getNumNodes()) {
            curSerialNodeIndex = 0;
        }
        // ensure that each node is initialized        
        if (getMustInit(curSerialNodeIndex)) {
            setMustInit(curSerialNodeIndex, false);
            SerialMessage m = (SerialMessage) (getNode(curSerialNodeIndex).createInitPacket());
            if (m != null) {
                log.debug("send init message: " + m);
                m.setTimeout(50);  // wait for init to finish (milliseconds)
                return m;
            }   // else fall through to continue
        }
        /*         // send Output packet if needed */
        /*         if (nodeArray[curSerialNodeIndex].mustSend()) { */
        /*             log.debug("request write command to send"); */
        /*             SerialMessage m = nodeArray[curSerialNodeIndex].createOutPacket(); */
        /*             nodeArray[curSerialNodeIndex].resetMustSend(); */
        /*             m.setTimeout(500); */
        /*             return m; */
        /*         } */
        /*         // poll for Sensor input */
        /*         if ( nodeArray[curSerialNodeIndex].getSensorsActive() ) { */
        /*             // Some sensors are active for this node, issue poll */
        /*             SerialMessage m = SerialMessage.getPoll( */
        /*                                 nodeArray[curSerialNodeIndex].getNodeAddress()); */
        /*             if (curSerialNodeIndex>=numNodes) curSerialNodeIndex = 0; */
        /*             return m; */
        /*         } */
        /*         else { */
        /*             // no Sensors (inputs) are active for this node */
        /*             return null; */
        /*         } */
        return null;
    }

    @Override
    protected synchronized void handleTimeout(AbstractMRMessage m, AbstractMRListener l) {
        // inform node, and if it resets then reinitialize 
        if (getNode(curSerialNodeIndex) != null) {
            if (getNode(curSerialNodeIndex).handleTimeout(m, l)) {
                setMustInit(curSerialNodeIndex, true);
            } else {
                log.warn("Timeout can't be handled due to missing node index=" + curSerialNodeIndex);
            }
        }
    }

    @Override
    protected synchronized void resetTimeout(AbstractMRMessage m) {
        // inform node
        getNode(curSerialNodeIndex).resetTimeout(m);

    }

    @Override
    protected AbstractMRListener pollReplyHandler() {
        return mSensorManager;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    @Override
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

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "temporary until mult-system; only set at startup")
    @Override
    @Deprecated
    protected void setInstance() {
        self = this;
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

    protected int currentAddr = -1; // at startup, can't match

    int nextReplyLen = 4;

    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        nextReplyLen = ((SerialMessage) m).getReplyLen();
        super.forwardToPort(m, reply);
    }

    byte[] buffer = new byte[4];
    int state = 0;

    @Override
    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        while (doNextStep(msg, istream)) {
        }
    }

    /**
     * Execute a state machine to parse messages from the input characters. May
     * consume one or more than one character. Returns true when the message has
     * been completely loaded.
     */
    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    boolean doNextStep(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        switch (state) {
            case 0:
                // get 1st char, check for address bit
                buffer[0] = readByteProtected(istream);
                if (logDebug) {
                    log.debug("state 0, rcv " + (buffer[0] & 0xFF));
                }
                if ((buffer[0] & 0x80) == 0) {
                    log.warn("1st byte not address: " + (buffer[0] & 0xFF));
                    return true;  // try again with next
                }
                state = 1;
            // and continue anyway
            case 1:
                buffer[1] = readByteProtected(istream);
                if (logDebug) {
                    log.debug("state 1, rcv " + (buffer[1] & 0xFF));
                }
                if ((buffer[1] & 0x80) != 0) {
                    buffer[0] = buffer[1];
                    state = 1; // use this as address and try again
                    log.warn("2nd byte HOB set: " + (buffer[1] & 0xFF) + ", going to state 1");
                    return true;
                }
                state = 2;
            // fall through
            case 2:
                // as a special case, see what happens if a short
                // message is expected
                if (nextReplyLen == 2) {
                    // we'll accept these two bytes as a reply
                    buffer[2] = 0;
                    buffer[3] = 0;
                    loadBuffer(msg);
                    ((SerialReply) msg).setNumDataElements(2); // flag short reply
                    nextReplyLen = 4; // only happens once
                    state = 0;
                    if (logDebug) {
                        log.debug("Short message complete: " + msg.toString());
                    }
                    return false;  // have received a message
                }
                // here for normal four byte message expected
                buffer[2] = readByteProtected(istream);
                if (logDebug) {
                    log.debug("state 2, rcv " + (buffer[2] & 0xFF));
                }
                if (buffer[0] != buffer[2]) {
                    // no match, consider buffer[2] start of new message
                    log.warn("addresses don't match: " + (buffer[0] & 0xFF) + ", " + (buffer[2] & 0xFF) + ", going to state 1");
                    buffer[0] = buffer[2];
                    state = 1;
                    return true;
                }
                state = 3;
            // fall through
            case 3:
                buffer[3] = readByteProtected(istream);
                if (logDebug) {
                    log.debug("state 3, rcv " + (buffer[3] & 0xFF));
                }
                if ((buffer[3] & 0x80) != 0) { // unexpected high bit
                    buffer[0] = buffer[3];
                    state = 1; // use this as address and try again
                    log.warn("3rd byte HOB set: " + (buffer[3] & 0xFF) + ", going to state 1");
                    return true;
                }
                // Check for "software version" command, error message; special
                // cases with deliberately bad parity
                boolean pollMsg = ((buffer[1] == buffer[3]) && (buffer[1] == 119));
                boolean errMsg = ((buffer[0] & 0xFF) == 0x80);

                // check 'parity'
                int parity = (buffer[0] & 0xF) + ((buffer[0] & 0x70) >> 4)
                        + ((buffer[1] * 2) & 0xF) + (((buffer[1] * 2) & 0xF0) >> 4)
                        + (buffer[3] & 0xF) + ((buffer[3] & 0x70) >> 4);
                if (((parity & 0xF) != 0) && !pollMsg && !errMsg) {
                    log.warn("parity mismatch: " + parity + ", going to state 2 with content " + (buffer[2] & 0xFF) + "," + (buffer[3] & 0xFF));
                    buffer[0] = buffer[2];
                    buffer[1] = buffer[3];
                    state = 2;
                    return true;
                }
                // success!
                loadBuffer(msg);
                if (logDebug) {
                    log.debug("Message complete: " + msg.toString());
                }
                state = 0;
                return false;
            default:
                log.error("unexpected loadChars state: " + state + ", go direct to state 0");
                state = 0;
                return true;
        }
    }

    void loadBuffer(AbstractMRReply msg) {
        msg.setElement(0, buffer[0]);
        msg.setElement(1, buffer[1]);
        msg.setElement(2, buffer[2]);
        msg.setElement(3, buffer[3]);
    }

    @Override
    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
        // does nothing
    }

    /**
     * Add header to the outgoing byte stream.
     *
     * @param msg The output byte stream
     * @return next location in the stream to fill
     */
    @Override
    protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
        return 0;  // Do nothing
    }

    /**
     * Although this protocol doesn't use a trailer, we implement this method to
     * set the expected reply address for this message.
     *
     * @param msg    The output byte stream
     * @param offset the first byte not yet used
     * @param m      the original message
     */
    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
        currentAddr = ((SerialMessage) m).getAddr();
        return;
    }

    /**
     * Determine how much many bytes the entire message will take, including
     * space for header and trailer
     *
     * @param m The message to be sent
     * @return Number of bytes
     */
    @Override
    protected int lengthOfByteStream(AbstractMRMessage m) {
        return m.getNumDataElements(); // All are same length as message
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficController.class);

}
