// IEEE802154TrafficController.java

package jmri.jmrix.ieee802154;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

/**
 * Converts Stream-based I/O to/from messages.  The "IEEE802154Interface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a IEEE802154PortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This maintains a list of nodes, but doesn't currently do anything
 * with it.
 * This implementation is complete and can be instantiated, but
 * is not functional.  It will be created e.g. when a default
 * object is needed for configuring nodes, etc, during the initial
 * configuration.  A subclass must be instantiated to actually
 * communicate with an adapter.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001, 2003, 2005, 2006, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * @author Paul Bender Copyright (C) 2013
 * @version			$Revision$
 */
abstract public class IEEE802154TrafficController extends AbstractMRTrafficController implements IEEE802154Interface {

	public IEEE802154TrafficController() {
        super();
        logDebug = log.isDebugEnabled();
        
        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 1000;  // can take a long time to send

    }

    /**
     * instance use of the traffic controller is no longer used for multiple connections
     */
	@Deprecated
    public void setInstance(){ log.error("Deprecated Method setInstance called"); }

    /**
     * Get a message of a specific length for filling in.
     * <p>
     * This is a default, null implementation, which must be overridden
     * in an adapter-specific subclass.
     */
    public IEEE802154Message getSerialMessage(int length) {return null;}
    
    // have several debug statements in tight loops, e.g. every character;
    // only want to check once
    protected boolean logDebug = false;

    // The methods to implement the IEEE802154Interface

    public synchronized void addIEEE802154Listener(IEEE802154Listener l) {
        this.addListener(l);
    }

    public synchronized void removeIEEE802154Listener(IEEE802154Listener l) {
        this.removeListener(l);
    }

    protected int enterProgModeDelayTime() {
	// we should to wait at least a second after enabling the programming track
	return 1000;
    }

    /**
     * Forward a IEEE802154Message to all registered IEEE802154Interface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((IEEE802154Listener)client).message((IEEE802154Message)m);
    }

    /**
     * Forward a reply to all registered IEEE802154Interface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((IEEE802154Listener)client).reply((IEEE802154Reply)r);
    }
    
    /**
     * Eventually, do initialization if needed
     */
    protected AbstractMRMessage pollMessage() {
	return null;
    }

    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendIEEE802154Message(IEEE802154Message m, IEEE802154Listener reply) {
        sendMessage(m, reply);
    }

    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (logDebug) log.debug("forward "+m);
        super.forwardToPort(m, reply);
    }
        
    protected AbstractMRMessage enterProgMode() {
        return null;
    }
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
    protected void waitForStartOfReply(java.io.DataInputStream istream) throws java.io.IOException {
       // loop looking for the start character
       while (readByteProtected(istream)!=0x7E) {}
    }
 
    /**
     * <p>
     * The length is the first two bytes of the message payload, 
     * with the MSB first.  The end of the message occurs when
     * the length of the message is equal to the payload length. 
     */
    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
         return ( ((msg.getElement(0)<<8)+msg.getElement(1)+2)== msg.getNumDataElements()); }

    /**
     * Add trailer to the outgoing byte stream.
     * This version adds the checksum to the last byte.
     * @param msg  The output byte stream
     * @param offset the first byte not yet used
     */
    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, jmri.jmrix.AbstractMRMessage m) {
        if(m.getNumDataElements()==0) return;
        ((IEEE802154Message)m).setParity();
    }

    /**
     * Add header to the outgoing byte stream.
     * @param msg  The output byte stream
     * @return next location in the stream to fill
     */
    //protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
    //    m.setElement(0)=0x7E;
    //    return 1;
    //}

    /**
     * <p>
     * This is a default, null implementation, which must be overridden
     * in an adapter-specific subclass.
     */
    //protected AbstractMRReply newReply() {return new IEEE802154Reply(this);}
      
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IEEE802154TrafficController.class.getName());

}


/* @(#)IEEE802154TrafficController.java */
