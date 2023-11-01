package jmri.jmrix.bidib;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bidib.jbidibc.core.BidibMessageProcessor;
import org.bidib.jbidibc.messages.ConnectionListener;
import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.messages.MessageReceiver;
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.core.NodeListener;
import org.bidib.jbidibc.messages.base.RawMessageListener;
import org.bidib.jbidibc.messages.exception.PortNotFoundException;
import org.bidib.jbidibc.messages.exception.PortNotOpenedException;
import org.bidib.jbidibc.messages.helpers.Context;
import org.bidib.jbidibc.core.node.AccessoryNode;
import org.bidib.jbidibc.core.node.BidibNode;
import org.bidib.jbidibc.core.node.BoosterNode;
import org.bidib.jbidibc.core.node.CommandStationNode;
import org.bidib.jbidibc.core.node.InterfaceNode;
import org.bidib.jbidibc.core.node.NodeRegistry;
import org.bidib.jbidibc.core.node.RootNode;
import org.bidib.jbidibc.core.node.listener.TransferListener;
import org.bidib.jbidibc.messages.utils.NodeUtils;

/**
 * Test scaffold to replace the jbidibc BidibInterface
 *
 * @author  Eckart Meyer  Copyright (C) 2020
 */

public class BiDiBInterfaceScaffold implements org.bidib.jbidibc.core.BidibInterface {
    
    private final NodeRegistry nodeRegistry;
    //private final BidibInterface bidib = Mockito.mock(BidibInterface.class);

    public BiDiBInterfaceScaffold() {
        nodeRegistry = new NodeRegistry();// {
//            @Override
//            public BidibNode getNode(final Node node) {
//                return super.getNode(node);
//            }
//            @Override
//            protected BidibNode createBidibNode(final Node node) {
//                // create the new bidib node
//                
//                setBidib(bidib);
//                BidibNode bidibNode = null;
//                bidibNode = super.createBidibNode(node);
//                if (Arrays.equals(Node.ROOTNODE_ADDR, node.getAddr())) {
//                    //bidibNode = new TestBidibRootNode(node);
//                    
//                }
//                else {
//                    //bidibNode = new TestBidibNode(node);
//                }
//                // initialize the node
//                //bidibNode.setUniqueId(node.getUniqueId());
//                //bidibNode.setBidib(this.getBidib());
//                
//                //bidibNode.setRequestFactory(requestFactory);
//                //bidibNode.setResponseTimeout(bidib.getResponseTimeout());
//                //bidibNode.setFirmwarePacketTimeout(bidib.getFirmwarePacketTimeout());
//
//                //LOGGER.info("Created new bidibNode, firmwarePacketTimeout: {}", bidib.getFirmwarePacketTimeout());
//
//                return bidibNode;
//            }
//            @Override
//            public RootNode getRootNode() {
//                byte[] a = {0};
////                Node node = new Node(42, a, 0);
//                Node node = org.bidib.jbidibc.messages.Node.createNode(42, a, 0);
//                BidibNode n = createBidibNode(node);
//                return (RootNode)n;
//            }
//
        //};
        nodeRegistry.setBidib(this);
        //nodeRegistry.setMessageReceiver(new TestMessageProcessor(nodeRegistry, null, false));

    }
    
    @Override
    public MessageReceiver getMessageReceiver() {
        return null;
    }

    @Override
    public BidibMessageProcessor getBidibMessageProcessor() {
        return null;
    }

    @Override
    public BidibNode getNode(Node node) {
        node.setRegistered(true);
        return nodeRegistry.getNode(node);
    }
    
    @Override
    public boolean isValidCoreNode(Node node) {
        return true;
    }
    
    @Override
    public BidibNode findNode(byte[] nodeAddress) {
        return null;
    }

    @Override
    public RootNode getRootNode() {
        return nodeRegistry.getRootNode();
    }

    @Override
    public void releaseRootNode() {
    }

    @Override
    public void releaseSubNodesOfRootNode() {
    }


    @Override
    public AccessoryNode getAccessoryNode(Node node) {
        return null;
    }
    
    @Override
    public BoosterNode getBoosterNode(Node node) {
        return null;
    }

    @Override
    public CommandStationNode getCommandStationNode(Node node) {
        if (NodeUtils.hasCommandStationFunctions(node.getUniqueId())) {
            return nodeRegistry.getCommandStationNode(node);
        }
        return null;
    }

    @Override
    public InterfaceNode getInterfaceNode(Node node) {
        return null;
    }

    @Override
    public void send(final byte[] data) {
    }
    
    @Override
    public void open(
        String portName, ConnectionListener connectionListener, Set<NodeListener> nodeListeners,
        Set<MessageListener> messageListeners, Set<TransferListener> transferListeners, final Context context)
        throws PortNotFoundException, PortNotOpenedException {
        
    }

    @Override
    public boolean isOpened() {
        return false;
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public void signalUserAction(String actionKey, final Context context) {    
    }
    
    @Override
    public List<String> getPortIdentifiers() {
        return new ArrayList<>();
    }

    @Override
    public void setIgnoreWaitTimeout(boolean ignoreWaitTimeout) {
    }

    @Override
    public int getResponseTimeout() {
        return 0;
    }
    
    @Override
    public void setResponseTimeout(int responseTimeout) {
    }
    
    @Override
    public void setFirmwarePacketTimeout(int firmwarePacketTimeout) {
    }
    
    @Override
    public int getFirmwarePacketTimeout() {
        return 0;
    }
        
    @Override
    public void addRawMessageListener(final RawMessageListener rawMessageListener) {
    }
    
    @Override
    public void removeRawMessageListener(final RawMessageListener rawMessageListener) {
    }
    
    @Override
    public void attach(Long uniqueId) {
    }       

    @Override
    public void detach(Long uniqueId) {
    }       
}
