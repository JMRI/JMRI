package jmri.jmrix.ecos.swing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.ecos.swing package
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
        TestSuite suite = new TestSuite("jmri.jmrix.ecos.swing.PackageTest");  // no tests in this class itself
        suite.addTest(BundleTest.suite());

        suite.addTest(jmri.jmrix.ecos.swing.locodatabase.PackageTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
        }

        return suite;
    }

}
