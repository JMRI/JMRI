package jmri.jmrix.qsi;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;
import jmri.jmrix.qsi.serialdriver.SerialDriverAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.SerialPort;

/**
 * Converts Stream-based I/O to/from QSI messages. The "QsiInterface" side
 * sends/receives message objects. The connection to a QsiPortController is via
 * a pair of *Streams, which then carry sequences of characters for
 * transmission. Note that this processing is handled in an independent thread.
 * <p>
 * Messages to and from the programmer are in a packet format. In both
 * directions, every message starts with 'S' and ends with 'E'. These are
 * handled automatically, and are not included in the QsiMessage and QsiReply
 * content.
 *
 * @author	Bob Jacobsen Copyright (C) 2007, 2008
 */
public class QsiTrafficController implements QsiInterface, Runnable {

    /**
     * Create a new QsiTrafficController instance.
     */
    public QsiTrafficController() {
    }

// The methods to implement the QsiInterface
    protected Vector<QsiListener> cmdListeners = new Vector<QsiListener>();

    @Override
    public boolean status() {
        return (ostream != null && istream != null);
    }

    @Override
    public synchronized void addQsiListener(QsiListener l) {
        // add only if not already registered
        if (l == null) {
            throw new java.lang.NullPointerException();
        }
        if (!cmdListeners.contains(l)) {
            cmdListeners.addElement(l);
        }
    }

    @Override
    public synchronized void removeQsiListener(QsiListener l) {
        if (cmdListeners.contains(l)) {
            cmdListeners.removeElement(l);
        }
    }

