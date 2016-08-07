package jmri.jmrix.lenz.li100;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.li100 package
 *
 * @author Paul Bender
 */
public class LI100Test extends TestCase {

    // from here down is testing infrastructure
    public LI100Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LI100Test.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.li100.LI100Test");  // no tests in this class itself
        suite.addTest(new TestSuite(LI100AdapterTest.class));
        suite.addTest(new TestSuite(LI100XNetInitializationManagerTest.class));
        suite.addTest(new TestSuite(LI100XNetProgrammerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConnectionConfigTest.class));
        return suite;
    }

}
