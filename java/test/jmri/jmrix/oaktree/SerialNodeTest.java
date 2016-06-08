package jmri.jmrix.oaktree;

import jmri.Sensor;
import jmri.jmrix.AbstractMRMessage;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialNode class
 *
 * @author	Bob Jacobsen Copyright 2003
 * @author	Dave Duchamp multi-node extensions 2003
 * @version	$Revision$
 */
public class SerialNodeTest extends TestCase {

    //private SerialNode a = new SerialNode(1,SerialNode.IO48);
    SerialNode b = new SerialNode();

    public void testConstructor1() {
        Assert.assertEquals("check default ctor type", SerialNode.IO24, b.getNodeType());
        Assert.assertEquals("check default ctor address", 0, b.getNodeAddress());
    }

    public void testConstructor2() {
        SerialNode c = new SerialNode(3, SerialNode.IO24);
        Assert.assertEquals("check ctor type", SerialNode.IO24, c.getNodeType());
        Assert.assertEquals("check ctor address", 3, c.getNodeAddress());
    }

    public void testAccessors() {
        SerialNode n = new SerialNode(2, SerialNode.IO24);
        n.setNodeAddress(7);
        Assert.assertEquals("check ctor type", SerialNode.IO24, n.getNodeType());
        Assert.assertEquals("check address", 7, n.getNodeAddress());
    }

    public void testInitialization1() {
        // no initialization in this protocol
        AbstractMRMessage m = b.createInitPacket();
        Assert.assertEquals("initpacket null", null, m);
    }

    public void testOutputBits1() {
        // IO48 with several output bits set
        SerialNode g = new SerialNode(5, SerialNode.IO48);
        Assert.assertTrue("must Send", g.mustSend());
        g.resetMustSend();
        Assert.assertTrue("must Send off", !(g.mustSend()));
        g.setOutputBit(2, false);
        g.setOutputBit(1, false);
        g.setOutputBit(23, false);
        g.setOutputBit(21, false);
        g.setOutputBit(31, false);
        g.setOutputBit(2, true);
        g.setOutputBit(19, false);
        g.setOutputBit(5, false);
        g.setOutputBit(26, false);
        g.setOutputBit(28, false);
        Assert.assertTrue("must Send on", g.mustSend());
        AbstractMRMessage m = g.createOutPacket();
        Assert.assertEquals("packet size", 5, m.getNumDataElements());
        Assert.assertEquals("node address", 5, m.getElement(0));
        Assert.assertEquals("packet type", 17, m.getElement(1));  // 'T'        
    }

    public void testMarkChanges() {
        SerialSensor s1 = new SerialSensor("OS1", "a");
        Assert.assertEquals("check bit number", 1, SerialAddress.getBitFromSystemName("OS1"));
        SerialSensor s2 = new SerialSensor("OS2", "ab");
        SerialSensor s3 = new SerialSensor("OS3", "abc");
        b.registerSensor(s1, 0);
        b.registerSensor(s2, 1);
        b.registerSensor(s3, 2);
        Assert.assertTrue("check sensors active", b.getSensorsActive());
        SerialReply r = new SerialReply();
        r.setElement(2, '2');
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
    }

    // from here down is testing infrastructure
    public SerialNodeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialNodeTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialNodeTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
