package jmri.jmrix.dcc4pc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import purejavacomm.SerialPort;

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

    //Dcc4Pc doesn't support this function!
    @Override
    protected AbstractMRMessage enterNormalMode() {
        return Dcc4PcMessage.getExitProgMode();
    }

    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
    }

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    @SuppressFBWarnings(value = "MS_PKGPROTECT")
    // SpotBugs wants this package protected, but we're removing it when multi-connection
    // migration is complete
    final static protected Dcc4PcTrafficController self = null;

    Dcc4PcMessage mLastMessage;  //Last message requested with a reply listener ie from external methods
    Dcc4PcMessage mLastSentMessage; //Last message actually sent from within the code, ie getResponse.

    @Override
    synchronized protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (log.isDebugEnabled()) {
            log.debug("forwardToPort message: [" + m + "]");
        }
        if (port == null) {
            return;
        }
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

    }

    //this forward to port is also used internally for repeating commands.
    private void forwardToPort(AbstractMRMessage m) {
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
                    StringBuilder f = new StringBuilder("formatted message: ");
                    for (int i = 0; i < msg.length; i++) {
                        f.append(Integer.toHexString(0xFF & msg[i]));
                        f.append(" ");
                    }
                    log.debug(new String(f));
                }
                while (m.getRetries() >= 0) {
                    if (portReadyToSend(controller)) {
                        port.setDTR(true);
                        ostream.write(msg);
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        } catch (Exception ex) {
                            log.warn("sendMessage: Exception: " + ex.toString());
                        }
                        ostream.flush();
                        port.setDTR(false);
                        break;
                    } else if (m.getRetries() >= 0) {
                        if (log.isDebugEnabled()) {
                            StringBuilder b = new StringBuilder("Retry message: ");
                            b.append(m.toString());
                            b.append(" attempts remaining: ");
                            b.append(m.getRetries());
                            log.debug(new String(b));
                        }
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
                        log.warn("sendMessage: port not ready for data sending: " + java.util.Arrays.toString(msg));
                    }
                }
            } else {
                // ostream is null
                // no stream connected
                connectionWarn();
            }
        } catch (java.io.IOException | RuntimeException e) {
            // TODO Currently there's no port recovery if an exception occurs
            // must restart JMRI to clear xmtException.
            xmtException = true;
            portWarn(e);
        }
    }
    SerialPort port;

    @Override
    public void connectPort(jmri.jmrix.AbstractPortController p) {

        super.connectPort(p);
        port = ((SerialDriverAdapter) controller).getSerialPort();

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
            log.error("IO Exception" + ex.toString());
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
                    log.debug("This is a message for a child board " + ((Dcc4PcReply) msg).toHexString());
                    log.debug("Originate " + mLastMessage.toString());
                }
                if ((mLastSentMessage.getNumDataElements() - 1) == msg.getElement(1)) {
                    log.debug("message lengths match");
                    waitingForMore = true;
                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {
                        log.debug(ex.getLocalizedMessage(), ex);
                    }
                    //log.debug("We do not forward the response to the listener as it has not been formed");
                    lastIncomplete = null;
                    forwardToPort(Dcc4PcMessage.getResponse());

                    return;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Not all of the command was sent, we need to figure out a way to resend the bits");
                        log.debug("Original Message length " + mLastSentMessage.getNumDataElements());
                        log.debug("What CID has procced in size " + (byte) msg.getElement(1));
                        log.debug("Reply is in error " + ((Dcc4PcReply) msg).toHexString());
                    }
                }
            } else if (mLastSentMessage.getElement(0) == 0x0C) {
                if (log.isDebugEnabled()) {
                    log.debug("last message was a get response " + ((Dcc4PcReply) msg).toHexString());
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

                    } else if (msg.getNumDataElements() > 1) {
                        lastIncomplete = (Dcc4PcReply) msg;
                    }
                    //We do not forward the response to the listener as it has not been formed
                    forwardToPort(Dcc4PcMessage.getResponse());

                    return;

                } else {
                    log.debug("Reply is an error mesage");
                    ((Dcc4PcReply) msg).setError(true);
                    mLastMessage.setRetries(mLastMessage.getRetries() - 1);
                    if (mLastMessage.getRetries() >= 0) {
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
            log.debug("Last message sent was null " + ((Dcc4PcReply) msg).toHexString());
        }

        // message is complete, dispatch it !!
        replyInDispatch = true;
        if (log.isDebugEnabled()) {
            log.debug("dispatch reply of length " + msg.getNumDataElements()
                    + " contains " + msg.toString() + " state " + mCurrentState);
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
                            log.debug("Automatic Recovery from Error Message: " + msg.toString());
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
                log.debug("Unsolicited Message Received "
                        + msg.toString());
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
                    if (controller.getInputStream().available() > 0) {
                        readingData = true;
                        byte char1 = readByteProtected(istream);
                        waiting = false;

                        //potentially add in a flush here that is generated by the transmit after a command has been sent, but this is not an error type flush.l
                        // if there was a timeout, flush any char received and start over
                        if (flushReceiveChars) {
                            lastIncomplete = null;
                            waitingForMore = false;
                            mLastMessage = null;
                            mLastSentMessage = null;
                            readingData = false;
                            log.warn("timeout flushes receive buffer: " + ((Dcc4PcReply) msg).toHexString());
                            msg.flush();
                            i = 0;  // restart
                            flushReceiveChars = false;
                            waiting = true;
                        } else {
                            if (canReceive()) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Set data " + i + ", " + (char1 & 0xff));
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
                                log.error("unsolicited character received: " + Integer.toHexString(char1));
                            }
                        }
                    } else if (!port.isDSR()) {
                        if (i == 0) {
                            waiting = true;
                        } else {
                            log.debug("We have data so will break");
                            waiting = false;
                            break MAINGET;
                        }
                    } else {
                        //As we have no data to process we will set the readingData flag false;
                        readingData = false;
                    }
                }
            }
        }
    }

    boolean readingData = false;

    @Override
    protected void transmitWait(int waitTime, int state, String InterruptMessage) {
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

                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                log.error(InterruptMessage);
            }
        }
        log.debug("TIMEOUT in transmitWait, mCurrentState:" + mCurrentState + " " + state + " port dsr " + port.isDSR() + " wait time " + waitTime);
    }

    private final static Logger log = LoggerFactory.getLogger(Dcc4PcTrafficController.class);
}
