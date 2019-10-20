package jmri.jmrix.powerline;

import jmri.NamedBean;
import jmri.Turnout;
import jmri.jmrix.powerline.simulator.SpecificSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import java.beans.PropertyVetoException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SerialTurnoutManagerTest.java
 *
 * Description: tests for the SerialTurnoutManager class
 *
 * @author Bob Jacobsen Copyright 2004, 2008 Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private SerialTrafficControlScaffold nis = null;

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SpecificSystemConnectionMemo memo = new SpecificSystemConnectionMemo();
        // prepare an interface, register
        nis = new SerialTrafficControlScaffold();
        nis.setAdapterMemo(memo);
        memo.setTrafficController(nis);
        memo.setSerialAddress(new SerialAddress(memo));
        // create and register the manager object
        l = new SerialTurnoutManager(nis);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @Override
    public String getSystemName(int n) {
        return "PTB" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("PTB1", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        Assert.assertTrue(null != (SerialTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("PTB1"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("PTB1"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    @Override
    @Test
    public void testProvideName() {
        // create
        Turnout t = l.provide(getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout(getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testUpperLower() {
        Turnout t = l.provideTurnout(getSystemName(getNumToTest2()));

        Assert.assertNull(l.getTurnout(t.getSystemName().toLowerCase()));
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
            Assert.assertEquals("\"PT1\" is not a recognized format.", ex.getMessage());
        }
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: \"PT1\" is not a recognized format.");
        String s = l.makeSystemName("B1");
        Assert.assertNotNull(s);
        Assert.assertFalse(s.isEmpty());
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManagerTest.class);

}
