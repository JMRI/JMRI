// SerialNodeTest.java

package jmri.jmrix.maple;

import jmri.Sensor;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.AbstractMRMessage;

/**
 * JUnit tests for the SerialNode class
 * @author		Bob Jacobsen  Copyright 2003
 * @author		Dave Duchamp  multi-node extensions 2003
 * @version		$Revision: 1.3 $
 */
public class SerialNodeTest extends TestCase {
		
    private SerialNode a = new SerialNode(1,SerialNode.USIC_SUSIC);

    public void testCountInput1() {
		a.cardTypeLocation = new byte[]{SerialNode.INPUT_CARD, SerialNode.NO_CARD, SerialNode.OUTPUT_CARD};
		Assert.assertEquals("check 1 cards, not in order", 1, a.numInputCards());
	}

    public void testCountInput2() {
		a.cardTypeLocation = new byte[]{SerialNode.INPUT_CARD, SerialNode.NO_CARD, SerialNode.INPUT_CARD};
		Assert.assertEquals("check 2 cards, not in order", 2, a.numInputCards());
	}
	
    public void testCountOutput0() {
		a.cardTypeLocation = new byte[]{SerialNode.INPUT_CARD, SerialNode.NO_CARD, SerialNode.INPUT_CARD};
		Assert.assertEquals("check 0 cards", 0, a.numOutputCards());
	}
	
    public void testCountOutput2() {
		a.cardTypeLocation = new byte[]{SerialNode.OUTPUT_CARD, SerialNode.OUTPUT_CARD, SerialNode.INPUT_CARD};
		Assert.assertEquals("check 2 cards", 2, a.numOutputCards());
	}
    
    SerialNode b = new SerialNode();
       
    public void testConstructor1() {
        Assert.assertEquals("check default ctor type", SerialNode.SMINI, b.getNodeType());
        Assert.assertEquals("check default ctor address", 0, b.getNodeAddress());
    }

    public void testConstructor2() {
        SerialNode c = new SerialNode(3,SerialNode.SMINI);
        Assert.assertEquals("check ctor type", SerialNode.SMINI, c.getNodeType());
        Assert.assertEquals("check ctor address", 3, c.getNodeAddress());
        Assert.assertEquals("check ctor default bitsPerCard", 24, c.getNumBitsPerCard());
        Assert.assertEquals("check ctor default delay", 0, c.getTransmissionDelay());
        Assert.assertEquals("check ctor default numOutputCards", 2, c.numOutputCards());
        Assert.assertEquals("check ctor default numInputCards", 1, c.numInputCards());
        Assert.assertEquals("check ctor default outputLocation", 1, c.getOutputCardIndex(1));
        Assert.assertEquals("check ctor default inputLocation", 0, c.getInputCardIndex(2));
    }

    public void testConstructor3() {
        SerialNode d = new SerialNode(4,SerialNode.USIC_SUSIC);
        Assert.assertEquals("check ctor type", SerialNode.USIC_SUSIC, d.getNodeType());
        Assert.assertEquals("check ctor address", 4, d.getNodeAddress());
        Assert.assertEquals("check ctor default bitsPerCard", 24, d.getNumBitsPerCard());
        Assert.assertEquals("check ctor default delay", 0, d.getTransmissionDelay());
        Assert.assertEquals("check ctor default numOutputCards", 0, d.numOutputCards());
        Assert.assertEquals("check ctor default numInputCards", 0, d.numInputCards());
    }

