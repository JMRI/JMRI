// SimpleClockTest.java
package jmri.jmrit.simpleclock;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.simpleclock package
 *
 * @author	Bob Jacobsen
 */
public class SimpleClockTest extends TestCase {

    // from here down is testing infrastructure
    public SimpleClockTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SimpleClockTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.simpleclock.SimpleClockTest"); // no tests in class itself
        suite.addTest(jmri.jmrit.simpleclock.SimpleTimebaseTest.suite());
        return suite;
    }

}
