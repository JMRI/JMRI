package jmri.jmrix.openlcb;

import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 */
public class OlcbSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "MSX010203040506070" + i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        // olcb addresses are hex values requirng 16 digits.
        Sensor t = l.provideSensor("MSx010203040506070" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct " + t.getSystemName(), t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testUpperLower() {
        // olcb addresses are hex values requirng 16 digits.
        Sensor t = l.provideSensor("MSx010203040506070" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getSensor(name.toLowerCase()));
    }

    @Override
    @Test
    public void testMoveUserName() {
        Sensor t1 = l.provideSensor("MSx010203040506070" + getNumToTest1());
        Sensor t2 = l.provideSensor("MSx010203040506070" + getNumToTest2());
        t1.setUserName("UserName");
        Assert.assertTrue(t1 == l.getByUserName("UserName"));

        t2.setUserName("UserName");
        Assert.assertTrue(t2 == l.getByUserName("UserName"));

        Assert.assertTrue(null == t1.getUserName());
    }

    @Test
    public void testDotted() {
        // olcb addresses are hex values requirng 16 digits.
        Sensor t = l.provideSensor("MS01.02.03.04.05.06.07.0" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getSensor(name.toLowerCase()));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        OlcbSystemConnectionMemo m = OlcbTestInterface.createForLegacyTests();

        l = new OlcbSensorManager(m);
    }

    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.tearDown();
    }

}
