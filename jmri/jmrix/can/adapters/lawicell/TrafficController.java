// TrafficController.java

package jmri.jmrix.can.adapters.lawicell;

import jmri.jmrix.can.*;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRListener;

/**
 * Traffic controller for the LAWICELL protocol.
 * <P>
 * GridConnect uses messages transmitted
 * as an ASCII string of up to 24 characters of the form:
 *      ;ShhhhNd0d1d2d3d4d5d6d7:
 * The S indicates a standard CAN frame
 * hhhh is the two byte header
 * N or R indicates a normal or remote frame
 * d0 - d7 are the (up to) 8 data bytes
 *
 * @author          Andrew Crosland Copyright (C) 2008
 * @author          Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */
public class TrafficController extends jmri.jmrix.can.TrafficController {
    
    public TrafficController() {
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
        log.debug("TrafficController sendCanMessage() " + m.toString());
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
    static public jmri.jmrix.can.TrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new TrafficController object");
            self = new TrafficController();
        }
        return self;
    }

    // New message for hardware protocol
    protected AbstractMRMessage newMessage() { 
        log.debug("New Message created");
        Message msg = new Message();
        return msg;
    }

    /** 
     * Make a CanReply from a system-specific reply
     */
    public CanReply decodeFromHardware(AbstractMRReply m) {
        if (log.isDebugEnabled()) log.debug("Decoding from hardware: '"+m+"'\n");
	    Reply gc = (Reply)m;
        CanReply ret = new CanReply();

	    // Get the ID
        ret.setId(gc.getID());
        
        // Get the data
        System.out.println("1");
        System.out.println(" byte 0: "+gc.getElement(0));
        System.out.println(" byte 1: "+gc.getElement(1));
        System.out.println(" byte 2: "+gc.getElement(2));
        System.out.println(" byte 3: "+gc.getElement(3));
        System.out.println(" byte 4: "+gc.getElement(4));
        System.out.println(" byte 5: "+gc.getElement(5));
        
        for (int i = 0; i < gc.getNumBytes(); i++) {
            System.out.println("2 "+i+" "+gc.getByte(i));
            ret.setElement(i, gc.getByte(i));
        }
        System.out.println("3");
        ret.setNumDataElements(gc.getNumBytes());
        System.out.println("4");
        if (log.isDebugEnabled()) log.debug("Decoded as "+ret);
        System.out.println("ret "+ret);
        
        return ret;
    }

    /**
     * Encode a CanMessage for the hardware
     */
    public AbstractMRMessage encodeForHardware(CanMessage m) {
        log.debug("Encoding for hardware");
	    Message ret = new Message();
	    int index = 0;
        // Standard frame?
        boolean extended = m.isExtended();
        if (extended) {
            // extended
            ret.setElement(index++, 'T');  
        } else {
             // standard
            ret.setElement(index++, 't'); 
        }
        // CAN ID
        index = ret.setID(m.getId(), extended, index);
        // length
        ret.setHexDigit(m.getNumDataElements(), index++);
        // Data payload
        for (int i = 0 ; i < m.getNumDataElements(); i++) {
            ret.setHexDigit((m.getElement(i)>>4)&0x0F, index++);
            ret.setHexDigit(m.getElement(i)&0x0F, index++);
        }
        // Terminator
        ret.setElement(index++, 0x0D);
        ret.setNumDataElements(index);
        if (log.isDebugEnabled()) log.debug("encoded as "+ret);
        return ret;
    }

    // New reply from hardware
    protected AbstractMRReply newReply() { 
        log.debug("New Reply created");
        Reply reply = new Reply();
        return reply;
    }
    
    /*
     * Normal CAN-RS replies will end with ":"
     * Bootloader will end with ETX with no preceding DLE
     */
    protected boolean endOfMessage(AbstractMRReply r) {
        if (endNormalReply(r)) return true;
//        if (endBootReply(r)) return true;
        return false;
    }
    
    boolean endNormalReply(AbstractMRReply r) {
        System.out.println("check endNormalReply "+r);
        // Detect if the reply buffer ends with bell or cr
        int num = r.getNumDataElements() - 1;
        System.out.println("check endNormalReply "+r+"\n index "+num+" last "+r.getElement(num));
        if (r.getElement(num) == 0x0D) return true;
        if (r.getElement(num) == 0x07) return true;
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
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TrafficController.class.getName());
}


/* @(#)TrafficController.java */

