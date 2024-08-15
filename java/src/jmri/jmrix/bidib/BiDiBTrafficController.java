package jmri.jmrix.bidib;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.CommandStation;
import jmri.jmrix.PortAdapter;
import jmri.NmraPacket;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.implementation.AbstractShutDownTask;

import org.bidib.jbidibc.messages.BidibLibrary;
import org.bidib.jbidibc.messages.exception.ProtocolException;
import org.bidib.jbidibc.messages.utils.ByteUtils;

import org.bidib.jbidibc.core.BidibMessageProcessor;
import org.bidib.jbidibc.core.BidibInterface;
import org.bidib.jbidibc.messages.BidibPort;
import org.bidib.jbidibc.messages.ConnectionListener;
import org.bidib.jbidibc.core.DefaultMessageListener;
import org.bidib.jbidibc.messages.Feature;
import org.bidib.jbidibc.messages.LcConfig;
import org.bidib.jbidibc.messages.LcConfigX;
import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.messages.base.RawMessageListener;
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.messages.ProtocolVersion;
import org.bidib.jbidibc.messages.StringData;
import org.bidib.jbidibc.messages.helpers.Context;
import org.bidib.jbidibc.core.node.listener.TransferListener;
import org.bidib.jbidibc.messages.exception.PortNotFoundException;
import org.bidib.jbidibc.core.node.BidibNode;
import org.bidib.jbidibc.messages.message.BidibCommandMessage;
import org.bidib.jbidibc.core.node.BidibNodeAccessor;
import org.bidib.jbidibc.messages.utils.NodeUtils;
import org.bidib.jbidibc.messages.enums.CommandStationState;
import org.bidib.jbidibc.messages.enums.LcOutputType;
import org.bidib.jbidibc.messages.enums.PortModelEnum;
import org.bidib.jbidibc.messages.message.AccessoryGetMessage;
import org.bidib.jbidibc.messages.message.BidibRequestFactory;
import org.bidib.jbidibc.messages.message.CommandStationSetStateMessage;
import org.bidib.jbidibc.messages.message.FeedbackGetRangeMessage;
import org.bidib.jbidibc.core.node.CommandStationNode;
import org.bidib.jbidibc.core.node.BoosterNode;
import org.bidib.jbidibc.messages.BoosterStateData;
import org.bidib.jbidibc.messages.enums.BoosterControl;
import org.bidib.jbidibc.messages.enums.BoosterState;
import org.bidib.jbidibc.messages.enums.CommandStationProgState;
import org.bidib.jbidibc.messages.port.BytePortConfigValue;
import org.bidib.jbidibc.messages.port.PortConfigValue;
import org.bidib.jbidibc.messages.port.ReconfigPortConfigValue;
import org.bidib.jbidibc.simulation.comm.SimulationBidib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The BiDiB Traffic Controller provides the interface for JMRI to the BiDiB Library (jbidibc) - it
 * does not handle any protocol functions itself. Therefor it does not extend AbstractMRTrafficController.
 * Instead, it delegates BiDiB handling to a BiDiB controller instance (serial, simulation, etc.) using BiDiBInterface.
 * 
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Eckart Meyer Copyright (C) 2019-2024
 *
 */
 
@SuppressFBWarnings(value = "JLM_JSR166_UTILCONCURRENT_MONITORENTER")
// This code uses several AtomicBoolean variables as synch objects.  In this use, 
// they're synchronizing access to code blocks, not just synchronizing access
// to the underlying boolean value. it would be possible to use separate
// Object variables for this process, but keeping those in synch would actually
// be more complex and confusing than this approach.

public class BiDiBTrafficController implements CommandStation {

    private final BidibInterface bidib;
    private final Set<TransferListener> transferListeners = new LinkedHashSet<>();
    private final Set<MessageListener> messageListeners = new LinkedHashSet<>();
    private final Set<NodeListener> nodeListeners = new LinkedHashSet<>();
    private final AtomicBoolean stallLock = new AtomicBoolean();
    private java.util.TimerTask watchdogTimer = null;
    private final AtomicBoolean watchdogStatus = new AtomicBoolean();

    private final BiDiBNodeInitializer nodeInitializer;
    protected final TreeMap<Long, Node> nodes = new TreeMap<>(); //our node list - use TreeMap since it retains order if insertion (HashMap has arbitrary order)

    private Node cachedCommandStationNode = null;
    
    //volatile protected boolean mIsProgMode = false;
    private final AtomicBoolean mIsProgMode = new AtomicBoolean();
    volatile protected CommandStationState mSavedMode;
    private Node currentGlobalProgrammerNode = null;
    private final javax.swing.Timer progTimer = new javax.swing.Timer(3000, e -> progTimeout());

    //private Thread shutdownHook = null; // retain shutdown hook for  possible removal.
    
    private final Map<Long, String> debugStringBuffer = new HashMap<>();

    /**
     * Create a new BiDiBTrafficController instance.
     * Must provide a BidibInterface reference at creation time.
     *
     * @param b reference to associated jbidibc object,
     *                        preserved for later.
     */
    public BiDiBTrafficController(BidibInterface b) {
        bidib = b;
        log.debug("BiDiBTrafficController created");
        mSavedMode = CommandStationState.OFF;
        setWatchdogTimer(false); //preset not enabled

        progTimer.setRepeats(false);
        mIsProgMode.set(false);
        
        nodeInitializer = new BiDiBNodeInitializer(this, bidib, nodes);
        
        // NO LONGER USED - When using JSerialComm, the port is obviously
        // already closed when the ShutdownHook is executed.
        // With PureJavacomm, this was not a problem...
        
        // Copied from AbstractMRTrafficController:
        // We use a shutdown hook here to make sure the connection is left
        // in a clean state prior to exiting.  This is required on systems
        // which have a service mode to ensure we don't leave the system 
        // in an unusable state (This code predates the ShutdownTask 
        // mechanisim).  Once the shutdown hook executes, the connection
        // must be considered closed.
        //shutdownHook = new Thread(new CleanupHook(this));
        //Runtime.getRuntime().addShutdownHook(shutdownHook);
        
        // We now use the ShutdownTask method. This task is executed earlier
        // (and before the external tasks are executed)
        // and the JSerialComm port is still usable.
        // And registering the shutdown task is moved to connnectPort().
        
    }
    
