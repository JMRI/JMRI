package jmri.jmrix.lenz.li100f;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.li100f package
 *
 * @author Paul Bender
 */
public class LI100FTest extends TestCase {

    // from here down is testing infrastructure
    public LI100FTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LI100FTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.li100f.LI100FTest");  // no tests in this class itself
        suite.addTest(new TestSuite(LI100AdapterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConnectionConfigTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.lenz.li100f.configurexml.PackageTest.class));
        return suite;
    }

}
