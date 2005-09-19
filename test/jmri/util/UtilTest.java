// UtilTest.java

package jmri.util;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.util tree
 *
 * @author	    Bob Jacobsen  Copyright 2003
 * @version         $Revision: 1.2 $
 */
public class UtilTest extends TestCase {

    // from here down is testing infrastructure
    public UtilTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {UtilTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.util.UtilTest");   // no tests in this class itself
        suite.addTest(jmri.util.JSpinnerUtilTest.suite());
        suite.addTest(jmri.util.VectorUtilTest.suite());
        suite.addTest(jmri.util.StringUtilTest.suite());

        return suite;
    }

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
