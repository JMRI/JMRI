package jmri.jmrix.jmriclient;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

/**
 * Converts Stream-based I/O to/from JMRIClient messages. The
 * "JMRIClientInterface" side sends/receives message objects.
 * <p>
 * The connection to a JMRIClientPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <p>
 * This handles the state transitions, based on the necessary state in each
 * message.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class JMRIClientTrafficController extends AbstractMRTrafficController
        implements JMRIClientInterface {

    /**
     * Create a new JMRIClientTrafficController instance.
     */
    public JMRIClientTrafficController() {
        super();
        setAllowUnexpectedReply(true);
    }

    // The methods to implement the JMRIClientInterface
    @Override
    public synchronized void addJMRIClientListener(JMRIClientListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeJMRIClientListener(JMRIClientListener l) {
        this.removeListener(l);
    }

    /**
     * Forward a JMRIClientMessage to all registered JMRIClientInterface
     * listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((JMRIClientListener) client).message((JMRIClientMessage) m);
    }

    /**
     * Forward a JMRIClientReply to all registered JMRIClientInterface
     * listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((JMRIClientListener) client).reply((JMRIClientReply) m);
    }

    @Override
    protected AbstractMRMessage pollMessage() {
        return null;
    }

    @Override
    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    @Override
    public void sendJMRIClientMessage(JMRIClientMessage m, JMRIClientListener reply) {
        sendMessage(m, reply);
    }

    @Override
    protected AbstractMRMessage enterProgMode() {
        return JMRIClientMessage.getProgMode();
    }

    @Override
    protected AbstractMRMessage enterNormalMode() {
        return JMRIClientMessage.getExitProgMode();
    }

    @Override
    protected AbstractMRReply newReply() {
        return new JMRIClientReply();
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        int index = msg.getNumDataElements() - 1;
        if (msg.getElement(index) == 0x0D) {
            return true;
        }
        if (msg.getElement(index) == 0x0A) {
            return true;
        } else {
            return false;
        }
    }
}
