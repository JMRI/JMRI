package jmri.jmrix.roco.z21;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Z21InterfaceScaffold.java
 *
 * Description:	Test scaffold implementation of Z21Interface
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2006
 *
 * Use an object of this type as a Z21TrafficController in tests
 */
public class Z21InterfaceScaffold extends Z21TrafficController {

    public Z21InterfaceScaffold() {
        super();
    }

    // override some Z21TrafficController methods for test purposes
    public boolean status() {
        return true;
    }

    /**
     * record Z21 messages sent, provide access for making sure they are OK
     */
    public Vector<Z21Message> outbound = new Vector<Z21Message>();  // public OK here, so long as this is a test class

    @Override
    public void sendz21Message(Z21Message m, Z21Listener replyTo) {
        if (log.isDebugEnabled()) {
            log.debug("sendZ21Message [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);

        Z21Reply testReply = new Z21Reply();
    }

    // test control member functions
    /**
     * forward a message to the listeners, e.g. test receipt
     */
    public void sendTestMessage(Z21Reply m) {
        // forward a test message to Z21Listeners
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
    @Override
    protected void connectionWarn() {
    }

    /**
     * Avoid error message, normal in parent
     */
    @Override
    protected void portWarn(Exception e) {
    }

    @Override
    public void receiveLoop() {
    }

    private final static Logger log = LoggerFactory.getLogger(Z21InterfaceScaffold.class.getName());

}

