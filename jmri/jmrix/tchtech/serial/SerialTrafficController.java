/*
 * SerialTrafficController.java
 *
 * Created on August 17, 2007, 8:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial;

/**
 *
 * "!" Amended By Tim Hatch "!"
 */

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import jmri.jmrix.tchtech.serial.nodeconfig.NodeConfigFrame;
import java.io.DataInputStream;

/**
 * Converts Stream-based I/O to/from "!"TCH Technology"!" serial messages.
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
     * Public method to set a TCH Technology Serial Output bit
     *     Note: systemName is of format HNnnnBxxxx where
     *              "nnn" is the serial node number (0 - 255)
     *              "xxxx' is the bit number within that node (1 thru number of defined bits)
     *           state is 'true' for 0, 'false' for 1
     *     The bit is transmitted to the TCH Technology hardware immediately before the
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
    private static int MAXNODE = 256;
    private SerialNode[] nodeArray = new SerialNode[MAXNODE+1];  // numbering from 0
    private boolean[] mustInit = new boolean[MAXNODE+1];

    //private int numInput;

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
     *      Note:   'na' is the node address, numbered from 0.
     *              Returns 'null' if a SerialNode with the specified address
     *                  was not found
     */
    public SerialNode getNodeFromAddress(int Addr) {//(int na)
        for (int i=0; i<numNodes; i++) {
            if (nodeArray[i].getNodeAddress() == Addr) {//na
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
        log.warn("enterProgMode doesnt make sense for NIC serial"); //"!"
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
     *  Handles initialization, output and polling for "!"TCH Technology"!" Serial Nodes
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
            log.debug("send init message: "+m);
            m.setTimeout(2000);  // wait for init to finish (milliseconds)
            return m;
        }
        // send Output packet if needed
        if (nodeArray[curSerialNodeIndex].mustSend()) {
            log.debug("request write command to send");
            nodeArray[curSerialNodeIndex].resetMustSend();
            SerialMessage m = nodeArray[curSerialNodeIndex].createOutPacket();
            m.setTimeout(50);  // no need to wait for output to answer
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
        // don't use super behavior, as timeout to init, transmit message is normal

        // inform node, and if it resets then reinitialize        
        if (nodeArray[curSerialNodeIndex].handleTimeout(m)) 
            mustInit[curSerialNodeIndex] = true;
        
    }
    
    protected void resetTimeout(AbstractMRMessage m) {
        // don't use super behavior, as timeout to init, transmit message is normal

        // and inform node
        nodeArray[curSerialNodeIndex].resetTimeout(m);
        
    }
    
    protected AbstractMRListener pollReplyHandler() {
        return mSensorManager;
    }

    /**&-
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
        for (i = 0; i < msg.maxSize(); i++) {
          byte char1 = readByteProtected(istream);
            if (char1 == 0x03) break;           // check before DLE handling
            if (char1 == 0x10) char1 = readByteProtected(istream);
             msg.setElement(i,char1&0xFF);// msg.setElement(i,char1&0xFF);
        }
    }
  // protected int currentAddr= -1; // at startup, can't match
    //protected int incomingLength= 2;
    
    //protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        // get 1st byte, see if ending too soon
        //byte char1 = readByteProtected(istream);
        //if (char1 == 0x10) char1 = readByteProtected(istream);
        //msg.setElement(0, char1&0xFF);
        
        //if ( (char1&0xFF) != currentAddr) {
            // mismatch, end early
           // return;
        //}
        //if (incomingLength <= 1) return;
        //for (int i = 1; i< incomingLength; i++) {  // reading next four bytes
            //char1 = readByteProtected(istream);
           // msg.setElement(i, char1&0xFF);
        //}
    //}

    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
        // loop looking for the start character
        //while (readByteProtected(istream)!=0x02) {}
    }

    /**
     * Add header to the outgoing byte stream.
     * @param msg  The output byte stream
     * @return next location in the stream to fill
     */
    protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
        msg[0] = (byte) 0xAA;//sync byte
               return 1;
    }
  
    /**
     * Determine how much many bytes the entire
     * message will take, including space for header and trailer
     * @param m  The message to be sent
     * @return Number of bytes
     */
    protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        int cr = 2;
        return len+cr;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTrafficController.class.getName());
}

/* @(#)SerialTrafficController.java */

