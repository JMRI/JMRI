package jmri.jmrix.can.adapters.loopback;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traffic controller for loopback CAN simulation.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class LoopbackTrafficController extends jmri.jmrix.can.TrafficController {

    public LoopbackTrafficController() {
        super();
    }

    protected jmri.jmrix.can.CanSystemConnectionMemo adaptermemo;

    /**
     * Forward a CanMessage to all registered CanInterface listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((CanListener) client).message((CanMessage) m);
    }

    /**
     * Forward a CanReply to all registered CanInterface listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((CanListener) client).reply((CanReply) r);
    }

    public boolean isBootMode() {
        return false;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    @Override
    public void sendCanMessage(CanMessage m, CanListener reply) {
        log.debug("TrafficController sendCanMessage() " + m.toString());
        notifyMessage(m, reply);
    }

    /**
     * Forward a preformatted reply to the actual interface.
     */
    @Override
    public void sendCanReply(CanReply r, CanListener reply) {
        log.debug("TrafficController sendCanReply() " + r.toString());
        notifyReply(r, reply);
    }

    /**
     * Add trailer to the outgoing byte stream.
     *
     * @param msg    The output byte stream
     * @param offset the first byte not yet used
     */
    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
        return;
    }

    /**
     * Determine how many bytes the entire message will take, including
     * space for header and trailer
     *
     * @param m The message to be sent
     * @return Number of bytes
     */
    @Override
    protected int lengthOfByteStream(AbstractMRMessage m) {
        return m.getNumDataElements();
    }

    // New message for hardware protocol
    @Override
    protected AbstractMRMessage newMessage() {
        log.debug("New CanMessage created");
        CanMessage msg = new CanMessage(getCanid());
        return msg;
    }

    /**
     * Make a CanReply from a system-specific reply
     */
    @Override
    public CanReply decodeFromHardware(AbstractMRReply m) {
        log.error("decodeFromHardware unexpected");
        return null;

        /*         if (log.isDebugEnabled()) log.debug("Decoding from hardware: '"+m+"'\n"); */
        /*      CanReply gc = (CanReply)m; */
        /*         CanReply ret = new CanReply(); */
        /*  */
        /*      // Get the ID */
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
    @Override
    public AbstractMRMessage encodeForHardware(CanMessage m) {
        log.error("encodeForHardware unexpected");
        return null;
    }

    // New reply from hardware
    @Override
    protected AbstractMRReply newReply() {
        log.debug("New CanReply created");
        CanReply reply = new CanReply();
        return reply;
    }

    /*
     * Dummy; loopback doesn't parse serial messages
     */
    @Override
    protected boolean endOfMessage(AbstractMRReply r) {
        log.error("endNormalReply unexpected");
        return true;
    }

    /*
     * Dummy; loopback doesn't parse serial messages
     */
    boolean endNormalReply(AbstractMRReply r) {
        log.error("endNormalReply unexpected");
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(LoopbackTrafficController.class);

}
