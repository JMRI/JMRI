package jmri.jmrix.can;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test scaffold to replace the TrafficController
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 */
public class TestTrafficController extends TrafficController {
    private final static Logger log = LoggerFactory.getLogger(TestTrafficController.class);

    public CanMessage rcvMessage = null;
    public CanReply sndMessage = null;

    @Override
    public void sendCanMessage(CanMessage m, CanListener l) {
        rcvMessage = m;
        log.debug("Message sent: header {} body {}", Integer.toHexString(m.getHeader()), m);
    }

    @Override
    public void sendCanReply(CanReply r, CanListener l) {
        sndMessage = r;
        log.debug("Message sent: header {} body {}", Integer.toHexString(r.getHeader()), r);
    }

    // dummies
    @Override
    public jmri.jmrix.AbstractMRMessage encodeForHardware(CanMessage m) {
        return null;
    }

    @Override
    public CanReply decodeFromHardware(jmri.jmrix.AbstractMRReply r) {
        return null;
    }

    @Override
    public jmri.jmrix.AbstractMRMessage newMessage() {
        return null;
    }

    @Override
    public boolean endOfMessage(jmri.jmrix.AbstractMRReply r) {
        return true;
    }

    @Override
    public jmri.jmrix.AbstractMRReply newReply() {
        return null;
    }

    @Override
    public void forwardReply(jmri.jmrix.AbstractMRListener l, jmri.jmrix.AbstractMRReply r) {
    }

    @Override
    public void forwardMessage(jmri.jmrix.AbstractMRListener l, jmri.jmrix.AbstractMRMessage r) {
    }
}
