// SerialTrafficController.java

package jmri.jmrix.cmri.serial;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Converts Stream-based I/O to/from CMRI messages.  The "SerialInterface"
 * side sends/receives message objects.  The connection to
 * a SerialPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 *
 * @author    Bob Jacobsen  Copyright (C) 2001, 2002
 * @version   $Revision: 1.2 $
 */
public class SerialTrafficController implements SerialInterface, Runnable {

    public SerialTrafficController() {
        if (log.isDebugEnabled()) log.debug("setting instance: "+this);
        self=this;
    }


    // The methods to implement the SerialInterface

    protected Vector cmdListeners = new Vector();

    public boolean status() { return (ostream != null & istream != null);
    }

    public synchronized void addSerialListener(SerialListener l) {
        // add only if not already registered
        if (l == null) throw new java.lang.NullPointerException();
        if (!cmdListeners.contains(l)) {
            cmdListeners.addElement(l);
        }
    }

    public synchronized void removeSerialListener(SerialListener l) {
        if (cmdListeners.contains(l)) {
            cmdListeners.removeElement(l);
        }
    }


    /**
     * Forward a SerialMessage to all registered Serialnterface listeners.
     */
    protected void notifyMessage(SerialMessage m, SerialListener notMe) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector v;
        synchronized(this)
            {
                v = (Vector) cmdListeners.clone();
            }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            SerialListener client = (SerialListener) v.elementAt(i);
            if (notMe != client) {
                if (log.isDebugEnabled()) log.debug("notify client: "+client);
                try {
                    client.message(m);
                }
                catch (Exception e)
                    {
                        log.warn("notify: During dispatch to "+client+"\nException "+e);
                    }
            }
        }
    }

    SerialListener lastSender = null;

    protected void notifyReply(SerialReply r) {

        // make a copy of the listener vector to synchronized (not needed for transmit?)
        Vector v;
        synchronized(this)
            {
                v = (Vector) cmdListeners.clone();
            }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            SerialListener client = (SerialListener) v.elementAt(i);
            if (log.isDebugEnabled()) log.debug("notify client: "+client);
            try {
                client.reply(r);
            }
            catch (Exception e)
                {
                    log.warn("notify: During dispatch to "+client+"\nException "+e);
                }
        }

        // forward to the last listener who send a message
        // this is done _second_ so monitoring can have already stored the reply
        // before a response is sent
        if (lastSender != null) lastSender.reply(r);
    }


    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendSerialMessage(SerialMessage m, SerialListener reply) {
        if (log.isDebugEnabled()) log.debug("sendSerialMessage message: ["+m+"]");
        // remember who sent this
        lastSender = reply;

        // notify all _other_ listeners
        notifyMessage(m, reply);

        // stream to port in single write, as that's needed by serial
        int len = m.getNumDataElements();
        int cr = 4;    // space for control characters

        byte msg[] = new byte[len+cr];

        // add the start sequence
        msg[0] = (byte) 0xFF;
        msg[1] = (byte) 0xFF;
        msg[2] = (byte) 0x02;  // STX

        for (int i=0; i< len; i++)
            msg[i+3] = (byte) m.getElement(i);
        msg[len] = 0x03;  // etx
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

    // methods to connect/disconnect to a source of data in a LnPortController
    private SerialPortController controller = null;

    /**
     * Make connection to existing PortController object.
     */
    public void connectPort(SerialPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null)
            log.warn("connectPort: connect called while connected");
        controller = p;
    }

    /**
     * Break connection to existing SerialPortController object. Once broken,
     * attempts to send via "message" member will fail.
     */
    public void disconnectPort(SerialPortController p) {
        istream = null;
        ostream = null;
        if (controller != p)
            log.warn("disconnectPort: disconnect called from non-connected LnPortController");
        controller = null;
    }

    /**
     * static function returning the SerialTrafficController instance to use.
     * @return The registered SerialTrafficController instance for general use,
     *         if need be creating one.
     */
    static public SerialTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new SerialTrafficController object");
            self = new SerialTrafficController();
        }
        return self;
    }

    static protected SerialTrafficController self = null;

    // data members to hold the streams
    DataInputStream istream = null;
    OutputStream ostream = null;


    /**
     * Handle incoming characters.  This is a permanent loop,
     * looking for input messages in character form on the
     * stream connected to the PortController via <code>connectPort</code>.
     * Terminates with the input stream breaking out of the try block.
     */
    public void run() {
        while (true) {   // loop permanently, stream close will exit via exception
            try {
                handleOneIncomingReply();
            }
            catch (java.io.IOException e) {
                log.warn("run: Exception: "+e.toString());
            }
        }
    }

    void handleOneIncomingReply() throws java.io.IOException {
        // we sit in this until the message is complete, relying on
        // threading to let other stuff happen

        // Create output message
        SerialReply msg = new SerialReply();
        // loop looking for the start character
        while (istream.readByte()!=0x02) {}

        // message exists, now read it into the buffer
        int i;
        for (i = 0; i < SerialReply.maxSize; i++) {
            byte char1 = istream.readByte();
            if (char1 == 0x03) break;
            msg.setElement(i, char1);
        }

        // message is complete, dispatch it !!
        if (log.isDebugEnabled()) log.debug("dispatch reply of length "+i);
        {
            final SerialReply thisMsg = msg;
            final SerialTrafficController thisTC = this;
            // return a notification via the queue to ensure end
            Runnable r = new Runnable() {
                    SerialReply msgForLater = thisMsg;
                    SerialTrafficController myTC = thisTC;
                    public void run() {
                        log.debug("Delayed notify starts");
                        myTC.notifyReply(msgForLater);
                    }
                };
            javax.swing.SwingUtilities.invokeLater(r);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTrafficController.class.getName());
}

/* @(#)SerialTrafficController.java */