    /**
     * Opens the BiDiB connection in the jbidibc library, add listeners and initialize BiDiB.
     * 
     * @param p BiDiB port adapter (serial or simulation)
     * @return a jbidibc context
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",justification = "Cast safe by design")
    public Context connnectPort(PortAdapter p) {
        // init bidib
        Context context = ((BiDiBPortController)p).getContext();
        stallLock.set(false); //not stalled
        
        messageListeners.add(new DefaultMessageListener() {

            @Override
            public void error(byte[] address, int messageNum, int errorCode, byte[] reasonData) {
                log.debug("Node error event: addr: {}, msg num: {}, error code: {}, data: {}", address, messageNum, errorCode, reasonData);
                if (errorCode == 1) {
                    log.info("error: {}", new String(reasonData));
                }
            }

            @Override
            @SuppressFBWarnings(value = "SLF4J_SIGN_ONLY_FORMAT",justification = "info message contains context information")
            public void nodeString(byte[] address, int messageNum, int namespace, int stringId, String value) {
                // handle debug messages from a node
                if (namespace == StringData.NAMESPACE_DEBUG) {
                    Node node = getNodeByAddr(address);
                    String uid = ByteUtils.getUniqueIdAsString(node.getUniqueId());
                    // the debug string buffer key is the node's 40 bit UID plus the string id in the upper 24 bit
                    long key = (node.getUniqueId() & 0x0000ffffffffffL) | (long)stringId << 40;
                    String prefix = "===== BiDiB";
                    if (value.charAt(value.length() - 1) == '\n') {
                        String txt = "";
                        // check if we have previous received imcomplete text
                        if (debugStringBuffer.containsKey(key)) {
                            txt = debugStringBuffer.get(key);
                            debugStringBuffer.remove(key);
                        }
                        txt += value.replace("\n","");
                        switch(stringId) {
                            case StringData.INDEX_DEBUG_STDOUT:
                                log.info("{} {} stdout: {}", prefix, uid, txt);
                                break;
                            case StringData.INDEX_DEBUG_STDERR:
                                log.info("{} {} stderr: {}", prefix, uid, txt);
                                break;
                            case StringData.INDEX_DEBUG_WARN:
                                log.warn("{} {}: {}", prefix, uid, txt);
                                break;
                            case StringData.INDEX_DEBUG_INFO:
                                log.info("{} {}: {}", prefix, uid, txt);
                                break;
                            case StringData.INDEX_DEBUG_DEBUG:
                                log.debug("{} {}: {}", prefix, uid, txt);
                                break;
                            case StringData.INDEX_DEBUG_TRACE:
                                log.trace("{} {}: {}", prefix, uid, txt);
                                break;
                            default: break;
                        }
                    }
                    else {
                        log.trace("incomplete debug string received: [{}]", value);
                        String txt = "";
                        if (debugStringBuffer.containsKey(key)) {
                            txt = debugStringBuffer.get(key);
                        }
                        debugStringBuffer.put(key, (txt + value));
                    }
                }
            }
            
            @Override
            public void nodeLost(byte[] address, int messageNum, Node node) {
                log.debug("Node lost event: {}", node);
                nodeInitializer.nodeLost(node);
            }

            @Override
            public void nodeNew(byte[] address, int messageNum, Node node) {
                log.debug("Node new event: {}", node);
                nodeInitializer.nodeNew(node);
            }
            
            @Override
            public void stall(byte[] address, int messageNum, boolean stall) {
                synchronized (stallLock) {
                    if (log.isDebugEnabled()) {
                        Node node = getNodeByAddr(address);
                        log.debug("stall - msg num: {}, new state: {}, node: {}, ", messageNum, stall, node);
                    }
                    if (stall != stallLock.get()) {
                        stallLock.set(stall);
                        if (!stall) {
                            log.debug("stall - wake send");
                            stallLock.notifyAll(); //wake pending send if any
                        }
                    }
                }
            }
            
            // don't know if this is the correct place...
            @Override
            public void csState(byte[] address, int messageNum, CommandStationState commandStationState) {
                Node node = getNodeByAddr(address);
                log.debug("CS STATE event: {} on node {}, current watchdog status: {}", commandStationState, node, watchdogStatus.get());
                synchronized (mIsProgMode) {
                    if (CommandStationState.isPtProgState(commandStationState)) {
                        mIsProgMode.set(true);
                    }
                    else {
                        mIsProgMode.set(false);
                        mSavedMode = commandStationState;
                    }
                }
                boolean newState = (commandStationState == CommandStationState.GO);
                if (node == getFirstCommandStationNode()  &&  newState != watchdogStatus.get()) {
                    log.trace("watchdog: new state: {}, current state: {}", newState, watchdogStatus.get());
                    setWatchdogTimer(newState);
                }
            }
            @Override
            public void csProgState(
                byte[] address, int messageNum, CommandStationProgState commandStationProgState, int remainingTime, int cvNumber, int cvData) {
                synchronized (progTimer) {
                    if ( (commandStationProgState.getType() & 0x80) != 0) { //bit 7 = 1 means operation has finished
                        progTimer.restart();
                        log.trace("PROG finished, progTimer (re)started.");
                    }
                    else {
                        progTimer.stop();
                        log.trace("PROG pending, progTimer stopped.");
                    }
                }
            }
            @Override
            public void boosterState(byte[] address, int messageNum, BoosterState state, BoosterControl control) {
                Node node = getNodeByAddr(address);
                log.info("BOOSTER STATE & CONTROL was signalled: {}, control: {}", state.getType(), control.getType());
                if (node != getFirstCommandStationNode()  &&  node == currentGlobalProgrammerNode  &&  control != BoosterControl.LOCAL) {
                    currentGlobalProgrammerNode = null;
                }
            }
        });
                
        transferListeners.add(new TransferListener() {

            @Override
            public void sendStopped() {
                // no implementation
                //log.trace("sendStopped");
            }

            @Override
            public void sendStarted() {
                log.debug("sendStarted");
                // TODO check node!
                synchronized (stallLock) {
                    if (stallLock.get()) {
                        try {
                            log.debug("sendStarted is stalled - waiting...");
                            stallLock.wait(1000L);
                            log.debug("sendStarted stall condition has been released");
                        }
                        catch (InterruptedException e) {
                            log.warn("waited too long for releasing stall condition - continue...");
                            stallLock.set(false);
                        }
                    }
                }
            }

            @Override
            public void receiveStopped() {
                // no implementation
                //log.trace("receiveStopped");
            }

            @Override
            public void receiveStarted() {
                // no implementation
                //log.trace("receiveStarted");
            }

            @Override
            public void ctsChanged(boolean cts, boolean manualEvent) { //new
//            public void ctsChanged(boolean cts) { //jbidibc 12.5
                // no implementation
                log.trace("ctsChanged");
            }
        });
        
        ConnectionListener connectionListener = new ConnectionListener() {
            @Override
            public void opened(String port) {
                // no implementation
                log.trace("opened port {}", port);
            }

            @Override
            public void closed(String port) {
                // no implementation
                log.trace("closed port {}", port);
            }

            @Override
            public void status(String messageKey, Context context) {
                // no implementation
                log.trace("status - message key {}", messageKey);
            }
        };
        
        String portName = ((BiDiBPortController)p).getRealPortName();
        log.info("Open BiDiB connection on \"{}\"", portName);

        try {
            if (!bidib.isOpened()) {
                bidib.setResponseTimeout(1600);
                bidib.open(portName, connectionListener, nodeListeners, messageListeners, transferListeners, context);
            }
            else {
                // if we get here, we assume that the adapter has already opened the port just for scanning the device
                // and that NO listeners have been registered. So just add them now.
                // If one day we start to really use the listeners we would have to check if this is o.k.
                ((BiDiBPortController)p).registerAllListeners(connectionListener, nodeListeners, messageListeners, transferListeners);
            }

            // the connection has been established - register a shutdown task
            log.info("registering shutdown task");
            ShutDownTask shutDownTask = new AbstractShutDownTask("BiDiB Shutdown Task") {
                @Override
                public void run() {
                    log.info("Shutdown Task - Terminate BiDiB");
                    terminate();
                    log.info("Shutdown task finished");
                }
            };
            InstanceManager.getDefault(ShutDownManager.class).register(shutDownTask);

            // get data from root node and from all other nodes
            log.debug("get relevant node data");
            BidibNode rootNode = bidib.getRootNode();
            int count = rootNode.getNodeCount();
            log.debug("node count: {}", count);
            byte[] nodeaddr = rootNode.getAddr();
            log.debug("node addr length: {}", nodeaddr.length);
            log.debug("node addr: {}", nodeaddr);
            for (int i = 0; i < nodeaddr.length; i++) {
                log.debug("  byte {}: {}", i, nodeaddr[i]);
            }
//            int featureCount = rootNode.getFeatureCount();
//            log.debug("feature count: {}", featureCount);
//            log.debug("** Unique ID: {}", String.format("0x%X",rootNode.getUniqueId()));
            
            for (int index = 1; index <= count; index++) {
                Node node = rootNode.getNextNode(null); //TODO org.bidib.jbidibc.messages.logger.Logger
                nodeInitializer.initNode(node);
                long uid = node.getUniqueId() & 0x0000ffffffffffL; //mask the classid
                nodes.put(uid, node);
            }
            rootNode.sysEnable();
            log.info("--- node init finished ---");
            
            Node csnode = getFirstCommandStationNode();
            if (csnode != null) {
                sendBiDiBMessage(new CommandStationSetStateMessage(CommandStationState.QUERY), csnode);
                // TODO: Should we remove all Locos from command station? MSG_SET_DRIVE with loco 0 and bitfields = 0 (see BiDiB spec)
                // TODO: use MSG_CS_ALLOCATE every second to disable direct control from local controllers like handhelds?
            }
            
            return context;

        }
        catch (PortNotFoundException ex) {
            log.error("The provided port was not found: {}. Verify that the BiDiB device is connected.", ex.getMessage());
        }
        catch (Exception ex) {
            log.error("Execute command failed: ", ex); // NOSONAR
        }
        return null;
    }
    
    Node debugSavedNode;
    public void TEST(boolean a) {///////////////////DEBUG
        log.debug("TEST {}", a);
        String nodename = "XXXX";
        Node node = a ? debugSavedNode : getNodeByUserName(nodename);
        if (node != null) {
            if (a) {
                nodeInitializer.nodeNew(node);
                //nodeInitializer.nodeLost(node);
            }
            else {
                debugSavedNode = node;
                nodeInitializer.nodeLost(node);
            }
        }
    }
    
    /**
     * Get Bidib Interface
     * 
     * @return Bidib Interface
     */
    public BidibInterface getBidib() {
        return bidib;
    }

// convenience methods for node handling
    
