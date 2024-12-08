package jmri.jmrix.marklin.simulation;

import jmri.jmrix.marklin.MarklinListener;
import jmri.jmrix.marklin.MarklinMessage;
import jmri.jmrix.marklin.MarklinReply;

/**
 * Traffic Controller for Simulated Marklin connections.
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinSimTrafficController extends jmri.jmrix.marklin.MarklinTrafficController {

    @Override
    protected void forwardMessage(jmri.jmrix.AbstractMRListener client, jmri.jmrix.AbstractMRMessage m) {
        ((MarklinListener) client).message((MarklinMessage) m);
    }

    @Override
    protected void forwardReply(jmri.jmrix.AbstractMRListener client, jmri.jmrix.AbstractMRReply r) {
        ((MarklinListener) client).reply( (MarklinReply) r);
    }

    @Override
    public void sendMarklinMessage(MarklinMessage m, MarklinListener l ) {
        notifyMessage(m, l);
    }

    @Override
    public String getUserName() {
        return ( adaptermemo != null ? adaptermemo.getUserName() : "Marklin Network Simulation");
    }

}
