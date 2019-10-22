package jmri.jmrix.ieee802154.serialdriver;

import jmri.jmrix.ieee802154.IEEE802154Node;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SerialTrafficControllerTest.java
 *
 * Description:	tests for the
 * jmri.jmrix.ieee802154.serialdriver.SerialTrafficController class
 *
 * @author	Paul Bender
 */
public class SerialTrafficControllerTest extends jmri.jmrix.ieee802154.IEEE802154TrafficControllerTest {

    @Override
    @Test
    public void testGetIEEE802154Messge() {
        Assert.assertNotNull("IEEE802154Message", ((SerialTrafficController)tc).getIEEE802154Message(5));
    }

    @Test
    public void testCreateNode() {
        // test the code to get a new IEEE802154 node
        IEEE802154Node node = ((SerialTrafficController)tc).newNode();
        Assert.assertNotNull("node create failed", node);
    }

    @Test
    @Override
    public void testGetNodeFromAddressTest() {
        // test the code to get an IEEE802154 node from its address
        // specified as a string to make sure it returns null on failure.
        IEEE802154Node node = ((SerialTrafficController)tc).newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        ((SerialTrafficController)tc).registerNode(node);
        IEEE802154Node n = (IEEE802154Node) ((SerialTrafficController)tc).getNodeFromAddress("00 01");
        Assert.assertNull("node found", n);
    }

    @Test
    @Override
    public void testGetNodeFromUserAddressIntTest() {
        // test the code to get an IEEE802154 node from its User address
        // specified as an integer array.
        IEEE802154Node node = ((SerialTrafficController)tc).newNode();
        ((SerialTrafficController)tc).registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        int iad[] = {0x6D, 0x97};
        node.setUserAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        IEEE802154Node n = (IEEE802154Node) ((SerialTrafficController)tc).getNodeFromAddress(iad);
        Assert.assertNotNull("node not found", n);
    }

    @Test
    @Override
    public void testGetNodeFromUserAddressByteTest() {
        // test the code to get an IEEE802154 node from its User address
        // specified as a byte array.
        IEEE802154Node node = ((SerialTrafficController)tc).newNode();
        ((SerialTrafficController)tc).registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        IEEE802154Node n = (IEEE802154Node) ((SerialTrafficController)tc).getNodeFromAddress(uad);
        Assert.assertNotNull("node not found", n);
    }

    @Test
    @Override
    public void testGetNodeFromUserAddressTest() {
        // test the code to get an IEEE802154 node from its User address
        // specified as a string.
        IEEE802154Node node = ((SerialTrafficController)tc).newNode();
        ((SerialTrafficController)tc).registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        ((SerialTrafficController)tc).registerNode(node);
        IEEE802154Node n = (IEEE802154Node) ((SerialTrafficController)tc).getNodeFromAddress("6D 97");
        Assert.assertNotNull("node not found", n);
    }

    @Test
    @Override
    public void testGetNodeFromAddressGlobalByteTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as a byte array.
        IEEE802154Node node = ((SerialTrafficController)tc).newNode();
        ((SerialTrafficController)tc).registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        ((SerialTrafficController)tc).registerNode(node);
        IEEE802154Node n = (IEEE802154Node) ((SerialTrafficController)tc).getNodeFromAddress(gad);
        Assert.assertNotNull("node not found", n);
    }

    @Test
    @Override
    public void testGetNodeFromAddressGlobalIntTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as an integer array.
        IEEE802154Node node = ((SerialTrafficController)tc).newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        int iad[] = {0x00, 0x13, 0xA2, 0x00, 0x40, 0xA0, 0x4D, 0x2D};
        node.setGlobalAddress(gad);
        ((SerialTrafficController)tc).registerNode(node);
        IEEE802154Node n = (IEEE802154Node) ((SerialTrafficController)tc).getNodeFromAddress(iad);
        Assert.assertNotNull("node not found", n);
    }

    @Test
    public void testGetNodeFromAddressGlobalTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as a string.
        IEEE802154Node node = ((SerialTrafficController)tc).newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        ((SerialTrafficController)tc).registerNode(node);
        IEEE802154Node n = (IEEE802154Node) ((SerialTrafficController)tc).getNodeFromAddress("00 13 A2 00 40 A0 4D 2D");
        Assert.assertNotNull("node not found", n);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new SerialTrafficController();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
