package jmri.jmrix.can;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for TrafficControllers in a CANbus based Message/Reply
 * protocol.
 * <p>
 * AbstractMRTrafficController is extended to allow for the translation between
 * CAN messages and the message format of the CAN adapter that connects to the
 * layout.
 *
 * @author Andrew Crosland Copyright (C) 2008
 */
abstract public class AbstractCanTrafficController
        extends AbstractMRTrafficController
        implements CanInterface {

    public AbstractCanTrafficController() {
        super();
        allowUnexpectedReply = true;
    }

    // The methods to implement the CAN Interface
    @Override
    public synchronized void addCanListener(CanListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeCanListener(CanListener l) {
        this.removeListener(l);
    }

    /**
     * Actually transmits the next message to the port
     *
     * Overridden to include translation to the correct CAN hardware message
     * format
     */
    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
//        if (log.isDebugEnabled()) log.debug("forwardToPort message: ["+m+"]");
        log.debug("forwardToPort message: [" + m + "]");//warn

        // remember who sent this
        mLastSender = reply;

        // forward the message to the registered recipients,
        // which includes the communications monitor, except the sender.
        // Schedule notification via the Swing event queue to ensure order
        Runnable r = new XmtNotifier(m, mLastSender, this);
        javax.swing.SwingUtilities.invokeLater(r);

        // Create the correct concrete class for sending to the hardware and encode the message to be sent
        AbstractMRMessage hm;
        if (((CanMessage) m).isTranslated()) {
            hm = m;
        } else {
            hm = encodeForHardware((CanMessage) m);
        }
        log.debug("Encoded for hardware: [" + hm.toString() + "]");

        // stream to port in single write, as that's needed by serial
        byte msg[] = new byte[lengthOfByteStream(hm)];

        // add header
        int offset = addHeaderToOutput(msg, hm);

        // add data content
        int len = hm.getNumDataElements();
        for (int i = 0; i < len; i++) {
            msg[i + offset] = (byte) hm.getElement(i);
        }

        // add trailer
        addTrailerToOutput(msg, len + offset, hm);

        // and stream the bytes
        try {
            if (ostream != null) {
                if (log.isDebugEnabled()) {
                    //String f = "formatted message: ";
                    StringBuffer buf = new StringBuffer("formatted message: ");
                    //for (int i = 0; i<msg.length; i++) f=f+Integer.toHexString(0xFF&msg[i])+" ";
                    for (int i = 0; i < msg.length; i++) {
                        buf.append(Integer.toHexString(0xFF & msg[i]));
                        buf.append(" ");
                    }
                    log.debug(buf.toString());
                }
                while (hm.getRetries() >= 0) {
                    if (portReadyToSend(controller)) {
                        ostream.write(msg);
                        log.debug("message written");
                        break;
                    } else if (hm.getRetries() >= 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("Retry message: " + hm.toString() + " attempts remaining: " + hm.getRetries());
                        }
                        hm.setRetries(hm.getRetries() - 1);
                        try {
                            synchronized (xmtRunnable) {
                                xmtRunnable.wait(hm.getTimeout());
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                            log.error("retry wait interrupted");
                        }
                    } else {
                        log.warn("sendMessage: port not ready for data sending: " + Arrays.toString(msg));
                    }
                }
            } else {
                // no stream connected
                connectionWarn();
            }
        } catch (java.io.IOException | RuntimeException e) {
            portWarn(e);
        }
    }

    /*
     * Default implementations of some of the abstract classes to save having
     * to implement them in every sub class
     */
    @Override
    protected AbstractMRMessage pollMessage() {
        return null;
    }

    @Override
    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /*
     * enterProgMode() and enterNormalMode() return any message that
     * needs to be returned to the command station to change modes.
     *
     * If no message is needed, you may return null.
     *
     * If the programmerIdle() function returns true, enterNormalMode() is
     * called after a timeout while in IDLESTATE during programming to
     * return the system to normal mode.
     *
     */
    @Override
    protected AbstractMRMessage enterProgMode() {
        return null;
    }

    @Override
    protected AbstractMRMessage enterNormalMode() {
        return null;
    }

    /**
     * Get the correct concrete class for the hardware connection message
     */
    abstract protected AbstractMRMessage newMessage();

    abstract public CanReply decodeFromHardware(AbstractMRReply m);

    abstract public AbstractMRMessage encodeForHardware(CanMessage m);

    /**
     * Handle each reply when complete.
     * <p>
     * Overridden to include translation form the CAN hardware format
     *
     */
    @SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE")
    // Ignore false positive that msg is never used
    @Override
    public void handleOneIncomingReply() throws java.io.IOException {
        // we sit in this until the message is complete, relying on
        // threading to let other stuff happen

        // Create messages off the right concrete classes
        // for the CanReply
        CanReply msg = new CanReply();

        // and for the incoming reply from the hardware
        AbstractMRReply hmsg = newReply();

        // wait for start if needed
        waitForStartOfReply(istream);

        // message exists, now fill it
        loadChars(hmsg, istream);

        // Decode the message from the hardware into a CanReply
        msg = decodeFromHardware(hmsg);
        if (msg == null) {
            return;  // some replies don't get forwarded
        }
        // message is complete, dispatch it !!
        replyInDispatch = true;

        if (log.isDebugEnabled()) {
            log.debug("dispatch reply of length " + msg.getNumDataElements()
                    + " contains " + msg.toString() + " state " + mCurrentState);
        }

        // actually distribute the reply
        distributeOneReply(msg, mLastSender);

        if (!msg.isUnsolicited()) {
            if (log.isDebugEnabled()) {
                log.debug("switch on state " + mCurrentState);
            }
            // effect on transmit:
            switch (mCurrentState) {

                case WAITMSGREPLYSTATE: {
                    // update state, and notify to continue
                    synchronized (xmtRunnable) {
                        mCurrentState = NOTIFIEDSTATE;
                        replyInDispatch = false;
                        xmtRunnable.notify();
                    }
                    break;
                }

                case WAITREPLYINPROGMODESTATE: {
                    // entering programming mode
                    mCurrentMode = PROGRAMINGMODE;
                    replyInDispatch = false;

                    // check to see if we need to delay to allow decoders to become
                    // responsive
                    int warmUpDelay = enterProgModeDelayTime();
                    if (warmUpDelay != 0) {
                        try {
                            synchronized (xmtRunnable) {
                                xmtRunnable.wait(warmUpDelay);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                        }

                    }

                    // update state, and notify to continue
                    synchronized (xmtRunnable) {
                        mCurrentState = OKSENDMSGSTATE;
                        xmtRunnable.notify();
                    }
                    break;
                }

                case WAITREPLYINNORMMODESTATE: {
                    // entering normal mode
                    mCurrentMode = NORMALMODE;
                    replyInDispatch = false;
                    // update state, and notify to continue
                    synchronized (xmtRunnable) {
                        mCurrentState = OKSENDMSGSTATE;
                        xmtRunnable.notify();
                    }
                    break;
                }

                default: {
                    replyInDispatch = false;
                    if (allowUnexpectedReply == true) {
                        if (log.isDebugEnabled()) {
                            log.debug("Allowed unexpected reply received in state: "
                                    + mCurrentState + " was " + msg.toString());
                        }
                    } else {
                        unexpectedReplyStateError(mCurrentState,msg.toString());
                    }
                }
            }
            // Unsolicited message
        } else {
            replyInDispatch = false;
        }
    }

    public void distributeOneReply(CanReply msg, AbstractMRListener mLastSender) {
        // forward the message to the registered recipients,
        // which includes the communications monitor
        Runnable r = newRcvNotifier(msg, mLastSender, this);
        distributeReply(r);
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractCanTrafficController.class);

}
