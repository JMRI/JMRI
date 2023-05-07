package jmri.jmrix.can;

import java.util.List;
import java.util.Vector;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stands in for the can.TrafficController class
 *
 * @author Bob Jacobsen 2008
 */
public class TrafficControllerScaffold extends TrafficController {

    public TrafficControllerScaffold() {
    }

    // non-functional dummy
    @Override
    public AbstractMRMessage encodeForHardware(CanMessage m) {
        return null;
    }

    // non-functional dummy
    @Override
    public CanReply decodeFromHardware(AbstractMRReply r) {
        return null;
    }

    // non-functional dummy
    @Override
    public AbstractMRMessage newMessage() {
        return null;
    }

    // non-functional dummy
    @Override
    public AbstractMRReply newReply() {
        return null;
    }

    // non-functional dummy
    @Override
    public boolean endOfMessage(AbstractMRReply r) {
        return false;
    }

    // non-functional dummy
    @Override
    public void forwardMessage(AbstractMRListener l, AbstractMRMessage r) {
    }

    // non-functional dummy
    @Override
    public void forwardReply(AbstractMRListener l, AbstractMRReply r) {
    }

    // override some methods for test purposes
    //public boolean status() { return true;
    //}
    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<CanMessage> outbound = new Vector<>();  // public OK here, so long as this is a test class
    public Vector<CanReply> inbound = new Vector<>();  // public OK here, so long as this is a test class

    @Override
    public void sendCanMessage(CanMessage m, CanListener l) {
        log.debug("sendCanMessage [{}]", m);
        // save a copy
        outbound.addElement(m);
        mLastSender = l;
    }

    @Override
    public void sendCanReply(CanReply r, CanListener l) {
        log.debug("sendCanReply [{}]", r);
        // save a copy
        inbound.addElement(r);
    }

    /*
     * Check number of listeners, used for testing dispose()
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    /**
     * Get List of Listeners.
     * @return List of CAN Listeners.
     */
    public List<AbstractMRListener> getListeners() {
        return cmdListeners;
    }

    private final static Logger log = LoggerFactory.getLogger(TrafficControllerScaffold.class);

}
