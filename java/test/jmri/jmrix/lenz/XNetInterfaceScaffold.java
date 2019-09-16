package jmri.jmrix.lenz;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test scaffold implementation of XNetInterface.
 * <p>
 * Use an object of this type as a XNetTrafficController in tests.
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2006
 */
public class XNetInterfaceScaffold extends XNetTrafficController {

    public XNetInterfaceScaffold(LenzCommandStation pCommandStation) {
        super(pCommandStation);
        setSystemConnectionMemo(new XNetSystemConnectionMemo(this));
    }

    // override some XNetTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * Record XNet messages sent, provide access for making sure they are OK.
     */
    public Vector<XNetMessage> outbound = new Vector<XNetMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendXNetMessage(XNetMessage m, XNetListener replyTo) {
        log.debug("sendXNetMessage [{}]", m);
        // save a copy
        outbound.addElement(m);
    }

    @Override
    public void sendHighPriorityXNetMessage(XNetMessage m, XNetListener replyTo) {
        log.debug("sendXNetMessage [{}]", m);
        // save a copy
        outbound.addElement(m);
    }

    // test control member functions

    /**
     * Forward a message to the listeners, e.g. test receipt.
     */
    public void sendTestMessage(XNetReply m) {
        // forward a test message to XNetListeners
        log.debug("sendTestMessage    [{}]", m);
        notifyReply(m, null);
        return;
    }

    /*
     * Check number of listeners, used for testing dispose().
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    /**
     * Avoid error message, normal in parent.
     */
    @Override
    protected void connectionWarn() {
    }

    /**
     * Avoid error message, normal in parent.
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

    private final static Logger log = LoggerFactory.getLogger(XNetInterfaceScaffold.class);

}
