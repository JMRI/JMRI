package jmri.jmrix.tams;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stands in for the TamsTrafficController class
 *
 * @author	Bob Jacobsen
 */
public class TamsInterfaceScaffold extends TamsTrafficController {

    public TamsInterfaceScaffold() {
    }

    // override some TamsInterfaceController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<TamsMessage> outbound = new Vector<TamsMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendTamsMessage(TamsMessage m, TamsListener l) {
        if (log.isDebugEnabled()) {
            log.debug("sendTamsMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
        mLastSender = l;
    }

    // test control member functions
    /**
     * forward a message to the listeners, e.g. test receipt
     */
    protected void sendTestMessage(TamsMessage m) {
        // forward a test message to Listeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestMessage    [" + m + "]");
        }
        notifyMessage(m, null);
        return;
    }

    protected void sendTestReply(TamsReply m, TamsProgrammer p) {
        // forward a test message to Listeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestReply    [" + m + "]");
        }
        notifyReply(m, p);
        return;
    }

    /*
     * Check number of listeners, used for testing dispose()
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    private final static Logger log = LoggerFactory.getLogger(TamsInterfaceScaffold.class);

}
