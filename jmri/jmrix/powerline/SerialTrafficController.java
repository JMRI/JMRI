// SerialTrafficController.java

package jmri.jmrix.powerline;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

import java.io.DataInputStream;

/**
 * Converts Stream-based I/O to/from powerline-device serial messages.
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
 * @author	Bob Jacobsen  Copyright (C) 2003, 2006, 2008
 * @author      Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @version	$Revision: 1.1 $
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
     * Public method to set an Output bit
     *     Note: systemName is of format CNnnnBxxxx where
     *              "nnn" is the serial node number (0 - 127)
     *              "xxxx' is the bit number within that node (1 thru number of defined bits)
     *           state is 'true' for 0, 'false' for 1
     *     The bit is transmitted to the hardware immediately before the
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
    private static int MAXNODE = 255;
    private SerialNode[] nodeArray = new SerialNode[MAXNODE+1];  // numbering from 0
    private boolean[] mustInit = new boolean[MAXNODE+1];

    /**
     *  Public method to register a Serial node
     */
     public void registerSerialNode(SerialNode node) {
        synchronized (this) {
            // no validity checking because at this point the node may not be fully defined
            nodeArray[numNodes] = node;
            mustInit[numNodes] = true;
            numNodes++;
        }
    }

    /**
     *  Public method to set up for initialization of a Serial node
     */
     public void initializeSerialNode(SerialNode node) {
        synchronized (this) {
            // find the node in the registered node list
            for (int i=0; i<numNodes; i++) {
                if (nodeArray[i] == node) {
                    // found node - set up for initialization
                    mustInit[i] = true;
                    return;
                }
            }
        }
    }

    /**
     * Public method to identify a SerialNode from its node address
     *      Note:   'addr' is the node address, numbered from 0.
     *              Returns 'null' if a SerialNode with the specified address
     *                  was not found
     */
    public SerialNode getNodeFromAddress(int addr) {
        for (int i=0; i<numNodes; i++) {
            if (nodeArray[i].getNodeAddress() == addr) {
                return(nodeArray[i]);
            }
        }
    	return (null);
    }

    /**
     *  Public method to delete a Serial node by node address
     */
     public synchronized void deleteSerialNode(int nodeAddress) {
        // find the serial node
        int index = 0;
        for (int i=0; i<numNodes; i++) {
            if (nodeArray[i].getNodeAddress() == nodeAddress) {
                index = i;
            }
        }
        if (index==curSerialNodeIndex) {
            log.warn("Deleting the serial node active in the polling loop");
        }
        // Delete the node from the node list
        numNodes --;
        if (index<numNodes) {
            // did not delete the last node, shift 
            for (int j=index; j<numNodes; j++) {
                nodeArray[j] = nodeArray[j+1];
            }
        }
        nodeArray[numNodes] = null;
    }

    /**
     *  Public method to return the Serial node with a given index
     *  Note:   To cycle through all nodes, begin with index=0, 
     *              and increment your index at each call.  
     *          When index exceeds the number of defined nodes,
     *              this routine returns 'null'.
     */
     public SerialNode getSerialNode(int index) {
        if (index >= numNodes) {
            return null;
        }
        return nodeArray[index];
    }

    protected AbstractMRMessage enterProgMode() {
        log.warn("enterProgMode doesnt make sense for powerline serial");
        return null;
    }
    protected AbstractMRMessage enterNormalMode() {
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
     *  Handles initialization, output and polling
     *      from within the running thread
     */
    protected synchronized AbstractMRMessage pollMessage() {
        // ensure validity of call
        if (numNodes<=0) return null;
        
        // move to a new node
        curSerialNodeIndex ++;
        if (curSerialNodeIndex>=numNodes) {
            curSerialNodeIndex = 0;
        }
        // ensure that each node is initialized        
        if (mustInit[curSerialNodeIndex]) {
            mustInit[curSerialNodeIndex] = false;
            SerialMessage m = nodeArray[curSerialNodeIndex].createInitPacket();
            if (m!=null) { // Devices don't need this yet
                log.debug("send init message: "+m);
                m.setTimeout(2000);  // wait for init to finish (milliseconds)
                return m;
            }   // else fall through to continue
        }
        // send Output packet if needed
        if (nodeArray[curSerialNodeIndex].mustSend()) {
            log.debug("request write command to send");
            SerialMessage m = nodeArray[curSerialNodeIndex].createOutPacket();
            nodeArray[curSerialNodeIndex].resetMustSend();
            m.setTimeout(500);
            return m;
        }
        // poll for Sensor input
        if ( nodeArray[curSerialNodeIndex].sensorsActive() ) {
            // Some sensors are active for this node, issue poll
            SerialMessage m = SerialMessage.getPoll(
                                nodeArray[curSerialNodeIndex].getNodeAddress());
            if (curSerialNodeIndex>=numNodes) curSerialNodeIndex = 0;
            return m;
        }
        else {
            // no Sensors (inputs) are active for this node
            return null;
        }
    }

    protected void handleTimeout(AbstractMRMessage m) {
        // inform node, and if it resets then reinitialize 
        if (nodeArray[curSerialNodeIndex] != null)      
            if (nodeArray[curSerialNodeIndex].handleTimeout(m)) 
                mustInit[curSerialNodeIndex] = true;
        else
            log.warn("Timeout can't be handled due to missing node index="+curSerialNodeIndex);
    }
    
    protected void resetTimeout(AbstractMRMessage m) {
        // inform node
        nodeArray[curSerialNodeIndex].resetTimeout(m);
        
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
    
    protected int currentAddr= -1; // at startup, can't match
    protected int incomingLength = 0;
    
    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        // get 1st byte, see if ending too soon
        byte char1 = readByteProtected(istream);
        msg.setElement(0, char1&0xFF);
        if ( (char1&0xFF) != currentAddr) {
            // mismatch, end early
            return;
        }
        if (incomingLength <= 1) return;
        for (int i = 1; i < incomingLength; i++) {  // reading next four bytes
            char1 = readByteProtected(istream);
            msg.setElement(i, char1&0xFF);
        }
    }

    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
        // does nothing
    }

    /**
     * Add header to the outgoing byte stream.
     * @param msg  The output byte stream
     * @return next location in the stream to fill
     */
    protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
        return 0;  // Do nothing
    }

    /**
     * Although this protocol doesn't use a trailer, 
     * we implement this method to set the 
     * expected reply length and address for this message.
     *
     * @param msg  The output byte stream
     * @param offset the first byte not yet used
     * @param m the original message
     */
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
        incomingLength = ((SerialMessage)m).getResponseLength();
        currentAddr = ((SerialMessage)m).getAddr();
        return;
    }

    /**
     * Determine how much many bytes the entire
     * message will take, including space for header and trailer
     * @param m  The message to be sent
     * @return Number of bytes
     */
    protected int lengthOfByteStream(AbstractMRMessage m) {
        return 5; // All are 5 bytes long
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTrafficController.class.getName());
}

/* @(#)SerialTrafficController.java */

