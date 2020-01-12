package jmri.jmrix.nce;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stands in for the NceTrafficController class
 *
 * @author	Bob Jacobsen
 */
public class NceInterfaceScaffold extends NceTrafficController {

    public NceInterfaceScaffold() {
        NceSystemConnectionMemo memo = new NceSystemConnectionMemo();
        super.setAdapterMemo(memo);
        memo.setNceTrafficController(this);
    }

    // override some NceInterfaceController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<NceMessage> outbound = new Vector<NceMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendNceMessage(NceMessage m, jmri.jmrix.nce.NceListener l) {
        if (log.isDebugEnabled()) {
            log.debug("sendNceMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
        mLastSender = l;
    }

    // test control member functions
    /**
     * forward a message to the listeners, e.g. test receipt
     */
    public void sendTestMessage(NceMessage m) {
        // forward a test message to Listeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestMessage    [" + m + "]");
        }
        notifyMessage(m, null);
        return;
    }

    public void sendTestReply(NceReply m, NceListener p) {
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

    private final static Logger log = LoggerFactory.getLogger(NceInterfaceScaffold.class);

}
