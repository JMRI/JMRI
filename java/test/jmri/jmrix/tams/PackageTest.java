package jmri.jmrix.tams;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.tams package.
 *
 * @author Bob Jacobsen Copyright 2003, 2016
 * @author  Paul Bender Copyright (C) 2016	
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.tams.PackageTest");
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsTurnoutManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.tams.simulator.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.tams.serialdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.tams.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.tams.swing.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsSystemConnectionMemoTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsTrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsConnectionTypeListTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsConstantsTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsMessageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsReplyTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsOpsModeProgrammerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsProgrammerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsProgrammerManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsPowerManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsSensorManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsSensorTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsThrottleManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsThrottleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TamsTurnoutTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }
}
