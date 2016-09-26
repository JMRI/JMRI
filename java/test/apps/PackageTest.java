// PackageTest.java
package apps;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the apps package
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007, 2012
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
        TestSuite suite = new TestSuite("apps.PackageTest");  // no tests in this class itself

        suite.addTest(BundleTest.suite());
        suite.addTest(ConfigBundleTest.suite());
        suite.addTest(ValidateConfigFilesTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(apps.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(apps.startup.PackageTest.class));
        suite.addTest(apps.PacketPro.PackageTest.suite());
        suite.addTest(apps.PacketScript.PackageTest.suite());
        suite.addTest(apps.InstallTest.PackageTest.suite());
        suite.addTest(apps.gui3.Gui3AppsTest.suite());
        suite.addTest(apps.DecoderPro.PackageTest.suite());
        suite.addTest(apps.JmriDemo.PackageTest.suite());
        suite.addTest(apps.DispatcherPro.PackageTest.suite());
        suite.addTest(apps.PanelPro.PackageTest.suite());
        suite.addTest(apps.SignalPro.PackageTest.suite());
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
