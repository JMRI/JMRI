package jmri.jmrix.bidib;

import java.util.Map;
import java.util.LinkedList;
import java.util.SortedSet;

import jmri.InstanceManager;

import org.bidib.jbidibc.core.BidibInterface;
import org.bidib.jbidibc.core.node.BidibNode;
import org.bidib.jbidibc.messages.BidibLibrary;
import org.bidib.jbidibc.messages.Feature;
import org.bidib.jbidibc.messages.FeatureData;
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.messages.StringData;
import org.bidib.jbidibc.messages.exception.ProtocolException;
import org.bidib.jbidibc.messages.utils.ByteUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class initializes or deinitializes a BiDiB node when it is found on system startup or if it
 * is discovered or lost while the system is running.\
 *
 * The real work is done in its own thread and from a node queue. Initializing is a time consuming
 * process since a lot of data is read from the node.
 * 
 * @author Eckart Meyer Copyright (C) 2023
 */
public class BiDiBNodeInitializer implements Runnable {

    private static class SimplePair {
        public Node node;
        public boolean isNewNode; //true: new node, false: node lost
        
        public SimplePair(Node node, boolean isNewNode) {
            this.node = node;
            this.isNewNode = isNewNode;
        }
    }
    
    private final Map<Long, Node> nodes;
    private final BidibInterface bidib;
    private final BiDiBTrafficController tc;
    private SimplePair currentNode;
    private Thread initThread;
    private final LinkedList<SimplePair> queue;
    
    
    public BiDiBNodeInitializer(BiDiBTrafficController tc, BidibInterface bidib, Map<Long, Node> nodes) {
        this.bidib = bidib;
        this.nodes = nodes;
        this.tc = tc;
        queue = new LinkedList<>();
        log.debug("BiDiB node initializer created");
    }
    
    /**
     * Get everything we need from the node. The node must already be inserted into the BiDiB node list.
     * 
     * @param node node to initialize
     * @throws ProtocolException when features can't be loaded
     */
    public void initNode(Node node) throws ProtocolException {
        if (node != null) {
            BidibNode bidibNode = bidib.getNode(node);
            log.info("+++ found node: {}", node);

            int magic = bidibNode.getMagic(0);
            log.debug("Node returned magic: 0x{}", ByteUtils.magicToHex(magic));
            if (magic == 0xAFFE) {
                node.setStoredString(StringData.INDEX_PRODUCTNAME, bidibNode.getString(0, StringData.INDEX_PRODUCTNAME).getValue());
                node.setStoredString(StringData.INDEX_USERNAME, bidibNode.getString(StringData.NAMESPACE_NODE, StringData.INDEX_USERNAME).getValue());
                node.setProtocolVersion(bidibNode.getProtocolVersion());
                node.setSoftwareVersion(bidibNode.getSwVersion());
                log.info("Product name: {}", node.getStoredString(StringData.INDEX_PRODUCTNAME));
                log.info("User name: {}", node.getStoredString(StringData.INDEX_USERNAME));
                log.info("Protocol version: {}", node.getProtocolVersion());
                log.info("Software version: {}", node.getSoftwareVersion());

                try {
                    FeatureData features = bidibNode.getFeaturesAll();
                    log.info("featureCount: {}", features.getFeatureCount());
                    if (features.isStreamingSupport()) {
                        int k = 1;//counter is for debug only
                        for (Feature feature : features.getFeatures()) {
                            log.trace("feature #{}/{}", k++, features.getFeatureCount());
                            log.info("feature.type: {}, value: {}, name: {}", feature.getType(), feature.getValue(), feature.getFeatureName());
                            node.setFeature(feature);
                        }
                    }
                    else {
                        Feature feature;
                        int k = 1;//counter is for debug only
                        try {
                            while ((feature = bidibNode.getNextFeature()) != null) {
                                log.trace("feature #{}/{}", k++, features.getFeatureCount());
                                log.info("feature.type: {}, value: {}, name: {}", feature.getType(), feature.getValue(), feature.getFeatureName());
                                node.setFeature(feature);
                            }
                        }
                        catch (ProtocolException ex) {
                            log.debug("No more features.");
                        }
                    }
                }
                catch (ProtocolException ex) {
                    log.error("Features can't be loaded from node: {}", ex.getMessage());
                }
                log.info("Finished query features."); // NOSONAR

                node.setFeature(new Feature(BidibLibrary.FEATURE_ACCESSORY_MACROMAPPED, 0)); //we do not handle macros in JMRI, so for test, don't assume them to be loaded
                //node.setFeature(new Feature(BidibLibrary.FEATURE_GEN_SWITCH_ACK, 0)); //Test

                Feature relevantPidBits = Feature.findFeature(node.getFeatures(), BidibLibrary.FEATURE_RELEVANT_PID_BITS);
                if (relevantPidBits != null) {
                    node.setRelevantPidBits(relevantPidBits.getValue());
                }
                Feature stringSize = Feature.findFeature(node.getFeatures(), BidibLibrary.FEATURE_STRING_SIZE);
                if (stringSize != null) {
                    node.setStringSize(stringSize.getValue());
                }
                Integer portcount = 0;
                Feature flatModel = Feature.findFeature(node.getFeatures(), BidibLibrary.FEATURE_CTRL_PORT_FLAT_MODEL_EXTENDED);
                if (flatModel != null) {
                    portcount = flatModel.getValue() * 256;
                }
                flatModel = Feature.findFeature(node.getFeatures(), BidibLibrary.FEATURE_CTRL_PORT_FLAT_MODEL);
                if (flatModel != null) {
                    portcount += flatModel.getValue();
                }
                if (portcount > 0) {
                    node.setPortFlatModel(portcount);
                }
            }
            log.debug("+++ node init finished: {}", node);
        }
    }
    
