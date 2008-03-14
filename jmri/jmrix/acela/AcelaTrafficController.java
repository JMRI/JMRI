// AcelaTrafficController.java

package jmri.jmrix.acela;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

import java.io.DataInputStream;

/**
 * Converts Stream-based I/O to/from Acela messages.
 * <P>
 * The "SerialInterface" side sends/receives message objects.
 * <P>
 * The connection to an AcelaPortController is via a pair of *Streams,
 * which then carry sequences of characters for transmission.
 * Note that this processing is handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the
 * necessary state in each message.
 * <P>
 * Handles initialization, polling, output, and input for multiple Serial Nodes.
 *
 * @author	Bob Jacobsen  Copyright (C) 2003
 * @author      Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @version	$Revision: 1.5 $
 *
 * @author	Bob Coleman Copyright (C) 2007. 2008
 *              Based on CMRI serial example, modified to establish Acela support. 
 */
public class AcelaTrafficController extends AbstractMRTrafficController implements AcelaInterface {

    public AcelaTrafficController() {
        super();

        // entirely poll driven, so reduce interval
        mWaitBeforePoll = 25;  // default = 25
    	setAllowUnexpectedReply(true);

        // clear the array of AcelaNodes
        for (int i=0; i<MAXNODE; i++) {
            nodeArray[i] = null;
            mustInit[i] = false;  // Do not normally need to init Acela nodes.
        }

        needToPollNodes = true;   // Need to poll and create corresponding nodes
        needToInitAcelaNetwork = true;   // Need to poll and create corresponding nodes
        needToCreateNodesState = 0; // Need to initialize system and then poll
        acelaTrafficControllerState = false;                //  Flag to indicate which state we are in: 
                                                            //  false == Initiallizing Acela Network
                                                            //  true == Polling Sensors
    }

    // The methods to implement the AcelaInterface

    public synchronized void addAcelaListener(AcelaListener l) {
        this.addListener(l);
    }

    public synchronized void removeAcelaListener(AcelaListener l) {
        this.removeListener(l);
    }

    private int numNodes = 0;               // Incremented as Acela Nodes are created and registered
                                            // Corresponds to next available address in nodeArray
    private int currentOutputAddress = -1;   // Incremented as Acela Nodes are created and registered
                                            // Corresponds to next available output address in nodeArray
                                            // Start at -1 to avoid issues with bit address 0
    private int currentSensorAddress = -1;   // Incremented as Acela Nodes are created and registered
                                            // Corresponds to next available sensor address in nodeArray
                                            // Start at -1 to avoid issues with bit address 0

    private boolean acelaTrafficControllerState = false;    //  Flag to indicate which state we are in: 
                                                            //  false == Initiallizing Acela Network
                                                            //  true == Polling Sensors
    private boolean needToPollNodes = true;   //  Flag to indicate that nodes have not yet been created
    private boolean needToInitAcelaNetwork = true;   //  Flag to indicate that Acela network must be initialized
    private int needToCreateNodesState = 0;     //  Need to do a few things:
                                                //      Reset Acela Network
                                                //      Set Acela Netwrok Online
                                                //      Poll for Acela Nodes (and create and register the nodes)
    
    private boolean acelaSensorsState = false;    //  Flag to indicate whether we have an active sensor and therefore need to poll: 
                                                   //  false == No active sensor
                                                   //  true == Active sensor, need to poll sensors
    
    private static int SPECIALNODE = 0;         //  Needed to initialize system

    static final int MINNODE = 0;
    static final int MAXNODE = 1024;     //  Artifical limit but economically reasonable

    private AcelaNode[] nodeArray = new AcelaNode[MAXNODE];  // numbering from 0
    private boolean[] mustInit = new boolean[MAXNODE];

    /**
     *  Public method to get minimum address of an Acela node
     */
    public int getMinimumNodeAddress() {
        return MINNODE;
    }
    
    /**
     *  Public method to get maximum number of Acela nodes
     */
    public int getMaximumNumberOfNodes() {
        return MAXNODE;
    }
    
    public boolean getAcelaTrafficControllerState() {
        return acelaTrafficControllerState;
    }
    
