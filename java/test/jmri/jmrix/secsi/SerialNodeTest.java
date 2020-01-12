package jmri.jmrix.secsi;

import jmri.Sensor;
import jmri.jmrix.AbstractMRMessage;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * JUnit tests for the SerialNode class
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008
 * @author Dave Duchamp multi-node extensions 2003
 */
public class SerialNodeTest {

    private SerialTrafficControlScaffold tcis = null;
    private SecsiSystemConnectionMemo memo = null;

    private SerialNode b = null;

    @Test
    public void testConstructor1() {
        Assert.assertEquals("check default ctor type", SerialNode.DAUGHTER, b.getNodeType());
        Assert.assertEquals("check default ctor address", 0, b.getNodeAddress());
    }

    @Test
    public void testConstructor2() {
        SerialNode c = new SerialNode(3, SerialNode.DAUGHTER, tcis);
        Assert.assertEquals("check ctor type", SerialNode.DAUGHTER, c.getNodeType());
        Assert.assertEquals("check ctor address", 3, c.getNodeAddress());
    }

    @Test
    public void testAccessors() {
        SerialNode n = new SerialNode(2, SerialNode.DAUGHTER, tcis);
        n.setNodeAddress(7);
        Assert.assertEquals("check ctor type", SerialNode.DAUGHTER, n.getNodeType());
        Assert.assertEquals("check address", 7, n.getNodeAddress());
    }

    @Test
    public void testInitialization1() {
        // no initialization in this protocol
        AbstractMRMessage m = b.createInitPacket();
        Assert.assertEquals("initpacket null", null, m);
    }

    @Test
    public void testOutputBits1() {
        // IO48 with several output bits set
        SerialNode g = new SerialNode(5, SerialNode.DAUGHTER, tcis);
        Assert.assertTrue("must Send", g.mustSend());
        g.resetMustSend();
        Assert.assertTrue("must Send off", !(g.mustSend()));
        g.setOutputBit(2, false);
        g.setOutputBit(1, false);
        g.setOutputBit(23, false);
        g.setOutputBit(21, false);
        g.setOutputBit(31, true);
        g.setOutputBit(2, true);
        g.setOutputBit(19, true);
        g.setOutputBit(5, true);
        g.setOutputBit(26, false);
        g.setOutputBit(28, true);
        Assert.assertTrue("must Send on", g.mustSend());
        AbstractMRMessage m = g.createOutPacket();
        Assert.assertEquals("packet size", 9, m.getNumDataElements());
        Assert.assertEquals("node address", 5, m.getElement(0));
        Assert.assertEquals("byte 1 lo nibble", 0x02, m.getElement(1));
        Assert.assertEquals("byte 1 hi nibble", 0x11, m.getElement(2));
        Assert.assertEquals("byte 2 lo nibble", 0x20, m.getElement(3));
        Assert.assertEquals("byte 2 hi nibble", 0x30, m.getElement(4));
        Assert.assertEquals("byte 3 lo nibble", 0x44, m.getElement(5));
        Assert.assertEquals("byte 3 hi nibble", 0x50, m.getElement(6));
        Assert.assertEquals("byte 4 lo nibble", 0x68, m.getElement(7));
        Assert.assertEquals("byte 4 hi nibble", 0x74, m.getElement(8));
    }

    @Test
    public void testMarkChanges() {
        SerialSensor s1 = new SerialSensor("VS1", "a", memo);
        Assert.assertEquals("check bit number", 1, SerialAddress.getBitFromSystemName("VS1", "V"));
        SerialSensor s2 = new SerialSensor("VS2", "ab", memo);
        SerialSensor s3 = new SerialSensor("VS3", "abc", memo);
        b.registerSensor(s1, 0);
        b.registerSensor(s2, 1);
        b.registerSensor(s3, 2);
        Assert.assertTrue("check sensors active", b.getSensorsActive());
        SerialReply r = new SerialReply();
        r.setElement(0, 0x02);
        r.setElement(1, 0x00);
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();

        memo = new SecsiSystemConnectionMemo();
        tcis = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tcis);
        b = new SerialNode(tcis);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
