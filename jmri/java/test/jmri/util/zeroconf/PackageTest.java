// PackageTest.java

package jmri.util.zeroconf;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.util.zeroconf tree
 *
 * @author	    Bob Jacobsen  Copyright 2003
 * @author          Paul Bender   Copyright 2014
 * @version         $Revision: 22233 $
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
        TestSuite suite = new TestSuite("jmri.util.zeroconf.ZeroConfTest");   // no tests in this class itself

        suite.addTest(ZeroConfServiceTest.suite());
        
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
