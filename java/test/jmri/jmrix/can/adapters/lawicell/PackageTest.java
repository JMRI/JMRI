package jmri.jmrix.can.adapters.lawicell;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.adapters.lawicell package.
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class PackageTest extends TestCase {

    public void testDefinitions() {
    }

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
        TestSuite suite = new TestSuite("jmri.jmrix.can.adapters.lawicell");
        suite.addTest(MessageTest.suite());
        suite.addTest(ReplyTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.can.adapters.lawicell.canusb.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialDriverAdapterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LawicellTrafficControllerTest.class));
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
