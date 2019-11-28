package jmri.jmrix.can;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.can.TrafficControllerScaffold;

/**
 * Stands in for the can.TrafficController class
 * Passes a CanMessage or CanReply to any internal listeners
 * You should normally use TrafficControllerScaffold, not this.
 *
 * @author Steve Young (c) 2019
 */
public class TrafficControllerScaffoldLoopback extends TrafficControllerScaffold {

    public TrafficControllerScaffoldLoopback() {
    }

    // forwards to any listeners
    @Override
    public void forwardMessage(AbstractMRListener l, AbstractMRMessage r) {
        ((CanListener) l).message((CanMessage) r);
    }

    // forwards to any listeners
    @Override
    public void forwardReply(AbstractMRListener l, AbstractMRReply r) {
        ((CanListener) l).reply((CanReply) r);
    }

    @Override
    public void sendCanMessage(CanMessage m, CanListener l) {
        // save a copy
        outbound.addElement(m);
        mLastSender = l;
        notifyMessage(m, l);
    }

    @Override
    public void sendCanReply(CanReply r, CanListener l) {
        // save a copy
        inbound.addElement(r);
        mLastSender = l;
        notifyReply(r, l);
    }

}
