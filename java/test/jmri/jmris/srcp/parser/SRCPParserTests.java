//SRCPParserTests.java
package jmri.jmris.srcp.parser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.srcp.parser package
 *
 * @author Paul Bender
 */
public class SRCPParserTests extends TestCase {

    // from here down is testing infrastructure
    public SRCPParserTests(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SRCPParserTests.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmris.srcp.SRCPParserTests");  // no tests in this class itself
        suite.addTest(new TestSuite(SRCPTokenizerTest.class));
        suite.addTest(new TestSuite(SRCPParserTest.class));
        suite.addTest(new TestSuite(SRCPVisitorTest.class));

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            // put any tests that require a UI here.
        }

        return suite;
    }

}
