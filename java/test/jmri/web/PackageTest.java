// PackageTest.java

package jmri.web;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.web tree
 *
 * @author	    Bob Jacobsen  Copyright 2008
 * @version         $Revision$
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.web.PackageTest");   // no tests in this class itself
        suite.addTest(jmri.web.servlet.PackageTest.suite());
        suite.addTest(jmri.web.xmlio.PackageTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
