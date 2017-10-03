package jmri.jmrix.lenz.liusbserver;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.liusbserver package
 *
 * @author Paul Bender
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
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.liusbserver");  // no tests in this class itself
        suite.addTest(new TestSuite(LIUSBServerAdapterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LIUSBServerXNetPacketizerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConnectionConfigTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.lenz.liusbserver.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        return suite;
    }

}