    public void setAcelaTrafficControllerState(boolean newstate) {
        acelaTrafficControllerState = newstate;
    }
    
    public boolean getAcelaSensorsState() {
        return acelaSensorsState;
    }
    
    public void setAcelaSensorsState(boolean newstate) {
        acelaSensorsState = newstate;
    }
    
    public boolean getNeedToPollNodes() {
        return needToPollNodes;
    }
    
    public void setNeedToPollNodes(boolean newstate) {
        needToPollNodes = newstate;
    }
    
    /**
     *  Public method to register a Acela node
     */
     public void registerAcelaNode(AcelaNode node) {
        synchronized (this) {
            // no validity checking because at this point the node may not be fully defined
            if (numNodes < MAXNODE) {
                nodeArray[numNodes] = node;
                mustInit[numNodes] = false;  // Do not normally need to init Acela nodes.
                numNodes++;
                if (node.getNumOutputBitsPerCard() == 0) {
                    node.setStartingOutputAddress(-1);
                    node.setEndingOutputAddress(-1);
                } else {
                    if (currentOutputAddress == -1) {  // Need to use -1 to correctly identify bit address 0
                        currentOutputAddress = 0;
                    }
                    node.setStartingOutputAddress(currentOutputAddress);
                    currentOutputAddress = currentOutputAddress + node.getNumOutputBitsPerCard()-1;
                    node.setEndingOutputAddress(currentOutputAddress);
                    currentOutputAddress = currentOutputAddress + 1;
                }
                if (node.getNumSensorBitsPerCard() == 0) {
                    node.setStartingSensorAddress(-1);
                    node.setEndingSensorAddress(-1);
                } else {
                    if (currentSensorAddress == -1) {  // Need to use -1 to correctly identify bit address 0
                        currentSensorAddress = 0;
                    }
                    node.setStartingSensorAddress(currentSensorAddress);
                    currentSensorAddress = currentSensorAddress + node.getNumSensorBitsPerCard()-1;
                    node.setEndingSensorAddress(currentSensorAddress);
                    currentSensorAddress = currentSensorAddress + 1;
                }
            } else {
                log.warn("Trying to register too many Acela nodes");
            }
        }
    }

