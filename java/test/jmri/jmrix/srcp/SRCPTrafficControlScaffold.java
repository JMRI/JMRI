/**
 * SRCPInterfaceScaffold stands in for the SRCPTrafficController class.
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.srcp;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SRCPTrafficControlScaffold extends SRCPTrafficController {

    public SRCPTrafficControlScaffold() {
    }

    // override some SRCPTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<SRCPMessage> outbound = new Vector<SRCPMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
        if (log.isDebugEnabled()) {
            log.debug("sendSRCPMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
        // we don't return an echo so that the processing before the echo can be
        // separately tested
    }

    // test control member functions
    /**
     * Forward a message to the listeners, e.g. test receipt.
     */
    protected void sendTestMessage(SRCPMessage m, SRCPListener l) {
        // forward a test message to NceListeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestMessage    [" + m + "]");
        }
        notifyMessage(m, l);
        return;
    }

    /**
     * Forward a message to the listeners, e.g. test receipt.
     */
    protected void sendTestReply(SRCPReply m) {
        // forward a test message to NceListeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestReply [" + m + "]");
        }
        notifyReply(m,null);
        return;
    }

    /**
     * Check number of listeners, used for testing dispose()
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    private final static Logger log = LoggerFactory.getLogger(SRCPTrafficControlScaffold.class);

}
