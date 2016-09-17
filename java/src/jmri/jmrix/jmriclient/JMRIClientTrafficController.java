package jmri.jmrix.jmriclient;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

/**
 * Converts Stream-based I/O to/from JMRIClient messages. The
 * "JMRIClientInterface" side sends/receives message objects.
 * <P>
 * The connection to a JMRIClientPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the necessary state in each
 * message.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class JMRIClientTrafficController extends AbstractMRTrafficController
        implements JMRIClientInterface {

    public JMRIClientTrafficController() {
        super();
        setAllowUnexpectedReply(true);
    }

    // The methods to implement the JMRIClientInterface
    public synchronized void addJMRIClientListener(JMRIClientListener l) {
        this.addListener(l);
    }

    public synchronized void removeJMRIClientListener(JMRIClientListener l) {
        this.removeListener(l);
    }

    /**
     * Forward a JMRIClientMessage to all registered JMRIClientInterface
     * listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((JMRIClientListener) client).message((JMRIClientMessage) m);
    }

    /**
     * Forward a JMRIClientReply to all registered JMRIClientInterface
     * listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((JMRIClientListener) client).reply((JMRIClientReply) m);
    }

    protected AbstractMRMessage pollMessage() {
        return null;
    }

    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendJMRIClientMessage(JMRIClientMessage m, JMRIClientListener reply) {
        sendMessage(m, reply);
    }

    protected AbstractMRMessage enterProgMode() {
        return JMRIClientMessage.getProgMode();
    }

    protected AbstractMRMessage enterNormalMode() {
        return JMRIClientMessage.getExitProgMode();
    }

    protected AbstractMRReply newReply() {
        return new JMRIClientReply();
    }

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

    @Override
    @Deprecated
    protected void setInstance() { /*do nothing*/ }

    public JMRIClientTrafficController instance() {
        return this;
    }
}
