package jmri.jmrix.can.adapters.gridconnect.canrs;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.canrs package.
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.can.adapters.gridconnect.canrs");
        suite.addTest(MergMessageTest.suite());
        suite.addTest(MergReplyTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(MergTrafficControllerTest.class));
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
