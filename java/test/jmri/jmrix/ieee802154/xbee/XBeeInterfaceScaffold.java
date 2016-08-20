package jmri.jmrix.ieee802154.xbee;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XBeeInterfaceScaffold.java
 *
 * Description:	Test scaffold implementation of XBeeInterface
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2006
 * @author	Paul Bender Copyright (C) 2016
 *
 * Use an object of this type as a XBeeTrafficController in tests
 */
public class XBeeInterfaceScaffold extends XBeeTrafficController {

    public XBeeInterfaceScaffold() {
        super();
    }

    // override some XBeeTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * record XBee messages sent, provide access for making sure they are OK
     */
    public Vector<XBeeMessage> outbound = new Vector<XBeeMessage>();  // public OK here, so long as this is a test class
 
    @Override
    public void sendXBeeMessage(XBeeMessage m, XBeeListener replyTo) {
        if (log.isDebugEnabled()) {
            log.debug("sendXBeeMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
    }

    // test control member functions
    /**
     * forward a message to the listeners, e.g. test receipt
     */
    public void sendTestMessage(XBeeReply m) {
        // forward a test message to XBeeListeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestMessage    [" + m + "]");
        }
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
     * Avoid error message, normal in parent
     */
    protected void connectionWarn() {
    }

    /**
     * Avoid error message, normal in parent
     */
    protected void portWarn(Exception e) {
    }

    public void receiveLoop() {
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeInterfaceScaffold.class.getName());

}
