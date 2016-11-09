package jmri.jmrix.zimo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.zimo package
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
        TestSuite suite = new TestSuite("jmri.jmrix.zimo.PackageTest");  // no tests in this class itself

        suite.addTest(jmri.jmrix.zimo.swing.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.zimo.mx1.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.zimo.mxulf.PackageTest.class));
        if (!System.getProperty("jmri.headlesstest", System.getProperty("java.awt.headless", "false")).equals("true")) {
        }

        return suite;
    }

}
