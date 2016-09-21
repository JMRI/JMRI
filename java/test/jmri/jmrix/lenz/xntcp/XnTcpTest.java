package jmri.jmrix.lenz.xntcp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.xntcp package
 *
 * @author Paul Bender
 */
public class XnTcpTest extends TestCase {

    // from here down is testing infrastructure
    public XnTcpTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {XnTcpTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.xntcp.XnTcpTest");  // no tests in this class itself
        suite.addTest(new TestSuite(XnTcpAdapterTest.class));
        suite.addTest(new TestSuite(XnTcpXNetPacketizerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConnectionConfigTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.lenz.xntcp.configurexml.PackageTest.class));
        return suite;
    }

}
