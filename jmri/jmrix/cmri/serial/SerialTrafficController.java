// SerialTrafficController.java

package jmri.jmrix.cmri.serial;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

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
 * @version	$Revision: 1.14 $
 */
public class SerialTrafficController extends AbstractMRTrafficController implements SerialInterface {

    public SerialTrafficController() {
        super();

        // entirely poll driven, so reduce interval
        mWaitBeforePoll = 25;  // default = 25
        
        // clear the array of SerialNodes
        for (int i=0; i<=MAXNODE; i++) {
            nodeArray[i] = null;
            mustInit[i] = true;
        }
    }

    // The methods to implement the SerialInterface

    public synchronized void addSerialListener(SerialListener l) {
        this.addListener(l);
    }

    public synchronized void removeSerialListener(SerialListener l) {
        this.removeListener(l);
    }

// remove this code when SerialLight is operational - obsoleted and doesn't belong here anyway
    /**
     * Public method to set a C/MRI Serial Output bit
     *     Note: systemName is of format CNnnnBxxxx where
     *              "nnn" is the serial node number (0 - 127)
     *              "xxxx' is the bit number within that node (1 thru number of defined bits)
     *           state is 'true' for 0, 'false' for 1
     *     The bit is transmitted to the C/MRI hardware immediately before the
     *           next poll packet is sent.
     */
    public void setSerialOutput(String systemName, boolean state) {
        // get the node and bit numbers
        SerialNode node = SerialAddress.getNodeFromSystemName(systemName);
        if ( node == null ) {
            log.error("bad SerialNode specification in SerialOutput system name:"+systemName);
            return;
        }
        int bit = SerialAddress.getBitFromSystemName(systemName);
        if ( bit == 0 ) {
            log.error("bad output bit specification in SerialOutput system name:"+systemName);
            return;
        }
        // set the bit
        node.setOutputBit(bit,state);
    }
// end of code to be removed
    
    private int numNodes = 0;       // Incremented as Serial Nodes are created and registered
                                    // Corresponds to next available address in nodeArray
    private static int MINNODE = 0;
    private static int MAXNODE = 127;
    private SerialNode[] nodeArray = new SerialNode[MAXNODE+1];  // numbering from 0
    private boolean[] mustInit = new boolean[MAXNODE+1]; 
    
    /** 
     *  Public method to register a Serial node
     */
     public void registerSerialNode(SerialNode node) {
        // no validity checking because at this point the node may not be fully defined
        nodeArray[numNodes] = node;
        numNodes ++;
    }
    
    /** 
     * Public method to identify a SerialNode from its node address
     *      Note:   'ua' is the node address, numbered from 0.
     *              Returns 'null' if a SerialNode with the specified address
     *                  was not found
     */
    public SerialNode getNodeFromAddress(int ua) {
        for (int i=0; i<numNodes; i++) {
            if (nodeArray[i].getNodeAddress() == ua) {
                return(nodeArray[i]);
            }
        }
    	return (null);
    }
    
    /** 
     *  Public method to return the first Serial node
     */
     public SerialNode getFirstSerialNode() {
        aNodeIndex = 0;
        return nodeArray[aNodeIndex];
    }
    
    int aNodeIndex = 128;   // used by getFirstSerialNode and getNextSerialNode to
                            //    cycle through the serial nodes    
    /** 
     *  Public method to return the next Serial node
     *     Note:  returns null if there is no 'next Serial node'
     */
     public SerialNode getNextSerialNode() {
        aNodeIndex ++;
        if (aNodeIndex >= numNodes) {
            return null;
        }
        return nodeArray[aNodeIndex];
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
    
    int curSerialNodeIndex = 0;   // cycles over defined nodes when pollMessage is called
    /** 
     *  Handles initialization, output and polling for C/MRI Serial Nodes 
     *      from within the running thread
     */
    protected AbstractMRMessage pollMessage() {
        if (numNodes<=0) return null;
        // ensure that each node is initialized
        if (mustInit[curSerialNodeIndex]) {
            SerialMessage m = nodeArray[curSerialNodeIndex].createInitPacket();
            log.debug("send init message: "+m);
            mustInit[curSerialNodeIndex] = false;
            m.setTimeout(2000);  // wait for init to finish (milliseconds)
            return m;
        }
        // send Output packet if needed
        synchronized (this) {
            // if need to send, do so
            if (nodeArray[curSerialNodeIndex].mustSend()) {
                log.debug("request write command to send");
                nodeArray[curSerialNodeIndex].resetMustSend();
                SerialMessage m = nodeArray[curSerialNodeIndex].createOutPacket();
                m.setTimeout(50);  // no need to wait for output to answer
                return m;
            }
        }
        // poll for Sensor input if needed
        if ( nodeArray[curSerialNodeIndex].sensorsActive() ) {
            // Some sensors are active for this node, issue poll
            SerialMessage m = SerialMessage.getPoll(
                                nodeArray[curSerialNodeIndex].getNodeAddress());
            curSerialNodeIndex ++;
            if (curSerialNodeIndex==numNodes) curSerialNodeIndex = 0;
            return m;
        }
        else {
            // no Sensors (inputs) are active for this node
            curSerialNodeIndex ++;
            if (curSerialNodeIndex==numNodes) curSerialNodeIndex = 0;
            return null;
        }
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
        log.error("Not using endOfMessage, should not be called");
        return false;
    }

    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        int i;
        for (i = 0; i < msg.maxSize; i++) {
            byte char1 = istream.readByte();
            if (char1 == 0x03) break;           // check before DLE handling
            if (char1 == 0x10) char1 = istream.readByte();
            msg.setElement(i, char1&0xFF);
        }
    }

    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
        // loop looking for the start character
        while (istream.readByte()!=0x02) {}
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

//  this method is obsoleted by multiple node extension
//      It is called in SerialDriverAdapter.java, and can be eliminated when
//          that module is updated for multiple serial nodes.
    static public void setInitMessage(SerialMessage s) {
    }
// end obsolete code

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTrafficController.class.getName());
}

/* @(#)SerialTrafficController.java */

