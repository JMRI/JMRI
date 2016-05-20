// PackageTest.java

package jmri.jmrit.logix;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.jmrit.logix tree
 *
 * @author	    Bob Jacobsen  Copyright 2010
 * @version         $Revision$
 */
public class PackageTest extends TestCase {
    
    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.logix.PackageTest");   // no tests in this class itself

        suite.addTest(OPathTest.suite());

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