    /**
     *  Public method to set up for initialization of a Acela node
     */
     public void initializeAcelaNode(AcelaNode node) {
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
     * Public method to identify a AcelaNode from its bit address
     *      Note:   nodeAddress is numbered from 0.
     *              Returns '-1' if a AcelaNode with the specified address
     *                  was not found
     */
    public int lookupAcelaNodeAddress(int bitAddress, boolean isSensor) {
        for (int i=0; i<numNodes; i++) { 
            if (isSensor) {
                if ((bitAddress >= nodeArray[i].getStartingSensorAddress())
                    && (bitAddress <= nodeArray[i].getEndingSensorAddress())) {
                    return(i);
                }
            } else {
                if ((bitAddress >= nodeArray[i].getStartingOutputAddress())
                    && (bitAddress <= nodeArray[i].getEndingOutputAddress())) {
                    return(i);
                }
            }
        }
    	return (-1);
    }

    /**
     * Public method to identify a AcelaNode from its node address
     *      Note:   nodeAddress is numbered from 0.
     *              Returns 'null' if a AcelaNode with the specified address
     *                  was not found
     */
    public AcelaNode getNodeFromAddress(int nodeAddress) {
        for (int i=0; i<numNodes; i++) {
            if (nodeArray[i].getNodeAddress() == nodeAddress) {
                return(nodeArray[i]);
            }
        }
    	return (null);
    }

    /**
     *  Public method to delete a AcelaNode by node address
     */
     public synchronized void deleteAcelaNode(int nodeAddress) {
        // find the serial node
        int index = 0;
        for (int i=0; i<numNodes; i++) {
            if (nodeArray[i].getNodeAddress() == nodeAddress) {
                index = i;
            }
        }
        if (index==curAcelaNodeIndex) {
            log.warn("Deleting the Acela node active in the polling loop");
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
     *  Public method to return the Acela node with a given index
     *  Note:   To cycle through all nodes, begin with index=0, 
     *              and increment your index at each call.  
     *          When index exceeds the number of defined nodes,
     *              this routine returns 'null'.
     */
     public AcelaNode getAcelaNode(int index) {
        if (index >= numNodes) {
            return null;
        }
        return nodeArray[index];
    }

    protected AbstractMRMessage enterProgMode() {
        log.warn("enterProgMode does NOT make sense for Acela serial");
        return null;
    }

    protected AbstractMRMessage enterNormalMode() {
        // can happen during error recovery, null is OK
        return null;
    }

    /**
     * Forward a AcelaMessage to all registered AcelaInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((AcelaListener)client).message((AcelaMessage)m);
    }

    /**
     * Forward a AcelaReply to all registered AcelaInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((AcelaListener)client).reply((AcelaReply)m);
    }

    AcelaSensorManager mSensorManager = null;

    public void setSensorManager(AcelaSensorManager m) { 
    	mSensorManager = m;
    }

    int curAcelaNodeIndex = -1;   // cycles over defined nodes when pollMessage is called

    /**
     *  Handles initialization, output and polling for Acela Nodes
     *      from within the running thread
     */
    protected synchronized AbstractMRMessage pollMessage() {
        if (needToInitAcelaNetwork) {
            if (needToCreateNodesState == 0) {
                if (needToPollNodes) {
        		AcelaNode specialnode = new AcelaNode(0, AcelaNode.AC);
                }
                curAcelaNodeIndex = SPECIALNODE;
                AcelaMessage m = AcelaMessage.getAcelaResetMsg();
            	log.debug("send init message: "+m);
            	m.setTimeout(8000);  // wait for init to finish (milliseconds)
            	mCurrentMode = NORMALMODE;
                needToCreateNodesState++;
            	return m;
            }
            if (needToCreateNodesState == 1) {
            	AcelaMessage m = AcelaMessage.getAcelaOnlineMsg();
            	log.debug("send init2 message: "+m);
            	m.setTimeout(8000);  // wait for init to finish (milliseconds)
            	mCurrentMode = NORMALMODE;
                needToCreateNodesState++;
        	return m;
            }
            if (needToPollNodes) {
                if (needToCreateNodesState == 2) {
                    AcelaMessage m = AcelaMessage.getAcelaPollNodesMsg();
                    log.debug("send poll message: "+m);
                    m.setTimeout(8000);  // wait for init to finish (milliseconds)
                    mCurrentMode = NORMALMODE;
                    needToInitAcelaNetwork = false;
                    needToPollNodes = false;
                    return m;
                }
            } else {
                needToInitAcelaNetwork = false;
                setAcelaTrafficControllerState(true); 
            }
        }
        
        // ensure validity of call
        if (numNodes<=0) {
            return null;
        }
        
        // move to a new node
        curAcelaNodeIndex ++;
        if (curAcelaNodeIndex>=numNodes) { 
            curAcelaNodeIndex = 0;
        }

        // ensure that each node is initialized        
       if (nodeArray[curAcelaNodeIndex].hasActiveSensors) {
            for (int s = 0; s < nodeArray[curAcelaNodeIndex].sensorbitsPerCard; s++) {
                if (nodeArray[curAcelaNodeIndex].sensorInit[s]) {
                    AcelaMessage m = AcelaMessage.getAcelaConfigSensorMsg();
                    int tempiaddr = s + nodeArray[curAcelaNodeIndex].getStartingSensorAddress();
                    byte tempbaddr = (byte) (tempiaddr);
                    m.setElement(2, tempbaddr);
                    log.debug("send Config Sesnsor message: "+m);
                    m.setTimeout(2000);  // wait for init to finish (milliseconds)
                    mCurrentMode = NORMALMODE;
                    nodeArray[curAcelaNodeIndex].sensorInit[s] = false;
                    return m;
                }
            }
        }
        
        // send Output packet if needed
        if (nodeArray[curAcelaNodeIndex].mustSend()) {
            log.debug("request write command to send");
            nodeArray[curAcelaNodeIndex].resetMustSend();
            AcelaMessage m = nodeArray[curAcelaNodeIndex].createOutPacket(curAcelaNodeIndex);
            m.setTimeout(400);  // no need to wait for output to answer
//            m.setTimeout(200);  // no need to wait for output to answer Before adding WM
        	mCurrentMode = NORMALMODE;
            return m;
        }

        // poll for Sensor input
        if (acelaSensorsState) {    //  Flag to indicate whether we have an active sensor and therefore need to poll
        	AcelaMessage m = AcelaMessage.getAcelaPollSensorsMsg();
        	log.debug("send poll message: "+m);
        	m.setTimeout(600);  // wait for init to finish (milliseconds)
//        	m.setTimeout(200);  // wait for init to finish (milliseconds) Before adding WM
        	mCurrentMode = NORMALMODE;
        	return m;
        } else {
            // no Sensors (inputs) are active for this node
            return null;
        }
    }

    protected void handleTimeout(AbstractMRMessage m) {
        // don't use super behavior, as timeout to init, transmit message is normal
        // inform node, and if it resets then reinitialize        
        if (nodeArray[curAcelaNodeIndex].handleTimeout(m)) 
            mustInit[curAcelaNodeIndex] = true;
    }
    
    protected void resetTimeout(AbstractMRMessage m) {
        // don't use super behavior, as timeout to init, transmit message is normal
        // and inform node
        nodeArray[curAcelaNodeIndex].resetTimeout(m);
    }

    protected AbstractMRListener pollReplyHandler() {
    	return mSensorManager;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendAcelaMessage(AcelaMessage m, AcelaListener reply) {
        sendMessage(m, reply);
    }

    /**
     * static function returning the AcelaTrafficController instance to use.
     * @return The registered AcelaTrafficController instance for general use,
     *         if need be creating one.
     */
    static public AcelaTrafficController instance() {
        // Bob C: This seems wrong
    	if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new AcelaTrafficController object");
            // Bob C: and the following line won't work in a static.
            // self = this;
            // So, ..., create a new one.
            self = new AcelaTrafficController();
        }
        return self;
    }

    static protected AcelaTrafficController self = null;

    protected void setInstance() { self = this; }

    protected AbstractMRReply newReply() { 
    	return new AcelaReply();
    }

    protected boolean endOfMessage(AbstractMRReply msg) {
        // our version of loadChars doesn't invoke this, so it shouldn't be called
        return true;
    }

    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        byte char1 = readByteProtected(istream);
        if (char1 == 0x00) {  // 0x00 means command processed OK.
            msg.setElement(0, char1);
                                //  0x01 means that the Acela network is offline
                                //  0x02 means that an illegal address was sent
                                //  0x03 means that an illegal command was sent
                                //  For now we are not going to check for these
                                //  three conditions since they will only catch
                                //  programming errors (versus runtime errors)
                                //  and the checking may mess up the polling replies.
 
        } else {
            if ((char1 == 0x81) || (char1 == 0x82)) {
                                //  0x81 means that a sensor has changed.
                                //  0x82 means that communications has been lost
                                //  For now we will check for these two 
                                //  conditions since they do represent
                                //  runtime errors at the risk that in a very very
                                //  large Acela network the checking may mess
                                //  up the polling replies.
                msg.setElement(0, char1);
            } else {
                                //  We have a reply to a poll (either pollnodes 
                                //  or pollsensors).  The first byte will be the
                                //  length of the reply followed by the
                                //  indicated number of bytes.
                                //
                                //  For now we will send the reply to the sensor
                                //  manager.  In the future we should really have
                                //  an Acela Network Manager and an Acela Sensor
                                //  Manager -- but, for now, we 'know' which state
                                //  we are in.
                for (int i=0; i< char1; i++) {
                    byte charn = readByteProtected(istream);
                    msg.setElement(i, charn);
        	}
            }
        }
    }

    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
        // Just return
    }

    /**
     * For each sensor node call markChanges.
     */
    public void updateSensorsFromPoll(AcelaReply r) {
        for (int i=0; i< numNodes; i++) {
            if (nodeArray[i].getSensorBitsPerCard() > 0) {
                nodeArray[i].markChanges(r);
            }
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AcelaTrafficController.class.getName());
}

/* @(#)AcelaTrafficController.java */