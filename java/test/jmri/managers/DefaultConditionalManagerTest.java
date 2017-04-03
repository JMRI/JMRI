package jmri.managers;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import jmri.Conditional;
import jmri.ConditionalManager;

/**
 * Tests for the jmri.managers.DefaultConditionalManager class.
 *
 * @author	Bob Jacobsen Copyright (C) 2015
 */
public class DefaultConditionalManagerTest extends TestCase {

    public void testCtor() {
        new DefaultConditionalManager();
    }

    public void testCreate() {
        ConditionalManager m = new DefaultConditionalManager();

        Conditional c1 = m.createNewConditional("IX01C01", "");        
        Conditional c2 = m.createNewConditional("IX01C02", "");
        
        Assert.assertFalse(c1 == c2);
        Assert.assertFalse(c1.equals(c2));
        
    }

    public void testUserNameOverlap() {
        ConditionalManager m = new DefaultConditionalManager();

        Conditional c1 = m.createNewConditional("IX01C01", "Foo");        
        Conditional c2 = m.createNewConditional("IX01C02", "Foo");
        
        Assert.assertTrue(c1.getUserName().equals("Foo"));
        Assert.assertTrue(c2.getUserName().equals("Foo"));
        
    }

    // from here down is testing infrastructure
    public DefaultConditionalManagerTest(String s) {
        super(s);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @Override
    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DefaultConditionalManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultConditionalManagerTest.class);
        return suite;
    }

}
