// TestTrafficController.java

package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TrafficController;

/**
 * Test scaffold to replace the TrafficController
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version     $Revision: 1.1 $
 */

public class TestTrafficController extends TrafficController {
    jmri.jmrix.can.CanMessage rcvMessage = null;
    public void sendCanMessage(jmri.jmrix.can.CanMessage m,jmri.jmrix.can.CanListener l) {
       rcvMessage = m;   
    }

    // dummies
    public jmri.jmrix.AbstractMRMessage encodeForHardware(jmri.jmrix.can.CanMessage m) { return null; }
    public jmri.jmrix.can.CanReply decodeFromHardware(jmri.jmrix.AbstractMRReply r){ return null; }
    public jmri.jmrix.AbstractMRMessage newMessage() { return null; }
    public boolean endOfMessage(jmri.jmrix.AbstractMRReply r) { return true; }
    public jmri.jmrix.AbstractMRReply newReply() { return null; }
    public void forwardReply(jmri.jmrix.AbstractMRListener l,jmri.jmrix.AbstractMRReply r) {}
    public void forwardMessage(jmri.jmrix.AbstractMRListener l,jmri.jmrix.AbstractMRMessage r) {}
}
