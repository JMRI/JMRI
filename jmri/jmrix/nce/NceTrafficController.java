// NceTrafficController.java

package jmri.jmrix.nce;

import jmri.jmrix.*;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Converts Stream-based I/O to/from NCE messages.  The "NceInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a NcePortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the
 * necessary state in each message.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.6 $
 */
public class NceTrafficController extends AbstractMRTrafficController implements NceInterface {

    public NceTrafficController() {
        super();
    }

    // The methods to implement the NceInterface

    public synchronized void addNceListener(NceListener l) {
        this.addListener(l);
    }

    public synchronized void removeNceListener(NceListener l) {
        this.removeListener(l);
    }


    /**
     * Forward a NceMessage to all registered NceInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((NceListener)client).message((NceMessage)m);
    }

    /**
     * Forward a NceReply to all registered NceInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((NceListener)client).reply((NceReply)m);
    }

    NceSensorManager mSensorManager = null;
    public void setSensorManager(NceSensorManager m) { mSensorManager = m; }
    protected AbstractMRMessage pollMessage() {
        if (mSensorManager == null) return null;
        else return mSensorManager.nextAiuPoll();
    }
    protected AbstractMRListener pollReplyHandler() {
        return mSensorManager;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendNceMessage(NceMessage m, NceListener reply) {
        sendMessage(m, reply);
    }

    protected AbstractMRMessage enterProgMode() {
        return NceMessage.getProgMode();
    }
    protected AbstractMRMessage enterNormalMode() {
        return NceMessage.getExitProgMode();
    }

    /**
     * static function returning the NceTrafficController instance to use.
     * @return The registered NceTrafficController instance for general use,
     *         if need be creating one.
     */
    static public NceTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new NceTrafficController object");
            self = new NceTrafficController();
        }
        return self;
    }

    static protected NceTrafficController self = null;
    protected void setInstance() { self = this; }

    protected AbstractMRReply newReply() { return new NceReply(); }

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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTrafficController.class.getName());
}


/* @(#)NceTrafficController.java */

