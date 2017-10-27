package jmri.jmrix.can.cbus;

import jmri.Sensor;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TestTrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private CanSystemConnectionMemo memo = null;

    @Override
    public String getSystemName(int i) {
        return "MSX0A;+N15E" + i;
    }

    @Test
    @Override
    public void testCreate() {
        Assert.assertNotNull("creaesSensor", l.provideSensor(memo.getSystemPrefix() + "SX0A;+N15E6"));
    }

    @Test
    @Override
    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor("MSX0A;+N15E" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    @Override
    public void testUpperLower() {
        Sensor t = l.provideSensor("MSX0A;+N15E" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getSensor(name.toLowerCase()));
    }

    @Override
    @Test
    public void testMoveUserName() {
        Sensor t1 = l.provideSensor("MSX0A;+N15E" + getNumToTest1());
        Sensor t2 = l.provideSensor("MSX0A;+N15E" + getNumToTest2());
        t1.setUserName("UserName");
        Assert.assertTrue(t1 == l.getByUserName("UserName"));

        t2.setUserName("UserName");
        Assert.assertTrue(t2 == l.getByUserName("UserName"));

        Assert.assertTrue(null == t1.getUserName());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(new TestTrafficController());
        l = new CbusSensorManager(memo);
    }

    @After
    public void tearDown() {
        l.dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }

}
