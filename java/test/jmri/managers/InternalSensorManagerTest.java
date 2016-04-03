package jmri.managers;

import jmri.Sensor;
import jmri.SensorManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.managers.InternalSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2016
 */
public class InternalSensorManagerTest extends jmri.managers.AbstractSensorMgrTest {

    public String getSystemName(int i) {
        return "IS" + i;
    }

    public void testAsAbstractFactory() {

        // ask for a Sensor, and check type
        SensorManager lm = jmri.InstanceManager.sensorManagerInstance();

        Sensor tl = lm.newSensor("IS21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received sensor value " + tl);
        }
        Assert.assertTrue(null != tl);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + lm.getBySystemName("IS21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + lm.getByUserName("my name"));
        }

        Assert.assertTrue(null != lm.getBySystemName("IS21"));
        Assert.assertTrue(null != lm.getByUserName("my name"));

    }

    public void testSetGetDefaultState() {

        // confirm default
        Assert.assertEquals("starting mode", Sensor.UNKNOWN, InternalSensorManager.getDefaultStateForNewSensors() );
        
        // set and retrieve
        InternalSensorManager.setDefaultStateForNewSensors(Sensor.INACTIVE);
        Assert.assertEquals("updated mode", Sensor.INACTIVE, InternalSensorManager.getDefaultStateForNewSensors() );
               
    }

    // from here down is testing infrastructure
    public InternalSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", InternalSensorManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(InternalSensorManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // create and register the manager object
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        l = jmri.InstanceManager.sensorManagerInstance();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(InternalSensorManagerTest.class.getName());

}
