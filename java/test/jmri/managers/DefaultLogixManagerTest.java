package jmri.managers;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.managers.DefaultLogixManager class.
 *
 * @author	Bob Jacobsen Copyright (C) 2015
 */
public class DefaultLogixManagerTest extends TestCase {

    public void testCtor() {
        new DefaultLogixManager();
    }

    // from here down is testing infrastructure
    public DefaultLogixManagerTest(String s) {
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
        String[] testCaseName = {"-noloading", DefaultLogixManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultLogixManagerTest.class);
        return suite;
    }

}
