package jmri.jmrix.loconet.hexfile;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.hexfile package.
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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.hexfile.LocoStatsTest");  // no tests in this class itself
        suite.addTest(BundleTest.suite());
        return suite;
    }

}
