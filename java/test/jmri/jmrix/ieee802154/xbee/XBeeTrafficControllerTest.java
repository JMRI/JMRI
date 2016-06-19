package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * XBeeTrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeTrafficController
 * class
 *
 * @author	Paul Bender
 */
public class XBeeTrafficControllerTest{

    XBeeTrafficController m;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", m);
    }

    @Test
    public void testCreateNode() {
        // test the code to get an new XBee 
        XBeeNode node = (XBeeNode) m.newNode();
        Assert.assertNotNull("node create failed", node);
    }

    @Test
    public void testGetIEEE802154Messge() {
        Assert.assertNull("IEEE802154Message", m.getIEEE802154Message(5));
    }

    @Test
    public void testGetPollReplyHandler() {
        Assert.assertNull("pollReplyHandler", m.pollReplyHandler());
    }

    @Test
    public void testGetNewReply() {
        Assert.assertNotNull("New Reply", m.newReply());
        Assert.assertTrue("New Reply class", m.newReply() instanceof jmri.jmrix.ieee802154.xbee.XBeeReply );
    }

    @Test
    public void checkPollMessageNoNodes() {
        // no nodes, should return null.
        Assert.assertNull("pollMessage", m.pollMessage());
    }

    @Test
    public void checkPollReplyHandler() {
        // always returns null.
        Assert.assertNull("pollReplyHandler", m.pollReplyHandler());
    }

    @Test
    public void checkEnterProgMode() {
        // No Programming Mode, returns null.
        Assert.assertNull("enterProgMode", m.enterProgMode());
    }

    @Test
    public void checkExitProgMode() {
        // No Programming Mode, returns null.
        Assert.assertNull("enterNormalMode", m.enterNormalMode());
    }


    @Test(expected=java.lang.IllegalArgumentException.class)
    public void registerNonXBeeNode(){
        m.registerNode(new jmri.jmrix.ieee802154.serialdriver.SerialNode());
    }

    @Test
    public void testGetNodeFromAddressTest() {
        // test the code to get an XBee node from its address
        // specified as a string to make sure it returns null on failure.
        XBeeNode node = (XBeeNode) m.newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        m.registerNode(node);
        XBeeNode n = (XBeeNode) m.getNodeFromAddress("00 01");
        Assert.assertNull("node found", n);
    }

    @Test
    public void testGetNodeFromUserAddressIntTest() {
        // test the code to get an XBee node from its User address
        // specified as an integer array.
        XBeeNode node = (XBeeNode) m.newNode();
        m.registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        int iad[] = {0x6D, 0x97};
        node.setUserAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        XBeeNode n = (XBeeNode) m.getNodeFromAddress(iad);
        Assert.assertNotNull("node not found", n);
    }

    @Test
    public void testGetNodeFromUserAddressByteTest() {
        // test the code to get an XBee node from its User address
        // specified as a byte array.
        XBeeNode node = (XBeeNode) m.newNode();
        m.registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        XBeeNode n = (XBeeNode) m.getNodeFromAddress(uad);
        Assert.assertNotNull("node not found", n);
    }

    @Test
    public void testGetNodeFromUserAddressTest() {
        // test the code to get an XBee node from its User address
        // specified as a string.
        XBeeNode node = (XBeeNode) m.newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        m.registerNode(node);
        XBeeNode n = (XBeeNode) m.getNodeFromAddress("6D 97");
        Assert.assertNotNull("node not found", n);
    }

    @Test
    public void testGetNodeFromAddressGlobalByteTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as a byte array.
        XBeeNode node = (XBeeNode) m.newNode();
        m.registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        m.registerNode(node);
        XBeeNode n = (XBeeNode) m.getNodeFromAddress(gad);
        Assert.assertNotNull("node not found", n);
    }

    @Test
    public void testGetNodeFromAddressGlobalIntTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as an intger array.
        XBeeNode node = (XBeeNode) m.newNode();
        m.registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        int iad[] = {0x00, 0x13, 0xA2, 0x00, 0x40, 0xA0, 0x4D, 0x2D};
        node.setGlobalAddress(gad);
        m.registerNode(node);
        XBeeNode n = (XBeeNode) m.getNodeFromAddress(iad);
        Assert.assertNotNull("node not found", n);
    }

    @Test
    public void testGetNodeFromAddressGlobalTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as a string.
        XBeeNode node = (XBeeNode) m.newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        m.registerNode(node);
        XBeeNode n = (XBeeNode) m.getNodeFromAddress("00 13 A2 00 40 A0 4D 2D");
        Assert.assertNotNull("node not found", n);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        m = new XBeeTrafficController() {
            public void setInstance() {
            }
        };
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
