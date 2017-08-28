//JmrisTest.java
package jmri.jmris;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Set of tests for the jmri.jmris package
 *
 * @author	Paul Bender Copyright 2010
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
        TestSuite suite = new TestSuite("jmri.jmris.JmrisTest");

        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmris.srcp.SRCPTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmris.simpleserver.PackageTest.class));
        suite.addTest(jmri.jmris.json.PackageTest.suite());
        suite.addTest(jmri.jmris.JmriServerTest.suite());
        suite.addTest(jmri.jmris.JmriConnectionTest.suite());
        suite.addTest(jmri.jmris.ServiceHandlerTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JmriServerFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JmriServerActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ServerMenuTest.class));

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
