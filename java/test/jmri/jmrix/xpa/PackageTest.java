package jmri.jmrix.xpa;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.xpa package
 *
 * @author	Paul Bender Copyright (C) 2012,2016
  */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.xpa.XpaTest");  // no tests in this class itself
        suite.addTest(new TestSuite(XpaMessageTest.class));
        suite.addTest(new TestSuite(XpaTrafficControllerTest.class));
        suite.addTest(new TestSuite(XpaSystemConnectionMemoTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XpaTurnoutTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XpaThrottleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XpaTurnoutManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XpaPowerManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XpaThrottleManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.xpa.serialdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.xpa.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.xpa.swing.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XpaPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        return suite;
    }

}
