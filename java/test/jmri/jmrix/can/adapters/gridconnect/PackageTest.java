package jmri.jmrix.can.adapters.gridconnect;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect package.
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
        TestSuite suite = new TestSuite("jmri.jmrix.can.adapters.gridconnect");
        suite.addTest(GridConnectMessageTest.suite());
        suite.addTest(GridConnectReplyTest.suite());
        suite.addTest(jmri.jmrix.can.adapters.gridconnect.canrs.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.can.adapters.gridconnect.lccbuffer.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.can.adapters.gridconnect.net.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.can.adapters.gridconnect.canusb.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.can.adapters.gridconnect.can2usbino.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(GcPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(GcTrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(GcSerialDriverAdapterTest.class));
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
