package jmri.jmrix.internal;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.internal.InternalTurnoutManager class.
 *
 * @author	Bob Jacobsen Copyright 2016
 */
public class InternalTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "IT" + i;
    }

    @Test
    public void testAsAbstractFactory() {

        // ask for a Turnout, and check type
        Turnout tl = l.newTurnout("IT21", "my name");

        Assert.assertTrue(null != tl);

        // make sure loaded into tables
        Assert.assertTrue(null != l.getBySystemName("IT21"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    @Test
    public void testOutputInterval() {
        Assert.assertEquals(0, l.getOutputInterval("IT1")); // IT1 need not exist, only the prefix is used to find manager
        l.setOutputInterval(50);
        Assert.assertEquals(0, l.getOutputInterval("IT1")); // IT1 need not exist, only the prefix is used to find manager, interval not stored in InternalTurnoutManager
    }

    // from here down is testing infrastructure
    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        // create and register the manager object
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        l = jmri.InstanceManager.turnoutManagerInstance();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
