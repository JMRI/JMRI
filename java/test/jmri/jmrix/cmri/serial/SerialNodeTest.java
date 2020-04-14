package jmri.jmrix.cmri.serial;

import jmri.Sensor;
import jmri.jmrix.AbstractMRMessage;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SerialNode class
 *
 * @author	Bob Jacobsen Copyright 2003
 * @author	Dave Duchamp multi-node extensions 2003
 */
public class SerialNodeTest {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold stcs = null;

    @Test
    public void testCountInput1() {
        SerialNode a = new SerialNode(1, SerialNode.USIC_SUSIC,stcs);
        a.cardTypeLocation = new byte[]{SerialNode.INPUT_CARD, SerialNode.NO_CARD, SerialNode.OUTPUT_CARD};
        Assert.assertEquals("check 1 cards, not in order", 1, a.numInputCards());
    }

    @Test
    public void testCountInput2() {
        SerialNode a = new SerialNode(1, SerialNode.USIC_SUSIC,stcs);
        a.cardTypeLocation = new byte[]{SerialNode.INPUT_CARD, SerialNode.NO_CARD, SerialNode.INPUT_CARD};
        Assert.assertEquals("check 2 cards, not in order", 2, a.numInputCards());
    }

    @Test
    public void testCountOutput0() {
        SerialNode a = new SerialNode(1, SerialNode.USIC_SUSIC,stcs);
        a.cardTypeLocation = new byte[]{SerialNode.INPUT_CARD, SerialNode.NO_CARD, SerialNode.INPUT_CARD};
        Assert.assertEquals("check 0 cards", 0, a.numOutputCards());
    }

    @Test
    public void testCountOutput2() {
        SerialNode a = new SerialNode(1, SerialNode.USIC_SUSIC,stcs);
        a.cardTypeLocation = new byte[]{SerialNode.OUTPUT_CARD, SerialNode.OUTPUT_CARD, SerialNode.INPUT_CARD};
        Assert.assertEquals("check 2 cards", 2, a.numOutputCards());
    }

    @Test
    public void testConstructor1() {
        SerialNode b = new SerialNode(stcs);
        Assert.assertEquals("check default ctor type", SerialNode.SMINI, b.getNodeType());
        Assert.assertEquals("check default ctor address", 0, b.getNodeAddress());
    }

    @Test
    public void testConstructor2() {
        SerialNode c = new SerialNode(3, SerialNode.SMINI,stcs);
        Assert.assertEquals("check ctor type", SerialNode.SMINI, c.getNodeType());
        Assert.assertEquals("check ctor address", 3, c.getNodeAddress());
        Assert.assertEquals("check ctor default bitsPerCard", 24, c.getNumBitsPerCard());
        Assert.assertEquals("check ctor default delay", 0, c.getTransmissionDelay());
        Assert.assertEquals("check ctor default numOutputCards", 2, c.numOutputCards());
        Assert.assertEquals("check ctor default numInputCards", 1, c.numInputCards());
        Assert.assertEquals("check ctor default outputLocation", 1, c.getOutputCardIndex(1));
        Assert.assertEquals("check ctor default inputLocation", 0, c.getInputCardIndex(2));
    }

    @Test
    public void testConstructor3() {
        SerialNode d = new SerialNode(4, SerialNode.USIC_SUSIC,stcs);
        Assert.assertEquals("check ctor type", SerialNode.USIC_SUSIC, d.getNodeType());
        Assert.assertEquals("check ctor address", 4, d.getNodeAddress());
        Assert.assertEquals("check ctor default bitsPerCard", 24, d.getNumBitsPerCard());
        Assert.assertEquals("check ctor default delay", 0, d.getTransmissionDelay());
        Assert.assertEquals("check ctor default numOutputCards", 0, d.numOutputCards());
        Assert.assertEquals("check ctor default numInputCards", 0, d.numInputCards());
    }

