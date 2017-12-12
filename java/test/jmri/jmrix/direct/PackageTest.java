package jmri.jmrix.direct;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.direct package.
 *
 * @author Bob Jacobsen Copyright 2004
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
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.direct.DirectTest");
        suite.addTest(jmri.jmrix.direct.MakePacketTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.direct.serial.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.direct.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DirectMenuTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ThrottleManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ThrottleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(MessageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.direct.swing.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DirectSystemConnectionMemoTest.class));
        return suite;
    }

}
