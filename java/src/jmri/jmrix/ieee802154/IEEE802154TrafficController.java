package jmri.jmrix.ieee802154;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRNodeTrafficController;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from messages. The "IEEE802154Interface" side
 * sends/receives message objects.
 * <p>
 * The connection to a IEEE802154PortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <p>
 * This maintains a list of nodes, but doesn't currently do anything with it.
 * This implementation is complete and can be instantiated, but is not
 * functional. It will be created e.g. when a default object is needed for
 * configuring nodes, etc, during the initial configuration. A subclass must be
 * instantiated to actually communicate with an adapter.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2005, 2006, 2008 Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * @author Paul Bender Copyright (C) 2013
 */
abstract public class IEEE802154TrafficController extends AbstractMRNodeTrafficController implements IEEE802154Interface {

    /**
     * Create a new IEEE802154TrafficController instance.
     */
    public IEEE802154TrafficController() {
        super();
        logDebug = log.isDebugEnabled();

        init(1, 100);

        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 1000;  // can take a long time to send
    }

    /**
     * Get a message of a specific length for filling in.
     * <p>
     * This is a default, null implementation, which must be overridden in an
     * adapter-specific subclass.
     *
     * @param length length for new message
     * @return null since this method should be over-ridden
     */
    public IEEE802154Message getIEEE802154Message(int length) {
        return null;
    }

    // have several debug statements in tight loops, e.g. every character;
    // only want to check once
    protected boolean logDebug = false;

    // The methods to implement the IEEE802154Interface

    @Override
    public synchronized void addIEEE802154Listener(IEEE802154Listener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeIEEE802154Listener(IEEE802154Listener l) {
        this.removeListener(l);
    }

    @Override
    protected int enterProgModeDelayTime() {
        // we should to wait at least a second after enabling the programming track
        return 1000;
    }

    /**
     * Forward a IEEE802154Message to all registered IEEE802154Interface
     * listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((IEEE802154Listener) client).message((IEEE802154Message) m);
    }

    /**
     * Forward a reply to all registered IEEE802154Interface listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((IEEE802154Listener) client).reply((IEEE802154Reply) r);
    }

    /**
     * Eventually, do initialization if needed
     */
    @Override
    protected AbstractMRMessage pollMessage() {
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
    public void sendIEEE802154Message(IEEE802154Message m, IEEE802154Listener reply) {
        sendMessage(m, reply);
    }

    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (logDebug) {
            log.debug("forward " + m);
        }
        super.forwardToPort(m, reply);
    }

    @Override
    protected AbstractMRMessage enterProgMode() {
        return null;
    }

    @Override
    protected AbstractMRMessage enterNormalMode() {
        return null;
    }

    public void setAdapterMemo(IEEE802154SystemConnectionMemo adaptermemo) {
        memo = adaptermemo;
    }

    public IEEE802154SystemConnectionMemo getAdapterMemo() {
        return memo;
    }

    private IEEE802154SystemConnectionMemo memo = null;

    /**
     * IEEE 802.15.4 messages start with a 0x7E delimiter byte.
     */
    @Override
    protected void waitForStartOfReply(java.io.DataInputStream istream) throws java.io.IOException {
        // loop looking for the start character
        while (readByteProtected(istream) != 0x7E) {
        }
    }

    /**
     * <p>
     * The length is the first byte of the message payload, The end of the
     * message occurs when the length of the message is equal to the payload
     * length.
     * <p>
     * NOTE: This function does not work with XBee nodes, which provide a
     * modified version of the packets sent via the radio.
     */
    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        return ((msg.getElement(0) + 2) == msg.getNumDataElements());
    }

