// SerialTrafficController.java

package jmri.jmrix.cmri.serial;

import jmri.jmrix.*;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Converts Stream-based I/O to/from C/MRI serial messages.  The "SerialInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a SerialPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the
 * necessary state in each message.
 *
 * @author			Bob Jacobsen  Copyright (C) 2003
 * @version			$Revision: 1.4 $
 */
public class SerialTrafficController extends AbstractMRTrafficController implements SerialInterface {

    public SerialTrafficController() {
        super();
    }

    // The methods to implement the SerialInterface

    public synchronized void addSerialListener(SerialListener l) {
        this.addListener(l);
    }

    public synchronized void removeSerialListener(SerialListener l) {
        this.removeListener(l);
    }

    /**
     * Do we need to send an output message?
     * <P>
     * This is currently written for only one node,
     * so only need to remember one bit of info
     */
    private boolean mustSend = false;

    int highByte = 0;

    int[] outputArray = new int[48];

    /**
     * Manage the outputs
     */
    public void setOutputState(int number, boolean closed) {
        int loc = (number-1)/8;
        if (loc>highByte) highByte=loc;
        int bit = 1<<((number-1) % 8);
        // update that bit
        int oldValue = outputArray[loc];
        if (closed) outputArray[loc] |= bit;
        else outputArray[loc] &= (~bit);
        if (log.isDebugEnabled()) log.debug("setOutputState n="+number+" loc="+loc+" bit="+bit);
        // force a send next time if the value changed
        synchronized (this) {
            if (oldValue != outputArray[loc])
                mustSend = true;
        }
    }

    protected AbstractMRMessage enterProgMode() {
        log.error("enterProgMode doesnt make sense for C/MRI serial");
        return null;
    }
    protected AbstractMRMessage enterNormalMode() {
        log.error("enterNormalMode doesnt make sense for C/MRI serial");
        return null;
    }

    /**
     * Forward a SerialMessage to all registered SerialInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((SerialListener)client).message((SerialMessage)m);
    }

    /**
     * Forward a SerialReply to all registered SerialInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((SerialListener)client).reply((SerialReply)m);
    }

    SerialSensorManager mSensorManager = null;
    public void setSensorManager(SerialSensorManager m) { mSensorManager = m; }
    protected AbstractMRMessage pollMessage() {
        synchronized (this) {
            // if need to send, do so
            if (mustSend) {
                mustSend = false;
                log.debug("request write commend to send");
                return nextWrite();
            }
        }
        if (mSensorManager == null) return null;
        else return mSensorManager.nextPoll();
    }
    protected AbstractMRListener pollReplyHandler() {
        return mSensorManager;
    }

    /**
     * Create the write message with the current states
     */
    protected SerialMessage nextWrite() {
        SerialMessage m = new SerialMessage(highByte+3);  // UA, 'T', plus 1 OB even if highByte == 0
        m.setElement(0,(byte)0x41);
        m.setElement(1,(byte)0x54);
        for (int i = 0; i<=highByte; i++) {
            m.setElement(i+2, outputArray[i]);
        }
        if (log.isDebugEnabled()) log.debug("nextWrite with highByte="+highByte+" is "+m);
        m.setTimeout(25); // short delay, as no reply expected in C/MRI
        return m;
    }
    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendSerialMessage(SerialMessage m, SerialListener reply) {
        sendMessage(m, reply);
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
    protected void setInstance() { self = this; }

    protected AbstractMRReply newReply() { return new SerialReply(); }

    protected boolean endOfMessage(AbstractMRReply msg) {
        // detect that the reply buffer ends with "COMMAND: " (note ending space)
        int num = msg.getNumDataElements();
        if ( num >= 9) {
            // ptr is offset of last element in NceReply
            int ptr = num-1;
            if (msg.getElement(ptr-1) != ':') return false;
            if (msg.getElement(ptr)   != ' ') return false;
            if (msg.getElement(ptr-2) != 'D') return false;
            return true;
        }
        else return false;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTrafficController.class.getName());
}


/* @(#)SerialTrafficController.java */

