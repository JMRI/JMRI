// SerialNodeTest.java

package jmri.jmrix.powerline;

import jmri.Sensor;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialNode class
 * @author		Bob Jacobsen  Copyright 2003, 2007, 2008
 * @author		Dave Duchamp  multi-node extensions 2003
 * @version		$Revision: 1.2 $
 */
public class SerialNodeTest extends TestCase {
		
    private SerialNode a = new SerialNode(1,SerialNode.DAUGHTER);

    SerialNode b = new SerialNode();
       
    public void testConstructor1() {
        Assert.assertEquals("check default ctor type", SerialNode.DAUGHTER, b.getNodeType());
        Assert.assertEquals("check default ctor address", 0, b.getNodeAddress());
    }

    public void testConstructor2() {
        SerialNode c = new SerialNode(3,SerialNode.DAUGHTER);
        Assert.assertEquals("check ctor type", SerialNode.DAUGHTER, c.getNodeType());
        Assert.assertEquals("check ctor address", 3, c.getNodeAddress());
    }

    public void testAccessors() {
        SerialNode n = new SerialNode(2,SerialNode.DAUGHTER);
        n.setNodeAddress (7);
        Assert.assertEquals("check ctor type", SerialNode.DAUGHTER, n.getNodeType());
        Assert.assertEquals("check address", 7, n.getNodeAddress());
    }
    
    public void testInitialization1() {
        // no initialization in this protocol
        SerialMessage m = b.createInitPacket();
        Assert.assertEquals("initpacket null", null, m );
    }
	
    public void testMarkChanges() {
        SerialSensor s1 = new SerialSensor("PS1","a");
        Assert.assertEquals("check bit number",1,SerialAddress.getBitFromSystemName("PS1"));
        SerialSensor s2 = new SerialSensor("PS2","ab");
        SerialSensor s3 = new SerialSensor("PS3","abc");
        b.registerSensor(s1, 0);
        b.registerSensor(s2, 1);
        b.registerSensor(s3, 2);
        Assert.assertTrue("check sensors active", b.sensorsActive());
        SerialReply r = new jmri.jmrix.powerline.cm11.SpecificReply();
        r.setElement(0, 0x02);
        r.setElement(1, 0x00);
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
        String[] testCaseName = {SerialNodeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialNodeTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    
}
