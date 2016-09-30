package jmri.jmrix.ieee802154;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRListener;

/**
 * IEEE802154TrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.IEEE802154TrafficController
 * class
 *
 * @author	Paul Bender
 */
public class IEEE802154TrafficControllerTest{

    IEEE802154TrafficController m;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", m);
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

    @Test
    public void testGetNodeFromAddressTest() {
        // test the code to get an IEEE 802.15.4 node from its address
        // specified as a string to make sure it returns null on failure.
        IEEE802154Node node = (IEEE802154Node) m.newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        m.registerNode(node);
        IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress("00 01");
        Assert.assertNull("node found", n);
    }

    @Test
    public void testGetNodeFromUserAddressIntTest() {
        // test the code to get an IEEE 802.15.4 node from its User address
        // specified as an integer array.
        IEEE802154Node node = (IEEE802154Node) m.newNode();
        m.registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        int iad[] = {0x6D, 0x97};
        node.setUserAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress(iad);
        Assert.assertNotNull("node not found", n);
    }

    @Test
    public void testGetNodeFromUserAddressByteTest() {
        // test the code to get an IEEE802.15.4 node from its User address
        // specified as a byte array.
        IEEE802154Node node = (IEEE802154Node) m.newNode();
        m.registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress(uad);
        Assert.assertNotNull("node not found", n);
    }

   @Test
    public void testGetNodeFromUserAddressTest() {
        // test the code to get an IEEE802154 node from its User address
        // specified as a string.
        IEEE802154Node node = (IEEE802154Node) m.newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        m.registerNode(node);
        IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress("6D 97");
        Assert.assertNotNull("node not found", n);
    }

    @Test
    public void testGetNodeFromAddressGlobalByteTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as a byte array.
        IEEE802154Node node = (IEEE802154Node) m.newNode();
        m.registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        m.registerNode(node);
        IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress(gad);
        Assert.assertNotNull("node not found", n);
    }

   @Test
    public void testGetNodeFromAddressGlobalIntTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as an intger array.
        IEEE802154Node node = (IEEE802154Node) m.newNode();
        m.registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        int iad[] = {0x00, 0x13, 0xA2, 0x00, 0x40, 0xA0, 0x4D, 0x2D};
        node.setGlobalAddress(gad);
        m.registerNode(node);
        IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress(iad);
        Assert.assertNotNull("node not found", n);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        m = new IEEE802154TrafficController() {
            public void setInstance() {
            }
            protected AbstractMRReply newReply() {
                return null;
            }
            public IEEE802154Node newNode() {
                return new IEEE802154Node(){
                     @Override
                     public AbstractMRMessage createInitPacket(){
                         return null;
                     }
                     @Override
                     public AbstractMRMessage createOutPacket(){
                         return null;
                     }
                     @Override
                     public boolean getSensorsActive(){
                         return false;
                     }
                     @Override
                     public boolean handleTimeout(AbstractMRMessage m,AbstractMRListener l){
                         return false;
                     }
                     @Override
                     public void resetTimeout(AbstractMRMessage m){
                     }
                };
            }
        };
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
