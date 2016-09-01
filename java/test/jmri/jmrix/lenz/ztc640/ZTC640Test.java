package jmri.jmrix.lenz.ztc640;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.ztc640 package
 *
 * @author Paul Bender
 */
public class ZTC640Test extends TestCase {

    // from here down is testing infrastructure
    public ZTC640Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ZTC640Test.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.ztc640.ZTC640Test");  // no tests in this class itself
        suite.addTest(new TestSuite(ZTC640AdapterTest.class));
        suite.addTest(new TestSuite(ZTC640XNetPacketizerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConnectionConfigTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.lenz.ztc640.configurexml.PackageTest.class));
        return suite;
    }

}
