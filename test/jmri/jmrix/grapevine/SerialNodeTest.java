// SerialNodeTest.java

package jmri.jmrix.grapevine;

import jmri.Sensor;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialNode class
 * @author		Bob Jacobsen  Copyright 2003, 2007
 * @author		Dave Duchamp  multi-node extensions 2003
 * @version		$Revision: 1.2 $
 */
public class SerialNodeTest extends TestCase {
		
    private SerialNode a = new SerialNode(1,SerialNode.NODE2002V6);

    SerialNode b = new SerialNode();
       
    public void testConstructor1() {
        Assert.assertEquals("check default ctor type", SerialNode.NODE2002V6, b.getNodeType());
        Assert.assertEquals("check default ctor address", 1, b.getNodeAddress());
    }

    public void testConstructor2() {
        SerialNode c = new SerialNode(3,SerialNode.NODE2002V1);
        Assert.assertEquals("check ctor type", SerialNode.NODE2002V1, c.getNodeType());
        Assert.assertEquals("check ctor address", 3, c.getNodeAddress());
    }

    public void testAccessors() {
        SerialNode n = new SerialNode(2,SerialNode.NODE2002V1);
        n.setNodeAddress (7);
        Assert.assertEquals("check ctor type", SerialNode.NODE2002V1, n.getNodeType());
        Assert.assertEquals("check address", 7, n.getNodeAddress());
    }
    
    public void testInitialization1() {
        // no initialization in this protocol
        SerialMessage m = b.createInitPacket();
        Assert.assertEquals("initpacket null", null, m );
    }

    public void testOutputBits1() {
        // mode with several output bits set
        SerialNode g = new SerialNode(5,SerialNode.NODE2002V6);
        Assert.assertTrue("must Send", g.mustSend() );
        g.resetMustSend();
        Assert.assertTrue("must Send off", !(g.mustSend()) );
        g.setOutputBit(2,false);
        g.setOutputBit(1,false);
        g.setOutputBit(3,false);
        g.setOutputBit(4,false);
        g.setOutputBit(5,false);
        g.setOutputBit(2,true);
        g.setOutputBit(9,false);
        g.setOutputBit(5,false);
        g.setOutputBit(11,false);
        g.setOutputBit(10,false);
        Assert.assertTrue("must Send on", g.mustSend() );
        SerialMessage m = g.createOutPacket();
        Assert.assertEquals("packet size", 4, m.getNumDataElements() );
        Assert.assertEquals("node address", 5, m.getElement(0) );
        Assert.assertEquals("packet type", 17, m.getElement(1) );  // 'T'        
    }
	
    public void testMarkChangesRealData1() {
        // new serial format
        
        b = new SerialNode(98,SerialNode.NODE2002V6);
        SerialSensor s1 = new SerialSensor("GS98101","s1");
        SerialSensor s2 = new SerialSensor("GS98102","s2");
        SerialSensor s3 = new SerialSensor("GS98103","s3");
        SerialSensor s4 = new SerialSensor("GS98104","s4");
        b.registerSensor(s1, 101);
        b.registerSensor(s2, 102);
        b.registerSensor(s3, 103);
        b.registerSensor(s4, 104);

        SerialReply r = new SerialReply();
        r.setElement(0, 128+98);
        r.setElement(1, 0x0E);
        r.setElement(2, 128+98);
        r.setElement(3, 0x56);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
        r.setElement(0, 128+98);
        r.setElement(1, 0x0F);
        r.setElement(2, 128+98);
        r.setElement(3, 0x54);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
    }

    public void testMarkChangesNewSerial1() {
        // new serial format
        
        b = new SerialNode(1,SerialNode.NODE2002V6);

        SerialSensor s1 = new SerialSensor("GS1001","s1");
        SerialSensor s2 = new SerialSensor("GS1002","s2");
        SerialSensor s3 = new SerialSensor("GS1003","s3");
        b.registerSensor(s1, 1);
        b.registerSensor(s2, 2);
        b.registerSensor(s3, 3);
        Assert.assertTrue("check sensors active", b.sensorsActive());
        Assert.assertEquals("check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.UNKNOWN, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());

        SerialReply r = new SerialReply();
        r.setElement(0, 0x81); // sensor 1 (from 0) active, GS1102
        r.setElement(1, 0x03);
        r.setElement(2, 0x81);
        r.setElement(3, 0x40);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());

        r.setElement(0, 0x81); // sensor 1 (from 0) inactive, GS1102
        r.setElement(1, 0x02);
        r.setElement(2, 0x81);
        r.setElement(3, 0x40);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());