    @Test
    public void testAccessors() {
        SerialNode n = new SerialNode(2, SerialNode.USIC_SUSIC,stcs);
        n.setNodeAddress(7);
        n.setNumBitsPerCard(32);
        n.setTransmissionDelay(2000);
        n.setCardTypeByAddress(0, SerialNode.INPUT_CARD);
        n.setCardTypeByAddress(1, SerialNode.OUTPUT_CARD);
        n.setCardTypeByAddress(2, SerialNode.OUTPUT_CARD);
        n.setCardTypeByAddress(3, SerialNode.OUTPUT_CARD);
        n.setCardTypeByAddress(4, SerialNode.INPUT_CARD);
        n.setCardTypeByAddress(5, SerialNode.OUTPUT_CARD);
        Assert.assertEquals("check ctor type", SerialNode.USIC_SUSIC, n.getNodeType());
        Assert.assertEquals("check address", 7, n.getNodeAddress());
        Assert.assertEquals("check bitsPerCard", 32, n.getNumBitsPerCard());
        Assert.assertEquals("check delay", 2000, n.getTransmissionDelay());
        Assert.assertEquals("check numOutputCards", 4, n.numOutputCards());
        Assert.assertEquals("check numInputCards", 2, n.numInputCards());
        Assert.assertEquals("check outputLocation", 3, n.getOutputCardIndex(5));
        Assert.assertEquals("check inputLocation", 1, n.getInputCardIndex(4));
        Assert.assertTrue("check output card type", n.isOutputCard(2));
        Assert.assertTrue("check input card type", n.isInputCard(0));
    }

    @Test
    public void testInitialization1() {
        SerialNode b = new SerialNode(stcs);
        // simple SMINI - no oscillating 2-lead searchlights
        AbstractMRMessage m = b.createInitPacket();
        Assert.assertEquals("packet size", 6, m.getNumDataElements());
        Assert.assertEquals("node address", 65, m.getElement(0));
        Assert.assertEquals("packet type", 73, m.getElement(1));  // 'I'
        Assert.assertEquals("node type", 77, m.getElement(2));    // 'M'
        Assert.assertEquals("delay high", 0, m.getElement(3));
        Assert.assertEquals("delay low", 0, m.getElement(4));
        Assert.assertEquals("NS", 0, m.getElement(5));            // No Searchlights
    }

    @Test
    public void testInitialization2() {
        // SMINI with searchlights - similar to CMRI User Manual, page B10
        SerialNode e = new SerialNode(9, SerialNode.SMINI,stcs);
        e.set2LeadSearchLight(0);
        e.set2LeadSearchLight(2);
        e.set2LeadSearchLight(4);
        e.set2LeadSearchLight(6);
        e.set2LeadSearchLight(10);
        e.set2LeadSearchLight(16);
        e.set2LeadSearchLight(19);
        e.set2LeadSearchLight(22);
        e.set2LeadSearchLight(46);
        e.set2LeadSearchLight(33);
        e.set2LeadSearchLight(37);
        e.set2LeadSearchLight(44);
        e.clear2LeadSearchLight(10);
        e.clear2LeadSearchLight(46);
        Assert.assertTrue("check searchlight bit", e.isSearchLightBit(7));
        Assert.assertTrue("check not searchlight bit", !(e.isSearchLightBit(35)));
        AbstractMRMessage m = e.createInitPacket();
        Assert.assertEquals("packet size", 12, m.getNumDataElements());
        Assert.assertEquals("node address", 74, m.getElement(0));
        Assert.assertEquals("packet type", 73, m.getElement(1));  // 'I'
        Assert.assertEquals("node type", 77, m.getElement(2));    // 'M'
        Assert.assertEquals("delay high", 0, m.getElement(3));
        Assert.assertEquals("delay low", 0, m.getElement(4));
        Assert.assertEquals("NS", 10, m.getElement(5));        // Num Searchlights
        Assert.assertEquals("sl code 1", 255, (m.getElement(6) & 0xff));
        Assert.assertEquals("sl code 2", 0, (m.getElement(7) & 0xff));
        Assert.assertEquals("sl code 3", 219, (m.getElement(8) & 0xff));
        Assert.assertEquals("sl code 4", 0, (m.getElement(9) & 0xff));
        Assert.assertEquals("sl code 5", 102, (m.getElement(10) & 0xff));
        Assert.assertEquals("sl code 6", 48, (m.getElement(11) & 0xff));
    }

