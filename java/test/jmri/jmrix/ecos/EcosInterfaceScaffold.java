package jmri.jmrix.ecos;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EcosInterfaceScaffold.java
 *
 * Description:	Test scaffold implementation of EcosInterface
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2006
  *
 * Use an object of this type as a EcosTrafficController in tests
 */
public class EcosInterfaceScaffold extends EcosTrafficController {

    public EcosInterfaceScaffold() {
    }

    // override some EcosTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * record Ecos messages sent, provide access for making sure they are OK
     */
    public Vector<EcosMessage> outbound = new Vector<EcosMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendEcosMessage(EcosMessage m, EcosListener replyTo) {
        if (log.isDebugEnabled()) {
            log.debug("sendEcosMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
    }

    // test control member functions
    /**
     * forward a message to the listeners, e.g. test receipt
     */
    public void sendTestMessage(EcosReply m) {
        // forward a test message to EcosListeners
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

    private final static Logger log = LoggerFactory.getLogger(EcosInterfaceScaffold.class);

}



