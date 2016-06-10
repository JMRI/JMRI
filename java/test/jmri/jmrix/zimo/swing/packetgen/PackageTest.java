package jmri.jmrix.zimo.swing.packetgen;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.zimo.swing.packetgen package
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
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.zimo.swing.packetgen.PackageTest");  // no tests in this class itself

        suite.addTest(BundleTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
        }

        return suite;
    }

}
