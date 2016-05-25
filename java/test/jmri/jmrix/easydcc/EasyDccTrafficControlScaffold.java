package jmri.jmrix.easydcc;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stands in for the EasyDccTrafficController class
 *
 * @author	Bob Jacobsen Copyright 2006
 * @version
 */
public class EasyDccTrafficControlScaffold extends EasyDccTrafficController {

    public EasyDccTrafficControlScaffold() {
        if (log.isDebugEnabled()) {
            log.debug("setting instance: " + this);
        }
        self = this;
    }

    // override some EasyDccTrafficController methods for test purposes
    public boolean status() {
        return true;
    }

    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<EasyDccMessage> outbound = new Vector<EasyDccMessage>();  // public OK here, so long as this is a test class

    public void sendEasyDccMessage(EasyDccMessage m, EasyDccListener reply) {
        if (log.isDebugEnabled()) {
            log.debug("sendEasyDccMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
        // we don't return an echo so that the processing before the echo can be
        // separately tested
    }

    // test control member functions
    /**
     * forward a message to the listeners, e.g. test receipt
     */
    protected void sendTestMessage(EasyDccMessage m, EasyDccListener l) {
        // forward a test message to EasyDccListeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestMessage    [" + m + "]");
        }
        notifyMessage(m, l);
        return;
    }

    /*
     * Check number of listeners, used for testing dispose()
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccTrafficControlScaffold.class.getName());

}
