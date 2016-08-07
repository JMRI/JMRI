package jmri.jmrix.lenz.liusb;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.liusb package
 *
 * @author Paul Bender
 */
public class LIUSBTest extends TestCase {

    // from here down is testing infrastructure
    public LIUSBTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LIUSBTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.liusb.LIUSBTest");  // no tests in this class itself
        suite.addTest(new TestSuite(LIUSBAdapterTest.class));
        suite.addTest(new TestSuite(LIUSBXNetPacketizerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConnectionConfigTest.class));
        return suite;
    }

}