    /**
     * Remove a node from all named beans and from the nodes list
     * 
     * @param node to remove
     */
    public void nodeLost(Node node) {
        log.error("BiDiB node lost! {}", node);
        startNodeUpdate(node, false);
    }
    
    /**
     * Add a node to nodes list and notify all named beans to update
     * 
     * @param node to add
     */
    public void nodeNew(Node node) {
        log.warn("New BiDiB node found {}", node);
        long uid = node.getUniqueId() & 0x0000ffffffffffL; //mask the classid
        nodes.put(uid, node);
        startNodeUpdate(node, true);
    }
    
    
    // private methods to execute nodeLost/nodeNew in one low priority thread
        
    private <T> void nodeLost(SortedSet<T> beanSet, long uniqueId) {
        beanSet.forEach( (nb) -> {
            if (nb instanceof BiDiBNamedBeanInterface) {
                BiDiBAddress addr = ((BiDiBNamedBeanInterface)nb).getAddr();
                log.trace("check bean: {}", nb);
                if (addr.getNodeUID() == uniqueId) {
                    addr.invalidate();
                    ((BiDiBNamedBeanInterface)nb).nodeLost();
                }
            }
        });
    }
    
    private void nodeLostBeans(long uniqueId) {
        long uid = uniqueId & 0x0000ffffffffffL; //mask the classid
        nodeLost(InstanceManager.getDefault(jmri.TurnoutManager.class).getNamedBeanSet(), uid);
        nodeLost(InstanceManager.getDefault(jmri.SensorManager.class).getNamedBeanSet(), uid);
        nodeLost(InstanceManager.getDefault(jmri.LightManager.class).getNamedBeanSet(), uid);
        nodeLost(InstanceManager.getDefault(jmri.ReporterManager.class).getNamedBeanSet(), uid);
        nodeLost(InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBeanSet(), uid);
        nodes.remove(uid);
    }
    
    private <T> void nodeNew(SortedSet<T> beanSet, Node node) {
        beanSet.forEach( (nb) -> {
            if (nb instanceof BiDiBNamedBeanInterface) {
                BiDiBAddress addr = ((BiDiBNamedBeanInterface)nb).getAddr();
                log.trace("check bean: {}", nb);
                if (!addr.isValid()) {
                    ((BiDiBNamedBeanInterface)nb).nodeNew();
                }
            }
        });
    }
    
    private void nodeNewBeans(Node node) {
        try {
            tc.getBidib().getRootNode().sysEnable();
        }
        catch (ProtocolException e) {
            log.warn("failed to ENABLE node {}", node, e);
        }
        nodeNew(InstanceManager.getDefault(jmri.TurnoutManager.class).getNamedBeanSet(), currentNode.node);
        nodeNew(InstanceManager.getDefault(jmri.SensorManager.class).getNamedBeanSet(), currentNode.node);
        nodeNew(InstanceManager.getDefault(jmri.LightManager.class).getNamedBeanSet(), currentNode.node);
        nodeNew(InstanceManager.getDefault(jmri.ReporterManager.class).getNamedBeanSet(), currentNode.node);
        nodeNew(InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBeanSet(), currentNode.node);
        BiDiBSensorManager bs = (BiDiBSensorManager)tc.getSystemConnectionMemo().getSensorManager();
        if (bs != null) {
            bs.updateNodeFeedbacks(node);
        }
        BiDiBReporterManager br = (BiDiBReporterManager)tc.getSystemConnectionMemo().getReporterManager();
        if (br != null) {
            br.updateNode(node);
        }
    }
    
    /**
     * Insert a node into the queue. Start Thread if currently not running
     * 
     * @param node to add or remove
     * @param isNewNode - true: add new node, false: remove node
     */
    private void startNodeUpdate(Node node, boolean isNewNode) {
        
        synchronized (queue) {
            // check if the thread is still working 
            if (queue.isEmpty()  ||  initThread == null  ||  !initThread.isAlive()) {
                if (initThread != null) {
                    try {
                        initThread.join(1000); //wait until the thread has definitly died
                    }
                    catch (InterruptedException e) {}
                }
                initThread = new Thread(this, "NodeInitThread"); //create a new thread
                initThread.setPriority(Thread.MIN_PRIORITY);
                queue.add(new SimplePair(node, isNewNode));
                initThread.start();
                log.debug("thread was started - return");
            }
            else {
                // Thread running, just add the node to the queue
                queue.add(new SimplePair(node, isNewNode));
                log.debug("thread running, just add node to queue and return");
            }
        }
    }
    
    
    /**
     * Execute queued node init and named beans update.
     * Finish the thread if the queue is empty
     */
    @Override
    public void run() {
        log.debug("starting thread for node initialization");
        while (true) {
            log.trace("-- loop, queue size: {}", queue.size());
            synchronized (queue) {
                log.trace("  currentNode: {}", currentNode);
                if (currentNode != null) { //if we just processed a node ...
                    queue.removeFirst(); //...remove it from the queue
                    currentNode = null;
                }
                currentNode = queue.peekFirst(); //get next from queue
                if (currentNode == null) {
                    break; //exit while loop and stop thread by exiting run()
                }
            }
            // now do the real work - initialize node and beans
            if (currentNode.isNewNode) {
                try {
                    initNode(currentNode.node);
                    nodeNewBeans(currentNode.node);
                }
                catch (Exception e) {
                    log.warn("error initializing node {}", currentNode.node, e);
                }
            }
            else {
                nodeLostBeans(currentNode.node.getUniqueId());
            }
        }
        log.debug("thread finished for node");
    }

    private final static Logger log = LoggerFactory.getLogger(BiDiBNodeInitializer.class);
}
