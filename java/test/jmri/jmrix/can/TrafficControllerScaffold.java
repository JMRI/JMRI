package jmri.jmrix.can;

import java.util.Vector;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stands in for the can.TrafficController class
 *
 * @author	Bob Jacobsen 2008
 * @version	$Revision$
 */
public class TrafficControllerScaffold extends TrafficController {

    public TrafficControllerScaffold() {
    }

    static protected TrafficControllerScaffold self = null;

    static public TrafficControllerScaffold instance() {
        if (self == null) {
            if (log.isDebugEnabled()) {
                log.debug("creating a new TrafficControllerScaffold object");
            }
            self = new TrafficControllerScaffold();
        }
        return self;
    }

    // non-functional dummy
    public AbstractMRMessage encodeForHardware(CanMessage m) {
        return null;
    }

    // non-functional dummy
    public CanReply decodeFromHardware(AbstractMRReply r) {
        return null;
    }

    // non-functional dummy
    public AbstractMRMessage newMessage() {
        return null;
    }

    // non-functional dummy
    public AbstractMRReply newReply() {
        return null;
    }

    // non-functional dummy
    public boolean endOfMessage(AbstractMRReply r) {
        return false;
    }

    // non-functional dummy
    public void forwardMessage(AbstractMRListener l, AbstractMRMessage r) {
    }

    // non-functional dummy
    public void forwardReply(AbstractMRListener l, AbstractMRReply r) {
    }

    // override some methods for test purposes
    //public boolean status() { return true;
    //}
    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<CanMessage> outbound = new Vector<CanMessage>();  // public OK here, so long as this is a test class

    public void sendCanMessage(CanMessage m, CanListener l) {
        if (log.isDebugEnabled()) {
            log.debug("sendCanMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
        mLastSender = l;
    }

    /*
     * Check number of listeners, used for testing dispose()
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    private final static Logger log = LoggerFactory.getLogger(TrafficControllerScaffold.class.getName());

}
