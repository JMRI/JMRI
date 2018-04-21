package jmri.jmrix.openlcb;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbTurnoutManager class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 */
public class OlcbTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private static OlcbSystemConnectionMemo m; 

    @Override
    public String getSystemName(int i) {
        return "MTX010203040506070" + i + ";X010203040506070" + (i - 1);
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
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
    public void testUpperLower() {
        Turnout t = l.provide(getSystemName(getNumToTest1()));
        Assert.assertNull(l.getTurnout(t.getSystemName().toLowerCase()));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provide(getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        l = new OlcbTurnoutManager(m);
    }
 
    @BeforeClass
    public static void preClassInit() {
        JUnitUtil.setUp();
        m = OlcbTestInterface.createForLegacyTests();
    }

    @After
    public void tearDown() {
        l.dispose();
    }

    @AfterClass
    public static void postClassTearDown() {
        if(m != null && m.getInterface() !=null ) {
           m.getInterface().dispose();
        }
        JUnitUtil.tearDown();
    } 
}
