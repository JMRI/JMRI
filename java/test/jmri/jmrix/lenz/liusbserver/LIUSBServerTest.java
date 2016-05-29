package jmri.jmrix.lenz.liusbserver;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.liusbserver package
 *
 * @author Paul Bender
 * @version $Revision: 17977 $
 */
public class LIUSBServerTest extends TestCase {

    // from here down is testing infrastructure
    public LIUSBServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LIUSBServerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.liusbserver.LIUSBServerTest");  // no tests in this class itself
        suite.addTest(new TestSuite(LIUSBServerAdapterTest.class));
        suite.addTest(new TestSuite(LIUSBServerXNetPacketizerTest.class));
        return suite;
    }

}