    /**
     * Get the list of nodes found
     * 
     * @return list of nodes
     */
    public Map<Long, Node> getNodeList() {
        return nodes;
    }
    
    /**
     * Get node by unique id from nodelist
     * 
     * @param uniqueId search for this
     * @return node
     */
    public Node getNodeByUniqueID(long uniqueId) {
        return nodes.get(uniqueId);
    }
    
    /**
     * Get node by node address from nodelist
     * 
     * @param addr input to search
     * @return node 
     */
    public Node getNodeByAddr(byte[] addr) {
        for(Map.Entry<Long, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            if (NodeUtils.isAddressEqual(node.getAddr(), addr)) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Get node by node username from nodelist
     * 
     * @param userName input to search
     * @return node 
     */
    public Node getNodeByUserName(String userName) {
        //log.debug("getNodeByUserName: [{}]", userName);
        for(Map.Entry<Long, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            //log.debug("  node: {}, usename: {}", node, node.getStoredString(StringData.INDEX_USERNAME));
            if (node.getStoredString(StringData.INDEX_USERNAME).equals(userName)) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Get root node from nodelist
     * 
     * @return node 
     */
    public Node getRootNode() {
        byte[] addr = {0};
        return getNodeByAddr(addr);
    }
    
    /**
     * A node suitable as a global programmer must be
     * 
     * - a command station,
     * - must support service mode programming and
     * - must be a booster.
     * - for other nodes than the global command station the local DCC generator
     *   must be switched on (MSG_BOOST_STAT returns this as "control")
     * 
     * @param node to check
     * 
     * @return true if the node is suitable as a global progreammer
     */
    public boolean isGlobalProgrammerNode(Node node) {
       
        if (NodeUtils.hasCommandStationFunctions(node.getUniqueId())) {
//                if (node.equals(getRootNode())) { //DEBUG
//                    log.trace("---is root node: {}", node);
//                    continue;//TEST: pretend that the root does not support service mode.
//                }
            if (NodeUtils.hasCommandStationProgrammingFunctions(node.getUniqueId()) 
                        &&  NodeUtils.hasBoosterFunctions(node.getUniqueId())) {
                log.trace("node supports command station, programming and booster functions: {}", node);
                if (node == getFirstCommandStationNode()  ||  hasLocalDccEnabled(node)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the most probably only global programmer node (aka service mode node, used for prgramming track - PT, not for POM)
     * If there are more than one suitable node, get the last one (reverse nodes list search).
     * In this case the command station (probably the root node and first entry in the list) should
     * probably not be used as a global programmer and the other have been added just for that purpose.
     * TODO: the user should select the global programmer node if there multiple nodes suitable
     * as a global programmer.
     * 
     * 
     * @return programmer node or null if none available
     */
    public Node getFirstGlobalProgrammerNode() {
        //log.debug("find  global programmer node");
        for(Map.Entry<Long, Node> entry : nodes.descendingMap().entrySet()) {
            Node node = entry.getValue();
            //log.trace("node entry: {}", node);
            if (isGlobalProgrammerNode(node)) {
                synchronized (progTimer) {
                    log.debug("global programmer found: {}", node);
                    progTimer.restart();
                    return node;
                }
            }
        }
        return null;
    }
    
    /**
     * Set the  global programmer node to use.
     * 
     * @param node to be used as global programmer node or null to remove the currentGlobalProgrammerNode
     * 
     * @return true if node is a suitable global programmer node, currentGlobalProgrammerNode is set to that node. false if it is not.
     */
    public boolean setCurrentGlobalProgrammerNode(Node node) {
        if (node == null  ||  isGlobalProgrammerNode(node)) {
            currentGlobalProgrammerNode = node;
            return true;
        }
        return false;
    }
    
    /**
     * Get the cached global programmer node. If there is no, try to find a suitable node.
     * Note that the global programmer node may dynamically change by user settings.
     * Be sure to update or invalidate currentGlobalProgrammerNode.
     *
     * @return the current global programmer node or null if none available.
     */
    public Node getCurrentGlobalProgrammerNode() {
        //log.trace("get current global programmer node: {}", currentGlobalProgrammerNode);
        if (currentGlobalProgrammerNode == null) {
            currentGlobalProgrammerNode = getFirstGlobalProgrammerNode();
        }
        return currentGlobalProgrammerNode;
    }
    
    /**
     * Ask the node if the local DCC generator is enabled. The state is returned as a
     * BoosterControl value of LOCAL.
     * Note that this function is expensive since it gets the information directly
     * from the node and receiving a MSG_BOOST_STATE message.
     * 
     * As far as I know (2023) there is only one device that supports the dynamically DCC generator switching: the Fichtelbahn ReadyBoost
     * 
     * @param node to ask
     * @return true if local DCC generator is enabled, false if the booster is connected to the
     *         global command station.
     *
     */
    private boolean hasLocalDccEnabled(Node node) {
        if ((bidib instanceof SimulationBidib)) { // **** this a hack for the simulator since it will never return LOCAL dcc...
            return true;
        }
        boolean hasLocalDCC = false;
        // check if the node has a local DCC generator
        BoosterNode bnode = getBidib().getBoosterNode(node);
        if (bnode != null) {
            try {
                BoosterStateData bdata = bnode.queryState(); //send and wait for response
                log.trace("Booster state data: {}", bdata);
                if (bdata.getControl() == BoosterControl.LOCAL) {
                    hasLocalDCC = true; //local DCC generator is enabled
                }
            }
            catch (ProtocolException e) {}
        }
        log.debug("node has local DCC enabled: {}, {}", hasLocalDCC, node);
        return hasLocalDCC;
    }
    
    
    /**
     * Get the first and most probably only command station node (also used for Programming on the Main - POM)
     * A cached value is returned here for performance reasons since the function is called very often.
     * We don't expect the command station to change.
     * 
     * @return command station node
     */
    public Node getFirstCommandStationNode() {
        if (cachedCommandStationNode == null) {
            for(Map.Entry<Long, Node> entry : nodes.entrySet()) {
                Node node = entry.getValue();
                if (NodeUtils.hasCommandStationFunctions(node.getUniqueId())) {
                    log.trace("node has command station functions: {}", node);
                    cachedCommandStationNode = node;
                    break;
                }
            }
        }
        return cachedCommandStationNode;
    }
    
    /**
     * Get the first booster node.
     * There may be more booster nodes, so prefer the command station and then try the others
     * @return booster node
     */
    public Node getFirstBoosterNode() {
        Node node = getFirstCommandStationNode();
        log.trace("getFirstBoosterNode: CS is {}", node);
        if (node != null  &&  NodeUtils.hasBoosterFunctions(node.getUniqueId())) {
            // if the command station has a booster, use this one
            log.trace("CS node also has booster functions: {}", node);
            return node;
        }
        // if the command station does not have a booster, try to find another node with booster capability
        for(Map.Entry<Long, Node> entry : nodes.entrySet()) {
            node = entry.getValue();
            if (NodeUtils.hasBoosterFunctions(node.getUniqueId())) {
                log.trace("node has booster functions: {}", node);
                return node;
            }
        }
        return null;
    }

    /**
     * Get the first output node - a node that can control LC ports.
     * TODO: the method does not make much sense and its only purpose is to check if we have an output node at all.
     * Therefor it should be converted to "hasOutputNode" or similar.
     * @return output node
     */
    public Node getFirstOutputNode() {  
        for(Map.Entry<Long, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            if (NodeUtils.hasAccessoryFunctions(node.getUniqueId())  ||  NodeUtils.hasSwitchFunctions(node.getUniqueId())) {
                log.trace("node has output functions (accessories or ports): {}", node);
                return node;
            }
        }
        return null;
    }
    
    /**
     * Check if we have at least one node capable of Accessory functions
     * @return true or false
     */
    public boolean hasAccessoryNode() {
        for(Map.Entry<Long, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            if (NodeUtils.hasAccessoryFunctions(node.getUniqueId())) {
                log.trace("node has accessory functions: {}", node);
                return true;
            }
        }
        return false;
    }
    
// convenience methods for feature handling
    
    /**
     * Find a feature for given node.
     * 
     * @param node selected node
     * @param requestedFeatureId as integer
     * @return a Feature object or null if the node does not have the feature at all
     */
    public Feature findNodeFeature(Node node, final int requestedFeatureId) {
        return Feature.findFeature(node.getFeatures(), requestedFeatureId);
    }
    
    /**
     * Get the feature value of a node.
     * 
     * @param node selected node
     * @param requestedFeatureId feature to get
     * @return the feature value as integer or 0 if the node does not have the feature
     */
    public int getNodeFeature(Node node, final int requestedFeatureId) {
        Feature f = Feature.findFeature(node.getFeatures(), requestedFeatureId);
        if (f == null) {
            return 0;
        }
        else {
            return f.getValue();
        }
    }
    
    // this is here only as a workaround until PortModelEnum.getPortModel eventually will support flat_extended itself
    public PortModelEnum getPortModel(final Node node) {
        if (PortModelEnum.getPortModel(node) == PortModelEnum.type) {
            return PortModelEnum.type;
        }
        else {
            if (node.getPortFlatModel() >= 256) {
                return PortModelEnum.flat_extended;
            }
            else {
                return PortModelEnum.flat;
            }
        }
    }

    // convenience methods to handle Message Listeners
    
    /**
     * Add a message Listener to the connection
     * @param messageListener to be added
     */
    public void addMessageListener(MessageListener messageListener) {
        if (bidib != null) {
            log.trace("addMessageListener called!");
            BidibMessageProcessor rcv = bidib.getBidibMessageProcessor();
            if (rcv != null) {
                rcv.addMessageListener(messageListener);
            }
        }
    }

    /**
     * Remove a message Listener from the connection
     * @param messageListener to be removed
     */
    public void removeMessageListener(MessageListener messageListener) {
        if (bidib != null) {
            log.trace("removeMessageListener called!");
            BidibMessageProcessor rcv = bidib.getBidibMessageProcessor();
            if (rcv != null) {
                rcv.removeMessageListener(messageListener);
            }
        }
    }

    /**
     * Add a raw message Listener to the connection
     * @param rawMessageListener to be added
     */
    public void addRawMessageListener(RawMessageListener rawMessageListener) {
        if (bidib != null) {
            bidib.addRawMessageListener(rawMessageListener);
        }
    }

    /**
     * Remove a raw message Listener from the connection
     * @param rawMessageListener to be removed
     */
    public void removeRawMessageListener(RawMessageListener rawMessageListener) {
        if (bidib != null) {
            bidib.removeRawMessageListener(rawMessageListener);
        }
    }

    
// Config and ConfigX handling
// NOTE: All of these methods should be either obsolete to moved to BiDiBOutputMessageHandler
    
    private int getTypeCount(Node node, LcOutputType type) {
        int id;
        switch (type) {
            case SWITCHPORT:
            case SWITCHPAIRPORT:
                id = BidibLibrary.FEATURE_CTRL_SWITCH_COUNT;
                break;
            case LIGHTPORT:
                id = BidibLibrary.FEATURE_CTRL_LIGHT_COUNT;
                break;
            case SERVOPORT:
                id = BidibLibrary.FEATURE_CTRL_SERVO_COUNT;
                break;
            case SOUNDPORT:
                id = BidibLibrary.FEATURE_CTRL_SOUND_COUNT;
                break;
            case MOTORPORT:
                id = BidibLibrary.FEATURE_CTRL_MOTOR_COUNT;
                break;
            case ANALOGPORT:
                id = BidibLibrary.FEATURE_CTRL_ANALOGOUT_COUNT;
                break;
            case BACKLIGHTPORT:
                id = BidibLibrary.FEATURE_CTRL_BACKLIGHT_COUNT;
                break;
            case INPUTPORT:
                id = BidibLibrary.FEATURE_CTRL_INPUT_COUNT;
                break;
            default:
                return 0;
        }
        return getNodeFeature(node, id);
    }

// semi-synchroneous methods using MSG_LC_CONFIGX_GET_ALL / MSG_LC_CONFIGX_GET (or MSG_LC_CONFIG_GET for older nodes) - use for init only
// semi-synchroneous means, that this method waits until all data has been received,
// but the data itself is delivered through the MessageListener to each registered components (e.g. BiDiBLight)
// Therefor this method must only be called after all component managers have initialized all components and thus their message listeners

    /**
     * Request CONFIGX from all ports on all nodes of this connection.
     * Returns after all data has been received.
     * Received data is delivered to registered Message Listeners.
     */
    public void allPortConfigX() {
        log.debug("{}: get alle LC ConfigX", getUserName());
        for(Map.Entry<Long, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            if (NodeUtils.hasSwitchFunctions(node.getUniqueId())) {
                getAllPortConfigX(node, null);
            }
        }
    }
    
    /**
     * Request CONFIGX from all ports on a given node and possibly only for a given type.
     * 
     * @param node requested node
     * @param type - if null, request all port types
     * @return Note: always returns null, since data is not collected synchroneously but delivered to registered Message Listeners.
     */
    public List<LcConfigX> getAllPortConfigX(Node node, LcOutputType type) {
        // get all ports of a type or really all ports if type = null or flat addressing is enabled
        List<LcConfigX> portConfigXList = null;
        int numPorts;
        try {
            if (node.getProtocolVersion().isHigherThan(ProtocolVersion.VERSION_0_5)) { //ConfigX is available since V0.6
                if (node.isPortFlatModelAvailable()) { //flat addressing
                    numPorts = node.getPortFlatModel();
                    if (numPorts > 0) {
                        bidib.getNode(node).getAllConfigX(getPortModel(node), type, 0, numPorts);
                    }
                }
                else { //type based addressing
                    for (LcOutputType t : LcOutputType.values()) {
                        if ( (type == null  ||  type == t)  &&  t.hasPortStatus()  &&  t.getType() <= 15) {
                            numPorts = getTypeCount(node, t);
                            if (numPorts > 0) {
                                bidib.getNode(node).getAllConfigX(getPortModel(node), t, 0, numPorts);
                            }
                        }
                    }
                }
            }
            else { //old Config - type based adressing only
                for (LcOutputType t : LcOutputType.values()) {
                    if ( (type == null  ||  type == t)  &&  t.hasPortStatus()  &&  t.getType() <= 15) {
                        numPorts = getTypeCount(node, t);
                        if (numPorts > 0) {
                            int[] plist = new int[numPorts];
                            for (int i = 0; i < numPorts; i++) {
                                plist[i] = i;
                            }
                            bidib.getNode(node).getConfigBulk(PortModelEnum.type, t, plist);
                        }
                    }
                }
            }
        } catch (ProtocolException e) {
            log.error("getAllConfigX message failed:", e);
        }
        return portConfigXList;
    }
    
    /**
     * Request CONFIGX if a given port on a given node and possibly only for a given type.
     * 
     * @param node requested node
     * @param portAddr as an integer
     * @param type - if null, request all port types
     * @return Note: always returns null, since data is not collected synchroneously but delivered to registered Message Listeners.
     */
    public LcConfigX getPortConfigX(Node node, int portAddr, LcOutputType type) {
        // synchroneous method using MSG_LC_CONFIGX_GET - use for init only
        try {
            if (node.getProtocolVersion().isHigherThan(ProtocolVersion.VERSION_0_5)) { //ConfigX is available since V0.6
                if (node.isPortFlatModelAvailable()) {
                    bidib.getNode(node).getConfigXBulk(getPortModel(node), type, 2 /*Window Size*/, portAddr);
                }
                else {
                    for (LcOutputType t : LcOutputType.values()) {
                        if ( (type == null  ||  type == t)  &&  t.hasPortStatus()  &&  t.getType() <= 15) {
                            bidib.getNode(node).getConfigXBulk(getPortModel(node), t, 2 /*Window Size*/, portAddr);
                        }
                    }
                }
            }
            else {
                for (LcOutputType t : LcOutputType.values()) {
                    if ( (type == null  ||  type == t)  &&  t.hasPortStatus()  &&  t.getType() <= 15) {
                        bidib.getNode(node).getConfigBulk(PortModelEnum.type, t, portAddr);
                    }
                }
            }
        } catch (ProtocolException e) {
            log.error("getConfigXBulk message failed", e);
        }
        return null; ////////TODO remove return value completely
    }
    
    /**
     * Convert a CONFIG object to a CONFIGX object.
     * This is a convenience method so the JMRI components need only to handle the CONFIGX format
     * 
     * @param node context node
     * @param lcConfig the LcConfig object
     * @return a new LcConfigX object
     */
    public LcConfigX convertConfig2ConfigX(Node node, LcConfig lcConfig) {
        Map<Byte, PortConfigValue<?>> portConfigValues = new HashMap<>();
        BidibPort bidibPort;
        PortModelEnum model;
        if (node.isPortFlatModelAvailable()) {
            model = PortModelEnum.flat;
            byte  portType = lcConfig.getOutputType(model).getType();
            int portMap = 1 << portType; //set the corresponding bit only
            ReconfigPortConfigValue pcfg = new ReconfigPortConfigValue(portType, portMap);
            portConfigValues.put(BidibLibrary.BIDIB_PCFG_RECONFIG, pcfg);
        }
        else {
            model = PortModelEnum.type;
        }
        bidibPort = BidibPort.prepareBidibPort(model, lcConfig.getOutputType(model), lcConfig.getOutputNumber(model));
        // fill portConfigValues from lcConfig
        switch (lcConfig.getOutputType(model)) {
            case SWITCHPORT:
                if (getNodeFeature(node, BidibLibrary.FEATURE_SWITCH_CONFIG_AVAILABLE) > 0) {
                    byte ioCtrl = ByteUtils.getLowByte(lcConfig.getValue1()); //BIDIB_PCFG_IO_CTRL - this is not supported for ConfigX any more
                    byte ticks = ByteUtils.getLowByte(lcConfig.getValue2()); //BIDIB_PCFG_TICKS
                    byte switchControl = (2 << 4) | 2; //tristate
                    switch (ioCtrl) {
                        case 0: //simple output
                            ticks = 0; //disable
                            switchControl = (1 << 4); //1 << 4 | 0
                            break;
                        case 1: //high pulse (same than simple output, but turns off after ticks
                            switchControl = (1 << 4); //1 << 4 | 0
                            break;
                        case 2: //low pulse
                            switchControl = 1; // 0 << 4 | 1
                            break;
                        case 3: //tristate
                            ticks = 0;
                            break;
                        default:
                            // same as tristate TODO: Support 4 (pullup) and 5 (pulldown) - port is an input then (??, spec not clear)
                            ticks = 0;
                            break;
                    }
                    BytePortConfigValue pcfgTicks = new BytePortConfigValue(ticks);
                    BytePortConfigValue pcfgSwitchControl = new BytePortConfigValue(switchControl);
                    portConfigValues.put(BidibLibrary.BIDIB_PCFG_TICKS, pcfgTicks);
                    portConfigValues.put(BidibLibrary.BIDIB_PCFG_SWITCH_CTRL, pcfgSwitchControl);
                }
                break;
            case LIGHTPORT:
                BytePortConfigValue pcfgLevelPortOff = new BytePortConfigValue(ByteUtils.getLowByte(lcConfig.getValue1()));
                BytePortConfigValue pcfgLevelPortOn = new BytePortConfigValue(ByteUtils.getLowByte(lcConfig.getValue2()));
                BytePortConfigValue pcfgDimmDown = new BytePortConfigValue(ByteUtils.getLowByte(lcConfig.getValue3()));
                BytePortConfigValue pcfgDimmUp = new BytePortConfigValue(ByteUtils.getLowByte(lcConfig.getValue4()));
                portConfigValues.put(BidibLibrary.BIDIB_PCFG_LEVEL_PORT_OFF, pcfgLevelPortOff);
                portConfigValues.put(BidibLibrary.BIDIB_PCFG_LEVEL_PORT_ON, pcfgLevelPortOn);
                portConfigValues.put(BidibLibrary.BIDIB_PCFG_DIMM_DOWN, pcfgDimmDown);
                portConfigValues.put(BidibLibrary.BIDIB_PCFG_DIMM_UP, pcfgDimmUp);
                break;
            case SERVOPORT:
                BytePortConfigValue pcfgServoAdjL = new BytePortConfigValue(ByteUtils.getLowByte(lcConfig.getValue1()));
                BytePortConfigValue pcfgServoAdjH = new BytePortConfigValue(ByteUtils.getLowByte(lcConfig.getValue2()));
                BytePortConfigValue pcfgServoSpeed = new BytePortConfigValue(ByteUtils.getLowByte(lcConfig.getValue3()));
                portConfigValues.put(BidibLibrary.BIDIB_PCFG_SERVO_ADJ_L, pcfgServoAdjL);
                portConfigValues.put(BidibLibrary.BIDIB_PCFG_SERVO_ADJ_H, pcfgServoAdjH);
                portConfigValues.put(BidibLibrary.BIDIB_PCFG_SERVO_SPEED, pcfgServoSpeed);
                break;
            case BACKLIGHTPORT:
                BytePortConfigValue pcfgDimmDown2 = new BytePortConfigValue(ByteUtils.getLowByte(lcConfig.getValue1()));
                BytePortConfigValue pcfgDimmUp2 = new BytePortConfigValue(ByteUtils.getLowByte(lcConfig.getValue2()));
                BytePortConfigValue pcfgOutputMap = new BytePortConfigValue(ByteUtils.getLowByte(lcConfig.getValue3()));
                portConfigValues.put(BidibLibrary.BIDIB_PCFG_DIMM_DOWN, pcfgDimmDown2);
                portConfigValues.put(BidibLibrary.BIDIB_PCFG_DIMM_UP, pcfgDimmUp2);
                portConfigValues.put(BidibLibrary.BIDIB_PCFG_OUTPUT_MAP, pcfgOutputMap);
                break;
            case INPUTPORT:
                // not really specified, but seems logical...
                byte ioCtrl = ByteUtils.getLowByte(lcConfig.getValue1()); //BIDIB_PCFG_IO_CTRL - this is not supported for ConfigX any more
                byte inputControl = 1; //active HIGH
                switch (ioCtrl) {
                    case 4: //pullup
                        inputControl = 2; //active LOW + Pullup
                        break;
                    case 5: //pulldown
                        inputControl = 3; //active HIGH + Pulldown
                        break;
                    default:
                        // do nothing, leave inputControl at 1
                        break;
                }
                BytePortConfigValue pcfgInputControl = new BytePortConfigValue(inputControl);
                portConfigValues.put(BidibLibrary.BIDIB_PCFG_INPUT_CTRL, pcfgInputControl);
                break;
            default:
                break;
        }
        LcConfigX configX = new LcConfigX(bidibPort, portConfigValues);
        return configX;
    }
    
    // Asynchronous methods to request the status of all BiDiB ports
    // For this reason there is no need to query  FEATURE_CTRL_PORT_QUERY_AVAILABLE, since without this feature there would simply be no answer for the port.
    
    /**
     * Request LC_STAT from all ports on all nodes of this connection.
     * Returns immediately.
     * Received data is delivered to registered Message Listeners.
     */
    public void allPortLcStat() {
        log.debug("{}: get alle LC stat", getUserName());
        for(Map.Entry<Long, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            if (NodeUtils.hasSwitchFunctions(node.getUniqueId())) {
                portLcStat(node, 0xFFFF);
            }
        }
    }
    
    /**
     * Request LC_STAT from all ports on a given node.
     * Returns immediately.
     * Received data is delivered to registered Message Listeners.
     * The differences for the addressing model an the old LC_STAT handling are hidden to the caller.
     * 
     * @param node selected node
     * @param typemask a 16 bit type mask where each bit represents a type, Bit0 is SWITCHPORT, Bit1 is LIGHTPORT and so on. Bit 15 is INPUTPORT.
     *        Return LC_STAT only for ports which are selected on the type mask (the correspondend bit is set).
     */
    public void portLcStat(Node node, int typemask) {
        if (NodeUtils.hasSwitchFunctions(node.getUniqueId())) {
            BidibRequestFactory rf = getBidib().getRootNode().getRequestFactory();
            if (node.getProtocolVersion().isHigherThan(ProtocolVersion.VERSION_0_6)) {
                // fast bulk query of all ports (new in bidib protocol version 0.7)
//                int numPorts;
//                if (node.isPortFlatModelAvailable()) {
//                    numPorts = node.getPortFlatModel();
//                    if (numPorts > 0) {
//                        //BidibCommandMessage m = (BidibCommandMessage)rf.createPortQueryAll(typemask, 0, numPorts);
//                        BidibCommandMessage m = (BidibCommandMessage)rf.createPortQueryAll(typemask, 0, 0xFFFF);
//                        sendBiDiBMessage(m, node);
//                    }
//                }
//                else { //type based addressing
//                    for (LcOutputType t : LcOutputType.values()) {
//                        int tmask = 1 << t.getType();
//                        if ( ((tmask & typemask) != 0)  &&  t.hasPortStatus()  &&  t.getType() <= 15) {
//                            numPorts = getTypeCount(node, t);
//                            if (numPorts > 0) {
//                                // its not clear how PORT_QUERY_ALL is defined for type based addressing
//                                // so we try the strictest way
//                                BidibPort fromPort = BidibPort.prepareBidibPort(PortModelEnum.type, t, 0);
//                                BidibPort toPort = BidibPort.prepareBidibPort(PortModelEnum.type, t, numPorts);
//                                int from = ByteUtils.getWORD(fromPort.getValues());
//                                int to = ByteUtils.getWORD(toPort.getValues());
//                                BidibCommandMessage m = (BidibCommandMessage)rf.createPortQueryAll(tmask, from, to);
//                                //BidibCommandMessage m = (BidibCommandMessage)rf.createPortQueryAll(typemask, 0, 0xFFFF);
//                                sendBiDiBMessage(m, node);
//                                //break;
//                            }
//                        }
//                    }
//                }
                // just query everything
                BidibCommandMessage m = rf.createPortQueryAll(typemask, 0, 0xFFFF);
                sendBiDiBMessage(m, node);
            }
            else {
                // old protocol versions (<= 0.6 - request every single port
                int numPorts;
                if (node.isPortFlatModelAvailable()) {
                    // since flat addressing is only available since version 0.6, this is only possible with exactly version 0.6
                    numPorts = node.getPortFlatModel();
                    for (int addr = 0; addr < numPorts; addr++) {
                        BidibCommandMessage m = rf.createLcPortQuery(getPortModel(node), null, addr);
                        sendBiDiBMessage(m, node);
                    }
                }
                else { //type based adressing
                    for (LcOutputType t : LcOutputType.values()) {
                        int tmask = 1 << t.getType();
                        if ( ((tmask & typemask) != 0)  &&  t.hasPortStatus()  &&  t.getType() <= 7) { //outputs only - for old protocol version
                            numPorts = getTypeCount(node, t);
                            for (int addr = 0; addr < numPorts; addr++) {
                                BidibCommandMessage m = rf.createLcPortQuery(getPortModel(node), t, addr);
                                sendBiDiBMessage(m, node);
                            }
                        }
                    }
                    // inputs have a separate message type in old versions: MSG_LC_KEY_QUERY
                    LcOutputType t = LcOutputType.INPUTPORT;
                    int tmask = 1 << t.getType();
                    if ( ((tmask & typemask) != 0) ) {
                        numPorts = getTypeCount(node, t);
                        for (int addr = 0; addr < numPorts; addr++) {
                            BidibCommandMessage m = rf.createLcKey(addr);
                            sendBiDiBMessage(m, node);
                        }
                    }
                }
            }
        }        
    }

// Asynchronous methods to request the status of all BiDiB feedback channels

    public void allAccessoryState() {
        log.debug("{}: get alle accessories", getUserName());
        for(Map.Entry<Long, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            if (NodeUtils.hasAccessoryFunctions(node.getUniqueId())) {
                accessoryState(node);
            }
        }
    }
    
    public void accessoryState(Node node) {
        int accSize = getNodeFeature(node, BidibLibrary.FEATURE_ACCESSORY_COUNT);
        if (NodeUtils.hasAccessoryFunctions(node.getUniqueId())  &&  accSize > 0 ) {
            log.info("Requesting accessory status on node {}", node);
            for (int addr = 0; addr < accSize; addr++) {
                sendBiDiBMessage(new AccessoryGetMessage(addr), node);
            }
        }
    }
    
    /**
     * Request Feedback Status (called BM status in BiDiB - BM (Belegtmelder) is german for "feedback") from all ports on all nodes of this connection.
     * Returns immediately.
     * Received data is delivered to registered Message Listeners.
     */
    public void allFeedback() {
        //log.debug("{}: get alle feedback", getUserName());
        for(Map.Entry<Long, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            if (NodeUtils.hasFeedbackFunctions(node.getUniqueId())) {
                feedback(node);
            }
        }
    }
    
    /**
     * Request Feedback Status (called BM status in BiDiB - BM (Belegtmelder) is german for "feedback") from all ports on a given node.
     * Returns immediately.
     * Received data is delivered to registered Message Listeners.
     * 
     * @param node selected node
     */
    public void feedback(Node node) {
        int bmSize = getNodeFeature(node, BidibLibrary.FEATURE_BM_SIZE);
        if (NodeUtils.hasFeedbackFunctions(node.getUniqueId())  &&  bmSize > 0 ) {
            log.info("Requesting feedback status on node {}", node);
            sendBiDiBMessage(new FeedbackGetRangeMessage(0, bmSize), node);
        }
    }
    
// End of obsolete Methods
    
// BiDiB Message handling
    
    /**
     * Forward a preformatted BiDiBMessage to the actual interface.
     *
     * @param m Message to send;
     * @param node BiDiB node to send the message to
     */
    public void sendBiDiBMessage(BidibCommandMessage m, Node node) {
        if (node == null) {
            log.error("node is undefined! - can't send message.");
            return;
        }
        log.trace("sendBiDiBMessage: {} on node {}", m, node);
        // be sure that the node is in correct mode
        if (checkProgMode((m.getType() == BidibLibrary.MSG_CS_PROG), node) >= 0) {
            try {
                log.trace("  bidib node: {}", getBidib().getNode(node));
                BidibNodeAccessor.sendNoWait(getBidib().getNode(node), m);
            } catch (ProtocolException e) {
                log.error("sending BiDiB message failed", e);
            }
        }
        else {
            log.error("switching to or from PROG mode (global programmer) failed!");
        }
    }
    
    /**
     * Check if the command station is in the requested state (Normal, PT)
     * If the command station is not in the requested state, a message is sent to BiDiB to switch to the requested state.
     * 
     * @param needProgMode true if we request the command station to be in programming state, false if normal state is requested
     * @param node selected node
     * @return 0 if nothing to do, 1 if state has been changed, -1 on error
     */
    public synchronized int checkProgMode(boolean needProgMode, Node node) {
        log.trace("checkProgMode: needProgMode: {}, node: {}", needProgMode, node);
        int hasChanged = 0;
        CommandStationState neededMode = needProgMode ? CommandStationState.PROG : mSavedMode;
        if (needProgMode != mIsProgMode.get()) {
            Node progNode = getCurrentGlobalProgrammerNode();
            if (node == progNode) {
                Node csNode = getFirstCommandStationNode();

                log.debug("use global programmer node: {}", progNode);
                CommandStationNode progCsNode = getBidib().getCommandStationNode(progNode); //check if the programmer node also a command station - should have been tested before anyway
                if (progCsNode == null) { //just in case...
                    currentGlobalProgrammerNode = null;
                    hasChanged = -1;
                }
                else {
                    log.debug("change command station mode to PROG? {}", needProgMode);
                    if (needProgMode) {
                        if (node == csNode) {
                            // if we have to switch to prog mode, disable watchdog timer - but only, if we switch the command station
                            setWatchdogTimer(false);
                        }
                    }
                    try {
                        CommandStationState CurrentMode = progCsNode.setState(neededMode); //send and wait for response
                        synchronized (mIsProgMode) {
                            if (!needProgMode) {
                                mSavedMode = CurrentMode;
                            }
                            mIsProgMode.set(needProgMode);
                        }
                        hasChanged = 1;
                    }
                    catch (ProtocolException e) {
                        log.error("sending MSG_CS_STATE message failed", e);
                        currentGlobalProgrammerNode = null;
                        hasChanged = -1;
                    }
                    log.trace("new saved mode: {}, is ProgMode: {}", mSavedMode, mIsProgMode);
                }
            }
        }
        if (!mIsProgMode.get()) {
            synchronized (progTimer) {
                if (progTimer.isRunning()) {
                    progTimer.stop();
                    log.trace("progTimer stopped.");
                }
            }
            setCurrentGlobalProgrammerNode(null); //invalidate programmer node so it must be evaluated again the next time
        }

        return hasChanged;
    }
        
    private void progTimeout() {
        log.trace("timeout - stop global programmer PROG mode - reset to {}", mSavedMode);
        checkProgMode(false, getCurrentGlobalProgrammerNode());
    }

    
// BiDiB Watchdog
    
    private class WatchdogTimerTask extends java.util.TimerTask {
        @Override
        public void run () {
            // If the timer times out, send MSG_CS_STATE_ON message
            synchronized (watchdogStatus) {
                if (watchdogStatus.get()) { //if still enabled
                    sendBiDiBMessage(new CommandStationSetStateMessage(CommandStationState.GO), getFirstCommandStationNode());
                }
            }
        }   
    }

    public final void setWatchdogTimer(boolean state) {
        synchronized (watchdogStatus) {
            Node csnode = getFirstCommandStationNode();
            long timeout = 0;
            log.trace("setWatchdogTimer {} on node {}", state, csnode);
            if (csnode != null) {
                timeout = getNodeFeature(csnode, BidibLibrary.FEATURE_GEN_WATCHDOG) * 100L; //value in milliseconds
                log.trace("FEATURE_GEN_WATCHDOG in ms: {}", timeout);
                if (timeout < 2000) {
                    timeout = timeout / 2; //half the devices watchdog timeout value for small values
                }
                else {
                    timeout = timeout - 1000; //one second less the devices watchdog timeout value for larger values
                }
            }
            if (timeout > 0  &&  state) {
                log.debug("set watchdog TRUE, timeout: {} ms", timeout);
                watchdogStatus.set(true);
                if (watchdogTimer != null) {
                    watchdogTimer.cancel();
                }
                watchdogTimer = new WatchdogTimerTask(); // Timer used to periodically MSG_CS_STATE_ON
                jmri.util.TimerUtil.schedule(watchdogTimer, timeout, timeout);
            }
            else {
                log.debug("set watchdog FALSE, requested state: {}, timeout", state);
                watchdogStatus.set(false);
                if (watchdogTimer != null) {
                    watchdogTimer.cancel();
                }
                watchdogTimer = null;
            }
        }
    }


    /**
     * Reference to the system connection memo.
     */
    BiDiBSystemConnectionMemo mMemo = null;

    /**
     * Get access to the system connection memo associated with this traffic
     * controller.
     *
     * @return associated systemConnectionMemo object
     */
    public BiDiBSystemConnectionMemo getSystemConnectionMemo() {
        return (mMemo);
    }

    /**
     * Set the system connection memo associated with this traffic controller.
     *
     * @param m associated systemConnectionMemo object
     */
    public void setSystemConnectionMemo(BiDiBSystemConnectionMemo m) {
        mMemo = m;
    }

// Command Station interface

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSystemPrefix() {
        if (mMemo != null) {
            return mMemo.getSystemPrefix();
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserName() {
        if (mMemo != null) {
            return mMemo.getUserName();
        }
        return "";
    }

    /**
     * {@inheritDoc}
     * 
     * Not supported! We probably don't need the command station interface at all...
     * ... besides perhaps consist control or DCC Signal Mast / Head ??
     */
    @Override
    public boolean sendPacket(byte[] packet, int repeats) {
        log.debug("sendPacket: {}, prefix: {}", packet, mMemo.getSystemPrefix());
        if (packet != null  &&  packet.length >= 4) {
            //log.debug("Addr: {}, aspect: {}, repeats: {}", NmraPacket.getAccSignalDecoderPktAddress(packet), packet[2], repeats);
            //log.debug("Addr: {}, aspect: {}, repeats: {}", NmraPacket.getAccDecoderPktAddress(packet), packet[2], repeats);
            log.debug("Addr: {}, addr type: {}, aspect: {}, repeats: {}", NmraPacket.extractAddressType(packet), NmraPacket.extractAddressNumber(packet), packet[2], repeats);
            //throw new UnsupportedOperationException("Not supported yet.");
            log.warn("sendPacket is not supported for BiDiB so far");
        }
        return false;
    }

// NOT USED for now
//    /**
//     * Get the Lower byte of a locomotive address from the decimal locomotive
//     * address.
//     */
//    public static int getDCCAddressLow(int address) {
//        /* For addresses below 128, we just return the address, otherwise,
//         we need to return the upper byte of the address after we add the
//         offset 0xC000. The first address used for addresses over 127 is 0xC080*/
//        if (address < 128) {
//            return (address);
//        } else {
//            int temp = address + 0xC000;
//            temp = temp & 0x00FF;
//            return temp;
//        }
//    }
//
//    /**
//     * Get the Upper byte of a locomotive address from the decimal locomotive
//     * address.
//     */
//    public static int getDCCAddressHigh(int address) {
//        /* this isn't actually the high byte, For addresses below 128, we
//         just return 0, otherwise, we need to return the upper byte of the
//         address after we add the offset 0xC000 The first address used for
//         addresses over 127 is 0xC080*/
//        if (address < 128) {
//            return (0x00);
//        } else {
//            int temp = address + 0xC000;
//            temp = temp & 0xFF00;
//            temp = temp / 256;
//            return temp;
//        }
//    }
    
    
// Shutdown function

    protected void terminate () {
        log.debug("Cleanup starts {}", this);
        if (bidib == null  ||  !bidib.isOpened()) {
            return;    // no connection established
        }
        Node node = getCurrentGlobalProgrammerNode();
        if (node != null) {
            checkProgMode(false, node); //possibly switch to normal mode
        }
        setWatchdogTimer(false); //stop watchdog
        // sending SYS_DISABLE disables all spontaneous messages and thus informs all nodes that the host will probably disappear
        try {
            log.info("sending sysDisable to {}", getRootNode());
            bidib.getRootNode().sysDisable(); //Throws ProtocolException
        }
        catch (ProtocolException e) {
            log.error("unable to disable node", e);
        }
        
        log.debug("Cleanup ends");
    }
    
//    /** NO LONGER USED
//     * Internal class to handle traffic controller cleanup. The primary task of
//     * this thread is to make sure the DCC system has exited service mode when
//     * the program exits.
//     */
//    static class CleanupHook implements Runnable {
//
//        BiDiBTrafficController tc;
//
//        CleanupHook(BiDiBTrafficController tc) {
//            this.tc = tc;
//        }
//
//        @Override
//        public void run() {
//            tc.terminate();
//        }
//    }
//
    
    private final static Logger log = LoggerFactory.getLogger(BiDiBTrafficController.class);

}
