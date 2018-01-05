package jmri.jmrix.srcp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Vector;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import jmri.jmrix.srcp.parser.ParseException;
import jmri.jmrix.srcp.parser.SRCPClientParser;
import jmri.jmrix.srcp.parser.SRCPClientVisitor;
import jmri.jmrix.srcp.parser.SimpleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from SRCP messages. The "SRCPInterface" side
 * sends/receives message objects.
 * <P>
 * The connection to a SRCPPortController is via a pair of *Streams, which then
 * carry sequences of characters for transmission. Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transitions, based on the necessary state in each
 * message.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SRCPTrafficController extends AbstractMRTrafficController
        implements SRCPInterface, jmri.ShutDownTask {

    protected SRCPSystemConnectionMemo _memo = null;

    public SRCPTrafficController() {
        super();
        if (jmri.InstanceManager.getNullableDefault(jmri.ShutDownManager.class) != null) {
            jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(this);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("attempted to register shutdown task, but shutdown manager is null");
            }
        }
    }

    // The methods to implement the SRCPInterface
    @Override
    public synchronized void addSRCPListener(SRCPListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeSRCPListener(SRCPListener l) {
        this.removeListener(l);
    }

    /*
     * Set the system connection memo associated with the traffic
     * controller
     */
    void setSystemConnectionMemo(SRCPSystemConnectionMemo memo) {
        _memo = memo;
    }

    /*
     * Get the system connection memo associated with the traffic
     * controller
     */
    SRCPSystemConnectionMemo getSystemConnectionMemo() {
        return _memo;
    }

    public static int HANDSHAKEMODE = 0;
    public static int RUNMODE = 1;
    private int mode = HANDSHAKEMODE;

    /*
     * We are going to override the receiveLoop() function so that we can
     * handle messages received by the system using the SRCP parser.
     */
    @Override
    public void receiveLoop() {
        if (log.isDebugEnabled()) {
            log.debug("SRCP receiveLoop starts");
        }
        SRCPClientParser parser = new SRCPClientParser(istream);
        while (true) {
            try {
                SimpleNode e;
                if (_memo.getMode() == HANDSHAKEMODE) {
                    e = parser.handshakeresponse();
                } else {
                    e = parser.commandresponse();
                }

                // forward the message to the registered recipients,
                // which includes the communications monitor
                // return a notification via the Swing event queue to ensure proper thread
                Runnable r = new SRCPRcvNotifier(e, mLastSender, this);
                try {
                    javax.swing.SwingUtilities.invokeAndWait(r);
                } catch (Exception ex) {
                    log.error("Unexpected exception in invokeAndWait:" + ex);
                    ex.printStackTrace();
                }
                if (log.isDebugEnabled()) {
                    log.debug("dispatch thread invoked");
                }

                log.debug("Mode " + mode + " child contains "
                        + ((SimpleNode) e.jjtGetChild(1)).jjtGetValue());
                //if (mode==HANDSHAKEMODE && ((String)((SimpleNode)e.jjtGetChild(1)).jjtGetValue()).contains("GO")) mode=RUNMODE;

                SRCPClientVisitor v = new SRCPClientVisitor();
                e.jjtAccept(v, _memo);

                // we need to re-write the switch below so that it uses the 
                // SimpleNode values instead of the reply message.            
                //SRCPReply msg = new SRCPReply((SimpleNode)e.jjtGetChild(1));
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

                        // check to see if we need to delay to allow decoders 
                        // to become responsive
                        int warmUpDelay = enterProgModeDelayTime();
                        if (warmUpDelay != 0) {
                            try {
                                synchronized (xmtRunnable) {
                                    xmtRunnable.wait(warmUpDelay);
                                }
                            } catch (InterruptedException ex) {
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
                                        + mCurrentState + " was "
                                        + e.toString());
                            }

                            synchronized (xmtRunnable) {
                                // The transmit thread sometimes gets stuck
                                // when unexpected replies are received.  Notify
                                // it to clear the block without a timeout.
                                // (do not change the current state)
                                xmtRunnable.notify();
                            }
                        } else {
                            unexpectedReplyStateError(mCurrentState,e.toString());
                        }
                    }
                }

            } catch (ParseException pe) {
                rcvException = true;
                reportReceiveLoopException(pe);
                break;
            }
        }
    }

    /**
     * Forward a SRCPMessage to all registered SRCPInterface listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((SRCPListener) client).message((SRCPMessage) m);
    }

    /**
     * Forward a SRCPReply to all registered SRCPInterface listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((SRCPListener) client).reply((SRCPReply) m);
    }

    /**
     * Forward a SRCPReply to all registered SRCPInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, SimpleNode n) {
        ((SRCPListener) client).reply(n);
    }

    public void setSensorManager(jmri.SensorManager m) {
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
    public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
        sendMessage(m, reply);
    }

    @Override
    protected AbstractMRMessage enterProgMode() {
        // we need to find the right bus number!
        return SRCPMessage.getProgMode(1);
    }

    @Override
    protected AbstractMRMessage enterNormalMode() {
        // we need to find the right bus number!
        return SRCPMessage.getExitProgMode(1);
    }

    /**
     * static function returning the SRCPTrafficController instance to use.
     *
     * @return The registered SRCPTrafficController instance for general use, if
     *         need be creating one.
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SRCPTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) {
                log.debug("creating a new SRCP TrafficController object");
            }
            self = new SRCPTrafficController();
        }
        return self;
    }

    static volatile protected SRCPTrafficController self = null;

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "temporary until mult-system; only set at startup")
    @Override
    protected void setInstance() {
        self = this;
    }

    @Override
    protected AbstractMRReply newReply() {
        return new SRCPReply();
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

    /**
     * Forward a "Reply" from layout to registered listeners.
     *
     * @param r    Reply to be forwarded intact
     * @param dest One (optional) listener to be skipped, usually because it's
     *             the originating object.
     */
    @SuppressWarnings("unchecked")
    protected void notifyReply(SimpleNode r, AbstractMRListener dest) {
        // make a copy of the listener vector to synchronized (not needed for transmit?)
        Vector<AbstractMRListener> v;
        synchronized (this) {
            v = (Vector<AbstractMRListener>) cmdListeners.clone();
        }
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            AbstractMRListener client = v.elementAt(i);
            if (log.isDebugEnabled()) {
                log.debug("notify client: " + client);
            }
            try {
                //skip dest for now, we'll send the message to there last.
                if (dest != client) {
                    forwardReply(client, r);
                }
            } catch (Exception ex) {
                log.warn("notify: During reply dispatch to " + client + "\nException " + ex);
                ex.printStackTrace();
            }
            // forward to the last listener who send a message
            // this is done _second_ so monitoring can have already stored the reply
            // before a response is sent
            if (dest != null) {
                forwardReply(dest, r);
            }

        }
    }

    /**
     * Ask if shut down is allowed.
     * <p>
     * The shut down manager must call this method first on all the tasks
     * before starting to execute the method execute() on the tasks.
     * <p>
     * If this method returns false on any task, the shut down process must
     * be aborted.
     *
     * @return true if it is OK to shut down, false to abort shut down.
     */
    @Override
    public boolean isShutdownAllowed() {
        return true;
    }

    /**
     * Take the necessary action.
     *
     * @return true if the shutdown should continue, false to abort.
     */
    @Override
    public boolean execute() {
        // notify the server we are exiting.
        sendSRCPMessage(new SRCPMessage("TERM 0 SESSION"), null);
        // the server will send a reply of "101 INFO 0 SESSION <id>.
        // but we aren't going to wait for the reply.
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public String name() {
        return this.getName();
    }

    @Override
    public String getName() {
        return SRCPTrafficController.class.getName();
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public boolean isComplete() {
        return !this.isParallel();
    }

    /**
     * Internal class to remember the Reply object and destination listener with
     * a reply is received.
     */
    protected static class SRCPRcvNotifier implements Runnable {

        SimpleNode e;
        SRCPListener mDest;
        SRCPTrafficController mTC;

        SRCPRcvNotifier(SimpleNode n, AbstractMRListener pDest,
                AbstractMRTrafficController pTC) {
            // the first child of n in the parse tree is
            // the response, without the timestamp
            e = (SimpleNode) n.jjtGetChild(1);
            mDest = (SRCPListener) pDest;
            mTC = (SRCPTrafficController) pTC;
        }

        @Override
        public void run() {
            log.debug("Delayed rcv notify starts");
            mTC.notifyReply(e, mDest);
        }
    } // SRCPRcvNotifier

    private final static Logger log = LoggerFactory.getLogger(SRCPTrafficController.class);
}



