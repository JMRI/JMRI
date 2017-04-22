package jmri.jmrix.pricom;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.pricom package.
 *
 * @author Bob Jacobsen Copyright 2005
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
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.pricom.PricomTest");
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.pricom.pockettester.PackageTest.class));
        suite.addTest(jmri.jmrix.pricom.downloader.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(PricomMenuTest.class));
        return suite;
    }

}
