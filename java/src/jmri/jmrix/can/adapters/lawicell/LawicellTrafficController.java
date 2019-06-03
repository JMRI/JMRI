package jmri.jmrix.can.adapters.lawicell;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traffic controller for the LAWICELL protocol.
 * <p>
 * Lawicell adapters use messages transmitted as an ASCII string of up to 24
 * characters of the form: ;ShhhhNd0d1d2d3d4d5d6d7: The S indicates a standard
 * CAN frame hhhh is the two byte header N or R indicates a normal or remote
 * frame d0 - d7 are the (up to) 8 data bytes
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class LawicellTrafficController extends jmri.jmrix.can.TrafficController {

    public LawicellTrafficController() {
        super();
    }

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

    // Current state
    public static final int NORMAL = 0;
    public static final int BOOTMODE = 1;

    public int getgcState() {
        return gcState;
    }

    public void setgcState(int s) {
        gcState = s;
        log.debug("Setting gcState {}",s);
    }

    public boolean isBootMode() {
        return gcState == BOOTMODE;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    @Override
    public void sendCanMessage(CanMessage m, CanListener reply) {
        log.debug("TrafficController sendCanMessage() {}", m.toString());
        sendMessage(m, reply);
    }

    /**
     * Forward a preformatted reply to the actual interface.
     */
    @Override
    public void sendCanReply(CanReply r, CanListener reply) {
        log.debug("TrafficController sendCanReply() {}", r.toString());
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
     * Determine how much many bytes the entire message will take, including
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
        log.debug("New Message created");
        Message msg = new Message();
        return msg;
    }

    /**
     * Make a CanReply from a system-specific reply
     */
    @Override
    public CanReply decodeFromHardware(AbstractMRReply m) {
        log.debug("Decoding from hardware: {}", m );
        CanReply ret = new CanReply();
        try {
            Reply gc = (Reply) m;
            ret = gc.createReply();
            log.debug("Decoded {} as {} ",gc,ret);
        } catch (java.lang.ClassCastException cce){
                log.error("Unable to cast Reply {}",m);
        }
        return ret;
    }

    /**
     * Encode a CanMessage for the hardware
     */
    @Override
    public AbstractMRMessage encodeForHardware(CanMessage m) {
        Message ret = new Message(m);
        log.debug("Encoding for hardware as {}",ret);
        return ret;
    }

    // New reply from hardware
    @Override
    protected AbstractMRReply newReply() {
        log.debug("New Reply created");
        Reply reply = new Reply();
        return reply;
    }

    /*
     * Normal Lawicall replies will end with CR; errors are BELL
     */
    @Override
    protected boolean endOfMessage(AbstractMRReply r) {
        if (endNormalReply(r)) {
            return true;
        }
        return false;
    }

    boolean endNormalReply(AbstractMRReply r) {
        // Detect if the reply buffer ends with bell or cr
        int num = r.getNumDataElements() - 1;
        if (r.getElement(num) == 0x0D) {
            return true;
        }
        if (r.getElement(num) == 0x07) {
            return true;
        }
        return false;
    }

    private int gcState;

    private final static Logger log = LoggerFactory.getLogger(LawicellTrafficController.class);
}
