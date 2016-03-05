//SRCPClientParserTests.java
package jmri.jmrix.srcp.parser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.srcp.parser package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class SRCPClientParserTests extends TestCase {

    // from here down is testing infrastructure
    public SRCPClientParserTests(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SRCPClientParserTests.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmris.srcp.SRCPClientParserTests");  // no tests in this class itself
        suite.addTest(new TestSuite(SRCPClientParserTokenizerTest.class));
        suite.addTest(new TestSuite(SRCPClientParserTest.class));

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            // put any tests that require a UI here.
        }

        return suite;
    }

}
