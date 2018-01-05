package jmri.jmrix.sprog;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;
import jmri.jmrix.AbstractPortController;
import jmri.jmrix.sprog.SprogConstants.SprogState;
import jmri.jmrix.sprog.serialdriver.SerialDriverAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.SerialPortEventListener;

/**
 * Converts Stream-based I/O to/from Sprog messages. The "SprogInterface" side
 * sends/receives message objects. The connection to a SprogPortController is
 * via a pair of *Streams, which then carry sequences of characters for
 * transmission. Note that this processing is handled in an independent thread.
 * <p>
 * Updated January 2010 for gnu io (RXTX) - Andrew Berridge.
 * Removed Runnable implementation and methods for it.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SprogTrafficController implements SprogInterface, SerialPortEventListener {

    private SprogReply reply = new SprogReply();
    private boolean waitingForReply = false;
    SprogListener lastSender = null;
    private SprogState sprogState = SprogState.NORMAL;


    public SprogTrafficController(SprogSystemConnectionMemo adaptermemo) {
       memo = adaptermemo;
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
        }
    }

    @Override
    public synchronized void removeSprogListener(SprogListener l) {
        if (cmdListeners.contains(l)) {
            cmdListeners.removeElement(l);
        }
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
        if (log.isDebugEnabled()) {
            log.debug("Setting sprogState {}", s);
        }
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
                // if is message don't send it back to the originator!
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
        log.debug("notifyReply starts last sender: {}", lastSender);
        for (SprogListener listener : this.getCopyOfListeners()) {
            try {
                //if is message don't send it back to the originator!
                // skip forwarding to the last sender for now, we'll get them later
                if (lastSender != listener) {
                    log.debug("Notify listener: {} {}", listener, r.toString());
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
            log.debug("notify last sender: {}{}", lastSender, r.toString());
            lastSender.notifyReply(r);
        }
    }

    /**
     * Forward a preformatted message to the interface.
     */
    public void sendSprogMessage(SprogMessage m) {
        // stream to port in single write, as that's needed by serial
        try {
            if (ostream != null) {
                ostream.write(m.getFormattedMessage(sprogState));
            } else {
                // no stream connected
                log.warn("sendMessage: no connection established");
            }
        } catch (Exception e) {
            log.warn("sendMessage: Exception: ", e);
        }
    }

    /**
     * Forward a preformatted message to the actual interface (by calling
     * SendSprogMessage(SprogMessage) after notifying any listeners.
     * <p>
     * Notifies listeners.
     *
     * @param m         Message to send
     * @param replyTo   Who is sending the message
     */
    @Override
    public synchronized void sendSprogMessage(SprogMessage m, SprogListener replyTo) {

        if (waitingForReply) {
            try {
                log.debug("Waiting for a reply");
                wait(100); // Will wait until notify()ed or 100ms timeout
            } catch (InterruptedException e) {
                log.debug("waitingForReply interrupted");
            }
        }
        log.debug("Setting waitingForReply");
        waitingForReply = true;

        if (log.isDebugEnabled()) {
            log.debug("sendSprogMessage message: [{}]", m.toString(isSIIBootMode()));
        }
        // remember who sent this
        log.debug("Updating last sender");
        lastSender = replyTo;
        // notify all _other_ listeners
        notifyMessage(m, replyTo);
        this.sendSprogMessage(m);
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
        return msg.endNormalReply() || msg.endBootReply()
                || msg.endBootloaderReply(this.getSprogState());
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
    void handleOneIncomingReply() {
        // we get here if data has been received
        //fill the current reply with any data received
        int replyCurrentSize = this.reply.getNumDataElements();
        int i;
        for (i = replyCurrentSize; i < SprogReply.maxSize - replyCurrentSize; i++) {
            try {
                if (istream.available() == 0) {
                    break; //nothing waiting to be read
                }
                byte char1 = istream.readByte();
                this.reply.setElement(i, char1);

            } catch (Exception e) {
                log.warn("Exception in DATA_AVAILABLE state: " + e);
            }
            if (endReply(this.reply)) {
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
        synchronized (this) {
            log.debug("Clearing waitingForReply");
            waitingForReply = false;
            notify();
        }
        if (log.isDebugEnabled()) {
            log.debug("dispatch reply of length {}", this.reply.getNumDataElements());
        }
        {
            final SprogReply thisReply = this.reply;
            final SprogListener thisLastSender = this.lastSender;
            if (unsolicited) {
                log.debug("Unsolicited Reply");
                thisReply.setUnsolicited();
            }
            final SprogTrafficController thisTC = this;
            // return a notification via the queue to ensure end
            Runnable r = new Runnable() {
                SprogReply replyForLater = thisReply;
                SprogListener lastSenderForLater = thisLastSender;
                SprogTrafficController myTC = thisTC;

                @Override
                public void run() {
                    log.debug("Delayed notify starts {}", replyForLater.toString());
                    myTC.notifyReply(replyForLater, lastSenderForLater);
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);
        }

        //Create a new reply, ready to be filled
        this.reply = new SprogReply();
    }

    private final static Logger log = LoggerFactory.getLogger(SprogTrafficController.class);

}
