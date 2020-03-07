package jmri.jmrix.qsi;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stands in for the QsiTrafficController class
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class QsiTrafficControlScaffold extends QsiTrafficController {

    public QsiTrafficControlScaffold() {
    }

    // override some QsiTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<QsiMessage> outbound = new Vector<QsiMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendQsiMessage(QsiMessage m, QsiListener reply) {
        if (log.isDebugEnabled()) {
            log.debug("sendQsiMessage [" + m + "]");
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
    protected void sendTestMessage(QsiMessage m, QsiListener l) {
        // forward a test message to QsiListeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestMessage    [" + m + "]");
        }
        notifyMessage(m, l);
    }

    /*
     * Check number of listeners, used for testing dispose()
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    private final static Logger log = LoggerFactory.getLogger(QsiTrafficControlScaffold.class);

}
