/**
 * XpaInterfaceScaffold.java
 *
 * Description:	Stands in for the XpaTrafficController class
 *
 * @author	Bob Jacobsen
  */
package jmri.jmrix.xpa;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XpaTrafficControlScaffold extends XpaTrafficController {

    public XpaTrafficControlScaffold() {
        if (log.isDebugEnabled()) {
            log.debug("setting instance: " + this);
        }
    }

    // override some XpaTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<XpaMessage> outbound = new Vector<XpaMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendXpaMessage(XpaMessage m, XpaListener reply) {
        if (log.isDebugEnabled()) {
            log.debug("sendXpaMessage [" + m + "]");
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
    protected void sendTestMessage(XpaMessage m, XpaListener l) {
        // forward a test message to NceListeners
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

    private final static Logger log = LoggerFactory.getLogger(XpaTrafficControlScaffold.class.getName());

}
