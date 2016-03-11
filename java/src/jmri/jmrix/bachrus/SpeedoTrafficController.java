// SpeedoTrafficController.java
package jmri.jmrix.bachrus;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from speedo messages. The "SpeedoInterface" side
 * sends/receives message objects. The connection to a SpeedoPortController is
 * via a pair of *Streams, which then carry sequences of characters for
 * transmission. Note that this processing is handled in an independent thread.
 *
 * Updated January 2010 for gnu io (RXTX) - Andrew Berridge. Comments tagged
 * with "AJB" indicate changes or observations by me
 *
 * Removed Runnable implementation and methods for it
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author	Andrew Crosland Copyright (C) 2010
 * @version	$Revision$
 */
public class SpeedoTrafficController implements SpeedoInterface, SerialPortEventListener {

    private SpeedoReply reply = new SpeedoReply();

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    // Ignore FindBugs warning as we can only have on SPROG instance at this time
    public SpeedoTrafficController() {
        self = this;
    }

    // The methods to implement the SpeedoInterface
    protected Vector<SpeedoListener> cmdListeners = new Vector<SpeedoListener>();

    public boolean status() {
        return (ostream != null & istream != null);
    }

    public synchronized void addSpeedoListener(SpeedoListener l) {
        // add only if not already registered
        if (l == null) {
            throw new java.lang.NullPointerException();
        }
        if (!cmdListeners.contains(l)) {
            cmdListeners.addElement(l);
        }
    }

    public synchronized void removeSpeedoListener(SpeedoListener l) {
        if (cmdListeners.contains(l)) {
            cmdListeners.removeElement(l);
        }
    }

    SpeedoListener lastSender = null;

    @SuppressWarnings("unchecked")
    protected void notifyReply(SpeedoReply r) {
        // make a copy of the listener vector to synchronized (not needed for transmit?)
        Vector<SpeedoListener> v;
        synchronized (this) {
            v = (Vector<SpeedoListener>) cmdListeners.clone();
        }
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            SpeedoListener client = v.elementAt(i);
            try {
                // skip forwarding to the last sender for now, we'll get them later
                if (lastSender != client) {
                    client.reply(r);
                }
            } catch (Exception e) {
                log.warn("notify: During dispatch to " + client + "\nException " + e);
            }
        }

        // forward to the last listener who send a message
        // this is done _second_ so monitoring can have already stored the reply
        // before a response is sent
        if (lastSender != null) {
            lastSender.reply(r);
        }
    }

    // methods to connect/disconnect to a source of data in a LnPortController
    private SpeedoPortController controller = null;

    /**
     * Make connection to existing PortController object.
     */
    public void connectPort(SpeedoPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null) {
            log.warn("connectPort: connect called while connected");
        }
        controller = p;
    }

    /**
     * Break connection to existing SpeedoPortController object. Once broken,
     * attempts to send via "message" member will fail.
     */
    public void disconnectPort(SpeedoPortController p) {
        istream = null;
        ostream = null;
        if (controller != p) {
            log.warn("disconnectPort: disconnect called from non-connected LnPortController");
        }
        controller = null;
    }

    /**
     * static function returning the SpeedoTrafficController instance to use.
     *
     * @return The registered SpeedoTrafficController instance for general use,
     *         if need be creating one.
     */
    static public SpeedoTrafficController instance() {
        if (self == null) {
            self = new SpeedoTrafficController();
        }
        return self;
    }

    static volatile protected SpeedoTrafficController self = null;

    // data members to hold the streams
    DataInputStream istream = null;
    OutputStream ostream = null;


    /*
     * Speedo replies end with ";"
     */
    boolean endReply(SpeedoReply msg) {
        // Detect that the reply buffer ends with ";"
        int num = msg.getNumDataElements();
        // ptr is offset of last element in SpeedoReply
        int ptr = num - 1;
        if (msg.getElement(ptr) != ';') {
            return false;
        }
        unsolicited = true;
        return true;
    }

    private boolean unsolicited;
    private final static Logger log = LoggerFactory.getLogger(SpeedoTrafficController.class.getName());

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
                // we get here if data has been received
                //fill the current reply with any data received
                int replyCurrentSize = this.reply.getNumDataElements();
                int i;
                for (i = replyCurrentSize; i < SpeedoReply.maxSize - replyCurrentSize; i++) {
                    try {
                        if (istream.available() == 0) {
                            break; //nothing waiting to be read
                        }
                        byte char1 = istream.readByte();
                        this.reply.setElement(i, char1);

                    } catch (Exception e) {
                    }
                    if (endReply(this.reply)) {
                        sendreply();
                        break;
                    }
                }

                break;
        }
    }

    /**
     * Send the current reply - built using data from seriaEvent
     */
    private void sendreply() {
        //send the reply
        {
            final SpeedoReply thisReply = this.reply;
            if (unsolicited) {
                thisReply.setUnsolicited();
            }
            final SpeedoTrafficController thisTC = this;
            // return a notification via the queue to ensure end
            Runnable r = new Runnable() {

                SpeedoReply msgForLater = thisReply;
                SpeedoTrafficController myTC = thisTC;

                public void run() {
                    myTC.notifyReply(msgForLater);
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);
        }
        //Create a new reply, ready to be filled
        this.reply = new SpeedoReply();
    }
}


/* @(#)SpeedoTrafficController.java */
