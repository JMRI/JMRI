package jmri.jmrix.nce;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stands in for the NceTrafficController class.
 *
 * @author	Bob Jacobsen
 */
public class NceTrafficControlScaffold extends NceTrafficController {

    public NceTrafficControlScaffold() {
    }

    // override some NceTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<NceMessage> outbound = new Vector<NceMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendNceMessage(NceMessage m, NceListener reply) {
        if (log.isDebugEnabled()) {
            log.debug("sendNceMessage [" + m + "]");
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
    protected void sendTestMessage(NceMessage m, NceListener l) {
        // forward a test message to NceListeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestMessage    [" + m + "]");
        }
        notifyMessage(m, l);
        return;
    }

    protected void sendTestReply(NceReply m) {
        // forward a test message to Listeners
        notifyReply(m, null);
        return;
   }


    /*
     * Check number of listeners, used for testing dispose()
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    /**
     * Get the port name for this connection from the TrafficController.
     *
     * @return the name of the port
     */
    @Override
    public String getPortName() {
        return jmri.jmrix.JmrixConfigPane.NONE_SELECTED; 
    }

    private final static Logger log = LoggerFactory.getLogger(NceTrafficControlScaffold.class);

}
