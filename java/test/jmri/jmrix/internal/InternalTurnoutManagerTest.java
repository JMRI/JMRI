package jmri.jmrix.internal;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;
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
    public void testCaseMatters() {
        java.util.ArrayList<NamedBean> list = new  java.util.ArrayList<>();

        NamedBean tn1a = l.provide("name1");
        Assert.assertTrue(tn1a != null);
        list.add(tn1a);

        // a test with specific system prefix attached (could get from this.getSystemName(1))
        NamedBean tn1b = l.provide("ITname1"); // meant to be same, note type-specific
        Assert.assertTrue(tn1a != null);
        list.add(tn1b);
        Assert.assertEquals("tn1a and tn1b didn't match", tn1a, tn1b);

        // case is checked
        NamedBean tN1 = l.provide("NAME1");
        Assert.assertTrue(tn1a != null);
        list.add(tN1);
        Assert.assertTrue("tn1a doesn't match tN1, case not handled right", tn1a != tN1);

        // spaces fine, kept
        NamedBean tSpaceM  = l.provide("NAME 1");
        Assert.assertFalse("tSPaceM not unique", list.contains(tSpaceM));
        Assert.assertTrue(tSpaceM != null);
        list.add(tSpaceM);

        NamedBean tSpaceMM = l.provide("NAME  1");
        Assert.assertFalse("tSpaceMM not unique", list.contains(tSpaceMM));
        Assert.assertTrue(tSpaceMM != null);
        list.add(tSpaceMM);

        NamedBean tSpaceE  = l.provide("NAME 1 ");
        Assert.assertFalse("tSpaceE not unique", list.contains(tSpaceE));
        Assert.assertTrue(tSpaceE != null);
        list.add(tSpaceE);

        NamedBean tSpaceEE  = l.provide("NAME 1  ");
        Assert.assertFalse("tSpaceEE not unique", list.contains(tSpaceEE));
        Assert.assertTrue(tSpaceEE != null);
        list.add(tSpaceEE);

        NamedBean tSpaceLEE  = l.provide(" NAME 1  ");
        Assert.assertFalse("tSpaceLEE not unique", list.contains(tSpaceLEE));
        Assert.assertTrue(tSpaceLEE != null);
        list.add(tSpaceLEE);

        NamedBean tSpaceLLEE  = l.provide("  NAME 1  ");
        Assert.assertFalse("tSpaceLLEE not unique", list.contains(tSpaceLLEE));
        Assert.assertTrue(tSpaceLLEE != null);
        list.add(tSpaceLLEE);
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
