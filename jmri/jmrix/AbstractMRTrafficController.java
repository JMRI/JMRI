// AbstractMRTrafficController.java

package jmri.jmrix;

import java.io.*;
import java.util.Vector;

import com.sun.java.util.collections.LinkedList;

/**
 * Abstract base for TrafficControllers in a Message/Reply protocol.
 * <P>
 * Two threads are used for the actual communication.  The "Transmit"
 * thread handles pushing characters to the port, and also changing
 * the mode.  The "Receive" thread converts characters from the input
 * stream into replies.
 * <P>
 * "Mode" refers to the state of the command station communications.<br>
 * "State" refers to the internal state machine used to control the mode,
 * e.g. to send commands to change mode.<br>
 * "Idle" is a special case, where there is no communications in process,
 * and the port is waiting to do something.
 *
 * @author			Bob Jacobsen  Copyright (C) 2003
 * @version			$Revision: 1.5 $
 */
abstract public class AbstractMRTrafficController {

    public AbstractMRTrafficController() {
        if (log.isDebugEnabled()) log.debug("setting instance: "+this);
        mCurrentMode = NORMALMODE;
        mCurrentState = IDLESTATE;
        setInstance();
        self = this;
    }

    AbstractMRTrafficController self;  // this is needed for synchronization

    // set the instance variable
    abstract protected void setInstance();

    // The methods to implement the abstract Interface

    protected Vector cmdListeners = new Vector();

