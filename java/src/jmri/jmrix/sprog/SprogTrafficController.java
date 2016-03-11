// SprogTrafficController.java
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
 *
 * Updated January 2010 for gnu io (RXTX) - Andrew Berridge. Comments tagged
 * with "AJB" indicate changes or observations by me
 *
 * Removed Runnable implementation and methods for it
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public class SprogTrafficController implements SprogInterface, SerialPortEventListener {

    private SprogReply reply = new SprogReply();

    private boolean waitingForReply = false;
    SprogListener lastSender = null;

    private SprogState sprogState = SprogState.NORMAL;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    // Ignore FindBugs warnings as there can only be one instance at present
    public SprogTrafficController() {
        if (log.isDebugEnabled()) {
            log.debug("setting instance: " + this);
        }
        self = this;
    }

// The methods to implement the SprogInterface
    protected Vector<SprogListener> cmdListeners = new Vector<SprogListener>();

    public boolean status() {
        return (ostream != null & istream != null);
    }

    public synchronized void addSprogListener(SprogListener l) {
        // add only if not already registered
        if (l == null) {
            throw new java.lang.NullPointerException();
        }
        if (!cmdListeners.contains(l)) {
            cmdListeners.addElement(l);
        }
    }

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
            SerialDriverAdapter.instance().setHandshake(SerialPort.FLOWCONTROL_RTSCTS_IN
                    | SerialPort.FLOWCONTROL_RTSCTS_OUT);

        } else {
            // disable flow control
            //AJB - removed Jan 2010 - this stops SPROG from sending. Could cause problems with
            //serial Sprogs, but I have no way of testing: 
            //SerialDriverAdapter.instance().setHandshake(0);
        }
        if (log.isDebugEnabled()) {
            log.debug("Setting sprogState " + s);
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

    protected void notifyMessage(SprogMessage m, SprogListener originator) {
        for (SprogListener listener : this.getCopyOfListeners()) {
            try {
                //don't send it back to the originator!
                if (listener != originator) {
                    // skip forwarding to the last sender for now, we'll get them later
                    if (lastSender != listener) {
                        listener.notifyMessage(m);
                    }
                }
            } catch (Exception e) {
                log.warn("notify: During dispatch to " + listener + "\nException " + e);
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
                //if is message don't send it back to the originator!
                // skip forwarding to the last sender for now, we'll get them later
                if (lastSender != listener) {
                    listener.notifyReply(r);
                }

            } catch (Exception e) {
                log.warn("notify: During dispatch to " + listener + "\nException " + e);
            }
        }
        // forward to the last listener who sent a message
        // this is done _second_ so monitoring can have already stored the reply
        // before a response is sent
        if (lastSender != null) {
            lastSender.notifyReply(r);
        }
    }

    /**
     * Forward a preformatted message to the interface
     *
     * @param m
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
            log.warn("sendMessage: Exception: " + e.toString());
        }
    }

    /**
     * Forward a preformatted message to the actual interface (by calling
     * SendSprogMessage(SprogMessage) after notifying any listeners Notifies
     * listeners
     */
    public synchronized void sendSprogMessage(SprogMessage m, SprogListener replyTo) {

        if (waitingForReply) {
            try {
                wait(100);  //Will wait until notify()ed or 100ms timeout
            } catch (InterruptedException e) {
            }
        }
        waitingForReply = true;

        if (log.isDebugEnabled()) {
            log.debug("sendSprogMessage message: [" + m + "]");
        }
        // remember who sent this
        lastSender = replyTo;
        // notify all _other_ listeners
        notifyMessage(m, replyTo);
        this.sendSprogMessage(m);

    }

    // methods to connect/disconnect to a source of data in a LnPortController
    private AbstractPortController controller = null;

    /**
     * Make connection to existing PortController object.
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
     * Break connection to existing SprogPortController object. Once broken,
     * attempts to send via "message" member will fail.
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
     * static function returning the SprogTrafficController instance to use.
     *
     * @return The registered SprogTrafficController instance for general use,
     *         if need be creating one.
     */
    static public SprogTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) {
                log.debug("creating a new SprogTrafficController object");
            }
            self = new SprogTrafficController();
        }
        return self;
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

    private final static Logger log = LoggerFactory.getLogger(SprogTrafficController.class.getName());

    /**
     * serialEvent - respond to an event triggered by RXTX. In this case we are
     * only dealing with DATA_AVAILABLE but the other events are left here for
     * reference. AJB Jan 2010
     */
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
                handleOneIncomingReply();
                break;
        }
    }

    /**
     * Handle an incoming reply
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
     * Send the current reply - built using data from serialEvent
     */
    private void sendreply() {
        //send the reply
        synchronized (this) {
            waitingForReply = false;
            notify();
        }
        if (log.isDebugEnabled()) {
            log.debug("dispatch reply of length " + this.reply.getNumDataElements());
        }
        {
            final SprogReply thisReply = this.reply;
            if (unsolicited) {
                log.debug("Unsolicited Reply");
                thisReply.setUnsolicited();
            }
            final SprogTrafficController thisTC = this;
            // return a notification via the queue to ensure end
            Runnable r = new Runnable() {
                SprogReply replyForLater = thisReply;
                SprogTrafficController myTC = thisTC;

                public void run() {
                    log.debug("Delayed notify starts");
                    myTC.notifyReply(replyForLater);
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);
        }

        //Create a new reply, ready to be filled
        this.reply = new SprogReply();

    }
}


/* @(#)SprogTrafficController.java */
