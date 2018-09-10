package jmri.jmrix.internal;

import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Tests for the jmri.jmrix.internal.InternalSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2016
 */
public class InternalSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "IS" + i;
    }

    public void testSensorNameCase() {
        Assert.assertEquals(0, l.getObjectCount());
        // create
        Sensor t = l.provideSensor("IS:XYZ");
        t = l.provideSensor("IS:xyz");  // upper canse and lower case are the same object
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("IS:XYZ", t.getSystemName());  // we force upper
        Assert.assertTrue("system name correct ", t == l.getBySystemName("IS:XYZ"));
        Assert.assertEquals(1, l.getObjectCount());
        Assert.assertEquals(1, l.getSystemNameAddedOrderList().size());

        t = l.provideSensor("IS:XYZ");
        Assert.assertEquals(1, l.getObjectCount());
        Assert.assertEquals(1, l.getSystemNameAddedOrderList().size());
    }

    @Test
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

    @Test
    public void testSetGetDefaultState() {

        // confirm default
        Assert.assertEquals("starting mode", Sensor.UNKNOWN, InternalSensorManager.getDefaultStateForNewSensors() );
        
        // set and retrieve
        InternalSensorManager.setDefaultStateForNewSensors(Sensor.INACTIVE);
        Assert.assertEquals("updated mode", Sensor.INACTIVE, InternalSensorManager.getDefaultStateForNewSensors() );
               
    }

    // from here down is testing infrastructure
    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        // create and register the manager object
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        l = jmri.InstanceManager.sensorManagerInstance();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    private final static Logger log = LoggerFactory.getLogger(InternalSensorManagerTest.class);

}
