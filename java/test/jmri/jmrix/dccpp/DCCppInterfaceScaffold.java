package jmri.jmrix.dccpp;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCCppInterfaceScaffold.java
 *
 * Description:	Test scaffold implementation of DCCppInterface
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2006
 * @author	Mark Underwood Copyright (C) 2015
  *
 * Use an object of this type as a DCCppTrafficController in tests
 */
public class DCCppInterfaceScaffold extends DCCppTrafficController {

    public DCCppInterfaceScaffold(DCCppCommandStation pCommandStation) {
        super(pCommandStation);
    }

    // override some DCCppTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * record DCC++ messages sent, provide access for making sure they are OK
     */
    public Vector<DCCppMessage> outbound = new Vector<DCCppMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendDCCppMessage(DCCppMessage m, DCCppListener replyTo) {
        if (log.isDebugEnabled()) {
            log.debug("sendDCCppMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
    }

    @Override
    public void sendHighPriorityDCCppMessage(DCCppMessage m, DCCppListener replyTo) {
        if (log.isDebugEnabled()) {
            log.debug("sendDCCppMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
    }

    // test control member functions
    /**
     * forward a message to the listeners, e.g. test receipt
     */
    public void sendTestMessage(DCCppReply m) {
        // forward a test message to DCCppListeners
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

    private final static Logger log = LoggerFactory.getLogger(DCCppInterfaceScaffold.class);

}



