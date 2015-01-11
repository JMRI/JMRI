// SerialTrafficController.java

package jmri.jmrix.cmri.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRNodeTrafficController;

import java.io.DataInputStream;

/**
 * Converts Stream-based I/O to/from C/MRI serial messages.
 * <P>
 * The "SerialInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a SerialPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the
 * necessary state in each message.
 * <P>
 * Handles initialization, polling, output, and input for multiple Serial Nodes.
 *
 * @author	Bob Jacobsen  Copyright (C) 2003
 * @author      Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @version	$Revision$
 */
public class SerialTrafficController extends AbstractMRNodeTrafficController implements SerialInterface {


    public SerialTrafficController() {
        super();
        
        // set node range
        init (0, 127);
        
        // entirely poll driven, so reduce interval
        mWaitBeforePoll = 5;  // default = 25

    }

    // The methods to implement the SerialInterface

    public synchronized void addSerialListener(SerialListener l) {
        this.addListener(l);
    }

    public synchronized void removeSerialListener(SerialListener l) {
        this.removeListener(l);
    }

    /**
     *  Public method to set up for initialization of a Serial node
     */
     public void initializeSerialNode(SerialNode node) {
        synchronized (this) {
            // find the node in the registered node list
            for (int i=0; i<getNumNodes(); i++) {
                if (getNode(i) == node) {
                    // found node - set up for initialization
                    setMustInit(i, true);
                    return;
                }
            }
        }
    }


    protected AbstractMRMessage enterProgMode() {
        log.warn("enterProgMode doesnt make sense for C/MRI serial");
        return null;
    }
    protected AbstractMRMessage enterNormalMode() {
        // can happen during error recovery, null is OK
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

    int curSerialNodeIndex = 0;   // cycles over defined nodes when pollMessage is called
    /**
     *  Handles initialization, output and polling for C/MRI Serial Nodes
     *      from within the running thread
     */
    protected synchronized AbstractMRMessage pollMessage() {
        // ensure validity of call
        if (getNumNodes()<=0) return null;
        
        int previousPollPointer = curSerialNodeIndex;
        updatePollPointer(); // service next node next

        // ensure that each node is initialized        
        if (getMustInit(curSerialNodeIndex)) {
            setMustInit(curSerialNodeIndex, false);
            AbstractMRMessage m = getNode(curSerialNodeIndex).createInitPacket();
            log.debug("send init message: "+m);
            m.setTimeout(500);  // wait for init to finish (milliseconds)
            return m;
        }
        // send Output packet if needed
        if (getNode(curSerialNodeIndex).mustSend()) {
            log.debug("request write command to send");
            getNode(curSerialNodeIndex).resetMustSend();
            AbstractMRMessage m = getNode(curSerialNodeIndex).createOutPacket();
            m.setTimeout(2);  // no need to wait for output to answer
            // reset poll pointer update, so next increment will poll from here
            curSerialNodeIndex = previousPollPointer;
            return m;
        }
        // poll for Sensor input
        if ( getNode(curSerialNodeIndex).getSensorsActive() ) {
            // Some sensors are active for this node, issue poll
            SerialMessage m = SerialMessage.getPoll(
                                getNode(curSerialNodeIndex).getNodeAddress());
            return m;
        }
        else {
            // no Sensors (inputs) are active for this node
            return null;
        }
    }
    
    /**
     * Update the curSerialNodeIndex so next node polled next time
     */
    private void updatePollPointer() {
        curSerialNodeIndex ++;
        if (curSerialNodeIndex>=getNumNodes()) {
            curSerialNodeIndex = 0;
        }
    }

    protected synchronized void handleTimeout(AbstractMRMessage m,AbstractMRListener l) {
        // don't use super behavior, as timeout to init, transmit message is normal

        // inform node, and if it resets then reinitialize        
        if (getNode(curSerialNodeIndex).handleTimeout(m,l)) 
            setMustInit(curSerialNodeIndex, true);
        
    }
    
    protected synchronized void resetTimeout(AbstractMRMessage m) {
        // don't use super behavior, as timeout to init, transmit message is normal

        // and inform node
        getNode(curSerialNodeIndex).resetTimeout(m);
        
    }
    
    protected AbstractMRListener pollReplyHandler() {
        return mSensorManager;
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

    static volatile protected SerialTrafficController self = null;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                        justification="temporary until mult-system; only set at startup")
    protected void setInstance() { self = this; }

    protected AbstractMRReply newReply() { return new SerialReply(); }

    protected boolean endOfMessage(AbstractMRReply msg) {
        // our version of loadChars doesn't invoke this, so it shouldn't be called
        log.error("Not using endOfMessage, should not be called");
        return false;
    }

    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        int i;
        for (i = 0; i < msg.maxSize(); i++) {
            byte char1 = readByteProtected(istream);
            if (char1 == 0x03) break;           // check before DLE handling
            if (char1 == 0x10) char1 = readByteProtected(istream);
            msg.setElement(i, char1&0xFF);
        }
    }

    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
        // loop looking for the start character
        while (readByteProtected(istream)!=0x02) {}
    }

    /**
     * Add header to the outgoing byte stream.
     * @param msg  The output byte stream
     * @return next location in the stream to fill
     */
    protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
        msg[0] = (byte) 0xFF;
        msg[1] = (byte) 0xFF;
        msg[2] = (byte) 0x02;  // STX
        return 3;
    }

    /**
     * Add trailer to the outgoing byte stream.
     * @param msg  The output byte stream
     * @param offset the first byte not yet used
     */
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
        msg[offset] = 0x03;  // etx
    }

    /**
     * Determine how much many bytes the entire
     * message will take, including space for header and trailer
     * @param m  The message to be sent
     * @return Number of bytes
     */
    protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        int cr = 4;
        return len+cr;
    }

    static Logger log = LoggerFactory.getLogger(SerialTrafficController.class.getName());
}

/* @(#)SerialTrafficController.java */

