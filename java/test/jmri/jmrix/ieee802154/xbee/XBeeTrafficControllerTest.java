package jmri.jmrix.ieee802154.xbee;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * XBeeTrafficControllerTest.java
 *
 * Test for the jmri.jmrix.ieee802154.xbee.XBeeTrafficController
 * class
 *
 * @author Paul Bender
 */
public class XBeeTrafficControllerTest extends jmri.jmrix.ieee802154.IEEE802154TrafficControllerTest {

    @Test
    public void testCreateNode() {
        // test the code to get an new XBee 
        XBeeNode node = (XBeeNode) ((XBeeTrafficController)tc).newNode();
        assertNotNull( node, "node create failed");
    }

    @Test
    @Override
    public void testGetIEEE802154Messge() {
        assertNull( ((XBeeTrafficController)tc).getIEEE802154Message(5), "IEEE802154Message");
    }

    @Test
    @Override
    public void testGetPollReplyHandler() {
        assertNull( ((XBeeTrafficController)tc).pollReplyHandler(), "pollReplyHandler");
    }

    @Test
    public void testGetNewReply() {
        assertNotNull( ((XBeeTrafficController)tc).newReply(), "New Reply");
        assertInstanceOf( XBeeReply.class, ((XBeeTrafficController)tc).newReply(), "New Reply class");
    }

    @Test
    @Override
    public void checkPollMessageNoNodes() {
        // no nodes, should return null.
        assertNull( ((XBeeTrafficController)tc).pollMessage(), "pollMessage");
    }

    @Test
    @Override
    public void checkPollReplyHandler() {
        // always returns null.
        assertNull( ((XBeeTrafficController)tc).pollReplyHandler(), "pollReplyHandler");
    }

    @Test
    @Override
    public void checkEnterProgMode() {
        // No Programming Mode, returns null.
        assertNull( ((XBeeTrafficController)tc).enterProgMode(), "enterProgMode");
    }

    @Test
    @Override
    public void checkExitProgMode() {
        // No Programming Mode, returns null.
        assertNull( ((XBeeTrafficController)tc).enterNormalMode(), "enterNormalMode");
    }


    @Test
    public void registerNonXBeeNode(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            ((XBeeTrafficController)tc).registerNode(new jmri.jmrix.ieee802154.serialdriver.SerialNode()));
        assertNotNull(ex);
    }

    @Test
    @Override
    public void testGetNodeFromAddressTest() {
        // test the code to get an XBee node from its address
        // specified as a string to make sure it returns null on failure.
        XBeeNode node = (XBeeNode) ((XBeeTrafficController)tc).newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        ((XBeeTrafficController)tc).registerNode(node);
        XBeeNode n = (XBeeNode) ((XBeeTrafficController)tc).getNodeFromAddress("00 01");
        assertNull( n, "node found");
    }

    @Test
    @Override
    public void testGetNodeFromUserAddressIntTest() {
        // test the code to get an XBee node from its User address
        // specified as an integer array.
        XBeeNode node = (XBeeNode) ((XBeeTrafficController)tc).newNode();
        ((XBeeTrafficController)tc).registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        int iad[] = {0x6D, 0x97};
        node.setUserAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        XBeeNode n = (XBeeNode) ((XBeeTrafficController)tc).getNodeFromAddress(iad);
        assertNotNull( n, "node not found");
    }

    @Test
    @Override
    public void testGetNodeFromUserAddressByteTest() {
        // test the code to get an XBee node from its User address
        // specified as a byte array.
        XBeeNode node = (XBeeNode) ((XBeeTrafficController)tc).newNode();
        ((XBeeTrafficController)tc).registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        XBeeNode n = (XBeeNode) ((XBeeTrafficController)tc).getNodeFromAddress(uad);
        assertNotNull( n, "node not found");
    }

   @Test
    @Override
    public void testGetNodeFromUserAddressTest() {
        // test the code to get an XBee node from its User address
        // specified as a string.
        XBeeNode node = (XBeeNode) ((XBeeTrafficController)tc).newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        ((XBeeTrafficController)tc).registerNode(node);
        XBeeNode n = (XBeeNode) ((XBeeTrafficController)tc).getNodeFromAddress("6D 97");
        assertNotNull( n, "node not found");
    }

    @Test
    @Override
    public void testGetNodeFromAddressGlobalByteTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as a byte array.
        XBeeNode node = (XBeeNode) ((XBeeTrafficController)tc).newNode();
        ((XBeeTrafficController)tc).registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        ((XBeeTrafficController)tc).registerNode(node);
        XBeeNode n = (XBeeNode) ((XBeeTrafficController)tc).getNodeFromAddress(gad);
        assertNotNull( n, "node not found");
    }

    @Test
    @Override
    public void testGetNodeFromAddressGlobalIntTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as an intger array.
        XBeeNode node = (XBeeNode) ((XBeeTrafficController)tc).newNode();
        ((XBeeTrafficController)tc).registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        int iad[] = {0x00, 0x13, 0xA2, 0x00, 0x40, 0xA0, 0x4D, 0x2D};
        node.setGlobalAddress(gad);
        ((XBeeTrafficController)tc).registerNode(node);
        XBeeNode n = (XBeeNode) ((XBeeTrafficController)tc).getNodeFromAddress(iad);
        assertNotNull( n, "node not found");
    }

    @Test
    public void testGetNodeFromAddressGlobalTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as a string.
        XBeeNode node = (XBeeNode) ((XBeeTrafficController)tc).newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        ((XBeeTrafficController)tc).registerNode(node);
        XBeeNode n = (XBeeNode) ((XBeeTrafficController)tc).getNodeFromAddress("00 13 A2 00 40 A0 4D 2D");
        assertNotNull( n, "node not found");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new XBeeTrafficController();
    }

    @AfterEach
    @Override
    public void tearDown() {
        ((XBeeTrafficController)tc).terminate();
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();

    }

}