    @Test
    public void testInitialization3() {
        // USIC_SUSIC with delay and 6 32-bit cards
        SerialNode p = new SerialNode(10, SerialNode.USIC_SUSIC,stcs);
        p.setNumBitsPerCard(32);
        p.setTransmissionDelay(2000);
        p.setCardTypeByAddress(0, SerialNode.INPUT_CARD);
        p.setCardTypeByAddress(1, SerialNode.OUTPUT_CARD);
        p.setCardTypeByAddress(2, SerialNode.OUTPUT_CARD);
        p.setCardTypeByAddress(3, SerialNode.OUTPUT_CARD);
        p.setCardTypeByAddress(4, SerialNode.INPUT_CARD);
        p.setCardTypeByAddress(5, SerialNode.OUTPUT_CARD);
        AbstractMRMessage m = p.createInitPacket();
        Assert.assertEquals("packet size", 9, m.getNumDataElements());
        Assert.assertEquals("node address", 75, m.getElement(0));
        Assert.assertEquals("packet type", 73, m.getElement(1));  // 'I'
        Assert.assertEquals("node type", 88, m.getElement(2));    // 'X'
        Assert.assertEquals("delay high", 7, (m.getElement(3) & 0xff));
        Assert.assertEquals("delay low", 208, (m.getElement(4) & 0xff));
        Assert.assertEquals("DLE", 16, m.getElement(5));   // DLE because 2 is next
        Assert.assertEquals("NS", 2, m.getElement(6));     // 2 groups of 4
        Assert.assertEquals("1st group", 169, (m.getElement(7) & 0xff)); // IOOO
        Assert.assertEquals("2nd group", 9, (m.getElement(8) & 0xff)); // IOXX
    }

    @Test
    public void testOutputBits1() {
        // SMINI with several output bits set
        SerialNode g = new SerialNode(5, SerialNode.SMINI,stcs);
        Assert.assertTrue("must Send", g.mustSend());
        g.resetMustSend();
        Assert.assertTrue("must Send off", !(g.mustSend()));
        g.setOutputBit(2, false);
        g.setOutputBit(1, false);
        g.setOutputBit(23, false);
        g.setOutputBit(41, false);
        g.setOutputBit(31, false);
        g.setOutputBit(2, true);
        g.setOutputBit(19, false);
        g.setOutputBit(5, false);
        g.setOutputBit(26, false);
        g.setOutputBit(48, false);
        Assert.assertTrue("must Send on", g.mustSend());
        AbstractMRMessage m = g.createOutPacket();
        Assert.assertEquals("packet size", 8, m.getNumDataElements());
        Assert.assertEquals("node address", 70, m.getElement(0));
        Assert.assertEquals("packet type", 84, m.getElement(1));  // 'T'
        Assert.assertEquals("out byte 1", 17, (m.getElement(2) & 0xff));
        Assert.assertEquals("out byte 2", 0, (m.getElement(3) & 0xff));
        Assert.assertEquals("out byte 3", 68, (m.getElement(4) & 0xff));
        Assert.assertEquals("out byte 4", 66, (m.getElement(5) & 0xff));
        Assert.assertEquals("out byte 5", 0, (m.getElement(6) & 0xff));
        Assert.assertEquals("out byte 6", 129, (m.getElement(7) & 0xff));
    }

    @Test
    public void testMarkChangesInitial() {
        SerialNode b = new SerialNode(stcs);
        SerialSensor s1 = new SerialSensor("CS1", "a");
        Assert.assertEquals("check bit number", 1, memo.getBitFromSystemName("CS1"));
        SerialSensor s2 = new SerialSensor("CS2", "ab");
        SerialSensor s3 = new SerialSensor("CS3", "abc");
        b.registerSensor(s1, 0);
        b.registerSensor(s2, 1);
        b.registerSensor(s3, 2);
        Assert.assertTrue("check sensors active", b.getSensorsActive());
        // from UNKNOWN, 1st poll goes to new state
        SerialReply r = new SerialReply();
        r.setElement(2, '2');
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
    }

    @Test
    public void testMarkChanges2ndByte() {
        SerialNode b = new SerialNode(stcs);
        SerialSensor s1 = new SerialSensor("CS9", "a");
        Assert.assertEquals("check bit number", 1, memo.getBitFromSystemName("CS1"));
        SerialSensor s2 = new SerialSensor("CS10", "ab");
        SerialSensor s3 = new SerialSensor("CS11", "abc");
        b.registerSensor(s1, 8);
        b.registerSensor(s2, 9);
        b.registerSensor(s3, 10);
        Assert.assertTrue("check sensors active", b.getSensorsActive());
        // from UNKNOWN, 1st poll goes to new state
        SerialReply r = new SerialReply();
        r.setElement(2, '0');
        r.setElement(3, '2');
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
    }

