package jmri.jmrit.ussctc;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for classes in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class UssCtcTest extends TestCase {

    // from here down is testing infrastructure
    public UssCtcTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", UssCtcTest.class.getName()};
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
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
