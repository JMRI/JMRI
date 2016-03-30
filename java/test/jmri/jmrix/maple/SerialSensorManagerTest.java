package jmri.jmrix.maple;

import jmri.Sensor;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2008
 * @version	$Revision$
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

//        SerialNode n1 = new SerialNode(1,0);
//        SerialNode n2 = new SerialNode(2,0);
        Sensor sensor = s.provideSensor("3");
        Assert.assertNotNull("found sensor", sensor);
        Assert.assertTrue("right name", sensor.getSystemName().equals("KS3"));
        Sensor s11 = s.provideSensor("11");
        Assert.assertNotNull("found s11", s11);
        Assert.assertTrue("right name s11", s11.getSystemName().equals("KS11"));
        //InputBits ibit = new InputBits();
        InputBits.setNumInputBits(1000);
        Sensor s248 = s.provideSensor("KS248");
        Assert.assertNotNull("found s248", s248);
        Assert.assertTrue("right name s248", s248.getSystemName().equals("KS248"));
        Sensor s1000 = s.provideSensor("1000");
        Assert.assertNotNull("found s1000", s1000);
        Assert.assertTrue("right name s1000", s1000.getSystemName().equals("KS1000"));
    }

    // from here down is testing infrastructure
    public SerialSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialSensorManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
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
