package jmri.jmrix.ieee802154;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of a node for IEEE 802.15.4 networks.
 * <p>
 * Integrated with {@link IEEE802154TrafficController}.
 * <p>
 * Each node has 3 addresses associated with it:
 * <ol>
 * <li>A 16 bit PAN (Personal Area Network) ID assigned by the user</li>
 * <li>A 16 bit User Assigned Address</li>
 * <li>A 64 bit Globally Unique ID assigned by the manufacturer</li>
 * </ol>
 * <p>
 * All nodes in a given network must have the same PAN ID
 *
 * @author Paul Bender Copyright 2013
 */
public abstract class IEEE802154Node extends AbstractNode {

    /**
     * Creates a new instance of AbstractNode
     */
    public IEEE802154Node() {
    }

    public IEEE802154Node(byte pan[], byte user[], byte global[]) {
        panaddress = pan;
        useraddress = user;
        globaladdress = global;
        setNodeAddress((user[0] << 8) + ( user[1] & 0xff) );
    }

    protected byte panaddress[] = {0, 0}; // default pan address to 0
    protected byte useraddress[] = {0, 0}; // default user address to 0
    protected byte globaladdress[] = {0, 0, 0, 0, 0, 0, 0, 0}; // default global address to 0

    /**
     * Public method to set the node address. Address range is checked in
     * subclasses.
     *
     * @throws IllegalArgumentException if out of range
     */
    @Override
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
    @Override
    protected boolean checkNodeAddress(int address) {
        // we're not using this address directly, so ignore.
        return true;
    }

    /**
     * Set PAN address.
     *
     * @param addr byte array containing upper and lower bytes of the 16 bit PAN
     *             address.
     */
    public void setPANAddress(byte addr[]) {
        for (int i = 0; i < panaddress.length; i++) {
            panaddress[i] = addr[i];
        }
    }

    /**
     * Get the PAN address
     *
     * @return byte array containing the upper and lower bytes of the PAN
     *         address
     */
    public byte[] getPANAddress() {
        return panaddress;
    }

    /**
     * Set User address.
     *
     * @param addr byte array containing upper and lower bytes of the 16 bit
     *             user assigned address.
     */
    public void setUserAddress(byte addr[]) {
        for (int i = 0; i < useraddress.length; i++) {
            useraddress[i] = addr[i];
        }
    }

    /**
     * Get the User address
     *
     * @return byte array containing the upper and lower bytes of the User
     *         assigned address
     */
    public byte[] getUserAddress() {
        return useraddress;
    }

    /**
     * Set global address.
     *
     * @param addr byte array containing bytes of the 64 bit global address.
     */
    public void setGlobalAddress(byte addr[]) {
        for (int i = 0; i < globaladdress.length; i++) {
            globaladdress[i] = addr[i];
        }
    }

    /**
     * Get the Global address
     *
     * @return byte array containing the 8 bytes of the global address
     */
    public byte[] getGlobalAddress() {
        return globaladdress;
    }

    /**
     * Create the needed Initialization packet (AbstractMRMessage) for this
     * node. Returns null if not needed.
     */
    @Override
    abstract public AbstractMRMessage createInitPacket();

    /**
     * Create an Transmit packet (AbstractMRMessage) to send current state
     */
    @Override
    abstract public AbstractMRMessage createOutPacket();

    /**
     * Are there sensors present, and hence this node will need to be polled?
     * Note: returns 'true' if at least one sensor is active for this node
     */
    @Override
    abstract public boolean getSensorsActive();

    /**
     * Deal with a timeout in the transmission controller.
     *
     * @param m message that didn't receive a reply
     * @param l listener that sent the message
     * @return true if initialization required
     */
    @Override
    abstract public boolean handleTimeout(AbstractMRMessage m, AbstractMRListener l);

    /**
     * A reply was received, so there was not timeout, do any needed processing.
     */
    @Override
    abstract public void resetTimeout(AbstractMRMessage m);

    /**
     * Return state of needSend flag.
     */
    @Override
    public boolean mustSend() {
        return needSend;
    }

    /**
     * Public to reset state of needSend flag. Subclasses may override to
     * enforce conditions.
     */
    @Override
    public void resetMustSend() {
        needSend = false;
    }

    /**
     * Public to set state of needSend flag.
     */
    @Override
    public void setMustSend() {
        needSend = true;
    }

    boolean needSend = true;          // 'true' if something has changed that requires data to be sent

    private final static Logger log = LoggerFactory.getLogger(IEEE802154Node.class);

}
