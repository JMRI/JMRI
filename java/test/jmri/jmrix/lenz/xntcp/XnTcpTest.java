// XNTCPTest.java
package jmri.jmrix.lenz.xntcp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.xntcp package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class XnTcpTest extends TestCase {

    // from here down is testing infrastructure
    public XnTcpTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {XnTcpTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.xntcp.XnTcpTest");  // no tests in this class itself
        suite.addTest(new TestSuite(XnTcpAdapterTest.class));
        suite.addTest(new TestSuite(XnTcpXNetPacketizerTest.class));
        return suite;
    }

}
