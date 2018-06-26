package jmri.jmrix.openlcb;

import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 */
public class OlcbSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private static OlcbSystemConnectionMemo m;

    @Override
    public String getSystemName(int i) {
        return "MSX010203040506070" + i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Test
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
        // olcb addresses are hex values requirng 16 digits.
        Sensor t = l.provideSensor(getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct " + t.getSystemName(), t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Ignore
    @Test
    public void testUpperLower() { // ignoring this test due to the system name format, needs to be properly coded
    }
    
    @Override
    @Test
    public void testMoveUserName() {
        Sensor t1 = l.provideSensor(getSystemName(getNumToTest1()));
        Sensor t2 = l.provideSensor(getSystemName(getNumToTest2()));
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
        Assert.assertEquals(t, l.getSensor(name));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        l = new OlcbSensorManager(m);
    }

    @BeforeClass
    public static void preClassInit(){
        JUnitUtil.setUp();
        m = OlcbTestInterface.createForLegacyTests();
    }

    @After
    public void tearDown() {
        l.dispose();
    }

    @AfterClass
    public static void postClassTearDown(){
        if(m != null && m.getInterface() !=null ) {
           m.getInterface().dispose();
        }
        JUnitUtil.tearDown();
    }

}
