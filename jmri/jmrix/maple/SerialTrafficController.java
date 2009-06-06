// SerialTrafficController.java

package jmri.jmrix.maple;

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
 * @author	Bob Jacobsen  Copyright (C) 2003, 2008
 * @author      Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @author Bob Jacobsen, adapt to use for Maple 2008
 *
 * @version	$Revision: 1.4 $
 * @since 2.3.7
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
    
    // initialization not needed ever
    protected boolean getMustInit(int i) { return false; }
    
    /**
     *  Handles initialization, output and polling for C/MRI Serial Nodes
     *      from within the running thread
     */
    protected synchronized AbstractMRMessage pollMessage() {
        // ensure validity of call
        if (getNumNodes()<=0) return null;
        
        // move to a new node
        curSerialNodeIndex ++;
        if (curSerialNodeIndex>=getNumNodes()) {
            curSerialNodeIndex = 0;
        }
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
            return m;
        }
        // poll for Sensor input
        if ( getNode(curSerialNodeIndex).getSensorsActive() ) {
            // Some sensors are active for this node, issue poll
            SerialMessage m = SerialMessage.getPoll(
                                getNode(curSerialNodeIndex).getNodeAddress());
            if (curSerialNodeIndex>=getNumNodes()) curSerialNodeIndex = 0;
            return m;
        }
        else {
            // no Sensors (inputs) are active for this node
            return null;
        }
    }

    protected void handleTimeout(AbstractMRMessage m) {
        // don't use super behavior, as timeout to init, transmit message is normal

        // inform node, and if it resets then reinitialize        
        if (getNode(curSerialNodeIndex).handleTimeout(m)) 
            setMustInit(curSerialNodeIndex, true);
        
    }
    
    protected void resetTimeout(AbstractMRMessage m) {
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

    static protected SerialTrafficController self = null;
    protected void setInstance() { self = this; }

    protected AbstractMRReply newReply() { return new SerialReply(); }

    protected boolean endOfMessage(AbstractMRReply msg) {
        // our version of loadChars doesn't invoke this, so it shouldn't be called
        log.error("Not using endOfMessage, should not be called");
        return false;
    }

    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        int i;
        boolean first = true;
        for (i = 0; i < msg.maxSize()-1; i++) {
            byte char1 = readByteProtected(istream);
            msg.setElement(i, char1&0xFF);
            if (first) {
                first = false;
                if (log.isDebugEnabled()) log.debug("start message with "+char1);
            }
            if (char1 == 0x03) {  // normal message
                // get checksum bytes and end
                if (log.isDebugEnabled()) log.debug("ETX ends message");
                char1 = readByteProtected(istream);
                msg.setElement(i+1, char1&0xFF);
                char1 = readByteProtected(istream);
                msg.setElement(i+2, char1&0xFF);
                break;           // end of message
            }
            if (char1 == 0x06) { // ACK OK
                // get station, command and end
                if (log.isDebugEnabled()) log.debug("ACK ends message");
                char1 = readByteProtected(istream);  // byte 2
                msg.setElement(++i, char1&0xFF);
                char1 = readByteProtected(istream);  // byte 3
                msg.setElement(++i, char1&0xFF);
                char1 = readByteProtected(istream);  // byte 4
                msg.setElement(++i, char1&0xFF); 
                char1 = readByteProtected(istream);  // byte 5
                msg.setElement(++i, char1&0xFF);
                break;           // end of message
            }
            if (char1 == 0x15) { // NAK error
                // get station, command, error bytes and end
                if (log.isDebugEnabled()) log.debug("NAK ends message");
                char1 = readByteProtected(istream);  // byte 2
                msg.setElement(++i, char1&0xFF);
                char1 = readByteProtected(istream);  // byte 3
                msg.setElement(++i, char1&0xFF);
                char1 = readByteProtected(istream);  // byte 4
                msg.setElement(++i, char1&0xFF); 
                char1 = readByteProtected(istream);  // byte 5
                msg.setElement(++i, char1&0xFF);
                char1 = readByteProtected(istream);  // byte 6
                msg.setElement(++i, char1&0xFF);
                break;           // end of message
            }
        }
    }

    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
        // don't skip anything
    }

    /**
     * Add header to the outgoing byte stream.
     * @param msg  The output byte stream
     * @return next location in the stream to fill
     */
    protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
        return 0;
    }

    /**
     * Add trailer to the outgoing byte stream.
     * @param msg  The output byte stream
     * @param offset the first byte not yet used
     */
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
    }

    /**
     * Determine how much many bytes the entire
     * message will take, including space for header and trailer
     * @param m  The message to be sent
     * @return Number of bytes
     */
    protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        return len;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialTrafficController.class.getName());
}

/* @(#)SerialTrafficController.java */

