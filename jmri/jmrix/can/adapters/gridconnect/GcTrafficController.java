// GcTrafficController.java

package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.can.*;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRListener;

import jmri.jmrix.can.TrafficController;

/**
 * Traffic controller for the GridConnect protocol.
 * <P>
 * GridConnect uses messages transmitted
 * as an ASCII string of up to 24 characters of the form:
 *      :ShhhhNd0d1d2d3d4d5d6d7;
 * The S indicates a standard CAN frame
 * hhhh is the two byte header
 * N or R indicates a normal or remote frame
 * d0 - d7 are the (up to) 8 data bytes
 *
 * @author                      Andrew Crosland Copyright (C) 2008
 * @version			$Revision: 1.4 $
 */
public class GcTrafficController extends TrafficController {
    
    public GcTrafficController() {
        super();
    }
   
    /**
     * Forward a CanMessage to all registered CanInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((CanListener)client).message((CanMessage)m);
    }

    /**
     * Forward a CanReply to all registered CanInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((CanListener)client).reply((CanReply)r);
    }
    
    // Current state
    public static final int NORMAL = 0;
    public static final int BOOTMODE = 1;
    
    public int getgcState() { return gcState; }
    public void setgcState(int s) {
        gcState = s;
        if (log.isDebugEnabled()) log.debug("Setting gcState " + s);
    }
    public boolean isBootMode() {return gcState == BOOTMODE; }
    
    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendCanMessage(CanMessage m, CanListener reply) {
        log.debug("GcTrafficController sendCanMessage() " + m.toString());
        sendMessage(m, reply);
    }

    /**
     * Add trailer to the outgoing byte stream.
     * @param msg  The output byte stream
     * @param offset the first byte not yet used
     */
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
        return;
    }

    /**
     * Determine how much many bytes the entire
     * message will take, including space for header and trailer
     * @param m  The message to be sent
     * @return Number of bytes
     */
    protected int lengthOfByteStream(AbstractMRMessage m) {
        return m.getNumDataElements();
    }

    /**
     * static function returning the CanTrafficController instance to use.
     * @return The registered SprogTrafficController instance for general use,
     *         if need be creating one.
     */
    static public TrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new GcTrafficController object");
            self = new GcTrafficController();
        }
        return self;
    }

    // New message for hardware protocol
    protected AbstractMRMessage newMessage() { 
        log.debug("New GridConnectMessage created");
        GridConnectMessage msg = new GridConnectMessage();
        return msg;
    }

    /** 
     * Make a CanReply from a GridConnect reply
     */
    public CanReply decodeFromHardware(AbstractMRReply m) {
        log.warn("Decoding from hardware");
	GridConnectReply gc = (GridConnectReply)m;
        CanReply ret = new CanReply();

	// Get the Priority
        ret.setPri(gc.getPri());
	// and ID
        ret.setId(gc.getID());
        // Is it an RTR frame
	if (gc.getElement(6) == 'R') ret.setRtr(true);
        // Get the data
        for (int i = 0; i < gc.getNumBytes(); i++) {
            ret.setElement(i, gc.getByte(i));
        }
        ret.setNumDataElements(gc.getNumBytes());
        return ret;
    }

    /**
     * Encode a CanMessage for the hardware
     */
    public AbstractMRMessage encodeForHardware(CanMessage m) {
        log.debug("Encoding for hardware");
	GridConnectMessage ret = new GridConnectMessage();
        // Prefix
        ret.setElement(0, ':');
        // Standard frame
        ret.setElement(1, 'S');
        // CBUS Priority
        ret.setPri(m.getPri());
        // CBUS ID 
        ret.setID(m.getId());
        // Normal or Remote frame?
        ret.setElement(6, m.isRtr() ? 'R' : 'N');
        // Data payload
        for (int i = 0 ; i < m.getNumDataElements(); i++) {
            ret.setByte(m.getElement(i), i);
        }
        // Terminator
        ret.setElement(7 + m.getNumDataElements()*2, ';');
        ret.setNumDataElements(8 + m.getNumDataElements()*2);
        if (log.isDebugEnabled()) log.debug("encoded as "+ret);
        return ret;
    }

    // New reply from hardware
    protected AbstractMRReply newReply() { 
        log.debug("New GridConnectReply created");
        GridConnectReply reply = new GridConnectReply();
        return reply;
    }
    
    /*
     * Normal CAN-RS replies will end with ";"
     * Bootloader will end with ETX with no preceding DLE
     */
    protected boolean endOfMessage(AbstractMRReply r) {
        if (endNormalReply(r)) return true;
//        if (endBootReply(r)) return true;
        return false;
    }
    
    boolean endNormalReply(AbstractMRReply r) {
        // Detect if the reply buffer ends with ";"
        int num = r.getNumDataElements() - 1;
        log.debug("endNormalReply checking "+(num+1)+" of "+(r.getNumDataElements()));
        if (r.getElement(num) == ';') {
            log.debug("End of normal message detected");
            return true;
        }
        return false;
    }

//    boolean endBootReply(CanReply msg) {
//        // Detect that the reply buffer ends with ETX with no preceding DLE
//        // This is the end of a CAN-RS bootloader reply
//        int num = msg.getNumDataElements();
//        if ( num >= 2) {
//            // ptr is offset of last element in CanrsReply
//            int ptr = num-1;
//            if ((int)(msg.getElement(ptr) & 0xff)   != CanrsMessage.ETX) return false;
//            if ((int)(msg.getElement(ptr-1) & 0xff) == CanrsMessage.DLE) return false;
//            return true;
//        } else return false;
//    }
    
    private boolean unsolicited;
    private int gcState;
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(GcTrafficController.class.getName());
}


/* @(#)GcTrafficController.java */

