package jmri.jmrit.ussctc;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for classes in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
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
        TestSuite suite = new TestSuite("jmri.jmrit.ussctc.PackageTest");   // no tests in this class itself
        suite.addTest(jmri.jmrit.ussctc.FollowerTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.ussctc.FollowerActionTest.class));
        suite.addTest(jmri.jmrit.ussctc.OsIndicatorTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.ussctc.OsIndicatorActionTest.class));
        suite.addTest(new JUnit4TestAdapter(BasePanelTest.class));
        suite.addTest(new JUnit4TestAdapter(FollowerFrameTest.class));
        suite.addTest(new JUnit4TestAdapter(FollowerPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(OsIndicatorFrameTest.class));
        suite.addTest(new JUnit4TestAdapter(OsIndicatorPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(ToolsMenuTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
