package jmri.jmrix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Vector;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for TrafficControllers in a Message/Reply protocol.
 * <P>
 * Two threads are used for the actual communication. The "Transmit" thread
 * handles pushing characters to the port, and also changing the mode. The
 * "Receive" thread converts characters from the input stream into replies.
 * <P>
 * A third thread is registered by the constructor as a shutdown hook. It
 * triggers the necessary cleanup code
 * <P>
 * "Mode" refers to the state of the command station communications.<br>
 * "State" refers to the internal state machine used to control the mode, e.g.
 * to send commands to change mode.<br>
 * "Idle" is a special case, where there is no communications in process, and
 * the port is waiting to do something.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Paul Bender Copyright (C) 2004-2010
 */
abstract public class AbstractMRTrafficController {

    private Thread shutdownHook = null; // retain shutdown hook for 
                                        // possible removal.

    public AbstractMRTrafficController() {
        log.debug("Creating AbstractMRTrafficController instance");
        mCurrentMode = NORMALMODE;
        mCurrentState = IDLESTATE;
        allowUnexpectedReply = false;
        setInstance();

        // We use a shutdown hook here to make sure the connection is left
        // in a clean state prior to exiting.  This is required on systems
        // which have a service mode to ensure we don't leave the system 
        // in an unusable state (This code predates the ShutdownTask 
        // mechanisim).  Once the shutdown hook executes, the connection
        // must be considered closed.
        shutdownHook = new Thread(new CleanupHook(this));
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private boolean synchronizeRx = true;
    
    protected void setSynchronizeRx(boolean val) {
        synchronizeRx = val;
    }
    protected boolean getSynchronizeRx() {
        return synchronizeRx;
    }

    // set the instance variable
    abstract protected void setInstance();

    // The methods to implement the abstract Interface
    protected Vector<AbstractMRListener> cmdListeners = new Vector<AbstractMRListener>();

    protected synchronized void addListener(AbstractMRListener l) {
        // add only if not already registered
        if (l == null) {
            throw new NullPointerException();
        }
        if (!cmdListeners.contains(l)) {
            cmdListeners.addElement(l);
        }
    }

    protected synchronized void removeListener(AbstractMRListener l) {
        if (cmdListeners.contains(l)) {
            cmdListeners.removeElement(l);
        }
    }

    /**
     * Forward a Message to registered listeners.
     *
     * @param m     Message to be forwarded intact
     * @param notMe One (optional) listener to be skipped, usually because it's
     *              the originating object.
     */
    @SuppressWarnings("unchecked")
    protected void notifyMessage(AbstractMRMessage m, AbstractMRListener notMe) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<AbstractMRListener> v;
        synchronized (this) {
            v = (Vector<AbstractMRListener>) cmdListeners.clone();
        }
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            AbstractMRListener client = v.elementAt(i);
            if (notMe != client) {
                log.debug("notify client: {}", client);
                try {
                    forwardMessage(client, m);
                } catch (RuntimeException e) {
                    log.warn("notify: During message dispatch to {}", client, e);
                }
            }
        }
    }

    /**
     * Implement this to forward a specific message type to a protocol-specific
     * listener interface. This puts the casting into the concrete class.
     */
    abstract protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m);

    /**
     * Invoked if it's appropriate to do low-priority polling of the command
     * station, this should return the next message to send, or null if the TC
     * should just sleep.
     */
    abstract protected AbstractMRMessage pollMessage();

    abstract protected AbstractMRListener pollReplyHandler();

    protected AbstractMRListener mLastSender = null;

    volatile protected int mCurrentMode;
    public static final int NORMALMODE = 1;
    public static final int PROGRAMINGMODE = 4;

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
    abstract protected AbstractMRMessage enterProgMode();

    abstract protected AbstractMRMessage enterNormalMode();

    // Use this function to check and see if the programmer is idle
    // Override in the system specific code if necessary (see notes for
    // enterNormalMode() Above).
    protected boolean programmerIdle() {
        return true;
    }

    // Allow subclasses to add a delay after enabling the programming track
    protected int enterProgModeDelayTime() {
        return 0;
    }

    volatile protected int mCurrentState;
    public static final int IDLESTATE = 10;        // nothing happened
    public static final int NOTIFIEDSTATE = 15;    // xmt notified, will next wake
    public static final int WAITMSGREPLYSTATE = 25;  // xmt has sent, await reply to message
    public static final int WAITREPLYINPROGMODESTATE = 30;  // xmt has done mode change, await reply
    public static final int WAITREPLYINNORMMODESTATE = 35;  // xmt has done mode change, await reply
    public static final int OKSENDMSGSTATE = 40;        // mode change reply here, send original msg
    public static final int AUTORETRYSTATE = 45;        // received message where automatic recovery may occur with a retransmission, re-send original msg
    public static final int POLLSTATE = 50;   // Send program mode or poll message

    protected boolean allowUnexpectedReply;

    // Use this function to identify If the command station may send
    // messages without a request sent to it
    protected void setAllowUnexpectedReply(boolean expected) {
        allowUnexpectedReply = expected;
    }

    /**
     * Forward a "Reply" from layout to registered listeners.
     *
     * @param r    Reply to be forwarded intact
     * @param dest One (optional) listener to be skipped, usually because it's
     *             the originating object.
     */
    @SuppressWarnings("unchecked")
    protected void notifyReply(AbstractMRReply r, AbstractMRListener dest) {
        // make a copy of the listener vector to synchronized (not needed for transmit?)
        Vector<AbstractMRListener> v;
        synchronized (this) {
            v = (Vector<AbstractMRListener>) cmdListeners.clone();
        }
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            AbstractMRListener client = v.elementAt(i);
            log.debug("notify client: {}", client);
            try {
                //skip dest for now, we'll send the message to there last.
                if (dest != client) {
                    forwardReply(client, r);
                }
            } catch (RuntimeException e) {
                log.warn("notify: During reply dispatch to {}", client, e);
            }
        }

        // forward to the last listener who send a message
        // this is done _second_ so monitoring can have already stored the reply
        // before a response is sent
        if (dest != null) {
            forwardReply(dest, r);
        }
    }

    abstract protected void forwardReply(AbstractMRListener client, AbstractMRReply m);

    /**
     * Messages to be transmitted
     */
    protected LinkedList<AbstractMRMessage> msgQueue = new LinkedList<AbstractMRMessage>();
    protected LinkedList<AbstractMRListener> listenerQueue = new LinkedList<AbstractMRListener>();

    /**
     * This is invoked with messages to be forwarded to the port. It queues
     * them, then notifies the transmission thread.
     */
    synchronized protected void sendMessage(AbstractMRMessage m, AbstractMRListener reply) {
        msgQueue.addLast(m);
        listenerQueue.addLast(reply);
        synchronized (xmtRunnable) {
            if (mCurrentState == IDLESTATE) {
                mCurrentState = NOTIFIEDSTATE;
                xmtRunnable.notify();
            }
        }
        if (m != null) {
            if (log.isDebugEnabled()) {
                log.debug("just notified transmit thread with message {}", m.toString());
            }
        }
    }

    /**
     * Permanent loop for the transmit thread.
     */
    protected void transmitLoop() {
        log.debug("transmitLoop starts");

        // loop forever
        while (!connectionError && !threadStopRequest) {
            AbstractMRMessage m = null;
            AbstractMRListener l = null;
            // check for something to do
            synchronized (this) {
                if (msgQueue.size() != 0) {
                    // yes, something to do
                    m = msgQueue.getFirst();
                    msgQueue.removeFirst();
                    l = listenerQueue.getFirst();
                    listenerQueue.removeFirst();
                    mCurrentState = WAITMSGREPLYSTATE;
                    log.debug("transmit loop has something to do: {}", m);
                }  // release lock here to proceed in parallel
            }
            // if a message has been extracted, process it
            if (m != null) {
                // check for need to change mode
                log.debug("Start msg, state = {}", mCurrentMode);
                if (m.getNeededMode() != mCurrentMode) {
                    AbstractMRMessage modeMsg;
                    if (m.getNeededMode() == PROGRAMINGMODE) {
                        // change state to programming mode and send message
                        modeMsg = enterProgMode();
                        if (modeMsg != null) {
                            mCurrentState = WAITREPLYINPROGMODESTATE;
                            log.debug("Enter Programming Mode");
                            forwardToPort(modeMsg, null);
                            // wait for reply
                            transmitWait(m.getTimeout(), WAITREPLYINPROGMODESTATE, "enter programming mode interrupted");
                        }
                    } else {
                        // change state to normal and send message
                        modeMsg = enterNormalMode();
                        if (modeMsg != null) {
                            mCurrentState = WAITREPLYINNORMMODESTATE;
                            log.debug("Enter Normal Mode");
                            forwardToPort(modeMsg, null);
                            // wait for reply
                            transmitWait(m.getTimeout(), WAITREPLYINNORMMODESTATE, "enter normal mode interrupted");
                        }
                    }
                    if (modeMsg != null) {
                        checkReplyInDispatch();
                        if (mCurrentState != OKSENDMSGSTATE) {
                            handleTimeout(modeMsg, l);
                        }
                        mCurrentState = WAITMSGREPLYSTATE;
                    } else {
                        // no mode message required, but the message
                        // needs a different mode
                        log.debug("Setting mode to: {}", m.getNeededMode());
                        mCurrentMode = m.getNeededMode();
                    }
                }
                forwardToPort(m, l);
                // reply expected?
                if (m.replyExpected()) {
                    // wait for a reply, or eventually timeout
                    transmitWait(m.getTimeout(), WAITMSGREPLYSTATE, "transmitLoop interrupted");
                    checkReplyInDispatch();
                    if (mCurrentState == WAITMSGREPLYSTATE) {
                        handleTimeout(m, l);
                    } else if (mCurrentState == AUTORETRYSTATE) {
                        log.info("Message added back to queue: {}", m.toString());
                        msgQueue.addFirst(m);
                        listenerQueue.addFirst(l);
                        synchronized (xmtRunnable) {
                            mCurrentState = IDLESTATE;
                        }
                    } else {
                        resetTimeout(m);
                    }
                } // just continue to the next message from here
            } else {
                // nothing to do
                if (mCurrentState != IDLESTATE) {
                    log.debug("Setting IDLESTATE");
                    log.debug("Current Mode {}", mCurrentMode);
                    mCurrentState = IDLESTATE;
                }
                // wait for something to send
                if (mWaitBeforePoll > waitTimePoll || mCurrentMode == PROGRAMINGMODE) {
                    try {
                        long startTime = Calendar.getInstance().getTimeInMillis();
                        synchronized (xmtRunnable) {
                            xmtRunnable.wait(mWaitBeforePoll);
                        }
                        long endTime = Calendar.getInstance().getTimeInMillis();
                        waitTimePoll = waitTimePoll + endTime - startTime;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // retain if needed later
                        // end of transmit loop
                        break;
                    }
                }
                // once we decide that mCurrentState is in the IDLESTATE and there's an xmt msg we must guarantee
                // the change of mCurrentState to one of the waiting for reply states.  Therefore we need to synchronize.
                synchronized (this) {
                    if (mCurrentState != NOTIFIEDSTATE && mCurrentState != IDLESTATE) {
                        log.error("left timeout in unexpected state: {}", mCurrentState);
                    }
                    if (mCurrentState == IDLESTATE) {
                        mCurrentState = POLLSTATE; // this prevents other transitions from the IDLESTATE
                    }
                }
                // went around with nothing to do; leave programming state if in it
                if (mCurrentMode == PROGRAMINGMODE) {
                    log.debug("Timeout - in service mode");
                }
                if (mCurrentState == POLLSTATE && mCurrentMode == PROGRAMINGMODE && programmerIdle()) {
                    log.debug("timeout causes leaving programming mode");
                    mCurrentState = WAITREPLYINNORMMODESTATE;
                    AbstractMRMessage msg = enterNormalMode();
                    // if the enterNormalMode() message is null, we
                    // don't want to try to send it to the port.
                    if (msg != null) {
                        forwardToPort(msg, null);
                        // wait for reply
                        transmitWait(msg.getTimeout(), WAITREPLYINNORMMODESTATE, "interrupted while leaving programming mode");
                        checkReplyInDispatch();
                        // exit program mode timeout?
                        if (mCurrentState == WAITREPLYINNORMMODESTATE) {
                            // entering normal mode via timeout
                            handleTimeout(msg, l);
                            mCurrentMode = NORMALMODE;
                        }
                        // and go around again
                    }
                } else if (mCurrentState == POLLSTATE && mCurrentMode == NORMALMODE) {
                    // We may need to poll
                    AbstractMRMessage msg = pollMessage();
                    if (msg != null) {
                        // yes, send that
                        log.debug("Sending poll, wait time {}", Long.toString(waitTimePoll));
                        mCurrentState = WAITMSGREPLYSTATE;
                        forwardToPort(msg, pollReplyHandler());
                        // wait for reply
                        transmitWait(msg.getTimeout(), WAITMSGREPLYSTATE, "interrupted while waiting poll reply");
                        checkReplyInDispatch();
                        // and go around again
                        if (mCurrentState == WAITMSGREPLYSTATE) {
                            handleTimeout(msg, l);
                        } else {
                            resetTimeout(msg);
                        }
                    }
                    waitTimePoll = 0;
                }
                // no messages, so back to idle
                if (mCurrentState == POLLSTATE) {
                    mCurrentState = IDLESTATE;
                }
            }
        }
    }   // end of transmit loop; go around again

    protected void transmitWait(int waitTime, int state, String interruptMessage) {
        // wait() can have spurious wakeup!
        // so we protect by making sure the entire timeout time is used
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long endTime = currentTime + waitTime;
        while (endTime > (currentTime = Calendar.getInstance().getTimeInMillis())) {
            long wait = endTime - currentTime;
            try {
                synchronized (xmtRunnable) {
                    // Do not wait if the current state has changed since we
                    // last set it.
                    if (mCurrentState != state) {
                        return;
                    }
                    xmtRunnable.wait(wait); // rcvr normally ends this w state change
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                String[] packages = this.getClass().getName().split("\\.");
                String name = (packages.length>=2 ? packages[packages.length-2]+"." :"")
                        +(packages.length>=1 ? packages[packages.length-1] :"");
                if (!threadStopRequest) {
                    log.error(interruptMessage+" in transmitWait(..) of {}", name);
                } else {
                    log.debug("during shutdown, "+interruptMessage+" in transmitWait(..) of {}", name);
                }
            }
        }
        log.debug("Timeout in transmitWait, mCurrentState: {}", mCurrentState);
    }

    // Dispatch control and timer
    protected boolean replyInDispatch = false;          // true when reply has been received but dispatch not completed
    private int maxDispatchTime = 0;
    private int warningMessageTime = DISPATCH_WARNING_TIME;
    private static final int DISPATCH_WAIT_INTERVAL = 100;
    private static final int DISPATCH_WARNING_TIME = 12000; // report warning when max dispatch time exceeded
    private static final int WARN_NEXT_TIME = 1000;         // report every second

    private void checkReplyInDispatch() {
        int loopCount = 0;
        while (replyInDispatch) {
            try {
                synchronized (xmtRunnable) {
                    xmtRunnable.wait(DISPATCH_WAIT_INTERVAL);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                String[] packages = this.getClass().getName().split("\\.");
                String name = (packages.length>=2 ? packages[packages.length-2]+"." :"")
                        +(packages.length>=1 ? packages[packages.length-1] :"");
                log.error("transmitLoop interrupted in class {}", name);
            }
            loopCount++;
            int currentDispatchTime = loopCount * DISPATCH_WAIT_INTERVAL;
            if (currentDispatchTime > maxDispatchTime) {
                maxDispatchTime = currentDispatchTime;
                if (currentDispatchTime >= warningMessageTime) {
                    warningMessageTime = warningMessageTime + WARN_NEXT_TIME;
                    log.debug("Max dispatch time is now {}", currentDispatchTime);
                }
            }
        }
    }

    // used to determine if interface is down
    public boolean hasTimeouts() {
        return timeoutFlag;
    }

    private boolean timeoutFlag = false;
    private int timeouts = 0;
    protected boolean flushReceiveChars = false;

    protected void handleTimeout(AbstractMRMessage msg, AbstractMRListener l) {
        //log.debug("Timeout mCurrentState: {}", mCurrentState);
        String[] packages = this.getClass().getName().split("\\.");
        String name = (packages.length>=2 ? packages[packages.length-2]+"." :"")
                +(packages.length>=1 ? packages[packages.length-1] :"");

        log.warn("Timeout on reply to message: {} consecutive timeouts = {} in {}", msg.toString(), timeouts, name);
        timeouts++;
        timeoutFlag = true;
        flushReceiveChars = true;
    }

    protected void resetTimeout(AbstractMRMessage msg) {
        if (timeouts > 0) {
            log.debug("Reset timeout after {} timeouts", timeouts);
        }
        timeouts = 0;
        timeoutFlag = false;
    }

    /**
     * Add header to the outgoing byte stream.
     *
     * @param msg The output byte stream
     * @return next location in the stream to fill
     */
    protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
        return 0;
    }

    protected int mWaitBeforePoll = 100;
    protected long waitTimePoll = 0;

    /**
     * Add trailer to the outgoing byte stream.
     *
     * @param msg    The output byte stream
     * @param offset the first byte not yet used
     */
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
        if (!m.isBinary()) {
            msg[offset] = 0x0d;
        }
    }

    /**
     * Determine how much many bytes the entire message will take, including
     * space for header and trailer
     *
     * @param m The message to be sent
     * @return Number of bytes
     */
    protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        int cr = 0;
        if (!m.isBinary()) {
            cr = 1;  // space for return
        }
        return len + cr;
    }

    protected boolean xmtException = false;

    /**
     * Actually transmits the next message to the port
     */
    @SuppressFBWarnings(value = {"TLW_TWO_LOCK_WAIT"},
            justification = "Two locks needed for synchronization here, this is OK")
    synchronized protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        log.debug("forwardToPort message: [{}]", m);
        // remember who sent this
        mLastSender = reply;

        // forward the message to the registered recipients,
        // which includes the communications monitor, except the sender.
        // Schedule notification via the Swing event queue to ensure order
        Runnable r = new XmtNotifier(m, mLastSender, this);
        SwingUtilities.invokeLater(r);

        // stream to port in single write, as that's needed by serial
        byte msg[] = new byte[lengthOfByteStream(m)];
        // add header
        int offset = addHeaderToOutput(msg, m);

        // add data content
        int len = m.getNumDataElements();
        for (int i = 0; i < len; i++) {
            msg[i + offset] = (byte) m.getElement(i);
        }
        // add trailer
        addTrailerToOutput(msg, len + offset, m);
        // and stream the bytes
        try {
            if (ostream != null) {
                if (log.isDebugEnabled()) {
                    StringBuilder f = new StringBuilder("formatted message: ");
                    for (int i = 0; i < msg.length; i++) {
                        f.append(Integer.toHexString(0xFF & msg[i]));
                        f.append(" ");
                    }
                    log.debug(f.toString());
                }
                while (m.getRetries() >= 0) {
                    if (portReadyToSend(controller)) {
                        ostream.write(msg);
                        ostream.flush();
                        log.debug("written, msg timeout: {} mSec", m.getTimeout());
                        break;
                    } else if (m.getRetries() >= 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("Retry message: {} attempts remaining: {}", m.toString(), m.getRetries());
                        }
                        m.setRetries(m.getRetries() - 1);
                        try {
                            synchronized (xmtRunnable) {
                                xmtRunnable.wait(m.getTimeout());
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                            log.error("retry wait interupted");
                        }
                    } else {
                        log.warn("sendMessage: port not ready for data sending: {}", Arrays.toString(msg));
                    }
                }
            } else {  // ostream is null
                // no stream connected
                connectionWarn();
            }
        } catch (IOException | RuntimeException e) {
            // TODO Currently there's no port recovery if an exception occurs
            // must restart JMRI to clear xmtException.
            xmtException = true;
            portWarn(e);
        }
    }

    protected void connectionWarn() {
        log.warn("sendMessage: no connection established for {}", this.getClass().getName(), new Exception());
    }

    protected void portWarn(Exception e) {
        log.warn("sendMessage: Exception: In {} port warn: ", this.getClass().getName(), e);
    }

    protected boolean connectionError = false;

    protected void portWarnTCP(Exception e) {
        log.warn("Exception java net: {}", e.toString());
        connectionError = true;
    }
    // methods to connect/disconnect to a source of data in a AbstractPortController
    public AbstractPortController controller = null;

    public boolean status() {
        return (ostream != null && istream != null);
    }

    volatile protected Thread xmtThread = null;
    volatile protected Thread rcvThread = null;

    volatile protected Runnable xmtRunnable = null;

    /**
     * Make connection to existing PortController object.
     */
    public void connectPort(AbstractPortController p) {
        rcvException = false;
        connectionError = false;
        xmtException = false;
        try {
            istream = p.getInputStream();
            ostream = p.getOutputStream();
            if (controller != null) {
                log.warn("connectPort: connect called while connected");
            } else {
                log.debug("connectPort invoked");
            }
            controller = p;
            // and start threads
            xmtThread = new Thread(xmtRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        transmitLoop();
                    } catch (Throwable e) {
                        if (!threadStopRequest) log.error("Transmit thread terminated prematurely by: {}", e.toString(), e);
                        // ThreadDeath must be thrown per Java API Javadocs
                        if (e instanceof ThreadDeath) {
                            throw e;
                        }
                    }
                }
            });
            
            String[] packages = this.getClass().getName().split("\\.");
            xmtThread.setName(
                (packages.length>=2 ? packages[packages.length-2]+"." :"")
                +(packages.length>=1 ? packages[packages.length-1] :"")
                +" Transmit thread");

            xmtThread.setDaemon(true);
            xmtThread.setPriority(Thread.MAX_PRIORITY-1);      //bump up the priority
            xmtThread.start();

            rcvThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    receiveLoop();
                }
            });
            rcvThread.setName(
                (packages.length>=2 ? packages[packages.length-2]+"." :"")
                +(packages.length>=1 ? packages[packages.length-1] :"")
                +" Receive thread");

            rcvThread.setPriority(Thread.MAX_PRIORITY);      //bump up the priority
            rcvThread.setDaemon(true);
            rcvThread.start();
            
        } catch (RuntimeException e) {
            log.error("Failed to start up communications. Error was {}", e.toString());
            log.debug("Full trace:", e);
        }
    }

    /**
     * Get the port name for this connection
     */
    public String getPortName() {
        return controller.getCurrentPortName();
    }

    /**
     * Break connection to existing PortController object. Once broken, attempts
     * to send via "message" member will fail.
     */
    public void disconnectPort(AbstractPortController p) {
        istream = null;
        ostream = null;
        if (controller != p) {
            log.warn("disconnectPort: disconnect called from non-connected AbstractPortController");
        }
        controller = null;
    }

    /**
     * Check to see if PortController object can be sent to. returns true if
     * ready, false otherwise May throw an Exception.
     */
    public boolean portReadyToSend(AbstractPortController p) {
        if (p != null && !xmtException && !rcvException) {
            return true;
        } else {
            return false;
        }
    }

    // data members to hold the streams
    protected DataInputStream istream = null;
    protected OutputStream ostream = null;

    protected boolean rcvException = false;

    protected int maxRcvExceptionCount = 100;

    /**
     * Handle incoming characters. This is a permanent loop, looking for input
     * messages in character form on the stream connected to the PortController
     * via <code>connectPort</code>. Each turn of the loop is the receipt of a
     * single message.
     */
    public void receiveLoop() {
        log.debug("receiveLoop starts");
        int errorCount = 0;
        while (errorCount < maxRcvExceptionCount && !threadStopRequest) { // stream close will exit via exception
            try {
                handleOneIncomingReply();
                errorCount = 0;
            } catch (java.io.InterruptedIOException e) {
                // related to InterruptedException, catch first
                break;
            } catch (IOException e) {
                rcvException = true;
                reportReceiveLoopException(e);
                break;
            } catch (RuntimeException e1) {
                log.error("Exception in receive loop: {}", e1.toString(), e1);
                errorCount++;
                if (errorCount == maxRcvExceptionCount) {
                    rcvException = true;
                    reportReceiveLoopException(e1);
                }
            }
        }
        if (!threadStopRequest) { // if e.g. unexpected end
            ConnectionStatus.instance().setConnectionState(controller.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
            log.error("Exit from rcv loop in {}", this.getClass().toString());
            recovery(); // see if you can restart
        }
    }

    /**
     * Invoked at abnormal end of receiveLoop
     */
    protected final void recovery() {
        AbstractPortController adapter = controller;
        disconnectPort(controller);
        adapter.recover();
    }

    /**
     * Report error on receive loop. Separated so tests can suppress, even
     * though message is asynchronous.
     */
    protected void reportReceiveLoopException(Exception e) {
        log.error("run: Exception: {} in {}", e.toString(), this.getClass().toString(), e);
        jmri.jmrix.ConnectionStatus.instance().setConnectionState(controller.getCurrentPortName(), jmri.jmrix.ConnectionStatus.CONNECTION_DOWN);
        if (controller instanceof AbstractNetworkPortController) {
            portWarnTCP(e);
        }
    }

    abstract protected AbstractMRReply newReply();

    abstract protected boolean endOfMessage(AbstractMRReply r);

    /**
     * Dummy routine, to be filled by protocols that have to skip some
     * start-of-message characters.
     */
    @SuppressWarnings("unused")
    protected void waitForStartOfReply(DataInputStream istream) throws IOException {
    }

    /**
     * Read a single byte, protecting against various timeouts, etc.
     * <P>
     * When a port is set to have a receive timeout (via the
     * enableReceiveTimeout() method), some will return zero bytes or an
     * EOFException at the end of the timeout. In that case, the read should be
     * repeated to get the next real character.
     *
     * @param istream stream to read
     * @return the byte read
     * @throws java.io.IOException if unable to read
     */
    protected byte readByteProtected(DataInputStream istream) throws IOException {
        while (true) { // loop will repeat until character found
            int nchars;
            nchars = istream.read(rcvBuffer, 0, 1);
            if (nchars == -1) {
                // No more bytes can be read from the channel
                throw new IOException("Connection not terminated normally");
            }
            if (nchars > 0) {
                return rcvBuffer[0];
            }
        }
    }

    // Defined this way to reduce new object creation
    private byte[] rcvBuffer = new byte[1];

    /**
     * Get characters from the input source, and file a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread.
     * <P>
     * Handles timeouts on read by ignoring zero-length reads.
     *
     * @param msg     message to fill
     * @param istream character source.
     * @throws IOException when presented by the input source.
     */
    protected void loadChars(AbstractMRReply msg, DataInputStream istream)
            throws IOException {
        int i;
        for (i = 0; i < msg.maxSize(); i++) {
            byte char1 = readByteProtected(istream);
            //if (log.isDebugEnabled()) log.debug("char: "+(char1&0xFF)+" i: "+i);
            // if there was a timeout, flush any char received and start over
            if (flushReceiveChars) {
                log.warn("timeout flushes receive buffer: {}", msg.toString());
                msg.flush();
                i = 0;  // restart
                flushReceiveChars = false;
            }
            if (canReceive()) {
                msg.setElement(i, char1);
                if (endOfMessage(msg)) {
                    break;
                }
            } else {
                i--; // flush char
                log.error("unsolicited character received: {}", Integer.toHexString(char1));
            }
        }
    }

    /**
     * Override in the system specific code if necessary
     *
     * @return true if it is okay to buffer receive characters into a reply
     *         message. When false, discard char received
     */
    protected boolean canReceive() {
        return true;
    }

    private int retransmitCount = 0;

    /**
     * Executes a reply distribution action on the appropriate thread for JMRI.
     * @param r a runnable typically encapsulating a MRReply and the iteration code needed to
     *          send it to all the listeners.
     */
    protected void distributeReply(Runnable r) {
        try {
            if (synchronizeRx) {
                SwingUtilities.invokeAndWait(r);
            } else {
                SwingUtilities.invokeLater(r);
            }
        } catch (InterruptedException ie) {
            if(threadStopRequest) return;
            log.error("Unexpected exception in invokeAndWait: {}" + ie.toString(), ie);
        } catch (java.lang.reflect.InvocationTargetException| RuntimeException e) {
            log.error("Unexpected exception in invokeAndWait: {}" + e.toString(), e);
            return;
        }
        log.debug("dispatch thread invoked");
    }

    /**
     * Handle each reply when complete.
     * <P>
     * (This is public for testing purposes) Runs in the "Receive" thread.
     *
     */
    public void handleOneIncomingReply() throws IOException {
        // we sit in this until the message is complete, relying on
        // threading to let other stuff happen

        // Create message off the right concrete class
        AbstractMRReply msg = newReply();

        // wait for start if needed
        waitForStartOfReply(istream);

        // message exists, now fill it
        loadChars(msg, istream);

        if (threadStopRequest) return;
        
        // message is complete, dispatch it !!
        replyInDispatch = true;
        if (log.isDebugEnabled()) {
            log.debug("dispatch reply of length {} contains {} state {}", msg.getNumDataElements(), msg.toString(), mCurrentState);
        }

        // forward the message to the registered recipients,
        // which includes the communications monitor
        // return a notification via the Swing event queue to ensure proper thread
        Runnable r = new RcvNotifier(msg, mLastSender, this);
        distributeReply(r);

        if (!msg.isUnsolicited()) {
            // effect on transmit:
            switch (mCurrentState) {
                case WAITMSGREPLYSTATE: {
                    // check to see if the response was an error message we want
                    // to automatically handle by re-queueing the last sent
                    // message, otherwise go on to the next message
                    if (msg.isRetransmittableErrorMsg()) {
                        log.error("Automatic Recovery from Error Message: {}.  Retransmitted {} times.", msg.toString(), retransmitCount);
                        synchronized (xmtRunnable) {
                            mCurrentState = AUTORETRYSTATE;
                            if (retransmitCount > 0) {
                                try {
                                    xmtRunnable.wait(retransmitCount * 100L);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt(); // retain if needed later
                                }
                            }
                            replyInDispatch = false;
                            xmtRunnable.notify();
                            retransmitCount++;
                        }
                    } else {
                        // update state, and notify to continue
                        synchronized (xmtRunnable) {
                            mCurrentState = NOTIFIEDSTATE;
                            replyInDispatch = false;
                            xmtRunnable.notify();
                            retransmitCount = 0;
                        }
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
                            log.debug("Allowed unexpected reply received in state: {} was {}", mCurrentState, msg.toString());
                        }
                        synchronized (xmtRunnable) {
                            // The transmit thread sometimes gets stuck
                            // when unexpected replies are received.  Notify
                            // it to clear the block without a timeout.
                            // (do not change the current state)
                            //if(mCurrentState!=IDLESTATE)
                            xmtRunnable.notify();
                        }
                    } else {
                        unexpectedReplyStateError(mCurrentState,msg.toString());
                    }
                }
            }
            // Unsolicited message
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unsolicited Message Received {}", msg.toString());
            }

            replyInDispatch = false;
        }
    }

    /*
     * log an error message for a message received in an unexpected state. 
     */
    protected void unexpectedReplyStateError(int State,String msgString) {
       String[] packages = this.getClass().getName().split("\\.");
       String name = (packages.length>=2 ? packages[packages.length-2]+"." :"")
                     +(packages.length>=1 ? packages[packages.length-1] :"");
       log.error("reply complete in unexpected state: {} was {} in class {}", State, msgString,name);
    }

    /*
     * for testing purposes, let us be able to find out
     * what the last sender was
     */
    public AbstractMRListener getLastSender() {
        return mLastSender;
    }

    // Override the finalize method for this class
    // to request termination, which might have happened
    // before in any case
    @Override
    protected final void finalize() throws Throwable {
        terminate();
        super.finalize();
    }

    protected void terminate() {
        log.debug("Cleanup Starts");
        if (ostream == null) {
            return;    // no connection established
        }
        AbstractMRMessage modeMsg = enterNormalMode();
        if (modeMsg != null) {
            modeMsg.setRetries(100); // set the number of retries
            // high, just in case the interface
            // is busy when we try to send
            forwardToPort(modeMsg, null);
            // wait for reply
            try {
                if (xmtRunnable != null) {
                    synchronized (xmtRunnable) {
                        xmtRunnable.wait(modeMsg.getTimeout());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                log.error("transmit interrupted");
            }
        }
    }

    /**
     * Internal class to remember the Reply object and destination listener with
     * a reply is received.
     */
    protected static class RcvNotifier implements Runnable {

        AbstractMRReply mMsg;
        AbstractMRListener mDest;
        AbstractMRTrafficController mTC;

        public RcvNotifier(AbstractMRReply pMsg, AbstractMRListener pDest,
                AbstractMRTrafficController pTC) {
            mMsg = pMsg;
            mDest = pDest;
            mTC = pTC;
        }

        @Override
        public void run() {
            log.debug("Delayed rcv notify starts");
            mTC.notifyReply(mMsg, mDest);
        }
    } // end RcvNotifier

    // allow creation of object outside package
    protected RcvNotifier newRcvNotifier(AbstractMRReply pMsg, AbstractMRListener pDest,
            AbstractMRTrafficController pTC) {
        return new RcvNotifier(pMsg, pDest, pTC);
    }

    /**
     * Internal class to remember the Message object and destination listener
     * when a message is queued for notification.
     */
    protected static class XmtNotifier implements Runnable {

        AbstractMRMessage mMsg;
        AbstractMRListener mDest;
        AbstractMRTrafficController mTC;

        public XmtNotifier(AbstractMRMessage pMsg, AbstractMRListener pDest,
                AbstractMRTrafficController pTC) {
            mMsg = pMsg;
            mDest = pDest;
            mTC = pTC;
        }

        @Override
        public void run() {
            log.debug("Delayed xmt notify starts");
            mTC.notifyMessage(mMsg, mDest);
        }
    }  // end XmtNotifier

    /**
     * Terminate the receive and transmit threads.
     *<p>
     * This is intended to be used only by testing subclasses.
     */
    public void terminateThreads() {
        threadStopRequest = true;
        if (xmtThread != null) {
            xmtThread.interrupt();
            try {
                xmtThread.join();
            } catch (InterruptedException ie){
                // interrupted durring cleanup.
            }
        }
        
        if (rcvThread != null) {
            rcvThread.interrupt();
            try {
                rcvThread.join();
            } catch (InterruptedException ie){
                // interrupted durring cleanup.
            }
        }    

        // we also need to remove the shutdown hook. 
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }
    
    /**
     * Flag that threads should terminate as soon as they can.
     */
    protected volatile boolean threadStopRequest = false;
    
    /**
     * Internal class to handle traffic controller cleanup. the primary task of
     * this thread is to make sure the DCC system has exited service mode when
     * the program exits.
     */
    static class CleanupHook implements Runnable {

        AbstractMRTrafficController mTC;

        CleanupHook(AbstractMRTrafficController pTC) {
            mTC = pTC;
        }

        @Override
        public void run() {
            mTC.terminate();
        }
    } // end cleanUpHook

    private final static Logger log = LoggerFactory.getLogger(AbstractMRTrafficController.class);

}
