package jmri.jmrix.cmri.serial;

import jmri.Sensor;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class SerialSensorManagerTest extends TestCase {

    public void testSensorCreationAndRegistration() {

        // replace SerialTurnoutManager to make sure nodes start
        // at the beginning
        new SerialTrafficController() {
            void reset() {
                self = null;
            }
        }.reset();

        SerialSensorManager s = new SerialSensorManager();

        SerialNode n0 = new SerialNode();
        SerialNode n1 = new SerialNode(1, SerialNode.SMINI);
        SerialNode n2 = new SerialNode(2, SerialNode.USIC_SUSIC);
        n2.setNumBitsPerCard(24);
        n2.setCardTypeByAddress(0, SerialNode.INPUT_CARD);
        n2.setCardTypeByAddress(1, SerialNode.OUTPUT_CARD);
        n2.setCardTypeByAddress(3, SerialNode.OUTPUT_CARD);
        n2.setCardTypeByAddress(4, SerialNode.INPUT_CARD);
        n2.setCardTypeByAddress(2, SerialNode.OUTPUT_CARD);

        Assert.assertTrue("none expected A0", !(n0.getSensorsActive()));
        Assert.assertTrue("none expected A1", !(n1.getSensorsActive()));
        Assert.assertTrue("none expected A2", !(n2.getSensorsActive()));

        Sensor sensor = s.provideSensor("3");
        Assert.assertNotNull("found sensor", sensor);
        Assert.assertTrue("right name", sensor.getSystemName().equals("CS3"));
        Assert.assertTrue("UA 0", n0.getSensorsActive());
        Assert.assertTrue("2nd none expected A1", !(n1.getSensorsActive()));
        Assert.assertTrue("2nd none expected A2", !(n2.getSensorsActive()));

        s.provideSensor("11");
        s.provideSensor("8");
        s.provideSensor("19");
        s.provideSensor("23");
        s.provideSensor("CS2048");
        Assert.assertTrue("2nd UA 0", n0.getSensorsActive());
        Assert.assertTrue("3rd none expected UA 1", !(n1.getSensorsActive()));
        Assert.assertTrue("UA 2", n2.getSensorsActive());

        s.provideSensor("15");
        s.provideSensor("1001");
        Assert.assertTrue("3rd UA 0", n0.getSensorsActive());
        Assert.assertTrue("UA 1", n1.getSensorsActive());
        Assert.assertTrue("2nd UA 2", n0.getSensorsActive());
        s.provideSensor("17");
        s.provideSensor("1017");
        s.provideSensor("2017");
        Assert.assertTrue("4th UA 0", n0.getSensorsActive());
        Assert.assertTrue("2nd UA 1", n1.getSensorsActive());
        Assert.assertTrue("3rd UA 2", n0.getSensorsActive());
    }

    // from here down is testing infrastructure
    public SerialSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialSensorManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialSensorManagerTest.class);
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
