package jmri.jmrix.powerline;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stands in for the SerialTrafficController class
 *
 * @author	Ken Cameron
 * @version	$Revision$ Copied from NceInterfaceScaffold
 *
 */
class SerialInterfaceScaffold extends SerialTrafficController {

    public SerialInterfaceScaffold() {
    }

    // override some SerialInterfaceController methods for test purposes
    public boolean status() {
        return true;
    }

    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<SerialMessage> outbound = new Vector<SerialMessage>();  // public OK here, so long as this is a test class

    public void sendSerialMessage(SerialMessage m, jmri.jmrix.powerline.SerialListener l) {
        if (log.isDebugEnabled()) {
            log.debug("sendSerialMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
        mLastSender = l;
    }

    // test control member functions
    /**
     * forward a message to the listeners, e.g. test receipt
     */
    protected void sendTestMessage(SerialMessage m) {
        // forward a test message to Listeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestMessage    [" + m + "]");
        }
        notifyMessage(m, null);
        return;
    }

    protected void sendTestReply(SerialReply m, jmri.jmrix.powerline.SerialListener l) {
        // forward a test message to Listeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestReply    [" + m + "]");
        }
        notifyReply(m, l);
        return;
    }

    /*
     * Check number of listeners, used for testing dispose()
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialInterfaceScaffold.class.getName());

}
