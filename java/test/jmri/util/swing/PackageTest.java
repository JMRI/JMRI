// PackageTest.java
package jmri.util.swing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.util.swing tree
 *
 * @author	Bob Jacobsen Copyright 2003
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
        TestSuite suite = new TestSuite("jmri.util.swing.PackageTest");   // no tests in this class itself

        suite.addTest(BundleTest.suite());
        suite.addTest(JmriAbstractActionTest.suite());
        suite.addTest(jmri.util.swing.multipane.PackageTest.suite());
        suite.addTest(jmri.util.swing.sdi.PackageTest.suite());
        suite.addTest(jmri.util.swing.mdi.PackageTest.suite());
        suite.addTest(jmri.util.swing.JCBHandleTest.suite());
        suite.addTest(jmri.util.swing.FontComboUtilTest.suite());
        suite.addTest(jmri.util.swing.GuiUtilBaseTest.suite());

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
