package jmri.jmrix.ieee802154.serialdriver;

import jmri.jmrix.ieee802154.IEEE802154Node;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SerialTrafficControllerTest.java
 *
 * Description:	tests for the
 * jmri.jmrix.ieee802154.serialdriver.SerialTrafficController class
 *
 * @author	Paul Bender
 */
public class SerialTrafficControllerTest extends TestCase {

    SerialTrafficController m;

    public void testCtor() {
        Assert.assertNotNull("exists", m);
    }

    public void testCreateNode() {
        // test the code to get a new IEEE802154 node
        IEEE802154Node node = m.newNode();
        Assert.assertNotNull("node create failed", node);
    }

    public void testGetNodeFromAddressTest() {
        // test the code to get an IEEE802154 node from its address
        // specified as a string to make sure it returns null on failure.
        IEEE802154Node node = m.newNode();
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

    public void testGetNodeFromUserAddressIntTest() {
        // test the code to get an IEEE802154 node from its User address
        // specified as an integer array.
        IEEE802154Node node = m.newNode();
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

    public void testGetNodeFromUserAddressByteTest() {
        // test the code to get an IEEE802154 node from its User address
        // specified as a byte array.
        IEEE802154Node node = m.newNode();
        m.registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress(uad);
        Assert.assertNotNull("node not found", n);
    }

    public void testGetNodeFromUserAddressTest() {
        // test the code to get an IEEE802154 node from its User address
        // specified as a string.
        IEEE802154Node node = m.newNode();
        m.registerNode(node);
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

    public void testGetNodeFromAddressGlobalByteTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as a byte array.
        IEEE802154Node node = m.newNode();
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

    public void testGetNodeFromAddressGlobalIntTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as an integer array.
        IEEE802154Node node = m.newNode();
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

    public void testGetNodeFromAddressGlobalTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as a string.
        IEEE802154Node node = m.newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        m.registerNode(node);
        IEEE802154Node n = (IEEE802154Node) m.getNodeFromAddress("00 13 A2 00 40 A0 4D 2D");
        Assert.assertNotNull("node not found", n);
    }

    // from here down is testing infrastructure
    public SerialTrafficControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialTrafficControllerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialTrafficControllerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        m = new SerialTrafficController();
        jmri.util.JUnitAppender.assertErrorMessage("Deprecated Method setInstance called");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
