package jmri.util.zeroconf;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.util.zeroconf tree
 *
 * @author	Bob Jacobsen Copyright 2003
 * @author Paul Bender Copyright 2014
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
        TestSuite suite = new TestSuite("jmri.util.zeroconf.ZeroConfTest");   // no tests in this class itself

        suite.addTest(new junit.framework.JUnit4TestAdapter(ZeroConfServiceTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ZeroConfClientTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ZeroConfServiceEventTest.class));

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
