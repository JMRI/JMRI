package jmri.jmrix.roco.z21;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.DatagramPacket;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractPortController;
import jmri.jmrix.ConnectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for TrafficControllers in a Message/Reply protocol.
 *
 * @author Paul Bender Copyright (C) 2014
 */
public class Z21TrafficController extends jmri.jmrix.AbstractMRTrafficController implements Z21Interface {

    private java.net.InetAddress host;
    private int port;

    public Z21TrafficController() {
        super();
        allowUnexpectedReply = true;
    }

    /**
     * Implement this to forward a specific message type to a protocol-specific
     * listener interface. This puts the casting into the concrete class.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((Z21Listener) client).message((Z21Message) m);
    }

    /**
     * Implement this to forward a specific Reply type to a protocol-specific
     * listener interface. This puts the casting into the concrete class.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((Z21Listener) client).reply((Z21Reply) m);
    }

    /**
     * Invoked if it's appropriate to do low-priority polling of the command
     * station, this should return the next message to send, or null if the TC
     * should just sleep.
     */
    @Override
    protected Z21Message pollMessage() {
        return null;
    }

    @Override
    protected Z21Listener pollReplyHandler() {
        return null;
    }

    /**
     * enterProgMode() and enterNormalMode() return any message that
     * needs to be returned to the command station to change modes.
     *
     * @see #enterNormalMode()
     * @return if no message is needed, you may return null.
     *
     * If the programmerIdle() function returns true, enterNormalMode() is
     * called after a timeout while in IDLESTATE during programming to
     * return the system to normal mode.
     */
    @Override
    protected Z21Message enterProgMode() {
        return null;
    }

    /**
     * enterProgMode() and enterNormalMode() return any message that
     * needs to be returned to the command station to change modes.
     *
     * @see #enterProgMode()
     * @return if no message is needed, you may return null.
     */
    @Override
    protected Z21Message enterNormalMode() {
        return null;
    }

    /**
     * Actually transmits the next message to the port.
     */
    @SuppressFBWarnings(value = {"TLW_TWO_LOCK_WAIT", "", "UW_UNCOND_WAIT"},
            justification = "Two locks needed for synchronization here, this is OK; String + only used for debug, so inefficient String processing not really a problem; Unconditional Wait is to give external hardware, which doesn't necessarilly respond, time to process the data.")
    @Override
    synchronized protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (log.isDebugEnabled()) {
            log.debug("forwardToPort message: [" + m + "]");
        }
        // remember who sent this
        mLastSender = reply;

