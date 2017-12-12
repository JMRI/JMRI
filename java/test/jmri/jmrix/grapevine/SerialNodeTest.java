package jmri.jmrix.grapevine;

import jmri.Sensor;
import jmri.jmrix.AbstractMRMessage;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * JUnit tests for the SerialNode class
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008
 * @author	Dave Duchamp multi-node extensions 2003
 */
@SuppressWarnings("null")
public class SerialNodeTest extends TestCase {

    private GrapevineSystemConnectionMemo memo = null; 

    public void testConstructor1() {
        SerialNode b = new SerialNode();
        Assert.assertEquals("check default ctor type", SerialNode.NODE2002V6, b.getNodeType());
        Assert.assertEquals("check default ctor address", 1, b.getNodeAddress());
    }

    public void testConstructor2() {
        SerialNode c = new SerialNode(3, SerialNode.NODE2002V1);
        Assert.assertEquals("check ctor type", SerialNode.NODE2002V1, c.getNodeType());
        Assert.assertEquals("check ctor address", 3, c.getNodeAddress());
    }

    public void testAccessors() {
        SerialNode n = new SerialNode(2, SerialNode.NODE2002V1);
        n.setNodeAddress(7);
        Assert.assertEquals("check ctor type", SerialNode.NODE2002V1, n.getNodeType());
        Assert.assertEquals("check address", 7, n.getNodeAddress());
    }

    public void testInitialization1() {
        // comment these out, because they cause a later timeout (since
        // the init message is actually queued in the createInitPacket() method)

        // SerialMessage m = b.createInitPacket();
        // Assert.assertEquals("initpacket", "81 71 81 0F", m.toString() );
    }

    public void testOutputBits1() {
        // mode with several output bits set
        SerialNode g = new SerialNode(5, SerialNode.NODE2002V6);
        Assert.assertTrue("must Send", g.mustSend());
        g.resetMustSend();
        Assert.assertTrue("must Send off", !(g.mustSend()));
        g.setOutputBit(2, false);
        g.setOutputBit(1, false);
        g.setOutputBit(3, false);
        g.setOutputBit(4, false);
        g.setOutputBit(5, false);
        g.setOutputBit(2, true);
        g.setOutputBit(9, false);
        g.setOutputBit(5, false);
        g.setOutputBit(11, false);
        g.setOutputBit(10, false);
        Assert.assertTrue("must Send on", g.mustSend());
        AbstractMRMessage m = g.createOutPacket();
        Assert.assertEquals("packet size", 4, m.getNumDataElements());
        Assert.assertEquals("node address", 5, m.getElement(0));
        Assert.assertEquals("packet type", 17, m.getElement(1));  // 'T'        
    }

    public void testMarkChangesRealData1() {
        // parallel format

        SerialNode b = new SerialNode(98, SerialNode.NODE2002V6);

        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();
        Sensor s1 = sm.provideSensor("GS98001");
        Sensor s2 = sm.provideSensor("GS98002");
        Sensor s3 = sm.provideSensor("GS98003");
        Sensor s4 = sm.provideSensor("GS98004");

        SerialReply r = new SerialReply();
        r.setElement(0, 128 + 98);
        r.setElement(1, 0x0E);
        r.setElement(2, 128 + 98);
        r.setElement(3, 0x56);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertNotNull("exists", s4);
        r.setElement(0, 128 + 98);
        r.setElement(1, 0x0F);
        r.setElement(2, 128 + 98);
        r.setElement(3, 0x54);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
    }

    public void testMarkChangesRealData1Alt() {
        // parallel format

        SerialNode b = new SerialNode(96, SerialNode.NODE2002V6);

        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();
        Sensor s1 = sm.provideSensor("GS96p1");
        Sensor s2 = sm.provideSensor("GS96p2");
        Sensor s3 = sm.provideSensor("GS96p3");
        Sensor s4 = sm.provideSensor("GS96p4");

        SerialReply r = new SerialReply();
        r.setElement(0, 128 + 96);
        r.setElement(1, 0x0E);
        r.setElement(2, 128 + 96);
        r.setElement(3, 0x56);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertNotNull("exists", s4);
        r.setElement(0, 128 + 98);
        r.setElement(1, 0x0F);
        r.setElement(2, 128 + 98);
        r.setElement(3, 0x54);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
    }

