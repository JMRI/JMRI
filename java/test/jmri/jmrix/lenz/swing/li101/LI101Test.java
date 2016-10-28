package jmri.jmrix.lenz.swing.li101;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.swing.li101 package
 *
 * @author Paul Bender
 */
public class LI101Test extends TestCase {

    // from here down is testing infrastructure
    public LI101Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LI101Test.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.swing.li101.LI101Test");  // no tests in this class itself
        suite.addTest(new TestSuite(LI101FrameTest.class));
        return suite;
    }

}