        // forward the message to the registered recipients,
        // which includes the communications monitor, except the sender.
        // Schedule notification via the Swing event queue to ensure order
        Runnable r = new XmtNotifier(m, mLastSender, this);
        javax.swing.SwingUtilities.invokeLater(r);

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
        // and send the bytes
        try {
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
                    // create a datagram with the data from the
                    // message.
                    byte data[] = ((Z21Message) m).getBuffer();
                    DatagramPacket sendPacket
                            = new DatagramPacket(data, ((Z21Message) m).getLength(), host, port);
                    // and send it.
                    ((Z21Adapter) controller).getSocket().send(sendPacket);
                    log.debug("written, msg timeout: " + m.getTimeout() + " mSec");
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
                        if(!threadStopRequest) {
                           log.error("retry wait interrupted");
                        } else {
                           log.error("retry wait interrupted during thread stop");
                        }
                    }
                } else {
                    log.warn("sendMessage: port not ready for data sending: {}", java.util.Arrays.toString(msg));
                }
            }
        } catch (Exception e) {
            // TODO Currently there's no port recovery if an exception occurs
            // must restart JMRI to clear xmtException.
            xmtException = true;
            portWarn(e);
        }
    }

    @Override()
    public boolean status() {
        if (controller == null) {
            return false;
        } else {
            return (controller.status());
        }
    }

    /**
     * Make connection to existing PortController object.
     */
    @Override
    public void connectPort(AbstractPortController p) {
        rcvException = false;
        xmtException = false;
        if (controller != null) {
            log.warn("connectPort: connect called while connected");
        } else {
            log.debug("connectPort invoked");
        }
        if (!(p instanceof Z21Adapter)) {
            throw new IllegalArgumentException("attempt to connect wrong port type");
        }
        controller = p;
        try {
            host = java.net.InetAddress.getByName(((Z21Adapter) controller).getHostName());
            port = ((Z21Adapter) controller).getPort();
            ConnectionStatus.instance().setConnectionState(
                    p.getSystemConnectionMemo().getUserName(),
                    ((Z21Adapter) p).getHostName() + ":" + ((Z21Adapter) p).getPort(), ConnectionStatus.CONNECTION_UP);
        } catch (java.net.UnknownHostException uhe) {
            log.error("Unknown Host: {} ", ((Z21Adapter) controller).getHostName());
            if (((Z21Adapter) p).getPort() != 0) {
                ConnectionStatus.instance().setConnectionState(
                        p.getSystemConnectionMemo().getUserName(),
                        ((Z21Adapter) controller).getHostName() + ":" + ((Z21Adapter) p).getPort(), ConnectionStatus.CONNECTION_DOWN);
            } else {
                ConnectionStatus.instance().setConnectionState(
                        p.getSystemConnectionMemo().getUserName(),
                        ((Z21Adapter) controller).getHostName(), ConnectionStatus.CONNECTION_DOWN);
            }
        }
        // and start threads
        xmtThread = new Thread(xmtRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    transmitLoop();
                } catch (Throwable e) {
                    if(!threadStopRequest) log.error("Transmit thread terminated prematurely by: " + e.toString(), e);
                    // ThreadDeath must be thrown per Java API JavaDocs
                    if (e instanceof ThreadDeath) {
                        throw e;
                    }
                }
            }
        });
        xmtThread.setName("z21.Z21TrafficController Transmit thread");
        xmtThread.start();
        rcvThread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiveLoop();
            }
        });
        rcvThread.setName("z21.Z21TrafficController Receive thread");
        int xr = rcvThread.getPriority();
        xr++;
        rcvThread.setPriority(xr);      //bump up the priority
        rcvThread.start();
    }

    /**
     * Break connection to existing PortController object. Once broken, attempts
     * to send via "message" member will fail.
     */
    @Override
    public void disconnectPort(AbstractPortController p) {
        if (controller != p) {
            log.warn("disconnectPort: disconnect called from non-connected AbstractPortController");
        }
        controller = null;
    }

    @Override
    protected Z21Reply newReply() {
        return new Z21Reply();
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply r) {
        // since this is a UDP protocol, and each UDP message contains
        // exactly one UDP reply, we don't check for end of message manually.
        return true;
    }

    /**
     * Handle each reply when complete.
     * <p>
     * (This is public for testing purposes) Runs in the "Receive" thread.
     */
    @SuppressFBWarnings(value = {"UW_UNCOND_WAIT", "WA_NOT_IN_LOOP", "NO_NOTIFY_NOT_NOTIFYALL"},
            justification = "Wait is for external hardware, which doesn't necessarilly respond, to process the data.  Notify is used because Having more than one thread waiting on xmtRunnable is an error.")
    @Override
    public void handleOneIncomingReply() throws java.io.IOException {
        // we sit in this until the message is complete, relying on
        // threading to let other stuff happen

        // create a buffer to hold the incoming data.
        byte buffer[] = new byte[100];  // the size here just needs to be longer
        // than the longest protocol message.  
        // Otherwise, the receive will truncate.

        // create the packet.
        DatagramPacket receivePacket = new DatagramPacket(buffer, 100, host, port);

        // and wait to receive data in the packet.
        try {
            ((Z21Adapter) controller).getSocket().receive(receivePacket);
        } catch (java.net.SocketException | NullPointerException se) {
            // if we are waiting when the controller is disposed,
            // a socket exception will be thrown.
            log.debug("Socket exception during receive.  Connection Closed?");
            rcvException = true;
            return;
        }
        if (threadStopRequest) return;
        
        // create the reply from the received data.
        Z21Reply msg = new Z21Reply(buffer, receivePacket.getLength());

        // message is complete, dispatch it !!
        replyInDispatch = true;
        if (log.isDebugEnabled()) {
            log.debug("dispatch reply of length " + msg.getNumDataElements()
                    + " contains " + msg.toString() + " state " + mCurrentState);
        }

        // forward the message to the registered recipients,
        // which includes the communications monitor
        // return a notification via the Swing event queue to ensure proper thread
        Runnable r = new RcvNotifier(msg, mLastSender, this);
        try {
            javax.swing.SwingUtilities.invokeAndWait(r);
        } catch (java.lang.InterruptedException ie) {
            if(threadStopRequest) return;
            log.error("Unexpected exception in invokeAndWait:" + ie,ie);
        } catch (Exception e) {
            log.error("Unexpected exception in invokeAndWait:" + e,e);
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
                            log.debug("Automatic Recovery from Error Message: +msg.toString()");
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
                            if (threadStopRequest) return;
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
                log.debug("Unsolicited Message Received "
                        + msg.toString());
            }

            replyInDispatch = false;
        }
    }

    @SuppressFBWarnings(value = {"UW_UNCOND_WAIT", "WA_NOT_IN_LOOP"},
            justification = "Wait is for external hardware, which doesn't necessarilly respond, to process the data.")
    @Override
    protected void terminate() {
        if (controller == null) {
            log.debug("terminate called while not connected");
            return;
        } else {
            log.debug("Cleanup Starts");
        }

        Z21Message logoffMessage = Z21Message.getLanLogoffRequestMessage();
        forwardToPort(logoffMessage, null);
        // wait for reply
        try {
            if (xmtRunnable != null) {
                synchronized (xmtRunnable) {
                    xmtRunnable.wait(logoffMessage.getTimeout());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // retain if needed later
            log.error("transmit interrupted");
        } finally {
            // set the controller to null, even if terminate fails.
            controller = null;
        }
    }

    /**
     * Terminate the receive and transmit threads.
     * <p>
     * This is intended to be used only by testing subclasses.
     */
    @Override
    public void terminateThreads() {
        threadStopRequest = true;
        // ensure socket closed to end pending operations
        if ( controller!=null && ((Z21Adapter) controller).getSocket()!=null) ((Z21Adapter) controller).getSocket().close();
        
        // usual stop process
        super.terminateThreads();
    }

    // The methods to implement the Z21Interface
    @Override
    public synchronized void addz21Listener(Z21Listener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removez21Listener(Z21Listener l) {
        this.removeListener(l);
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    @Override
    public void sendz21Message(Z21Message m, Z21Listener reply) {
        sendMessage(m, reply);
    }

    private final static Logger log = LoggerFactory.getLogger(Z21TrafficController.class);
}
