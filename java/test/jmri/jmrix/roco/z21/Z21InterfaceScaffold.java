package jmri.jmrix.roco.z21;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test scaffold implementation of Z21Interface.
 * <p>
 * Use an object of this type as a Z21TrafficController in tests
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2006
 */
public class Z21InterfaceScaffold extends Z21TrafficController {

    public Z21InterfaceScaffold() {
        super();
    }

    // override some Z21TrafficController methods for test purposes
    @Override
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
    }

    // test control member functions
    /**
     * forward a message to the listeners, e.g.test receipt
     * @param m the message to test
     */
    public void sendTestMessage(Z21Reply m) {
        // forward a test message to Z21Listeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestMessage    [" + m + "]");
        }
        notifyReply(m, null);
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

    /**
     * This is normal, don't log at ERROR level
     */
    @Override 
    protected void reportReceiveLoopException(Exception e) {
        log.debug("run: Exception: {} in {} (considered normal in testing)", e.toString(), this.getClass().toString(), e);
        jmri.jmrix.ConnectionStatus.instance().setConnectionState(controller.getUserName(), controller.getCurrentPortName(), jmri.jmrix.ConnectionStatus.CONNECTION_DOWN);
        if (controller instanceof jmri.jmrix.AbstractNetworkPortController) {
            portWarnTCP(e);
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(Z21InterfaceScaffold.class);

}

