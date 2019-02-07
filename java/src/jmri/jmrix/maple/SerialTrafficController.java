package jmri.jmrix.maple;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInputStream;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRNodeTrafficController;
import jmri.jmrix.AbstractMRReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from Maple serial messages.
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
 * @author Bob Jacobsen Copyright (C) 2003, 2008
 * @author Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @author Bob Jacobsen, Dave Duchamp, adapt to use for Maple 2008, 2009, 2010
 *
 * @since 2.3.7
 */
@SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "multiple variables accessed outside synchronized core, which is quite suspicious, but code seems to interlock properly")
public class SerialTrafficController extends AbstractMRNodeTrafficController implements SerialInterface {

    /**
     * Create a new Maple SerialTrafficController instance.
     */
    public SerialTrafficController() {
        super();

        // set node range
        init(0, 127);

        // entirely poll driven, so reduce interval
        mWaitBeforePoll = 5;  // default = 25

        // initialize input and output utility classes
        mInputBits = new InputBits(this);
        mOutputBits = new OutputBits(this);
    }

    // InputBits and OutputBits
    private InputBits mInputBits = null;
    private OutputBits mOutputBits = null;

    public InputBits inputBits(){
      return mInputBits;
    }

    public OutputBits outputBits(){
      return mOutputBits;
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
     * Public method to set up for initialization of a Serial node.
     */
    public void initializeSerialNode(SerialNode node) {
        // dummy routine - Maple System devices do not require initialization
    }

    @Override
    protected AbstractMRMessage enterProgMode() {
        log.warn("enterProgMode doesn't make sense for Maple serial");
        return null;
    }

    @Override
    protected AbstractMRMessage enterNormalMode() {
        // can happen during error recovery, null is OK
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

    // initialization not needed ever
    @Override
    protected boolean getMustInit(int i) {
        return false;
    }

    // With the Maple Systems Protocol, output packets are limited to 99 bits.  If there are more than 
    // 99 bits configured, multiple output packets must be sent.  The following cycle through that
    // process.
    private boolean mNeedSend = true;
    private int mStartBitNumber = 1;
    // Similarly the poll command can only poll 99 input bits at a time, so more packets may be needed.
    private boolean mNeedAdditionalPollPacket = false;
    private int mStartPollAddress = 1;
    // The Maple poll response does not contain an address, so the following is needed.
    private int mSavedPollAddress = 1;

    public int getSavedPollAddress() {
        return mSavedPollAddress;
    }
    private int mCurrentNodeIndexInPoll = -1;

    /**
     * Handle output and polling for Maple Serial Nodes from within the running
     * thread.
     */
    @Override
    protected synchronized AbstractMRMessage pollMessage() {
        // ensure validity of call - are nodes in yet?
        if (getNumNodes() <= 0) {
            return null;
        }
        if (curSerialNodeIndex >= getNumNodes()) {
            curSerialNodeIndex = 0;
            // process input bits
            mInputBits.makeChanges();
            // initialize send of output bits
            mNeedSend = true;
            mStartBitNumber = 1;
        }
        // send Output packet if needed
        if (mNeedSend) {
            int endBitNumber = mStartBitNumber + 98;
            if (endBitNumber > OutputBits.getNumOutputBits()) {
                endBitNumber = OutputBits.getNumOutputBits();
                mNeedSend = false;
            }
            if (endBitNumber == OutputBits.getNumOutputBits()) {
                mNeedSend = false;
            }
            SerialMessage m = mOutputBits.createOutPacket(mStartBitNumber, endBitNumber);
            mCurrentNodeIndexInPoll = -1;

            // update the starting bit number if additional packets are needed
            if (mNeedSend) {
                mStartBitNumber = endBitNumber + 1;
            }
            return m;
        }
        // poll for Sensor input
        int count = 99;
        if (count > (InputBits.getNumInputBits() - mStartPollAddress + 1)) {
            count = InputBits.getNumInputBits() - mStartPollAddress + 1;
        }
        SerialMessage m = SerialMessage.getPoll(
                getNode(curSerialNodeIndex).getNodeAddress(), mStartPollAddress, count);
        mSavedPollAddress = mStartPollAddress;
        mCurrentNodeIndexInPoll = curSerialNodeIndex;

        // check if additional packet is needed
        if ((mStartPollAddress + count - 1) < InputBits.getNumInputBits()) {
            mNeedAdditionalPollPacket = true;
            mStartPollAddress = mStartPollAddress + 99;
        } else {
            mNeedAdditionalPollPacket = false;
            mStartPollAddress = 1;
            curSerialNodeIndex++;
        }
        return m;
    }

    protected int wrTimeoutCount = 0;

    public int getWrTimeoutCount() {
        return wrTimeoutCount;
    }

    public void resetWrTimeoutCount() {
        wrTimeoutCount = 0;
    }

    @Override
    protected void handleTimeout(AbstractMRMessage m, AbstractMRListener l) {
        if (m.getElement(3) == 'W' && m.getElement(4) == 'C') {
            wrTimeoutCount++;    // should not happen
        } else if (m.getElement(3) == 'R' && m.getElement(4) == 'C') {
            if (mNeedAdditionalPollPacket) {
                // log.warn("Timeout of poll message, node = {} beg addr = {}", curSerialNodeIndex, mSavedPollAddress);
                getNode(curSerialNodeIndex).handleTimeout(m, l);
            } else {
                // log.warn("Timeout of poll message, node = {} beg addr = {}", (curSerialNodeIndex-1), mSavedPollAddress);
                getNode(curSerialNodeIndex - 1).handleTimeout(m, l);
            }
        } else {
            log.error("Timeout of unknown message - {}", m.toString());
        }
    }

    @Override
    protected void resetTimeout(AbstractMRMessage m) {
        if (mCurrentNodeIndexInPoll < 0) {
            wrTimeoutCount = 0;    // should never happen - outputs should not be timed
        } else {
            // don't use super behavior, as timeout to init, transmit message is normal
            // inform node
            getNode(mCurrentNodeIndexInPoll).resetTimeout(m);
        }
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
     * Static function returning the SerialTrafficController instance to use.
     *
     * @return The registered SerialTrafficController instance for general use,
     *         if need be creating one.
     */
    @Deprecated
    static public SerialTrafficController instance() {
        return null;
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
    public void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        int i;
        boolean first = true;
        for (i = 0; i < msg.maxSize() - 1; i++) {
            byte char1 = readByteProtected(istream);
            msg.setElement(i, char1 & 0xFF);
            if (first) {
                first = false;
                log.debug("start message with {}", char1);
            }
            if (char1 == 0x03) { // normal message
                // get checksum bytes and end
                log.debug("ETX ends message");
                char1 = readByteProtected(istream);
                msg.setElement(i + 1, char1 & 0xFF);
                char1 = readByteProtected(istream);
                msg.setElement(i + 2, char1 & 0xFF);
                break;           // end of message
            }
            if (char1 == 0x06) { // ACK OK
                // get station, command and end
                log.debug("ACK ends message");
                char1 = readByteProtected(istream);  // byte 2
                msg.setElement(++i, char1 & 0xFF);
                char1 = readByteProtected(istream);  // byte 3
                msg.setElement(++i, char1 & 0xFF);
                char1 = readByteProtected(istream);  // byte 4
                msg.setElement(++i, char1 & 0xFF);
                char1 = readByteProtected(istream);  // byte 5
                msg.setElement(++i, char1 & 0xFF);
                break;           // end of message
            }
            if (char1 == 0x15) { // NAK error
                // get station, command, error bytes and end
                log.debug("NAK ends message");
                char1 = readByteProtected(istream);  // byte 2
                msg.setElement(++i, char1 & 0xFF);
                char1 = readByteProtected(istream);  // byte 3
                msg.setElement(++i, char1 & 0xFF);
                char1 = readByteProtected(istream);  // byte 4
                msg.setElement(++i, char1 & 0xFF);
                char1 = readByteProtected(istream);  // byte 5
                msg.setElement(++i, char1 & 0xFF);
                char1 = readByteProtected(istream);  // byte 6
                msg.setElement(++i, char1 & 0xFF);
                break;           // end of message
            }
        }
    }

    @Override
    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
        // don't skip anything
    }

    /**
     * Add header to the outgoing byte stream.
     *
     * @param msg the output byte stream
     * @param m the message to add the header to
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
     * space for header and trailer.
     *
     * @param m the message to be sent
     * @return Number of bytes
     */
    @Override
    protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        return len;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficController.class);

}
