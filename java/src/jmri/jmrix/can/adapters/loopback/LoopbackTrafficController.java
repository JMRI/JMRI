// LoopbackTrafficController.java

package jmri.jmrix.can.adapters.loopback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.can.*;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRListener;

/**
 * Traffic controller for loopback CAN simulation.
 *
 * @author          Bob Jacobsen Copyright (C) 2008
 * @version			$Revision$
 */
public class LoopbackTrafficController extends jmri.jmrix.can.TrafficController {
    
    public LoopbackTrafficController() {
        super();
    }
    
    protected jmri.jmrix.can.CanSystemConnectionMemo adaptermemo;
   
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
        
    public boolean isBootMode() {return false; }
    
    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendCanMessage(CanMessage m, CanListener reply) {
        log.debug("TrafficController sendCanMessage() " + m.toString());
        notifyMessage(m, reply);
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

    // New message for hardware protocol
    protected AbstractMRMessage newMessage() { 
        log.debug("New CanMessage created");
        CanMessage msg = new CanMessage(getCanid());
        return msg;
    }

    /** 
     * Make a CanReply from a system-specific reply
     */
    public CanReply decodeFromHardware(AbstractMRReply m) {
        log.error("decodeFromHardware unexpected");
        return null;

/*         if (log.isDebugEnabled()) log.debug("Decoding from hardware: '"+m+"'\n"); */
/* 	    CanReply gc = (CanReply)m; */
/*         CanReply ret = new CanReply(); */
/*  */
/* 	    // Get the ID */
/*         ret.setId(gc.getID()); */
/*          */
/*         // Get the data */
/*         for (int i = 0; i < gc.getNumBytes(); i++) { */
/*             ret.setElement(i, gc.getByte(i)); */
/*         } */
/*         ret.setNumDataElements(gc.getNumBytes()); */
/*         if (log.isDebugEnabled()) log.debug("Decoded as "+ret); */
/*          */
/*         return ret; */
    }

    /**
     * Encode a CanMessage for the hardware
     */
    public AbstractMRMessage encodeForHardware(CanMessage m) {
        log.error("encodeForHardware unexpected");
        return null;
    }

    // New reply from hardware
    protected AbstractMRReply newReply() { 
        log.debug("New CanReply created");
        CanReply reply = new CanReply();
        return reply;
    }
    
    /*
     * Dummy; lookback doesn't parse serial messages
     */
    protected boolean endOfMessage(AbstractMRReply r) {
        log.error("endNormalReply unexpected");
        return true;
    }
    
    /*
     * Dummy; lookback doesn't parse serial messages
     */
    boolean endNormalReply(AbstractMRReply r) {
        log.error("endNormalReply unexpected");
        return true;
    }
        
    static Logger log = LoggerFactory.getLogger(LoopbackTrafficController.class.getName());
}


/* @(#)LoopbackTrafficController.java */

