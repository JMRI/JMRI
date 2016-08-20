package jmri.jmrix.roco.z21.swing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.roco.z21.swing package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.roco.z21.swing");  // no tests in this class itself
        suite.addTest(jmri.jmrix.roco.z21.swing.configtool.PackageTest.suite());
        suite.addTest(BundleTest.suite());

        return suite;
    }

}
