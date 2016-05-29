/**
 * PackageTest.java
 *
 * Description:	tests for the jmri.implementation.swing package
 *
 * @author	Bob Jacobsen 2009
 */
package jmri.implementation.swing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
        TestSuite suite = new TestSuite("jmri.implementation.swing");   // no tests in this class itself

        suite.addTest(SwingShutDownTaskTest.suite());
        suite.addTest(BundleTest.suite());

        return suite;
    }

}