    /**
     * Forward a QsiMessage to all registered QsiInterface listeners.
     */
    @SuppressWarnings("unchecked")
    protected void notifyMessage(QsiMessage m, QsiListener notMe) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<QsiListener> v;
        synchronized (this) {
            v = (Vector<QsiListener>) cmdListeners.clone();
        }
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            QsiListener client = v.elementAt(i);
            if (notMe != client) {
                log.debug("notify client: {}", client);
                try {
                    client.message(m);
                } catch (Exception e) {
                    log.warn("notify: During dispatch to " + client + "\nException " + e);
                }
            }
        }
    }

    QsiListener lastSender = null;

    // Current QSI state
    public static final int NORMAL = 0;
    public static final int SIIBOOTMODE = 1;
    public static final int V4BOOTMODE = 2;

    private int qsiState = NORMAL;

    public int getQsiState() {
        return qsiState;
    }

    public void setQsiState(int s) {
        qsiState = s;
        if(controller instanceof SerialDriverAdapter) {
           if (s == V4BOOTMODE) {
               // enable flow control - required for QSI v4 bootloader
               ((SerialDriverAdapter)controller).setHandshake(SerialPort.FLOWCONTROL_RTSCTS_IN
                       | SerialPort.FLOWCONTROL_RTSCTS_OUT);

           } else {
               // disable flow control
               ((SerialDriverAdapter)controller).setHandshake(0);
           }
           log.debug("Setting qsiState {}", s);
       }
    }

    public boolean isNormalMode() {
        return qsiState == NORMAL;
    }

    public boolean isSIIBootMode() {
        return qsiState == SIIBOOTMODE;
    }

    public boolean isV4BootMode() {
        return qsiState == V4BOOTMODE;
    }

    @SuppressWarnings("unchecked")
    protected void notifyReply(QsiReply r) {
        // make a copy of the listener vector to synchronized (not needed for transmit?)
        Vector<QsiListener> v;
        synchronized (this) {
            v = (Vector<QsiListener>) cmdListeners.clone();
        }
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            QsiListener client = v.elementAt(i);
            log.debug("notify client: {}", client);
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

    /**
     * Forward a preformatted message to the actual interface.
     */
    @Override
    public void sendQsiMessage(QsiMessage m, QsiListener reply) {
        log.debug("sendQsiMessage message: [{}]", m);
        // remember who sent this
        lastSender = reply;

        // notify all _other_ listeners
        notifyMessage(m, reply);

        // stream to port in single write, as that's needed by serial
        int len = m.getNumDataElements();

        // space for carriage return if required
        int cr = 0;
        int start = 0;
        if (isSIIBootMode()) {
            cr = 1;
            start = 0;
        } else {
            cr = 3;  // 'S', CRC, 'E'
            start = 1;
        }

        byte msg[] = new byte[len + cr];

        byte crc = 0;

        for (int i = 0; i < len; i++) {
            msg[i + start] = (byte) m.getElement(i);
            crc ^= msg[i + start];
        }

        if (isSIIBootMode()) {
            msg[len] = 0x0d;
        } else {
            msg[0] = 'S';
            msg[len + cr - 2] = crc;
            msg[len + cr - 1] = 'E';
        }

        try {
            if (ostream != null) {
                if (log.isDebugEnabled()) {
                    log.debug("write message: {}", jmri.util.StringUtil.hexStringFromBytes(msg));
                }
                ostream.write(msg);
            } else {
                // no stream connected
                log.warn("sendMessage: no connection established");
            }
        } catch (Exception e) {
            log.warn("sendMessage: Exception: " + e.toString());
        }
    }

    // methods to connect/disconnect to a source of data in a LnPortController
    private QsiPortController controller = null;

    /**
     * Make connection to existing PortController object.
     */
    public void connectPort(QsiPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null) {
            log.warn("connectPort: connect called while connected");
        }
        controller = p;
    }

    /**
     * Break connection to existing QsiPortController object. Once broken,
     * attempts to send via "message" member will fail.
     */
    public void disconnectPort(QsiPortController p) {
        istream = null;
        ostream = null;
        if (controller != p) {
            log.warn("disconnectPort: disconnect called from non-connected LnPortController");
        }
        controller = null;
    }

    /**
     * static function returning the QsiTrafficController instance to use.
     *
     * @return The registered QsiTrafficController instance for general use, if
     *         need be creating one.
     * deprecated since 4.5.1
     */
    @Deprecated
    static public QsiTrafficController instance() {
        return null;
    }

    // data members to hold the streams
    DataInputStream istream = null;
    OutputStream ostream = null;

    /**
     * Handle incoming characters. This is a permanent loop, looking for input
     * messages in character form on the stream connected to the PortController
     * via <code>connectPort</code>. Terminates with the input stream breaking
     * out of the try block.
     */
    @Override
    public void run() {
        while (true) {   // loop permanently, stream close will exit via exception
            try {
                handleOneIncomingReply();
            } catch (java.io.IOException e) {
                log.warn("run: Exception: " + e.toString());
            }
        }
    }

    void handleOneIncomingReply() throws java.io.IOException {
          // we sit in this until the message is complete, relying on
        // threading to let other stuff happen

        // Create output message
        QsiReply msg = new QsiReply();
        // message exists, now fill it
        int i;
        for (i = 0; i < QsiReply.MAXSIZE; i++) {
            byte char1 = istream.readByte();
            if (log.isDebugEnabled()) {
                log.debug("   Rcv char: {}", jmri.util.StringUtil.twoHexFromInt(char1));
            }
            msg.setElement(i, char1);
            if (endReply(msg)) {
                break;
            }
        }

        // message is complete, dispatch it !!
        log.debug("dispatch reply of length {}",i);
        {
            final QsiReply thisMsg = msg;
            final QsiTrafficController thisTc = this;
            // return a notification via the queue to ensure end
            Runnable r = new Runnable() {
                QsiReply msgForLater = thisMsg;
                QsiTrafficController myTc = thisTc;

                @Override
                public void run() {
                    log.debug("Delayed notify starts");
                    myTc.notifyReply(msgForLater);
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);
        }
    }

    /*
     * Normal QSI replies will end with the prompt for the next command
     */
    boolean endReply(QsiReply msg) {
        if (endNormalReply(msg)) {
            return true;
        }
        return false;
    }

    boolean endNormalReply(QsiReply msg) {
        // Detect that the reply buffer ends with "E"
        // This should really be based on length....
        int num = msg.getNumDataElements();
        if (num >= 3) {
            // ptr is offset of last element in QsiReply
            int ptr = num - 1;
            if (msg.getElement(ptr) != 'E') {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(QsiTrafficController.class);

}
