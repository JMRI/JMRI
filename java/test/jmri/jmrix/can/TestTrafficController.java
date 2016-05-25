package jmri.jmrix.can;

/**
 * Test scaffold to replace the TrafficController
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 */
public class TestTrafficController extends TrafficController {

    public CanMessage rcvMessage = null;

    public void sendCanMessage(CanMessage m, CanListener l) {
        rcvMessage = m;
    }

    // dummies
    public jmri.jmrix.AbstractMRMessage encodeForHardware(CanMessage m) {
        return null;
    }

    public CanReply decodeFromHardware(jmri.jmrix.AbstractMRReply r) {
        return null;
    }

    public jmri.jmrix.AbstractMRMessage newMessage() {
        return null;
    }

    public boolean endOfMessage(jmri.jmrix.AbstractMRReply r) {
        return true;
    }

    public jmri.jmrix.AbstractMRReply newReply() {
        return null;
    }

    public void forwardReply(jmri.jmrix.AbstractMRListener l, jmri.jmrix.AbstractMRReply r) {
    }

    public void forwardMessage(jmri.jmrix.AbstractMRListener l, jmri.jmrix.AbstractMRMessage r) {
    }
}
