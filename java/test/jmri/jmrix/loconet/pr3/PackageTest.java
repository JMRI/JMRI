package jmri.jmrix.loconet.pr3;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.pr3 package.
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2006, 2008
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.pr3.LocoStatsTest");  // no tests in this class itself
        suite.addTest(BundleTest.suite());
        return suite;
    }

}
