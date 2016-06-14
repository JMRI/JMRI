package jmri.jmrix.lenz.swing.stackmon;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.swing.stackmon package
 *
 * @author Paul Bender
 */
public class StackMonTest extends TestCase {

    // from here down is testing infrastructure
    public StackMonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {StackMonTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.swing.stackmon.StackMonTest");  // no tests in this class itself
        suite.addTest(new TestSuite(StackMonFrameTest.class));
        return suite;
    }

}