        r.setElement(0, 0x81); // sensor 0 (from 0) active, GS1101
        r.setElement(1, 0x01);
        r.setElement(2, 0x81);
        r.setElement(3, 0x40);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());
    }

    public void testMarkChangesOldSerial1() {
        // old serial format
        
        b = new SerialNode(1,SerialNode.NODE2002V6);

        SerialSensor s1 = new SerialSensor("GS1001","s1");
        SerialSensor s2 = new SerialSensor("GS1002","s2");
        SerialSensor s3 = new SerialSensor("GS1003","s3");
        SerialSensor s4 = new SerialSensor("GS1004","s4");
        SerialSensor s5 = new SerialSensor("GS1005","s5");
        SerialSensor s6 = new SerialSensor("GS1006","s6");
        SerialSensor s7 = new SerialSensor("GS1007","s7");
        SerialSensor s8 = new SerialSensor("GS1008","s8");
        b.registerSensor(s1, 1);
        b.registerSensor(s2, 2);
        b.registerSensor(s3, 3);
        b.registerSensor(s4, 4);
        b.registerSensor(s5, 5);
        b.registerSensor(s6, 6);
        b.registerSensor(s7, 7);
        b.registerSensor(s8, 8);
        Assert.assertTrue("check sensors active", b.sensorsActive());
        Assert.assertEquals("1 check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("1 check s2", Sensor.UNKNOWN, s2.getKnownState());
        Assert.assertEquals("1 check s3", Sensor.UNKNOWN, s3.getKnownState());
        Assert.assertEquals("1 check s4", Sensor.UNKNOWN, s4.getKnownState());
        Assert.assertEquals("1 check s5", Sensor.UNKNOWN, s5.getKnownState());
        Assert.assertEquals("1 check s6", Sensor.UNKNOWN, s6.getKnownState());
        Assert.assertEquals("1 check s7", Sensor.UNKNOWN, s7.getKnownState());
        Assert.assertEquals("1 check s8", Sensor.UNKNOWN, s8.getKnownState());

        SerialReply r = new SerialReply();
        r.setElement(0, 0x81); // sensor 1-4 (from 0) inactive, GS1001-GS1004
        r.setElement(1, 0x6F);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);
        Assert.assertEquals("2 check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("2 check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("2 check s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("2 check s4", Sensor.INACTIVE, s4.getKnownState());
        Assert.assertEquals("2 check s5", Sensor.UNKNOWN, s5.getKnownState());
        Assert.assertEquals("2 check s6", Sensor.UNKNOWN, s6.getKnownState());
        Assert.assertEquals("2 check s7", Sensor.UNKNOWN, s7.getKnownState());
        Assert.assertEquals("2 check s8", Sensor.UNKNOWN, s8.getKnownState());

        r.setElement(0, 0x81); // sensor 1-4 (from 0) inactive
        r.setElement(1, 0x60);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);
        Assert.assertEquals("3 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("3 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("3 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("3 check s4", Sensor.ACTIVE, s4.getKnownState());
        Assert.assertEquals("3 check s5", Sensor.UNKNOWN, s5.getKnownState());
        Assert.assertEquals("3 check s6", Sensor.UNKNOWN, s6.getKnownState());
        Assert.assertEquals("3 check s7", Sensor.UNKNOWN, s7.getKnownState());
        Assert.assertEquals("3 check s8", Sensor.UNKNOWN, s8.getKnownState());

        r.setElement(0, 0x81); // sensor 5-8 (from 0) mixed
        r.setElement(1, 0x75);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);
        Assert.assertEquals("4 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("4 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("4 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("4 check s4", Sensor.ACTIVE, s4.getKnownState());
        Assert.assertEquals("4 check s5", Sensor.INACTIVE, s5.getKnownState());
        Assert.assertEquals("4 check s6", Sensor.ACTIVE, s6.getKnownState());
        Assert.assertEquals("4 check s7", Sensor.INACTIVE, s7.getKnownState());
        Assert.assertEquals("4 check s8", Sensor.ACTIVE, s8.getKnownState());
    }

    public void testMarkChangesParallel() {

        b = new SerialNode(1,SerialNode.NODE2002V6);

        SerialSensor s1 = new SerialSensor("GS1101","s1");
        SerialSensor s2 = new SerialSensor("GS1102","s2");
        SerialSensor s3 = new SerialSensor("GS1103","s3");
        SerialSensor s4 = new SerialSensor("GS1104","s4");
        SerialSensor s5 = new SerialSensor("GS1105","s5");
        SerialSensor s6 = new SerialSensor("GS1106","s6");
        SerialSensor s7 = new SerialSensor("GS1107","s7");
        SerialSensor s8 = new SerialSensor("GS1108","s8");
        b.registerSensor(s1, 101);
        b.registerSensor(s2, 102);
        b.registerSensor(s3, 103);
        b.registerSensor(s4, 104);
        b.registerSensor(s5, 105);
        b.registerSensor(s6, 106);
        b.registerSensor(s7, 107);
        b.registerSensor(s8, 108);
        Assert.assertTrue("check sensors active", b.sensorsActive());
        Assert.assertEquals("1 check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("1 check s2", Sensor.UNKNOWN, s2.getKnownState());
        Assert.assertEquals("1 check s3", Sensor.UNKNOWN, s3.getKnownState());
        Assert.assertEquals("1 check s4", Sensor.UNKNOWN, s4.getKnownState());
        Assert.assertEquals("1 check s5", Sensor.UNKNOWN, s5.getKnownState());
        Assert.assertEquals("1 check s6", Sensor.UNKNOWN, s6.getKnownState());
        Assert.assertEquals("1 check s7", Sensor.UNKNOWN, s7.getKnownState());
        Assert.assertEquals("1 check s8", Sensor.UNKNOWN, s8.getKnownState());

        SerialReply r = new SerialReply();
        r.setElement(0, 0x81); // sensor 1-4 (from 0) inactive, GS1001-GS1004
        r.setElement(1, 0x4F);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);
        Assert.assertEquals("2 check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("2 check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("2 check s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("2 check s4", Sensor.INACTIVE, s4.getKnownState());
        Assert.assertEquals("2 check s5", Sensor.UNKNOWN, s5.getKnownState());
        Assert.assertEquals("2 check s6", Sensor.UNKNOWN, s6.getKnownState());
        Assert.assertEquals("2 check s7", Sensor.UNKNOWN, s7.getKnownState());
        Assert.assertEquals("2 check s8", Sensor.UNKNOWN, s8.getKnownState());

        r.setElement(0, 0x81); // sensor 1-4 (from 0) inactive
        r.setElement(1, 0x40);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);
        Assert.assertEquals("3 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("3 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("3 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("3 check s4", Sensor.ACTIVE, s4.getKnownState());
        Assert.assertEquals("3 check s5", Sensor.UNKNOWN, s5.getKnownState());
        Assert.assertEquals("3 check s6", Sensor.UNKNOWN, s6.getKnownState());
        Assert.assertEquals("3 check s7", Sensor.UNKNOWN, s7.getKnownState());
        Assert.assertEquals("3 check s8", Sensor.UNKNOWN, s8.getKnownState());

        r.setElement(0, 0x81); // sensor 5-8 (from 0) mixed
        r.setElement(1, 0x55);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);
        Assert.assertEquals("4 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("4 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("4 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("4 check s4", Sensor.ACTIVE, s4.getKnownState());
        Assert.assertEquals("4 check s5", Sensor.INACTIVE, s5.getKnownState());
        Assert.assertEquals("4 check s6", Sensor.ACTIVE, s6.getKnownState());
        Assert.assertEquals("4 check s7", Sensor.INACTIVE, s7.getKnownState());
        Assert.assertEquals("4 check s8", Sensor.ACTIVE, s8.getKnownState());
    }

    // from here down is testing infrastructure
    public SerialNodeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialNodeTest.class.getName()};
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
