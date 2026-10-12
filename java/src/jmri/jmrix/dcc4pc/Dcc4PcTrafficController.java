package jmri.jmrix.dcc4pc;

import java.io.DataInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import jmri.jmrix.dcc4pc.serialdriver.SerialDriverAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from DCC4PC messages. The "Dcc4PcInterface" side
 * sends/receives message objects.
 * <p>
 * The connection to a Dcc4PcPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <p>
 * This handles the state transitions, based on the necessary state in each
 * message.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class Dcc4PcTrafficController extends AbstractMRTrafficController implements Dcc4PcInterface {

    /**
     * Create a new DccPcTrafficController instance.
     */
    public Dcc4PcTrafficController() {
        super();
        if (log.isDebugEnabled()) {
            log.debug("creating a new Dcc4PcTrafficController object");
        }
        this.setAllowUnexpectedReply(false);
    }

    public void setAdapterMemo(Dcc4PcSystemConnectionMemo memo) {
        adaptermemo = memo;
    }

    Dcc4PcSystemConnectionMemo adaptermemo;

    @Override
    public synchronized void addDcc4PcListener(Dcc4PcListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeDcc4PcListener(Dcc4PcListener l) {
        this.removeListener(l);
    }

    public static final int RETRIEVINGDATA = 100;

    /**
     * Forward a Dcc4PcMessage to all registered Dcc4PcInterface listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((Dcc4PcListener) client).message((Dcc4PcMessage) m);
    }

    /**
     * Forward a Dcc4PcReply to all registered Dcc4PcInterface listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((Dcc4PcListener) client).reply((Dcc4PcReply) r);
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
    public void sendDcc4PcMessage(Dcc4PcMessage m, Dcc4PcListener reply) {
        sendMessage(m, reply);
    }

    protected boolean unsolicitedSensorMessageSeen = false;

    //Dcc4Pc doesn't support this function.
    @Override
    protected AbstractMRMessage enterProgMode() {
        return Dcc4PcMessage.getProgMode();
    }

    // DCC4PC has no programming mode, so there's nothing to send. The old
    // one-byte 0x00 message was the interface's own Get Info, and at shutdown
    // terminate() sent it with 100 retries, flooding the log.
    @Override
    protected AbstractMRMessage enterNormalMode() {
        return null;
    }

    /**
     * At shutdown, put every board and the interface back to 8 kbps so other
     * programs can talk to them. The next JMRI start resets the speed anyway.
     */
    @Override
    protected void terminate() {
        log.debug("Cleanup Starts");
        stopJamWatchdog();
        if (ostream == null || port == null) {
            return;    // no connection established
        }
        // let a reply that is still being fetched finish first
        long end = System.currentTimeMillis() + 1000;
        while (sequenceInProgress && System.currentTimeMillis() < end) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        Dcc4PcMessage m = Dcc4PcMessage.busResetSpeeds();
        m.setRetries(0);
        // Don't wait on a write that will never finish: with the USB link jammed,
        // waiting here would stop JMRI quitting.
        boolean locked = false;
        try {
            locked = !xmtException && portLock.tryLock(1, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (!locked) {
            log.warn("DCC4PC interface not responding; bus speeds not reset at quit");
            return;
        }
        try {
            forwardToPort(m, null);
        } finally {
            portLock.unlock();
        }
    }

    /**
     * Held while writing to the port, by the transmit thread and by the
     * receive thread (which sends Get Response itself), so the two never
     * write at once. It's a separate lock, not this object's monitor, because
     * a write can block for a long time when the interface's USB link jams;
     * the GUI takes the monitor (to notify listeners and queue messages), and
     * holding it during the write froze the whole window.
     */
    private final java.util.concurrent.locks.ReentrantLock portLock = new java.util.concurrent.locks.ReentrantLock();

    /** When the current write started, or 0 if no write is in progress. */
    private volatile long writeStartedAt = 0;

    /** A write that hasn't finished after this long means the USB link has jammed. */
    static final long STUCK_WRITE_MILLIS = 3000;

    private volatile boolean jamReported = false;
    private java.util.Timer jamWatchdog;

    /**
     * Watch for a write that never finishes. jSerialComm's write timeout
     * doesn't always fire: on 2026-10-12 a write stayed blocked for over 10
     * minutes, until the interface was unplugged. Call with portLock held.
     */
    private void startJamWatchdog() {
        if (jamWatchdog != null) {
            return;
        }
        jamWatchdog = new java.util.Timer("DCC4PC write watchdog", true);
        jamWatchdog.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                long started = writeStartedAt;
                if (started != 0 && System.currentTimeMillis() - started > STUCK_WRITE_MILLIS) {
                    reportJam("a write to the port has not finished after " + STUCK_WRITE_MILLIS / 1000 + " s");
                }
            }
        }, 1000, 1000);
    }

    private void stopJamWatchdog() {
        java.util.Timer t = jamWatchdog;
        if (t != null) {
            t.cancel();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminateThreads() {
        stopJamWatchdog();
        Thread t = recoveryThread;
        if (t != null) {
            t.interrupt();
        }
        super.terminateThreads();
    }

    /**
     * Tell the user, once per failure, that the interface has stopped
     * responding and how to recover. Killing JMRI while a write is stuck in
     * the USB driver has crashed Windows (bugcheck 0x1A), so the advice is to
     * unplug first.
     */
    private void reportJam(String why) {
        if (jamReported) {
            return;
        }
        jamReported = true;
        String text = "The DCC4PC interface has stopped responding (" + why + ").\n"
                + "Unplug its USB cable and plug it back in: JMRI will reconnect by itself.\n"
                + "Don't end JMRI from Task Manager while the interface is still plugged in.";
        log.error("{}", text.replace('\n', ' '));
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            jmri.util.swing.JmriJOptionPane.showMessageDialogNonModal(null, text,
                    "DCC4PC interface not responding", jmri.util.swing.JmriJOptionPane.ERROR_MESSAGE, null);
        }
    }

    /**
     * Run on the layout thread once the port has been reopened after a
     * failure. The interface restarts at 8 kbps when it is plugged back in, so
     * the managers must set the bus up again as they do at startup.
     */
    private volatile Runnable reconnectHandler;

    public void setReconnectHandler(Runnable handler) {
        reconnectHandler = handler;
    }

    /** True from a port failure until the port has been reopened. */
    volatile boolean recovering = false;
    private volatile Thread recoveryThread;
    private final Object recoveryLock = new Object();

    /** How often to check whether the port is back. */
    long recoveryPollMillis = 1000;

    /** Longest wait between attempts to reopen a port that is listed but won't open. */
    static final long MAX_REOPEN_DELAY = 30000;

    /**
     * Reading or writing the port has failed, usually because the interface's
     * USB link jammed and was then unplugged. Stop sending, tell the user, and
     * reopen the port once it is back (issue 26).
     */
    private void portFailed(String why) {
        xmtException = true;
        reportJam(why);
        if (!(controller instanceof SerialDriverAdapter)) {
            return;
        }
        synchronized (recoveryLock) {
            if (recovering || threadStopRequest) {
                return;
            }
            recovering = true;
            Thread t = new Thread(() -> recoverPort((SerialDriverAdapter) controller), "DCC4PC port recovery");
            t.setDaemon(true);
            recoveryThread = t;
            t.start();
        }
    }

    /**
     * Close the failed port, wait until the operating system lists it again
     * (it disappears while the interface is unplugged), then reopen it.
     * Runs on its own thread.
     */
    private void recoverPort(SerialDriverAdapter adapter) {
        String portName = adapter.getCurrentPortName();
        jmri.jmrix.ConnectionStatus.instance().setConnectionState(adapter.getSystemConnectionMemo(),
                jmri.jmrix.ConnectionStatus.CONNECTION_DOWN);
        log.info("Closing {}; it will be reopened when the interface is back", portName);
        try {
            closePort(adapter, portName);
            boolean wasGone = false;
            long delay = recoveryPollMillis;
            while (!threadStopRequest) {
                Thread.sleep(delay);
                if (!adapter.isPortPresent()) {
                    if (!wasGone) {
                        log.info("{} has gone; waiting for the interface to be plugged back in", portName);
                        wasGone = true;
                    }
                    delay = recoveryPollMillis;
                    continue;
                }
                if (wasGone) {
                    // just plugged back in: give the system a moment to finish setting it up
                    Thread.sleep(2 * recoveryPollMillis);
                    wasGone = false;
                }
                String error = adapter.openPort(portName, "JMRI");
                if (error == null) {
                    reattach(adapter, portName);
                    return;
                }
                delay = Math.min(Math.max(delay, recoveryPollMillis) * 2, MAX_REOPEN_DELAY);
                log.warn("Could not reopen {} ({}); trying again in {} s", portName, error, delay / 1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Port recovery stopped");
        }
    }

    /**
     * Close the port on a separate thread, in case the driver blocks: a write
     * stuck in a jammed USB driver has held on to the port before.
     */
    private void closePort(SerialDriverAdapter adapter, String portName) throws InterruptedException {
        Thread closer = new Thread(adapter::closeSerialPort, "DCC4PC port close");
        closer.setDaemon(true);
        closer.start();
        closer.join(2000);
        if (closer.isAlive()) {
            log.warn("{} did not close within 2 s; trying to reopen it anyway", portName);
        }
    }

    /** Switch to the reopened port's streams, and start sending again. */
    private void reattach(SerialDriverAdapter adapter, String portName) throws InterruptedException {
        portLock.lockInterruptibly(); // a write may still be finishing on the old port
        try {
            istream = adapter.getInputStream();
            ostream = adapter.getOutputStream();
            port = adapter.getSerialPort();
            lastIncomplete = null;
            waitingForMore = false;
            sequenceInProgress = false;
            mLastMessage = null;
            mLastSentMessage = null;
            rcvException = false;
            xmtException = false;
            jamReported = false;
        } finally {
            portLock.unlock();
        }
        // whatever was queued during the outage is stale; the managers start again from scratch
        synchronized (this) {
            msgQueue.clear();
            listenerQueue.clear();
        }
        recovering = false;
        jmri.jmrix.ConnectionStatus.instance().setConnectionState(adapter.getSystemConnectionMemo(),
                jmri.jmrix.ConnectionStatus.CONNECTION_UP);
        log.info("Reopened {}; setting up the DCC4PC bus again", portName);
        Runnable handler = reconnectHandler;
        if (handler != null) {
            jmri.util.ThreadingUtil.runOnLayoutEventually(handler::run);
        }
    }

    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
    }

    Dcc4PcMessage mLastMessage;  //Last message requested with a reply listener ie from external methods
    Dcc4PcMessage mLastSentMessage; //Last message actually sent from within the code, ie getResponse.

    // Not synchronized on this object: see portLock.
    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (log.isDebugEnabled()) {
            log.debug("forwardToPort message: [{}]", m);
        }
        if (port == null) {
            return;
        }
        portLock.lock();
        try {
            // remember who sent this
            mLastSender = reply;
            mLastMessage = (Dcc4PcMessage) m;

            // forward the message to the registered recipients,
            // which includes the communications monitor, except the sender.
            // Schedule notification via the Swing event queue to ensure order
            if (!mLastMessage.isGetResponse()) {
                //Do not forward on the get response packets, saves filling up the monitors with chaff
                Runnable r = new XmtNotifier(m, mLastSender, this);
                javax.swing.SwingUtilities.invokeLater(r);
            }
            forwardToPort(m);
        } finally {
            portLock.unlock();
        }
    }

    //this forward to port is also used internally for repeating commands.
    private void forwardToPort(AbstractMRMessage m) {
        portLock.lock();
        try {
            writeToPort(m);
        } finally {
            portLock.unlock();
        }
    }

    // Call with portLock held.
    private void writeToPort(AbstractMRMessage m) {
        if (xmtException) {
            // A write has failed and the port is being reopened, so the retry
            // loop below would only wait out each message's timeouts.
            return;
        }
        startJamWatchdog();
        mLastSentMessage = (Dcc4PcMessage) m;
        // stream to port in single write, as that's needed by serial
        byte msg[] = new byte[lengthOfByteStream(m)];

        // add data content
        int len = m.getNumDataElements();
        for (int i = 0; i < len; i++) {
            msg[i] = (byte) m.getElement(i);
        }

        try {
            if (ostream != null) {
                if (log.isDebugEnabled()) {
                    StringBuilder f = new StringBuilder();
                    for (int i = 0; i < msg.length; i++) {
                        f.append(Integer.toHexString(0xFF & msg[i]));
                        f.append(" ");
                    }
                    log.debug("formatted message: {}", f);
                }
                while (m.getRetries() >= 0) {
                    if (portReadyToSend(controller)) {
                        // DSR high means the interface is still answering the last command.
                        // (It also reads high just after the port opens, so don't wait long.)
                        waitForDsrLow(200, false);
                        waitForGap();
                        port.setDTR(true);
                        writeStartedAt = System.currentTimeMillis();
                        try {
                            ostream.write(msg);
                        } finally {
                            writeStartedAt = 0;
                        }
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        } catch (Exception ex) {
                            log.warn("sendMessage: Exception: {}", ex.toString());
                        }
                        ostream.flush();
                        port.setDTR(false);
                        if (((Dcc4PcMessage) m).isNoDataReply()) {
                            waitForDsrLow(m.getTimeout(), true);
                            lastTransactionEnd = System.currentTimeMillis();
                        }
                        break;
                    } else if (m.getRetries() >= 0) {
                        log.debug("Retry message: {} attempts remaining: {}", m, m.getRetries());
                        m.setRetries(m.getRetries() - 1);
                        try {
                            synchronized (xmtRunnable) {
                                xmtRunnable.wait(m.getTimeout());
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                            log.error("retry wait interrupted");
                        }
                    } else {
                        log.warn("sendMessage: port not ready for data sending: {}", java.util.Arrays.toString(msg));
                    }
                }
            } else {
                // ostream is null
                // no stream connected
                connectionWarn();
            }
        } catch (java.io.IOException | RuntimeException e) {
            portWarn(e);
            portFailed("a write to the port failed: " + e.getMessage());
        }
    }
    /**
     * Wait for the interface to finish a command that it answers with no data:
     * DSR falls when it is done, usually within 2 ms of DTR going low.
     *
     * @param timeout longest wait, in milliseconds
     * @param warn    log a warning if DSR is still high at the end
     */
    private void waitForDsrLow(int timeout, boolean warn) {
        long end = System.currentTimeMillis() + timeout;
        while (port.isDSR()) {
            if (System.currentTimeMillis() > end) {
                if (warn) {
                    log.warn("Interface still busy (DSR high) {} ms after {}", timeout, mLastSentMessage);
                } else {
                    log.debug("DSR still high after {} ms, sending anyway", timeout);
                }
                return;
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Quiet time between the end of one transaction and the start of the
     * next. Sending each Get Response straight after the last reply ended
     * jammed the interface's USB link after a few minutes of polling with two
     * locos; our standalone tools always leave about this long, and ran for
     * minutes without a jam.
     */
    static final int MIN_GAP_MS = 20;

    volatile long lastTransactionEnd = 0;

    private void waitForGap() {
        long wait = lastTransactionEnd + MIN_GAP_MS - System.currentTimeMillis();
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    volatile jmri.jmrix.purejavacomm.SerialPort port;

    @Override
    public void connectPort(jmri.jmrix.AbstractPortController p) {

        super.connectPort(p);
        port = ((SerialDriverAdapter) controller).getSerialPort();

    }

    /**
     * Like the base class's loop, but a failed port doesn't end it: the port
     * is reopened (see {@link #portFailed}) and reading carries on.
     */
    @Override
    public void receiveLoop() {
        log.debug("receiveLoop starts in {}", this);
        int errorCount = 0;
        while (!threadStopRequest) {
            if (recovering) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }
            try {
                handleOneIncomingReply();
                errorCount = 0;
            } catch (java.io.InterruptedIOException e) {
                if (!recovering) {
                    break; // asked to stop
                }
            } catch (java.io.IOException e) {
                if (!threadStopRequest) {
                    portFailed("reading from the port failed: " + e.getMessage());
                }
            } catch (RuntimeException e) {
                log.error("Exception in receive loop: {}", e.toString(), e);
                if (++errorCount >= maxRcvExceptionCount) {
                    errorCount = 0;
                    portFailed("too many errors reading from the port");
                }
            }
        }
        log.debug("receiveLoop ends");
    }

    @Override
    protected AbstractMRReply newReply() {
        Dcc4PcReply reply = new Dcc4PcReply();
        return reply;
    }

    // for now, receive always OK
    @Override
    protected boolean canReceive() {
        return true;
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        if (port.isDSR()) {
            return false;
        }
        try {
            if (controller.getInputStream().available() > 0) {
                if (port.isRI()) {
                    log.debug("??? Ringing true ???");
                }
                return false;
            }

            //log.debug("No more input available " + port.isDSR());
            if (port.isRI()) {
                log.debug("??? Ringing true ???");
            }
            return true;
        } catch (java.io.IOException ex) {
            log.error("IO Exception{}", ex.toString());
        }
        return !port.isDSR();
    }

    @Override
    protected void handleTimeout(AbstractMRMessage msg, AbstractMRListener l) {
        if(l != null){
            ((Dcc4PcListener) l).handleTimeout((Dcc4PcMessage) msg);
        }
        super.handleTimeout(msg, l);
    }

    Dcc4PcReply lastIncomplete;
    boolean waitingForMore = false;
    boolean loading = false;

    // True while the receive thread is fetching a reply with Get Response; the
    // transmit thread must not send another command until it has finished.
    volatile boolean sequenceInProgress = false;

    /** Longest a single reply sequence may hold up the transmit thread. */
    static final int MAX_SEQUENCE_TIME = 10000;

    final int GETMOREDATA = 0x01;

    /**
     * Handle each reply when complete.
     * <p>
     * (This is public for testing purposes) Runs in the "Receive" thread.
     *
     */
    @Override
    public void handleOneIncomingReply() throws java.io.IOException {
        // we sit in this until the message is complete, relying on
        // threading to let other stuff happen

        // Create message off the right concrete class
        AbstractMRReply msg = newReply();

        // message exists, now fill it
        loadChars(msg, istream);
        if (mLastSentMessage != null) {
            ((Dcc4PcReply)msg).setOriginalRequest(mLastMessage);
            //log.debug(mLastMessage.getElement(0));
            if (mLastSentMessage.isForChildBoard()) {
                if (log.isDebugEnabled()) {
                    log.debug("This is a message for a child board {}", ((Dcc4PcReply) msg).toHexString());
                    log.debug("Originate {}", mLastMessage.toString());
                }
                if ((mLastSentMessage.getNumDataElements() - 1) == msg.getElement(1)) {
                    log.debug("message lengths match");
                    waitingForMore = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        log.debug("InterruptedException", ex);
                    }
                    //log.debug("We do not forward the response to the listener as it has not been formed");
                    lastIncomplete = null;
                    sequenceInProgress = true;
                    forwardToPort(Dcc4PcMessage.getResponse());

                    return;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Not all of the command was sent, we need to figure out a way to resend the bits");
                        log.debug("Original Message length {}", mLastSentMessage.getNumDataElements());
                        log.debug("What CID has procced in size {}", (byte) msg.getElement(1));
                        log.debug("Reply is in error {}", ((Dcc4PcReply) msg).toHexString());
                    }
                }
            } else if (mLastSentMessage.getElement(0) == 0x0C) {
                if (log.isDebugEnabled()) {
                    log.debug("last message was a get response {}", ((Dcc4PcReply) msg).toHexString());
                }
                if (msg.getElement(0) == Dcc4PcReply.SUCCESS) {
                    ((Dcc4PcReply) msg).strip();
                    if (lastIncomplete != null) {
                        //log.debug("Need to add the new reply to this message");
                        //log.debug("existing : " + lastIncomplete.toHexString());

                        //Append this message to the last incomplete message
                        if (msg.getNumDataElements() != 0) {
                            int iOrig = lastIncomplete.getNumDataElements();
                            int iNew = 0;
                            while (iNew < msg.getNumDataElements()) {
                                lastIncomplete.setElement(iOrig, msg.getElement(iNew));
                                iOrig++;
                                iNew++;
                            }
                        }
                        //set the last incomplete message as the one to return
                        log.debug("Reply set as lastIncomplete");
                        msg = lastIncomplete;
                    }
                    ((Dcc4PcReply) msg).setError(false);
                    ((Dcc4PcReply)msg).setOriginalRequest(mLastMessage);
                    lastIncomplete = null;
                    waitingForMore = false;
                    mLastMessage = null;
                    mLastSentMessage = null;
                } else if (msg.getElement(0) == Dcc4PcReply.INCOMPLETE) {
                    waitingForMore = true;
                    ((Dcc4PcReply) msg).strip();
                    if (lastIncomplete != null) {
                        //Append this message to the last incomplete message
                        if (msg.getNumDataElements() != 0) {
                            int iOrig = lastIncomplete.getNumDataElements();
                            int iNew = 0;
                            while (iNew < msg.getNumDataElements()) {
                                lastIncomplete.setElement(iOrig, msg.getElement(iNew));
                                iOrig++;
                                iNew++;
                            }
                        }

                    } else if (msg.getNumDataElements() > 0) { // a first chunk of one byte is data too
                        lastIncomplete = (Dcc4PcReply) msg;
                    }
                    //We do not forward the response to the listener as it has not been formed
                    sequenceInProgress = true;
                    forwardToPort(Dcc4PcMessage.getResponse());

                    return;

                } else {
                    log.debug("Reply is an error mesage");
                    ((Dcc4PcReply) msg).setError(true);
                    mLastMessage.setRetries(mLastMessage.getRetries() - 1);
                    if (mLastMessage.getRetries() >= 0) {
                        sequenceInProgress = false;
                        synchronized (xmtRunnable) {
                            mCurrentState = AUTORETRYSTATE;
                            replyInDispatch = false;
                            xmtRunnable.notify();
                        }
                        return;
                    }
                }
            }
        } else {
            log.debug("Last message sent was null {}", ((Dcc4PcReply) msg).toHexString());
        }

        // message is complete, dispatch it !!
        sequenceInProgress = false;
        replyInDispatch = true;
        if (log.isDebugEnabled()) {
            log.debug("dispatch reply of length {} contains {} state {}", msg.getNumDataElements(), msg.toString(), mCurrentState);
        }
        // forward the message to the registered recipients,
        // which includes the communications monitor
        // return a notification via the Swing event queue to ensure proper thread
        Runnable r = newRcvNotifier(msg, mLastSender, this);
        try {
            javax.swing.SwingUtilities.invokeAndWait(r);
        } catch (InterruptedException | InvocationTargetException e) {
            log.error("Unexpected exception in invokeAndWait:", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("dispatch thread invoked");
        }
        if (!msg.isUnsolicited()) {
            // effect on transmit:
            switch (mCurrentState) {
                case WAITMSGREPLYSTATE: {
                    // check to see if the response was an error message we want
                    // to automatically handle by re-queueing the last sent
                    // message, otherwise go on to the next message
                    if (msg.isRetransmittableErrorMsg()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Automatic Recovery from Error Message: {}", msg.toString());
                        }
                        synchronized (xmtRunnable) {
                            mCurrentState = AUTORETRYSTATE;
                            replyInDispatch = false;
                            xmtRunnable.notify();
                        }
                    } else {
                        // update state, and notify to continue
                        synchronized (xmtRunnable) {
                            mCurrentState = NOTIFIEDSTATE;
                            replyInDispatch = false;
                            xmtRunnable.notify();
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
                    unexpectedReplyStateError(mCurrentState,msg.toString());
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

    boolean normalFlushReceiveChars = false;

    //Need a way to detect that the dsr has gone low.
    @Override
    protected void loadChars(AbstractMRReply msg, DataInputStream istream)
            throws java.io.IOException {
        int i;
        readingData = false;
        MAINGET:
        {
            for (i = 0; i < msg.maxSize(); i++) {
                boolean waiting = true;
                while (waiting) {
                    if (recovering) {
                        // the port is closed or about to be; the receive loop waits for it to reopen
                        throw new java.io.InterruptedIOException("port being reopened");
                    }
                    if (controller.getInputStream().available() > 0) {
                        readingData = true;
                        byte char1 = readByteProtected(istream);
                        waiting = false;

                        //potentially add in a flush here that is generated by the transmit after a command has been sent, but this is not an error type flush.l
                        // if there was a timeout, flush any char received and start over
                        if (flushReceiveChars) {
                            lastIncomplete = null;
                            waitingForMore = false;
                            sequenceInProgress = false;
                            mLastMessage = null;
                            mLastSentMessage = null;
                            readingData = false;
                            log.warn("timeout flushes receive buffer: {}", ((Dcc4PcReply) msg).toHexString());
                            msg.flush();
                            i = 0;  // restart
                            flushReceiveChars = false;
                            waiting = true;
                        } else {
                            if (canReceive()) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Set data {}, {}", i, char1 & 0xff);
                                }
                                msg.setElement(i, char1);
                                waiting = false;
                                if (port.isRI()) {
                                    log.debug("Ring high error");
                                    ((Dcc4PcReply) msg).setError(true);
                                    break MAINGET;
                                }
                                if (endOfMessage(msg)) {
                                    break MAINGET;
                                }
                            } else {
                                i--; // flush char
                                log.error("unsolicited character received: {}", Integer.toHexString(char1));
                            }
                        }
                    } else if (!port.isDSR()) {
                        if (i == 0) {
                            waiting = true;
                            idleWait();
                        } else {
                            log.debug("We have data so will break");
                            waiting = false;
                            break MAINGET;
                        }
                    } else {
                        //As we have no data to process we will set the readingData flag false;
                        readingData = false;
                        // Only pause before the reply starts. Once its bytes are arriving, read
                        // each one as soon as it's there: pausing mid-reply read the bytes late,
                        // and was the likely cause of replies cut short (issue 29).
                        if (i == 0) {
                            idleWait();
                        }
                    }
                }
            }
        }
        lastTransactionEnd = System.currentTimeMillis();
    }

    /**
     * Pause briefly while loadChars is waiting for a reply to start. Without
     * it the receive thread polled available() and DSR non-stop, using a whole
     * CPU core whenever JMRI was idle (issue 29). It isn't used once a reply's
     * bytes are arriving: on Windows a 1 ms sleep can last up to ~15 ms, and
     * with it the Raw stream misaligned (7 times in one 5-minute run, against
     * none without it).
     *
     * @throws java.io.InterruptedIOException if the thread is interrupted or
     *                                        asked to stop, which ends the
     *                                        receive loop
     */
    private void idleWait() throws java.io.InterruptedIOException {
        if (threadStopRequest) {
            throw new java.io.InterruptedIOException("receive thread stopping");
        }
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new java.io.InterruptedIOException("receive thread interrupted");
        }
    }

    boolean readingData = false;

    @Override
    protected void transmitWait(int waitTime, int state, String InterruptMessage) {
        // wait() can have spurious wakeup!
        // so we protect by making sure the entire timeout time is used
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long endTime = currentTime + waitTime;
        long hardEnd = currentTime + Math.max(waitTime, MAX_SEQUENCE_TIME);
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
                    //If we are in the process of reading the data then do not time out.
                    if (readingData) {
                        endTime = endTime + 10;
                    }
                    //if we have received a packet and a seperate message has been sent to retrieve
                    //the reply we will add more time to our wait process.
                    if (waitingForMore) {
                        waitingForMore = false;
                        //if we are in the process of retrieving data, then we shall increase the endTime by 200ms.
                        endTime = endTime + 200;
                    }
                    // Timing out part way through a Get Response sequence would let the next
                    // command interrupt it, which can jam the interface's USB link.
                    long now = Calendar.getInstance().getTimeInMillis();
                    if (sequenceInProgress && endTime <= now + 50 && now < hardEnd) {
                        endTime = now + 50;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                log.error("{} from {}", InterruptMessage, e.getMessage());
            }
        }
        log.debug("TIMEOUT in transmitWait, mCurrentState:{} {} port dsr {} wait time {}", mCurrentState, state, port.isDSR(), waitTime);
    }

    private static final Logger log = LoggerFactory.getLogger(Dcc4PcTrafficController.class);
}
