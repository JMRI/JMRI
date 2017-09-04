package jmri.jmrit.roster.swing.speedprofile;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.roster.swing package
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2012
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
        TestSuite suite = new TestSuite("jmri.jmrit.roster.swing.speedprofile.PackageTest");

        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SpeedProfileFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SpeedProfilePanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SpeedProfileTableTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SpeedProfileActionTest.class));
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
