package jmri.jmrix.lenz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import jmri.jmrix.lenz.liusb.LIUSBXNetPacketizer;
import jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A special flavour of XPressNet simulator suitable for testing. It allows
 * to delay responses, capture outgoing or incoming messages etc.
 * 
 * @author svatopluk.dedic@gmail.com
 */
abstract class XNetTestSimulator extends XNetSimulatorAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(XNetTestSimulator.class);
    
    /**
     * The replies to be sent from simulated command station.
     */
    private final List<XNetReply> replyBuffer = new ArrayList<>();
    
    /**
     * Additional replies accumulated during the processing.
     */
    private final List<XNetReply> additionalReplies = new ArrayList<>();
    
    /**
     * Accessory state cache. A "1" bit means THROWN, "0" means
     * CLOSED. 
     */
    protected final BitSet accessoryState = new BitSet(1024);
    
    /**
     * Bit is set if the accessory was operated.
     */
    protected final BitSet accessoryOperated = new BitSet(1024);

    /**
     * If set to true, the {@link #repliesAllowed} semaphore will
     * release replies. If false, all replies are released immediately.
     */
    public volatile boolean limitReplies;
    
    /**
     * Number of replies allowed to return to the JMRI.
     */
    public final Semaphore repliesAllowed = new Semaphore(0);
    
    /**
     * Accumulates messages sent "to the command station".
     */
    private final List<XNetMessage>   outgoingMessages = new ArrayList<>();
    
    /**
     * Accumulates replies from the "command station".
     */
    private final List<XNetReply>     incomingReplies = new ArrayList<>();

    /**
     * If true, accumulates sent/received messages. Use {@link #getOutgoingMessages()}
     * and {@link #getIncomingReplies()} to get messages. Use {@link #clearMesages()}
     * to reset the lists.
     */
    private volatile boolean captureMessages;
    
    public void setCaptureMessages(boolean captureMessages) {
        this.captureMessages = captureMessages;
    }

    public synchronized List<XNetMessage> getOutgoingMessages() {
        return new ArrayList<>(outgoingMessages);
    }

    public synchronized List<XNetReply> getIncomingReplies() {
        return new ArrayList<>(incomingReplies);
    }
    
    public synchronized void clearMesages() {
        outgoingMessages.clear();
        incomingReplies.clear();
    }
    
    private void insertAdditionalReplies() {
        replyBuffer.addAll(additionalReplies);
        additionalReplies.clear();
    }

    protected XNetReply addReply(XNetReply r) {
        additionalReplies.add(r);
        return r;
    }

    @Override
    public void configure(XNetTrafficController ctrls) {
        super.configure(ctrls);
    }

    private void maybeWaitBeforeReply(XNetReply reply) {
        if (!limitReplies) {
            return;
        }
        if (reply instanceof PrimaryXNetReply) {
            try {
                repliesAllowed.acquire();
            } catch (InterruptedException ex) {
                LOG.debug("Interrupted", ex);
            }
        }
    }

    @Override
    protected XNetMessage readMessage() {
        XNetMessage msg = super.readMessage();
        if (captureMessages) {
            synchronized (this) {
                outgoingMessages.add(msg);
            }
        }
        return msg;
    }
    
    private XNetReply captureReply(XNetReply r) {
        maybeWaitBeforeReply(r);
        if (r instanceof PrimaryXNetReply) {
            r = ((PrimaryXNetReply)r).original;
        }
        if (captureMessages) {
            synchronized (this) {
                incomingReplies.add(r);
            }
        }
        LOG.debug("Returning reply: {} ... {} ", r, r.toMonitorString());
        return r;
    }

    
    /**
     * Serves the batched items through FIFO. The test class may generate
     * additional replies, which are ordered after the primary one.
     *
     * @param m XNet message instance
     * @return the current reply
     */
    @Override
    protected XNetReply generateReply(XNetMessage m) {
        insertAdditionalReplies();
        if (m == null) {
            if (replyBuffer.isEmpty()) {
                return null;
            }
            XNetReply r = replyBuffer.remove(0);
            return captureReply(r);
        }
        XNetReply reply = super.generateReply(m);
        if (isPrimaryReply(m)) {
            reply = new PrimaryXNetReply(reply);
        }
        if (replyBuffer.isEmpty()) {
            insertAdditionalReplies();
            return captureReply(reply);
        }
        replyBuffer.add(reply);
        insertAdditionalReplies();
        XNetReply r = replyBuffer.remove(0);
        return captureReply(r);
    }

    /**
     * Determines, if the command is interesting for reply limiting feature.
     * If this method returns true, and {@link #limitReplies} is also true, this reply
     * will require a permit from {@link #repliesAllowed} semaphore before
     * it is released to the JMRI.
     * <p>
     * The default implementation limits Accessory Operation Requests. Override to extend
     * or change to different XPressnet message type.
     * 
     * @param msg the command sent to the simulated command station
     * @return true, if the reply should possibly wait for permission
     */
    protected boolean isPrimaryReply(XNetMessage msg) {
        return msg.getElement(0) == XNetConstants.ACC_OPER_REQ;
    }

    /**
     * Marker wrapper to indicate this reply may need to wait
     * on the {@link #repliesAllowed} guard.
     */
    private static class PrimaryXNetReply extends XNetReply {
        private final XNetReply original;
        public PrimaryXNetReply(XNetReply reply) {
            super(reply);
            this.original = reply;
        }
    }

    /**
     * The previous accessory state before the current message was recevied
     */
    protected boolean previousAccessoryState;

    @Override
    protected XNetReply accReqReply(XNetMessage m) {
        int baseaddress = m.getElement(1);
        int subaddress = (m.getElement(2) & 0x06) >> 1;
        int address = (baseaddress * 4) + subaddress + 1;
        int output = m.getElement(2) & 0x01;
        boolean on = ((m.getElement(2) & 0x08)) == 0x08;
        previousAccessoryState = accessoryState.get(address);
        if (on) {
            accessoryState.set(address, output != 0);
        }
        LOG.debug("Received command {} ... {}", m, m.toMonitorString());
        return generateAccRequestReply(address, output, on);
    }

    /**
     * Generate reply to accessory request command.
     * The returned XNetReply is the first to be returned by this simulated command station.
     * Additional replies can be added with {@link #addReply}.
     * @param address the accessory address
     * @param output the output to be manipulated
     * @param state true if output should be on, false for off
     * @return the reply instance.
     */
    protected abstract XNetReply generateAccRequestReply(int address, int output, boolean state);

    protected XNetReply accInfoReply(int dccTurnoutAddress) {
        dccTurnoutAddress--;
        int baseAddress = dccTurnoutAddress / 4;
        boolean upperNibble = dccTurnoutAddress % 4 >= 2;
        return accInfoReply(true, baseAddress, upperNibble);
    }

    @Override
    protected XNetReply accInfoReply(XNetMessage m) {
        boolean nibble = (m.getElement(2) & 0x01) == 0x01;
        int ba = m.getElement(1);
        return accInfoReply(false, ba, nibble);
    }

    /**
     * Return the turnout feedback type.
     * <ul>
     * <li>0x00 - turnout without feedback, ie DR5000
     * <li>0x01 - turnout with feedback, ie NanoX
     * <li>0x10 - feedback module
     * </ul>
     * @return the turnout type reported by this station.
     */
    protected int getTurnoutFeedbackType() {
        return 0x01;
    }
    
    protected int getAccessoryStateBits(int a) {
        boolean state = accessoryState.get(a);
        int zbits = state ? 0b10 : 0b01;
        return zbits;
    }

    protected XNetReply accInfoReply(boolean broadcast, int baseAddress, boolean nibble) {
        XNetReply r = new XNetReply();
        r.setOpCode(broadcast ? XNetConstants.ACC_INFO_RESPONSE : XNetConstants.ACC_INFO_RESPONSE);
        r.setElement(1, baseAddress);
        int nibbleVal = 0;
        int a = baseAddress * 4 + 1;
        if (nibble) {
            a += 2;
        }
        int zbits = getAccessoryStateBits(a++);
        nibbleVal |= zbits;
        zbits = getAccessoryStateBits(a++);
        nibbleVal |= (zbits << 2);
        r.setElement(2, 0 << 7 | // turnout movement completed
        getTurnoutFeedbackType() << 5 | // two bits: accessory without feedback
        (nibble ? 1 : 0) << 4 | // upper / lower nibble
        nibbleVal & 0x0f);
        r.setElement(3, 0);
        r.setParity();
        return r;
    }
    
    /**
     * Get characters from the input source, and file a message.
     * <p>
     * Returns only when the message is complete.
     * <p>
     * Only used in the Receive thread.
     *
     * @return filled message
     * @throws IOException when presented by the input source.
     */
    @Override
    protected XNetMessage loadChars() throws java.io.IOException {
        int i;
        byte char1;
        char1 = readByteProtected();
        while (((char1 & 0xF0) == 0xF0)) {
            if ((char1 & 0xFF) != 0xF0 && (char1 & 0xFF) != 0xF2) {
                //  toss this byte and read the next one
                char1 = readByteProtected();
            }

        }

        int len = (char1 & 0x0f) + 2;  // opCode+Nbytes+ECC
        XNetMessage msg = new XNetMessage(len);
        msg.setElement(0, char1 & 0xFF);
        for (i = 1; i < len; i++) {
            char1 = readByteProtected();
            msg.setElement(i, char1 & 0xFF);
        }
        return msg;
    }

    /**
     * Simulator of Paco Canada Nano-X command station, connected using GEN-LI serial
     * interface.
     * This command station reports just single feedback for accessory "ON" operation.
     */
    static class NanoXGenLi extends XNetTestSimulator {
        @Override
        protected XNetReply generateAccRequestReply(int address, int output, boolean state) {
            if (state) {
                return accInfoReply(address);
            } else {
                return okReply();
            }
        }
    }
    
    /**
     * Simulator of DR5000 command station connected through USB interface.
     * {@link LIUSBXNetPacketizer} must be used with this station.
     */
    static class DR5000 extends XNetTestSimulator {
        @Override
        protected XNetReply generateAccRequestReply(int address, int output, boolean state) {
            if (state) {
                addReply(accInfoReply(address));
                return okReply();
            } else {
                return okReply();
            }
        }

        @Override
        protected int getTurnoutFeedbackType() {
            return 0;
        }

        @Override
        protected int addHeaderToOutput(byte[] msg, XNetReply m) {
            m.resetUnsolicited();
            msg[0] = (byte)0xFF;
            msg[1] = (byte)(m.isUnsolicited() ? 0xFD : 0xFE);
            return 2;
        }

        @Override
        protected int lengthOfByteStream(XNetReply reply) {
            return super.lengthOfByteStream(reply) + 2;
        }
    }
    
    /**
     * Simulator of a LZV100 command station, connected through a serial
     * interface.
     */
    static class LZV100 extends XNetTestSimulator {

        @Override
        protected int getAccessoryStateBits(int a) {
            if (accessoryOperated.get(a)) {
                return super.getAccessoryStateBits(a);
            } else {
                // not operated
                return 0x00;
            }
        }
        
        @Override
        protected XNetReply generateAccRequestReply(int address, int output, boolean state) {
            XNetReply r;
            
            if (state) {
                if (accessoryOperated.get(address) && previousAccessoryState == (output != 0)) {
                    // just OK, the accessory is in the same state.
                    return okReply();
                } else {
                    accessoryOperated.set(address);
                    r = accInfoReply(address);
                    r.setUnsolicited();
                    addReply(okReply());
                }
            } else {
                accessoryOperated.set(address);
                r = okReply();
            }
            return r;
        }
    }
    
    /**
     * Represents a LZV100, connected through LI-USB interface. 
     * {@link LIUSBXNetPacketizer} must be used.
     */
    static class LZV100_USB extends LZV100 {

        @Override
        protected int addHeaderToOutput(byte[] msg, XNetReply m) {
            m.resetUnsolicited();
            msg[0] = (byte)0xFF;
            msg[1] = (byte)(m.isUnsolicited() ? 0xFD : 0xFE);
            return 2;
        }

        @Override
        protected int lengthOfByteStream(XNetReply reply) {
            return super.lengthOfByteStream(reply) + 2;
        }
        
    }
}
