package jmri.jmrix.acela;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInputStream;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRNodeTrafficController;
import jmri.jmrix.AbstractMRReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from Acela messages.
 * <p>
 * The "SerialInterface" side sends/receives message objects.
 * <p>
 * The connection to an AcelaPortController is via a pair of Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <p>
 * This handles the state transitions, based on the necessary state in each
 * message.
 * <p>
 * Handles initialization, polling, output, and input for multiple Serial Nodes.
 * @see jmri.jmrix.AbstractMRNodeTrafficController
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @author Bob Coleman Copyright (C) 2007. 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaTrafficController extends AbstractMRNodeTrafficController implements AcelaInterface {

    /**
     * Create a new AcelaTrafficController instance.
     */
    public AcelaTrafficController() {
        super();

        // entirely poll driven, so reduce Polling interval
        mWaitBeforePoll = 25;  // default = 25
        setAllowUnexpectedReply(true);

        super.init(0, 1024); // 1024 is an artifical limit but economically reasonable maxNode upper limit

        reallyReadyToPoll = false;           // Need to not start polling until we are ready
        needToPollNodes = true;              // Need to poll and create corresponding nodes
        needToInitAcelaNetwork = true;       // Need to poll and create corresponding nodes
        needToCreateNodesState = 0;          // Need to initialize system and then poll
        acelaTrafficControllerState = false; //  Flag to indicate which state we are in:
                                             //  false == Initializing Acela Network
                                             //  true == Polling Sensors
    }

    // The methods to implement the AcelaInterface

    @Override
    public synchronized void addAcelaListener(AcelaListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeAcelaListener(AcelaListener l) {
        this.removeListener(l);
    }

    transient int curAcelaNodeIndex = -1;   // cycles over defined nodes when pollMessage is called

    transient private int currentOutputAddress = -1;   // Incremented as Acela Nodes are created and registered
    // Corresponds to next available output address in nodeArray
    // Start at -1 to avoid issues with bit address 0
    transient private int currentSensorAddress = -1;   // Incremented as Acela Nodes are created and registered
    // Corresponds to next available sensor address in nodeArray
    // Start at -1 to avoid issues with bit address 0

    private boolean acelaTrafficControllerState = false;    //  Flag to indicate which state we are in: 
    //  false == Initializing Acela Network
    //  true == Polling Sensors
    private boolean reallyReadyToPoll = false;   //  Flag to indicate that we are really ready to poll nodes
    transient private boolean needToPollNodes = true;   //  Flag to indicate that nodes have not yet been created
    private boolean needToInitAcelaNetwork = true;   //  Flag to indicate that Acela network must be initialized
    private int needToCreateNodesState = 0;     //  Need to do a few things:
    //      Reset Acela Network
    //      Set Acela Network Online
    //      Poll for Acela Nodes (and create and register the nodes)

    private boolean acelaSensorsState = false;    //  Flag to indicate whether we have an active sensor and therefore need to poll: 
    //  false == No active sensor
    //  true == Active sensor, need to poll sensors

    private int acelaSensorInitCount = 0;     //  Need to count sensors initialized so we know when we can poll them

    private static int SPECIALNODE = 0;         //  Needed to initialize system

    /**
     * Get minimum address of an Acela node as set on this TrafficController.
     */
    public int getMinimumNodeAddress() {
        return minNode;
    }

    /**
     * Get maximum number of Acela nodes as set on this TrafficController.
     */
    public int getMaximumNumberOfNodes() {
        return maxNode;
    }

    public boolean getAcelaTrafficControllerState() {
        return acelaTrafficControllerState;
    }

    public void setAcelaTrafficControllerState(boolean newstate) {
        acelaTrafficControllerState = newstate;
    }

    public synchronized void resetStartingAddresses() {
        currentOutputAddress = -1;
        currentSensorAddress = -1;
    }

    public boolean getAcelaSensorsState() {
        return acelaSensorsState;
    }

    public void setAcelaSensorsState(boolean newstate) {
        acelaSensorsState = newstate;
    }

    public void incrementAcelaSensorInitCount() {
        acelaSensorInitCount++;
        log.debug("Number of Acela sensors initialized: " + getAcelaSensorInitCount());
    }

    public int getAcelaSensorInitCount() {
        return acelaSensorInitCount;
    }

    public synchronized boolean getNeedToPollNodes() {
        return needToPollNodes;
    }

    public synchronized void setNeedToPollNodes(boolean newstate) {
        needToPollNodes = newstate;
    }

    public boolean getReallyReadyToPoll() {
        return reallyReadyToPoll;
    }

    public void setReallyReadyToPoll(boolean newstate) {
        log.debug("setting really ready to poll (nodes): " + newstate);
        reallyReadyToPoll = newstate;
    }

    /**
     * Public method to register an Acela node.
     */
    public void registerAcelaNode(AcelaNode node) {
        synchronized (this) {
            super.registerNode(node);

            // no node validity checking because at this point the node may not be fully defined
            setMustInit(node, false);  // Do not normally need to init Acela nodes.
            if (node.getNumOutputBitsPerCard() == 0) {
                node.setStartingOutputAddress(-1);
                node.setEndingOutputAddress(-1);
            } else {
                if (currentOutputAddress == -1) {  // Need to use -1 to correctly identify bit address 0
                    currentOutputAddress = 0;
                }
                node.setStartingOutputAddress(currentOutputAddress);
                currentOutputAddress = currentOutputAddress + node.getNumOutputBitsPerCard() - 1;
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
                currentSensorAddress = currentSensorAddress + node.getNumSensorBitsPerCard() - 1;
                node.setEndingSensorAddress(currentSensorAddress);
                currentSensorAddress = currentSensorAddress + 1;
            }
        }
    }

    /**
     * Public method to set up for initialization of an Acela node.
     */
    public void initializeAcelaNode(AcelaNode node) {
        synchronized (this) {
            setMustInit(node, true);
            node.initNode();
        }
    }

    /**
     * Public method to identify an AcelaNode from its bit address.
     * <p>
     * Note: nodeAddress is numbered from 0
     *
     * @return '-1' if an AcelaNode with the specified address was not found
     */
    public int lookupAcelaNodeAddress(int bitAddress, boolean isSensor) {
        for (int i = 0; i < getNumNodes(); i++) {
            AcelaNode node = (AcelaNode) getNode(i);
            if (isSensor) {
                if ((bitAddress >= node.getStartingSensorAddress())
                        && (bitAddress <= node.getEndingSensorAddress())) {
                    return (i);
                }
            } else {
                if ((bitAddress >= node.getStartingOutputAddress())
                        && (bitAddress <= node.getEndingOutputAddress())) {
                    return (i);
                }
            }
        }
        return (-1);
    }

    @Override
    protected AbstractMRMessage enterProgMode() {
        log.warn("enterProgMode does NOT make sense for Acela serial");
        return null;
    }

    @Override
    protected AbstractMRMessage enterNormalMode() {
        // can happen during error recovery, null is OK
        return null;
    }

    /**
     * Forward an AcelaMessage to all registered AcelaInterface listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((AcelaListener) client).message((AcelaMessage) m);
    }

    /**
     * Forward an AcelaReply to all registered AcelaInterface listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((AcelaListener) client).reply((AcelaReply) m);
    }

    AcelaSensorManager mSensorManager = null;

    public void setSensorManager(AcelaSensorManager m) {
        mSensorManager = m;
    }

    AcelaTurnoutManager mTurnoutManager = null;

    public void setTurnoutManager(AcelaTurnoutManager m) {
        mTurnoutManager = m;
    }

    /**
     * Handle initialization, output and polling for Acela Nodes from within
     * the running thread.
     */
    @Override
    protected synchronized AbstractMRMessage pollMessage() {
        // Need to wait until we have read config file
        if (!reallyReadyToPoll) {
            return null;
        }

        if (needToInitAcelaNetwork) {
            if (needToCreateNodesState == 0) {
                if (needToPollNodes) {
                    new AcelaNode(0, AcelaNode.AC,this);
                    log.info("Created a new Acela Node [0] in order to poll Acela network: " + AcelaNode.AC);
                }
                curAcelaNodeIndex = SPECIALNODE;
                AcelaMessage m = AcelaMessage.getAcelaResetMsg();
                log.debug("send Acela reset (init step 1) message: " + m);
                m.setTimeout(1000);  // wait for init to finish (milliseconds)
                mCurrentMode = NORMALMODE;
                needToCreateNodesState++;
                return m;
            }
            if (needToCreateNodesState == 1) {
                AcelaMessage m = AcelaMessage.getAcelaOnlineMsg();
                log.debug("send Acela Online (init step 2) message: " + m);
                m.setTimeout(1000);  // wait for init to finish (milliseconds)
                mCurrentMode = NORMALMODE;
                needToCreateNodesState++;
                return m;
            }
            if (needToPollNodes) {
                if (needToCreateNodesState == 2) {
                    AcelaMessage m = AcelaMessage.getAcelaPollNodesMsg();
                    log.debug("send Acela poll nodes message: " + m);
                    m.setTimeout(100);  // wait for init to finish (milliseconds)
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
        if (getNumNodes() <= 0) {
            return null;
        }

        // move to a new node
        curAcelaNodeIndex++;
        if (curAcelaNodeIndex >= getNumNodes()) {
            curAcelaNodeIndex = 0;
        }

        // ensure that each node is initialized
        AcelaNode node = (AcelaNode) getNode(curAcelaNodeIndex);
        if (node.hasActiveSensors) {
            for (int s = 0; s < node.sensorbitsPerCard; s++) {
                if (node.sensorNeedInit[s] && !node.sensorHasBeenInit[s]) {
                    AcelaMessage m = AcelaMessage.getAcelaConfigSensorMsg();
                    int tempiaddr = s + node.getStartingSensorAddress();
                    byte tempbaddr = (byte) (tempiaddr);
                    m.setElement(2, tempbaddr);
                    m.setElement(3, node.sensorConfigArray[s]);
                    log.debug("send Acela Config Sensor message: " + m);
                    incrementAcelaSensorInitCount();
                    m.setTimeout(100);  // wait for init to finish (milliseconds)
                    mCurrentMode = NORMALMODE;
                    node.sensorHasBeenInit[s] = true;
                    node.sensorNeedInit[s] = false;
                    return m;
                }
            }
        }

        // send Output packet if needed
        if (getNode(curAcelaNodeIndex).mustSend()) {
            getNode(curAcelaNodeIndex).resetMustSend();
            AbstractMRMessage m = getNode(curAcelaNodeIndex).createOutPacket();
            m.setTimeout(100);  // no need to wait for output to answer
            log.debug("request write command to send: " + m);
            mCurrentMode = NORMALMODE;
            return m;
        }

        // Trying to serialize Acela initiatization so system is stable
        // So we will not poll sensors or send om/off commands until we have
        // initialized all of the sensor modules -- this can take several seconds
        // during a cold system startup.
        if ((currentSensorAddress == 0) || (currentSensorAddress != getAcelaSensorInitCount())) {
            return null;
        }

        if (acelaSensorsState) {    //  Flag to indicate whether we have an active sensor and therefore need to poll
            AcelaMessage m = AcelaMessage.getAcelaPollSensorsMsg();
            log.debug("send Acela poll sensors message: " + m);
            m.setTimeout(100);  // wait for init to finish (milliseconds)
            mCurrentMode = NORMALMODE;
            return m;
        } else {
            // no Sensors (inputs) are active for this node
            return null;
        }
    }

    @Override
    protected synchronized void handleTimeout(AbstractMRMessage m, AbstractMRListener l) {
        // don't use super behavior, as timeout to init, transmit message is normal
        // inform node, and if it resets then reinitialize        
        if (getNode(curAcelaNodeIndex).handleTimeout(m, l)) {
            setMustInit(curAcelaNodeIndex, true);
        }
    }

    @Override
    protected synchronized void resetTimeout(AbstractMRMessage m) {
        // don't use super behavior, as timeout to init, transmit message is normal
        // and inform node
        getNode(curAcelaNodeIndex).resetTimeout(m);
    }

    @Override
    protected AbstractMRListener pollReplyHandler() {
        return mSensorManager;
    }

    /**
     * Forward a pre-formatted message to the actual interface.
     */
    @Override
    public void sendAcelaMessage(AcelaMessage m, AcelaListener reply) {
        sendMessage(m, reply);
    }

    @Override
    protected AbstractMRReply newReply() {
        return new AcelaReply();
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        // our version of loadChars doesn't invoke this, so it shouldn't be called
        return true;
    }

    @Override
    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        int char1 = readByteProtected(istream)&0xFF;
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
                for (int i = 0; i < char1; i++) {
                    byte charn = readByteProtected(istream);
                    msg.setElement(i, charn);
                }
            }
        }
    }

    @Override
    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
        // Just return
    }

    /**
     * For each sensor node call markChanges.
     */
    public void updateSensorsFromPoll(AcelaReply r) {
        for (int i = 0; i < getNumNodes(); i++) {
            AcelaNode node = (AcelaNode) getNode(i);
            if (node.getSensorBitsPerCard() > 0) {
                node.markChanges(r);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaTrafficController.class);

}
