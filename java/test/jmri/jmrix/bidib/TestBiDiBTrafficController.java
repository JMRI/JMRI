package jmri.jmrix.bidib;

import jmri.jmrix.PortAdapter;
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.core.node.BidibNode;
import org.bidib.jbidibc.core.BidibInterface;
import org.bidib.jbidibc.messages.BidibLibrary;
import org.bidib.jbidibc.messages.Feature;
import org.bidib.jbidibc.messages.ProtocolVersion;
import org.bidib.jbidibc.messages.SoftwareVersion;
import org.bidib.jbidibc.messages.StringData;
import org.bidib.jbidibc.messages.helpers.Context;
import org.bidib.jbidibc.messages.helpers.DefaultContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class to replace the BiDiBTrafficController in tests
 *
 * @author  Eckart Meyer  Copyright (C) 2020, 2025
 */
public class TestBiDiBTrafficController extends BiDiBTrafficController {
    
    public TestBiDiBTrafficController(BidibInterface b) {
        super(b);
        BidibNode bidibNode;
        byte[] addr = {0};
        int nodeVersion = 42;
        long uid = 0xDE000D68001234L; //this is command station like GMBboost Master (CS, Prog and feedback)
        //Node node = new Node(nodeVersion, addr, uid);
        Node node = org.bidib.jbidibc.messages.Node.createNode(nodeVersion, addr, uid);
        bidibNode = b.getNode(node);
        node.setStoredString(StringData.INDEX_USERNAME, "Test0");
        node.setProtocolVersion(ProtocolVersion.VERSION_0_7);
        node.setSoftwareVersion(new SoftwareVersion (1,42,0));
        nodes.put(uid & 0x0000FFFFFFFFFFL, node);
        //log.warn("new node1: {}", node);
        addr[0] = 1;
        uid = 0x45000DE8004321L; //this a light and feedback and accessory decoder
        //node = new Node(nodeVersion, addr, uid);
        node = org.bidib.jbidibc.messages.Node.createNode(nodeVersion, addr, uid);
        bidibNode = b.getNode(node);
        node.setStoredString(StringData.INDEX_USERNAME, "Test1");
        node.setProtocolVersion(ProtocolVersion.VERSION_0_7);
        node.setSoftwareVersion(new SoftwareVersion (1,42,1));
        node.setFeature(new Feature(BidibLibrary.FEATURE_CTRL_LIGHT_COUNT, 32));
        node.setFeature(new Feature(BidibLibrary.FEATURE_CTRL_INPUT_COUNT, 32));
        nodes.put(uid & 0x0000FFFFFFFFFFL, node);
        //log.warn("new node2: {}", node);
        addr[0] = 2;
        uid = 0x01000DE8009876L; //this a light only controller
        //node = new Node(nodeVersion, addr, uid);
        node = org.bidib.jbidibc.messages.Node.createNode(nodeVersion, addr, uid);
        bidibNode = b.getNode(node);
        node.setStoredString(StringData.INDEX_USERNAME, "Test2");
        node.setProtocolVersion(ProtocolVersion.VERSION_0_7);
        node.setSoftwareVersion(new SoftwareVersion (1,42,2));
        node.setFeature(new Feature(BidibLibrary.FEATURE_CTRL_PORT_FLAT_MODEL, 64));
        node.setPortFlatModel(64);
        nodes.put(uid & 0x0000FFFFFFFFFFL, node);
        //log.warn("new node3: {}", node);
        addr[0] = 3;
        uid = 0x45000DE8014321L; //this a light and feedback and accessory decoder
        //node = new Node(nodeVersion, addr, uid);
        node = org.bidib.jbidibc.messages.Node.createNode(nodeVersion, addr, uid);
        bidibNode = b.getNode(node);
        node.setStoredString(StringData.INDEX_USERNAME, "01.02-03_04"); //contains all allowed special characters and a leading number
        node.setProtocolVersion(ProtocolVersion.VERSION_0_7);
        node.setSoftwareVersion(new SoftwareVersion (1,42,3));
        node.setFeature(new Feature(BidibLibrary.FEATURE_CTRL_LIGHT_COUNT, 32));
        node.setFeature(new Feature(BidibLibrary.FEATURE_CTRL_INPUT_COUNT, 32));
        nodes.put(uid & 0x0000FFFFFFFFFFL, node);
        //log.warn("new node2: {}", node);
        log.trace("bidibNode: {}", bidibNode);
    }

    @Override
    public Context connnectPort(PortAdapter p) {
        return new DefaultContext();
    }

    @Override
    public boolean sendPacket(byte[] packet, int repeats) {
        log.debug("sendPacket: {}, prefix: {}", packet, mMemo.getSystemPrefix());
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(TestBiDiBTrafficController.class);

}
