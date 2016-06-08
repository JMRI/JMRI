package jmri.jmrix.lenz.liusbethernet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.liusbethernet package
 *
 * @author Paul Bender
 */
public class LIUSBEthernetTest extends TestCase {

    // from here down is testing infrastructure
    public LIUSBEthernetTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LIUSBEthernetTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.liusbethernet.LIUSBEthernetTest");  // no tests in this class itself
        suite.addTest(new TestSuite(LIUSBEthernetAdapterTest.class));
        suite.addTest(new TestSuite(LIUSBEthernetXNetPacketizerTest.class));
        return suite;
    }

}