    protected synchronized void addListener(AbstractMRListener l) {
        // add only if not already registered
        if (l == null) throw new java.lang.NullPointerException();
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
     * Forward a message to all registered listeners.
     */
    protected void notifyMessage(AbstractMRMessage m, AbstractMRListener notMe) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector v;
        synchronized(this)
            {
                v = (Vector) cmdListeners.clone();
            }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            AbstractMRListener client = (AbstractMRListener) v.elementAt(i);
            if (notMe != client) {
                if (log.isDebugEnabled()) log.debug("notify client: "+client);
                try {
                    forwardMessage(client, m);
                }
                catch (Exception e)
                    {
                        log.warn("notify: During dispatch to "+client+"\nException "+e);
                    }
            }
        }
    }

    /**
     * Implement this to foward a specific message type to a protocol-specific
     * listener interface. This puts the casting into the concrete class.
     */
    abstract protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m);

    /**
     * Invoked if it's appropriate to do low-priority polling of the
     * command station, this should return the next message to send,
     * or null if the TC should just sleep.
     */
    abstract protected AbstractMRMessage pollMessage();
    abstract protected AbstractMRListener pollReplyHandler();

    protected AbstractMRListener mLastSender = null;

    volatile protected int mCurrentMode;
    public static final int NORMALMODE=1;
    public static final int PROGRAMINGMODE=4;

    abstract protected AbstractMRMessage enterProgMode();
    abstract protected AbstractMRMessage enterNormalMode();

    volatile protected int mCurrentState;
    public static final int IDLESTATE = 10;        // nothing happened
    public static final int NOTIFIEDSTATE = 15;    // xmt notified, will next wake
    public static final int WAITMSGREPLYSTATE = 25;  // xmt has sent, await reply to message
    public static final int WAITREPLYINPROGMODESTATE = 30;  // xmt has done mode change, await reply
    public static final int WAITREPLYINNORMMODESTATE = 35;  // xmt has done mode change, await reply
    public static final int OKSENDMSGSTATE = 40;        // mode change reply here, send original msg

    protected void notifyReply(AbstractMRReply r, AbstractMRListener dest) {
        // make a copy of the listener vector to synchronized (not needed for transmit?)
        Vector v;
        synchronized(this)
            {
                v = (Vector) cmdListeners.clone();
            }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            AbstractMRListener client = (AbstractMRListener) v.elementAt(i);
            if (log.isDebugEnabled()) log.debug("notify client: "+client);
            try {
                forwardReply(client, r);
            }
            catch (Exception e)
                {
                    log.warn("notify: During dispatch to "+client+"\nException "+e);
                }
        }

        // forward to the last listener who send a message
        // this is done _second_ so monitoring can have already stored the reply
        // before a response is sent
        if (dest != null) forwardReply(dest, r);
    }

    abstract protected void forwardReply(AbstractMRListener client, AbstractMRReply m);

    /**
     * Messages to be transmitted
     */
    LinkedList msgQueue = new LinkedList();
    LinkedList listenerQueue = new LinkedList();

    /**
     * This is invoked with messages to be forwarded to the port.
     * It queues them, then notifies the transmission thread.
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
        log.debug("just notified transmit thread");
    }

    /**
     * Permanent loop for the transmit thread.
     */
    private void transmitLoop() {
        log.debug("transmitLoop starts");

        // loop forever
        while(true) {
            AbstractMRMessage m = null;
            AbstractMRListener l = null;
            // check for something to do
            synchronized(self) {
                if (msgQueue.size()!=0) {
                    // yes, something to do
                    log.debug("transmit loop has something to do");
                    m = (AbstractMRMessage)msgQueue.getFirst();
                    msgQueue.removeFirst();
                    l = (AbstractMRListener)listenerQueue.getFirst();
                    listenerQueue.removeFirst();
                    mCurrentState = WAITMSGREPLYSTATE;
                }  // release lock here to proceed in parallel
            }
            // if a message has been extracted, process it
            if (m!=null) {
                // check for need to change mode
                if (m.getNeededMode()!=mCurrentMode) {
                    AbstractMRMessage modeMsg;
                    if (m.getNeededMode() == PROGRAMINGMODE ) {
                        // change state and send message
                        modeMsg = enterProgMode();
                        mCurrentState = WAITREPLYINPROGMODESTATE;
                    } else { // must be normal mode
                        // change state and send message
                        modeMsg = enterNormalMode();
                        mCurrentState = WAITREPLYINNORMMODESTATE;
                    }
                    forwardToPort(modeMsg, null);
                    // wait for reply
                    try {
                        synchronized(xmtRunnable) {
                            xmtRunnable.wait(m.getTimeout());
                        }
                    } catch (InterruptedException e) { log.error("transmitLoop interrupted"); }
                    mCurrentState = WAITMSGREPLYSTATE;
                }
                forwardToPort(m, l);
                // wait for a reply, or eventually timeout
                try {
                    synchronized(xmtRunnable) {
                        xmtRunnable.wait(m.getTimeout());
                    }
                } catch (InterruptedException e) { log.error("transmitLoop interrupted"); }
            } else {
                // nothing to do
                mCurrentState =IDLESTATE;
                // wait for something to send
                try {
                    synchronized(xmtRunnable) {
                        xmtRunnable.wait(mWaitBeforePoll);
                    }
                } catch (InterruptedException e) { log.error("transmitLoop interrupted"); }
                if (mCurrentState!=NOTIFIEDSTATE && mCurrentState!=IDLESTATE)
                    log.error("left timeout in unexpected state: "+mCurrentState);
                if (mCurrentState == IDLESTATE) {
                    // went around with nothing to do; leave programming state if in it
                    if (mCurrentMode == PROGRAMINGMODE) {
                        log.debug("timeout causes leaving programming mode");
                        mCurrentState = WAITREPLYINNORMMODESTATE;
                        AbstractMRMessage msg = enterNormalMode();
                        forwardToPort(msg, null);
                        // wait for reply
                        try {
                            synchronized(xmtRunnable) {
                                xmtRunnable.wait(msg.getTimeout());
                            }
                        } catch (InterruptedException e) { log.error("interrupted while leaving programming mode"); }
                        // and go around again
                    } else if (mCurrentMode == NORMALMODE) {
                        // We may need to poll
                        AbstractMRMessage msg = pollMessage();
                        if (msg != null) {
                            // yes, send that
                            mCurrentState = WAITMSGREPLYSTATE;
                            forwardToPort(msg, pollReplyHandler());
                            // wait for reply
                            try {
                                synchronized(xmtRunnable) {
                                    xmtRunnable.wait(msg.getTimeout());
                                }
                            } catch (InterruptedException e) { log.error("interrupted while leaving programming mode"); }
                            // and go around again
                       } else {
                            // no, just wait
                        }
                    }
                }
            }
        }   // end of permanent loop; go around again
    }

    /**
     * Add header to the outgoing byte stream.
     * @param msg  The output byte stream
     * @returns next location in the stream to fill
     */
    protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
        return 0;

    }

    protected int mWaitBeforePoll = 100;

    /**
     * Add trailer to the outgoing byte stream.
     * @param msg  The output byte stream
     * @param offset the first byte not yet used
     */
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
        if (! m.isBinary()) msg[offset] = 0x0d;
    }

    /**
     * Determine how much many bytes the entire
     * message will take, including space for header and trailer
     * @param m  The message to be sent
     * @returns Number of bytes
     */
    protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        int cr = 0;
        if (! m.isBinary()) cr = 1;  // space for return
        return len+cr;

    }

    /**
     * Actually transmits the next message to the port
     */
     private void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (log.isDebugEnabled()) log.debug("forwardToPort message: ["+m+"]");
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
        for (int i=0; i< len; i++)
            msg[i+offset] = (byte) m.getElement(i);

        // add trailer
        addTrailerToOutput(msg, len+offset, m);

        // and stream the bytes
        try {
            if (ostream != null) {
                if (log.isDebugEnabled()) {
                    String f = "write message: ";
                    for (int i = 0; i<msg.length; i++) f=f+Integer.toHexString(0xFF&msg[i])+" ";
                    log.debug(f);
                }
                ostream.write(msg);
            }
            else {
                // no stream connected
                log.warn("sendMessage: no connection established");
            }
        }
        catch (Exception e) {
            log.warn("sendMessage: Exception: "+e.toString());
        }
    }

    // methods to connect/disconnect to a source of data in a AbstractPortController
    private AbstractPortController controller = null;

    public boolean status() { return (ostream != null & istream != null);
    }

    Thread xmtThread = null;
    Runnable xmtRunnable = null;
    Thread rcvThread = null;

    /**
     * Make connection to existing PortController object.
     */
    public void connectPort(AbstractPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null)
            log.warn("connectPort: connect called while connected");
        else
            log.debug("connectPort invoked");
        controller = p;
        // and start threads
        xmtThread = new Thread(xmtRunnable = new Runnable() {
            public void run() { transmitLoop(); }
        });
        xmtThread.setName("Transmit");
        xmtThread.start();
        rcvThread = new Thread(new Runnable() {
            public void run() { receiveLoop(); }
        });
        rcvThread.setName("Receive");
        rcvThread.start();

    }

    /**
     * Break connection to existing PortController object. Once broken,
     * attempts to send via "message" member will fail.
     */
    public void disconnectPort(AbstractPortController p) {
        istream = null;
        ostream = null;
        if (controller != p)
            log.warn("disconnectPort: disconnect called from non-connected AbstractPortController");
        controller = null;
    }

    // data members to hold the streams
    protected DataInputStream istream = null;
    protected OutputStream ostream = null;


    /**
     * Handle incoming characters.  This is a permanent loop,
     * looking for input messages in character form on the
     * stream connected to the PortController via <code>connectPort</code>.
     * Terminates with the input stream breaking out of the try block.
     */
    public void receiveLoop() {
        log.debug("receiveLoop starts");
        while (true) {   // loop permanently, stream close will exit via exception
            try {
                handleOneIncomingReply();
            }
            catch (java.io.IOException e) {
                log.warn("run: Exception: "+e.toString());
            }
        }
    }

    abstract protected AbstractMRReply newReply();
    abstract protected boolean endOfMessage(AbstractMRReply r);

    /**
     * Dummy routine, which can be filled by protocols that
     * have to skip some start-of-message characters
     */
    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {}

    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        int i;
        for (i = 0; i < msg.maxSize; i++) {
            byte char1 = istream.readByte();
            msg.setElement(i, char1);
            if (endOfMessage(msg)) break;
        }
    }

    /**
     * (This is public for testing purposes)
     * @throws IOException
     */
    public void handleOneIncomingReply() throws java.io.IOException {
        // we sit in this until the message is complete, relying on
        // threading to let other stuff happen

        // Create message off the right concrete class
        AbstractMRReply msg = newReply();

        // wait for start if needede
        waitForStartOfReply(istream);

        // message exists, now fill it
        loadChars(msg, istream);

        // message is complete, dispatch it !!
        if (log.isDebugEnabled()) log.debug("dispatch reply of length "+msg.getNumDataElements());
        switch (mCurrentState) {
        case WAITMSGREPLYSTATE: {
            // update state, and notify to continue
            synchronized (xmtRunnable) {
                mCurrentState = NOTIFIEDSTATE;
                xmtRunnable.notify();
            }
            break;
        }
        case WAITREPLYINPROGMODESTATE: {
            // entering programming mode
            mCurrentMode = PROGRAMINGMODE;
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
            // update state, and notify to continue
            synchronized (xmtRunnable) {
                mCurrentState = OKSENDMSGSTATE;
                xmtRunnable.notify();
            }
            break;
        }
        default:
            log.error("reply complete in unexpected state: "
                        +mCurrentState);
        }
        // forward the message to the registered recipients,
        // which includes the communications monitor
        // return a notification via the Swing event queue to ensure end
        Runnable r = new RcvNotifier(msg, mLastSender, this);
        javax.swing.SwingUtilities.invokeLater(r);
    }

    /**
     * Internal class to remember the Reply object and destination
     * listener with a reply is received.
     */
    class RcvNotifier implements Runnable {
        AbstractMRReply mMsg;
        AbstractMRListener mDest;
        AbstractMRTrafficController mTC;
        RcvNotifier(AbstractMRReply pMsg, AbstractMRListener pDest,
                    AbstractMRTrafficController pTC) {
            mMsg = pMsg;
            mDest = pDest;
            mTC = pTC;
        }
        public void run() {
            log.debug("Delayed rcv notify starts");
            mTC.notifyReply(mMsg, mDest);
        }
    }

    /**
     * Internal class to remember the Message object and destination
     * listener when a message is queued for notification.
     */
    class XmtNotifier implements Runnable {
        AbstractMRMessage mMsg;
        AbstractMRListener mDest;
        AbstractMRTrafficController mTC;
        XmtNotifier(AbstractMRMessage pMsg, AbstractMRListener pDest,
                    AbstractMRTrafficController pTC) {
            mMsg = pMsg;
            mDest = pDest;
            mTC = pTC;
        }
        public void run() {
            log.debug("Delayed xmt notify starts");
            mTC.notifyMessage(mMsg, mDest);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractMRTrafficController.class.getName());
}


/* @(#)AbstractMRTrafficController.java */

