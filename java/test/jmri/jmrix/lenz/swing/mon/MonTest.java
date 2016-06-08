package jmri.jmrix.lenz.swing.mon;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.swing.mon package
 *
 * @author Paul Bender
 */
public class MonTest extends TestCase {

    // from here down is testing infrastructure
    public MonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MonTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.swing.mon.MonTest");  // no tests in this class itself
        suite.addTest(new TestSuite(XNetMonPaneTest.class));
        return suite;
    }

}
