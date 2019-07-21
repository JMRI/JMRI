package jmri.jmrix.rps;

import jmri.Sensor;
import jmri.util.JUnitUtil;

import java.beans.PropertyVetoException;

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

    @Test
    @Override
    public void testProvideName() {
        // create
        Sensor t = l.provide(getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        // RPS sensors use coordinates as their address
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
    }

    @Test
    @Override
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
    @Test
    public void testRegisterDuplicateSystemName() throws PropertyVetoException, NoSuchFieldException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        testRegisterDuplicateSystemName(l,
                "RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)",
                "RS(0,0,0);(1,0,0);(1,1,0);(0,1,2)");
    }

    @Override
    @Test
    public void testMakeSystemName() {
        String s = l.makeSystemName("(0,0,0);(1,0,0);(1,1,0);(0,1,0)");
        Assert.assertNotNull(s);
        Assert.assertFalse(s.isEmpty());
    }

    @Test
    public void testGetSystemPrefix() {
        Assert.assertEquals("R", l.getSystemPrefix());
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        l = new RpsSensorManager(new RpsSystemConnectionMemo());
    }

    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.tearDown();
    }

}
