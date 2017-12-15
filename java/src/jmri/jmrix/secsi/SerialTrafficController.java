package jmri.jmrix.secsi;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInputStream;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRNodeTrafficController;
import jmri.jmrix.AbstractMRReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from SECSI serial messages.
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
 * @author	Bob Jacobsen Copyright (C) 2003, 2006
 * @author Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 */
public class SerialTrafficController extends AbstractMRNodeTrafficController implements SerialInterface {

    public SerialTrafficController() {
        super();

        // set node range
        init(0, 255);

        // entirely poll driven, so reduce interval
        mWaitBeforePoll = 25;  // default = 25

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
        log.warn("enterProgMode doesn't make sense for SECSI serial");
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
    }

    /**
     * Handles initialization, output and polling from within the running thread
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
            AbstractMRMessage m = getNode(curSerialNodeIndex).createInitPacket();
            if (m != null) { // SECSI boards don't need this yet
                log.debug("send init message: " + m);
                m.setTimeout(2000);  // wait for init to finish (milliseconds)
                return m;
            }   // else fall through to continue
        }
        // send Output packet if needed
        if (getNode(curSerialNodeIndex).mustSend()) {
            log.debug("request write command to send");
            AbstractMRMessage m = getNode(curSerialNodeIndex).createOutPacket();
            getNode(curSerialNodeIndex).resetMustSend();
            m.setTimeout(500);
            return m;
        }
        // poll for Sensor input
        if (getNode(curSerialNodeIndex).getSensorsActive()) {
            // Some sensors are active for this node, issue poll
            SerialMessage m = SerialMessage.getPoll(
                    getNode(curSerialNodeIndex).getNodeAddress());
            if (curSerialNodeIndex >= getNumNodes()) {
                curSerialNodeIndex = 0;
            }
            return m;
        } else {
            // no Sensors (inputs) are active for this node
            return null;
        }
    }

    @Override
    synchronized protected void handleTimeout(AbstractMRMessage m, AbstractMRListener l) {
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
    synchronized protected void resetTimeout(AbstractMRMessage m) {
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
    @Deprecated
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
    protected int incomingLength = 0;

    @Override
    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        // get 1st byte, see if ending too soon
        byte char1 = readByteProtected(istream);
        msg.setElement(0, char1 & 0xFF);
        if ((char1 & 0xFF) != currentAddr) {
            // mismatch, end early
            return;
        }
        if (incomingLength <= 1) {
            return;
        }
        for (int i = 1; i < incomingLength; i++) {  // reading next four bytes
            char1 = readByteProtected(istream);
            msg.setElement(i, char1 & 0xFF);
        }
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
     * set the expected reply length and address for this message.
     *
     * @param msg    The output byte stream
     * @param offset the first byte not yet used
     * @param m      the original message
     */
    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
        incomingLength = ((SerialMessage) m).getResponseLength();
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
        return 5; // All are 5 bytes long
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficController.class);
}
