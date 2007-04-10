// UssCtcTest.java

package jmri.jmrit.ussctc;

import junit.framework.*;

/**
 * Tests for classes in the jmri.jmrit.ussctc package
 * @author	Bob Jacobsen  Copyright 2007
 * @version	$Revision: 1.1 $
 */
public class UssCtcTest extends TestCase {


    // from here down is testing infrastructure
    public UssCtcTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {UssCtcTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.ussctc.UssCtcTest");   // no tests in this class itself
        suite.addTest(jmri.jmrit.ussctc.FollowerTest.suite());
        suite.addTest(jmri.jmrit.ussctc.FollowerActionTest.suite());
        suite.addTest(jmri.jmrit.ussctc.OsIndicatorTest.suite());
        suite.addTest(jmri.jmrit.ussctc.OsIndicatorActionTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
