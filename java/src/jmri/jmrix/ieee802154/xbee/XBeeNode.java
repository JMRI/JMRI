// XBeeNode.java

package jmri.jmrix.ieee802154.xbee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractNode;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.ieee802154.IEEE802154Node;

/**
 * Implementation of a node for XBee networks.
 * <p>
 * Integrated with {@link XBeeTrafficController}.
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
public class XBeeNode extends IEEE802154Node {

    private String Identifier;
    
    /**
     * Creates a new instance of XBeeNode
     */
    public XBeeNode() {
         Identifier="";
    }

    public XBeeNode(byte pan[],byte user[], byte global[]) {
        super(pan,user,global);
        Identifier="";
    }


   
    /**
     * Create the needed Initialization packet (AbstractMRMessage) for this node.
     * Returns null if not needed.
     */
    public AbstractMRMessage createInitPacket() {return null; }

    /**
     * Create an Transmit packet (AbstractMRMessage) to send current state
     */
    public AbstractMRMessage createOutPacket() {return null;} //TODO

    /**
     * Are there sensors present, and hence this node will need to be polled?
     *  Note:  returns 'true' if at least one sensor is active for this node
     */
    public boolean getSensorsActive() {return false;} //TODO
    
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
    public void resetTimeout(AbstractMRMessage m) { return; }


    /**
     * XBee Nodes store an identifier. we want to be able to store
     * and retrieve this information.
     */
    public void setIdentifier(String id){ Identifier=id; }
    public String getIdentifier(){ return Identifier; }

    private static Logger log = LoggerFactory.getLogger(XBeeNode.class.getName());
}
