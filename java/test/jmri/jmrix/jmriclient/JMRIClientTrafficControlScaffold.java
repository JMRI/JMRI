/**
 * JMRIClientInterfaceScaffold.java
 *
 * Description:	Stands in for the JMRIClientTrafficController class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.jmriclient;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMRIClientTrafficControlScaffold extends JMRIClientTrafficController {

    public JMRIClientTrafficControlScaffold() {
    }

    // override some JMRIClientTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<JMRIClientMessage> outbound = new Vector<JMRIClientMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendJMRIClientMessage(JMRIClientMessage m, JMRIClientListener reply) {
        if (log.isDebugEnabled()) {
            log.debug("sendJMRIClientMessage [" + m + "]");
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
    protected void sendTestMessage(JMRIClientMessage m, JMRIClientListener l) {
        // forward a test message to NceListeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestMessage    [" + m + "]");
        }
        notifyMessage(m, l);
        return;
    }

    /**
     * forward a message to the listeners, e.g. test receipt
     */
    protected void sendTestReply(JMRIClientReply m) {
        // forward a test message to NceListeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestReply [" + m + "]");
        }
        notifyReply(m,null);
        return;
    }

    /*
     * Check number of listeners, used for testing dispose()
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    private final static Logger log = LoggerFactory.getLogger(JMRIClientTrafficControlScaffold.class);

}
