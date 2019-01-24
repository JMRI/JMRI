package jmri.jmrix.loconet;

import jmri.jmrix.loconet.LocoNetListener;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test scaffold implementation of LocoNetInterface.
 * Use an object of this type as a LnTrafficController in tests.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2006
 */
public class LocoNetInterfaceScaffold extends LnTrafficController {

    public LocoNetInterfaceScaffold() {
    }

    public LocoNetInterfaceScaffold(LocoNetSystemConnectionMemo adaptermemo) {
        super(adaptermemo);
    }

    // override some LnTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * Record LocoNet messages sent, provide access for making sure they are OK.
     */
    public Vector<LocoNetMessage> outbound = new Vector<LocoNetMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendLocoNetMessage(LocoNetMessage m) {
        log.debug("sendLocoNetMessage [{}]", m);
        // save a copy
        outbound.addElement(m);
        // we don't return an echo so that the processing before the echo can be
        // separately tested
    }

    @Override
    public boolean isXmtBusy() {
        return false;
    }

    // test control member functions

    /**
     * Forward a message that came from unit under test.
     */
    void forwardMessage(int i) {
        sendTestMessage(outbound.elementAt(i));
    }

    /**
     * Forward a message to the listeners, e.g. test receipt
     */
    public void sendTestMessage(LocoNetMessage m) {
        // forward a test message to LocoNetListeners
        log.debug("sendTestMessage    [{}]", m);
        notify(m);
        return;
    }

    /**
     * Check number of listeners, used for testing dispose().
     */
    public int numListeners() {
        return listeners.size();
    }

    /**
     * Get listeners, used for testing dispose().
     */
    public Vector<LocoNetListener> getListeners() {
        return listeners;
    }

    private final static Logger log = LoggerFactory.getLogger(LocoNetInterfaceScaffold.class);

}