    public void testAccessors() {
        SerialNode n = new SerialNode(2,SerialNode.USIC_SUSIC);
        n.setNodeAddress (7);
        n.setNumBitsPerCard (32);
        n.setTransmissionDelay (2000);
        n.setCardTypeByAddress (0,SerialNode.INPUT_CARD);
        n.setCardTypeByAddress (1,SerialNode.OUTPUT_CARD);
        n.setCardTypeByAddress (2,SerialNode.OUTPUT_CARD);
        n.setCardTypeByAddress (3,SerialNode.OUTPUT_CARD);
        n.setCardTypeByAddress (4,SerialNode.INPUT_CARD);
        n.setCardTypeByAddress (5,SerialNode.OUTPUT_CARD);
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
    
    public void testInitialization1() {
        // simple SMINI - no oscillating 2-lead searchlights
        AbstractMRMessage m = b.createInitPacket();
        Assert.assertEquals("null message", null, m );
    }

    public void testOutputBits1() {
        // SMINI with several output bits set
        SerialNode g = new SerialNode(5,SerialNode.SMINI);
        Assert.assertTrue("must Send", g.mustSend() );
        g.resetMustSend();
        Assert.assertTrue("must Send off", !(g.mustSend()) );
        g.setOutputBit(2,false);
        g.setOutputBit(1,false);
        g.setOutputBit(23,false);
        g.setOutputBit(41,false);
        g.setOutputBit(31,false);
        g.setOutputBit(2,true);
        g.setOutputBit(19,false);
        g.setOutputBit(5,false);
        g.setOutputBit(26,false);
        g.setOutputBit(48,false);
        Assert.assertTrue("must Send on", g.mustSend() );

        AbstractMRMessage m = g.createOutPacket();

        Assert.assertEquals("packet size", 62, m.getNumDataElements() );
        Assert.assertEquals("node address 1", '0', m.getElement(1) );
        Assert.assertEquals("node address 2", '5', m.getElement(2) );
        Assert.assertEquals("packet type 1", 'W', m.getElement(3) );        
        Assert.assertEquals("packet type 2", 'C', m.getElement(4) );        
        Assert.assertEquals("TO val 1 f", '1', (m.getElement(10+1) & 0xff));      
        Assert.assertEquals("TO val 2 t", '0', (m.getElement(10+2) & 0xff));      
        Assert.assertEquals("TO val 3 unknown", '0', (m.getElement(10+3) & 0xff));      
    }
	
    public void testMarkChangesInitial() {
        SerialSensor s1 = new SerialSensor("KS1","a");
        Assert.assertEquals("check bit number",1,SerialAddress.getBitFromSystemName("KS1"));
        SerialSensor s2 = new SerialSensor("KS2","ab");
        SerialSensor s3 = new SerialSensor("KS3","abc");
        b.registerSensor(s1, 0);
        b.registerSensor(s2, 1);
        b.registerSensor(s3, 2);
        Assert.assertTrue("check sensors active", b.getSensorsActive());
        // from UNKNOWN, 1st poll goes to new state
        SerialReply r = new SerialReply();
        // set reply all zero
        for (int i=5; i<5+48; i++) r.setElement(i, '0');

        // and interpret
        b.markChanges(r);

        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
    }

    public void testMarkChangesDebounce() {
        SerialSensor s1 = new SerialSensor("KS1","a");
        SerialSensor s2 = new SerialSensor("KS2","ab");
        SerialSensor s3 = new SerialSensor("KS3","abc");
        SerialSensor s4 = new SerialSensor("KS4","abcd");
        b.registerSensor(s1, 0);
        b.registerSensor(s2, 1);
        b.registerSensor(s3, 2);
        b.registerSensor(s4, 3);
        // from UNKNOWN, 1st poll goes to new state
        SerialReply r = new SerialReply();
        // set reply all zero
        for (int i=5; i<5+48; i++) r.setElement(i, '0');

        // and interpret
        b.markChanges(r);

        Assert.assertEquals("poll0 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll0 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll0 s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("poll0 s4", Sensor.INACTIVE, s4.getKnownState());
        // stabilize startup
        b.markChanges(r);
        b.markChanges(r);
        b.markChanges(r);
        Assert.assertEquals("poll1 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll1 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll1 s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("poll1 s4", Sensor.INACTIVE, s4.getKnownState());

        // single poll shouldn't change
        r = new SerialReply();
        r.setElement(5, '0');  
        r.setElement(6, '1');  
        r.setElement(7, '0');  
        b.markChanges(r);
        Assert.assertEquals("poll2 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll2 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll2 s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("poll2 s4", Sensor.INACTIVE, s4.getKnownState());
        // 2nd poll should, but only if same
        r = new SerialReply();
        r.setElement(5, '1');  
        r.setElement(6, '1');  
        r.setElement(7, '1');  
        b.markChanges(r);
        Assert.assertEquals("poll3 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll3 s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("poll3 s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("poll3 s4", Sensor.INACTIVE, s4.getKnownState());
        // 3rd poll changes last two
        r = new SerialReply();
        r.setElement(5, '1');  
        r.setElement(6, '0');  
        r.setElement(7, '1');  
        b.markChanges(r);
        Assert.assertEquals("poll4 s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("poll4 s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("poll4 s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("poll4 s4", Sensor.INACTIVE, s4.getKnownState());
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
