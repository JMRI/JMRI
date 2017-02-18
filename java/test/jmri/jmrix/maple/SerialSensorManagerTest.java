package jmri.jmrix.maple;

import jmri.Sensor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SerialSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2008
  */
public class SerialSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "KS" + i;
    }

    @Test
    public void testSensorCreationAndRegistration() {

        Sensor sensor = l.provideSensor("3");
        Assert.assertNotNull("found sensor", sensor);
        Assert.assertTrue("right name", sensor.getSystemName().equals("KS3"));
        Sensor s11 = l.provideSensor("11");
        Assert.assertNotNull("found s11", s11);
        Assert.assertTrue("right name s11", s11.getSystemName().equals("KS11"));
        //InputBits ibit = new InputBits();
        InputBits.setNumInputBits(1000);
        Sensor s248 = l.provideSensor("KS248");
        Assert.assertNotNull("found s248", s248);
        Assert.assertTrue("right name s248", s248.getSystemName().equals("KS248"));
        Sensor s1000 = l.provideSensor("1000");
        Assert.assertNotNull("found s1000", s1000);
        Assert.assertTrue("right name s1000", s1000.getSystemName().equals("KS1000"));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // replace SerialTurnoutManager to make sure nodes start
        // at the beginning
        new SerialTrafficController() {
            void reset() {
                self = null;
            }
        }.reset();

        l = new SerialSensorManager();

//        SerialNode n1 = new SerialNode(1,0);
//        SerialNode n2 = new SerialNode(2,0);
    }

    @After
    public void tearDown() {
        l.dispose();
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
