// EasyDccTrafficController.java

package jmri.jmrix.easydcc;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Converts Stream-based I/O to/from EasyDcc messages.  The "EasyDccInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a EasyDccPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the
 * necessary state in each message. To do this, there's a thread
 * that's working through a queue of messages to send, making
 * transitions as needed.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 */
public class EasyDccTrafficController implements EasyDccInterface, Runnable {
    
    public EasyDccTrafficController() {
        if (log.isDebugEnabled()) log.debug("setting instance: "+this);
        self=this;
    }
    
    
    // The methods to implement the EasyDccInterface
    
    protected Vector cmdListeners = new Vector();
    
    public boolean status() { return (ostream != null & istream != null);
    }
    
    public synchronized void addEasyDccListener(EasyDccListener l) {
        // add only if not already registered
        if (l == null) throw new java.lang.NullPointerException();
			if (!cmdListeners.contains(l)) {
                            cmdListeners.addElement(l);
                        }
    }
    
    public synchronized void removeEasyDccListener(EasyDccListener l) {
        if (cmdListeners.contains(l)) {
            cmdListeners.removeElement(l);
				}
    }
    
    
    /**
     * Forward a EasyDccMessage to all registered EasyDccInterface listeners.
     */
    protected void notifyMessage(EasyDccMessage m, EasyDccListener notMe) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector v;
        synchronized(this)
            {
                v = (Vector) cmdListeners.clone();
            }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            EasyDccListener client = (EasyDccListener) v.elementAt(i);
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
    
    EasyDccListener lastSender = null;
    
    protected void notifyReply(EasyDccReply r) {
        
        // make a copy of the listener vector to synchronized (not needed for transmit?)
        Vector v;
        synchronized(this)
            {
                v = (Vector) cmdListeners.clone();
            }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            EasyDccListener client = (EasyDccListener) v.elementAt(i);
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
    public void sendEasyDccMessage(EasyDccMessage m, EasyDccListener reply) {
        if (log.isDebugEnabled()) log.debug("sendEasyDccMessage message: ["+m+"]");
        // remember who sent this
        lastSender = reply;
        
        // notify all _other_ listeners
        notifyMessage(m, reply);
        
        // stream to port in single write, as that's needed by serial
        int len = m.getNumDataElements();
        int cr = 1;  // space for carriage return linefeed
        
        byte msg[] = new byte[len+cr];
        
        for (int i=0; i< len; i++)
            msg[i] = (byte) m.getElement(i);
        msg[len] = 0x0d;
        
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
    private EasyDccPortController controller = null;
    
    /**
     * Make connection to existing PortController object.
     */
    public void connectPort(EasyDccPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null)
            log.warn("connectPort: connect called while connected");
        controller = p;
    }
    
    /**
     * Break connection to existing EasyDccPortController object. Once broken,
     * attempts to send via "message" member will fail.
     */
    public void disconnectPort(EasyDccPortController p) {
        istream = null;
        ostream = null;
        if (controller != p)
            log.warn("disconnectPort: disconnect called from non-connected LnPortController");
        controller = null;
    }
    
    /**
     * static function returning the EasyDccTrafficController instance to use.
     * @return The registered EasyDccTrafficController instance for general use,
     *         if need be creating one.
     */
    static public EasyDccTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new EasyDccTrafficController object");
            self = new EasyDccTrafficController();
        }
        return self;
    }
    
    static protected EasyDccTrafficController self = null;
    
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
        EasyDccReply msg = new EasyDccReply();
        // message exists, now fill it
        int i;
        for (i = 0; i < EasyDccReply.maxSize; i++) {
            byte char1 = istream.readByte();
            msg.setElement(i, char1);
            if (endReply(msg)) break;
        }
        
        // message is complete, dispatch it !!
        if (log.isDebugEnabled()) log.debug("dispatch reply of length "+i);
        {
            final EasyDccReply thisMsg = msg;
            final EasyDccTrafficController thisTC = this;
            // return a notification via the queue to ensure end
            Runnable r = new Runnable() {
                    EasyDccReply msgForLater = thisMsg;
                    EasyDccTrafficController myTC = thisTC;
                    public void run() {
                        log.debug("Delayed notify starts");
                        myTC.notifyReply(msgForLater);
                    }
                };
            javax.swing.SwingUtilities.invokeLater(r);
        }
    }
    
    boolean endReply(EasyDccReply msg) {
        // detect that the reply buffer ends with "\n"
        int index = msg.getNumDataElements()-1;
        if (msg.getElement(index) != 0x0d) return false;
        else return true;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccTrafficController.class.getName());
}


/* @(#)EasyDccTrafficController.java */

