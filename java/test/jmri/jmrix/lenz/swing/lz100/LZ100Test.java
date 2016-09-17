package jmri.jmrix.lenz.swing.lz100;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.swing.lz100 package
 *
 * @author Paul Bender
 */
public class LZ100Test extends TestCase {

    // from here down is testing infrastructure
    public LZ100Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LZ100Test.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.swing.lz100.LZ100Test");  // no tests in this class itself
        suite.addTest(new TestSuite(LZ100FrameTest.class));
        return suite;
    }

}
