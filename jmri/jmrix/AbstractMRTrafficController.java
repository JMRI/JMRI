// AbstractMRTrafficController.java

package jmri.jmrix;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;
import com.sun.java.util.collections.*;

/**
 * Abstract base for TrafficControllers in a Message/Reply protocol.
 *
 * @author			Bob Jacobsen  Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
abstract public class AbstractMRTrafficController {

    public AbstractMRTrafficController() {
        if (log.isDebugEnabled()) log.debug("setting instance: "+this);
        mCurrentState = NORMAL;
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

    protected AbstractMRListener lastSender = null;

    protected int mCurrentState;
    public static final int NORMAL=1;
    public static final int PROGRAMING=2;

    protected void notifyReply(AbstractMRReply r) {
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
        if (lastSender != null) forwardReply(lastSender, r);
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
            xmtRunnable.notify();
        }
        log.debug("just notified transmit thread");
    }

    private void transmitLoop() {
        log.debug("transmitLoop starts");
        while(true) {
            // wait for something to send
            try {
                synchronized(xmtRunnable) {
                    xmtRunnable.wait(20000);
                }
            } catch (InterruptedException e) { log.error("transmitLoop interrupted"); }
            log.debug("transmit loop past wait");
            // can have timed out, or could be woken
            // see if anything ready to go
            synchronized(self) {
                if (msgQueue.size()!=0) {
                    // yes, something to do
                    log.debug("transmit loop has something to do");
                    AbstractMRMessage m = (AbstractMRMessage)msgQueue.getFirst();
                    msgQueue.removeFirst();
                    AbstractMRListener l = (AbstractMRListener)listenerQueue.getFirst();
                    listenerQueue.removeFirst();
                    forwardToPort(m, l);
                } else {
                    // nothing to transmit
                }
            }
        }
    }

    /**
     * Actually transmits the next message to the port
     */
     private void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (log.isDebugEnabled()) log.debug("forwardToPort message: ["+m+"]");
        // remember who sent this
        lastSender = reply;

        // notify all _other_ listeners
        notifyMessage(m, reply);

        // stream to port in single write, as that's needed by serial
        int len = m.getNumDataElements();
        int cr = 0;
        if (! m.isBinary()) cr = 1;  // space for return

        byte msg[] = new byte[len+cr];

        for (int i=0; i< len; i++)
            msg[i] = (byte) m.getElement(i);
        if (! m.isBinary()) msg[len] = 0x0d;
        try {
            if (ostream != null) {
                if (log.isDebugEnabled()) log.debug("write message: "+msg);
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
     * Break connection to existing NcePortController object. Once broken,
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
     * (This is public for testing purposes)
     * @throws IOException
     */
    public void handleOneIncomingReply() throws java.io.IOException {
        // we sit in this until the message is complete, relying on
        // threading to let other stuff happen

        // Create message off the right concrete class
        AbstractMRReply msg = newReply();
        // message exists, now fill it
        int i;
        for (i = 0; i < msg.maxSize; i++) {
            byte char1 = istream.readByte();
            msg.setElement(i, char1);
            if (endOfMessage(msg)) break;
        }

        // message is complete, dispatch it !!
        if (log.isDebugEnabled()) log.debug("dispatch reply of length "+i);
        {
            final AbstractMRReply thisMsg = msg;
            final AbstractMRTrafficController thisTC = this;
            // return a notification via the queue to ensure end
            Runnable r = new Runnable() {
                    AbstractMRReply msgForLater = thisMsg;
                    AbstractMRTrafficController myTC = thisTC;
                    public void run() {
                        log.debug("Delayed notify starts");
                        myTC.notifyReply(msgForLater);
                    }
                };
            javax.swing.SwingUtilities.invokeLater(r);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractMRTrafficController.class.getName());
}


/* @(#)AbstractMRTrafficController.java */

