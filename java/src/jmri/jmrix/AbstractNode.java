package jmri.jmrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of a node for JMRI protocol support.
 * <p>
 * Integrated with {@link AbstractMRNodeTrafficController}.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public abstract class AbstractNode {

    /**
     * Creates a new instance of AbstractNode
     */
    public AbstractNode() {
    }

    public int nodeAddress = 0;  // Node address, range varies by subclass

    /**
     * Public method to return the node address.
     */
    public int getNodeAddress() {
        return (nodeAddress);
    }

    /**
     * Public method to set the node address. Address range is checked in
     * subclasses.
     *
     * @throws IllegalArgumentException if out of range
     */
    public void setNodeAddress(int address) {
        if (checkNodeAddress(address)) {
            nodeAddress = address;
        } else {
            log.error("illegal node address: " + Integer.toString(address));
            nodeAddress = 0;
            throw new IllegalArgumentException("Attempt to set address to invalid value: " + address);
        }
    }

    /**
     * Check for valid address with respect to range, etc.
     *
     * @return true if valid
     */
    abstract protected boolean checkNodeAddress(int address);

    /**
     * Create the needed Initialization packet (AbstractMRMessage) for this
     * node. Returns null if not needed.
     */
    abstract public AbstractMRMessage createInitPacket();

    /**
     * Create an Transmit packet (AbstractMRMessage) to send current state
     */
    abstract public AbstractMRMessage createOutPacket();

    /**
     * Are there sensors present, and hence this node will need to be polled?
     * Note: returns 'true' if at least one sensor is active for this node
     */
    abstract public boolean getSensorsActive();

    /**
     * Deal with a timeout in the transmission controller.
     *
     * @param m message that didn't receive a reply
     * @param l listener that sent the message
     * @return true if initialization required
     */
    abstract public boolean handleTimeout(AbstractMRMessage m, AbstractMRListener l);

    /**
     * A reply was received, so there was not timeout, do any needed processing.
     */
    abstract public void resetTimeout(AbstractMRMessage m);

    /**
     * Return state of needSend flag.
     */
    public boolean mustSend() {
        return needSend;
    }

    /**
     * Public to reset state of needSend flag. Subclasses may override to
     * enforce conditions.
     */
    public void resetMustSend() {
        needSend = false;
    }

    /**
     * Public to set state of needSend flag.
     */
    public void setMustSend() {
        needSend = true;
    }

    boolean needSend = true;          // 'true' if something has changed that requires data to be sent

    private static Logger log = LoggerFactory.getLogger(AbstractNode.class.getName());
}