    public void testMarkChangesSerialSlave1() {
        // advanced serial format, 1st slave card, 
        // sensors 1109 to 1116 adn 1209 to 1216

        SerialNode b = new SerialNode(1, SerialNode.NODE2002V6);

        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();
        Sensor s1 = sm.provideSensor("GS1109");
        Sensor s2 = sm.provideSensor("GS1110");
        Sensor s3 = sm.provideSensor("GS1111");

        //Assert.assertTrue("check sensors active", b.getSensorsActive());
        Assert.assertEquals("check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.UNKNOWN, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());

        SerialReply r = new SerialReply();
        r.setElement(0, 0x81); // sensor 1 (from 0) active, GS1110
        r.setElement(1, 0x22);
        r.setElement(2, 0x81);
        r.setElement(3, 0x40);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());

        r.setElement(0, 0x81); // sensor 1 (from 0) inactive, GS1110
        r.setElement(1, 0x23);
        r.setElement(2, 0x81);
        r.setElement(3, 0x40);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());

        r.setElement(0, 0x81); // sensor 0 (from 0) active, GS1109
        r.setElement(1, 0x20);
        r.setElement(2, 0x81);
        r.setElement(3, 0x40);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());
    }

    public void testMarkChangesNewSerial1() {
        // advanced serial format

        SerialNode b = new SerialNode(1, SerialNode.NODE2002V6);

        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();
        Sensor s1 = sm.provideSensor("GS1101");
        Sensor s2 = sm.provideSensor("GS1102");
        Sensor s3 = sm.provideSensor("GS1103");

        //Assert.assertTrue("check sensors active", b.getSensorsActive());
        Assert.assertEquals("check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.UNKNOWN, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());

        SerialReply r = new SerialReply();
        r.setElement(0, 0x81); // sensor 1 (from 0) active, GS1102
        r.setElement(1, 0x02);
        r.setElement(2, 0x81);
        r.setElement(3, 0x40);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());

        r.setElement(0, 0x81); // sensor 1 (from 0) inactive, GS1102
        r.setElement(1, 0x03);
        r.setElement(2, 0x81);
        r.setElement(3, 0x40);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());

        r.setElement(0, 0x81); // sensor 0 (from 0) active, GS1101
        r.setElement(1, 0x00);
        r.setElement(2, 0x81);
        r.setElement(3, 0x40);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());
    }

    public void testMarkChangesNewSerial1Alt() {
        // advanced serial format

        SerialNode b = new SerialNode(1, SerialNode.NODE2002V6);

        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();
        Sensor s1 = sm.provideSensor("GS1a1");
        Sensor s2 = sm.provideSensor("GS1a2");
        Sensor s3 = sm.provideSensor("GS1a3");

        //Assert.assertTrue("check sensors active", b.getSensorsActive());
        Assert.assertEquals("check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.UNKNOWN, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());

        SerialReply r = new SerialReply();
        r.setElement(0, 0x81); // sensor 1 (from 0) active, GS1102
        r.setElement(1, 0x02);
        r.setElement(2, 0x81);
        r.setElement(3, 0x40);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());

        r.setElement(0, 0x81); // sensor 1 (from 0) inactive, GS1102
        r.setElement(1, 0x03);
        r.setElement(2, 0x81);
        r.setElement(3, 0x40);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());

        r.setElement(0, 0x81); // sensor 0 (from 0) active, GS1101
        r.setElement(1, 0x00);
        r.setElement(2, 0x81);
        r.setElement(3, 0x40);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.UNKNOWN, s3.getKnownState());
    }

    public void testMarkChangesOldSerial1() {
        // old serial format

        SerialNode b = new SerialNode(1, SerialNode.NODE2002V6);

        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();
        Sensor s1 = sm.provideSensor("GS1021");
        Sensor s2 = sm.provideSensor("GS1022");
        Sensor s3 = sm.provideSensor("GS1023");
        Sensor s4 = sm.provideSensor("GS1024");
        Sensor s5 = sm.provideSensor("GS1025");
        Sensor s6 = sm.provideSensor("GS1026");
        Sensor s7 = sm.provideSensor("GS1027");
        Sensor s8 = sm.provideSensor("GS1028");

        Assert.assertTrue("check sensors active", b.getSensorsActive());
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
        r.setElement(1, 0x2F);
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
        r.setElement(1, 0x20);
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
        r.setElement(1, 0x35);
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

    public void testMarkChangesOldSerial1Alt() {
        // old serial format

        SerialNode b = new SerialNode(1, SerialNode.NODE2002V6);

        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();
        Sensor s1 = sm.provideSensor("GS1s1");
        Sensor s2 = sm.provideSensor("GS1s2");
        Sensor s3 = sm.provideSensor("GS1s3");
        Sensor s4 = sm.provideSensor("GS1s4");
        Sensor s5 = sm.provideSensor("GS1s5");
        Sensor s6 = sm.provideSensor("GS1s6");
        Sensor s7 = sm.provideSensor("GS1s7");
        Sensor s8 = sm.provideSensor("GS1s8");

        Assert.assertTrue("check sensors active", b.getSensorsActive());
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
        r.setElement(1, 0x2F);
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
        r.setElement(1, 0x20);
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
        r.setElement(1, 0x35);
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

    public void testMarkChangesParallelLowBankLowNibble() {
        // test with them not created
        SerialNode b = new SerialNode(1, SerialNode.NODE2002V6);

        Sensor s1, s2, s3, s4, s5, s6, s7, s8;
        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();

        // send message, which should create
        SerialReply r = new SerialReply();
        r.setElement(0, 0x81); // sensor 1-4 (from 0) inactive, GS1001-GS1004
        r.setElement(1, 0x0F);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);

        // created first four only
        s1 = sm.getSensor("GS1001");
        Assert.assertTrue("s1 exists", s1 != null);
        s2 = sm.getSensor("GS1002");
        Assert.assertTrue("s2 exists", s2 != null);
        s3 = sm.getSensor("GS1003");
        Assert.assertTrue("s3 exists", s3 != null);
        s4 = sm.getSensor("GS1004");
        Assert.assertTrue("s4 exists", s4 != null);
        s5 = sm.getSensor("GS1005");
        Assert.assertTrue("s5 not exist", s5 == null);
        s6 = sm.getSensor("GS1006");
        Assert.assertTrue("s6 not exist", s6 == null);
        s7 = sm.getSensor("GS1007");
        Assert.assertTrue("s7 not exist", s7 == null);
        s8 = sm.getSensor("GS1008");
        Assert.assertTrue("s8 not exist", s8 == null);

        Assert.assertEquals("2 check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("2 check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("2 check s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("2 check s4", Sensor.INACTIVE, s4.getKnownState());

        r.setElement(0, 0x81); // sensor 1-4 (from 0) inactive
        r.setElement(1, 0x00);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);
        // create first four only
        s5 = sm.getSensor("GS1005");
        Assert.assertTrue("s5 not exist", s5 == null);
        s6 = sm.getSensor("GS1006");
        Assert.assertTrue("s6 not exist", s6 == null);
        s7 = sm.getSensor("GS1007");
        Assert.assertTrue("s7 not exist", s7 == null);
        s8 = sm.getSensor("GS1008");
        Assert.assertTrue("s8 not exist", s8 == null);

        Assert.assertEquals("3 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("3 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("3 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("3 check s4", Sensor.ACTIVE, s4.getKnownState());

        r.setElement(0, 0x81); // sensor 5-8 (from 0) mixed
        r.setElement(1, 0x15);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);

        // create next four
        s5 = sm.getSensor("GS1005");
        Assert.assertTrue("s5 exists", s5 != null);
        s6 = sm.getSensor("GS1006");
        Assert.assertTrue("s6 exists", s6 != null);
        s7 = sm.getSensor("GS1007");
        Assert.assertTrue("s7 exists", s7 != null);
        s8 = sm.getSensor("GS1008");
        Assert.assertTrue("s8 exists", s8 != null);

        Assert.assertEquals("4 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("4 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("4 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("4 check s4", Sensor.ACTIVE, s4.getKnownState());
        Assert.assertEquals("4 check s5", Sensor.INACTIVE, s5.getKnownState());
        Assert.assertEquals("4 check s6", Sensor.ACTIVE, s6.getKnownState());
        Assert.assertEquals("4 check s7", Sensor.INACTIVE, s7.getKnownState());
        Assert.assertEquals("4 check s8", Sensor.ACTIVE, s8.getKnownState());
    }

    public void testMarkChangesParallelLowBankLowNibbleAlt() {
        // test with them not created
        SerialNode b = new SerialNode(1, SerialNode.NODE2002V6);

        Sensor s1, s2, s3, s4, s5, s6, s7, s8;
        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();

        // send message, which should create
        SerialReply r = new SerialReply();
        r.setElement(0, 0x81); // sensor 1-4 (from 0) inactive, GS1001-GS1004
        r.setElement(1, 0x0F);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);

        // created first four only
        s1 = sm.getSensor("GS1p1");
        Assert.assertTrue("s1 exists", s1 != null);
        s2 = sm.getSensor("GS1p2");
        Assert.assertTrue("s2 exists", s2 != null);
        s3 = sm.getSensor("GS1p3");
        Assert.assertTrue("s3 exists", s3 != null);
        s4 = sm.getSensor("GS1p4");
        Assert.assertTrue("s4 exists", s4 != null);
        s5 = sm.getSensor("GS1p5");
        Assert.assertTrue("s5 not exist", s5 == null);
        s6 = sm.getSensor("GS1p6");
        Assert.assertTrue("s6 not exist", s6 == null);
        s7 = sm.getSensor("GS1p7");
        Assert.assertTrue("s7 not exist", s7 == null);
        s8 = sm.getSensor("GS1p8");
        Assert.assertTrue("s8 not exist", s8 == null);

        Assert.assertEquals("2 check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("2 check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("2 check s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("2 check s4", Sensor.INACTIVE, s4.getKnownState());

        r.setElement(0, 0x81); // sensor 1-4 (from 0) inactive
        r.setElement(1, 0x00);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);
        // create first four only
        s5 = sm.getSensor("GS1p5");
        Assert.assertTrue("s5 not exist", s5 == null);
        s6 = sm.getSensor("GS1p6");
        Assert.assertTrue("s6 not exist", s6 == null);
        s7 = sm.getSensor("GS1p7");
        Assert.assertTrue("s7 not exist", s7 == null);
        s8 = sm.getSensor("GS1p8");
        Assert.assertTrue("s8 not exist", s8 == null);

        Assert.assertEquals("3 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("3 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("3 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("3 check s4", Sensor.ACTIVE, s4.getKnownState());

        r.setElement(0, 0x81); // sensor 5-8 (from 0) mixed
        r.setElement(1, 0x15);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);

        // create next four
        s5 = sm.getSensor("GS1005");
        Assert.assertTrue("s5 exists", s5 != null);
        s6 = sm.getSensor("GS1006");
        Assert.assertTrue("s6 exists", s6 != null);
        s7 = sm.getSensor("GS1007");
        Assert.assertTrue("s7 exists", s7 != null);
        s8 = sm.getSensor("GS1008");
        Assert.assertTrue("s8 exists", s8 != null);

        Assert.assertEquals("4 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("4 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("4 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("4 check s4", Sensor.ACTIVE, s4.getKnownState());
        Assert.assertEquals("4 check s5", Sensor.INACTIVE, s5.getKnownState());
        Assert.assertEquals("4 check s6", Sensor.ACTIVE, s6.getKnownState());
        Assert.assertEquals("4 check s7", Sensor.INACTIVE, s7.getKnownState());
        Assert.assertEquals("4 check s8", Sensor.ACTIVE, s8.getKnownState());
    }

    public void testMarkChangesParallelLowBankHighNibble() {
        // test with them not created
        SerialNode b = new SerialNode(1, SerialNode.NODE2002V6);

        Sensor s1, s2, s3, s4, s5, s6, s7, s8;
        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();

        // send message, which should create
        SerialReply r = new SerialReply();
        r.setElement(0, 0x81); // sensor 5-8 (from 1) inactive, GS1005-GS1008
        r.setElement(1, 0x1F);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);

        // create correct nibble only
        s1 = sm.getSensor("GS1005");
        Assert.assertTrue("s1 exists", s1 != null);
        s2 = sm.getSensor("GS1006");
        Assert.assertTrue("s2 exists", s2 != null);
        s3 = sm.getSensor("GS1007");
        Assert.assertTrue("s3 exists", s3 != null);
        s4 = sm.getSensor("GS1008");
        Assert.assertTrue("s4 exists", s4 != null);
        s5 = sm.getSensor("GS1001");
        Assert.assertTrue("s5 not exist", s5 == null);
        s6 = sm.getSensor("GS1002");
        Assert.assertTrue("s6 not exist", s6 == null);
        s7 = sm.getSensor("GS1003");
        Assert.assertTrue("s7 not exist", s7 == null);
        s8 = sm.getSensor("GS1004");
        Assert.assertTrue("s8 not exist", s8 == null);

        Assert.assertEquals("2 check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("2 check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("2 check s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("2 check s4", Sensor.INACTIVE, s4.getKnownState());

        r.setElement(0, 0x81); // sensor 1-4 (from 0) inactive
        r.setElement(1, 0x10);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);

        // create correct nibble only
        s5 = sm.getSensor("GS1001");
        Assert.assertTrue("s5 not exist", s5 == null);
        s6 = sm.getSensor("GS1002");
        Assert.assertTrue("s6 not exist", s6 == null);
        s7 = sm.getSensor("GS1003");
        Assert.assertTrue("s7 not exist", s7 == null);
        s8 = sm.getSensor("GS1004");
        Assert.assertTrue("s8 not exist", s8 == null);

        Assert.assertEquals("3 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("3 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("3 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("3 check s4", Sensor.ACTIVE, s4.getKnownState());

        r.setElement(0, 0x81); // sensor 1-4 (from 1) mixed
        r.setElement(1, 0x05);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);

        // create other nibble
        s5 = sm.getSensor("GS1001");
        Assert.assertTrue("s5 exist", s5 != null);
        s6 = sm.getSensor("GS1002");
        Assert.assertTrue("s6 exist", s6 != null);
        s7 = sm.getSensor("GS1003");
        Assert.assertTrue("s7 exist", s7 != null);
        s8 = sm.getSensor("GS1004");
        Assert.assertTrue("s8 exist", s8 != null);

        Assert.assertEquals("4 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("4 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("4 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("4 check s4", Sensor.ACTIVE, s4.getKnownState());
        Assert.assertEquals("4 check s5", Sensor.INACTIVE, s5.getKnownState());
        Assert.assertEquals("4 check s6", Sensor.ACTIVE, s6.getKnownState());
        Assert.assertEquals("4 check s7", Sensor.INACTIVE, s7.getKnownState());
        Assert.assertEquals("4 check s8", Sensor.ACTIVE, s8.getKnownState());
    }

    public void testMarkChangesParallelHighBankLowNibble() {
        // test with them not created
        SerialNode b = new SerialNode(1, SerialNode.NODE2002V6);

        Sensor s1, s2, s3, s4, s5, s6, s7, s8;
        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();

        // send message, which should create
        SerialReply r = new SerialReply();
        r.setElement(0, 0x81); // sensor 1-4 (from 0) inactive, GS1001-GS1004
        r.setElement(1, 0x4F);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);

        // created first four only
        s1 = sm.getSensor("GS1009");
        Assert.assertTrue("s1 exists", s1 != null);
        s2 = sm.getSensor("GS1010");
        Assert.assertTrue("s2 exists", s2 != null);
        s3 = sm.getSensor("GS1011");
        Assert.assertTrue("s3 exists", s3 != null);
        s4 = sm.getSensor("GS1012");
        Assert.assertTrue("s4 exists", s4 != null);
        s5 = sm.getSensor("GS1013");
        Assert.assertTrue("s5 not exist", s5 == null);
        s6 = sm.getSensor("GS1014");
        Assert.assertTrue("s6 not exist", s6 == null);
        s7 = sm.getSensor("GS1015");
        Assert.assertTrue("s7 not exist", s7 == null);
        s8 = sm.getSensor("GS1016");
        Assert.assertTrue("s8 not exist", s8 == null);

        Assert.assertEquals("2 check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("2 check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("2 check s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("2 check s4", Sensor.INACTIVE, s4.getKnownState());

        r.setElement(0, 0x81); // sensor 1-4 (from 0) inactive
        r.setElement(1, 0x40);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);
        // create first four only
        s5 = sm.getSensor("GS1013");
        Assert.assertTrue("s5 not exist", s5 == null);
        s6 = sm.getSensor("GS1014");
        Assert.assertTrue("s6 not exist", s6 == null);
        s7 = sm.getSensor("GS1015");
        Assert.assertTrue("s7 not exist", s7 == null);
        s8 = sm.getSensor("GS1016");
        Assert.assertTrue("s8 not exist", s8 == null);

        Assert.assertEquals("3 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("3 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("3 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("3 check s4", Sensor.ACTIVE, s4.getKnownState());

        r.setElement(0, 0x81); // sensor 5-8 (from 0) mixed
        r.setElement(1, 0x55);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);

        // create next four
        s5 = sm.getSensor("GS1013");
        Assert.assertTrue("s5 exist", s5 != null);
        s6 = sm.getSensor("GS1014");
        Assert.assertTrue("s6 exist", s6 != null);
        s7 = sm.getSensor("GS1015");
        Assert.assertTrue("s7 exist", s7 != null);
        s8 = sm.getSensor("GS1016");
        Assert.assertTrue("s8 exist", s8 != null);

        Assert.assertEquals("4 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("4 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("4 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("4 check s4", Sensor.ACTIVE, s4.getKnownState());
        Assert.assertEquals("4 check s5", Sensor.INACTIVE, s5.getKnownState());
        Assert.assertEquals("4 check s6", Sensor.ACTIVE, s6.getKnownState());
        Assert.assertEquals("4 check s7", Sensor.INACTIVE, s7.getKnownState());
        Assert.assertEquals("4 check s8", Sensor.ACTIVE, s8.getKnownState());
    }

    public void testMarkChangesParallelHighBankHighNibble() {
        // test with them not created
        SerialNode b = new SerialNode(1, SerialNode.NODE2002V6);

        Sensor s1, s2, s3, s4, s5, s6, s7, s8;
        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();

        // send message, which should create
        SerialReply r = new SerialReply();
        r.setElement(0, 0x81); // sensor 5-8 (from 1) inactive, GS1005-GS1008
        r.setElement(1, 0x5F);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);

        // create correct nibble only
        s1 = sm.getSensor("GS1013");
        Assert.assertTrue("s1 exists", s1 != null);
        s2 = sm.getSensor("GS1014");
        Assert.assertTrue("s2 exists", s2 != null);
        s3 = sm.getSensor("GS1015");
        Assert.assertTrue("s3 exists", s3 != null);
        s4 = sm.getSensor("GS1016");
        Assert.assertTrue("s4 exists", s4 != null);
        s5 = sm.getSensor("GS1009");
        Assert.assertTrue("s5 not exist", s5 == null);
        s6 = sm.getSensor("GS1010");
        Assert.assertTrue("s6 not exist", s6 == null);
        s7 = sm.getSensor("GS1011");
        Assert.assertTrue("s7 not exist", s7 == null);
        s8 = sm.getSensor("GS1012");
        Assert.assertTrue("s8 not exist", s8 == null);

        Assert.assertEquals("2 check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("2 check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("2 check s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("2 check s4", Sensor.INACTIVE, s4.getKnownState());

        r.setElement(0, 0x81); // sensor 1-4 (from 0) inactive
        r.setElement(1, 0x50);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);

        // create correct nibble only
        s5 = sm.getSensor("GS1009");
        Assert.assertTrue("s5 not exist", s5 == null);
        s6 = sm.getSensor("GS1010");
        Assert.assertTrue("s6 not exist", s6 == null);
        s7 = sm.getSensor("GS1011");
        Assert.assertTrue("s7 not exist", s7 == null);
        s8 = sm.getSensor("GS1012");
        Assert.assertTrue("s8 not exist", s8 == null);

        Assert.assertEquals("3 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("3 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("3 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("3 check s4", Sensor.ACTIVE, s4.getKnownState());

        r.setElement(0, 0x81); // sensor 1-4 (from 1) mixed
        r.setElement(1, 0x45);
        r.setElement(2, 0x81);
        r.setElement(3, 0x50);
        b.markChanges(r);

        // create other nibble
        s5 = sm.getSensor("GS1009");
        Assert.assertTrue("s5 exist", s5 != null);
        s6 = sm.getSensor("GS1010");
        Assert.assertTrue("s6 exist", s6 != null);
        s7 = sm.getSensor("GS1011");
        Assert.assertTrue("s7 exist", s7 != null);
        s8 = sm.getSensor("GS1012");
        Assert.assertTrue("s8 exist", s8 != null);

        Assert.assertEquals("4 check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("4 check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("4 check s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("4 check s4", Sensor.ACTIVE, s4.getKnownState());
        Assert.assertEquals("4 check s5", Sensor.INACTIVE, s5.getKnownState());
        Assert.assertEquals("4 check s6", Sensor.ACTIVE, s6.getKnownState());
        Assert.assertEquals("4 check s7", Sensor.INACTIVE, s7.getKnownState());
        Assert.assertEquals("4 check s8", Sensor.ACTIVE, s8.getKnownState());
    }

    public void testMarkChangesParallelCreated() {
        // test the low bank with them already created
        SerialNode b = new SerialNode(1, SerialNode.NODE2002V6);

        jmri.SensorManager sm = jmri.InstanceManager.sensorManagerInstance();
        Sensor s1 = sm.provideSensor("GS1001");
        Sensor s2 = sm.provideSensor("GS1002");
        Sensor s3 = sm.provideSensor("GS1003");
        Sensor s4 = sm.provideSensor("GS1004");
        Sensor s5 = sm.provideSensor("GS1005");
        Sensor s6 = sm.provideSensor("GS1006");
        Sensor s7 = sm.provideSensor("GS1007");
        Sensor s8 = sm.provideSensor("GS1008");

        Assert.assertTrue("check sensors active", b.getSensorsActive());
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
        r.setElement(1, 0x0F);
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
        r.setElement(1, 0x00);
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
        r.setElement(1, 0x15);
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
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialNodeTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();

        // replace the traffic manager
        SerialTrafficControlScaffold tcis = new SerialTrafficControlScaffold();
        memo = new GrapevineSystemConnectionMemo();
        memo.setTrafficController(tcis);
        // install a grapevine sensor manager
        jmri.InstanceManager.setSensorManager(new jmri.jmrix.grapevine.SerialSensorManager(memo));
        Assert.assertNotNull("exists", tcis);
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
