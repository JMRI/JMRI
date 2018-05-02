package jmri.jmrix.rps;

import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the RPS SensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @author Paul Bender Copyright (C) 2016
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

    @Test
    public void testMoveUserName() {
        Sensor t1 = l.provideSensor("RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)");
        Sensor t2 = l.provideSensor("RS(0,0,0);(1,0,0);(1,1,0);(0,1,2)");
        t1.setUserName("UserName");
        Assert.assertTrue(t1 == l.getByUserName("UserName"));

        t2.setUserName("UserName");
        Assert.assertTrue(t2 == l.getByUserName("UserName"));

        Assert.assertTrue(null == t1.getUserName());
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        l = new RpsSensorManager();
    }

    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.tearDown();

    }
}
