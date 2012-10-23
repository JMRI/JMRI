package jmri.jmrix.loconet.sdf;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.sdf package.
 * @author	Bob Jacobsen Copyright 2007
 * @version     $Revision$
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
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.sdf.SdfTest");  // no tests in this class itself
        suite.addTest(InitiateSoundTest.suite());
        suite.addTest(PlayTest.suite());
        suite.addTest(SdfBufferTest.suite());
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PackageTest.class.getName());

}