    /**
     * Add trailer to the outgoing byte stream. This version adds the checksum
     * to the end of the message.
     *
     * @param msg    The output byte stream
     * @param offset the first byte not yet used
     */
    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, jmri.jmrix.AbstractMRMessage m) {
        if (m.getNumDataElements() == 0) {
            return;
        }
        ((IEEE802154Message) m).setParity();
    }

    /**
     * Add header to the outgoing byte stream.
     *
     * @param msg The output byte stream
     * @return next location in the stream to fill
     */
    //protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
    //    m.setElement(0)=0x7E;
    //    return 1;
    //}

    /**
     * <p>
     * This is a default, null implementation, which must be overridden in an
     * adapter-specific subclass.
     */
    //protected AbstractMRReply newReply() {return new IEEE802154Reply();}

    /**
     * Build a new IEEE802154 Node.
     * Must be implemented by derived classes
     * @return new IEEE802154Node.
     */
    abstract public IEEE802154Node newNode();

    /**
     * Identify a SerialNode from its node address.
     *
     * @param addr hex string for address, numbered from 0
     * @return serial node id, or 'niull' if a SerialNode with the specified
     * address was not found
     */
    synchronized public AbstractNode getNodeFromAddress(String addr) {
        log.debug("String getNodeFromAddress called with {}", addr);
        byte ba[] = jmri.util.StringUtil.bytesFromHexString(addr);
        return getNodeFromAddress(ba);
    }

    /**
     * Identify a SerialNode from its node address Note: 'addr'
     * is the node address.
     *
     * @param ia int array of node address, numbered from 0
     * @return serial address, or 'null' if a SerialNode with the specified
     * address was not found
     */
    synchronized public AbstractNode getNodeFromAddress(int ia[]) {
        if(logDebug) {
           String s="";
           for( int i=0;i<ia.length;i++) {
               s=jmri.util.StringUtil.appendTwoHexFromInt(ia[i],s);
           }
           log.debug("int array getNodeFromAddress called with {}", s);
        }
        byte ba[] = new byte[ia.length];
        for (int i = 0; i < ia.length; i++) {
            ba[i] = (byte) (ia[i] & 0xff);
        }
        return getNodeFromAddress(ba);
    }

    /**
     * Identify a SerialNode from its node address.
     *
     * @param ba array of bytes in hex address
     * @return serial node id, or 'null' if a SerialNode with the specified
     * address was not found
     */
    synchronized public AbstractNode getNodeFromAddress(byte ba[]) {
        log.debug("byte array getNodeFromAddress called with {}",
                jmri.util.StringUtil.hexStringFromBytes(ba));
        for (int i = 0; i < numNodes; i++) {
            byte bsa[] = ((IEEE802154Node) getNode(i)).getUserAddress();
            byte bga[] = ((IEEE802154Node) getNode(i)).getGlobalAddress();
            if (bsa.length == ba.length) {
                int j = 0;
                for (; j < bsa.length; j++) {
                    if (bsa[j] != ba[j]) {
                        break;
                    }
                }
                if (j == bsa.length) {
                    return (getNode(i));
                }
            } else if (bga.length == ba.length) {
                int j = 0;
                for (; j < bga.length; j++) {
                    if (bga[j] != ba[j]) {
                        break;
                    }
                }
                if (j == bga.length) {
                    return (getNode(i));
                }
            }
        }
        return (null);
    }

    /**
     * Delete a node by the string representation of its address.
     *
     * @param nodeAddress text of hex node address
     */
    @SuppressFBWarnings(value="VO_VOLATILE_INCREMENT", justification="synchronized method provides locking")
    public synchronized void deleteNode(String nodeAddress) {
        // find the serial node
        int index = 0;
        for (int i = 0; i < numNodes; i++) {
            if (nodeArray[i] == getNodeFromAddress(nodeAddress)) {
                index = i;
            }
        }
        if (index == curSerialNodeIndex) {
            log.warn("Deleting the serial node active in the polling loop");
        }
        // Delete the node from the node list
        numNodes--;
        if (index < numNodes) {
            // did not delete the last node, shift
            for (int j = index; j < numNodes; j++) {
                nodeArray[j] = nodeArray[j + 1];
            }
        }
        nodeArray[numNodes] = null;
    }

    private final static Logger log = LoggerFactory.getLogger(IEEE802154TrafficController.class);

}
