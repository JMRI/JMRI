// SerialNodeTest.java

package jmri.jmrix.cmri.serial;

import jmri.Sensor;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialNode class
 * @author		Bob Jacobsen  Copyright 2003
 * @version		$Revision: 1.6 $
 */
public class SerialNodeTest extends TestCase {

    public void testCountInput1() {
		SerialNode a = new SerialNode();
		a.cardTypeLocation = new byte[]{SerialNode.INPUT_CARD, SerialNode.NO_CARD, SerialNode.OUTPUT_CARD};
		Assert.assertEquals("check 1 cards, not in order", 1, a.numInputCards());
	}

    public void testCountInput2() {
		SerialNode a = new SerialNode();
		a.cardTypeLocation = new byte[]{SerialNode.INPUT_CARD, SerialNode.NO_CARD, SerialNode.INPUT_CARD};
		Assert.assertEquals("check 2 cards, not in order", 2, a.numInputCards());
	}
	
    public void testCountOutput0() {
		SerialNode a = new SerialNode();
		a.cardTypeLocation = new byte[]{SerialNode.INPUT_CARD, SerialNode.NO_CARD, SerialNode.INPUT_CARD};
		Assert.assertEquals("check 0 cards", 0, a.numOutputCards());
	}
	
    public void testCountOutput2() {
		SerialNode a = new SerialNode();
		a.cardTypeLocation = new byte[]{SerialNode.OUTPUT_CARD, SerialNode.OUTPUT_CARD, SerialNode.INPUT_CARD};
		Assert.assertEquals("check 2 cards", 2, a.numOutputCards());
	}
	
    public void testMarkChanges() {
        SerialNode a = new SerialNode();
        SerialSensor s1 = new SerialSensor("1");
        SerialSensor s2 = new SerialSensor("2");
        SerialSensor s3 = new SerialSensor("3");
        a.registerSensor(s1, 0);
        a.registerSensor(s2, 1);
        a.registerSensor(s3, 2);
        SerialReply r = new SerialReply();
        r.setElement(2, '2');
        a.markChanges(r);
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

}
