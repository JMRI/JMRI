// SerialNode.java

package jmri.jmrix.ieee802154.serialdriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractNode;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.ieee802154.IEEE802154Node;

/**
 * Implementation of a serial node for IEEE 802.15.4 networks.
 * <p>
 * Integrated with {@link SerialTrafficController}.
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
 * @version   $Revision$
 */
public class SerialNode extends IEEE802154Node{
    
    /**
     * Creates a new instance of AbstractNode
     */
    public SerialNode() {
    }

    public SerialNode(byte pan[],byte user[], byte global[]) {
        super(pan,user,global);
    }
   
    /**
     * Create the needed Initialization packet (AbstractMRMessage) for this node.
     * Returns null if not needed.
     */
    public AbstractMRMessage createInitPacket() {return null;}

    /**
     * Create an Transmit packet (AbstractMRMessage) to send current state
     */
    public AbstractMRMessage createOutPacket() { return null; }

    /**
     * Are there sensors present, and hence this node will need to be polled?
     *  Note:  returns 'true' if at least one sensor is active for this node
     */
    public boolean getSensorsActive() {return false;}
    
    /**
     * Deal with a timeout in the transmission controller.
     * @param m message that didn't receive a reply
     * @param l listener that sent the message 
     * @return true if initialization required
     */
    public boolean handleTimeout(AbstractMRMessage m,AbstractMRListener l) { return false; }

    /**
     * A reply was received, so there was not timeout, do any
     * needed processing.
     */
    public void resetTimeout(AbstractMRMessage m){ return; }

    private static Logger log = LoggerFactory.getLogger(SerialNode.class.getName());
}