    @Test
    public void testMarkChangesShortReply() {
        SerialNode b = new SerialNode(stcs);
        SerialSensor s1 = new SerialSensor("CS9", "a");
        Assert.assertEquals("check bit number", 1, memo.getBitFromSystemName("CS1"));
        SerialSensor s2 = new SerialSensor("CS10", "ab");
        SerialSensor s3 = new SerialSensor("CS11", "abc");
        b.registerSensor(s1, 8);
        b.registerSensor(s2, 9);
        b.registerSensor(s3, 10);
        Assert.assertTrue("check sensors active", b.getSensorsActive());
        // from UNKNOWN, 1st poll goes to new state
        SerialReply r = new SerialReply();
        r.setElement(2, '0');
        r.setElement(3, '2');
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
        r = new SerialReply();
        r.setElement(2, '0');
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
    }

    @Test
    public void testMarkChangesEmptyReply() {
        SerialNode b = new SerialNode(stcs);
        SerialSensor s1 = new SerialSensor("CS9", "a");
        Assert.assertEquals("check bit number", 1, memo.getBitFromSystemName("CS1"));
        SerialSensor s2 = new SerialSensor("CS10", "ab");
        SerialSensor s3 = new SerialSensor("CS11", "abc");
        b.registerSensor(s1, 8);
        b.registerSensor(s2, 9);
        b.registerSensor(s3, 10);
        Assert.assertTrue("check sensors active", b.getSensorsActive());
        // from UNKNOWN, 1st poll goes to new state
        SerialReply r = new SerialReply();
        r.setElement(2, '0');
        r.setElement(3, '2');
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
        r = new SerialReply();
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
        r = new SerialReply();
        r.setElement(3, '5');
        b.markChanges(r);
        b.markChanges(r); // for debounce
        Assert.assertEquals("check s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.ACTIVE, s3.getKnownState());
    }

    @Test
    public void testMarkChangesDebounce() {
        SerialNode b = new SerialNode(stcs);
        SerialSensor s1 = new SerialSensor("CS1", "a");
        SerialSensor s2 = new SerialSensor("CS2", "ab");
        SerialSensor s3 = new SerialSensor("CS3", "abc");
        SerialSensor s4 = new SerialSensor("CS4", "abcd");
        b.registerSensor(s1, 0);
        b.registerSensor(s2, 1);
        b.registerSensor(s3, 2);
        b.registerSensor(s4, 3);
        // from UNKNOWN, 1st poll goes to new state
        SerialReply r;
        r = new SerialReply();
        r.setElement(2, '5');
        b.markChanges(r);
        Assert.assertEquals("poll0 s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("poll0 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll0 s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("poll0 s4", Sensor.INACTIVE, s4.getKnownState());
        // stabilize startup
        b.markChanges(r);
        b.markChanges(r);
        b.markChanges(r);
        Assert.assertEquals("poll1 s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("poll1 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll1 s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("poll1 s4", Sensor.INACTIVE, s4.getKnownState());
        // single poll shouldn't change
        r = new SerialReply();
        r.setElement(2, '0' + 10);
        b.markChanges(r);
        Assert.assertEquals("poll2 s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("poll2 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll2 s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("poll2 s4", Sensor.INACTIVE, s4.getKnownState());
        // 2nd poll should, but only if same
        r = new SerialReply();
        r.setElement(2, '6');
        b.markChanges(r);
        Assert.assertEquals("poll3 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll3 s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("poll3 s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("poll3 s4", Sensor.INACTIVE, s4.getKnownState());
        // 3rd poll changes last two
        r = new SerialReply();
        r.setElement(2, '5');
        b.markChanges(r);
        Assert.assertEquals("poll4 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll4 s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("poll4 s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("poll4 s4", Sensor.INACTIVE, s4.getKnownState());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // replace the SerialTrafficController
        stcs = new SerialTrafficControlScaffold();
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(stcs);
    }

    @After
    public void tearDown() {
        if (stcs != null) stcs.terminateThreads();
        stcs = null;
        memo = null;
        
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
