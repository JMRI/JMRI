package jmri.jmrix.sprog;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.AbstractPortController;
import jmri.jmrix.sprog.SprogConstants.SprogState;
import jmri.jmrix.sprog.serialdriver.SerialDriverAdapter;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.SerialPortEventListener;

/**
 * Converts Stream-based I/O to/from Sprog messages. The "SprogInterface" side
 * sends/receives message objects. The connection to a SprogPortController is
 * via a pair of *Streams, which then carry sequences of characters for
 * transmission. Note that this processing is handled in an independent thread.
 * <p>
 * Rewritten during 4.11.x series. Create a high priority thread for the tc to
 * move everything off the swing thread. Use a blocking queue to handle
 * asynchronous messages from multiple sources.
 * 
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author	Andrew Crosland Copyright (C) 2018
 */
public class SprogTrafficController implements SprogInterface, SerialPortEventListener,
        Runnable {

    private SprogReply reply = new SprogReply();
    SprogListener lastSender = null;
    private SprogState sprogState = SprogState.NORMAL;
    private int lastId;

    private Thread tcThread;
    private final Object lock = new Object();
    private boolean replyAvailable = false;
    // Make this public so it can be overridden by a script for debug
    public int timeout = SprogConstants.TC_PROG_REPLY_TIMEOUT;
    
    /**
     * Create a new SprogTrafficController instance.
     *
     * @param adaptermemo the associated SystemConnectionMemo
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="SC_START_IN_CTOR", justification="done at end, waits for data")
    public SprogTrafficController(SprogSystemConnectionMemo adaptermemo) {
        memo = adaptermemo;

        // Set the timeout for communication with hardware
        resetTimeout();

        tcThread = new Thread(this);
        tcThread.setName("SPROG TC thread");
        tcThread.setPriority(Thread.MAX_PRIORITY-1);
        tcThread.setDaemon(true);
        log.debug("starting TC thread from {} in group {}", this, tcThread.getThreadGroup(), jmri.util.Log4JUtil.shortenStacktrace(new Exception("traceback"),6));
        tcThread.start();
    }

    // Methods to implement the Sprog Interface

    protected Vector<SprogListener> cmdListeners = new Vector<SprogListener>();

    @Override
    public boolean status() {
        return (ostream != null && istream != null);
    }

    @Override
    public synchronized void addSprogListener(SprogListener l) {
        // add only if not already registered
        if (l == null) {
            throw new java.lang.NullPointerException();
        }
        if (!cmdListeners.contains(l)) {
            cmdListeners.addElement(l);
            log.debug("SprogListener added to {} tc", memo.getUserName());
        }
    }

    @Override
    public synchronized void removeSprogListener(SprogListener l) {
        if (cmdListeners.contains(l)) {
            cmdListeners.removeElement(l);
        }
    }

    /**
     * Reset timeout to default depending on current mode
     */
    public void resetTimeout() {
        if (memo.getSprogMode() == SprogConstants.SprogMode.OPS) {
            timeout = SprogConstants.TC_OPS_REPLY_TIMEOUT;
        } else {
            timeout = SprogConstants.TC_PROG_REPLY_TIMEOUT;
        }
    }

    public void setTimeout(int t) {
        timeout = t;
    }
    
    public SprogState getSprogState() {
        return sprogState;
    }

    public void setSprogState(SprogState s) {
        this.sprogState = s;
        if (s == SprogState.V4BOOTMODE) {
            // enable flow control - required for sprog v4 bootloader
            getController().setHandshake(SerialPort.FLOWCONTROL_RTSCTS_IN
                    | SerialPort.FLOWCONTROL_RTSCTS_OUT);

        } else {
            // disable flow control
            // removed Jan 2010 - this stops SPROG from sending. Could cause problems with
            // serial Sprogs, but I have no way of testing:
            // getController().setHandshake(0);
        }
        log.debug("Setting sprogState {}", s);
    }

    public boolean isNormalMode() {
        return sprogState == SprogState.NORMAL;
    }

    public boolean isSIIBootMode() {
        return sprogState == SprogState.SIIBOOTMODE;
    }

    public boolean isV4BootMode() {
        return sprogState == SprogState.V4BOOTMODE;
    }

    @SuppressWarnings("unchecked")
    private synchronized Vector<SprogListener> getCopyOfListeners() {
        return (Vector<SprogListener>) cmdListeners.clone();

    }

    protected synchronized void notifyMessage(SprogMessage m, SprogListener originator) {
        for (SprogListener listener : this.getCopyOfListeners()) {
            try {
                // don't send it back to the originator!
                if (listener != originator) {
                    // skip forwarding to the last sender for now, we'll get them later
                    if (lastSender != listener) {
                        listener.notifyMessage(m);
                    }
                }
            } catch (Exception e) {
                log.warn("notify: During dispatch to {}\nException {}", listener, e.toString());
            }
        }
        // forward to the last listener who sent a message
        // this is done _second_ so monitoring can have already stored the reply
        // before a response is sent
        if (lastSender != null && lastSender != originator) {
            lastSender.notifyMessage(m);
        }
    }

    protected synchronized void notifyReply(SprogReply r) {
        for (SprogListener listener : this.getCopyOfListeners()) {
            try {
                // don't send message back to the originator!
                // skip forwarding to the last sender for now, we'll get them later
                if (lastSender != listener) {
                    listener.notifyReply(r);
                }

            } catch (Exception e) {
                log.warn("notify: During dispatch to {}\nException {}", listener, e.toString());
            }
        }
        // forward to the last listener who sent a message
        // this is done _second_ so monitoring can have already stored the reply
        // before a response is sent
        if (lastSender != null) {
            lastSender.notifyReply(r);
        }
    }

    protected synchronized void notifyReply(SprogReply r, SprogListener lastSender) {
//        log.debug("notifyReply starts last sender: {}", lastSender);
        for (SprogListener listener : this.getCopyOfListeners()) {
            try {
            //if is message don't send it back to the originator!
                // skip forwarding to the last sender for now, we'll get them later
                if (lastSender != listener) {
//                    log.debug("Notify listener: {} {}", listener, r.toString());
                    listener.notifyReply(r);
                }

            } catch (Exception e) {
                log.warn("notify: During dispatch to {}\nException: {}", listener, e.toString());
            }
        }
        
        // forward to the last listener who sent a message
        // this is done _second_ so monitoring can have already stored the reply
        // before a response is sent
        if (lastSender != null) {
//            log.debug("notify last sender: {}{}", lastSender, r.toString());
            lastSender.notifyReply(r);
        }
    }

    // A class to remember the message and who sent it
    static private class MessageTuple {
        private final SprogMessage message;
        private final SprogListener listener;
        
        public MessageTuple(SprogMessage m, SprogListener l) {
            message = m;
            listener = l;
        }
        
        // Copy constructor
        public MessageTuple(MessageTuple mt) {
            message = mt.message;
            listener = mt.listener;
        }
    }
    
    // The queue to hold messages being sent
    BlockingQueue<MessageTuple> sendQueue = new LinkedBlockingQueue<MessageTuple>();
        
    /**
     * Enqueue a preformatted message to be sent to the actual interface
     * 
     * @param m The message to be forwarded
     */
    public void sendSprogMessage(SprogMessage m) {
        log.debug("Add message to queue: [{}] id: {}", m.toString(isSIIBootMode()), m.getId());
        try {
            sendQueue.add(new MessageTuple(m, null));
        } catch (Exception e) {
            log.error("Could not add message to queue {}", e);
        }
    }

    /**
     * Enqueue a preformatted message to be sent to the actual interface
     *
     * @param m         Message to send
     * @param replyTo   Who is sending the message
     */
    @Override
    public synchronized void sendSprogMessage(SprogMessage m, SprogListener replyTo) {
        log.debug("Add message to queue: [{}] id: {}", m.toString(isSIIBootMode()), m.getId());
        try {
            sendQueue.add(new MessageTuple(m, replyTo));
        } catch (Exception e) {
            log.error("Could not add message to queue {}", e);
        }
    }

    /**
     * Block until a message is available from the queue, send it to the interface
     * and then block until reply is received or a timeout occurs. This will be 
     * a very long timeout to allow for page mode programming operations in SPROG
     * programmer mode.
     */
    @Override
    public void run() {
        MessageTuple messageToSend;
        log.debug("Traffic controller queuing thread starts");
        while (true) {
            try {
                messageToSend = new MessageTuple(sendQueue.take());
            } catch (InterruptedException e) {
                log.debug("Thread interrupted while dequeuing message to send");
                return;
            }
            log.debug("Message dequeued id: {}", messageToSend.message.getId());
            // remember who sent this
            lastSender = messageToSend.listener;
            lastId = messageToSend.message.getId();
            // notify all _other_ listeners
            notifyMessage(messageToSend.message, messageToSend.listener);
            replyAvailable = false;
            sendToInterface(messageToSend.message);
            log.debug("Waiting for a reply");
            try {
                synchronized (lock) {
                    lock.wait(timeout); // Wait for notify
                }
            } catch (InterruptedException e) {
                log.debug("waitingForReply interrupted");
            }
            if (!replyAvailable) {
                // Timed out
                log.warn("Timeout waiting for reply from hardware");
            } else {
                log.debug("Notified of reply");
            }
        }
    }

    /**
     * Forward a preformatted message to the interface.
     * 
     * @param m The message to be forwarded
     */
    public void sendToInterface(SprogMessage m) {
        // stream to port in single write, as that's needed by serial
        try {
            if (ostream != null) {
                ostream.write(m.getFormattedMessage(sprogState));
                log.debug("sendSprogMessage written to ostream");
            } else {
                // no stream connected
                log.warn("sendMessage: no connection established");
            }
        } catch (Exception e) {
            log.warn("sendMessage: Exception: ", e);
        }
    }

// methods to connect/disconnect to a source of data in a SprogPortController
    private AbstractPortController controller = null;

    /**
     * Make connection to existing PortController object.
     * 
     * @param p The port controller
     */
    public void connectPort(AbstractPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null) {
            log.warn("connectPort: connect called while connected");
        }
        controller = p;
    }

    /**
     * Get the port controller, as a SerialDriverAdapter.
     * 
     * @return the port controller
     */
    protected SerialDriverAdapter getController(){
       return (SerialDriverAdapter) controller;
    }

    /**
     * Break connection to existing SprogPortController object.
     * <p>
     * Once broken, attempts to send via "message" member will fail.
     * 
     * @param p the connection to break
     */
    public void disconnectPort(AbstractPortController p) {
        istream = null;
        ostream = null;
        if (controller != p) {
            log.warn("disconnectPort: disconnect called from non-connected SprogPortController");
        }
        controller = null;
    }

    /**
     * Static function returning the SprogTrafficController instance to use.
     *
     * @return The registered SprogTrafficController instance for general use,
     *         if need be creating one.
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SprogTrafficController instance() {
        return null;
    }

    static volatile protected SprogTrafficController self = null;

    public void setAdapterMemo(SprogSystemConnectionMemo adaptermemo) {
        memo = adaptermemo;
    }

    public SprogSystemConnectionMemo getAdapterMemo() {
        return memo;
    }

    private SprogSystemConnectionMemo memo = null;

    // data members to hold the streams
    DataInputStream istream = null;
    OutputStream ostream = null;

    boolean endReply(SprogReply msg) {
        return msg.endNormalReply() || msg.endBootReply();
    }

    private boolean unsolicited;

    /**
     * Respond to an event triggered by RXTX.
     * <p>
     * In this case we are only dealing with DATA_AVAILABLE but the other
     * events are left here for reference.
     *
     * @author AJB Jan 2010
     */
    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                log.debug("Data Available");
                handleOneIncomingReply();
                break;
            default:
                log.warn("Unhandled serial port event code: {}", event.getEventType());
                break;
        }
    }

    /**
     * Handle an incoming reply.
     */
    public void handleOneIncomingReply() {
        // we get here if data has been received and this method is explicitly invoked
        // fill the current reply with any data received
        int replyCurrentSize = reply.getNumDataElements();
        int i;
        for (i = replyCurrentSize; i < SprogReply.maxSize - replyCurrentSize; i++) {
            try {
                if (istream.available() == 0) {
                    break; // nothing waiting to be read
                }
                byte char1 = istream.readByte();
                reply.setElement(i, char1);

            } catch (Exception e) {
                log.warn("Exception in DATA_AVAILABLE state: {}", e);
            }
            if (endReply(reply)) {
                sendreply();
                break;
            }
        }
    }

    /**
     * Send the current reply - built using data from serialEvent.
     */
    private void sendreply() {
        //send the reply
        log.debug("dispatch reply of length {}", reply.getNumDataElements());
        if (unsolicited) {
            log.debug("Unsolicited Reply");
            reply.setUnsolicited();
        }
        // Insert the id
        reply.setId(lastId);
        notifyReply(reply, lastSender);
        log.debug("Notify() wait");
        replyAvailable = true;
        synchronized(lock) {
            lock.notifyAll();
        }

        //Create a new reply, ready to be filled
        reply = new SprogReply();
    }

    public void dispose(){
       tcThread.interrupt();
    }

    private final static Logger log = LoggerFactory.getLogger(SprogTrafficController.class);

}
