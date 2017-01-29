package jmri.jmrix.rps;

import jmri.Sensor;
import jmri.SensorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the RPS SensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @author      Paul Bender Copyright (C) 2016
 */
public class RpsSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)";
    }


    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        // RPS sensors use coordinates as their address, and they require a 
        // 2 characterprefix (for now).
        Sensor t = l.provideSensor("RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testUpperLower() {
        // RPS sensors use coordinates as their address, and they require a 
        // 2 characterprefix (for now).
        Sensor t = l.provideSensor("RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)");
        String name = t.getSystemName();
        Assert.assertNull(l.getSensor(name.toLowerCase()));
    }


    @Override
    @Before
    public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        l = new RpsSensorManager();
    }

    @After
    public void tearDown(){
        l.dispose();
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();

    }
}
