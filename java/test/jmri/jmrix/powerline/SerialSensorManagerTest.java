package jmri.jmrix.powerline;

import jmri.NamedBean;
import jmri.Sensor;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import java.beans.PropertyVetoException;

import org.junit.*;

/**
 * JUnit tests for the SerialSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "PSP" + i;
    }

    /**
     * Number of sensor to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers
     */
    @Override
    protected int getNumToTest1() {
        return 8;
    }

    @Override
    protected int getNumToTest2() {
        return 9;
    }

    @Test
    public void testSensorCreationAndRegistration() {
        l.provideSensor("PSA3");

        l.provideSensor("PSA11");

        l.provideSensor("PSP8");

        l.provideSensor("PSP9");

        l.provideSensor("PSK13");

        l.provideSensor("PSJ6");

        l.provideSensor("PSA15");

        l.provideSensor("PSB5");

        l.provideSensor("PSH7");

        l.provideSensor("PSI7");

        l.provideSensor("PSJ7");
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
        // powerline systems require a module letter(?) which
        // isn't provided by makeSystemName();
        Sensor t = l.provideSensor(getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Ignore("ignoring this test due to the system name format, needs to be properly coded")
    @Test
    @ToDo("modify system name format, then remove this overriden test so that the test in the parent class can run")
    public void testUpperLower() {
    }

    @Test
    @Override
    public void testMoveUserName() {
        Sensor t1 = l.provideSensor(getSystemName(getNumToTest1()));
        Sensor t2 = l.provideSensor(getSystemName(getNumToTest2()));
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
        String s1 = l.makeSystemName("B1");
        String s2 = l.makeSystemName("B2");
        testRegisterDuplicateSystemName(l, s1, s2);
    }

    @Override
    @Test
    public void testMakeSystemName() {
        try {
            l.makeSystemName("1");
            Assert.fail("Expected exception not thrown");
        } catch (NamedBean.BadSystemNameException ex) {
            Assert.assertEquals("\"PS1\" is not a recognized format.", ex.getMessage());
        }
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: \"PS1\" is not a recognized format.");
        String s = l.makeSystemName("B1");
        Assert.assertNotNull(s);
        Assert.assertFalse(s.isEmpty());
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // replace the SerialTrafficController to get clean reset
        SerialTrafficController t = new jmri.jmrix.powerline.SerialTrafficController() {
            SerialTrafficController test() {
                return this;
            }
        }.test();
        Assert.assertNotNull("exists", t);

        SerialSystemConnectionMemo m = new SerialSystemConnectionMemo();

        m.setSerialAddress(new SerialAddress(m));

        t.setAdapterMemo(m);

        l = new SerialSensorManager(t) {
            @Override
            public void reply(SerialReply r) {
            }
        };
    }

    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
